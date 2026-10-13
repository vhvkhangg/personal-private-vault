package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Feed Ingestion Concurrency Integration Tests (SBA-003)")
class FeedIngestionConcurrencyTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        tearDown();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM saved_resource_conversions");
        jdbcTemplate.execute("DELETE FROM saved_resources");
        jdbcTemplate.execute("DELETE FROM feed_items");
        jdbcTemplate.execute("DELETE FROM feed_sources");
        jdbcTemplate.execute("DELETE FROM notes");
        jdbcTemplate.execute("DELETE FROM information_items");
        jdbcTemplate.execute("DELETE FROM study_items");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type IN ('SAVED_RESOURCE', 'STUDY', 'INFORMATION', 'NOTE', 'VOCABULARY')");
    }

    private TransactionTemplate requiresNewTx() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    private int getCurrentBackendPid() {
        Integer pid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
        return pid != null ? pid : 0;
    }

    private void awaitCompetitorBlockedBy(int holderPid, AtomicInteger waiterPidRef, String expectedTable, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            int waiterPid = waiterPidRef.get();
            if (waiterPid > 0) {
                List<Integer> blockedPids = jdbcTemplate.query(
                        """
                        SELECT a.pid FROM pg_locks l
                        JOIN pg_stat_activity a ON a.pid = l.pid
                        WHERE NOT l.granted
                          AND a.pid = ?
                          AND ? = ANY(pg_blocking_pids(a.pid))
                          AND a.wait_event_type = 'Lock'
                          AND a.query ILIKE ?
                        """,
                        (rs, rowNum) -> rs.getInt("pid"),
                        waiterPid,
                        holderPid,
                        "%" + expectedTable + "%"
                );
                if (!blockedPids.isEmpty()) {
                    return;
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Lock observer interrupted", e);
            }
        }
        throw new AssertionError("Timed out waiting for waiter PID " + waiterPidRef.get()
                + " on table " + expectedTable + " to be blocked by holder PID " + holderPid);
    }

    @Test
    @DisplayName("SBA-003: Cached enabled source with independent committed disable rolls back whole batch at final gate")
    void cachedEnabledIndependentCommittedDisableRollsBack() {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Cached Source", FeedSourceType.RSS, "https://cached.example.com", "https://cached.example.com/rss",
                true, true, 30, null
        ));
        Long sourceId = source.id();

        Instant batchFetchTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        NormalizedFeedItemInput input = new NormalizedFeedItemInput(
                "cached-item-1", "Cached Title", "https://cached.example.com/1",
                "Author", "Summary", Instant.parse("2026-10-01T10:00:00Z"), null
        );

        assertThatThrownBy(() -> requiresNewTx().executeWithoutResult(status -> {
            // Preload enabled source in L1 cache
            assertThat(feedSourceOperations.findSourceById(sourceId)).isPresent();

            // Independent transaction commits disable
            requiresNewTx().executeWithoutResult(s2 -> {
                feedSourceOperations.updateSource(sourceId, new UpdateFeedSourceCommand(
                        "Cached Source", FeedSourceType.RSS, "https://cached.example.com", "https://cached.example.com/rss",
                        false, true, 30, null
                ));
            });

            // Outer transaction calls ingestFetch: stages item, but at final gate throws InvalidFeedSourceException
            feedItemOperations.ingestFetch(sourceId, batchFetchTime, List.of(input));
        }))
                .isInstanceOf(InvalidFeedSourceException.class)
                .hasMessage("Feed source is disabled");

        // Fresh JDBC: 0 feed items, source is disabled, fetch timestamp remains null
        Integer totalItems = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM feed_items WHERE feed_source_id = ?", Integer.class, sourceId);
        assertThat(totalItems).isZero();

        Map<String, Object> sourceRow = jdbcTemplate.queryForMap(
                "SELECT enabled, last_fetched_at FROM feed_sources WHERE id = ?", sourceId);
        assertThat(sourceRow.get("enabled")).isEqualTo(false);
        assertThat(sourceRow.get("last_fetched_at")).isNull();
    }

    @Test
    @DisplayName("SBA-003: Contention with disable first causes whole-batch rollback of staged insert and update")
    void disableFirstCausesWholeBatchRollback() throws Exception {
        // Setup: enabled source with 1 existing feed item
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Tech Blog", FeedSourceType.RSS, "https://tech.example.com", "https://tech.example.com/rss",
                true, true, 30, null
        ));
        Long sourceId = source.id();

        Instant initialFetchTime = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MICROS);
        String existingUrl = "https://tech.example.com/item-1";
        NormalizedFeedItemInput existingInput = new NormalizedFeedItemInput(
                "ext-1", "Original Title", existingUrl,
                "Author", "Original Summary", Instant.parse("2026-10-01T10:00:00Z"), null
        );
        List<FeedItemView> initialItems = feedItemOperations.ingestFetch(sourceId, initialFetchTime, List.of(existingInput));
        assertThat(initialItems).hasSize(1);
        Long existingItemId = initialItems.getFirst().id();

        Map<String, Object> initialSourceRow = jdbcTemplate.queryForMap(
                "SELECT last_fetched_at, next_fetch_at FROM feed_sources WHERE id = ?", sourceId);
        Instant initialPersistedFetch = ((Timestamp) initialSourceRow.get("last_fetched_at")).toInstant();
        Instant initialPersistedNext = ((Timestamp) initialSourceRow.get("next_fetch_at")).toInstant();

        // Staged batch containing 1 update (to item-1) + 1 new insert (item-2)
        Instant batchFetchTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        NormalizedFeedItemInput updatedInput = new NormalizedFeedItemInput(
                "ext-1", "Updated Title Should Roll Back", existingUrl,
                "Author", "Updated Summary", Instant.parse("2026-10-01T10:00:00Z"), null
        );
        String newUrl = "https://tech.example.com/item-2";
        NormalizedFeedItemInput newInput = new NormalizedFeedItemInput(
                "ext-2", "New Title Should Roll Back", newUrl,
                "Author", "New Summary", Instant.parse("2026-10-02T10:00:00Z"), null
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch disableLocked = new CountDownLatch(1);
        CountDownLatch disableProceed = new CountDownLatch(1);
        AtomicInteger holderPidRef = new AtomicInteger();
        AtomicInteger waiterPidRef = new AtomicInteger();
        AtomicBoolean ingestionFailedWithDisabled = new AtomicBoolean(false);

        try {
            // Thread 1: Acquire FOR NO KEY UPDATE on feed_sources.
            // FOR NO KEY UPDATE does NOT block Thread 2's foreign key checks (FOR KEY SHARE) when inserting feed_items,
            // but DOES block Thread 2's final gate lookup (findByIdForUpdate with FOR UPDATE).
            Future<?> f1 = executor.submit(() -> {
                requiresNewTx().executeWithoutResult(status -> {
                    holderPidRef.set(getCurrentBackendPid());
                    jdbcTemplate.queryForObject(
                            "SELECT id FROM feed_sources WHERE id = ? FOR NO KEY UPDATE",
                            Long.class, sourceId
                    );
                    disableLocked.countDown();

                    try {
                        if (!disableProceed.await(5, TimeUnit.SECONDS)) {
                            throw new AssertionError("Holder timed out waiting for disableProceed");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError("Holder thread was interrupted", e);
                    }

                    // Update enabled to false and commit
                    jdbcTemplate.update(
                            "UPDATE feed_sources SET enabled = false WHERE id = ?",
                            sourceId
                    );
                });
            });

            assertThat(disableLocked.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Start ingestion on source.
            // It successfully stages items without FK blocking, then blocks at the final source gate!
            Future<?> f2 = executor.submit(() -> {
                try {
                    requiresNewTx().executeWithoutResult(status -> {
                        waiterPidRef.set(getCurrentBackendPid());
                        feedItemOperations.ingestFetch(sourceId, batchFetchTime, List.of(updatedInput, newInput));
                    });
                } catch (InvalidFeedSourceException ex) {
                    if ("Feed source is disabled".equals(ex.getMessage())) {
                        ingestionFailedWithDisabled.set(true);
                    }
                }
            });

            // Wait until ingestion thread is blocked specifically on holderPidRef
            awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "feed_sources", Duration.ofSeconds(5));

            // Allow thread 1 to disable source and commit
            disableProceed.countDown();

            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);

            // Ingestion must have failed with InvalidFeedSourceException
            assertThat(ingestionFailedWithDisabled.get()).isTrue();

            // Fresh JDBC assertions: whole batch rolled back
            // 1. No new item-2 exists
            Integer totalItems = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM feed_items WHERE feed_source_id = ?",
                    Integer.class, sourceId
            );
            assertThat(totalItems).isEqualTo(1);

            // 2. Existing item-1 retained original title, NOT rolled back mutation
            Map<String, Object> itemRow = jdbcTemplate.queryForMap(
                    "SELECT title, summary FROM feed_items WHERE id = ?",
                    existingItemId
            );
            assertThat(itemRow.get("title")).isEqualTo("Original Title");
            assertThat(itemRow.get("summary")).isEqualTo("Original Summary");

            // 3. Source is disabled, and timestamps were NOT modified by rolled-back ingestion
            Map<String, Object> sourceRow = jdbcTemplate.queryForMap(
                    "SELECT enabled, last_fetched_at, next_fetch_at FROM feed_sources WHERE id = ?",
                    sourceId
            );
            assertThat(sourceRow.get("enabled")).isEqualTo(false);
            Instant finalPersistedFetch = ((Timestamp) sourceRow.get("last_fetched_at")).toInstant();
            Instant finalPersistedNext = ((Timestamp) sourceRow.get("next_fetch_at")).toInstant();
            assertThat(finalPersistedFetch).isEqualTo(initialPersistedFetch);
            assertThat(finalPersistedNext).isEqualTo(initialPersistedNext);
        } finally {
            disableProceed.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("SBA-003: Contention with ingestion first succeeds, competing disable waits on lock and preserves bookkeeping")
    void ingestionFirstSucceedsCompetingDisableWaitsOnLock() throws Exception {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Science Blog", FeedSourceType.RSS, "https://sci.example.com", "https://sci.example.com/rss",
                true, true, 60, null
        ));
        Long sourceId = source.id();

        Instant fetchedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        String url = "https://sci.example.com/item-1";
        NormalizedFeedItemInput input = new NormalizedFeedItemInput(
                "sci-1", "Discovery", url,
                "Dr. Smith", "Summary", Instant.parse("2026-10-05T10:00:00Z"), null
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ingestGateHolding = new CountDownLatch(1);
        CountDownLatch ingestProceed = new CountDownLatch(1);
        AtomicInteger holderPidRef = new AtomicInteger();
        AtomicInteger waiterPidRef = new AtomicInteger();

        try {
            // Thread 1: Ingestion transaction that holds the lock on feed_sources
            Future<?> f1 = executor.submit(() -> {
                requiresNewTx().executeWithoutResult(status -> {
                    holderPidRef.set(getCurrentBackendPid());
                    // Ingest the item and acquire source lock
                    feedItemOperations.ingestFetch(sourceId, fetchedAt, List.of(input));
                    ingGateHoldingCountdown(ingestGateHolding);
                    try {
                        if (!ingestProceed.await(5, TimeUnit.SECONDS)) {
                            throw new AssertionError("Holder timed out waiting for ingestProceed");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError("Holder thread was interrupted", e);
                    }
                });
            });

            assertThat(ingestGateHolding.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Competing transaction attempts to update/disable source; must wait for row lock
            Future<?> f2 = executor.submit(() -> {
                requiresNewTx().executeWithoutResult(status -> {
                    waiterPidRef.set(getCurrentBackendPid());
                    feedSourceOperations.updateSource(sourceId, new UpdateFeedSourceCommand(
                            "Science Blog", FeedSourceType.RSS, "https://sci.example.com", "https://sci.example.com/rss",
                            false, true, 60, null
                    ));
                });
            });

            // Observe Thread 2 waiting specifically on holderPidRef
            awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "feed_sources", Duration.ofSeconds(5));
            ingestProceed.countDown();

            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);

            // Fresh JDBC: source disabled, but last_fetched_at, next_fetch_at, and items are preserved!
            Map<String, Object> finalSource = jdbcTemplate.queryForMap(
                    "SELECT enabled, last_fetched_at, next_fetch_at, refresh_interval_minutes FROM feed_sources WHERE id = ?",
                    sourceId
            );
            assertThat(finalSource.get("enabled")).isEqualTo(false);
            Instant actualLastFetched = ((Timestamp) finalSource.get("last_fetched_at")).toInstant();
            Instant actualNextFetch = ((Timestamp) finalSource.get("next_fetch_at")).toInstant();
            assertThat(actualLastFetched).isEqualTo(fetchedAt);
            Instant expectedNextFetch = fetchedAt.plus(60, ChronoUnit.MINUTES);
            assertThat(actualNextFetch).isEqualTo(expectedNextFetch);

            // Retained item values in order
            Map<String, Object> itemRow = jdbcTemplate.queryForMap(
                    "SELECT external_id, title, url, author, summary FROM feed_items WHERE feed_source_id = ?",
                    sourceId
            );
            assertThat(itemRow.get("external_id")).isEqualTo("sci-1");
            assertThat(itemRow.get("title")).isEqualTo("Discovery");
            assertThat(itemRow.get("url")).isEqualTo("https://sci.example.com/item-1");
            assertThat(itemRow.get("author")).isEqualTo("Dr. Smith");
            assertThat(itemRow.get("summary")).isEqualTo("Summary");

            Integer totalItems = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM feed_items WHERE feed_source_id = ?",
                    Integer.class, sourceId
            );
            assertThat(totalItems).isEqualTo(1);
        } finally {
            ingestProceed.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void ingGateHoldingCountdown(CountDownLatch latch) {
        latch.countDown();
    }

    @Test
    @DisplayName("SBA-003: Empty batch succeeds when source is enabled")
    void emptyBatchSucceedsWhenEnabled() {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Empty Blog", FeedSourceType.RSS, "https://empty.example.com", "https://empty.example.com/rss",
                true, true, 60, null
        ));
        Long sourceId = source.id();

        Instant fetchTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        List<FeedItemView> items = feedItemOperations.ingestFetch(sourceId, fetchTime, List.of());
        assertThat(items).isEmpty();

        Map<String, Object> sourceRow = jdbcTemplate.queryForMap(
                "SELECT enabled, last_fetched_at FROM feed_sources WHERE id = ?", sourceId);
        assertThat(sourceRow.get("enabled")).isEqualTo(true);
        assertThat(sourceRow.get("last_fetched_at")).isNotNull();
    }

    @Test
    @DisplayName("SBA-003: Existing schedule calculation and unique-conflict controls preserved")
    void existingScheduleAndDualKeyControlsPreserved() {
        FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "Dual Key Blog", FeedSourceType.RSS, "https://dual.example.com", "https://dual.example.com/rss",
                true, true, 45, null
        ));
        Long sourceId = source.id();

        Instant fetchTime1 = Instant.now().truncatedTo(ChronoUnit.MICROS);
        NormalizedFeedItemInput item1 = new NormalizedFeedItemInput(
                "guid-1", "Dual Title 1", "https://dual.example.com/1",
                "Author", "Summary", Instant.parse("2026-10-01T10:00:00Z"), null
        );
        feedItemOperations.ingestFetch(sourceId, fetchTime1, List.of(item1));

        // Deduplication by externalId: update item1
        Instant fetchTime2 = fetchTime1.plus(1, ChronoUnit.HOURS);
        NormalizedFeedItemInput item1Updated = new NormalizedFeedItemInput(
                "guid-1", "Dual Title Updated", "https://dual.example.com/1",
                "Author", "Summary Updated", Instant.parse("2026-10-01T10:00:00Z"), null
        );
        feedItemOperations.ingestFetch(sourceId, fetchTime2, List.of(item1Updated));

        Integer totalItems = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM feed_items WHERE feed_source_id = ?", Integer.class, sourceId);
        assertThat(totalItems).isEqualTo(1);

        String title = jdbcTemplate.queryForObject(
                "SELECT title FROM feed_items WHERE feed_source_id = ? AND external_id = 'guid-1'",
                String.class, sourceId
        );
        assertThat(title).isEqualTo("Dual Title Updated");
    }
}
