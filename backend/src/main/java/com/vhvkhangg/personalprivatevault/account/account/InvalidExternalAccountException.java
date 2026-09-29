package com.vhvkhangg.personalprivatevault.account.account;

/**
 * Thrown when an External Account command is invalid (e.g. invalid platform, missing identifier).
 */
public class InvalidExternalAccountException extends RuntimeException {
    public InvalidExternalAccountException(String message) {
        super(message);
    }
}
