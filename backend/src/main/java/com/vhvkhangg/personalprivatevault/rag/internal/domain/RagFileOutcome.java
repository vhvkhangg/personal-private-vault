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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "file_outcomes", schema = "rag")
@IdClass(RagFileOutcome.RagFileOutcomeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagFileOutcome {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Id
    @Column(name = "root_key", nullable = false, length = 255)
    private String rootKey;

    @Column(name = "result", nullable = false, length = 32)
    private String result;

    @Column(name = "candidate_generation_id")
    private UUID candidateGenerationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warning_codes", columnDefinition = "jsonb", nullable = false)
    private List<String> warningCodes;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "page_count", nullable = false)
    private int pageCount;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagFileOutcome(
            UUID jobId,
            String rootKey,
            String result,
            UUID candidateGenerationId,
            List<String> warningCodes,
            int chunkCount,
            int pageCount,
            String errorCode,
            String errorMessage,
            Instant now
    ) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.rootKey = Objects.requireNonNull(rootKey, "rootKey must not be null");
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.candidateGenerationId = candidateGenerationId;
        this.warningCodes = warningCodes != null ? List.copyOf(warningCodes) : List.of();
        this.chunkCount = chunkCount;
        this.pageCount = pageCount;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class RagFileOutcomeId implements Serializable {
        private UUID jobId;
        private String rootKey;

        public RagFileOutcomeId(UUID jobId, String rootKey) {
            this.jobId = jobId;
            this.rootKey = rootKey;
        }
    }
}
