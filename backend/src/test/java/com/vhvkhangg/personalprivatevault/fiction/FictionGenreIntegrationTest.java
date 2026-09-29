package com.vhvkhangg.personalprivatevault.fiction;

import com.vhvkhangg.personalprivatevault.fiction.genre.FictionGenreOperations;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.UpdateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FictionGenreIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FictionGenreOperations genreOperations;

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
        jdbcTemplate.execute("DELETE FROM fiction_story_archetypes");
        jdbcTemplate.execute("DELETE FROM fiction_world_settings");
        jdbcTemplate.execute("DELETE FROM fiction_links");
        jdbcTemplate.execute("DELETE FROM fictions");
        jdbcTemplate.execute("DELETE FROM fiction_genres");
    }

    @Test
    @DisplayName("Creates and finds fiction genre by ID and by name")
    void createAndFindGenre() {
        FictionGenreView created = genreOperations.create(new CreateFictionGenreCommand(
                "Science Fiction", "Futuristic speculative stories"
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Science Fiction");
        assertThat(created.description()).isEqualTo("Futuristic speculative stories");

        Optional<FictionGenreView> foundById = genreOperations.find(created.id());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().name()).isEqualTo("Science Fiction");

        Optional<FictionGenreView> foundByName = genreOperations.findByName("science fiction");
        assertThat(foundByName).isPresent();
        assertThat(foundByName.get().id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("Exact duplicate genre name throws FictionGenreNameAlreadyExistsException")
    void exactDuplicateNameThrowsDomainException() {
        genreOperations.create(new CreateFictionGenreCommand("Fantasy", "Magic and monsters"));

        assertThatThrownBy(() -> genreOperations.create(new CreateFictionGenreCommand("Fantasy", "Duplicate")))
                .isInstanceOf(FictionGenreNameAlreadyExistsException.class)
                .hasMessageContaining("Fantasy");
    }

    @Test
    @DisplayName("Case-insensitive duplicate genre name throws FictionGenreNameAlreadyExistsException")
    void caseInsensitiveDuplicateNameThrowsDomainException() {
        genreOperations.create(new CreateFictionGenreCommand("Cyberpunk", "High tech, low life"));

        assertThatThrownBy(() -> genreOperations.create(new CreateFictionGenreCommand("cYbErPuNk", "Duplicate")))
                .isInstanceOf(FictionGenreNameAlreadyExistsException.class)
                .hasMessageContaining("cYbErPuNk");
    }

    @Test
    @DisplayName("Concurrent duplicate genre creation with deterministic uncommitted lock contention throws domain conflict exception without leaking raw error")
    void concurrentDuplicateGenreCreationRecoversFromUniqueIndexConflict() throws Exception {
        String genreNameA = "Steampunk";
        String genreNameB = "steampunk";

        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Starts independent transaction, executes insert, holds uncommitted lock in PostgreSQL
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long id = jdbcTemplate.queryForObject(
                        "INSERT INTO fiction_genres (name, description) VALUES (?, ?) RETURNING id",
                        Long.class,
                        genreNameA,
                        "Retro-futurism"
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return id;
            }));

            // Wait until thread 1 has executed insert (holding uncommitted unique index lock)
            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls genreOperations.create with same name (different case)
            // It passes application-level check (since Thread 1 is uncommitted) and blocks on PostgreSQL's unique index
            Future<FictionGenreView> thread2Future = executor.submit(() -> {
                return genreOperations.create(new CreateFictionGenreCommand(genreNameB, "Different case"));
            });

            // Observe that competing transaction has reached an ungranted PostgreSQL lock on fiction_genres
            awaitCompetingLock("fiction_genres", Duration.ofSeconds(5));

            // Now release thread 1 to commit its transaction
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must receive FictionGenreNameAlreadyExistsException, not raw DataIntegrityViolationException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(FictionGenreNameAlreadyExistsException.class)
                    .hasMessageContaining(genreNameB);

            // Verify only one case-insensitive row exists in the database
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM fiction_genres WHERE lower(name) = 'steampunk'",
                    Integer.class
            );
            assertThat(count).isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Updates genre attributes successfully")
    void updateGenreAttributes() {
        FictionGenreView created = genreOperations.create(new CreateFictionGenreCommand("Romance", "Initial"));

        FictionGenreView updated = genreOperations.update(new UpdateFictionGenreCommand(
                created.id(), "Romantic Drama", "Updated description"
        ));

        assertThat(updated.name()).isEqualTo("Romantic Drama");
        assertThat(updated.description()).isEqualTo("Updated description");

        Optional<FictionGenreView> reloaded = genreOperations.find(created.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().name()).isEqualTo("Romantic Drama");
    }

    @Test
    @DisplayName("Update genre with name conflicting with another genre throws FictionGenreNameAlreadyExistsException")
    void updateGenreDuplicateConflict() {
        genreOperations.create(new CreateFictionGenreCommand("Horror", "Spooky"));
        FictionGenreView thriller = genreOperations.create(new CreateFictionGenreCommand("Thriller", "Suspense"));

        assertThatThrownBy(() -> genreOperations.update(new UpdateFictionGenreCommand(
                thriller.id(), "horror", "Renaming to existing horror"
        )))
                .isInstanceOf(FictionGenreNameAlreadyExistsException.class)
                .hasMessageContaining("horror");
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back genre creation")
    void rollbackOfEnclosingTransactionRollsBackGenre() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            genreOperations.create(new CreateFictionGenreCommand("Doomed Genre", "Will be rolled back"));
            throw new RuntimeException("Force rollback");
        })).hasMessageContaining("Force rollback");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM fiction_genres WHERE name = 'Doomed Genre'",
                Integer.class
        );
        assertThat(count).isZero();
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
