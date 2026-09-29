package com.vhvkhangg.personalprivatevault.account.view;

import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;

import java.time.Instant;

/**
 * Immutable view of a Follower Snapshot header.
 */
public record FollowerSnapshotView(
        Long id,
        Long ownerAccountId,
        Instant capturedAt,
        FollowerSnapshotSource source,
        Integer reportedTotalCount,
        String importedFileName,
        Instant createdAt,
        int entryCount
) {
}
