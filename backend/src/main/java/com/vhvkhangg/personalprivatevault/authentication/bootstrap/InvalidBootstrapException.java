package com.vhvkhangg.personalprivatevault.authentication.bootstrap;

/**
 * Thrown when bootstrap inputs fail validation.
 */
public class InvalidBootstrapException extends RuntimeException {

    public InvalidBootstrapException(String message) {
        super(message);
    }
}
