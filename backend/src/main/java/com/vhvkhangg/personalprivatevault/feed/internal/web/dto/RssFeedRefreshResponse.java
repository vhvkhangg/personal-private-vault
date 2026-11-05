package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response payload for RSS feed source manual refresh.
 */
@Schema(description = "Response payload for RSS feed source manual refresh")
public record RssFeedRefreshResponse(
        @Schema(description = "ID of the refreshed feed source", example = "1")
        Long sourceId,

        @Schema(description = "Timestamp when the refresh completed", example = "2026-11-04T10:00:00Z")
        Instant fetchedAt,

        @Schema(description = "Total item/entry records scanned in complete document", example = "30")
        int parsedEntries,

        @Schema(description = "Individually eligible records before URL grouping", example = "28")
        int eligibleEntries,

        @Schema(description = "Distinct article URLs grouped before selection cap", example = "27")
        int distinctArticleUrls,

        @Schema(description = "Number of items processed by ingestion", example = "27")
        int processedItems
) {}
