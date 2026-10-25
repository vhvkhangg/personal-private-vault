package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagIndexGenerationRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
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
class RagSlice4LifecycleIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @Autowired
    private RagOriginalBytesStore byteStore;

    @Autowired
    private RagGenerationState generationState;

    @Autowired
    private RagIndexGenerationRepository indexGenerations;

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
    void testS4_05_EligibilityProbesAndTamperDenial() throws Exception {
        PublishedFile pub = publishMd("root-s4-05", "note_s4_05.md", "# S4-05\n\nContent.");
        UUID genId = pub.generationId();
        assertThat(generationState.state(genId)).isEqualTo("CURRENT");

        // 1. Pipeline version tamper -> returns OUTDATED
        Long activeGenPk = jdbcTemplate.queryForObject(
                "SELECT id FROM rag.index_generations WHERE generation_id = ?", Long.class, genId
        );
        jdbcTemplate.update("UPDATE rag.generation_roots SET model_version = 'tampered-model' WHERE generation_id = ?", activeGenPk);
        assertThat(generationState.state(genId)).isEqualTo("OUTDATED");
        jdbcTemplate.update("UPDATE rag.generation_roots SET model_version = '2.0.0' WHERE generation_id = ?", activeGenPk);
        assertThat(generationState.state(genId)).isEqualTo("CURRENT");

        // 2. Revoke source -> returns REVOKED
        enabledRag.revokeSource(pub.view().id());
        assertThat(generationState.state(genId)).isEqualTo("REVOKED");
    }

    @Test
    void testS4_07_ConcurrentRevokeAndAntiResurrection() throws Exception {
        PublishedFile pub = publishMd("root-s4-07", "note_s4_07.md", "# S4-07\n\nContent.");
        enabledRag.revokeSource(pub.view().id());

        RagSourceView postRevoke = enabledRag.findSource(pub.view().id()).orElseThrow();
        assertThat(postRevoke.accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(postRevoke.revokedAt()).isNotNull();

        // Attempting to capture equal bytes on revoked source cannot resurrect authority
        RagSnapshotCaptureCommand cmd = new RagSnapshotCaptureCommand(pub.path().toString(), pub.view().snapshot());
        assertThatThrownBy(() -> enabledRag.capturePendingRevision(pub.view().id(), cmd))
                .isInstanceOf(RagSourceChangedException.class);
    }

    @Test
    void testS4_10_MixedBatchExecutionAndIdempotentFinalize() throws Exception {
        // Publish file A
        PublishedFile pubA = publishMd("root-a", "note_a.md", "# Note A\n\nContent A.");
        UUID genA = pubA.generationId();

        // Register and approve file B
        Path fileB = workDir.resolve("note_b.md");
        Files.write(fileB, "# Note B\n\nContent B.".getBytes(StandardCharsets.UTF_8));
        RagSourceView regB = enabledRag.registerSnapshotCandidate(fileB.toString());
        RagConsentCommand cmdB = new RagConsentCommand(
                Set.of("content"), regB.sourceVersion(), regB.consentResetVersion(),
                regB.authorizationEpoch(), regB.fieldPolicyVersion(), null, regB.snapshot()
        );
        RagSourceView appB = enabledRag.approveSource(regB.id(), cmdB);

        // Mixed batch: roster contains root-a (unchanged) and root-b (new)
        RagDocumentRootItem itemA = new RagDocumentRootItem(
                "root-a", pubA.view().sourceType(), pubA.view().sourceId(), pubA.view().sourceVersion(),
                pubA.view().authorizationEpoch(), pubA.view().fieldPolicyVersion(), pubA.view().approvedFields()
        );
        RagDocumentRootItem itemB = new RagDocumentRootItem(
                "root-b", appB.sourceType(), appB.sourceId(), appB.sourceVersion(),
                appB.authorizationEpoch(), appB.fieldPolicyVersion(), appB.approvedFields()
        );

        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(itemA, itemB), Map.of(), Set.of(itemA.rootKey()))
        );

        // Java-proven unchanged intent was bound at admission; only root-b executes.
        RagDocumentBatchTask taskB = new RagDocumentBatchTask(
                "root-b", appB.sourceId(), "MD", null, null, null,
                appB.approvedFields().stream().sorted().toList(), Map.of()
        );
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(taskB));

        // Finalize: Java gate handles root-a as unexecuted -> assigns NO_CHANGE; root-b as SUCCESS -> publishes new G
        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        RagBatchFinalizationResult result1 = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        assertThat(result1.jobStatus()).isEqualTo("COMPLETED");
        assertThat(result1.publishedCount()).isEqualTo(1); // Only root-b published a new generation
        assertThat(result1.failedCount()).isZero();

        // Verify root-a points to retained G_A
        assertThat(result1.publishedGenerations().get("root-a")).isEqualTo(genA);
        // Verify root-b has new G_B
        UUID genB = result1.publishedGenerations().get("root-b");
        assertThat(genB).isNotNull();

        // Idempotent retry of finalizeDocumentBatch returns same result without error
        RagBatchFinalizationResult result2 = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);
        assertThat(result2.jobStatus()).isEqualTo(result1.jobStatus());
        assertThat(result2.publishedCount()).isEqualTo(result1.publishedCount());
        assertThat(result2.publishedGenerations()).isEqualTo(result1.publishedGenerations());
    }

    @Test
    void testS4_11_TruthfulBatchReport_AllOutcomesAndQualifiedFreshness() throws Exception {
        // Publish file A
        PublishedFile pubA = publishMd("root-rep-a", "note_rep_a.md", "# Rep A\n\nContent A.");

        // Register and approve file B
        Path fileB = workDir.resolve("note_rep_b.md");
        Files.write(fileB, "# Rep B\n\nContent B.".getBytes(StandardCharsets.UTF_8));
        RagSourceView regB = enabledRag.registerSnapshotCandidate(fileB.toString());
        RagConsentCommand cmdB = new RagConsentCommand(
                Set.of("content"), regB.sourceVersion(), regB.consentResetVersion(),
                regB.authorizationEpoch(), regB.fieldPolicyVersion(), null, regB.snapshot()
        );
        RagSourceView appB = enabledRag.approveSource(regB.id(), cmdB);

        // Mixed batch admission
        RagDocumentRootItem itemA = new RagDocumentRootItem(
                "root-rep-a", pubA.view().sourceType(), pubA.view().sourceId(), pubA.view().sourceVersion(),
                pubA.view().authorizationEpoch(), pubA.view().fieldPolicyVersion(), pubA.view().approvedFields()
        );
        RagDocumentRootItem itemB = new RagDocumentRootItem(
                "root-rep-b", appB.sourceType(), appB.sourceId(), appB.sourceVersion(),
                appB.authorizationEpoch(), appB.fieldPolicyVersion(), appB.approvedFields()
        );

        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(itemA, itemB), Map.of(), Set.of(itemA.rootKey()))
        );

        // Execute only root-rep-b
        RagDocumentBatchTask taskB = new RagDocumentBatchTask(
                "root-rep-b", appB.sourceId(), "MD", null, null, null,
                appB.approvedFields().stream().sorted().toList(), Map.of()
        );
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(taskB));

        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        // Verify report
        RagDocumentBatchReport report = enabledRag.getDocumentBatchReport(admitted.jobId());
        assertThat(report.status()).isEqualTo("COMPLETED");
        assertThat(report.summaryOutcome()).isEqualTo("COMPLETED");
        assertThat(report.outcomes()).hasSize(2);

        Map<String, RagFileOutcomeView> byKey = new HashMap<>();
        for (var o : report.outcomes()) {
            byKey.put(o.rootKey(), o);
        }

        RagFileOutcomeView viewA = byKey.get("root-rep-a");
        assertThat(viewA.result()).isEqualTo("NO_CHANGE");
        assertThat(viewA.publishedNewGeneration()).isFalse();
        assertThat(viewA.freshnessBasis()).isEqualTo("RAG_SNAPSHOT");
        assertThat(viewA.snapshotIndexState()).isEqualTo("SNAPSHOT_CURRENT");
        assertThat(viewA.originalFreshness()).isEqualTo("UNVERIFIED");

        RagFileOutcomeView viewB = byKey.get("root-rep-b");
        assertThat(viewB.result()).isEqualTo("SUCCESS");
        assertThat(viewB.publishedNewGeneration()).isTrue();
        assertThat(viewB.freshnessBasis()).isEqualTo("RAG_SNAPSHOT");
        assertThat(viewB.snapshotIndexState()).isEqualTo("SNAPSHOT_CURRENT");
        assertThat(viewB.originalFreshness()).isEqualTo("UNVERIFIED");
    }

    @Test
    void testS4_12_StorageCleanup_TempCaptureCleanedAfterCapture() throws Exception {
        Path file = workDir.resolve("note_s4_12.md");
        Files.write(file, "# S4-12\n\nContent.".getBytes(StandardCharsets.UTF_8));
        RagSourceView registered = enabledRag.registerSnapshotCandidate(file.toString());

        // Verify temp directory has no leftover .tmp files
        var entries = byteStore.nextRecoveryBatch(50);
        assertThat(entries.stream().filter(RagOriginalBytesStore.StorageEntry::temporary)).isEmpty();
    }
}
