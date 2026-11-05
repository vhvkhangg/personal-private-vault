package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssAtomParser;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedDestinationPolicy;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedNormalizer;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.RssFeedRefreshResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates manual RSS feed source refresh without holding database locks over network I/O.
 */
@Service
public class RssFeedRefreshService {

    public static final long OVERALL_REFRESH_BUDGET_SECONDS = 25L;
    public static final long FETCH_SUB_BUDGET_SECONDS = 20L;
    public static final long FINALIZER_MIN_REMAINING_SECONDS = 5L;

    private final FeedSourceRepository feedSourceRepository;
    private final HackerNewsRefreshAdmission admission;
    private final RssFeedDestinationPolicy destinationPolicy;
    private final RssFeedClient client;
    private final RssAtomParser parser;
    private final RssFeedNormalizer normalizer;
    private final RssFeedRefreshFinalizer finalizer;
    private final Clock clock;

    @Autowired
    public RssFeedRefreshService(
            FeedSourceRepository feedSourceRepository,
            HackerNewsRefreshAdmission admission,
            RssFeedDestinationPolicy destinationPolicy,
            RssFeedClient client,
            RssAtomParser parser,
            RssFeedNormalizer normalizer,
            RssFeedRefreshFinalizer finalizer,
            @Autowired(required = false) Clock clock
    ) {
        this.feedSourceRepository = feedSourceRepository;
        this.admission = admission;
        this.destinationPolicy = destinationPolicy;
        this.client = client;
        this.parser = parser;
        this.normalizer = normalizer;
        this.finalizer = finalizer;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    @Transactional(propagation = Propagation.NEVER)
    public RssFeedRefreshResultView refreshSource(Long sourceId) {
        // 1. Preflight source validation (short read, no lock across HTTP)
        FeedSource source = feedSourceRepository.findById(sourceId)
                .orElseThrow(() -> new FeedSourceNotFoundException(sourceId));

        // Check enabled before type
        if (!source.isEnabled()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.SOURCE_DISABLED);
        }
        if (source.getType() != FeedSourceType.RSS) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UNSUPPORTED_SOURCE_TYPE);
        }

        String feedUrl = source.getFeedUrl();
        if (feedUrl == null || feedUrl.isBlank()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "feedUrl must be configured for RSS refresh");
        }
        URI feedUri;
        try {
            feedUri = URI.create(feedUrl);
        } catch (IllegalArgumentException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Malformed feedUrl URI", ex);
        }
        destinationPolicy.validateUri(feedUri);

        long startNanos = System.nanoTime();
        long overallDeadlineNanos = startNanos + TimeUnit.SECONDS.toNanos(OVERALL_REFRESH_BUDGET_SECONDS);
        long fetchDeadlineNanos = startNanos + TimeUnit.SECONDS.toNanos(FETCH_SUB_BUDGET_SECONDS);

        if (Thread.currentThread().isInterrupted()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh interrupted before execution");
        }

        // 2. Single-flight and capacity admission
        admission.acquire(sourceId);
        try {
            // 3. Fetch from RSS upstream outside database transaction
            byte[] responseBytes = client.fetchFeed(feedUrl, fetchDeadlineNanos);

            if (Thread.currentThread().isInterrupted()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh interrupted after fetch");
            }

            // Capture batch now for relative date validity
            Instant batchNow = Instant.now(clock);

            // 4. Parse XML with strict streaming and budget constraints
            RssAtomParser.ParsedFeed parsedFeed = parser.parseFeed(responseBytes);

            if (Thread.currentThread().isInterrupted()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh interrupted after parsing");
            }

            // 5. Normalize items, deduplicate, group by URL, select representatives
            RssFeedNormalizer.NormalizationResult normResult = normalizer.normalize(parsedFeed, batchNow, clock);

            if (Thread.currentThread().isInterrupted()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh interrupted after normalization");
            }

            // 6. Remaining budget check before finalization (reserve 5s)
            long remainingNanos = overallDeadlineNanos - System.nanoTime();
            if (remainingNanos < TimeUnit.SECONDS.toNanos(FINALIZER_MIN_REMAINING_SECONDS)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Overall refresh deadline expired before finalization");
            }

            // Single completion instant after complete fetch, parse, and normalization succeeds
            Instant fetchedAt = normResult.fetchedAt() != null ? normResult.fetchedAt() : Instant.now(clock);

            // 7. Finalize within short transactional boundary (timeout = 5s)
            List<FeedItemView> itemViews;
            try {
                itemViews = finalizer.finalizeRefresh(sourceId, feedUrl, fetchedAt, normResult.normalizedItems());
            } catch (FeedRefreshException ex) {
                throw ex;
            } catch (QueryTimeoutException | TransactionTimedOutException | PessimisticLockingFailureException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.REFRESH_FINALIZATION_UNAVAILABLE, "Finalization lock or transaction timed out", ex);
            } catch (TransactionException | DataAccessException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.REFRESH_FINALIZATION_UNAVAILABLE, "Finalization transaction failure", ex);
            }

            return new RssFeedRefreshResultView(
                    sourceId,
                    fetchedAt,
                    normResult.parsedEntries(),
                    normResult.eligibleEntries(),
                    normResult.distinctArticleUrls(),
                    itemViews.size()
            );
        } finally {
            admission.release(sourceId);
        }
    }
}
