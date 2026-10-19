package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage;

public class SnapshotMissingException extends RuntimeException {
    public SnapshotMissingException(String message) {
        super(message);
    }
}
