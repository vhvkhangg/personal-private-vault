package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record CreateFollowerSnapshotRequest(
        @NotNull Instant capturedAt,
        @NotNull FollowerSnapshotSource source,
        @PositiveOrZero Integer reportedTotalCount,
        @Size(max = 500) String importedFileName,
        List<@NotNull @Valid CreateFollowerSnapshotEntryRequest> entries
) {
    public CreateFollowerSnapshotRequest {
        importedFileName = trimOrNull(importedFileName);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
