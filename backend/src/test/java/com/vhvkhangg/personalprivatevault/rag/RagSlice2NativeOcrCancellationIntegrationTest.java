package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;

/** Real admission, grant, bound HTTP, supervisor and native ONNX call; no finalization/publication. */
@Timeout(90)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RagSlice2NativeOcrCancellationIntegrationTest extends AbstractEnabledRagIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    @MockitoSpyBean private RagWorkerSupervisor supervisor;
    private final Map<UUID, NativeWorkerProcess.ResourceUsage> usage = new ConcurrentHashMap<>();

    @BeforeEach
    void useTestOnlyWorkloadDirectory() {
        // The inherited DynamicPropertySource also registers this property; explicitly bind
        // only this disposable context to the fixture. Production launch/stop code stays real.
        ReflectionTestUtils.setField(supervisor, "directory", fixture().toString());
        assertThat(ReflectionTestUtils.getField(supervisor, "directory")).isEqualTo(fixture().toString());
        doAnswer(call -> {
            UUID job = call.getArgument(0);
            usage.putIfAbsent(job, supervisor.stoppedResources(job));
            return call.callRealMethod();
        }).when(supervisor).forget(org.mockito.ArgumentMatchers.any(UUID.class));
    }

    private static Path fixture() {
        return repository().resolve("backend/src/test/resources/rag/native-ocr-worker");
    }

    @DynamicPropertySource
    static void enableIsolatedWorker(DynamicPropertyRegistry properties) {
        AbstractEnabledRagIntegrationTest.enableIsolatedWorker(properties);
        properties.add("ppv.rag.worker.working-directory", () -> fixture().toString());
        properties.add("ppv.rag.engine.timeout-seconds", () -> "10");
    }

    @Test
    void cancellationDuringNativeOcrTerminatesOwnedTreeAndPurgesWithoutStaging() throws Exception {
        runNativeBoundary(true);
    }

    @Test
    void requestDeadlineDuringNativeOcrAbortsAndTerminatesWithoutStaging() throws Exception {
        runNativeBoundary(false);
    }

    private void runNativeBoundary(boolean cancel) throws Exception {
        var source = approvedSource();
        var admitted = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(
                new RagDocumentRootItem("root", source.sourceType(), source.sourceId(), source.sourceVersion(),
                        source.authorizationEpoch(), source.fieldPolicyVersion(), source.approvedFields())), Map.of()));
        var task = new RagDocumentBatchTask("root", source.sourceId(), "DOMAIN", null, null, null,
                source.approvedFields().stream().sorted().toList(), Map.of());
        Path events = fixture().resolve("native-" + admitted.jobId() + ".jsonl");
        var executor = Executors.newSingleThreadExecutor();
        Future<?> execution = executor.submit(() -> enabledRag.executeDocumentBatch(
                admitted.jobId(), admitted.attemptId(), List.of(task)));
        long started = System.nanoTime();
        try {
            await().atMost(Duration.ofSeconds(9)).pollInterval(Duration.ofMillis(2)).until(() -> {
                if (execution.isDone()) {
                    execution.get();
                    throw new IllegalStateException("Native workload was not entered before batch returned");
                }
                var entries = readEvents(events);
                return !entries.isEmpty() && entries.getLast().path("event").asText().equals("native_enter");
            });
            Map<String, Object> worker = jdbcTemplate.queryForMap(
                    "SELECT worker_identity FROM rag.job_leases WHERE job_id=?", admitted.jobId());
            JsonNode identity = JSON.readTree(worker.get("worker_identity").toString());
            long pid = identity.path("process_id").asLong();
            long creation = Long.parseLong(identity.path("creation_file_time").asText());
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(pid, creation)).isFalse();
            long action = System.nanoTime();
            if (cancel) enabledRag.cancelJob(admitted.jobId());
            assertThatThrownBy(() -> execution.get(20, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class);
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(pid, creation, Duration.ofSeconds(2))).isTrue();
            List<JsonNode> entries = readEvents(events);
            assertThat(entries.getLast().path("event").asText())
                    .as("native entered but did not return before owned-process termination")
                    .isEqualTo("native_enter");
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=?", Integer.class, admitted.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks", Integer.class)).isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT staging_closed FROM rag.job_leases WHERE job_id=?", Boolean.class, admitted.jobId())).isTrue();
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM rag.job_leases WHERE job_id=?", String.class, admitted.jobId());
            if (cancel) assertThat(status).isIn("CANCELLED", "ABORTED");
            else assertThat(status).isEqualTo("ABORTED");
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL", Integer.class)).isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM rag.worker_grants WHERE job_id=?", Integer.class, admitted.jobId())).isZero();
            var resources = usage.get(admitted.jobId());
            assertThat(resources).isNotNull();
            assertThat(resources.peakJobMemoryBytes()).isBetween(1L, 2048L * 1024 * 1024);
            assertThat(resources.totalUserCpu100ns() + resources.totalKernelCpu100ns()).isPositive();
            Path scratch = fixture().resolve("scratch-" + admitted.jobId());
            try (var files = Files.list(scratch)) { assertThat(files.toList()).isEmpty(); }
            Files.delete(scratch);
            Path receipt = repository().resolve("logs/phase18/s2-blocker-checkpoint-2026-10-20/native-"
                    + (cancel ? "cancel" : "deadline") + ".json");
            Files.createDirectories(receipt.getParent());
            JSON.writerWithDefaultPrettyPrinter().writeValue(receipt.toFile(), Map.of(
                    "trigger", cancel ? "OWNER_CANCEL" : "EXISTING_HTTP_REQUEST_DEADLINE_10_SECONDS",
                    "status", status, "worker_pid", pid, "owned_tree_terminated", true,
                    "staged_outputs", 0, "staging_closed", true,
                    "elapsed_seconds", (System.nanoTime() - started) / 1e9,
                    "action_to_terminal_seconds", (System.nanoTime() - action) / 1e9,
                    "windows_job_resource_counters", resources,
                    "native_events", entries));
        } finally {
            if (!execution.isDone()) {
                try { enabledRag.cancelJob(admitted.jobId()); } catch (RuntimeException ignored) { }
            }
            executor.shutdownNow();
            assertThat(executor.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
            // Only test instrumentation is removed; no original file or production snapshot is opened.
            Files.deleteIfExists(events);
        }
    }

    private List<JsonNode> readEvents(Path path) {
        if (!Files.exists(path)) return List.of();
        try {
            var result = new ArrayList<JsonNode>();
            for (String line : Files.readAllLines(path)) {
                try { result.add(JSON.readTree(line)); } catch (Exception partialConcurrentLine) { break; }
            }
            return result;
        } catch (Exception concurrentWrite) { return List.of(); }
    }

    private RagSourceView approvedSource() {
        Long genre = jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES (?) RETURNING id",
                Long.class, UUID.randomUUID().toString());
        Long person = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Public Test Author')", person);
        Long id = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,is_nsfw) VALUES (?,'Public native lifecycle fixture','NOVEL',?,?,false)",
                id, genre, person);
        var source = enabledRag.registerDomainCandidate(RagSourceType.FICTION, id.toString(), RagClassification.NON_PRIVATE, null, null);
        return enabledRag.approveSource(source.id(), new RagConsentCommand(Set.of("title"), source.sourceVersion(),
                source.consentResetVersion(), source.authorizationEpoch(), source.fieldPolicyVersion(), null));
    }
}
