package com.vhvkhangg.personalprivatevault.account.snapshot;

/**
 * Command holding submitted historical copies for one target account inside a snapshot batch.
 */
public record CreateFollowerSnapshotEntryCommand(
        Long targetAccountId,
        String usernameSnapshot,
        String displayNameSnapshot,
        String externalIdSnapshot,
        String profileUrlSnapshot
) {
}
