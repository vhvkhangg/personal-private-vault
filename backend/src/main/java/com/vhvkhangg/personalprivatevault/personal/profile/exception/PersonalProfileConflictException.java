package com.vhvkhangg.personalprivatevault.personal.profile.exception;

/**
 * Thrown when an active self personal profile uniqueness conflict occurs.
 */
public class PersonalProfileConflictException extends RuntimeException {

    public PersonalProfileConflictException(String message) {
        super(message);
    }
}
