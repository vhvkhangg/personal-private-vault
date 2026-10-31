package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor.RawCandidateGeneration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@org.springframework.boot.test.context.SpringBootTest(
        classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
)
class RagQaSenderIntegrationTest extends AbstractRagIntegrationTest {

    @DynamicPropertySource
    static void enableProperties(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.qa.enabled", () -> "true");
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @Autowired
    private RagQaSupervisor ragQaSupervisor;

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
                List.of("No context available"),
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
    @DisplayName("Non-blocking asynchronous sender flushes full response and cleans up reader")
    void nonBlockingSenderFlushesFullResponseAndCleansUp() throws Exception {
        UUID readerId = UUID.randomUUID();
        var command = new RagQaCommand(readerId, "Sender test question", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en");

        var response = requestQaOverHttp(command);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).isPresent().get().asString().contains("application/json");

        var tree = objectMapper.readTree(response.body());
        assertThat(tree.path("error").isNull()).isTrue();
        assertThat(tree.path("data").isObject()).isTrue();
        assertThat(tree.path("data").path("requestId").asText()).isEqualTo(readerId.toString());

        // Reader transitions to TERMINAL
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'",
                        Integer.class, readerId) == 1
        );
    }

    @Test
    @DisplayName("Client disconnect during transmission cleans up reader to TERMINAL without resource leaks")
    void clientDisconnectCleansUpReader() throws Exception {
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        UUID readerId = UUID.randomUUID();
        var command = new RagQaCommand(readerId, "Disconnect test question", RagRetrievalMode.GENERIC, null, null, null, 5, List.of(), "en");
        byte[] body = objectMapper.writeValueAsBytes(command);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            OutputStream out = socket.getOutputStream();
            String rawHttp = "POST /api/v1/rag/qa HTTP/1.1\r\n" +
                    "Host: 127.0.0.1:" + port + "\r\n" +
                    "Authorization: " + bearerHeader() + "\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + body.length + "\r\n" +
                    "Connection: close\r\n\r\n";
            out.write(rawHttp.getBytes(StandardCharsets.UTF_8));
            out.write(body);
            out.flush();

            // Read just the first few bytes of response line then abort socket abruptly
            InputStream in = socket.getInputStream();
            byte[] buf = new byte[32];
            int read = in.read(buf);
            assertThat(read).isGreaterThan(0);
            socket.setSoLinger(true, 0); // Force TCP RST on close
        }

        // Reader must still transition to TERMINAL even after client socket abort
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(10)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'",
                        Integer.class, readerId) == 1
        );
    }
}
