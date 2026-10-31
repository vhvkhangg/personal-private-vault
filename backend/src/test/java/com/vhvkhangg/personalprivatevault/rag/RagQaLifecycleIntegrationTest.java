package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor.RawCandidateGeneration;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagQaCancelResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.boot.test.context.SpringBootTest(
        classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RagQaLifecycleIntegrationTest extends AbstractRagIntegrationTest {

    @DynamicPropertySource
    static void enableProperties(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.qa.enabled", () -> "true");
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @Autowired
    private RagQaSupervisor ragQaSupervisor;

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
    }

    @AfterEach
    void tearDown() {
        ragQaSupervisor.setVectorOverride(null);
        ragQaSupervisor.setGenerationOverride(null);
    }

    @Test
    @DisplayName("Single reader mutual exclusion: second concurrent QA request receives 409 Busy")
    void concurrentQaRequestsReceive409Busy() throws Exception {
        CountDownLatch generationStarted = new CountDownLatch(1);
        CountDownLatch letGenerationFinish = new CountDownLatch(1);

        ragQaSupervisor.setGenerationOverride((readerId, cmd) -> {
            generationStarted.countDown();
            try {
                letGenerationFinish.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new RawCandidateGeneration(
                    "ABSTAINED", List.of(), List.of("Unanswered"), List.of(),
                    "Qwen3-4B-Q4_K_M.gguf", "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
            );
        });

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        var executor = Executors.newSingleThreadExecutor();
        AtomicReference<HttpResponse<String>> firstResponse = new AtomicReference<>();
        executor.submit(() -> {
            try {
                firstResponse.set(requestQaOverHttp(new RagQaCommand(id1, "Q1", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait until first request is actively in generation
        assertThat(generationStarted.await(5, TimeUnit.SECONDS)).isTrue();

        // Second request while first is active must receive 409
        HttpResponse<String> secondResponse = requestQaOverHttp(new RagQaCommand(id2, "Q2", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en"));
        assertThat(secondResponse.statusCode()).isEqualTo(409);

        // Allow first request to finish
        letGenerationFinish.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(firstResponse.get().statusCode()).isEqualTo(200);

        // Verify both reader states cleaned up
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE state<>'TERMINAL'", Integer.class) == 0
        );
    }

    @Test
    @DisplayName("Mutual exclusion across Retrieval and QA: Retrieval receives 409 while QA is active")
    void retrievalReceives409WhileQaActive() throws Exception {
        CountDownLatch generationStarted = new CountDownLatch(1);
        CountDownLatch letGenerationFinish = new CountDownLatch(1);

        ragQaSupervisor.setGenerationOverride((readerId, cmd) -> {
            generationStarted.countDown();
            try {
                letGenerationFinish.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new RawCandidateGeneration(
                    "ABSTAINED", List.of(), List.of("Unanswered"), List.of(),
                    "Qwen3-4B-Q4_K_M.gguf", "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
            );
        });

        UUID qaId = UUID.randomUUID();
        UUID retrievalId = UUID.randomUUID();

        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                requestQaOverHttp(new RagQaCommand(qaId, "QA Q", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en"));
            } catch (Exception ignored) {
            }
        });

        assertThat(generationStarted.await(5, TimeUnit.SECONDS)).isTrue();

        // Retrieval request must be rejected with 409
        HttpResponse<String> retrievalResponse = requestRetrievalOverHttp(new RagRetrievalCommand(retrievalId, "Retrieval Q", RagRetrievalMode.GENERIC, null, null, null, 5));
        assertThat(retrievalResponse.statusCode()).isEqualTo(409);

        letGenerationFinish.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE state<>'TERMINAL'", Integer.class) == 0
        );
    }

    @Test
    @DisplayName("Active QA request cancelled via /api/v1/rag/qa/{requestId}/cancel releases reader promptly")
    void activeQaRequestCancellationReleasesReader() throws Exception {
        CountDownLatch generationStarted = new CountDownLatch(1);
        CountDownLatch cancellationAcknowledged = new CountDownLatch(1);

        ragQaSupervisor.setGenerationOverride((readerId, cmd) -> {
            generationStarted.countDown();
            try {
                cancellationAcknowledged.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new RawCandidateGeneration(
                    "ABSTAINED", List.of(), List.of("Unanswered"), List.of(),
                    "Qwen3-4B-Q4_K_M.gguf", "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
            );
        });

        UUID qaId = UUID.randomUUID();
        var executor = Executors.newSingleThreadExecutor();
        AtomicReference<HttpResponse<String>> qaHttpResponse = new AtomicReference<>();
        executor.submit(() -> {
            try {
                qaHttpResponse.set(requestQaOverHttp(new RagQaCommand(qaId, "Cancel me", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en")));
            } catch (Exception ignored) {
            }
        });

        assertThat(generationStarted.await(5, TimeUnit.SECONDS)).isTrue();

        // Issue cancel command
        HttpResponse<String> cancelResponse = requestQaCancelOverHttp(qaId);
        assertThat(cancelResponse.statusCode()).isEqualTo(200);

        var json = objectMapper.readTree(cancelResponse.body());
        var cancelDto = objectMapper.treeToValue(json.path("data"), RagQaCancelResponseDto.class);
        assertThat(cancelDto.disposition()).isIn("CANCELLED", "ALREADY_TERMINATED");

        cancellationAcknowledged.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        // Reader row should be TERMINAL
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'", Integer.class, qaId) == 1
        );
    }
}
