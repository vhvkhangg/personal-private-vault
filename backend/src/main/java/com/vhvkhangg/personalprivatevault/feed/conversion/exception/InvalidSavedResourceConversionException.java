package com.vhvkhangg.personalprivatevault.feed.conversion.exception;

/**
 * Thrown when a saved resource conversion operation violates domain invariants.
 */
public class InvalidSavedResourceConversionException extends RuntimeException {

    public InvalidSavedResourceConversionException(String message) {
        super(message);
    }
}
