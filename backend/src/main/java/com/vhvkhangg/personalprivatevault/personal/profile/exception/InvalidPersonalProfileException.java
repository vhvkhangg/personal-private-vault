package com.vhvkhangg.personalprivatevault.personal.profile.exception;

/**
 * Thrown when personal profile input validation fails.
 */
public class InvalidPersonalProfileException extends RuntimeException {

    public InvalidPersonalProfileException(String message) {
        super(message);
    }

    public InvalidPersonalProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}
