package com.vhvkhangg.personalprivatevault.feed.item.exception;

/**
 * Thrown when a requested feed item is not found.
 */
public class FeedItemNotFoundException extends RuntimeException {

    public FeedItemNotFoundException(Long id) {
        super("Feed item not found with id: " + id);
    }

    public FeedItemNotFoundException(String message) {
        super(message);
    }
}
