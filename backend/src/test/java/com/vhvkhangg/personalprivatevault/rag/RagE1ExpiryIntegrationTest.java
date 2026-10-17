package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRun;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRunRepository;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Actual native/HTTP/PG E1 ordering, without editing deadlines or replacing security triggers.
 */
@Timeout(60)
class RagE1ExpiryIntegrationTest extends AbstractEnabledRagIntegrationTest {
    private static final long BARRIER = 61_701_001L;
    private static final long MARKER = 61_701_002L;
    private static final String BEFORE = "aa_p17_e1_gate";
    private static final String AFTER = "zz_p17_e1_gate";

    @Autowired
    private RagSourceAuthority authority;
    @Autowired
    private SourceIndexReservations reservations;
    @Autowired
    private RagJobLeaseRepository leases;
    @Autowired
    private RagRunRepository runs;
    @Autowired
    private RagRuntimeIdentity runtime;
    @Autowired
    private PlatformTransactionManager transactions;

    @Test
    void observedExpiryAfterActualGenerationRowLockWaitStillDeniesPublication() throws Exception {
        RagSourceView source = approvedFilm();
        RagJobAdmissionResult prior = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(prior.jobId(), prior.attemptId(), "SYNTHETIC_TEST");
        RagFinalizationResult previous = enabledRag.finalizeGeneration(prior.jobId(), prior.attemptId(), null);
        RagJobAdmissionResult attempt = shortBudgetFixture(source);
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        ExecutorService tasks = Executors.newSingleThreadExecutor();
        try (Connection blocker = administrativeConnection()) {
            blocker.setAutoCommit(false);
            int blockerPid = backendPid(blocker);
            try (var lock = blocker.prepareStatement(
                "SELECT id FROM rag.index_generations WHERE generation_id=? FOR UPDATE")) {
                lock.setObject(1, previous.generationId());
                try (var rows = lock.executeQuery()) {
                    assertThat(rows.next()).isTrue();
                }
            }
            waitForFinalizationWindow(attempt);
            Future<RagFinalizationResult> finish = tasks.submit(() -> enabledRag.finalizeGeneration(
                attempt.jobId(), attempt.attemptId(), null));
            int finalizerPid;
            try {
                finalizerPid = awaitBlocked(blockerPid, "%index_generations%");
                assertStillReserved(source, attempt);
                waitForExecutionExpiry(attempt);
                assertThat(finish.isDone()).isFalse();
            } finally {
                blocker.commit();
            }
            Throwable denied = failure(finish);
            assertThat(denied).isInstanceOf(RuntimeException.class);
            assertRolledBackPublication(source, attempt);
            assertThat(enabledRag.getJobStatus(prior.jobId()).indexState()).isEqualTo("CURRENT");
            record("generation-lock-expiry-denied", Map.of("finalizer_backend_pid", finalizerPid,
                "deadline", attempt.leasedUntil().toString(), "observed_expired", true,
                "new_publication_count", 0, "previous_index_current", true));
            enabledRag.cancelJob(attempt.jobId());
        } finally {
            finishTasks(tasks);
        }
    }

    @Test
    void expiryBeforeRetainedDefaultDeferredGateRejectsPhysicalCommit() throws Exception {
        runDeferredBoundary(false);
    }

    @Test
    void expiryAfterTimelyLastDeferredGateAllowsRealLateCommitAndPersistentCurrentIndex() throws Exception {
        runDeferredBoundary(true);
    }

    private void runDeferredBoundary(boolean afterSecurityGate) throws Exception {
        RagSourceView source = approvedFilm();
        RagJobAdmissionResult attempt = shortBudgetFixture(source);
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        String originalFunction = jdbcTemplate.queryForObject(
            "SELECT pg_get_functiondef('rag.commit_publication_deadline()'::regprocedure)", String.class);
        String helper = afterSecurityGate ? AFTER : BEFORE;
        installDeferredProbe(helper, attempt.jobId());
        ExecutorService tasks = Executors.newSingleThreadExecutor();
        try (Connection blocker = administrativeConnection()) {
            int blockerPid = backendPid(blocker);
            try (var lock = blocker.prepareStatement("SELECT pg_advisory_lock(?)")) {
                lock.setLong(1, BARRIER);
                lock.execute();
            }
            List<String> deferredOrder = jdbcTemplate.queryForList("""
                SELECT tgname FROM pg_catalog.pg_trigger
                WHERE tgrelid='rag.index_generations'::regclass AND tgdeferrable AND NOT tgisinternal
                ORDER BY tgname
                """, String.class);
            assertThat(deferredOrder).contains("trg_publication_commit_deadline", helper);
            assertThat(deferredOrder.indexOf(helper) > deferredOrder.indexOf("trg_publication_commit_deadline"))
                .isEqualTo(afterSecurityGate);
            assertThat(jdbcTemplate.queryForObject("""
                SELECT tgenabled IN ('O','A') AND tgdeferrable AND tginitdeferred
                FROM pg_catalog.pg_trigger WHERE tgrelid='rag.index_generations'::regclass
                    AND tgname='trg_publication_commit_deadline'
                """, Boolean.class)).isTrue();
            waitForFinalizationWindow(attempt);
            Future<RagFinalizationResult> finish = tasks.submit(() -> enabledRag.finalizeGeneration(
                attempt.jobId(), attempt.attemptId(), null));
            int finalizerPid;
            try {
                // Observe actual JDBC COMMIT executing deferred triggers, not a callback
                // that merely runs before connection.commit(). The marker is held only
                // after the helper really began; its row remains uncommitted/invisible.
                finalizerPid = awaitBlocked(blockerPid, "COMMIT");
                assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_catalog.pg_locks
                    WHERE pid=? AND locktype='advisory' AND granted AND objid::bigint=?
                    """, Integer.class, finalizerPid, MARKER)).isEqualTo(1);
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.p17_e1_probe", Integer.class)).isZero();
                assertStillReserved(source, attempt);
                assertOwnerWriteConflicts(source);
                waitForExecutionExpiry(attempt);
                assertThat(finish.isDone()).isFalse();
                assertStillReserved(source, attempt);
            } finally {
                try (var unlock = blocker.prepareStatement("SELECT pg_advisory_unlock(?)")) {
                    unlock.setLong(1, BARRIER);
                    unlock.execute();
                }
            }
            if (afterSecurityGate) {
                RagFinalizationResult publication = finish.get(8, TimeUnit.SECONDS);
                assertThat(publication.active()).isTrue();
                assertThat(publication.status()).isEqualTo("PUBLISHED");
                Map<String, Object> observed = jdbcTemplate.queryForMap("""
                    SELECT before_wait<l.leased_until AS timely_helper_entry,
                           after_wait>=l.leased_until AS late_after_wait,
                           clock_timestamp()>=l.leased_until AS lease_expired,
                           clock_timestamp()>=g.session_until AS service_session_expired,
                           p.actor AS actor,
                           p.before_wait::text AS before_wait,
                           p.after_wait::text AS after_wait
                    FROM rag.p17_e1_probe p JOIN rag.job_leases l USING(job_id)
                    JOIN rag.worker_grants g USING(job_id) WHERE p.job_id=?
                    """, attempt.jobId());
                assertThat(observed.get("timely_helper_entry")).isEqualTo(true);
                assertThat(observed.get("late_after_wait")).isEqualTo(true);
                assertThat(observed.get("lease_expired")).isEqualTo(true);
                assertThat(observed.get("service_session_expired")).isEqualTo(true);
                assertThat(observed.get("actor")).isEqualTo("rag_runtime");
                assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("COMPLETED");
                assertThat(enabledRag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");
                assertThat(enabledRag.findSource(source.id()).orElseThrow().accessState()).isEqualTo(RagAccessState.APPROVED);
                assertThat(reservation(source)).isNull();
                assertThat(jdbcTemplate.queryForObject("""
                    SELECT staging_closed FROM rag.job_leases WHERE job_id=? AND status='COMPLETED'
                    """, Boolean.class, attempt.jobId())).isTrue();
                // If the unchanged real default gate ran after the expired barrier it
                // would reject. Successful late physical C therefore demonstrates it
                // completed earlier; later read authorization does not make C timely.
                record("after-last-gate-late-publication-allowed", Map.of("finalizer_backend_pid", finalizerPid,
                    "deadline", attempt.leasedUntil().toString(), "deferred_trigger_order", deferredOrder,
                    "observations", observed, "index_current_after_expiry", true));
            } else {
                assertThat(sqlState(failure(finish))).isEqualTo("23514");
                assertRolledBackPublication(source, attempt);
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.p17_e1_probe", Integer.class)).isZero();
                record("before-default-deferred-gate-expiry-denied", Map.of("finalizer_backend_pid", finalizerPid,
                    "deadline", attempt.leasedUntil().toString(), "deferred_trigger_order", deferredOrder,
                    "sql_state", "23514", "publication_count", 0));
                enabledRag.cancelJob(attempt.jobId());
            }
            assertThat(jdbcTemplate.queryForObject(
                "SELECT pg_get_functiondef('rag.commit_publication_deadline()'::regprocedure)", String.class))
                .isEqualTo(originalFunction);
        } finally {
            try {
                finishTasks(tasks);
            } finally {
                dropDeferredProbe(helper);
            }
        }
    }

    /**
     * Clock fixture only: compose the real guarded admission/reserve physical TX
     * with an initially short immutable deadline. This does not certify the public
     * admission API/CAS path; all later production services and security gates are real.
     */
    private RagJobAdmissionResult shortBudgetFixture(RagSourceView source) {
        var prepared = authority.prepareSource(source.id(), Map.of());
        TransactionTemplate transaction = new TransactionTemplate(transactions);
        transaction.setTimeout(10);
        return transaction.execute(status -> {
            var snapshot = authority.lockPrepared(prepared, null, true);
            var root = snapshot.root();
            Instant now = authority.databaseTime();
            Instant deadline = now.plusSeconds(8);
            UUID job = UUID.randomUUID();
            UUID attempt = UUID.randomUUID();
            String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
            RagJobLease lease = new RagJobLease(job, attempt, source.id(), "ADMITTED", token, deadline,
                root.getAuthorizationEpoch(), root.getFieldPolicyVersion(), root.getApprovedFieldsText(),
                root.getSourceRevisionHash(), root.getDescriptionRevisionHash(), snapshot.manifest(), now);
            lease.bindController(Map.of("process_id", ProcessHandle.current().pid(),
                "started_at", ProcessHandle.current().info().startInstant().orElseThrow().toString(),
                "runtime_id", runtime.instance().toString()));
            leases.saveAndFlush(lease);
            var owners = new TreeSet<Long>();
            prepared.proofs().values().forEach(proof -> owners.add(proof.ownerId()));
            reservations.reserve(owners, attempt);
            runs.saveAndFlush(new RagRun(UUID.randomUUID(), job, source.id(), root.getAuthorizationEpoch(),
                "ADMITTED", "{}", "NONE", now));
            return new RagJobAdmissionResult(job, attempt, source.id(), token, deadline, root.getAuthorizationEpoch());
        });
    }

    private void installDeferredProbe(String name, UUID job) throws SQLException {
        try (Connection admin = administrativeConnection(); var statement = admin.createStatement()) {
            statement.execute("CREATE TABLE rag.p17_e1_probe(job_id uuid PRIMARY KEY, before_wait timestamptz, "
                + "after_wait timestamptz, actor text)");
            statement.execute("GRANT SELECT ON rag.p17_e1_probe TO rag_runtime");
            statement.execute("""
                CREATE FUNCTION rag.p17_e1_gate() RETURNS trigger LANGUAGE plpgsql
                SECURITY DEFINER SET search_path=pg_catalog AS $$
                DECLARE entered timestamptz; deadline timestamptz;
                BEGIN
                    IF NEW.job_id <> '%s'::uuid OR NOT NEW.is_active THEN RETURN NULL; END IF;
                    entered:=clock_timestamp();
                    SELECT leased_until INTO deadline FROM rag.job_leases WHERE job_id=NEW.job_id;
                    IF entered>=deadline THEN RAISE EXCEPTION 'Test gate was not entered timely'; END IF;
                    PERFORM pg_advisory_xact_lock(%d);
                    PERFORM pg_advisory_xact_lock(%d);
                    INSERT INTO rag.p17_e1_probe VALUES(NEW.job_id,entered,clock_timestamp(),session_user::text);
                    RETURN NULL;
                END $$
                """.formatted(job, MARKER, BARRIER));
            statement.execute("REVOKE ALL ON FUNCTION rag.p17_e1_gate() FROM PUBLIC,rag_runtime,rag_worker");
            statement.execute("CREATE CONSTRAINT TRIGGER " + name
                + " AFTER INSERT OR UPDATE ON rag.index_generations DEFERRABLE INITIALLY DEFERRED "
                + "FOR EACH ROW EXECUTE FUNCTION rag.p17_e1_gate()");
        }
    }

    private void dropDeferredProbe(String name) throws SQLException {
        try (Connection admin = administrativeConnection(); var statement = admin.createStatement()) {
            statement.execute("DROP TRIGGER IF EXISTS " + name + " ON rag.index_generations");
            statement.execute("DROP FUNCTION IF EXISTS rag.p17_e1_gate()");
            statement.execute("DROP TABLE IF EXISTS rag.p17_e1_probe");
        }
    }

    private void waitForFinalizationWindow(RagJobAdmissionResult attempt) {
        assertThat(remainingMillis(attempt)).as("actual native stage must finish before the immutable short deadline")
            .isGreaterThan(2_000);
        await().atMost(Duration.ofSeconds(9)).pollInterval(Duration.ofMillis(10)).until(() ->
            remainingMillis(attempt) <= 1_800);
        assertThat(remainingMillis(attempt)).isPositive();
    }

    private Long remainingMillis(RagJobAdmissionResult attempt) {
        return jdbcTemplate.queryForObject("""
            SELECT (EXTRACT(EPOCH FROM (leased_until-clock_timestamp()))*1000)::bigint
            FROM rag.job_leases WHERE job_id=? AND attempt_id=?
            """, Long.class, attempt.jobId(), attempt.attemptId());
    }

    private void waitForExecutionExpiry(RagJobAdmissionResult attempt) {
        await().atMost(Duration.ofSeconds(3)).pollInterval(Duration.ofMillis(10)).until(() ->
            Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT clock_timestamp()>=l.leased_until AND clock_timestamp()>=g.session_until
                FROM rag.job_leases l JOIN rag.worker_grants g USING(job_id,attempt_id)
                WHERE l.job_id=? AND l.attempt_id=?
                """, Boolean.class, attempt.jobId(), attempt.attemptId())));
    }

    private int awaitBlocked(int blockerPid, String query) {
        await().atMost(Duration.ofSeconds(1)).pollInterval(Duration.ofMillis(10)).until(() ->
            blockedBy(blockerPid, query).size() == 1);
        return blockedBy(blockerPid, query).getFirst();
    }

    private List<Integer> blockedBy(int blockerPid, String query) {
        return jdbcTemplate.queryForList("""
            SELECT pid FROM pg_catalog.pg_stat_activity WHERE usename='rag_runtime'
                AND ?=ANY(pg_catalog.pg_blocking_pids(pid)) AND query ILIKE ?
            """, Integer.class, blockerPid, query);
    }

    private void assertStillReserved(RagSourceView source, RagJobAdmissionResult attempt) {
        assertThat(reservation(source)).isEqualTo(attempt.attemptId());
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",
            String.class, attempt.jobId())).isEqualTo("ACTIVE");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",
            Integer.class, attempt.jobId())).isZero();
    }

    private void assertRolledBackPublication(RagSourceView source, RagJobAdmissionResult attempt) {
        assertThat(reservation(source)).isEqualTo(attempt.attemptId());
        assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("RECOVERY_REQUIRED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",
            Integer.class, attempt.jobId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
            Boolean.class, attempt.jobId())).isFalse();
    }

    private void assertOwnerWriteConflicts(RagSourceView source) {
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE films SET is_nsfw=true WHERE id=?",
            Long.parseLong(source.sourceId())))
            .satisfies(problem -> assertThat(sqlState(problem)).isIn("55P03", "P1701"));
        assertThat(jdbcTemplate.queryForObject("SELECT is_nsfw FROM films WHERE id=?",
            Boolean.class, Long.parseLong(source.sourceId()))).isFalse();
    }

    private UUID reservation(RagSourceView source) {
        return jdbcTemplate.queryForObject("SELECT reserved_by FROM public.vault_rag_source_state WHERE source_id=?",
            UUID.class, Long.parseLong(source.sourceId()));
    }

    private RagSourceView approvedFilm() {
        Long owner = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) "
            + "VALUES (?,'Synthetic E1 boundary','MOVIE','LIVE_ACTION',false)", owner);
        var candidate = RagTestCommands.register(enabledRag, RagSourceType.FILM, owner.toString());
        return RagTestCommands.approve(enabledRag, candidate.id(), Set.of("title", "format"));
    }

    private static Throwable failure(Future<?> task) throws Exception {
        try {
            task.get(8, TimeUnit.SECONDS);
            throw new AssertionError("Expired verification unexpectedly committed");
        } catch (ExecutionException rejected) {
            return rejected.getCause();
        }
    }

    private static int backendPid(Connection connection) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT pg_backend_pid()")) {
            assertThat(rows.next()).isTrue();
            return rows.getInt(1);
        }
    }

    private static Connection administrativeConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static void finishTasks(ExecutorService tasks) throws InterruptedException {
        tasks.shutdownNow();
        assertThat(tasks.awaitTermination(15, TimeUnit.SECONDS)).as("no finalization SQL/control task may linger").isTrue();
    }

    private static String sqlState(Throwable problem) {
        for (Throwable cause = problem; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) {
                return sql.getSQLState();
            }
        }
        return null;
    }

    private static void record(String name, Map<String, Object> values) throws Exception {
        var destination = repository().resolve("logs/phase17-alternative-b-implementation-2026-10-15/e1-" + name + ".json");
        Files.createDirectories(destination.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(destination.toFile(), values);
    }
}
