package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.source.RagSnapshotExpectation;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Timeout(90)
class RagSlice4NoChangeIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @Autowired
    private RagOriginalBytesStore byteStore;

    @Autowired
    private RagGenerationState generationState;

    @TempDir
    Path workDir;

    private record PublishedFile(RagSourceView view, UUID jobId, UUID generationId, Path path) {}

    private PublishedFile publishMd(String rootKey, String filename, String content) throws Exception {
        Path file = workDir.resolve(filename);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        RagSourceView registered = enabledRag.registerSnapshotCandidate(file.toString());
        RagConsentCommand cmd = new RagConsentCommand(
                Set.of("content"),
                registered.sourceVersion(),
                registered.consentResetVersion(),
                registered.authorizationEpoch(),
                registered.fieldPolicyVersion(),
                null,
                registered.snapshot()
        );
        RagSourceView approved = enabledRag.approveSource(registered.id(), cmd);

        RagDocumentRootItem rootItem = new RagDocumentRootItem(
                rootKey, approved.sourceType(), approved.sourceId(), approved.sourceVersion(),
                approved.authorizationEpoch(), approved.fieldPolicyVersion(), approved.approvedFields()
        );
        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(rootItem), Map.of())
        );

        RagDocumentBatchTask task = new RagDocumentBatchTask(
                rootKey, approved.sourceId(), "MD", null, null, null,
                approved.approvedFields().stream().sorted().toList(), Map.of()
        );
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(task));

        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        RagBatchFinalizationResult result = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        RagSourceView refreshed = enabledRag.findSource(approved.id()).orElseThrow();
        UUID genId = result.publishedGenerations().get(rootKey);
        return new PublishedFile(refreshed, admitted.jobId(), genId, file);
    }

    @Test
    void testS4_01_ValidNoChange_UnchangedBytes_PreservesAllState_ZeroWorkerCalls() throws Exception {
        String content = "# Test Note\n\nContent for S4-01 NO_CHANGE test.";
        PublishedFile pub = publishMd("root-s4-01", "note_s4_01.md", content);

        long initialJobCount = jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Long.class);
        long initialRunCount = jdbcTemplate.queryForObject("SELECT count(*) FROM rag.runs", Long.class);
        long initialGenCount = jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations", Long.class);

        // Perform Update capture on exact same file content
        RagSnapshotCaptureCommand updateCmd = new RagSnapshotCaptureCommand(pub.path().toString(), pub.view().snapshot());
        RagSourceView updated = enabledRag.capturePendingRevision(pub.view().id(), updateCmd);

        // Assert S4-01: unchanged S/R/ordinal/sourceVersion/G/pointers/grants
        assertThat(updated.id()).isEqualTo(pub.view().id());
        assertThat(updated.sourceVersion()).isEqualTo(pub.view().sourceVersion());
        assertThat(updated.consentResetVersion()).isEqualTo(pub.view().consentResetVersion());
        assertThat(updated.authorizationEpoch()).isEqualTo(pub.view().authorizationEpoch());
        assertThat(updated.fieldPolicyVersion()).isEqualTo(pub.view().fieldPolicyVersion());
        assertThat(updated.approvedFields()).isEqualTo(pub.view().approvedFields());
        assertThat(updated.snapshot().selectedRevisionId()).isEqualTo(pub.view().snapshot().selectedRevisionId());
        assertThat(updated.snapshot().pendingRevisionId()).isNull();

        // Standalone unchanged update: no new indexing job or fake COMPLETED job created
        long currentJobCount = jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases", Long.class);
        long currentGenCount = jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations", Long.class);
        assertThat(currentJobCount).isEqualTo(initialJobCount);
        assertThat(currentGenCount).isEqualTo(initialGenCount);

        // Stored R bytes independently verify
        UUID fileSourceId = UUID.fromString(pub.view().sourceId());
        UUID selectedRev = pub.view().snapshot().selectedRevisionId();
        assertThat(byteStore.verifyIntegrity(fileSourceId, selectedRev,
                pub.view().sourceRevisionHash(), (long) content.getBytes(StandardCharsets.UTF_8).length)).isTrue();

        // Qualified currentness remains SNAPSHOT_CURRENT
        assertThat(generationState.state(pub.generationId())).isEqualTo("CURRENT");
        assertThat(generationState.snapshotState(pub.generationId())).isEqualTo("SNAPSHOT_CURRENT");
    }

    @Test
    void testS4_02_NoChangeNegatives_ParameterizeDenialsAndReindexRequirements() throws Exception {
        // Case A: Missing active generation -> equal bytes throws IllegalStateException ("Reindex required")
        Path unindexedFile = workDir.resolve("unindexed.md");
        Files.write(unindexedFile, "# Unindexed\n\nContent.".getBytes(StandardCharsets.UTF_8));
        RagSourceView registered = enabledRag.registerSnapshotCandidate(unindexedFile.toString());
        RagConsentCommand cmd = new RagConsentCommand(
                Set.of("content"), registered.sourceVersion(), registered.consentResetVersion(),
                registered.authorizationEpoch(), registered.fieldPolicyVersion(), null, registered.snapshot()
        );
        RagSourceView approvedUnindexed = enabledRag.approveSource(registered.id(), cmd);

        RagSnapshotCaptureCommand updateCmd = new RagSnapshotCaptureCommand(unindexedFile.toString(), approvedUnindexed.snapshot());
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(approvedUnindexed.id(), updateCmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reindex required");

        // Case B: Pipeline mismatch -> equal bytes throws IllegalStateException ("Reindex required: pipeline outdated")
        PublishedFile pubB = publishMd("root-s4-02b", "note_s4_02b.md", "# B Note\n\nContent.");
        Long activeGenPk = jdbcTemplate.queryForObject(
                "SELECT id FROM rag.index_generations WHERE generation_id = ?", Long.class, pubB.generationId()
        );
        jdbcTemplate.update("UPDATE rag.generation_roots SET parser_version = 'stale-parser-v0' WHERE generation_id = ?", activeGenPk);

        RagSnapshotCaptureCommand updateCmdB = new RagSnapshotCaptureCommand(pubB.path().toString(), pubB.view().snapshot());
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(pubB.view().id(), updateCmdB))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pipeline outdated");

        // Restore pipeline version
        jdbcTemplate.update("UPDATE rag.generation_roots SET parser_version = 'ppv-parser-v18.6-d17-html-container-boundaries' WHERE generation_id = ?", activeGenPk);

        // Case C: Stale expectations -> RagSourceChangedException
        RagSnapshotExpectation staleExpectation = new RagSnapshotExpectation(
                999L, pubB.view().consentResetVersion(), pubB.view().authorizationEpoch(),
                pubB.view().fieldPolicyVersion(), pubB.view().authorizationEpoch(), pubB.view().fieldPolicyVersion(),
                pubB.view().snapshot().selectedRevisionId(), null, null, null
        );
        RagSnapshotCaptureCommand staleCmd = new RagSnapshotCaptureCommand(pubB.path().toString(), staleExpectation);
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(pubB.view().id(), staleCmd))
                .isInstanceOf(RagSourceChangedException.class);

        // Case D: Revoked / unapproved source -> RagSourceChangedException
        PublishedFile pubD = publishMd("root-s4-02d", "note_s4_02d.md", "# D Note\n\nContent.");
        enabledRag.revokeSource(pubD.view().id());
        RagSnapshotCaptureCommand revokeUpdateCmd = new RagSnapshotCaptureCommand(pubD.path().toString(), pubD.view().snapshot());
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(pubD.view().id(), revokeUpdateCmd))
                .isInstanceOf(RagSourceChangedException.class);

        // Case E: Consent reset version mismatch -> RagSourceChangedException
        PublishedFile pubE = publishMd("root-s4-02e", "note_s4_02e.md", "# E Note\n\nContent.");
        jdbcTemplate.update("UPDATE rag.sources SET approved_owner_reset_version = 888 WHERE id = ?", pubE.view().id());
        RagSnapshotCaptureCommand resetMismatchCmd = new RagSnapshotCaptureCommand(pubE.path().toString(), pubE.view().snapshot());
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(pubE.view().id(), resetMismatchCmd))
                .isInstanceOf(RagSourceChangedException.class);

        // Case F: Changed capture target file path -> IllegalArgumentException
        Path differentFile = workDir.resolve("different.md");
        Files.write(differentFile, "# Different\n\nDifferent content.".getBytes(StandardCharsets.UTF_8));
        RagSnapshotCaptureCommand wrongPathCmd = new RagSnapshotCaptureCommand(differentFile.toString(), pubB.view().snapshot());
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(pubB.view().id(), wrongPathCmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File intent does not match registered source");
    }

    @Test
    void testS4_06_FreshnessBasisAndOriginalUnverifiedAfterFileEditOrDeletion() throws Exception {
        String content = "# S4-06 Freshness\n\nTesting qualified freshness.";
        PublishedFile pub = publishMd("root-s4-06", "note_s4_06.md", content);

        // Delete the original file on disk
        Files.delete(pub.path());
        assertThat(Files.exists(pub.path())).isFalse();

        // 1. Snapshot generation remains SNAPSHOT_CURRENT despite O being deleted
        assertThat(generationState.state(pub.generationId())).isEqualTo("CURRENT");
        assertThat(generationState.snapshotState(pub.generationId())).isEqualTo("SNAPSHOT_CURRENT");

        // 2. Structured report reflects qualified fields
        RagDocumentBatchReport report = enabledRag.getDocumentBatchReport(pub.jobId());
        assertThat(report.outcomes()).hasSize(1);
        RagFileOutcomeView outcome = report.outcomes().getFirst();

        assertThat(outcome.rootKey()).isEqualTo("root-s4-06");
        assertThat(outcome.result()).isEqualTo("SUCCESS");
        assertThat(outcome.freshnessBasis()).isEqualTo("RAG_SNAPSHOT");
        assertThat(outcome.snapshotIndexState()).isEqualTo("SNAPSHOT_CURRENT");
        assertThat(outcome.originalFreshness()).isEqualTo("UNVERIFIED");
        assertThat(outcome.publishedNewGeneration()).isTrue();
        assertThat(outcome.selectedRevisionId()).isEqualTo(pub.view().snapshot().selectedRevisionId());
        assertThat(outcome.pendingRevisionId()).isNull();
    }
}
