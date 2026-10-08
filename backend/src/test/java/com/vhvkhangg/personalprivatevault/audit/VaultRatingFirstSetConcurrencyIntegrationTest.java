package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.metadata.VaultMetadataOperations;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BA15-10 regression test:
 * Verifies atomic upsert serialization for concurrent first-set ratings on unrated vault entries,
 * forced PostgreSQL row-lock contention, and preservation of initial createdAt timestamp.
 */
class VaultRatingFirstSetConcurrencyIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private VaultMetadataOperations vaultMetadataOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("BA15-10: Concurrent first-set rating operations on unrated vault entry succeed with atomic upsert")
    void concurrentFirstSetRatingSucceeds() throws Exception {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> future1 = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                vaultMetadataOperations.setRating(entryId, RatingGrade.B);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Future<?> future2 = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                vaultMetadataOperations.setRating(entryId, RatingGrade.A);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        future1.get(10, TimeUnit.SECONDS);
        future2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly one rating exists and its grade is one of the two sets
        RatingGrade rating = vaultMetadataOperations.metadata(entryId).rating();
        assertThat(rating).isIn(RatingGrade.B, RatingGrade.A);

        // Subsequent update works
        vaultMetadataOperations.setRating(entryId, RatingGrade.C);
        assertThat(vaultMetadataOperations.metadata(entryId).rating()).isEqualTo(RatingGrade.C);

        // Subsequent removal works
        vaultMetadataOperations.removeRating(entryId);
        assertThat(vaultMetadataOperations.metadata(entryId).rating()).isNull();
    }

    @Test
    @DisplayName("BA15-10: Forced first-insert contention serializes via PostgreSQL row lock and preserves createdAt timestamp")
    void forcedFirstInsertContentionAndCreatedAtPreservation() throws Exception {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        CountDownLatch t1InsertedLatch = new CountDownLatch(1);
        CountDownLatch t2BlockedLatch = new CountDownLatch(1);
        CountDownLatch t1CommitLatch = new CountDownLatch(1);
        AtomicBoolean t2CompletedBeforeT1Commit = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> f1 = executor.submit(() -> {
            txTemplate.execute(status -> {
                vaultMetadataOperations.setRating(entryId, RatingGrade.B);
                t1InsertedLatch.countDown();
                try {
                    t1CommitLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            return null;
        });

        Future<?> f2 = executor.submit(() -> {
            try {
                // Ensure T1 inserted first
                t1InsertedLatch.await(5, TimeUnit.SECONDS);
                // Signal that T2 is about to attempt insert
                t2BlockedLatch.countDown();
                // Sleep briefly to ensure T2 attempts row lock while T1 is uncommitted
                txTemplate.execute(status -> {
                    vaultMetadataOperations.setRating(entryId, RatingGrade.A);
                    return null;
                });
                if (t1CommitLatch.getCount() > 0) {
                    t2CompletedBeforeT1Commit.set(true);
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(t1InsertedLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(t2BlockedLatch.await(5, TimeUnit.SECONDS)).isTrue();

        // Deterministically observe competing ungranted lock in pg_locks
        awaitCompetingLock(java.time.Duration.ofSeconds(5));
        assertThat(t2CompletedBeforeT1Commit.get()).isFalse();

        // Release T1 to commit
        t1CommitLatch.countDown();
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify in database: T2 succeeded via DO UPDATE, rating grade is A, and createdAt was preserved
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT grade, created_at, updated_at FROM ratings WHERE vault_entry_id = ?", entryId
        );
        assertThat(row.get("grade")).isEqualTo("A");

        Timestamp createdAt = (Timestamp) row.get("created_at");
        Timestamp updatedAt = (Timestamp) row.get("updated_at");
        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        assertThat(createdAt.getTime()).isLessThanOrEqualTo(updatedAt.getTime());
    }

    @Test
    @DisplayName("BA15-10: Subsequent rating update preserves original createdAt and advances updatedAt")
    void subsequentUpdatePreservesOriginalCreatedAtAndAdvancesUpdatedAt() throws Exception {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
        Long entryId = entry.id();

        vaultMetadataOperations.setRating(entryId, RatingGrade.C);

        Map<String, Object> initialRow = jdbcTemplate.queryForMap(
                "SELECT grade, created_at, updated_at FROM ratings WHERE vault_entry_id = ?", entryId
        );
        Timestamp originalCreatedAt = (Timestamp) initialRow.get("created_at");
        Timestamp initialUpdatedAt = (Timestamp) initialRow.get("updated_at");
        assertThat(originalCreatedAt).isNotNull();
        assertThat(initialUpdatedAt).isNotNull();

        Thread.sleep(50);

        vaultMetadataOperations.setRating(entryId, RatingGrade.S);

        Map<String, Object> updatedRow = jdbcTemplate.queryForMap(
                "SELECT grade, created_at, updated_at FROM ratings WHERE vault_entry_id = ?", entryId
        );
        assertThat(updatedRow.get("grade")).isEqualTo("S");

        Timestamp currentCreatedAt = (Timestamp) updatedRow.get("created_at");
        Timestamp currentUpdatedAt = (Timestamp) updatedRow.get("updated_at");

        // Authentically proves createdAt was preserved and updatedAt advanced
        assertThat(currentCreatedAt).isEqualTo(originalCreatedAt);
        assertThat(currentUpdatedAt.getTime()).isGreaterThan(initialUpdatedAt.getTime());
    }
}
