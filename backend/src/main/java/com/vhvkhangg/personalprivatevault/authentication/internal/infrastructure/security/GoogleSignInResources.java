package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleSignInResources {

    private final Clock clock;
    private final Semaphore endpointSlots = new Semaphore(4);
    private final Semaphore providerSlots = new Semaphore(2);

    private final Map<String, RouteWindow> routeRateLimits = new ConcurrentHashMap<>();

    public GoogleSignInResources(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    private static class RouteWindow {
        private volatile long windowMinute;
        private final AtomicInteger count = new AtomicInteger();

        boolean allow(long currentMinute) {
            if (windowMinute != currentMinute) {
                synchronized (this) {
                    if (windowMinute != currentMinute) {
                        windowMinute = currentMinute;
                        count.set(0);
                    }
                }
            }
            return count.incrementAndGet() <= 32;
        }
    }

    public interface Slot extends AutoCloseable {
        @Override
        void close();
    }

    public Slot acquireEndpoint() {
        if (!endpointSlots.tryAcquire()) {
            throw GoogleSignInException.unavailable();
        }
        return endpointSlots::release;
    }

    public Slot acquireProvider() {
        if (!providerSlots.tryAcquire()) {
            throw GoogleSignInException.unavailable();
        }
        return providerSlots::release;
    }

    public void checkRate(String route) {
        long currentMinute = clock.millis() / 60000L;
        RouteWindow window = routeRateLimits.computeIfAbsent(route, k -> new RouteWindow());
        if (!window.allow(currentMinute)) {
            throw GoogleSignInException.rateLimited();
        }
    }
}
