package com.vhvkhangg.personalprivatevault.feed.source.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Categorical failure reasons for feed source refresh operations.
 */
@Getter
@RequiredArgsConstructor
public enum FeedRefreshFailureReason {

    SOURCE_DISABLED("SOURCE_DISABLED", "Feed source is disabled"),
    UNSUPPORTED_SOURCE_TYPE("UNSUPPORTED_SOURCE_TYPE", "Feed source type is not supported for manual refresh"),
    REFRESH_ALREADY_RUNNING("REFRESH_ALREADY_RUNNING", "Refresh is already running for this source"),
    SOURCE_CHANGED_DURING_REFRESH("SOURCE_CHANGED_DURING_REFRESH", "Feed source configuration changed during refresh"),
    UPSTREAM_UNAVAILABLE("UPSTREAM_UNAVAILABLE", "Upstream provider is unavailable"),
    UPSTREAM_RATE_LIMITED("UPSTREAM_RATE_LIMITED", "Upstream provider rate limited the request"),
    REFRESH_CAPACITY_REACHED("REFRESH_CAPACITY_REACHED", "Global refresh capacity reached"),
    REFRESH_FINALIZATION_UNAVAILABLE("REFRESH_FINALIZATION_UNAVAILABLE", "Refresh finalization timed out or unavailable"),
    UPSTREAM_INVALID_RESPONSE("UPSTREAM_INVALID_RESPONSE", "Upstream provider returned an invalid response"),
    UPSTREAM_DESTINATION_REJECTED("UPSTREAM_DESTINATION_REJECTED", "Upstream destination was rejected by network policy"),
    FETCH_LIMIT_EXCEEDED("FETCH_LIMIT_EXCEEDED", "Fetch limit exceeded");

    private final String code;
    private final String defaultMessage;
}
