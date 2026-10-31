package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;

/** No unfinished work resumes; normal boot preserves consent and completed indexes. */
@Component
public class RagRecoveryService implements SmartInitializingSingleton {
    private final EntityManager entityManager;
    private final RagRuntimeIdentity runtime;
    private final RagPurgeService purge;
    private final RagRuntimeSecurity security;
    private final RagWorkerSupervisor supervisor;
    private final RagGenerationState current;
    private final RagNoChangeService noChange;
    private final TransactionTemplate transaction;
    private final com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore byteStore;
    private final TransactionTemplate snapshotTransaction;
    private long snapshotCursor;
    private long generationCursor;
    private volatile boolean registeredOnce;
    private final com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService retrieval;
    private final RagSnapshotInspections inspections;
    private final org.springframework.beans.factory.ObjectProvider<com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaService> qaProvider;

    public RagRecoveryService(EntityManager entityManager,RagRuntimeIdentity runtime,RagPurgeService purge,
            PlatformTransactionManager transactions,RagRuntimeSecurity security,
            RagWorkerSupervisor supervisor,RagGenerationState current,RagNoChangeService noChange,
            com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore byteStore,
            com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService retrieval,
            RagSnapshotInspections inspections) {
        this(entityManager, runtime, purge, transactions, security, supervisor, current, noChange, byteStore, retrieval, inspections, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public RagRecoveryService(EntityManager entityManager,RagRuntimeIdentity runtime,RagPurgeService purge,
            PlatformTransactionManager transactions,RagRuntimeSecurity security,
            RagWorkerSupervisor supervisor,RagGenerationState current,RagNoChangeService noChange,
            com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore byteStore,
            com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService retrieval,
            RagSnapshotInspections inspections,
            org.springframework.beans.factory.ObjectProvider<com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaService> qaProvider) {
        this.entityManager=entityManager; this.runtime=runtime; this.purge=purge; this.security=security;
        this.supervisor=supervisor; this.current=current;this.noChange=noChange;
        transaction=new TransactionTemplate(transactions); transaction.setTimeout(10);
        this.byteStore=byteStore;
        this.retrieval=retrieval;
        this.inspections=inspections;
        this.qaProvider=qaProvider;
        snapshotTransaction=new TransactionTemplate(transactions); snapshotTransaction.setTimeout(10);
        snapshotTransaction.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    private boolean isQuiescentReader(UUID readerId) {
        if (retrieval.hasSender(readerId)) {
            return retrieval.isQuiescentReader(readerId);
        }
        var qa = qaProvider != null ? qaProvider.getIfAvailable() : null;
        if (qa != null && qa.hasSender(readerId)) {
            return qa.isQuiescentReader(readerId);
        }
        return false;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!security.isIsolatedRole()) return;
        if (registeredOnce && !security.isCurrentRuntime()) return;
        // Take no ownership of a live/unknown old controller. Proof and any native
        // process waits happen outside SQL transactions; the final TX rechecks it.
        if(noChange.hasLiveForeignOperation())return;
        var stoppedReaders=proveStoppedReaders();
        if (stoppedReaders == null) return;
        var inspectionInventory=inspections.stoppedOrOwnedInventory();
        if(inspectionInventory==null) return;
        inspections.reconcile(stoppedReaders.keySet(),this::isQuiescentReader);
        var outstanding=nonterminal();
        var stopped=new HashSet<UUID>();
        for(var lease:outstanding) {
            if (!supervisor.recoverStopped(lease)) return;
            stopped.add(lease.getJobId());
        }
        Boolean registered=transaction.execute(status -> {
            entityManager.createNativeQuery("SET LOCAL lock_timeout='3s'").executeUpdate();
            entityManager.createNativeQuery("SET LOCAL statement_timeout='5s'").executeUpdate();
            entityManager.createNativeQuery("SET LOCAL transaction_timeout='10s'").executeUpdate();
            entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:key)")
                    .setParameter("key",RagDependencyGraph.LOCK_ID).getSingleResult();
            if(noChange.hasLiveForeignOperation() || !readersMatchStopProof(stoppedReaders)
                    || !inspections.inventoryStillKnown(inspectionInventory)) return false;
            var fresh=entityManager.createQuery("SELECT l FROM RagJobLease l WHERE l.status IN "
                    +"('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING') ORDER BY l.id",RagJobLease.class)
                    .setMaxResults(2).getResultList();
            for(var lease:fresh) {
                entityManager.refresh(lease,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                if (!stopped.contains(lease.getJobId()) || !supervisor.proveStopped(lease.getJobId())) return false;
            }
            for (var proof : stoppedReaders.values()) {
                entityManager.createNativeQuery("""
                        SELECT rag.retrieval_reader_finish(:reader,:token,'RECOVERY_REQUIRED','ABORTED',
                            '{"reason":"EXACT_OLD_CONTROLLER_STOPPED","sender_quiescent":true,"native_quiescent":true,"transport_quiescent":true,"buffers_discarded":true,"controller_stopped":true}'::jsonb,
                            ARRAY[]::uuid[],ARRAY[]::uuid[])
                        """).setParameter("reader",proof.reader()).setParameter("token",proof.token()).getSingleResult();
            }
            entityManager.createNativeQuery("""
                    INSERT INTO rag.runtime_state(singleton,current_instance) VALUES(true,:instance)
                    ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance
                    """).setParameter("instance",runtime.instance()).executeUpdate();
            entityManager.createNativeQuery("SELECT rag.retrieval_reader_recover_stale(:instance)")
                    .setParameter("instance", runtime.instance().toString())
                    .getSingleResult();
            return true;
        });
        if (Boolean.TRUE.equals(registered)) {
            registeredOnce=true;
            noChange.reconcile();
            for(var lease:outstanding) purge.abortJob(lease.getJobId(),false);
            byteStore.resetRecoveryScan();
            reconcileSnapshots();
        }
    }

    private record ReaderStopProof(UUID reader, UUID token, String controller, Long workerPid, Long workerFileTime) {}

    /** Native/process waits occur before graph/source SQL guards. Unknown identities stay fenced. */
    private Map<UUID,ReaderStopProof> proveStoppedReaders() {
        try {
            var active = entityManager.createQuery(
                    "SELECT r FROM com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalReader r WHERE r.state <> 'TERMINAL'",
                    com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalReader.class
            ).setMaxResults(5).getResultList();
            var stopped=new HashMap<UUID,ReaderStopProof>();
            for (var reader : active) {
                var payload=reader.getScopePayload();
                if (!(payload.get("controller_pid") instanceof Number pid)
                        || !(payload.get("controller_creation_file_time") instanceof String fileTime)) return null;
                if (pid.longValue()==ProcessHandle.current().pid()
                        && !com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess
                        .isRecordedProcessTerminated(pid.longValue(),Long.parseLong(fileTime),java.time.Duration.ZERO)) return null;
                if (!com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess
                        .isRecordedProcessTerminated(pid.longValue(),Long.parseLong(fileTime),java.time.Duration.ofSeconds(5))) return null;
                // A complete recorded controller tuple is required, not an absent worker PID.
                // A stopped controller cannot run Java/context/transport callbacks again.
                if (reader.getWorkerPid() == null) {
                    if (!"ADMITTED".equals(reader.getState()) || reader.isEmissionStarted()) return null;
                } else if (reader.getWorkerCreationFileTime() == null
                        || !com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess
                        .isRecordedProcessTerminated(reader.getWorkerPid(),reader.getWorkerCreationFileTime(),java.time.Duration.ofSeconds(5))) {
                    return null;
                }
                stopped.put(reader.getReaderId(),new ReaderStopProof(reader.getReaderId(),reader.getReservationToken(),
                        reader.getControllerInstance(),reader.getWorkerPid(),reader.getWorkerCreationFileTime()));
            }
            return Map.copyOf(stopped);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean readersMatchStopProof(Map<UUID,ReaderStopProof> stopped) {
        var active=entityManager.createQuery("SELECT r FROM com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalReader r WHERE r.state<>'TERMINAL'",
                com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalReader.class).setMaxResults(2).getResultList();
        for (var reader : active) {
            entityManager.refresh(reader);
            var proof=stopped.get(reader.getReaderId());
            if (proof==null || !proof.token().equals(reader.getReservationToken())
                    || !proof.controller().equals(reader.getControllerInstance())
                    || !Objects.equals(proof.workerPid(),reader.getWorkerPid())
                    || !Objects.equals(proof.workerFileTime(),reader.getWorkerCreationFileTime())) return false;
        }
        return active.size()==stopped.size();
    }

    @Scheduled(fixedDelayString="${ppv.rag.reconciliation-delay-ms:30000}",initialDelayString="${ppv.rag.reconciliation-delay-ms:30000}")
    public synchronized void reconcile() {
        if (!security.isIsolatedRole()) return;
        if (!security.isCurrentRuntime()) {
            if (!registeredOnce) afterSingletonsInstantiated();
            return;
        }
        noChange.reconcile();
        inspections.reconcile(Set.of(),this::isQuiescentReader);
        retrieval.reconcileCompletedSenders();
        var qa = qaProvider != null ? qaProvider.getIfAvailable() : null;
        if (qa != null) qa.reconcileCompletedSenders();
        reconcileSnapshots();
        for(var attempt:nonterminal()) recover(attempt);
        for(UUID job:supervisor.provenStoppedJobs()) {
            try {
                Boolean terminal=transaction.execute(status -> {
                    var attempts=entityManager.createQuery("SELECT l FROM RagJobLease l WHERE l.jobId=:job",RagJobLease.class)
                            .setParameter("job",job).setMaxResults(2).getResultList();
                    if(attempts.size()!=1) return false;
                    var lease=attempts.getFirst(); entityManager.refresh(lease);
                    return lease.isStagingClosed() && Set.of("COMPLETED","CANCELLED","ABORTED","PURGED").contains(lease.getStatus());
                });
                if(Boolean.TRUE.equals(terminal) && supervisor.proveStopped(job)) supervisor.forget(job);
            } catch (RuntimeException uncertainRead) {
                // No successful reconciliation C means native handles remain held.
            }
        }
        for(UUID job:purge.pendingJobs()) purge.retryCleanup(job);
        var generations=transaction.execute(status -> entityManager.createQuery(
                "SELECT g FROM RagIndexGeneration g WHERE g.id>:cursor AND (g.active=true OR g.sourceRevisionHash<>'' "
                        +"OR g.dependencyManifestDigest<>'' OR EXISTS(SELECT r.id FROM RagRun r WHERE r.jobId=g.jobId "
                        +"AND r.purgeStatus<>'PURGE_COMPLETE')) ORDER BY g.id",RagIndexGeneration.class)
                .setParameter("cursor",generationCursor).setMaxResults(25).getResultList());
        if (generations==null) return;
        generationCursor=generations.size()<25?0:generations.getLast().getId();
        for(var generation:generations) {
            String state=current.state(generation.getGenerationId());
            if (Set.of("OUTDATED","REVOKED").contains(state)) {
                try {
                    // Batch identity survives deletion of a generation's sensitive manifest.
                    Number rootCount = (Number) entityManager.createNativeQuery(
                            "SELECT count(*) FROM rag.batch_roots WHERE job_id=:job")
                            .setParameter("job", generation.getJobId())
                            .getSingleResult();
                    if (rootCount != null && rootCount.longValue() > 0) {
                        purge.invalidateGeneration(generation.getGenerationId(), state);
                        purge.eraseGeneration(generation.getGenerationId());
                    } else {
                        purge.invalidateJob(generation.getJobId(), state);
                        purge.retryCleanup(generation.getJobId());
                    }
                } catch (RuntimeException unavailable) {
                    // Fresh authority denies access while erasure remains retryable.
                }
            }
        }
    }

    /** Targeted post-commit retirement; the remaining files are retried by bounded inventory scans. */
    public void reconcileRevision(UUID source, UUID revision) {
        if (source==null || revision==null) return;
        try {
            if (!security.isIsolatedRole() || !security.isCurrentRuntime()) return;
            recoverSnapshotArtifact(new com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore.StorageEntry(
                    null,source,revision,false));
        } catch (RuntimeException unavailable) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Snapshot retirement deferred; inventory retained for retry");
        }
    }

    private void recoverSnapshotArtifact(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore.StorageEntry entry) {
            snapshotTransaction.executeWithoutResult(status -> {
                    entityManager.createNativeQuery("SET LOCAL lock_timeout='2s'").executeUpdate();
                    entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:key)")
                            .setParameter("key", RagDependencyGraph.LOCK_ID).getSingleResult();
                    // Startup changes runtime ownership under this same graph lock.
                    if (!security.isCurrentRuntime()) return;
                    if (byteStore.active(entry)) return;
                    if(entry.temporary() && noChange.protectsCapture(entry.artifactId()))return;
                    if (!entry.temporary()) {
                        Number count=(Number)entityManager.createNativeQuery("""
                                SELECT count(*) FROM rag.external_file_sources
                                WHERE file_source_id=:source AND eligible
                                AND (pending_revision_id=:revision OR selected_revision_id=:revision)
                                """).setParameter("source",entry.sourceId()).setParameter("revision",entry.artifactId()).getSingleResult();
                        if (count.longValue()!=0) return;
                        Number inspections=(Number)entityManager.createNativeQuery("""
                                SELECT count(*) FROM rag.retrieval_inspections WHERE affected_file_source_id=:source
                                AND (affected_revision_id IS NULL OR affected_revision_id=:revision)
                                AND state IN ('UNRESOLVED','INCIDENT_PENDING')
                                """).setParameter("source",entry.sourceId()).setParameter("revision",entry.artifactId()).getSingleResult();
                        if(inspections.longValue()!=0) return;
                    }
                    var report=entry.temporary() ? byteStore.cleanupTemp(entry.artifactId())
                            : byteStore.deleteRevision(entry.sourceId(),entry.artifactId());
                    if (report.status()!=com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore.DeletionStatus.COMPLETED)
                        org.slf4j.LoggerFactory.getLogger(getClass()).warn("Snapshot cleanup pending; inventory retained for retry");
                });
    }

    /** Existing startup/scheduled lifecycle, also requested after exposed snapshot mutations.
     * Never adopts files. A failed/unknown SQL read cannot authorize deletion. Acquiring the
     * same graph lock as registration/CAS resolves in-flight commit uncertainty before erasure.
     */
    public synchronized void reconcileSnapshots() {
        try {
            if (!security.isIsolatedRole() || !security.isCurrentRuntime()) return;
            inspections.reconcile(Set.of(),this::isQuiescentReader);
            for (var entry : byteStore.nextRecoveryBatch(25)) {
                recoverSnapshotArtifact(entry);
            }
            // Bounded metadata cursor also detects registered pairs whose BOTH files are missing.
            var rows=snapshotTransaction.execute(status -> entityManager.createQuery(
                    "SELECT s FROM com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource s "
                    + "WHERE s.id>:cursor AND s.eligible=true AND s.snapshotIntegrityFailed=false ORDER BY s.id",
                    com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource.class)
                    .setParameter("cursor",snapshotCursor).setMaxResults(25).getResultList());
            if (rows==null) return;
            snapshotCursor=rows.size()<25?0:rows.getLast().getId();
            for (var row:rows) {
                var revisions=new LinkedHashMap<UUID,SnapshotRevision>();
                if(row.getPendingRevisionId()!=null) revisions.put(row.getPendingRevisionId(),new SnapshotRevision(row.getPendingRevisionId(),row.getPendingRevisionHash(),row.getPendingByteLength()));
                if(row.getSelectedRevisionId()!=null) revisions.put(row.getSelectedRevisionId(),new SnapshotRevision(row.getSelectedRevisionId(),row.getSelectedRevisionHash(),row.getSelectedByteLength()));
                for(var revision:revisions.values()) {
                    if(inspections.hasBlocking(row.getFileSourceId(),revision.id())) continue;
                    try {
                        if(revision.hash()==null || revision.length()==null) continue;
                        inspections.verify(row.getFileSourceId(),revision.id(),revision.hash(),revision.length(),"RECOVERY_SNAPSHOT",
                                row.getCanonicalIdentityDigest()!=null?row.getCanonicalIdentityDigest():"recovery:integrity");
                    } catch(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotIntegrityException
                            | com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException knownIncident) {
                        // The inspection owner has durably classified this exact original operation.
                    }
                }
            }
        } catch (RuntimeException unavailable) {
            // Files/rows remain the durable retry inventory. No original is reopened.
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Snapshot reconciliation deferred; authority or storage unavailable");
        }
    }

    private record SnapshotRevision(UUID id,String hash,Long length) {}

    private List<RagJobLease> nonterminal() {
        var result=transaction.execute(status -> entityManager.createQuery("""
                SELECT l FROM RagJobLease l WHERE l.status IN
                ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING') ORDER BY l.id
                """,RagJobLease.class).setMaxResults(2).getResultList());
        if (result==null) return List.of();
        if (result.size()>1) throw new IllegalStateException("Single-attempt invariant violated");
        return result;
    }

    private void recover(RagJobLease lease) {
        boolean own=lease.getControllerIdentity()!=null && runtime.instance().toString()
                .equals(String.valueOf(lease.getControllerIdentity().get("runtime_id")));
        if (own && Set.of("ADMITTED","STARTING","ACTIVE","FINALIZING").contains(lease.getStatus())
                && lease.getLeasedUntil().isAfter(databaseTime())) return;
        try {
            if (own || supervisor.recoverStopped(lease)) purge.abortJob(lease.getJobId(),false);
            else purge.requireRecovery(lease.getJobId());
        } catch (RuntimeException unknown) { purge.requireRecovery(lease.getJobId()); }
    }

    private java.time.Instant databaseTime() {
        return transaction.execute(status -> {
            Number micros=(Number)entityManager.createNativeQuery("SELECT CAST(EXTRACT(EPOCH FROM clock_timestamp())*1000000 AS bigint)").getSingleResult();
            return java.time.Instant.ofEpochSecond(micros.longValue()/1_000_000,(micros.longValue()%1_000_000)*1000);
        });
    }
}
