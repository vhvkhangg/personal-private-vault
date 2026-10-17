package com.vhvkhangg.personalprivatevault.rag.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Sanitized, non-content-bearing job status view.
 */
public record RagJobStatusView(
        UUID jobId,
        UUID attemptId,
        Long sourceId,
        String status,
        String indexState,
        Instant leasedUntil,
        String purgeStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public RagJobStatusView {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(indexState, "indexState must not be null");
        Objects.requireNonNull(purgeStatus, "purgeStatus must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
