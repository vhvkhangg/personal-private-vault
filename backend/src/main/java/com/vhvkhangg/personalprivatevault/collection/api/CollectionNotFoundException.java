package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection facade exception thrown when a requested collection item is not found.
 */
public class CollectionNotFoundException extends RuntimeException {

    public CollectionNotFoundException(String message) {
        super(message);
    }

    public CollectionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
