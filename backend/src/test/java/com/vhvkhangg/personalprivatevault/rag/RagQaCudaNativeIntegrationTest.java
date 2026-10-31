package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; no CUDA QA runs authorized.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 * Historical opt-in CUDA harness over a disposable corpus unrelated to frozen semantic slots.
 */
@EnabledIfSystemProperty(named = "ppv.rag.qa.cuda.launcher", matches = ".+")
class RagQaCudaNativeIntegrationTest extends RagQaNativeIntegrationTest {
    @MockitoSpyBean private RagQaSupervisor cudaSupervisor;

    @DynamicPropertySource
    static void cudaRuntime(org.springframework.test.context.DynamicPropertyRegistry properties) {
        Path repo = Files.isDirectory(Path.of("rag-engine")) ? Path.of(".") : Path.of("..");
        properties.add("ppv.rag.qa.cuda-test-enabled", () -> "true");
        properties.add("ppv.rag.qa.cuda-test-launcher", () -> System.getProperty("ppv.rag.qa.cuda.launcher"));
        properties.add("ppv.rag.qa.model-path", () -> repo.toAbsolutePath().normalize().resolve(
                "rag-engine/models/qwen2.5-7b-instruct-q4_k_m-00001-of-00002.gguf").toString());
    }

    static long availableMemory() {
        try (var arena = java.lang.foreign.Arena.ofConfined()) {
            var call = java.lang.foreign.Linker.nativeLinker().downcallHandle(
                    java.lang.foreign.SymbolLookup.libraryLookup("kernel32", arena).find("GlobalMemoryStatusEx").orElseThrow(),
                    java.lang.foreign.FunctionDescriptor.of(java.lang.foreign.ValueLayout.JAVA_INT, java.lang.foreign.ValueLayout.ADDRESS));
            var value = arena.allocate(64, 8); value.set(java.lang.foreign.ValueLayout.JAVA_INT, 0, 64);
            if ((int) call.invokeExact(value) == 0) throw new IllegalStateException("Headroom unavailable");
            return value.get(java.lang.foreign.ValueLayout.JAVA_LONG, 16);
        } catch (Throwable failure) { throw new IllegalStateException("Headroom unavailable", failure); }
    }

    @Test @Override
    void realMarkdownCorpusNativeQaFlow() throws Exception {
        Path run = Path.of(System.getProperty("ppv.rag.qa.cuda.evidence")).toAbsolutePath();
        var json = new com.fasterxml.jackson.databind.ObjectMapper();
        var id = new AtomicReference<UUID>(); var done = new AtomicBoolean();
        var min = new AtomicLong(Long.MAX_VALUE); var resourceStop = new AtomicBoolean();
        var preflight = new AtomicLong(); var samplerFailure = new AtomicReference<Throwable>();
        var usage = new AtomicReference<Object>(); var measures = new AtomicReference<Object>();
        var memory = Collections.synchronizedList(new ArrayList<Map<String, Long>>());
        doAnswer(call -> {
            // Test-owned fixture setup precedes QA; reclaim its transient heap before measuring admission.
            System.gc();
            long available = availableMemory(); preflight.set(available);
            if (available <= 3L * 1024 * 1024 * 1024) {
                throw new com.vhvkhangg.personalprivatevault.rag.api.RagQueryUnavailableException(
                        "App-ready QA startup margin unavailable");
            }
            id.set(call.getArgument(0));
            Object session = call.callRealMethod();
            var owned = (RagQaSupervisor.QaSession) session;
            Files.writeString(run.resolve("qa-worker-identity.json"), json.writeValueAsString(Map.of(
                    "reader_id", id.get(), "pid", owned.process().pid(), "creation_filetime", owned.process().creationTimeFileTime())), StandardOpenOption.CREATE_NEW);
            return session;
        }).when(cudaSupervisor).startSession(any());
        doAnswer(call -> {
            Object result = call.callRealMethod();
            var recorded = cudaSupervisor.resourceUsage(call.getArgument(0));
            if (recorded.peakJobMemoryBytes() > 0) usage.set(recorded);
            measures.set(cudaSupervisor.generationMeasurements(call.getArgument(0)));
            return result;
        }).when(cudaSupervisor).stopWorker(any(), anyLong());
        Thread sampler = Thread.ofPlatform().start(() -> {
            try { while (!done.get()) {
                if (id.get() != null) {
                    long available = availableMemory(); min.accumulateAndGet(available, Math::min);
                    memory.add(Map.of("monotonic_nanos", System.nanoTime(), "available_bytes", available));
                    if (available < 2L * 1024 * 1024 * 1024 && resourceStop.compareAndSet(false, true)) {
                        cudaSupervisor.stopWorker(id.get(), System.nanoTime() + Duration.ofSeconds(5).toNanos());
                    }
                }
                try { Thread.sleep(50); } catch (InterruptedException stop) { return; }
            } } catch (Throwable failure) { samplerFailure.set(failure); }
        });
        boolean httpAssertionsPassed = false;
        long started = System.nanoTime();
        try { super.realMarkdownCorpusNativeQaFlow(); httpAssertionsPassed = true; }
        finally {
            done.set(true); sampler.join(2000);
            var record = new LinkedHashMap<String, Object>();
            record.put("http_assertions_passed", httpAssertionsPassed); record.put("qa_reader_id", id.get());
            record.put("app_ready_preflight_available_bytes", preflight.get());
            record.put("sampler_failure", samplerFailure.get() == null ? null : samplerFailure.get().getClass().getSimpleName());
            record.put("minimum_available_ram_bytes", min.get()); record.put("memory_samples", memory);
            record.put("resource_stop_requested", resourceStop.get()); record.put("post_stop_native_usage", usage.get());
            record.put("generation_measurements", measures.get()); record.put("arrival_to_assertions_nanos", System.nanoTime() - started);
            Files.writeString(run.resolve("native-qa-smoke.json"), json.writeValueAsString(record), StandardOpenOption.CREATE_NEW);
        }
        assertThat(min.get()).isGreaterThanOrEqualTo(2L * 1024 * 1024 * 1024);
        assertThat(memory).isNotEmpty(); assertThat(samplerFailure.get()).isNull();
        assertThat(usage.get()).as("post-stop native measurement").isNotNull();
    }
}
