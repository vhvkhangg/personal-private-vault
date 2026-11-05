package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.application.RssFeedRefreshFinalizer;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRefreshResultView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.feed.view.RssFeedRefreshResultView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RssFeedRefreshConcurrencyIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private RssFeedRefreshFinalizer rssFinalizer;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @MockitoBean
    private RssFeedClient rssFeedClient;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        executor = Executors.newFixedThreadPool(4);
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

    private FeedSourceView createRssSource(String name, String feedUrl, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.RSS,
                null,
                feedUrl,
                enabled,
                false,
                null,
                null
        ));
    }

    private FeedSourceView createHackerNewsSource(String name, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.HACKER_NEWS,
                "https://news.ycombinator.com",
                null,
                enabled,
                false,
                null,
                null
        ));
    }

    private static final String SIMPLE_RSS = """
            <rss version="2.0">
              <channel>
                <item>
                  <title>Concurrent RSS Item</title>
                  <link>https://example.com/concurrent-rss</link>
                  <guid>concurrent-guid-1</guid>
                  <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
            """;

    @Test
    @DisplayName("Admission: single-flight per source rejects concurrent refresh on same RSS source")
    void singleFlightSameRssSourceAdmission() throws Exception {
        FeedSourceView source = createRssSource("Single Flight RSS", "https://example.com/feed.xml", true);

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch unblockLatch = new CountDownLatch(1);

        when(rssFeedClient.fetchFeed(eq("https://example.com/feed.xml"), anyLong()))
                .thenAnswer(inv -> {
                    fetchBlockedLatch.countDown();
                    if (!unblockLatch.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout waiting to unblock fetch");
                    }
                    return SIMPLE_RSS.getBytes(StandardCharsets.UTF_8);
                });

        Future<RssFeedRefreshResultView> future1 = executor.submit(() ->
                feedSourceOperations.refreshRssSource(source.id()));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrent request on same RSS source must fail immediately with REFRESH_ALREADY_RUNNING
        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING));

        // Unblock first refresh and verify it completes
        unblockLatch.countDown();
        RssFeedRefreshResultView res1 = future1.get(5, TimeUnit.SECONDS);
        assertThat(res1.processedItems()).isEqualTo(1);

        // Subsequent refresh on the same source must succeed (admission released and reusable)
        RssFeedRefreshResultView res2 = feedSourceOperations.refreshRssSource(source.id());
        assertThat(res2.processedItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Capacity: global concurrency limit of 2 across HN and RSS rejects 3rd refresh")
    void globalCapacityLimitOfTwoAcrossHnAndRss() throws Exception {
        FeedSourceView hnSource = createHackerNewsSource("HN Concurrent Source", true);
        FeedSourceView rssSource1 = createRssSource("RSS Concurrent Source 1", "https://example.com/feed1.xml", true);
        FeedSourceView rssSource2 = createRssSource("RSS Concurrent Source 2", "https://example.com/feed2.xml", true);

        CountDownLatch twoRunningLatch = new CountDownLatch(2);
        CountDownLatch unblockLatch = new CountDownLatch(1);

        // Mock HN fetch blocking
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            twoRunningLatch.countDown();
            if (!unblockLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout waiting to unblock HN");
            }
            return List.of(9901L);
        });
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode story = mapper.createObjectNode();
        story.put("id", 9901L);
        story.put("type", "story");
        story.put("title", "HN Story");
        story.put("url", "https://example.com/hn-story");
        story.put("by", "author");
        story.put("time", 1700000000L);
        when(hackerNewsClient.fetchItem(eq(9901L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        // Mock RSS source 1 fetch blocking
        when(rssFeedClient.fetchFeed(eq("https://example.com/feed1.xml"), anyLong()))
                .thenAnswer(inv -> {
                    twoRunningLatch.countDown();
                    if (!unblockLatch.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout waiting to unblock RSS 1");
                    }
                    return SIMPLE_RSS.getBytes(StandardCharsets.UTF_8);
                });

        // Launch HN refresh and RSS refresh 1 concurrently
        Future<FeedRefreshResultView> hnFuture = executor.submit(() ->
                feedSourceOperations.refreshSource(hnSource.id(), 30));
        Future<RssFeedRefreshResultView> rssFuture1 = executor.submit(() ->
                feedSourceOperations.refreshRssSource(rssSource1.id()));

        assertThat(twoRunningLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // 3rd concurrent refresh (RSS source 2) must fail immediately with REFRESH_CAPACITY_REACHED
        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(rssSource2.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.REFRESH_CAPACITY_REACHED));

        // Unblock running refreshes
        unblockLatch.countDown();
        assertThat(hnFuture.get(5, TimeUnit.SECONDS).processedItems()).isEqualTo(1);
        assertThat(rssFuture1.get(5, TimeUnit.SECONDS).processedItems()).isEqualTo(1);

        // Now RSS source 2 can run successfully
        when(rssFeedClient.fetchFeed(eq("https://example.com/feed2.xml"), anyLong()))
                .thenReturn(SIMPLE_RSS.getBytes(StandardCharsets.UTF_8));
        RssFeedRefreshResultView rssRes2 = feedSourceOperations.refreshRssSource(rssSource2.id());
        assertThat(rssRes2.processedItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Race: source disabled while RSS HTTP fetch is blocked commits safely; finalizer rolls back all items")
    void sourceDisabledWhileFetchBlockedRollsBack() throws Exception {
        FeedSourceView source = createRssSource("RSS To Disable", "https://example.com/rss-disable.xml", true);

        // Pre-insert an item to prove preexisting row update is rolled back
        NormalizedFeedItemInput initialInput = new NormalizedFeedItemInput(
                null,
                "Original Preexisting Title",
                "https://example.com/concurrent-rss",
                "Author A",
                null,
                Instant.ofEpochSecond(1600000000L),
                null
        );
        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(initialInput));

        Instant preRefreshFetchedAt = feedSourceRepository.findById(source.id()).orElseThrow().getLastFetchedAt();

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceUpdatedLatch = new CountDownLatch(1);

        when(rssFeedClient.fetchFeed(eq("https://example.com/rss-disable.xml"), anyLong()))
                .thenAnswer(inv -> {
                    fetchBlockedLatch.countDown();
                    if (!sourceUpdatedLatch.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout waiting for source disable");
                    }
                    return SIMPLE_RSS.getBytes(StandardCharsets.UTF_8);
                });

        Future<RssFeedRefreshResultView> refreshFuture = executor.submit(() ->
                feedSourceOperations.refreshRssSource(source.id()));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrently disable source and commit independently
        feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                source.name(),
                FeedSourceType.RSS,
                source.sourceUrl(),
                source.feedUrl(),
                false, // disabled
                false,
                null,
                null
        ));

        // Unblock HTTP fetch
        sourceUpdatedLatch.countDown();

        // Refresh future must fail with SOURCE_CHANGED_DURING_REFRESH
        assertThatThrownBy(refreshFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex.getCause()).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));

        FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloaded.isEnabled()).isFalse();
        assertThat(reloaded.getLastFetchedAt()).isEqualTo(preRefreshFetchedAt);

        // Preexisting item was not modified
        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getTitle()).isEqualTo("Original Preexisting Title");
    }

    @Test
    @DisplayName("Race: feedUrl modified while RSS HTTP fetch is blocked commits safely; finalizer rolls back all items")
    void sourceFeedUrlChangedWhileFetchBlockedRollsBack() throws Exception {
        FeedSourceView source = createRssSource("RSS URL Changed", "https://example.com/original-feed.xml", true);

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceUpdatedLatch = new CountDownLatch(1);

        when(rssFeedClient.fetchFeed(eq("https://example.com/original-feed.xml"), anyLong()))
                .thenAnswer(inv -> {
                    fetchBlockedLatch.countDown();
                    if (!sourceUpdatedLatch.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout waiting for URL update");
                    }
                    return SIMPLE_RSS.getBytes(StandardCharsets.UTF_8);
                });

        Future<RssFeedRefreshResultView> refreshFuture = executor.submit(() ->
                feedSourceOperations.refreshRssSource(source.id()));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrently update feedUrl and commit independently
        feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                source.name(),
                FeedSourceType.RSS,
                source.sourceUrl(),
                "https://example.com/changed-feed.xml",
                true,
                false,
                null,
                null
        ));

        sourceUpdatedLatch.countDown();

        assertThatThrownBy(refreshFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex.getCause()).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));

        FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloaded.getFeedUrl()).isEqualTo("https://example.com/changed-feed.xml");
        assertThat(feedItemRepository.count()).isZero();
    }

    @Test
    @DisplayName("FR-06: Staged UPDATE plus INSERT rollback when feedUrl or type changes mid-refresh")
    void stagedUpdateAndInsertRollbackOnUrlChangeOrTypeFlip() throws Exception {
        FeedSourceView source = createRssSource("Staged Rollback RSS", "https://example.com/staged-original.xml", true);

        // Pre-insert Item A to be updated
        NormalizedFeedItemInput initialItemA = new NormalizedFeedItemInput(
                null,
                "Preexisting Item A",
                "https://example.com/art-a",
                "Author A",
                null,
                Instant.parse("2026-11-04T08:00:00Z"),
                null
        );
        feedItemOperations.ingestFetch(source.id(), Instant.parse("2026-11-04T08:00:00Z"), List.of(initialItemA));
        Instant preRefreshFetchedAt = feedSourceRepository.findById(source.id()).orElseThrow().getLastFetchedAt();

        String stagedXml = """
                <rss version="2.0"><channel>
                  <item>
                    <title>Staged Updated Item A</title>
                    <link>https://example.com/art-a</link>
                    <guid>guid-a</guid>
                    <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                  </item>
                  <item>
                    <title>Staged Brand New Item B</title>
                    <link>https://example.com/art-b</link>
                    <guid>guid-b</guid>
                    <pubDate>Wed, 04 Nov 2026 10:30:00 GMT</pubDate>
                  </item>
                </channel></rss>
                """;

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceUpdatedLatch = new CountDownLatch(1);

        when(rssFeedClient.fetchFeed(eq("https://example.com/staged-original.xml"), anyLong()))
                .thenAnswer(inv -> {
                    fetchBlockedLatch.countDown();
                    if (!sourceUpdatedLatch.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout waiting for source update");
                    }
                    return stagedXml.getBytes(StandardCharsets.UTF_8);
                });

        Future<RssFeedRefreshResultView> refreshFuture = executor.submit(() ->
                feedSourceOperations.refreshRssSource(source.id()));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrently change feedUrl on the source and commit independently
        feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                source.name(),
                FeedSourceType.RSS,
                source.sourceUrl(),
                "https://example.com/staged-changed.xml",
                true,
                false,
                null,
                null
        ));

        sourceUpdatedLatch.countDown();

        assertThatThrownBy(refreshFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex.getCause()).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));

        // Verify staged UPDATE rolled back: Item A has original title and timestamp
        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedItem itemA = items.get(0);
        assertThat(itemA.getUrl()).isEqualTo("https://example.com/art-a");
        assertThat(itemA.getTitle()).isEqualTo("Preexisting Item A");

        // Verify staged INSERT rolled back: Item B does not exist
        assertThat(items).noneMatch(i -> i.getUrl().equals("https://example.com/art-b"));

        // Source fetch timestamp remains unmodified
        FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloaded.getLastFetchedAt()).isEqualTo(preRefreshFetchedAt);
    }

    @Test
    @DisplayName("FR-06: Source type flipped from RSS to HACKER_NEWS mid-refresh rolls back staged items")
    void sourceTypeFlipWhileFetchBlockedRollsBack() throws Exception {
        FeedSourceView source = createRssSource("Type Flip RSS", "https://example.com/flip.xml", true);

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceUpdatedLatch = new CountDownLatch(1);

        when(rssFeedClient.fetchFeed(eq("https://example.com/flip.xml"), anyLong()))
                .thenAnswer(inv -> {
                    fetchBlockedLatch.countDown();
                    if (!sourceUpdatedLatch.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout waiting for type flip");
                    }
                    return SIMPLE_RSS.getBytes(StandardCharsets.UTF_8);
                });

        Future<RssFeedRefreshResultView> refreshFuture = executor.submit(() ->
                feedSourceOperations.refreshRssSource(source.id()));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrently change type from RSS to HACKER_NEWS
        feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                source.name(),
                FeedSourceType.HACKER_NEWS,
                "https://news.ycombinator.com",
                null,
                true,
                false,
                null,
                null
        ));

        sourceUpdatedLatch.countDown();

        assertThatThrownBy(refreshFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex.getCause()).getReason())
                        .isEqualTo(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));

        assertThat(feedItemRepository.count()).isZero();
        FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloaded.getType()).isEqualTo(FeedSourceType.HACKER_NEWS);
        assertThat(reloaded.getLastFetchedAt()).isNull();
    }

    @Test
    @DisplayName("FR-06: Real PostgreSQL lock timeout failure rolls back staged items and translates to REFRESH_FINALIZATION_UNAVAILABLE (503)")
    void realPostgresLockTimeoutRollsBackAndTranslatesToUnavailable() throws Exception {
        FeedSourceView source = createRssSource("RSS Real Lock Timeout", "https://example.com/lock-timeout.xml", true);

        when(rssFeedClient.fetchFeed(eq("https://example.com/lock-timeout.xml"), anyLong()))
                .thenReturn(SIMPLE_RSS.getBytes(StandardCharsets.UTF_8));

        CountDownLatch lockAcquiredLatch = new CountDownLatch(1);
        CountDownLatch releaseLockLatch = new CountDownLatch(1);

        Future<?> backgroundLockFuture = executor.submit(() -> {
            org.springframework.transaction.support.TransactionTemplate tt =
                    new org.springframework.transaction.support.TransactionTemplate(transactionManager);
            tt.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            tt.execute(status -> {
                jdbcTemplate.queryForObject("SELECT id FROM feed_sources WHERE id = ? FOR UPDATE", Long.class, source.id());
                lockAcquiredLatch.countDown();
                try {
                    releaseLockLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        });

        assertThat(lockAcquiredLatch.await(5, TimeUnit.SECONDS)).isTrue();

        FeedSourceRepository originalRepo = (FeedSourceRepository) ReflectionTestUtils.getField(feedItemOperations, "feedSourceRepository");
        FeedSourceRepository originalFinalizerRepo = (FeedSourceRepository) ReflectionTestUtils.getField(rssFinalizer, "feedSourceRepository");

        FeedSourceRepository proxyRepo = (FeedSourceRepository) java.lang.reflect.Proxy.newProxyInstance(
                FeedSourceRepository.class.getClassLoader(),
                new Class<?>[]{FeedSourceRepository.class},
                (proxy, method, args) -> {
                    if (args != null && args.length > 0 && source.id().equals(args[0])) {
                        entityManager.createNativeQuery("SET LOCAL lock_timeout = '300ms'").executeUpdate();
                    }
                    try {
                        return method.invoke(originalRepo, args);
                    } catch (java.lang.reflect.InvocationTargetException ite) {
                        throw ite.getTargetException();
                    }
                }
        );

        ReflectionTestUtils.setField(feedItemOperations, "feedSourceRepository", proxyRepo);
        ReflectionTestUtils.setField(rssFinalizer, "feedSourceRepository", proxyRepo);

        try {
            long startTime = System.currentTimeMillis();
            assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> {
                        FeedRefreshException fre = (FeedRefreshException) ex;
                        assertThat(fre.getReason())
                                .as("Lock timeout must translate to REFRESH_FINALIZATION_UNAVAILABLE")
                                .isEqualTo(FeedRefreshFailureReason.REFRESH_FINALIZATION_UNAVAILABLE);
                        org.springframework.http.ResponseEntity<?> resp =
                                new com.vhvkhangg.personalprivatevault.feed.internal.web.advice.FeedExceptionAdvice().handleRefreshException(fre);
                        assertThat(resp.getStatusCode().value())
                                .as("REFRESH_FINALIZATION_UNAVAILABLE must map to HTTP 503")
                                .isEqualTo(503);
                    });
            long durationMs = System.currentTimeMillis() - startTime;
            assertThat(durationMs)
                    .as("Duration with 300ms lock timeout must abort quickly (< 2500ms)")
                    .isLessThan(2500);

            List<FeedItem> items = feedItemRepository.findAll();
            assertThat(items).noneMatch(i -> i.getUrl().equals("https://example.com/concurrent-rss"));
        } finally {
            ReflectionTestUtils.setField(feedItemOperations, "feedSourceRepository", originalRepo);
            ReflectionTestUtils.setField(rssFinalizer, "feedSourceRepository", originalFinalizerRepo);
            releaseLockLatch.countDown();
            backgroundLockFuture.get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Locking: row lock held through outer commit; concurrent FOR UPDATE NOWAIT is blocked")
    void retainedSourceLockHeldUntilOuterCommit() throws Exception {
        FeedSourceView source = createRssSource("RSS Retained Lock", "https://example.com/lock-feed.xml", true);

        when(rssFeedClient.fetchFeed(eq("https://example.com/lock-feed.xml"), anyLong()))
                .thenReturn(SIMPLE_RSS.getBytes(StandardCharsets.UTF_8));

        AtomicReference<String> lockDeniedSqlState = new AtomicReference<>();
        CountDownLatch lockCheckDone = new CountDownLatch(1);

        FeedSourceRepository originalRepo = (FeedSourceRepository) ReflectionTestUtils.getField(rssFinalizer, "feedSourceRepository");

        FeedSourceRepository proxyRepo = (FeedSourceRepository) java.lang.reflect.Proxy.newProxyInstance(
                FeedSourceRepository.class.getClassLoader(),
                new Class<?>[]{FeedSourceRepository.class},
                (proxy, method, args) -> {
                    if ("findByIdForUpdate".equals(method.getName()) && args != null && args.length > 0 && source.id().equals(args[0])) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void beforeCommit(boolean readOnly) {
                                try {
                                    executor.submit(() -> {
                                        try {
                                            jdbcTemplate.queryForObject(
                                                    "SELECT id FROM feed_sources WHERE id = ? FOR UPDATE NOWAIT",
                                                    Long.class,
                                                    source.id()
                                            );
                                        } catch (Exception ex) {
                                            Throwable root = ex;
                                            while (root.getCause() != null) {
                                                root = root.getCause();
                                            }
                                            if (root instanceof PSQLException pe) {
                                                lockDeniedSqlState.set(pe.getSQLState());
                                            } else {
                                                lockDeniedSqlState.set(ex.getClass().getSimpleName());
                                            }
                                        } finally {
                                            lockCheckDone.countDown();
                                        }
                                    }).get(5, TimeUnit.SECONDS);
                                } catch (Exception e) {
                                    lockCheckDone.countDown();
                                }
                            }
                        });
                    }
                    try {
                        return method.invoke(originalRepo, args);
                    } catch (java.lang.reflect.InvocationTargetException ite) {
                        throw ite.getTargetException();
                    }
                }
        );

        ReflectionTestUtils.setField(rssFinalizer, "feedSourceRepository", proxyRepo);

        try {
            RssFeedRefreshResultView result = feedSourceOperations.refreshRssSource(source.id());
            assertThat(result.processedItems()).isEqualTo(1);

            assertThat(lockCheckDone.await(5, TimeUnit.SECONDS)).isTrue();
            // PostgreSQL 55P03 is lock_not_available
            assertThat(lockDeniedSqlState.get()).isEqualTo("55P03");
        } finally {
            ReflectionTestUtils.setField(rssFinalizer, "feedSourceRepository", originalRepo);
        }
    }

    @Test
    @DisplayName("Admission: exception during fetch releases admission permit for next caller")
    void admissionReleasedOnFetchException() {
        FeedSourceView source = createRssSource("Error RSS", "https://example.com/err.xml", true);

        when(rssFeedClient.fetchFeed(eq("https://example.com/err.xml"), anyLong()))
                .thenThrow(new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Denied"));

        assertThatThrownBy(() -> feedSourceOperations.refreshRssSource(source.id()))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));

        // Next caller succeeds immediately without REFRESH_ALREADY_RUNNING
        when(rssFeedClient.fetchFeed(eq("https://example.com/err.xml"), anyLong()))
                .thenReturn(SIMPLE_RSS.getBytes(StandardCharsets.UTF_8));

        RssFeedRefreshResultView result = feedSourceOperations.refreshRssSource(source.id());
        assertThat(result.processedItems()).isEqualTo(1);
    }
}
