package com.vhvkhangg.personalprivatevault.finance.category.exception;

/**
 * Thrown when a transaction category cannot be found by its identifier.
 */
public class TransactionCategoryNotFoundException extends RuntimeException {

    public TransactionCategoryNotFoundException(Long id) {
        super("Transaction category not found with id: " + id);
    }
}
