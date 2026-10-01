package com.vhvkhangg.personalprivatevault.feed.view;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable read model for a saved web resource sharing Vault identity.
 */
public record SavedResourceView(
        Long id,
        Long feedItemId,
        SavedResourceKind kind,
        String sourceName,
        String sourceUrl,
        String externalId,
        String title,
        String resourceUrl,
        String resourceUrlHash,
        String author,
        String summary,
        Instant publishedAt,
        Instant fetchedAt,
        Instant savedAt,
        Map<String, Object> rawMetadata
) {
    public SavedResourceView {
        rawMetadata = FeedJsonSnapshot.toUnmodifiableSnapshot(rawMetadata);
    }
}
