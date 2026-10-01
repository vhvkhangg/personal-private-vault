package com.vhvkhangg.personalprivatevault.feed.item.exception;

/**
 * Thrown when feed item ingestion encounters key ambiguity, conflicting batch candidates, or unique constraint conflicts.
 */
public class FeedItemConflictException extends RuntimeException {

    public FeedItemConflictException(String message) {
        super(message);
    }
}
