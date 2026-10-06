package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import jakarta.validation.constraints.NotNull;

public record CreateFeedSavedResourceRequest(
        @NotNull Long feedItemId,
        @NotNull SavedResourceKind kind
) {}
