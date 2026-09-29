package com.vhvkhangg.personalprivatevault.location.category;

/**
 * Thrown when location category command inputs fail validation.
 */
public class InvalidLocationCategoryException extends RuntimeException {

    public InvalidLocationCategoryException(String message) {
        super(message);
    }

    public InvalidLocationCategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
