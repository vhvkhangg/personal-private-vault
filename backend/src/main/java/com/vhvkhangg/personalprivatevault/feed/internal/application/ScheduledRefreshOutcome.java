package com.vhvkhangg.personalprivatevault.feed.internal.application;

/**
 * Categorical outcome of a scheduled feed source refresh attempt.
 */
public enum ScheduledRefreshOutcome {
    REFRESHED,
    NOT_ELIGIBLE,
    BUSY,
    FAILED
}
