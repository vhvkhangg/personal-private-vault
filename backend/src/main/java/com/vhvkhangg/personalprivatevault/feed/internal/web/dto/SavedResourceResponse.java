package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;

import java.time.Instant;
import java.util.Map;

public record SavedResourceResponse(
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
) {}
