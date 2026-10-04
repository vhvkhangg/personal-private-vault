package com.vhvkhangg.personalprivatevault.finance.category.exception;

/**
 * Thrown when transaction category validation fails.
 */
public class InvalidTransactionCategoryException extends RuntimeException {

    public InvalidTransactionCategoryException(String message) {
        super(message);
    }
}
