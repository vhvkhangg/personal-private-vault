package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FeedItem}.
 */
public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {

    Optional<FeedItem> findByFeedSourceIdAndExternalId(Long feedSourceId, String externalId);

    Optional<FeedItem> findByFeedSourceIdAndUrlHash(Long feedSourceId, String urlHash);

    @Query("SELECT i FROM FeedItem i " +
            "WHERE i.feedSourceId = :sourceId " +
            "ORDER BY CASE WHEN i.publishedAt IS NULL THEN 1 ELSE 0 END ASC, i.publishedAt DESC, i.id DESC")
    List<FeedItem> findRecentBySource(@Param("sourceId") Long sourceId, Pageable pageable);
}
