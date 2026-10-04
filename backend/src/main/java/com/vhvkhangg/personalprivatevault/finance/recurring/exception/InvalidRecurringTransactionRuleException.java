package com.vhvkhangg.personalprivatevault.finance.recurring.exception;

/**
 * Thrown when recurring transaction rule input validation fails.
 */
public class InvalidRecurringTransactionRuleException extends RuntimeException {

    public InvalidRecurringTransactionRuleException(String message) {
        super(message);
    }
}
