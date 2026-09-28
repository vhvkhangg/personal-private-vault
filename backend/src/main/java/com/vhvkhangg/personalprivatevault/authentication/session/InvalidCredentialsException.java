package com.vhvkhangg.personalprivatevault.authentication.session;

/**
 * Thrown when authentication credentials cannot be verified.
 * Generic message to avoid leaking user existence.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
