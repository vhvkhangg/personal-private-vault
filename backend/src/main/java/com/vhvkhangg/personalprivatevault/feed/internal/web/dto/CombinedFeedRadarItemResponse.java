package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Individual item in the combined feed radar preview.
 */
@Schema(description = "Individual item in the combined feed radar preview")
public record CombinedFeedRadarItemResponse(
        @Schema(description = "ID of the feed item", example = "101")
        Long feedItemId,

        @Schema(description = "ID of the originating feed source", example = "1")
        Long sourceId,

        @Schema(description = "Name of the originating feed source", example = "Tech News RSS")
        String sourceName,

        @Schema(description = "Type of the originating feed source", example = "RSS")
        FeedSourceType sourceType,

        @Schema(description = "Article title", example = "Example Article Title")
        String title,

        @Schema(description = "Stored article URL", example = "https://example.com/article")
        String url,

        @Schema(description = "Publication or submission timestamp", example = "2026-11-04T08:30:00Z")
        Instant publishedAt,

        @Schema(description = "Timestamp when the item was fetched", example = "2026-11-04T09:00:00Z")
        Instant fetchedAt,

        @Schema(description = "Signal classification type", example = "RSS_SOURCE_CACHE")
        String signalType,

        @Schema(description = "1-based top-list position at capture time, or null if unranked/RSS", example = "1", nullable = true)
        Integer sourceRank,

        @Schema(description = "Timestamp when the signal was captured (fetchedAt for RSS), or null", example = "2026-11-04T09:00:00Z", nullable = true)
        Instant signalCapturedAt,

        @Schema(description = "Hacker News story ID, or null for RSS", example = "41982734", nullable = true)
        Long hnStoryId,

        @Schema(description = "Hacker News discussion URL, or null for RSS", example = "https://news.ycombinator.com/item?id=41982734", nullable = true)
        String hnDiscussionUrl
) {}
