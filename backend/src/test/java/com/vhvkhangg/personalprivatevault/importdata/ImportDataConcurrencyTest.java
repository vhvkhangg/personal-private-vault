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
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportTransitionException;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
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

import com.vhvkhangg.personalprivatevault.importdata.internal.domain.ImportJob;
import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImportDataConcurrencyTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ImportJobOperations importJobOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        tearDown();
        executor = Executors.newFixedThreadPool(4);
    }

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
        jdbcTemplate.execute("DELETE FROM import_job_items");
        jdbcTemplate.execute("DELETE FROM import_jobs");
        jdbcTemplate.execute("DELETE FROM information_items");
        jdbcTemplate.execute("DELETE FROM vocabulary_reviews");
        jdbcTemplate.execute("DELETE FROM vocabulary_items");
        jdbcTemplate.execute("DELETE FROM notes");
        jdbcTemplate.execute("DELETE FROM study_items");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type IN ('INFORMATION', 'VOCABULARY', 'NOTE', 'STUDY')");
    }

    private void awaitCompetingLock(String tablePattern, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT count(*) FROM pg_locks l
                    JOIN pg_stat_activity a ON l.pid = a.pid
                    WHERE NOT l.granted
                      AND a.pid != pg_backend_pid()
                      AND a.query ILIKE ?
                    """,
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

    // =========================================================================
    // 1. Execute-vs-Execute Contention (Target without Natural Uniqueness: Information)
    // =========================================================================
    @Test
    @DisplayName("Execute-vs-execute: exactly one execution creates targets; loser waits on lock and rejects with zero additional writes")
    void executeVsExecuteContention() throws Exception {
        String json = """
                [
                  {
                    "title": "Concurrent Info Test",
                    "type": "TECHNOLOGY",
                    "description": "Concurrency target without natural key deduplication"
                  }
                ]
                """;

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.INFORMATION, ImportFormat.JSON, "info.json", null
        ));
        importJobOperations.parse(job.id(), json);
        importJobOperations.validate(job.id());

        CountDownLatch thread1Executed = new CountDownLatch(1);
        CountDownLatch thread1AllowCommit = new CountDownLatch(1);
        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);

        // Thread 1: Executes import in transaction, holds lock before commit
        Future<ImportJobView> t1Future = executor.submit(() -> tx1.execute(status -> {
            ImportJobView result = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));
            thread1Executed.countDown();
            try {
                if (!thread1AllowCommit.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Thread 1 timed out waiting to commit");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return result;
        }));

        // Wait until Thread 1 has executed target writes and holds lock
        assertThat(thread1Executed.await(5, TimeUnit.SECONDS)).isTrue();

        // Thread 2: Preloads job into its persistence context before Thread 1 commits,
        // then attempts execute on the same job, blocking on PostgreSQL row lock in acquireGuardAndRefresh.
        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        Future<ImportJobView> t2Future = executor.submit(() -> tx2.execute(status -> {
            ImportJob preloaded = entityManager.find(ImportJob.class, job.id());
            assertThat(preloaded).isNotNull();
            assertThat(preloaded.getStatus()).isEqualTo(ImportJobStatus.VALIDATED);
            thread2Preloaded.countDown();
            return importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));
        }));

        assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

        // Observe actual PostgreSQL lock waiting
        awaitCompetingLock("import_jobs", Duration.ofSeconds(5));

        // Release Thread 1 to commit
        thread1AllowCommit.countDown();

        ImportJobView t1Result = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(t1Result.status()).isEqualTo(ImportJobStatus.IMPORTED);

        // Thread 2 must unblock, recheck fresh authoritative state, and reject with InvalidImportTransitionException
        assertThatThrownBy(() -> {
            try {
                t2Future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        })
                .isInstanceOf(InvalidImportTransitionException.class)
                .satisfies(e -> {
                    InvalidImportTransitionException ex = (InvalidImportTransitionException) e;
                    assertThat(ex.getCurrentStatus()).isEqualTo(ImportJobStatus.IMPORTED);
                    assertThat(ex.getRequestedTransition()).isEqualTo("EXECUTE");
                    assertThat(ex.getJobId()).isEqualTo(job.id());
                });

        // Verify state invariants: exactly 1 information item created (NOT 2)
        Integer infoCount = jdbcTemplate.queryForObject("SELECT count(*) FROM information_items", Integer.class);
        assertThat(infoCount).isEqualTo(1);

        Integer vaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries WHERE entry_type = 'INFORMATION'", Integer.class);
        assertThat(vaultCount).isEqualTo(1);

        ImportJobView freshJob = importJobOperations.findJobById(job.id()).orElseThrow();
        assertThat(freshJob.status()).isEqualTo(ImportJobStatus.IMPORTED);
        assertThat(freshJob.importedItems()).isEqualTo(1);

        List<ImportJobItemView> items = importJobOperations.findJobItems(job.id(), 10);
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.IMPORTED);
    }

    // =========================================================================
    // 2. Execute-vs-Cancel: Cancel Wins
    // =========================================================================
    @Test
    @DisplayName("Execute-vs-cancel (cancel wins): job becomes CANCELLED, zero target writes, waiting execute rejects")
    void executeVsCancelCancelWins() throws Exception {
        String json = """
                [
                  {
                    "title": "Cancel Wins Target",
                    "type": "TECHNOLOGY"
                  }
                ]
                """;

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.INFORMATION, ImportFormat.JSON, "cancel-wins.json", null
        ));
        importJobOperations.parse(job.id(), json);
        importJobOperations.validate(job.id());

        CountDownLatch thread1Cancelled = new CountDownLatch(1);
        CountDownLatch thread1AllowCommit = new CountDownLatch(1);
        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);

        // Thread 1: Cancels job under transaction and holds lock
        Future<ImportJobView> t1Future = executor.submit(() -> tx1.execute(status -> {
            ImportJobView result = importJobOperations.cancel(job.id());
            thread1Cancelled.countDown();
            try {
                if (!thread1AllowCommit.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Thread 1 timed out waiting to commit");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return result;
        }));

        assertThat(thread1Cancelled.await(5, TimeUnit.SECONDS)).isTrue();

        // Thread 2: Preloads job into its persistence context before Thread 1 commits,
        // then attempts execute on the same job, blocking on PostgreSQL row lock in acquireGuardAndRefresh.
        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        Future<ImportJobView> t2Future = executor.submit(() -> tx2.execute(status -> {
            ImportJob preloaded = entityManager.find(ImportJob.class, job.id());
            assertThat(preloaded).isNotNull();
            assertThat(preloaded.getStatus()).isEqualTo(ImportJobStatus.VALIDATED);
            thread2Preloaded.countDown();
            return importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));
        }));

        assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

        // Observe real lock contention
        awaitCompetingLock("import_jobs", Duration.ofSeconds(5));

        // Commit Thread 1
        thread1AllowCommit.countDown();

        ImportJobView t1Result = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(t1Result.status()).isEqualTo(ImportJobStatus.CANCELLED);

        // Thread 2 unblocks, observes CANCELLED fresh state, and rejects
        assertThatThrownBy(() -> {
            try {
                t2Future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        })
                .isInstanceOf(InvalidImportTransitionException.class)
                .satisfies(e -> {
                    InvalidImportTransitionException ex = (InvalidImportTransitionException) e;
                    assertThat(ex.getCurrentStatus()).isEqualTo(ImportJobStatus.CANCELLED);
                    assertThat(ex.getRequestedTransition()).isEqualTo("EXECUTE");
                    assertThat(ex.getJobId()).isEqualTo(job.id());
                });

        // Zero target records created
        Integer infoCount = jdbcTemplate.queryForObject("SELECT count(*) FROM information_items", Integer.class);
        assertThat(infoCount).isEqualTo(0);

        Integer vaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries WHERE entry_type = 'INFORMATION'", Integer.class);
        assertThat(vaultCount).isEqualTo(0);

        ImportJobView freshJob = importJobOperations.findJobById(job.id()).orElseThrow();
        assertThat(freshJob.status()).isEqualTo(ImportJobStatus.CANCELLED);
        assertThat(freshJob.importedItems()).isEqualTo(0);
    }

    // =========================================================================
    // 3. Execute-vs-Cancel: Execute Wins
    // =========================================================================
    @Test
    @DisplayName("Execute-vs-cancel (execute wins): target created, job IMPORTED, waiting cancel rejects without status overwrite")
    void executeVsCancelExecuteWins() throws Exception {
        String json = """
                [
                  {
                    "title": "Execute Wins Target",
                    "type": "FINANCE"
                  }
                ]
                """;

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.INFORMATION, ImportFormat.JSON, "exec-wins.json", null
        ));
        importJobOperations.parse(job.id(), json);
        importJobOperations.validate(job.id());

        CountDownLatch thread1Executed = new CountDownLatch(1);
        CountDownLatch thread1AllowCommit = new CountDownLatch(1);
        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);

        // Thread 1: Executes under transaction and holds lock
        Future<ImportJobView> t1Future = executor.submit(() -> tx1.execute(status -> {
            ImportJobView result = importJobOperations.execute(job.id(), new ExecuteImportJobCommand(
                    List.of(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT))
            ));
            thread1Executed.countDown();
            try {
                if (!thread1AllowCommit.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Thread 1 timed out waiting to commit");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return result;
        }));

        assertThat(thread1Executed.await(5, TimeUnit.SECONDS)).isTrue();

        // Thread 2: Preloads job into its persistence context before Thread 1 commits,
        // then attempts cancel on the same job, blocking on PostgreSQL row lock in acquireGuardAndRefresh.
        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        Future<ImportJobView> t2Future = executor.submit(() -> tx2.execute(status -> {
            ImportJob preloaded = entityManager.find(ImportJob.class, job.id());
            assertThat(preloaded).isNotNull();
            assertThat(preloaded.getStatus()).isEqualTo(ImportJobStatus.VALIDATED);
            thread2Preloaded.countDown();
            return importJobOperations.cancel(job.id());
        }));

        assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

        // Observe real lock contention
        awaitCompetingLock("import_jobs", Duration.ofSeconds(5));

        // Commit Thread 1
        thread1AllowCommit.countDown();

        ImportJobView t1Result = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(t1Result.status()).isEqualTo(ImportJobStatus.IMPORTED);

        // Thread 2 unblocks, observes IMPORTED fresh state, and rejects without overwriting
        assertThatThrownBy(() -> {
            try {
                t2Future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        })
                .isInstanceOf(InvalidImportTransitionException.class)
                .satisfies(e -> {
                    InvalidImportTransitionException ex = (InvalidImportTransitionException) e;
                    assertThat(ex.getCurrentStatus()).isEqualTo(ImportJobStatus.IMPORTED);
                    assertThat(ex.getRequestedTransition()).isEqualTo("CANCEL");
                    assertThat(ex.getJobId()).isEqualTo(job.id());
                });

        // Job status remains IMPORTED
        ImportJobView freshJob = importJobOperations.findJobById(job.id()).orElseThrow();
        assertThat(freshJob.status()).isEqualTo(ImportJobStatus.IMPORTED);

        // Target record is preserved
        Integer infoCount = jdbcTemplate.queryForObject("SELECT count(*) FROM information_items", Integer.class);
        assertThat(infoCount).isEqualTo(1);
    }

    // =========================================================================
    // 4. Parse-vs-Cancel from CREATED
    // =========================================================================
    @Test
    @DisplayName("Parse-vs-cancel from CREATED (cancel wins): job CANCELLED, waiting parse rejects with fresh state")
    void parseVsCancelFromCreated() throws Exception {
        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.NOTE, ImportFormat.MARKDOWN, "parse-race.md", null
        ));

        CountDownLatch thread1Cancelled = new CountDownLatch(1);
        CountDownLatch thread1AllowCommit = new CountDownLatch(1);
        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);

        // Thread 1: Cancels from CREATED under transaction and holds lock
        Future<ImportJobView> t1Future = executor.submit(() -> tx1.execute(status -> {
            ImportJobView result = importJobOperations.cancel(job.id());
            thread1Cancelled.countDown();
            try {
                if (!thread1AllowCommit.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Thread 1 timed out waiting to commit");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return result;
        }));

        assertThat(thread1Cancelled.await(5, TimeUnit.SECONDS)).isTrue();

        // Thread 2: Preloads job into its persistence context before Thread 1 commits,
        // then attempts parse on the same job, blocking on PostgreSQL row lock in acquireGuardAndRefresh.
        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        Future<ImportJobView> t2Future = executor.submit(() -> tx2.execute(status -> {
            ImportJob preloaded = entityManager.find(ImportJob.class, job.id());
            assertThat(preloaded).isNotNull();
            assertThat(preloaded.getStatus()).isEqualTo(ImportJobStatus.CREATED);
            thread2Preloaded.countDown();
            return importJobOperations.parse(job.id(), "# Note");
        }));

        assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

        awaitCompetingLock("import_jobs", Duration.ofSeconds(5));

        thread1AllowCommit.countDown();

        ImportJobView t1Result = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(t1Result.status()).isEqualTo(ImportJobStatus.CANCELLED);

        // Thread 2 observes CANCELLED fresh status and rejects
        assertThatThrownBy(() -> {
            try {
                t2Future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        })
                .isInstanceOf(InvalidImportTransitionException.class)
                .satisfies(e -> {
                    InvalidImportTransitionException ex = (InvalidImportTransitionException) e;
                    assertThat(ex.getCurrentStatus()).isEqualTo(ImportJobStatus.CANCELLED);
                    assertThat(ex.getRequestedTransition()).isEqualTo("PARSE");
                    assertThat(ex.getJobId()).isEqualTo(job.id());
                });

        // Zero items persisted
        Integer itemCount = jdbcTemplate.queryForObject("SELECT count(*) FROM import_job_items WHERE import_job_id = ?", Integer.class, job.id());
        assertThat(itemCount).isEqualTo(0);
    }

    // =========================================================================
    // 5. Validate-vs-Cancel from PARSED
    // =========================================================================
    @Test
    @DisplayName("Validate-vs-cancel from PARSED (cancel wins): job CANCELLED, waiting validate rejects with fresh state")
    void validateVsCancelFromParsed() throws Exception {
        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.NOTE, ImportFormat.MARKDOWN, "val-race.md", null
        ));
        importJobOperations.parse(job.id(), "# Note Body");

        CountDownLatch thread1Cancelled = new CountDownLatch(1);
        CountDownLatch thread1AllowCommit = new CountDownLatch(1);
        TransactionTemplate tx1 = new TransactionTemplate(transactionManager);

        // Thread 1: Cancels from PARSED under transaction and holds lock
        Future<ImportJobView> t1Future = executor.submit(() -> tx1.execute(status -> {
            ImportJobView result = importJobOperations.cancel(job.id());
            thread1Cancelled.countDown();
            try {
                if (!thread1AllowCommit.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Thread 1 timed out waiting to commit");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return result;
        }));

        assertThat(thread1Cancelled.await(5, TimeUnit.SECONDS)).isTrue();

        // Thread 2: Preloads job into its persistence context before Thread 1 commits,
        // then attempts validate on the same job, blocking on PostgreSQL row lock in acquireGuardAndRefresh.
        TransactionTemplate tx2 = new TransactionTemplate(transactionManager);
        CountDownLatch thread2Preloaded = new CountDownLatch(1);
        Future<ImportJobView> t2Future = executor.submit(() -> tx2.execute(status -> {
            ImportJob preloaded = entityManager.find(ImportJob.class, job.id());
            assertThat(preloaded).isNotNull();
            assertThat(preloaded.getStatus()).isEqualTo(ImportJobStatus.PARSED);
            thread2Preloaded.countDown();
            return importJobOperations.validate(job.id());
        }));

        assertThat(thread2Preloaded.await(5, TimeUnit.SECONDS)).isTrue();

        awaitCompetingLock("import_jobs", Duration.ofSeconds(5));

        thread1AllowCommit.countDown();

        ImportJobView t1Result = t1Future.get(10, TimeUnit.SECONDS);
        assertThat(t1Result.status()).isEqualTo(ImportJobStatus.CANCELLED);

        // Thread 2 observes CANCELLED fresh status and rejects
        assertThatThrownBy(() -> {
            try {
                t2Future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        })
                .isInstanceOf(InvalidImportTransitionException.class)
                .satisfies(e -> {
                    InvalidImportTransitionException ex = (InvalidImportTransitionException) e;
                    assertThat(ex.getCurrentStatus()).isEqualTo(ImportJobStatus.CANCELLED);
                    assertThat(ex.getRequestedTransition()).isEqualTo("VALIDATE");
                    assertThat(ex.getJobId()).isEqualTo(job.id());
                });

        ImportJobView freshJob = importJobOperations.findJobById(job.id()).orElseThrow();
        assertThat(freshJob.status()).isEqualTo(ImportJobStatus.CANCELLED);
    }
}
