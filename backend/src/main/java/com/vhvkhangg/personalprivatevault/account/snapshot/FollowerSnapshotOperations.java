package com.vhvkhangg.personalprivatevault.account.snapshot;

import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotEntryView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotView;

import java.util.List;
import java.util.Optional;

/**
 * Public capability-oriented contract for Follower Snapshot operations.
 */
public interface FollowerSnapshotOperations {

    FollowerSnapshotView createSnapshot(CreateFollowerSnapshotCommand command);

    Optional<FollowerSnapshotView> findById(Long id);

    List<FollowerSnapshotView> findRecentByOwner(Long ownerAccountId, int limit);

    List<FollowerSnapshotEntryView> findEntriesBySnapshotId(Long snapshotId, int limit);
}
