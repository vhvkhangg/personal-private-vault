package com.vhvkhangg.personalprivatevault.collection.shopping.shopping;

/**
 * Exception thrown when shopping item validation fails.
 */
public class InvalidShoppingItemException extends RuntimeException {

    public InvalidShoppingItemException(String message) {
        super(message);
    }

    public InvalidShoppingItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
