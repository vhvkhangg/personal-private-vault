package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagDomainCommitGuardIntegrationTest extends AbstractRagIntegrationTest {

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("Conflicting source writers fail busy before mutation and succeed after the guard commits")
    void serializesConcurrentWritersOnVaultEntry() throws Exception {
        Long vaultEntryId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );

        CountDownLatch t1Locked = new CountDownLatch(1);
        CountDownLatch t1Release = new CountDownLatch(1);
        AtomicBoolean t2Acquired = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setTimeout(10);

        try {
            // T1 acquires guard and holds lock
            Future<?> t1Future = executor.submit(() -> {
                tt.execute(status -> {
                    vaultEntryOperations.acquireSourceGuard(vaultEntryId);
                    t1Locked.countDown();
                    try {
                        t1Release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                });
            });

            assertThat(t1Locked.await(5, TimeUnit.SECONDS)).isTrue();

            // T2 attempts to acquire guard on the same vault entry
            Future<?> t2Future = executor.submit(() -> {
                tt.execute(status -> {
                    vaultEntryOperations.acquireSourceGuard(vaultEntryId);
                    t2Acquired.set(true);
                    return null;
                });
            });

            // Alternative B's earlier source-state NOWAIT guard must reject the
            // competing transaction; waiting on the later Vault row is forbidden.
            assertThatThrownBy(() -> t2Future.get(5, TimeUnit.SECONDS))
                .isInstanceOf(java.util.concurrent.ExecutionException.class)
                .hasRootCauseInstanceOf(java.sql.SQLException.class)
                .rootCause().extracting(failure -> ((java.sql.SQLException)failure).getSQLState())
                .isEqualTo("P1701");
            assertThat(t2Acquired.get()).isFalse();

            // Release T1
            t1Release.countDown();
            t1Future.get(5, TimeUnit.SECONDS);
            // A fresh command after the first physical COMMIT may acquire it.
            tt.executeWithoutResult(status -> {
                vaultEntryOperations.acquireSourceGuard(vaultEntryId);
                t2Acquired.set(true);
            });
            assertThat(t2Acquired.get()).isTrue();
        } finally {
            t1Release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5,TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("acquireSourceGuards locks entries in strictly ascending order without deadlocking")
    void acquiresMultipleSourceGuardsInAscendingOrder() {
        Long id1 = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );
        Long id2 = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FILM'::vault_entry_type) RETURNING id",
                Long.class
        );
        Long id3 = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('NOTE'::vault_entry_type) RETURNING id",
                Long.class
        );

        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(status -> {
            // Pass in reverse order -> service must sort to [id1, id2, id3]
            vaultEntryOperations.acquireSourceGuards(List.of(id3, id1, id2));
            return null;
        });
    }

    @Test
    @DisplayName("acquireSourceGuard rejects invocation without active caller transaction (MANDATORY)")
    void rejectsInvocationWithoutActiveCallerTransaction() {
        Long vaultEntryId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );

        assertThatThrownBy(() -> vaultEntryOperations.acquireSourceGuard(vaultEntryId))
                .isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
    }

    @Test
    @DisplayName("acquireSourceGuard rejects invocation inside read-only caller transaction")
    void rejectsInvocationInsideReadOnlyCallerTransaction() {
        Long vaultEntryId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );

        TransactionTemplate readOnlyTt = new TransactionTemplate(transactionManager);
        readOnlyTt.setReadOnly(true);

        assertThatThrownBy(() -> readOnlyTt.execute(status -> {
            vaultEntryOperations.acquireSourceGuard(vaultEntryId);
            return null;
        })).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a writable caller transaction");
    }

    @Test
    @DisplayName("Two-source concurrent interleavings avoid 40P01 deadlock in both admission-first and owner-first orders")
    void twoSourceConcurrentInterleavingsAvoidDeadlockAndFailBusy() throws Exception {
        Long idA = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );
        Long idB = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FILM'::vault_entry_type) RETURNING id",
                Long.class
        );
        Long idC = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('NOTE'::vault_entry_type) RETURNING id",
                Long.class
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setTimeout(10);

        try {
            // Case 1: Admission-first order (T1 holds {idA, idB}, T2 attempts idB)
            CountDownLatch t1Locked = new CountDownLatch(1);
            CountDownLatch t1Release = new CountDownLatch(1);

            Future<?> t1Future = executor.submit(() -> {
                tt.execute(status -> {
                    vaultEntryOperations.acquireSourceGuards(List.of(idA, idB));
                    t1Locked.countDown();
                    try {
                        t1Release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                });
            });

            assertThat(t1Locked.await(5, TimeUnit.SECONDS)).isTrue();

            // T2 attempts to acquire guard on idB -> must fail busy P1701 without 40P01 deadlock
            Future<?> t2Future = executor.submit(() -> {
                tt.execute(status -> {
                    vaultEntryOperations.acquireSourceGuard(idB);
                    return null;
                });
            });

            assertThatThrownBy(() -> t2Future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(java.util.concurrent.ExecutionException.class)
                    .hasRootCauseInstanceOf(java.sql.SQLException.class)
                    .rootCause().extracting(f -> ((java.sql.SQLException) f).getSQLState())
                    .isEqualTo("P1701");

            // Unrelated entry idC remains editable
            tt.executeWithoutResult(status -> vaultEntryOperations.acquireSourceGuard(idC));

            t1Release.countDown();
            t1Future.get(5, TimeUnit.SECONDS);

            // Case 2: Owner-first order (T3 holds idB, T4 attempts {idA, idB})
            CountDownLatch t3Locked = new CountDownLatch(1);
            CountDownLatch t3Release = new CountDownLatch(1);

            Future<?> t3Future = executor.submit(() -> {
                tt.execute(status -> {
                    vaultEntryOperations.acquireSourceGuard(idB);
                    t3Locked.countDown();
                    try {
                        t3Release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                });
            });

            assertThat(t3Locked.await(5, TimeUnit.SECONDS)).isTrue();

            // T4 attempts multi-source acquisition on {idA, idB} -> must fail busy P1701 without 40P01 deadlock
            Future<?> t4Future = executor.submit(() -> {
                tt.execute(status -> {
                    vaultEntryOperations.acquireSourceGuards(List.of(idA, idB));
                    return null;
                });
            });

            assertThatThrownBy(() -> t4Future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(java.util.concurrent.ExecutionException.class)
                    .hasRootCauseInstanceOf(java.sql.SQLException.class)
                    .rootCause().extracting(f -> ((java.sql.SQLException) f).getSQLState())
                    .isEqualTo("P1701");

            // Unrelated entry idC remains editable
            tt.executeWithoutResult(status -> vaultEntryOperations.acquireSourceGuard(idC));

            t3Release.countDown();
            t3Future.get(5, TimeUnit.SECONDS);

            // After all transactions complete, both can be acquired cleanly
            tt.executeWithoutResult(status -> vaultEntryOperations.acquireSourceGuards(List.of(idA, idB)));
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }
}
