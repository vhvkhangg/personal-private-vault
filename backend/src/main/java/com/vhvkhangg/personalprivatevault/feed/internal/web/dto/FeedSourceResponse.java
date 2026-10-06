package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;

import java.time.Instant;
import java.util.Map;

public record FeedSourceResponse(
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
) {}
