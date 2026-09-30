package com.vhvkhangg.personalprivatevault.collection.software.software;

/**
 * Exception thrown when software item validation fails.
 */
public class InvalidSoftwareItemException extends RuntimeException {

    public InvalidSoftwareItemException(String message) {
        super(message);
    }

    public InvalidSoftwareItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
