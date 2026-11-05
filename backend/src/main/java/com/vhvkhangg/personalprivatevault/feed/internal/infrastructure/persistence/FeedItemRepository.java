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

    @Query("SELECT i.id AS id, " +
            "i.feedSourceId AS feedSourceId, " +
            "s.name AS sourceName, " +
            "s.type AS sourceType, " +
            "i.title AS title, " +
            "i.url AS url, " +
            "i.publishedAt AS publishedAt, " +
            "i.fetchedAt AS fetchedAt, " +
            "i.rawMetadata AS rawMetadata " +
            "FROM FeedItem i, FeedSource s " +
            "WHERE i.feedSourceId = s.id " +
            "AND s.enabled = true " +
            "AND s.type = :hnType " +
            "AND i.publishedAt IS NOT NULL " +
            "AND i.publishedAt >= :windowStart " +
            "AND i.publishedAt <= :windowEnd " +
            "AND (:sourceId IS NULL OR s.id = :sourceId) " +
            "ORDER BY i.publishedAt DESC, i.id DESC")
    List<FeedRadarProjection> findRadarItems(
            @Param("windowStart") java.time.Instant windowStart,
            @Param("windowEnd") java.time.Instant windowEnd,
            @Param("hnType") com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType hnType,
            @Param("sourceId") Long sourceId,
            Pageable pageable
    );

    @Query("SELECT i.id AS id, " +
            "i.feedSourceId AS feedSourceId, " +
            "s.name AS sourceName, " +
            "s.type AS sourceType, " +
            "i.title AS title, " +
            "i.url AS url, " +
            "i.publishedAt AS publishedAt, " +
            "i.fetchedAt AS fetchedAt, " +
            "i.rawMetadata AS rawMetadata " +
            "FROM FeedItem i, FeedSource s " +
            "WHERE i.feedSourceId = s.id " +
            "AND s.enabled = true " +
            "AND s.type IN (:sourceTypes) " +
            "AND i.publishedAt IS NOT NULL " +
            "AND i.publishedAt >= :windowStart " +
            "AND i.publishedAt <= :windowEnd " +
            "ORDER BY i.publishedAt DESC, i.id DESC")
    List<FeedRadarProjection> findCombinedRadarItems(
            @Param("windowStart") java.time.Instant windowStart,
            @Param("windowEnd") java.time.Instant windowEnd,
            @Param("sourceTypes") java.util.Collection<com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType> sourceTypes,
            Pageable pageable
    );
}
