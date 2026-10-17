package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagStagedOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RagStagedOutputRepository extends JpaRepository<RagStagedOutput, Long> {

    List<RagStagedOutput> findByJobIdAndAttemptId(UUID jobId, UUID attemptId);

    long countByJobIdAndAttemptId(UUID jobId, UUID attemptId);

    @Query("SELECT COALESCE(SUM(LENGTH(s.payload)), 0) FROM RagStagedOutput s WHERE s.jobId = :jobId AND s.attemptId = :attemptId")
    long sumPayloadLengthByJobIdAndAttemptId(@Param("jobId") UUID jobId, @Param("attemptId") UUID attemptId);

    List<RagStagedOutput> findByJobId(UUID jobId);

    @Modifying
    @Query("DELETE FROM RagStagedOutput s WHERE s.jobId = :jobId")
    int deleteByJobId(@Param("jobId") UUID jobId);

    @Modifying
    @Query("DELETE FROM RagStagedOutput s WHERE s.jobId = :jobId AND s.attemptId = :attemptId")
    int deleteByJobIdAndAttemptId(@Param("jobId") UUID jobId, @Param("attemptId") UUID attemptId);
}
