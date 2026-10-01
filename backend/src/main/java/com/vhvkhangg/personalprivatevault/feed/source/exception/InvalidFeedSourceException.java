package com.vhvkhangg.personalprivatevault.feed.source.exception;

/**
 * Thrown when feed source data or scheduling violates domain constraints.
 */
public class InvalidFeedSourceException extends RuntimeException {

    public InvalidFeedSourceException(String message) {
        super(message);
    }
}
