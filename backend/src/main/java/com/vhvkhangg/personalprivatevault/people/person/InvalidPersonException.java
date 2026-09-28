package com.vhvkhangg.personalprivatevault.people.person;

/**
 * Thrown when person attributes or operations violate domain validation rules.
 */
public class InvalidPersonException extends RuntimeException {

    public InvalidPersonException(String message) {
        super(message);
    }

    public InvalidPersonException(String message, Throwable cause) {
        super(message, cause);
    }
}
