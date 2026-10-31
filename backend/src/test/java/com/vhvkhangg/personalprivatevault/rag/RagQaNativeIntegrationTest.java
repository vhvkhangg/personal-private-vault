package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor.QaSession;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; no native QA runs authorized.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 */
@org.springframework.boot.test.context.SpringBootTest(
        classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RagQaNativeIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @DynamicPropertySource
    static void enableNativeQa(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.qa.enabled", () -> "true");
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
        properties.add("ppv.rag.test-scoped-execution", () -> "true");
    }

    @Autowired
    private RagQaSupervisor ragQaSupervisor;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority retrievalAuthority;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity runtime;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @org.junit.jupiter.api.io.TempDir
    java.nio.file.Path producerInput;

    @BeforeEach
    void setUp() {
        ragQaSupervisor.setVectorOverride(null);
        ragQaSupervisor.setGenerationOverride(null);
    }

    @AfterEach
    void tearDown() {
        ragQaSupervisor.setVectorOverride(null);
        ragQaSupervisor.setGenerationOverride(null);
    }

    @Test
    @DisplayName("Native QA worker lifecycle: startSession, readiness, embed, and clean process tree termination")
    void nativeQaWorkerLifecycleReadinessAndEmbed() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        retrievalAuthority.admitEmpty(readerId, token, runtime.instance().toString());
        QaSession session = ragQaSupervisor.startSession(readerId);
        long pid = session.process().pid();
        try {
            assertThat(session).isNotNull();
            assertThat(session.process().pid()).isGreaterThan(0);
            assertThat(session.port()).isGreaterThan(1024);

            // Test query embedding via native worker
            float[] vector = ragQaSupervisor.computeEmbedding(session, "What is the capital of Vietnam?");
            assertThat(vector).hasSize(384);

            // Verify L2 norm == 1.0
            double sumSq = 0.0;
            for (float v : vector) sumSq += (double) v * (double) v;
            assertThat(Math.sqrt(sumSq)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.01));
        } finally {
            ragQaSupervisor.stopWorker(readerId);
            ragQaSupervisor.forgetStopped(readerId);
            try {
                retrievalAuthority.finish(readerId, token, "TERMINAL", "CANCELLED", Map.of(), null, null);
            } catch (Exception ignored) {}
            assertThat(ProcessHandle.of(pid).isEmpty() || !ProcessHandle.of(pid).get().isAlive()).isTrue();
        }
    }

    @Test
    @DisplayName("End-to-end native QA flow with real Markdown corpus and Candidate C1 generation")
    void realMarkdownCorpusNativeQaFlow() throws Exception {
        // Prepare genuine Markdown document
        String markdown = "# Thư viện và Kho lưu trữ\n\nKho lưu trữ bí mật được đặt tại tầng hầm tòa nhà vòm xanh ở Hà Nội.";
        Path notePath = producerInput.resolve("archive.md");
        Files.writeString(notePath, markdown, StandardCharsets.UTF_8);

        var candidate = enabledRag.registerSnapshotCandidate(notePath.toString());
        var approved = enabledRag.approveSource(candidate.id(), new RagConsentCommand(
                Set.of("content"), candidate.sourceVersion(), candidate.consentResetVersion(),
                candidate.authorizationEpoch(), candidate.fieldPolicyVersion(), null, candidate.snapshot()
        ));

        var roots = List.of(new RagDocumentRootItem(
                "archive-1", approved.sourceType(), approved.sourceId(), approved.sourceVersion(),
                approved.authorizationEpoch(), approved.fieldPolicyVersion(), approved.approvedFields()
        ));
        var tasks = List.of(new RagDocumentBatchTask(
                "archive-1", approved.sourceId(), "MD", null, null, null, List.of("content"), Map.of()
        ));

        var job = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots, Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(), job.attemptId(), tasks);
        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, job.jobId()
        );
        var pub = enabledRag.finalizeDocumentBatch(
                job.jobId(), job.attemptId(),
                new RagDocumentBatchFinalizeCommand(com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest))
        );
        assertThat(pub.publishedCount()).isEqualTo(1);

        // Execute real QA over HTTP using native worker (without overrides)
        UUID requestId = UUID.randomUUID();
        var command = new RagQaCommand(
                requestId,
                "Kho lưu trữ bí mật được đặt ở đâu?",
                RagRetrievalMode.GENERIC,
                null, null, null, 5,
                List.of(),
                "vi"
        );

        HttpResponse<String> response = requestQaOverHttp(command);
        System.out.println("TEST_NATIVE_BODY=" + response.body());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);

        var json = objectMapper.readTree(response.body());
        assertThat(json.path("error").isNull()).isTrue();
        assertThat(json.path("data").isObject()).isTrue();
        var data = json.path("data");
        RagQaResponseDto dto = objectMapper.treeToValue(data, RagQaResponseDto.class);

        assertThat(dto.requestId()).isEqualTo(requestId);
        assertThat(dto.outcome()).isIn(RagQaOutcome.ANSWERED, RagQaOutcome.PARTIAL);
        assertThat(dto.answer()).isNotEmpty();
        assertThat(dto.citations()).isNotEmpty();
        assertThat(dto.claims()).isNotEmpty();

        // Verify reader is marked TERMINAL
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'",
                        Integer.class, requestId) == 1
        );
    }
}
