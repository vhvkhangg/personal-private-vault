package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.util.Map;
import java.util.UUID;

public record RagBatchFinalizationResponse(
        UUID jobId,
        String jobStatus,
        int publishedCount,
        int failedCount,
        Map<String, UUID> publishedGenerations
) {
}
