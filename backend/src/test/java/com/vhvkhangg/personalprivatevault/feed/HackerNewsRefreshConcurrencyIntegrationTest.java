package com.vhvkhangg.personalprivatevault.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedItem;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.FeedSource;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.view.FeedRefreshResultView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class HackerNewsRefreshConcurrencyIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private com.vhvkhangg.personalprivatevault.feed.internal.application.HackerNewsRefreshFinalizer finalizer;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
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

    private JsonNode createStoryJson(long id, String title, String url, String author, long time) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("type", "story");
        node.put("title", title);
        node.put("url", url);
        node.put("by", author);
        node.put("time", time);
        return node;
    }

    @Test
    @DisplayName("Race: source edited to RSS while HTTP fetch is blocked commits safely; finalizer stages then rolls back all items")
    void sourceTypeChangedToRssWhileFetchBlocked_stagesAndRollsBack() throws Exception {
        FeedSourceView source = createHackerNewsSource("HN To RSS", true);

        // Pre-insert an item to prove preexisting row update is rolled back
        NormalizedFeedItemInput initialInput = new NormalizedFeedItemInput(
                null,
                "Original Preexisting Title",
                "https://example.com/preexisting",
                "Author A",
                null,
                Instant.ofEpochSecond(1600000000L),
                null
        );
        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(initialInput));

        FeedItem initial = feedItemRepository.findAll().get(0);
        assertThat(initial.getTitle()).isEqualTo("Original Preexisting Title");

        Instant preRefreshFetchedAt = feedSourceRepository.findById(source.id()).orElseThrow().getLastFetchedAt();

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceUpdatedLatch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            fetchBlockedLatch.countDown();
            if (!sourceUpdatedLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout waiting for source update");
            }
            return List.of(901L, 902L);
        });

        // 901 updates preexisting URL, 902 inserts brand-new item
        when(hackerNewsClient.fetchItem(eq(901L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(901L, "Updated Staged Title", "https://example.com/preexisting", "Author A", 1700000000L));
        when(hackerNewsClient.fetchItem(eq(902L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(902L, "Brand New Staged Item", "https://example.com/brand-new", "Author B", 1700000000L));

        Future<FeedRefreshResultView> refreshFuture = executor.submit(() ->
                feedSourceOperations.refreshSource(source.id(), 30));

        // Wait until HTTP fetch is in progress
        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrently edit source to RSS and commit independently
        feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                "Now RSS Source",
                FeedSourceType.RSS,
                "https://example.com/rss",
                null,
                true,
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

        // Authoritative source state in DB must be RSS, not reverted
        FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
        assertThat(reloaded.getType()).isEqualTo(FeedSourceType.RSS);
        assertThat(reloaded.getName()).isEqualTo("Now RSS Source");
        assertThat(reloaded.getLastFetchedAt()).isEqualTo(preRefreshFetchedAt);

        // Staged update and staged insert rolled back completely!
        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedItem itemAfterRollback = items.get(0);
        assertThat(itemAfterRollback.getId()).isEqualTo(initial.getId());
        assertThat(itemAfterRollback.getTitle()).isEqualTo("Original Preexisting Title");
        assertThat(items).noneMatch(i -> i.getUrl().equals("https://example.com/brand-new"));
    }

    @Test
    @DisplayName("Race: source disabled while HTTP fetch is blocked commits safely; finalizer stages then rolls back all items")
    void sourceDisabledWhileFetchBlocked_stagesAndRollsBack() throws Exception {
        FeedSourceView source = createHackerNewsSource("HN To Disable", true);

        // Pre-insert an item to prove preexisting row update is rolled back
        NormalizedFeedItemInput initialInput = new NormalizedFeedItemInput(
                null,
                "Original Preexisting Title",
                "https://example.com/preexisting",
                "Author A",
                null,
                Instant.ofEpochSecond(1600000000L),
                null
        );
        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(initialInput));

        FeedItem initial = feedItemRepository.findAll().get(0);
        assertThat(initial.getTitle()).isEqualTo("Original Preexisting Title");

        Instant preRefreshFetchedAt = feedSourceRepository.findById(source.id()).orElseThrow().getLastFetchedAt();

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceUpdatedLatch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            fetchBlockedLatch.countDown();
            if (!sourceUpdatedLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout waiting for source disable");
            }
            return List.of(903L, 904L);
        });

        // 903 updates preexisting URL, 904 inserts brand-new item
        when(hackerNewsClient.fetchItem(eq(903L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(903L, "Updated Staged Title", "https://example.com/preexisting", "Author A", 1700000000L));
        when(hackerNewsClient.fetchItem(eq(904L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(904L, "Brand New Staged Item", "https://example.com/brand-new", "Author B", 1700000000L));

        Future<FeedRefreshResultView> refreshFuture = executor.submit(() ->
                feedSourceOperations.refreshSource(source.id(), 30));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrently disable source and commit
        feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                source.name(),
                FeedSourceType.HACKER_NEWS,
                source.sourceUrl(),
                source.feedUrl(),
                false, // disabled
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
        assertThat(reloaded.isEnabled()).isFalse();
        assertThat(reloaded.getLastFetchedAt()).isEqualTo(preRefreshFetchedAt);

        // Staged update and staged insert rolled back completely!
        List<FeedItem> items = feedItemRepository.findAll();
        assertThat(items).hasSize(1);
        FeedItem itemAfterRollback = items.get(0);
        assertThat(itemAfterRollback.getId()).isEqualTo(initial.getId());
        assertThat(itemAfterRollback.getTitle()).isEqualTo("Original Preexisting Title");
        assertThat(items).noneMatch(i -> i.getUrl().equals("https://example.com/brand-new"));
    }

    @Test
    @DisplayName("Concurrency: independent source progresses while one refresh is blocked on HTTP fetch")
    void independentSourceProgressesWhileOneBlocked() throws Exception {
        FeedSourceView sourceA = createHackerNewsSource("Source A", true);
        FeedSourceView sourceB = createHackerNewsSource("Source B", true);

        CountDownLatch sourceABlockedLatch = new CountDownLatch(1);
        CountDownLatch sourceAUnblockLatch = new CountDownLatch(1);

        // Source A blocks in fetchTopStories
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            sourceABlockedLatch.countDown();
            if (!sourceAUnblockLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout waiting to unblock Source A");
            }
            return List.of(1001L);
        });
        when(hackerNewsClient.fetchItem(eq(1001L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(1001L, "Article A", "https://example.com/a", "authorA", 1700000000L));

        // Source B has its own items and does not block
        when(hackerNewsClient.fetchItem(eq(2001L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(2001L, "Article B", "https://example.com/b", "authorB", 1700000000L));

        Future<FeedRefreshResultView> futureA = executor.submit(() ->
                feedSourceOperations.refreshSource(sourceA.id(), 30));

        // Wait until Source A is blocked
        assertThat(sourceABlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // While Source A is blocked, mock client for Source B top stories
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(2001L));

        // Source B refresh runs and completes on another thread
        Future<FeedRefreshResultView> futureB = executor.submit(() ->
                feedSourceOperations.refreshSource(sourceB.id(), 30));

        FeedRefreshResultView resultB = futureB.get(5, TimeUnit.SECONDS);
        assertThat(resultB.sourceId()).isEqualTo(sourceB.id());
        assertThat(resultB.processedItems()).isEqualTo(1);

        // Unblock Source A and verify it also completes
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(1001L));
        sourceAUnblockLatch.countDown();

        FeedRefreshResultView resultA = futureA.get(5, TimeUnit.SECONDS);
        assertThat(resultA.sourceId()).isEqualTo(sourceA.id());
        assertThat(resultA.processedItems()).isEqualTo(1);

        // Both items exist in DB
        assertThat(feedItemRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Admission: single-flight per source rejects concurrent refresh and is reusable afterwards")
    void singleFlightSameSourceAdmission() throws Exception {
        FeedSourceView source = createHackerNewsSource("Single Flight Source", true);

        CountDownLatch fetchBlockedLatch = new CountDownLatch(1);
        CountDownLatch unblockLatch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            fetchBlockedLatch.countDown();
            if (!unblockLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout");
            }
            return List.of(3001L);
        });
        when(hackerNewsClient.fetchItem(eq(3001L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(3001L, "Title 3001", "https://example.com/3001", "author", 1700000000L));

        Future<FeedRefreshResultView> future1 = executor.submit(() ->
                feedSourceOperations.refreshSource(source.id(), 30));

        assertThat(fetchBlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Concurrent request on same source must fail immediately with REFRESH_ALREADY_RUNNING
        assertThatThrownBy(() -> feedSourceOperations.refreshSource(source.id(), 30))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.REFRESH_ALREADY_RUNNING));

        // Unblock first refresh
        unblockLatch.countDown();
        FeedRefreshResultView res1 = future1.get(5, TimeUnit.SECONDS);
        assertThat(res1.processedItems()).isEqualTo(1);

        // Subsequent refresh on the same source must succeed (admission released and reusable)
        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(3001L));
        FeedRefreshResultView res2 = feedSourceOperations.refreshSource(source.id(), 30);
        assertThat(res2.processedItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Capacity: global concurrency limit of 2 rejects 3rd concurrent refresh")
    void globalCapacityLimitOfTwo() throws Exception {
        FeedSourceView s1 = createHackerNewsSource("Cap S1", true);
        FeedSourceView s2 = createHackerNewsSource("Cap S2", true);
        FeedSourceView s3 = createHackerNewsSource("Cap S3", true);

        CountDownLatch blockedLatch = new CountDownLatch(2);
        CountDownLatch unblockLatch = new CountDownLatch(1);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenAnswer(inv -> {
            blockedLatch.countDown();
            if (!unblockLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timeout");
            }
            return List.of(4001L);
        });
        when(hackerNewsClient.fetchItem(anyLong(), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(4001L, "Cap Item", "https://example.com/cap", "author", 1700000000L));

        Future<FeedRefreshResultView> f1 = executor.submit(() -> feedSourceOperations.refreshSource(s1.id(), 30));
        Future<FeedRefreshResultView> f2 = executor.submit(() -> feedSourceOperations.refreshSource(s2.id(), 30));

        assertThat(blockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // 3rd concurrent refresh must fail with REFRESH_CAPACITY_REACHED
        assertThatThrownBy(() -> feedSourceOperations.refreshSource(s3.id(), 30))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.REFRESH_CAPACITY_REACHED));

        unblockLatch.countDown();
        assertThat(f1.get(5, TimeUnit.SECONDS).processedItems()).isEqualTo(1);
        assertThat(f2.get(5, TimeUnit.SECONDS).processedItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("FR-05: Source disabled strictly AFTER staging rolls back both staged inserts and staged updates")
    void sourceDisabledAfterStaging_rollsBackStagedInsertsAndUpdates() throws Exception {
        FeedSourceView source = createHackerNewsSource("HN Disable After Staging", true);

        // Pre-insert an item to prove preexisting row update is rolled back
        NormalizedFeedItemInput initialInput = new NormalizedFeedItemInput(
                null,
                "Original Preexisting Title",
                "https://example.com/preexisting",
                "Author A",
                null,
                Instant.ofEpochSecond(1600000000L),
                null
        );
        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(initialInput));

        FeedItem initial = feedItemRepository.findAll().get(0);
        assertThat(initial.getTitle()).isEqualTo("Original Preexisting Title");

        Instant preRefreshFetchedAt = feedSourceRepository.findById(source.id()).orElseThrow().getLastFetchedAt();

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(903L, 904L));
        when(hackerNewsClient.fetchItem(eq(903L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(903L, "Updated Staged Title", "https://example.com/preexisting", "Author A", 1700000000L));
        when(hackerNewsClient.fetchItem(eq(904L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(904L, "Brand New Staged Item", "https://example.com/brand-new", "Author B", 1700000000L));

        // Test-local dynamic proxy on FeedSourceRepository inside feedItemOperations:
        // Intercepts findByIdForUpdate, which is invoked strictly AFTER feed items have been staged,
        // but BEFORE the lock is acquired on the main thread
        FeedSourceRepository originalRepo = (FeedSourceRepository) ReflectionTestUtils.getField(feedItemOperations, "feedSourceRepository");

        FeedSourceRepository proxyRepo = (FeedSourceRepository) java.lang.reflect.Proxy.newProxyInstance(
                FeedSourceRepository.class.getClassLoader(),
                new Class<?>[]{FeedSourceRepository.class},
                (proxy, method, args) -> {
                    if ("findByIdForUpdate".equals(method.getName()) && args != null && args.length > 0 && source.id().equals(args[0])) {
                        try {
                            executor.submit(() -> {
                                jdbcTemplate.update("UPDATE feed_sources SET enabled = false WHERE id = ?", source.id());
                            }).get(5, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        return method.invoke(originalRepo, args);
                    } catch (java.lang.reflect.InvocationTargetException ite) {
                        throw ite.getTargetException();
                    }
                }
        );

        ReflectionTestUtils.setField(feedItemOperations, "feedSourceRepository", proxyRepo);

        try {
            assertThatThrownBy(() -> feedSourceOperations.refreshSource(source.id(), 30))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH));

            FeedSource reloaded = feedSourceRepository.findById(source.id()).orElseThrow();
            assertThat(reloaded.isEnabled()).isFalse();
            assertThat(reloaded.getLastFetchedAt()).isEqualTo(preRefreshFetchedAt);

            // Staged update and staged insert rolled back completely!
            List<FeedItem> items = feedItemRepository.findAll();
            assertThat(items).hasSize(1);
            FeedItem itemAfterRollback = items.get(0);
            assertThat(itemAfterRollback.getId()).isEqualTo(initial.getId());
            assertThat(itemAfterRollback.getTitle()).isEqualTo("Original Preexisting Title");
            assertThat(items).noneMatch(i -> i.getUrl().equals("https://example.com/brand-new"));
        } finally {
            ReflectionTestUtils.setField(feedItemOperations, "feedSourceRepository", originalRepo);
        }
    }

    @Test
    @DisplayName("FR-05: Retained source lock held through outer commit; concurrent FOR UPDATE NOWAIT is blocked/rejected")
    void retainedSourceLockHeldUntilOuterCommit() throws Exception {
        FeedSourceView source = createHackerNewsSource("HN Retained Lock", true);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(905L));
        when(hackerNewsClient.fetchItem(eq(905L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(905L, "Retained Lock Item", "https://example.com/retained-lock", "Author", 1700000000L));

        java.util.concurrent.atomic.AtomicReference<String> lockDeniedSqlState = new java.util.concurrent.atomic.AtomicReference<>();
        CountDownLatch lockCheckDone = new CountDownLatch(1);

        FeedSourceRepository originalRepo = (FeedSourceRepository) ReflectionTestUtils.getField(finalizer, "feedSourceRepository");

        FeedSourceRepository proxyRepo = (FeedSourceRepository) java.lang.reflect.Proxy.newProxyInstance(
                FeedSourceRepository.class.getClassLoader(),
                new Class<?>[]{FeedSourceRepository.class},
                (proxy, method, args) -> {
                    if ("findByIdForUpdate".equals(method.getName()) && args != null && args.length > 0 && source.id().equals(args[0])) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void beforeCommit(boolean readOnly) {
                                // This runs AFTER finalizeRefresh has passed final validation,
                                // while the transaction is holding the row lock and about to commit!
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
                                            if (root instanceof org.postgresql.util.PSQLException pe) {
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

        ReflectionTestUtils.setField(finalizer, "feedSourceRepository", proxyRepo);

        try {
            FeedRefreshResultView result = feedSourceOperations.refreshSource(source.id(), 30);
            assertThat(result.processedItems()).isEqualTo(1);

            assertThat(lockCheckDone.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(lockDeniedSqlState.get())
                    .as("Concurrent FOR UPDATE NOWAIT must be rejected with PostgreSQL 55P03 (lock_not_available) while transaction is in beforeCommit")
                    .isEqualTo("55P03");

            // After outer commit completes, lock is released and concurrent FOR UPDATE NOWAIT succeeds immediately
            Long lockedId = jdbcTemplate.queryForObject(
                    "SELECT id FROM feed_sources WHERE id = ? FOR UPDATE NOWAIT",
                    Long.class,
                    source.id()
            );
            assertThat(lockedId).isEqualTo(source.id());
        } finally {
            ReflectionTestUtils.setField(finalizer, "feedSourceRepository", originalRepo);
        }
    }

    @Test
    @DisplayName("FR-05: Real PostgreSQL lock timeout failure rolls back staged items and translates to REFRESH_FINALIZATION_UNAVAILABLE (503)")
    void realPostgresLockTimeoutRollsBackAndTranslatesToUnavailable() throws Exception {
        FeedSourceView source = createHackerNewsSource("HN Real Lock Timeout", true);

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(906L));
        when(hackerNewsClient.fetchItem(eq(906L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(906L, "Timeout Item", "https://example.com/timeout-item", "Author", 1700000000L));

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
        FeedSourceRepository originalFinalizerRepo = (FeedSourceRepository) ReflectionTestUtils.getField(finalizer, "feedSourceRepository");

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
        ReflectionTestUtils.setField(finalizer, "feedSourceRepository", proxyRepo);

        try {
            long startTime = System.currentTimeMillis();
            assertThatThrownBy(() -> feedSourceOperations.refreshSource(source.id(), 30))
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
                    .as("Duration with 300ms lock timeout must abort quickly (< 2500ms), proving 300ms timeout configuration took effect")
                    .isLessThan(2500);

            List<FeedItem> items = feedItemRepository.findAll();
            assertThat(items).noneMatch(i -> i.getUrl().equals("https://example.com/timeout-item"));
        } finally {
            ReflectionTestUtils.setField(feedItemOperations, "feedSourceRepository", originalRepo);
            ReflectionTestUtils.setField(finalizer, "feedSourceRepository", originalFinalizerRepo);
            releaseLockLatch.countDown();
            backgroundLockFuture.get(5, TimeUnit.SECONDS);
        }
    }
}
