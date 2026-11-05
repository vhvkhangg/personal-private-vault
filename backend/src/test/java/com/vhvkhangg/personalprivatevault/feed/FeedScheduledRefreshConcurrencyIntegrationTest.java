package com.vhvkhangg.personalprivatevault.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshAdmission;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshFinalizer;
import com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshFinalizer;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshService;
import com.vhvkhangg.personalprivatevault.feed.internal.application.ScheduledRefreshOutcome;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class FeedScheduledRefreshConcurrencyIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private HackerNewsRefreshService hackerNewsRefreshService;

    @Autowired
    private RssFeedRefreshService rssFeedRefreshService;

    @Autowired
    private HackerNewsRefreshFinalizer hnFinalizer;

    @Autowired
    private RssFeedRefreshFinalizer rssFinalizer;

    @Autowired
    private HackerNewsRefreshAdmission admission;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    @MockitoBean
    private RssFeedClient rssFeedClient;

    @MockitoBean
    private Clock clock;

    private ExecutorService executor;
    private static final Instant BASE_NOW = Instant.parse("2026-11-05T14:00:00Z");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final byte[] VALID_RSS_BYTES = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Concurrency RSS</title>
                <link>https://example.com</link>
                <item>
                  <title>Concurrent Article</title>
                  <link>https://example.com/concurrent-article</link>
                </item>
              </channel>
            </rss>
            """.getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(BASE_NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        executor = Executors.newFixedThreadPool(4);
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
        cleanDatabase();
    }

    private void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM saved_resource_conversions");
        jdbcTemplate.execute("DELETE FROM saved_resources");
        jdbcTemplate.execute("DELETE FROM feed_items");
        jdbcTemplate.execute("DELETE FROM feed_sources");
    }

    private FeedSourceView createSource(String name, FeedSourceType type, boolean enabled, boolean scheduled, Integer interval, String feedUrl) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                type,
                type == FeedSourceType.HACKER_NEWS ? "https://news.ycombinator.com" : "https://example.com",
                feedUrl != null ? feedUrl : (type == FeedSourceType.RSS ? "https://example.com/rss.xml" : null),
                enabled,
                scheduled,
                interval,
                null
        ));
    }

    private ObjectNode createStoryJson(long id, String title, String url) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("type", "story");
        node.put("title", title);
        node.put("url", url);
        node.put("by", "author");
        node.put("time", 1700000000L);
        node.put("score", 100);
        node.put("descendants", 5);
        return node;
    }

    @Test
    @DisplayName("Shared admission: 2 active refreshes across HN/RSS block a third attempt with BUSY")
    void sharedAdmission_twoActiveRefreshesBlockThirdWithBusy() throws Exception {
        FeedSourceView hnSource = createSource("HN 1", FeedSourceType.HACKER_NEWS, true, true, 60, null);
        FeedSourceView rssSource = createSource("RSS 1", FeedSourceType.RSS, true, true, 60, "https://example.com/rss1.xml");
        FeedSourceView thirdSource = createSource("HN 2", FeedSourceType.HACKER_NEWS, true, true, 60, null);

        CountDownLatch hnFetchStarted = new CountDownLatch(1);
        CountDownLatch rssFetchStarted = new CountDownLatch(1);
        CountDownLatch releaseFetches = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenAnswer(inv -> {
            hnFetchStarted.countDown();
            releaseFetches.await(5, TimeUnit.SECONDS);
            return List.of(501L);
        });
        when(hackerNewsClient.fetchItem(eq(501L), anyLong(), any()))
                .thenReturn(createStoryJson(501L, "HN Shared", "https://example.com/hn-shared"));

        when(rssFeedClient.fetchFeed(eq("https://example.com/rss1.xml"), anyLong())).thenAnswer(inv -> {
            rssFetchStarted.countDown();
            releaseFetches.await(5, TimeUnit.SECONDS);
            return VALID_RSS_BYTES;
        });

        Future<ScheduledRefreshOutcome> f1 = executor.submit(() -> hackerNewsRefreshService.refreshScheduled(hnSource.id()));
        Future<ScheduledRefreshOutcome> f2 = executor.submit(() -> rssFeedRefreshService.refreshScheduled(rssSource.id()));

        assertThat(hnFetchStarted.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(rssFetchStarted.await(5, TimeUnit.SECONDS)).isTrue();

        // 2 slots full -> 3rd attempt returns BUSY
        ScheduledRefreshOutcome outcome3 = hackerNewsRefreshService.refreshScheduled(thirdSource.id());
        assertThat(outcome3).isEqualTo(ScheduledRefreshOutcome.BUSY);

        // Same source concurrent attempt returns BUSY (single flight)
        ScheduledRefreshOutcome outcomeDuplicate = hackerNewsRefreshService.refreshScheduled(hnSource.id());
        assertThat(outcomeDuplicate).isEqualTo(ScheduledRefreshOutcome.BUSY);

        releaseFetches.countDown();
        assertThat(f1.get(5, TimeUnit.SECONDS)).isEqualTo(ScheduledRefreshOutcome.REFRESHED);
        assertThat(f2.get(5, TimeUnit.SECONDS)).isEqualTo(ScheduledRefreshOutcome.REFRESHED);
    }

    @Test
    @DisplayName("Scheduled refresh: turning scheduledRefreshEnabled OFF during fetch rolls back staged items and timestamps (HN & RSS)")
    void scheduledRefresh_scheduledDisabledMidFetch_rollsBackStagedItemsAndTimestamps() throws Exception {
        // HN test
        FeedSourceView hnSource = createSource("HN Scheduled Off", FeedSourceType.HACKER_NEWS, true, true, 60, null);
        CountDownLatch hnFetchLatch = new CountDownLatch(1);
        CountDownLatch hnProceedLatch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenAnswer(inv -> {
            hnFetchLatch.countDown();
            hnProceedLatch.await(5, TimeUnit.SECONDS);
            return List.of(601L);
        });
        when(hackerNewsClient.fetchItem(eq(601L), anyLong(), any()))
                .thenReturn(createStoryJson(601L, "HN Item", "https://example.com/hn-disabled-mid-fetch"));

        Future<ScheduledRefreshOutcome> hnFuture = executor.submit(() -> hackerNewsRefreshService.refreshScheduled(hnSource.id()));
        assertThat(hnFetchLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Edit source mid-fetch: disable scheduled refresh
        jdbcTemplate.update("UPDATE feed_sources SET scheduled_refresh_enabled = false WHERE id = ?", hnSource.id());
        hnProceedLatch.countDown();

        ScheduledRefreshOutcome hnOutcome = hnFuture.get(5, TimeUnit.SECONDS);
        assertThat(hnOutcome).isEqualTo(ScheduledRefreshOutcome.FAILED);

        // Staged items and timestamps must be rolled back!
        assertThat(feedItemRepository.findAll()).isEmpty();
        FeedSource hnDb = feedSourceRepository.findById(hnSource.id()).orElseThrow();
        assertThat(hnDb.getLastFetchedAt()).isNull();

        // RSS test
        FeedSourceView rssSource = createSource("RSS Scheduled Off", FeedSourceType.RSS, true, true, 60, "https://example.com/rss-off.xml");
        CountDownLatch rssFetchLatch = new CountDownLatch(1);
        CountDownLatch rssProceedLatch = new CountDownLatch(1);

        when(rssFeedClient.fetchFeed(eq("https://example.com/rss-off.xml"), anyLong())).thenAnswer(inv -> {
            rssFetchLatch.countDown();
            rssProceedLatch.await(5, TimeUnit.SECONDS);
            return VALID_RSS_BYTES;
        });

        Future<ScheduledRefreshOutcome> rssFuture = executor.submit(() -> rssFeedRefreshService.refreshScheduled(rssSource.id()));
        assertThat(rssFetchLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Disable scheduled refresh mid-fetch
        jdbcTemplate.update("UPDATE feed_sources SET scheduled_refresh_enabled = false WHERE id = ?", rssSource.id());
        rssProceedLatch.countDown();

        ScheduledRefreshOutcome rssOutcome = rssFuture.get(5, TimeUnit.SECONDS);
        assertThat(rssOutcome).isEqualTo(ScheduledRefreshOutcome.FAILED);

        assertThat(feedItemRepository.findAll()).isEmpty();
        FeedSource rssDb = feedSourceRepository.findById(rssSource.id()).orElseThrow();
        assertThat(rssDb.getLastFetchedAt()).isNull();
    }

    @Test
    @DisplayName("Scheduled refresh: interval-only edit mid-fetch succeeds and updates nextFetchAt using fresh interval")
    void scheduledRefresh_intervalOnlyEditMidFetch_succeedsAndUsesFreshInterval() throws Exception {
        FeedSourceView source = createSource("HN Interval Edit", FeedSourceType.HACKER_NEWS, true, true, 10, null);
        CountDownLatch fetchLatch = new CountDownLatch(1);
        CountDownLatch proceedLatch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenAnswer(inv -> {
            fetchLatch.countDown();
            proceedLatch.await(5, TimeUnit.SECONDS);
            return List.of(701L);
        });
        when(hackerNewsClient.fetchItem(eq(701L), anyLong(), any()))
                .thenReturn(createStoryJson(701L, "HN Interval Title", "https://example.com/hn-interval"));

        Future<ScheduledRefreshOutcome> future = executor.submit(() -> hackerNewsRefreshService.refreshScheduled(source.id()));
        assertThat(fetchLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Update interval from 10m to 45m mid-fetch (scheduling remains enabled)
        jdbcTemplate.update("UPDATE feed_sources SET refresh_interval_minutes = 45 WHERE id = ?", source.id());
        proceedLatch.countDown();

        ScheduledRefreshOutcome outcome = future.get(5, TimeUnit.SECONDS);
        assertThat(outcome).isEqualTo(ScheduledRefreshOutcome.REFRESHED);

        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedSource dbSource = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(dbSource.getLastFetchedAt()).isEqualTo(BASE_NOW);
        // nextFetchAt must be fetchedAt + 45 minutes!
        assertThat(dbSource.getNextFetchAt()).isEqualTo(BASE_NOW.plus(Duration.ofMinutes(45)));
    }

    @Test
    @DisplayName("Physical transaction proof: Scheduled HN refresh executes ingestion and finalizer in same PostgreSQL PID+XID")
    void scheduledRefresh_hn_sharedPhysicalTransactionProof() {
        FeedSourceView source = createSource("HN Tx Proof", FeedSourceType.HACKER_NEWS, true, true, 15, null);
        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenReturn(List.of(801L));
        when(hackerNewsClient.fetchItem(eq(801L), anyLong(), any()))
                .thenReturn(createStoryJson(801L, "HN Tx Title", "https://example.com/hn-tx-proof"));

        // Normal execution: shared transaction
        RefreshTransactionIdentities identities = captureTransactionIdentitiesHn(source.id(), false);
        assertSharedPhysicalTransaction(identities);

        // Negative control: independent transaction via REQUIRES_NEW must fail the assertion
        cleanDatabase();
        FeedSourceView sourceControl = createSource("HN Tx Control", FeedSourceType.HACKER_NEWS, true, true, 15, null);
        RefreshTransactionIdentities controlIdentities = captureTransactionIdentitiesHn(sourceControl.id(), true);
        assertThat(controlIdentities.ingestCommittedBeforeFinalizerCommit()).isTrue();
        assertThat(controlIdentities.ingest().transactionId())
                .isNotEqualTo(controlIdentities.finalizerBeforeCommit().transactionId());
        assertThatThrownBy(() -> assertSharedPhysicalTransaction(controlIdentities))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("Physical transaction proof: Scheduled RSS refresh executes ingestion and finalizer in same PostgreSQL PID+XID")
    void scheduledRefresh_rss_sharedPhysicalTransactionProof() {
        FeedSourceView source = createSource("RSS Tx Proof", FeedSourceType.RSS, true, true, 15, "https://example.com/rss-tx.xml");
        when(rssFeedClient.fetchFeed(eq("https://example.com/rss-tx.xml"), anyLong())).thenReturn(VALID_RSS_BYTES);

        // Normal execution: shared transaction
        RefreshTransactionIdentities identities = captureTransactionIdentitiesRss(source.id(), false);
        assertSharedPhysicalTransaction(identities);

        // Negative control: independent transaction via REQUIRES_NEW must fail the assertion
        cleanDatabase();
        FeedSourceView sourceControl = createSource("RSS Tx Control", FeedSourceType.RSS, true, true, 15, "https://example.com/rss-tx.xml");
        RefreshTransactionIdentities controlIdentities = captureTransactionIdentitiesRss(sourceControl.id(), true);
        assertThat(controlIdentities.ingestCommittedBeforeFinalizerCommit()).isTrue();
        assertThat(controlIdentities.ingest().transactionId())
                .isNotEqualTo(controlIdentities.finalizerBeforeCommit().transactionId());
        assertThatThrownBy(() -> assertSharedPhysicalTransaction(controlIdentities))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("Scheduled eligibility: post-selection manual success makes source not due, returns NOT_ELIGIBLE without I/O")
    void scheduledRefresh_postSelectionManualSuccess_makesSourceNotDue_returnsNotEligible() {
        FeedSourceView source = createSource("HN Post-Select Due", FeedSourceType.HACKER_NEWS, true, true, 60, null);

        // Before scheduled refresh runs, manual refresh completes and updates nextFetchAt into future
        Instant futureNextFetchAt = BASE_NOW.plus(Duration.ofMinutes(60));
        jdbcTemplate.update("UPDATE feed_sources SET next_fetch_at = ? WHERE id = ?",
                java.sql.Timestamp.from(futureNextFetchAt), source.id());

        ScheduledRefreshOutcome outcome = hackerNewsRefreshService.refreshScheduled(source.id());

        assertThat(outcome).isEqualTo(ScheduledRefreshOutcome.NOT_ELIGIBLE);
        assertThat(feedItemRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Same-source two-way barrier: active manual refresh blocks scheduled refresh with BUSY, and active scheduled blocks manual with REFRESH_ALREADY_RUNNING")
    void sameSourceBarrier_manualBlocksScheduled_andScheduledBlocksManual() throws Exception {
        FeedSourceView source = createSource("Barrier Source", FeedSourceType.HACKER_NEWS, true, true, 60, null);

        CountDownLatch manualFetchStarted = new CountDownLatch(1);
        CountDownLatch releaseManualFetch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenAnswer(inv -> {
            manualFetchStarted.countDown();
            releaseManualFetch.await(5, TimeUnit.SECONDS);
            return List.of(901L);
        });
        when(hackerNewsClient.fetchItem(eq(901L), anyLong(), any()))
                .thenReturn(createStoryJson(901L, "Barrier Story", "https://example.com/barrier"));

        // Case 1: Active manual blocks scheduled attempt on same source
        Future<com.vhvkhangg.personalprivatevault.feed.view.FeedRefreshResultView> manualFuture =
                executor.submit(() -> feedSourceOperations.refreshSource(source.id(), 30));
        assertThat(manualFetchStarted.await(5, TimeUnit.SECONDS)).isTrue();

        ScheduledRefreshOutcome scheduledOutcome = hackerNewsRefreshService.refreshScheduled(source.id());
        assertThat(scheduledOutcome).isEqualTo(ScheduledRefreshOutcome.BUSY);

        releaseManualFetch.countDown();
        manualFuture.get(5, TimeUnit.SECONDS);

        // Case 2: Active scheduled blocks manual attempt on same source
        CountDownLatch scheduledFetchStarted = new CountDownLatch(1);
        CountDownLatch releaseScheduledFetch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenAnswer(inv -> {
            scheduledFetchStarted.countDown();
            releaseScheduledFetch.await(5, TimeUnit.SECONDS);
            return List.of(902L);
        });
        when(hackerNewsClient.fetchItem(eq(902L), anyLong(), any()))
                .thenReturn(createStoryJson(902L, "Barrier Story 2", "https://example.com/barrier2"));

        // Reset nextFetchAt so source is eligible for scheduled refresh
        jdbcTemplate.update("UPDATE feed_sources SET next_fetch_at = NULL WHERE id = ?", source.id());

        Future<ScheduledRefreshOutcome> schedFuture = executor.submit(() -> hackerNewsRefreshService.refreshScheduled(source.id()));
        assertThat(scheduledFetchStarted.await(5, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> feedSourceOperations.refreshSource(source.id(), 30))
                .isInstanceOf(com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException.class)
                .hasFieldOrPropertyWithValue("reason", com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING);

        releaseScheduledFetch.countDown();
        assertThat(schedFuture.get(5, TimeUnit.SECONDS)).isEqualTo(ScheduledRefreshOutcome.REFRESHED);
    }

    @Test
    @DisplayName("Scheduled refresh: late schedule-OFF after item staging rolls back existing-item update and new item insert")
    void scheduledRefresh_lateScheduleOffAfterStaging_rollsBackExistingItemUpdateAndNewInsert() throws Exception {
        FeedSourceView source = createSource("Late Schedule Off", FeedSourceType.HACKER_NEWS, true, true, 15, null);

        // Seed 1 existing item for this source
        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenReturn(List.of(991L));
        when(hackerNewsClient.fetchItem(eq(991L), anyLong(), any()))
                .thenReturn(createStoryJson(991L, "Original Item Title", "https://example.com/late-item-1"));
        feedSourceOperations.refreshSource(source.id(), 30);

        FeedItem existingItem = feedItemRepository.findAll().stream()
                .filter(i -> i.getUrl().equals("https://example.com/late-item-1"))
                .findFirst().orElseThrow();
        assertThat(existingItem.getTitle()).isEqualTo("Original Item Title");

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenReturn(List.of(991L, 992L));
        when(hackerNewsClient.fetchItem(eq(991L), anyLong(), any()))
                .thenReturn(createStoryJson(991L, "Updated Item Title", "https://example.com/late-item-1"));
        when(hackerNewsClient.fetchItem(eq(992L), anyLong(), any()))
                .thenReturn(createStoryJson(992L, "Brand New Item 2", "https://example.com/late-item-2"));

        // Reset nextFetchAt and lastFetchedAt so source is eligible for scheduled refresh and clean rollback assertion
        jdbcTemplate.update("UPDATE feed_sources SET next_fetch_at = NULL, last_fetched_at = NULL WHERE id = ?", source.id());

        Object ingestTarget = AopTestUtils.getTargetObject(feedItemOperations);
        FeedSourceRepository originalIngestRepository = (FeedSourceRepository) ReflectionTestUtils.getField(ingestTarget, "feedSourceRepository");
        FeedItemRepository itemRepo = (FeedItemRepository) ReflectionTestUtils.getField(ingestTarget, "feedItemRepository");

        CountDownLatch stagingDoneLatch = new CountDownLatch(1);
        CountDownLatch proceedAfterDisableLatch = new CountDownLatch(1);
        AtomicReference<Throwable> finalizerException = new AtomicReference<>();

        // Intercept finalizer via Spring's Advised interface to record exact rejection exception thrown by finalizeRefresh
        MethodInterceptor finalizerAdvice = invocation -> {
            try {
                return invocation.proceed();
            } catch (Throwable t) {
                finalizerException.set(t);
                throw t;
            }
        };
        ((org.springframework.aop.framework.Advised) hnFinalizer).addAdvice(finalizerAdvice);

        // Intercept ingest's feedSourceRepository.findByIdForUpdate:
        // Hook is placed AFTER items are staged and flushed, but BEFORE first source FOR UPDATE lock is acquired!
        ProxyFactory ingestRepoProxy = new ProxyFactory(originalIngestRepository);
        ingestRepoProxy.addAdvice((MethodInterceptor) invocation -> {
            if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                // Ingest has executed feedItemRepository.save(existing) and save(newItem).
                // Flush itemRepo so UPDATE on 991 and INSERT on 992 are flushed to PostgreSQL in this physical transaction.
                itemRepo.flush();

                // Signal that items are staged in the transaction before source row is locked
                stagingDoneLatch.countDown();

                // Pause until independent commit disables scheduling
                boolean proceeded = proceedAfterDisableLatch.await(3, TimeUnit.SECONDS);
                assertThat(proceeded).as("proceedAfterDisableLatch await must not time out").isTrue();
            }
            return invocation.proceed();
        });

        try {
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", ingestRepoProxy.getProxy());

            Future<ScheduledRefreshOutcome> future = executor.submit(() -> hackerNewsRefreshService.refreshScheduled(source.id()));

            // Await staging done (items flushed in active transaction before source FOR UPDATE)
            assertThat(stagingDoneLatch.await(3, TimeUnit.SECONDS)).isTrue();

            // Concurrently disable scheduling in independent commit; source row is NOT locked so this commits immediately
            int updated = jdbcTemplate.update("UPDATE feed_sources SET scheduled_refresh_enabled = false WHERE id = ?", source.id());
            assertThat(updated).isEqualTo(1);

            // Release provider transaction
            proceedAfterDisableLatch.countDown();

            // Refresh outcome must be FAILED, completing quickly (well within finalizer 5s timeout)
            ScheduledRefreshOutcome outcome = future.get(3, TimeUnit.SECONDS);
            assertThat(outcome).isEqualTo(ScheduledRefreshOutcome.FAILED);

            // Observe the actual SOURCE_CHANGED_DURING_REFRESH rejection from the finalizer
            assertThat(finalizerException.get())
                    .isNotNull()
                    .isInstanceOf(FeedRefreshException.class)
                    .hasFieldOrPropertyWithValue("reason", FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH);

            // Staged changes must roll back:
            // Existing item keeps old title
            FeedItem itemAfterRollback = feedItemRepository.findById(existingItem.getId()).orElseThrow();
            assertThat(itemAfterRollback.getTitle()).isEqualTo("Original Item Title");

            // New item 2 is not inserted
            List<FeedItem> allItems = feedItemRepository.findAll();
            assertThat(allItems).hasSize(1);

            // Fetch timestamps on source remain null
            FeedSource sourceAfterRollback = feedSourceRepository.findById(source.id()).orElseThrow();
            assertThat(sourceAfterRollback.getLastFetchedAt()).isNull();
            assertThat(sourceAfterRollback.getNextFetchAt()).isNull();

        } finally {
            proceedAfterDisableLatch.countDown();
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", originalIngestRepository);
            ((org.springframework.aop.framework.Advised) hnFinalizer).removeAdvice(finalizerAdvice);
        }
    }

    @Test
    @DisplayName("Scheduled refresh: retained source lock blocks concurrent NOWAIT update until commit")
    void scheduledRefresh_retainedSourceLock_blocksConcurrentNowaitUpdate() throws Exception {
        FeedSourceView source = createSource("Lock Retained Source", FeedSourceType.HACKER_NEWS, true, true, 15, null);

        CountDownLatch finalizerLocked = new CountDownLatch(1);
        CountDownLatch releaseFinalizer = new CountDownLatch(1);

        // Intercept finalizer to pause while holding source lock
        Object finalizerTarget = AopTestUtils.getTargetObject(hnFinalizer);
        FeedSourceRepository originalFinalizerRepository = (FeedSourceRepository) ReflectionTestUtils.getField(finalizerTarget, "feedSourceRepository");

        ProxyFactory proxyFactory = new ProxyFactory(originalFinalizerRepository);
        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                finalizerLocked.countDown();
                try {
                    releaseFinalizer.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {}
            }
            return result;
        });

        when(hackerNewsClient.fetchTopStories(anyLong(), any())).thenReturn(List.of(995L));
        when(hackerNewsClient.fetchItem(eq(995L), anyLong(), any()))
                .thenReturn(createStoryJson(995L, "Lock Story", "https://example.com/lock-story"));

        try {
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", proxyFactory.getProxy());

            Future<ScheduledRefreshOutcome> future = executor.submit(() -> hackerNewsRefreshService.refreshScheduled(source.id()));
            assertThat(finalizerLocked.await(5, TimeUnit.SECONDS)).isTrue();

            // Try to acquire lock with NOWAIT in another transaction: must fail because finalizer holds FOR UPDATE lock!
            AtomicBoolean lockConflictDetected = new AtomicBoolean(false);
            Future<?> concurrentLockAttempt = executor.submit(() -> {
                try {
                    jdbcTemplate.execute("SELECT id FROM feed_sources WHERE id = " + source.id() + " FOR UPDATE NOWAIT");
                } catch (Exception ex) {
                    lockConflictDetected.set(true);
                }
            });
            concurrentLockAttempt.get(5, TimeUnit.SECONDS);
            assertThat(lockConflictDetected.get()).isTrue();

            releaseFinalizer.countDown();
            ScheduledRefreshOutcome outcome = future.get(5, TimeUnit.SECONDS);
            assertThat(outcome).isEqualTo(ScheduledRefreshOutcome.REFRESHED);
        } finally {
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", originalFinalizerRepository);
        }
    }

    private record PhysicalTransactionIdentity(long backendPid, long transactionId) {}

    private record RefreshTransactionIdentities(
            PhysicalTransactionIdentity ingest,
            PhysicalTransactionIdentity sourceUpdate,
            PhysicalTransactionIdentity finalizerBeforeCommit,
            boolean ingestCommittedBeforeFinalizerCommit
    ) {}

    private PhysicalTransactionIdentity currentPhysicalTransactionIdentity() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        PhysicalTransactionIdentity identity = jdbcTemplate.queryForObject(
                "SELECT pg_backend_pid(), pg_current_xact_id()::text::bigint",
                (row, index) -> new PhysicalTransactionIdentity(row.getLong(1), row.getLong(2)));
        assertThat(identity).isNotNull();
        assertThat(identity.backendPid()).isPositive();
        assertThat(identity.transactionId()).isPositive();
        return identity;
    }

    private void assertSharedPhysicalTransaction(RefreshTransactionIdentities identities) {
        assertThat(identities.sourceUpdate()).as("generic ingest source update").isEqualTo(identities.ingest());
        assertThat(identities.ingest())
                .as("actual ingest and authoritative finalizer beforeCommit must share PostgreSQL backend PID and transaction identity")
                .isEqualTo(identities.finalizerBeforeCommit());
        assertThat(identities.ingestCommittedBeforeFinalizerCommit()).isFalse();
    }

    private RefreshTransactionIdentities captureTransactionIdentitiesHn(Long sourceId, boolean requiresNewIngest) {
        Object finalizerTarget = AopTestUtils.getTargetObject(hnFinalizer);
        Object ingestTarget = AopTestUtils.getTargetObject(feedItemOperations);

        AtomicReference<PhysicalTransactionIdentity> ingest = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> sourceUpdate = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> finalizerBeforeCommit = new AtomicReference<>();
        AtomicBoolean ingestCommitted = new AtomicBoolean(false);
        AtomicBoolean ingestCommittedBeforeFinalizerCommit = new AtomicBoolean(false);

        FeedItemOperations originalOps = (FeedItemOperations) ReflectionTestUtils.getField(finalizerTarget, "feedItemOperations");
        FeedSourceRepository originalIngestRepository = (FeedSourceRepository) ReflectionTestUtils.getField(ingestTarget, "feedSourceRepository");
        FeedSourceRepository originalFinalizerRepository = (FeedSourceRepository) ReflectionTestUtils.getField(finalizerTarget, "feedSourceRepository");

        ProxyFactory ingestRepository = new ProxyFactory(originalIngestRepository);
        ingestRepository.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findById".equals(invocation.getMethod().getName())) {
                assertThat(ingest.get()).isNull();
                ingest.set(currentPhysicalTransactionIdentity());
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ingestCommitted.set(true);
                    }
                });
            } else if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                sourceUpdate.set(currentPhysicalTransactionIdentity());
            }
            return result;
        });

        ProxyFactory finalizerRepository = new ProxyFactory(originalFinalizerRepository);
        finalizerRepository.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void beforeCommit(boolean readOnly) {
                        assertThat(finalizerBeforeCommit.get()).isNull();
                        finalizerBeforeCommit.set(currentPhysicalTransactionIdentity());
                        ingestCommittedBeforeFinalizerCommit.set(ingestCommitted.get());
                    }
                });
            }
            return result;
        });

        try {
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", ingestRepository.getProxy());
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", finalizerRepository.getProxy());

            if (requiresNewIngest) {
                NameMatchTransactionAttributeSource attributes = new NameMatchTransactionAttributeSource();
                attributes.addTransactionalMethod("ingestFetch",
                        new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
                TransactionInterceptor requiresNewAdvice = new TransactionInterceptor();
                requiresNewAdvice.setTransactionManager(transactionManager);
                requiresNewAdvice.setTransactionAttributeSource(attributes);
                ProxyFactory independentIngest = new ProxyFactory(originalOps);
                independentIngest.addAdvice(requiresNewAdvice);
                ReflectionTestUtils.setField(finalizerTarget, "feedItemOperations", independentIngest.getProxy());
            }

            ScheduledRefreshOutcome outcome = hackerNewsRefreshService.refreshScheduled(sourceId);
            assertThat(outcome).isEqualTo(ScheduledRefreshOutcome.REFRESHED);

            assertThat(ingest.get()).isNotNull();
            assertThat(sourceUpdate.get()).isNotNull();
            assertThat(finalizerBeforeCommit.get()).isNotNull();
            assertThat(ingestCommitted.get()).isTrue();

            return new RefreshTransactionIdentities(
                    ingest.get(),
                    sourceUpdate.get(),
                    finalizerBeforeCommit.get(),
                    ingestCommittedBeforeFinalizerCommit.get()
            );
        } finally {
            ReflectionTestUtils.setField(finalizerTarget, "feedItemOperations", originalOps);
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", originalFinalizerRepository);
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", originalIngestRepository);
        }
    }

    private RefreshTransactionIdentities captureTransactionIdentitiesRss(Long sourceId, boolean requiresNewIngest) {
        Object finalizerTarget = AopTestUtils.getTargetObject(rssFinalizer);
        Object ingestTarget = AopTestUtils.getTargetObject(feedItemOperations);

        AtomicReference<PhysicalTransactionIdentity> ingest = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> sourceUpdate = new AtomicReference<>();
        AtomicReference<PhysicalTransactionIdentity> finalizerBeforeCommit = new AtomicReference<>();
        AtomicBoolean ingestCommitted = new AtomicBoolean(false);
        AtomicBoolean ingestCommittedBeforeFinalizerCommit = new AtomicBoolean(false);

        FeedItemOperations originalOps = (FeedItemOperations) ReflectionTestUtils.getField(finalizerTarget, "feedItemOperations");
        FeedSourceRepository originalIngestRepository = (FeedSourceRepository) ReflectionTestUtils.getField(ingestTarget, "feedSourceRepository");
        FeedSourceRepository originalFinalizerRepository = (FeedSourceRepository) ReflectionTestUtils.getField(finalizerTarget, "feedSourceRepository");

        ProxyFactory ingestRepository = new ProxyFactory(originalIngestRepository);
        ingestRepository.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findById".equals(invocation.getMethod().getName())) {
                assertThat(ingest.get()).isNull();
                ingest.set(currentPhysicalTransactionIdentity());
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ingestCommitted.set(true);
                    }
                });
            } else if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                sourceUpdate.set(currentPhysicalTransactionIdentity());
            }
            return result;
        });

        ProxyFactory finalizerRepository = new ProxyFactory(originalFinalizerRepository);
        finalizerRepository.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();
            if ("findByIdForUpdate".equals(invocation.getMethod().getName())) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void beforeCommit(boolean readOnly) {
                        assertThat(finalizerBeforeCommit.get()).isNull();
                        finalizerBeforeCommit.set(currentPhysicalTransactionIdentity());
                        ingestCommittedBeforeFinalizerCommit.set(ingestCommitted.get());
                    }
                });
            }
            return result;
        });

        try {
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", ingestRepository.getProxy());
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", finalizerRepository.getProxy());

            if (requiresNewIngest) {
                NameMatchTransactionAttributeSource attributes = new NameMatchTransactionAttributeSource();
                attributes.addTransactionalMethod("ingestFetch",
                        new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
                TransactionInterceptor requiresNewAdvice = new TransactionInterceptor();
                requiresNewAdvice.setTransactionManager(transactionManager);
                requiresNewAdvice.setTransactionAttributeSource(attributes);
                ProxyFactory independentIngest = new ProxyFactory(originalOps);
                independentIngest.addAdvice(requiresNewAdvice);
                ReflectionTestUtils.setField(finalizerTarget, "feedItemOperations", independentIngest.getProxy());
            }

            ScheduledRefreshOutcome outcome = rssFeedRefreshService.refreshScheduled(sourceId);
            assertThat(outcome).isEqualTo(ScheduledRefreshOutcome.REFRESHED);

            assertThat(ingest.get()).isNotNull();
            assertThat(sourceUpdate.get()).isNotNull();
            assertThat(finalizerBeforeCommit.get()).isNotNull();
            assertThat(ingestCommitted.get()).isTrue();

            return new RefreshTransactionIdentities(
                    ingest.get(),
                    sourceUpdate.get(),
                    finalizerBeforeCommit.get(),
                    ingestCommittedBeforeFinalizerCommit.get()
            );
        } finally {
            ReflectionTestUtils.setField(finalizerTarget, "feedItemOperations", originalOps);
            ReflectionTestUtils.setField(finalizerTarget, "feedSourceRepository", originalFinalizerRepository);
            ReflectionTestUtils.setField(ingestTarget, "feedSourceRepository", originalIngestRepository);
        }
    }
}
