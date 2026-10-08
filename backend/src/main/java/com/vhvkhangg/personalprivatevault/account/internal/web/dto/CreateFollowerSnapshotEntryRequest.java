package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFollowerSnapshotEntryRequest(
        @NotNull Long targetAccountId,
        @Size(max = 255) String usernameSnapshot,
        @Size(max = 500) String displayNameSnapshot,
        @Size(max = 255) String externalIdSnapshot,
        @Size(max = 2048) String profileUrlSnapshot
) {
    public CreateFollowerSnapshotEntryRequest {
        displayNameSnapshot = trimOrNull(displayNameSnapshot);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
