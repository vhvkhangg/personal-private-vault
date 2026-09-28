package com.vhvkhangg.personalprivatevault.authentication.session;

/**
 * Thrown when a refresh token is invalid, expired, revoked, or already replaced.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
