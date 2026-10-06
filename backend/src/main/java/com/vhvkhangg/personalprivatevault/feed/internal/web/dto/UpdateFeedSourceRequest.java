package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateFeedSourceRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull FeedSourceType type,
        @Size(max = 2048) String sourceUrl,
        @NotBlank @Size(max = 2048) String feedUrl,
        Boolean enabled,
        Boolean scheduledRefreshEnabled,
        @Positive Integer refreshIntervalMinutes,
        Map<String, Object> config
) {}
