package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response payload for feed source manual refresh.
 */
@Schema(description = "Response payload for feed source manual refresh")
public record FeedRefreshResponse(
        @Schema(description = "ID of the refreshed feed source", example = "1")
        Long sourceId,

        @Schema(description = "Timestamp when the refresh completed", example = "2026-11-02T10:00:00Z")
        Instant fetchedAt,

        @Schema(description = "Number of top story IDs selected from upstream", example = "30")
        int selectedTopIds,

        @Schema(description = "Number of live, valid article-bearing stories fetched before grouping", example = "28")
        int eligibleStories,

        @Schema(description = "Number of distinct article URLs grouped", example = "27")
        int distinctArticleUrls,

        @Schema(description = "Number of items processed by ingestion", example = "27")
        int processedItems
) {}
