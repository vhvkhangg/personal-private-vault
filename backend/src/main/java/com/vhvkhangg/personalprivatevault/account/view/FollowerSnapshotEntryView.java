package com.vhvkhangg.personalprivatevault.account.view;

/**
 * Immutable view of a Follower Snapshot Entry holding historical copies.
 */
public record FollowerSnapshotEntryView(
        Long snapshotId,
        Long targetAccountId,
        String usernameSnapshot,
        String displayNameSnapshot,
        String externalIdSnapshot,
        String profileUrlSnapshot
) {
}
