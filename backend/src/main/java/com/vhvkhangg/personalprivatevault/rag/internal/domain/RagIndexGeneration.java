package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "index_generations", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagIndexGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "generation_id", nullable = false, unique = true)
    private UUID generationId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "authorization_epoch", nullable = false)
    private long authorizationEpoch;

    @Column(name = "source_revision_hash", nullable = false, length = 128)
    private String sourceRevisionHash;

    @Column(name = "dependency_manifest_digest", nullable = false, length = 128)
    private String dependencyManifestDigest;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public RagIndexGeneration(
            UUID generationId,
            UUID jobId,
            Long sourceId,
            String status,
            long authorizationEpoch,
            String sourceRevisionHash,
            String dependencyManifestDigest,
            boolean active,
            Instant now) {
        this(generationId, sourceId, status, authorizationEpoch, sourceRevisionHash, dependencyManifestDigest, active, now);
        this.jobId = jobId;
    }

    public RagIndexGeneration(
            UUID generationId,
            Long sourceId,
            String status,
            long authorizationEpoch,
            String sourceRevisionHash,
            String dependencyManifestDigest,
            boolean active,
            Instant now) {
        this.generationId = Objects.requireNonNull(generationId, "generationId must not be null");
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.authorizationEpoch = authorizationEpoch;
        this.sourceRevisionHash = Objects.requireNonNull(sourceRevisionHash, "sourceRevisionHash must not be null");
        this.dependencyManifestDigest = Objects.requireNonNull(dependencyManifestDigest, "dependencyManifestDigest must not be null");
        this.active = active;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        if (active) {
            this.publishedAt = now;
        }
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public void publish(Instant now) {
        this.status = "PUBLISHED";
        this.active = true;
        this.publishedAt = Objects.requireNonNull(now, "now must not be null");
        this.revokedAt = null;
    }

    public void supersede(Instant now) {
        this.status = "SUPERSEDED";
        this.active = false;
    }

    public void revoke(Instant now) {
        this.status = "REVOKED";
        this.active = false;
        this.revokedAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RagIndexGeneration other)) return false;
        return Objects.equals(generationId, other.generationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generationId);
    }
}
