package com.vhvkhangg.personalprivatevault.location.address;

/**
 * Thrown when address command inputs fail validation.
 */
public class InvalidAddressException extends RuntimeException {

    public InvalidAddressException(String message) {
        super(message);
    }
}
