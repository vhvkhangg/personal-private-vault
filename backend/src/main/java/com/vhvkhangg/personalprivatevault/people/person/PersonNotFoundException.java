package com.vhvkhangg.personalprivatevault.people.person;

/**
 * Thrown when a person is not found by ID.
 */
public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(Long personId) {
        super("Person not found with id: " + personId);
    }

    public PersonNotFoundException(String message) {
        super(message);
    }

    public PersonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
