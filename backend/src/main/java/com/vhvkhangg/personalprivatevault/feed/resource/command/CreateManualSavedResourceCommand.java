package com.vhvkhangg.personalprivatevault.feed.resource.command;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;

import java.time.Instant;
import java.util.Map;

/**
 * Command for manually saving a web, social, or external resource.
 */
public record CreateManualSavedResourceCommand(
        SavedResourceKind kind,
        String title,
        String resourceUrl,
        String author,
        String summary,
        Instant publishedAt,
        String sourceName,
        String sourceUrl,
        String externalId,
        Map<String, Object> rawMetadata
) {
    public CreateManualSavedResourceCommand {
        rawMetadata = FeedJsonSnapshot.toUnmodifiableSnapshot(rawMetadata);
    }
}
