package com.vhvkhangg.personalprivatevault.location.brand;

/**
 * Thrown when brand command inputs fail validation.
 */
public class InvalidBrandException extends RuntimeException {

    public InvalidBrandException(String message) {
        super(message);
    }
}
