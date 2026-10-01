package com.vhvkhangg.personalprivatevault.feed.source.command;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;

import java.util.Map;

/**
 * Command for updating an existing feed source.
 */
public record UpdateFeedSourceCommand(
        String name,
        FeedSourceType type,
        String sourceUrl,
        String feedUrl,
        Boolean enabled,
        Boolean scheduledRefreshEnabled,
        Integer refreshIntervalMinutes,
        Map<String, Object> config
) {
    public UpdateFeedSourceCommand {
        config = FeedJsonSnapshot.toUnmodifiableSnapshot(config);
    }
}
