package com.vhvkhangg.personalprivatevault.account.snapshot;

/**
 * Thrown when a Follower Snapshot command is invalid (e.g. invalid owner/target account, conflicting copies).
 */
public class InvalidFollowerSnapshotException extends RuntimeException {
    public InvalidFollowerSnapshotException(String message) {
        super(message);
    }
}
