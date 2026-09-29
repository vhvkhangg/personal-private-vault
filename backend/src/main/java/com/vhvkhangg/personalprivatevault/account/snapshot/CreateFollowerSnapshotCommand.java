package com.vhvkhangg.personalprivatevault.account.snapshot;

import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;

import java.time.Instant;
import java.util.List;

/**
 * Command to atomically create a Follower Snapshot header and its submitted entries.
 */
public record CreateFollowerSnapshotCommand(
        Long ownerAccountId,
        Instant capturedAt,
        FollowerSnapshotSource source,
        Integer reportedTotalCount,
        String importedFileName,
        List<CreateFollowerSnapshotEntryCommand> entries
) {
    public CreateFollowerSnapshotCommand {
        entries = entries != null ? List.copyOf(entries) : List.of();
    }
}
