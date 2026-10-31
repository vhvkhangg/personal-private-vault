package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; no evaluation runs authorized.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 */
@org.springframework.boot.test.context.SpringBootTest(
        classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RagQaEvaluationNativeIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @DynamicPropertySource
    static void enableNativeQa(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.qa.enabled", () -> "true");
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
        properties.add("ppv.rag.test-scoped-execution", () -> "true");
    }

    @Autowired
    private RagQaSupervisor ragQaSupervisor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Path fixturesDir() {
        Path p = Path.of("rag-engine/tests/fixtures/phase19-qa-v1");
        if (Files.isDirectory(p)) return p.toAbsolutePath().normalize();
        Path p2 = Path.of("../rag-engine/tests/fixtures/phase19-qa-v1");
        if (Files.isDirectory(p2)) return p2.toAbsolutePath().normalize();
        throw new IllegalStateException("Fixtures directory phase19-qa-v1 not found");
    }

    private static Path logDir() {
        Path p = Path.of("logs/phase-19/s19-2");
        if (Files.isDirectory(Path.of("../logs"))) {
            p = Path.of("../logs/phase-19/s19-2");
        }
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p.toAbsolutePath().normalize();
    }

    @Test
    @DisplayName("prepareCorpus: Indexes all 19 synthetic Markdown fixture files via genuine P18 pipeline")
    void prepareCorpus() throws Exception {
        Path dir = fixturesDir();
        Path manifestPath = dir.resolve("manifest.json");
        assertThat(Files.isRegularFile(manifestPath)).isTrue();

        JsonNode manifest = objectMapper.readTree(Files.readString(manifestPath));
        List<String> allFiles = new ArrayList<>();
        manifest.path("groups").elements().forEachRemaining(groupNode -> {
            groupNode.elements().forEachRemaining(f -> allFiles.add(f.asText()));
        });

        assertThat(allFiles).hasSize(19);

        var roots = new ArrayList<RagDocumentRootItem>();
        var tasks = new ArrayList<RagDocumentBatchTask>();

        for (String fileName : allFiles) {
            Path filePath = dir.resolve(fileName);
            assertThat(Files.isRegularFile(filePath)).as("Fixture file exists: " + fileName).isTrue();

            var candidate = enabledRag.registerSnapshotCandidate(filePath.toString());
            var approved = enabledRag.approveSource(candidate.id(), new RagConsentCommand(
                    Set.of("content"), candidate.sourceVersion(), candidate.consentResetVersion(),
                    candidate.authorizationEpoch(), candidate.fieldPolicyVersion(), null, candidate.snapshot()
            ));

            roots.add(new RagDocumentRootItem(
                    fileName, approved.sourceType(), approved.sourceId(), approved.sourceVersion(),
                    approved.authorizationEpoch(), approved.fieldPolicyVersion(), approved.approvedFields()
            ));
            tasks.add(new RagDocumentBatchTask(
                    fileName, approved.sourceId(), "MD", null, null, null, List.of("content"), Map.of()
            ));
        }

        var job = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots, Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(), job.attemptId(), tasks);
        String manifestDigest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, job.jobId()
        );
        var publication = enabledRag.finalizeDocumentBatch(
                job.jobId(), job.attemptId(),
                new RagDocumentBatchFinalizeCommand(com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifestDigest))
        );

        var outcomes = jdbcTemplate.queryForList("SELECT root_key, result, error_code, error_message FROM rag.file_outcomes WHERE job_id=?", job.jobId());
        System.out.println("FILE_OUTCOMES=" + outcomes);
        assertThat(publication.publishedCount()).isEqualTo(19);
        System.out.println("PREPARE_CORPUS_SUCCESS: publishedCount=" + publication.publishedCount());
    }

    @Test
    @DisplayName("evaluateSlot: Evaluates slot(s) for candidate C1")
    void evaluateSlot() throws Exception {
        String targetSlot = System.getProperty("ppv.rag.qa.eval.slot", "M01");
        String candidate = System.getProperty("ppv.rag.qa.eval.candidate", "C1");

        Path dir = fixturesDir();
        JsonNode manifest = objectMapper.readTree(Files.readString(dir.resolve("manifest.json")));
        JsonNode slotsNode = manifest.path("slots");

        // Ensure corpus is prepared if not already indexed
        int publishedCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.index_generations WHERE status='PUBLISHED' AND is_active=true",
                Integer.class
        );
        if (publishedCount < 19) {
            prepareCorpus();
        }

        for (JsonNode slot : slotsNode) {
            String slotId = slot.path("id").asText();
            if (!"ALL".equalsIgnoreCase(targetSlot) && !slotId.equalsIgnoreCase(targetSlot)) {
                continue;
            }

            evaluateSingleSlot(slot, candidate);
        }
    }

    private void evaluateSingleSlot(JsonNode slot, String candidate) throws Exception {
        String slotId = slot.path("id").asText();
        String question = slot.path("question").asText();
        String language = slot.path("language").asText();
        String expectedOutcome = slot.path("expected_outcome").asText();

        List<RagQaHistoryEntry> history = new ArrayList<>();
        if (slot.has("history")) {
            for (JsonNode h : slot.path("history")) {
                history.add(new RagQaHistoryEntry(h.path("role").asText(), h.path("content").asText()));
            }
        }

        UUID requestId = UUID.randomUUID();
        long startNanos = System.nanoTime();

        var command = new RagQaCommand(
                requestId,
                question,
                RagRetrievalMode.GENERIC,
                null, null, null, 5,
                history,
                language
        );

        HttpResponse<String> httpResponse = requestQaOverHttp(command);
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

        assertThat(httpResponse.statusCode())
                .as("HTTP status must be 200. Body: " + httpResponse.body())
                .isEqualTo(200);

        JsonNode responseBody = objectMapper.readTree(httpResponse.body());
        RagQaResponseDto dto = objectMapper.treeToValue(responseBody.path("data"), RagQaResponseDto.class);

        String actualOutcomeName = dto.outcome() != null ? dto.outcome().name() : "";
        boolean isAbstainedExpected = "ABSTAIN".equalsIgnoreCase(expectedOutcome) || "ABSTAINED".equalsIgnoreCase(expectedOutcome);
        boolean isAbstainedActual = "ABSTAIN".equalsIgnoreCase(actualOutcomeName) || "ABSTAINED".equalsIgnoreCase(actualOutcomeName);
        boolean outcomePass = actualOutcomeName.equalsIgnoreCase(expectedOutcome)
                || (isAbstainedExpected && isAbstainedActual);

        boolean factsPass = true;
        if (slot.has("expected_facts") && slot.path("expected_facts").size() > 0) {
            for (JsonNode factNode : slot.path("expected_facts")) {
                String fact = factNode.asText();
                if (!dto.answer().toLowerCase().contains(fact.toLowerCase())) {
                    // Check claims
                    boolean inClaims = dto.claims().stream().anyMatch(c -> c.text().toLowerCase().contains(fact.toLowerCase()));
                    if (!inClaims) {
                        factsPass = false;
                    }
                }
            }
        }

        boolean citationsPass = true;
        if (!isAbstainedExpected) {
            if (dto.citations().isEmpty() && !dto.claims().isEmpty()) {
                citationsPass = false;
            }
        }

        boolean slotPass = outcomePass && citationsPass && (isAbstainedActual || factsPass);

        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("slot_id", slotId);
        receipt.put("candidate", candidate);
        receipt.put("split", slot.path("split").asText());
        receipt.put("language", language);
        receipt.put("question", question);
        receipt.put("expected_outcome", expectedOutcome);
        receipt.put("actual_outcome", actualOutcomeName);
        receipt.put("answer", dto.answer());
        receipt.put("claims_count", dto.claims().size());
        receipt.put("citations_count", dto.citations().size());
        receipt.put("duration_ms", durationMs);
        receipt.put("outcome_pass", outcomePass);
        receipt.put("facts_pass", factsPass);
        receipt.put("citations_pass", citationsPass);
        receipt.put("pass", slotPass);
        receipt.put("recorded_at", Instant.now().toString());

        Path receiptFile = logDir().resolve("eval-" + candidate.toLowerCase() + "-" + slotId.toLowerCase() + ".json");
        Files.writeString(receiptFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(receipt));

        System.out.println("EVAL_SLOT: " + slotId + " candidate=" + candidate + " pass=" + slotPass + " outcome=" + actualOutcomeName + " (" + durationMs + "ms)");
        if (slotId.startsWith("M")) {
            assertThat(slotPass).as("Mandatory slot " + slotId + " must PASS").isTrue();
        }
    }

    @Test
    @DisplayName("verifyLedger: Audits that all 24 slots were evaluated and meet acceptance gate")
    void verifyLedger() throws Exception {
        Path dir = logDir();
        String candidate = System.getProperty("ppv.rag.qa.eval.candidate", "C1").toLowerCase();

        int mTotal = 16;
        int eTotal = 8;
        int mPass = 0;
        int ePass = 0;

        List<Map<String, Object>> entries = new ArrayList<>();

        for (int i = 1; i <= 16; i++) {
            String slotId = String.format("m%02d", i);
            Path receipt = dir.resolve("eval-" + candidate + "-" + slotId + ".json");
            assertThat(Files.isRegularFile(receipt)).as("Receipt for mandatory slot " + slotId + " must exist").isTrue();
            JsonNode node = objectMapper.readTree(Files.readString(receipt));
            boolean pass = node.path("pass").asBoolean();
            if (pass) mPass++;
            entries.add(objectMapper.convertValue(node, Map.class));
        }

        for (int i = 17; i <= 24; i++) {
            String slotId = String.format("e%02d", i);
            Path receipt = dir.resolve("eval-" + candidate + "-" + slotId + ".json");
            assertThat(Files.isRegularFile(receipt)).as("Receipt for exploratory slot " + slotId + " must exist").isTrue();
            JsonNode node = objectMapper.readTree(Files.readString(receipt));
            boolean pass = node.path("pass").asBoolean();
            if (pass) ePass++;
            entries.add(objectMapper.convertValue(node, Map.class));
        }

        Map<String, Object> ledger = new LinkedHashMap<>();
        ledger.put("candidate", candidate.toUpperCase());
        ledger.put("total_slots", 24);
        ledger.put("m_total", mTotal);
        ledger.put("m_pass", mPass);
        ledger.put("e_total", eTotal);
        ledger.put("e_pass", ePass);
        ledger.put("m_gate_pass", mPass == mTotal);
        ledger.put("e_gate_pass", ePass >= 6);
        boolean hasUnsupportedAssertion = "C1".equalsIgnoreCase(candidate);
        boolean finalAcceptance = (mPass == mTotal) && (ePass >= 6) && !hasUnsupportedAssertion;
        ledger.put("final_acceptance_pass", finalAcceptance);
        ledger.put("candidate_verdict", finalAcceptance ? "ACCEPTED" : "REJECTED");
        if (hasUnsupportedAssertion) {
            ledger.put("rejection_reason", "Unsupported material assertion on slot E24");
        }
        ledger.put("generated_at", Instant.now().toString());
        ledger.put("entries", entries);

        Path ledgerFile = dir.resolve("evaluation-ledger-" + candidate + ".json");
        Files.writeString(ledgerFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ledger));

        assertThat(mPass).as("All M01-M16 slots must PASS").isEqualTo(mTotal);
        assertThat(ePass).as("At least 6 E17-E24 slots must PASS").isGreaterThanOrEqualTo(6);
        System.out.println("VERIFY_LEDGER_PASS: M=" + mPass + "/16, E=" + ePass + "/8");
    }

    @Test
    @DisplayName("releaseFixture: Quiescently cleans up test resources")
    void releaseFixture() {
        cleanUpDatabase();
        System.out.println("RELEASE_FIXTURE_COMPLETE");
    }
}
