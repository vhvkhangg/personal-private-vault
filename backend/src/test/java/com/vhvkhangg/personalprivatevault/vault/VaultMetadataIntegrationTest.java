package com.vhvkhangg.personalprivatevault.vault;

import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.metadata.VaultMetadataOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultMetadataView;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;

import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.internal.application.metadata.VaultMetadataService;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.Rating;
import com.vhvkhangg.personalprivatevault.vault.internal.domain.Tag;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.FavoriteRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.RatingRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.TagRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryTagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class VaultMetadataIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private VaultMetadataOperations vaultMetadataOperations;

    @Autowired
    private VaultEntryRepository vaultEntryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private VaultEntryTagRepository vaultEntryTagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("Favorite and unfavorite operations are idempotent and reflected in metadata")
    void favoriteAndUnfavoriteAreIdempotent() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FICTION);

        // Initially not favorited
        VaultMetadataView meta0 = vaultMetadataOperations.metadata(entry.id());
        assertThat(meta0.favorite()).isFalse();

        // Favorite
        vaultMetadataOperations.favorite(entry.id());
        assertThat(vaultMetadataOperations.metadata(entry.id()).favorite()).isTrue();

        // Repeated favorite is idempotent
        vaultMetadataOperations.favorite(entry.id());
        assertThat(vaultMetadataOperations.metadata(entry.id()).favorite()).isTrue();

        // Unfavorite
        vaultMetadataOperations.unfavorite(entry.id());
        assertThat(vaultMetadataOperations.metadata(entry.id()).favorite()).isFalse();

        // Repeated unfavorite is idempotent
        vaultMetadataOperations.unfavorite(entry.id());
        assertThat(vaultMetadataOperations.metadata(entry.id()).favorite()).isFalse();
    }

    @Test
    @DisplayName("Rating create-or-update preserves createdAt and updates updatedAt; removal is idempotent")
    void ratingCreateOrUpdateAndRemovalWithClock() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FILM);

        Instant t0 = Instant.parse("2026-09-27T12:00:00Z");
        Instant t1 = Instant.parse("2026-09-27T12:30:00Z");

        // 1. Initial rating at t0
        Clock clock0 = Clock.fixed(t0, ZoneOffset.UTC);
        VaultMetadataService service0 = new VaultMetadataService(
                vaultEntryRepository, favoriteRepository, ratingRepository, tagRepository, vaultEntryTagRepository, clock0
        );
        service0.setRating(entry.id(), RatingGrade.B);

        Rating r0 = ratingRepository.findById(entry.id()).orElseThrow();
        assertThat(r0.getGrade()).isEqualTo(RatingGrade.B);
        assertThat(r0.getCreatedAt()).isEqualTo(t0);
        assertThat(r0.getUpdatedAt()).isEqualTo(t0);

        VaultMetadataView meta1 = service0.metadata(entry.id());
        assertThat(meta1.rating()).isEqualTo(RatingGrade.B);

        // 2. Update rating at t1: preserves createdAt, updates updatedAt
        Clock clock1 = Clock.fixed(t1, ZoneOffset.UTC);
        VaultMetadataService service1 = new VaultMetadataService(
                vaultEntryRepository, favoriteRepository, ratingRepository, tagRepository, vaultEntryTagRepository, clock1
        );
        service1.setRating(entry.id(), RatingGrade.S);

        Rating r1 = ratingRepository.findById(entry.id()).orElseThrow();
        assertThat(r1.getGrade()).isEqualTo(RatingGrade.S);
        assertThat(r1.getCreatedAt()).isEqualTo(t0);
        assertThat(r1.getUpdatedAt()).isEqualTo(t1);

        assertThat(service1.metadata(entry.id()).rating()).isEqualTo(RatingGrade.S);

        // 3. Remove rating
        service1.removeRating(entry.id());
        assertThat(service1.metadata(entry.id()).rating()).isNull();
        assertThat(ratingRepository.findById(entry.id())).isEmpty();

        // 4. Repeated removal is idempotent
        service1.removeRating(entry.id());
        assertThat(service1.metadata(entry.id()).rating()).isNull();
    }

    @Test
    @DisplayName("Tag creation: whitespace trimming, rejection of empty, diacritics preservation, and case-insensitive reuse")
    void tagCreationValidationAndCaseInsensitiveReuse() {
        // Trims surrounding whitespace
        TagView tag1 = vaultMetadataOperations.createTag("  action  ");
        assertThat(tag1.name()).isEqualTo("action");
        assertThat(tag1.id()).isNotNull();

        // Preserves diacritics and case
        TagView tagVi = vaultMetadataOperations.createTag("  Kiếm Hiệp  ");
        assertThat(tagVi.name()).isEqualTo("Kiếm Hiệp");

        // Rejects null and blank names
        assertThatThrownBy(() -> vaultMetadataOperations.createTag(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.createTag(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.createTag("   "))
                .isInstanceOf(IllegalArgumentException.class);

        // Case-insensitive duplicate returns existing tag
        TagView tagDup = vaultMetadataOperations.createTag("ACTION");
        assertThat(tagDup.id()).isEqualTo(tag1.id());
        assertThat(tagDup.name()).isEqualTo("action");
    }

    @Test
    @DisplayName("Concurrent case-insensitive tag creation forces unique-index conflict and both return the same canonical tag")
    void concurrentCaseInsensitiveTagCreationRecoversFromUniqueIndexConflict() throws Exception {
        String tagA = "  ConcurrentTag  ";
        String tagB = "concurrentTAG  ";
        String canonicalLower = "concurrenttag";

        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Starts independent transaction, executes insert, holds lock before commit
            Future<Tag> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Tag tag = tagRepository.saveAndFlush(new Tag("ConcurrentTag", Instant.now()));
                thread1Inserted.countDown();
                try {
                    // Wait until thread 2 has entered createTag and is blocked on the unique index
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return tag;
            }));

            // Wait until thread 1 has executed saveAndFlush (holding uncommitted unique index lock)
            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls vaultMetadataOperations.createTag with same name but different case
            Future<TagView> thread2Future = executor.submit(() -> {
                return vaultMetadataOperations.createTag(tagB);
            });

            // Allow thread 2 to reach and block on PostgreSQL's uq_ci_tags_name index
            Thread.sleep(200);

            // Now let thread 1 commit its transaction
            thread2ReadyToCommit.countDown();

            Tag tag1 = thread1Future.get(10, TimeUnit.SECONDS);
            TagView tag2 = thread2Future.get(10, TimeUnit.SECONDS);

            // Both callers must obtain the same persisted canonical tag
            assertThat(tag1).isNotNull();
            assertThat(tag2).isNotNull();
            assertThat(tag2.id()).isEqualTo(tag1.getId());
            assertThat(tag2.name()).isEqualTo("ConcurrentTag");

            // Verify only one case-insensitive row exists in the database
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM tags WHERE LOWER(name) = ?",
                    Integer.class,
                    canonicalLower
            );
            assertThat(count).isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            jdbcTemplate.update("DELETE FROM tags WHERE LOWER(name) = ?", canonicalLower);
        }
    }

    @Test
    @DisplayName("Tag attach and detach: idempotency, duplicate prevention, and deterministic tag ordering")
    void tagAttachmentIdempotencyAndDeterministicOrdering() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.STUDY);

        TagView tagA = vaultMetadataOperations.createTag("zeta");
        TagView tagB = vaultMetadataOperations.createTag("Alpha");
        TagView tagC = vaultMetadataOperations.createTag("beta");

        vaultMetadataOperations.attachTag(entry.id(), tagA.id());
        vaultMetadataOperations.attachTag(entry.id(), tagB.id());
        vaultMetadataOperations.attachTag(entry.id(), tagC.id());

        // Repeated attach is idempotent and does not create duplicate rows
        vaultMetadataOperations.attachTag(entry.id(), tagA.id());

        VaultMetadataView meta = vaultMetadataOperations.metadata(entry.id());
        assertThat(meta.tags()).hasSize(3);
        // Deterministic ordering: case-insensitive name ("Alpha", "beta", "zeta")
        assertThat(meta.tags()).extracting(TagView::name)
                .containsExactly("Alpha", "beta", "zeta");

        // Detach tag
        vaultMetadataOperations.detachTag(entry.id(), tagB.id());
        VaultMetadataView metaAfterDetach = vaultMetadataOperations.metadata(entry.id());
        assertThat(metaAfterDetach.tags()).extracting(TagView::name)
                .containsExactly("beta", "zeta");

        // Repeated detach is idempotent
        vaultMetadataOperations.detachTag(entry.id(), tagB.id());
        assertThat(vaultMetadataOperations.metadata(entry.id()).tags()).extracting(TagView::name)
                .containsExactly("beta", "zeta");

        // Attach/detach with non-existent tag throws NoSuchElementException
        assertThatThrownBy(() -> vaultMetadataOperations.attachTag(entry.id(), 999999L))
                .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.detachTag(entry.id(), 999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Soft delete retains metadata rows; metadata writes against trashed entry fail with IllegalStateException")
    void softDeleteRetainsMetadataAndProtectsAgainstWrites() {
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FICTION);
        TagView tag = vaultMetadataOperations.createTag("Fantasy");

        vaultMetadataOperations.favorite(entry.id());
        vaultMetadataOperations.setRating(entry.id(), RatingGrade.A);
        vaultMetadataOperations.attachTag(entry.id(), tag.id());

        // Move to trash
        vaultEntryOperations.moveToTrash(entry.id());

        // Reading metadata is allowed and rows are retained
        VaultMetadataView trashedMeta = vaultMetadataOperations.metadata(entry.id());
        assertThat(trashedMeta.favorite()).isTrue();
        assertThat(trashedMeta.rating()).isEqualTo(RatingGrade.A);
        assertThat(trashedMeta.tags()).extracting(TagView::name).containsExactly("Fantasy");

        // Metadata writes against trashed entry fail with IllegalStateException
        assertThatThrownBy(() -> vaultMetadataOperations.favorite(entry.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in trash");

        assertThatThrownBy(() -> vaultMetadataOperations.unfavorite(entry.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in trash");

        assertThatThrownBy(() -> vaultMetadataOperations.setRating(entry.id(), RatingGrade.S))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in trash");

        assertThatThrownBy(() -> vaultMetadataOperations.removeRating(entry.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in trash");

        TagView newTag = vaultMetadataOperations.createTag("Adventure");
        assertThatThrownBy(() -> vaultMetadataOperations.attachTag(entry.id(), newTag.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in trash");

        assertThatThrownBy(() -> vaultMetadataOperations.detachTag(entry.id(), tag.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("in trash");

        // Restore entry: writes succeed again
        vaultEntryOperations.restore(entry.id());
        vaultMetadataOperations.setRating(entry.id(), RatingGrade.S);
        assertThat(vaultMetadataOperations.metadata(entry.id()).rating()).isEqualTo(RatingGrade.S);
    }

    @Test
    @DisplayName("Capability matrix enforcement: FILM_CREDIT is favorite-only; BRAND supports rating and tags")
    void capabilityMatrixEnforcementInService() {
        // FILM_CREDIT: favorite allowed, rating & tag forbidden
        VaultEntryView filmCredit = vaultEntryOperations.create(VaultEntryType.FILM_CREDIT);
        TagView tag = vaultMetadataOperations.createTag("Actor Credit");

        vaultMetadataOperations.favorite(filmCredit.id());
        assertThat(vaultMetadataOperations.metadata(filmCredit.id()).favorite()).isTrue();

        assertThatThrownBy(() -> vaultMetadataOperations.setRating(filmCredit.id(), RatingGrade.A))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support ratings");

        assertThatThrownBy(() -> vaultMetadataOperations.removeRating(filmCredit.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support ratings");

        assertThatThrownBy(() -> vaultMetadataOperations.attachTag(filmCredit.id(), tag.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support tags");

        assertThatThrownBy(() -> vaultMetadataOperations.detachTag(filmCredit.id(), tag.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support tags");

        // BRAND: supports favorite, rating, and tags
        VaultEntryView brand = vaultEntryOperations.create(VaultEntryType.BRAND);
        vaultMetadataOperations.favorite(brand.id());
        vaultMetadataOperations.setRating(brand.id(), RatingGrade.S);
        vaultMetadataOperations.attachTag(brand.id(), tag.id());

        VaultMetadataView brandMeta = vaultMetadataOperations.metadata(brand.id());
        assertThat(brandMeta.favorite()).isTrue();
        assertThat(brandMeta.rating()).isEqualTo(RatingGrade.S);
        assertThat(brandMeta.tags()).extracting(TagView::name).containsExactly("Actor Credit");
    }

    @Test
    @DisplayName("Null arguments and missing entry checks in VaultMetadataOperations")
    void nullArgumentsAndMissingEntryChecks() {
        assertThatThrownBy(() -> vaultMetadataOperations.metadata(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.favorite(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.unfavorite(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.setRating(null, RatingGrade.A))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.setRating(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.removeRating(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.attachTag(null, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.attachTag(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.detachTag(null, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.detachTag(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        // Missing entry
        assertThatThrownBy(() -> vaultMetadataOperations.metadata(999999L))
                .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> vaultMetadataOperations.favorite(999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Concurrent favorite calls from independent transactions succeed idempotently leaving exactly one row")
    void concurrentFavoriteCallsSucceedIdempotentlyAndLeaveSingleRow() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        VaultEntryView entry = txTemplate.execute(status -> vaultEntryOperations.create(VaultEntryType.FICTION));
        assertThat(entry).isNotNull();

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Future<Void>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    barrier.await(5, TimeUnit.SECONDS);
                    vaultMetadataOperations.favorite(entry.id());
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM favorites WHERE vault_entry_id = ?",
                    Integer.class,
                    entry.id()
            );
            assertThat(count).isEqualTo(1);
            assertThat(vaultMetadataOperations.metadata(entry.id()).favorite()).isTrue();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            txTemplate.execute(status -> {
                jdbcTemplate.update("DELETE FROM favorites WHERE vault_entry_id = ?", entry.id());
                jdbcTemplate.update("DELETE FROM vault_entries WHERE id = ?", entry.id());
                return null;
            });
        }
    }

    @Test
    @DisplayName("Concurrent attachTag calls from independent transactions succeed idempotently leaving exactly one row")
    void concurrentAttachTagCallsSucceedIdempotentlyAndLeaveSingleRow() throws Exception {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        VaultEntryView entry = txTemplate.execute(status -> vaultEntryOperations.create(VaultEntryType.BRAND));
        TagView tag = txTemplate.execute(status -> vaultMetadataOperations.createTag("ConcurrentTagAttachTest"));
        assertThat(entry).isNotNull();
        assertThat(tag).isNotNull();

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Future<Void>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    barrier.await(5, TimeUnit.SECONDS);
                    vaultMetadataOperations.attachTag(entry.id(), tag.id());
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ? AND tag_id = ?",
                    Integer.class,
                    entry.id(),
                    tag.id()
            );
            assertThat(count).isEqualTo(1);
            assertThat(vaultMetadataOperations.metadata(entry.id()).tags())
                    .extracting(TagView::id)
                    .contains(tag.id());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            txTemplate.execute(status -> {
                jdbcTemplate.update("DELETE FROM vault_entry_tags WHERE vault_entry_id = ? AND tag_id = ?", entry.id(), tag.id());
                jdbcTemplate.update("DELETE FROM tags WHERE id = ?", tag.id());
                jdbcTemplate.update("DELETE FROM vault_entries WHERE id = ?", entry.id());
                return null;
            });
        }
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back favorite and tag attachment")
    void rollbackOfEnclosingTransactionRollsBackFavoriteAndTagAttachment() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        VaultEntryView entry = txTemplate.execute(status -> vaultEntryOperations.create(VaultEntryType.IMAGE));
        TagView tag = txTemplate.execute(status -> vaultMetadataOperations.createTag("RollbackTagTest"));
        assertThat(entry).isNotNull();
        assertThat(tag).isNotNull();

        try {
            // Execute favorite and attachTag inside a transaction that is rolled back
            txTemplate.execute(status -> {
                vaultMetadataOperations.favorite(entry.id());
                vaultMetadataOperations.attachTag(entry.id(), tag.id());
                status.setRollbackOnly();
                return null;
            });

            // Verify both rows were rolled back
            Integer favCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM favorites WHERE vault_entry_id = ?",
                    Integer.class,
                    entry.id()
            );
            assertThat(favCount).isZero();

            Integer tagCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entry_tags WHERE vault_entry_id = ? AND tag_id = ?",
                    Integer.class,
                    entry.id(),
                    tag.id()
            );
            assertThat(tagCount).isZero();
        } finally {
            txTemplate.execute(status -> {
                jdbcTemplate.update("DELETE FROM vault_entry_tags WHERE vault_entry_id = ? AND tag_id = ?", entry.id(), tag.id());
                jdbcTemplate.update("DELETE FROM favorites WHERE vault_entry_id = ?", entry.id());
                jdbcTemplate.update("DELETE FROM tags WHERE id = ?", tag.id());
                jdbcTemplate.update("DELETE FROM vault_entries WHERE id = ?", entry.id());
                return null;
            });
        }
    }
}
