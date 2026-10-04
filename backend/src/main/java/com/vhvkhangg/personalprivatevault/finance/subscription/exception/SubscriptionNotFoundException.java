package com.vhvkhangg.personalprivatevault.finance.subscription.exception;

/**
 * Thrown when a subscription cannot be found by its identifier.
 */
public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(Long id) {
        super("Subscription not found with id: " + id);
    }
}
