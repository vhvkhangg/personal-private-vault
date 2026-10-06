package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

public record CreateManualSavedResourceRequest(
        @NotNull SavedResourceKind kind,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2048) String resourceUrl,
        @Size(max = 255) String author,
        String summary,
        Instant publishedAt,
        @Size(max = 255) String sourceName,
        @Size(max = 2048) String sourceUrl,
        @Size(max = 255) String externalId,
        Map<String, Object> rawMetadata
) {}
