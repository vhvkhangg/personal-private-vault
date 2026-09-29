package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.exception.InvalidFilmException;
import com.vhvkhangg.personalprivatevault.film.genre.FilmGenreOperations;
import com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.view.FilmClassificationsView;
import com.vhvkhangg.personalprivatevault.film.view.FilmGenreView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;
import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
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
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilmIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FilmOperations filmOperations;

    @Autowired
    private FilmGenreOperations genreOperations;

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long genreId1;
    private Long genreId2;
    private Long directorPersonId;
    private Long storyArchetypeId1;
    private Long storyArchetypeId2;
    private Long worldSettingId1;
    private Long worldSettingId2;

    @BeforeEach
    void cleanUp() {
        tearDown();

        // Seed reference fixtures
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('US', 'United States', 'Hoa Kỳ')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('JP', 'Japan', 'Nhật Bản')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('VN', 'Vietnam', 'Việt Nam')
                ON CONFLICT (code) DO NOTHING
                """);

        storyArchetypeId1 = jdbcTemplate.queryForObject(
                "INSERT INTO story_archetypes (name, description) VALUES ('Hero Journey', 'Classic monomyth quest') RETURNING id",
                Long.class);
        storyArchetypeId2 = jdbcTemplate.queryForObject(
                "INSERT INTO story_archetypes (name, description) VALUES ('Underdog', 'Rise from struggle') RETURNING id",
                Long.class);

        worldSettingId1 = jdbcTemplate.queryForObject(
                "INSERT INTO world_settings (name, description) VALUES ('Cyberpunk Metropolis', 'High-tech neon dystopia') RETURNING id",
                Long.class);
        worldSettingId2 = jdbcTemplate.queryForObject(
                "INSERT INTO world_settings (name, description) VALUES ('Ancient Fantasy Empire', 'Swords and sorcery') RETURNING id",
                Long.class);

        FilmGenreView genre1 = genreOperations.create(new CreateFilmGenreCommand("Sci-Fi", "Science fiction"));
        FilmGenreView genre2 = genreOperations.create(new CreateFilmGenreCommand("Action", "Action films"));
        genreId1 = genre1.id();
        genreId2 = genre2.id();

        PersonView director = personOperations.create(new CreatePersonCommand(
                "Christopher Nolan", null, null, null, null, null, null, null
        ));
        directorPersonId = director.id();
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
        jdbcTemplate.execute("DELETE FROM story_archetypes");
        jdbcTemplate.execute("DELETE FROM world_settings");
        jdbcTemplate.execute("DELETE FROM person_roles");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entries");
    }

    @Test
    @DisplayName("Creates and finds film backed by FILM vault entry with all assignments")
    void createAndFindFilm() {
        FilmView created = filmOperations.create(new CreateFilmCommand(
                "Inception", "Inception Original", "US", "https://posters.example.com/inception.jpg",
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                directorPersonId, "Dream within a dream", null,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, "Watched in IMAX", "10/10 masterpiece",
                Set.of(genreId1, genreId2), Set.of(storyArchetypeId1), Set.of(worldSettingId1)
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Inception");
        assertThat(created.originalTitle()).isEqualTo("Inception Original");
        assertThat(created.nationalityCode()).isEqualTo("US");
        assertThat(created.posterUrl()).isEqualTo("https://posters.example.com/inception.jpg");
        assertThat(created.format()).isEqualTo(FilmFormat.MOVIE);
        assertThat(created.productionStyle()).isEqualTo(FilmProductionStyle.LIVE_ACTION);
        assertThat(created.isNsfw()).isFalse();
        assertThat(created.directorPersonId()).isEqualTo(directorPersonId);
        assertThat(created.totalEpisodes()).isNull();
        assertThat(created.progressStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(created.consumptionStatus()).isEqualTo(ConsumptionStatus.CONSUMED);
        assertThat(created.genreIds()).containsExactlyInAnyOrder(genreId1, genreId2);
        assertThat(created.storyArchetypeIds()).containsExactly(storyArchetypeId1);
        assertThat(created.worldSettingIds()).containsExactly(worldSettingId1);

        // Verify underlying vault entry has type FILM and matching ID
        String entryType = jdbcTemplate.queryForObject(
                "SELECT entry_type::text FROM vault_entries WHERE id = ?",
                String.class,
                created.id()
        );
        assertThat(entryType).isEqualTo("FILM");

        Optional<FilmView> reloaded = filmOperations.find(created.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().title()).isEqualTo("Inception");
        assertThat(reloaded.get().genreIds()).containsExactlyInAnyOrder(genreId1, genreId2);
        assertThat(reloaded.get().storyArchetypeIds()).containsExactly(storyArchetypeId1);
        assertThat(reloaded.get().worldSettingIds()).containsExactly(worldSettingId1);
    }

    @Test
    @DisplayName("Updates film attributes successfully")
    void updateFilmAttributes() {
        FilmView created = filmOperations.create(new CreateFilmCommand(
                "Anime Series", null, "JP", null,
                FilmFormat.SERIES, FilmProductionStyle.ANIMATION, false,
                null, "Initial description", 12,
                ProgressStatus.ONGOING, ConsumptionStatus.BEING_CONSUMED, "Ep 5", null
        ));

        FilmView updated = filmOperations.update(new UpdateFilmCommand(
                created.id(), "Anime Series (Season 1)", "Anime Original", "JP",
                "https://anime.example.com/poster.jpg",
                FilmFormat.SERIES, FilmProductionStyle.ANIMATION, false,
                null, "Updated description", 24,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, "Ep 24 completed", "Great animation"
        ));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.title()).isEqualTo("Anime Series (Season 1)");
        assertThat(updated.originalTitle()).isEqualTo("Anime Original");
        assertThat(updated.totalEpisodes()).isEqualTo(24);
        assertThat(updated.progressStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(updated.consumptionStatus()).isEqualTo(ConsumptionStatus.CONSUMED);

        Optional<FilmView> reloaded = filmOperations.find(created.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().totalEpisodes()).isEqualTo(24);
    }

    @Test
    @DisplayName("Rollback during film creation leaves zero orphan vault entries or film rows")
    void filmRollbackLeavesNoOrphanVaultEntry() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        long initialVaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'FILM'",
                Long.class
        );

        assertThatThrownBy(() -> tx.execute(status -> {
            filmOperations.create(new CreateFilmCommand(
                    "Doomed Film", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            ));
            throw new RuntimeException("Force film rollback");
        })).hasMessageContaining("Force film rollback");

        long finalVaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'FILM'",
                Long.class
        );
        long filmCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM films",
                Long.class
        );

        assertThat(finalVaultCount).isEqualTo(initialVaultCount);
        assertThat(filmCount).isZero();
    }

    @Test
    @DisplayName("Optional director validation succeeds and does NOT mutate Person roles")
    void optionalDirectorValidationDoesNotMutatePersonRoles() {
        // Assert initial person has NO roles
        Set<PersonRole> initialRoles = personOperations.getRoles(directorPersonId);
        assertThat(initialRoles).isEmpty();

        // Assign director in film creation
        FilmView film = filmOperations.create(new CreateFilmCommand(
                "Interstellar", null, "US", null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                directorPersonId, "Space exploration", null,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, null, null
        ));
        assertThat(film.directorPersonId()).isEqualTo(directorPersonId);

        // Director assignment must NOT mutate Person roles!
        Set<PersonRole> rolesAfterFilmCreate = personOperations.getRoles(directorPersonId);
        assertThat(rolesAfterFilmCreate).isEmpty();
        assertThat(rolesAfterFilmCreate).doesNotContain(PersonRole.DIRECTOR);
    }

    @Test
    @DisplayName("Rejects non-existent director person ID")
    void rejectsNonExistentDirector() {
        assertThatThrownBy(() -> filmOperations.create(new CreateFilmCommand(
                "Unknown Director Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                999999L, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFilmException.class)
                .hasMessageContaining("Director person with ID 999999 does not exist");
    }

    @Test
    @DisplayName("Enforces total_episodes bounds: null and >= 0 are valid, negative throws")
    void enforcesTotalEpisodesBounds() {
        // 0 episodes is valid
        FilmView zeroEpisodes = filmOperations.create(new CreateFilmCommand(
                "Short Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, 0,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, null, null
        ));
        assertThat(zeroEpisodes.totalEpisodes()).isZero();

        // null episodes is valid
        FilmView nullEpisodes = filmOperations.create(new CreateFilmCommand(
                "Feature Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, null, null
        ));
        assertThat(nullEpisodes.totalEpisodes()).isNull();

        // Negative episodes throws domain exception
        assertThatThrownBy(() -> filmOperations.create(new CreateFilmCommand(
                "Invalid Episodes", null, null, null,
                FilmFormat.SERIES, FilmProductionStyle.LIVE_ACTION, false,
                null, null, -5,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFilmException.class)
                .hasMessageContaining("Total episodes must be greater than or equal to 0");
    }

    @Test
    @DisplayName("Enforces nationality validation against ReferenceCatalog")
    void enforcesNationalityValidation() {
        // Valid country code (case-insensitive input normalized to uppercase)
        FilmView film = filmOperations.create(new CreateFilmCommand(
                "Vietnamese Cinema", null, "vn", null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, null, null
        ));
        assertThat(film.nationalityCode()).isEqualTo("VN");

        // Invalid country code throws
        assertThatThrownBy(() -> filmOperations.create(new CreateFilmCommand(
                "Invalid Country Film", null, "ZZ", null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, null, null
        )))
                .isInstanceOf(InvalidFilmException.class)
                .hasMessageContaining("Nationality code 'ZZ' does not exist in reference catalog");
    }

    @Test
    @DisplayName("Sequential and concurrent film genre assignments are idempotent")
    void genreAssignmentIdempotencyAndDeterministicContention() throws Exception {
        FilmView film = filmOperations.create(new CreateFilmCommand(
                "Genre Explorer", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        // Sequential duplicate additions
        filmOperations.addGenre(film.id(), genreId1);
        filmOperations.addGenre(film.id(), genreId1);
        filmOperations.addGenre(film.id(), genreId1);

        assertThat(filmOperations.getGenres(film.id()))
                .containsExactly(genreId1);

        // Deterministic concurrent database contention for genreId2
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Inserts assignment in uncommitted transaction, holding row/index lock in PostgreSQL
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO film_genre_assignments (film_id, genre_id) VALUES (?, ?)",
                        film.id(),
                        genreId2
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            // Wait until thread 1 has executed insert and holds the uncommitted primary key lock
            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls addGenre for same (film_id, genre_id)
            // It executes INSERT ... ON CONFLICT DO NOTHING and blocks waiting for Thread 1 to resolve
            Future<Void> thread2Future = executor.submit(() -> {
                filmOperations.addGenre(film.id(), genreId2);
                return null;
            });

            // Observe that competing transaction has reached an ungranted PostgreSQL lock on film_genre_assignments
            awaitCompetingLock("film_genre_assignments", Duration.ofSeconds(5));

            // Release thread 1 to commit its transaction
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            // Both threads completed with zero errors, and exactly one row exists in the database
            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM film_genre_assignments WHERE film_id = ? AND genre_id = ?",
                    Integer.class,
                    film.id(),
                    genreId2
            );
            assertThat(rowCount).isEqualTo(1);

            assertThat(filmOperations.getGenres(film.id()))
                    .containsExactlyInAnyOrder(genreId1, genreId2);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Sequential and concurrent story archetype assignments are idempotent")
    void storyArchetypeAssignmentIdempotencyAndDeterministicContention() throws Exception {
        FilmView film = filmOperations.create(new CreateFilmCommand(
                "Archetype Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        // Sequential duplicate additions
        filmOperations.addStoryArchetype(film.id(), storyArchetypeId1);
        filmOperations.addStoryArchetype(film.id(), storyArchetypeId1);

        assertThat(filmOperations.getStoryArchetypes(film.id()))
                .containsExactly(storyArchetypeId1);

        // Deterministic concurrent database contention for storyArchetypeId2
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO film_story_archetypes (film_id, story_archetype_id) VALUES (?, ?)",
                        film.id(),
                        storyArchetypeId2
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> thread2Future = executor.submit(() -> {
                filmOperations.addStoryArchetype(film.id(), storyArchetypeId2);
                return null;
            });

            awaitCompetingLock("film_story_archetypes", Duration.ofSeconds(5));
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM film_story_archetypes WHERE film_id = ? AND story_archetype_id = ?",
                    Integer.class,
                    film.id(),
                    storyArchetypeId2
            );
            assertThat(rowCount).isEqualTo(1);

            assertThat(filmOperations.getStoryArchetypes(film.id()))
                    .containsExactlyInAnyOrder(storyArchetypeId1, storyArchetypeId2);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Sequential and concurrent world setting assignments are idempotent")
    void worldSettingAssignmentIdempotencyAndDeterministicContention() throws Exception {
        FilmView film = filmOperations.create(new CreateFilmCommand(
                "World Setting Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        // Sequential duplicate additions
        filmOperations.addWorldSetting(film.id(), worldSettingId1);
        filmOperations.addWorldSetting(film.id(), worldSettingId1);

        assertThat(filmOperations.getWorldSettings(film.id()))
                .containsExactly(worldSettingId1);

        // Deterministic concurrent database contention for worldSettingId2
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO film_world_settings (film_id, world_setting_id) VALUES (?, ?)",
                        film.id(),
                        worldSettingId2
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> thread2Future = executor.submit(() -> {
                filmOperations.addWorldSetting(film.id(), worldSettingId2);
                return null;
            });

            awaitCompetingLock("film_world_settings", Duration.ofSeconds(5));
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM film_world_settings WHERE film_id = ? AND world_setting_id = ?",
                    Integer.class,
                    film.id(),
                    worldSettingId2
            );
            assertThat(rowCount).isEqualTo(1);

            assertThat(filmOperations.getWorldSettings(film.id()))
                    .containsExactlyInAnyOrder(worldSettingId1, worldSettingId2);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("getClassifications returns all assigned genres, archetypes, and world settings")
    void getClassificationsReturnsAllAssigned() {
        FilmView film = filmOperations.create(new CreateFilmCommand(
                "Full Spectrum Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null,
                Set.of(genreId1), Set.of(storyArchetypeId1), Set.of(worldSettingId1)
        ));

        FilmClassificationsView classifications = filmOperations.getClassifications(film.id());
        assertThat(classifications.filmId()).isEqualTo(film.id());
        assertThat(classifications.genreIds()).containsExactly(genreId1);
        assertThat(classifications.storyArchetypeIds()).containsExactly(storyArchetypeId1);
        assertThat(classifications.worldSettingIds()).containsExactly(worldSettingId1);
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
