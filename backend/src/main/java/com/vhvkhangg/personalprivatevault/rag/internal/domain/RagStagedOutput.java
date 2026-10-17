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
@Table(name = "staged_outputs", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagStagedOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "lease_token", length = 128)
    private String leaseToken;

    @Column(name = "stage_type", nullable = false, length = 64)
    private String stageType;

    @Column(name = "payload")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagStagedOutput(UUID jobId, UUID attemptId, String leaseToken, String stageType, String payload, Instant now) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.attemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        this.leaseToken = leaseToken;
        this.stageType = Objects.requireNonNull(stageType, "stageType must not be null");
        this.payload = payload;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }

    public RagStagedOutput(UUID jobId, UUID attemptId, String stageType, String payload, Instant now) {
        this(jobId, attemptId, null, stageType, payload, now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RagStagedOutput other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
