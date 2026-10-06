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
        @Size(max = 255) String importedFileName,
        List<@Valid CreateFollowerSnapshotEntryRequest> entries
) {}
