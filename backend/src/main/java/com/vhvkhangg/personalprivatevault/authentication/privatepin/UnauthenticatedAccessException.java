package com.vhvkhangg.personalprivatevault.authentication.privatepin;

/**
 * Thrown when a private PIN operation is attempted without an authenticated user context.
 */
public class UnauthenticatedAccessException extends RuntimeException {

    public UnauthenticatedAccessException(String message) {
        super(message);
    }
}
