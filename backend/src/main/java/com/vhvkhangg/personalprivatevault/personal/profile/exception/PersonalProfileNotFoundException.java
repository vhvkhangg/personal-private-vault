package com.vhvkhangg.personalprivatevault.personal.profile.exception;

/**
 * Thrown when a personal profile cannot be found by its identifier.
 */
public class PersonalProfileNotFoundException extends RuntimeException {

    public PersonalProfileNotFoundException(Long id) {
        super("Personal profile not found with id: " + id);
    }
}
