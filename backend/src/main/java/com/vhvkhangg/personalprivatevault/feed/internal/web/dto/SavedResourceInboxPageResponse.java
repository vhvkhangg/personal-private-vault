package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SavedResourceInboxPageResponse", description = "Paged Universal Inbox saved resources")
public record SavedResourceInboxPageResponse(
        @Schema(description = "Page items in descending saved-at order", requiredMode = Schema.RequiredMode.REQUIRED)
        List<SavedResourceResponse> items,

        @Schema(description = "Opaque continuation cursor for next page, or null if exhausted", nullable = true)
        String nextCursor
) {
    public SavedResourceInboxPageResponse {
        items = items != null ? List.copyOf(items) : List.of();
    }
}
