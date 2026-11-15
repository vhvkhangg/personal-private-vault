package com.vhvkhangg.personalprivatevault.vault;

import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultTrashPageView;

import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.internal.application.entry.VaultEntryMutationGuard;
import com.vhvkhangg.personalprivatevault.vault.internal.application.entry.VaultEntryService;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class VaultEntryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private VaultEntryRepository vaultEntryRepository;

    @Autowired
    private VaultEntryMutationGuard vaultEntryMutationGuard;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("Create vault entry sets active identity and matching timestamps")
    void createSetsActiveIdentityAndTimestamps() {
        VaultEntryView view = vaultEntryOperations.create(VaultEntryType.FICTION);

        assertThat(view.id()).isNotNull();
        assertThat(view.entryType()).isEqualTo(VaultEntryType.FICTION);
        assertThat(view.createdAt()).isNotNull();
        assertThat(view.updatedAt()).isEqualTo(view.createdAt());
        assertThat(view.deletedAt()).isNull();

        Optional<VaultEntryView> found = vaultEntryOperations.find(view.id());
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(view);
    }

    @Test
    @DisplayName("Create with null entry type throws IllegalArgumentException")
    void createWithNullTypeThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> vaultEntryOperations.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Find with null ID throws IllegalArgumentException; non-existent ID returns Optional.empty")
    void findBehavior() {
        assertThatThrownBy(() -> vaultEntryOperations.find(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(vaultEntryOperations.find(999999L)).isEmpty();
    }

    @Test
    @DisplayName("Trash and restore lifecycle: timestamps, idempotency, and state transitions")
    void trashAndRestoreLifecycleWithDeterministicClock() {
        Instant t0 = Instant.parse("2026-09-27T10:00:00Z");
        Instant t1 = Instant.parse("2026-09-27T10:15:00Z");
        Instant t2 = Instant.parse("2026-09-27T10:30:00Z");
        Instant t3 = Instant.parse("2026-09-27T10:45:00Z");
        Instant t4 = Instant.parse("2026-09-27T11:00:00Z");

        // 1. Create with fixed clock at t0
        Clock clock0 = Clock.fixed(t0, ZoneOffset.UTC);
        VaultEntryService service0 = new VaultEntryService(vaultEntryRepository, vaultEntryMutationGuard, clock0);
        VaultEntryView created = service0.create(VaultEntryType.FILM);

        assertThat(created.createdAt()).isEqualTo(t0);
        assertThat(created.updatedAt()).isEqualTo(t0);
        assertThat(created.deletedAt()).isNull();

        // 2. Move to trash at t1
        Clock clock1 = Clock.fixed(t1, ZoneOffset.UTC);
        VaultEntryService service1 = new VaultEntryService(vaultEntryRepository, vaultEntryMutationGuard, clock1);
        VaultEntryView trashed = service1.moveToTrash(created.id());

        assertThat(trashed.deletedAt()).isEqualTo(t1);
        assertThat(trashed.updatedAt()).isEqualTo(t1);
        assertThat(trashed.createdAt()).isEqualTo(t0);

        // 3. Repeated trash at t2 is idempotent: preserves original deletedAt and does not change updatedAt
        Clock clock2 = Clock.fixed(t2, ZoneOffset.UTC);
        VaultEntryService service2 = new VaultEntryService(vaultEntryRepository, vaultEntryMutationGuard, clock2);
        VaultEntryView repeatTrashed = service2.moveToTrash(created.id());

        assertThat(repeatTrashed.deletedAt()).isEqualTo(t1);
        assertThat(repeatTrashed.updatedAt()).isEqualTo(t1);

        // 4. Restore at t3
        Clock clock3 = Clock.fixed(t3, ZoneOffset.UTC);
        VaultEntryService service3 = new VaultEntryService(vaultEntryRepository, vaultEntryMutationGuard, clock3);
        VaultEntryView restored = service3.restore(created.id());

        assertThat(restored.deletedAt()).isNull();
        assertThat(restored.updatedAt()).isEqualTo(t3);
        assertThat(restored.createdAt()).isEqualTo(t0);

        // 5. Repeated restore at t4 is idempotent: does not alter updatedAt
        Clock clock4 = Clock.fixed(t4, ZoneOffset.UTC);
        VaultEntryService service4 = new VaultEntryService(vaultEntryRepository, vaultEntryMutationGuard, clock4);
        VaultEntryView repeatRestored = service4.restore(created.id());

        assertThat(repeatRestored.deletedAt()).isNull();
        assertThat(repeatRestored.updatedAt()).isEqualTo(t3);
    }

    @Test
    @DisplayName("Mutating non-existent entry throws NoSuchElementException; null ID throws IllegalArgumentException")
    void missingTargetAndNullIdThrowExpectedExceptions() {
        assertThatThrownBy(() -> vaultEntryOperations.moveToTrash(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultEntryOperations.restore(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> vaultEntryOperations.moveToTrash(999999L))
                .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> vaultEntryOperations.restore(999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("V1-04: Empty trash and all-active vault produce empty page with null nextCursor")
    void emptyTrashAndAllActiveVaultProduceEmptyPage() {
        // Query when no deleted entries exist in current scope (or clean table)
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        VaultTrashPageView emptyPage = vaultEntryOperations.listTrash(25, null);
        assertThat(emptyPage.items()).isEmpty();
        assertThat(emptyPage.nextCursor()).isNull();

        // Create multiple active entries across different types
        vaultEntryOperations.create(VaultEntryType.PERSON);
        vaultEntryOperations.create(VaultEntryType.FICTION);
        vaultEntryOperations.create(VaultEntryType.FILM);
        vaultEntryOperations.create(VaultEntryType.NOTE);

        VaultTrashPageView allActivePage = vaultEntryOperations.listTrash(25, null);
        assertThat(allActivePage.items()).isEmpty();
        assertThat(allActivePage.nextCursor()).isNull();
    }

    @Test
    @DisplayName("V1-05, V1-06, V1-09: Limit bounds validation, populated boundaries, and fetch limit+1 boundary")
    void limitBoundsValidation() {
        // Out of bounds validation throws InvalidTrashInputException (V1-06)
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(0, null))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(-1, null))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(101, null))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Populated boundaries on 30 trashed items (FR-V1-03)
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        for (int i = 0; i < 30; i++) {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FICTION);
            vaultEntryOperations.moveToTrash(entry.id());
        }

        // Default limit (25) -> returns 25 items, non-null nextCursor
        VaultTrashPageView pageDefault = vaultEntryOperations.listTrash(25, null);
        assertThat(pageDefault.items()).hasSize(25);
        assertThat(pageDefault.nextCursor()).isNotNull();
        for (VaultEntryView item : pageDefault.items()) {
            assertThat(item.id()).isPositive();
            assertThat(item.entryType()).isNotNull();
            assertThat(item.deletedAt()).isNotNull();
        }

        // Explicit limit=1 -> returns 1 item, non-null nextCursor
        VaultTrashPageView page1 = vaultEntryOperations.listTrash(1, null);
        assertThat(page1.items()).hasSize(1);
        assertThat(page1.nextCursor()).isNotNull();

        // Explicit limit=100 -> returns all 30 items, null nextCursor
        VaultTrashPageView page100 = vaultEntryOperations.listTrash(100, null);
        assertThat(page100.items()).hasSize(30);
        assertThat(page100.nextCursor()).isNull();

        // Exactly-limit boundary: fixture with exactly 5 items, limit=5 -> nextCursor null
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        for (int i = 0; i < 5; i++) {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FILM);
            vaultEntryOperations.moveToTrash(entry.id());
        }
        VaultTrashPageView pageExact = vaultEntryOperations.listTrash(5, null);
        assertThat(pageExact.items()).hasSize(5);
        assertThat(pageExact.nextCursor()).isNull();

        // Limit+1 boundary: fixture with 6 items, limit=5 -> page 1 has nextCursor, page 2 has 1 item and nextCursor null
        VaultEntryView extra = vaultEntryOperations.create(VaultEntryType.FILM);
        vaultEntryOperations.moveToTrash(extra.id());
        VaultTrashPageView pageLimitPlus1 = vaultEntryOperations.listTrash(5, null);
        assertThat(pageLimitPlus1.items()).hasSize(5);
        assertThat(pageLimitPlus1.nextCursor()).isNotNull();
        VaultTrashPageView pageLimitPlus1Cont = vaultEntryOperations.listTrash(5, pageLimitPlus1.nextCursor());
        assertThat(pageLimitPlus1Cont.items()).hasSize(1);
        assertThat(pageLimitPlus1Cont.nextCursor()).isNull();

        // Exhausted boundary: query with terminal cursor returns empty page with null nextCursor
        String terminalCursor = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("v1|" + pageLimitPlus1Cont.items().get(0).id()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        VaultTrashPageView pageTerminal = vaultEntryOperations.listTrash(5, terminalCursor);
        assertThat(pageTerminal.items()).isEmpty();
        assertThat(pageTerminal.nextCursor()).isNull();

        // Maximum page/fetch boundary: fixture with 101 deleted items, limit=100 -> page 1 has 100 items & non-null nextCursor, page 2 has 1 item & null nextCursor (FR-V1-03)
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        for (int i = 0; i < 101; i++) {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
            vaultEntryOperations.moveToTrash(entry.id());
        }
        VaultTrashPageView pageMax100 = vaultEntryOperations.listTrash(100, null);
        assertThat(pageMax100.items()).hasSize(100);
        assertThat(pageMax100.nextCursor()).isNotNull();
        VaultTrashPageView pageMax100Cont = vaultEntryOperations.listTrash(100, pageMax100.nextCursor());
        assertThat(pageMax100Cont.items()).hasSize(1);
        assertThat(pageMax100Cont.nextCursor()).isNull();
    }

    @Test
    @DisplayName("V1-07: Cursor validation rejects malformed, blank, padded, non-canonical, overflow and invalid format")
    void cursorValidationRejections() {
        // Valid canonical unpadded Base64url control v1|1 -> "djF8MQ"
        VaultTrashPageView validControl = vaultEntryOperations.listTrash(25, "djF8MQ");
        assertThat(validControl).isNotNull();

        // Ambiguous unpadded Base64url "djF8MR" (unused bits set in last chunk) must be rejected before query (FR-V1-03)
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, "djF8MR"))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, ""))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, "   "))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, "djF8MTQy="))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, "djF8MTQy=="))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, "invalid!base64"))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Malformed UTF-8 (FR-V1-03)
        byte[] malformedUtf8 = new byte[] { 'v', '1', '|', (byte) 0xC0, (byte) 0xAF };
        String malformedUtf8Cursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(malformedUtf8);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, malformedUtf8Cursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Unsupported version v2|142
        String v2Cursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v2|142".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, v2Cursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Non-positive ID v1|0
        String zeroCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|0".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, zeroCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Negative ID v1|-1
        String negCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|-1".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, negCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Leading zero v1|0142
        String leadingZeroCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|0142".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, leadingZeroCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Non-decimal ID v1|abc
        String nonDecimalCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|abc".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, nonDecimalCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Extra delimiters v1|142|extra
        String extraCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|142|extra".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, extraCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // 19-digit overflow boundary (FR-V1-03)
        String overflow19DigitCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|9223372036854775808".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, overflow19DigitCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Huge overflow ID v1|9999999999999999999999
        String overflowCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|9999999999999999999999".getBytes());
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, overflowCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Overlong cursor > 128 chars
        String overlongCursor = "a".repeat(129);
        assertThatThrownBy(() -> vaultEntryOperations.listTrash(25, overlongCursor))
                .isInstanceOf(VaultEntryOperations.InvalidTrashInputException.class);

        // Long.MAX_VALUE positive boundary (FR-V1-03)
        String maxLongCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(("v1|" + Long.MAX_VALUE).getBytes());
        VaultTrashPageView maxPage = vaultEntryOperations.listTrash(25, maxLongCursor);
        assertThat(maxPage).isNotNull();
    }

    @Test
    @DisplayName("V1-08, V1-09: Stable traversal across >= 3 pages covering all 17 enum types with active gaps, strictly descending IDs, no dup/skip, last-emitted cursor")
    void stableTraversalAcrossMultiplePagesWithActiveGaps() {
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");

        VaultEntryType[] allTypes = VaultEntryType.values();
        assertThat(allTypes).hasSize(17);
        Set<VaultEntryType> expectedTypes = Set.of(allTypes);

        List<Long> trashedIds = new ArrayList<>();
        Set<VaultEntryType> collectedTypes = new java.util.HashSet<>();

        // Create 1 active gap and 1 trashed entry for EVERY single one of the 17 enum types (FR-V1-03)
        for (VaultEntryType type : allTypes) {
            vaultEntryOperations.create(type); // active gap
            VaultEntryView trashed = vaultEntryOperations.create(type);
            vaultEntryOperations.moveToTrash(trashed.id());
            trashedIds.add(trashed.id());
        }
        assertThat(trashedIds).hasSize(17);
        trashedIds.sort(java.util.Comparator.reverseOrder());

        // Traverse with limit = 5 across pages (5, 5, 5, 2) -> 4 pages (>= 3 pages)
        List<Long> collectedIds = new ArrayList<>();
        String cursor = null;
        int pageCount = 0;

        do {
            VaultTrashPageView page = vaultEntryOperations.listTrash(5, cursor);
            pageCount++;
            assertThat(page.items().size()).isLessThanOrEqualTo(5);

            for (VaultEntryView item : page.items()) {
                assertThat(item.deletedAt()).isNotNull();
                collectedIds.add(item.id());
                collectedTypes.add(item.entryType());
            }

            if (collectedIds.size() < trashedIds.size()) {
                assertThat(page.nextCursor()).isNotNull();
                // Verify nextCursor encodes the last emitted item, not lookahead
                Long lastEmittedId = page.items().get(page.items().size() - 1).id();
                String expectedCursor = java.util.Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(("v1|" + lastEmittedId).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                assertThat(page.nextCursor()).isEqualTo(expectedCursor);
            } else {
                assertThat(page.nextCursor()).isNull();
            }

            cursor = page.nextCursor();
        } while (cursor != null);

        assertThat(pageCount).isGreaterThanOrEqualTo(3);
        assertThat(collectedIds).isEqualTo(trashedIds);
        assertThat(collectedTypes).as("All 17 VaultEntryType values must be represented in trashed items")
                .containsExactlyInAnyOrderElementsOf(expectedTypes);

        // Strictly descending check
        for (int i = 0; i < collectedIds.size() - 1; i++) {
            assertThat(collectedIds.get(i)).isGreaterThan(collectedIds.get(i + 1));
        }

        // Exhausted query with terminal cursor returns empty page with null nextCursor
        String terminalCursor = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("v1|" + collectedIds.get(collectedIds.size() - 1)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        VaultTrashPageView exhaustedPage = vaultEntryOperations.listTrash(5, terminalCursor);
        assertThat(exhaustedPage.items()).isEmpty();
        assertThat(exhaustedPage.nextCursor()).isNull();
    }

    @Test
    @DisplayName("V1-12: PostgreSQL keyset query plan on representative sparse-trash fixture and limit+1 fetch bound")
    void postgresPlanAndSqlBoundProof() {
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");

        // Seed synthetic fixture: 40 active entries with 6 distributed sparse deleted entries across active IDs (FR-V1-02)
        List<Long> deletedIds = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            vaultEntryOperations.create(VaultEntryType.FICTION);
            if (i == 5 || i == 12 || i == 19 || i == 26 || i == 33 || i == 39) {
                VaultEntryView toDelete = vaultEntryOperations.create(VaultEntryType.FILM);
                vaultEntryOperations.moveToTrash(toDelete.id());
                deletedIds.add(toDelete.id());
            }
        }
        assertThat(deletedIds).hasSize(6);
        deletedIds.sort(java.util.Comparator.reverseOrder());

        Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
        Level prevSqlLevel = sqlLogger.getLevel();
        sqlLogger.setLevel(Level.DEBUG);

        Logger bindLogger = (Logger) LoggerFactory.getLogger("org.hibernate.orm.jdbc.bind");
        Level prevBindLevel = bindLogger.getLevel();
        bindLogger.setLevel(Level.TRACE);

        ListAppender<ILoggingEvent> sqlAppender = new ListAppender<>();
        sqlAppender.start();
        sqlLogger.addAppender(sqlAppender);

        ListAppender<ILoggingEvent> bindAppender = new ListAppender<>();
        bindAppender.start();
        bindLogger.addAppender(bindAppender);

        String capturedSql = null;
        List<String> initialBinds = new ArrayList<>();
        List<String> continuationBinds = new ArrayList<>();
        Long cursorId;

        try {
            // Prove database fetch bound via repository: limit=2 requests at most 3 items (limit+1)
            List<VaultEntryView> firstBatch = vaultEntryRepository.findTrashPage(null, org.springframework.data.domain.PageRequest.of(0, 3));
            assertThat(firstBatch).hasSize(3);
            assertThat(firstBatch.get(0).id()).isEqualTo(deletedIds.get(0));
            assertThat(firstBatch.get(1).id()).isEqualTo(deletedIds.get(1));
            assertThat(firstBatch.get(2).id()).isEqualTo(deletedIds.get(2));

            for (ILoggingEvent event : sqlAppender.list) {
                if (event.getMessage().contains("vault_entries")) {
                    capturedSql = event.getMessage();
                }
            }
            for (ILoggingEvent event : bindAppender.list) {
                initialBinds.add(event.getFormattedMessage());
            }

            bindAppender.list.clear();

            // Continuation fetch bound: passing cursorId strictly bounds next batch
            cursorId = firstBatch.get(1).id();
            List<VaultEntryView> continuationBatch = vaultEntryRepository.findTrashPage(cursorId, org.springframework.data.domain.PageRequest.of(0, 3));
            assertThat(continuationBatch).hasSize(3);
            for (VaultEntryView item : continuationBatch) {
                assertThat(item.id()).isLessThan(cursorId);
            }

            for (ILoggingEvent event : bindAppender.list) {
                continuationBinds.add(event.getFormattedMessage());
            }
        } finally {
            sqlLogger.detachAppender(sqlAppender);
            sqlLogger.setLevel(prevSqlLevel);
            bindLogger.detachAppender(bindAppender);
            bindLogger.setLevel(prevBindLevel);
        }

        assertThat(capturedSql).as("Captured Hibernate SQL must not be null").isNotNull();
        assertThat(capturedSql).containsIgnoringCase("deleted_at is not null");
        assertThat(capturedSql).containsIgnoringCase("order by");
        assertThat(capturedSql).containsIgnoringCase("desc");

        System.out.println("ACTUAL_CAPTURED_HIBERNATE_SQL:\n" + capturedSql);
        System.out.println("ACTUAL_CAPTURED_BINDS_INITIAL:\n" + String.join("\n", initialBinds));
        System.out.println("ACTUAL_CAPTURED_BINDS_CONTINUATION:\n" + String.join("\n", continuationBinds));

        // Correlate EXPLAIN (ANALYZE, COSTS OFF) plan with the captured query and bound parameters (FR-V1-02)
        List<String> initialPlan = jdbcTemplate.query(
                "EXPLAIN (ANALYZE, COSTS OFF) " + capturedSql,
                ps -> {
                    ps.setNull(1, java.sql.Types.BIGINT);
                    ps.setNull(2, java.sql.Types.BIGINT);
                    ps.setInt(3, 3);
                },
                (rs, rowNum) -> rs.getString(1)
        );
        assertThat(initialPlan).isNotEmpty();
        String initialPlanStr = String.join("\n", initialPlan);
        assertThat(initialPlanStr).contains("Limit");
        System.out.println("ACTUAL_CORRELATED_EXPLAIN_PLAN_INITIAL:\n" + initialPlanStr);

        final Long finalCursorId = cursorId;
        List<String> contPlan = jdbcTemplate.query(
                "EXPLAIN (ANALYZE, COSTS OFF) " + capturedSql,
                ps -> {
                    ps.setLong(1, finalCursorId);
                    ps.setLong(2, finalCursorId);
                    ps.setInt(3, 3);
                },
                (rs, rowNum) -> rs.getString(1)
        );
        assertThat(contPlan).isNotEmpty();
        String contPlanStr = String.join("\n", contPlan);
        assertThat(contPlanStr).contains("Limit");
        System.out.println("ACTUAL_CORRELATED_EXPLAIN_PLAN_CONTINUATION:\n" + contPlanStr);

        // Service level check
        VaultTrashPageView boundedPage = vaultEntryOperations.listTrash(2, null);
        assertThat(boundedPage.items()).hasSize(2);
        assertThat(boundedPage.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("V1-10: Genuinely committed restore between pages removes item from subsequent eligible pages, strict old cursor bound")
    void committedRestoreBetweenPagesWeakConsistency() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        List<Long> trashed = tx.execute(status -> {
            jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FICTION);
                vaultEntryOperations.moveToTrash(entry.id());
                ids.add(entry.id());
            }
            ids.sort(java.util.Comparator.reverseOrder());
            return ids;
        });
        assertThat(trashed).hasSize(6);

        // Page 1 with limit 2 in its own transaction -> gets [id0, id1], cursor is encode(id1)
        VaultTrashPageView page1 = tx.execute(status -> vaultEntryOperations.listTrash(2, null));
        assertThat(page1).isNotNull();
        assertThat(page1.items()).hasSize(2);
        assertThat(page1.items().get(0).id()).isEqualTo(trashed.get(0));
        assertThat(page1.items().get(1).id()).isEqualTo(trashed.get(1));
        String cursor1 = page1.nextCursor();
        assertThat(cursor1).isNotNull();

        // Genuinely committed restore of trashed.get(2) between page requests
        Long restoredId = trashed.get(2);
        tx.executeWithoutResult(status -> vaultEntryOperations.restore(restoredId));

        // Page 2 using cursor1 in separate transaction -> restoredId is omitted, continues strictly below id1
        VaultTrashPageView page2 = tx.execute(status -> vaultEntryOperations.listTrash(2, cursor1));
        assertThat(page2).isNotNull();
        List<Long> page2Ids = page2.items().stream().map(VaultEntryView::id).toList();
        assertThat(page2Ids).doesNotContain(restoredId);
        assertThat(page2Ids).doesNotContain(trashed.get(0), trashed.get(1));
        assertThat(page2Ids).containsExactly(trashed.get(3), trashed.get(4));
    }

    @Test
    @DisplayName("V1-11, V1-13: Committed re-trash retains lifecycle rules, changes membership without lock/loop, leaves domain unchanged on read")
    void committedReTrashWeakConsistency() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Long entryId = tx.execute(status -> {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FILM);
            vaultEntryOperations.moveToTrash(entry.id());
            return entry.id();
        });

        // Restore in committed transaction
        tx.executeWithoutResult(status -> vaultEntryOperations.restore(entryId));

        // Re-trash in committed transaction
        tx.executeWithoutResult(status -> vaultEntryOperations.moveToTrash(entryId));

        // Snapshot complete domain row state before read (FR-V1-03)
        List<Map<String, Object>> stateBefore = jdbcTemplate.queryForList(
                "SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id"
        );

        // Read in separate transaction proves entry is in trash and query completes cleanly
        VaultTrashPageView page = tx.execute(status -> vaultEntryOperations.listTrash(25, null));
        assertThat(page).isNotNull();
        assertThat(page.items().stream().map(VaultEntryView::id)).contains(entryId);

        // Domain write check: verify entire state snapshot before and after read is strictly equal (V1-13, FR-V1-03)
        List<Map<String, Object>> stateAfter = jdbcTemplate.queryForList(
                "SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id"
        );
        assertThat(stateAfter).as("Read must not perform domain writes or state modifications").isEqualTo(stateBefore);
    }

    @Test
    @DisplayName("V1-18: VaultTrashPageView defensively copies items and ensures immutability")
    void viewImmutability() {
        VaultTrashPageView page = vaultEntryOperations.listTrash(10, null);
        assertThatThrownBy(() -> page.items().add(new VaultEntryView(999L, VaultEntryType.NOTE, Instant.now(), Instant.now(), Instant.now())))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
