package com.vhvkhangg.personalprivatevault.knowledge;

import com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeSrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.CreateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InvalidInformationItemException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.UpdateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.view.InformationItemView;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.CreateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.InvalidNoteException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteOperations;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.UpdateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.view.NoteView;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.CreateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.view.StudyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.CreateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.InvalidVocabularyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyOperations;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class KnowledgeIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private StudyItemOperations studyItemOperations;

    @Autowired
    private InformationItemOperations informationItemOperations;

    @Autowired
    private VocabularyOperations vocabularyOperations;

    @Autowired
    private NoteOperations noteOperations;

    @Autowired
    private KnowledgeOperations knowledgeOperations;

    @Autowired
    private ExternalAccountOperations accountOperations;

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private CreatorGroupOperations groupOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long youtubePlatformId;

    @BeforeEach
    void setUp() {
        tearDown();

        // Seed currencies
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

        // Seed languages
        jdbcTemplate.update("""
                INSERT INTO languages (code, name_en, name_vi)
                VALUES ('en', 'English', 'Tiếng Anh')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO languages (code, name_en, name_vi)
                VALUES ('ja', 'Japanese', 'Tiếng Nhật')
                ON CONFLICT (code) DO NOTHING
                """);

        // Seed platforms: YouTube
        youtubePlatformId = jdbcTemplate.query(
                "SELECT id FROM platforms WHERE name = 'YouTube'",
                (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst().orElseGet(() -> jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('YouTube', 'MEDIA'::platform_kind, 'https://youtube.com') RETURNING id",
                Long.class
        ));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM vocabulary_reviews");
        jdbcTemplate.execute("DELETE FROM vocabulary_items");
        jdbcTemplate.execute("DELETE FROM study_items");
        jdbcTemplate.execute("DELETE FROM information_items");
        jdbcTemplate.execute("DELETE FROM notes");
        jdbcTemplate.execute("DELETE FROM follower_snapshot_entries");
        jdbcTemplate.execute("DELETE FROM follower_snapshots");
        jdbcTemplate.execute("DELETE FROM external_account_relationships");
        jdbcTemplate.execute("DELETE FROM external_accounts");
        jdbcTemplate.execute("DELETE FROM creator_group_members");
        jdbcTemplate.execute("DELETE FROM creator_groups");
        jdbcTemplate.execute("DELETE FROM person_roles");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type IN ('STUDY', 'INFORMATION', 'VOCABULARY', 'NOTE', 'EXTERNAL_ACCOUNT', 'PERSON')");
    }

    // =========================================================================
    // 1. Schema Validation
    // =========================================================================
    @Nested
    @DisplayName("Schema validation tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Verifies knowledge tables exist in PostgreSQL schema")
        void verifiesKnowledgeTablesExist() {
            List<String> tables = jdbcTemplate.query(
                    """
                    SELECT table_name FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name IN (
                        'study_items', 'information_items', 'vocabulary_items',
                        'vocabulary_reviews', 'notes'
                    )
                    """,
                    (rs, rowNum) -> rs.getString("table_name")
            );

            assertThat(tables).containsExactlyInAnyOrder(
                    "study_items", "information_items", "vocabulary_items",
                    "vocabulary_reviews", "notes"
            );
        }
    }

    // =========================================================================
    // 2. Vault Entry Rollback Tests
    // =========================================================================
    @Nested
    @DisplayName("Vault entry rollback tests")
    class VaultEntryRollbackTests {

        @Test
        @DisplayName("Study creation failure rolls back Vault entry")
        void studyFailureRollsBackVaultEntry() {
            var invalidCommand = new CreateStudyItemCommand(
                    "Failing Study", null, StudyType.BOOK, null, null, null, null, null,
                    new BigDecimal("10.00"), "INVALID_CURRENCY", null, null, null,
                    StudyStatus.PLANNED, BigDecimal.ZERO, null
            );

            assertThatThrownBy(() -> studyItemOperations.create(invalidCommand))
                    .isInstanceOf(InvalidStudyItemException.class);

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vault_entries WHERE entry_type = 'STUDY'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Information creation failure rolls back Vault entry")
        void informationFailureRollsBackVaultEntry() {
            var invalidCommand = new CreateInformationItemCommand(
                    "  ", InformationType.TECHNOLOGY, null, null, null, null, null
            );

            assertThatThrownBy(() -> informationItemOperations.create(invalidCommand))
                    .isInstanceOf(InvalidInformationItemException.class);

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vault_entries WHERE entry_type = 'INFORMATION'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Vocabulary creation failure rolls back Vault entry")
        void vocabularyFailureRollsBackVaultEntry() {
            var invalidCommand = new CreateVocabularyItemCommand(
                    "Word", "nonexistent_lang", "Meaning", null, null, null, null, null, null,
                    VocabularyLearningStatus.NEW, null, 0, new BigDecimal("2.50"), 0, 0
            );

            assertThatThrownBy(() -> vocabularyOperations.create(invalidCommand))
                    .isInstanceOf(InvalidVocabularyItemException.class);

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vault_entries WHERE entry_type = 'VOCABULARY'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Note creation failure rolls back Vault entry")
        void noteFailureRollsBackVaultEntry() {
            var invalidCommand = new CreateNoteCommand(
                    "  ", "Content", null, null, null, null, null, null
            );

            assertThatThrownBy(() -> noteOperations.create(invalidCommand))
                    .isInstanceOf(InvalidNoteException.class);

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vault_entries WHERE entry_type = 'NOTE'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }
    }

    // =========================================================================
    // 3. Study Items Tests
    // =========================================================================
    @Nested
    @DisplayName("Study items integration tests")
    class StudyItemsIntegrationTests {

        @Test
        @DisplayName("Creates study items with Person XOR Group author and verifies Vault backing")
        void createWithAuthorPersonOrGroup() {
            PersonView authorPerson = personOperations.create(new CreatePersonCommand(
                    "Joshua", "Bloch", null, null, null, null, null, null
            ));
            CreatorGroupView authorGroup = groupOperations.create(new CreateCreatorGroupCommand(
                    "Clean Code Team", "Internal software craftsmanship team"
            ));

            StudyItemView personStudy = studyItemOperations.create(new CreateStudyItemCommand(
                    "Effective Java", "https://cdn.example.com/ej.jpg", StudyType.BOOK, null, null,
                    authorPerson.id(), null, LocalDate.of(2018, 1, 1), new BigDecimal("45.00"), "USD",
                    "Best practices for Java", "https://books.example.com/ej", "Essential reading",
                    StudyStatus.COMPLETED, new BigDecimal("100.00"), "Done"
            ));

            assertThat(personStudy.id()).isNotNull();
            assertThat(personStudy.authorPersonId()).isEqualTo(authorPerson.id());
            assertThat(personStudy.authorGroupId()).isNull();
            assertThat(personStudy.currencyCode()).isEqualTo("USD");

            // Verify Vault entry
            String vaultType = jdbcTemplate.queryForObject(
                    "SELECT entry_type FROM vault_entries WHERE id = ?",
                    String.class,
                    personStudy.id()
            );
            assertThat(vaultType).isEqualTo("STUDY");

            StudyItemView groupStudy = studyItemOperations.create(new CreateStudyItemCommand(
                    "Team Architecture Guide", null, StudyType.COURSE, null, null,
                    null, authorGroup.id(), null, null, null,
                    "Internal course", null, null,
                    StudyStatus.IN_PROGRESS, new BigDecimal("50.00"), "Chapter 5"
            ));

            assertThat(groupStudy.id()).isNotNull();
            assertThat(groupStudy.authorPersonId()).isNull();
            assertThat(groupStudy.authorGroupId()).isEqualTo(authorGroup.id());
        }

        @Test
        @DisplayName("Validates YouTube channel account invariants and links properly")
        void linksYoutubeChannelAccount() {
            ExternalAccountView ytAccount = accountOperations.create(new CreateExternalAccountCommand(
                    youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                    "fireship", "https://youtube.com/@fireship", "Fireship", null, null, null, null, null, null
            ));

            StudyItemView ytStudy = studyItemOperations.create(new CreateStudyItemCommand(
                    "Fireship Channel", null, StudyType.YOUTUBE_CHANNEL, null, ytAccount.id(),
                    null, null, null, null, null, null, null, null,
                    StudyStatus.IN_PROGRESS, new BigDecimal("30.00"), "Watching 100-seconds series"
            ));

            assertThat(ytStudy.id()).isNotNull();
            assertThat(ytStudy.youtubeChannelAccountId()).isEqualTo(ytAccount.id());
        }

        @Test
        @DisplayName("Rejects invalid website domain hostnames on create and update")
        void rejectsInvalidWebsiteHostnames() {
            assertThatThrownBy(() -> studyItemOperations.create(new CreateStudyItemCommand(
                    "Site", null, StudyType.WEBSITE, "https://example.com/course", null, null, null, null, null, null, null,
                    "https://example.com/course", null, StudyStatus.PLANNED, null, null
            )))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Site domain must be a valid hostname");

            assertThatThrownBy(() -> studyItemOperations.create(new CreateStudyItemCommand(
                    "Site", null, StudyType.WEBSITE, "example.com/path", null, null, null, null, null, null, null,
                    "https://example.com/path", null, StudyStatus.PLANNED, null, null
            )))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Site domain must be a valid hostname");

            var validSite = studyItemOperations.create(new CreateStudyItemCommand(
                    "Site", null, StudyType.WEBSITE, "  SPRING.IO  ", null, null, null, null, null, null, null,
                    "https://spring.io", null, StudyStatus.PLANNED, null, null
            ));
            assertThat(validSite.siteDomain()).isEqualTo("spring.io");

            assertThatThrownBy(() -> studyItemOperations.update(validSite.id(), new UpdateStudyItemCommand(
                    "Site Updated", null, StudyType.WEBSITE, "example.com:8080", null, null, null, null, null, null, null,
                    "https://spring.io", null, StudyStatus.PLANNED, null, null
            )))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Site domain must be a valid hostname");
        }

        @Test
        @DisplayName("Concurrent YouTube account uniqueness race reaches PostgreSQL unique constraint, observes lock contention, succeeds once, fails once, and logs privacy-safely")
        void concurrentYoutubeAccountRaceIsSafe(CapturedOutput output) throws Exception {
            String privateMarker = "FIRESHIP_CHANNEL_MARKER_999";
            ExternalAccountView ytAccount = accountOperations.create(new CreateExternalAccountCommand(
                    youtubePlatformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL,
                    privateMarker, "https://youtube.com/@" + privateMarker, "Fireship Test", null, null, null, null, null, null
            ));

            Long accountId = ytAccount.id();
            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, inserts study item with YouTube account and holds uncommitted lock
                Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    Long vaultId = jdbcTemplate.queryForObject(
                            "INSERT INTO vault_entries (entry_type) VALUES ('STUDY') RETURNING id",
                            Long.class
                    );
                    jdbcTemplate.update(
                            "INSERT INTO study_items (id, title, type, youtube_channel_account_id, learning_status, progress_percent) " +
                            "VALUES (?, 'Study Thread 1', 'YOUTUBE_CHANNEL'::study_type, ?, 'PLANNED'::study_status, 0)",
                            vaultId, accountId
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

                // Thread 2: Attempts to create study item with same YouTube channel account via service.
                // Pre-check passes because Thread 1 is uncommitted (READ COMMITTED), then blocks on PostgreSQL unique index study_items_youtube_channel_account_id_uq.
                Future<StudyItemView> thread2Future = executor.submit(() -> studyItemOperations.create(new CreateStudyItemCommand(
                        "Study Thread 2", null, StudyType.YOUTUBE_CHANNEL, null,
                        accountId, null, null, null, null, null, null, null, null,
                        StudyStatus.PLANNED, BigDecimal.ZERO, null
                )));

                // Observe PostgreSQL lock contention on study_items table
                awaitCompetingLock("study_items", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
                assertThat(id1).isNotNull();

                // Thread 2 must receive StudyConflictException
                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(StudyConflictException.class)
                        .hasMessageContaining("already exists for the YouTube channel account");

                // Assert exact 1 study item exists in DB for this YouTube account
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM study_items WHERE youtube_channel_account_id = ?",
                        Integer.class,
                        accountId
                );
                assertThat(count).isEqualTo(1);

                // Assert exact 1 vault entry remains (Thread 2's Vault entry rolled back)
                Integer vaultCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM vault_entries WHERE entry_type = 'STUDY'",
                        Integer.class
                );
                assertThat(vaultCount).isEqualTo(1);

                // Assert privacy-safe logging across both worker streams: private marker and Detail: Key must not appear
                assertThat(output.getAll())
                        .doesNotContain("Detail: Key")
                        .doesNotContain(privateMarker);
            } finally {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }

    // =========================================================================
    // 4. Information Items Tests
    // =========================================================================
    @Nested
    @DisplayName("Information items integration tests")
    class InformationItemsIntegrationTests {

        @Test
        @DisplayName("Creates and updates Information item without artificial uniqueness")
        void createUpdateAndAllowDuplicates() {
            var info1 = informationItemOperations.create(new CreateInformationItemCommand(
                    "PostgreSQL Locking", InformationType.TECHNOLOGY, "Use pessimistic write lock for serialization.",
                    "```sql\nSELECT * FROM tab FOR UPDATE;\n```", "Important note",
                    "https://postgresql.org/docs", "Official docs"
            ));

            assertThat(info1.id()).isNotNull();
            assertThat(info1.title()).isEqualTo("PostgreSQL Locking");

            // Allows duplicate title & type
            var info2 = informationItemOperations.create(new CreateInformationItemCommand(
                    "PostgreSQL Locking", InformationType.TECHNOLOGY, "Another article on locking.",
                    null, null, null, null
            ));
            assertThat(info2.id()).isNotNull();
            assertThat(info2.id()).isNotEqualTo(info1.id());

            // Updates info1
            var updated = informationItemOperations.update(info1.id(), new UpdateInformationItemCommand(
                    "PostgreSQL Row Locking", InformationType.TECHNOLOGY, "Updated content",
                    "New example", "Updated note", "https://new-url.com", "Updated citation"
            ));
            assertThat(updated.title()).isEqualTo("PostgreSQL Row Locking");

            Optional<InformationItemView> found = informationItemOperations.findById(info1.id());
            assertThat(found).isPresent();
            assertThat(found.get().title()).isEqualTo("PostgreSQL Row Locking");
        }
    }

    // =========================================================================
    // 5. Vocabulary Items & SRS Review Tests
    // =========================================================================
    @Nested
    @DisplayName("Vocabulary items and SRS review integration tests")
    class VocabularyIntegrationTests {

        @Test
        @DisplayName("Allows duplicate words and validates language code")
        void allowsDuplicateWordsAndValidatesLanguage() {
            var vocab1 = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "bank", "en", "Financial institution", null, "I deposited money in the bank.",
                    null, null, null, null, VocabularyLearningStatus.NEW, null, 0,
                    new BigDecimal("2.50"), 0, 0
            ));
            var vocab2 = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "bank", "en", "River bank", null, "He sat on the grassy bank.",
                    null, null, null, null, VocabularyLearningStatus.NEW, null, 0,
                    new BigDecimal("2.50"), 0, 0
            ));

            assertThat(vocab1.id()).isNotNull();
            assertThat(vocab2.id()).isNotNull();
            assertThat(vocab1.id()).isNotEqualTo(vocab2.id());

            // Fails on non-existent language
            assertThatThrownBy(() -> vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "test", "xx", "meaning", null, null, null, null, null, null, null, null, null, null, null, null
            ))).isInstanceOf(InvalidVocabularyItemException.class)
              .hasMessageContaining("does not exist in reference catalog");
        }

        @Test
        @DisplayName("Performs explicit atomic SRS review transition and due query")
        void explicitAtomicReviewTransitionAndDueQuery() {
            var item = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "ephemeral", "en", "Lasting for a very short time", null, "Ephemeral pleasures.",
                    null, null, null, null, VocabularyLearningStatus.NEW, null, 0,
                    new BigDecimal("2.50"), 0, 0
            ));

            Instant reviewTime = Instant.now().minus(Duration.ofHours(1));
            Instant nextReviewTime = reviewTime.plus(Duration.ofDays(1));

            var transitionCmd = new VocabularyReviewTransitionCommand(
                    SrsReviewResponse.GOOD,
                    VocabularyLearningStatus.REVIEW,
                    nextReviewTime,
                    1,
                    new BigDecimal("2.60"),
                    1,
                    0,
                    reviewTime
            );

            VocabularyReviewTransitionResultView result = vocabularyOperations.reviewTransition(item.id(), transitionCmd);
            assertThat(result.item().learningStatus()).isEqualTo(VocabularyLearningStatus.REVIEW);
            assertThat(result.item().intervalDays()).isEqualTo(1);
            assertThat(result.item().easeFactor()).isEqualByComparingTo(new BigDecimal("2.60"));
            assertThat(result.item().repetitionCount()).isEqualTo(1);
            assertThat(result.item().lapseCount()).isEqualTo(0);

            assertThat(result.review().response()).isEqualTo(SrsReviewResponse.GOOD);
            assertThat(result.review().previousIntervalDays()).isEqualTo(0);
            assertThat(result.review().previousEaseFactor()).isEqualByComparingTo(new BigDecimal("2.50"));
            assertThat(result.review().newIntervalDays()).isEqualTo(1);
            assertThat(result.review().newEaseFactor()).isEqualByComparingTo(new BigDecimal("2.60"));

            // Check due items
            List<VocabularyItemView> dueItems = vocabularyOperations.findDue(
                    Instant.now().plus(Duration.ofDays(2)),
                    10
            );
            assertThat(dueItems).hasSize(1);
            assertThat(dueItems.get(0).id()).isEqualTo(item.id());

            // Check review history
            List<VocabularyReviewView> history = vocabularyOperations.findReviewsByVocabularyId(item.id(), 10);
            assertThat(history).hasSize(1);
            assertThat(history.get(0).response()).isEqualTo(SrsReviewResponse.GOOD);
        }

        @Test
        @DisplayName("Rejects ease factor boundaries (rounds to zero, overflow) on create and review without database constraint error")
        void rejectsEaseFactorBoundariesAgainstPostgres() {
            // Rounds to zero on create
            assertThatThrownBy(() -> vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "word", "en", "meaning", null, null, null, null, null, null,
                    null, null, 0, new BigDecimal("0.001"), 0, 0
            )))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Ease factor must be greater than zero");

            // Exceeds numeric(5,2) max on create
            assertThatThrownBy(() -> vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "word", "en", "meaning", null, null, null, null, null, null,
                    null, null, 0, new BigDecimal("1000.00"), 0, 0
            )))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Ease factor must not exceed 999.99");

            // Create valid item
            var item = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "word", "en", "meaning", null, null, null, null, null, null,
                    null, null, 0, new BigDecimal("2.50"), 0, 0
            ));

            // Rounds to zero on review transition
            assertThatThrownBy(() -> vocabularyOperations.reviewTransition(item.id(), new VocabularyReviewTransitionCommand(
                    SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now().plus(Duration.ofDays(1)),
                    1, new BigDecimal("0.001"), 1, 0, Instant.now()
            )))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New ease factor must be greater than zero");

            // Exceeds numeric(5,2) max on review transition
            assertThatThrownBy(() -> vocabularyOperations.reviewTransition(item.id(), new VocabularyReviewTransitionCommand(
                    SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now().plus(Duration.ofDays(1)),
                    1, new BigDecimal("1000.00"), 1, 0, Instant.now()
            )))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New ease factor must not exceed 999.99");
        }

        @Test
        @DisplayName("Concurrent review transition contention observes row-lock contention and serializes state chaining safely")
        void concurrentReviewContentionSerializes() throws Exception {
            var item = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "tenacious", "en", "Tending to keep a firm hold", null, null,
                    null, null, null, null, VocabularyLearningStatus.LEARNING, null, 1,
                    new BigDecimal("2.50"), 1, 0
            ));

            Long itemId = item.id();
            CountDownLatch thread1Locked = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, acquires row lock on vocabulary item (SELECT ... FOR UPDATE)
                Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    jdbcTemplate.queryForObject(
                            "SELECT id FROM vocabulary_items WHERE id = ? FOR UPDATE",
                            Long.class,
                            itemId
                    );
                    thread1Locked.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    // Apply transition 1 inside Thread 1's transaction
                    Instant reviewedAt1 = Instant.now().minus(Duration.ofMinutes(1));
                    Instant nextReviewAt1 = reviewedAt1.plus(Duration.ofDays(3));
                    jdbcTemplate.update(
                            "INSERT INTO vocabulary_reviews (vocabulary_id, response, reviewed_at, previous_interval_days, new_interval_days, previous_ease_factor, new_ease_factor, next_review_at) " +
                            "VALUES (?, 'HARD'::srs_review_response, ?, 1, 3, 2.50, 2.35, ?)",
                            itemId, Timestamp.from(reviewedAt1), Timestamp.from(nextReviewAt1)
                    );
                    jdbcTemplate.update(
                            "UPDATE vocabulary_items SET interval_days = 3, ease_factor = 2.35, repetition_count = 2, lapse_count = 0, " +
                            "learning_status = 'REVIEW'::vocabulary_learning_status, next_review_at = ? WHERE id = ?",
                            Timestamp.from(nextReviewAt1), itemId
                    );
                    return null;
                }));

                assertThat(thread1Locked.await(5, TimeUnit.SECONDS)).isTrue();

                // Thread 2: Calls vocabularyOperations.reviewTransition via service.
                // It issues findByIdForUpdate (SELECT ... FOR UPDATE), which blocks in PostgreSQL on Thread 1's row lock!
                Instant reviewTime2 = Instant.now();
                Future<VocabularyReviewTransitionResultView> thread2Future = executor.submit(() -> vocabularyOperations.reviewTransition(
                        itemId,
                        new VocabularyReviewTransitionCommand(
                                SrsReviewResponse.GOOD,
                                VocabularyLearningStatus.REVIEW,
                                reviewTime2.plus(Duration.ofDays(6)),
                                6,
                                new BigDecimal("2.60"),
                                3,
                                0,
                                reviewTime2
                        )
                ));

                // Observe PostgreSQL row-lock contention on vocabulary_items table
                awaitCompetingLock("vocabulary_items", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                thread1Future.get(10, TimeUnit.SECONDS);
                VocabularyReviewTransitionResultView res2 = thread2Future.get(10, TimeUnit.SECONDS);

                assertThat(res2).isNotNull();

                // Verify review 2 observed Thread 1's committed state as its previous_* values
                assertThat(res2.review().previousIntervalDays()).isEqualTo(3);
                assertThat(res2.review().previousEaseFactor()).isEqualByComparingTo(new BigDecimal("2.35"));
                assertThat(res2.review().newIntervalDays()).isEqualTo(6);
                assertThat(res2.review().newEaseFactor()).isEqualByComparingTo(new BigDecimal("2.60"));

                // Verify both reviews recorded in DB, ordered by reviewed_at DESC
                List<VocabularyReviewView> history = vocabularyOperations.findReviewsByVocabularyId(itemId, 10);
                assertThat(history).hasSize(2);

                // History[0] is Thread 2's review (most recent)
                VocabularyReviewView latestReview = history.get(0);
                assertThat(latestReview.response()).isEqualTo(SrsReviewResponse.GOOD);
                assertThat(latestReview.previousIntervalDays()).isEqualTo(3);
                assertThat(latestReview.previousEaseFactor()).isEqualByComparingTo(new BigDecimal("2.35"));
                assertThat(latestReview.newIntervalDays()).isEqualTo(6);
                assertThat(latestReview.newEaseFactor()).isEqualByComparingTo(new BigDecimal("2.60"));

                // History[1] is Thread 1's review
                VocabularyReviewView firstReview = history.get(1);
                assertThat(firstReview.response()).isEqualTo(SrsReviewResponse.HARD);
                assertThat(firstReview.previousIntervalDays()).isEqualTo(1);
                assertThat(firstReview.previousEaseFactor()).isEqualByComparingTo(new BigDecimal("2.50"));
                assertThat(firstReview.newIntervalDays()).isEqualTo(3);
                assertThat(firstReview.newEaseFactor()).isEqualByComparingTo(new BigDecimal("2.35"));

                // Serial chaining assertion: latest review's previous state matches exactly first review's new state!
                assertThat(latestReview.previousIntervalDays()).isEqualTo(firstReview.newIntervalDays());
                assertThat(latestReview.previousEaseFactor()).isEqualByComparingTo(firstReview.newEaseFactor());

                // Verify final state in vocabulary_items matches Thread 2's transition
                VocabularyItemView finalItem = vocabularyOperations.findById(itemId).orElseThrow();
                assertThat(finalItem.intervalDays()).isEqualTo(6);
                assertThat(finalItem.easeFactor()).isEqualByComparingTo(new BigDecimal("2.60"));
                assertThat(finalItem.repetitionCount()).isEqualTo(3);
                assertThat(finalItem.lapseCount()).isEqualTo(0);
                assertThat(finalItem.learningStatus()).isEqualTo(VocabularyLearningStatus.REVIEW);
            } finally {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("reviewTransition refreshes stale preloaded entity state under row lock to prevent stale history")
        void reviewTransitionRefreshesStalePreloadedStateUnderLock() throws Exception {
            var item = knowledgeOperations.createVocabularyItem(new CreateKnowledgeVocabularyItemCommand(
                    "stale-check", "en", "Testing L1 refresh", null, null,
                    null, null, null, null, KnowledgeVocabularyLearningStatus.LEARNING, null, 0,
                    new BigDecimal("2.50"), 0, 0
            ));
            Long itemId = item.id();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            CountDownLatch txAPreloaded = new CountDownLatch(1);
            CountDownLatch txBCommitted = new CountDownLatch(1);

            try {
                Future<KnowledgeVocabularyReviewTransitionResultView> txAFuture = executor.submit(() -> txTemplate.execute(status -> {
                    // 1. Preload item into Tx A's L1 persistence context via parent Knowledge API
                    var preloaded = knowledgeOperations.findVocabularyItemById(itemId);
                    assertThat(preloaded).isPresent();
                    assertThat(preloaded.get().intervalDays()).isEqualTo(0);
                    assertThat(preloaded.get().easeFactor()).isEqualByComparingTo(new BigDecimal("2.50"));

                    txAPreloaded.countDown();

                    // Wait for Tx B to commit its updates
                    try {
                        boolean awaited = txBCommitted.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    // 3. Tx A invokes reviewTransition via parent Knowledge API while holding preloaded entity in L1 cache
                    Instant reviewTimeA = Instant.now();
                    return knowledgeOperations.reviewVocabularyItem(itemId, new KnowledgeVocabularyReviewTransitionCommand(
                            KnowledgeSrsReviewResponse.GOOD,
                            KnowledgeVocabularyLearningStatus.REVIEW,
                            reviewTimeA.plus(Duration.ofDays(20)),
                            20,
                            new BigDecimal("2.90"),
                            2,
                            0,
                            reviewTimeA
                    ));
                }));

                assertThat(txAPreloaded.await(5, TimeUnit.SECONDS)).isTrue();

                // 2. Tx B runs in its own transaction, reviews item to interval 10, ease 2.70 via parent Knowledge API, and commits
                Future<KnowledgeVocabularyReviewTransitionResultView> txBFuture = executor.submit(() -> txTemplate.execute(status -> {
                    Instant reviewTimeB = Instant.now().minus(Duration.ofMinutes(1));
                    return knowledgeOperations.reviewVocabularyItem(itemId, new KnowledgeVocabularyReviewTransitionCommand(
                            KnowledgeSrsReviewResponse.GOOD,
                            KnowledgeVocabularyLearningStatus.REVIEW,
                            reviewTimeB.plus(Duration.ofDays(10)),
                            10,
                            new BigDecimal("2.70"),
                            1,
                            0,
                            reviewTimeB
                    ));
                }));

                KnowledgeVocabularyReviewTransitionResultView resB = txBFuture.get(5, TimeUnit.SECONDS);
                assertThat(resB).isNotNull();
                assertThat(resB.review().previousIntervalDays()).isEqualTo(0);
                assertThat(resB.review().newIntervalDays()).isEqualTo(10);
                assertThat(resB.review().newEaseFactor()).isEqualByComparingTo(new BigDecimal("2.70"));

                // Signal Tx A that Tx B is committed
                txBCommitted.countDown();

                KnowledgeVocabularyReviewTransitionResultView resA = txAFuture.get(5, TimeUnit.SECONDS);
                assertThat(resA).isNotNull();

                // Verify Tx A observed Tx B's committed state as previous_* rather than stale L1 values
                assertThat(resA.review().previousIntervalDays())
                        .as("Tx A must observe refreshed previousIntervalDays = 10 from DB under lock, not stale 0 from L1 cache")
                        .isEqualTo(10);
                assertThat(resA.review().previousEaseFactor())
                        .as("Tx A must observe refreshed previousEaseFactor = 2.70 from DB under lock, not stale 2.50 from L1 cache")
                        .isEqualByComparingTo(new BigDecimal("2.70"));
                assertThat(resA.review().newIntervalDays()).isEqualTo(20);
                assertThat(resA.review().newEaseFactor()).isEqualByComparingTo(new BigDecimal("2.90"));

                // Verify history chain reflects B then A with contiguous transitions via parent Knowledge API
                List<KnowledgeVocabularyReviewView> history = knowledgeOperations.findVocabularyReviews(itemId, 10);
                assertThat(history).hasSize(2);

                // History is ordered by reviewed_at DESC (resA is newest)
                KnowledgeVocabularyReviewView reviewA = history.get(0);
                KnowledgeVocabularyReviewView reviewB = history.get(1);

                assertThat(reviewA.previousIntervalDays()).isEqualTo(reviewB.newIntervalDays());
                assertThat(reviewA.previousEaseFactor()).isEqualByComparingTo(reviewB.newEaseFactor());

                // Verify final state in DB via parent Knowledge API
                KnowledgeVocabularyItemView finalItem = knowledgeOperations.findVocabularyItemById(itemId).orElseThrow();
                assertThat(finalItem.intervalDays()).isEqualTo(20);
                assertThat(finalItem.easeFactor()).isEqualByComparingTo(new BigDecimal("2.90"));
                assertThat(finalItem.repetitionCount()).isEqualTo(2);
            } finally {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("findDue query tests full matrix: inclusive cutoff, null-time NEW, excludes other null-time states and MASTERED, deterministic ordering, and limit")
        void dueVocabularyQueryMatrixAndOrdering() {
            Instant cutoff = Instant.parse("2026-09-30T12:00:00Z");

            // 1. Scheduled before cutoff (LEARNING) -> DUE
            var pastScheduled = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "past", "en", "Past scheduled", null, null, null, null, null, null,
                    VocabularyLearningStatus.LEARNING, cutoff.minus(Duration.ofHours(2)), 1, new BigDecimal("2.50"), 1, 0
            ));

            // 2. Scheduled EXACTLY AT cutoff (REVIEW, tie candidate 1) -> DUE (inclusive cutoff boundary!)
            var exactCutoff1 = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "exact1", "en", "Exact cutoff 1", null, null, null, null, null, null,
                    VocabularyLearningStatus.REVIEW, cutoff, 2, new BigDecimal("2.50"), 2, 0
            ));

            // 3. Scheduled EXACTLY AT cutoff (REVIEW, tie candidate 2) -> DUE (inclusive cutoff boundary!)
            var exactCutoff2 = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "exact2", "en", "Exact cutoff 2", null, null, null, null, null, null,
                    VocabularyLearningStatus.REVIEW, cutoff, 2, new BigDecimal("2.50"), 2, 0
            ));

            // 4. Scheduled AFTER cutoff (REVIEW) -> NOT DUE
            vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "future", "en", "Future scheduled", null, null, null, null, null, null,
                    VocabularyLearningStatus.REVIEW, cutoff.plus(Duration.ofSeconds(1)), 3, new BigDecimal("2.50"), 3, 0
            ));

            // 5. Unscheduled NEW (next_review_at is null) -> DUE (unscheduled NEW)
            var unscheduledNew1 = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "new1", "en", "Unscheduled new 1", null, null, null, null, null, null,
                    VocabularyLearningStatus.NEW, null, 0, new BigDecimal("2.50"), 0, 0
            ));

            // 6. Unscheduled NEW (second item, for tie-break ordering) -> DUE
            var unscheduledNew2 = vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "new2", "en", "Unscheduled new 2", null, null, null, null, null, null,
                    VocabularyLearningStatus.NEW, null, 0, new BigDecimal("2.50"), 0, 0
            ));

            // 7. Unscheduled LEARNING (next_review_at is null) -> NOT DUE
            vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "unscheduled-learning", "en", "Unscheduled learning", null, null, null, null, null, null,
                    VocabularyLearningStatus.LEARNING, null, 1, new BigDecimal("2.50"), 1, 0
            ));

            // 8. Unscheduled REVIEW (next_review_at is null) -> NOT DUE
            vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "unscheduled-review", "en", "Unscheduled review", null, null, null, null, null, null,
                    VocabularyLearningStatus.REVIEW, null, 2, new BigDecimal("2.50"), 2, 0
            ));

            // 9. Mastered with past next_review_at -> NOT DUE (MASTERED is never due!)
            vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "mastered-past", "en", "Mastered past", null, null, null, null, null, null,
                    VocabularyLearningStatus.MASTERED, cutoff.minus(Duration.ofDays(1)), 10, new BigDecimal("2.50"), 10, 0
            ));

            // 10. Mastered with null next_review_at -> NOT DUE
            vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "mastered-null", "en", "Mastered null", null, null, null, null, null, null,
                    VocabularyLearningStatus.MASTERED, null, 10, new BigDecimal("2.50"), 10, 0
            ));

            // 11. NEW with future next_review_at -> NOT DUE
            vocabularyOperations.create(new CreateVocabularyItemCommand(
                    "new-future", "en", "New future", null, null, null, null, null, null,
                    VocabularyLearningStatus.NEW, cutoff.plus(Duration.ofDays(1)), 0, new BigDecimal("2.50"), 0, 0
            ));

            // Query with large limit: should return exactly 5 due items in exact order:
            // 1. pastScheduled (oldest scheduled time)
            // 2. exactCutoff1 (exact cutoff, smaller ID)
            // 3. exactCutoff2 (exact cutoff, larger ID)
            // 4. unscheduledNew1 (null schedule, smaller ID)
            // 5. unscheduledNew2 (null schedule, larger ID)
            List<VocabularyItemView> dueItems = vocabularyOperations.findDue(cutoff, 100);
            assertThat(dueItems).hasSize(5);

            assertThat(dueItems.get(0).id()).isEqualTo(pastScheduled.id());
            assertThat(dueItems.get(1).id()).isEqualTo(exactCutoff1.id());
            assertThat(dueItems.get(2).id()).isEqualTo(exactCutoff2.id());
            assertThat(dueItems.get(3).id()).isEqualTo(unscheduledNew1.id());
            assertThat(dueItems.get(4).id()).isEqualTo(unscheduledNew2.id());

            // Test limit constraint
            List<VocabularyItemView> limited = vocabularyOperations.findDue(cutoff, 2);
            assertThat(limited).hasSize(2);
            assertThat(limited.get(0).id()).isEqualTo(pastScheduled.id());
            assertThat(limited.get(1).id()).isEqualTo(exactCutoff1.id());

            // Test parameter validation
            assertThatThrownBy(() -> vocabularyOperations.findDue(null, 10))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Cutoff must not be null");

            assertThatThrownBy(() -> vocabularyOperations.findDue(cutoff, 0))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Limit must be positive");

            assertThatThrownBy(() -> vocabularyOperations.findDue(cutoff, -1))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Limit must be positive");
        }
    }

    // =========================================================================
    // 6. Note Integration Tests
    // =========================================================================
    @Nested
    @DisplayName("Note integration tests")
    class NoteIntegrationTests {

        @Test
        @DisplayName("Preserves Obsidian markdown syntax and arbitrary JSONB frontmatter round-trip")
        void preservesMarkdownAndJsonbFrontmatter() {
            String obsidianMarkdown = """
                    # Obsidian Vault Note
                    Links: [[Another Note]], [[Nested/Path/Doc|Custom Label]]
                    Tags: #vault #knowledge/obsidian
                    Code block:
                    ```java
                    public static void main(String[] args) {
                        System.out.println("Hello, Vault!");
                    }
                    ```
                    Latex: $$E = mc^2$$
                    Callout:
                    > [!NOTE]
                    > Obsidian callouts must remain untouched.
                    """;

            Map<String, Object> frontmatter = Map.of(
                    "title", "Obsidian Note",
                    "tags", List.of("vault", "knowledge"),
                    "aliases", List.of("Alias 1", "Alias 2"),
                    "publish", true,
                    "score", 42,
                    "metadata", Map.of("author", "Vault Owner", "reviewed", false)
            );

            NoteView created = noteOperations.create(new CreateNoteCommand(
                    "Obsidian Syntax Test",
                    obsidianMarkdown,
                    null,
                    "Personal Vault",
                    "vault://notes/obsidian.md",
                    "obsidian.md",
                    "hash_test_12345",
                    frontmatter
            ));

            assertThat(created.id()).isNotNull();
            assertThat(created.contentMarkdown()).isEqualTo(obsidianMarkdown);
            assertThat(created.frontmatter()).isEqualTo(frontmatter);
            assertThat(created.importedFileHash()).isEqualTo("hash_test_12345");

            // Verify database content directly
            String dbContent = jdbcTemplate.queryForObject(
                    "SELECT content_markdown FROM notes WHERE id = ?",
                    String.class,
                    created.id()
            );
            assertThat(dbContent).isEqualTo(obsidianMarkdown);

            // Update content and frontmatter
            Map<String, Object> updatedFrontmatter = Map.of("version", 2, "active", true);
            NoteView updated = noteOperations.update(created.id(), new UpdateNoteCommand(
                    "Obsidian Syntax Test (Updated)",
                    obsidianMarkdown + "\nAppended line.",
                    null,
                    "Personal Vault",
                    "vault://notes/obsidian.md",
                    "obsidian.md",
                    "hash_test_67890",
                    updatedFrontmatter
            ));

            assertThat(updated.title()).isEqualTo("Obsidian Syntax Test (Updated)");
            assertThat(updated.contentMarkdown()).contains("Appended line.");
            assertThat(updated.frontmatter()).isEqualTo(updatedFrontmatter);
        }

        @Test
        @DisplayName("Concurrent imported_file_hash race reaches PostgreSQL unique constraint, observes lock contention, succeeds once, fails once, and logs privacy-safely")
        void concurrentImportedFileHashRaceIsSafe(CapturedOutput output) throws Exception {
            String privateFileHash = "SHA256_PRIVATE_HASH_MARKER_99999999";

            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, inserts note with private hash and holds uncommitted lock
                Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    Long vaultId = jdbcTemplate.queryForObject(
                            "INSERT INTO vault_entries (entry_type) VALUES ('NOTE') RETURNING id",
                            Long.class
                    );
                    jdbcTemplate.update(
                            "INSERT INTO notes (id, title, content_markdown, imported_file_hash) " +
                            "VALUES (?, 'Note Thread 1', 'Content 1', ?)",
                            vaultId, privateFileHash
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

                // Thread 2: Attempts to create note with same imported_file_hash via service.
                // Pre-check passes because Thread 1 is uncommitted (READ COMMITTED), then blocks on PostgreSQL unique index notes_imported_hash_uq.
                Future<NoteView> thread2Future = executor.submit(() -> noteOperations.create(new CreateNoteCommand(
                        "Note Thread 2", "Content 2", null, null, null,
                        "thread2.md", privateFileHash, Map.of("key", "value")
                )));

                // Observe PostgreSQL lock contention on notes table
                awaitCompetingLock("notes", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
                assertThat(id1).isNotNull();

                // Thread 2 must receive NoteConflictException
                assertThatThrownBy(() -> {
                    try {
                        thread2Future.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                })
                        .isInstanceOf(NoteConflictException.class)
                        .hasMessageContaining("already exists");

                // Assert exact 1 note exists in DB with this hash
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM notes WHERE imported_file_hash = ?",
                        Integer.class,
                        privateFileHash
                );
                assertThat(count).isEqualTo(1);

                // Assert exact 1 vault entry remains (Thread 2's Vault entry rolled back)
                Integer vaultCount = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM vault_entries WHERE entry_type = 'NOTE'",
                        Integer.class
                );
                assertThat(vaultCount).isEqualTo(1);

                // Assert privacy-safe logging across both worker streams: private hash and Detail: Key must not appear
                assertThat(output.getAll())
                        .doesNotContain("Detail: Key")
                        .doesNotContain(privateFileHash);
            } finally {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("Caller input map and collection mutations do not mutate managed or persisted note frontmatter")
        void callerInputMutationDoesNotAffectManagedOrPersistedNoteState() {
            Map<String, Object> nestedMap = new HashMap<>();
            nestedMap.put("childKey", "childValue");
            List<Object> nestedList = new ArrayList<>(List.of("element1", "element2"));

            Map<String, Object> callerFrontmatter = new HashMap<>();
            callerFrontmatter.put("rootKey", "initialRoot");
            callerFrontmatter.put("nestedMap", nestedMap);
            callerFrontmatter.put("nestedList", nestedList);

            NoteView created = noteOperations.create(new CreateNoteCommand(
                    "Isolation Test", "content", null, null, null, null, null, callerFrontmatter
            ));

            // Mutate caller input structures
            callerFrontmatter.put("rootKey", "mutatedRoot");
            callerFrontmatter.put("newKey", "injectedValue");
            nestedMap.put("childKey", "mutatedChildValue");
            nestedList.add("injectedElement");

            // Assert returned view is unchanged
            assertThat(created.frontmatter().get("rootKey")).isEqualTo("initialRoot");
            assertThat(created.frontmatter()).doesNotContainKey("newKey");
            @SuppressWarnings("unchecked")
            Map<String, Object> createdNestedMap = (Map<String, Object>) created.frontmatter().get("nestedMap");
            assertThat(createdNestedMap.get("childKey")).isEqualTo("childValue");
            @SuppressWarnings("unchecked")
            List<Object> createdNestedList = (List<Object>) created.frontmatter().get("nestedList");
            assertThat(createdNestedList).containsExactly("element1", "element2");

            // Assert fresh database reload is unchanged
            NoteView reloaded = noteOperations.findById(created.id()).orElseThrow();
            assertThat(reloaded.frontmatter().get("rootKey")).isEqualTo("initialRoot");
            assertThat(reloaded.frontmatter()).doesNotContainKey("newKey");
            @SuppressWarnings("unchecked")
            Map<String, Object> reloadedNestedMap = (Map<String, Object>) reloaded.frontmatter().get("nestedMap");
            assertThat(reloadedNestedMap.get("childKey")).isEqualTo("childValue");
            @SuppressWarnings("unchecked")
            List<Object> reloadedNestedList = (List<Object>) reloaded.frontmatter().get("nestedList");
            assertThat(reloadedNestedList).containsExactly("element1", "element2");

            // Do the same for update
            Map<String, Object> updateNestedMap = new HashMap<>();
            updateNestedMap.put("updChild", "updVal");
            List<Object> updateNestedList = new ArrayList<>(List.of("u1"));
            Map<String, Object> updateFrontmatter = new HashMap<>();
            updateFrontmatter.put("updRoot", "initialUpdRoot");
            updateFrontmatter.put("nestedMap", updateNestedMap);
            updateFrontmatter.put("nestedList", updateNestedList);

            NoteView updated = noteOperations.update(created.id(), new UpdateNoteCommand(
                    "Isolation Test Updated", "content updated", null, null, null, null, null, updateFrontmatter
            ));

            // Mutate update input structures
            updateFrontmatter.put("updRoot", "mutatedUpdRoot");
            updateNestedMap.put("updChild", "mutatedUpdVal");
            updateNestedList.add("u2");

            assertThat(updated.frontmatter().get("updRoot")).isEqualTo("initialUpdRoot");
            @SuppressWarnings("unchecked")
            Map<String, Object> updatedNestedMap = (Map<String, Object>) updated.frontmatter().get("nestedMap");
            assertThat(updatedNestedMap.get("updChild")).isEqualTo("updVal");
            @SuppressWarnings("unchecked")
            List<Object> updatedNestedList = (List<Object>) updated.frontmatter().get("nestedList");
            assertThat(updatedNestedList).containsExactly("u1");

            NoteView reloadedUpdated = noteOperations.findById(created.id()).orElseThrow();
            assertThat(reloadedUpdated.frontmatter().get("updRoot")).isEqualTo("initialUpdRoot");
        }

        @Test
        @DisplayName("NoteOperations read view is unmodifiable and prevents dirty checking mutation in active transaction")
        void nestedApiReadIsolationPreventsUnintendedManagedStateMutation() {
            Map<String, Object> initialFrontmatter = new HashMap<>();
            initialFrontmatter.put("title", "Immutable Test");
            initialFrontmatter.put("tags", new ArrayList<>(List.of("tag1", "tag2")));
            initialFrontmatter.put("meta", new HashMap<>(Map.of("k", "v")));

            NoteView created = noteOperations.create(new CreateNoteCommand(
                    "Read Isolation Test", "content", null, null, null, null, null, initialFrontmatter
            ));
            Long noteId = created.id();

            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            txTemplate.executeWithoutResult(status -> {
                NoteView view = noteOperations.findById(noteId).orElseThrow();
                Map<String, Object> fm = view.frontmatter();

                assertThatThrownBy(() -> fm.put("title", "Hacked Title"))
                        .isInstanceOf(UnsupportedOperationException.class);
                assertThatThrownBy(() -> fm.remove("title"))
                        .isInstanceOf(UnsupportedOperationException.class);

                @SuppressWarnings("unchecked")
                List<Object> tags = (List<Object>) fm.get("tags");
                assertThatThrownBy(() -> tags.add("hackedTag"))
                        .isInstanceOf(UnsupportedOperationException.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> meta = (Map<String, Object>) fm.get("meta");
                assertThatThrownBy(() -> meta.put("k", "hackedValue"))
                        .isInstanceOf(UnsupportedOperationException.class);
            });

            // Verify after transaction commit that database remains intact
            NoteView fresh = noteOperations.findById(noteId).orElseThrow();
            assertThat(fresh.frontmatter().get("title")).isEqualTo("Immutable Test");
            @SuppressWarnings("unchecked")
            List<Object> tags = (List<Object>) fresh.frontmatter().get("tags");
            assertThat(tags).containsExactly("tag1", "tag2");
        }

        @Test
        @DisplayName("KnowledgeOperations facade read view is unmodifiable and prevents dirty checking mutation in active transaction")
        void parentApiReadIsolationPreventsUnintendedManagedStateMutation() {
            Map<String, Object> initialFrontmatter = new HashMap<>();
            initialFrontmatter.put("title", "Facade Immutable Test");
            initialFrontmatter.put("tags", new ArrayList<>(List.of("ftag1")));

            var created = knowledgeOperations.createNote(new CreateKnowledgeNoteCommand(
                    "Facade Isolation Test", "content", null, null, null, null, null, initialFrontmatter
            ));
            Long noteId = created.id();

            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            txTemplate.executeWithoutResult(status -> {
                var view = knowledgeOperations.findNoteById(noteId).orElseThrow();
                Map<String, Object> fm = view.frontmatter();

                assertThatThrownBy(() -> fm.put("title", "Hacked Title"))
                        .isInstanceOf(UnsupportedOperationException.class);

                @SuppressWarnings("unchecked")
                List<Object> tags = (List<Object>) fm.get("tags");
                assertThatThrownBy(() -> tags.add("hackedTag"))
                        .isInstanceOf(UnsupportedOperationException.class);
            });

            var fresh = knowledgeOperations.findNoteById(noteId).orElseThrow();
            assertThat(fresh.frontmatter().get("title")).isEqualTo("Facade Immutable Test");
            @SuppressWarnings("unchecked")
            List<Object> tags = (List<Object>) fresh.frontmatter().get("tags");
            assertThat(tags).containsExactly("ftag1");
        }

        @Test
        @DisplayName("Preserves JSON fidelity with unknown keys, JSON nulls, nested structures, booleans, and numbers")
        void preservesJsonFidelityWithUnknownKeysAndJsonNull() {
            Map<String, Object> frontmatter = new HashMap<>();
            frontmatter.put("unknownKeyA", "arbitraryValue");
            frontmatter.put("nullKey", null);
            frontmatter.put("booleanTrue", true);
            frontmatter.put("booleanFalse", false);
            frontmatter.put("intNumber", 42);
            frontmatter.put("floatNumber", 3.14159);

            Map<String, Object> nested = new HashMap<>();
            nested.put("nestedNull", null);
            nested.put("nestedStr", "hello");
            frontmatter.put("nestedObject", nested);

            List<Object> listWithNull = new ArrayList<>();
            listWithNull.add("item1");
            listWithNull.add(null);
            listWithNull.add(99);
            frontmatter.put("listWithNull", listWithNull);

            NoteView created = noteOperations.create(new CreateNoteCommand(
                    "Fidelity Note", "Markdown body", null, null, null, null, null, frontmatter
            ));

            // Verify round-trip through noteOperations
            NoteView retrieved = noteOperations.findById(created.id()).orElseThrow();
            assertThat(retrieved.frontmatter()).containsKey("nullKey");
            assertThat(retrieved.frontmatter().get("nullKey")).isNull();
            assertThat(retrieved.frontmatter().get("unknownKeyA")).isEqualTo("arbitraryValue");
            assertThat(retrieved.frontmatter().get("booleanTrue")).isEqualTo(true);
            assertThat(retrieved.frontmatter().get("booleanFalse")).isEqualTo(false);
            assertThat(((Number) retrieved.frontmatter().get("intNumber")).intValue()).isEqualTo(42);
            assertThat(((Number) retrieved.frontmatter().get("floatNumber")).doubleValue()).isEqualTo(3.14159);

            @SuppressWarnings("unchecked")
            Map<String, Object> retrievedNested = (Map<String, Object>) retrieved.frontmatter().get("nestedObject");
            assertThat(retrievedNested).containsKey("nestedNull");
            assertThat(retrievedNested.get("nestedNull")).isNull();
            assertThat(retrievedNested.get("nestedStr")).isEqualTo("hello");

            @SuppressWarnings("unchecked")
            List<Object> retrievedList = (List<Object>) retrieved.frontmatter().get("listWithNull");
            assertThat(retrievedList).hasSize(3);
            assertThat(retrievedList.get(0)).isEqualTo("item1");
            assertThat(retrievedList.get(1)).isNull();
            assertThat(((Number) retrievedList.get(2)).intValue()).isEqualTo(99);

            // Verify direct JSON query in PostgreSQL
            String dbJson = jdbcTemplate.queryForObject(
                    "SELECT frontmatter::text FROM notes WHERE id = ?",
                    String.class,
                    created.id()
            );
            assertThat(dbJson).isNotNull();
            assertThat(dbJson).contains("\"nullKey\": null");
            assertThat(dbJson).contains("\"unknownKeyA\": \"arbitraryValue\"");
            assertThat(dbJson).contains("\"nestedNull\": null");
        }

        @Test
        @DisplayName("Mutable numeric leaves (AtomicInteger, AtomicLong, AtomicBoolean) are normalized to immutable types preventing aliasing across snapshots")
        void mutableNumericLeavesAreNormalizedAndPreventAliasingAcrossSnapshots() {
            AtomicInteger ai = new AtomicInteger(1);
            AtomicLong al = new AtomicLong(10L);
            AtomicBoolean ab = new AtomicBoolean(true);

            Map<String, Object> callerFm = new HashMap<>();
            callerFm.put("ai", ai);
            callerFm.put("al", al);
            callerFm.put("ab", ab);

            // 1. Create via nested NoteOperations
            NoteView nestedCreated = noteOperations.create(new CreateNoteCommand(
                    "Numeric Aliasing Test", "content", null, null, null, null, null, callerFm
            ));

            // Mutate originals
            ai.set(2);
            al.set(20L);
            ab.set(false);

            // Assert view retained initial normalized values
            assertThat(nestedCreated.frontmatter().get("ai")).isEqualTo(1);
            assertThat(nestedCreated.frontmatter().get("ai")).isInstanceOf(Integer.class);
            assertThat(nestedCreated.frontmatter().get("al")).isEqualTo(10L);
            assertThat(nestedCreated.frontmatter().get("al")).isInstanceOf(Long.class);
            assertThat(nestedCreated.frontmatter().get("ab")).isEqualTo(true);
            assertThat(nestedCreated.frontmatter().get("ab")).isInstanceOf(Boolean.class);

            // Assert database reload retained initial normalized values
            NoteView reloaded = noteOperations.findById(nestedCreated.id()).orElseThrow();
            assertThat(reloaded.frontmatter().get("ai")).isEqualTo(1);
            assertThat(((Number) reloaded.frontmatter().get("al")).longValue()).isEqualTo(10L);
            assertThat(reloaded.frontmatter().get("ab")).isEqualTo(true);

            // 2. Update via nested NoteOperations with new atomics
            AtomicInteger aiUpd = new AtomicInteger(100);
            Map<String, Object> updateFm = new HashMap<>(Map.of("ai", aiUpd));
            NoteView nestedUpdated = noteOperations.update(nestedCreated.id(), new UpdateNoteCommand(
                    "Numeric Aliasing Updated", "content updated", null, null, null, null, null, updateFm
            ));
            aiUpd.set(999);
            assertThat(nestedUpdated.frontmatter().get("ai")).isEqualTo(100);
            assertThat(nestedUpdated.frontmatter().get("ai")).isInstanceOf(Integer.class);

            // 3. Create via parent KnowledgeOperations
            AtomicInteger facadeAi = new AtomicInteger(5);
            AtomicLong facadeAl = new AtomicLong(50L);
            Map<String, Object> facadeFm = new HashMap<>();
            facadeFm.put("ai", facadeAi);
            facadeFm.put("al", facadeAl);

            var facadeCreated = knowledgeOperations.createNote(new CreateKnowledgeNoteCommand(
                    "Facade Numeric Test", "content", null, null, null, null, null, facadeFm
            ));

            facadeAi.set(555);
            facadeAl.set(5555L);

            assertThat(facadeCreated.frontmatter().get("ai")).isEqualTo(5);
            assertThat(facadeCreated.frontmatter().get("ai")).isInstanceOf(Integer.class);
            assertThat(facadeCreated.frontmatter().get("al")).isEqualTo(50L);
            assertThat(facadeCreated.frontmatter().get("al")).isInstanceOf(Long.class);

            // In active write transaction, verify read isolation prevents dirty checking writes
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            txTemplate.executeWithoutResult(status -> {
                var loaded = knowledgeOperations.findNoteById(facadeCreated.id()).orElseThrow();
                assertThat(loaded.frontmatter().get("ai")).isEqualTo(5);
                assertThat(loaded.frontmatter().get("ai")).isInstanceOf(Integer.class);
                assertThat(((Number) loaded.frontmatter().get("al")).longValue()).isEqualTo(50L);
            });

            var freshSqlReload = knowledgeOperations.findNoteById(facadeCreated.id()).orElseThrow();
            assertThat(freshSqlReload.frontmatter().get("ai")).isEqualTo(5);
            assertThat(((Number) freshSqlReload.frontmatter().get("al")).longValue()).isEqualTo(50L);
        }

        @Test
        @DisplayName("Unsupported numeric types (LongAdder, custom Number) are rejected with stable InvalidNoteException without DB writes")
        void unsupportedNumericTypeIsRejectedWithStableException() {
            Map<String, Object> fm = Map.of("adder", new LongAdder());

            // Command construction fails directly
            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, fm))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, fm))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            // Custom Number subclass
            Number customNumber = new Number() {
                @Override public int intValue() { return 0; }
                @Override public long longValue() { return 0; }
                @Override public float floatValue() { return 0; }
                @Override public double doubleValue() { return 0; }
            };
            Map<String, Object> customFm = Map.of("custom", customNumber);

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, customFm))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            // Enclosing transaction rejection leaves zero notes written in DB
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            assertThatThrownBy(() -> txTemplate.executeWithoutResult(status ->
                    noteOperations.create(new CreateNoteCommand("T", "c", null, null, null, null, null, fm))
            )).isInstanceOf(InvalidNoteException.class);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(count).isZero();
        }

        private static final class MutableBigDecimal extends BigDecimal {
            private int mutableVal;

            MutableBigDecimal(int val) {
                super(val);
                this.mutableVal = val;
            }

            void setMutableVal(int val) {
                this.mutableVal = val;
            }

            @Override
            public int intValue() {
                return mutableVal;
            }

            @Override
            public String toString() {
                return String.valueOf(mutableVal);
            }
        }

        private static final class MutableBigInteger extends BigInteger {
            private int mutableVal;

            MutableBigInteger(int val) {
                super(String.valueOf(val));
                this.mutableVal = val;
            }

            void setMutableVal(int val) {
                this.mutableVal = val;
            }

            @Override
            public int intValue() {
                return mutableVal;
            }

            @Override
            public String toString() {
                return String.valueOf(mutableVal);
            }
        }

        @Test
        @DisplayName("Mutable subclasses of BigDecimal and BigInteger are rejected across commands and snapshots without DB writes, while authentic BigDecimals and BigIntegers are preserved with fidelity")
        void mutableSubclassesOfBigDecimalAndBigIntegerAreRejectedWithStableExceptionAndNoWrites() {
            MutableBigDecimal mutableDec = new MutableBigDecimal(42);
            MutableBigInteger mutableBig = new MutableBigInteger(99);

            // 1. Direct snapshot helper rejection
            assertThatThrownBy(() -> NoteFrontmatterSnapshot.deepCopy(Map.of("dec", mutableDec)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> NoteFrontmatterSnapshot.toUnmodifiableSnapshot(Map.of("dec", mutableDec)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> NoteFrontmatterSnapshot.deepCopy(Map.of("big", mutableBig)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> NoteFrontmatterSnapshot.toUnmodifiableSnapshot(Map.of("big", mutableBig)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            // 2. Nested create & update commands rejection
            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, Map.of("dec", mutableDec)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, Map.of("big", mutableBig)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new UpdateNoteCommand("T", "c", null, null, null, null, null, Map.of("dec", mutableDec)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new UpdateNoteCommand("T", "c", null, null, null, null, null, Map.of("big", mutableBig)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            // 3. Parent create & update commands rejection
            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, Map.of("dec", mutableDec)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, Map.of("big", mutableBig)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new UpdateKnowledgeNoteCommand("T", "c", null, null, null, null, null, Map.of("dec", mutableDec)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            assertThatThrownBy(() -> new UpdateKnowledgeNoteCommand("T", "c", null, null, null, null, null, Map.of("big", mutableBig)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported numeric type in note frontmatter");

            // 4. Stable no-write rejection under active write transaction
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            assertThatThrownBy(() -> txTemplate.executeWithoutResult(status ->
                    noteOperations.create(new CreateNoteCommand("T", "c", null, null, null, null, null, Map.of("dec", mutableDec)))
            )).isInstanceOf(InvalidNoteException.class);

            assertThatThrownBy(() -> txTemplate.executeWithoutResult(status ->
                    knowledgeOperations.createNote(new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, Map.of("big", mutableBig)))
            )).isInstanceOf(InvalidNoteException.class);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(count).isZero();

            // 5. Authentic BigDecimal and BigInteger fidelity (nested and parent APIs)
            BigDecimal exactDec = new BigDecimal("123.45");
            BigInteger exactBig = new BigInteger("9876543210123456789");
            Map<String, Object> validFm = new HashMap<>();
            validFm.put("dec", exactDec);
            validFm.put("big", exactBig);

            // Nested create & reload
            NoteView nestedCreated = noteOperations.create(new CreateNoteCommand(
                    "Authentic Numbers Note", "Markdown content", null, null, null, null, null, validFm
            ));
            assertThat(nestedCreated.frontmatter().get("dec")).isEqualTo(exactDec);
            assertThat(nestedCreated.frontmatter().get("dec")).isInstanceOf(BigDecimal.class);
            assertThat(nestedCreated.frontmatter().get("big")).isEqualTo(exactBig);
            assertThat(nestedCreated.frontmatter().get("big")).isInstanceOf(BigInteger.class);

            NoteView nestedReloaded = noteOperations.findById(nestedCreated.id()).orElseThrow();
            assertThat(((Number) nestedReloaded.frontmatter().get("dec")).doubleValue()).isEqualTo(123.45);
            assertThat(nestedReloaded.frontmatter().get("big").toString()).isEqualTo("9876543210123456789");

            // Nested update
            NoteView nestedUpdated = noteOperations.update(nestedCreated.id(), new UpdateNoteCommand(
                    "Authentic Numbers Updated", "Markdown content updated", null, null, null, null, null,
                    Map.of("dec", new BigDecimal("999.99"), "big", new BigInteger("1111111111111111111"))
            ));
            assertThat(nestedUpdated.frontmatter().get("dec")).isEqualTo(new BigDecimal("999.99"));
            assertThat(nestedUpdated.frontmatter().get("big")).isEqualTo(new BigInteger("1111111111111111111"));

            // Parent facade create & update
            var facadeCreated = knowledgeOperations.createNote(new CreateKnowledgeNoteCommand(
                    "Facade Authentic Numbers", "Facade content", null, null, null, null, null,
                    Map.of("dec", new BigDecimal("543.21"), "big", new BigInteger("8888888888888888888"))
            ));
            assertThat(facadeCreated.frontmatter().get("dec")).isEqualTo(new BigDecimal("543.21"));
            assertThat(facadeCreated.frontmatter().get("big")).isEqualTo(new BigInteger("8888888888888888888"));

            var facadeUpdated = knowledgeOperations.updateNote(facadeCreated.id(), new UpdateKnowledgeNoteCommand(
                    "Facade Authentic Updated", "Facade content updated", null, null, null, null, null,
                    Map.of("dec", new BigDecimal("888.88"))
            ));
            assertThat(facadeUpdated.frontmatter().get("dec")).isEqualTo(new BigDecimal("888.88"));

            // Enclosing transaction isolation proof on reload
            txTemplate.executeWithoutResult(status -> {
                var loaded = knowledgeOperations.findNoteById(facadeCreated.id()).orElseThrow();
                assertThat(((Number) loaded.frontmatter().get("dec")).doubleValue()).isEqualTo(888.88);
            });
        }

        @Test
        @DisplayName("Cyclic frontmatter graphs in maps or lists are rejected with InvalidNoteException without StackOverflowError")
        void cyclicFrontmatterGraphIsRejectedWithoutStackOverflow() {
            // 1. Direct map cycle: map.put("self", map)
            Map<String, Object> cycleMap = new HashMap<>();
            cycleMap.put("key", "val");
            cycleMap.put("self", cycleMap);

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, cycleMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Cyclic reference detected in note frontmatter");

            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, cycleMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Cyclic reference detected in note frontmatter");

            // 2. Direct list cycle: list.add(list)
            List<Object> cycleList = new ArrayList<>();
            cycleList.add("item");
            cycleList.add(cycleList);

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, Map.of("list", cycleList)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Cyclic reference detected in note frontmatter");

            // 3. Indirect cycle: map -> list -> map
            Map<String, Object> indirectMap = new HashMap<>();
            List<Object> containerList = new ArrayList<>();
            containerList.add(indirectMap);
            indirectMap.put("loop", containerList);

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, indirectMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Cyclic reference detected in note frontmatter");

            // 4. Update command with cycle
            assertThatThrownBy(() -> new UpdateNoteCommand("T", "c", null, null, null, null, null, cycleMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Cyclic reference detected in note frontmatter");

            assertThatThrownBy(() -> new UpdateKnowledgeNoteCommand("T", "c", null, null, null, null, null, cycleMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Cyclic reference detected in note frontmatter");

            // Verify no DB entries created
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(count).isZero();
        }

        @Test
        @DisplayName("Non-string and null keys are rejected with stable InvalidNoteException without ClassCastException")
        void nonStringAndNullKeysAreRejectedWithoutClassCastException() {
            // Non-string key
            Map<Object, Object> nonStringKeyMap = new HashMap<>();
            nonStringKeyMap.put(12345, "numericKey");

            @SuppressWarnings("unchecked")
            Map<String, Object> rawTypedMap = (Map<String, Object>) (Map<?, ?>) nonStringKeyMap;

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, rawTypedMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Frontmatter map key must be a string");

            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, rawTypedMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Frontmatter map key must be a string");

            // Null key
            Map<Object, Object> nullKeyMap = new HashMap<>();
            nullKeyMap.put(null, "nullKeyValue");

            @SuppressWarnings("unchecked")
            Map<String, Object> rawNullKeyMap = (Map<String, Object>) (Map<?, ?>) nullKeyMap;

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, rawNullNullTyped(rawNullKeyMap)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Frontmatter map contains null key");

            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, rawNullNullTyped(rawNullKeyMap)))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Frontmatter map contains null key");
        }

        private Map<String, Object> rawNullNullTyped(Map<String, Object> map) {
            return map;
        }

        @Test
        @DisplayName("Unsupported leaf types (arbitrary objects) are rejected with stable InvalidNoteException without payload leakage")
        void unsupportedLeafTypesAreRejectedWithInvalidNoteException() {
            Map<String, Object> unsupportedMap = new HashMap<>();
            unsupportedMap.put("badLeaf", new Thread());

            assertThatThrownBy(() -> new CreateNoteCommand("T", "c", null, null, null, null, null, unsupportedMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported value type in note frontmatter");

            assertThatThrownBy(() -> new CreateKnowledgeNoteCommand("T", "c", null, null, null, null, null, unsupportedMap))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Unsupported value type in note frontmatter");
        }
    }

    // =========================================================================
    // 7. Parent Facade Delegation Tests
    // =========================================================================
    @Nested
    @DisplayName("Knowledge parent facade integration tests")
    class KnowledgeFacadeIntegrationTests {

        @Test
        @DisplayName("Facade successfully delegates operations across all 4 knowledge domains")
        void facadeDelegationWorksAcrossAllDomains() {
            // Study through facade
            var study = knowledgeOperations.createStudyItem(new CreateKnowledgeStudyItemCommand(
                    "Design Patterns", null, KnowledgeStudyType.BOOK, null, null, null, null,
                    null, null, null, "Gang of Four", null, null, KnowledgeStudyStatus.PLANNED, BigDecimal.ZERO, null
            ));
            assertThat(study.id()).isNotNull();
            assertThat(knowledgeOperations.findStudyItemById(study.id())).isPresent();

            // Information through facade
            var info = knowledgeOperations.createInformationItem(new CreateKnowledgeInformationItemCommand(
                    "API Design", KnowledgeInformationType.TECHNOLOGY, "RESTful guidelines",
                    null, null, null, null
            ));
            assertThat(info.id()).isNotNull();
            assertThat(knowledgeOperations.findInformationItemById(info.id())).isPresent();

            // Vocabulary through facade
            var vocab = knowledgeOperations.createVocabularyItem(new CreateKnowledgeVocabularyItemCommand(
                    "serendipity", "en", "Pleasant surprise", null, null, null, null, null, null,
                    KnowledgeVocabularyLearningStatus.NEW, null, 0, new BigDecimal("2.50"), 0, 0
            ));
            assertThat(vocab.id()).isNotNull();
            assertThat(knowledgeOperations.findVocabularyItemById(vocab.id())).isPresent();

            var reviewed = knowledgeOperations.reviewVocabularyItem(vocab.id(), new KnowledgeVocabularyReviewTransitionCommand(
                    KnowledgeSrsReviewResponse.GOOD, KnowledgeVocabularyLearningStatus.REVIEW,
                    Instant.now().plus(Duration.ofDays(1)), 1, new BigDecimal("2.60"), 1, 0, Instant.now()
            ));
            assertThat(reviewed.item().repetitionCount()).isEqualTo(1);

            // Note through facade
            var note = knowledgeOperations.createNote(new CreateKnowledgeNoteCommand(
                    "Facade Note", "Markdown content through facade", null, "Vault",
                    null, "facade.md", "hash_facade_1", Map.of("author", "Facade")
            ));
            assertThat(note.id()).isNotNull();
            assertThat(knowledgeOperations.findNoteById(note.id())).isPresent();
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
            Thread.sleep(20);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
