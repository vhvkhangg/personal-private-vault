package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RagIndexGenerationRepository extends JpaRepository<RagIndexGeneration, Long> {

    Optional<RagIndexGeneration> findByGenerationId(UUID generationId);

    Optional<RagIndexGeneration> findBySourceIdAndActiveTrue(Long sourceId);

    List<RagIndexGeneration> findBySourceId(Long sourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM RagIndexGeneration g WHERE g.sourceId = :sourceId ORDER BY g.id ASC")
    List<RagIndexGeneration> findBySourceIdForUpdate(@Param("sourceId") Long sourceId);

    List<RagIndexGeneration> findByJobId(UUID jobId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM RagIndexGeneration g WHERE g.jobId = :jobId ORDER BY g.id ASC")
    List<RagIndexGeneration> findByJobIdForUpdate(@Param("jobId") UUID jobId);
}
