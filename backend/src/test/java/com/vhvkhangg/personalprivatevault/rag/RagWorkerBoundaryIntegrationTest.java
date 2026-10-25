package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRun;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRunRepository;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.concurrent.*;
import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Original regression identities retained; ADR-0020 explicitly supersedes daemon, TTL and multi-job expectations. */
@Timeout(90)
class RagWorkerBoundaryIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @Autowired RagOperations rag;
    @Autowired RagWorkerSupervisor supervisor;
    @Autowired RagEngineClient client;
    @Autowired RagSourceAuthority authority;
    @Autowired SourceIndexReservations reservations;
    @Autowired RagJobLeaseRepository leases;
    @Autowired RagRunRepository runs;
    @Autowired RagRuntimeIdentity runtime;
    @Autowired RagRecoveryService recovery;
    @Autowired RagJobService jobs;
    @Autowired RagWorkerGrantService grants;
    @Autowired RagPurgeService purge;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    private RagSourceView approvedSource() {
        Long genre=jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES (?) RETURNING id",Long.class,UUID.randomUUID().toString());
        Long person=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic')",person);
        Long id=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,is_nsfw) VALUES (?,'Synthetic \"title\"','NOVEL',?,?,false)",id,genre,person);
        Long source=RagTestCommands.register(rag, RagSourceType.FICTION,id.toString()).id();
        RagTestCommands.approve(rag, source,Set.of("title","format","consumptionStatus","genreId","storyArchetypeIds"));
        return rag.findSource(source).orElseThrow();
    }
    private RagJobAdmissionResult admission() { return RagTestCommands.index(rag, approvedSource().id()); }

    @Test void nsfwAndRemovedFieldsBeforeExecutionNeverReachActualPythonWorker() throws Exception {
        for (boolean nsfw : List.of(true, false)) {
            var attempt = admission();
            long owner = Long.parseLong(rag.findSource(attempt.sourceId()).orElseThrow().sourceId());
            if (nsfw) {
                assertThatThrownBy(() -> jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", owner))
                    .isInstanceOf(org.springframework.dao.DataAccessException.class);
                assertThat(jdbcTemplate.queryForObject("SELECT is_nsfw FROM fictions WHERE id=?", Boolean.class, owner)).isFalse();
            } else {
                assertThatThrownBy(() -> RagTestCommands.approve(rag, attempt.sourceId(), Set.of("format")))
                    .isInstanceOf(org.springframework.dao.DataAccessException.class)
                    .satisfies(failure -> assertThat(sqlState(failure)).isEqualTo("P1701"));
            }
            // Cancel-first: restriction must not commit against a reserved source.
            rag.cancelJob(attempt.jobId());
            if (nsfw) {
                jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", owner);
                assertThatThrownBy(() -> RagTestCommands.index(rag, attempt.sourceId())).isInstanceOf(IllegalStateException.class);
            } else RagTestCommands.approve(rag, attempt.sourceId(), Set.of("format"));
            assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST"))
                .isInstanceOf(IllegalStateException.class);
        }
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases WHERE worker_identity IS NOT NULL", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs", Integer.class)).isZero();
    }

    @Test void javaPythonRestrictedPostgresRoundTripPublishesVerifiedReceipt() {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE worker_receipt",Integer.class)).isEqualTo(1);
        var result=rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null);
        assertThat(result.active()).isTrue();
    }
    @Test void repeatedWorkerRequestReturnsOneDurableReceipt() {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        // One-use body consumption replaces the old daemon's successful repeated dispatch.
        assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST"))
            .isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs",Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants WHERE consumed_at IS NOT NULL",Integer.class)).isEqualTo(1);
    }

    @Test void unissuedForgedAndExpiredScopesDenyAsActualWorkerRole() throws Exception {
        var attempt=shortBudgetFixture(approvedSource());
        try (Connection connection=workerConnection()) {
            assertWorkerDenies(connection, attempt, "a".repeat(64), "b".repeat(64), attempt.leaseToken());
            jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
            var grant=issuedGrant(attempt);
            assertWorkerAccepts(connection, grant);
            assertWorkerDenies(connection, attempt, grant.inputDigest(), grant.scopeDigest(), "forged");
            awaitExpired(attempt.jobId());
            assertWorkerDenies(connection, attempt, grant.inputDigest(), grant.scopeDigest(), attempt.leaseToken());
        }
    }

    @Test void actualWorkerCannotReadBusinessAuthorityOrEscalate() throws Exception {
        try (Connection connection=workerConnection();var statement=connection.createStatement()) {
            for (String sql:List.of("SELECT * FROM public.fictions","SELECT * FROM public.films",
                    "SELECT * FROM public.app_users","SELECT * FROM public.refresh_tokens","SELECT * FROM rag.worker_grants",
                    "SELECT * FROM rag.job_leases","SELECT * FROM rag.sources","UPDATE rag.index_generations SET is_active=true",
                    "CREATE TABLE rag.exploit(id int)","CREATE TEMP TABLE exploit(id int)","CREATE SCHEMA exploit","CREATE ROLE exploit SUPERUSER",
                    "SET ROLE rag_runtime","SELECT rag.stage_worker_output(gen_random_uuid(),gen_random_uuid(),'t','CHUNK','x')")) {
                assertThatThrownBy(()->statement.execute(sql)).as(sql).isInstanceOf(SQLException.class);
            }
        }
    }
    @Test void malformedStageDigestAndMissingWorkerProvenanceCannotPublish() throws Exception {
        var attempt=admission();
        for (boolean claimedProvenance : List.of(true, false)) {
            assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO rag.staged_outputs(job_id,attempt_id,lease_token,stage_type,payload,created_at,worker_receipt)
                VALUES (?,?,?,'SYNTHETIC_TEST','invalid digest',clock_timestamp(),?)
                """, attempt.jobId(),attempt.attemptId(),attempt.leaseToken(),claimedProvenance))
                .as("neither fabricated nor missing provenance may enter staging")
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
        }
        assertThatThrownBy(() -> rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null))
            .isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations", Integer.class)).isZero();
    }

    @Test void leaseExpiryDuringActualGenerationLockWaitDeniesPublication() throws Exception {
        var first=admission();
        jobs.executeSyntheticStage(first.jobId(),first.attemptId(),"SYNTHETIC_TEST");
        var published=rag.finalizeGeneration(first.jobId(),first.attemptId(),null);
        var second=shortBudgetFixture(rag.findSource(first.sourceId()).orElseThrow());
        jobs.executeSyntheticStage(second.jobId(),second.attemptId(),"SYNTHETIC_TEST");
        try (var blocker=adminConnection(); var executor=Executors.newSingleThreadExecutor()) {
            blocker.setAutoCommit(false);
            int blockerPid=backendPid(blocker);
            try(var lock=blocker.prepareStatement("SELECT id FROM rag.index_generations WHERE generation_id=? FOR UPDATE")) {
                lock.setObject(1,published.generationId());lock.executeQuery().close();
            }
            waitForFinalizationWindow(second);
            var finalizer=executor.submit(() -> rag.finalizeGeneration(second.jobId(),second.attemptId(),null));
            try {
                awaitCondition(() -> jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid))
                        AND query LIKE '%index_generations%'
                    """,Integer.class,blockerPid)==1);
                awaitExpired(second.jobId());
            } finally { blocker.commit(); }
            assertThatThrownBy(() -> finalizer.get(10,TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,second.jobId())).isZero();
            assertThat(rag.getJobStatus(first.jobId()).indexState()).isEqualTo("CURRENT");
        }
    }

    @Test void expiryAfterFinalizerReturnsBeforeOuterPhysicalCommitRollsBackActivation() throws Exception {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        // ADR-0020 forbids the outer-TX premise; finalization must own its physical COMMIT.
        assertThatThrownBy(() -> new TransactionTemplate(transactions).execute(status ->
            rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null)))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("ambient");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,attempt.jobId())).isZero();
        assertThat(rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null).active()).isTrue();
    }

    @Test void actualWorkerStagingCommitAndPurgeShareAttemptFenceInBothOrders() throws Exception {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        var grant=issuedGrant(attempt);
        String receipt=durableReceipt(attempt);
        try (var connection=workerConnection(); var executor=Executors.newSingleThreadExecutor()) {
            connection.setAutoCommit(false);
            int pid=backendPid(connection);
            stage(connection,grant,receipt);
            var purger=executor.submit(() -> rag.cancelJob(attempt.jobId()));
            try {
                awaitCondition(() -> jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid))
                    """,Integer.class,pid)>0);
            } finally { connection.commit(); }
            purger.get(10,TimeUnit.SECONDS);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,attempt.jobId())).isZero();
            assertThat(rag.getJobStatus(attempt.jobId()).purgeStatus()).isEqualTo("PURGE_COMPLETE");
            connection.setAutoCommit(true);
            assertThatThrownBy(() -> stage(connection,grant,receipt)).isInstanceOf(SQLException.class).hasMessageContaining("denied");
        }
        var fresh=RagTestCommands.index(rag,attempt.sourceId());
        jobs.executeSyntheticStage(fresh.jobId(),fresh.attemptId(),"SYNTHETIC_TEST");
        assertThat(rag.finalizeGeneration(fresh.jobId(),fresh.attemptId(),null).active()).isTrue();
    }

    @Test void workerExpiryAfterLastLeaseCheckRejectsPhysicalCommit() throws Exception {
        var attempt=shortBudgetFixture(approvedSource());
        supervisor.begin(attempt.jobId(),attempt.attemptId());
        var grant=grants.issue(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        UUID session=grants.executionSession(attempt.jobId(),attempt.attemptId());
        long barrier=61_702_001L;
        try (var blocker=adminConnection(); var statement=blocker.createStatement();
             var executor=Executors.newSingleThreadExecutor()) {
            statement.execute("SELECT pg_advisory_lock("+barrier+")");
            statement.execute("""
                CREATE FUNCTION rag.test_worker_deferred_gate() RETURNS trigger LANGUAGE plpgsql
                SECURITY DEFINER SET search_path=pg_catalog AS $$
                BEGIN PERFORM pg_advisory_xact_lock(61702001); RETURN NULL; END $$
                """);
            statement.execute("REVOKE ALL ON FUNCTION rag.test_worker_deferred_gate() FROM PUBLIC,rag_worker,rag_runtime");
            statement.execute("""
                CREATE CONSTRAINT TRIGGER aa_worker_deferred_gate AFTER INSERT ON rag.staged_outputs
                DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.test_worker_deferred_gate()
                """);
            String unchanged=jdbcTemplate.queryForObject(
                "SELECT pg_get_functiondef('rag.commit_stage_deadline()'::regprocedure)",String.class);
            try {
                int pid=backendPid(blocker);
                var execution=executor.submit(() -> client.triggerBoundWorkerStage(grant,session,
                    supervisor.requireReady(attempt.jobId(),attempt.attemptId())));
                try {
                    awaitCondition(() -> {
                        boolean pending=jdbcTemplate.queryForObject("""
                            SELECT count(*) FROM pg_stat_activity WHERE usename='rag_worker'
                                AND ?=ANY(pg_blocking_pids(pid))
                            """,Integer.class,pid)==1;
                        if(!pending && execution.isDone()) {
                            try { execution.get(1,TimeUnit.SECONDS); }
                            catch(Exception failure) {
                                throw new AssertionError("Actual sender failed before the required deferred COMMIT gate",failure);
                            }
                            throw new AssertionError("Actual sender returned without entering the required deferred COMMIT gate");
                        }
                        return pending;
                    });
                    awaitExpired(attempt.jobId());
                } finally { statement.execute("SELECT pg_advisory_unlock("+barrier+")"); }
                assertThatThrownBy(() -> execution.get(8,TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class);
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,attempt.jobId())).isZero();
                assertThat(jdbcTemplate.queryForObject(
                    "SELECT pg_get_functiondef('rag.commit_stage_deadline()'::regprocedure)",String.class)).isEqualTo(unchanged);
            } finally {
                statement.execute("SELECT pg_advisory_unlock("+barrier+")");
                statement.execute("DROP TRIGGER IF EXISTS aa_worker_deferred_gate ON rag.staged_outputs");
                statement.execute("DROP FUNCTION IF EXISTS rag.test_worker_deferred_gate()");
            }
        } finally {
            supervisor.end(attempt.jobId());
            assertThat(supervisor.stopAndVerify(attempt.jobId())).isTrue();
        }
    }

    @Test void lostCompletionAckReturnsSameGenerationAndIndependentSessionExpiryDoesNotMergeConsent() throws Exception {
        var first=admission();
        jobs.executeSyntheticStage(first.jobId(),first.attemptId(),"SYNTHETIC_TEST");
        var result=rag.finalizeGeneration(first.jobId(),first.attemptId(),null);
        assertThat(rag.finalizeGeneration(first.jobId(),first.attemptId(),null).generationId()).isEqualTo(result.generationId());
        var second=shortBudgetFixture(rag.findSource(first.sourceId()).orElseThrow());
        jobs.executeSyntheticStage(second.jobId(),second.attemptId(),"SYNTHETIC_TEST");
        awaitExpired(second.jobId());
        assertThatThrownBy(() -> rag.finalizeGeneration(second.jobId(),second.attemptId(),null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jobs.executeSyntheticStage(second.jobId(),second.attemptId(),"SYNTHETIC_TEST")).isInstanceOf(IllegalStateException.class);
        assertThat(rag.finalizeGeneration(first.jobId(),first.attemptId(),null).generationId()).isEqualTo(result.generationId());
        assertThat(rag.getJobStatus(first.jobId()).indexState()).isEqualTo("CURRENT");
    }

    @Test void physicalDeletionFailureCannotUndoDenialAndRetriesAfterRepair() throws Exception {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null);
        try(var admin=adminConnection(); var statement=admin.createStatement()) {
            statement.execute("CREATE FUNCTION rag.test_delete_failure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Synthetic deletion unavailable'; END $$");
            statement.execute("CREATE TRIGGER test_delete_failure BEFORE DELETE ON rag.staged_outputs FOR EACH ROW EXECUTE FUNCTION rag.test_delete_failure()");
            try {
                rag.revokeSource(attempt.sourceId());
                // Historical job completion is distinct from current derivative access.
                assertThat(rag.getJobStatus(attempt.jobId()).status()).isEqualTo("COMPLETED");
                assertThat(rag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("REVOKED");
                recovery.reconcile();
                assertThat(jdbcTemplate.queryForObject("SELECT access_state FROM rag.sources WHERE id=?",String.class,attempt.sourceId())).isEqualTo("REVOKED");
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,attempt.jobId())).isEqualTo(1);
                assertThat(purge.retryCleanup(attempt.jobId())).isFalse();
                assertThat(rag.getJobStatus(attempt.jobId()).purgeStatus()).isEqualTo("PURGE_PENDING_QUIESCENCE");
                assertThat(rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null).active()).isFalse();
            } finally {
                statement.execute("DROP TRIGGER test_delete_failure ON rag.staged_outputs");
                statement.execute("DROP FUNCTION rag.test_delete_failure()");
            }
        }
        assertThat(purge.retryCleanup(attempt.jobId())).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,attempt.jobId())).isZero();
        assertThat(rag.getJobStatus(attempt.jobId()).purgeStatus()).isEqualTo("PURGE_COMPLETE");
    }

    @Test void unavailableWorkerCleanupStaysPendingAndRealRestartCompletesIt() throws Exception {
        var attempt=admission();
        CountDownLatch actualStageReturned=new CountDownLatch(1);
        CountDownLatch senderMayReturn=new CountDownLatch(1);
        ExecutorService tasks=Executors.newSingleThreadExecutor();
        var sender=tasks.submit(() -> {
            supervisor.begin(attempt.jobId(),attempt.attemptId());
            try {
                var grant=grants.issue(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
                client.triggerBoundWorkerStage(grant,grants.executionSession(attempt.jobId(),attempt.attemptId()),
                    supervisor.requireReady(attempt.jobId(),attempt.attemptId()));
                actualStageReturned.countDown();
                if(!senderMayReturn.await(25,TimeUnit.SECONDS)) throw new IllegalStateException("Sender barrier expired");
                return true;
            } finally { supervisor.end(attempt.jobId()); }
        });
        try {
            assertThat(actualStageReturned.await(10,TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> rag.cancelJob(attempt.jobId())).isInstanceOf(IllegalStateException.class);
            assertThat(purge.retryCleanup(attempt.jobId())).isFalse();
            assertThat(rag.getJobStatus(attempt.jobId()).status()).isEqualTo("RECOVERY_REQUIRED");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by=?",
                Integer.class,attempt.attemptId())).isEqualTo(1);
            // A dead Python process alone cannot certify a still-active sender scope.
            assertThat(supervisor.proveStopped(attempt.jobId())).isFalse();
            senderMayReturn.countDown();
            assertThat(sender.get(5,TimeUnit.SECONDS)).isTrue();
            recovery.reconcile();
            assertThat(Set.of("ABORTED", "CANCELLED")).contains(rag.getJobStatus(attempt.jobId()).status());
            assertThat(rag.getJobStatus(attempt.jobId()).purgeStatus()).isEqualTo("PURGE_COMPLETE");
            assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST"))
                .isInstanceOf(IllegalStateException.class);
        } finally {
            senderMayReturn.countDown();
            tasks.shutdownNow();
            assertThat(tasks.awaitTermination(15,TimeUnit.SECONDS)).isTrue();
        }
        // Recovery never starts another Python process or resumes the failed job.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isEqualTo(1);
    }

    @Test void actualAuthorityLockTimeoutAndPublicationSavepointRollbackDenyActivation() throws Exception {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        long owner=Long.parseLong(rag.findSource(attempt.sourceId()).orElseThrow().sourceId());
        try(var blocker=adminConnection()) {
            blocker.setAutoCommit(false);
            try(var lock=blocker.prepareStatement("SELECT source_id FROM public.vault_rag_source_state WHERE source_id=? FOR UPDATE")) {
                lock.setLong(1,owner);lock.executeQuery().close();
            }
            assertThatThrownBy(() -> rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null))
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
            blocker.rollback();
        }
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE is_active",Integer.class)).isZero();
        assertThat(rag.getJobStatus(attempt.jobId()).status()).isEqualTo("RECOVERY_REQUIRED");
        assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM public.vault_rag_source_state WHERE source_id=?",
            UUID.class,owner)).isEqualTo(attempt.attemptId());
        // Nested/savepoint publication is rejected at entry before any trusted durable work.
        assertThatThrownBy(() -> new TransactionTemplate(transactions).executeWithoutResult(status ->
            entityManager.unwrap(org.hibernate.Session.class).doWork(connection -> {
                var savepoint=connection.setSavepoint();
                try { rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null); }
                finally { connection.rollback(savepoint);connection.releaseSavepoint(savepoint); }
            }))).isInstanceOf(IllegalStateException.class).hasMessageContaining("ambient");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE is_active",Integer.class)).isZero();
        assertThatThrownBy(() -> rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null))
            .isInstanceOf(IllegalStateException.class);
        rag.cancelJob(attempt.jobId());
        var manual=RagTestCommands.index(rag,attempt.sourceId());
        jobs.executeSyntheticStage(manual.jobId(),manual.attemptId(),"SYNTHETIC_TEST");
        assertThat(rag.finalizeGeneration(manual.jobId(),manual.attemptId(),null).active()).isTrue();
    }

    @Test void workerSqlRejectsMalformedAndSameCountSubstitutedDependencyManifests() throws Exception {
        var root=approvedSource();var child=approvedSource();var outsider=approvedSource();
        rag.addDependency(root.id(),child.id());
        var attempt=RagTestCommands.index(rag,root.id());
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        var grant=issuedGrant(attempt);
        String original=jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,attempt.jobId());
        var mapper=new ObjectMapper();
        try(var connection=workerConnection()) {
            assertWorkerAccepts(connection,grant);
            for(String variant:List.of("missing","extra","duplicate","malformed","substituted","topology")) {
                var manifest=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(original);
                var nodes=(com.fasterxml.jackson.databind.node.ArrayNode)manifest.path("nodes");
                switch(variant) {
                    case "missing" -> nodes.remove(1);
                    case "extra" -> manifest.put("unreviewed",true);
                    case "duplicate" -> nodes.add(nodes.get(0).deepCopy());
                    case "malformed" -> ((com.fasterxml.jackson.databind.node.ObjectNode)nodes.get(0)).put("epoch","unknown");
                    case "substituted" -> ((com.fasterxml.jackson.databind.node.ObjectNode)nodes.get(1)).put("source_id",outsider.id());
                    case "topology" -> ((com.fasterxml.jackson.databind.node.ArrayNode)manifest.path("edges")).removeAll();
                    default -> throw new AssertionError();
                }
                String altered=mapper.writeValueAsString(manifest);
                // The immutable admitted closure blocks the original attack before acceptance.
                assertThatThrownBy(() -> jdbcTemplate.update("UPDATE rag.job_leases SET dependency_manifest=? WHERE job_id=?",
                    altered,attempt.jobId())).as(variant).isInstanceOf(org.springframework.dao.DataAccessException.class);
                assertThat(jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",
                    String.class,attempt.jobId())).isEqualTo(original);
                assertWorkerDenies(connection,attempt,grant.inputDigest(),RagSourceAuthority.digest(altered),grant.token());
            }
        }
    }

    @Test void actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants() throws Exception {
        var attempt=admission();
        jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        var grant=issuedGrant(attempt);
        assertThat(rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null).active()).isTrue();
        UUID before=jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class);
        try(var restarted=new org.springframework.boot.builder.SpringApplicationBuilder(
                com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class)
                .run("--server.port=0","--spring.datasource.url="+POSTGRES.getJdbcUrl(),
                    "--spring.datasource.username=rag_runtime","--spring.datasource.password="+AbstractRagIntegrationTest.PASSWORD,
                    "--spring.flyway.enabled=false",
                    "--ppv.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==")) {
            assertThat(jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class)).isNotEqualTo(before);
            var restartedRag=restarted.getBean(RagOperations.class);
            // ADR-0020 preserves valid completed indexes and consent across a normal boot.
            assertThat(restartedRag.findSource(attempt.sourceId()).orElseThrow().accessState()).isEqualTo(RagAccessState.APPROVED);
            assertThat(restartedRag.getJobStatus(attempt.jobId()).status()).isEqualTo("COMPLETED");
            assertThat(restartedRag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isEqualTo(1);
            try(var connection=workerConnection()) {
                assertWorkerDenies(connection,attempt,grant.inputDigest(),grant.scopeDigest(),grant.token());
            }
            assertThatThrownBy(() -> RagTestCommands.index(rag,attempt.sourceId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("identity");
            restarted.getBean(RagRecoveryService.class).reconcile();
            assertThat(restartedRag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");
        } finally {
            // Test-only cached Spring context restoration, after the second context is closed.
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",before);
        }
    }

    @Test void actualPostgresDeadlockAbortsOneTransactionWithoutPartialPublication() throws Exception {
        var attempt=admission();jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        long owner=Long.parseLong(rag.findSource(attempt.sourceId()).orElseThrow().sourceId());
        try(var blocker=adminConnection();var executor=Executors.newSingleThreadExecutor()) {
            blocker.setAutoCommit(false);
            int blockerPid=backendPid(blocker);
            try(var statement=blocker.createStatement()) {
                statement.execute("SET LOCAL deadlock_timeout='100ms'");
                statement.execute("SELECT source_id FROM public.vault_rag_source_state WHERE source_id="+owner+" FOR UPDATE");
                var finalizer=executor.submit(() -> rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null));
                try {
                    awaitCondition(() -> jdbcTemplate.queryForObject("""
                        SELECT count(*) FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid))
                            AND query LIKE '%vault_rag_lock_reserved%'
                        """,Integer.class,blockerPid)==1);
                    assertThatThrownBy(() -> statement.execute("SELECT pg_advisory_xact_lock("+RagDependencyGraph.LOCK_ID+")"))
                        .isInstanceOf(SQLException.class).extracting(error -> ((SQLException)error).getSQLState()).isEqualTo("40P01");
                } finally { blocker.rollback(); }
                assertThat(finalizer.get(10,TimeUnit.SECONDS).active()).isTrue();
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=? AND is_active",
                    Integer.class,attempt.jobId())).isEqualTo(1);
            }
        }
    }

    @Test void cachedCompletedGenerationCannotReportSupersededPublicationAsActive() {
        var first=admission();
        jobs.executeSyntheticStage(first.jobId(),first.attemptId(),"SYNTHETIC_TEST");
        var original=rag.finalizeGeneration(first.jobId(),first.attemptId(),null);
        var second=RagTestCommands.index(rag,first.sourceId());
        jobs.executeSyntheticStage(second.jobId(),second.attemptId(),"SYNTHETIC_TEST");
        try(var executor=Executors.newSingleThreadExecutor()) {
            assertThatThrownBy(() -> new TransactionTemplate(transactions).executeWithoutResult(status -> {
                var cached=entityManager.createQuery("SELECT g FROM RagIndexGeneration g WHERE g.generationId=:generation",
                    com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration.class)
                    .setParameter("generation",original.generationId()).getSingleResult();
                assertThat(cached.isActive()).isTrue();
                try { assertThat(executor.submit(() -> rag.finalizeGeneration(second.jobId(),second.attemptId(),null))
                    .get(10,TimeUnit.SECONDS).active()).isTrue(); }
                catch(Exception failure) { throw new AssertionError("Competing actual publication failed",failure); }
                rag.finalizeGeneration(first.jobId(),first.attemptId(),null);
            })).isInstanceOf(IllegalStateException.class).hasMessageContaining("ambient");
        }
        var repeated=rag.finalizeGeneration(first.jobId(),first.attemptId(),null);
        assertThat(repeated.active()).isFalse();
        assertThat(repeated.status()).isEqualTo("OUTDATED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE is_active",Integer.class)).isEqualTo(1);
    }

    @Test void actualWorkflowBenchmarkRecordsOperationLatencyAndProcessResources() throws Exception {
        long source=approvedSource().id();
        long[] admissionNs=new long[16],stageNs=new long[16],publishNs=new long[16],totalNs=new long[16];
        long heapPeak=0;
        var processIds=new HashSet<String>();
        for(int i=0;i<16;i++) {
            long start=System.nanoTime();
            var attempt=RagTestCommands.index(rag,source);long admitted=System.nanoTime();
            jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");long staged=System.nanoTime();
            processIds.add(jdbcTemplate.queryForObject("""
                SELECT (worker_identity->>'process_id') || ':' || (worker_identity->>'creation_file_time')
                FROM rag.job_leases WHERE job_id=?
                """,String.class,attempt.jobId()));
            assertThat(rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null).active()).isTrue();long published=System.nanoTime();
            admissionNs[i]=admitted-start;stageNs[i]=staged-admitted;publishNs[i]=published-staged;totalNs[i]=published-start;
            heapPeak=Math.max(heapPeak,Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        }
        assertThat(processIds).hasSize(16);
        var result=new LinkedHashMap<String,Object>();
        result.put("samples",16);result.put("successful_publications",16);
        result.put("queue","one active job; repeated cold job-scoped Python startup");
        for(var entry:Map.of("admission",admissionNs,"worker_roundtrip",stageNs,"publication_guard",publishNs,"total",totalNs).entrySet()) {
            Arrays.sort(entry.getValue());result.put(entry.getKey()+"_p50_ms",entry.getValue()[7]/1e6);
            result.put(entry.getKey()+"_p95_ms",entry.getValue()[15]/1e6);
        }
        result.put("sampled_java_used_heap_peak_bytes",heapPeak);
        result.put("native_worker_peak_resources","NOT_VERIFIED by this test; see separate single-job benchmark");
        var destination=repository().resolve("logs/phase17-alternative-b-implementation-2026-10-15/legacy-workflow-benchmark.json");
        java.nio.file.Files.createDirectories(destination.getParent());
        java.nio.file.Files.writeString(destination,new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    @Test void declaredRestoreOrUncertainSourceHistorySuspendsIndexUntilRenewedConsent() throws Exception {
        var source = approvedSource();
        var attempt = RagTestCommands.index(rag, source.id());
        jobs.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        var published = rag.finalizeGeneration(attempt.jobId(), attempt.attemptId(), null);
        assertThat(published.active()).isTrue();
        assertThat(rag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");

        // Also have an unfinished / in-flight job on an admitted source
        var inFlight = RagTestCommands.index(rag, source.id());
        assertThat(inFlight.jobId()).isNotEqualTo(attempt.jobId());

        // Supported Operator Declared-Restore Procedure:
        // 1. Resolve unfinished work under declared restore: cancel in-flight jobs
        rag.cancelJob(inFlight.jobId());
        assertThat(rag.getJobStatus(inFlight.jobId()).status()).isEqualTo("CANCELLED");

        // 2. Declare restore / revoke standing consent for affected sources via supported revokeSource API
        rag.revokeSource(source.id());

        // Also simulate database watermark change from restore
        long ownerId = Long.parseLong(source.sourceId());
        try (var admin = adminConnection(); var statement = admin.createStatement()) {
            statement.executeUpdate("UPDATE public.vault_rag_source_state SET consent_reset_version = consent_reset_version + 1 WHERE source_id=" + ownerId);
        }

        // 3. Verify immediate index suspension: existing CURRENT index becomes REVOKED
        assertThat(rag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("REVOKED");

        // Stale consent commands are rejected
        var staleConsent = RagTestCommands.consent(source, Set.of("title", "format"));
        assertThatThrownBy(() -> rag.approveSource(source.id(), staleConsent))
                .isInstanceOf(com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException.class);

        // 4. Operator queries fresh source state and provides explicit renewed consent
        var freshSource = rag.findSource(source.id()).orElseThrow();
        assertThat(freshSource.authorizationEpoch()).isGreaterThan(source.authorizationEpoch());

        var renewedConsent = RagTestCommands.consent(freshSource, Set.of("title", "format"));
        var renewed = rag.approveSource(source.id(), renewedConsent);
        assertThat(renewed.isEligible()).isTrue();

        // 5. Operator admits a new indexing job and publishes fresh generation
        var newAttempt = RagTestCommands.index(rag, source.id());
        assertThat(newAttempt.jobId()).isNotEqualTo(attempt.jobId());
        jobs.executeSyntheticStage(newAttempt.jobId(), newAttempt.attemptId(), "SYNTHETIC_TEST");
        var newPublished = rag.finalizeGeneration(newAttempt.jobId(), newAttempt.attemptId(), null);
        assertThat(newPublished.active()).isTrue();
        assertThat(rag.getJobStatus(newAttempt.jobId()).indexState()).isEqualTo("CURRENT");
    }

    @Test void staleListenerOrPortCollisionDeniesWorkerBindingWithZeroSourceDispatch() throws Exception {
        var attempt = admission();
        int port = client.port();
        var receivedExecutionRequests = new java.util.concurrent.atomic.AtomicInteger();
        var receivedExecutionBytes = new java.util.concurrent.atomic.AtomicLong();
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/v1/worker/execute".equals(path) || "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                receivedExecutionRequests.incrementAndGet();
                byte[] body = exchange.getRequestBody().readAllBytes();
                receivedExecutionBytes.addAndGet(body.length);
            }
            byte[] resp = "{\"error\":\"stale_listener\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });
        server.start();
        try {
            // Invoke the production execution entry against the responding stale/wrong binding
            assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_STAGE"))
                    .isInstanceOf(RuntimeException.class);

            // Zero execution-body requests or bytes were transferred
            assertThat(receivedExecutionRequests.get()).isZero();
            assertThat(receivedExecutionBytes.get()).isZero();

            // No grant was consumed and no output was staged
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?", Integer.class, attempt.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants WHERE job_id=? AND consumed_at IS NOT NULL", Integer.class, attempt.jobId())).isZero();

            // Safe abort cleaned up the failed startup and released reservations
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?", String.class, attempt.jobId())).isEqualTo("ABORTED");
            var freshAttempt = RagTestCommands.index(rag, attempt.sourceId());
            assertThat(freshAttempt.jobId()).isNotNull();
            rag.cancelJob(freshAttempt.jobId());
        } finally {
            server.stop(0);
        }
    }

    @Test void liveConsumerUnprovenTerminationRetainsReservationAndPreventsSealUntilQuiescence() throws Exception {
        var source = approvedSource();
        var attempt = RagTestCommands.index(rag, source.id());
        supervisor.begin(attempt.jobId(), attempt.attemptId());
        // Worker is running and session.dispatching is true (outstanding launch/send task).
        try {
            // Attempting to cancel while consumer is live and dispatching forces unsuccessful/unproven stop outcome
            assertThatThrownBy(() -> rag.cancelJob(attempt.jobId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Safe cancellation is not yet verified");

            // Verify lease is NOT in a terminal state (COMPLETED, CANCELLED, ABORTED)
            String status = jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",
                String.class, attempt.jobId());
            assertThat(Set.of("COMPLETED", "CANCELLED", "ABORTED")).doesNotContain(status);

            // Staging is NOT sealed
            assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                Boolean.class, attempt.jobId())).isFalse();

            // Source reservation is STILL HELD: competing admission is rejected
            assertThatThrownBy(() -> RagTestCommands.index(rag, source.id()))
                .isInstanceOf(RuntimeException.class);

            // Signal task completion / quiescence
            supervisor.end(attempt.jobId());
            assertThat(supervisor.stopAndVerify(attempt.jobId())).isTrue();

            // Now that quiescence and verified stop are proven, retry cancellation
            rag.cancelJob(attempt.jobId());

            // Verify terminal outcome, staging sealed, and reservation released
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",
                String.class, attempt.jobId())).isIn("CANCELLED", "ABORTED");
            assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                Boolean.class, attempt.jobId())).isTrue();

            // Source reservation is now released: fresh admission succeeds
            var nextAttempt = RagTestCommands.index(rag, source.id());
            assertThat(nextAttempt.jobId()).isNotNull();
            rag.cancelJob(nextAttempt.jobId());
        } finally {
            supervisor.end(attempt.jobId());
            supervisor.stopAndVerify(attempt.jobId());
            try { rag.cancelJob(attempt.jobId()); } catch (RuntimeException ignored) {}
        }
    }

    @Test void workerTimelyLastDeferredGateAllowsLateStageCommitThenExpiredVerificationRejectsPublication() throws Exception {
        var attempt = shortBudgetFixture(approvedSource());
        supervisor.begin(attempt.jobId(), attempt.attemptId());
        var grant = grants.issue(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");
        UUID session = grants.executionSession(attempt.jobId(), attempt.attemptId());
        long barrier = 61_703_001L;
        try (var blocker = adminConnection(); var statement = blocker.createStatement();
             var executor = Executors.newSingleThreadExecutor()) {
            statement.execute("SELECT pg_advisory_lock(" + barrier + ")");
            statement.execute("""
                CREATE FUNCTION rag.test_worker_e1_gate() RETURNS trigger LANGUAGE plpgsql
                SECURITY DEFINER SET search_path=pg_catalog AS $$
                BEGIN PERFORM pg_advisory_xact_lock(61703001); RETURN NULL; END $$
                """);
            statement.execute("REVOKE ALL ON FUNCTION rag.test_worker_e1_gate() FROM PUBLIC,rag_worker,rag_runtime");
            statement.execute("""
                CREATE CONSTRAINT TRIGGER zz_worker_deferred_gate AFTER INSERT ON rag.staged_outputs
                DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.test_worker_e1_gate()
                """);
            try {
                int pid = backendPid(blocker);
                waitForFinalizationWindow(attempt);
                var execution = executor.submit(() -> client.triggerBoundWorkerStage(grant, session,
                    supervisor.requireReady(attempt.jobId(), attempt.attemptId())));
                try {
                    awaitCondition(() -> {
                        boolean pending = jdbcTemplate.queryForObject("""
                            SELECT count(*) FROM pg_stat_activity WHERE usename='rag_worker'
                                AND ?=ANY(pg_blocking_pids(pid))
                            """, Integer.class, pid) == 1;
                        if (!pending && execution.isDone()) {
                            try { execution.get(1, TimeUnit.SECONDS); }
                            catch (Exception failure) {
                                throw new AssertionError("Actual sender failed before deferred gate", failure);
                            }
                            throw new AssertionError("Actual sender returned without entering deferred gate");
                        }
                        return pending;
                    });
                    awaitExpired(attempt.jobId());
                } finally { statement.execute("SELECT pg_advisory_unlock(" + barrier + ")"); }

                execution.get(8, TimeUnit.SECONDS);
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",
                    Integer.class, attempt.jobId())).isEqualTo(1);

                // Proven worker termination and quiescence must precede finalization
                supervisor.end(attempt.jobId());
                assertThat(supervisor.stopAndVerify(attempt.jobId())).isTrue();

                // Now publication reaches and rejects at the expired verification gate
                assertThatThrownBy(() -> rag.finalizeGeneration(attempt.jobId(), attempt.attemptId(), null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("expired");

                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE is_active",
                    Integer.class)).isZero();

                // Reservation remains held until safe abort
                assertThatThrownBy(() -> RagTestCommands.index(rag, attempt.sourceId()))
                    .isInstanceOf(RuntimeException.class);
            } finally {
                statement.execute("SELECT pg_advisory_unlock(" + barrier + ")");
                statement.execute("DROP TRIGGER IF EXISTS zz_worker_deferred_gate ON rag.staged_outputs");
                statement.execute("DROP FUNCTION IF EXISTS rag.test_worker_e1_gate()");
            }
        } finally {
            supervisor.end(attempt.jobId());
            supervisor.stopAndVerify(attempt.jobId());
            rag.cancelJob(attempt.jobId());
            // Safe abort releases reservation
            var fresh = RagTestCommands.index(rag, attempt.sourceId());
            assertThat(fresh.jobId()).isNotNull();
            rag.cancelJob(fresh.jobId());
        }
    }

    @Test void concurrentFinishAbortFirstThenCancelReconcilesSafeTerminalOutcome() throws Exception {
        var source = approvedSource();
        var attempt = RagTestCommands.index(rag, source.id());
        supervisor.begin(attempt.jobId(), attempt.attemptId());
        supervisor.end(attempt.jobId());
        assertThat(supervisor.stopAndVerify(attempt.jobId())).isTrue();

        try (var blocker = adminConnection(); var statement = blocker.createStatement();
             var executor = Executors.newFixedThreadPool(2)) {
            statement.execute("SELECT pg_advisory_lock(" + RagDependencyGraph.LOCK_ID + ")");
            int pid = backendPid(blocker);
            try {
                // First actor: abortJob(jobId, false)
                var abortTask = executor.submit(() -> purge.abortJob(attempt.jobId(), false));
                awaitCondition(() -> jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity WHERE ? = ANY(pg_blocking_pids(pid))
                    """, Integer.class, pid) == 1);

                // Second actor: cancelJob(jobId)
                var cancelTask = executor.submit(() -> { rag.cancelJob(attempt.jobId()); return true; });
                awaitCondition(() -> jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity WHERE ? = ANY(pg_blocking_pids(pid))
                    """, Integer.class, pid) == 2);

                // Release barrier to let both finishers race through
                statement.execute("SELECT pg_advisory_unlock(" + RagDependencyGraph.LOCK_ID + ")");

                // Assert both finish actors succeed without throwing false denials
                assertThat(abortTask.get(10, TimeUnit.SECONDS)).isTrue();
                assertThat(cancelTask.get(10, TimeUnit.SECONDS)).isTrue();

                // Durable lease is terminal, staging is sealed, purge status is PURGE_COMPLETE
                var status = rag.getJobStatus(attempt.jobId());
                assertThat(Set.of("ABORTED", "CANCELLED")).contains(status.status());
                assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                    Boolean.class, attempt.jobId())).isTrue();
                assertThat(status.purgeStatus()).isEqualTo("PURGE_COMPLETE");
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",
                    Integer.class, attempt.jobId())).isZero();

                // Reservation is released: competing admission succeeds
                var fresh = RagTestCommands.index(rag, source.id());
                assertThat(fresh.jobId()).isNotNull();
                rag.cancelJob(fresh.jobId());
            } finally {
                statement.execute("SELECT pg_advisory_unlock_all()");
                executor.shutdownNow();
            }
        }
    }

    @Test void concurrentFinishCancelFirstThenAbortReconcilesSafeTerminalOutcome() throws Exception {
        var source = approvedSource();
        var attempt = RagTestCommands.index(rag, source.id());
        supervisor.begin(attempt.jobId(), attempt.attemptId());
        supervisor.end(attempt.jobId());
        assertThat(supervisor.stopAndVerify(attempt.jobId())).isTrue();

        try (var blocker = adminConnection(); var statement = blocker.createStatement();
             var executor = Executors.newFixedThreadPool(2)) {
            statement.execute("SELECT pg_advisory_lock(" + RagDependencyGraph.LOCK_ID + ")");
            int pid = backendPid(blocker);
            try {
                // First actor: cancelJob(jobId)
                var cancelTask = executor.submit(() -> { rag.cancelJob(attempt.jobId()); return true; });
                awaitCondition(() -> jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity WHERE ? = ANY(pg_blocking_pids(pid))
                    """, Integer.class, pid) == 1);

                // Second actor: abortJob(jobId, false)
                var abortTask = executor.submit(() -> purge.abortJob(attempt.jobId(), false));
                awaitCondition(() -> jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM pg_stat_activity WHERE ? = ANY(pg_blocking_pids(pid))
                    """, Integer.class, pid) == 2);

                // Release barrier to let both finishers race through
                statement.execute("SELECT pg_advisory_unlock(" + RagDependencyGraph.LOCK_ID + ")");

                // Assert both finish actors succeed without throwing false denials
                assertThat(cancelTask.get(10, TimeUnit.SECONDS)).isTrue();
                assertThat(abortTask.get(10, TimeUnit.SECONDS)).isTrue();

                // Durable lease is terminal, staging is sealed, purge status is PURGE_COMPLETE
                var status = rag.getJobStatus(attempt.jobId());
                assertThat(Set.of("ABORTED", "CANCELLED")).contains(status.status());
                assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                    Boolean.class, attempt.jobId())).isTrue();
                assertThat(status.purgeStatus()).isEqualTo("PURGE_COMPLETE");
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",
                    Integer.class, attempt.jobId())).isZero();

                // Reservation is released: competing admission succeeds
                var fresh = RagTestCommands.index(rag, source.id());
                assertThat(fresh.jobId()).isNotNull();
                rag.cancelJob(fresh.jobId());
            } finally {
                statement.execute("SELECT pg_advisory_unlock_all()");
                executor.shutdownNow();
            }
        }
    }



    /** Initial immutable clock fixture; all later services, worker and SQL gates remain real. */
    private RagJobAdmissionResult shortBudgetFixture(RagSourceView source) {
        var prepared=authority.prepareSource(source.id(),Map.of());
        var tx=new TransactionTemplate(transactions);tx.setTimeout(10);
        return tx.execute(status -> {
            var snapshot=authority.lockPrepared(prepared,null,true);var root=snapshot.root();
            Instant now=authority.databaseTime(),deadline=now.plusSeconds(8);
            UUID job=UUID.randomUUID(),attempt=UUID.randomUUID();
            String token=UUID.randomUUID().toString().replace("-","")+UUID.randomUUID().toString().replace("-","");
            var lease=new RagJobLease(job,attempt,source.id(),"ADMITTED",token,deadline,
                root.getAuthorizationEpoch(),root.getFieldPolicyVersion(),root.getApprovedFieldsText(),
                root.getSourceRevisionHash(),root.getDescriptionRevisionHash(),snapshot.manifest(),now);
            lease.bindController(Map.of("process_id",ProcessHandle.current().pid(),
                "started_at",ProcessHandle.current().info().startInstant().orElseThrow().toString(),
                "runtime_id",runtime.instance().toString()));
            leases.saveAndFlush(lease);
            var owners=new TreeSet<Long>();prepared.proofs().values().forEach(proof -> owners.add(proof.ownerId()));
            reservations.reserve(owners,attempt);
            runs.saveAndFlush(new RagRun(UUID.randomUUID(),job,source.id(),root.getAuthorizationEpoch(),"ADMITTED","{}","NONE",now));
            return new RagJobAdmissionResult(job,attempt,source.id(),token,deadline,root.getAuthorizationEpoch());
        });
    }
    private void waitForFinalizationWindow(RagJobAdmissionResult attempt) {
        assertThat(remainingMillis(attempt)).as("actual startup/staging must finish before the short immutable deadline")
            .isGreaterThan(2_000);
        awaitCondition(() -> remainingMillis(attempt)<=1_800);
        assertThat(remainingMillis(attempt)).isPositive();
    }
    private Long remainingMillis(RagJobAdmissionResult attempt) {
        return jdbcTemplate.queryForObject("""
            SELECT (EXTRACT(EPOCH FROM (leased_until-clock_timestamp()))*1000)::bigint
            FROM rag.job_leases WHERE job_id=? AND attempt_id=?
            """,Long.class,attempt.jobId(),attempt.attemptId());
    }
    private RagWorkerGrant issuedGrant(RagJobAdmissionResult attempt) {
        var row=jdbcTemplate.queryForMap("""
            SELECT stage_type,input_digest,scope_digest,output_digest FROM rag.worker_grants
            WHERE job_id=? AND attempt_id=?
            """,attempt.jobId(),attempt.attemptId());
        return new RagWorkerGrant(attempt.jobId(),attempt.attemptId(),attempt.leaseToken(),
            (String)row.get("stage_type"),"",(String)row.get("input_digest"),
            (String)row.get("scope_digest"),(String)row.get("output_digest"));
    }
    private String durableReceipt(RagJobAdmissionResult attempt) {
        return jdbcTemplate.queryForObject("SELECT payload FROM rag.staged_outputs WHERE job_id=? AND attempt_id=?",
            String.class,attempt.jobId(),attempt.attemptId());
    }
    private static Connection adminConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
    }
    private static int backendPid(Connection connection) throws SQLException {
        try(var query=connection.createStatement();var rows=query.executeQuery("SELECT pg_backend_pid()")) {
            assertThat(rows.next()).isTrue();return rows.getInt(1);
        }
    }
    private static void assertWorkerAccepts(Connection connection,RagWorkerGrant grant) throws SQLException {
        try(var query=connection.prepareStatement("SELECT rag.verify_worker_grant(?,?,?,?,?,?)")) {
            query.setObject(1,grant.jobId());query.setObject(2,grant.attemptId());query.setString(3,grant.token());
            query.setString(4,grant.stageType());query.setString(5,grant.inputDigest());query.setString(6,grant.scopeDigest());
            try(var rows=query.executeQuery()) { assertThat(rows.next()).isTrue();assertThat(rows.getBoolean(1)).isTrue(); }
        }
    }
    private static void assertWorkerDenies(Connection connection,RagJobAdmissionResult attempt,String input,String scope,String token) throws SQLException {
        try(var query=connection.prepareStatement("SELECT rag.verify_worker_grant(?,?,?,?,?,?)")) {
            query.setObject(1,attempt.jobId());query.setObject(2,attempt.attemptId());query.setString(3,token);
            query.setString(4,"SYNTHETIC_TEST");query.setString(5,input);query.setString(6,scope);
            assertThatThrownBy(query::executeQuery).isInstanceOf(SQLException.class).hasMessageContaining("denied");
        }
    }
    private void stage(Connection connection,com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant grant,String receipt) throws Exception {
        try(var statement=connection.prepareStatement("SELECT rag.stage_worker_receipt(?,?,?,?)")) {
            statement.setObject(1,grant.jobId());statement.setObject(2,grant.attemptId());statement.setString(3,grant.token());statement.setString(4,receipt);
            statement.executeQuery().close();
        }
    }
    private void awaitExpired(UUID job) throws Exception {
        awaitCondition(()->Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT clock_timestamp()>=leased_until FROM rag.job_leases WHERE job_id=?",Boolean.class,job)));
    }
    private void awaitCondition(java.util.function.BooleanSupplier condition) {
        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(10)).until(condition::getAsBoolean);
    }
    private static String sqlState(Throwable failure) {
        for(Throwable cause=failure;cause!=null;cause=cause.getCause())
            if(cause instanceof SQLException sql) return sql.getSQLState();
        return null;
    }
}
