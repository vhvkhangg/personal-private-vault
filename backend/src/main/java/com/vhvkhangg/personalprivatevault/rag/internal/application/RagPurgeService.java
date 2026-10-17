package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRun;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;

/** Source-scoped denial and verified cleanup; the existing reconciler is the only scheduler. */
@Service
public class RagPurgeService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RagPurgeService.class);
    private static final Set<String> TERMINAL=Set.of("COMPLETED","CANCELLED","ABORTED","PURGED");
    private final EntityManager entityManager;
    private final RagDependencyGraph graph;
    private final RagJobLeaseRepository leases;
    private final SourceIndexReservations reservations;
    private final RagWorkerSupervisor supervisor;
    private final TransactionTemplate transaction;
    private long cleanupCursor;

    public RagPurgeService(EntityManager entityManager,RagDependencyGraph graph,RagJobLeaseRepository leases,
            SourceIndexReservations reservations,RagWorkerSupervisor supervisor,PlatformTransactionManager transactions) {
        this.entityManager=entityManager; this.graph=graph; this.leases=leases;
        this.reservations=reservations; this.supervisor=supervisor;
        transaction=new TransactionTemplate(transactions); transaction.setTimeout(10);
    }

    /** Cancellation is not participation revocation or deletion of another job's index. */
    public boolean abortJob(UUID job) { return abortJob(job,true); }

    public boolean abortJob(UUID job,boolean cancelled) {
        requireOutside();
        String decision=transaction.execute(status -> {
            var found=leases.findByJobIdForUpdate(job);
            if (found.isEmpty()) throw new NoSuchElementException("Job not found");
            var lease=found.getFirst(); entityManager.refresh(lease);
            if ("COMPLETED".equals(lease.getStatus())) return "COMPLETED";
            if (TERMINAL.contains(lease.getStatus())) return "TERMINAL";
            lease.updateStatus("STOPPING",java.time.Instant.now()); leases.saveAndFlush(lease);
            return "STOP";
        });
        if (!"STOP".equals(decision)) {
            if (supervisor.proveStopped(job)) supervisor.forget(job);
            return "TERMINAL".equals(decision);
        }
        if (!supervisor.proveStopped(job) && !supervisor.stopAndVerify(job)) {
            requireRecovery(job); return false;
        }
        var initial=leases.findByJobId(job).getFirst();
        if (TERMINAL.contains(initial.getStatus())) {
            if (supervisor.proveStopped(job)) supervisor.forget(job);
            return !"COMPLETED".equals(initial.getStatus());
        }
        final SortedSet<Long> owners;
        try { owners=RagManifestScope.owners(initial.getDependencyManifest()); }
        catch (RuntimeException unknownScope) { requireRecovery(job); return false; }
        try {
            Boolean finished=transaction.execute(status -> {
                graph.lockAndRead();
                var current=leases.findByJobIdAndAttemptId(job,initial.getAttemptId()).orElse(null);
                if (current!=null && TERMINAL.contains(current.getStatus())) {
                    if (!current.isStagingClosed()) {
                        throw new IllegalStateException("Terminal lease staging is not closed");
                    }
                    return !"COMPLETED".equals(current.getStatus());
                }
                reservations.lockReserved(owners,initial.getReservationToken());
                var lease=leases.findByJobIdAndAttemptIdForUpdate(job,initial.getAttemptId()).orElseThrow();
                entityManager.refresh(lease);
                if (TERMINAL.contains(lease.getStatus())) return !"COMPLETED".equals(lease.getStatus());
                if (!supervisor.proveStopped(job)) throw new IllegalStateException("Consumer termination is not proven");
                // The acquired attempt lock drained every pre-existing staging writer.
                lease.sealStaging();
                lease.updateStatus(cancelled?"CANCELLED":"ABORTED",java.time.Instant.now());
                leases.saveAndFlush(lease);
                entityManager.createNativeQuery("UPDATE rag.runs SET status=:outcome WHERE job_id=:job")
                        .setParameter("outcome",cancelled?"CANCELLED":"ABORTED").setParameter("job",job).executeUpdate();
                denyArtifacts(job);
                eraseWithSavepoint(job);
                reservations.release(owners,lease.getReservationToken());
                entityManager.flush();
                return true;
            });
            if (Boolean.TRUE.equals(finished)) supervisor.forget(job);
            return Boolean.TRUE.equals(finished);
        } catch (RuntimeException unknownCommitOrWriter) {
            log.debug("abortJob primary transaction failed for job {}: {}", job, unknownCommitOrWriter.getMessage());
            // No retry of a publication/finish callback and no release-only recovery.
            // A later reconciliation reads the real committed outcome.
            requireRecovery(job); return false;
        }
    }

    public void requireRecovery(UUID job) {
        try {
            transaction.executeWithoutResult(status -> {
                var found=leases.findByJobIdForUpdate(job);
                if (found.isEmpty()) return;
                var lease=found.getFirst(); entityManager.refresh(lease);
                if (!TERMINAL.contains(lease.getStatus())) {
                    lease.updateStatus("RECOVERY_REQUIRED",java.time.Instant.now()); leases.saveAndFlush(lease);
                }
            });
        } catch (RuntimeException unavailable) {
            // Existing reservation and nonterminal state are already durable.
        }
    }

    /** Called inside the owner policy TX; no process wait or external operation. */
    @Transactional
    public void purgeJobDerivatives(UUID job) {
        graph.lockAndRead();
        var found=leases.findByJobIdForUpdate(job);
        if (found.isEmpty()) return;
        var lease=found.getFirst(); entityManager.refresh(lease);
        if (!TERMINAL.contains(lease.getStatus()))
            throw new IllegalStateException("An active source requires cancellation first");
        denyArtifacts(job);
    }

    /** Exact historical manifest scope, never every new job sharing an old root. */
    @Transactional
    public void purgeSourceDerivatives(Long source) {
        graph.lockAndRead();
        var candidates=entityManager.createQuery(
                "SELECT l FROM RagJobLease l WHERE l.dependencyManifest<>'SCRUBBED' ORDER BY l.id",RagJobLease.class)
                .setMaxResults(256).getResultList();
        for (var lease:candidates) {
            boolean affected;
            try { affected=RagManifestScope.references(lease.getDependencyManifest(),source); }
            catch (RuntimeException unprovable) { affected=lease.getSourceId().equals(source); }
            if (affected && TERMINAL.contains(lease.getStatus())) {
                leases.findByJobIdForUpdate(lease.getJobId());
                entityManager.createNativeQuery("UPDATE rag.runs SET purge_status='PURGE_PENDING_QUIESCENCE',updated_at=clock_timestamp() WHERE job_id=:job")
                        .setParameter("job",lease.getJobId()).executeUpdate();
            }
        }
        // Fresh source/epoch/version predicates deny all remaining historical
        // derivatives immediately; bounded reconciliation discovers later pages.
    }

    private void denyArtifacts(UUID job) {
        denyArtifacts(job,"REVOKED");
    }

    @Transactional
    public void invalidateJob(UUID job,String reason) {
        if (!Set.of("OUTDATED","REVOKED").contains(reason)) throw new IllegalArgumentException("Invalid index state");
        graph.lockAndRead();
        var found=leases.findByJobIdForUpdate(job);
        if (found.isEmpty()) return;
        entityManager.refresh(found.getFirst());
        if (!TERMINAL.contains(found.getFirst().getStatus())) throw new IllegalStateException("Attempt still requires safe cancellation");
        denyArtifacts(job,reason);
    }

    private void denyArtifacts(UUID job,String reason) {
        entityManager.flush();
        entityManager.createNativeQuery("UPDATE rag.index_generations SET is_active=false,status=:reason,revoked_at=clock_timestamp() WHERE job_id=:job")
                .setParameter("job",job).setParameter("reason",reason).executeUpdate();
        entityManager.createNativeQuery("UPDATE rag.runs SET purge_status='PURGE_PENDING_QUIESCENCE',updated_at=clock_timestamp() WHERE job_id=:job")
                .setParameter("job",job).executeUpdate();
    }

    private boolean eraseWithSavepoint(UUID job) {
        boolean[] erased={false};
        entityManager.unwrap(org.hibernate.Session.class).doWork(connection -> {
            var savepoint=connection.setSavepoint();
            try {
                eraseRows(connection,job);
                connection.releaseSavepoint(savepoint); erased[0]=true;
            } catch (SQLException deletionUnavailable) {
                connection.rollback(savepoint); connection.releaseSavepoint(savepoint);
            }
        });
        if (erased[0]) entityManager.createNativeQuery("UPDATE rag.runs SET purge_status='PURGE_COMPLETE',updated_at=clock_timestamp() WHERE job_id=:job")
                .setParameter("job",job).executeUpdate();
        return erased[0];
    }

    private static void eraseRows(Connection connection,UUID job) throws SQLException {
        for (String sql:List.of(
                "DELETE FROM rag.staged_outputs WHERE job_id=?",
                "DELETE FROM rag.worker_grants WHERE job_id=?",
                "UPDATE rag.index_generations SET source_revision_hash='',dependency_manifest_digest='' WHERE job_id=? AND NOT is_active",
                "UPDATE rag.job_leases SET lease_token='SCRUBBED',approved_fields_text='',content_revision_hash='',description_revision_hash='',dependency_manifest='SCRUBBED' WHERE job_id=? AND staging_closed AND status IN ('COMPLETED','CANCELLED','ABORTED','PURGED')")) {
            try(var statement=connection.prepareStatement(sql)) { statement.setObject(1,job); statement.executeUpdate(); }
        }
        try(var statement=connection.prepareStatement("""
                SELECT (SELECT count(*) FROM rag.staged_outputs WHERE job_id=?)
                  +(SELECT count(*) FROM rag.worker_grants WHERE job_id=?)
                  +(SELECT count(*) FROM rag.index_generations WHERE job_id=? AND
                      (is_active OR source_revision_hash<>'' OR dependency_manifest_digest<>''))
                  +(SELECT count(*) FROM rag.job_leases WHERE job_id=? AND
                      (NOT staging_closed OR status NOT IN ('COMPLETED','CANCELLED','ABORTED','PURGED')
                          OR dependency_manifest<>'SCRUBBED' OR lease_token<>'SCRUBBED'
                          OR approved_fields_text<>'' OR content_revision_hash<>'' OR description_revision_hash<>''))
                """)) {
            for(int parameter=1;parameter<=4;parameter++) statement.setObject(parameter,job);
            try(var result=statement.executeQuery()) {
                if(!result.next() || result.getLong(1)!=0) throw new SQLException("Cleanup verification incomplete");
            }
        }
    }

    /** Terminal C is durable prior process/writer proof; TTL and health never substitute. */
    public boolean retryCleanup(UUID job) {
        requireOutside();
        var found=leases.findByJobId(job);
        if (found.isEmpty()) return false;
        var initial=found.getFirst();
        if (!TERMINAL.contains(initial.getStatus())) return false;
        try {
            return Boolean.TRUE.equals(transaction.execute(status -> {
                graph.lockAndRead();
                var lease=leases.findByJobIdForUpdate(job).getFirst(); entityManager.refresh(lease);
                if (!TERMINAL.contains(lease.getStatus()) || !lease.isStagingClosed()) return false;
                Number active=(Number)entityManager.createNativeQuery("SELECT count(*) FROM rag.index_generations WHERE job_id=:job AND is_active")
                        .setParameter("job",job).getSingleResult();
                if (active.longValue()!=0) return false;
                return eraseWithSavepoint(job);
            }));
        } catch (RuntimeException unavailable) { return false; }
    }

    public synchronized List<UUID> pendingJobs() {
        var page=transaction.execute(status -> entityManager.createQuery(
                "SELECT r FROM RagRun r WHERE r.purgeStatus='PURGE_PENDING_QUIESCENCE' AND r.id>:cursor ORDER BY r.id",RagRun.class)
                .setParameter("cursor",cleanupCursor).setMaxResults(25).getResultList());
        if (page==null) return List.of();
        cleanupCursor=page.size()<25?0:page.getLast().getId();
        return page.stream().map(RagRun::getJobId).toList();
    }

    private static void requireOutside() {
        if(TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Process and cleanup coordination requires no ambient transaction");
    }
}
