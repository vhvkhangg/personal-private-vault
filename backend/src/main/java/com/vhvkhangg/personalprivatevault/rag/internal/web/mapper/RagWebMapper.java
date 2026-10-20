package com.vhvkhangg.personalprivatevault.rag.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobStatusView;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagFinalizationResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagJobAdmissionResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagJobStatusResponse;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagSourceResponse;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;

public final class RagWebMapper {

    private RagWebMapper() {}

    public static RagSourceResponse toResponse(RagSourceView view) {
        if (view == null) return null;
        String sourceId = view.sourceId();
        if (view.sourceType() == RagSourceType.EXTERNAL_FILE && sourceId != null) {
            try {
                java.nio.file.Path p = java.nio.file.Path.of(sourceId);
                java.nio.file.Path fn = p.getFileName();
                sourceId = fn != null ? fn.toString() : "file";
            } catch (Exception ignored) {
                sourceId = "file";
            }
        }
        return new RagSourceResponse(
                view.id(),
                view.sourceType(),
                sourceId,
                view.classification(),
                view.authorizationEpoch(),
                view.fieldPolicyVersion(),
                view.approvedFields(),
                view.accessState(),
                view.sourceRevisionHash(),
                view.descriptionRevisionHash(),
                view.createdAt(),
                view.updatedAt(),
                view.revokedAt(),
                view.sourceVersion(),
                view.consentResetVersion(),
                view.currentDescriptionRevisionHash(),
                view.snapshot()
        );
    }

    public static RagJobAdmissionResponse toResponse(RagJobAdmissionResult result) {
        if (result == null) return null;
        return new RagJobAdmissionResponse(
                result.jobId(),
                result.attemptId(),
                result.sourceId(),
                result.leaseToken(),
                result.leasedUntil(),
                result.authorizationEpoch()
        );
    }

    public static RagJobStatusResponse toResponse(RagJobStatusView view) {
        if (view == null) return null;
        return new RagJobStatusResponse(
                view.jobId(),
                view.attemptId(),
                view.sourceId(),
                view.status(),
                view.indexState(),
                view.leasedUntil(),
                view.purgeStatus(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static RagFinalizationResponse toResponse(RagFinalizationResult result) {
        if (result == null) return null;
        return new RagFinalizationResponse(
                result.generationId(),
                result.sourceId(),
                result.authorizationEpoch(),
                result.status(),
                result.active(),
                result.publishedAt()
        );
    }

    public static com.vhvkhangg.personalprivatevault.rag.internal.web.dto.FileOutcomeDto toResponse(com.vhvkhangg.personalprivatevault.rag.api.RagFileOutcomeView view) {
        if (view == null) return null;
        return new com.vhvkhangg.personalprivatevault.rag.internal.web.dto.FileOutcomeDto(
                view.rootKey(),
                view.result(),
                view.candidateGenerationId(),
                view.warningCodes(),
                view.chunkCount(),
                view.pageCount(),
                view.errorCode(),
                view.errorMessage(),
                view.currentIndexState()
        );
    }

    public static com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagDocumentBatchReportResponse toResponse(com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchReport report) {
        if (report == null) return null;
        java.util.List<com.vhvkhangg.personalprivatevault.rag.internal.web.dto.FileOutcomeDto> outcomeDtos = report.outcomes() != null
                ? report.outcomes().stream().map(RagWebMapper::toResponse).toList()
                : java.util.List.of();
        return new com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagDocumentBatchReportResponse(
                report.jobId(),
                report.status(),
                report.summaryOutcome(),
                outcomeDtos,
                report.modelArtifactHash(),
                report.modelVersion(),
                report.peakSystemRamBytes(),
                report.peakGpuVramBytes(),
                report.createdAt(),
                report.finishedAt()
        );
    }

    public static com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagBatchFinalizationResponse toResponse(com.vhvkhangg.personalprivatevault.rag.api.RagBatchFinalizationResult result) {
        if (result == null) return null;
        return new com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagBatchFinalizationResponse(
                result.jobId(),
                result.jobStatus(),
                result.publishedCount(),
                result.failedCount(),
                result.publishedGenerations()
        );
    }
}
