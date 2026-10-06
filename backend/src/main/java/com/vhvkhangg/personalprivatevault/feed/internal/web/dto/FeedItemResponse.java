package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import java.time.Instant;
import java.util.Map;

public record FeedItemResponse(
        Long id,
        Long feedSourceId,
        String externalId,
        String title,
        String url,
        String urlHash,
        String author,
        String summary,
        Instant publishedAt,
        Instant fetchedAt,
        Map<String, Object> rawMetadata
) {}
