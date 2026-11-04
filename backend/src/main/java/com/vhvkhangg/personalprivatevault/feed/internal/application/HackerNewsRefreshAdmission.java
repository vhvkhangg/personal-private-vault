package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages in-memory single-flight admission per source and bounds global concurrent refreshes.
 */
@Component
public class HackerNewsRefreshAdmission {

    private final ConcurrentHashMap<Long, Boolean> activeSources = new ConcurrentHashMap<>();
    private final AtomicInteger activeCount = new AtomicInteger(0);

    public void acquire(Long sourceId) {
        if (activeSources.putIfAbsent(sourceId, Boolean.TRUE) != null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING);
        }
        int current = activeCount.incrementAndGet();
        if (current > 2) {
            activeCount.decrementAndGet();
            activeSources.remove(sourceId);
            throw new FeedRefreshException(FeedRefreshFailureReason.REFRESH_CAPACITY_REACHED);
        }
    }

    public void release(Long sourceId) {
        if (activeSources.remove(sourceId) != null) {
            activeCount.decrementAndGet();
        }
    }

    int getActiveCount() {
        return activeCount.get();
    }

    boolean isSourceActive(Long sourceId) {
        return activeSources.containsKey(sourceId);
    }
}
