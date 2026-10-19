package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagFileOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RagFileOutcomeRepository extends JpaRepository<RagFileOutcome, RagFileOutcome.RagFileOutcomeId> {

    List<RagFileOutcome> findByJobId(UUID jobId);
}
