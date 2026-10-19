package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Result of trusted Java physical batch finalization (ADR-0021 Clause 6).
 */
public record RagBatchFinalizationResult(
        UUID jobId,
        String jobStatus,
        int publishedCount,
        int failedCount,
        Map<String, UUID> publishedGenerations
) {
    public RagBatchFinalizationResult {
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(jobStatus, "jobStatus must not be null");
        publishedGenerations = publishedGenerations != null ? Map.copyOf(publishedGenerations) : Map.of();
    }
}
