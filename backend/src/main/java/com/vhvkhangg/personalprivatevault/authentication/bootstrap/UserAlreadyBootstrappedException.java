package com.vhvkhangg.personalprivatevault.authentication.bootstrap;

/**
 * Thrown when attempting to bootstrap an already bootstrapped singleton user.
 */
public class UserAlreadyBootstrappedException extends RuntimeException {

    public UserAlreadyBootstrappedException(String message) {
        super(message);
    }
}
