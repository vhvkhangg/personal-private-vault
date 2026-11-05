package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Response payload for rolling 24-hour combined feed radar preview across enabled sources.
 */
@Schema(description = "Response payload for rolling 24-hour combined feed radar preview across enabled sources")
public record CombinedFeedRadarResponse(
        @Schema(description = "List of cached radar items submitted or published within the last 24 hours")
        List<CombinedFeedRadarItemResponse> items,

        @Schema(description = "Start of the 24-hour submission/publication time window", example = "2026-11-03T10:00:00Z")
        Instant windowStart,

        @Schema(description = "End of the 24-hour submission/publication time window", example = "2026-11-04T10:00:00Z")
        Instant windowEnd,

        @Schema(description = "Timestamp when the combined preview was retrieved", example = "2026-11-04T10:00:00Z")
        Instant retrievedAt
) {}
