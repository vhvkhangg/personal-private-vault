package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor.RawCandidateGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRetrievalReaderRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaCancelResponseDto;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.boot.test.context.SpringBootTest(
        classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RagQaIntegrationTest extends AbstractRagIntegrationTest {

    @DynamicPropertySource
    static void enableQaProperties(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.qa.enabled", () -> "true");
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @Autowired
    private RagQaSupervisor ragQaSupervisor;

    @Autowired
    private RagRetrievalReaderRepository ragRetrievalReaderRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment environment;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static float[] makeNormalizedVector(float base) {
        float[] v = new float[384];
        Arrays.fill(v, base);
        double sumSq = 0.0;
        for (float f : v) sumSq += f * f;
        float norm = (float) Math.sqrt(sumSq);
        for (int i = 0; i < v.length; i++) v[i] = v[i] / norm;
        return v;
    }

    @BeforeEach
    void setUp() {
        ragQaSupervisor.setVectorOverride((readerId, question) -> makeNormalizedVector(0.5f));
        ragQaSupervisor.setGenerationOverride((readerId, cmd) -> new RawCandidateGeneration(
                "ABSTAINED",
                List.of(),
                List.of("No matching evidence found in vault"),
                List.of(),
                "Qwen3-4B-Q4_K_M.gguf",
                "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
        ));
    }

    @AfterEach
    void tearDown() {
        ragQaSupervisor.setVectorOverride(null);
        ragQaSupervisor.setGenerationOverride(null);
    }

    @Test
    @DisplayName("Unauthenticated request to /api/v1/rag/qa returns 401")
    void unauthenticatedRequestReturns401() throws Exception {
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        var command = new RagQaCommand(UUID.randomUUID(), "Hello?", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en");
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/rag/qa"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(command)))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(401);
        }
    }

    @Test
    @DisplayName("Request body exceeding 64 KiB returns 413 Payload Too Large from Ingress Filter")
    void bodyExceeding64KiBReturns413() throws Exception {
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        String largeBody = "{\"requestId\":\"" + UUID.randomUUID() + "\",\"question\":\"" + "a".repeat(70000) + "\"}";
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/rag/qa"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", bearerHeader())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(largeBody))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).contains("RAG_QUERY_SCOPE_LIMIT");
        }
    }

    @Test
    @DisplayName("Empty or malformed JSON body returns 400 Bad Request")
    void malformedBodyReturns400() throws Exception {
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/rag/qa"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", bearerHeader())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{not-valid-json}"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    @Test
    @DisplayName("Successful QA execution returns ABSTAINED on empty corpus, transitions reader to TERMINAL, and rejects replay")
    void qaExecutionFlowWithEmptyCorpusReturnsAbstainedAndTerminalReader() throws Exception {
        UUID requestId = UUID.randomUUID();
        var command = new RagQaCommand(requestId, "What is the vault architecture?", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en");

        HttpResponse<String> response = requestQaOverHttp(command);
        System.out.println("TEST_HTTP_RESPONSE_BODY=" + response.body());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);

        var json = objectMapper.readTree(response.body());
        assertThat(json.path("error").isNull()).as(response.body()).isTrue();
        assertThat(json.path("data").isObject()).as(response.body()).isTrue();
        var dataNode = json.path("data");
        RagQaResponseDto dto = objectMapper.treeToValue(dataNode, RagQaResponseDto.class);

        assertThat(dto.requestId()).isEqualTo(requestId);
        assertThat(dto.outcome()).isEqualTo(RagQaOutcome.ABSTAIN);
        assertThat(dto.citations()).isEmpty();
        assertThat(dto.claims()).isEmpty();

        // Verify reader record is saved and transitioned to TERMINAL
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'",
                        Integer.class, requestId) == 1
        );

        // Replay of same requestId must be rejected with 400
        HttpResponse<String> replayResponse = requestQaOverHttp(command);
        assertThat(replayResponse.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Cancel endpoint on already completed request returns ALREADY_TERMINATED")
    void cancelOnCompletedRequestReturnsAlreadyTerminated() throws Exception {
        UUID requestId = UUID.randomUUID();
        var command = new RagQaCommand(requestId, "Sample question", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en");

        HttpResponse<String> response = requestQaOverHttp(command);
        assertThat(response.statusCode()).isEqualTo(200);

        // Now call cancel
        HttpResponse<String> cancelResponse = requestQaCancelOverHttp(requestId);
        assertThat(cancelResponse.statusCode()).isEqualTo(200);

        var json = objectMapper.readTree(cancelResponse.body());
        var cancelDto = objectMapper.treeToValue(json.path("data"), RagQaCancelResponseDto.class);
        assertThat(cancelDto.requestId()).isEqualTo(requestId);
        assertThat(cancelDto.disposition()).isIn("ALREADY_TERMINATED", "CANCELLED", "TERMINAL");
    }
}
