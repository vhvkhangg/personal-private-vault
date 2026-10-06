package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;

import java.time.Instant;

public record FollowerSnapshotResponse(
        Long id,
        Long ownerAccountId,
        Instant capturedAt,
        FollowerSnapshotSource source,
        Integer reportedTotalCount,
        String importedFileName,
        Instant createdAt,
        int entryCount
) {}
