package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobStatusView;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class RagService implements RagOperations {

    private final RagSourceService sourceService;
    private final RagJobService jobService;
    private final RagFinalizationGate finalizationGate;
    private final RagRuntimeSecurity runtimeSecurity;
    private final RagDocumentBatchService documentBatchService;
    private final RagDocumentReportService documentReportService;
    private final RagDocumentFinalizationGate documentFinalizationGate;

    public RagService(

        RagSourceService sourceService,
        RagJobService jobService,
        RagFinalizationGate finalizationGate,
        RagRuntimeSecurity runtimeSecurity,
        RagDocumentBatchService documentBatchService,
        RagDocumentReportService documentReportService,
        RagDocumentFinalizationGate documentFinalizationGate) {
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService must not be null");
        this.jobService = Objects.requireNonNull(jobService, "jobService must not be null");
        this.finalizationGate = Objects.requireNonNull(finalizationGate, "finalizationGate must not be null");
        this.runtimeSecurity = runtimeSecurity;
        this.documentBatchService = Objects.requireNonNull(documentBatchService, "documentBatchService must not be null");
        this.documentReportService = Objects.requireNonNull(documentReportService, "documentReportService must not be null");
        this.documentFinalizationGate = Objects.requireNonNull(documentFinalizationGate, "documentFinalizationGate must not be null");
    }

    @Override
    public RagSourceView registerFileCandidate(String absoluteFilePath) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.registerFileCandidate(absoluteFilePath);
    }

    @Override
    public RagSourceView registerDomainCandidate(RagSourceType type, String sourceId) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.registerDomainCandidate(type, sourceId);
    }

    @Override
    public RagSourceView approveSource(Long sourceId, Set<String> approvedFields) {
        runtimeSecurity.requireIsolatedRuntime();
        throw new IllegalArgumentException("Explicit source and consent versions are required");
    }

    @Override
    public RagSourceView registerDomainCandidate(RagSourceType type, String sourceId,
            com.vhvkhangg.personalprivatevault.rag.source.RagClassification classification,
            Long expectedAuthorizationEpoch, Integer expectedFieldPolicyVersion) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.registerDomainCandidate(type, sourceId, classification,
                expectedAuthorizationEpoch, expectedFieldPolicyVersion);
    }

    @Override
    public RagSourceView approveSource(Long sourceId, com.vhvkhangg.personalprivatevault.rag.api.RagConsentCommand command) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.approveSource(sourceId, command);
    }

    @Override
    public RagSourceView revokeSource(Long sourceId) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.revokeSource(sourceId);
    }

    @Override
    public void addDependency(Long parentSourceId, Long childSourceId) {
        runtimeSecurity.requireIsolatedRuntime();
        sourceService.addDependency(parentSourceId, childSourceId);
    }

    @Override
    public Optional<RagSourceView> findSource(Long sourceId) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.findSource(sourceId);
    }

    @Override
    public List<RagSourceView> listSources(int offset, int limit) {
        runtimeSecurity.requireIsolatedRuntime();
        return sourceService.listSources(offset, limit);
    }

    @Override
    public RagJobAdmissionResult admitJob(Long sourceId) {
        runtimeSecurity.requireIsolatedRuntime();
        throw new IllegalArgumentException("Explicit source and policy versions are required");
    }

    @Override
    public RagJobAdmissionResult admitJob(Long sourceId, com.vhvkhangg.personalprivatevault.rag.api.RagIndexCommand command) {
        runtimeSecurity.requireIsolatedRuntime();
        return jobService.admitJob(sourceId, command);
    }

    @Override
    public RagJobStatusView getJobStatus(UUID jobId) {
        runtimeSecurity.requireIsolatedRuntime();
        return jobService.getJobStatus(jobId);
    }

    @Override
    public void cancelJob(UUID jobId) {
        runtimeSecurity.requireIsolatedRuntime();
        jobService.cancelJob(jobId);
    }

    @Override
    public void executeSyntheticStage(UUID jobId, UUID attemptId, String stageType) {
        runtimeSecurity.requireIsolatedRuntime();
        jobService.executeSyntheticStage(jobId, attemptId, stageType);
    }

    @Override
    public RagFinalizationResult finalizeGeneration(UUID jobId, UUID attemptId, String dependencyDigest) {
        runtimeSecurity.requireIsolatedRuntime();
        return finalizationGate.finalizeGeneration(jobId, attemptId, dependencyDigest);
    }

    @Override
    public RagJobAdmissionResult admitDocumentBatch(com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchCommand command) {
        runtimeSecurity.requireIsolatedRuntime();
        return documentBatchService.admitDocumentBatch(command);
    }

    @Override
    public void executeDocumentBatch(UUID jobId, UUID attemptId, List<com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchTask> tasks) {
        runtimeSecurity.requireIsolatedRuntime();
        documentBatchService.executeDocumentBatch(jobId, attemptId, tasks);
    }

    @Override
    public com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchReport getDocumentBatchReport(UUID jobId) {
        runtimeSecurity.requireIsolatedRuntime();
        return documentReportService.getReport(jobId);
    }

    @Override
    public com.vhvkhangg.personalprivatevault.rag.api.RagBatchFinalizationResult finalizeDocumentBatch(
            UUID jobId, UUID attemptId, com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchFinalizeCommand command) {
        runtimeSecurity.requireIsolatedRuntime();
        return documentFinalizationGate.finalizeDocumentBatch(jobId, attemptId, command);
    }
}
