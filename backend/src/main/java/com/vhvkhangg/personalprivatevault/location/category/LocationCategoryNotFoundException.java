package com.vhvkhangg.personalprivatevault.location.category;

/**
 * Thrown when a location category cannot be found.
 */
public class LocationCategoryNotFoundException extends RuntimeException {

    public LocationCategoryNotFoundException(Long id) {
        super("Location category with ID " + id + " was not found");
    }

    public LocationCategoryNotFoundException(String message) {
        super(message);
    }
}
