package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "batch_roots", schema = "rag")
@IdClass(RagBatchRoot.RagBatchRootId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagBatchRoot {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Id
    @Column(name = "root_key", nullable = false, length = 255)
    private String rootKey;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "expected_source_version", nullable = false)
    private Long expectedSourceVersion;

    @Column(name = "expected_authorization_epoch", nullable = false)
    private Long expectedAuthorizationEpoch;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagBatchRoot(
            UUID jobId,
            String rootKey,
            Long sourceId,
            Long expectedSourceVersion,
            Long expectedAuthorizationEpoch,
            Instant now
    ) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.rootKey = Objects.requireNonNull(rootKey, "rootKey must not be null");
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.expectedSourceVersion = expectedSourceVersion;
        this.expectedAuthorizationEpoch = expectedAuthorizationEpoch;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class RagBatchRootId implements Serializable {
        private UUID jobId;
        private String rootKey;

        public RagBatchRootId(UUID jobId, String rootKey) {
            this.jobId = jobId;
            this.rootKey = rootKey;
        }
    }
}
