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
    private final TransactionTemplate transaction;
    private long generationCursor;
    private volatile boolean registeredOnce;

    public RagRecoveryService(EntityManager entityManager,RagRuntimeIdentity runtime,RagPurgeService purge,
            PlatformTransactionManager transactions,RagRuntimeSecurity security,
            RagWorkerSupervisor supervisor,RagGenerationState current) {
        this.entityManager=entityManager; this.runtime=runtime; this.purge=purge; this.security=security;
        this.supervisor=supervisor; this.current=current;
        transaction=new TransactionTemplate(transactions); transaction.setTimeout(10);
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!security.isIsolatedRole()) return;
        if (registeredOnce && !security.isCurrentRuntime()) return;
        // Take no ownership of a live/unknown old controller. Proof and any native
        // process waits happen outside SQL transactions; the final TX rechecks it.
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
            var fresh=entityManager.createQuery("SELECT l FROM RagJobLease l WHERE l.status IN "
                    +"('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING') ORDER BY l.id",RagJobLease.class)
                    .setMaxResults(2).getResultList();
            for(var lease:fresh) {
                entityManager.refresh(lease,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                if (!stopped.contains(lease.getJobId()) || !supervisor.proveStopped(lease.getJobId())) return false;
            }
            entityManager.createNativeQuery("""
                    INSERT INTO rag.runtime_state(singleton,current_instance) VALUES(true,:instance)
                    ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance
                    """).setParameter("instance",runtime.instance()).executeUpdate();
            return true;
        });
        if (Boolean.TRUE.equals(registered)) {
            registeredOnce=true;
            for(var lease:outstanding) purge.abortJob(lease.getJobId(),false);
        }
    }

    @Scheduled(fixedDelayString="${ppv.rag.reconciliation-delay-ms:30000}",initialDelayString="${ppv.rag.reconciliation-delay-ms:30000}")
    public synchronized void reconcile() {
        if (!security.isIsolatedRole()) return;
        if (!security.isCurrentRuntime()) {
            if (!registeredOnce) afterSingletonsInstantiated();
            return;
        }
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
                    purge.invalidateJob(generation.getJobId(),state);
                    purge.retryCleanup(generation.getJobId());
                } catch (RuntimeException unavailable) {
                    // Fresh authority denies access while erasure remains retryable.
                }
            }
        }
    }

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
