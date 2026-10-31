package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; no evaluation runs authorized.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 * Historical prospective, separately owned fixture; never inherits the resetting driver.
 */
@SpringBootTest(classes = PersonalPrivateVaultApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "ppv.rag.qa.eval.run", matches = ".+")
class RagQaProspectiveEvaluationNativeIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
    private static final Path REPO = Files.isDirectory(Path.of("rag-engine"))
            ? Path.of(".").toAbsolutePath().normalize() : Path.of("..").toAbsolutePath().normalize();
    private static final Path RUN = runDirectory();
    private static final Path STATE = System.getProperty("ppv.rag.qa.eval.state") == null ? RUN
            : Path.of(System.getProperty("ppv.rag.qa.eval.state")).toAbsolutePath().normalize();
    private static final Path RUNTIME_FREEZE = System.getProperty("ppv.rag.qa.eval.runtime-freeze") == null
            ? STATE.resolve("pre-answer-freeze.json")
            : Path.of(System.getProperty("ppv.rag.qa.eval.runtime-freeze")).toAbsolutePath().normalize();
    private static final JsonNode OWNER = read(System.getProperty("ppv.rag.qa.eval.owner-config") == null
            ? STATE.resolve("fixture-ownership.json") : Path.of(System.getProperty("ppv.rag.qa.eval.owner-config")));
    private static final Path FIXTURES = REPO.resolve("rag-engine/tests/fixtures/phase19-qa-v2");

    @Autowired private RagOperations rag;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService tokens;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor supervisor;
    @org.springframework.boot.test.web.server.LocalServerPort private int port;

    private static long availableSystemMemory() {
        try (var arena = java.lang.foreign.Arena.ofConfined()) {
            var symbol = java.lang.foreign.SymbolLookup.libraryLookup("kernel32", arena)
                    .find("GlobalMemoryStatusEx").orElseThrow();
            var call = java.lang.foreign.Linker.nativeLinker().downcallHandle(symbol,
                    java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_INT,
                            java.lang.foreign.ValueLayout.ADDRESS));
            var status = arena.allocate(64, 8);
            status.set(java.lang.foreign.ValueLayout.JAVA_INT, 0, 64);
            int ok = (int) call.invokeExact(status);
            if (ok == 0) throw new IllegalStateException("System headroom unavailable");
            return status.get(java.lang.foreign.ValueLayout.JAVA_LONG, 16);
        } catch (Throwable failure) { throw new IllegalStateException("System headroom unavailable", failure); }
    }

    @Test
    void verifyRuntimeBeforeAnySlotInference() throws Exception {
        JsonNode freeze = verifiedRuntimeFreeze();
        assertFrozenRuntimeConfiguration(freeze);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE state<>'TERMINAL'", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by IS NOT NULL", Integer.class)).isZero();
        long available = availableSystemMemory();
        assertThat(available).as("App-ready conservative preflight without inference")
                .isGreaterThan(appReadyThreshold(freeze));
        Files.writeString(RUN.resolve("runtime-preflight.json"), JSON.writeValueAsString(Map.of(
                "result", "PASS_RUNTIME_AND_APP_READY_ONLY", "available_system_bytes", available,
                "runtime_freeze_sha256", hash(Files.readAllBytes(RUNTIME_FREEZE)),
                "source_freeze_sha256", hash(Files.readAllBytes(STATE.resolve("source-freeze.json"))),
                "native_worker_starts", 0, "candidate_requests", 0, "semantic_slots", 0)), StandardOpenOption.CREATE_NEW);
    }

    private static long appReadyThreshold(JsonNode freeze) {
        if ("C2_OFFLINE_CUDA_TEST_V1".equals(freeze.path("generation_profile").asText())) {
            // Pinned before inference from measured full-GPU startup pressure plus
            // >=2-GiB continuous reserve and 512-MiB additional margin; never below4 GiB.
            return Math.max(4L * 1024 * 1024 * 1024,
                    freeze.path("minimum_app_ready_system_bytes").asLong(4L * 1024 * 1024 * 1024));
        }
        return 4_683_073_632L + 2_147_483_648L;
    }

    private void assertFrozenRuntimeConfiguration(JsonNode freeze) {
        if (!"C2_OFFLINE_CUDA_TEST_V1".equals(freeze.path("generation_profile").asText())) return;
        for (String field : List.of("cudaTestEnabled", "testScopedExecution", "candidate2")) {
            assertThat(org.springframework.test.util.ReflectionTestUtils.getField(supervisor, field)).isEqualTo(true);
        }
        for (var expected : Map.of("threads", 4, "contextTokens", 4096, "maxGeneratedTokens", 768).entrySet()) {
            assertThat(org.springframework.test.util.ReflectionTestUtils.getField(supervisor, expected.getKey())).isEqualTo(expected.getValue());
        }
        for (var expected : Map.of("modelPath", "model_path", "cudaTestLauncher", "runtime_launcher").entrySet()) {
            String actual = (String) org.springframework.test.util.ReflectionTestUtils.getField(supervisor, expected.getKey());
            assertThat(Path.of(actual).toAbsolutePath().normalize())
                    .isEqualTo(Path.of(freeze.path(expected.getValue()).asText()).toAbsolutePath().normalize());
        }
    }

    private static JsonNode verifiedRuntimeFreeze() throws Exception {
        assertThat(RUNTIME_FREEZE.startsWith(REPO.resolve("logs/phase-19/s19-2"))).isTrue();
        JsonNode freeze = read(RUNTIME_FREEZE);
        Path instrumentationPath = RUN.resolve(freeze.path("instrumentation_file")
                .asText("resource-instrumentation-checkpoint-3.json")).normalize();
        assertThat(instrumentationPath.startsWith(RUN)).isTrue();
        JsonNode instrumentation = read(instrumentationPath);
        JsonNode manifest = read(FIXTURES.resolve("manifest.json"));
        assertThat(hash(Files.readAllBytes(STATE.resolve("source-freeze.json")))).isEqualTo(freeze.path("source_freeze_sha256").asText());
        assertThat(hash(Files.readAllBytes(FIXTURES.resolve("manifest.json"))))
                .isEqualTo(freeze.path("manifest_sha256").asText());
        var controls = freeze.path("controls").fields();
        while (controls.hasNext()) {
            var control = controls.next();
            String expected = instrumentation.path("controls").path(control.getKey()).asText(control.getValue().asText());
            assertThat(hash(Files.readAllBytes(REPO.resolve(control.getKey())))).isEqualTo(expected);
        }
        return freeze;
    }

    @Test
    void evaluateSlot() throws Exception {
        JsonNode freeze = verifiedRuntimeFreeze();
        assertFrozenRuntimeConfiguration(freeze);
        JsonNode sourceFreeze = read(STATE.resolve("source-freeze.json"));
        Path instrumentationPath = RUN.resolve(freeze.path("instrumentation_file")
                .asText("resource-instrumentation-checkpoint-3.json")).normalize();
        JsonNode manifest = read(FIXTURES.resolve("manifest.json"));
        String slotId = System.getProperty("ppv.rag.qa.eval.slot", "M01");
        JsonNode slot = java.util.stream.StreamSupport.stream(manifest.path("slots").spliterator(), false)
                .filter(s -> slotId.equals(s.path("id").asText())).findFirst().orElseThrow();
        var selection = new ArrayList<RagSourceSelectionEntry>();
        for (JsonNode name : slot.path("contributors")) {
            JsonNode recorded = sourceFreeze.path("sources").path(name.asText()).path("source");
            var source = rag.findSource(recorded.path("id").asLong()).orElseThrow();
            assertThat(source.snapshot().selectedRevisionId().toString())
                    .isEqualTo(recorded.path("snapshot").path("selectedRevisionId").asText());
            assertThat(source.authorizationEpoch()).isEqualTo(recorded.path("authorizationEpoch").asLong());
            assertThat(source.sourceId()).isEqualTo(recorded.path("sourceId").asText());
            assertThat(source.sourceVersion()).isEqualTo(recorded.path("sourceVersion").asLong());
            assertThat(source.consentResetVersion()).isEqualTo(recorded.path("consentResetVersion").asLong());
            assertThat(source.fieldPolicyVersion()).isEqualTo(recorded.path("fieldPolicyVersion").asInt());
            var expectedGenerations = new ArrayList<String>();
            for (JsonNode old : sourceFreeze.path("sources").path(name.asText()).path("generations")) {
                expectedGenerations.add(JSON.readTree(old.path("row").asText()).path("generation_id").asText());
            }
            assertThat(jdbc.queryForList("SELECT generation_id::text FROM rag.index_generations WHERE source_id=? AND is_active AND status='PUBLISHED'",
                    String.class, source.id())).containsExactlyInAnyOrderElementsOf(expectedGenerations);
            String group = manifest.path("files").path(name.asText()).path("canon").asText();
            selection.add(new RagSourceSelectionEntry(source.id(), source.snapshot().selectedRevisionId(), group, name.asText()));
        }
        var history = new ArrayList<RagQaHistoryEntry>();
        for (JsonNode h : slot.path("history")) history.add(new RagQaHistoryEntry(h.path("role").asText(), h.path("content").asText()));
        UUID requestId = UUID.randomUUID();
        var stoppedUsage = new java.util.concurrent.atomic.AtomicReference<
                com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess.ResourceUsage>();
        var stopNanos = new java.util.concurrent.atomic.AtomicLong();
        var startupNanos = new java.util.concurrent.atomic.AtomicLong();
        var generatorInput = new java.util.concurrent.atomic.AtomicReference<Object>();
        var generatorMeasurements = new java.util.concurrent.atomic.AtomicReference<Map<String, Long>>(Map.of());
        var nativeStarts = new java.util.concurrent.atomic.AtomicInteger();
        var generationCalls = new java.util.concurrent.atomic.AtomicInteger();
        var workerIdentity = new java.util.concurrent.atomic.AtomicReference<Map<String, Long>>();
        org.mockito.Mockito.doAnswer(call -> {
            generationCalls.incrementAndGet();
            generatorInput.set(Map.<String, Object>of("question", call.getArgument(1), "history", call.getArgument(2),
                    "response_language", Objects.toString(call.getArgument(3), ""), "evidence", call.getArgument(4)));
            Object result = call.callRealMethod();
            generatorMeasurements.set(supervisor.generationMeasurements(requestId));
            return result;
        }).when(supervisor).generateCandidate(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.nullable(String.class), org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.nullable(String.class));
        org.mockito.Mockito.doAnswer(call -> {
            nativeStarts.incrementAndGet();
            long began = System.nanoTime();
            var session = (com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor.QaSession) call.callRealMethod();
            startupNanos.set(System.nanoTime() - began);
            workerIdentity.set(Map.of("pid", session.process().pid(), "creation_filetime", session.process().creationTimeFileTime()));
            Files.writeString(RUN.resolve("c2").resolve(slotId + "-worker-identity.json"),
                    JSON.writeValueAsString(workerIdentity.get()), StandardOpenOption.CREATE_NEW);
            return session;
        }).when(supervisor).startSession(org.mockito.ArgumentMatchers.eq(requestId));
        org.mockito.Mockito.doAnswer(call -> {
            long stopStart = System.nanoTime();
            Object result = call.callRealMethod();
            var usage = supervisor.resourceUsage(call.getArgument(0));
            if (usage.peakJobMemoryBytes() > 0) stoppedUsage.set(usage);
            stopNanos.accumulateAndGet(System.nanoTime() - stopStart, Math::max);
            return result;
        }).when(supervisor).stopWorker(org.mockito.ArgumentMatchers.eq(requestId), org.mockito.ArgumentMatchers.anyLong());
        var command = new RagQaCommand(requestId, slot.path("question").asText(),
                RagRetrievalMode.valueOf(slot.path("mode").asText()), selection,
                "prospective-v2", "synthetic-qa-v2", 5, history, slot.path("language").asText());
        Path output = RUN.resolve("c2"); Files.createDirectories(output);
        assertThat(Files.exists(output.resolve(slotId + ".json"))).isFalse();
        Files.writeString(output.resolve(slotId + "-request.json"), JSON.writeValueAsString(command), StandardOpenOption.CREATE_NEW);
        long start = System.nanoTime();
        long minAvailable = availableSystemMemory();
        // A point-in-time >=2 GiB is insufficient. Use the pinned backend-specific
        // startup margin with the disposable JVM resident; CPU weight screen stays unchanged.
        assertThat(minAvailable).as("App-ready conservative preflight before any candidate request")
                .isGreaterThan(appReadyThreshold(freeze));
        var memorySamples = new ArrayList<Map<String, Long>>();
        boolean resourceStopRequested = false;
        java.net.http.HttpResponse<String> response;
        try (var client = java.net.http.HttpClient.newBuilder().proxy(java.net.http.HttpClient.Builder.NO_PROXY).build()) {
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + port + "/api/v1/rag/qa"))
                    .timeout(java.time.Duration.ofSeconds(245)).header("Authorization", "Bearer " + tokens.issueAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(command))).build();
            var future = client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            while (!future.isDone()) {
                long available = availableSystemMemory();
                minAvailable = Math.min(minAvailable, available);
                memorySamples.add(Map.of("elapsed_nanos", System.nanoTime() - start,
                        "available_bytes", available));
                if (!resourceStopRequested && (minAvailable < 2L * 1024 * 1024 * 1024
                        || Files.exists(RUN.resolve("c2").resolve(slotId + "-gpu-resource-stop.request")))) {
                    // Stop only this owned request when the actual system reserve is lost.
                    resourceStopRequested = true;
                    supervisor.stopWorker(requestId, System.nanoTime() + java.time.Duration.ofSeconds(5).toNanos());
                }
                Thread.sleep(50);
            }
            response = future.get();
        }
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(8)).until(() ->
                jdbc.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'", Integer.class, requestId) == 1);
        var terminalReader = jdbc.queryForMap("""
                SELECT reader_id::text AS reader_id,state,emission_started,finish_disposition,emission_disposition,
                       worker_pid,worker_creation_file_time,quiescence_evidence::text AS quiescence_evidence
                FROM rag.retrieval_readers WHERE reader_id=?
                """, requestId);
        long finalAvailable = availableSystemMemory();
        minAvailable = Math.min(minAvailable, finalAvailable);
        memorySamples.add(Map.of("elapsed_nanos", System.nanoTime() - start, "available_bytes", finalAvailable));
        var result = new LinkedHashMap<String, Object>();
        result.put("slot", slotId); result.put("candidate", "C2"); result.put("request_id", requestId);
        result.put("freeze_sha256", hash(Files.readAllBytes(RUNTIME_FREEZE)));
        result.put("resource_instrumentation_sha256", hash(Files.readAllBytes(instrumentationPath)));
        result.put("http_status", response.statusCode()); result.put("response", JSON.readTree(response.body()));
        result.put("arrival_to_terminal_ms", (System.nanoTime() - start) / 1_000_000);
        result.put("minimum_system_available_bytes", minAvailable);
        result.put("system_headroom_timeseries", memorySamples);
        result.put("owned_resource_stop_requested", resourceStopRequested);
        result.put("post_termination_native_usage", stoppedUsage.get());
        result.put("supported_native_measurement_available", stoppedUsage.get() != null);
        result.put("maximum_positive_stop_call_nanos", stopNanos.get());
        result.put("native_startup_to_ready_nanos", startupNanos.get());
        result.put("file_cache_state", "warm after immutable artifact hashing; new child per request");
        result.put("actual_generation_input", generatorInput.get());
        result.put("worker_measurements", generatorMeasurements.get());
        result.put("native_start_calls", nativeStarts.get());
        result.put("generation_calls", generationCalls.get());
        result.put("worker_identity", workerIdentity.get());
        result.put("terminal_reader", terminalReader);
        result.put("native_measurement_applicability", nativeStarts.get() == 0 ? "NO_NATIVE_CHILD" : "REQUIRED");
        result.put("semantic_assessment", "NOT_VERIFIED; actual material support must be assessed separately");
        Files.writeString(output.resolve(slotId + ".json"), JSON.writerWithDefaultPrettyPrinter().writeValueAsString(result), StandardOpenOption.CREATE_NEW);
        assertThat(response.statusCode()).as("Actual prospective candidate HTTP result").isEqualTo(200);
        assertThat(minAvailable).as("Measured system headroom").isGreaterThanOrEqualTo(2L * 1024 * 1024 * 1024);
        assertThat(nativeStarts.get()).isLessThanOrEqualTo(1);
        assertThat(generationCalls.get()).isLessThanOrEqualTo(1);
        if (nativeStarts.get() == 0) {
            JsonNode data = JSON.readTree(response.body()).path("data");
            assertThat(data.path("outcome").asText()).isEqualTo("ABSTAIN");
            assertThat(data.path("claims").isEmpty()).isTrue();
            assertThat(data.path("citations").isEmpty()).isTrue();
            assertThat(generationCalls.get()).isZero();
            assertThat(terminalReader.get("worker_pid")).isNull();
        } else {
            assertThat(stoppedUsage.get()).as("Supported post-termination native accounting").isNotNull();
            assertThat(stoppedUsage.get().peakJobMemoryBytes()).isPositive().isLessThanOrEqualTo(8L * 1024 * 1024 * 1024);
            assertThat(terminalReader.get("worker_pid")).isEqualTo(workerIdentity.get().get("pid"));
            assertThat(terminalReader.get("worker_creation_file_time")).isEqualTo(workerIdentity.get().get("creation_filetime"));
        }
    }

    private static Path runDirectory() {
        String name = System.getProperty("ppv.rag.qa.eval.run");
        if (name == null) throw new IllegalStateException("Explicit owned prospective run required");
        Path dir = Path.of(name).toAbsolutePath().normalize();
        if (!dir.startsWith(REPO.resolve("logs/phase-19/s19-2"))) {
            throw new IllegalStateException("Fixture must be confined to scoped ignored evidence");
        }
        return dir;
    }

    private static JsonNode read(Path path) {
        try { return JSON.readTree(Files.readAllBytes(path)); }
        catch (Exception failure) { throw new IllegalStateException("Fixture metadata unavailable", failure); }
    }

    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void verifyOwnership() throws Exception {
        UUID.fromString(OWNER.path("owner_id").asText());
        String id = OWNER.path("container_id").asText();
        if (Files.isRegularFile(STATE.resolve("source-freeze.json"))) {
            JsonNode frozen = read(STATE.resolve("source-freeze.json"));
            assertThat(id).isEqualTo(frozen.path("container_id").asText());
            assertThat(OWNER.path("owner_id").asText()).isEqualTo(frozen.path("owner_id").asText());
        }
        if (!id.matches("[0-9a-f]{64}")) throw new IllegalStateException("Invalid owned container");
        var process = new ProcessBuilder("docker", "inspect", id).start();
        byte[] output = process.getInputStream().readNBytes(65536);
        if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("Cannot inspect owned fixture container");
        }
        JsonNode actual = JSON.readTree(output).get(0);
        assertThat(actual.path("Config").path("Labels").path("ppv.qa.freeze").asText())
                .isEqualTo(OWNER.path("owner_id").asText());
        assertThat(actual.path("Image").asText()).isEqualTo(OWNER.path("image_id").asText());
        JsonNode port = actual.path("NetworkSettings").path("Ports").path("5432/tcp").get(0);
        assertThat(port.path("HostIp").asText()).isEqualTo("127.0.0.1");
        assertThat(port.path("HostPort").asInt()).isEqualTo(OWNER.path("port").asInt());
        assertThat(OWNER.path("database").asText()).startsWith("ppv_qa_");
    }

    private static String jdbcUrl() {
        return "jdbc:postgresql://127.0.0.1:" + OWNER.path("port").asInt()
                + "/" + OWNER.path("database").asText();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry properties) throws Exception {
        verifyOwnership();
        var migration = org.flywaydb.core.Flyway.configure()
                .dataSource(jdbcUrl(), "ppv_test", OWNER.path("password").asText())
                .locations("classpath:db/migration").load();
        migration.migrate();
        migration.validate();
        assertThat(migration.info().current().getVersion().getVersion()).isEqualTo("24");
        String password = OWNER.path("password").asText();
        assertThat(password).matches("[a-f0-9]{32}");
        try (var connection = DriverManager.getConnection(jdbcUrl(), "ppv_test", password);
                var statement = connection.createStatement()) {
            statement.execute("ALTER ROLE rag_runtime LOGIN PASSWORD '" + password + "'");
            statement.execute("ALTER ROLE rag_worker LOGIN PASSWORD '" + password + "'");
        }
        Path worker = REPO.resolve("rag-engine");
        Path venv = worker.resolve(".venv-d17");
        String home = Files.readAllLines(venv.resolve("pyvenv.cfg")).stream()
                .filter(line -> line.startsWith("home = ")).findFirst().orElseThrow().substring(7);
        properties.add("spring.datasource.url", RagQaProspectiveEvaluationNativeIntegrationTest::jdbcUrl);
        properties.add("spring.datasource.username", () -> "rag_runtime");
        properties.add("spring.datasource.password", () -> password);
        properties.add("spring.flyway.enabled", () -> "false");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        properties.add("ppv.security.jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
        properties.add("ppv.rag.storage.snapshot-root", () -> STATE.resolve("snapshots").toString());
        properties.add("ppv.rag.test-scoped-execution", () -> "true");
        properties.add("ppv.rag.qa.model-path", () -> worker.resolve(
                "models/qwen2.5-7b-instruct-q4_k_m-00001-of-00002.gguf").toString());
        properties.add("ppv.rag.worker.executable", () -> Path.of(home).resolve("python.exe").toString());
        properties.add("ppv.rag.worker.venv-launcher", () -> venv.resolve("Scripts/python.exe").toString());
        properties.add("ppv.rag.worker.working-directory", worker::toString);
        properties.add("ppv.rag.worker.dsn", () -> "postgresql://rag_worker:" + password
                + "@127.0.0.1:" + OWNER.path("port").asInt() + "/" + OWNER.path("database").asText());
        properties.add("ppv.rag.engine.service-key", () -> OWNER.path("service_key").asText());
        properties.add("ppv.rag.engine.url", () -> "http://127.0.0.1:" + OWNER.path("worker_port").asInt());
    }

    @Test
    void prepareCorpus() throws Exception {
        assertThat(Files.exists(RUN.resolve("source-freeze.json"))).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM rag.sources", Integer.class)).isZero();
        JsonNode manifest = read(FIXTURES.resolve("manifest.json"));
        var sources = new LinkedHashMap<String, RagSourceView>();
        var fields = manifest.path("files").fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            Path original = FIXTURES.resolve(entry.getKey()).normalize();
            assertThat(original.startsWith(FIXTURES)).isTrue();
            byte[] bytes = Files.readAllBytes(original);
            assertThat(hash(bytes)).isEqualTo(entry.getValue().path("sha256").asText());
            Path copy = RUN.resolve("inputs").resolve(entry.getKey());
            Files.createDirectories(copy.getParent());
            Files.write(copy, bytes, StandardOpenOption.CREATE_NEW);
            var candidate = rag.registerSnapshotCandidate(copy.toString());
            var approved = rag.approveSource(candidate.id(), new RagConsentCommand(Set.of("content"),
                    candidate.sourceVersion(), candidate.consentResetVersion(), candidate.authorizationEpoch(),
                    candidate.fieldPolicyVersion(), null, candidate.snapshot()));
            sources.put(entry.getKey(), approved);
        }
        for (var entry : sources.entrySet()) {
            for (var child : manifest.path("files").path(entry.getKey()).path("dependencies")) {
                rag.addDependency(entry.getValue().id(), sources.get(child.asText()).id());
            }
        }
        var roots = new ArrayList<RagDocumentRootItem>();
        var tasks = new ArrayList<RagDocumentBatchTask>();
        for (var entry : sources.entrySet()) {
            var source = rag.findSource(entry.getValue().id()).orElseThrow();
            entry.setValue(source);
            roots.add(new RagDocumentRootItem(entry.getKey(), source.sourceType(), source.sourceId(),
                    source.sourceVersion(), source.authorizationEpoch(), source.fieldPolicyVersion(), source.approvedFields()));
            tasks.add(new RagDocumentBatchTask(entry.getKey(), source.sourceId(), "MD", null, null, null,
                    List.of("content"), Map.of()));
        }
        var job = rag.admitDocumentBatch(new RagDocumentBatchCommand(roots, Map.of()));
        rag.executeDocumentBatch(job.jobId(), job.attemptId(), tasks);
        String dependencyManifest = jdbc.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, job.jobId());
        var publication = rag.finalizeDocumentBatch(job.jobId(), job.attemptId(),
                new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(dependencyManifest)));
        assertThat(publication.publishedCount()).isEqualTo(sources.size() - 1);
        captureSourceState(manifest, sources);
    }

    @Test
    void freezePreparedCorpus() throws Exception {
        // Preserve the already imported R/G/consent identities after the initial
        // preparation assertion failed. Never recreate approvals or improve evidence.
        assertThat(Files.exists(RUN.resolve("source-freeze.json"))).isFalse();
        JsonNode manifest = read(FIXTURES.resolve("manifest.json"));
        var sources = new LinkedHashMap<String, RagSourceView>();
        var roots = jdbc.queryForList("SELECT root_key,source_id FROM rag.batch_roots ORDER BY root_key");
        assertThat(roots).hasSize(manifest.path("files").size());
        for (var row : roots) {
            String key = (String) row.get("root_key");
            var source = rag.findSource(((Number) row.get("source_id")).longValue()).orElseThrow();
            assertThat(sources.put(key, source)).isNull();
            assertThat(hash(Files.readAllBytes(RUN.resolve("inputs").resolve(key))))
                    .isEqualTo(manifest.path("files").path(key).path("sha256").asText());
        }
        captureSourceState(manifest, sources);
    }

    private void captureSourceState(JsonNode manifest, Map<String, RagSourceView> sources) throws Exception {

        var actual = new LinkedHashMap<String, Object>();
        Map<Long, String> splitById = new HashMap<>();
        for (var entry : sources.entrySet()) {
            var source = rag.findSource(entry.getValue().id()).orElseThrow();
            assertThat(source.isEligible()).isTrue();
            String split = manifest.path("files").path(entry.getKey()).path("split").asText();
            splitById.put(source.id(), split);
            var generations = jdbc.queryForList("SELECT row_to_json(g)::text AS row FROM rag.index_generations g WHERE source_id=? AND is_active", source.id());
            boolean emptyDenied = "DENIED_EMPTY_CONTENT".equals(
                    manifest.path("files").path(entry.getKey()).path("expected_publication").asText());
            if (emptyDenied) {
                assertThat(source.snapshot().selectedRevisionId()).isNull();
                assertThat(generations).isEmpty();
                assertThat(jdbc.queryForObject("SELECT error_code FROM rag.file_outcomes WHERE root_key=?",
                        String.class, entry.getKey())).isEqualTo("EMPTY_CONTENT");
            } else {
                assertThat(source.snapshot().selectedRevisionId()).isNotNull();
                assertThat(generations).hasSize(1);
            }
            var chunks = jdbc.queryForList("""
                    SELECT c.chunk_id,c.content,c.locator::text,c.structure_metadata::text,
                           r.exact_dependency_manifest::text,g.id AS generation_row_id,g.generation_id
                    FROM rag.document_chunks c JOIN rag.generation_roots r ON r.generation_id=c.generation_id
                    JOIN rag.index_generations g ON g.id=r.generation_id WHERE g.source_id=? AND g.is_active
                    ORDER BY c.chunk_id
                    """, source.id());
            actual.put(entry.getKey(), Map.of("source", source, "generations", generations,
                    "chunks", chunks, "split", split));
        }
        var edges = jdbc.queryForList("SELECT parent_source_id,child_source_id FROM rag.dependencies ORDER BY parent_source_id,child_source_id");
        for (var edge : edges) {
            long parent = ((Number) edge.get("parent_source_id")).longValue();
            long child = ((Number) edge.get("child_source_id")).longValue();
            assertThat(splitById).containsKeys(parent, child);
            assertThat(splitById.get(parent)).isEqualTo(splitById.get(child));
        }
        var slotIds = new HashSet<String>();
        for (var slot : manifest.path("slots")) {
            assertThat(slotIds.add(slot.path("id").asText())).isTrue();
            for (var contributor : slot.path("contributors")) {
                var view = sources.get(contributor.asText());
                assertThat(view).isNotNull();
                assertThat(view.snapshot().selectedRevisionId()).isNotNull();
                assertThat(splitById.get(view.id())).isEqualTo(slot.path("split").asText());
            }
        }
        assertThat(slotIds).hasSize(24);
        var freeze = new LinkedHashMap<String, Object>();
        freeze.put("version", "S19-2-PROSPECTIVE-V2");
        freeze.put("created_utc", Instant.now());
        freeze.put("manifest_sha256", hash(Files.readAllBytes(FIXTURES.resolve("manifest.json"))));
        freeze.put("owner_id", OWNER.path("owner_id").asText());
        freeze.put("container_id", OWNER.path("container_id").asText());
        freeze.put("sources", actual);
        freeze.put("actual_dependency_edges", edges);
        freeze.put("source_group_disjoint", true);
        freeze.put("candidate_inference", "NOT_RUN; complete profile + independent freeze check required");
        Files.writeString(RUN.resolve("source-freeze.json"), JSON.writerWithDefaultPrettyPrinter().writeValueAsString(freeze),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        System.out.println("PROSPECTIVE_SOURCE_FREEZE: " + sources.size() + " actual sources; 24 scoped slots; no inference");
    }
}
