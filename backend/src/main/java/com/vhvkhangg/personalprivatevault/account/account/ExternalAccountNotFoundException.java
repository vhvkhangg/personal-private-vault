package com.vhvkhangg.personalprivatevault.account.account;

/**
 * Thrown when an External Account cannot be found by its ID.
 */
public class ExternalAccountNotFoundException extends RuntimeException {
    public ExternalAccountNotFoundException(String message) {
        super(message);
    }
}
