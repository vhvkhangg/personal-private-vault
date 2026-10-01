package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.conversion.SavedResourceConversionOperations;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemConflictException;
import com.vhvkhangg.personalprivatevault.feed.item.exception.InvalidFeedItemException;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceConflictException;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedItemView;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceConversionView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class FeedIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private SavedResourceOperations savedResourceOperations;

    @Autowired
    private SavedResourceConversionOperations conversionOperations;

    @Autowired
    private KnowledgeOperations knowledgeOperations;

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

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // =========================================================================
    // 1. Schema Validation
    // =========================================================================
    @Nested
    @DisplayName("Schema validation tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Verifies all four feed tables exist in PostgreSQL schema")
        void verifiesFeedTablesExist() {
            List<String> tables = jdbcTemplate.query(
                    """
                    SELECT table_name FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name IN (
                        'feed_sources', 'feed_items', 'saved_resources', 'saved_resource_conversions'
                    )
                    """,
                    (rs, rowNum) -> rs.getString("table_name")
            );

            assertThat(tables).containsExactlyInAnyOrder(
                    "feed_sources", "feed_items", "saved_resources", "saved_resource_conversions"
            );
        }
    }

    // =========================================================================
    // 2. FeedSource Operations
    // =========================================================================
    @Nested
    @DisplayName("FeedSource operations tests")
    class FeedSourceOperationsTests {

        @Test
        @DisplayName("Creates feed source with defaults and retrieves by ID")
        void createsFeedSourceWithDefaults() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Engineering RSS",
                    FeedSourceType.RSS,
                    "https://engineering.example.com",
                    "https://engineering.example.com/rss",
                    null,
                    null,
                    null,
                    null
            ));

            assertThat(source.id()).isNotNull();
            assertThat(source.name()).isEqualTo("Engineering RSS");
            assertThat(source.type()).isEqualTo(FeedSourceType.RSS);
            assertThat(source.enabled()).isTrue();
            assertThat(source.scheduledRefreshEnabled()).isFalse();
            assertThat(source.refreshIntervalMinutes()).isNull();
            assertThat(source.lastFetchedAt()).isNull();
            assertThat(source.nextFetchAt()).isNull();

            Optional<FeedSourceView> found = feedSourceOperations.findSourceById(source.id());
            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(source.id());
        }

        @Test
        @DisplayName("Creates feed source with scheduled refresh and computes due state")
        void createsFeedSourceWithScheduledRefresh() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Hacker News Front",
                    FeedSourceType.HACKER_NEWS,
                    "https://news.ycombinator.com",
                    null,
                    true,
                    true,
                    60,
                    Map.of("category", "frontpage")
            ));

            assertThat(source.enabled()).isTrue();
            assertThat(source.scheduledRefreshEnabled()).isTrue();
            assertThat(source.refreshIntervalMinutes()).isEqualTo(60);
            assertThat(source.nextFetchAt()).isNull(); // No prior fetch, due immediately

            Instant now = Instant.now();
            List<FeedSourceView> dueSources = feedSourceOperations.findDueSources(now, 10);
            assertThat(dueSources).extracting(FeedSourceView::id).contains(source.id());
        }

        @Test
        @DisplayName("Updates feed source and adjusts next_fetch_at correctly")
        void updatesFeedSourceAndAdjustsNextFetchAt() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Tech Reddit",
                    FeedSourceType.REDDIT,
                    null,
                    null,
                    true,
                    false,
                    null,
                    null
            ));

            // Simulate fetch
            Instant fetchTime = Instant.now().minus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MICROS);
            feedItemOperations.ingestFetch(source.id(), fetchTime, List.of(
                    new NormalizedFeedItemInput(null, "item1", "https://reddit.com/r/tech/1", null, null, null, null)
            ));

            FeedSourceView fetched = feedSourceOperations.findSourceById(source.id()).orElseThrow();
            assertThat(fetched.lastFetchedAt()).isEqualTo(fetchTime);
            assertThat(fetched.nextFetchAt()).isNull(); // scheduling was disabled

            // Now enable scheduling with 30 min interval -> next_fetch_at = last_fetched_at + 30m
            FeedSourceView updated = feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                    "Tech Reddit Active",
                    FeedSourceType.REDDIT,
                    null,
                    null,
                    true,
                    true,
                    30,
                    null
            ));

            assertThat(updated.scheduledRefreshEnabled()).isTrue();
            assertThat(updated.refreshIntervalMinutes()).isEqualTo(30);
            assertThat(updated.nextFetchAt()).isEqualTo(fetchTime.plus(30, ChronoUnit.MINUTES));

            // Now disable scheduling -> next_fetch_at cleared to null
            FeedSourceView disabled = feedSourceOperations.updateSource(source.id(), new UpdateFeedSourceCommand(
                    "Tech Reddit Inactive",
                    FeedSourceType.REDDIT,
                    null,
                    null,
                    true,
                    false,
                    30,
                    null
            ));

            assertThat(disabled.scheduledRefreshEnabled()).isFalse();
            assertThat(disabled.nextFetchAt()).isNull();
        }

        @Test
        @DisplayName("Validates scheduling constraints: enabling schedule requires positive interval")
        void validatesSchedulingConstraints() {
            assertThatThrownBy(() -> feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Invalid Schedule",
                    FeedSourceType.WEBSITE,
                    null, null,
                    true,
                    true, // scheduled enabled
                    null, // interval missing!
                    null
            )))
                    .isInstanceOf(InvalidFeedSourceException.class)
                    .hasMessageContaining("Scheduled refresh requires a positive refresh interval");

            assertThatThrownBy(() -> feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Invalid Interval",
                    FeedSourceType.WEBSITE,
                    null, null,
                    true,
                    true,
                    -10,
                    null
            )))
                    .isInstanceOf(InvalidFeedSourceException.class)
                    .hasMessageContaining("Refresh interval must be greater than zero");
        }

        @Test
        @DisplayName("Due sources ordering: null next_fetch_at first, then ascending next_fetch_at, then id ASC")
        void dueSourcesOrdering() {
            Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MICROS);

            // Source 1: null next_fetch_at (due immediately)
            FeedSourceView s1 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Source 1", FeedSourceType.RSS, null, null, true, true, 60, null
            ));

            // Source 2: next_fetch_at in 10 minutes (simulate by fetching 50 mins ago)
            FeedSourceView s2 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Source 2", FeedSourceType.RSS, null, null, true, true, 60, null
            ));
            feedItemOperations.ingestFetch(s2.id(), baseTime.minus(50, ChronoUnit.MINUTES), List.of());

            // Source 3: next_fetch_at in 5 minutes (simulate by fetching 55 mins ago)
            FeedSourceView s3 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Source 3", FeedSourceType.RSS, null, null, true, true, 60, null
            ));
            feedItemOperations.ingestFetch(s3.id(), baseTime.minus(55, ChronoUnit.MINUTES), List.of());

            // Source 4: disabled scheduled refresh (should not appear)
            FeedSourceView s4 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Source 4", FeedSourceType.RSS, null, null, true, false, null, null
            ));

            // Cutoff is baseTime + 15 mins
            Instant cutoff = baseTime.plus(15, ChronoUnit.MINUTES);
            List<FeedSourceView> due = feedSourceOperations.findDueSources(cutoff, 10);

            assertThat(due).extracting(FeedSourceView::id).containsExactly(s1.id(), s3.id(), s2.id());

            // Limit check
            List<FeedSourceView> dueLimited = feedSourceOperations.findDueSources(cutoff, 2);
            assertThat(dueLimited).hasSize(2);
            assertThat(dueLimited).extracting(FeedSourceView::id).containsExactly(s1.id(), s3.id());
        }

        @Test
        @DisplayName("Due read is strictly side-effect free")
        void dueReadIsSideEffectFree() {
            FeedSourceView s1 = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Source S", FeedSourceType.RSS, null, null, true, true, 60, null
            ));

            feedSourceOperations.findDueSources(Instant.now(), 10);
            FeedSourceView fresh = feedSourceOperations.findSourceById(s1.id()).orElseThrow();
            assertThat(fresh.lastFetchedAt()).isNull();
            assertThat(fresh.nextFetchAt()).isNull();
        }
    }

    // =========================================================================
    // 3. Feed Item Ingestion & Dual-Key Uniqueness
    // =========================================================================
    @Nested
    @DisplayName("Feed item ingestion & dual-key tests")
    class FeedItemIngestionTests {

        @Test
        @DisplayName("Ingest fetch inserts items, computes url_hash as trimmed SHA-256 hex, and updates source timestamps")
        void ingestFetchBasic() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "GH Trending", FeedSourceType.GITHUB_TRENDING, null, null, true, true, 120, null
            ));

            Instant fetchedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
            String itemUrl = "  https://github.com/torvalds/linux  ";
            String expectedHash = sha256Hex(itemUrl);

            List<FeedItemView> items = feedItemOperations.ingestFetch(source.id(), fetchedAt, List.of(
                    new NormalizedFeedItemInput("gh-1", "Linux Kernel", itemUrl, "Linus", "Linux mirror", fetchedAt, Map.of("stars", 180000))
            ));

            assertThat(items).hasSize(1);
            FeedItemView item = items.getFirst();
            assertThat(item.id()).isNotNull();
            assertThat(item.feedSourceId()).isEqualTo(source.id());
            assertThat(item.title()).isEqualTo("Linux Kernel");
            assertThat(item.url()).isEqualTo("https://github.com/torvalds/linux");
            assertThat(item.urlHash()).isEqualTo(expectedHash);
            assertThat(item.externalId()).isEqualTo("gh-1");
            assertThat(item.rawMetadata()).containsEntry("stars", 180000);

            // Source timestamps updated
            FeedSourceView updatedSource = feedSourceOperations.findSourceById(source.id()).orElseThrow();
            assertThat(updatedSource.lastFetchedAt()).isEqualTo(fetchedAt);
            assertThat(updatedSource.nextFetchAt()).isEqualTo(fetchedAt.plus(120, ChronoUnit.MINUTES));
        }

        @Test
        @DisplayName("Dual-key upsert: updates existing item when url_hash or external_id matches")
        void dualKeyUpsert() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "HN", FeedSourceType.HACKER_NEWS, null, null, true, false, null, null
            ));

            Instant t1 = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MICROS);
            List<FeedItemView> initial = feedItemOperations.ingestFetch(source.id(), t1, List.of(
                    new NormalizedFeedItemInput("hn-100", "Show HN: PPV", "https://news.ycombinator.com/item?id=100", "alice", "v1", t1, null)
            ));
            Long itemId = initial.getFirst().id();

            // Refresh 1: Same external ID, updated title and metadata
            Instant t2 = Instant.now().truncatedTo(ChronoUnit.MICROS);
            List<FeedItemView> refreshed = feedItemOperations.ingestFetch(source.id(), t2, List.of(
                    new NormalizedFeedItemInput("hn-100", "Show HN: PPV v2", "https://news.ycombinator.com/item?id=100", "alice", "v2 updated", t2, Map.of("points", 50))
            ));

            assertThat(refreshed).hasSize(1);
            assertThat(refreshed.getFirst().id()).isEqualTo(itemId);
            assertThat(refreshed.getFirst().title()).isEqualTo("Show HN: PPV v2");
            assertThat(refreshed.getFirst().summary()).isEqualTo("v2 updated");

            // Verify count in DB is still 1
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM feed_items WHERE feed_source_id = ?", Integer.class, source.id());
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Dual-key conflict: rejects batch when external_id and url_hash resolve to different existing rows")
        void dualKeyCrossRowConflict() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Reddit", FeedSourceType.REDDIT, null, null, true, false, null, null
            ));

            Instant t1 = Instant.now().truncatedTo(ChronoUnit.MICROS);
            feedItemOperations.ingestFetch(source.id(), t1, List.of(
                    new NormalizedFeedItemInput("ext-1", "Post 1", "https://reddit.com/r/1", null, null, null, null),
                    new NormalizedFeedItemInput("ext-2", "Post 2", "https://reddit.com/r/2", null, null, null, null)
            ));

            // Candidate pairs ext-1 with url 2 -> cross-row split ambiguity!
            assertThatThrownBy(() -> feedItemOperations.ingestFetch(source.id(), t1.plusSeconds(60), List.of(
                    new NormalizedFeedItemInput("ext-1", "Conflicting Post", "https://reddit.com/r/2", null, null, null, null)
            )))
                    .isInstanceOf(FeedItemConflictException.class)
                    .hasMessageContaining("Feed item key ambiguity");
        }

        @Test
        @DisplayName("Rejects conflicting same-key candidates in one submitted batch")
        void conflictingCandidatesInSameBatch() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "News", FeedSourceType.WEBSITE, null, null, true, false, null, null
            ));

            Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
            assertThatThrownBy(() -> feedItemOperations.ingestFetch(source.id(), now, List.of(
                    new NormalizedFeedItemInput("s1", "Title A", "https://example.com/story", null, null, null, null),
                    new NormalizedFeedItemInput("s1", "Title B (Conflicting)", "https://example.com/story", null, null, null, null)
            )))
                    .isInstanceOf(FeedItemConflictException.class)
                    .hasMessageContaining("Conflicting feed item candidates");
        }

        @Test
        @DisplayName("Batch failure rolls back completely without advancing source fetch timestamps")
        void batchFailureRollback() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "News", FeedSourceType.WEBSITE, null, null, true, false, null, null
            ));

            Instant attemptTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
            assertThatThrownBy(() -> feedItemOperations.ingestFetch(source.id(), attemptTime, List.of(
                    new NormalizedFeedItemInput("s1", "Valid Item", "https://example.com/valid", null, null, null, null),
                    new NormalizedFeedItemInput("s2", "Blank URL", "", null, null, null, null) // will fail validation
            )))
                    .isInstanceOf(InvalidFeedItemException.class);

            // Verify no items were saved
            Integer itemCount = jdbcTemplate.queryForObject("SELECT count(*) FROM feed_items WHERE feed_source_id = ?", Integer.class, source.id());
            assertThat(itemCount).isEqualTo(0);

            // Verify source timestamps remain null
            FeedSourceView currentSource = feedSourceOperations.findSourceById(source.id()).orElseThrow();
            assertThat(currentSource.lastFetchedAt()).isNull();
            assertThat(currentSource.nextFetchAt()).isNull();
        }

        @Test
        @DisplayName("Concurrent dual-key ingestion converges safely into PostgreSQL unique key contention")
        void concurrentDualKeyIngestionConvergesSafely(CapturedOutput output) throws Exception {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Concurrent Feed", FeedSourceType.RSS, null, null, true, false, null, null
            ));

            String secretParam = "SECRET_INGEST_777";
            String privateItemUrl = "https://feed.example.com/item-1?param=" + secretParam;
            NormalizedFeedItemInput input = new NormalizedFeedItemInput(
                    "ext-concurrent-1",
                    "Concurrent Title",
                    privateItemUrl,
                    "Author",
                    "Summary",
                    Instant.now(),
                    Map.of()
            );

            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, ingests fetch item (which executes flush), holds uncommitted lock
                Future<List<FeedItemView>> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    List<FeedItemView> items = feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(input));
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return items;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                // Thread 2: Ingests same item. In READ_COMMITTED isolation, MVCC pre-checks do not see uncommitted Thread 1.
                // Thread 2 attempts to insert and flushes, blocking in PostgreSQL on feed_items unique constraints.
                Future<List<FeedItemView>> thread2Future = executor.submit(() ->
                        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(input))
                );

                // Observe actual PostgreSQL lock contention on feed_items
                awaitCompetingLock("feed_items", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                List<FeedItemView> items1 = thread1Future.get(10, TimeUnit.SECONDS);
                assertThat(items1).hasSize(1);

                // Thread 2 must unblock, catch DataIntegrityViolationException and throw FeedItemConflictException
                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(FeedItemConflictException.class)
                        .hasMessage("Feed item unique constraint violation during ingestion");

                Integer itemCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM feed_items WHERE feed_source_id = ?",
                        Integer.class,
                        source.id()
                );
                assertThat(itemCount).isEqualTo(1);

                assertThat(output.getAll()).doesNotContain(secretParam);
                assertThat(output.getAll()).doesNotContain(privateItemUrl);
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("Concurrent ingestion colliding on external_id races safely into PostgreSQL constraint contention")
        void concurrentDualKeyIngestionCollidingOnExternalIdRacesSafely() throws Exception {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Concurrent Ext Feed", FeedSourceType.RSS, null, null, true, false, null, null
            ));

            NormalizedFeedItemInput input1 = new NormalizedFeedItemInput(
                    "ext-shared-id", "Title 1", "https://example.com/unique-url-1", null, null, Instant.now(), null
            );
            NormalizedFeedItemInput input2 = new NormalizedFeedItemInput(
                    "ext-shared-id", "Title 2", "https://example.com/unique-url-2", null, null, Instant.now(), null
            );

            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                Future<List<FeedItemView>> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    List<FeedItemView> items = feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(input1));
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return items;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                Future<List<FeedItemView>> thread2Future = executor.submit(() ->
                        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(input2))
                );

                awaitCompetingLock("feed_items", Duration.ofSeconds(5));
                thread2ReadyToCommit.countDown();

                assertThat(thread1Future.get(10, TimeUnit.SECONDS)).hasSize(1);

                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(FeedItemConflictException.class)
                        .hasMessage("Feed item unique constraint violation during ingestion");

                Integer itemCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM feed_items WHERE feed_source_id = ?",
                        Integer.class,
                        source.id()
                );
                assertThat(itemCount).isEqualTo(1);
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("Concurrent ingestion colliding on url_hash races safely into PostgreSQL constraint contention")
        void concurrentDualKeyIngestionCollidingOnUrlHashRacesSafely() throws Exception {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Concurrent Hash Feed", FeedSourceType.RSS, null, null, true, false, null, null
            ));

            NormalizedFeedItemInput input1 = new NormalizedFeedItemInput(
                    "ext-id-alpha", "Title Alpha", "https://example.com/same-url", null, null, Instant.now(), null
            );
            NormalizedFeedItemInput input2 = new NormalizedFeedItemInput(
                    "ext-id-beta", "Title Beta", "https://example.com/same-url", null, null, Instant.now(), null
            );

            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                Future<List<FeedItemView>> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    List<FeedItemView> items = feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(input1));
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return items;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                Future<List<FeedItemView>> thread2Future = executor.submit(() ->
                        feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(input2))
                );

                awaitCompetingLock("feed_items", Duration.ofSeconds(5));
                thread2ReadyToCommit.countDown();

                assertThat(thread1Future.get(10, TimeUnit.SECONDS)).hasSize(1);

                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(FeedItemConflictException.class)
                        .hasMessage("Feed item unique constraint violation during ingestion");

                Integer itemCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM feed_items WHERE feed_source_id = ?",
                        Integer.class,
                        source.id()
                );
                assertThat(itemCount).isEqualTo(1);
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("Reads recent feed items bounded and ordered by published_at DESC NULLS LAST, id DESC")
        void readsRecentFeedItemsOrdered() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Blog", FeedSourceType.RSS, null, null, true, false, null, null
            ));

            Instant t0 = Instant.now().truncatedTo(ChronoUnit.MICROS);
            Instant t1 = t0.minus(10, ChronoUnit.HOURS);
            Instant t2 = t0.minus(5, ChronoUnit.HOURS);

            feedItemOperations.ingestFetch(source.id(), t0, List.of(
                    new NormalizedFeedItemInput("1", "Item Null 1", "https://example.com/null1", null, null, null, null),
                    new NormalizedFeedItemInput("2", "Item Early", "https://example.com/early", null, null, t1, null),
                    new NormalizedFeedItemInput("3", "Item Late", "https://example.com/late", null, null, t2, null),
                    new NormalizedFeedItemInput("4", "Item Null 2", "https://example.com/null2", null, null, null, null)
            ));

            List<FeedItemView> recent = feedItemOperations.findRecentItemsBySource(source.id(), 10);
            assertThat(recent).hasSize(4);

            // Ordered by published_at DESC NULLS LAST, then id DESC
            assertThat(recent.get(0).title()).isEqualTo("Item Late");  // t2 (-5h)
            assertThat(recent.get(1).title()).isEqualTo("Item Early"); // t1 (-10h)
            // The two null items ordered by id DESC
            assertThat(recent.get(2).publishedAt()).isNull();
            assertThat(recent.get(3).publishedAt()).isNull();
            assertThat(recent.get(2).id()).isGreaterThan(recent.get(3).id());
        }
    }

    // =========================================================================
    // 4. SavedResource Operations
    // =========================================================================
    @Nested
    @DisplayName("SavedResource operations tests")
    class SavedResourceOperationsTests {

        @Test
        @DisplayName("Creates manual saved resource backed by Vault SAVED_RESOURCE entry")
        void createsManualSavedResource() {
            SavedResourceView resource = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Spring Modulith Guide",
                    "https://spring.io/projects/spring-modulith",
                    "Oliver Drotbohm",
                    "A modular monolith architecture framework for Spring Boot",
                    null,
                    "Spring",
                    "https://spring.io",
                    null,
                    Map.of("read_later", true)
            ));

            assertThat(resource.id()).isNotNull();
            assertThat(resource.feedItemId()).isNull();
            assertThat(resource.title()).isEqualTo("Spring Modulith Guide");
            assertThat(resource.resourceUrl()).isEqualTo("https://spring.io/projects/spring-modulith");
            assertThat(resource.resourceUrlHash()).isEqualTo(sha256Hex("https://spring.io/projects/spring-modulith"));
            assertThat(resource.kind()).isEqualTo(SavedResourceKind.ARTICLE);
            assertThat(resource.author()).isEqualTo("Oliver Drotbohm");
            assertThat(resource.rawMetadata()).containsEntry("read_later", true);

            // Verify Vault entry exists with same ID and entry_type = 'SAVED_RESOURCE'
            String vaultType = jdbcTemplate.queryForObject(
                    "SELECT entry_type FROM vault_entries WHERE id = ?",
                    String.class,
                    resource.id()
            );
            assertThat(vaultType).isEqualTo("SAVED_RESOURCE");
        }

        @Test
        @DisplayName("Creates saved resource from feed item retaining provenance snapshot")
        void createsSavedResourceFromFeedItem() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "GH", FeedSourceType.GITHUB_TRENDING, null, null, true, false, null, null
            ));
            Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
            FeedItemView feedItem = feedItemOperations.ingestFetch(source.id(), now, List.of(
                    new NormalizedFeedItemInput("r-1", "Repo 1", "https://github.com/example/repo1", "author-x", "cool repo", now, Map.of("forks", 42))
            )).getFirst();

            SavedResourceView saved = savedResourceOperations.saveFeedItem(new CreateFeedSavedResourceCommand(
                    feedItem.id(),
                    SavedResourceKind.REPOSITORY
            ));

            assertThat(saved.id()).isNotNull();
            assertThat(saved.feedItemId()).isEqualTo(feedItem.id());
            assertThat(saved.title()).isEqualTo("Repo 1");
            assertThat(saved.author()).isEqualTo("author-x");
            assertThat(saved.summary()).isEqualTo("cool repo");
            assertThat(saved.rawMetadata()).containsEntry("forks", 42);
        }

        @Test
        @DisplayName("Duplicate resource URL hash is rejected with conflict and leaves no orphan Vault entry")
        void duplicateUrlHashRollback() {
            String url = "https://example.com/unique-article";
            savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Article 1", url, null, null, null, null, null, null, null
            ));

            int vaultCountBefore = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'",
                    Integer.class
            );

            // Attempt duplicate save with same URL
            assertThatThrownBy(() -> savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Article 2 (Duplicate)", url, null, null, null, null, null, null, null
            )))
                    .isInstanceOf(SavedResourceConflictException.class)
                    .hasMessageContaining("A saved resource with this URL already exists");

            // Verify no orphan Vault entry was retained
            int vaultCountAfter = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'",
                    Integer.class
            );
            assertThat(vaultCountAfter).isEqualTo(vaultCountBefore);
        }

        @Test
        @DisplayName("Concurrent manual saved resource creation races safely without orphan vault entries or secret leakage")
        void concurrentSavedResourceCreationRacesSafely(CapturedOutput output) throws Exception {
            String secretToken = "SECRET_MANUAL_999";
            String privateUrl = "https://example.com/concurrent-manual?token=" + secretToken;
            CreateManualSavedResourceCommand command = new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Concurrent Manual",
                    privateUrl,
                    null, null, null, null, null, null, null
            );

            int vaultCountBefore = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'",
                    Integer.class
            );

            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: Executes in new transaction, calls saveManual (which executes saveAndFlush),
                // holds uncommitted lock, and waits for Thread 2 to hit PostgreSQL unique key contention.
                Future<SavedResourceView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    SavedResourceView resource = savedResourceOperations.saveManual(command);
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return resource;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                // Thread 2: Attempts to create saved resource with same URL.
                // In READ_COMMITTED isolation, MVCC pre-check does not see uncommitted Thread 1.
                // It creates a Vault entry, calls saveAndFlush, and blocks in PostgreSQL on uq_saved_resources_url_hash.
                Future<SavedResourceView> thread2Future = executor.submit(() -> savedResourceOperations.saveManual(command));

                // Observe actual PostgreSQL lock contention on saved_resources
                awaitCompetingLock("saved_resources", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                SavedResourceView resource1 = thread1Future.get(10, TimeUnit.SECONDS);
                assertThat(resource1).isNotNull();

                // Thread 2 must unblock, catch DataIntegrityViolationException and throw SavedResourceConflictException
                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(SavedResourceConflictException.class)
                        .hasMessage("A saved resource with this URL already exists");

                int savedResourceCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM saved_resources WHERE resource_url_hash = ?",
                        Integer.class,
                        sha256Hex(privateUrl)
                );
                assertThat(savedResourceCount).isEqualTo(1);

                // Verify losing transaction rolled back its Vault entry: exactly 1 SAVED_RESOURCE added
                int vaultCountAfter = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'",
                        Integer.class
                );
                assertThat(vaultCountAfter).isEqualTo(vaultCountBefore + 1);

                assertThat(output.getAll()).doesNotContain(secretToken);
                assertThat(output.getAll()).doesNotContain(privateUrl);
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("Concurrent feed saved resource creation races safely without orphan vault entries or secret leakage")
        void concurrentFeedSavedResourceCreationRacesSafely(CapturedOutput output) throws Exception {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Feed Race", FeedSourceType.RSS, null, null, true, false, null, null
            ));
            String secretToken = "SECRET_FEED_888";
            String privateUrl = "https://example.com/concurrent-feed-item?token=" + secretToken;
            FeedItemView feedItem = feedItemOperations.ingestFetch(source.id(), Instant.now(), List.of(
                    new NormalizedFeedItemInput("item-race", "Race Item", privateUrl, null, null, Instant.now(), null)
            )).getFirst();

            int vaultCountBefore = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'",
                    Integer.class
            );

            CreateFeedSavedResourceCommand command = new CreateFeedSavedResourceCommand(
                    feedItem.id(),
                    SavedResourceKind.ARTICLE
            );

            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, calls saveFeedItem (executing saveAndFlush), holds uncommitted lock
                Future<SavedResourceView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    SavedResourceView resource = savedResourceOperations.saveFeedItem(command);
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return resource;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                // Thread 2: Calls saveFeedItem with same feed item.
                // Passes MVCC pre-check because Thread 1 is uncommitted, creates Vault entry, flushes and blocks in PostgreSQL.
                Future<SavedResourceView> thread2Future = executor.submit(() -> savedResourceOperations.saveFeedItem(command));

                // Observe actual PostgreSQL lock contention on saved_resources
                awaitCompetingLock("saved_resources", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                SavedResourceView resource1 = thread1Future.get(10, TimeUnit.SECONDS);
                assertThat(resource1).isNotNull();

                // Thread 2 must unblock and receive SavedResourceConflictException
                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(SavedResourceConflictException.class)
                        .hasMessage("A saved resource with this URL already exists");

                int savedResourceCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM saved_resources WHERE feed_item_id = ?",
                        Integer.class,
                        feedItem.id()
                );
                assertThat(savedResourceCount).isEqualTo(1);

                // Verify losing transaction rolled back its Vault entry: exactly 1 SAVED_RESOURCE added
                int vaultCountAfter = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'",
                        Integer.class
                );
                assertThat(vaultCountAfter).isEqualTo(vaultCountBefore + 1);

                assertThat(output.getAll()).doesNotContain(secretToken);
                assertThat(output.getAll()).doesNotContain(privateUrl);
            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("Finds saved resource by ID and by URL hash")
        void findsSavedResourceByIdAndHash() {
            String url = "https://example.com/lookup-test";
            SavedResourceView created = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.WEB_PAGE,
                    "Lookup Test", url, null, null, null, null, null, null, null
            ));

            Optional<SavedResourceView> byId = savedResourceOperations.findSavedResourceById(created.id());
            assertThat(byId).isPresent();
            assertThat(byId.get().id()).isEqualTo(created.id());

            Optional<SavedResourceView> byHash = savedResourceOperations.findSavedResourceByUrlHash(created.resourceUrlHash());
            assertThat(byHash).isPresent();
            assertThat(byHash.get().id()).isEqualTo(created.id());
        }

        @Test
        @DisplayName("Finds bounded recent saved resources ordered by saved_at DESC, id DESC")
        void findsRecentSavedResources() {
            SavedResourceView r1 = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "R1", "https://example.com/r1", null, null, null, null, null, null, null
            ));
            SavedResourceView r2 = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "R2", "https://example.com/r2", null, null, null, null, null, null, null
            ));

            List<SavedResourceView> recent = savedResourceOperations.findRecentSavedResources(10);
            assertThat(recent).hasSize(2);
            assertThat(recent.get(0).id()).isEqualTo(r2.id());
            assertThat(recent.get(1).id()).isEqualTo(r1.id());
        }
    }

    // =========================================================================
    // 5. SavedResource Conversion Operations (Knowledge Targets)
    // =========================================================================
    @Nested
    @DisplayName("SavedResource conversion tests")
    class SavedResourceConversionTests {

        @Test
        @DisplayName("Converts saved resource to StudyItem and records conversion history")
        void convertsToStudyItem() {
            SavedResourceView resource = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Postgres Internals Book", "https://postgres.org/book", null, null, null, null, null, null, null
            ));

            SavedResourceConversionView conversion = conversionOperations.convertSavedResourceToStudy(
                    resource.id(),
                    new CreateKnowledgeStudyItemCommand(
                            "PostgreSQL Deep Dive",
                            null,
                            KnowledgeStudyType.BOOK,
                            null, null, null, null, null, null, null,
                            "Deep dive into Postgres internals",
                            "https://postgres.org/book",
                            null,
                            KnowledgeStudyStatus.IN_PROGRESS,
                            null, null
                    )
            );

            assertThat(conversion.savedResourceId()).isEqualTo(resource.id());
            assertThat(conversion.targetVaultEntryId()).isNotNull();

            // Verify target exists in vault_entries as STUDY
            String entryType = jdbcTemplate.queryForObject(
                    "SELECT entry_type FROM vault_entries WHERE id = ?",
                    String.class,
                    conversion.targetVaultEntryId()
            );
            assertThat(entryType).isEqualTo("STUDY");

            // Verify conversion history
            List<SavedResourceConversionView> history = conversionOperations.findConversionsBySavedResourceId(resource.id(), 10);
            assertThat(history).hasSize(1);
            assertThat(history.getFirst().targetVaultEntryId()).isEqualTo(conversion.targetVaultEntryId());
        }

        @Test
        @DisplayName("Converts saved resource to InformationItem and Note, allowing multiple conversions")
        void convertsToInformationItemAndNote() {
            SavedResourceView resource = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Architecture Doc", "https://architecture.example.com", null, null, null, null, null, null, null
            ));

            // Convert 1: Information
            SavedResourceConversionView infoConv = conversionOperations.convertSavedResourceToInformation(
                    resource.id(),
                    new CreateKnowledgeInformationItemCommand(
                            "Clean Architecture Summary",
                            KnowledgeInformationType.TECHNOLOGY,
                            "Summary description",
                            "# Clean Architecture",
                            null, null, null
                    )
            );
            assertThat(infoConv.targetVaultEntryId()).isNotNull();

            // Convert 2: Note
            SavedResourceConversionView noteConv = conversionOperations.convertSavedResourceToNote(
                    resource.id(),
                    new CreateKnowledgeNoteCommand(
                            "Study Note on Clean Architecture",
                            "# Clean Architecture\n\nNotes from article...",
                            "Summary",
                            null, null, null, null, null
                    )
            );
            assertThat(noteConv.targetVaultEntryId()).isNotNull();
            assertThat(noteConv.targetVaultEntryId()).isNotEqualTo(infoConv.targetVaultEntryId());

            // History contains both conversions
            List<SavedResourceConversionView> history = conversionOperations.findConversionsBySavedResourceId(resource.id(), 10);
            assertThat(history).hasSize(2);
            assertThat(history).extracting(SavedResourceConversionView::targetVaultEntryId)
                    .containsExactlyInAnyOrder(infoConv.targetVaultEntryId(), noteConv.targetVaultEntryId());
        }

        @Test
        @DisplayName("Conversion failure after target creation rolls back target, vault entry, and provenance link")
        void conversionFailureRollback() {
            SavedResourceView resource = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE,
                    "Rollback Doc", "https://rollback.example.com", null, null, null, null, null, null, null
            ));

            int vaultCountBefore = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries",
                    Integer.class
            );

            // Install trigger that fails only on saved_resource_conversions insert
            jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION fail_conversion_insert() RETURNS TRIGGER AS $$
                BEGIN
                    RAISE EXCEPTION 'Simulated provenance insert failure';
                END;
                $$ LANGUAGE plpgsql;
            """);
            jdbcTemplate.execute("""
                CREATE TRIGGER trg_fail_conversion_insert
                BEFORE INSERT ON saved_resource_conversions
                FOR EACH ROW EXECUTE FUNCTION fail_conversion_insert();
            """);

            try {
                // Attempt valid study item conversion; target creation succeeds, but conversion insert fails
                CreateKnowledgeStudyItemCommand validCommand = new CreateKnowledgeStudyItemCommand(
                        "Valid Study Title",
                        null,
                        KnowledgeStudyType.BOOK,
                        null, null, null, null, null, null, null,
                        "Description", "https://rollback.example.com", null,
                        KnowledgeStudyStatus.PLANNED,
                        null, null
                );

                assertThatThrownBy(() -> conversionOperations.convertSavedResourceToStudy(resource.id(), validCommand))
                        .hasMessageContaining("Simulated provenance insert failure");

                // Verify the entire transaction rolled back: no study item, no study vault entry, no conversion
                int studyCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM study_items",
                        Integer.class
                );
                assertThat(studyCount).isEqualTo(0);

                int studyVaultCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM vault_entries WHERE entry_type = 'STUDY'",
                        Integer.class
                );
                assertThat(studyVaultCount).isEqualTo(0);

                int vaultCountAfter = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM vault_entries",
                        Integer.class
                );
                assertThat(vaultCountAfter).isEqualTo(vaultCountBefore);

                List<SavedResourceConversionView> history = conversionOperations.findConversionsBySavedResourceId(resource.id(), 10);
                assertThat(history).isEmpty();
            } finally {
                jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_fail_conversion_insert ON saved_resource_conversions");
                jdbcTemplate.execute("DROP FUNCTION IF EXISTS fail_conversion_insert()");
            }
        }
    }

    // =========================================================================
    // 6. JSON Snapshot Deep Isolation
    // =========================================================================
    @Nested
    @DisplayName("JSON snapshot deep isolation tests")
    class JsonSnapshotIsolationTests {

        @Test
        @DisplayName("Mutating input map after save does not affect stored or retrieved JSON")
        void inputMapIsolation() {
            Map<String, Object> mutableConfig = new LinkedHashMap<>();
            mutableConfig.put("topic", "algorithms");
            mutableConfig.put("tags", List.of("tree", "graph"));

            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "Algo Feed", FeedSourceType.WEBSITE, null, null, true, false, null, mutableConfig
            ));

            // Mutate input map
            mutableConfig.put("topic", "MUTATED");
            mutableConfig.put("extra", "MALICIOUS");

            FeedSourceView retrieved = feedSourceOperations.findSourceById(source.id()).orElseThrow();
            assertThat(retrieved.config()).containsEntry("topic", "algorithms");
            assertThat(retrieved.config()).doesNotContainKey("extra");
        }

        @Test
        @DisplayName("Retrieved view returns defensive snapshot that cannot mutate underlying data")
        void viewMapImmutability() {
            FeedSourceView source = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                    "View Imm", FeedSourceType.WEBSITE, null, null, true, false, null, Map.of("key", "value")
            ));

            FeedSourceView retrieved = feedSourceOperations.findSourceById(source.id()).orElseThrow();
            assertThatThrownBy(() -> retrieved.config().put("illegal", "edit"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // =========================================================================
    // 7. Privacy-Safe Errors & Logging
    // =========================================================================
    @Nested
    @DisplayName("Privacy-safe error tests")
    class PrivacySafeErrorTests {

        @Test
        @DisplayName("Conflict exceptions do not leak raw URLs or secret tokens")
        void conflictExceptionsDoNotLeakRawUrls(CapturedOutput output) {
            String privateUrl = "https://internal.corp/token=SECRET_12345/doc";
            savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.WEB_PAGE,
                    "Private Doc", privateUrl, null, null, null, null, null, null, null
            ));

            assertThatThrownBy(() -> savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                    SavedResourceKind.WEB_PAGE,
                    "Private Doc Duplicate", privateUrl, null, null, null, null, null, null, null
            )))
                    .isInstanceOf(SavedResourceConflictException.class)
                    .satisfies(ex -> {
                        assertThat(ex.getMessage()).doesNotContain(privateUrl);
                        assertThat(ex.getMessage()).doesNotContain("SECRET_12345");
                    });

            assertThat(output.getAll()).doesNotContain("SECRET_12345");
        }
    }

    private void awaitCompetingLock(String tablePattern, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l " +
                    "JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted " +
                    "  AND a.pid != pg_backend_pid() " +
                    "  AND a.query ILIKE ?",
                    Integer.class,
                    "%" + tablePattern + "%"
            );
            if (count != null && count > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
