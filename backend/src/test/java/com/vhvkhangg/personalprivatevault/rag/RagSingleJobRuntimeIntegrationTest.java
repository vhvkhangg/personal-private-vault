package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobBusyException;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagDependencyGraph;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/** Real Java -> suspended native child -> authenticated HTTP -> restricted PG -> P path. */
@Timeout(90)
class RagSingleJobRuntimeIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @Test
    void twoExplicitJobsUseFreshActualProcessesAndPublishVerifiedSingleUseReceipts() {
        RagSourceView source = approvedFilm("Synthetic first title");
        String previousIdentity = null;
        for (int index = 0; index < 2; index++) {
            RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
            assertThat(reservation(Long.parseLong(source.sourceId()))).isEqualTo(attempt.attemptId());
            enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
            assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM rag.worker_grants
                    WHERE job_id=? AND attempt_id=? AND consumed_at IS NOT NULL
                    """, Integer.class, attempt.jobId(), attempt.attemptId())).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM rag.staged_outputs
                    WHERE job_id=? AND attempt_id=? AND worker_receipt
                    """, Integer.class, attempt.jobId(), attempt.attemptId())).isEqualTo(1);
            assertThatThrownBy(() -> enabledRag.executeSyntheticStage(
                    attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST"))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM rag.staged_outputs WHERE job_id=? AND attempt_id=?
                    """, Integer.class, attempt.jobId(), attempt.attemptId())).isEqualTo(1);
            String identity = jdbcTemplate.queryForObject("""
                    SELECT (worker_identity->>'process_id') || ':' ||
                           (worker_identity->>'creation_file_time')
                    FROM rag.job_leases WHERE job_id=?
                    """, String.class, attempt.jobId());
            assertThat(identity).isNotBlank().doesNotContain("null");
            assertThat(identity).isNotEqualTo(previousIdentity);
            previousIdentity = identity;
            RagFinalizationResult published = enabledRag.finalizeGeneration(
                    attempt.jobId(), attempt.attemptId(), null);
            assertThat(published.active()).isTrue();
            assertThat(published.status()).isEqualTo("PUBLISHED");
            assertTerminalPublication(attempt, source);
            String publishedDigest = jdbcTemplate.queryForObject("""
                    SELECT dependency_manifest_digest FROM rag.index_generations WHERE generation_id=?
                    """, String.class, published.generationId());
            assertThatThrownBy(() -> enabledRag.finalizeGeneration(
                    attempt.jobId(), attempt.attemptId(), "wrong-expected-dependency-digest"))
                    .isInstanceOf(IllegalArgumentException.class);
            RagFinalizationResult duplicate = enabledRag.finalizeGeneration(
                    attempt.jobId(), attempt.attemptId(), publishedDigest);
            assertThat(duplicate.generationId()).isEqualTo(published.generationId());
            assertThat(duplicate.active()).isTrue();
        }
        assertThat(jdbcTemplate.queryForObject("""
                SELECT count(*) FROM rag.job_leases WHERE status='COMPLETED' AND staging_closed
                """, Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT count(*) FROM rag.index_generations WHERE is_active
                """, Integer.class)).isEqualTo(1);
    }

    @Test
    void cancelBeforeLaunchCommitsSealAndReleaseWhileUnrelatedItemsRemainEditable() {
        RagSourceView source = approvedFilm("Synthetic reserved title");
        RagSourceView unrelated = approvedFilm("Synthetic unrelated title");
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE films SET is_nsfw=true WHERE id=?", Long.parseLong(source.sourceId())))
                .satisfies(error -> assertThat(sqlState(error)).isEqualTo("P1701"));
        assertThat(jdbcTemplate.queryForObject("SELECT is_nsfw FROM films WHERE id=?",
                Boolean.class, Long.parseLong(source.sourceId()))).isFalse();
        assertThat(jdbcTemplate.update("UPDATE films SET title='Synthetic unrelated edit' WHERE id=?",
                Long.parseLong(unrelated.sourceId()))).isEqualTo(1);
        enabledRag.cancelJob(attempt.jobId());
        assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("CANCELLED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT staging_closed AND worker_identity IS NULL FROM rag.job_leases WHERE job_id=?",
                Boolean.class, attempt.jobId())).isTrue();
        assertThat(reservation(Long.parseLong(source.sourceId()))).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants",
                Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs",
                Integer.class)).isZero();
        assertThat(jdbcTemplate.update("UPDATE films SET is_nsfw=true WHERE id=?",
                Long.parseLong(source.sourceId()))).isEqualTo(1);
    }

    @Test
    void cancellingNewAttemptPreservesAnIndependentlyCurrentPreviousGeneration() {
        RagSourceView source = approvedFilm("Synthetic preserved title");
        RagJobAdmissionResult first = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(first.jobId(), first.attemptId(), "SYNTHETIC_TEST");
        RagFinalizationResult original = enabledRag.finalizeGeneration(first.jobId(), first.attemptId(), null);
        RagJobAdmissionResult replacement = RagTestCommands.index(enabledRag, source.id());
        enabledRag.cancelJob(replacement.jobId());
        assertThat(enabledRag.getJobStatus(first.jobId()).status()).isEqualTo("COMPLETED");
        assertThat(enabledRag.getJobStatus(first.jobId()).indexState()).isEqualTo("CURRENT");
        assertThat(enabledRag.getJobStatus(replacement.jobId()).status()).isEqualTo("CANCELLED");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT is_active FROM rag.index_generations WHERE generation_id=?
                """, Boolean.class, original.generationId())).isTrue();
        assertThat(reservation(Long.parseLong(source.sourceId()))).isNull();
    }

    @Test
    void normalEditPreservesConsentButMakesIndexOutdatedUntilManualReindex() {
        RagSourceView source = approvedFilm("Synthetic original version");
        RagJobAdmissionResult original = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(original.jobId(), original.attemptId(), "SYNTHETIC_TEST");
        enabledRag.finalizeGeneration(original.jobId(), original.attemptId(), null);
        jdbcTemplate.update("UPDATE films SET title='Synthetic manual revision' WHERE id=?",
                Long.parseLong(source.sourceId()));
        RagSourceView changed = enabledRag.findSource(source.id()).orElseThrow();
        assertThat(changed.accessState()).isEqualTo(RagAccessState.APPROVED);
        assertThat(changed.authorizationEpoch()).isEqualTo(source.authorizationEpoch());
        assertThat(changed.sourceVersion()).isGreaterThan(source.sourceVersion());
        assertThat(enabledRag.getJobStatus(original.jobId()).status()).isEqualTo("COMPLETED");
        assertThat(enabledRag.getJobStatus(original.jobId()).indexState()).isEqualTo("OUTDATED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Integer.class))
                .as("ordinary editing never automatically creates an indexing job").isEqualTo(1);
        RagJobAdmissionResult manual = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(manual.jobId(), manual.attemptId(), "SYNTHETIC_TEST");
        assertThat(enabledRag.finalizeGeneration(manual.jobId(), manual.attemptId(), null).active()).isTrue();
        assertThat(enabledRag.getJobStatus(manual.jobId()).indexState()).isEqualTo("CURRENT");
    }

    @Test
    void completedIndexDoesNotDependOnRetainedWorkerCredential() {
        RagSourceView source = approvedFilm("Synthetic persistent index");
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        RagFinalizationResult original = enabledRag.finalizeGeneration(attempt.jobId(), attempt.attemptId(), null);
        assertThat(jdbcTemplate.update("DELETE FROM rag.worker_grants WHERE job_id=?", attempt.jobId()))
                .isEqualTo(1);
        assertThat(enabledRag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");
        RagFinalizationResult repeated = enabledRag.finalizeGeneration(attempt.jobId(), attempt.attemptId(), null);
        assertThat(repeated.active()).isTrue();
        assertThat(repeated.generationId()).isEqualTo(original.generationId());
    }

    @Test
    void twoActualConcurrentManualStartsCommitOneReservationAndOneActiveAttempt() throws Exception {
        RagSourceView first = approvedFilm("Synthetic concurrent first");
        RagSourceView second = approvedFilm("Synthetic concurrent second");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService tasks = Executors.newFixedThreadPool(2);
        try (Connection gate = administrativeConnection()) {
            gate.setAutoCommit(false);
            int gatePid = lockGraph(gate);
            var left = tasks.submit(() -> concurrentStart(first, ready, start));
            var right = tasks.submit(() -> concurrentStart(second, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            await().atMost(Duration.ofSeconds(2)).pollInterval(Duration.ofMillis(10)).until(() ->
                    blockedBy(gatePid, "pg_advisory_xact_lock").size() == 2);
            gate.commit();
            List<StartResult> results = List.of(left.get(10, TimeUnit.SECONDS), right.get(10, TimeUnit.SECONDS));
            assertThat(results.stream().filter(result -> result.admission() != null)).hasSize(1);
            assertThat(results.stream().filter(result -> result.failure() != null)
                    .map(StartResult::failure)).singleElement().isInstanceOf(RagJobBusyException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Integer.class))
                    .isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL
                    """, Integer.class)).isEqualTo(1);
        } finally {
            start.countDown();
            finishTasks(tasks);
        }
    }

    @Test
    void publicationWinsActualAttemptFenceBeforeCancellation() throws Exception {
        RagSourceView source = approvedFilm("Synthetic publication ordering");
        RagJobAdmissionResult previous = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(previous.jobId(), previous.attemptId(), "SYNTHETIC_TEST");
        RagFinalizationResult oldGeneration = enabledRag.finalizeGeneration(
                previous.jobId(), previous.attemptId(), null);
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        ExecutorService tasks = Executors.newFixedThreadPool(2);
        try (Connection gate = administrativeConnection()) {
            gate.setAutoCommit(false);
            int gatePid = backendPid(gate);
            try (var statement = gate.prepareStatement(
                    "SELECT id FROM rag.index_generations WHERE generation_id=? FOR UPDATE")) {
                statement.setObject(1, oldGeneration.generationId());
                try (var rows = statement.executeQuery()) {
                    assertThat(rows.next()).isTrue();
                }
            }
            var publishing = tasks.submit(() -> enabledRag.finalizeGeneration(
                    attempt.jobId(), attempt.attemptId(), null));
            int publishingPid = awaitBlockedBy(gatePid, "index_generations");
            var cancellation = tasks.submit(() -> cancelOutcome(attempt.jobId()));
            awaitBlockedBy(publishingPid, "job_leases");
            gate.commit();
            assertThat(publishing.get(10, TimeUnit.SECONDS).active()).isTrue();
            assertThat(cancellation.get(10, TimeUnit.SECONDS)).isInstanceOf(IllegalStateException.class);
            assertTerminalPublication(attempt, source);
        } finally {
            finishTasks(tasks);
        }
    }

    @Test
    void cancellationCommittedFirstPreventsQueuedPublicationAndReleasesOnlyAfterSeal() throws Exception {
        RagSourceView source = approvedFilm("Synthetic cancellation ordering");
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        ExecutorService tasks = Executors.newFixedThreadPool(2);
        try (Connection gate = administrativeConnection()) {
            gate.setAutoCommit(false);
            int gatePid = backendPid(gate);
            try (var statement = gate.prepareStatement("""
                    SELECT source_id FROM public.vault_rag_source_state WHERE source_id=? FOR UPDATE
                    """)) {
                statement.setLong(1, Long.parseLong(source.sourceId()));
                try (var rows = statement.executeQuery()) {
                    assertThat(rows.next()).isTrue();
                }
            }
            var cancellation = tasks.submit(() -> cancelOutcome(attempt.jobId()));
            int cancellationPid = awaitBlockedBy(gatePid, "vault_rag_lock_reserved");
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",
                    String.class, attempt.jobId())).isEqualTo("STOPPING");
            assertThat(reservation(Long.parseLong(source.sourceId()))).isEqualTo(attempt.attemptId());
            var publishing = tasks.submit(() -> publicationOutcome(attempt));
            awaitBlockedBy(cancellationPid, "pg_advisory_xact_lock");
            gate.commit();
            assertThat(cancellation.get(10, TimeUnit.SECONDS)).isNull();
            assertThat(publishing.get(10, TimeUnit.SECONDS)).isInstanceOf(IllegalStateException.class);
            assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("CANCELLED");
            assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                    Boolean.class, attempt.jobId())).isTrue();
            assertThat(reservation(Long.parseLong(source.sourceId()))).isNull();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",
                    Integer.class)).isZero();
        } finally {
            finishTasks(tasks);
        }
    }

    @Test
    void actualWorkerIsDeadButPriorSqlWriterMustCommitBeforePublicationCanReleaseReservation() throws Exception {
        RagSourceView source = approvedFilm("Synthetic pending SQL writer");
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        assertWorkerStopped(attempt);
        String receipt = durableReceipt(attempt);
        ExecutorService tasks = Executors.newSingleThreadExecutor();
        try (Connection writer = workerConnection()) {
            writer.setAutoCommit(false);
            int writerPid = backendPid(writer);
            assertThat(stageReceipt(writer, attempt, receipt)).isPositive();
            var publication = tasks.submit(() -> enabledRag.finalizeGeneration(
                    attempt.jobId(), attempt.attemptId(), null));
            awaitBlockedBy(writerPid, "job_leases");
            assertThat(reservation(Long.parseLong(source.sourceId()))).isEqualTo(attempt.attemptId());
            assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                    Boolean.class, attempt.jobId())).isFalse();
            writer.commit();
            assertThat(publication.get(10, TimeUnit.SECONDS).active()).isTrue();
            assertTerminalPublication(attempt, source);
        } finally {
            finishTasks(tasks);
        }
    }

    @Test
    void queuedActualRoleWriterReloadsTheSealAfterPublicationPhysicalCommit() throws Exception {
        RagSourceView source = approvedFilm("Synthetic seal before queued SQL writer");
        RagJobAdmissionResult previous = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(previous.jobId(), previous.attemptId(), "SYNTHETIC_TEST");
        RagFinalizationResult oldGeneration = enabledRag.finalizeGeneration(
                previous.jobId(), previous.attemptId(), null);
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        assertWorkerStopped(attempt);
        String receipt = durableReceipt(attempt);
        ExecutorService tasks = Executors.newFixedThreadPool(2);
        try (Connection gate = administrativeConnection(); Connection writer = workerConnection()) {
            gate.setAutoCommit(false);
            writer.setAutoCommit(false);
            int gatePid = backendPid(gate);
            int writerPid = backendPid(writer);
            try (var statement = gate.prepareStatement(
                    "SELECT id FROM rag.index_generations WHERE generation_id=? FOR UPDATE")) {
                statement.setObject(1, oldGeneration.generationId());
                try (var rows = statement.executeQuery()) {
                    assertThat(rows.next()).isTrue();
                }
            }
            var publication = tasks.submit(() -> enabledRag.finalizeGeneration(
                    attempt.jobId(), attempt.attemptId(), null));
            int publicationPid = awaitBlockedBy(gatePid, "index_generations");
            var lateWriter = tasks.submit(() -> {
                try {
                    stageReceipt(writer, attempt, receipt);
                    writer.commit();
                    return (SQLException) null;
                } catch (SQLException denied) {
                    writer.rollback();
                    return denied;
                }
            });
            await().atMost(Duration.ofSeconds(2)).pollInterval(Duration.ofMillis(10)).until(() ->
                    Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                            "SELECT ?=ANY(pg_catalog.pg_blocking_pids(?))", Boolean.class, publicationPid, writerPid)));
            gate.commit();
            assertThat(publication.get(10, TimeUnit.SECONDS).active()).isTrue();
            SQLException denied = lateWriter.get(10, TimeUnit.SECONDS);
            assertThat((Throwable) denied).isNotNull();
            assertThat(denied.getSQLState()).isEqualTo("23514");
            assertTerminalPublication(attempt, source);
            assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM rag.staged_outputs WHERE job_id=? AND attempt_id=?
                    """, Integer.class, attempt.jobId(), attempt.attemptId())).isEqualTo(1);
        } finally {
            finishTasks(tasks);
        }
    }

    private String durableReceipt(RagJobAdmissionResult attempt) {
        return jdbcTemplate.queryForObject("""
                SELECT payload FROM rag.staged_outputs WHERE job_id=? AND attempt_id=?
                """, String.class, attempt.jobId(), attempt.attemptId());
    }

    private static long stageReceipt(Connection writer, RagJobAdmissionResult attempt, String receipt) throws SQLException {
        try (var statement = writer.prepareStatement("SELECT rag.stage_worker_receipt(?,?,?,?)")) {
            statement.setObject(1, attempt.jobId());
            statement.setObject(2, attempt.attemptId());
            statement.setString(3, attempt.leaseToken());
            statement.setString(4, receipt);
            try (var rows = statement.executeQuery()) {
                assertThat(rows.next()).isTrue();
                return rows.getLong(1);
            }
        }
    }

    private record StartResult(RagJobAdmissionResult admission, RuntimeException failure) {
    }

    private StartResult concurrentStart(RagSourceView source, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
        try {
            return new StartResult(RagTestCommands.index(enabledRag, source.id()), null);
        } catch (RuntimeException failure) {
            return new StartResult(null, failure);
        }
    }

    private RuntimeException cancelOutcome(UUID job) {
        try {
            enabledRag.cancelJob(job);
            return null;
        } catch (RuntimeException failure) {
            return failure;
        }
    }

    private RuntimeException publicationOutcome(RagJobAdmissionResult attempt) {
        try {
            enabledRag.finalizeGeneration(attempt.jobId(), attempt.attemptId(), null);
            return null;
        } catch (RuntimeException failure) {
            return failure;
        }
    }

    private List<Integer> blockedBy(int gatePid, String queryPart) {
        return jdbcTemplate.queryForList("""
                SELECT pid FROM pg_catalog.pg_stat_activity
                WHERE usename='rag_runtime' AND ?=ANY(pg_catalog.pg_blocking_pids(pid))
                    AND query ILIKE ?
                """, Integer.class, gatePid, "%" + queryPart + "%");
    }

    private int awaitBlockedBy(int gatePid, String queryPart) {
        await().atMost(Duration.ofSeconds(2)).pollInterval(Duration.ofMillis(10))
                .until(() -> blockedBy(gatePid, queryPart).size() == 1);
        return blockedBy(gatePid, queryPart).getFirst();
    }

    private Connection administrativeConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static int lockGraph(Connection connection) throws SQLException {
        int pid = backendPid(connection);
        try (var statement = connection.prepareStatement("SELECT pg_advisory_xact_lock(?)")) {
            statement.setLong(1, RagDependencyGraph.LOCK_ID);
            statement.execute();
        }
        return pid;
    }

    private static int backendPid(Connection connection) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT pg_backend_pid()")) {
            assertThat(rows.next()).isTrue();
            return rows.getInt(1);
        }
    }

    private static void finishTasks(ExecutorService tasks) throws InterruptedException {
        tasks.shutdownNow();
        assertThat(tasks.awaitTermination(15, TimeUnit.SECONDS)).as("no consumer/control tasks linger").isTrue();
    }

    private RagSourceView approvedFilm(String title) {
        Long owner = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id", Long.class);
        jdbcTemplate.update("""
                INSERT INTO films(id,title,format,production_style,is_nsfw)
                VALUES (?,?,'MOVIE','LIVE_ACTION',false)
                """, owner, title);
        RagSourceView candidate = RagTestCommands.register(enabledRag, RagSourceType.FILM, owner.toString());
        return RagTestCommands.approve(enabledRag, candidate.id(), Set.of("title", "format"));
    }

    private UUID reservation(long owner) {
        return jdbcTemplate.queryForObject(
                "SELECT reserved_by FROM public.vault_rag_source_state WHERE source_id=?", UUID.class, owner);
    }

    private void assertTerminalPublication(RagJobAdmissionResult attempt, RagSourceView source) {
        assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("COMPLETED");
        assertThat(enabledRag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT staging_closed FROM rag.job_leases WHERE job_id=?
                """, Boolean.class, attempt.jobId())).isTrue();
        assertThat(reservation(Long.parseLong(source.sourceId()))).isNull();
        assertWorkerStopped(attempt);
    }

    private void assertWorkerStopped(RagJobAdmissionResult attempt) {
        Long process = jdbcTemplate.queryForObject("""
                SELECT (worker_identity->>'process_id')::bigint FROM rag.job_leases WHERE job_id=?
                """, Long.class, attempt.jobId());
        String creation = jdbcTemplate.queryForObject("""
                SELECT worker_identity->>'creation_file_time' FROM rag.job_leases WHERE job_id=?
                """, String.class, attempt.jobId());
        assertThat(NativeWorkerProcess.isRecordedProcessTerminated(process, Long.parseLong(creation))).isTrue();
    }

    private static String sqlState(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (current instanceof SQLException sql) {
                return sql.getSQLState();
            }
        }
        return null;
    }
}
