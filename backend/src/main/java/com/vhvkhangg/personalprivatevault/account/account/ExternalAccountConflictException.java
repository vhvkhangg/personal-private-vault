package com.vhvkhangg.personalprivatevault.account.account;

/**
 * Thrown when an External Account conflict occurs (such as duplicate platform and external ID).
 */
public class ExternalAccountConflictException extends RuntimeException {
    public ExternalAccountConflictException(String message) {
        super(message);
    }
}
