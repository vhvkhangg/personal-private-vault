package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;

import java.sql.DriverManager;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real native/HTTP/worker SQL and physical PostgreSQL COMMIT, followed by an
 * injected Java acknowledgement exception. This is not packet-level ACK loss.
 */
@Timeout(90)
@TestPropertySource(properties="ppv.rag.reconciliation-delay-ms=3600000")
@Import(RagCommitAcknowledgementRecoveryIntegrationTest.AcknowledgementFault.class)
class RagCommitAcknowledgementRecoveryIntegrationTest extends AbstractEnabledRagIntegrationTest {
    private static final AtomicReference<Fault> ARMED=new AtomicReference<>();
    private record Fault(UUID job,String terminalStatus) {}

    @Autowired RagWorkerSupervisor supervisor;
    @Autowired RagRecoveryService recovery;

    @AfterEach void disarm() { ARMED.set(null); }

    @Test void committedPublicationWithInjectedLostJavaAcknowledgementRetainsCurrentIndexAndReconcilesHandles() {
        RagSourceView source=approved();
        RagJobAdmissionResult attempt=RagTestCommands.index(enabledRag,source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        assertThat(supervisor.proveStopped(attempt.jobId())).isTrue();
        ARMED.set(new Fault(attempt.jobId(),"COMPLETED"));

        assertThatThrownBy(()->enabledRag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null))
                .isInstanceOf(TransactionSystemException.class).hasMessageContaining("Injected Java acknowledgement loss");
        assertThat(ARMED.get()).isNull();
        assertTerminalReleased(attempt,"COMPLETED");
        UUID generation=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE job_id=? AND is_active",
                UUID.class,attempt.jobId());
        assertThat(supervisor.provenStoppedJobs()).contains(attempt.jobId());

        recovery.reconcile();
        assertThat(supervisor.provenStoppedJobs()).doesNotContain(attempt.jobId());
        assertThat(enabledRag.getJobStatus(attempt.jobId()).indexState()).isEqualTo("CURRENT");
        assertThat(enabledRag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null).generationId()).isEqualTo(generation);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,attempt.jobId())).isEqualTo(1);
    }

    @Test void committedCancellationWithInjectedLostJavaAcknowledgementIsNotResumedAndReconcilesHandles() {
        RagSourceView source=approved();
        RagJobAdmissionResult attempt=RagTestCommands.index(enabledRag,source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        ARMED.set(new Fault(attempt.jobId(),"CANCELLED"));

        // The cancellation API reports a source-free conflict for an unknown
        // acknowledgement; the next authoritative read resolves its durable C.
        assertThatThrownBy(()->enabledRag.cancelJob(attempt.jobId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Safe cancellation is not yet verified");
        assertThat(ARMED.get()).isNull();
        assertTerminalReleased(attempt,"CANCELLED");
        assertThat(supervisor.provenStoppedJobs()).contains(attempt.jobId());
        recovery.reconcile();
        assertThat(supervisor.provenStoppedJobs()).doesNotContain(attempt.jobId());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,attempt.jobId())).isZero();
        assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("CANCELLED");
    }

    @Test void wrongExpectedDigestBeforePublicationTransactionDoesNotAbortTheValidStagedAttempt() {
        RagSourceView source=approved();
        RagJobAdmissionResult attempt=RagTestCommands.index(enabledRag,source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST");
        assertThatThrownBy(()->enabledRag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),"wrong-digest"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,attempt.jobId())).isEqualTo("ACTIVE");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,attempt.jobId())).isEqualTo(1);
        assertThat(enabledRag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null).active()).isTrue();
    }

    @Test void ordinaryDependencyRemovalAndReadditionPreserveConsentButRequireManualReindex() {
        RagSourceView root=approved();
        RagSourceView child=approved();
        enabledRag.addDependency(root.id(),child.id());
        RagJobAdmissionResult original=RagTestCommands.index(enabledRag,root.id());
        enabledRag.executeSyntheticStage(original.jobId(),original.attemptId(),"SYNTHETIC_TEST");
        enabledRag.finalizeGeneration(original.jobId(),original.attemptId(),null);
        assertThat(enabledRag.getJobStatus(original.jobId()).indexState()).isEqualTo("CURRENT");

        assertThat(jdbcTemplate.update("DELETE FROM rag.dependencies WHERE parent_source_id=? AND child_source_id=?",root.id(),child.id())).isEqualTo(1);
        assertThat(enabledRag.getJobStatus(original.jobId()).indexState()).isEqualTo("OUTDATED");
        enabledRag.addDependency(root.id(),child.id());
        assertThat(enabledRag.getJobStatus(original.jobId()).indexState()).isEqualTo("OUTDATED");
        assertThat(enabledRag.findSource(root.id()).orElseThrow().accessState()).isEqualTo(com.vhvkhangg.personalprivatevault.rag.source.RagAccessState.APPROVED);
        recovery.reconcile();
        assertThat(enabledRag.getJobStatus(original.jobId()).status()).isEqualTo("COMPLETED");
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.index_generations WHERE job_id=?",String.class,original.jobId())).isEqualTo("OUTDATED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isEqualTo(1);

        RagJobAdmissionResult manual=RagTestCommands.index(enabledRag,root.id());
        enabledRag.executeSyntheticStage(manual.jobId(),manual.attemptId(),"SYNTHETIC_TEST");
        assertThat(enabledRag.finalizeGeneration(manual.jobId(),manual.attemptId(),null).active()).isTrue();
        assertThat(enabledRag.getJobStatus(manual.jobId()).indexState()).isEqualTo("CURRENT");
    }

    @Test void oldDependentGenerationCleanupCannotCancelNewReservedScopeThatExcludesTheRevokedItem() {
        RagSourceView root=approved();
        RagSourceView oldChild=approved();
        enabledRag.addDependency(root.id(),oldChild.id());
        RagJobAdmissionResult historical=RagTestCommands.index(enabledRag,root.id());
        enabledRag.executeSyntheticStage(historical.jobId(),historical.attemptId(),"SYNTHETIC_TEST");
        enabledRag.finalizeGeneration(historical.jobId(),historical.attemptId(),null);

        assertThat(jdbcTemplate.update("DELETE FROM rag.dependencies WHERE parent_source_id=? AND child_source_id=?",root.id(),oldChild.id())).isEqualTo(1);
        RagJobAdmissionResult replacement=RagTestCommands.index(enabledRag,root.id());
        enabledRag.executeSyntheticStage(replacement.jobId(),replacement.attemptId(),"SYNTHETIC_TEST");
        enabledRag.revokeSource(oldChild.id());
        assertThat(enabledRag.getJobStatus(historical.jobId()).indexState()).isEqualTo("REVOKED");
        recovery.reconcile();

        assertThat(enabledRag.getJobStatus(historical.jobId()).status()).isEqualTo("COMPLETED");
        assertThat(enabledRag.getJobStatus(historical.jobId()).purgeStatus()).isEqualTo("PURGE_COMPLETE");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,historical.jobId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants WHERE job_id=?",Integer.class,historical.jobId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT source_revision_hash='' AND dependency_manifest_digest='' FROM rag.index_generations WHERE job_id=?",Boolean.class,historical.jobId())).isTrue();
        assertThat(enabledRag.getJobStatus(replacement.jobId()).status()).isEqualTo("ACTIVE");
        assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM vault_rag_source_state WHERE source_id=?",UUID.class,Long.valueOf(root.sourceId()))).isEqualTo(replacement.attemptId());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,replacement.jobId())).isEqualTo(1);
        assertThat(enabledRag.finalizeGeneration(replacement.jobId(),replacement.attemptId(),null).active()).isTrue();
        assertThat(enabledRag.getJobStatus(replacement.jobId()).indexState()).isEqualTo("CURRENT");
    }

    @Test void lowDiskOrStorageFailureDuringPublicationRollsBackAndRetainsReservationsForSafeRecovery() throws Exception {
        RagSourceView source = approved();
        RagJobAdmissionResult attempt = RagTestCommands.index(enabledRag, source.id());
        enabledRag.executeSyntheticStage(attempt.jobId(), attempt.attemptId(), "SYNTHETIC_TEST");

        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("""
                CREATE FUNCTION rag.test_storage_full() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN RAISE EXCEPTION 'could not extend file: No space left on device' USING ERRCODE='53100'; END $$;
                """);
            statement.execute("""
                CREATE CONSTRAINT TRIGGER test_storage_full AFTER INSERT ON rag.index_generations
                DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.test_storage_full()
                """);
            try {
                // Finalization attempts to commit publication row, which triggers 53100 disk_full:
                assertThatThrownBy(() -> enabledRag.finalizeGeneration(attempt.jobId(), attempt.attemptId(), null))
                        .satisfies(failure -> assertThat(sqlState(failure)).isEqualTo("53100"));

                // Publication rolled back completely: zero index generations committed:
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",
                        Integer.class, attempt.jobId())).isZero();

                // Staging is not closed as COMPLETED; lease enters RECOVERY_REQUIRED:
                assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",
                        String.class, attempt.jobId())).isEqualTo("RECOVERY_REQUIRED");
                assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",
                        Boolean.class, attempt.jobId())).isFalse();

                // Source reservation remains intact (no partial/corrupt release):
                assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM vault_rag_source_state WHERE source_id=?",
                        UUID.class, Long.valueOf(source.sourceId()))).isEqualTo(attempt.attemptId());
            } finally {
                statement.execute("DROP TRIGGER IF EXISTS test_storage_full ON rag.index_generations");
                statement.execute("DROP FUNCTION IF EXISTS rag.test_storage_full()");
            }
        }

        // Subsequent safe finish / cancellation cleanly aborts and releases:
        enabledRag.cancelJob(attempt.jobId());
        assertThat(enabledRag.getJobStatus(attempt.jobId()).status()).isEqualTo("CANCELLED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_rag_source_state WHERE reserved_by=?",
                Integer.class, attempt.attemptId())).isZero();
    }

    private static String sqlState(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql) return sql.getSQLState();
        }
        return "";
    }

    private RagSourceView approved() {
        Long owner=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES('FILM') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) VALUES(?,'Synthetic acknowledgement fixture','MOVIE','ANIMATION',false)",owner);
        var candidate=RagTestCommands.register(enabledRag,RagSourceType.FILM,owner.toString());
        return RagTestCommands.approve(enabledRag,candidate.id(),Set.of("title"));
    }

    private void assertTerminalReleased(RagJobAdmissionResult attempt,String status) {
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,attempt.jobId())).isEqualTo(status);
        assertThat(jdbcTemplate.queryForObject("SELECT staging_closed FROM rag.job_leases WHERE job_id=?",Boolean.class,attempt.jobId())).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_rag_source_state WHERE reserved_by=?",Integer.class,attempt.attemptId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases WHERE status IN ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING')",Integer.class)).isZero();
    }

    @TestConfiguration(proxyBeanMethods=false)
    static class AcknowledgementFault {
        @Bean static BeanPostProcessor injectAfterActualCommit() {
            return new BeanPostProcessor() {
                @Override public Object postProcessAfterInitialization(Object bean,String name) {
                    if(!(bean instanceof PlatformTransactionManager delegate))return bean;
                    return new PlatformTransactionManager() {
                        @Override public TransactionStatus getTransaction(TransactionDefinition definition) {
                            return delegate.getTransaction(definition);
                        }
                        @Override public void rollback(TransactionStatus status) { delegate.rollback(status); }
                        @Override public void commit(TransactionStatus status) {
                            delegate.commit(status);
                            Fault fault=ARMED.get();
                            if(fault==null || !status.isNewTransaction())return;
                            var properties=new java.util.Properties();
                            properties.setProperty("user",POSTGRES.getUsername());
                            properties.setProperty("password",POSTGRES.getPassword());
                            properties.setProperty("connectTimeout","3");
                            properties.setProperty("socketTimeout","5");
                            try(var database=DriverManager.getConnection(POSTGRES.getJdbcUrl(),properties);
                                    var query=database.prepareStatement("SELECT status FROM rag.job_leases WHERE job_id=? AND staging_closed")) {
                                query.setQueryTimeout(5);
                                query.setObject(1,fault.job());
                                try(var rows=query.executeQuery()) {
                                    if(rows.next() && fault.terminalStatus().equals(rows.getString(1)) && ARMED.compareAndSet(fault,null))
                                        throw new TransactionSystemException("Injected Java acknowledgement loss after actual COMMIT");
                                }
                            } catch(java.sql.SQLException inspectionFailed) {
                                throw new IllegalStateException("Acknowledgement fixture inspection failed",inspectionFailed);
                            }
                        }
                    };
                }
            };
        }
    }
}
