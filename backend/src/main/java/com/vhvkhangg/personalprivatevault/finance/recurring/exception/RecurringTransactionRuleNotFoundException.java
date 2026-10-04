package com.vhvkhangg.personalprivatevault.finance.recurring.exception;

/**
 * Thrown when a recurring transaction rule cannot be found by its identifier.
 */
public class RecurringTransactionRuleNotFoundException extends RuntimeException {

    public RecurringTransactionRuleNotFoundException(Long id) {
        super("Recurring transaction rule not found with id: " + id);
    }
}
