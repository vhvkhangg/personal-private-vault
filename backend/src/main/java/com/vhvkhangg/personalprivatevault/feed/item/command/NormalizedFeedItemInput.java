package com.vhvkhangg.personalprivatevault.feed.item.command;

import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized item input delivered by an external feed fetch adapter.
 */
public record NormalizedFeedItemInput(
        String externalId,
        String title,
        String url,
        String author,
        String summary,
        Instant publishedAt,
        Map<String, Object> rawMetadata
) {
    public NormalizedFeedItemInput {
        rawMetadata = FeedJsonSnapshot.toUnmodifiableSnapshot(rawMetadata);
    }
}
