package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/** Python's own COMMIT is blocked at the actual PostgreSQL deferred-trigger boundary. */
@Timeout(90)
class RagPythonCommitTerminationIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @Autowired
    private RagWorkerSupervisor supervisor;

    @Test
    void nativePythonDeathDoesNotReleaseSourcesWhileItsOwnPostgresCommitIsPending() throws Exception {
        RagSourceView source = approvedFilm();
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        String trigger = "p17_pending_commit_" + UUID.randomUUID().toString().replace("-", "");
        long advisoryKey = 0x5031370000000000L | ThreadLocalRandom.current().nextLong(0x000000FFFFFFFFFFL);
        ExecutorService sender = Executors.newSingleThreadExecutor();
        Future<RuntimeException> dispatch = null;
        try (Connection gate = admin(); Connection observer = admin()) {
            try {
                installCommitBarrier(observer, trigger, attempt.jobId(), advisoryKey);
                gate.setAutoCommit(false);
                int gatePid = backendPid(gate);
                try (var lock = gate.prepareStatement("SELECT pg_advisory_xact_lock(?)")) {
                    lock.setLong(1, advisoryKey);
                    lock.execute();
                }
                dispatch = sender.submit(() -> {
                    try {
                        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
                        return null;
                    } catch (RuntimeException failure) {
                        return failure;
                    }
                });
                await().pollInSameThread().atMost(Duration.ofSeconds(12))
                        .pollInterval(Duration.ofMillis(10))
                        .until(() -> pendingPythonCommits(observer, gatePid).size() == 1);
                int backend = pendingPythonCommits(observer, gatePid).getFirst();
                long workerPid = jdbcTemplate.queryForObject("""
                        SELECT (worker_identity->>'process_id')::bigint FROM rag.job_leases WHERE job_id=?
                        """, Long.class, attempt.jobId());
                long workerCreation = Long.parseLong(jdbcTemplate.queryForObject("""
                        SELECT worker_identity->>'creation_file_time' FROM rag.job_leases WHERE job_id=?
                        """, String.class, attempt.jobId()));
                assertThat(NativeWorkerProcess.isRecordedProcessTerminated(workerPid, workerCreation)).isFalse();
                assertThat(supervisor.stopAndVerify(attempt.jobId())).isTrue();
                assertThat(NativeWorkerProcess.isRecordedProcessTerminated(workerPid, workerCreation)).isTrue();
                // The very same backend is still executing Python's explicit COMMIT.
                // Process exit and HTTP completion did not make its transaction disappear.
                assertThat(pendingPythonCommits(observer, gatePid)).containsExactly(backend);
                assertThat(jdbcTemplate.queryForObject("""
                        SELECT reserved_by FROM public.vault_rag_source_state WHERE source_id=?
                        """, UUID.class, Long.parseLong(source.sourceId()))).isEqualTo(attempt.attemptId());
                assertThat(jdbcTemplate.queryForObject("""
                        SELECT count(*) FROM rag.worker_grants WHERE job_id=? AND consumed_at IS NOT NULL
                        """, Integer.class, attempt.jobId())).isEqualTo(1);
                assertThatThrownBy(() -> jdbcTemplate.update(
                        "UPDATE films SET is_nsfw=true WHERE id=?", Long.parseLong(source.sourceId())))
                        .satisfies(failure -> assertThat(sqlState(failure)).isEqualTo("P1701"));
                assertThat(jdbcTemplate.queryForObject("SELECT is_nsfw FROM films WHERE id=?",
                        Boolean.class, Long.parseLong(source.sourceId()))).isFalse();
                gate.commit();
                assertThat(dispatch.get(10, TimeUnit.SECONDS)).isInstanceOf(IllegalStateException.class);
                // Resolve either actual COMMIT or rollback through the real attempt fence;
                // connection loss never supplies an assumed outcome.
                enabledRag.cancelJob(attempt.jobId());
                assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("ABORTED");
                assertThat(jdbcTemplate.queryForObject("""
                        SELECT staging_closed FROM rag.job_leases WHERE job_id=?
                        """, Boolean.class, attempt.jobId())).isTrue();
                assertThat(jdbcTemplate.queryForObject("""
                        SELECT reserved_by FROM public.vault_rag_source_state WHERE source_id=?
                        """, UUID.class, Long.parseLong(source.sourceId()))).isNull();
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs",
                        Integer.class)).isZero();
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants",
                        Integer.class)).isZero();
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",
                        Integer.class)).isZero();
                assertThat(jdbcTemplate.queryForObject("SELECT purge_status FROM rag.runs WHERE job_id=?",
                        String.class, attempt.jobId())).isEqualTo("PURGE_COMPLETE");
                assertThat(jdbcTemplate.update("UPDATE films SET is_nsfw=true WHERE id=?",
                        Long.parseLong(source.sourceId()))).isEqualTo(1);
            } finally {
                if (!gate.getAutoCommit()) {
                    gate.rollback();
                }
                String status = enabledRag.getJobStatus(attempt.jobId()).status();
                if (Set.of("ADMITTED", "STARTING", "ACTIVE", "STOPPING", "RECOVERY_REQUIRED", "FINALIZING")
                        .contains(status)) {
                    supervisor.stopAndVerify(attempt.jobId());
                    if (dispatch != null) {
                        dispatch.get(10, TimeUnit.SECONDS);
                    }
                    enabledRag.cancelJob(attempt.jobId());
                }
                dropCommitBarrier(observer, trigger);
            }
        } finally {
            sender.shutdownNow();
            assertThat(sender.awaitTermination(15, TimeUnit.SECONDS))
                    .as("no launcher or source sender may survive the test").isTrue();
        }
    }

    private RagSourceView approvedFilm() {
        Long id = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id", Long.class);
        jdbcTemplate.update("""
                INSERT INTO films(id,title,format,production_style,is_nsfw)
                VALUES (?,'Synthetic pending Python COMMIT','MOVIE','LIVE_ACTION',false)
                """, id);
        RagSourceView candidate = RagTestCommands.register(enabledRag, RagSourceType.FILM, id.toString());
        return RagTestCommands.approve(enabledRag, candidate.id(), Set.of("title", "format"));
    }

    private static void installCommitBarrier(Connection connection, String name, UUID job, long key)
            throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("SET statement_timeout='5s'");
            statement.execute("SET lock_timeout='3s'");
            statement.execute("""
                    CREATE FUNCTION rag.%s() RETURNS trigger
                    LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $p17$
                    BEGIN
                        IF NEW.job_id=TG_ARGV[0]::uuid THEN
                            PERFORM pg_catalog.pg_advisory_xact_lock(TG_ARGV[1]::bigint);
                        END IF;
                        RETURN NULL;
                    END;
                    $p17$
                    """.formatted(name));
            statement.execute("REVOKE ALL ON FUNCTION rag." + name + "() FROM PUBLIC,rag_worker,rag_runtime");
            statement.execute("""
                    CREATE CONSTRAINT TRIGGER %s AFTER INSERT ON rag.staged_outputs
                    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW
                    EXECUTE FUNCTION rag.%s('%s','%s')
                    """.formatted(name, name, job, key));
        }
    }

    private static void dropCommitBarrier(Connection connection, String name) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("DROP TRIGGER IF EXISTS " + name + " ON rag.staged_outputs");
            statement.execute("DROP FUNCTION IF EXISTS rag." + name + "()");
        }
    }

    private static List<Integer> pendingPythonCommits(Connection observer, int gatePid) throws SQLException {
        try (var statement = observer.prepareStatement("""
                SELECT pid FROM pg_catalog.pg_stat_activity
                WHERE usename='rag_worker' AND query ILIKE 'COMMIT%'
                    AND ?=ANY(pg_catalog.pg_blocking_pids(pid))
                ORDER BY pid
                """)) {
            statement.setInt(1, gatePid);
            List<Integer> result = new ArrayList<>();
            try (var rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(rows.getInt(1));
                }
            }
            return result;
        }
    }

    private static int backendPid(Connection connection) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT pg_backend_pid()")) {
            assertThat(rows.next()).isTrue();
            return rows.getInt(1);
        }
    }

    private static Connection admin() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static String sqlState(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) {
                return sql.getSQLState();
            }
        }
        return null;
    }
}
