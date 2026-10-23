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
@Table(name = "job_leases", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagJobLease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "lease_token", nullable = false, length = 128)
    private String leaseToken;

    @Column(name = "leased_until", nullable = false)
    private Instant leasedUntil;

    @Column(name = "admitted_epoch", nullable = false)
    private Long admittedEpoch;

    @Column(name = "admitted_field_policy_version", nullable = false)
    private Integer admittedFieldPolicyVersion;

    @Column(name = "approved_fields_text", nullable = false, columnDefinition = "TEXT")
    private String approvedFieldsText;

    @Column(name = "content_revision_hash", nullable = false, length = 128)
    private String contentRevisionHash;

    @Column(name = "description_revision_hash", nullable = false, length = 128)
    private String descriptionRevisionHash;

    @Column(name = "dependency_manifest", nullable = false, columnDefinition = "TEXT")
    private String dependencyManifest;

    @Column(name = "reservation_token", nullable = false)
    private UUID reservationToken;

    @Column(name = "staging_closed", nullable = false)
    private boolean stagingClosed;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "controller_identity", columnDefinition = "jsonb")
    private java.util.Map<String, Object> controllerIdentity;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "worker_identity", columnDefinition = "jsonb")
    private java.util.Map<String, Object> workerIdentity;

    public void bindController(java.util.Map<String, Object> identity) {
        if (controllerIdentity != null) throw new IllegalStateException("Controller already bound");
        controllerIdentity = java.util.Map.copyOf(identity);
    }

    public void bindWorker(java.util.Map<String, Object> identity) {
        if (workerIdentity != null) throw new IllegalStateException("Worker already bound");
        workerIdentity = java.util.Map.copyOf(identity);
    }

    public void bindVerification(java.util.Map<String,Object> identity) {
        if (workerIdentity==null || workerIdentity.containsKey("verification"))
            throw new IllegalStateException("Document verification is single use");
        var updated=new java.util.HashMap<>(workerIdentity);
        updated.put("verification",java.util.Map.copyOf(identity)); workerIdentity=java.util.Map.copyOf(updated);
    }

    public void sealStaging() { stagingClosed = true; }

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RagJobLease(
            UUID jobId,
            UUID attemptId,
            Long sourceId,
            String status,
            String leaseToken,
            Instant leasedUntil,
            Long admittedEpoch,
            Integer admittedFieldPolicyVersion,
            String approvedFieldsText,
            String contentRevisionHash,
            String descriptionRevisionHash,
            String dependencyManifest,
            Instant now) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.attemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        this.reservationToken = attemptId;
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.leaseToken = Objects.requireNonNull(leaseToken, "leaseToken must not be null");
        this.leasedUntil = Objects.requireNonNull(leasedUntil, "leasedUntil must not be null");
        this.admittedEpoch = admittedEpoch != null ? admittedEpoch : 1L;
        this.admittedFieldPolicyVersion = admittedFieldPolicyVersion != null ? admittedFieldPolicyVersion : 1;
        this.approvedFieldsText = approvedFieldsText != null ? approvedFieldsText : "";
        this.contentRevisionHash = contentRevisionHash != null ? contentRevisionHash : "";
        this.descriptionRevisionHash = descriptionRevisionHash != null ? descriptionRevisionHash : "";
        this.dependencyManifest = dependencyManifest != null ? dependencyManifest : "";
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
    }

    public RagJobLease(
            UUID jobId,
            UUID attemptId,
            Long sourceId,
            String status,
            String leaseToken,
            Instant leasedUntil,
            Long admittedEpoch,
            Integer admittedFieldPolicyVersion,
            String approvedFieldsText,
            String contentRevisionHash,
            String descriptionRevisionHash,
            Instant now) {
        this(jobId, attemptId, sourceId, status, leaseToken, leasedUntil, admittedEpoch, admittedFieldPolicyVersion,
                approvedFieldsText, contentRevisionHash, descriptionRevisionHash, "", now);
    }

    public void updateStatus(String newStatus, Instant now) {
        this.status = Objects.requireNonNull(newStatus, "newStatus must not be null");
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    public boolean isExpired(Instant now) {
        return !leasedUntil.isAfter(now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RagJobLease other)) return false;
        return Objects.equals(jobId, other.jobId) && Objects.equals(attemptId, other.attemptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, attemptId);
    }
}
