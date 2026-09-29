package com.vhvkhangg.personalprivatevault.fiction;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.InvalidFictionException;
import com.vhvkhangg.personalprivatevault.fiction.genre.FictionGenreOperations;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionClassificationsView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionView;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
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
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FictionIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FictionOperations fictionOperations;

    @Autowired
    private FictionGenreOperations genreOperations;

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private CreatorGroupOperations groupOperations;

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long genreId;
    private Long authorPersonId;
    private Long authorGroupId;
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

        FictionGenreView genre = genreOperations.create(new CreateFictionGenreCommand(
                "Sci-Fi", "Science fiction"
        ));
        genreId = genre.id();

        PersonView person = personOperations.create(new CreatePersonCommand(
                "Isaac Asimov", null, null, null, null, null, "US", null
        ));
        authorPersonId = person.id();

        CreatorGroupView group = groupOperations.create(new CreateCreatorGroupCommand(
                "Studio Clamp", "Manga artist collective"
        ));
        authorGroupId = group.id();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM fiction_story_archetypes");
        jdbcTemplate.execute("DELETE FROM fiction_world_settings");
        jdbcTemplate.execute("DELETE FROM fiction_links");
        jdbcTemplate.execute("DELETE FROM fictions");
        jdbcTemplate.execute("DELETE FROM fiction_genres");
        jdbcTemplate.execute("DELETE FROM creator_group_members");
        jdbcTemplate.execute("DELETE FROM creator_groups");
        jdbcTemplate.execute("DELETE FROM person_roles");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM ratings");
        jdbcTemplate.execute("DELETE FROM vault_entries");
        jdbcTemplate.execute("DELETE FROM story_archetypes");
        jdbcTemplate.execute("DELETE FROM world_settings");
        jdbcTemplate.execute("ALTER TABLE story_archetypes ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE world_settings ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("Creates fiction work with author person sharing Vault Entry identity")
    void createsFictionWithAuthorPersonAndSharedVaultIdentity() {
        CreateFictionCommand command = new CreateFictionCommand(
                "Foundation",
                "Foundation Original",
                "US",
                "https://example.com/foundation.jpg",
                FictionFormat.NOVEL,
                false,
                genreId,
                authorPersonId,
                null,
                "The galactic empire is decaying",
                50,
                ProgressStatus.COMPLETED,
                ConsumptionStatus.CONSUMED,
                "Finished all chapters",
                "Masterpiece of science fiction",
                Set.of(storyArchetypeId1),
                Set.of(worldSettingId1)
        );

        FictionView created = fictionOperations.create(command);

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("Foundation");
        assertThat(created.originalTitle()).isEqualTo("Foundation Original");
        assertThat(created.nationalityCode()).isEqualTo("US");
        assertThat(created.posterUrl()).isEqualTo("https://example.com/foundation.jpg");
        assertThat(created.format()).isEqualTo(FictionFormat.NOVEL);
        assertThat(created.isNsfw()).isFalse();
        assertThat(created.genreId()).isEqualTo(genreId);
        assertThat(created.authorPersonId()).isEqualTo(authorPersonId);
        assertThat(created.authorGroupId()).isNull();
        assertThat(created.totalChapters()).isEqualTo(50);
        assertThat(created.progressStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(created.consumptionStatus()).isEqualTo(ConsumptionStatus.CONSUMED);
        assertThat(created.currentProgressText()).isEqualTo("Finished all chapters");
        assertThat(created.review()).isEqualTo("Masterpiece of science fiction");
        assertThat(created.storyArchetypeIds()).containsExactly(storyArchetypeId1);
        assertThat(created.worldSettingIds()).containsExactly(worldSettingId1);

        // Verify shared vault entry exists with type FICTION and same ID
        String entryType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                created.id()
        );
        assertThat(entryType).isEqualTo("FICTION");

        // Verify VaultEntryOperations sees the entry
        assertThat(vaultEntryOperations.find(created.id())).isPresent();

        // Verify row exists in fictions table
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM fictions WHERE id = ?",
                Integer.class,
                created.id()
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates fiction work with author group satisfying XOR invariant")
    void createsFictionWithAuthorGroup() {
        CreateFictionCommand command = new CreateFictionCommand(
                "Cardcaptor Sakura",
                null,
                "JP",
                null,
                FictionFormat.COMIC,
                false,
                genreId,
                null,
                authorGroupId,
                "Magical girl story",
                60,
                ProgressStatus.COMPLETED,
                ConsumptionStatus.CONSUMED,
                null,
                null
        );

        FictionView created = fictionOperations.create(command);

        assertThat(created.id()).isNotNull();
        assertThat(created.authorPersonId()).isNull();
        assertThat(created.authorGroupId()).isEqualTo(authorGroupId);

        // Verify database XOR constraint holds
        Integer validRows = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM fictions WHERE id = ? AND author_person_id IS NULL AND author_group_id IS NOT NULL",
                Integer.class,
                created.id()
        );
        assertThat(validRows).isEqualTo(1);
    }

    @Test
    @DisplayName("Failed fiction creation rolls back transaction and leaves no orphan Vault Entry")
    void failedFictionCreationLeavesNoOrphanVaultEntry() {
        long initialVaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries",
                Long.class
        );

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            fictionOperations.create(new CreateFictionCommand(
                    "Temporary Fiction", null, null, null, FictionFormat.NOVEL, false,
                    genreId, authorPersonId, null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            ));
            throw new RuntimeException("Simulated transaction failure");
        })).hasMessageContaining("Simulated transaction failure");

        long finalVaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries",
                Long.class
        );
        long finalFictionsCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM fictions",
                Long.class
        );

        assertThat(finalVaultCount).isEqualTo(initialVaultCount);
        assertThat(finalFictionsCount).isZero();
    }

    @Test
    @DisplayName("Enforces author XOR: rejects create when neither or both author fields are set")
    void enforcesAuthorXorOnCreate() {
        // Neither
        assertThatThrownBy(() -> fictionOperations.create(new CreateFictionCommand(
                "Title", null, null, null, FictionFormat.NOVEL, false,
                genreId, null, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFictionException.class)
                .hasMessageContaining("authorPersonId XOR authorGroupId");

        // Both
        assertThatThrownBy(() -> fictionOperations.create(new CreateFictionCommand(
                "Title", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, authorGroupId, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFictionException.class)
                .hasMessageContaining("authorPersonId XOR authorGroupId");
    }

    @Test
    @DisplayName("Enforces non-existent author validation")
    void enforcesNonExistentAuthorValidation() {
        assertThatThrownBy(() -> fictionOperations.create(new CreateFictionCommand(
                "Title", null, null, null, FictionFormat.NOVEL, false,
                genreId, 999999L, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFictionException.class)
                .hasMessageContaining("Author person with ID 999999 does not exist");

        assertThatThrownBy(() -> fictionOperations.create(new CreateFictionCommand(
                "Title", null, null, null, FictionFormat.NOVEL, false,
                genreId, null, 999999L, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFictionException.class)
                .hasMessageContaining("Author creator group with ID 999999 does not exist");
    }

    @Test
    @DisplayName("Enforces total_chapters nonnegative constraint")
    void enforcesChapterBounds() {
        // 0 chapters is valid
        FictionView zeroChapters = fictionOperations.create(new CreateFictionCommand(
                "One-shot Story", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, 0,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));
        assertThat(zeroChapters.totalChapters()).isZero();

        // null chapters is valid
        FictionView nullChapters = fictionOperations.create(new CreateFictionCommand(
                "Ongoing Web Novel", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));
        assertThat(nullChapters.totalChapters()).isNull();

        // Negative chapters throws domain exception
        assertThatThrownBy(() -> fictionOperations.create(new CreateFictionCommand(
                "Invalid Chapters", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, -5,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFictionException.class)
                .hasMessageContaining("Total chapters must be greater than or equal to 0");
    }

    @Test
    @DisplayName("Enforces nationality validation against ReferenceCatalog")
    void enforcesNationalityValidation() {
        // Valid country code
        FictionView fiction = fictionOperations.create(new CreateFictionCommand(
                "Vietnamese Novel", null, "vn", null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, 10,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));
        assertThat(fiction.nationalityCode()).isEqualTo("VN");

        // Invalid country code throws
        assertThatThrownBy(() -> fictionOperations.create(new CreateFictionCommand(
                "Invalid Country", null, "ZZ", null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, 10,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        )))
                .isInstanceOf(InvalidFictionException.class)
                .hasMessageContaining("Nationality code 'ZZ' does not exist in reference catalog");
    }

    @Test
    @DisplayName("Sequential and concurrent story archetype assignments are idempotent")
    void storyArchetypeAssignmentIdempotencyAndConcurrency() throws Exception {
        FictionView fiction = fictionOperations.create(new CreateFictionCommand(
                "Epic Novel", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        // Sequential duplicate additions
        fictionOperations.addStoryArchetype(fiction.id(), storyArchetypeId1);
        fictionOperations.addStoryArchetype(fiction.id(), storyArchetypeId1);
        fictionOperations.addStoryArchetype(fiction.id(), storyArchetypeId1);

        assertThat(fictionOperations.getStoryArchetypes(fiction.id()))
                .containsExactly(storyArchetypeId1);

        // Deterministic concurrent database contention for storyArchetypeId2
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Inserts classification in uncommitted transaction, holding row/index lock in PostgreSQL
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO fiction_story_archetypes (fiction_id, story_archetype_id) VALUES (?, ?)",
                        fiction.id(),
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

            // Wait until thread 1 has executed insert and holds the uncommitted primary key lock
            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls addStoryArchetype for same (fiction_id, story_archetype_id)
            // It executes INSERT ... ON CONFLICT DO NOTHING and blocks waiting for Thread 1 to resolve
            Future<Void> thread2Future = executor.submit(() -> {
                fictionOperations.addStoryArchetype(fiction.id(), storyArchetypeId2);
                return null;
            });

            // Observe that competing transaction has reached an ungranted PostgreSQL lock on fiction_story_archetypes
            awaitCompetingLock("fiction_story_archetypes", Duration.ofSeconds(5));

            // Release thread 1 to commit its transaction
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            // Both threads completed with zero errors, and exactly one row exists in the database
            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM fiction_story_archetypes WHERE fiction_id = ? AND story_archetype_id = ?",
                    Integer.class,
                    fiction.id(),
                    storyArchetypeId2
            );
            assertThat(rowCount).isEqualTo(1);

            assertThat(fictionOperations.getStoryArchetypes(fiction.id()))
                    .containsExactlyInAnyOrder(storyArchetypeId1, storyArchetypeId2);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Sequential and deterministic concurrent world setting assignments are idempotent")
    void worldSettingAssignmentIdempotencyAndConcurrency() throws Exception {
        FictionView fiction = fictionOperations.create(new CreateFictionCommand(
                "World Explorer", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        // Sequential duplicate additions
        fictionOperations.addWorldSetting(fiction.id(), worldSettingId1);
        fictionOperations.addWorldSetting(fiction.id(), worldSettingId1);

        assertThat(fictionOperations.getWorldSettings(fiction.id()))
                .containsExactly(worldSettingId1);

        // Deterministic concurrent database contention for worldSettingId2
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: Inserts classification in uncommitted transaction, holding row/index lock in PostgreSQL
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO fiction_world_settings (fiction_id, world_setting_id) VALUES (?, ?)",
                        fiction.id(),
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

            // Wait until thread 1 has executed insert and holds the uncommitted primary key lock
            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls addWorldSetting for same (fiction_id, world_setting_id)
            // It executes INSERT ... ON CONFLICT DO NOTHING and blocks waiting for Thread 1 to resolve
            Future<Void> thread2Future = executor.submit(() -> {
                fictionOperations.addWorldSetting(fiction.id(), worldSettingId2);
                return null;
            });

            // Observe that competing transaction has reached an ungranted PostgreSQL lock on fiction_world_settings
            awaitCompetingLock("fiction_world_settings", Duration.ofSeconds(5));

            // Release thread 1 to commit its transaction
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            // Both threads completed with zero errors, and exactly one row exists in the database
            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM fiction_world_settings WHERE fiction_id = ? AND world_setting_id = ?",
                    Integer.class,
                    fiction.id(),
                    worldSettingId2
            );
            assertThat(rowCount).isEqualTo(1);

            assertThat(fictionOperations.getWorldSettings(fiction.id()))
                    .containsExactlyInAnyOrder(worldSettingId1, worldSettingId2);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("getClassifications returns parent-scoped classifications view")
    void getClassificationsReturnsCompleteView() {
        FictionView fiction = fictionOperations.create(new CreateFictionCommand(
                "Classified Fiction", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        fictionOperations.addStoryArchetype(fiction.id(), storyArchetypeId1);
        fictionOperations.addWorldSetting(fiction.id(), worldSettingId1);

        FictionClassificationsView classifications = fictionOperations.getClassifications(fiction.id());
        assertThat(classifications.fictionId()).isEqualTo(fiction.id());
        assertThat(classifications.storyArchetypeIds()).containsExactly(storyArchetypeId1);
        assertThat(classifications.worldSettingIds()).containsExactly(worldSettingId1);
    }

    @Test
    @DisplayName("Updates fiction attributes and switches author from person to group")
    void updateFictionModifiesAttributesAndSwitchesAuthor() {
        FictionView created = fictionOperations.create(new CreateFictionCommand(
                "Original Title", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, "Initial desc", 20,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        UpdateFictionCommand updateCmd = new UpdateFictionCommand(
                created.id(),
                "Updated Title",
                "Native Title",
                "JP",
                "https://example.com/cover.png",
                FictionFormat.COMIC,
                true,
                genreId,
                null,
                authorGroupId,
                "Updated desc",
                25,
                ProgressStatus.COMPLETED,
                ConsumptionStatus.CONSUMED,
                "Completed reading",
                "Amazing story"
        );

        FictionView updated = fictionOperations.update(updateCmd);

        assertThat(updated.title()).isEqualTo("Updated Title");
        assertThat(updated.originalTitle()).isEqualTo("Native Title");
        assertThat(updated.nationalityCode()).isEqualTo("JP");
        assertThat(updated.posterUrl()).isEqualTo("https://example.com/cover.png");
        assertThat(updated.format()).isEqualTo(FictionFormat.COMIC);
        assertThat(updated.isNsfw()).isTrue();
        assertThat(updated.authorPersonId()).isNull();
        assertThat(updated.authorGroupId()).isEqualTo(authorGroupId);
        assertThat(updated.totalChapters()).isEqualTo(25);
        assertThat(updated.progressStatus()).isEqualTo(ProgressStatus.COMPLETED);
        assertThat(updated.consumptionStatus()).isEqualTo(ConsumptionStatus.CONSUMED);
        assertThat(updated.currentProgressText()).isEqualTo("Completed reading");
        assertThat(updated.review()).isEqualTo("Amazing story");

        // Verify vault entry remains intact
        assertThat(vaultEntryOperations.find(created.id())).isPresent();
    }

    @Test
    @DisplayName("Bounded reads: find returns empty and other operations throw for non-existent fiction")
    void boundedReadsForNonExistentFiction() {
        assertThat(fictionOperations.find(999999L)).isEmpty();

        assertThatThrownBy(() -> fictionOperations.getClassifications(999999L))
                .isInstanceOf(FictionNotFoundException.class)
                .hasMessageContaining("Fiction with ID 999999 not found");

        assertThatThrownBy(() -> fictionOperations.addStoryArchetype(999999L, storyArchetypeId1))
                .isInstanceOf(FictionNotFoundException.class);

        assertThatThrownBy(() -> fictionOperations.addWorldSetting(999999L, worldSettingId1))
                .isInstanceOf(FictionNotFoundException.class);
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back classification additions")
    void rollbackOfEnclosingTransactionRollsBackAssignments() {
        FictionView fiction = fictionOperations.create(new CreateFictionCommand(
                "Transaction Fiction", null, null, null, FictionFormat.NOVEL, false,
                genreId, authorPersonId, null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            fictionOperations.addStoryArchetype(fiction.id(), storyArchetypeId1);
            throw new RuntimeException("Force rollback of assignment");
        })).hasMessageContaining("Force rollback of assignment");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM fiction_story_archetypes WHERE fiction_id = ? AND story_archetype_id = ?",
                Integer.class,
                fiction.id(),
                storyArchetypeId1
        );
        assertThat(count).isZero();
        assertThat(fictionOperations.getStoryArchetypes(fiction.id())).isEmpty();
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
