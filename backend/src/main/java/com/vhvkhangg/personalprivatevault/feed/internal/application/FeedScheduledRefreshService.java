package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service orchestrating scheduled feed refresh ticks across supported providers.
 *
 * <p>Enforces stable-ID cursor traversal, per-tick candidate and invocation caps,
 * scheduler-local 5-minute cooldown with deterministic 512-entry eviction, and graceful stop.
 */
@Service
public class FeedScheduledRefreshService {

    private static final Logger log = LoggerFactory.getLogger(FeedScheduledRefreshService.class);

    public static final int PAGE_SIZE = 20;
    public static final int MAX_INVOCATIONS_PER_TICK = 2;
    public static final int MAX_COOLDOWN_ENTRIES = 512;
    public static final Duration COOLDOWN_DURATION = Duration.ofMinutes(5);

    private static final Set<FeedSourceType> SUPPORTED_TYPES = Set.of(
            FeedSourceType.HACKER_NEWS,
            FeedSourceType.RSS
    );

    private final FeedSourceRepository feedSourceRepository;
    private final HackerNewsRefreshService hackerNewsRefreshService;
    private final RssFeedRefreshService rssFeedRefreshService;
    private final Clock clock;

    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private volatile Long lastExaminedSourceId = null;
    private final ConcurrentMap<Long, Instant> cooldownMap = new ConcurrentHashMap<>();

    @Autowired
    public FeedScheduledRefreshService(
            FeedSourceRepository feedSourceRepository,
            HackerNewsRefreshService hackerNewsRefreshService,
            RssFeedRefreshService rssFeedRefreshService,
            @Autowired(required = false) Clock clock
    ) {
        this.feedSourceRepository = feedSourceRepository;
        this.hackerNewsRefreshService = hackerNewsRefreshService;
        this.rssFeedRefreshService = rssFeedRefreshService;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public void executeTick() {
        if (stopping.get() || Thread.currentThread().isInterrupted()) {
            return;
        }

        Instant cutoff = Instant.now(clock);
        pruneExpiredCooldown(cutoff);

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        List<FeedSource> candidates;
        try {
            Long cursor = this.lastExaminedSourceId;
            if (cursor == null) {
                candidates = feedSourceRepository.findScheduledDueSourcesFromStart(SUPPORTED_TYPES, cutoff, pageable);
            } else {
                candidates = feedSourceRepository.findScheduledDueSourcesAfterCursor(SUPPORTED_TYPES, cutoff, cursor, pageable);
            }
        } catch (Exception ex) {
            log.warn("Scheduled due query failed; retaining cursor for next tick");
            return;
        }

        if (candidates.isEmpty()) {
            this.lastExaminedSourceId = null;
            return;
        }

        int invocations = 0;
        for (FeedSource candidate : candidates) {
            if (stopping.get() || Thread.currentThread().isInterrupted()) {
                break;
            }

            Long sourceId = candidate.getId();
            this.lastExaminedSourceId = sourceId;

            Instant checkNow = Instant.now(clock);
            Instant retryNotBefore = cooldownMap.get(sourceId);
            if (retryNotBefore != null && retryNotBefore.isAfter(checkNow)) {
                continue;
            }

            if (stopping.get() || Thread.currentThread().isInterrupted()) {
                break;
            }

            invocations++;
            ScheduledRefreshOutcome outcome;
            try {
                if (candidate.getType() == FeedSourceType.HACKER_NEWS) {
                    outcome = hackerNewsRefreshService.refreshScheduled(sourceId);
                } else if (candidate.getType() == FeedSourceType.RSS) {
                    outcome = rssFeedRefreshService.refreshScheduled(sourceId);
                } else {
                    outcome = ScheduledRefreshOutcome.NOT_ELIGIBLE;
                }
            } catch (Exception ex) {
                outcome = ScheduledRefreshOutcome.FAILED;
            }

            if (outcome == ScheduledRefreshOutcome.REFRESHED) {
                cooldownMap.remove(sourceId);
            } else if (outcome == ScheduledRefreshOutcome.BUSY || outcome == ScheduledRefreshOutcome.FAILED) {
                Instant outcomeInstant = Instant.now(clock);
                setCooldown(sourceId, outcomeInstant.plus(COOLDOWN_DURATION), outcomeInstant);
            }

            if (invocations >= MAX_INVOCATIONS_PER_TICK) {
                break;
            }
        }
    }

    public void stop() {
        stopping.set(true);
    }

    public boolean isStopping() {
        return stopping.get();
    }

    public Long getLastExaminedSourceId() {
        return lastExaminedSourceId;
    }

    public Map<Long, Instant> getCooldownMap() {
        return Collections.unmodifiableMap(cooldownMap);
    }

    public void resetCursor() {
        this.lastExaminedSourceId = null;
    }

    public void resetCooldown() {
        this.cooldownMap.clear();
    }

    private synchronized void setCooldown(Long sourceId, Instant retryNotBefore, Instant now) {
        if (cooldownMap.containsKey(sourceId)) {
            cooldownMap.put(sourceId, retryNotBefore);
            return;
        }
        pruneExpiredCooldown(now);
        if (cooldownMap.size() >= MAX_COOLDOWN_ENTRIES) {
            Map.Entry<Long, Instant> candidateToEvict = null;
            for (Map.Entry<Long, Instant> entry : cooldownMap.entrySet()) {
                if (candidateToEvict == null) {
                    candidateToEvict = entry;
                } else {
                    int cmp = entry.getValue().compareTo(candidateToEvict.getValue());
                    if (cmp < 0 || (cmp == 0 && entry.getKey() < candidateToEvict.getKey())) {
                        candidateToEvict = entry;
                    }
                }
            }
            if (candidateToEvict != null) {
                cooldownMap.remove(candidateToEvict.getKey());
            }
        }
        cooldownMap.put(sourceId, retryNotBefore);
    }

    private void pruneExpiredCooldown(Instant now) {
        cooldownMap.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
