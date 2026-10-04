package com.vhvkhangg.personalprivatevault.finance.transaction.exception;

/**
 * Thrown when a financial transaction cannot be found by its identifier.
 */
public class FinancialTransactionNotFoundException extends RuntimeException {

    public FinancialTransactionNotFoundException(Long id) {
        super("Financial transaction not found with id: " + id);
    }
}
