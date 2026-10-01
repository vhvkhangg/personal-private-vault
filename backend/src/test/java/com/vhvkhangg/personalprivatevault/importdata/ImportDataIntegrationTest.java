package com.vhvkhangg.personalprivatevault.importdata;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemDecision;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations;
import com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ExecuteImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ImportItemDecisionInput;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportTransitionException;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImportDataIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ImportJobOperations importJobOperations;

    @Autowired
    private KnowledgeOperations knowledgeOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        tearDown();
        jdbcTemplate.execute("INSERT INTO languages (code, name_en, name_vi) VALUES ('en', 'English', 'Tieng Anh') ON CONFLICT DO NOTHING");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM import_job_items");
        jdbcTemplate.execute("DELETE FROM import_jobs");
        jdbcTemplate.execute("DELETE FROM notes");
        jdbcTemplate.execute("DELETE FROM information_items");
        jdbcTemplate.execute("DELETE FROM vocabulary_reviews");
        jdbcTemplate.execute("DELETE FROM vocabulary_items");
        jdbcTemplate.execute("DELETE FROM study_items");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type IN ('NOTE', 'INFORMATION', 'VOCABULARY', 'STUDY')");
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // =========================================================================
    // 1. Schema Validation
    // =========================================================================
    @Nested
    @DisplayName("Schema validation tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Verifies importdata tables exist in PostgreSQL schema")
        void verifiesImportDataTablesExist() {
            List<String> tables = jdbcTemplate.query(
                    """
                    SELECT table_name FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name IN ('import_jobs', 'import_job_items')
                    """,
                    (rs, rowNum) -> rs.getString("table_name")
            );

            assertThat(tables).containsExactlyInAnyOrder("import_jobs", "import_job_items");
        }
    }

    // =========================================================================
    // 2. Target + Format Matrix
    // =========================================================================
    @Nested
    @DisplayName("Target and format matrix tests")
    class TargetAndFormatMatrixTests {

        @Test
        @DisplayName("Allows supported combinations: CSV and JSON for all four targets, Markdown for Note only")
        void allowsSupportedMatrix() {
            for (ImportTargetType target : ImportTargetType.values()) {
                ImportJobView csvJob = importJobOperations.createJob(new CreateImportJobCommand(
                        target, ImportFormat.CSV, "test.csv", null
                ));
                assertThat(csvJob.status()).isEqualTo(ImportJobStatus.CREATED);

                ImportJobView jsonJob = importJobOperations.createJob(new CreateImportJobCommand(
                        target, ImportFormat.JSON, "test.json", null
                ));
                assertThat(jsonJob.status()).isEqualTo(ImportJobStatus.CREATED);
            }

            ImportJobView mdJob = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "test.md", null
            ));
            assertThat(mdJob.status()).isEqualTo(ImportJobStatus.CREATED);
        }

        @Test
        @DisplayName("Rejects unsupported combinations before job creation")
        void rejectsUnsupportedMatrix() {
            for (ImportTargetType target : List.of(ImportTargetType.STUDY, ImportTargetType.INFORMATION, ImportTargetType.VOCABULARY)) {
                assertThatThrownBy(() -> importJobOperations.createJob(new CreateImportJobCommand(
                        target, ImportFormat.MARKDOWN, "invalid.md", null
                )))
                        .isInstanceOf(InvalidImportJobException.class)
                        .hasMessageContaining("Markdown format is only supported for NOTE target");
            }
        }
    }

    // =========================================================================
    // 3. Markdown Note Import Lifecycle & Duplicate Detection
    // =========================================================================
    @Nested
    @DisplayName("Markdown Note import lifecycle tests")
    class MarkdownNoteLifecycleTests {

        @Test
        @DisplayName("Full Note lifecycle: CREATED -> PARSED -> VALIDATED -> IMPORTED with frontmatter & hash")
        void fullMarkdownNoteLifecycle() {
            String rawMarkdown = """
                    ---
                    title: Architecting Modular Systems
                    author: John Doe
                    tags:
                      - architecture
                      - java
                    ---
                    # Modular Systems
                    Content of the note goes here.
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "modular-systems.md", null
            ));
            assertThat(job.status()).isEqualTo(ImportJobStatus.CREATED);
            assertThat(job.fileHash()).isNull();

            // 1. Parse
            ImportJobView parsed = importJobOperations.parse(job.id(), rawMarkdown);
            assertThat(parsed.status()).isEqualTo(ImportJobStatus.PARSED);
            assertThat(parsed.fileHash()).isEqualTo(sha256Hex(rawMarkdown));
            assertThat(parsed.totalItems()).isEqualTo(1);
            assertThat(parsed.validItems()).isEqualTo(1);

            List<ImportJobItemView> items = importJobOperations.findJobItems(job.id(), 10);
            assertThat(items).hasSize(1);
            ImportJobItemView item = items.getFirst();
            assertThat(item.itemIndex()).isEqualTo(0);
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.parsedPayload()).containsEntry("title", "Architecting Modular Systems");
            assertThat(item.parsedPayload()).containsEntry("contentMarkdown", rawMarkdown);

            // 2. Validate
            ImportJobView validated = importJobOperations.validate(job.id());
            assertThat(validated.status()).isEqualTo(ImportJobStatus.VALIDATED);
            assertThat(validated.validItems()).isEqualTo(1);
            assertThat(validated.duplicateItems()).isEqualTo(0);

            // 3. Execute with IMPORT decision
            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));
            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            assertThat(executed.importedItems()).isEqualTo(1);

            List<ImportJobItemView> executedItems = importJobOperations.findJobItems(job.id(), 10);
            ImportJobItemView executedItem = executedItems.getFirst();
            assertThat(executedItem.status()).isEqualTo(ImportItemStatus.IMPORTED);
            assertThat(executedItem.importedVaultEntryId()).isNotNull();

            // Verify target Note exists in DB
            String noteTitle = jdbcTemplate.queryForObject(
                    "SELECT title FROM notes WHERE id = ?",
                    String.class,
                    executedItem.importedVaultEntryId()
            );
            assertThat(noteTitle).isEqualTo("Architecting Modular Systems");

            // Verify imported_file_hash is stored in notes table
            String noteFileHash = jdbcTemplate.queryForObject(
                    "SELECT imported_file_hash FROM notes WHERE id = ?",
                    String.class,
                    executedItem.importedVaultEntryId()
            );
            assertThat(noteFileHash).isEqualTo(parsed.fileHash());
        }

        @Test
        @DisplayName("Duplicate detection: re-importing same Note file detects duplicate by file hash and updates")
        void duplicateNoteDetectionAndUpdate() {
            String rawMarkdown = """
                    ---
                    title: Original Title
                    ---
                    # Body
                    """;

            // Job 1: Initial import
            ImportJobView job1 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "note.md", null
            ));
            importJobOperations.parse(job1.id(), rawMarkdown);
            importJobOperations.validate(job1.id());
            ImportJobView executed1 = importJobOperations.execute(job1.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));
            Long noteId = importJobOperations.findJobItems(job1.id(), 1).getFirst().importedVaultEntryId();

            // Job 2: Re-import exact same markdown content
            ImportJobView job2 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "note.md", null
            ));
            importJobOperations.parse(job2.id(), rawMarkdown);
            ImportJobView validated2 = importJobOperations.validate(job2.id());

            // Must detect DUPLICATE
            assertThat(validated2.status()).isEqualTo(ImportJobStatus.VALIDATED);
            assertThat(validated2.duplicateItems()).isEqualTo(1);
            assertThat(validated2.validItems()).isEqualTo(0);

            ImportJobItemView item2 = importJobOperations.findJobItems(job2.id(), 1).getFirst();
            assertThat(item2.status()).isEqualTo(ImportItemStatus.DUPLICATE);
            assertThat(item2.duplicateVaultEntryId()).isEqualTo(noteId);

            // Execute with UPDATE decision
            ImportJobView executed2 = importJobOperations.execute(job2.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.UPDATE))
            ));
            assertThat(executed2.status()).isEqualTo(ImportJobStatus.IMPORTED);
            assertThat(executed2.importedItems()).isEqualTo(1);

            ImportJobItemView updatedItem = importJobOperations.findJobItems(job2.id(), 1).getFirst();
            assertThat(updatedItem.status()).isEqualTo(ImportItemStatus.UPDATED);
            assertThat(updatedItem.importedVaultEntryId()).isEqualTo(noteId);

            // Total notes in DB is still 1
            Integer noteCount = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(noteCount).isEqualTo(1);
        }
    }

    // =========================================================================
    // 4. CSV Import Lifecycle (Study Target)
    // =========================================================================
    @Nested
    @DisplayName("CSV Study import tests")
    class CsvStudyImportTests {

        @Test
        @DisplayName("Parses quoted CSV with multiple records, executes IMPORT and SKIP decisions")
        void parsesQuotedCsvAndExecutes() {
            String csvContent = """
                    title,type,learningStatus,url,description
                    "Spring Boot in Action",BOOK,IN_PROGRESS,"https://example.com/spring","Comprehensive guide"
                    "Kubernetes Patterns",BOOK,PLANNED,"https://example.com/k8s","Design patterns for container-native apps"
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.STUDY, ImportFormat.CSV, "books.csv", null
            ));

            // Parse
            ImportJobView parsed = importJobOperations.parse(job.id(), csvContent);
            assertThat(parsed.status()).isEqualTo(ImportJobStatus.PARSED);
            assertThat(parsed.totalItems()).isEqualTo(2);
            assertThat(parsed.validItems()).isEqualTo(2);

            // Validate (Study has no natural dedupe lookup -> both remain VALID)
            ImportJobView validated = importJobOperations.validate(job.id());
            assertThat(validated.status()).isEqualTo(ImportJobStatus.VALIDATED);
            assertThat(validated.validItems()).isEqualTo(2);
            assertThat(validated.duplicateItems()).isEqualTo(0);

            // Execute: item 0 -> IMPORT, item 1 -> SKIP
            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(
                            new ImportItemDecisionInput(0, ImportItemDecision.IMPORT),
                            new ImportItemDecisionInput(1, ImportItemDecision.SKIP)
                    )
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            assertThat(executed.importedItems()).isEqualTo(1);

            List<ImportJobItemView> items = importJobOperations.findJobItems(job.id(), 10);
            assertThat(items.get(0).status()).isEqualTo(ImportItemStatus.IMPORTED);
            assertThat(items.get(0).importedVaultEntryId()).isNotNull();
            assertThat(items.get(1).status()).isEqualTo(ImportItemStatus.SKIPPED);
            assertThat(items.get(1).importedVaultEntryId()).isNull();

            // Verify exactly 1 study item was created
            Integer studyCount = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Integer.class);
            assertThat(studyCount).isEqualTo(1);
        }
    }

    // =========================================================================
    // 5. JSON Import Lifecycle (Information Target)
    // =========================================================================
    @Nested
    @DisplayName("JSON Information import tests")
    class JsonInformationImportTests {

        @Test
        @DisplayName("Parses JSON array of Information items and executes import")
        void parsesJsonAndExecutes() {
            String jsonContent = """
                    [
                      {
                        "title": "PostgreSQL Index Types",
                        "type": "TECHNOLOGY",
                        "description": "B-Tree, Hash, GiST, GIN, BRIN overview"
                      },
                      {
                        "title": "Dollar Cost Averaging",
                        "type": "FINANCE",
                        "description": "Investment strategy overview"
                      }
                    ]
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.INFORMATION, ImportFormat.JSON, "info.json", null
            ));

            importJobOperations.parse(job.id(), jsonContent);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(
                            new ImportItemDecisionInput(0, ImportItemDecision.IMPORT),
                            new ImportItemDecisionInput(1, ImportItemDecision.IMPORT)
                    )
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            assertThat(executed.importedItems()).isEqualTo(2);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM information_items", Integer.class);
            assertThat(count).isEqualTo(2);
        }
    }

    // =========================================================================
    // 6. Whole-Job Transactional Rollback
    // =========================================================================
    @Nested
    @DisplayName("Whole-job transactional rollback tests")
    class TransactionalRollbackTests {

        @Test
        @DisplayName("Whole job rolls back when any target write fails, leaving job in VALIDATED status")
        void wholeJobRollbackOnTargetFailure() {
            // Note with blank title will fail at Knowledge validation time
            String csvContent = """
                    title,contentMarkdown
                    "Valid Note 1","Valid content 1"
                    "","Invalid blank title will fail Knowledge creation"
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.CSV, "notes.csv", null
            ));

            importJobOperations.parse(job.id(), csvContent);
            importJobOperations.validate(job.id());

            // Attempt to execute with IMPORT for both items
            assertThatThrownBy(() -> importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(
                            new ImportItemDecisionInput(0, ImportItemDecision.IMPORT),
                            new ImportItemDecisionInput(1, ImportItemDecision.IMPORT)
                    )
            )));

            // Job must remain VALIDATED, not IMPORTED or corrupted
            ImportJobView freshJob = importJobOperations.findJobById(job.id()).orElseThrow();
            assertThat(freshJob.status()).isEqualTo(ImportJobStatus.VALIDATED);
            assertThat(freshJob.importedItems()).isEqualTo(0);

            // Zero notes or vault entries should have been committed
            Integer noteCount = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(noteCount).isEqualTo(0);
        }
    }

    // =========================================================================
    // 7. Cancellation & Terminal State Immutability
    // =========================================================================
    @Nested
    @DisplayName("Cancellation and terminal state tests")
    class CancellationAndTerminalStateTests {

        @Test
        @DisplayName("Cancels job from CREATED, PARSED, and VALIDATED states")
        void cancelsFromPreImportStates() {
            // Cancel from CREATED
            ImportJobView j1 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "j1.md", null
            ));
            ImportJobView c1 = importJobOperations.cancel(j1.id());
            assertThat(c1.status()).isEqualTo(ImportJobStatus.CANCELLED);

            // Cancel from PARSED
            ImportJobView j2 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "j2.md", null
            ));
            importJobOperations.parse(j2.id(), "# Note 2");
            ImportJobView c2 = importJobOperations.cancel(j2.id());
            assertThat(c2.status()).isEqualTo(ImportJobStatus.CANCELLED);

            // Cancel from VALIDATED
            ImportJobView j3 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "j3.md", null
            ));
            importJobOperations.parse(j3.id(), "# Note 3");
            importJobOperations.validate(j3.id());
            ImportJobView c3 = importJobOperations.cancel(j3.id());
            assertThat(c3.status()).isEqualTo(ImportJobStatus.CANCELLED);
        }

        @Test
        @DisplayName("Terminal states cannot be mutated")
        void terminalStatesCannotMutate() {
            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "term.md", null
            ));
            importJobOperations.cancel(job.id());

            // All mutations on CANCELLED job fail
            assertThatThrownBy(() -> importJobOperations.parse(job.id(), "# Test"))
                    .isInstanceOf(InvalidImportTransitionException.class);

            assertThatThrownBy(() -> importJobOperations.validate(job.id()))
                    .isInstanceOf(InvalidImportTransitionException.class);

            assertThatThrownBy(() -> importJobOperations.execute(job.id(), new ExecuteImportJobCommand(List.of())))
                    .isInstanceOf(InvalidImportTransitionException.class);

            assertThatThrownBy(() -> importJobOperations.cancel(job.id()))
                    .isInstanceOf(InvalidImportTransitionException.class);
        }
    }

    // =========================================================================
    // 8. Bounded Reads & Ordering
    // =========================================================================
    @Nested
    @DisplayName("Bounded reads & ordering tests")
    class BoundedReadsTests {

        @Test
        @DisplayName("findJobItems returns items ordered strictly by item_index ASC")
        void findJobItemsOrderedByIndex() {
            String csv = """
                    title,type,learningStatus
                    "Book 1",BOOK,PLANNED
                    "Book 2",BOOK,PLANNED
                    "Book 3",BOOK,PLANNED
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.STUDY, ImportFormat.CSV, "books.csv", null
            ));
            importJobOperations.parse(job.id(), csv);

            List<ImportJobItemView> items = importJobOperations.findJobItems(job.id(), 10);
            assertThat(items).hasSize(3);
            assertThat(items.get(0).itemIndex()).isEqualTo(0);
            assertThat(items.get(1).itemIndex()).isEqualTo(1);
            assertThat(items.get(2).itemIndex()).isEqualTo(2);

            // Bounded limit
            List<ImportJobItemView> limited = importJobOperations.findJobItems(job.id(), 2);
            assertThat(limited).hasSize(2);
            assertThat(limited.get(0).itemIndex()).isEqualTo(0);
            assertThat(limited.get(1).itemIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("findRecentJobs returns jobs ordered by created_at DESC, id DESC")
        void findRecentJobsOrdered() {
            ImportJobView j1 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "1.md", null
            ));
            ImportJobView j2 = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "2.md", null
            ));

            List<ImportJobView> recent = importJobOperations.findRecentJobs(10);
            assertThat(recent).hasSize(2);
            assertThat(recent.get(0).id()).isEqualTo(j2.id());
            assertThat(recent.get(1).id()).isEqualTo(j1.id());
        }
    }

    // =========================================================================
    // 9. Vocabulary Import Lifecycle
    // =========================================================================
    @Nested
    @DisplayName("Vocabulary import tests")
    class VocabularyImportTests {

        @Test
        @DisplayName("Parses CSV Vocabulary items and executes import end-to-end")
        void parsesCsvAndExecutesVocabularyImport() {
            String csvContent = """
                    word,languageCode,meaning,partOfSpeech,easeFactor
                    "ephemeral","en","Lasting for a very short time","adjective",2.50
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.VOCABULARY, ImportFormat.CSV, "vocab.csv", null
            ));

            importJobOperations.parse(job.id(), csvContent);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            assertThat(executed.importedItems()).isEqualTo(1);

            List<ImportJobItemView> items = importJobOperations.findJobItems(job.id(), 10);
            assertThat(items).hasSize(1);
            Long vocabId = items.getFirst().importedVaultEntryId();
            assertThat(vocabId).isNotNull();

            // Verify Vocabulary item created in DB
            String word = jdbcTemplate.queryForObject(
                    "SELECT word FROM vocabulary_items WHERE id = ?",
                    String.class,
                    vocabId
            );
            assertThat(word).isEqualTo("ephemeral");

            String lang = jdbcTemplate.queryForObject(
                    "SELECT language_code FROM vocabulary_items WHERE id = ?",
                    String.class,
                    vocabId
            );
            assertThat(lang).isEqualTo("en");

            String meaning = jdbcTemplate.queryForObject(
                    "SELECT meaning FROM vocabulary_items WHERE id = ?",
                    String.class,
                    vocabId
            );
            assertThat(meaning).isEqualTo("Lasting for a very short time");

            String vaultType = jdbcTemplate.queryForObject(
                    "SELECT entry_type FROM vault_entries WHERE id = ?",
                    String.class,
                    vocabId
            );
            assertThat(vaultType).isEqualTo("VOCABULARY");
        }
    }

    // =========================================================================
    // 10. CSV Whitespace & Markdown Preservation End-to-End
    // =========================================================================
    @Nested
    @DisplayName("CSV Markdown whitespace preservation tests")
    class CsvWhitespacePreservationTests {

        @Test
        @DisplayName("Preserves Note Markdown leading indentation, trailing hard breaks, and final newlines verbatim end-to-end")
        void preservesNoteMarkdownWhitespaceVerbatimEndToEnd() {
            String exactMarkdown = "  Indented heading\nLine with two trailing spaces  \n    Code block line\nLast line with newline\n";
            String csvContent = "title,contentMarkdown\n\"Indented Note\",\"" + exactMarkdown + "\"";

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.CSV, "whitespace-note.csv", null
            ));

            importJobOperations.parse(job.id(), csvContent);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            Long noteId = importJobOperations.findJobItems(job.id(), 1).getFirst().importedVaultEntryId();
            assertThat(noteId).isNotNull();

            String persistedMarkdown = jdbcTemplate.queryForObject(
                    "SELECT content_markdown FROM notes WHERE id = ?",
                    String.class,
                    noteId
            );
            assertThat(persistedMarkdown).isEqualTo(exactMarkdown);
        }

        @Test
        @DisplayName("Preserves Information Markdown internal indentation and trailing spaces verbatim end-to-end")
        void preservesInformationMarkdownWhitespaceVerbatimEndToEnd() {
            String exactMarkdown = "Information Header\n  Indented info line\nTrailing spaces here  \n    Four space indent\nEnd of info";
            String csvContent = "title,type,contentMarkdown\n\"Indented Info\",TECHNOLOGY,\"" + exactMarkdown + "\"";

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.INFORMATION, ImportFormat.CSV, "whitespace-info.csv", null
            ));

            importJobOperations.parse(job.id(), csvContent);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            Long infoId = importJobOperations.findJobItems(job.id(), 1).getFirst().importedVaultEntryId();
            assertThat(infoId).isNotNull();

            String persistedMarkdown = jdbcTemplate.queryForObject(
                    "SELECT content_markdown FROM information_items WHERE id = ?",
                    String.class,
                    infoId
            );
            assertThat(persistedMarkdown).isEqualTo(exactMarkdown);
        }

        @Test
        @DisplayName("Preserves unquoted Note Markdown leading indentation, trailing hard breaks, and spaces verbatim end-to-end")
        void preservesUnquotedNoteMarkdownWhitespaceVerbatimEndToEnd() {
            String exactMarkdown = "  Indented heading  ";
            String csvContent = "title,contentMarkdown\nUnquoted Note," + exactMarkdown + "\n";

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.CSV, "unquoted-whitespace-note.csv", null
            ));

            importJobOperations.parse(job.id(), csvContent);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            Long noteId = importJobOperations.findJobItems(job.id(), 1).getFirst().importedVaultEntryId();
            assertThat(noteId).isNotNull();

            String persistedMarkdown = jdbcTemplate.queryForObject(
                    "SELECT content_markdown FROM notes WHERE id = ?",
                    String.class,
                    noteId
            );
            assertThat(persistedMarkdown).isEqualTo(exactMarkdown);
        }

        @Test
        @DisplayName("Preserves unquoted Information Markdown leading indentation and trailing spaces verbatim end-to-end")
        void preservesUnquotedInformationMarkdownWhitespaceVerbatimEndToEnd() {
            String exactMarkdown = "  Indented info line  ";
            String csvContent = "title,type,contentMarkdown\nUnquoted Info,TECHNOLOGY," + exactMarkdown + "\n";

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.INFORMATION, ImportFormat.CSV, "unquoted-whitespace-info.csv", null
            ));

            importJobOperations.parse(job.id(), csvContent);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            ImportJobItemView jobItem = importJobOperations.findJobItems(job.id(), 1).getFirst();
            Long infoId = jobItem.importedVaultEntryId();
            assertThat(infoId).isNotNull();

            // Verify ImportData preserved unquoted content markdown whitespace verbatim in parsed payload
            assertThat(jobItem.parsedPayload().get("contentMarkdown")).isEqualTo(exactMarkdown);

            // Verify Information item created via KnowledgeOperations receives canonical Phase 8 Information domain normalization (trimOrNull)
            String persistedMarkdown = jdbcTemplate.queryForObject(
                    "SELECT content_markdown FROM information_items WHERE id = ?",
                    String.class,
                    infoId
            );
            assertThat(persistedMarkdown).isEqualTo("Indented info line");
        }

        @Test
        @DisplayName("Preserves high precision YAML decimal frontmatter end-to-end into PostgreSQL JSONB")
        void preservesHighPrecisionYamlDecimalFrontmatterEndToEnd() {
            String raw = """
                    ---
                    precise: 123456789.987654321
                    ---
                    # High Precision Markdown Note
                    Content here.
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.MARKDOWN, "precise-note.md", null
            ));

            importJobOperations.parse(job.id(), raw);
            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));

            assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
            Long noteId = importJobOperations.findJobItems(job.id(), 1).getFirst().importedVaultEntryId();
            assertThat(noteId).isNotNull();

            String frontmatterJson = jdbcTemplate.queryForObject(
                    "SELECT frontmatter::text FROM notes WHERE id = ?",
                    String.class,
                    noteId
            );
            assertThat(frontmatterJson).contains("123456789.987654321");
        }

        @Test
        @DisplayName("CSV with inconsistent column count produces INVALID item with zero target writes")
        void csvWithInconsistentColumnCountProducesInvalidItemAndNoTargetWrites() {
            String csvContent = """
                    title,contentMarkdown
                    Valid Note,Valid content
                    Extra Cell Note,Content,Extra cell discarded
                    Missing Cell Note
                    """;

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.CSV, "inconsistent.csv", null
            ));

            ImportJobView parsed = importJobOperations.parse(job.id(), csvContent);
            assertThat(parsed.totalItems()).isEqualTo(3);
            assertThat(parsed.validItems()).isEqualTo(1);
            assertThat(parsed.invalidItems()).isEqualTo(2);

            List<ImportJobItemView> items = importJobOperations.findJobItems(job.id(), 10);
            assertThat(items.get(0).status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(items.get(1).status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.get(1).errorMessage()).isEqualTo("Inconsistent column count in CSV record");
            assertThat(items.get(2).status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.get(2).errorMessage()).isEqualTo("Inconsistent column count in CSV record");

            importJobOperations.validate(job.id());

            ImportJobView executed = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(
                            new ImportItemDecisionInput(0, ImportItemDecision.IMPORT),
                            new ImportItemDecisionInput(1, ImportItemDecision.SKIP),
                            new ImportItemDecisionInput(2, ImportItemDecision.SKIP)
                    )
            ));
            assertThat(executed.importedItems()).isEqualTo(1);

            Integer totalNotes = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(totalNotes).isEqualTo(1);
        }

        @Test
        @DisplayName("CSV with duplicate headers fails parse catastrophically with zero target writes")
        void csvWithDuplicateHeadersFailsParseCatastrophically() {
            String csvContent = "title,contentMarkdown,title\nNote,Content,Duplicate\n";

            ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                    ImportTargetType.NOTE, ImportFormat.CSV, "duplicate-header.csv", null
            ));

            assertThatThrownBy(() -> importJobOperations.parse(job.id(), csvContent))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Duplicate or ambiguous header in CSV import");

            Integer totalNotes = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
            assertThat(totalNotes).isEqualTo(0);
        }
    }
}
