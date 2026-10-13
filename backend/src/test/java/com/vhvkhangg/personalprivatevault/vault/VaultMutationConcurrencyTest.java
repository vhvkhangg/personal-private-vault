package com.vhvkhangg.personalprivatevault.vault;

import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.metadata.VaultMetadataOperations;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SBA-001 regression tests:
 * Verifies authoritative lifecycle state refresh under PostgreSQL row lock,
 * deterministic contention between lifecycle and metadata operations in both orders,
 * parent-before-child lock order, and preservation of timestamps and child state.
 */
class VaultMutationConcurrencyTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private VaultMetadataOperations vaultMetadataOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate requiresNewTx() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx;
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
    @DisplayName("SBA-001: Preloaded active entry refuses metadata when competing tx committed trash")
    void cachedActiveCommittedTrashRefusesMetadata() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();
        TagView tag = vaultMetadataOperations.createTag("cached-active-test");

        assertThatThrownBy(() -> {
            requiresNewTx().executeWithoutResult(status -> {
                // Preload active entry in L1 cache
                assertThat(vaultEntryOperations.find(entryId)).isPresent();

                // Competing transaction moves entry to trash and commits
                requiresNewTx().executeWithoutResult(s2 -> vaultEntryOperations.moveToTrash(entryId));

                // In outer transaction, metadata operations must authoritatively refresh and deny
                vaultMetadataOperations.attachTag(entryId, tag.id());
            });
        }).isInstanceOf(IllegalStateException.class).hasMessageContaining("trash");

        // Fresh JDBC assertion: entry is trashed, tag count remains 0
        Boolean deleted = jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, entryId);
        Long tagCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ?", Long.class, entryId);
        assertThat(deleted).isTrue();
        assertThat(tagCount).isZero();
    }

    @Test
    @DisplayName("SBA-001: Preloaded active entry returns and persists active when competing tx committed trash then restore")
    void cachedActiveCommittedTrashRestoreReturnsActive() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        VaultEntryView restoredView = requiresNewTx().execute(status -> {
            // Preload active entry in L1 cache
            assertThat(vaultEntryOperations.find(entryId)).isPresent();

            // Competing transaction trashes then restores and commits
            requiresNewTx().executeWithoutResult(s2 -> {
                vaultEntryOperations.moveToTrash(entryId);
            });

            // Outer transaction restores: must lock, refresh, see trashed, transition to active
            return vaultEntryOperations.restore(entryId);
        });

        assertThat(restoredView).isNotNull();
        assertThat(restoredView.deletedAt()).isNull();

        // Fresh JDBC assertion: entry is active in PostgreSQL
        Boolean deleted = jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, entryId);
        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("SBA-001: Preloaded trashed entry trashes and admits metadata after competing restore")
    void cachedTrashedCommittedRestoreAdmitsOperations() {
        // Part 1: Stale-trashed in L1 -> committed restore -> trash transition succeeds
        {
            VaultEntryView entry1 = vaultEntryOperations.create(VaultEntryType.NOTE);
            Long id1 = entry1.id();
            vaultEntryOperations.moveToTrash(id1);

            requiresNewTx().executeWithoutResult(status -> {
                // Preload trashed entry in L1 cache
                assertThat(vaultEntryOperations.find(id1).get().deletedAt()).isNotNull();

                // Competing transaction restores entry and commits
                requiresNewTx().executeWithoutResult(s2 -> vaultEntryOperations.restore(id1));

                // Calling trash on refreshed entry must succeed (since entry is now active)
                VaultEntryView trashedView = vaultEntryOperations.moveToTrash(id1);
                assertThat(trashedView.deletedAt()).isNotNull();
            });

            Boolean deleted1 = jdbcTemplate.queryForObject(
                    "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, id1);
            assertThat(deleted1).isTrue();
        }

        // Part 2: Stale-trashed in L1 -> committed restore -> metadata admission
        {
            VaultEntryView entry2 = vaultEntryOperations.create(VaultEntryType.NOTE);
            Long id2 = entry2.id();
            vaultEntryOperations.moveToTrash(id2);
            TagView tag = vaultMetadataOperations.createTag("cached-trashed-test");

            requiresNewTx().executeWithoutResult(status -> {
                // Preload trashed entry in L1 cache
                assertThat(vaultEntryOperations.find(id2).get().deletedAt()).isNotNull();

                // Competing transaction restores entry and commits
                requiresNewTx().executeWithoutResult(s2 -> vaultEntryOperations.restore(id2));

                // Metadata mutation must authoritatively see restored state and succeed
                vaultMetadataOperations.attachTag(id2, tag.id());
            });

            Boolean deleted2 = jdbcTemplate.queryForObject(
                    "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, id2);
            Long tagCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ?", Long.class, id2);
            assertThat(deleted2).isFalse();
            assertThat(tagCount).isEqualTo(1L);
        }
    }

    @Test
    @DisplayName("SBA-001: Real trash-vs-restore contention in both orders, returning valid views")
    void realTrashVsRestoreContentionBothOrders() throws Exception {
        // Order 1: Trash first, Restore contends on row lock -> Final active
        {
            VaultEntryView entry1 = vaultEntryOperations.create(VaultEntryType.NOTE);
            Long id1 = entry1.id();

            CountDownLatch t1LockAcquired = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            AtomicReference<VaultEntryView> v1Ref = new AtomicReference<>();
            AtomicReference<VaultEntryView> v2Ref = new AtomicReference<>();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        v1Ref.set(vaultEntryOperations.moveToTrash(id1));
                        t1LockAcquired.countDown();
                        try {
                            if (!t1Proceed.await(5, TimeUnit.SECONDS)) {
                                throw new AssertionError("Holder timed out waiting for t1Proceed");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Holder thread was interrupted", e);
                        }
                    });
                });

                assertThat(t1LockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

                Future<?> f2 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        waiterPidRef.set(getCurrentBackendPid());
                        v2Ref.set(vaultEntryOperations.restore(id1));
                    });
                });

                // Observe f2 blocked specifically by holderPidRef
                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "vault_entries", Duration.ofSeconds(5));

                t1Proceed.countDown();
                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                // Assert views returned to callers reflect their operations
                assertThat(v1Ref.get()).isNotNull();
                assertThat(v1Ref.get().deletedAt()).isNotNull();
                assertThat(v2Ref.get()).isNotNull();
                assertThat(v2Ref.get().deletedAt()).isNull();
                assertThat(v2Ref.get().updatedAt()).isAfterOrEqualTo(v1Ref.get().updatedAt());

                // Final state in DB: Restore was last -> active
                Boolean deleted = jdbcTemplate.queryForObject(
                        "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, id1);
                assertThat(deleted).isFalse();
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }

        // Order 2: Restore first (on trashed entry), Trash contends on row lock -> Final trashed
        {
            VaultEntryView entry2 = vaultEntryOperations.create(VaultEntryType.NOTE);
            Long id2 = entry2.id();
            vaultEntryOperations.moveToTrash(id2);

            CountDownLatch t1LockAcquired = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            AtomicReference<VaultEntryView> v1Ref = new AtomicReference<>();
            AtomicReference<VaultEntryView> v2Ref = new AtomicReference<>();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        v1Ref.set(vaultEntryOperations.restore(id2));
                        t1LockAcquired.countDown();
                        try {
                            if (!t1Proceed.await(5, TimeUnit.SECONDS)) {
                                throw new AssertionError("Holder timed out waiting for t1Proceed");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Holder thread was interrupted", e);
                        }
                    });
                });

                assertThat(t1LockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

                Future<?> f2 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        waiterPidRef.set(getCurrentBackendPid());
                        v2Ref.set(vaultEntryOperations.moveToTrash(id2));
                    });
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "vault_entries", Duration.ofSeconds(5));

                t1Proceed.countDown();
                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                // Assert views returned to callers reflect their operations
                assertThat(v1Ref.get()).isNotNull();
                assertThat(v1Ref.get().deletedAt()).isNull();
                assertThat(v2Ref.get()).isNotNull();
                assertThat(v2Ref.get().deletedAt()).isNotNull();
                assertThat(v2Ref.get().updatedAt()).isAfterOrEqualTo(v1Ref.get().updatedAt());

                // Final state: Trash was last -> trashed
                Boolean deleted = jdbcTemplate.queryForObject(
                        "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, id2);
                assertThat(deleted).isTrue();
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }
    }

    @ParameterizedTest(name = "SBA-001 metadata operation: {0}")
    @ValueSource(strings = {"favorite", "unfavorite", "setRating", "removeRating", "attachTag", "detachTag"})
    @DisplayName("SBA-001: All six metadata operations parameterization against trash with real contention in both orders")
    void parameterizeAllSixMetadataOperationsAgainstTrash(String operation) throws Exception {
        // Case A: Trash first -> Metadata operation contends, blocked by holder lock, then denied with IllegalStateException
        {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
            Long entryId = entry.id();
            TagView tag = vaultMetadataOperations.createTag("param-tag-a-" + operation);

            // Pre-seed state for remove/unfavorite/detach tests
            if ("unfavorite".equals(operation)) {
                vaultMetadataOperations.favorite(entryId);
            } else if ("removeRating".equals(operation)) {
                vaultMetadataOperations.setRating(entryId, RatingGrade.B);
            } else if ("detachTag".equals(operation)) {
                vaultMetadataOperations.attachTag(entryId, tag.id());
            }

            CountDownLatch t1Locked = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicBoolean metadataDenied = new AtomicBoolean(false);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        vaultEntryOperations.moveToTrash(entryId);
                        t1Locked.countDown();
                        try {
                            if (!t1Proceed.await(5, TimeUnit.SECONDS)) {
                                throw new AssertionError("Holder timed out waiting for t1Proceed");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Holder thread was interrupted", e);
                        }
                    });
                });

                assertThat(t1Locked.await(5, TimeUnit.SECONDS)).isTrue();

                Future<?> f2 = executor.submit(() -> {
                    try {
                        requiresNewTx().executeWithoutResult(status -> {
                            waiterPidRef.set(getCurrentBackendPid());
                            executeMetadataOp(operation, entryId, tag.id());
                        });
                    } catch (IllegalStateException ex) {
                        if (ex.getMessage() != null && ex.getMessage().contains("trash")) {
                            metadataDenied.set(true);
                        }
                    }
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "vault_entries", Duration.ofSeconds(5));
                t1Proceed.countDown();

                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                assertThat(metadataDenied.get()).as("Metadata operation must be denied against committed trash").isTrue();

                // Assert child rows were NOT modified by denied operation
                assertChildRowsUnchanged(operation, entryId);
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }

        // Case B: Metadata first -> succeeds, Trash contends on row lock and then commits
        {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
            Long entryId = entry.id();
            TagView tag = vaultMetadataOperations.createTag("param-tag-b-" + operation);

            if ("unfavorite".equals(operation)) {
                vaultMetadataOperations.favorite(entryId);
            } else if ("removeRating".equals(operation)) {
                vaultMetadataOperations.setRating(entryId, RatingGrade.C);
            } else if ("detachTag".equals(operation)) {
                vaultMetadataOperations.attachTag(entryId, tag.id());
            }

            CountDownLatch t1Locked = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        executeMetadataOp(operation, entryId, tag.id());
                        t1Locked.countDown();
                        try {
                            if (!t1Proceed.await(5, TimeUnit.SECONDS)) {
                                throw new AssertionError("Holder timed out waiting for t1Proceed");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Holder thread was interrupted", e);
                        }
                    });
                });

                assertThat(t1Locked.await(5, TimeUnit.SECONDS)).isTrue();

                Future<?> f2 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        waiterPidRef.set(getCurrentBackendPid());
                        vaultEntryOperations.moveToTrash(entryId);
                    });
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "vault_entries", Duration.ofSeconds(5));
                t1Proceed.countDown();

                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                // Both metadata result AND trash status are persisted!
                Boolean deleted = jdbcTemplate.queryForObject(
                        "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, entryId);
                assertThat(deleted).isTrue();

                assertMetadataResultPersisted(operation, entryId);
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }
    }

    private void executeMetadataOp(String operation, Long entryId, Long tagId) {
        switch (operation) {
            case "favorite" -> vaultMetadataOperations.favorite(entryId);
            case "unfavorite" -> vaultMetadataOperations.unfavorite(entryId);
            case "setRating" -> vaultMetadataOperations.setRating(entryId, RatingGrade.S);
            case "removeRating" -> vaultMetadataOperations.removeRating(entryId);
            case "attachTag" -> vaultMetadataOperations.attachTag(entryId, tagId);
            case "detachTag" -> vaultMetadataOperations.detachTag(entryId, tagId);
        }
    }

    private void assertChildRowsUnchanged(String operation, Long entryId) {
        if ("unfavorite".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM favorites WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isEqualTo(1L);
        } else if ("removeRating".equals(operation)) {
            String grade = jdbcTemplate.queryForObject("SELECT grade FROM ratings WHERE vault_entry_id = ?", String.class, entryId);
            assertThat(grade).isEqualTo("B");
        } else if ("detachTag".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isEqualTo(1L);
        } else if ("favorite".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM favorites WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isZero();
        } else if ("setRating".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM ratings WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isZero();
        } else if ("attachTag".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isZero();
        }
    }

    private void assertMetadataResultPersisted(String operation, Long entryId) {
        if ("favorite".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM favorites WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isEqualTo(1L);
        } else if ("unfavorite".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM favorites WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isZero();
        } else if ("setRating".equals(operation)) {
            String grade = jdbcTemplate.queryForObject("SELECT grade FROM ratings WHERE vault_entry_id = ?", String.class, entryId);
            assertThat(grade).isEqualTo("S");
        } else if ("removeRating".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM ratings WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isZero();
        } else if ("attachTag".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isEqualTo(1L);
        } else if ("detachTag".equals(operation)) {
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ?", Long.class, entryId);
            assertThat(count).isZero();
        }
    }

    @Test
    @DisplayName("SBA-001: Repeated trash and restore preserve timestamps when no transition occurs")
    void repeatedTransitionsPreserveTimestamps() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        // 1. First move to trash
        VaultEntryView trashed1 = vaultEntryOperations.moveToTrash(entryId);
        Instant updatedAtAfterTrash = trashed1.updatedAt();
        Instant deletedAtAfterTrash = trashed1.deletedAt();
        assertThat(deletedAtAfterTrash).isNotNull();

        Map<String, Object> dbAfterTrash1 = jdbcTemplate.queryForMap(
                "SELECT deleted_at, updated_at FROM vault_entries WHERE id = ?", entryId);
        assertThat(dbAfterTrash1.get("deleted_at")).isNotNull();

        // Repeated move to trash (idempotent no-op)
        VaultEntryView trashed2 = vaultEntryOperations.moveToTrash(entryId);
        assertThat(trashed2.deletedAt()).isEqualTo(((java.sql.Timestamp) dbAfterTrash1.get("deleted_at")).toInstant());
        assertThat(trashed2.deletedAt()).isCloseTo(deletedAtAfterTrash, org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MILLIS));
        assertThat(trashed2.updatedAt()).isCloseTo(updatedAtAfterTrash, org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MILLIS));

        Map<String, Object> dbAfterTrash2 = jdbcTemplate.queryForMap(
                "SELECT deleted_at, updated_at FROM vault_entries WHERE id = ?", entryId);
        assertThat(dbAfterTrash2.get("deleted_at")).isEqualTo(dbAfterTrash1.get("deleted_at"));
        assertThat(dbAfterTrash2.get("updated_at")).isEqualTo(dbAfterTrash1.get("updated_at"));

        // 2. First restore
        VaultEntryView restored1 = vaultEntryOperations.restore(entryId);
        Instant updatedAtAfterRestore = restored1.updatedAt();
        assertThat(restored1.deletedAt()).isNull();

        Map<String, Object> dbAfterRestore1 = jdbcTemplate.queryForMap(
                "SELECT deleted_at, updated_at FROM vault_entries WHERE id = ?", entryId);
        assertThat(dbAfterRestore1.get("deleted_at")).isNull();

        // Repeated restore (idempotent no-op)
        VaultEntryView restored2 = vaultEntryOperations.restore(entryId);
        assertThat(restored2.deletedAt()).isNull();
        assertThat(restored2.updatedAt()).isCloseTo(updatedAtAfterRestore, org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MILLIS));

        Map<String, Object> dbAfterRestore2 = jdbcTemplate.queryForMap(
                "SELECT deleted_at, updated_at FROM vault_entries WHERE id = ?", entryId);
        assertThat(dbAfterRestore2.get("deleted_at")).isNull();
        assertThat(dbAfterRestore2.get("updated_at")).isEqualTo(dbAfterRestore1.get("updated_at"));
    }

    @Test
    @DisplayName("SBA-001: Composition of trash->restore and restore->trash within single transaction")
    void compositionWithinSingleTransaction() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        // Composition 1: trash -> restore in same tx
        requiresNewTx().executeWithoutResult(status -> {
            VaultEntryView trashed = vaultEntryOperations.moveToTrash(entryId);
            assertThat(trashed.deletedAt()).isNotNull();

            VaultEntryView restored = vaultEntryOperations.restore(entryId);
            assertThat(restored.deletedAt()).isNull();
        });

        Boolean finalDeleted1 = jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, entryId);
        assertThat(finalDeleted1).isFalse();

        // Composition 2: restore (on trashed) -> trash in same tx
        vaultEntryOperations.moveToTrash(entryId);
        requiresNewTx().executeWithoutResult(status -> {
            VaultEntryView restored = vaultEntryOperations.restore(entryId);
            assertThat(restored.deletedAt()).isNull();

            VaultEntryView trashed = vaultEntryOperations.moveToTrash(entryId);
            assertThat(trashed.deletedAt()).isNotNull();
        });

        Boolean finalDeleted2 = jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, entryId);
        assertThat(finalDeleted2).isTrue();
    }

    @Test
    @DisplayName("SBA-001: Outer transaction holding after public mutation returns blocks competing FOR UPDATE until commit")
    void outerTransactionHoldingAfterMutationReturnsBlocksCompetitor() throws Exception {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        CountDownLatch mutationReturnedLatch = new CountDownLatch(1);
        CountDownLatch commitOuterLatch = new CountDownLatch(1);
        AtomicBoolean competitorFinishedBeforeOuterCommit = new AtomicBoolean(false);
        AtomicInteger holderPidRef = new AtomicInteger();
        AtomicInteger waiterPidRef = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> f1 = executor.submit(() -> {
                requiresNewTx().executeWithoutResult(status -> {
                    holderPidRef.set(getCurrentBackendPid());
                    vaultEntryOperations.moveToTrash(entryId);
                    mutationReturnedLatch.countDown(); // Public mutation has returned!
                    try {
                        if (!commitOuterLatch.await(5, TimeUnit.SECONDS)) {
                            throw new AssertionError("Holder timed out waiting for commitOuterLatch");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError("Holder thread was interrupted", e);
                    }
                });
            });

            assertThat(mutationReturnedLatch.await(5, TimeUnit.SECONDS)).isTrue();

            Future<?> f2 = executor.submit(() -> {
                requiresNewTx().executeWithoutResult(status -> {
                    waiterPidRef.set(getCurrentBackendPid());
                    // Competing attempt to restore or lock
                    vaultEntryOperations.restore(entryId);
                    if (commitOuterLatch.getCount() > 0) {
                        competitorFinishedBeforeOuterCommit.set(true);
                    }
                });
            });

            // Competitor MUST be blocked on PostgreSQL lock held by outer uncommitted tx
            awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "vault_entries", Duration.ofSeconds(5));
            assertThat(competitorFinishedBeforeOuterCommit.get()).isFalse();

            // Commit outer transaction
            commitOuterLatch.countDown();
            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);

            // Final state: restore won after outer trash commit
            Boolean deleted = jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id = ?", Boolean.class, entryId);
            assertThat(deleted).isFalse();
        } finally {
            commitOuterLatch.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }
}
