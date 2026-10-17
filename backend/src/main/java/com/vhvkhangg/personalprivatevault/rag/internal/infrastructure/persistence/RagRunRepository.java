package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RagRunRepository extends JpaRepository<RagRun, Long> {

    Optional<RagRun> findByRunId(UUID runId);

    Optional<RagRun> findByJobId(UUID jobId);
}
