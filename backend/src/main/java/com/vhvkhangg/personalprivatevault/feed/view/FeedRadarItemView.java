package com.vhvkhangg.personalprivatevault.feed.view;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable view of an individual item in the feed radar preview.
 */
public record FeedRadarItemView(
        Long feedItemId,
        Long sourceId,
        String sourceName,
        FeedSourceType sourceType,
        String title,
        String url,
        Instant publishedAt,
        Instant fetchedAt,
        String signalType,
        Integer sourceRank,
        Instant signalCapturedAt,
        Long hnStoryId,
        String hnDiscussionUrl
) {
    public FeedRadarItemView {
        Objects.requireNonNull(feedItemId, "feedItemId must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(sourceName, "sourceName must not be null");
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        Objects.requireNonNull(fetchedAt, "fetchedAt must not be null");
        Objects.requireNonNull(signalType, "signalType must not be null");
    }
}
