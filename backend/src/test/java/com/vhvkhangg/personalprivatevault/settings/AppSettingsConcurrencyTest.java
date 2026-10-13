package com.vhvkhangg.personalprivatevault.settings;

import com.vhvkhangg.personalprivatevault.settings.configuration.AppSettingsOperations;
import com.vhvkhangg.personalprivatevault.settings.configuration.InvalidSettingsException;
import com.vhvkhangg.personalprivatevault.settings.configuration.UpdateSettingsCommand;
import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SBA-002 regression tests:
 * Verifies atomic read-merge-write of settings singleton under pessimistic row lock,
 * authoritative state refresh in presence of cached/managed L1 entities,
 * preservation of disjoint concurrent updates in both commit orders across all omitted fields,
 * backup enable/interval and auto-lock rules, and JDBC assertions.
 */
class AppSettingsConcurrencyTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AppSettingsOperations settingsOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM app_settings");
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol, decimal_places)
                VALUES ('USD', 'US Dollar', '$', 2)
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol, decimal_places)
                VALUES ('VND', 'Vietnamese Dong', '₫', 0)
                ON CONFLICT (code) DO NOTHING
                """);
    }

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
    @DisplayName("SBA-002: Exact audit reproduction - cached pagination 20, committed 50, timezone update preserves 50")
    void cachedSettingsCommittedUpdatePreservesDisjointFields() {
        settingsOperations.initializeOrUpdate(
                new UpdateSettingsCommand("UTC", "USD", 20, 5, false, null));

        requiresNewTx().executeWithoutResult(status -> {
            // Preload settings with pagination 20 in L1 cache
            assertThat(settingsOperations.read()).isPresent();

            // Independent transaction commits pagination 50
            requiresNewTx().executeWithoutResult(s2 -> {
                settingsOperations.update(new UpdateSettingsCommand(null, null, 50, null, null, null));
            });

            // Outer update supplies only timezone Asia/Bangkok
            AppSettingsView updated = settingsOperations.update(
                    new UpdateSettingsCommand("Asia/Bangkok", null, null, null, null, null));

            assertThat(updated.timezone()).isEqualTo("Asia/Bangkok");
            assertThat(updated.paginationSize()).isEqualTo(50);
            assertThat(updated.defaultCurrencyCode()).isEqualTo("USD");
            assertThat(updated.privateModeAutoLockMinutes()).isEqualTo(5);
            assertThat(updated.backupEnabled()).isFalse();
            assertThat(updated.backupIntervalHours()).isNull();
        });

        // Fresh JDBC assertion: all fields preserved, pagination is 50, timezone is Asia/Bangkok
        Map<String, Object> finalRow = jdbcTemplate.queryForMap(
                "SELECT timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
        assertThat(finalRow.get("timezone")).isEqualTo("Asia/Bangkok");
        assertThat(finalRow.get("default_currency_code")).isEqualTo("USD");
        assertThat(((Number) finalRow.get("pagination_size")).intValue()).isEqualTo(50);
        assertThat(((Number) finalRow.get("private_mode_auto_lock_minutes")).intValue()).isEqualTo(5);
        assertThat(finalRow.get("backup_enabled")).isEqualTo(false);
        assertThat(finalRow.get("backup_interval_hours")).isNull();
    }

    @Test
    @DisplayName("SBA-002: Two disjoint partial updates contend on row lock in both commit orders, preserving all fields")
    void disjointPartialUpdatesContendInBothOrders() throws Exception {
        // Order A: T1 (timezone Asia/Tokyo) first, T2 (pagination 100) contends
        {
            cleanUp();
            settingsOperations.initializeOrUpdate(
                    new UpdateSettingsCommand("UTC", "USD", 20, 5, false, null));

            CountDownLatch t1LockAcquired = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        settingsOperations.update(new UpdateSettingsCommand("Asia/Tokyo", null, null, null, null, null));
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
                        settingsOperations.update(new UpdateSettingsCommand(null, null, 100, null, null, null));
                    });
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "app_settings", Duration.ofSeconds(5));
                t1Proceed.countDown();

                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                Map<String, Object> finalState = jdbcTemplate.queryForMap(
                        "SELECT timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
                assertThat(finalState.get("timezone")).isEqualTo("Asia/Tokyo");
                assertThat(finalState.get("default_currency_code")).isEqualTo("USD");
                assertThat(((Number) finalState.get("pagination_size")).intValue()).isEqualTo(100);
                assertThat(((Number) finalState.get("private_mode_auto_lock_minutes")).intValue()).isEqualTo(5);
                assertThat(finalState.get("backup_enabled")).isEqualTo(false);
                assertThat(finalState.get("backup_interval_hours")).isNull();
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }

        // Order B: T1 (pagination 75) first, T2 (timezone Asia/Seoul) contends
        {
            cleanUp();
            settingsOperations.initializeOrUpdate(
                    new UpdateSettingsCommand("UTC", "USD", 20, 5, false, null));

            CountDownLatch t1LockAcquired = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        settingsOperations.update(new UpdateSettingsCommand(null, null, 75, null, null, null));
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
                        settingsOperations.update(new UpdateSettingsCommand("Asia/Seoul", null, null, null, null, null));
                    });
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "app_settings", Duration.ofSeconds(5));
                t1Proceed.countDown();

                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                Map<String, Object> finalState = jdbcTemplate.queryForMap(
                        "SELECT timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
                assertThat(finalState.get("timezone")).isEqualTo("Asia/Seoul");
                assertThat(finalState.get("default_currency_code")).isEqualTo("USD");
                assertThat(((Number) finalState.get("pagination_size")).intValue()).isEqualTo(75);
                assertThat(((Number) finalState.get("private_mode_auto_lock_minutes")).intValue()).isEqualTo(5);
                assertThat(finalState.get("backup_enabled")).isEqualTo(false);
                assertThat(finalState.get("backup_interval_hours")).isNull();
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }
    }

    @Test
    @DisplayName("SBA-002: Auto-lock and backup enable/interval contend on row lock in both orders, preserving both changes")
    void autoLockAndBackupContentionBothOrders() throws Exception {
        // Order 1: T1 (auto-lock 15) first, T2 (backupEnabled=true, interval=12) contends
        {
            cleanUp();
            settingsOperations.initializeOrUpdate(
                    new UpdateSettingsCommand("UTC", "USD", 20, 5, false, null));

            CountDownLatch t1LockAcquired = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        settingsOperations.update(new UpdateSettingsCommand(null, null, null, 15, null, null));
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
                        settingsOperations.update(new UpdateSettingsCommand(null, null, null, null, true, 12));
                    });
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "app_settings", Duration.ofSeconds(5));
                t1Proceed.countDown();

                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                Map<String, Object> finalState = jdbcTemplate.queryForMap(
                        "SELECT timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
                assertThat(finalState.get("timezone")).isEqualTo("UTC");
                assertThat(finalState.get("default_currency_code")).isEqualTo("USD");
                assertThat(((Number) finalState.get("pagination_size")).intValue()).isEqualTo(20);
                assertThat(((Number) finalState.get("private_mode_auto_lock_minutes")).intValue()).isEqualTo(15);
                assertThat(finalState.get("backup_enabled")).isEqualTo(true);
                assertThat(((Number) finalState.get("backup_interval_hours")).intValue()).isEqualTo(12);
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }

        // Order 2: T1 (backupEnabled=true, interval=12) first, T2 (auto-lock 15) contends
        {
            cleanUp();
            settingsOperations.initializeOrUpdate(
                    new UpdateSettingsCommand("UTC", "USD", 20, 5, false, null));

            CountDownLatch t1LockAcquired = new CountDownLatch(1);
            CountDownLatch t1Proceed = new CountDownLatch(1);
            AtomicInteger holderPidRef = new AtomicInteger();
            AtomicInteger waiterPidRef = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<?> f1 = executor.submit(() -> {
                    requiresNewTx().executeWithoutResult(status -> {
                        holderPidRef.set(getCurrentBackendPid());
                        settingsOperations.update(new UpdateSettingsCommand(null, null, null, null, true, 12));
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
                        settingsOperations.update(new UpdateSettingsCommand(null, null, null, 15, null, null));
                    });
                });

                awaitCompetitorBlockedBy(holderPidRef.get(), waiterPidRef, "app_settings", Duration.ofSeconds(5));
                t1Proceed.countDown();

                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                Map<String, Object> finalState = jdbcTemplate.queryForMap(
                        "SELECT timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
                assertThat(finalState.get("timezone")).isEqualTo("UTC");
                assertThat(finalState.get("default_currency_code")).isEqualTo("USD");
                assertThat(((Number) finalState.get("pagination_size")).intValue()).isEqualTo(20);
                assertThat(((Number) finalState.get("private_mode_auto_lock_minutes")).intValue()).isEqualTo(15);
                assertThat(finalState.get("backup_enabled")).isEqualTo(true);
                assertThat(((Number) finalState.get("backup_interval_hours")).intValue()).isEqualTo(12);
            } finally {
                t1Proceed.countDown();
                executor.shutdown();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            }
        }
    }

    @Test
    @DisplayName("SBA-002: Auto-lock and backup enable/interval preserved vs unrelated updates")
    void autoLockAndBackupPreservedVsUnrelatedUpdates() {
        settingsOperations.initializeOrUpdate(
                new UpdateSettingsCommand("UTC", "USD", 25, 15, true, 12));

        // Unrelated update changing only currency
        AppSettingsView updated = settingsOperations.update(
                new UpdateSettingsCommand(null, "VND", null, null, null, null));

        assertThat(updated.defaultCurrencyCode()).isEqualTo("VND");
        assertThat(updated.timezone()).isEqualTo("UTC");
        assertThat(updated.paginationSize()).isEqualTo(25);
        assertThat(updated.privateModeAutoLockMinutes()).isEqualTo(15);
        assertThat(updated.backupEnabled()).isTrue();
        assertThat(updated.backupIntervalHours()).isEqualTo(12);

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT default_currency_code, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
        assertThat(row.get("default_currency_code")).isEqualTo("VND");
        assertThat(((Number) row.get("private_mode_auto_lock_minutes")).intValue()).isEqualTo(15);
        assertThat(row.get("backup_enabled")).isEqualTo(true);
        assertThat(((Number) row.get("backup_interval_hours")).intValue()).isEqualTo(12);
    }

    @Test
    @DisplayName("SBA-002: Explicit backup disable clears interval; omission preserves committed values")
    void explicitBackupDisableClearsIntervalOmissionPreserves() {
        // Start with backup enabled, interval 24
        settingsOperations.initializeOrUpdate(
                new UpdateSettingsCommand("UTC", "USD", 20, 5, true, 24));

        // Omission of backup fields preserves committed values
        AppSettingsView omittedUpdate = settingsOperations.update(
                new UpdateSettingsCommand("Asia/Tokyo", null, null, null, null, null));
        assertThat(omittedUpdate.backupEnabled()).isTrue();
        assertThat(omittedUpdate.backupIntervalHours()).isEqualTo(24);

        // Explicit backup disable clears interval
        AppSettingsView disabledUpdate = settingsOperations.update(
                new UpdateSettingsCommand(null, null, null, null, false, null));
        assertThat(disabledUpdate.backupEnabled()).isFalse();
        assertThat(disabledUpdate.backupIntervalHours()).isNull();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT backup_enabled, backup_interval_hours FROM app_settings WHERE id = 1");
        assertThat(row.get("backup_enabled")).isEqualTo(false);
        assertThat(row.get("backup_interval_hours")).isNull();
    }

    @Test
    @DisplayName("SBA-002: Invalid-merge raises InvalidSettingsException and does not write to database")
    void invalidMergeDoesNotWrite() {
        settingsOperations.initializeOrUpdate(
                new UpdateSettingsCommand("UTC", "USD", 20, 5, false, null));
        java.sql.Timestamp before = jdbcTemplate.queryForObject("SELECT updated_at FROM app_settings WHERE id = 1", java.sql.Timestamp.class);

        assertThatThrownBy(() -> settingsOperations.update(
                new UpdateSettingsCommand(null, null, -10, null, null, null)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("Pagination size must be positive");

        java.sql.Timestamp after = jdbcTemplate.queryForObject("SELECT updated_at FROM app_settings WHERE id = 1", java.sql.Timestamp.class);
        assertThat(after).isEqualTo(before);
    }

    @Test
    @DisplayName("SBA-002: Missing-row path initializes with Schema v1 defaults; existing row applies merge")
    void missingRowDefaultsAndExistingRowMerge() {
        // Missing row: initialize applies defaults
        AppSettingsView initialized = settingsOperations.initialize("USD");
        assertThat(initialized.defaultCurrencyCode()).isEqualTo("USD");
        assertThat(initialized.timezone()).isEqualTo("Asia/Ho_Chi_Minh");
        assertThat(initialized.paginationSize()).isEqualTo(20);
        assertThat(initialized.privateModeAutoLockMinutes()).isEqualTo(5);
        assertThat(initialized.backupEnabled()).isFalse();
        assertThat(initialized.backupIntervalHours()).isNull();

        // Existing row: partial update merges correctly
        AppSettingsView updated = settingsOperations.update(
                new UpdateSettingsCommand("Europe/London", null, null, null, null, null));
        assertThat(updated.timezone()).isEqualTo("Europe/London");
        assertThat(updated.defaultCurrencyCode()).isEqualTo("USD");
        assertThat(updated.paginationSize()).isEqualTo(20);
    }
}
