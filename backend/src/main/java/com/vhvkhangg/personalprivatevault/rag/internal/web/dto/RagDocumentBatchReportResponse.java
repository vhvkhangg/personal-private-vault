package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RagDocumentBatchReportResponse(
        UUID jobId,
        String status,
        String summaryOutcome,
        List<FileOutcomeDto> outcomes,
        String modelArtifactHash,
        String modelVersion,
        Long peakSystemRamBytes,
        Long peakGpuVramBytes,
        Instant createdAt,
        Instant finishedAt
) {
}
