package com.vhvkhangg.personalprivatevault.finance.subscription.exception;

/**
 * Thrown when a subscription operation violates unique constraints, such as linking
 * a recurring transaction rule that is already linked to another subscription.
 */
public class SubscriptionConflictException extends RuntimeException {

    public SubscriptionConflictException(String message) {
        super(message);
    }
}
