package com.vhvkhangg.personalprivatevault.finance.category.exception;

/**
 * Thrown when a category mutation violates compatibility with historical transaction
 * or recurring rule references.
 */
public class TransactionCategoryConflictException extends RuntimeException {

    public TransactionCategoryConflictException(String message) {
        super(message);
    }
}
