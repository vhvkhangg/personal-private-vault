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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "runs", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true)
    private UUID runId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "authorization_epoch", nullable = false)
    private long authorizationEpoch;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metrics", columnDefinition = "jsonb")
    private String metrics;

    @Column(name = "purge_status", nullable = false, length = 32)
    private String purgeStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RagRun(
            UUID runId,
            UUID jobId,
            Long sourceId,
            long authorizationEpoch,
            String status,
            String metrics,
            String purgeStatus,
            Instant now) {
        this.runId = Objects.requireNonNull(runId, "runId must not be null");
        this.jobId = jobId;
        this.sourceId = sourceId;
        this.authorizationEpoch = authorizationEpoch;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.metrics = metrics;
        this.purgeStatus = Objects.requireNonNull(purgeStatus, "purgeStatus must not be null");
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
        this.updatedAt = now;
    }

    public void updatePurgeStatus(String newPurgeStatus, Instant now) {
        this.purgeStatus = Objects.requireNonNull(newPurgeStatus, "newPurgeStatus must not be null");
        this.updatedAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RagRun other)) return false;
        return Objects.equals(runId, other.runId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId);
    }
}
