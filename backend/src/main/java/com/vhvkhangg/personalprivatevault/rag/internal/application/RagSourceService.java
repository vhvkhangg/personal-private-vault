package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.source.FictionSourceOperations;
import com.vhvkhangg.personalprivatevault.film.source.FilmSourceOperations;
import com.vhvkhangg.personalprivatevault.rag.api.RagConsentCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.vault.entry.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.*;

/** Standing item consent and owner intent, independent of worker credentials. */
@Service
@Transactional(readOnly = true)
public class RagSourceService {
    private final RagSourceRepository sources;
    private final RagDependencyRepository dependencies;
    private final RagDependencyGraph graph;
    private final RagPurgeService purge;
    private final FictionSourceOperations fiction;
    private final FilmSourceOperations film;
    private final VaultEntryOperations vault;
    private final SourceIndexReservations reservations;
    private final SourceEditGuard edits;
    private final EntityManager entityManager;
    private final RagRuntimeIdentity runtime;
    private final Clock clock;
    private final int maxDepth;
    private final RagLimits limits;
    private final TransactionTemplate transaction;

    public RagSourceService(RagSourceRepository sources, RagDependencyRepository dependencies, RagDependencyGraph graph,
            RagPurgeService purge, FictionSourceOperations fiction, FilmSourceOperations film, VaultEntryOperations vault,
            SourceIndexReservations reservations, SourceEditGuard edits, EntityManager entityManager,
            RagRuntimeIdentity runtime, Clock clock, RagLimits limits, PlatformTransactionManager transactions,
            @Value("${ppv.rag.limits.max-dependency-depth:5}") int maxDepth) {
        this.sources = sources; this.dependencies = dependencies; this.graph = graph; this.purge = purge;
        this.fiction = fiction; this.film = film; this.vault = vault; this.reservations = reservations;
        this.edits = edits; this.entityManager = entityManager; this.runtime = runtime; this.clock = clock;
        if (maxDepth < 1 || maxDepth > 8) throw new IllegalArgumentException("Invalid dependency depth budget");
        this.maxDepth = maxDepth; this.limits = limits;
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setTimeout(10);
    }

    public RagSourceView registerFileCandidate(String path) {
        throw new IllegalStateException("Live external-file capability is disabled until namespace and metadata authority are proven");
    }

    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView registerDomainCandidate(RagSourceType type, String sourceId) {
        return registerDomainCandidate(type, sourceId, RagClassification.UNKNOWN, null, null);
    }

    /** Explicit classification config uses the existing registration action; it never grants participation. */
    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView registerDomainCandidate(RagSourceType type, String sourceId, RagClassification classification,
            Long expectedEpoch, Integer expectedPolicy) {
        if (type == null || type == RagSourceType.EXTERNAL_FILE || classification == null)
            throw new IllegalArgumentException("Unsupported domain source");
        long ownerId = Long.parseLong(sourceId);
        if (ownerId <= 0) throw new IllegalArgumentException("Invalid source identifier");
        var ids = new TreeSet<>(Set.of(ownerId));
        var captured = reservations.read(ids).require(ownerId);
        if (!type.name().equals(captured.sourceType())) throw new IllegalArgumentException("Source type mismatch");
        // No content hashing under source/graph SQL guards. Version CAS detects intervening mutations.
        var revision = classification == RagClassification.NON_PRIVATE ? revision(type, ownerId) : new Revision("UNCONFIRMED", "UNCONFIRMED");
        return transaction.execute(status -> {
            var topology = graph.lockAndRead();
            var existing = sources.findBySourceTypeAndSourceId(type, Long.toString(ownerId));
            var affected = existing.map(s -> affected(topology, s.getId())).orElseGet(TreeSet::new);
            guardOwners(affected, ids);
            var current = reservations.read(ids).require(ownerId);
            if (current.sourceVersion() != captured.sourceVersion()) throw new RagSourceChangedException();
            RagSource source;
            if (existing.isPresent()) {
                source = lockSources(affected).get(existing.get().getId());
                if (source.getClassification() != classification) {
                    if (!Objects.equals(expectedEpoch, source.getAuthorizationEpoch())
                            || !Objects.equals(expectedPolicy, source.getFieldPolicyVersion())) throw new RagSourceChangedException();
                    source.configureClassification(classification, clock.instant());
                    sources.saveAndFlush(source);
                    for (Long id : affected) purge.purgeSourceDerivatives(id);
                }
            } else {
                if (sources.count() >= limits.catalogSources()) throw new IllegalStateException("Source catalog budget exceeded");
                source = sources.saveAndFlush(new RagSource(type, Long.toString(ownerId), classification, 1, 1, Set.of(),
                        RagAccessState.DENIED, revision.content(), "UNCONFIRMED", clock.instant()));
            }
            return toView(source, current, revision.description());
        });
    }

    /** Legacy fields-only commands cannot acquire fresh consent. */
    public RagSourceView approveSource(Long id, Set<String> fields) {
        throw new IllegalArgumentException("Explicit source and consent versions are required");
    }

    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView approveSource(Long sourceId, RagConsentCommand command) {
        Objects.requireNonNull(command);
        var initial = sources.findById(sourceId).orElseThrow();
        if (initial.getSourceType() == RagSourceType.EXTERNAL_FILE) throw new IllegalStateException("Live external-file capability is disabled");
        RagFieldAllowlist.validateApprovedFields(initial.getSourceType(), command.approvedFields());
        if (initial.getClassification() != RagClassification.NON_PRIVATE) throw new IllegalStateException("Private or unclassified source cannot be approved");
        long ownerId = Long.parseLong(initial.getSourceId());
        var projection = revision(initial.getSourceType(), ownerId);
        if (command.approvedFields().contains("description")
                && !Objects.equals(command.confirmedDescriptionHash(), projection.description())) throw new RagSourceChangedException();
        return transaction.execute(status -> {
            var topology = graph.lockAndRead();
            var affected = affected(topology, sourceId);
            var ids = new TreeSet<>(Set.of(ownerId));
            guardOwners(affected, ids);
            var owner = reservations.read(ids).require(ownerId);
            var source = lockSources(affected).get(sourceId);
            if (owner.deleted() || owner.sourceVersion() != command.expectedSourceVersion()
                    || owner.consentResetVersion() != command.expectedConsentResetVersion()
                    || source.getAuthorizationEpoch() != command.expectedAuthorizationEpoch()
                    || source.getFieldPolicyVersion() != command.expectedFieldPolicyVersion()) throw new RagSourceChangedException();
            if (source.getClassification() != RagClassification.NON_PRIVATE) throw new IllegalStateException("Private or unclassified source cannot be approved");
            for (Long id : affected) purge.purgeSourceDerivatives(id);
            String description = command.approvedFields().contains("description") ? projection.description() : source.getDescriptionRevisionHash();
            source.approve(command.approvedFields(), projection.content(), description, clock.instant());
            source.bindOwnerResetVersion(owner.consentResetVersion());
            // Execution SQL remains boot-scoped; standing consent no longer depends on this legacy execution field.
            source.bindApprovalInstance(runtime.instance());
            sources.saveAndFlush(source);
            return toView(source, owner, projection.description());
        });
    }

    @Transactional
    public RagSourceView revokeSource(Long sourceId) {
        var topology = graph.lockAndRead();
        var affected = affected(topology, sourceId);
        guardOwners(affected, new TreeSet<>());
        var source = lockSources(affected).get(sourceId);
        source.revoke(clock.instant());
        sources.saveAndFlush(source);
        for (Long id : affected) purge.purgeSourceDerivatives(id);
        return toView(source, reservations.read(new TreeSet<>(Set.of(Long.valueOf(source.getSourceId())))).require(Long.parseLong(source.getSourceId())), "");
    }

    @Transactional
    public void addDependency(Long parent, Long child) {
        Objects.requireNonNull(parent); Objects.requireNonNull(child);
        var topology = graph.lockAndRead();
        if (parent.equals(child) || topology.descendants(child).contains(parent)) throw new IllegalStateException("Cyclic dependency detected");
        if (dependencies.findByParentSourceIdAndChildSourceId(parent, child).isPresent()) return;
        if (topology.ancestorDepth(parent) + topology.descendantDepth(child) > maxDepth) throw new IllegalArgumentException("Dependency depth exceeds maximum allowed limit");
        var affected = affected(topology, parent);
        guardOwners(affected, new TreeSet<>());
        var all = new TreeSet<>(affected); all.add(child);
        var locked = lockSources(all);
        for (Long root : affected) {
            var descendants = new HashSet<>(topology.descendants(root));
            descendants.add(child); descendants.addAll(topology.descendants(child));
            if (descendants.size() >= limits.selectedSources()) throw new IllegalStateException("Dependency count budget exceeded");
        }
        dependencies.saveAndFlush(new RagDependency(parent, child, clock.instant()));
        for (Long root : affected) {
            // SQL trigger is the canonical affected-ancestor version/epoch backstop.
            entityManager.refresh(locked.get(root));
            purge.purgeSourceDerivatives(root);
        }
    }

    private void guardOwners(Collection<Long> ragIds, SortedSet<Long> additional) {
        var ids = new TreeSet<>(additional);
        for (var source : sources.findAllById(ragIds)) {
            if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) throw new IllegalStateException("Live external-file capability is disabled");
            ids.add(Long.valueOf(source.getSourceId()));
        }
        if (!ids.isEmpty()) vault.acquireSourceGuards(ids);
    }

    private static SortedSet<Long> affected(RagDependencyGraph.Snapshot topology, Long source) {
        var ids = new TreeSet<>(topology.ancestors(source)); ids.add(source); return ids;
    }

    private Map<Long, RagSource> lockSources(Collection<Long> ids) {
        var locked = sources.findAllByIdInForUpdate(ids);
        if (locked.size() != ids.size()) throw new NoSuchElementException("Source authority unavailable");
        var map = new HashMap<Long, RagSource>();
        for (var source : locked) { entityManager.refresh(source); map.put(source.getId(), source); }
        return map;
    }

    private record Revision(String content, String description) {}

    private Revision revision(RagSourceType type, long id) {
        if (type == RagSourceType.FICTION) {
            var p = fiction.findSource(id).orElseThrow(() -> new IllegalStateException("Source is ineligible (NSFW or deleted)"));
            if (!p.isEligible()) throw new IllegalStateException("Source is ineligible (NSFW or deleted)");
            return new Revision(p.contentRevisionHash(), p.descriptionRevisionHash());
        }
        var p = film.findSource(id).orElseThrow(() -> new IllegalStateException("Source is ineligible (NSFW or deleted)"));
        if (!p.isEligible()) throw new IllegalStateException("Source is ineligible (NSFW or deleted)");
        return new Revision(p.contentRevisionHash(), p.descriptionRevisionHash());
    }

    public static boolean hasRestrictiveMetadata(String content) {
        if (content == null || content.isBlank()) return true;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ".,!?'-".indexOf(ch) >= 0))
                return true;
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t') return true;
        }
        return content.lines().anyMatch(line -> line.stripLeading().startsWith("---"));
    }


    public Optional<RagSourceView> findSource(Long id) {
        return id == null ? Optional.empty() : sources.findById(id).map(this::preview);
    }

    public List<RagSourceView> listSources(int offset, int limit) {
        return entityManager.createQuery("SELECT s FROM RagSource s ORDER BY s.id", RagSource.class)
                .setFirstResult(Math.max(0, offset)).setMaxResults(Math.clamp(limit, 1, 100)).getResultList().stream()
                .map(source -> viewWithoutContent(source)).toList();
    }

    private RagSourceView preview(RagSource source) {
        if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) return externalView(source);
        long id = Long.parseLong(source.getSourceId());
        var owner = reservations.read(new TreeSet<>(Set.of(id))).require(id);
        String description = "";
        if (!owner.deleted() && source.getClassification() == RagClassification.NON_PRIVATE) {
            var optional = source.getSourceType() == RagSourceType.FICTION
                    ? fiction.findSource(id).filter(p -> p.isEligible()).map(p -> p.descriptionRevisionHash())
                    : film.findSource(id).filter(p -> p.isEligible()).map(p -> p.descriptionRevisionHash());
            description = optional.orElse("");
        }
        return toView(source, owner, description);
    }

    private RagSourceView viewWithoutContent(RagSource source) {
        if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) return externalView(source);
        long id = Long.parseLong(source.getSourceId());
        return toView(source, reservations.read(new TreeSet<>(Set.of(id))).require(id), "");
    }

    private RagSourceView externalView(RagSource source) {
        return new RagSourceView(source.getId(), source.getSourceType(), "EXTERNAL_DISABLED", source.getClassification(),
                source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), Set.of(), RagAccessState.REVOKED,
                "DISABLED", "DISABLED", source.getCreatedAt(), source.getUpdatedAt(), source.getRevokedAt(), 0, 0, "");
    }

    private RagSourceView toView(RagSource source, SourceVersion owner, String currentDescription) {
        var access = owner.deleted() || (source.getAccessState() == RagAccessState.APPROVED
                && !Objects.equals(source.getApprovedOwnerResetVersion(), owner.consentResetVersion()))
                ? RagAccessState.REVOKED : source.getAccessState();
        return new RagSourceView(source.getId(), source.getSourceType(), source.getSourceId(), source.getClassification(),
                source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), source.getApprovedFields(), access,
                source.getSourceRevisionHash(), source.getDescriptionRevisionHash(), source.getCreatedAt(),
                source.getUpdatedAt(), source.getRevokedAt(), owner.sourceVersion(), owner.consentResetVersion(), currentDescription);
    }
}
