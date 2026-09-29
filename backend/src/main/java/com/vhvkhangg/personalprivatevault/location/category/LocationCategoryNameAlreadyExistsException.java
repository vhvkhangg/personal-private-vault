package com.vhvkhangg.personalprivatevault.location.category;

/**
 * Thrown when attempting to create or update a Location Category with a name that already exists (case-insensitively).
 */
public class LocationCategoryNameAlreadyExistsException extends RuntimeException {

    public LocationCategoryNameAlreadyExistsException(String name) {
        super("Location category with name '" + name + "' already exists (case-insensitive)");
    }

    public LocationCategoryNameAlreadyExistsException(String name, Throwable cause) {
        super("Location category with name '" + name + "' already exists (case-insensitive)", cause);
    }
}
