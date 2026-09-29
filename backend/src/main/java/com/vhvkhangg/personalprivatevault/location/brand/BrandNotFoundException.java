package com.vhvkhangg.personalprivatevault.location.brand;

/**
 * Thrown when a brand cannot be found by its ID.
 */
public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(Long id) {
        super("Brand with ID " + id + " was not found");
    }

    public BrandNotFoundException(String message) {
        super(message);
    }
}
