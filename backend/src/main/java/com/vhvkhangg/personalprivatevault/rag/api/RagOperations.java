package com.vhvkhangg.personalprivatevault.rag.api;

import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Public capability interface orchestrating RAG foundation lifecycle operations.
 */
public interface RagOperations {

    RagSourceView registerFileCandidate(String absoluteFilePath);

    RagSourceView registerDomainCandidate(RagSourceType type, String sourceId);

    RagSourceView registerDomainCandidate(RagSourceType type, String sourceId,
            com.vhvkhangg.personalprivatevault.rag.source.RagClassification classification,
            Long expectedAuthorizationEpoch, Integer expectedFieldPolicyVersion);

    RagSourceView approveSource(Long sourceId, Set<String> approvedFields);

    RagSourceView approveSource(Long sourceId, RagConsentCommand command);

    RagSourceView revokeSource(Long sourceId);

    void addDependency(Long parentSourceId, Long childSourceId);

    Optional<RagSourceView> findSource(Long sourceId);

    List<RagSourceView> listSources(int offset, int limit);

    RagJobAdmissionResult admitJob(Long sourceId);

    RagJobAdmissionResult admitJob(Long sourceId, RagIndexCommand command);

    RagJobStatusView getJobStatus(UUID jobId);

    void cancelJob(UUID jobId);

    void executeSyntheticStage(UUID jobId, UUID attemptId, String stageType);

    RagFinalizationResult finalizeGeneration(UUID jobId, UUID attemptId, String dependencyDigest);
}
