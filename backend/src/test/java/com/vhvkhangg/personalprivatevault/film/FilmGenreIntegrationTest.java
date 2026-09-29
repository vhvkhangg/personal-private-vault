package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.film.genre.FilmGenreOperations;
import com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.command.UpdateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.film.view.FilmGenreView;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilmGenreIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FilmGenreOperations genreOperations;

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
        jdbcTemplate.execute("DELETE FROM film_credits");
        jdbcTemplate.execute("DELETE FROM film_links");
        jdbcTemplate.execute("DELETE FROM film_story_archetypes");
        jdbcTemplate.execute("DELETE FROM film_world_settings");
        jdbcTemplate.execute("DELETE FROM film_genre_assignments");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM film_genres");
    }

    @Test
    @DisplayName("Creates and finds film genre by ID and by name")
    void createAndFindGenre() {
        FilmGenreView created = genreOperations.create(new CreateFilmGenreCommand(
                "Action", "High-energy films"
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Action");
        assertThat(created.description()).isEqualTo("High-energy films");

        Optional<FilmGenreView> foundById = genreOperations.find(created.id());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().name()).isEqualTo("Action");

        Optional<FilmGenreView> foundByName = genreOperations.findByName("action");
        assertThat(foundByName).isPresent();
        assertThat(foundByName.get().id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("Exact duplicate genre name throws FilmGenreNameAlreadyExistsException")
    void exactDuplicateNameThrowsDomainException() {
        genreOperations.create(new CreateFilmGenreCommand("Drama", "Dramatic films"));

        assertThatThrownBy(() -> genreOperations.create(new CreateFilmGenreCommand("Drama", "Duplicate")))
                .isInstanceOf(FilmGenreNameAlreadyExistsException.class)
                .hasMessageContaining("Drama");
    }

    @Test
    @DisplayName("Case-insensitive duplicate genre name throws FilmGenreNameAlreadyExistsException")
    void caseInsensitiveDuplicateNameThrowsDomainException() {
        genreOperations.create(new CreateFilmGenreCommand("Comedy", "Funny films"));

        assertThatThrownBy(() -> genreOperations.create(new CreateFilmGenreCommand("cOmEdY", "Duplicate")))
                .isInstanceOf(FilmGenreNameAlreadyExistsException.class)
                .hasMessageContaining("cOmEdY");
    }

    @Test
    @DisplayName("Concurrent duplicate genre creation with deterministic uncommitted lock contention throws domain conflict exception without leaking raw error")
    void concurrentDuplicateGenreCreationRecoversFromUniqueIndexConflict() throws Exception {
        String genreNameA = "Thriller";
        String genreNameB = "thriller";

        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Starts independent transaction, executes insert, holds uncommitted lock in PostgreSQL
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long id = jdbcTemplate.queryForObject(
                        "INSERT INTO film_genres (name, description) VALUES (?, ?) RETURNING id",
                        Long.class,
                        genreNameA,
                        "Suspenseful movies"
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
            Future<FilmGenreView> thread2Future = executor.submit(() -> {
                return genreOperations.create(new CreateFilmGenreCommand(genreNameB, "Different case"));
            });

            // Observe that competing transaction has reached an ungranted PostgreSQL lock on film_genres
            awaitCompetingLock("film_genres", Duration.ofSeconds(5));

            // Now release thread 1 to commit its transaction
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must receive FilmGenreNameAlreadyExistsException, not raw DataIntegrityViolationException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(FilmGenreNameAlreadyExistsException.class)
                    .hasMessageContaining(genreNameB);

            // Verify only one case-insensitive row exists in the database
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM film_genres WHERE lower(name) = 'thriller'",
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
        FilmGenreView created = genreOperations.create(new CreateFilmGenreCommand("Documentary", "Initial"));

        FilmGenreView updated = genreOperations.update(new UpdateFilmGenreCommand(
                created.id(), "Nature Documentary", "Updated description"
        ));

        assertThat(updated.name()).isEqualTo("Nature Documentary");
        assertThat(updated.description()).isEqualTo("Updated description");

        Optional<FilmGenreView> reloaded = genreOperations.find(created.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().name()).isEqualTo("Nature Documentary");
    }

    @Test
    @DisplayName("Update genre with name conflicting with another genre throws FilmGenreNameAlreadyExistsException")
    void updateGenreDuplicateConflict() {
        genreOperations.create(new CreateFilmGenreCommand("Horror", "Spooky"));
        FilmGenreView mystery = genreOperations.create(new CreateFilmGenreCommand("Mystery", "Whodunit"));

        assertThatThrownBy(() -> genreOperations.update(new UpdateFilmGenreCommand(
                mystery.id(), "horror", "Renaming to existing horror"
        )))
                .isInstanceOf(FilmGenreNameAlreadyExistsException.class)
                .hasMessageContaining("horror");
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back genre creation")
    void rollbackOfEnclosingTransactionRollsBackGenre() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            genreOperations.create(new CreateFilmGenreCommand("Doomed Genre", "Will be rolled back"));
            throw new RuntimeException("Force rollback");
        })).hasMessageContaining("Force rollback");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM film_genres WHERE name = 'Doomed Genre'",
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
