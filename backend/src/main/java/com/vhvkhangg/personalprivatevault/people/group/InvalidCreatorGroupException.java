package com.vhvkhangg.personalprivatevault.people.group;

/**
 * Thrown when creator group attributes or operations violate domain validation rules.
 */
public class InvalidCreatorGroupException extends RuntimeException {

    public InvalidCreatorGroupException(String message) {
        super(message);
    }

    public InvalidCreatorGroupException(String message, Throwable cause) {
        super(message, cause);
    }
}
