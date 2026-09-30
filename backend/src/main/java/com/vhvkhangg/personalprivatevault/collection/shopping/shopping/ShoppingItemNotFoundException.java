package com.vhvkhangg.personalprivatevault.collection.shopping.shopping;

/**
 * Exception thrown when a shopping item is not found.
 */
public class ShoppingItemNotFoundException extends RuntimeException {

    public ShoppingItemNotFoundException(Long id) {
        super("Shopping item not found: " + id);
    }

    public ShoppingItemNotFoundException(String message) {
        super(message);
    }
}
