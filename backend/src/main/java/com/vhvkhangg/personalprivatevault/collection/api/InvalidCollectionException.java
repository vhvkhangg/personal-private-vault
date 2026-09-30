package com.vhvkhangg.personalprivatevault.collection.api;

/**
 * Public parent collection facade exception thrown when validation fails across collection operations.
 */
public class InvalidCollectionException extends RuntimeException {

    public InvalidCollectionException(String message) {
        super(message);
    }

    public InvalidCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
