package com.vhvkhangg.personalprivatevault.feed.view;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable view representing the outcome of a manual RSS feed source refresh.
 */
public record RssFeedRefreshResultView(
        Long sourceId,
        Instant fetchedAt,
        int parsedEntries,
        int eligibleEntries,
        int distinctArticleUrls,
        int processedItems
) {
    public RssFeedRefreshResultView {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(fetchedAt, "fetchedAt must not be null");
    }
}
