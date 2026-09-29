package com.vhvkhangg.personalprivatevault.fiction.fiction.exception;

/**
 * Thrown when a requested fiction work is not found.
 */
public class FictionNotFoundException extends RuntimeException {

    public FictionNotFoundException(Long fictionId) {
        super("Fiction with ID " + fictionId + " not found");
    }

    public FictionNotFoundException(String message) {
        super(message);
    }
}
