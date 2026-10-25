package com.vhvkhangg.personalprivatevault.rag.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Structured document batch indexing report (P18-10, AC-08).
 * Note: summaryOutcome distinguishes COMPLETED, ABORTED, CANCELLED, RECOVERY_REQUIRED,
 * and report-only PARTIAL_SUCCESS without mutating the DB status enum.
 */
public record RagDocumentBatchReport(
        UUID jobId,
        String status,
        String summaryOutcome,
        List<RagFileOutcomeView> outcomes,
        String modelArtifactHash,
        String modelVersion,
        Long peakSystemRamBytes,
        Long peakGpuVramBytes,
        Instant createdAt,
        Instant finishedAt,
        UUID operationId
) {
    public RagDocumentBatchReport(UUID job,String status,String summary,List<RagFileOutcomeView> outcomes,String artifact,String model,Long ram,Long vram,Instant created,Instant finished) {
        this(job,status,summary,outcomes,artifact,model,ram,vram,created,finished,null);
    }
    public RagDocumentBatchReport {
        if(operationId==null) {
            Objects.requireNonNull(jobId,"jobId must not be null");Objects.requireNonNull(status,"status must not be null");
        } else if(jobId!=null || status!=null)throw new IllegalArgumentException("Nonpublication report cannot claim an indexing job status");
        Objects.requireNonNull(summaryOutcome, "summaryOutcome must not be null");
        outcomes = outcomes != null ? List.copyOf(outcomes) : List.of();
    }
}
