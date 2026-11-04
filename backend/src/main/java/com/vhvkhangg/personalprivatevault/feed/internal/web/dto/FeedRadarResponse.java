package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Response payload for rolling 24-hour feed radar preview.
 */
@Schema(description = "Response payload for rolling 24-hour feed radar preview")
public record FeedRadarResponse(
        @Schema(description = "List of cached radar items submitted within the last 24 hours")
        List<FeedRadarItemResponse> items,

        @Schema(description = "Start of the 24-hour submission time window", example = "2026-11-01T10:00:00Z")
        Instant windowStart,

        @Schema(description = "End of the 24-hour submission time window", example = "2026-11-02T10:00:00Z")
        Instant windowEnd,

        @Schema(description = "Timestamp when the preview was retrieved", example = "2026-11-02T10:00:00Z")
        Instant retrievedAt
) {}
