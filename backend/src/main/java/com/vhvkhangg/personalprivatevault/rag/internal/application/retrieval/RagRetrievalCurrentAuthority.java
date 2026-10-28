package com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval;

import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;

import java.util.*;

/** Bridges expensive canonical preparation to the short admission/emission transaction. */
@Component
public class RagRetrievalCurrentAuthority {
    private final RagSourceAuthority sources;
    private final RagGenerationState current;
    private final RagRuntimeSecurity security;
    private final RagJobLeaseRepository leases;
    private final RagIndexGenerationRepository generations;
    private final RagGenerationRootRepository roots;
    private final EntityManager entityManager;

    public RagRetrievalCurrentAuthority(RagSourceAuthority sources, RagGenerationState current,
            RagRuntimeSecurity security, RagJobLeaseRepository leases, RagIndexGenerationRepository generations,
            RagGenerationRootRepository roots, EntityManager entityManager) {
        this.sources=sources; this.current=current; this.security=security;
        this.leases=leases; this.generations=generations; this.roots=roots;
        this.entityManager=entityManager;
    }

    public record Proof(UUID generationId, UUID jobId, UUID attemptId,
                        RagSourceAuthority.Prepared source, String manifest, String producerManifest) {}

    public Proof prepare(RagIndexGeneration generation) {
        return prepare(generation,null);
    }

    public Proof prepare(RagIndexGeneration generation, Map<UUID,RagSourceAuthority.SelectedSnapshot> inspected) {
        var full = inspected==null ? sources.prepareSource(generation.getSourceId(),Map.of())
                : sources.prepareSelectedSource(generation.getSourceId(),inspected);
        // Read guards need authority metadata, never a retained copy of the prepared corpus.
        var prepared = new RagSourceAuthority.Prepared(full.rootId(),full.proofs(),full.manifest(), "");
        var lease = leases.findByJobId(generation.getJobId()).stream().findFirst().orElseThrow();
        var root = roots.findById(generation.getId()).orElse(null);
        String manifest = root != null && root.getExactDependencyManifest().get("manifest_text") instanceof String text
                ? text : lease.getDependencyManifest();
        if (!"COMPLETED".equals(lease.getStatus()) || !lease.isStagingClosed()
                || !generation.isActive() || !"PUBLISHED".equals(generation.getStatus())
                || !prepared.manifest().equals(manifest)
                || !RagSourceAuthority.digest(manifest).equals(generation.getDependencyManifestDigest())
                || (root != null && !current.isPipelineCurrent(root))) {
            throw new com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException();
        }
        String producerManifest = root != null && root.getExactDependencyManifest().get("input_manifest_text") instanceof String input
                ? input : lease.getDependencyManifest();
        return new Proof(generation.getGenerationId(), generation.getJobId(), lease.getAttemptId(), prepared, manifest,
                producerManifest);
    }

    /** Caller owns one short writable transaction; no bytes, child or transport I/O here. */
    public void lock(Collection<Proof> proofs) {
        security.requireIsolatedRuntime();
        sources.lockPreparedCurrentBatch(proofs.stream().map(Proof::source).toList());
        security.requireIsolatedRuntime();
        for (var proof : proofs.stream().sorted(Comparator.comparing(Proof::generationId)).toList()) {
            var lease=leases.findByJobIdAndAttemptIdForUpdate(proof.jobId(),proof.attemptId()).orElseThrow();
            entityManager.refresh(lease);
            var generation=generations.findByGenerationId(proof.generationId()).orElseThrow();
            entityManager.refresh(generation,LockModeType.PESSIMISTIC_READ);
            var root=roots.findById(generation.getId()).orElse(null);
            if (root != null) entityManager.refresh(root,LockModeType.PESSIMISTIC_READ);
            String manifest=root != null && root.getExactDependencyManifest().get("manifest_text") instanceof String text
                    ? text : lease.getDependencyManifest();
            String inputManifest=root != null && root.getExactDependencyManifest().get("input_manifest_text") instanceof String input
                    ? input : lease.getDependencyManifest();
            if (!"COMPLETED".equals(lease.getStatus()) || !lease.isStagingClosed()
                    || !generation.isActive() || !"PUBLISHED".equals(generation.getStatus())
                    || !generation.getSourceId().equals(proof.source().rootId())
                    || !proof.manifest().equals(manifest)
                    || !proof.producerManifest().equals(inputManifest)
                    || !RagSourceAuthority.digest(manifest).equals(generation.getDependencyManifestDigest())
                    || (root != null && !current.isPipelineCurrent(root))) {
                throw new IllegalStateException("Guarded index proof changed");
            }
        }
    }
}
