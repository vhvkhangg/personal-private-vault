package com.vhvkhangg.personalprivatevault.finance.transaction.exception;

/**
 * Thrown when financial transaction input validation fails.
 */
public class InvalidFinancialTransactionException extends RuntimeException {

    public InvalidFinancialTransactionException(String message) {
        super(message);
    }
}
