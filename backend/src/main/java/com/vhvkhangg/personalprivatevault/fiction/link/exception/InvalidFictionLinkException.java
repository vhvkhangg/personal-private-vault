package com.vhvkhangg.personalprivatevault.fiction.link.exception;

/**
 * Thrown when fiction link attributes violate validation rules or reference constraints.
 */
public class InvalidFictionLinkException extends RuntimeException {

    public InvalidFictionLinkException(String message) {
        super(message);
    }

    public InvalidFictionLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
