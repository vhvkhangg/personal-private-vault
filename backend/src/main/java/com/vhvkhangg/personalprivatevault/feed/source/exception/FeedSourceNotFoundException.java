package com.vhvkhangg.personalprivatevault.feed.source.exception;

/**
 * Thrown when a requested feed source is not found.
 */
public class FeedSourceNotFoundException extends RuntimeException {

    public FeedSourceNotFoundException(Long id) {
        super("Feed source not found with id: " + id);
    }

    public FeedSourceNotFoundException(String message) {
        super(message);
    }
}
