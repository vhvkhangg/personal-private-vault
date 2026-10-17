package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagPurgeService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

/** Real Java/PG cancellation and C boundaries. Native consumer tests are separate. */
@Timeout(40)
@TestPropertySource(properties="ppv.rag.reconciliation-delay-ms=3600000")
class RagCancellationRecoveryIntegrationTest extends AbstractRagIntegrationTest {
    @Autowired RagOperations rag;
    @Autowired RagPurgeService purge;
    @Autowired RagRecoveryService recovery;
    @Autowired SourceIndexReservations reservations;

    private RagSourceView approved() {
        Long owner=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES('FILM') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style) VALUES(?,'Synthetic recovery source','MOVIE','ANIMATION')",owner);
        var candidate=rag.registerDomainCandidate(RagSourceType.FILM,owner.toString(),RagClassification.NON_PRIVATE,null,null);
        return rag.approveSource(candidate.id(),new RagConsentCommand(Set.of("title"),candidate.sourceVersion(),candidate.consentResetVersion(),
                candidate.authorizationEpoch(),candidate.fieldPolicyVersion(),null));
    }
    private RagJobAdmissionResult admit(RagSourceView view) {
        return rag.admitJob(view.id(),new RagIndexCommand(view.sourceVersion(),view.authorizationEpoch(),view.fieldPolicyVersion(),Map.of()));
    }
    private UUID reservation(RagSourceView view) {
        return reservations.read(new TreeSet<>(Set.of(Long.parseLong(view.sourceId())))).require(Long.parseLong(view.sourceId())).reservedBy();
    }
    private static Connection admin() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
    }
    @Test void neverDispatchedCancellationSealsAndReleasesOnlyAfterKnownCommit() {
        var source=approved(); var job=admit(source);
        assertThat(purge.abortJob(job.jobId())).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
        assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",Boolean.class,job.jobId())).isTrue();
        assertThat(reservation(source)).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("SCRUBBED");
        jdbcTemplate.update("UPDATE films SET title='Synthetic manual revision' WHERE id=?",Long.parseLong(source.sourceId()));
        var fresh=rag.findSource(source.id()).orElseThrow();
        assertThat(admit(fresh).jobId()).isNotEqualTo(job.jobId());
    }
    @Test void unknownWorkerBindingRetainsReservationAndSingleJobSlot() {
        var source=approved(); var unrelated=approved(); var job=admit(source);
        jdbcTemplate.update("UPDATE rag.job_leases SET worker_identity=CAST(? AS jsonb) WHERE job_id=?",
                "{\"process_id\":1,\"started_at\":\"2026-01-01T00:00:00Z\",\"endpoint_nonce\":\"00000000-0000-0000-0000-000000000001\"}",job.jobId());
        assertThat(purge.abortJob(job.jobId())).isFalse();
        assertThat(reservation(source)).isEqualTo(job.attemptId());
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("RECOVERY_REQUIRED");
        assertThatThrownBy(() -> admit(unrelated)).isInstanceOf(RagJobBusyException.class);
        jdbcTemplate.update("UPDATE films SET title='Synthetic unrelated edit' WHERE id=?",Long.parseLong(unrelated.sourceId()));
    }
    @Test void failedErasureRetainsPendingCleanupButSafeTerminalReleaseIsAllowed() throws Exception {
        var source=approved(); var job=admit(source);
        try(var connection=admin();var statement=connection.createStatement()) {
            statement.execute("CREATE FUNCTION rag.test_cleanup_failure() RETURNS trigger LANGUAGE plpgsql AS 'BEGIN RAISE EXCEPTION ''Synthetic unavailable storage''; END'");
            statement.execute("CREATE TRIGGER test_cleanup_failure BEFORE DELETE ON rag.staged_outputs FOR EACH STATEMENT EXECUTE FUNCTION rag.test_cleanup_failure()");
        }
        try {
            assertThat(purge.abortJob(job.jobId())).isTrue();
            assertThat(reservation(source)).isNull();
            assertThat(jdbcTemplate.queryForObject("SELECT purge_status FROM rag.runs WHERE job_id=?",String.class,job.jobId())).isEqualTo("PURGE_PENDING_QUIESCENCE");
            assertThat(purge.retryCleanup(job.jobId())).isFalse();
        } finally {
            try(var connection=admin();var statement=connection.createStatement()) {
                statement.execute("DROP TRIGGER test_cleanup_failure ON rag.staged_outputs");
                statement.execute("DROP FUNCTION rag.test_cleanup_failure()");
            }
        }
        assertThat(purge.retryCleanup(job.jobId())).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT purge_status FROM rag.runs WHERE job_id=?",String.class,job.jobId())).isEqualTo("PURGE_COMPLETE");
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
    }
    @Test void sourceReservationRemainsVisibleWhileTerminalPhysicalCommitWaits() throws Exception {
        var source=approved(); var unrelated=approved(); var job=admit(source);
        long barrier=0x17334567L;
        try(var blocker=admin();var setup=admin();var query=setup.createStatement();var lock=blocker.createStatement()) {
            query.execute("CREATE FUNCTION rag.test_cancel_commit_barrier() RETURNS trigger LANGUAGE plpgsql AS 'BEGIN IF NEW.status=''CANCELLED'' THEN PERFORM pg_advisory_xact_lock("+barrier+"); END IF; RETURN NULL; END'");
            query.execute("CREATE CONSTRAINT TRIGGER test_cancel_commit_barrier AFTER UPDATE ON rag.job_leases DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.test_cancel_commit_barrier()");
            blocker.setAutoCommit(false); lock.execute("SELECT pg_advisory_xact_lock("+barrier+")");
            try(var executor=Executors.newSingleThreadExecutor()) {
                var cancellation=executor.submit(() -> purge.abortJob(job.jobId()));
                try {
                    org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> jdbcTemplate.queryForObject(
                            "SELECT count(*) FROM pg_stat_activity WHERE pid<>pg_backend_pid() AND query LIKE '%COMMIT%' AND cardinality(pg_blocking_pids(pid))>0",Integer.class)>0);
                    assertThat(cancellation.isDone()).isFalse();
                    assertThat(reservation(source)).isEqualTo(job.attemptId());
                    assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("STOPPING");
                    jdbcTemplate.update("UPDATE films SET title='Synthetic unrelated during commit' WHERE id=?",Long.parseLong(unrelated.sourceId()));
                } finally { blocker.rollback(); }
                assertThat(cancellation.get(10,TimeUnit.SECONDS)).isTrue();
                assertThat(reservation(source)).isNull();
            } finally {
                blocker.rollback();
                query.execute("DROP TRIGGER test_cancel_commit_barrier ON rag.job_leases");
                query.execute("DROP FUNCTION rag.test_cancel_commit_barrier()");
            }
        }
    }
    @Test void startupReconciliationDiscardsUndispatchedWorkWithoutIndexing() {
        var source=approved(); var job=admit(source);
        recovery.afterSingletonsInstantiated();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("ABORTED");
        assertThat(reservation(source)).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants",Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isZero();
    }
    @Test void directSqlRevocationAndRetainedCatalogCannotAcceptStaleApproval() throws Exception {
        String script=new ClassPathResource("rag/retained_consent_history.sql").getContentAsString(StandardCharsets.UTF_8);
        try(var connection=admin();var statement=connection.createStatement()) {
            statement.setQueryTimeout(20); statement.execute(script);
            while(statement.getMoreResults() || statement.getUpdateCount()!=-1) { /* all real SQL assertions */ }
        }
        var source=approved();
        jdbcTemplate.update("UPDATE rag.sources SET access_state='REVOKED' WHERE id=?",source.id());
        assertThatThrownBy(() -> rag.approveSource(source.id(),new RagConsentCommand(Set.of("title"),source.sourceVersion(),
                source.consentResetVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),null)))
                .isInstanceOf(RagSourceChangedException.class);
    }

    @Test void previouslyRegisteredControllerNeverReclaimsAnotherRuntimeOnReconciliation() {
        UUID original=jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state WHERE singleton",UUID.class);
        UUID other=UUID.randomUUID();
        try {
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",other);
            recovery.reconcile();
            assertThat(jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state WHERE singleton",UUID.class)).isEqualTo(other);
        } finally {
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",original);
        }
    }
}
