package com.vhvkhangg.personalprivatevault.feed.view;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable read model for a normalized feed item.
 */
public record FeedItemView(
        Long id,
        Long feedSourceId,
        String externalId,
        String title,
        String url,
        String urlHash,
        String author,
        String summary,
        Instant publishedAt,
        Instant fetchedAt,
        Map<String, Object> rawMetadata
) {
    public FeedItemView {
        rawMetadata = FeedJsonSnapshot.toUnmodifiableSnapshot(rawMetadata);
    }
}
