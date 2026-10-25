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
        Instant finishedAt,
        UUID operationId
) {
    public RagDocumentBatchReportResponse(UUID job,String status,String summary,List<FileOutcomeDto> outcomes,String artifact,String model,Long ram,Long vram,Instant created,Instant finished) {
        this(job,status,summary,outcomes,artifact,model,ram,vram,created,finished,null);
    }
}
