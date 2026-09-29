package com.vhvkhangg.personalprivatevault.location.location;

/**
 * Thrown when a location cannot be found by its ID.
 */
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(Long id) {
        super("Location with ID " + id + " was not found");
    }

    public LocationNotFoundException(String message) {
        super(message);
    }
}
