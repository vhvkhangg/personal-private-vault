package com.vhvkhangg.personalprivatevault.feed.resource.exception;

/**
 * Thrown when a requested saved resource is not found.
 */
public class SavedResourceNotFoundException extends RuntimeException {

    public SavedResourceNotFoundException(Long id) {
        super("Saved resource not found with id: " + id);
    }

    public SavedResourceNotFoundException(String message) {
        super(message);
    }
}
