package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;

/** Fresh index authority, independent of historical execution deadlines and boot. */
@Service
public class RagGenerationState {
    private final RagSourceAuthority authority;
    private final RagIndexGenerationRepository generations;
    private final RagJobLeaseRepository leases;
    private final RagSourceRepository sources;
    private final SourceIndexReservations reservations;
    private final EntityManager entityManager;
    private final TransactionTemplate transaction;
    public RagGenerationState(RagSourceAuthority authority,RagIndexGenerationRepository generations,
            RagJobLeaseRepository leases,RagSourceRepository sources,SourceIndexReservations reservations,
            EntityManager entityManager,PlatformTransactionManager transactions) {
        this.authority=authority; this.generations=generations; this.leases=leases; this.sources=sources;
        this.reservations=reservations; this.entityManager=entityManager;
        transaction=new TransactionTemplate(transactions); transaction.setTimeout(10);
    }
    public String stateForJob(UUID job) {
        return generations.findByJobId(job).stream().findFirst().map(g -> state(g.getGenerationId())).orElse("ABSENT");
    }
    public String state(UUID generationId) {
        if (TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Index authorization must prepare outside an ambient transaction");
        try {
            var initial=generations.findByGenerationId(generationId).orElse(null);
            if (initial==null) return "ABSENT";
            if (!initial.isActive()) return "REVOKED".equals(initial.getStatus())?"REVOKED":"OUTDATED";
            var lease=leases.findByJobId(initial.getJobId()).stream().findFirst().orElse(null);
            if (lease==null || !"COMPLETED".equals(lease.getStatus()) || !lease.isStagingClosed()) return "UNAVAILABLE";
            var manifest=RagManifestScope.parse(lease.getDependencyManifest());
            var owners=reservations.read(RagManifestScope.owners(lease.getDependencyManifest()));
            var selected=new TreeSet<Long>();
            manifest.path("nodes").forEach(node -> selected.add(node.path("source_id").asLong()));
            var policy=new HashMap<Long,com.vhvkhangg.personalprivatevault.rag.internal.domain.RagSource>();
            sources.findAllById(selected).forEach(source -> policy.put(source.getId(),source));
            boolean outdated=false;
            for (var node:manifest.path("nodes")) {
                var source=policy.get(node.path("source_id").asLong());
                var owner=owners.require(Long.parseLong(node.path("domain_id").asText()));
                if (source==null || owner.deleted() || source.getClassification()!=RagClassification.NON_PRIVATE
                        || source.getAccessState()!=RagAccessState.APPROVED
                        || !Objects.equals(source.getApprovedOwnerResetVersion(),owner.consentResetVersion())
                        || owner.consentResetVersion()!=node.path("owner_reset_version").asLong()) return "REVOKED";
                for(var field:node.path("fields"))
                    if(!source.getApprovedFields().contains(field.asText())) return "REVOKED";
                if (owner.sourceVersion()!=node.path("owner_source_version").asLong()) outdated=true;
                else if (source.getAuthorizationEpoch()!=node.path("epoch").asLong()
                        || source.getFieldPolicyVersion()!=node.path("field_version").asInt()) return "REVOKED";
            }
            // Check every old dependency for privacy loss before reporting an
            // ordinary version/scope edit. No stale result is served either way.
            if(outdated) return "OUTDATED";
            var prepared=authority.prepareSource(initial.getSourceId(),Map.of());
            String digest=RagSourceAuthority.digest(prepared.manifest());
            if (!prepared.manifest().equals(lease.getDependencyManifest()) || !digest.equals(initial.getDependencyManifestDigest())) return "OUTDATED";
            return transaction.execute(status -> {
                authority.lockPreparedCurrent(prepared);
                var locked=leases.findByJobIdAndAttemptIdForUpdate(lease.getJobId(),lease.getAttemptId()).orElseThrow();
                entityManager.refresh(locked);
                var generation=generations.findByGenerationId(generationId).orElseThrow();
                entityManager.refresh(generation,jakarta.persistence.LockModeType.PESSIMISTIC_READ);
                return "COMPLETED".equals(locked.getStatus()) && locked.isStagingClosed() && generation.isActive()
                        && "PUBLISHED".equals(generation.getStatus()) && locked.getDependencyManifest().equals(prepared.manifest())
                        && generation.getDependencyManifestDigest().equals(digest)?"CURRENT":"OUTDATED";
            });
        } catch (RuntimeException unproven) {
            // A database/authority outage denies access but is not proof that a valid
            // index should be irreversibly erased. Reconciliation retries later.
            return "UNAVAILABLE";
        }
    }
}
