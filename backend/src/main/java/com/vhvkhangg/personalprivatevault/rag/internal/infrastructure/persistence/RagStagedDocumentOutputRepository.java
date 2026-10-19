package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagStagedDocumentOutput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RagStagedDocumentOutputRepository extends JpaRepository<RagStagedDocumentOutput, Long> {

    List<RagStagedDocumentOutput> findByJobIdAndAttemptId(UUID jobId, UUID attemptId);

    Optional<RagStagedDocumentOutput> findByJobIdAndAttemptIdAndRootKey(UUID jobId, UUID attemptId, String rootKey);

    long countByJobIdAndAttemptId(UUID jobId, UUID attemptId);
}
