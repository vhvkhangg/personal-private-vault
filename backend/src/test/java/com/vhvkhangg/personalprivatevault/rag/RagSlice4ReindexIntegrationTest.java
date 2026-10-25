package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagIndexGenerationRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Timeout(90)
class RagSlice4ReindexIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @Autowired
    private RagOriginalBytesStore byteStore;

    @Autowired
    private RagGenerationState generationState;

    @Autowired
    private RagIndexGenerationRepository indexGenerations;

    @TempDir
    Path workDir;

    private void admin(String sql, Object... args) throws Exception {
        try (var c = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var s = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) s.setObject(i + 1, args[i]);
            s.executeUpdate();
        }
    }

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
    void testS4_03_NativeExplicitSameRReindex_WithoutTouchingOriginal() throws Exception {
        String content = "# Reindex Original\n\nPure same-R reindex without original on disk.";
        PublishedFile pub = publishMd("root-s4-03", "reindex_s4_03.md", content);

        UUID gen1 = pub.generationId();
        assertThat(generationState.state(gen1)).isEqualTo("CURRENT");

        // Make original O unavailable on disk
        Files.delete(pub.path());
        assertThat(Files.exists(pub.path())).isFalse();

        // Admit reindex batch using selected revision (same expectedSourceVersion)
        RagDocumentRootItem reindexRoot = new RagDocumentRootItem(
                "root-s4-03", pub.view().sourceType(), pub.view().sourceId(), pub.view().sourceVersion(),
                pub.view().authorizationEpoch(), pub.view().fieldPolicyVersion(), pub.view().approvedFields()
        );
        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(reindexRoot), Map.of())
        );

        RagDocumentBatchTask reindexTask = new RagDocumentBatchTask(
                "root-s4-03", pub.view().sourceId(), "MD", null, null, null,
                pub.view().approvedFields().stream().sorted().toList(), Map.of()
        );
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(reindexTask));

        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        RagBatchFinalizationResult finalizeResult = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        assertThat(finalizeResult.jobStatus()).isEqualTo("COMPLETED");
        assertThat(finalizeResult.publishedCount()).isEqualTo(1);
        UUID gen2 = finalizeResult.publishedGenerations().get("root-s4-03");
        assertThat(gen2).isNotNull();
        assertThat(gen2).isNotEqualTo(gen1);

        // Check sourceVersion is NOT bumped on same-R reindex
        RagSourceView refreshed = enabledRag.findSource(pub.view().id()).orElseThrow();
        assertThat(refreshed.sourceVersion()).isEqualTo(pub.view().sourceVersion());
        assertThat(refreshed.snapshot().selectedRevisionId()).isEqualTo(pub.view().snapshot().selectedRevisionId());

        // Prior generation G1 is now superseded
        RagIndexGeneration g1Entity = indexGenerations.findByGenerationId(gen1).orElseThrow();
        assertThat(g1Entity.isActive()).isFalse();

        // New generation G2 is active and CURRENT
        RagIndexGeneration g2Entity = indexGenerations.findByGenerationId(gen2).orElseThrow();
        assertThat(g2Entity.isActive()).isTrue();
        assertThat(generationState.state(gen2)).isEqualTo("CURRENT");
        assertThat(generationState.snapshotState(gen2)).isEqualTo("SNAPSHOT_CURRENT");
    }

    @Test
    void testS4_04_ReindexFailures_RetainsPriorGenerationWhenEligible() throws Exception {
        String content = "# Reindex Failure Note\n\nTesting failure retention.";
        PublishedFile pub = publishMd("root-s4-04", "reindex_s4_04.md", content);
        UUID gen1 = pub.generationId();
        assertThat(generationState.state(gen1)).isEqualTo("CURRENT");

        // Admit reindex batch
        RagDocumentRootItem reindexRoot = new RagDocumentRootItem(
                "root-s4-04", pub.view().sourceType(), pub.view().sourceId(), pub.view().sourceVersion(),
                pub.view().authorizationEpoch(), pub.view().fieldPolicyVersion(), pub.view().approvedFields()
        );
        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(reindexRoot), Map.of())
        );

        RagDocumentBatchTask reindexTask = new RagDocumentBatchTask(
                "root-s4-04", pub.view().sourceId(), "MD", null, null, null,
                pub.view().approvedFields().stream().sorted().toList(), Map.of()
        );
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(reindexTask));

        // Tamper staged output to simulate verifier failure
        admin("UPDATE rag.staged_document_outputs SET chunks_payload = '[]'::jsonb WHERE job_id = ?", admitted.jobId());

        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));

        // Finalization fails due to altered/empty staged output
        assertThatThrownBy(() -> enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd))
                .isInstanceOf(RuntimeException.class);

        // Zero new selected G published
        assertThat(indexGenerations.findByJobId(admitted.jobId())).isEmpty();

        // Prior generation G1 remains active and CURRENT because it is independently eligible
        RagIndexGeneration g1Entity = indexGenerations.findByGenerationId(gen1).orElseThrow();
        assertThat(g1Entity.isActive()).isTrue();
        assertThat(generationState.state(gen1)).isEqualTo("CURRENT");
    }
}
