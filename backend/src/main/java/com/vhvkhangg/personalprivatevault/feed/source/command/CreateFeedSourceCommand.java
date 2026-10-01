package com.vhvkhangg.personalprivatevault.feed.source.command;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;

import java.util.Map;

/**
 * Command for creating a feed source.
 */
public record CreateFeedSourceCommand(
        String name,
        FeedSourceType type,
        String sourceUrl,
        String feedUrl,
        Boolean enabled,
        Boolean scheduledRefreshEnabled,
        Integer refreshIntervalMinutes,
        Map<String, Object> config
) {
    public CreateFeedSourceCommand {
        config = FeedJsonSnapshot.toUnmodifiableSnapshot(config);
    }
}
