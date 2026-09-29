package com.vhvkhangg.personalprivatevault.location.location;

/**
 * Thrown when location command inputs fail validation.
 */
public class InvalidLocationException extends RuntimeException {

    public InvalidLocationException(String message) {
        super(message);
    }
}
