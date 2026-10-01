package com.vhvkhangg.personalprivatevault.feed.resource.exception;

/**
 * Thrown when a saved resource with an identical resource URL hash already exists.
 */
public class SavedResourceConflictException extends RuntimeException {

    public SavedResourceConflictException(String message) {
        super(message);
    }
}
