package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;

/** Existing-row C_N history and exact retained-generation proof; never an indexing job. */
@Service
public class RagNoChangeService {
    public record Proof(RagSourceAuthority.Prepared source, UUID generation) {}
    private final RagSourceAuthority authority;
    private final RagGenerationState current;
    private final RagIndexGenerationRepository generations;
    private final RagGenerationRootRepository roots;
    private final RagJobLeaseRepository leases;
    private final RagSourceRepository sources;
    private final RagExternalFileAuthority external;
    private final SourceIndexReservations vault;
    private final RagRunRepository runs;
    private final RagRuntimeIdentity runtime;
    private final RagRuntimeSecurity security;
    private final EntityManager em;
    private final TransactionTemplate tx;
    private final Set<UUID> cancelledOperations=java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Set<UUID> stoppedOperations=java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final ObjectMapper json=new ObjectMapper();

    public RagNoChangeService(RagSourceAuthority authority,RagGenerationState current,
            RagIndexGenerationRepository generations,RagGenerationRootRepository roots,
            RagJobLeaseRepository leases,RagSourceRepository sources,RagExternalFileAuthority external,
            SourceIndexReservations vault,RagRunRepository runs,RagRuntimeIdentity runtime,RagRuntimeSecurity security,
            EntityManager em,PlatformTransactionManager transactions) {
        this.authority=authority;this.current=current;this.generations=generations;this.roots=roots;
        this.leases=leases;this.sources=sources;this.external=external;this.vault=vault;this.runs=runs;
        this.runtime=runtime;this.security=security;this.em=em;tx=new TransactionTemplate(transactions);tx.setTimeout(10);
    }

    /** Integrity and currentness preparation, including child closure, occurs outside guards. */
    public Proof prepare(Long source) {
        var prepared=authority.prepareSource(source,Map.of());
        RagDocumentInput.validateClosure(prepared);
        for (var p:prepared.proofs().values()) if ("EXTERNAL_FILE".equals(p.type())) {
            var ext=external.findByFileSourceId(UUID.fromString(p.sourceIdentity()));
            if (ext==null || ext.getSelectedRevisionId()==null || ext.getPendingRevisionId()!=null)
                throw new IllegalStateException("NO_CHANGE requires selected revisions without pending replacement");
        }
        var candidates=generations.findBySourceId(source).stream().filter(RagIndexGeneration::isActive).toList();
        if(candidates.size()==1 && !current.isPipelineCurrent(roots.findById(candidates.getFirst().getId()).orElse(null)))throw new IllegalStateException("Reindex required: pipeline outdated");
        if (candidates.size()!=1 || !"CURRENT".equals(current.state(candidates.getFirst().getGenerationId())))
            throw new IllegalStateException("Authorized revision resolution or reindex required");
        return new Proof(prepared,candidates.getFirst().getGenerationId());
    }

    /** Caller holds the complete source-first proof guards through physical finish. No file I/O. */
    public void validateLocked(Proof proof) {
        var gen=generations.findByGenerationId(proof.generation()).orElseThrow();
        em.refresh(gen,jakarta.persistence.LockModeType.PESSIMISTIC_READ);
        var root=roots.findById(gen.getId()).orElseThrow();
        em.refresh(root,jakarta.persistence.LockModeType.PESSIMISTIC_READ);
        var lease=leases.findByJobId(gen.getJobId()).getFirst();em.refresh(lease);
        String manifest=proof.source().manifest();
        if (!gen.getSourceId().equals(proof.source().rootId()) || !gen.isActive() || !"PUBLISHED".equals(gen.getStatus()) || !current.isPipelineCurrent(root)
                || !"COMPLETED".equals(lease.getStatus()) || !lease.isStagingClosed()
                || !manifest.equals(root.getExactDependencyManifest().get("manifest_text"))
                || !RagSourceAuthority.digest(manifest).equals(gen.getDependencyManifestDigest()))
            throw new IllegalStateException("Retained generation is not eligible for NO_CHANGE");
        for (var p:proof.source().proofs().values()) if ("EXTERNAL_FILE".equals(p.type())) {
            var ext=external.findByFileSourceId(UUID.fromString(p.sourceIdentity()));em.refresh(ext);
            if (ext.getSelectedRevisionId()==null || ext.getPendingRevisionId()!=null
                    || !ext.getSelectedRevisionId().toString().equals(p.snapshot().get("revision_id")))
                throw new IllegalStateException("Selected revision changed before NO_CHANGE");
        }
    }

    public UUID begin(Map<String,Proof> proofs,UUID capture) {
        UUID operation=UUID.randomUUID();
        var metadata=new TreeMap<String,Object>();metadata.put("kind","NONPUBLICATION");
        metadata.put("disposition","PENDING");metadata.put("runtime_id",runtime.instance().toString());
        metadata.put("controller_pid",ProcessHandle.current().pid());
        metadata.put("controller_started_at",ProcessHandle.current().info().startInstant().orElseThrow().toString());
        if(capture!=null)metadata.put("capture_id",capture.toString());
        var externalIds=new TreeSet<String>();var owners=new TreeSet<Long>();var sourceIds=new TreeSet<Long>();
        var roster=new ArrayList<Map<String,Object>>();
        proofs.forEach((key,p) -> {
            roster.add(Map.of("root_key",key,"source_id",p.source().rootId(),"generation_id",p.generation().toString(),"manifest",p.source().manifest()));
            p.source().proofs().forEach((id,node) -> {sourceIds.add(id);if("EXTERNAL_FILE".equals(node.type()))externalIds.add(node.sourceIdentity());else owners.add(node.ownerId());});
        });
        metadata.put("roots",roster);metadata.put("external_ids",externalIds);metadata.put("owner_ids",owners);metadata.put("source_ids",sourceIds);
        try {
            tx.executeWithoutResult(status -> {
                authority.lockPreparedBatch(proofs.values().stream().map(Proof::source).toList(),null,true);
                security.requireIsolatedRuntime();
                proofs.values().forEach(this::validateLocked);
                external.reserve(externalIds.stream().map(UUID::fromString).toList(),operation);if(!owners.isEmpty())vault.reserve(owners,operation);
                var first=proofs.values().iterator().next().source();var node=first.proofs().get(first.rootId());
                runs.saveAndFlush(new RagRun(operation,null,first.rootId(),node.epoch(),"ADMITTED",encode(metadata),"NONE",Instant.now()));
            });
            return operation;
        } catch(RuntimeException unknown) {
            // A new guarded read determines committed admission versus rollback. No blind release.
            stoppedOperations.add(operation);resolve(operation,true);throw unknown;
        }
    }

    public void finish(UUID operation,Map<String,Proof> proofs) {finish(operation,proofs,() -> {});}

    public void finish(UUID operation,Map<String,Proof> proofs,Runnable captureCheck) {
        try {
            tx.executeWithoutResult(status -> {
                captureCheck.run();
                authority.lockPreparedBatch(proofs.values().stream().map(Proof::source).toList(),operation,false);
                var row=operation(operation);if("COMPLETED".equals(row.getStatus()))return;
                if(!"ADMITTED".equals(row.getStatus()))throw new IllegalStateException("Nonpublication operation is not active");
                security.requireIsolatedRuntime();
                proofs.values().forEach(this::validateLocked);
                terminal(row,true);
            });
        } catch(RuntimeException unknown) {
            if(unknown instanceof java.util.concurrent.CancellationException)cancelledOperations.add(operation);
            stoppedOperations.add(operation);if(!resolve(operation,true))throw unknown;
        }
    }

    /** Reconciliation never reruns capture/indexing. Positive task completion or dead controller is required. */
    public boolean resolve(UUID operation,boolean callerStopped) {
        try {
            var rows=em.createQuery("SELECT r FROM RagRun r WHERE r.runId=:id AND r.jobId IS NULL",RagRun.class).setParameter("id",operation).getResultList();
            if(rows.isEmpty())return false;
            if(!security.isCurrentRuntime())return false;
            var metrics=decode(rows.getFirst().getMetrics());
            if(!"NONPUBLICATION".equals(metrics.get("kind")))return false;
            if(!callerStopped && !(runtime.instance().toString().equals(metrics.get("runtime_id")) && stoppedOperations.contains(operation)) && !controllerDead(metrics))return false;
            return Boolean.TRUE.equals(tx.execute(status -> {
                em.createNativeQuery("SELECT pg_advisory_xact_lock(:key)").setParameter("key",RagDependencyGraph.LOCK_ID).getSingleResult();
                if(!security.isCurrentRuntime())return false;
                UUID[] ids=strings(metrics,"external_ids").stream().map(UUID::fromString).toArray(UUID[]::new);
                var owners=numbers(metrics,"owner_ids");
                // Only unresolved operations still own reservations. A committed C_N must not reacquire them.
                var row=runs.findByRunId(operation).orElseThrow();em.refresh(row);
                if("COMPLETED".equals(row.getStatus()))return true;
                if(!"ADMITTED".equals(row.getStatus()))return false;
                if(!owners.isEmpty())vault.lockReserved(new TreeSet<>(owners),operation);external.lockReserved(List.of(ids),operation);
                sources.findAllByIdInForUpdate(new TreeSet<>(numbers(metrics,"source_ids")));
                row=operation(operation);terminal(row,false);return false;
            }));
        } catch(RuntimeException unavailable) {return false;}
    }

    public boolean hasLiveForeignOperation() {
        for(var row:em.createQuery("SELECT r FROM RagRun r WHERE r.jobId IS NULL AND r.status='ADMITTED' ORDER BY r.id",RagRun.class).setMaxResults(25).getResultList()) {
            var m=decode(row.getMetrics());if("NONPUBLICATION".equals(m.get("kind")) && !runtime.instance().toString().equals(m.get("runtime_id")) && !controllerDead(m))return true;
        }
        return false;
    }

    public void reconcile() {
        for(var row:em.createQuery("SELECT r FROM RagRun r WHERE r.jobId IS NULL AND r.status='ADMITTED' ORDER BY r.id",RagRun.class).setMaxResults(25).getResultList())resolve(row.getRunId(),false);
    }

    public boolean protectsCapture(UUID capture) {
        Number count=(Number)em.createNativeQuery("SELECT count(*) FROM rag.runs WHERE job_id IS NULL AND status='ADMITTED' AND metrics->>'kind'='NONPUBLICATION' AND metrics->>'capture_id'=:capture").setParameter("capture",capture.toString()).getSingleResult();
        return count.longValue()>0;
    }

    private RagRun operation(UUID id) {
        var row=em.createQuery("SELECT r FROM RagRun r WHERE r.runId=:id AND r.jobId IS NULL",RagRun.class).setParameter("id",id).getSingleResult();
        em.refresh(row,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);return row;
    }
    private void terminal(RagRun row,boolean success) {
        String failure=cancelledOperations.contains(row.getRunId())?"CANCELLED":"ABORTED";
        var metrics=decode(row.getMetrics());metrics.put("disposition",success?"NO_CHANGE":failure);
        external.release(strings(metrics,"external_ids").stream().map(UUID::fromString).toList(),row.getRunId());if(!numbers(metrics,"owner_ids").isEmpty())vault.release(new TreeSet<>(numbers(metrics,"owner_ids")),row.getRunId());
        em.createNativeQuery("UPDATE rag.runs SET status=:status,metrics=CAST(:metrics AS jsonb),purge_status='PURGE_COMPLETE',updated_at=clock_timestamp() WHERE run_id=:id")
            .setParameter("status",success?"COMPLETED":failure).setParameter("metrics",encode(metrics)).setParameter("id",row.getRunId()).executeUpdate();
    }
    private boolean controllerDead(Map<String,Object> m) {
        try {
            var p=ProcessHandle.of(((Number)m.get("controller_pid")).longValue());
            return p.isEmpty() || !p.get().isAlive() || p.get().info().startInstant().map(s -> !s.toString().equals(m.get("controller_started_at"))).orElse(false);
        } catch(RuntimeException unknown) {return false;}
    }
    private List<String> strings(Map<String,Object> m,String key) {return ((List<?>)m.get(key)).stream().map(String::valueOf).toList();}
    private List<Long> numbers(Map<String,Object> m,String key) {return ((List<?>)m.get(key)).stream().map(v -> ((Number)v).longValue()).toList();}
    private String encode(Object v) {try{return json.writeValueAsString(v);}catch(Exception invalid){throw new IllegalStateException("Invalid nonpublication record",invalid);}}
    private Map<String,Object> decode(String v) {try{return json.readValue(v,new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>(){});}catch(Exception invalid){throw new IllegalStateException("Invalid nonpublication record",invalid);}}
}
