package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage;

public class SnapshotIntegrityException extends RuntimeException {
    public SnapshotIntegrityException(String message) {
        super(message);
    }

    public SnapshotIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}
