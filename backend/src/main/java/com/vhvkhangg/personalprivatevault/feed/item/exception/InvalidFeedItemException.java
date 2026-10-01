package com.vhvkhangg.personalprivatevault.feed.item.exception;

/**
 * Thrown when feed item fields violate length or nullability constraints.
 */
public class InvalidFeedItemException extends RuntimeException {

    public InvalidFeedItemException(String message) {
        super(message);
    }
}
