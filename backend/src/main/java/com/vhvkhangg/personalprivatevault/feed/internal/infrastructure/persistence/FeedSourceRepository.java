package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FeedSource}.
 */
public interface FeedSourceRepository extends JpaRepository<FeedSource, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM FeedSource s WHERE s.id = :id")
    Optional<FeedSource> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT s FROM FeedSource s " +
            "WHERE s.enabled = true " +
            "  AND s.scheduledRefreshEnabled = true " +
            "  AND (s.nextFetchAt IS NULL OR s.nextFetchAt <= :cutoff) " +
            "ORDER BY CASE WHEN s.nextFetchAt IS NULL THEN 0 ELSE 1 END ASC, s.nextFetchAt ASC, s.id ASC")
    List<FeedSource> findDueSources(@Param("cutoff") Instant cutoff, Pageable pageable);
}
