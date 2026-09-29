package com.vhvkhangg.personalprivatevault.fiction.fiction.exception;

/**
 * Thrown when fiction attributes or references violate domain validation rules.
 */
public class InvalidFictionException extends RuntimeException {

    public InvalidFictionException(String message) {
        super(message);
    }

    public InvalidFictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
