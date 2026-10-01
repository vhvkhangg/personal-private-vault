package com.vhvkhangg.personalprivatevault.feed.resource.exception;

/**
 * Thrown when saved resource parameters violate domain constraints.
 */
public class InvalidSavedResourceException extends RuntimeException {

    public InvalidSavedResourceException(String message) {
        super(message);
    }
}
