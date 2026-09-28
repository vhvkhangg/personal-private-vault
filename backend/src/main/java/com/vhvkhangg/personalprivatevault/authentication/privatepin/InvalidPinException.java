package com.vhvkhangg.personalprivatevault.authentication.privatepin;

/**
 * Thrown when a PIN fails format validation (must be exactly six ASCII digits).
 */
public class InvalidPinException extends RuntimeException {

    public InvalidPinException(String message) {
        super(message);
    }
}
