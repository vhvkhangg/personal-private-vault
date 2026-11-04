package com.vhvkhangg.personalprivatevault.feed.view;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Immutable view of the rolling 24-hour feed radar preview.
 */
public record FeedRadarPageView(
        List<FeedRadarItemView> items,
        Instant windowStart,
        Instant windowEnd,
        Instant retrievedAt
) {
    public FeedRadarPageView {
        items = items != null ? List.copyOf(items) : List.of();
        Objects.requireNonNull(windowStart, "windowStart must not be null");
        Objects.requireNonNull(windowEnd, "windowEnd must not be null");
        Objects.requireNonNull(retrievedAt, "retrievedAt must not be null");
    }
}
