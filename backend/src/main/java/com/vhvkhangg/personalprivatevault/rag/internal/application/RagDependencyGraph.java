package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDependency;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A bounded graph snapshot under a transaction-scoped RAG topology barrier.
 */
@Component
public class RagDependencyGraph {
    public static final long LOCK_ID = 0x5241475F44455047L;
    private final EntityManager entityManager;
    private final RagLimits limits;

    public RagDependencyGraph(EntityManager entityManager, RagLimits limits) {
        this.entityManager = entityManager;
        this.limits = limits;
    }

    public Snapshot lockAndRead() {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
            || org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("RAG graph requires a writable caller transaction");
        }
        entityManager.createNativeQuery("SET LOCAL lock_timeout = '" + limits.lockMillis() + "ms'").executeUpdate();
        entityManager.createNativeQuery("SET LOCAL statement_timeout = '" + limits.statementMillis() + "ms'").executeUpdate();
        entityManager.createNativeQuery("SET LOCAL transaction_timeout = '" + limits.transactionSeconds() + "s'").executeUpdate();
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:id)").setParameter("id", LOCK_ID).getSingleResult();
        return read();
    }

    /** Optimistic preparation only; callers must recheck topology/epochs under lock before admission. */
    public Snapshot read() {
        List<RagDependency> edges = entityManager.createQuery(
                "SELECT d FROM RagDependency d ORDER BY d.parentSourceId, d.childSourceId", RagDependency.class)
            .setMaxResults(limits.graphEdges() + 1).getResultList();
        if (edges.size() > limits.graphEdges()) throw new IllegalStateException("RAG graph edge budget exceeded");
        Map<Long, Set<Long>> forward = new TreeMap<>();
        Map<Long, Set<Long>> reverse = new TreeMap<>();
        for (RagDependency edge : edges) {
            forward.computeIfAbsent(edge.getParentSourceId(), ignored -> new TreeSet<>()).add(edge.getChildSourceId());
            reverse.computeIfAbsent(edge.getChildSourceId(), ignored -> new TreeSet<>()).add(edge.getParentSourceId());
        }
        return new Snapshot(forward, reverse);
    }

    public record Snapshot(Map<Long, Set<Long>> forward, Map<Long, Set<Long>> reverse) {
        public Snapshot {
            forward = immutable(forward);
            reverse = immutable(reverse);
        }

        private static Map<Long, Set<Long>> immutable(Map<Long, Set<Long>> map) {
            Map<Long, Set<Long>> result = new TreeMap<>();
            map.forEach((key, value) -> result.put(key, Set.copyOf(value)));
            return Collections.unmodifiableMap(result);
        }

        public List<Long> descendants(Long root) {
            return closure(root, forward);
        }

        public List<Long> ancestors(Long root) {
            return closure(root, reverse);
        }

        private static List<Long> closure(Long root, Map<Long, Set<Long>> edges) {
            Set<Long> found = new TreeSet<>();
            Deque<Long> pending = new ArrayDeque<>();
            pending.add(root);
            while (!pending.isEmpty()) {
                for (Long child : edges.getOrDefault(pending.removeFirst(), Set.of())) {
                    if (child.equals(root)) throw new IllegalStateException("Cyclic dependency");
                    if (found.add(child)) {
                        if (found.size() > 100) throw new IllegalStateException("Dependency count budget exceeded");
                        pending.addLast(child);
                    }
                }
            }
            // Memoized longest paths also detect cycles that do not include the root.
            depth(root, edges, new HashMap<>(), new HashSet<>());
            return List.copyOf(found);
        }

        public int descendantDepth(Long root) {
            return depth(root, forward, new HashMap<>(), new HashSet<>());
        }

        public int ancestorDepth(Long root) {
            return depth(root, reverse, new HashMap<>(), new HashSet<>());
        }

        private static int depth(Long id, Map<Long, Set<Long>> edges, Map<Long, Integer> memo, Set<Long> active) {
            Integer known = memo.get(id);
            if (known != null) return known;
            if (!active.add(id)) throw new IllegalStateException("Cyclic dependency");
            if (active.size() > 8) throw new IllegalStateException("Dependency depth budget exceeded");
            int result = 1;
            for (Long child : edges.getOrDefault(id, Set.of())) {
                result = Math.max(result, 1 + depth(child, edges, memo, active));
            }
            active.remove(id);
            memo.put(id, result);
            return result;
        }
    }
}
