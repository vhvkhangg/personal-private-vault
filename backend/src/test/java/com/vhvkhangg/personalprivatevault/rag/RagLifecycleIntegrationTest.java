package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobStatusView;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagPurgeService;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@org.junit.jupiter.api.Timeout(90)
class RagLifecycleIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @Autowired
    private RagOperations ragOperations;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor supervisor;

    @Autowired
    private RagPurgeService ragPurgeService;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService recovery;

    @Test
    @DisplayName("End-to-end RAG lifecycle: domain registration, approval, job admission, staging, finalization, revocation, and purge")
    void fullRagLifecycle() {
        // 1. Create a Fiction item backed by proper vault_entries and persons rows
        Long genreId = jdbcTemplate.queryForObject(
                "INSERT INTO fiction_genres (name) VALUES ('SciFi-Test') RETURNING id",
                Long.class
        );
        Long authorVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('PERSON'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update("INSERT INTO persons (id, name) VALUES (?, 'Arthur C. Clarke')", authorVaultId);

        Long fictionVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update(
                """
                INSERT INTO fictions (id, title, format, genre_id, author_person_id, progress_status, consumption_status, description, is_nsfw)
                VALUES (?, 'SciFi Odyssey', 'NOVEL'::fiction_format, ?, ?, 'COMPLETED'::progress_status, 'CONSUMED'::consumption_status, 'A monolith discovered.', false)
                """,
                fictionVaultId, genreId, authorVaultId
        );

        // 2. Register domain candidate -> starts in DENIED state
        RagSourceView candidate = RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionVaultId));
        assertThat(candidate).isNotNull();
        assertThat(candidate.id()).isNotNull();
        assertThat(candidate.sourceType()).isEqualTo(RagSourceType.FICTION);
        assertThat(candidate.sourceId()).isEqualTo(String.valueOf(fictionVaultId));
        assertThat(candidate.accessState()).isEqualTo(RagAccessState.DENIED);

        // Verify retrieval by ID and listing
        Optional<RagSourceView> found = ragOperations.findSource(candidate.id());
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(candidate.id());

        List<RagSourceView> list = ragOperations.listSources(0, 10);
        assertThat(list).isNotEmpty();

        // 3. Approve source with explicit allowed fields -> transitions to APPROVED
        RagSourceView approved = RagTestCommands.approve(ragOperations, candidate.id(), Set.of("title", "description"));
        assertThat(approved.accessState()).isEqualTo(RagAccessState.APPROVED);
        assertThat(approved.approvedFields()).containsExactlyInAnyOrder("title", "description");

        // 4. Admit job -> short-lived lease granted
        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, approved.id());
        assertThat(admission).isNotNull();
        assertThat(admission.jobId()).isNotNull();
        assertThat(admission.attemptId()).isNotNull();
        assertThat(admission.leaseToken()).isNotBlank();
        assertThat(admission.leasedUntil()).isNotNull();

        // 5. Query sanitized job status
        RagJobStatusView statusView = ragOperations.getJobStatus(admission.jobId());
        assertThat(statusView).isNotNull();
        assertThat(statusView.jobId()).isEqualTo(admission.jobId());
        assertThat(statusView.status()).isEqualTo("ADMITTED");

        // 6. Worker inserts staged outputs
        RagWorkerFixture.stage(ragOperations, admission);

        Integer stagedCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.staged_outputs WHERE job_id = ? AND attempt_id = ?",
                Integer.class,
                admission.jobId(), admission.attemptId()
        );
        assertThat(stagedCount).isEqualTo(1);

        // 7. Guarded finalization -> commits into index generation and records run history
        RagFinalizationResult finalization = ragOperations.finalizeGeneration(
                admission.jobId(),
                admission.attemptId(),
                ""
        );
        assertThat(finalization).isNotNull();
        assertThat(finalization.active()).isTrue();
        assertThat(finalization.status()).isEqualTo("PUBLISHED");
        assertThat(finalization.generationId()).isNotNull();

        // Verify active index generation exists in DB
        Integer activeGens = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.index_generations WHERE source_id = ? AND is_active = true",
                Integer.class,
                approved.id()
        );
        assertThat(activeGens).isEqualTo(1);

        // 8. Revoke source -> synchronous deny and active generation cancellation
        RagSourceView revoked = ragOperations.revokeSource(approved.id());
        assertThat(revoked.accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(ragOperations.getJobStatus(admission.jobId()).indexState()).isEqualTo("REVOKED");
        recovery.reconcile();

        Integer remainingActiveGens = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.index_generations WHERE source_id = ? AND is_active = true",
                Integer.class,
                approved.id()
        );
        assertThat(remainingActiveGens).isZero();

        // 9. Purge derivatives barrier
        ragPurgeService.purgeJobDerivatives(admission.jobId());
        assertThat(ragPurgeService.retryCleanup(admission.jobId())).isTrue();
        Integer remainingStaged = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.staged_outputs WHERE job_id = ?",
                Integer.class,
                admission.jobId()
        );
        assertThat(remainingStaged).isZero();
    }

    @Test
    @DisplayName("Rejects source approval with denied or unknown fields (FR17-2)")
    void rejectsApprovalWithDeniedField() {
        Long fictionVaultId = createFiction("Novel Test", false);
        RagSourceView candidate = RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionVaultId));

        assertThatThrownBy(() -> RagTestCommands.approve(ragOperations, candidate.id(), Set.of("title", "review")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not eligible for RAG approval");

        assertThatThrownBy(() -> RagTestCommands.approve(ragOperations, candidate.id(), Set.of("title", "secret_metadata")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not eligible for RAG approval");
    }

    @Test
    @DisplayName("Rejects publication with zero staged outputs (FR17-3)")
    void rejectsPublicationWithZeroStagedOutputs() {
        Long fictionVaultId = createFiction("Empty Test", false);
        RagSourceView candidate = RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionVaultId));
        RagSourceView approved = RagTestCommands.approve(ragOperations, candidate.id(), Set.of("title"));

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, approved.id());

        assertThat(supervisor.stopAndVerify(admission.jobId())).isTrue();
        assertThatThrownBy(() -> ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected one bounded foundation receipt");
    }

    @Test
    @DisplayName("Rejects publication when source epoch/policy changes after admission (FR17-3)")
    void rejectsPublicationWhenEpochChanges() {
        Long fictionVaultId = createFiction("Epoch Test", false);
        RagSourceView candidate = RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionVaultId));
        RagSourceView approved = RagTestCommands.approve(ragOperations, candidate.id(), Set.of("title"));

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, approved.id());

        RagWorkerFixture.stage(ragOperations, admission);

        // Re-approve source with new field -> increments authorization epoch and fieldPolicyVersion
        assertBusy(()->RagTestCommands.approve(ragOperations, approved.id(), Set.of("title", "description")));
        ragOperations.cancelJob(admission.jobId());
        RagTestCommands.approve(ragOperations, approved.id(), Set.of("title", "description"));

        // Finalization with the old lease must fail
        assertThatThrownBy(() -> ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), ""))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects publication when child dependency becomes NSFW concurrently (FR17-3)")
    void rejectsPublicationWhenChildBecomesNsfw() {
        Long parentFictionId = createFiction("Parent Fiction", false);
        Long childFilmId = createFilm("Child Film", false);

        RagSourceView parentCandidate = RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(parentFictionId));
        RagSourceView parentApproved = RagTestCommands.approve(ragOperations, parentCandidate.id(), Set.of("title"));

        RagSourceView childCandidate = RagTestCommands.register(ragOperations, RagSourceType.FILM, String.valueOf(childFilmId));
        RagSourceView childApproved = RagTestCommands.approve(ragOperations, childCandidate.id(), Set.of("title"));

        ragOperations.addDependency(parentApproved.id(), childApproved.id());

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, parentApproved.id());

        RagWorkerFixture.stage(ragOperations, admission);

        // Concurrently mutate child film to NSFW
        assertBusy(()->jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", childFilmId));
        ragOperations.cancelJob(admission.jobId());
        jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", childFilmId);

        assertThatThrownBy(() -> ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), ""))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cascades revocation across three-level dependency graph (FR17-6)")
    void cascadesRevocationAcrossThreeLevelDependencyGraph() {
        Long fictionC = createFiction("Leaf C", false);
        Long fictionB = createFiction("Middle B", false);
        Long fictionA = createFiction("Root A", false);

        RagSourceView srcC = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionC)).id(), Set.of("title"));
        RagSourceView srcB = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionB)).id(), Set.of("title"));
        RagSourceView srcA = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionA)).id(), Set.of("title"));

        // A depends on B, B depends on C
        ragOperations.addDependency(srcB.id(), srcC.id());
        ragOperations.addDependency(srcA.id(), srcB.id());

        // Publish all three
        publishSource(srcC.id());
        publishSource(srcB.id());
        publishSource(srcA.id());

        // Verify all 3 have active generations
        assertThat(countActiveGenerations(srcC.id())).isEqualTo(1);
        assertThat(countActiveGenerations(srcB.id())).isEqualTo(1);
        assertThat(countActiveGenerations(srcA.id())).isEqualTo(1);

        // Revoke leaf C -> must cascade and revoke active generations for C, B, and A
        ragOperations.revokeSource(srcC.id());
        for(var job:jdbcTemplate.queryForList("SELECT job_id FROM rag.index_generations",java.util.UUID.class))
            assertThat(ragOperations.getJobStatus(job).indexState()).isEqualTo("REVOKED");
        recovery.reconcile();

        assertThat(countActiveGenerations(srcC.id())).isZero();
        assertThat(countActiveGenerations(srcB.id())).isZero();
        assertThat(countActiveGenerations(srcA.id())).isZero();
    }

    @Test
    @DisplayName("Cancelling a historically completed job preserves its independently current index (approved E1 lifetime)")
    void cancellingJobRevokesActiveIndexGeneration() {
        Long fictionId = createFiction("Cancel Test", false);
        RagSourceView src = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId)).id(), Set.of("title"));

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, src.id());
        RagWorkerFixture.stage(ragOperations, admission);
        ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), "");
        assertThat(countActiveGenerations(src.id())).isEqualTo(1);

        // A completed worker's cancellation cannot revoke standing source consent.
        assertThatThrownBy(()->ragOperations.cancelJob(admission.jobId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countActiveGenerations(src.id())).isEqualTo(1);
        assertThat(ragOperations.getJobStatus(admission.jobId()).status()).isEqualTo("COMPLETED");
        assertThat(ragOperations.getJobStatus(admission.jobId()).indexState()).isEqualTo("CURRENT");
    }

    @Test
    @DisplayName("Rejects publication when caller provides mismatched dependency digest (FR17-3)")
    void rejectsMismatchedDependencyDigest() {
        Long fictionId = createFiction("Digest Mismatch Test", false);
        RagSourceView src = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId)).id(), Set.of("title"));

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, src.id());
        RagWorkerFixture.stage(ragOperations, admission);

        assertThatThrownBy(() -> ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), "wrong-sha256-digest"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dependency manifest digest mismatch");
    }

    @Test
    @DisplayName("Rejects publication across three-level dependency when grandchild becomes NSFW concurrently (FR17-3 / NSFW_GRANDCHILD_PARENT_PUBLISHED)")
    void rejectsPublicationWhenGrandchildBecomesNsfw() {
        Long fictionAId = createFiction("Root Fiction A", false);
        Long fictionBId = createFiction("Middle Fiction B", false);
        Long filmCId = createFilm("Grandchild Film C", false);

        RagSourceView srcA = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionAId)).id(), Set.of("title"));
        RagSourceView srcB = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionBId)).id(), Set.of("title"));
        RagSourceView srcC = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FILM, String.valueOf(filmCId)).id(), Set.of("title"));

        ragOperations.addDependency(srcB.id(), srcC.id());
        ragOperations.addDependency(srcA.id(), srcB.id());

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, srcA.id());
        RagWorkerFixture.stage(ragOperations, admission);

        // Mutate grandchild film to NSFW concurrently
        assertBusy(()->jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", filmCId));
        ragOperations.cancelJob(admission.jobId());
        jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", filmCId);

        assertThatThrownBy(() -> ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), ""))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects publication when child external file content changes concurrently (FR17-3 / CHANGED_EXTERNAL_CHILD_PARENT_PUBLISHED)")
    void rejectsPublicationWhenChildExternalFileChangesConcurrently(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        java.nio.file.Path file=tempDir.resolve("child_external.txt");
        java.nio.file.Files.writeString(file,"Initial plain text");
        assertThatThrownBy(()->ragOperations.registerFileCandidate(file.toString()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
        Long parentId=createFiction("Synthetic parent",false);
        var parent=RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION,parentId.toString()).id(),Set.of("title"));
        var attempt=RagTestCommands.index(ragOperations, parent.id());
        RagWorkerFixture.stage(ragOperations,attempt);
        Long child=jdbcTemplate.queryForObject("INSERT INTO rag.sources(source_type,source_id,classification,approved_fields_text,access_state,source_revision_hash,description_revision_hash,created_at,updated_at) VALUES ('EXTERNAL_FILE',?,'NON_PRIVATE','content','APPROVED','legacy','legacy',clock_timestamp(),clock_timestamp()) RETURNING id",Long.class,file.toString());
        assertBusy(()->ragOperations.addDependency(parent.id(),child));
        ragOperations.cancelJob(attempt.jobId());
        ragOperations.addDependency(parent.id(),child);
        java.nio.file.Files.writeString(file,"Changed content");
        assertThatThrownBy(()->RagTestCommands.index(ragOperations, parent.id())).isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
        assertThatThrownBy(()->ragOperations.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null)).isInstanceOf(IllegalStateException.class);
        assertThat(countActiveGenerations(parent.id())).isZero();
    }

    @Test
    @DisplayName("Rejects approval for external file with restrictive metadata (FR17-2 / RESTRICTIVE_METADATA_FILE_APPROVED_AND_PUBLISHED)")
    void rejectsApprovalForExternalFileWithRestrictiveMetadata(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        java.nio.file.Path externalFile = tempDir.resolve("confidential_doc.txt");
        java.nio.file.Files.writeString(externalFile, """
                ---
                title: Confidential Research
                confidential: true
                ---
                Secret internal vault documentation.
                """);

        assertThatThrownBy(() -> ragOperations.registerFileCandidate(externalFile.toString()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
    }

    @Test
    @DisplayName("Field consent reduction immediately revokes active generation and purges derivative staging (FR17-6 / FIELD_REDUCTION_LEAVES_OLD_GENERATION_ACTIVE)")
    void fieldReductionImmediatelyRevokesActiveGenerationAndPurgesStagedOutputs() {
        Long fictionId = createFiction("Field Reduction Test", false);
        RagSourceView src = RagTestCommands.approve(ragOperations,
                RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId)).id(),
                Set.of("title", "description")
        );

        publishSource(src.id());
        assertThat(countActiveGenerations(src.id())).isEqualTo(1);

        // Reduce consent: remove description
        RagTestCommands.approve(ragOperations, src.id(), Set.of("title"));

        for(var job:jdbcTemplate.queryForList("SELECT job_id FROM rag.index_generations WHERE source_id=?",java.util.UUID.class,src.id()))
            assertThat(ragOperations.getJobStatus(job).indexState()).isEqualTo("REVOKED");
        recovery.reconcile();
        // Fresh authority denies immediately; verified cleanup records deactivation.
        assertThat(countActiveGenerations(src.id())).isZero();
    }

    @Test
    @DisplayName("Execution deadline cannot be rewritten to bypass expiry gates (actual expiry and waits are in RagE1ExpiryIntegrationTest; FR17-7 identity preserved)")
    void rejectsPublicationWithAlreadyExpiredLease() {
        Long fictionId = createFiction("Expiry Test", false);
        RagSourceView src = RagTestCommands.approve(ragOperations,
                RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId)).id(),
                Set.of("title")
        );

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, src.id());
        RagWorkerFixture.stage(ragOperations, admission);

        // Simulate lease expiration during wait
        assertThatThrownBy(()->jdbcTemplate.update("UPDATE rag.job_leases SET leased_until = CURRENT_TIMESTAMP - INTERVAL '1 second' WHERE job_id = ?", admission.jobId()))
                .satisfies(error->assertThat(sqlState(error)).isEqualTo("23514"));
        assertThat(jdbcTemplate.queryForObject("SELECT leased_until>clock_timestamp() FROM rag.job_leases WHERE job_id=?",Boolean.class,admission.jobId())).isTrue();
        assertThat(countActiveGenerations(src.id())).isZero();
    }

    private void publishSource(Long sourceId) {
        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, sourceId);
        RagWorkerFixture.stage(ragOperations, admission);
        ragOperations.finalizeGeneration(admission.jobId(), admission.attemptId(), "");
    }

    private Integer countActiveGenerations(Long sourceId) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.index_generations WHERE source_id = ? AND is_active = true",
                Integer.class,
                sourceId
        );
    }

    private Long createFiction(String title, boolean isNsfw) {
        Long genreId = jdbcTemplate.queryForObject(
                "INSERT INTO fiction_genres (name) VALUES ('Genre-" + System.nanoTime() + "') RETURNING id",
                Long.class
        );
        Long authorVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('PERSON'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update("INSERT INTO persons (id, name) VALUES (?, 'Author')", authorVaultId);

        Long fictionVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update(
                """
                INSERT INTO fictions (id, title, format, genre_id, author_person_id, progress_status, consumption_status, description, is_nsfw)
                VALUES (?, ?, 'NOVEL'::fiction_format, ?, ?, 'COMPLETED'::progress_status, 'CONSUMED'::consumption_status, 'Desc', ?)
                """,
                fictionVaultId, title, genreId, authorVaultId, isNsfw
        );
        return fictionVaultId;
    }

    private Long createFilm(String title, boolean isNsfw) {
        Long filmVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FILM'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update(
                """
                INSERT INTO films (id, title, format, production_style, progress_status, consumption_status, description, is_nsfw)
                VALUES (?, ?, 'MOVIE'::film_format, 'LIVE_ACTION'::film_production_style, 'COMPLETED'::progress_status, 'CONSUMED'::consumption_status, 'Film Desc', ?)
                """,
                filmVaultId, title, isNsfw
        );
        return filmVaultId;
    }

    @Test
    @DisplayName("Rejects external file with restrictive metadata containing whitespace, quotes, or brackets (FR17-2)")
    void rejectsApprovalForExternalFileWithRestrictiveMetadataWhitespaceAndQuotes(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        java.nio.file.Path f1 = tempDir.resolve("spaces.txt");
        java.nio.file.Files.writeString(f1, "private:   true\nSome text");
        assertThatThrownBy(() -> ragOperations.registerFileCandidate(f1.toString()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");

        java.nio.file.Path f2 = tempDir.resolve("quotes.txt");
        java.nio.file.Files.writeString(f2, "visibility: 'private'\nSome text");
        assertThatThrownBy(() -> ragOperations.registerFileCandidate(f2.toString()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");

        java.nio.file.Path f3 = tempDir.resolve("brackets.txt");
        java.nio.file.Files.writeString(f3, "private: [true]\nSome text");
        assertThatThrownBy(() -> ragOperations.registerFileCandidate(f3.toString()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
    }

    @Test
    @DisplayName("Rejects approval and activation of Markdown (.md) files in Phase 17 foundation (FR17-2)")
    void rejectsActivationOfMarkdownFiles(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        java.nio.file.Path mdFile = tempDir.resolve("notes.md");
        java.nio.file.Files.writeString(mdFile, "# Some Markdown Document\n\nContent here");

        assertThatThrownBy(() -> ragOperations.registerFileCandidate(mdFile.toString()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
    }

    @Test
    @DisplayName("Rejects publication of legacy unversioned or malformed dependency manifest (FR17-3)")
    void rejectsPublicationOfLegacyUnversionedManifest() {
        Long fictionId = createFiction("Legacy Manifest Fiction", false);
        RagSourceView src = RagTestCommands.approve(ragOperations,
                RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId)).id(),
                Set.of("title")
        );

        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, src.id());
        RagWorkerFixture.stage(ragOperations, admission);

        // Corrupt dependency manifest to remove v1: version header
        assertThatThrownBy(()->jdbcTemplate.update(
                "UPDATE rag.job_leases SET dependency_manifest = '100:1:1:hash:desc' WHERE job_id = ?",
                admission.jobId()
        )).satisfies(error->assertThat(sqlState(error)).isEqualTo("23514"));

        assertThat(jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,admission.jobId()))
                .doesNotContain("100:1:1:hash:desc");
        assertThat(countActiveGenerations(src.id())).isZero();
    }

    @Test
    @DisplayName("One issued Foundation receipt is stricter than the retained 50-row ceiling: retries cannot grow staging (FR17-9)")
    void rejectsStagingOverCumulativeRowBudget() throws Exception {
        Long fictionId = createFiction("Budget Row Fiction", false);
        RagSourceView src = RagTestCommands.approve(ragOperations,
                RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId)).id(),
                Set.of("title")
        );
        RagJobAdmissionResult admission = RagTestCommands.index(ragOperations, src.id());

        RagWorkerFixture.stage(ragOperations,admission);
        String receipt=jdbcTemplate.queryForObject("SELECT payload FROM rag.staged_outputs WHERE job_id=?",String.class,admission.jobId());
        Long row=jdbcTemplate.queryForObject("SELECT id FROM rag.staged_outputs WHERE job_id=?",Long.class,admission.jobId());
        try(var worker=workerConnection();var query=worker.prepareStatement("SELECT rag.stage_worker_receipt(?,?,?,?)")) {
            query.setObject(1,admission.jobId());query.setObject(2,admission.attemptId());
            query.setString(3,admission.leaseToken());query.setString(4,receipt);
            for(int i=0;i<50;i++) {
                try(var result=query.executeQuery()) {
                    assertThat(result.next()).isTrue();assertThat(result.getLong(1)).isEqualTo(row);
                }
            }
            var forged=new com.fasterxml.jackson.databind.ObjectMapper().readTree(receipt);
            ((com.fasterxml.jackson.databind.node.ObjectNode)forged).put("output_digest","0".repeat(64));
            query.setString(4,forged.toString());
            assertThatThrownBy(query::executeQuery).satisfies(error->assertThat(sqlState(error)).isEqualTo("23514"));
        }
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,admission.jobId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Serial edge additions reject a cycle (actual concurrency proof is separate) (FR17-7)")
    void disjointConcurrentEdgeCycleDetection() {
        Long fA = createFiction("Fiction A", false);
        Long fB = createFiction("Fiction B", false);
        Long fC = createFiction("Fiction C", false);
        Long fD = createFiction("Fiction D", false);

        Long sA = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fA)).id(), Set.of("title")).id();
        Long sB = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fB)).id(), Set.of("title")).id();
        Long sC = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fC)).id(), Set.of("title")).id();
        Long sD = RagTestCommands.approve(ragOperations, RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fD)).id(), Set.of("title")).id();

        // Form B -> C and D -> A
        ragOperations.addDependency(sB, sC);
        ragOperations.addDependency(sD, sA);

        // Add A -> B
        ragOperations.addDependency(sA, sB);

        // Now adding C -> D would close the cycle: A -> B -> C -> D -> A
        assertThatThrownBy(() -> ragOperations.addDependency(sC, sD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cyclic dependency detected");
    }

    @Test
    @DisplayName("Repeated policy mutator monotonically increments authorization epoch (cached-context proof is separate) (FR17-7)")
    void cachedPolicyMutatorMonotonicallyIncrementsEpoch() {
        Long fictionId = createFiction("Epoch Test Fiction", false);
        RagSourceView src = RagTestCommands.register(ragOperations, RagSourceType.FICTION, String.valueOf(fictionId));

        RagSourceView v1 = RagTestCommands.approve(ragOperations, src.id(), Set.of("title"));
        long epoch1 = v1.authorizationEpoch();

        RagSourceView v2 = RagTestCommands.approve(ragOperations, src.id(), Set.of("title", "description"));
        long epoch2 = v2.authorizationEpoch();
        assertThat(epoch2).isGreaterThan(epoch1);

        RagSourceView v3 = RagTestCommands.approve(ragOperations, src.id(), Set.of("title"));
        long epoch3 = v3.authorizationEpoch();
        assertThat(epoch3).isGreaterThan(epoch2);
    }

    private static void assertBusy(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
    }

    private static String sqlState(Throwable error) {
        while(error!=null) {
            if(error instanceof java.sql.SQLException sql)return sql.getSQLState();
            error=error.getCause();
        }
        throw new AssertionError("Expected actual PostgreSQL conflict");
    }
}
