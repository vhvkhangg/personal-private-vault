package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage;

public class SnapshotStorageException extends RuntimeException {
    public SnapshotStorageException(String message) {
        super(message);
    }

    public SnapshotStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
