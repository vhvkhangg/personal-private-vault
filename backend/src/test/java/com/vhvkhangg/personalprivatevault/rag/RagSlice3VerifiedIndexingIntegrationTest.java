package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentFinalizationGate;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagExternalFileAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagSourceRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
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
class RagSlice3VerifiedIndexingIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @Autowired
    private RagExternalFileAuthority externalFileAuthority;

    @Autowired
    private RagOriginalBytesStore byteStore;

    @Autowired
    private RagSourceRepository sourceRepository;

    @Autowired
    private RagDocumentFinalizationGate finalizationGate;

    @TempDir
    Path workDir;

    private RagSourceView createAndApproveMdFile(String filename, String content) throws Exception {
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
        return enabledRag.approveSource(registered.id(), cmd);
    }

    @Test
    void testS3_02_Transport_ImmutableBytesAndUnicodeAndFormatDenial() throws Exception {
        // Unicode text: Vietnamese, English, Simplified Chinese, Traditional Chinese
        String unicodeContent = "# Tiêu đề văn bản\n\n"
                + "Nội dung tiếng Việt có dấu đầy đủ.\n"
                + "English paragraph for verified indexing.\n"
                + "简体中文段落内容验证。\n"
                + "繁體中文段落內容驗證。\n";

        Path file = workDir.resolve("unicode_test.md");
        Files.write(file, unicodeContent.getBytes(StandardCharsets.UTF_8));

        RagSourceView view = enabledRag.registerSnapshotCandidate(file.toString());
        RagConsentCommand cmd = new RagConsentCommand(
                Set.of("content"),
                view.sourceVersion(),
                view.consentResetVersion(),
                view.authorizationEpoch(),
                view.fieldPolicyVersion(),
                null,
                view.snapshot()
        );
        RagSourceView approved = enabledRag.approveSource(view.id(), cmd);
        UUID fileSourceId = UUID.fromString(approved.sourceId());

        // Alter / delete original file after seal
        Files.delete(file);
        assertThat(Files.exists(file)).isFalse();

        // 1. Verify denial of wrong format (e.g. "TXT" or ".markdown")
        RagDocumentRootItem rootItem = new RagDocumentRootItem(
                "root-s2", approved.sourceType(), approved.sourceId(), approved.sourceVersion(),
                approved.authorizationEpoch(), approved.fieldPolicyVersion(), approved.approvedFields()
        );
        RagJobAdmissionResult admittedWrongFmt = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(rootItem), Map.of())
        );

        RagDocumentBatchTask wrongFmtTask = new RagDocumentBatchTask(
                "root-s2", approved.sourceId(), "TXT", null, null, null,
                approved.approvedFields().stream().sorted().toList(), Map.of()
        );
        assertThatThrownBy(() -> enabledRag.executeDocumentBatch(
                admittedWrongFmt.jobId(), admittedWrongFmt.attemptId(), List.of(wrongFmtTask)
        )).isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(IllegalArgumentException.class);

        // 2. Verify denial of caller-supplied body
        RagJobAdmissionResult admittedCallerBody = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(rootItem), Map.of())
        );
        RagDocumentBatchTask callerBodyTask = new RagDocumentBatchTask(
                "root-s2", approved.sourceId(), "MD", "unauthorized caller body text", null, null,
                approved.approvedFields().stream().sorted().toList(), Map.of()
        );
        assertThatThrownBy(() -> enabledRag.executeDocumentBatch(
                admittedCallerBody.jobId(), admittedCallerBody.attemptId(), List.of(callerBodyTask)
        )).isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(IllegalArgumentException.class);

        // 3. Legitimate dispatch: Java-owned snapshot bytes read from byteStore
        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(rootItem), Map.of())
        );
        RagDocumentBatchTask validTask = new RagDocumentBatchTask(
                "root-s2", approved.sourceId(), "MD", null, null, null,
                approved.approvedFields().stream().sorted().toList(), Map.of()
        );
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(validTask));

        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        RagBatchFinalizationResult result = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        assertThat(result.jobStatus()).isEqualTo("COMPLETED");
        assertThat(result.publishedCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();

        // Check external file source promoted
        RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);
        assertThat(ext.getSelectedRevisionId()).isNotNull();
        assertThat(ext.getPendingRevisionId()).isNull();
        assertThat(ext.getSourceVersion()).isEqualTo(2L);
        assertThat(ext.getReservedBy()).isNull();

        // Check published chunks contain Unicode text
        String chunkContent = jdbcTemplate.queryForObject(
                "SELECT content FROM rag.document_chunks WHERE source_identity=? LIMIT 1", String.class, approved.sourceId()
        );
        assertThat(chunkContent).contains("Tiêu đề văn bản");
    }

    @Test
    void testS3_03_MixedEightSuccessTwoFailure_AtomicPublicationAndRelease() throws Exception {
        var approvedSources = new ArrayList<RagSourceView>();
        var rootItems = new ArrayList<RagDocumentRootItem>();
        var tasks = new ArrayList<RagDocumentBatchTask>();

        for (int i = 0; i < 10; i++) {
            boolean failCase = i >= 8;
            String filename = "note_" + i + ".md";
            String content;
            if (failCase) {
                // Required S3-03 failures are unsupported in the frozen D17 profile.
                // Keep the separate all-empty batch as independent zero-publish coverage.
                content = i == 8 ? "<script>unsupported executable content</script>\n"
                        : "<form><input name=\"blocked\"></form>\n";
            } else {
                content = "# Heading Note " + i + "\n\nThis is valid document content for index note " + i + " with some details.";
            }

            RagSourceView view = createAndApproveMdFile(filename, content);
            approvedSources.add(view);
            String key = "root-" + i;
            rootItems.add(new RagDocumentRootItem(
                    key, view.sourceType(), view.sourceId(), view.sourceVersion(),
                    view.authorizationEpoch(), view.fieldPolicyVersion(), view.approvedFields()
            ));
            tasks.add(new RagDocumentBatchTask(
                    key, view.sourceId(), "MD", null, null, null,
                    view.approvedFields().stream().sorted().toList(), Map.of()
            ));
        }

        // Admit batch
        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(rootItems, Map.of()));

        // Verify reservation union: external_file_sources.reserved_by is set to attemptId for all 10
        Integer reservedCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.external_file_sources WHERE reserved_by = ?",
                Integer.class, admitted.attemptId()
        );
        assertThat(reservedCount).isEqualTo(10);

        // Execute batch
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), tasks);

        // Verify 10 staged outputs exist
        Integer stagedCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.staged_document_outputs WHERE job_id = ?",
                Integer.class, admitted.jobId()
        );
        assertThat(stagedCount).isEqualTo(10);
        assertThat(jdbcTemplate.queryForList(
                "SELECT reason_code FROM rag.staged_document_outputs WHERE job_id=? AND NOT success ORDER BY root_key",
                String.class, admitted.jobId())).containsExactly(
                        "MD_UNSUPPORTED_ACTIVE_HTML", "MD_UNSUPPORTED_ACTIVE_HTML");

        // Finalize batch
        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        RagBatchFinalizationResult result = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        assertThat(result.jobStatus()).isEqualTo("COMPLETED");
        assertThat(result.publishedCount()).isEqualTo(8);
        assertThat(result.failedCount()).isEqualTo(2);

        // Verify reservation union release: 0 sources remain reserved
        Integer remainingReserved = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.external_file_sources WHERE reserved_by IS NOT NULL",
                Integer.class
        );
        assertThat(remainingReserved).isZero();

        // Verify promotions: 8 successful files have selectedRevisionId != null and version 2
        for (int i = 0; i < 8; i++) {
            UUID fileSourceId = UUID.fromString(approvedSources.get(i).sourceId());
            RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);
            assertThat(ext.getSelectedRevisionId()).isNotNull();
            assertThat(ext.getPendingRevisionId()).isNull();
            assertThat(ext.getSourceVersion()).isEqualTo(2L);
        }

        // Verify 2 failed files have selectedRevisionId == null and version 1
        for (int i = 8; i < 10; i++) {
            UUID fileSourceId = UUID.fromString(approvedSources.get(i).sourceId());
            RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);
            assertThat(ext.getSelectedRevisionId()).isNull();
            assertThat(ext.getSourceVersion()).isEqualTo(1L);
        }

        // S3-13: Lost ACK reconciliation: repeat call returns exact same result without duplicate generation
        RagBatchFinalizationResult repeatedResult = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);
        assertThat(repeatedResult).isEqualTo(result);

        // Check batch report summary
        RagDocumentBatchReport report = enabledRag.getDocumentBatchReport(admitted.jobId());
        assertThat(report.summaryOutcome()).isEqualTo("PARTIAL_SUCCESS");
    }

    @Test
    void testS3_04_AllFailedBatch_AbortedAndNoNewGenerations() throws Exception {
        var approvedSources = new ArrayList<RagSourceView>();
        var rootItems = new ArrayList<RagDocumentRootItem>();
        var tasks = new ArrayList<RagDocumentBatchTask>();

        for (int i = 0; i < 2; i++) {
            String filename = "empty_" + i + ".md";
            RagSourceView view = createAndApproveMdFile(filename, "");
            approvedSources.add(view);
            String key = "fail-root-" + i;
            rootItems.add(new RagDocumentRootItem(
                    key, view.sourceType(), view.sourceId(), view.sourceVersion(),
                    view.authorizationEpoch(), view.fieldPolicyVersion(), view.approvedFields()
            ));
            tasks.add(new RagDocumentBatchTask(
                    key, view.sourceId(), "MD", null, null, null,
                    view.approvedFields().stream().sorted().toList(), Map.of()
            ));
        }

        RagJobAdmissionResult admitted = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(rootItems, Map.of()));
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), tasks);

        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId()
        );
        RagDocumentBatchFinalizeCommand finalizeCmd = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        RagBatchFinalizationResult result = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), finalizeCmd);

        assertThat(result.jobStatus()).isEqualTo("ABORTED");
        assertThat(result.publishedCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(2);

        // Zero sources remain reserved
        Integer remainingReserved = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.external_file_sources WHERE reserved_by IS NOT NULL",
                Integer.class
        );
        assertThat(remainingReserved).isZero();

        // Neither file is promoted
        for (var source : approvedSources) {
            UUID fileSourceId = UUID.fromString(source.sourceId());
            RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);
            assertThat(ext.getSelectedRevisionId()).isNull();
            assertThat(ext.getSourceVersion()).isEqualTo(1L);
        }
    }

    @Test
    void testS3_09_ConsentBarrier_StaleVersionOrRevoked_RejectsAdmission() throws Exception {
        RagSourceView view = createAndApproveMdFile("stale_test.md", "# Stale Note\n\nContent.");

        // Stale expectedSourceVersion: view has version 1, but we request with expected version 99
        RagDocumentRootItem staleItem = new RagDocumentRootItem(
                "stale-root", view.sourceType(), view.sourceId(), 99L,
                view.authorizationEpoch(), view.fieldPolicyVersion(), view.approvedFields()
        );

        assertThatThrownBy(() -> enabledRag.admitDocumentBatch(
                new RagDocumentBatchCommand(List.of(staleItem), Map.of())
        )).isInstanceOf(com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException.class);
    }
}
