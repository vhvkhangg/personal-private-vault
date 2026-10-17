package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Validates immutable issued receipt scope. New issuance is contained until a current
 * owning-source consumption fence is proven (FR17-5).
 */
@Component
public class RagWorkerGrantService {
    private final RagSourceAuthority authority;
    private final EntityManager entityManager;
    private final RagWorkerSupervisor supervisor;
    private final org.springframework.transaction.support.TransactionTemplate transactions;
    private final com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository leases;
    private final ObjectMapper json = new ObjectMapper().enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

    public RagWorkerGrantService(RagSourceAuthority authority, EntityManager entityManager) {
        this(authority,entityManager,null,null,null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public RagWorkerGrantService(RagSourceAuthority authority, EntityManager entityManager, RagWorkerSupervisor supervisor,
            org.springframework.transaction.PlatformTransactionManager manager,
            com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository leases) {
        this.authority = authority;
        this.entityManager = entityManager;
        this.supervisor=supervisor; this.leases=leases;
        this.transactions=manager==null?null:new org.springframework.transaction.support.TransactionTemplate(manager);
        if(transactions!=null) transactions.setTimeout(10);
    }

    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NEVER)
    public RagWorkerGrant issue(UUID jobId, UUID attemptId, String stageType) {
        if(supervisor==null) RagWorkerGrant.requireProvenConsumptionFence();
        supervisor.requireReady(jobId,attemptId);
        if (!Set.of("SYNTHETIC_STAGE", "SYNTHETIC_TEST").contains(stageType)) {
            throw new IllegalArgumentException("Unsupported foundation stage");
        }
        var initial=leases.findByJobIdAndAttemptId(jobId,attemptId).orElseThrow();
        var prepared=authority.prepareSource(initial.getSourceId(),Map.of());
        String input = RagSourceAuthority.digest(prepared.payload());
        String scope = RagSourceAuthority.digest(prepared.manifest());
        String output = RagSourceAuthority.digest(stageType + ":" + prepared.payload());
        return transactions.execute(status -> {
        supervisor.requireReady(jobId,attemptId);
        var proof = authority.lockPreparedAttempt(jobId, attemptId,prepared,false);
        if(!"ACTIVE".equals(proof.lease().getStatus())) throw new IllegalStateException("Worker dispatch is inactive");
        entityManager.createNativeQuery("""
                INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,output_digest,stage_type)
                VALUES (:job,:attempt,:session,:until,:input,:scope,:output,:stage)
                """).setParameter("job", jobId).setParameter("attempt", attemptId)
            .setParameter("session", UUID.randomUUID()).setParameter("until", proof.lease().getLeasedUntil())
            .setParameter("input", input).setParameter("scope", scope).setParameter("output", output)
            .setParameter("stage", stageType).executeUpdate();
        RagWorkerGrant grant = new RagWorkerGrant(jobId, attemptId, proof.lease().getLeaseToken(), stageType, proof.snapshot().payload(), input, scope, output);
        requireIssued(grant);
        proof.lease().updateStatus("ACTIVE", authority.databaseTime());
        return grant;
        });
    }

    private void requireIssued(RagWorkerGrant grant) {
        requireIssued(grant,false,false);
    }

    @Transactional(readOnly=true)
    public UUID executionSession(UUID job, UUID attempt) {
        supervisor.requireReady(job,attempt);
        return (UUID)entityManager.createNativeQuery("SELECT session_id FROM rag.worker_grants WHERE job_id=:job AND attempt_id=:attempt AND consumed_at IS NULL AND session_until>clock_timestamp()")
                .setParameter("job",job).setParameter("attempt",attempt).getSingleResult();
    }

    private void requireIssued(RagWorkerGrant grant, boolean consumed, boolean completed) {
        Number matches = (Number) entityManager.createNativeQuery("""
                SELECT count(*) FROM rag.worker_grants WHERE job_id=:job AND attempt_id=:attempt
                  AND input_digest=:input AND scope_digest=:scope AND output_digest=:output AND stage_type=:stage
                  AND (:completed OR session_until>clock_timestamp()) AND (NOT :consumed OR consumed_at IS NOT NULL)
                """).setParameter("job", grant.jobId()).setParameter("attempt", grant.attemptId())
            .setParameter("input", grant.inputDigest()).setParameter("scope", grant.scopeDigest())
            .setParameter("output", grant.outputDigest()).setParameter("stage", grant.stageType())
            .setParameter("completed",completed).setParameter("consumed",consumed).getSingleResult();
        if (matches.intValue() != 1) throw new IllegalStateException("Conflicting or expired worker issuance");
    }

    public void verifyDurableReceipt(RagJobLease lease, String stageType, String receipt) {
        try {
            var node = json.readTree(receipt);
            if (node == null || !node.isObject() || java.util.stream.StreamSupport.stream(node.spliterator(), false).anyMatch(value -> !value.isTextual()))
                throw new IllegalStateException("Invalid typed worker receipt");
            if (!node.isObject() || node.size() != 9 || !"2.0".equals(node.path("contract_version").asText())
                || !"STAGED".equals(node.path("status").asText()) || !"SYNTHETIC_DIGEST".equals(node.path("output_kind").asText())
                || !lease.getJobId().toString().equals(node.path("job_id").asText())
                || !lease.getAttemptId().toString().equals(node.path("attempt_id").asText())
                || !stageType.equals(node.path("stage_type").asText()))
                throw new IllegalStateException("Invalid typed worker receipt");
            RagWorkerGrant g = new RagWorkerGrant(lease.getJobId(), lease.getAttemptId(), lease.getLeaseToken(), stageType, "",
                node.path("input_digest").asText(), node.path("scope_digest").asText(), node.path("output_digest").asText());
            for (String digest : List.of(g.inputDigest(), g.scopeDigest(), g.outputDigest()))
                if (!digest.matches("[0-9a-f]{64}")) throw new IllegalStateException("Invalid worker digest");
            requireIssued(g,true,"COMPLETED".equals(lease.getStatus()));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Invalid worker receipt", e);
        }
    }
}
