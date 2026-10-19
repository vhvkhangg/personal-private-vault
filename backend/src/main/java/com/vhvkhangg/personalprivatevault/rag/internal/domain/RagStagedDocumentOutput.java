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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "staged_document_outputs", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagStagedDocumentOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "root_key", nullable = false, length = 255)
    private String rootKey;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "reason_code", nullable = false, length = 64)
    private String reasonCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warning_codes", columnDefinition = "jsonb", nullable = false)
    private List<String> warningCodes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chunks_payload", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> chunksPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_report", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> resourceReport;

    @Column(name = "worker_receipt", nullable = false)
    private boolean workerReceipt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagStagedDocumentOutput(
            UUID jobId,
            UUID attemptId,
            String rootKey,
            boolean success,
            String reasonCode,
            List<String> warningCodes,
            List<Map<String, Object>> chunksPayload,
            Map<String, Object> resourceReport,
            Instant now
    ) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.attemptId = Objects.requireNonNull(attemptId, "attemptId must not be null");
        this.rootKey = Objects.requireNonNull(rootKey, "rootKey must not be null");
        this.success = success;
        this.reasonCode = Objects.requireNonNull(reasonCode, "reasonCode must not be null");
        this.warningCodes = warningCodes != null ? List.copyOf(warningCodes) : List.of();
        this.chunksPayload = chunksPayload != null ? List.copyOf(chunksPayload) : List.of();
        this.resourceReport = resourceReport != null ? Map.copyOf(resourceReport) : Map.of();
        this.workerReceipt = true;
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }
}
