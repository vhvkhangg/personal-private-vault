package com.vhvkhangg.personalprivatevault.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression test verifying that expected uniqueness constraint conflicts
 * do not emit raw PostgreSQL {@code Detail: Key ...} lines containing private
 * business values (object keys, checksums, category names) to application logs.
 */
@ExtendWith(OutputCaptureExtension.class)
class PrivacySafeConstraintLoggingIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ImageOperations imageOperations;

    @Autowired
    private LocationCategoryOperations categoryOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        tearDown();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM images");
        jdbcTemplate.execute("DELETE FROM location_categories");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type = 'IMAGE'");
    }

    @Test
    @DisplayName("Effective log level for org.hibernate.orm.jdbc.error is ERROR and root is INFO")
    void effectiveHibernateJdbcErrorLoggerLevelIsError() {
        Logger hibernateJdbcErrorLogger = (Logger) LoggerFactory.getLogger("org.hibernate.orm.jdbc.error");
        assertThat(hibernateJdbcErrorLogger.getEffectiveLevel()).isEqualTo(Level.ERROR);

        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        assertThat(rootLogger.getEffectiveLevel()).isEqualTo(Level.INFO);
    }

    @Test
    @DisplayName("Image object_key unique conflict does not log private marker or PostgreSQL detail across worker threads")
    void imageObjectKeyConflictDoesNotLogPrivateMarkerAcrossWorkerThreads(CapturedOutput output) throws Exception {
        String privateMarker = "PPV_PRIVATE_OBJECT_KEY_MARKER";
        String objectKey = "vault/private/photos/" + privateMarker + ".jpg";

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
                    null, "Competing Image", null, objectKey, null,
                    "image/jpeg", 200L, 200, 200, null, null, null
            )));

            // Observe PostgreSQL lock contention on images table
            awaitCompetingLock("images", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 receives ImageConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessageContaining(objectKey);

            // Database and Vault integrity: exactly 1 IMAGE entry and 1 images row exists
            Integer imageCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM images WHERE object_key = ?",
                    Integer.class,
                    objectKey
            );
            assertThat(imageCount).isEqualTo(1);

            Integer vaultCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'IMAGE'",
                    Integer.class
            );
            assertThat(vaultCount).isEqualTo(1);

            // Privacy verification: captured application logs must contain neither marker nor PostgreSQL vendor detail
            assertThat(output.getAll())
                    .doesNotContain(privateMarker)
                    .doesNotContain("Detail: Key (object_key)=(")
                    .doesNotContain("Detail: Key ");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Image checksum_sha256 unique conflict does not log private checksum or PostgreSQL detail across worker threads")
    void imageChecksumConflictDoesNotLogPrivateChecksumAcrossWorkerThreads(CapturedOutput output) throws Exception {
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
            // Bypasses initial precheck because Thread 1 is uncommitted, then blocks on PostgreSQL unique index.
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

            // Thread 2 receives ImageConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(ImageConflictException.class)
                    .hasMessageContaining(checksum);

            // Database and Vault integrity: exactly 1 IMAGE entry and 1 images row exists
            Integer imageCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM images WHERE checksum_sha256 = ?",
                    Integer.class,
                    checksum
            );
            assertThat(imageCount).isEqualTo(1);

            Integer vaultCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'IMAGE'",
                    Integer.class
            );
            assertThat(vaultCount).isEqualTo(1);

            // Privacy verification: captured application logs must contain neither checksum nor PostgreSQL vendor detail
            assertThat(output.getAll())
                    .doesNotContain(checksum)
                    .doesNotContain("Detail: Key (checksum_sha256)=(")
                    .doesNotContain("Detail: Key ");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Location category name unique conflict does not log private category name across worker threads")
    void locationCategoryNameConflictDoesNotLogPrivateCategoryName(CapturedOutput output) throws Exception {
        String privateMarker = "PPV_PRIVATE_CATEGORY_NAME_MARKER";
        String categoryName = "Bistro " + privateMarker;

        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, inserts category and holds uncommitted lock in PostgreSQL
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long catId = jdbcTemplate.queryForObject(
                        "INSERT INTO location_categories (name, description) VALUES (?, ?) RETURNING id",
                        Long.class,
                        categoryName, "French style bistro"
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return catId;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Attempts to create category with same name via service.
            // Bypasses initial precheck because Thread 1 is uncommitted, then blocks on PostgreSQL unique index.
            Future<LocationCategoryView> thread2Future = executor.submit(() -> categoryOperations.create(
                    new CreateLocationCategoryCommand(categoryName.toLowerCase(), "Competing bistro")
            ));

            // Observe PostgreSQL lock contention on location_categories table
            awaitCompetingLock("location_categories", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 receives LocationCategoryNameAlreadyExistsException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(LocationCategoryNameAlreadyExistsException.class)
                    .hasMessageContaining(categoryName.toLowerCase());

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM location_categories WHERE lower(name) = lower(?)",
                    Integer.class,
                    categoryName
            );
            assertThat(count).isEqualTo(1);

            // Privacy verification: assert private marker and raw vendor detail are not logged
            assertThat(output.getAll())
                    .doesNotContain(privateMarker)
                    .doesNotContain("Detail: Key (name)=(")
                    .doesNotContain("Detail: Key (lower(name::text))=(")
                    .doesNotContain("Detail: Key ");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Unexpected Hibernate/JPA persistence failure violates fk_images_id_vault_entries, remains observable, and logs no private values")
    void unexpectedHibernatePersistenceFailureRemainsObservableAndPrivacySafe(CapturedOutput output) {
        String privateMarker = "PPV_UNEXPECTED_FAILURE_PRIVATE_MARKER";
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Attempt an invalid insert through Hibernate/JPA that violates foreign key fk_images_id_vault_entries
        // (parent vault_entries id 999999 does not exist).
        assertThatThrownBy(() -> txTemplate.execute(status -> {
            entityManager.createNativeQuery(
                    "INSERT INTO images (id, object_key) VALUES (999999, :objectKey)"
            )
                    .setParameter("objectKey", "secret/photo-" + privateMarker + ".jpg")
                    .executeUpdate();
            return null;
        }))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(throwable -> {
                    ConstraintViolationException cve = (ConstraintViolationException) throwable;
                    assertThat(cve.getConstraintName()).isEqualTo("fk_images_id_vault_entries");
                    assertThat(cve.getSQLState()).isEqualTo("23503");
                });

        assertThat(output.getAll())
                .doesNotContain(privateMarker)
                .doesNotContain("Detail: Key (id)=(999999)")
                .doesNotContain("Detail: Key ");
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
            Thread.sleep(25);
        }
        throw new IllegalStateException("Timed out waiting for competing lock on " + tablePattern);
    }
}
