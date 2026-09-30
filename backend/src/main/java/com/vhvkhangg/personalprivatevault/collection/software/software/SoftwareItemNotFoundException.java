package com.vhvkhangg.personalprivatevault.collection.software.software;

/**
 * Exception thrown when a software item is not found.
 */
public class SoftwareItemNotFoundException extends RuntimeException {

    public SoftwareItemNotFoundException(Long id) {
        super("Software item not found: " + id);
    }

    public SoftwareItemNotFoundException(String message) {
        super(message);
    }
}
