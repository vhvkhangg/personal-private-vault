package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RagJobLeaseRepository extends JpaRepository<RagJobLease, Long> {
    long countByStatusInAndLeasedUntilAfter(java.util.Collection<String> statuses,java.time.Instant now);

    List<RagJobLease> findByJobId(UUID jobId);

    Optional<RagJobLease> findByJobIdAndAttemptId(UUID jobId, UUID attemptId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM RagJobLease l WHERE l.jobId = :jobId ORDER BY l.id ASC")
    List<RagJobLease> findByJobIdForUpdate(@Param("jobId") UUID jobId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM RagJobLease l WHERE l.jobId = :jobId AND l.attemptId = :attemptId")
    Optional<RagJobLease> findByJobIdAndAttemptIdForUpdate(@Param("jobId") UUID jobId, @Param("attemptId") UUID attemptId);

    List<RagJobLease> findBySourceId(Long sourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM RagJobLease l WHERE l.sourceId = :sourceId ORDER BY l.id ASC")
    List<RagJobLease> findBySourceIdForUpdate(@Param("sourceId") Long sourceId);
}
