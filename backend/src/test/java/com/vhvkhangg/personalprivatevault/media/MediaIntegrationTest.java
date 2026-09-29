package com.vhvkhangg.personalprivatevault.media;

import com.vhvkhangg.personalprivatevault.media.album.AlbumOperations;
import com.vhvkhangg.personalprivatevault.media.album.CreateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.album.UpdateAlbumCommand;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand;
import com.vhvkhangg.personalprivatevault.media.view.AlbumView;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class MediaIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AlbumOperations albumOperations;

    @Autowired
    private ImageOperations imageOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanUp() {
        tearDown();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM images");
        jdbcTemplate.execute("DELETE FROM albums");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type IN ('ALBUM', 'IMAGE')");
    }

    @Test
    @DisplayName("Creates and updates Album backed by Vault Entry")
    void createAndUpdateAlbum() {
        AlbumView created = albumOperations.create(new CreateAlbumCommand("Holiday 2026", "Family trip"));

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Holiday 2026");
        assertThat(created.description()).isEqualTo("Family trip");
        assertThat(created.imageCount()).isZero();

        // Verify Vault Entry exists with type ALBUM
        String vaultType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                created.id()
        );
        assertThat(vaultType).isEqualTo("ALBUM");

        // Update album
        AlbumView updated = albumOperations.update(new UpdateAlbumCommand(
                created.id(), "Holiday 2026 Summer", "Updated description"
        ));
        assertThat(updated.title()).isEqualTo("Holiday 2026 Summer");
        assertThat(updated.description()).isEqualTo("Updated description");

        AlbumView reloaded = albumOperations.findById(created.id());
        assertThat(reloaded.title()).isEqualTo("Holiday 2026 Summer");
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back Album and its Vault Entry")
    void rollbackOfAlbumRollsBackVaultEntry() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            albumOperations.create(new CreateAlbumCommand("Doomed Album", "Will rollback"));
            throw new RuntimeException("Force rollback");
        })).hasMessageContaining("Force rollback");

        Integer albumCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM albums WHERE title = 'Doomed Album'",
                Integer.class
        );
        assertThat(albumCount).isZero();

        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'ALBUM'",
                Integer.class
        );
        assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("Creates standalone and album-assigned Images with derived album imageCount")
    void createImagesAndDerivedAlbumCount() {
        AlbumView album = albumOperations.create(new CreateAlbumCommand("Vacation", "Summer"));
        assertThat(album.imageCount()).isZero();

        Instant now = Instant.now();
        ImageView img1 = imageOperations.create(new CreateImageCommand(
                album.id(), "Beach", "photo", "photos/beach.jpg", "https://cdn.example.com/beach.jpg",
                "image/jpeg", 204800L, 1920, 1080,
                "1111111111111111111111111111111111111111111111111111111111111111", now, "Da Nang"
        ));
        assertThat(img1.id()).isNotNull();
        assertThat(img1.albumId()).isEqualTo(album.id());

        // Verify Vault Entry exists with type IMAGE
        String vaultType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                img1.id()
        );
        assertThat(vaultType).isEqualTo("IMAGE");

        ImageView img2 = imageOperations.create(new CreateImageCommand(
                album.id(), "Sunset", "photo", "photos/sunset.jpg", null,
                "image/jpeg", 102400L, 1280, 720,
                "2222222222222222222222222222222222222222222222222222222222222222", now, null
        ));

        // Create standalone image (albumId = null)
        ImageView standalone = imageOperations.create(new CreateImageCommand(
                null, "Document", "scan", "scans/doc.png", null,
                "image/png", 51200L, 800, 600,
                "3333333333333333333333333333333333333333333333333333333333333333", null, null
        ));
        assertThat(standalone.albumId()).isNull();

        // Check derived imageCount on AlbumView without loading images into memory
        AlbumView reloadedAlbum = albumOperations.findById(album.id());
        assertThat(reloadedAlbum.imageCount()).isEqualTo(2L);
        assertThat(albumOperations.getImageCount(album.id())).isEqualTo(2L);
    }

    @Test
    @DisplayName("Rollback of Image creation rolls back Image and its Vault Entry")
    void rollbackOfImageRollsBackVaultEntry() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            imageOperations.create(new CreateImageCommand(
                    null, "Doomed Image", null, "doomed.jpg", null,
                    "image/jpeg", 100L, 100, 100,
                    null, null, null
            ));
            throw new RuntimeException("Force image rollback");
        })).hasMessageContaining("Force image rollback");

        Integer imageCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM images WHERE object_key = 'doomed.jpg'",
                Integer.class
        );
        assertThat(imageCount).isZero();

        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'IMAGE'",
                Integer.class
        );
        assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("Retrieves bounded paged images for an album")
    void pagedRetrievalByAlbumId() {
        AlbumView album = albumOperations.create(new CreateAlbumCommand("Gallery", null));

        for (int i = 1; i <= 5; i++) {
            imageOperations.create(new CreateImageCommand(
                    album.id(), "Photo " + i, "photo", "gallery/p" + i + ".jpg", null,
                    "image/jpeg", 1000L * i, 100 * i, 100 * i,
                    String.format("%064d", i), null, null
            ));
        }

        List<ImageView> page0 = imageOperations.findByAlbumId(album.id(), 2, 0);
        assertThat(page0).hasSize(2);
        assertThat(page0.get(0).title()).isEqualTo("Photo 1");
        assertThat(page0.get(1).title()).isEqualTo("Photo 2");

        List<ImageView> page1 = imageOperations.findByAlbumId(album.id(), 2, 2);
        assertThat(page1).hasSize(2);
        assertThat(page1.get(0).title()).isEqualTo("Photo 3");
        assertThat(page1.get(1).title()).isEqualTo("Photo 4");

        List<ImageView> page2 = imageOperations.findByAlbumId(album.id(), 2, 4);
        assertThat(page2).hasSize(1);
        assertThat(page2.get(0).title()).isEqualTo("Photo 5");
    }

    @Test
    @DisplayName("Updates image metadata successfully")
    void updateImageMetadata() {
        AlbumView album1 = albumOperations.create(new CreateAlbumCommand("Album 1", null));
        AlbumView album2 = albumOperations.create(new CreateAlbumCommand("Album 2", null));

        ImageView created = imageOperations.create(new CreateImageCommand(
                album1.id(), "Old Title", "photo", "original.jpg", null,
                "image/jpeg", 1000L, 800, 600,
                null, null, "Old Loc"
        ));

        ImageView updated = imageOperations.updateMetadata(new UpdateImageMetadataCommand(
                created.id(), album2.id(), "New Title", "scan", "https://cdn.example.com/new.jpg",
                "image/png", 2000L, 1024, 768, null, "New Loc"
        ));

        assertThat(updated.albumId()).isEqualTo(album2.id());
        assertThat(updated.title()).isEqualTo("New Title");
        assertThat(updated.imageType()).isEqualTo("scan");
        assertThat(updated.sourceUrl()).isEqualTo("https://cdn.example.com/new.jpg");
        assertThat(updated.mimeType()).isEqualTo("image/png");
        assertThat(updated.sizeBytes()).isEqualTo(2000L);
        assertThat(updated.widthPx()).isEqualTo(1024);
        assertThat(updated.heightPx()).isEqualTo(768);
        assertThat(updated.locationText()).isEqualTo("New Loc");
        assertThat(updated.objectKey()).isEqualTo("original.jpg"); // objectKey not mutated

        // Verify album counts updated
        assertThat(albumOperations.findById(album1.id()).imageCount()).isZero();
        assertThat(albumOperations.findById(album2.id()).imageCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Duplicate object_key throws ImageConflictException")
    void duplicateObjectKeyThrowsConflict() {
        imageOperations.create(new CreateImageCommand(
                null, "First", null, "duplicate.jpg", null,
                "image/jpeg", 100L, 100, 100, null, null, null
        ));

        assertThatThrownBy(() -> imageOperations.create(new CreateImageCommand(
                null, "Second", null, "duplicate.jpg", null,
                "image/jpeg", 200L, 200, 200, null, null, null
        )))
                .isInstanceOf(ImageConflictException.class)
                .hasMessageContaining("duplicate.jpg");
    }

    @Test
    @DisplayName("Duplicate checksum_sha256 throws ImageConflictException")
    void duplicateChecksumThrowsConflict() {
        String sha = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
        imageOperations.create(new CreateImageCommand(
                null, "First", null, "one.jpg", null,
                "image/jpeg", 100L, 100, 100, sha, null, null
        ));

        assertThatThrownBy(() -> imageOperations.create(new CreateImageCommand(
                null, "Second", null, "two.jpg", null,
                "image/jpeg", 200L, 200, 200, sha, null, null
        )))
                .isInstanceOf(ImageConflictException.class)
                .hasMessageContaining(sha);
    }

    @Test
    @DisplayName("Concurrent duplicate image creation on object_key recovers from unique constraint conflict")
    void concurrentDuplicateObjectKeyThrowsDomainConflict(CapturedOutput output) throws Exception {
        String objectKey = "concurrent/photo-PPV_PRIVATE_OBJECT_KEY_MARKER.jpg";
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, inserts image and holds uncommitted lock in PostgreSQL
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long vaultId = jdbcTemplate.queryForObject(
                        "INSERT INTO vault_entries (entry_type) VALUES ('IMAGE') RETURNING id",
                        Long.class
                );
                jdbcTemplate.update(
                        "INSERT INTO images (id, object_key, size_bytes, width_px, height_px) VALUES (?, ?, 100, 100, 100)",
                        vaultId, objectKey
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return vaultId;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Attempts to create image with the same objectKey via service.
            // Bypasses initial precheck because Thread 1 is uncommitted, then blocks on PostgreSQL unique index.
            Future<ImageView> thread2Future = executor.submit(() -> imageOperations.create(new CreateImageCommand(
                    null, "Competing", null, objectKey, null,
                    "image/jpeg", 200L, 200, 200, null, null, null
            )));

            // Observe PostgreSQL lock contention on images table
            awaitCompetingLock("images", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must receive ImageConflictException, not raw DataIntegrityViolationException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessageContaining(objectKey);

            // Verify only one image exists in PostgreSQL
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM images WHERE object_key = ?",
                    Integer.class,
                    objectKey
            );
            assertThat(count).isEqualTo(1);

            // Verify losing transaction rolled back its Vault entry: exactly 1 IMAGE entry exists
            Integer vaultCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'IMAGE'",
                    Integer.class
            );
            assertThat(vaultCount).isEqualTo(1);

            // Privacy verification: assert private marker and raw vendor detail are not logged
            assertThat(output.getAll())
                    .doesNotContain("PPV_PRIVATE_OBJECT_KEY_MARKER")
                    .doesNotContain("Detail: Key (object_key)=(");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent duplicate image creation on checksum_sha256 recovers from unique constraint conflict and rolls back vault entry")
    void concurrentDuplicateChecksumThrowsDomainConflict(CapturedOutput output) throws Exception {
        String checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, inserts image with checksum and holds uncommitted lock in PostgreSQL
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long vaultId = jdbcTemplate.queryForObject(
                        "INSERT INTO vault_entries (entry_type) VALUES ('IMAGE') RETURNING id",
                        Long.class
                );
                jdbcTemplate.update(
                        "INSERT INTO images (id, object_key, size_bytes, width_px, height_px, checksum_sha256) VALUES (?, 'thread1/photo.jpg', 100, 100, 100, ?)",
                        vaultId, checksum
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return vaultId;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Attempts to create image with different objectKey but same checksum via service.
            // Bypasses initial precheck because Thread 1 is uncommitted, then blocks on PostgreSQL unique index images_checksum_sha256_key.
            Future<ImageView> thread2Future = executor.submit(() -> imageOperations.create(new CreateImageCommand(
                    null, "Competing Checksum Image", null, "thread2/photo.jpg", null,
                    "image/jpeg", 200L, 200, 200, checksum, null, null
            )));

            // Observe PostgreSQL lock contention on images table
            awaitCompetingLock("images", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must receive ImageConflictException, not raw DataIntegrityViolationException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessageContaining(checksum);

            // Verify only one image exists in PostgreSQL with this checksum
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM images WHERE checksum_sha256 = ?",
                    Integer.class,
                    checksum
            );
            assertThat(count).isEqualTo(1);

            // Verify that the losing transaction rolled back its Vault entry: exactly 1 IMAGE vault entry exists
            Integer vaultCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'IMAGE'",
                    Integer.class
            );
            assertThat(vaultCount).isEqualTo(1);

            // Privacy verification: assert distinctive checksum and raw vendor detail are not logged
            assertThat(output.getAll())
                    .doesNotContain(checksum)
                    .doesNotContain("Detail: Key (checksum_sha256)=(");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
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
            Thread.sleep(10);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
