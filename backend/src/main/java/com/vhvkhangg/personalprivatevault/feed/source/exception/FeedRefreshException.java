package com.vhvkhangg.personalprivatevault.feed.source.exception;

import lombok.Getter;

import java.util.Objects;

/**
 * Exception thrown when a feed source refresh operation fails.
 */
@Getter
public class FeedRefreshException extends RuntimeException {

    private final FeedRefreshFailureReason reason;

    public FeedRefreshException(FeedRefreshFailureReason reason) {
        super(Objects.requireNonNull(reason, "reason must not be null").getDefaultMessage());
        this.reason = reason;
    }

    public FeedRefreshException(FeedRefreshFailureReason reason, String message) {
        super(message);
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
    }

    public FeedRefreshException(FeedRefreshFailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
    }
}
