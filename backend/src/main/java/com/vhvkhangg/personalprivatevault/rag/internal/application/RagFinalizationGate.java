package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Stopped consumer, drained writer fence, then P/seal/terminal/release in one physical C. */
@Service
public class RagFinalizationGate {
    private final RagSourceAuthority authority;
    private final RagWorkerGrantService grants;
    private final RagIndexGenerationRepository generations;
    private final RagStagedOutputRepository outputs;
    private final RagJobLeaseRepository leases;
    private final RagRunRepository runs;
    private final SourceIndexReservations reservations;
    private final RagWorkerSupervisor supervisor;
    private final RagGenerationState current;
    private final RagPurgeService purge;
    private final EntityManager entityManager;
    private final TransactionTemplate transaction;

    public RagFinalizationGate(RagSourceAuthority authority, RagWorkerGrantService grants,
            RagIndexGenerationRepository generations, RagStagedOutputRepository outputs,
            RagJobLeaseRepository leases, RagRunRepository runs, SourceIndexReservations reservations,
            RagWorkerSupervisor supervisor, RagGenerationState current, EntityManager entityManager,
            PlatformTransactionManager transactions, RagPurgeService purge) {
        this.authority=authority; this.grants=grants; this.generations=generations; this.outputs=outputs;
        this.leases=leases; this.runs=runs; this.reservations=reservations; this.supervisor=supervisor;
        this.current=current; this.entityManager=entityManager; this.purge=purge;
        transaction=new TransactionTemplate(transactions); transaction.setTimeout(10);
    }

    public RagFinalizationResult finalizeGeneration(UUID jobId, UUID attemptId, String expectedDigest) {
        if (TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Finish must enter outside an ambient transaction");
        Objects.requireNonNull(jobId); Objects.requireNonNull(attemptId);
        var initial=leases.findByJobIdAndAttemptId(jobId,attemptId).orElseThrow();
        if ("COMPLETED".equals(initial.getStatus())) {
            var generation=generations.findByJobId(jobId).stream().findFirst().orElseThrow();
            if (expectedDigest!=null && !expectedDigest.isBlank()
                    && !expectedDigest.equals(generation.getDependencyManifestDigest()))
                throw new IllegalArgumentException("Dependency manifest digest mismatch");
            String state=current.state(generation.getGenerationId());
            return new RagFinalizationResult(generation.getGenerationId(),generation.getSourceId(),generation.getAuthorizationEpoch(),
                    "CURRENT".equals(state)?"PUBLISHED":state,"CURRENT".equals(state),generation.getPublishedAt());
        }
        if (!supervisor.proveStopped(jobId)) throw new IllegalStateException("Consumer termination is not proven");
        var prepared=authority.prepareSource(initial.getSourceId(),Map.of());
        String digest=RagSourceAuthority.digest(prepared.manifest());
        if (expectedDigest!=null && !expectedDigest.isBlank() && !expectedDigest.equals(digest))
            throw new IllegalArgumentException("Dependency manifest digest mismatch");
        RagFinalizationResult result;
        try {
            result=transaction.execute(status -> {
            var attempt=authority.lockPreparedAttempt(jobId,attemptId,prepared,true);
            var lease=attempt.lease();
            if (!supervisor.proveStopped(jobId)) throw new IllegalStateException("Consumer termination is not proven");
            lease.sealStaging();
            long count=outputs.countByJobIdAndAttemptId(jobId,attemptId);
            if (count!=1 || outputs.sumPayloadLengthByJobIdAndAttemptId(jobId,attemptId)>10_485_760)
                throw new IllegalStateException("Expected one bounded foundation receipt");
            Number verified=(Number)entityManager.createNativeQuery("SELECT count(*) FROM rag.staged_outputs WHERE job_id=:job AND attempt_id=:attempt AND worker_receipt")
                    .setParameter("job",jobId).setParameter("attempt",attemptId).getSingleResult();
            if (verified.intValue()!=1) throw new IllegalStateException("Unverified worker provenance");
            var output=outputs.findByJobIdAndAttemptId(jobId,attemptId).getFirst();
            var existing=generations.findBySourceIdForUpdate(attempt.snapshot().root().getId());
            existing.forEach(entityManager::refresh);
            var run=runs.findByJobId(jobId).orElseThrow();
            entityManager.refresh(run,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            // All waits precede the final observed-expiry gates. E1 allows a later
            // pause, not a newly observed expired verification/publication gate.
            grants.verifyDurableReceipt(lease,output.getStageType(),output.getPayload());
            var now=authority.databaseTime();
            if (lease.isExpired(now)) throw new IllegalStateException("Execution expired after publication waits");
            if (!"ACTIVE".equals(lease.getStatus())) throw new IllegalStateException("Attempt is not publishable");
            for (var generation:existing) if (generation.isActive()) generation.supersede(now);
            generations.saveAll(existing);
            var root=attempt.snapshot().root();
            UUID generationId=UUID.randomUUID();
            generations.save(new RagIndexGeneration(generationId,jobId,root.getId(),"PUBLISHED",root.getAuthorizationEpoch(),
                    root.getSourceRevisionHash(),digest,true,now));
            lease.updateStatus("COMPLETED",now); leases.save(lease);
            run.updatePurgeStatus("ACTIVE",now); runs.save(run);
            reservations.release(RagManifestScope.owners(lease.getDependencyManifest()),lease.getReservationToken());
            entityManager.flush();
            entityManager.createNativeQuery("UPDATE rag.runs SET status='COMPLETED' WHERE job_id=:job")
                    .setParameter("job",jobId).executeUpdate();
            return new RagFinalizationResult(generationId,root.getId(),root.getAuthorizationEpoch(),"PUBLISHED",true,now);
            });
        } catch (RuntimeException unknownCommitOrRollback) {
            // A fresh read may discover a completed C; requireRecovery never
            // rewrites a terminal outcome. Failed P is discarded, not resumed.
            purge.requireRecovery(jobId);
            throw unknownCommitOrRollback;
        }
        // Reached only after a known successful physical C, never an unknown C.
        supervisor.forget(jobId);
        return result;
    }
}
