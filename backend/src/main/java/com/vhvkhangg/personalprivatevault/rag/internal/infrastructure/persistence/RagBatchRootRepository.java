package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagBatchRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RagBatchRootRepository extends JpaRepository<RagBatchRoot, RagBatchRoot.RagBatchRootId> {

    List<RagBatchRoot> findByJobId(UUID jobId);

    boolean existsByJobId(UUID jobId);
}
