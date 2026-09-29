package com.vhvkhangg.personalprivatevault.account.snapshot;

/**
 * Thrown when a Follower Snapshot cannot be found by its ID.
 */
public class FollowerSnapshotNotFoundException extends RuntimeException {
    public FollowerSnapshotNotFoundException(String message) {
        super(message);
    }
}
