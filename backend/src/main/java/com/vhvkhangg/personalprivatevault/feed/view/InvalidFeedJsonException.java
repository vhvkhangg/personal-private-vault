package com.vhvkhangg.personalprivatevault.feed.view;

/**
 * Thrown when feed JSON configuration or metadata contains invalid keys, cycles, or unsupported types.
 */
public class InvalidFeedJsonException extends RuntimeException {

    public InvalidFeedJsonException(String message) {
        super(message);
    }
}
