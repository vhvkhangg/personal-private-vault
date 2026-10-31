package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Actual native generation followed by the existing PostgreSQL/Servlet safety boundaries.
 * Spies pause or fault individual boundaries; embedding, generation, source authority and HTTP are real.
 * These disposable fixtures are unrelated to the frozen semantic evaluation corpus.
 */
@SpringBootTest(classes = PersonalPrivateVaultApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RagQaNativeSafetyIntegrationTest.OutputConfiguration.class)
@EnabledIfSystemProperty(named = "ppv.rag.qa.cuda.launcher", matches = ".+")
class RagQaNativeSafetyIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @MockitoSpyBean private RagQaSupervisor supervisor;
    @MockitoSpyBean private RagRetrievalAuthority authority;
    @Autowired private RagQaService qa;
    @Autowired private RagRecoveryService recovery;
    @Autowired private RagGenerationState generations;
    @Autowired private RagRuntimeIdentity runtime;
    @LocalServerPort private int port;
    @TempDir private Path input;

    private final ObjectMapper json = new ObjectMapper();
    private final AtomicReference<UUID> reader = new AtomicReference<>();
    private final AtomicReference<RagQaSupervisor.QaSession> session = new AtomicReference<>();
    private final AtomicReference<Object> usage = new AtomicReference<>();
    private final AtomicReference<Object> measurements = new AtomicReference<>();
    private final AtomicBoolean samplingDone = new AtomicBoolean();
    private final AtomicBoolean resourceStopped = new AtomicBoolean();
    private final AtomicReference<Throwable> samplerFailure = new AtomicReference<>();
    private final AtomicLong minimumRam = new AtomicLong(Long.MAX_VALUE);
    private final List<Map<String, Long>> memory = Collections.synchronizedList(new ArrayList<>());
    private final List<CountDownLatch> releases = new ArrayList<>();
    private final List<Runnable> faultRestorations = new ArrayList<>();
    private final Map<String, Object> observations = new LinkedHashMap<>();
    private Thread sampler;
    private String method;
    private NativeWorkerProcess faultedProcess;

    @DynamicPropertySource
    static void enableNative(DynamicPropertyRegistry properties) {
        RagQaNativeIntegrationTest.enableNativeQa(properties);
        RagQaCudaNativeIntegrationTest.cudaRuntime(properties);
    }

    @BeforeEach
    void monitorOwnedRequest(TestInfo info) {
        method = info.getTestMethod().orElseThrow().getName();
        supervisor.setVectorOverride(null);
        supervisor.setGenerationOverride(null);
        doAnswer(call -> {
            System.gc();
            long available = RagQaCudaNativeIntegrationTest.availableMemory();
            observations.put("app_ready_available_bytes", available);
            if (available <= 3L * 1024 * 1024 * 1024) throw new RagQueryUnavailableException("QA startup margin unavailable");
            assertThat(reader.compareAndSet(null, call.getArgument(0))).as("one native reader per test").isTrue();
            var owned = (RagQaSupervisor.QaSession) call.callRealMethod();
            session.set(owned);
            observations.put("worker", Map.of("pid", owned.process().pid(), "creation_filetime", owned.process().creationTimeFileTime()));
            return owned;
        }).when(supervisor).startSession(any());
        doAnswer(call -> {
            Object result = call.callRealMethod();
            var recorded = supervisor.resourceUsage(call.getArgument(0));
            if (recorded.peakJobMemoryBytes() > 0) usage.set(recorded);
            var metrics = supervisor.generationMeasurements(call.getArgument(0));
            if (!metrics.isEmpty()) measurements.set(metrics);
            return result;
        }).when(supervisor).stopWorker(any(), anyLong());
        sampler = Thread.ofPlatform().start(() -> {
            try {
                while (!samplingDone.get()) {
                    if (reader.get() != null) {
                        long available = RagQaCudaNativeIntegrationTest.availableMemory();
                        minimumRam.accumulateAndGet(available, Math::min);
                        memory.add(Map.of("nanos", System.nanoTime(), "available_bytes", available));
                        if (available < 2L * 1024 * 1024 * 1024 && resourceStopped.compareAndSet(false, true)) {
                            supervisor.stopWorker(reader.get(), System.nanoTime() + Duration.ofSeconds(5).toNanos());
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException stopped) {
                Thread.currentThread().interrupt();
            } catch (Throwable failed) {
                samplerFailure.set(failed);
            }
        });
    }

    @AfterEach
    void finishOwnedEvidence() throws Exception {
        if (sampler == null) return; // A failed superclass fixture gate has already failed the test.
        releases.forEach(CountDownLatch::countDown);
        faultRestorations.forEach(Runnable::run);
        output.set(null);
        if (faultedProcess != null) doCallRealMethod().when(faultedProcess).terminateAndVerify(any());
        try {
            if (reader.get() != null) {
                if (supervisor.hasActiveProcess(reader.get()) || qa.hasSender(reader.get())) supervisor.stopWorker(reader.get());
                await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> {
                    qa.reconcileCompletedSenders();
                    assertThat(state(reader.get())).isEqualTo("TERMINAL");
                    assertThat(qa.hasSender(reader.get())).isFalse();
                });
                if (session.get() != null) {
                    assertThat(NativeWorkerProcess.isRecordedProcessTerminated(session.get().process().pid(),
                            session.get().process().creationTimeFileTime(), Duration.ZERO)).isTrue();
                    observations.put("recorded_worker_terminated", true);
                    assertThat(usage.get()).as("positive native stop accounting").isNotNull();
                } else {
                    assertThat(supervisor.hasActiveProcess(reader.get())).isFalse();
                    observations.put("native_worker_started", false);
                }
                supervisor.forgetStopped(reader.get());
            }
        } finally {
            samplingDone.set(true);
            sampler.join(2000);
            observations.put("reader_id", reader.get());
            observations.put("minimum_available_ram_bytes", minimumRam.get());
            observations.put("resource_stop_requested", resourceStopped.get());
            observations.put("sampler_failure", samplerFailure.get() == null ? null : samplerFailure.get().getClass().getSimpleName());
            observations.put("native_usage", usage.get());
            observations.put("generation_measurements", measurements.get());
            observations.put("memory_samples", memory);
            Path evidence = Path.of(System.getProperty("ppv.rag.qa.cuda.evidence"));
            Files.writeString(evidence.resolve(method + ".json"), json.writeValueAsString(observations), StandardOpenOption.CREATE_NEW);
        }
        assertThat(memory).isNotEmpty();
        assertThat(minimumRam.get()).isGreaterThanOrEqualTo(2L * 1024 * 1024 * 1024);
        assertThat(samplerFailure.get()).isNull();
        assertThat(resourceStopped.get()).isFalse();
    }

    @Test
    void realGenerationReservesTrueChildAndCancellationStopsTheSameChild() throws Exception {
        var fixture = fixture();
        var pause = pauseAfterRealGeneration();
        UUID id = UUID.randomUUID();
        var response = send(command(id, fixture), false);
        assertThat(pause.entered().await(90, TimeUnit.SECONDS)).isTrue();
        assertReserved(fixture.parent(), true);
        assertReserved(fixture.child(), true);
        assertReserved(fixture.sibling(), false);
        assertBusy(() -> enabledRag.revokeSource(fixture.child().id()));
        assertBusy(() -> enabledRag.capturePendingRevision(fixture.child().id(),
                new RagSnapshotCaptureCommand(input.resolve("Child.md").toString(), fixture.child().snapshot())));
        var child = enabledRag.findSource(fixture.child().id()).orElseThrow();
        assertBusy(() -> enabledRag.approveSource(child.id(), new RagConsentCommand(Set.of("content"), child.sourceVersion(),
                child.consentResetVersion(), child.authorizationEpoch(), child.fieldPolicyVersion(), null, child.snapshot())));
        assertBusy(() -> enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(root("busy", child)), Map.of())));
        assertIndependentConnectionCanLock(fixture.child());
        assertCompetingRequestsBusy(fixture);
        enabledRag.revokeSource(fixture.sibling().id());
        assertThat(requestQaCancelOverHttp(id).statusCode()).isEqualTo(200);
        pause.release().countDown();
        assertThat(response.get(10, TimeUnit.SECONDS).statusCode()).isEqualTo(409);
        awaitTerminal(id);
        assertReserved(fixture.parent(), false);
        assertReserved(fixture.child(), false);
        verifyOneNativeBindAndGeneration(id);
        observations.put("proof", "real generation completed; same child remained owned while normal mutations/competing readers were denied; cancel positively stopped it and released true-child closure");
    }

    @Test
    void realChildIncidentBeforeEmissionDeniesEveryAnswerByte() throws Exception {
        var fixture = fixture();
        var pause = pauseAfterRealGeneration();
        UUID id = UUID.randomUUID();
        var response = send(command(id, fixture), false);
        assertThat(pause.entered().await(90, TimeUnit.SECONDS)).isTrue();
        UUID inspection = incident(fixture.child());
        pause.release().countDown();
        var actual = response.get(12, TimeUnit.SECONDS);
        assertThat(actual.statusCode()).isEqualTo(503);
        assertThat(json.readTree(actual.body()).path("data").isNull()).isTrue();
        assertThat(actual.body()).doesNotContain("garden level", "amber room", "citationKey");
        awaitTerminal(id);
        assertThat(failed(fixture.child())).isTrue();
        assertThat(inspectionState(inspection)).isEqualTo("DENIED");
        assertThat(generationState(fixture.sibling())).isEqualTo("CURRENT");
        verifyOneNativeBindAndGeneration(id);
        observations.put("proof", "real native generation -> trusted true-child incident first -> actual C_E denied -> error-only HTTP -> guarded finish; sibling remains CURRENT");
    }

    @Test
    void emittingFirstChildIncidentRetainsStalledServletOutputUntilOwnedFinish() throws Exception {
        var fixture = fixture();
        var gate = new OutputGate(new CountDownLatch(1), release());
        output.set(gate);
        UUID id = UUID.randomUUID();
        var response = send(command(id, fixture), true);
        assertThat(gate.entered().await(90, TimeUnit.SECONDS)).isTrue();
        assertThat(state(id)).isEqualTo("EMITTING");
        UUID inspection = incident(fixture.child());
        assertThat(inspectionState(inspection)).isEqualTo("INCIDENT_PENDING");
        assertThat(failed(fixture.child())).isFalse();
        assertReserved(fixture.child(), true);
        assertIndependentConnectionCanLock(fixture.child());
        assertBusy(() -> enabledRag.revokeSource(fixture.child().id()));
        assertCompetingRequestsBusy(fixture);
        assertThat(response.isDone()).isFalse();
        gate.release().countDown();
        var actual = response.get(12, TimeUnit.SECONDS);
        assertThat(actual.statusCode()).isEqualTo(200);
        assertThat(json.readTree(actual.body()).path("data").path("claims").isEmpty()).isFalse();
        awaitTerminal(id);
        assertThat(jdbcTemplate.queryForObject("SELECT finish_disposition FROM rag.retrieval_readers WHERE reader_id=?", String.class, id)).isEqualTo("INCIDENT_DENIED");
        assertThat(failed(fixture.child())).isTrue();
        assertThat(inspectionState(inspection)).isEqualTo("DENIED");
        assertReserved(fixture.child(), false);
        assertThat(generationState(fixture.sibling())).isEqualTo("CURRENT");
        gate.listener().get().onWritePossible();
        assertThat(state(id)).isEqualTo("TERMINAL");
        verifyOneNativeBindAndGeneration(id);
        observations.put("proof", "native generation -> actual C_E -> stalled real Servlet write -> child incident pending with retained exclusion -> owned completion/C_D -> denial; late callback does not replay");
    }

    @Test
    void unknownFinishBeforeCommitRetainsExclusionUntilOwnedReconciliation() throws Exception {
        finishFailure(false);
    }

    @Test
    void lostFinishAcknowledgementAfterRealCommitCannotReplayOrOpenAnOwnerGap() throws Exception {
        finishFailure(true);
    }

    private void finishFailure(boolean afterCommit) throws Exception {
        var fixture = fixture();
        UUID id = UUID.randomUUID();
        CountDownLatch faultObserved = new CountDownLatch(1);
        var inject = new AtomicBoolean(true);
        faultRestorations.add(() -> inject.set(false));
        doAnswer(call -> {
            if (inject.get() && id.equals(call.getArgument(0))) {
                if (!afterCommit) {
                    faultObserved.countDown();
                    throw new IllegalStateException("Synthetic pre-commit acknowledgement uncertainty");
                }
                assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCommit() {
                        faultObserved.countDown();
                        throw new IllegalStateException("Synthetic lost acknowledgement after durable commit");
                    }
                });
            }
            return call.callRealMethod();
        }).when(authority).finish(any(), any(), anyString(), anyString(), anyMap(), any(), any());
        var response = send(command(id, fixture), false);
        assertThat(response.get(90, TimeUnit.SECONDS).statusCode()).isEqualTo(200);
        assertThat(faultObserved.await(8, TimeUnit.SECONDS)).isTrue();
        await().atMost(Duration.ofSeconds(7)).untilAsserted(() -> assertThat(qa.isQuiescentReader(id)).isTrue());
        assertThat(state(id)).isEqualTo(afterCommit ? "TERMINAL" : "EMITTING");
        assertThat(qa.hasSender(id)).isTrue();
        assertCompetingRequestsBusy(fixture);
        if (!afterCommit) {
            assertReserved(fixture.child(), true);
            assertThatThrownBy(() -> authority.admitEmpty(UUID.randomUUID(), UUID.randomUUID(), runtime.instance().toString()))
                    .hasMessageContaining("RAG_READER_BUSY");
        }
        inject.set(false);
        qa.reconcileCompletedSenders();
        awaitTerminal(id);
        assertThat(qa.hasSender(id)).isFalse();
        assertReserved(fixture.child(), false);
        verifyOneNativeBindAndGeneration(id);
        observations.put("proof", afterCommit ? "real commit succeeded, afterCommit acknowledgement fault retained Java owner; competing HTTP readers denied; reconciliation observed TERMINAL without native/answer replay" : "failed/unknown finish retained SQL and Java exclusion; independent SQL/HTTP admission denied; owned reconciliation alone released it");
    }

    @Test
    void missingConfiguredModelDeniesBeforeAnyNativeWorkerBind() throws Exception {
        modelAssetRefusal(true);
    }

    @Test
    void corruptedConfiguredModelDeniesInTheActualWorkerWithoutFallback() throws Exception {
        modelAssetRefusal(false);
    }

    private void modelAssetRefusal(boolean missing) throws Exception {
        var fixture = fixture();
        Path target = input.resolve("qwen2.5-7b-instruct-q4_k_m-00001-of-00002.gguf");
        if (!missing) Files.writeString(target, "Synthetic invalid model bytes, never loadable.");
        Object original = org.springframework.test.util.ReflectionTestUtils.getField(supervisor, "modelPath");
        faultRestorations.add(() -> org.springframework.test.util.ReflectionTestUtils.setField(supervisor, "modelPath", original));
        org.springframework.test.util.ReflectionTestUtils.setField(supervisor, "modelPath", target.toString());
        UUID id = UUID.randomUUID();
        var response = send(command(id, fixture), false).get(90, TimeUnit.SECONDS);
        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(json.readTree(response.body()).path("data").isNull()).isTrue();
        assertThat(response.body()).doesNotContain(target.toString(), "garden level", "amber room");
        awaitTerminal(id);
        if (missing) {
            verify(authority, never()).bindWorker(any(), anyString(), anyLong(), any(), anyLong());
            assertThat(session.get()).isNull();
        } else {
            verifyOneNativeBindAndGeneration(id);
            assertThat(measurements.get()).isNull();
        }
        observations.put("proof", missing ? "explicit missing configured C2 asset refused before native launch/bind; no basename fallback" : "actual query/native worker rejected corrupt configured C2 bytes before successful model generation; sanitized HTTP503 and positive stop; original valid model untouched");
    }

    @Test
    void missingPinnedDllInIsolatedRuntimeDeniesWithoutCpuOrRuntimeFallback() throws Exception {
        var fixture = fixture();
        Path launcher = Path.of(System.getProperty("ppv.rag.qa.cuda.missing-dll-launcher")).toAbsolutePath().normalize();
        assertThat(launcher.startsWith(repository().resolve("logs/phase-19/s19-2"))).isTrue();
        assertThat(launcher).isRegularFile();
        assertThat(launcher.getParent().getParent().resolve("Lib/site-packages/llama_cpp/lib/llama.dll")).doesNotExist();
        Object original = org.springframework.test.util.ReflectionTestUtils.getField(supervisor, "cudaTestLauncher");
        faultRestorations.add(() -> org.springframework.test.util.ReflectionTestUtils.setField(supervisor, "cudaTestLauncher", original));
        org.springframework.test.util.ReflectionTestUtils.setField(supervisor, "cudaTestLauncher", launcher.toString());
        UUID id = UUID.randomUUID();
        var response = send(command(id, fixture), false).get(90, TimeUnit.SECONDS);
        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(json.readTree(response.body()).path("data").isNull()).isTrue();
        assertThat(response.body()).doesNotContain("garden level", "amber room", "llama.dll", launcher.toString());
        awaitTerminal(id);
        verifyOneNativeBindAndGeneration(id);
        assertThat(measurements.get()).isNull();
        observations.put("proof", "actual worker/query in byte-copied isolated runtime missing exactly pinned llama.dll; generation runtime guard denied, sanitized HTTP503, no CPU/runtime/model fallback, positive same-child stop and SQL finish");
    }

    @Test
    void actualNativeProtocolRefusesWrongIdentityDuplicateFieldsAndReplay() throws Exception {
        UUID id = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        authority.admitEmpty(id, token, runtime.instance().toString());
        var owned = supervisor.startSession(id);
        var body = new LinkedHashMap<String, Object>();
        body.put("reader_id", id.toString());
        body.put("nonce", owned.nonce().toString());
        body.put("question", "What is supported by the provided evidence?");
        body.put("evidence", List.of());
        body.put("history", List.of());
        body.put("response_language", "en");
        body.put("scope_digest", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        body.put("reader_id", UUID.randomUUID().toString());
        assertThat(workerRequest(owned, json.writeValueAsString(body)).statusCode()).isEqualTo(403);
        body.put("reader_id", id.toString());
        body.put("nonce", UUID.randomUUID().toString());
        assertThat(workerRequest(owned, json.writeValueAsString(body)).statusCode()).isEqualTo(403);
        body.put("nonce", owned.nonce().toString());
        body.put("unexpected", true);
        assertThat(workerRequest(owned, json.writeValueAsString(body)).statusCode()).isEqualTo(400);
        body.remove("unexpected");
        String valid = json.writeValueAsString(body);
        String duplicate = valid.substring(0, valid.length() - 1) + ",\"question\":\"duplicate\"}";
        assertThat(workerRequest(owned, duplicate).statusCode()).isEqualTo(400);
        body.put("scope_digest", "0".repeat(64));
        assertThat(workerRequest(owned, json.writeValueAsString(body)).statusCode()).isEqualTo(400);
        var result = workerRequest(owned, valid);
        assertThat(result.statusCode()).isEqualTo(200);
        var actual = json.readTree(result.body());
        assertThat(actual.path("outcome").asText()).isEqualTo("ABSTAIN");
        assertThat(actual.path("claims").isEmpty()).isTrue();
        assertThat(actual.path("process_id").asLong()).isEqualTo(owned.process().pid());
        assertThat(actual.path("profile").asText()).isEqualTo("C2_OFFLINE_CUDA_TEST_V1");
        assertThat(workerRequest(owned, valid).statusCode()).isEqualTo(409);
        supervisor.stopWorker(id);
        authority.finish(id, token, "CANCELLED", "ABORTED", Map.of("reason", "STOPPED_SYNTHETIC_NATIVE_PROTOCOL_PROBE",
                "native_quiescent", true, "transport_quiescent", true, "sender_quiescent", true,
                "context_complete", true, "buffers_discarded", true), null, null);
        verify(authority, times(1)).bindWorker(eq(id), anyString(), eq(owned.process().pid()), any(), eq(owned.process().creationTimeFileTime()));
        observations.put("proof", "fresh real offline native worker: wrong reader/nonce403; unknown/duplicate fields and scope400; valid empty-evidence ABSTAIN200 without model inference; replay409; positive same-child/transport stop and owned SQL finish");
    }

    private HttpResponse<String> workerRequest(RagQaSupervisor.QaSession owned, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + owned.port() + "/v1/qa/generate"))
                .header("X-PPV-Service-Key", owned.serviceKey()).header("X-PPV-Reader-Id", owned.readerId().toString())
                .header("X-PPV-Nonce", owned.nonce().toString()).header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return owned.transport().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void falseStopOfRealNativeChildRetainsReaderAndIncidentUntilPositiveRecovery() throws Exception {
        var fixture = fixture();
        var pause = pauseAfterRealGeneration();
        UUID id = UUID.randomUUID();
        var response = send(command(id, fixture), false);
        assertThat(pause.entered().await(90, TimeUnit.SECONDS)).isTrue();
        var original = session.get().process();
        faultedProcess = spy(original);
        var field = RagQaSupervisor.class.getDeclaredField("activeProcesses");
        field.setAccessible(true);
        var processes = (Map<UUID, NativeWorkerProcess>) field.get(supervisor);
        assertThat(processes.replace(id, original, faultedProcess)).isTrue();
        doReturn(false).when(faultedProcess).terminateAndVerify(any());
        pause.release().countDown();
        assertThat(response.get(12, TimeUnit.SECONDS).statusCode()).isEqualTo(503);
        await().atMost(Duration.ofSeconds(7)).untilAsserted(() -> assertThat(qa.isQuiescentReader(id)).isTrue());
        UUID inspection = incident(fixture.child());
        recovery.reconcile();
        assertThat(state(id)).isNotEqualTo("TERMINAL");
        assertThat(supervisor.hasActiveProcess(id)).isTrue();
        assertThat(faultedProcess.hasTerminatedTree()).isFalse();
        assertReserved(fixture.child(), true);
        assertThat(inspectionState(inspection)).isEqualTo("INCIDENT_PENDING");
        assertCompetingRequestsBusy(fixture);
        doCallRealMethod().when(faultedProcess).terminateAndVerify(any());
        supervisor.stopWorker(id);
        qa.reconcileCompletedSenders();
        recovery.reconcile();
        awaitTerminal(id);
        assertReserved(fixture.child(), false);
        assertThat(inspectionState(inspection)).isEqualTo("DENIED");
        assertThat(generationState(fixture.sibling())).isEqualTo("CURRENT");
        verifyOneNativeBindAndGeneration(id);
        observations.put("proof", "same real generated native child reported false stop; SQL reservation/incident retained through reconciler; only positive owned stop then finish allowed release");
    }

    private record Fixture(RagSourceView parent, RagSourceView child, RagSourceView sibling) {}
    private record Pause(CountDownLatch entered, CountDownLatch release) {}

    private Fixture fixture() throws Exception {
        var child = approve("Child.md", "# Child\n\nThe child archive has an amber room.\n");
        var parent = approve("Parent.md", "# Archive\n\nThe archive is on the garden level.\n\n![[Child]]\n");
        var sibling = approve("Sibling.md", "# Independent\n\nThe independent museum opens on Friday.\n");
        enabledRag.addDependency(parent.id(), child.id());
        parent = enabledRag.findSource(parent.id()).orElseThrow();
        List<RagSourceView> views = List.of(parent, sibling);
        var roots = new ArrayList<RagDocumentRootItem>();
        var tasks = new ArrayList<RagDocumentBatchTask>();
        for (int i = 0; i < views.size(); i++) {
            var v = views.get(i);
            String key = "safety-" + i;
            roots.add(root(key, v));
            tasks.add(new RagDocumentBatchTask(key, v.sourceId(), "MD", null, null, null, List.of("content"), Map.of()));
        }
        var job = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots, Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(), job.attemptId(), tasks);
        String manifest = jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, job.jobId());
        assertThat(manifest).contains(child.sourceId());
        assertThat(enabledRag.finalizeDocumentBatch(job.jobId(), job.attemptId(), new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest))).publishedCount()).isEqualTo(2);
        return new Fixture(enabledRag.findSource(parent.id()).orElseThrow(), enabledRag.findSource(child.id()).orElseThrow(), enabledRag.findSource(sibling.id()).orElseThrow());
    }

    private RagSourceView approve(String name, String text) throws Exception {
        Path path = input.resolve(name);
        Files.writeString(path, text, StandardCharsets.UTF_8);
        var candidate = enabledRag.registerSnapshotCandidate(path.toString());
        return enabledRag.approveSource(candidate.id(), new RagConsentCommand(Set.of("content"), candidate.sourceVersion(), candidate.consentResetVersion(),
                candidate.authorizationEpoch(), candidate.fieldPolicyVersion(), null, candidate.snapshot()));
    }

    private static RagDocumentRootItem root(String key, RagSourceView view) {
        return new RagDocumentRootItem(key, view.sourceType(), view.sourceId(), view.sourceVersion(), view.authorizationEpoch(), view.fieldPolicyVersion(), view.approvedFields());
    }

    private RagQaCommand command(UUID id, Fixture fixture) {
        var parent = fixture.parent();
        return new RagQaCommand(id, "Where is the archive?", RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(parent.id(), parent.snapshot().selectedRevisionId(), null, "Parent.md")), null, null, 5, List.of(), "en");
    }

    private CompletableFuture<HttpResponse<String>> send(RagQaCommand command, boolean barrier) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/rag/qa"))
                .header("Authorization", bearerHeader()).header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120));
        if (barrier) request.header("X-PPV-Test-QA-Barrier", "1");
        return HttpClient.newHttpClient().sendAsync(request.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build(), HttpResponse.BodyHandlers.ofString());
    }

    private Pause pauseAfterRealGeneration() {
        var pause = new Pause(new CountDownLatch(1), release());
        doAnswer(call -> {
            Object actual = call.callRealMethod();
            observations.put("real_generation_returned", true);
            pause.entered().countDown();
            if (!pause.release().await(20, TimeUnit.SECONDS)) throw new IllegalStateException("Test generation-to-emission barrier expired");
            return actual;
        }).when(supervisor).generateCandidate(any(), anyString(), anyList(), anyString(), anyList(), any());
        return pause;
    }

    private CountDownLatch release() {
        var latch = new CountDownLatch(1);
        releases.add(latch);
        return latch;
    }

    private UUID incident(RagSourceView view) {
        UUID inspection = UUID.randomUUID();
        authority.beginInspection(inspection, null, "TRUSTED_SYNTHETIC_QA_TEST", "qa-native-safety", UUID.fromString(view.sourceId()), view.snapshot().selectedRevisionId());
        authority.recordInspection(inspection, "INCIDENT_PENDING", true, Map.of());
        return inspection;
    }

    private void assertCompetingRequestsBusy(Fixture fixture) throws Exception {
        assertThat(requestQaOverHttp(command(UUID.randomUUID(), fixture)).statusCode()).isEqualTo(409);
        assertThat(requestRetrievalOverHttp(command(UUID.randomUUID(), fixture).toRetrievalCommand()).statusCode()).isEqualTo(409);
    }

    private void assertReserved(RagSourceView view, boolean reserved) {
        UUID value = jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?", UUID.class, UUID.fromString(view.sourceId()));
        assertThat(value != null).isEqualTo(reserved);
    }

    private void assertIndependentConnectionCanLock(RagSourceView view) {
        UUID file = UUID.fromString(view.sourceId());
        assertThat(jdbcTemplate.queryForObject("SELECT file_source_id FROM rag.external_file_sources WHERE file_source_id=? FOR UPDATE NOWAIT", UUID.class, file)).isEqualTo(file);
    }

    private boolean failed(RagSourceView view) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?", Boolean.class, UUID.fromString(view.sourceId())));
    }

    private String state(UUID id) {
        return jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?", String.class, id);
    }

    private String inspectionState(UUID id) {
        return jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?", String.class, id);
    }

    private String generationState(RagSourceView view) {
        UUID generation = jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=?", UUID.class, view.id());
        return generations.state(generation);
    }

    private void awaitTerminal(UUID id) {
        await().atMost(Duration.ofSeconds(8)).untilAsserted(() -> assertThat(state(id)).isEqualTo("TERMINAL"));
    }

    private void verifyOneNativeBindAndGeneration(UUID id) {
        verify(authority, times(1)).bindWorker(eq(id), anyString(), eq(session.get().process().pid()), any(), eq(session.get().process().creationTimeFileTime()));
        verify(supervisor, times(1)).generateCandidate(any(), anyString(), anyList(), anyString(), anyList(), any());
        observations.put("single_native_bind_and_generation", true);
    }

    private static void assertBusy(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).hasStackTraceContaining("RAG_SOURCE_BUSY");
    }

    private record OutputGate(CountDownLatch entered, CountDownLatch release, AtomicReference<WriteListener> listener) {
        OutputGate(CountDownLatch entered, CountDownLatch release) { this(entered, release, new AtomicReference<>()); }
    }
    private static final AtomicReference<OutputGate> output = new AtomicReference<>();

    @TestConfiguration(proxyBeanMethods = false)
    static class OutputConfiguration {
        @Bean
        FilterRegistrationBean<jakarta.servlet.Filter> nativeQaOutputBarrier() {
            jakarta.servlet.Filter filter = (request, response, chain) -> {
                OutputGate gate = output.get();
                if (!(request instanceof HttpServletRequest http) || gate == null || !"1".equals(http.getHeader("X-PPV-Test-QA-Barrier"))) {
                    chain.doFilter(request, response);
                    return;
                }
                var wrapped = new HttpServletResponseWrapper((HttpServletResponse) response) {
                    private ServletOutputStream stream;
                    @Override public ServletOutputStream getOutputStream() throws IOException {
                        if (stream == null) {
                            var delegate = super.getOutputStream();
                            stream = new ServletOutputStream() {
                                private boolean entered;
                                @Override public boolean isReady() { return delegate.isReady(); }
                                @Override public void setWriteListener(WriteListener listener) { gate.listener().set(listener); delegate.setWriteListener(listener); }
                                @Override public void write(int value) throws IOException { write(new byte[]{(byte) value}, 0, 1); }
                                @Override public void write(byte[] bytes, int offset, int length) throws IOException {
                                    if (!entered) {
                                        entered = true;
                                        gate.entered().countDown();
                                        try {
                                            if (!gate.release().await(15, TimeUnit.SECONDS)) throw new IOException("Synthetic stalled writer expired");
                                        } catch (InterruptedException stopped) {
                                            Thread.currentThread().interrupt();
                                            throw new IOException("Synthetic writer interrupted");
                                        }
                                    }
                                    delegate.write(bytes, offset, length);
                                }
                                @Override public void flush() throws IOException { delegate.flush(); }
                            };
                        }
                        return stream;
                    }
                };
                var wrappedRequest = new HttpServletRequestWrapper(http) {
                    @Override public jakarta.servlet.AsyncContext startAsync() { return super.startAsync(this, wrapped); }
                };
                chain.doFilter(wrappedRequest, wrapped);
            };
            var registration = new FilterRegistrationBean<>(filter);
            registration.setAsyncSupported(true);
            registration.addUrlPatterns("/api/v1/rag/qa");
            return registration;
        }
    }
}
