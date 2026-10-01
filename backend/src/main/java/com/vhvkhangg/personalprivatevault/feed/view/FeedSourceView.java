package com.vhvkhangg.personalprivatevault.feed.view;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable read model for a feed source.
 */
public record FeedSourceView(
        Long id,
        String name,
        FeedSourceType type,
        String sourceUrl,
        String feedUrl,
        boolean enabled,
        boolean scheduledRefreshEnabled,
        Integer refreshIntervalMinutes,
        Map<String, Object> config,
        Instant lastFetchedAt,
        Instant nextFetchAt,
        Instant createdAt,
        Instant updatedAt
) {
    public FeedSourceView {
        config = FeedJsonSnapshot.toUnmodifiableSnapshot(config);
    }
}
