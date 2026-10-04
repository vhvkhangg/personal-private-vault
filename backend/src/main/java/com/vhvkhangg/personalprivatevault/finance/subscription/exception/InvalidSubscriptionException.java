package com.vhvkhangg.personalprivatevault.finance.subscription.exception;

/**
 * Thrown when subscription input validation fails.
 */
public class InvalidSubscriptionException extends RuntimeException {

    public InvalidSubscriptionException(String message) {
        super(message);
    }

    public InvalidSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
