package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.application.SavedResourceService;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedItemRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.SavedResourceRepository;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceInboxPageView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.search.VaultSearchOperations;
import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResource;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavedResourceInboxIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private SavedResourceOperations savedResourceOperations;

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private VaultSearchOperations vaultSearchOperations;

    @Autowired
    private SavedResourceRepository savedResourceRepository;

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private FeedSourceRepository feedSourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanTables();
    }

    @AfterEach
    void tearDown() {
        cleanTables();
    }

    private void cleanTables() {
        jdbcTemplate.execute("DELETE FROM saved_resource_conversions");
        jdbcTemplate.execute("DELETE FROM saved_resources");
        jdbcTemplate.execute("DELETE FROM feed_items");
        jdbcTemplate.execute("DELETE FROM feed_sources");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'");
    }

    private SavedResourceView saveResource(String slug, SavedResourceKind kind) {
        return savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                kind != null ? kind : SavedResourceKind.ARTICLE,
                "Title " + slug,
                "https://example.com/res-" + slug,
                "Author",
                "Summary",
                null,
                "Source",
                "https://example.com",
                null,
                null
        ));
    }

    private void trashVaultEntry(Long id) {
        vaultEntryOperations.moveToTrash(id);
    }

    private void restoreVaultEntry(Long id) {
        vaultEntryOperations.restore(id);
    }

    @Test
    @DisplayName("Empty inbox returns empty items list and null nextCursor")
    void emptyInboxReturnsEmptyPage() {
        SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 50, null);

        assertThat(page.items()).isEmpty();
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Single eligible item returns list of size 1 and null nextCursor")
    void singleItemReturnsPageWithNullCursor() {
        SavedResourceView saved = saveResource("single", SavedResourceKind.ARTICLE);

        SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 50, null);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).id()).isEqualTo(saved.id());
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Exactly 100 eligible rows with limit 100 returns 100 items and null nextCursor")
    void exactly100ItemsReturnsNullCursor() {
        for (int i = 1; i <= 100; i++) {
            saveResource("item-" + i, SavedResourceKind.ARTICLE);
        }

        SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 100, null);

        assertThat(page.items()).hasSize(100);
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("101 eligible rows with limit 100 returns 100 items with cursor, and page 2 returns 1 item with null cursor")
    void boundary101ItemsPaginatesCorrectly() {
        List<Long> expectedIdsDesc = new ArrayList<>();
        for (int i = 1; i <= 101; i++) {
            SavedResourceView saved = saveResource("paged-" + i, SavedResourceKind.ARTICLE);
            expectedIdsDesc.add(0, saved.id()); // Latest inserted has highest savedAt / id
        }

        // Page 1: limit 100
        SavedResourceInboxPageView page1 = savedResourceOperations.findInboxSavedResources(null, 100, null);
        assertThat(page1.items()).hasSize(100);
        assertThat(page1.nextCursor()).isNotNull();

        List<Long> page1Ids = page1.items().stream().map(SavedResourceView::id).toList();
        assertThat(page1Ids).isEqualTo(expectedIdsDesc.subList(0, 100));

        // Page 2: with cursor from page 1
        SavedResourceInboxPageView page2 = savedResourceOperations.findInboxSavedResources(null, 100, page1.nextCursor());
        assertThat(page2.items()).hasSize(1);
        assertThat(page2.nextCursor()).isNull();

        List<Long> page2Ids = page2.items().stream().map(SavedResourceView::id).toList();
        assertThat(page2Ids).isEqualTo(expectedIdsDesc.subList(100, 101));

        // Combined: all 101 items, 0 duplicates, 0 skips
        List<Long> combined = new ArrayList<>(page1Ids);
        combined.addAll(page2Ids);
        assertThat(combined).isEqualTo(expectedIdsDesc);
    }

    @Test
    @DisplayName("Ties in saved_at timestamps are ordered strictly by id DESC without duplicate or skip across pages")
    void savedAtTiesPaginateDeterministically() {
        Instant tieTimestamp = Instant.parse("2026-11-01T10:00:00.123456Z");
        List<Long> insertedIds = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            SavedResourceView v = saveResource("tie-" + i, SavedResourceKind.ARTICLE);
            insertedIds.add(v.id());
            // Align all to the exact same database timestamp
            jdbcTemplate.update("UPDATE saved_resources SET saved_at = ? WHERE id = ?",
                    Timestamp.from(tieTimestamp), v.id());
        }

        // Sort descending by ID to match tie-breaker
        insertedIds.sort((a, b) -> Long.compare(b, a));

        // Query with page size 2
        SavedResourceInboxPageView page1 = savedResourceOperations.findInboxSavedResources(null, 2, null);
        assertThat(page1.items()).hasSize(2);
        assertThat(page1.nextCursor()).isNotNull();
        assertThat(page1.items().stream().map(SavedResourceView::id).toList())
                .isEqualTo(insertedIds.subList(0, 2));

        SavedResourceInboxPageView page2 = savedResourceOperations.findInboxSavedResources(null, 2, page1.nextCursor());
        assertThat(page2.items()).hasSize(2);
        assertThat(page2.nextCursor()).isNotNull();
        assertThat(page2.items().stream().map(SavedResourceView::id).toList())
                .isEqualTo(insertedIds.subList(2, 4));

        SavedResourceInboxPageView page3 = savedResourceOperations.findInboxSavedResources(null, 2, page2.nextCursor());
        assertThat(page3.items()).hasSize(2);
        assertThat(page3.nextCursor()).isNull();
        assertThat(page3.items().stream().map(SavedResourceView::id).toList())
                .isEqualTo(insertedIds.subList(4, 6));
    }

    @Test
    @DisplayName("Kind filter restricts results before qualification and persists across cursor continuation")
    void kindFilterAppliesDeterministically() {
        SavedResourceView art1 = saveResource("k-art1", SavedResourceKind.ARTICLE);
        SavedResourceView rep1 = saveResource("k-rep1", SavedResourceKind.REPOSITORY);
        SavedResourceView art2 = saveResource("k-art2", SavedResourceKind.ARTICLE);
        SavedResourceView soc1 = saveResource("k-soc1", SavedResourceKind.SOCIAL_POST);
        SavedResourceView art3 = saveResource("k-art3", SavedResourceKind.ARTICLE);

        // Fetch ARTICLE items with limit 2
        SavedResourceInboxPageView page1 = savedResourceOperations.findInboxSavedResources(SavedResourceKind.ARTICLE, 2, null);
        assertThat(page1.items()).hasSize(2);
        assertThat(page1.items().get(0).id()).isEqualTo(art3.id());
        assertThat(page1.items().get(1).id()).isEqualTo(art2.id());
        assertThat(page1.nextCursor()).isNotNull();

        // Continuation for ARTICLE
        SavedResourceInboxPageView page2 = savedResourceOperations.findInboxSavedResources(SavedResourceKind.ARTICLE, 2, page1.nextCursor());
        assertThat(page2.items()).hasSize(1);
        assertThat(page2.items().get(0).id()).isEqualTo(art1.id());
        assertThat(page2.nextCursor()).isNull();
    }

    @Test
    @DisplayName("More than 100 leading trashed rows do not cause false exhaustion or hide active lookahead")
    void leadingTrashDoesNotCauseFalseExhaustion() {
        // Create 105 trashed rows (earlier/higher saved_at or later)
        List<Long> trashedIds = new ArrayList<>();
        for (int i = 1; i <= 105; i++) {
            SavedResourceView v = saveResource("trash-" + i, SavedResourceKind.ARTICLE);
            trashedIds.add(v.id());
            trashVaultEntry(v.id());
        }

        // Create 3 active rows
        SavedResourceView act1 = saveResource("act-1", SavedResourceKind.ARTICLE);
        SavedResourceView act2 = saveResource("act-2", SavedResourceKind.ARTICLE);
        SavedResourceView act3 = saveResource("act-3", SavedResourceKind.ARTICLE);

        // Even though act3, act2, act1 were inserted after, let's test where active rows are OLDER than the 105 trashed rows
        // by updating saved_at of active rows to be older than the trashed rows
        Instant olderTime = Instant.now().minus(10, ChronoUnit.DAYS);
        jdbcTemplate.update("UPDATE saved_resources SET saved_at = ? WHERE id = ?", Timestamp.from(olderTime), act3.id());
        jdbcTemplate.update("UPDATE saved_resources SET saved_at = ? WHERE id = ?", Timestamp.from(olderTime.minus(1, ChronoUnit.HOURS)), act2.id());
        jdbcTemplate.update("UPDATE saved_resources SET saved_at = ? WHERE id = ?", Timestamp.from(olderTime.minus(2, ChronoUnit.HOURS)), act1.id());

        // Now: 105 leading trashed rows in descending saved_at order, followed by 3 active rows.
        // A single batch of 100 will encounter ONLY trashed rows!
        // The scanner must scan into batch 2 and find the 3 active rows.
        SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 10, null);

        assertThat(page.items()).hasSize(3);
        assertThat(page.items().stream().map(SavedResourceView::id).toList())
                .containsExactly(act3.id(), act2.id(), act1.id());
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Interleaved trash across multiple full batches filters out all trashed items")
    void interleavedTrashAcrossMultipleBatches() {
        List<Long> activeIds = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            SavedResourceView active = saveResource("inter-act-" + i, SavedResourceKind.ARTICLE);
            activeIds.add(0, active.id()); // latest first

            SavedResourceView trashed = saveResource("inter-trash-" + i, SavedResourceKind.ARTICLE);
            trashVaultEntry(trashed.id());
        }

        // Total 120 rows (60 active, 60 trashed interleaved).
        // Query with limit 50 -> page 1 should have 50 active, non-null nextCursor
        SavedResourceInboxPageView page1 = savedResourceOperations.findInboxSavedResources(null, 50, null);
        assertThat(page1.items()).hasSize(50);
        assertThat(page1.nextCursor()).isNotNull();
        assertThat(page1.items().stream().map(SavedResourceView::id).toList())
                .isEqualTo(activeIds.subList(0, 50));

        // Page 2 -> 10 active items, null cursor
        SavedResourceInboxPageView page2 = savedResourceOperations.findInboxSavedResources(null, 50, page1.nextCursor());
        assertThat(page2.items()).hasSize(10);
        assertThat(page2.nextCursor()).isNull();
        assertThat(page2.items().stream().map(SavedResourceView::id).toList())
                .isEqualTo(activeIds.subList(50, 60));
    }

    @Test
    @DisplayName("All-trash dataset results in genuine exhaustion with empty page and null cursor")
    void allTrashExhaustionReturnsEmpty() {
        for (int i = 1; i <= 120; i++) {
            SavedResourceView v = saveResource("all-trash-" + i, SavedResourceKind.ARTICLE);
            trashVaultEntry(v.id());
        }

        SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 50, null);
        assertThat(page.items()).isEmpty();
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Exactly limit eligible rows followed only by trash returns items and null cursor")
    void exactlyLimitRowsFollowedByTrashEmitsNullCursor() {
        List<Long> activeIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            SavedResourceView v = saveResource("limit-act-" + i, SavedResourceKind.ARTICLE);
            activeIds.add(0, v.id());
        }
        for (int i = 1; i <= 40; i++) {
            SavedResourceView v = saveResource("limit-trash-" + i, SavedResourceKind.ARTICLE);
            trashVaultEntry(v.id());
            // Make trash older than active rows
            jdbcTemplate.update("UPDATE saved_resources SET saved_at = ? WHERE id = ?",
                    Timestamp.from(Instant.now().minus(5, ChronoUnit.DAYS).minus(i, ChronoUnit.MINUTES)), v.id());
        }

        SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 5, null);
        assertThat(page.items()).hasSize(5);
        assertThat(page.items().stream().map(SavedResourceView::id).toList()).isEqualTo(activeIds);
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Restoring a trashed item restores it to its original saved_at and id position")
    void restoringTrashedItemReappearsInInbox() {
        SavedResourceView r1 = saveResource("restore-1", SavedResourceKind.ARTICLE);
        SavedResourceView r2 = saveResource("restore-2", SavedResourceKind.ARTICLE);
        SavedResourceView r3 = saveResource("restore-3", SavedResourceKind.ARTICLE);

        // Trash r2
        trashVaultEntry(r2.id());

        SavedResourceInboxPageView beforeRestore = savedResourceOperations.findInboxSavedResources(null, 10, null);
        assertThat(beforeRestore.items().stream().map(SavedResourceView::id).toList())
                .containsExactly(r3.id(), r1.id());

        // Restore r2
        restoreVaultEntry(r2.id());

        SavedResourceInboxPageView afterRestore = savedResourceOperations.findInboxSavedResources(null, 10, null);
        assertThat(afterRestore.items().stream().map(SavedResourceView::id).toList())
                .containsExactly(r3.id(), r2.id(), r1.id());
    }

    @Test
    @DisplayName("Cursor continuation works even if the anchor row is subsequently trashed or deleted")
    void cursorContinuationWorksWhenAnchorTrashed() {
        SavedResourceView r1 = saveResource("anchor-1", SavedResourceKind.ARTICLE);
        SavedResourceView r2 = saveResource("anchor-2", SavedResourceKind.ARTICLE);
        SavedResourceView r3 = saveResource("anchor-3", SavedResourceKind.ARTICLE);
        SavedResourceView r4 = saveResource("anchor-4", SavedResourceKind.ARTICLE);

        // Page 1 with limit 2: returns r4 and r3
        SavedResourceInboxPageView page1 = savedResourceOperations.findInboxSavedResources(null, 2, null);
        assertThat(page1.items()).hasSize(2);
        assertThat(page1.items().get(1).id()).isEqualTo(r3.id());
        String cursor = page1.nextCursor();
        assertThat(cursor).isNotNull();

        // Now trash the anchor row r3!
        trashVaultEntry(r3.id());

        // Page 2 should still continue from r3's seek position and return r2 and r1
        SavedResourceInboxPageView page2 = savedResourceOperations.findInboxSavedResources(null, 2, cursor);
        assertThat(page2.items()).hasSize(2);
        assertThat(page2.items().stream().map(SavedResourceView::id).toList())
                .containsExactly(r2.id(), r1.id());
    }

    @Test
    @DisplayName("Committed trash interleaving: candidate trashed immediately before qualification is excluded from page")
    void committedTrashBeforeQualificationIsExcluded() throws Exception {
        SavedResourceView r1 = saveResource("interleave-1", SavedResourceKind.ARTICLE);
        SavedResourceView r2 = saveResource("interleave-2", SavedResourceKind.ARTICLE);
        SavedResourceView r3 = saveResource("interleave-3", SavedResourceKind.ARTICLE);

        // We wrap VaultSearchOperations with a test-local coordinator
        CountDownLatch beforeQualify = new CountDownLatch(1);
        CountDownLatch trashCommitted = new CountDownLatch(1);
        AtomicBoolean intercepted = new AtomicBoolean(false);

        VaultSearchOperations coordinatingQualifier = new VaultSearchOperations() {
            @Override
            public Set<Long> filterQualifyingActiveEntries(Set<Long> candidateIds, Set<Long> requiredTagIds) {
                if (candidateIds.contains(r2.id()) && intercepted.compareAndSet(false, true)) {
                    beforeQualify.countDown();
                    try {
                        boolean awaited = trashCommitted.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
                return vaultSearchOperations.filterQualifyingActiveEntries(candidateIds, requiredTagIds);
            }

            @Override
            public List<com.vhvkhangg.personalprivatevault.vault.search.VaultTagCandidateHit> searchByTag(com.vhvkhangg.personalprivatevault.vault.search.VaultTagSearchQuery query) {
                return vaultSearchOperations.searchByTag(query);
            }
        };

        SavedResourceService customService = new SavedResourceService(
                savedResourceRepository,
                feedItemRepository,
                feedSourceRepository,
                vaultEntryOperations,
                coordinatingQualifier
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionTemplate readTxTemplate = new TransactionTemplate(transactionManager);
        readTxTemplate.setReadOnly(true);

        try {
            Future<SavedResourceInboxPageView> queryFuture = executor.submit(() ->
                    readTxTemplate.execute(status -> customService.findInboxSavedResources(null, 10, null))
            );

            // Wait until reader is just about to qualify
            assertThat(beforeQualify.await(5, TimeUnit.SECONDS)).isTrue();

            // Commit trash of r2 in separate transaction
            executor.submit(() -> txTemplate.execute(status -> {
                trashVaultEntry(r2.id());
                return null;
            })).get(5, TimeUnit.SECONDS);

            trashCommitted.countDown();

            SavedResourceInboxPageView page = queryFuture.get(5, TimeUnit.SECONDS);

            // r2 was trashed before SQL qualifier ran, so it must be excluded!
            assertThat(page.items().stream().map(SavedResourceView::id).toList())
                    .containsExactly(r3.id(), r1.id());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Missing qualification result excludes resource from Inbox")
    void missingQualificationResultExcluded() {
        SavedResourceView r1 = saveResource("active-qual-1", SavedResourceKind.ARTICLE);
        SavedResourceView r2 = saveResource("active-qual-2", SavedResourceKind.ARTICLE);

        VaultSearchOperations omittingQualifier = new VaultSearchOperations() {
            @Override
            public Set<Long> filterQualifyingActiveEntries(Set<Long> candidateIds, Set<Long> requiredTagIds) {
                // Deliberately omit r2 from qualification result
                return Set.of(r1.id());
            }

            @Override
            public List<com.vhvkhangg.personalprivatevault.vault.search.VaultTagCandidateHit> searchByTag(com.vhvkhangg.personalprivatevault.vault.search.VaultTagSearchQuery query) {
                return List.of();
            }
        };

        SavedResourceService service = new SavedResourceService(
                savedResourceRepository, feedItemRepository, feedSourceRepository,
                vaultEntryOperations, omittingQualifier
        );

        SavedResourceInboxPageView page = service.findInboxSavedResources(null, 10, null);
        assertThat(page.items()).extracting(SavedResourceView::id).containsExactly(r1.id());
    }

    @Test
    @DisplayName("All five kinds filter correctly as database queries")
    void allFiveKindsFilterCorrectlyAtDatabase() {
        SavedResourceView art = saveResource("kind-art", SavedResourceKind.ARTICLE);
        SavedResourceView repo = saveResource("kind-repo", SavedResourceKind.REPOSITORY);
        SavedResourceView soc = saveResource("kind-soc", SavedResourceKind.SOCIAL_POST);
        SavedResourceView web = saveResource("kind-web", SavedResourceKind.WEB_PAGE);
        SavedResourceView oth = saveResource("kind-oth", SavedResourceKind.OTHER);

        // Verify each kind filter returns only its respective kind
        SavedResourceInboxPageView artPage = savedResourceOperations.findInboxSavedResources(SavedResourceKind.ARTICLE, 10, null);
        assertThat(artPage.items()).extracting(SavedResourceView::id).containsExactly(art.id());

        SavedResourceInboxPageView repoPage = savedResourceOperations.findInboxSavedResources(SavedResourceKind.REPOSITORY, 10, null);
        assertThat(repoPage.items()).extracting(SavedResourceView::id).containsExactly(repo.id());

        SavedResourceInboxPageView socPage = savedResourceOperations.findInboxSavedResources(SavedResourceKind.SOCIAL_POST, 10, null);
        assertThat(socPage.items()).extracting(SavedResourceView::id).containsExactly(soc.id());

        SavedResourceInboxPageView webPage = savedResourceOperations.findInboxSavedResources(SavedResourceKind.WEB_PAGE, 10, null);
        assertThat(webPage.items()).extracting(SavedResourceView::id).containsExactly(web.id());

        SavedResourceInboxPageView othPage = savedResourceOperations.findInboxSavedResources(SavedResourceKind.OTHER, 10, null);
        assertThat(othPage.items()).extracting(SavedResourceView::id).containsExactly(oth.id());

        // Unfiltered returns all 5
        SavedResourceInboxPageView allPage = savedResourceOperations.findInboxSavedResources(null, 10, null);
        assertThat(allPage.items()).hasSize(5);
    }

    @Test
    @DisplayName("Changed limit across continuation pages and deleted anchor row continues seamlessly")
    void changedPageSizeAndRemovedAnchorRow() {
        List<SavedResourceView> created = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            created.add(saveResource("changed-limit-" + i, SavedResourceKind.ARTICLE));
        }

        // Page 1 with limit 2
        SavedResourceInboxPageView page1 = savedResourceOperations.findInboxSavedResources(null, 2, null);
        assertThat(page1.items()).hasSize(2);
        assertThat(page1.nextCursor()).isNotNull();

        String cursor1 = page1.nextCursor();
        Long anchorId = page1.items().get(1).id();

        // Delete the anchor row entirely from the database
        jdbcTemplate.update("DELETE FROM saved_resources WHERE id = ?", anchorId);
        jdbcTemplate.update("DELETE FROM vault_entries WHERE id = ?", anchorId);

        // Page 2 with DIFFERENT limit = 3 using cursor1
        SavedResourceInboxPageView page2 = savedResourceOperations.findInboxSavedResources(null, 3, cursor1);
        assertThat(page2.items()).hasSize(3);
        assertThat(page2.items()).extracting(SavedResourceView::id).doesNotContain(anchorId);
    }

    @Test
    @DisplayName("Cursor timestamp must use database projection tuple even with managed creation entity in same transaction")
    void generatedCursorMustUseDatabaseTupleEvenWithManagedCreationEntity() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.execute(status -> {
            SavedResourceView older = saveResource("precision-older", SavedResourceKind.ARTICLE);
            SavedResourceView anchor = saveResource("precision-anchor", SavedResourceKind.ARTICLE);

            SavedResource olderEntity = entityManager.find(SavedResource.class, older.id());
            SavedResource anchorEntity = entityManager.find(SavedResource.class, anchor.id());

            ReflectionTestUtils.setField(olderEntity, "savedAt", Instant.parse("2026-11-01T14:00:00.123456100Z"));
            ReflectionTestUtils.setField(anchorEntity, "savedAt", Instant.parse("2026-11-01T14:00:00.123456789Z"));
            entityManager.flush();

            SavedResourceInboxPageView page = savedResourceOperations.findInboxSavedResources(null, 1, null);
            assertThat(page.nextCursor()).isNotNull();
            SavedResourceView returned = page.items().getFirst();
            Instant persisted = jdbcTemplate.queryForObject(
                    "SELECT saved_at FROM saved_resources WHERE id = ?",
                    (rs, row) -> rs.getTimestamp(1).toInstant(), returned.id());
            String payload = new String(Base64.getUrlDecoder().decode(page.nextCursor()), StandardCharsets.UTF_8);
            Instant encoded = Instant.parse(payload.split("\\|", -1)[2]);
            assertThat(encoded).as("issued handoff requires database-read cursor timestamp").isEqualTo(persisted);
            return null;
        });
    }

    @Test
    @DisplayName("Non-positive or excessive limit in application read throws IllegalArgumentException")
    void invalidLimitThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> savedResourceOperations.findInboxSavedResources(null, 0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 100");

        assertThatThrownBy(() -> savedResourceOperations.findInboxSavedResources(null, -5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 100");

        assertThatThrownBy(() -> savedResourceOperations.findInboxSavedResources(null, 101, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 100");
    }
}
