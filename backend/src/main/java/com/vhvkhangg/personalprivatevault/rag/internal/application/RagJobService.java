package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobStatusView;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRun;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagSource;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRunRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagStagedOutputRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
public class RagJobService {

    private final RagJobLeaseRepository jobLeaseRepository;
    private final RagStagedOutputRepository stagedOutputRepository;
    private final RagRunRepository runRepository;
    private final RagPurgeService purgeService;
    private final RagEngineClient engineClient;
    private final TransactionTemplate transactionTemplate;
    private final RagSourceAuthority authority;
    private final RagWorkerGrantService grants;
    private final Duration leaseDuration;
    private final RagLimits limits;
    private final com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations reservations;
    private final RagRuntimeIdentity runtime;
    private final RagWorkerSupervisor supervisor;
    private final RagGenerationState current;

    public RagJobService(RagJobLeaseRepository jobLeaseRepository, RagStagedOutputRepository stagedOutputRepository,
                         RagRunRepository runRepository, RagPurgeService purgeService, RagEngineClient engineClient,
                         PlatformTransactionManager transactionManager, RagSourceAuthority authority, RagWorkerGrantService grants, RagLimits limits,
                         com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations reservations,
                         RagRuntimeIdentity runtime,
                         RagWorkerSupervisor supervisor,
                         RagGenerationState current,
                         @Value("${ppv.rag.limits.lease-duration-minutes:15}") int leaseDurationMinutes) {
        this.jobLeaseRepository = jobLeaseRepository;
        this.stagedOutputRepository = stagedOutputRepository;
        this.runRepository = runRepository;
        this.purgeService = purgeService;
        this.engineClient = engineClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setTimeout(10);
        this.authority = authority;
        this.grants = grants;
        this.limits = limits;
        this.reservations = reservations;
        this.runtime = runtime;
        this.supervisor=supervisor;
        this.current=current;
        if (leaseDurationMinutes < 1 || leaseDurationMinutes > 15)
            throw new IllegalArgumentException("Invalid lease duration budget");
        this.leaseDuration = Duration.ofMinutes(leaseDurationMinutes);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public RagJobAdmissionResult admitJob(Long sourceId) {
        throw new IllegalArgumentException("Explicit source and policy versions are required");
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public RagJobAdmissionResult admitJob(Long sourceId, com.vhvkhangg.personalprivatevault.rag.api.RagIndexCommand command) {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(command);
        var prepared = authority.prepareSource(sourceId, command.descriptionConfirmations());
        try {
        return transactionTemplate.execute(status -> {
        RagSourceAuthority.Snapshot snapshot = authority.lockPrepared(prepared, null, true);
        var rootProof = prepared.proofs().get(sourceId);
        if (rootProof.version() != command.expectedSourceVersion()
                || rootProof.epoch() != command.expectedAuthorizationEpoch()
                || rootProof.policy() != command.expectedFieldPolicyVersion())
            throw new com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException();
        if (jobLeaseRepository.count() >= limits.retainedJobs() || jobLeaseRepository.findBySourceId(sourceId).size() >= limits.jobsPerSource()) {
            throw new IllegalStateException("Retained job budget exceeded");
        }
        RagSource source = snapshot.root();
        Instant now = authority.databaseTime();
        UUID jobId = UUID.randomUUID(), attemptId = UUID.randomUUID();
        String token = generateLeaseToken();
        Instant deadline = now.plus(leaseDuration);
        RagJobLease lease = new RagJobLease(jobId, attemptId, sourceId, "ADMITTED", token, deadline,
            source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), source.getApprovedFieldsText(),
            source.getSourceRevisionHash(), source.getDescriptionRevisionHash(), snapshot.manifest(), now);
        lease.bindController(java.util.Map.of("process_id", ProcessHandle.current().pid(),
                "started_at", ProcessHandle.current().info().startInstant().orElseThrow().toString(),
                "runtime_id", runtime.instance().toString()));
        jobLeaseRepository.saveAndFlush(lease);
        var ownerIds = new java.util.TreeSet<Long>();
        prepared.proofs().values().forEach(proof -> ownerIds.add(proof.ownerId()));
        reservations.reserve(ownerIds, attemptId);
        runRepository.save(new RagRun(UUID.randomUUID(), jobId, sourceId, source.getAuthorizationEpoch(),
            "ADMITTED", "{}", "NONE", now));
        return new RagJobAdmissionResult(jobId, attemptId, sourceId, token, deadline, source.getAuthorizationEpoch());
        });
        } catch (org.springframework.dao.DataIntegrityViolationException collision) {
            for (Throwable cause = collision; cause != null; cause = cause.getCause()) {
                if (cause instanceof java.sql.SQLException sql && "23505".equals(sql.getSQLState()))
                    throw new com.vhvkhangg.personalprivatevault.rag.api.RagJobBusyException();
            }
            throw collision;
        }
    }

    private String generateLeaseToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    public RagJobStatusView getJobStatus(UUID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        List<RagJobLease> leases = jobLeaseRepository.findByJobId(jobId);
        if (leases.isEmpty()) {
            throw new NoSuchElementException("Job not found with id: " + jobId);
        }
        RagJobLease latest = leases.get(leases.size() - 1);
        String purgeStatus = runRepository.findByJobId(jobId)
            .map(RagRun::getPurgeStatus)
            .orElse("NONE");

        return new RagJobStatusView(
            latest.getJobId(),
            latest.getAttemptId(),
            latest.getSourceId(),
            latest.getStatus(),
            current.stateForJob(jobId),
            latest.getLeasedUntil(),
            purgeStatus,
            latest.getCreatedAt(),
            latest.getUpdatedAt()
        );
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public void cancelJob(UUID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        if(!purgeService.abortJob(jobId)) throw new IllegalStateException("Safe cancellation is not yet verified");

    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public void executeSyntheticStage(UUID jobId, UUID attemptId, String stageType) {
        String stage = stageType == null || stageType.isBlank() ? "SYNTHETIC_STAGE" : stageType;
        try { supervisor.begin(jobId,attemptId); }
        catch(RagWorkerSupervisor.StartupFailure startupFailure) {
            // A duplicate request must never stop the first dispatch. Only the
            // supervisor's positive, owned termination proof permits cleanup.
            if(supervisor.proveStopped(jobId)) {
                try { purgeService.abortJob(jobId,false); }
                catch(RuntimeException unknownSql) { purgeService.requireRecovery(jobId); }
            }
            throw startupFailure;
        }
        boolean succeeded=false;
        try {
        var grant = grants.issue(jobId, attemptId, stage);
        var session=grants.executionSession(jobId,attemptId);
        String receipt = engineClient.triggerBoundWorkerStage(grant,session,supervisor.requireReady(jobId,attemptId));
        var initial=jobLeaseRepository.findByJobIdAndAttemptId(jobId,attemptId).orElseThrow();
        var prepared=authority.prepareSource(initial.getSourceId(),java.util.Map.of());
        transactionTemplate.executeWithoutResult(status -> {
            var attempt = authority.lockPreparedAttempt(jobId, attemptId,prepared,false);
            grants.verifyDurableReceipt(attempt.lease(), stage, receipt);
            var rows = stagedOutputRepository.findByJobIdAndAttemptId(jobId, attemptId);
            if (rows.size() != 1) throw new IllegalStateException("Worker result has no unique durable receipt");
            grants.verifyDurableReceipt(attempt.lease(), rows.getFirst().getStageType(), rows.getFirst().getPayload());
        });
        succeeded=true;
        } finally {
            supervisor.end(jobId);
            boolean stopped;
            try { stopped=supervisor.stopAndVerify(jobId); }
            catch(RuntimeException uncertainTermination) { stopped=false; }
            if(!stopped) {
                purgeService.requireRecovery(jobId);
                if(succeeded) throw new IllegalStateException("Worker termination is not yet verified");
            } else if(!succeeded) {
                try { purgeService.abortJob(jobId,false); }
                catch(RuntimeException unknownSql) { purgeService.requireRecovery(jobId); }
            }
        }
    }
}
