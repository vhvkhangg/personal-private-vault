package com.vhvkhangg.personalprivatevault.feed.view;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable view representing the outcome of a manual feed source refresh.
 */
public record FeedRefreshResultView(
        Long sourceId,
        Instant fetchedAt,
        int selectedTopIds,
        int eligibleStories,
        int distinctArticleUrls,
        int processedItems
) {
    public FeedRefreshResultView {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(fetchedAt, "fetchedAt must not be null");
    }
}
