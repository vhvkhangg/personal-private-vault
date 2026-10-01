package com.vhvkhangg.personalprivatevault.feed.source;

import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Public synchronous API for managing feed sources and querying due refresh schedules.
 */
public interface FeedSourceOperations {

    FeedSourceView createSource(CreateFeedSourceCommand command);

    FeedSourceView updateSource(Long id, UpdateFeedSourceCommand command);

    Optional<FeedSourceView> findSourceById(Long id);

    List<FeedSourceView> findDueSources(Instant cutoff, int limit);
}
