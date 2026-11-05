package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsNormalizer;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRefreshResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates manual Hacker News feed source refresh without holding database locks over network I/O.
 */
@Service
public class HackerNewsRefreshService {

    private static final long OVERALL_REFRESH_BUDGET_SECONDS = 70L;
    private static final long FETCH_SUB_BUDGET_SECONDS = 60L;
    private static final long FINALIZER_MIN_REMAINING_SECONDS = 5L;

    private final FeedSourceRepository feedSourceRepository;
    private final HackerNewsRefreshAdmission admission;
    private final HackerNewsClient client;
    private final HackerNewsNormalizer normalizer;
    private final HackerNewsRefreshFinalizer finalizer;
    private final Clock clock;

    @Autowired
    public HackerNewsRefreshService(
            FeedSourceRepository feedSourceRepository,
            HackerNewsRefreshAdmission admission,
            HackerNewsClient client,
            HackerNewsNormalizer normalizer,
            HackerNewsRefreshFinalizer finalizer,
            @Autowired(required = false) Clock clock
    ) {
        this.feedSourceRepository = feedSourceRepository;
        this.admission = admission;
        this.client = client;
        this.normalizer = normalizer;
        this.finalizer = finalizer;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public static final int SCHEDULED_DEFAULT_MAX_ITEMS = 30;

    @Transactional(propagation = Propagation.NEVER)
    public FeedRefreshResultView refreshSource(Long sourceId, int maxItems) {
        // Enforce application boundary cap on selected items
        int boundedMaxItems = Math.min(Math.max(1, maxItems), HackerNewsNormalizer.MAX_SELECTED_ITEMS);

        // 1. Preflight source validation (short read, no lock across HTTP)
        FeedSource source = feedSourceRepository.findById(sourceId)
                .orElseThrow(() -> new FeedSourceNotFoundException(sourceId));

        // Check enabled before type
        if (!source.isEnabled()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.SOURCE_DISABLED);
        }
        if (source.getType() != FeedSourceType.HACKER_NEWS) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UNSUPPORTED_SOURCE_TYPE);
        }

        // 2. Single-flight and capacity admission
        admission.acquire(sourceId);
        try {
            return executeRefreshFlow(sourceId, boundedMaxItems, false);
        } finally {
            admission.release(sourceId);
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    public ScheduledRefreshOutcome refreshScheduled(Long sourceId) {
        boolean admitted = false;
        try {
            admission.acquire(sourceId);
            admitted = true;

            Instant now = Instant.now(clock);
            FeedSource source = feedSourceRepository.findById(sourceId).orElse(null);
            if (source == null
                    || !source.isEnabled()
                    || source.getType() != FeedSourceType.HACKER_NEWS
                    || !source.isScheduledRefreshEnabled()
                    || source.getRefreshIntervalMinutes() == null
                    || source.getRefreshIntervalMinutes() <= 0
                    || (source.getNextFetchAt() != null && source.getNextFetchAt().isAfter(now))) {
                return ScheduledRefreshOutcome.NOT_ELIGIBLE;
            }

            executeRefreshFlow(sourceId, SCHEDULED_DEFAULT_MAX_ITEMS, true);
            return ScheduledRefreshOutcome.REFRESHED;

        } catch (FeedRefreshException ex) {
            if (ex.getReason() == FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING
                    || ex.getReason() == FeedRefreshFailureReason.REFRESH_CAPACITY_REACHED) {
                return ScheduledRefreshOutcome.BUSY;
            }
            return ScheduledRefreshOutcome.FAILED;
        } catch (Exception ex) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
            }
            return ScheduledRefreshOutcome.FAILED;
        } finally {
            if (admitted) {
                admission.release(sourceId);
            }
        }
    }

    private FeedRefreshResultView executeRefreshFlow(Long sourceId, int boundedMaxItems, boolean scheduledMode) {
        long startNanos = System.nanoTime();
        long overallDeadlineNanos = startNanos + TimeUnit.SECONDS.toNanos(OVERALL_REFRESH_BUDGET_SECONDS);
        long fetchDeadlineNanos = startNanos + TimeUnit.SECONDS.toNanos(FETCH_SUB_BUDGET_SECONDS);

        if (Thread.currentThread().isInterrupted()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh interrupted before execution");
        }

        // Fetch from Hacker News provider outside database transaction
        HackerNewsNormalizer.RawFetchResult rawResult = normalizer.fetchStories(client, boundedMaxItems, fetchDeadlineNanos);

        if (Thread.currentThread().isInterrupted()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh interrupted after fetch");
        }

        // Capture completion timestamp strictly AFTER provider fetch succeeds (FR-06)
        Instant fetchedAt = Instant.now(clock);

        // Build normalized inputs with representative metadata stamped with post-fetch completion time
        HackerNewsNormalizer.NormalizationResult normResult = normalizer.buildNormalizationResult(rawResult, fetchedAt);

        // Remaining budget check before finalization (FR-01)
        long remainingNanos = overallDeadlineNanos - System.nanoTime();
        if (remainingNanos < TimeUnit.SECONDS.toNanos(FINALIZER_MIN_REMAINING_SECONDS)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Overall refresh deadline expired before finalization");
        }

        // Finalize within short transactional boundary (FR-05)
        List<FeedItemView> itemViews;
        try {
            itemViews = finalizer.finalizeRefresh(sourceId, fetchedAt, normResult.normalizedItems(), scheduledMode);
        } catch (FeedRefreshException ex) {
            throw ex;
        } catch (QueryTimeoutException | TransactionTimedOutException | PessimisticLockingFailureException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.REFRESH_FINALIZATION_UNAVAILABLE, "Finalization lock or transaction timed out", ex);
        } catch (TransactionException | DataAccessException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.REFRESH_FINALIZATION_UNAVAILABLE, "Finalization transaction failure", ex);
        }

        return new FeedRefreshResultView(
                sourceId,
                fetchedAt,
                normResult.selectedTopIds(),
                normResult.eligibleStories(),
                normResult.distinctArticleUrls(),
                itemViews.size()
        );
    }
}
