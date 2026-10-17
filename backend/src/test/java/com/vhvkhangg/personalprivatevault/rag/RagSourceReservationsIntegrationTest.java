package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.vault.entry.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/** Actual PG/Java owner boundary; no mock lock, worker or inferred COMMIT. */
@Timeout(40)
class RagSourceReservationsIntegrationTest extends AbstractRagIntegrationTest {
    @Autowired RagOperations rag;
    @Autowired SourceIndexReservations reservations;
    @Autowired SourceEditGuard edits;
    @Autowired VaultEntryOperations vault;
    @Autowired PlatformTransactionManager transactions;
    @Autowired EntityManager entities;

    private TransactionTemplate tx() {
        var tx = new TransactionTemplate(transactions);
        tx.setTimeout(10);
        return tx;
    }

    private RagSourceView candidate() {
        Long genre = jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES (?) RETURNING id", Long.class, UUID.randomUUID().toString());
        Long person = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic person')", person);
        Long id = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,description,is_nsfw) VALUES (?,'Synthetic title','NOVEL',?,?,'Synthetic description',false)", id, genre, person);
        return rag.registerDomainCandidate(RagSourceType.FICTION, id.toString(), RagClassification.NON_PRIVATE, null, null);
    }

    private static RagConsentCommand consent(RagSourceView view, Set<String> fields) {
        return new RagConsentCommand(fields, view.sourceVersion(), view.consentResetVersion(),
                view.authorizationEpoch(), view.fieldPolicyVersion(),
                fields.contains("description") ? view.currentDescriptionRevisionHash() : null);
    }

    private SortedSet<Long> ids(RagSourceView... views) {
        var ids = new TreeSet<Long>();
        for (var view : views) ids.add(Long.valueOf(view.sourceId()));
        return ids;
    }

    @Test void realOwnerSqlBackstopAndPrivileges() throws Exception {
        String script = new ClassPathResource("rag/source_state_reservations.sql").getContentAsString(StandardCharsets.UTF_8);
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.setQueryTimeout(20);
            statement.execute(script);
            while (statement.getMoreResults() || statement.getUpdateCount() != -1) { /* drain all SQL assertions */ }
        }
    }

    @Test void normalEligibleEditPreservesConsentAndAdvancesOnlySourceVersion() {
        var initial = candidate();
        var approved = rag.approveSource(initial.id(), consent(initial, Set.of("title", "description")));
        jdbcTemplate.update("UPDATE fictions SET title='Synthetic edited title' WHERE id=?", Long.valueOf(approved.sourceId()));
        var changed = rag.findSource(approved.id()).orElseThrow();
        assertThat(changed.accessState()).isEqualTo(RagAccessState.APPROVED);
        assertThat(changed.sourceVersion()).isGreaterThan(approved.sourceVersion());
        assertThat(changed.consentResetVersion()).isEqualTo(approved.consentResetVersion());
        assertThat(changed.authorizationEpoch()).isEqualTo(approved.authorizationEpoch());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Integer.class)).isZero();
    }

    @Test void realSingleJobSqlAndRestrictedWorkerStagingRoutes() throws Exception {
        String script = new ClassPathResource("rag/single_job_staging.sql").getContentAsString(StandardCharsets.UTF_8);
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.setQueryTimeout(20);
            statement.execute(script);
            while (statement.getMoreResults() || statement.getUpdateCount() != -1) { /* every assertion must finish */ }
        }
    }

    @Test void lostNsfwRoundTripCannotRestoreConsentOrAcceptOldOwnerIntent() {
        var initial = candidate();
        var old = consent(initial, Set.of("title"));
        var approved = rag.approveSource(initial.id(), old);
        long owner = Long.parseLong(initial.sourceId());
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", owner);
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=false WHERE id=?", owner);
        var restored = rag.findSource(initial.id()).orElseThrow();
        assertThat(restored.accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(restored.consentResetVersion()).isGreaterThan(approved.consentResetVersion());
        assertThatThrownBy(() -> rag.approveSource(initial.id(), old)).isInstanceOf(RagSourceChangedException.class);
        var renewed = rag.approveSource(initial.id(), consent(restored, Set.of("title")));
        assertThat(renewed.isEligible()).isTrue();
        assertThat(renewed.authorizationEpoch()).isGreaterThan(approved.authorizationEpoch());
    }

    @Test void delayedApprovalCannotUndoRevokeOrFieldReduction() {
        var initial = candidate();
        var approved = rag.approveSource(initial.id(), consent(initial, Set.of("title", "description")));
        var delayed = consent(approved, Set.of("title", "description"));
        var reduced = rag.approveSource(approved.id(), consent(approved, Set.of("title")));
        assertThatThrownBy(() -> rag.approveSource(approved.id(), delayed)).isInstanceOf(RagSourceChangedException.class);
        var beforeRevoke = consent(reduced, Set.of("title"));
        rag.revokeSource(reduced.id());
        assertThatThrownBy(() -> rag.approveSource(reduced.id(), beforeRevoke)).isInstanceOf(RagSourceChangedException.class);
    }

    @Test void privateConfigurationNeverChangesImplicitlyDuringApproval() {
        var initial = candidate();
        var approved = rag.approveSource(initial.id(), consent(initial, Set.of("title")));
        var privateView = rag.registerDomainCandidate(approved.sourceType(), approved.sourceId(), RagClassification.PRIVATE,
                approved.authorizationEpoch(), approved.fieldPolicyVersion());
        assertThat(privateView.classification()).isEqualTo(RagClassification.PRIVATE);
        assertThat(privateView.accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThatThrownBy(() -> rag.approveSource(initial.id(), consent(privateView, Set.of("title")))).isInstanceOf(IllegalStateException.class);
        var restored = rag.registerDomainCandidate(privateView.sourceType(), privateView.sourceId(), RagClassification.NON_PRIVATE,
                privateView.authorizationEpoch(), privateView.fieldPolicyVersion());
        assertThat(restored.accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(restored.approvedFields()).containsExactly("title");
        assertThat(rag.approveSource(restored.id(), consent(restored, Set.of("title"))).isEligible()).isTrue();
    }

    @Test void descriptionConfirmationUsesExactBytesAndCannotRenewItemConsent() {
        var initial = candidate();
        var stale = consent(initial, Set.of("title", "description"));
        jdbcTemplate.update("UPDATE fictions SET description='Synthetic description ' WHERE id=?", Long.valueOf(initial.sourceId()));
        assertThatThrownBy(() -> rag.approveSource(initial.id(), stale)).isInstanceOf(RagSourceChangedException.class);
        var fresh = rag.findSource(initial.id()).orElseThrow();
        assertThat(fresh.currentDescriptionRevisionHash()).isNotEqualTo(initial.currentDescriptionRevisionHash());
        var approved = rag.approveSource(initial.id(), consent(fresh, Set.of("title", "description")));
        assertThat(approved.descriptionRevisionHash()).isEqualTo(fresh.currentDescriptionRevisionHash());
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", Long.valueOf(initial.sourceId()));
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=false WHERE id=?", Long.valueOf(initial.sourceId()));
        assertThat(rag.findSource(initial.id()).orElseThrow().accessState()).isEqualTo(RagAccessState.REVOKED);
    }

    @Test void reservationSurvivesPhysicalCommitAndBlocksSelectedButNotUnrelatedEdits() {
        var selected = candidate(); var unrelated = candidate();
        var token = UUID.randomUUID();
        tx().executeWithoutResult(status -> reservations.reserve(ids(selected), token));
        assertThat(reservations.read(ids(selected)).require(Long.parseLong(selected.sourceId())).reservedBy()).isEqualTo(token);
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", Long.valueOf(selected.sourceId())))
                .satisfies(error -> assertThat(sqlState(error)).isEqualTo("P1701"));
        assertThatThrownBy(() -> vault.moveToTrash(Long.valueOf(selected.sourceId())))
                .satisfies(error -> assertThat(sqlState(error)).isEqualTo("P1701"));
        jdbcTemplate.update("UPDATE fictions SET title='Synthetic unrelated edit' WHERE id=?", Long.valueOf(unrelated.sourceId()));
        assertThat(jdbcTemplate.queryForObject("SELECT is_nsfw FROM fictions WHERE id=?", Boolean.class, Long.valueOf(selected.sourceId()))).isFalse();
        tx().executeWithoutResult(status -> reservations.release(ids(selected), token));
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", Long.valueOf(selected.sourceId()));
    }

    @Test void exactTokenAndWholeReservedSetAreRequiredForRelease() {
        var a = candidate(); var b = candidate(); var token = UUID.randomUUID();
        tx().executeWithoutResult(status -> reservations.reserve(ids(a, b), token));
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> reservations.release(ids(a), token))).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> reservations.release(ids(a, b), UUID.randomUUID()))).isInstanceOf(RuntimeException.class);
        assertThat(reservations.read(ids(a, b)).sources().values()).allMatch(s -> token.equals(s.reservedBy()));
        tx().executeWithoutResult(status -> reservations.release(ids(a, b), token));
    }

    @Test void sourceGuardUsesSamePhysicalConnectionAndRejectsReadOnlyOrMissingTransaction() {
        var source = candidate();
        assertThatThrownBy(() -> reservations.reserve(ids(source), UUID.randomUUID())).isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
        var readOnly = tx(); readOnly.setReadOnly(true);
        assertThatThrownBy(() -> readOnly.executeWithoutResult(status -> reservations.reserve(ids(source), UUID.randomUUID()))).isInstanceOf(IllegalStateException.class);
        tx().executeWithoutResult(status -> {
            edits.requireEditable(ids(source));
            Number jpa = (Number) entities.createNativeQuery("SELECT pg_backend_pid()").getSingleResult();
            assertThat(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class)).isEqualTo(jpa.intValue());
        });
    }

    @Test void realOwnerFirstLockWaitObservesCommittedNewVersion() throws Exception {
        var source = candidate(); var token = UUID.randomUUID();
        var locked = new CountDownLatch(1); var commit = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var writer = executor.submit(() -> tx().executeWithoutResult(status -> {
                edits.requireEditable(ids(source));
                jdbcTemplate.update("UPDATE fictions SET title='Synthetic owner first' WHERE id=?", Long.valueOf(source.sourceId()));
                locked.countDown();
                try { if (!commit.await(8, TimeUnit.SECONDS)) throw new IllegalStateException("Commit barrier timed out"); }
                catch (InterruptedException failure) { Thread.currentThread().interrupt(); throw new IllegalStateException(failure); }
            }));
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            var reserving = executor.submit(() -> tx().executeWithoutResult(status -> {
                jdbcTemplate.execute("SET LOCAL application_name='p17-source-reserver'");
                reservations.reserve(ids(source), token);
            }));
            try {
                org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(2)).until(() ->
                        jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE application_name='p17-source-reserver' AND cardinality(pg_blocking_pids(pid))>0", Integer.class) == 1);
            } finally { commit.countDown(); }
            writer.get(10, TimeUnit.SECONDS); reserving.get(10, TimeUnit.SECONDS);
            assertThat(reservations.read(ids(source)).require(Long.parseLong(source.sourceId())).sourceVersion()).isGreaterThan(source.sourceVersion());
        } finally { commit.countDown(); }
    }

    private static String sqlState(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) return sql.getSQLState();
        }
        return "";
    }

    private RagSourceView approved() {
        var source = candidate();
        return rag.approveSource(source.id(), consent(source, Set.of("title", "description")));
    }

    private static RagIndexCommand index(RagSourceView view, Map<Long, String> confirmations) {
        return new RagIndexCommand(view.sourceVersion(), view.authorizationEpoch(), view.fieldPolicyVersion(), confirmations);
    }

    @Test void admissionCommitsExactlyTheEntireDependencyReservationAndOneJob() {
        var parent = approved(); var child = approved(); var other = approved();
        rag.addDependency(parent.id(), child.id());
        var current = rag.findSource(parent.id()).orElseThrow();
        var admitted = rag.admitJob(parent.id(), index(current, Map.of()));
        assertThat(reservations.read(ids(parent, child)).sources().values()).allMatch(value -> admitted.attemptId().equals(value.reservedBy()));
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", Long.valueOf(child.sourceId())))
                .satisfies(error -> assertThat(sqlState(error)).isEqualTo("P1701"));
        assertThatThrownBy(() -> rag.admitJob(other.id(), index(other, Map.of()))).isInstanceOf(RagJobBusyException.class);
        assertThat(reservations.read(ids(other)).require(Long.parseLong(other.sourceId())).reservedBy()).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Integer.class)).isEqualTo(1);
    }

    @Test void manualReindexOfNormalEditRetainsConsentWithoutAnotherApproval() {
        var source = approved();
        jdbcTemplate.update("UPDATE fictions SET title='Synthetic manual revision' WHERE id=?", Long.valueOf(source.sourceId()));
        var current = rag.findSource(source.id()).orElseThrow();
        assertThat(current.authorizationEpoch()).isEqualTo(source.authorizationEpoch());
        assertThatThrownBy(() -> rag.admitJob(source.id(), index(source, Map.of()))).isInstanceOf(RagSourceChangedException.class);
        var job = rag.admitJob(source.id(), index(current, Map.of()));
        assertThat(job.authorizationEpoch()).isEqualTo(source.authorizationEpoch());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants", Integer.class)).isZero();
    }

    @Test void changedDescriptionConfirmationAndReservationCommitTogether() {
        var source = approved();
        jdbcTemplate.update("UPDATE fictions SET description='Synthetic changed description' WHERE id=?", Long.valueOf(source.sourceId()));
        var current = rag.findSource(source.id()).orElseThrow();
        assertThatThrownBy(() -> rag.admitJob(source.id(), index(current, Map.of()))).isInstanceOf(RagSourceChangedException.class);
        assertThatThrownBy(() -> rag.admitJob(source.id(), index(current, Map.of(source.id(), source.descriptionRevisionHash())))).isInstanceOf(RagSourceChangedException.class);
        assertThat(reservations.read(ids(source)).require(Long.parseLong(source.sourceId())).reservedBy()).isNull();
        var job = rag.admitJob(source.id(), index(current, Map.of(source.id(), current.currentDescriptionRevisionHash())));
        var after = rag.findSource(source.id()).orElseThrow();
        assertThat(after.descriptionRevisionHash()).isEqualTo(current.currentDescriptionRevisionHash());
        assertThat(after.authorizationEpoch()).isEqualTo(source.authorizationEpoch());
        assertThat(reservations.read(ids(source)).require(Long.parseLong(source.sourceId())).reservedBy()).isEqualTo(job.attemptId());
    }

    @Test void nsfwOwnerFirstAndResetRoundTripDenyAdmissionWithoutWorkerGrant() {
        var source = approved();
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?", Long.valueOf(source.sourceId()));
        assertThatThrownBy(() -> rag.admitJob(source.id(), index(source, Map.of()))).isInstanceOf(IllegalStateException.class);
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=false WHERE id=?", Long.valueOf(source.sourceId()));
        var restored = rag.findSource(source.id()).orElseThrow();
        assertThatThrownBy(() -> rag.admitJob(source.id(), index(restored, Map.of()))).isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants", Integer.class)).isZero();
    }

    @Test void associationReassignmentBetweenDifferentOldAndNewOwnersRequiresBothUnreserved() {
        var ownerA = candidate(); var ownerB = candidate();
        Long archetype = jdbcTemplate.queryForObject("SELECT min(id) FROM story_archetypes", Long.class);
        Long idA = Long.parseLong(ownerA.sourceId());
        Long idB = Long.parseLong(ownerB.sourceId());
        jdbcTemplate.update("INSERT INTO fiction_story_archetypes(fiction_id,story_archetype_id) VALUES (?,?)", idA, archetype);

        var tokenA = UUID.randomUUID();
        tx().executeWithoutResult(status -> reservations.reserve(ids(ownerA), tokenA));

        // Reassigning from reserved ownerA (OLD-A) to unreserved ownerB (NEW-B) must fail with P1701:
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE fiction_story_archetypes SET fiction_id=? WHERE fiction_id=?", idB, idA))
                .satisfies(error -> assertThat(sqlState(error)).isEqualTo("P1701"));

        tx().executeWithoutResult(status -> reservations.release(ids(ownerA), tokenA));

        var tokenB = UUID.randomUUID();
        tx().executeWithoutResult(status -> reservations.reserve(ids(ownerB), tokenB));

        // Reassigning from unreserved ownerA (OLD-A) to reserved ownerB (NEW-B) must fail with P1701:
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE fiction_story_archetypes SET fiction_id=? WHERE fiction_id=?", idB, idA))
                .satisfies(error -> assertThat(sqlState(error)).isEqualTo("P1701"));

        tx().executeWithoutResult(status -> reservations.release(ids(ownerB), tokenB));

        // When neither is reserved, reassignment succeeds and advances source versions of both:
        long vA = reservations.read(ids(ownerA)).require(idA).sourceVersion();
        long vB = reservations.read(ids(ownerB)).require(idB).sourceVersion();
        int updated = jdbcTemplate.update("UPDATE fiction_story_archetypes SET fiction_id=? WHERE fiction_id=?", idB, idA);
        assertThat(updated).isEqualTo(1);
        assertThat(reservations.read(ids(ownerA)).require(idA).sourceVersion()).isGreaterThan(vA);
        assertThat(reservations.read(ids(ownerB)).require(idB).sourceVersion()).isGreaterThan(vB);
    }
}

