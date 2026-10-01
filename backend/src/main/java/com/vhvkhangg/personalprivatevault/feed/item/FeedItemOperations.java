package com.vhvkhangg.personalprivatevault.feed.item;

import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Public synchronous API for normalized feed item ingestion and querying recent items.
 */
public interface FeedItemOperations {

    List<FeedItemView> ingestFetch(Long sourceId, Instant fetchedAt, List<NormalizedFeedItemInput> normalizedItems);

    Optional<FeedItemView> findItemById(Long id);

    List<FeedItemView> findRecentItemsBySource(Long sourceId, int limit);
}
