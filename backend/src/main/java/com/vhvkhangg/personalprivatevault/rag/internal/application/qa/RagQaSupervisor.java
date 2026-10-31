package com.vhvkhangg.personalprivatevault.rag.internal.application.qa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentFinalizationGate;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalControllerIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not enable or deploy.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 *
 * Supervises isolated, request-bound QA worker processes under Windows Job Object.
 * Preserves exactly one child per reader across query embedding and grounded generation,
 * with positive process-tree cleanup and strict model metadata verification.
 */
@ConditionalOnProperty(name = "ppv.rag.test-scoped-execution", havingValue = "true")
@Service
public class RagQaSupervisor {

    private static final Logger log = LoggerFactory.getLogger(RagQaSupervisor.class);
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration EMBED_HTTP_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration QA_HTTP_TIMEOUT = Duration.ofSeconds(180);
    private static final long MEMORY_LIMIT_BYTES = 8192L * 1024 * 1024; // 8 GiB

    static void validateGenerationSchema(JsonNode root) {
        exactKeys(root, Set.of("contract_version", "reader_id", "process_id", "endpoint_nonce",
                "scope_digest", "profile", "prompt_profile_hash", "outcome", "claims", "unanswered_parts",
                "conflict_pairs", "model_version", "artifact_hash"));
        for (String key : List.of("contract_version", "reader_id", "endpoint_nonce", "scope_digest",
                "profile", "prompt_profile_hash", "outcome", "model_version", "artifact_hash")) {
            requireText(root.get(key));
        }
        if (!root.get("process_id").isIntegralNumber() || !root.get("process_id").canConvertToLong()
                || root.get("process_id").longValue() <= 0) schemaFailure();
        for (String key : List.of("claims", "unanswered_parts", "conflict_pairs")) {
            if (!root.get(key).isArray()) schemaFailure();
        }
        for (JsonNode claim : root.get("claims")) {
            exactKeys(claim, Set.of("id", "text", "supports"));
            requireText(claim.get("id")); requireText(claim.get("text"));
            if (!claim.get("supports").isArray()) schemaFailure();
            for (JsonNode support : claim.get("supports")) {
                exactKeys(support, Set.of("citation_key", "quote"));
                requireText(support.get("citation_key")); requireText(support.get("quote"));
            }
        }
        for (JsonNode unanswered : root.get("unanswered_parts")) requireText(unanswered);
        for (JsonNode pair : root.get("conflict_pairs")) {
            exactKeys(pair, Set.of("claim_id_a", "claim_id_b", "description"));
            requireText(pair.get("claim_id_a")); requireText(pair.get("claim_id_b"));
            requireText(pair.get("description"));
        }
    }

    private static void exactKeys(JsonNode node, Set<String> expected) {
        if (node == null || !node.isObject()) schemaFailure();
        Set<String> actual = new HashSet<>();
        node.fieldNames().forEachRemaining(actual::add);
        if (!actual.equals(expected)) schemaFailure();
    }

    private static void requireText(JsonNode node) {
        if (node == null || !node.isTextual() || node.textValue().isBlank()) schemaFailure();
    }

    private static void schemaFailure() {
        throw new RagQueryUnavailableException("Malformed QA generation schema");
    }

    JsonNode parseGenerationResponse(byte[] bytes) throws IOException {
        JsonNode root = objectMapper.readTree(bytes);
        validateGenerationSchema(root);
        return root;
    }

    String workerFailureCategory(byte[] bytes) {
        try {
            String category = objectMapper.readTree(bytes).path("detail").path("category").textValue();
            if (Set.of("CANDIDATE_INVALID", "HEADROOM_UNAVAILABLE", "GENERATION_UNAVAILABLE").contains(category)) {
                return category;
            }
        } catch (Exception ignored) { /* Untrusted error bodies never become diagnostics. */ }
        return "UNCLASSIFIED_FAILURE";
    }

    public static final String EXPECTED_C1_MODEL_VERSION = "Qwen3-4B-Q4_K_M";
    public static final String EXPECTED_C1_ARTIFACT_HASH = "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5";
    public static final String EXPECTED_CONTRACT_VERSION = "1.0";
    public static final String EXPECTED_GENERATION_PROFILE = "C1_OFFLINE_CPU_V2";
    public static final String EXPECTED_PROMPT_PROFILE_HASH = "ad3cd9b7e53b29f76b152824e9a04a4249c1a9d2e322c1f50e47de37e33de4c1";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

    private final boolean qaEnabled;
    private final boolean retrievalEnabled;
    private final boolean testScopedExecution;
    private final String executable;
    private final String launcher;
    private final String directory;
    private final String modelPath;
    private final int threads;
    private final int contextTokens;
    private final int maxGeneratedTokens;
    private final boolean candidate2;
    private final boolean cudaTestEnabled;
    private final String cudaTestLauncher;
    private final RagRetrievalAuthority authority;
    private final RagRuntimeIdentity runtime;

    private final Map<UUID, NativeWorkerProcess> activeProcesses = new ConcurrentHashMap<>();
    private final Map<UUID, HttpClient> activeTransports = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> activePorts = new ConcurrentHashMap<>();
    private final Map<UUID, String> activeServiceKeys = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> activeNonces = new ConcurrentHashMap<>();
    private final Map<UUID, NativeWorkerProcess.ResourceUsage> resourceUsages = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> generationMeasurements = new ConcurrentHashMap<>();
    private final Set<UUID> stoppedReaders = ConcurrentHashMap.newKeySet();
    private final Object launchGuard = new Object();

    private volatile BiFunction<UUID, String, float[]> vectorOverride = null;
    private volatile BiFunction<UUID, RagQaCommand, RawCandidateGeneration> generationOverride = null;

    public record RawCandidateGeneration(
            String outcome,
            List<RagQaClaim> claims,
            List<String> unansweredParts,
            List<RagQaConflictPair> conflictPairs,
            String modelVersion,
            String artifactHash
    ) {}

    public record QaSession(
            UUID readerId,
            int port,
            String serviceKey,
            UUID nonce,
            NativeWorkerProcess process,
            HttpClient transport
    ) {}

    public RagQaSupervisor(
            @Value("${ppv.rag.qa.enabled:false}") boolean qaEnabled,
            @Value("${ppv.rag.retrieval.enabled:false}") boolean retrievalEnabled,
            @Value("${ppv.rag.test-scoped-execution:false}") boolean testScopedExecution,
            @Value("${ppv.rag.worker.executable:}") String executable,
            @Value("${ppv.rag.worker.venv-launcher:}") String launcher,
            @Value("${ppv.rag.worker.working-directory:}") String directory,
            @Value("${ppv.rag.qa.model-path:rag-engine/models/Qwen3-4B-Q4_K_M.gguf}") String modelPath,
            @Value("${ppv.rag.qa.threads:4}") int threads,
            @Value("${ppv.rag.qa.context-tokens:4096}") int contextTokens,
            @Value("${ppv.rag.qa.max-generated-tokens:768}") int maxGeneratedTokens,
            @Value("${ppv.rag.qa.cuda-test-enabled:false}") boolean cudaTestEnabled,
            @Value("${ppv.rag.qa.cuda-test-launcher:}") String cudaTestLauncher,
            RagRetrievalAuthority authority,
            RagRuntimeIdentity runtime
    ) {
        this.qaEnabled = qaEnabled;
        this.retrievalEnabled = retrievalEnabled;
        this.testScopedExecution = testScopedExecution;
        this.executable = executable;
        this.launcher = launcher;
        this.directory = directory;
        this.modelPath = modelPath;
        this.threads = threads;
        this.contextTokens = contextTokens;
        this.maxGeneratedTokens = maxGeneratedTokens;
        this.candidate2 = Path.of(modelPath).getFileName().toString()
                .equals("qwen2.5-7b-instruct-q4_k_m-00001-of-00002.gguf");
        if (cudaTestEnabled && (!testScopedExecution || !candidate2 || cudaTestLauncher.isBlank())) {
            throw new IllegalArgumentException("CUDA QA requires an explicit C2 test-scoped runtime");
        }
        this.cudaTestEnabled = cudaTestEnabled;
        this.cudaTestLauncher = cudaTestLauncher;
        this.authority = authority;
        this.runtime = runtime;
    }

    public void setVectorOverride(BiFunction<UUID, String, float[]> override) {
        this.vectorOverride = override;
    }

    public void setGenerationOverride(BiFunction<UUID, RagQaCommand, RawCandidateGeneration> override) {
        this.generationOverride = override;
    }

    public boolean isEnabled() {
        return qaEnabled || testScopedExecution;
    }

    public boolean hasGenerationOverride() {
        return generationOverride != null;
    }

    public RawCandidateGeneration applyGenerationOverride(UUID readerId, RagQaCommand command) {
        if (generationOverride != null) {
            return generationOverride.apply(readerId, command);
        }
        return null;
    }

    public float[] applyVectorOverride(UUID readerId, String question) {
        if (vectorOverride != null) {
            float[] vec = vectorOverride.apply(readerId, question);
            if (vec != null) {
                validateVector(vec);
                return vec;
            }
        }
        return null;
    }

    public QaSession startSession(UUID readerId) {
        if (readerId == null) {
            throw new IllegalArgumentException("readerId must not be null");
        }
        if (!isEnabled()) {
            throw new RagQueryUnavailableException("RAG QA is disabled");
        }
        synchronized (launchGuard) {
            if (stoppedReaders.contains(readerId)) {
                throw new RagQueryUnavailableException("QA worker stopped");
            }
        }

        if (!System.getProperty("os.name").toLowerCase().contains("win")
                || !NativeWorkerProcess.class.getModule().isNativeAccessEnabled()) {
            throw new RagQueryUnavailableException("Native worker execution is not supported on this platform");
        }

        Path workDir = resolveDirectory(directory);
        Path venv = cudaTestEnabled ? Path.of(cudaTestLauncher).toAbsolutePath().normalize() : resolveVenv(launcher);
        if (!Files.isRegularFile(venv)) throw new RagQueryUnavailableException("QA runtime unavailable");
        Path binary = resolveBinary(executable, venv);

        int port = allocatePort();
        String serviceKey = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        UUID nonce = UUID.randomUUID();

        List<String> args = List.of(
                "-m", "uvicorn",
                "ppv_rag.grounded_qa:app",
                "--host", "127.0.0.1",
                "--port", String.valueOf(port),
                "--no-access-log",
                "--no-server-header",
                "--log-level", "critical"
        );

        Map<String, String> env = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String name : List.of("SystemRoot", "WINDIR", "TEMP", "TMP", "LOCALAPPDATA", "PATH")) {
            String val = System.getenv(name);
            if (val != null) env.put(name, val);
        }
        env.put("__PYVENV_LAUNCHER__", venv.toString());
        env.put("PPV_RAG_QA_RUNTIME_PROFILE", cudaTestEnabled ? "CUDA_TEST_V1" : "CPU_V2");
        if (cudaTestEnabled) {
            Path nvidia = venv.getParent().getParent().resolve("Lib/site-packages/nvidia/cu13/bin/x86_64");
            if (!Files.isRegularFile(nvidia.resolve("cublas64_13.dll"))) {
                throw new RagQueryUnavailableException("Pinned CUDA runtime unavailable");
            }
            env.put("PATH", nvidia + ";" + System.getenv("SystemRoot") + "\\System32");
            env.put("PPV_RAG_QA_TEST_SCOPED", "1");
            env.put("CUDA_VISIBLE_DEVICES", "0");
            env.put("OMP_NUM_THREADS", "4");
        }
        env.put("PYTHONPATH", workDir.resolve("src").toString());
        env.put("PYTHONNOUSERSITE", "1");
        env.put("PYTHONDONTWRITEBYTECODE", "1");
        env.put("PPV_RAG_READER_ID", readerId.toString());
        env.put("PPV_RAG_NONCE", nonce.toString());
        env.put("PPV_RAG_SERVICE_KEY", serviceKey);
        Path resolvedModel = resolveModelPath(modelPath, workDir);
        env.put("PPV_RAG_QA_MODEL_PATH", resolvedModel.toString());
        env.put("PPV_RAG_QA_CANDIDATE", candidate2 ? "C2" : "C1");
        env.put("PPV_RAG_QA_THREADS", String.valueOf(threads));
        env.put("PPV_RAG_QA_CONTEXT_TOKENS", String.valueOf(contextTokens));
        env.put("PPV_RAG_QA_MAX_GENERATED_TOKENS", String.valueOf(maxGeneratedTokens));

        NativeWorkerProcess process = null;
        HttpClient httpClient = null;
        try {
            synchronized (launchGuard) {
                if (stoppedReaders.contains(readerId)) {
                    throw new RagQueryUnavailableException("QA worker stopped");
                }
                process = NativeWorkerProcess.launchSuspended(readerId, binary, args, env, workDir, MEMORY_LIMIT_BYTES, 1);
                activeProcesses.put(readerId, process);
                activePorts.put(readerId, port);
                activeServiceKeys.put(readerId, serviceKey);
                activeNonces.put(readerId, nonce);
                authority.bindWorker(readerId, runtime.instance().toString(), process.pid(), process.creationTime(), process.creationTimeFileTime());
                process.resume();
            }

            httpClient = HttpClient.newBuilder()
                    .proxy(HttpClient.Builder.NO_PROXY)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();
            activeTransports.put(readerId, httpClient);

            boolean ready = false;
            long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();

            while (System.nanoTime() < deadline) {
                if (process.hasTerminatedTree()) {
                    throw new RagQueryUnavailableException("QA worker terminated unexpectedly");
                }
                try {
                    HttpRequest readyReq = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/query/readiness"))
                            .timeout(Duration.ofSeconds(1))
                            .header("X-PPV-Service-Key", serviceKey)
                            .header("X-PPV-Reader-Id", readerId.toString())
                            .header("X-PPV-Nonce", nonce.toString())
                            .GET()
                            .build();

                    HttpResponse<InputStream> readyResp = httpClient.send(readyReq, HttpResponse.BodyHandlers.ofInputStream());
                    if (readyResp.statusCode() == 200) {
                        byte[] body;
                        try (InputStream is = readyResp.body()) {
                            body = readBoundedBytes(is, 65_536);
                        }
                        JsonNode root = objectMapper.readTree(body);
                        String respReader = root.path("reader_id").asText();
                        String respNonce = root.has("endpoint_nonce") ? root.path("endpoint_nonce").asText() : root.path("nonce").asText();
                        long respPid = root.path("process_id").asLong();
                        int dims = root.path("dimensions").asInt();
                        String modelVer = root.path("model_version").asText();
                        String artifactHash = root.path("artifact_hash").asText();
                        String tokenizerVer = root.path("tokenizer_version").asText();

                        String expectedModelVer = String.valueOf(RagDocumentFinalizationGate.PIPELINE.get("model_version"));
                        String expectedHash = String.valueOf(RagDocumentFinalizationGate.PIPELINE.get("artifact_hash"));
                        String expectedTokenizerVer = String.valueOf(RagDocumentFinalizationGate.PIPELINE.get("tokenizer_version"));

                        if ("ready".equalsIgnoreCase(root.path("status").asText())
                                && "1.0".equals(root.path("contract_version").asText())
                                && readerId.toString().equals(respReader)
                                && nonce.toString().equals(respNonce)
                                && dims == 384
                                && respPid == process.pid()
                                && Objects.equals(expectedModelVer, modelVer)
                                && expectedHash.equalsIgnoreCase(artifactHash)
                                && Objects.equals(expectedTokenizerVer, tokenizerVer)) {
                            if (!Objects.equals(RagDocumentFinalizationGate.PIPELINE.get("tokenizer_artifact_hash"), root.path("tokenizer_artifact_hash").asText())) {
                                throw new RagQueryUnavailableException("Tokenizer artifact identity mismatch");
                            }
                            ready = true;
                            break;
                        }
                    } else {
                        readyResp.body().close();
                    }
                } catch (Exception ignored) {
                    // Waiting for uvicorn to bind port
                }
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
            }

            if (!ready) {
                throw new RagQueryUnavailableException("QA worker readiness deadline exceeded");
            }

            return new QaSession(readerId, port, serviceKey, nonce, process, httpClient);

        } catch (RagQueryUnavailableException e) {
            stopWorker(readerId);
            throw e;
        } catch (Exception e) {
            stopWorker(readerId);
            throw new RagQueryUnavailableException("QA worker launch failed: " + e.getMessage(), e);
        }
    }

    public float[] computeEmbedding(QaSession session, String text) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(text, "text must not be null");

        if (vectorOverride != null) {
            float[] vec = vectorOverride.apply(session.readerId(), text);
            if (vec != null) {
                validateVector(vec);
                return vec;
            }
        }

        try {
            if (!session.process().ownsProcess(session.process().pid(), session.process().creationTimeFileTime())) {
                throw new RagQueryUnavailableException("QA child identity is no longer current");
            }

            Map<String, Object> reqBodyMap = Map.of(
                    "reader_id", session.readerId().toString(),
                    "question", text
            );
            String reqBodyJson = objectMapper.writeValueAsString(reqBodyMap);

            HttpRequest embedReq = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + session.port() + "/v1/query/embed"))
                    .timeout(EMBED_HTTP_TIMEOUT)
                    .header("X-PPV-Service-Key", session.serviceKey())
                    .header("X-PPV-Reader-Id", session.readerId().toString())
                    .header("X-PPV-Nonce", session.nonce().toString())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqBodyJson))
                    .build();

            HttpResponse<InputStream> embedResp = session.transport().send(embedReq, HttpResponse.BodyHandlers.ofInputStream());
            if (embedResp.statusCode() != 200) {
                try {
                    embedResp.body().close();
                } catch (Exception ignored) {}
                if (embedResp.statusCode() == 400) throw new IllegalArgumentException("Query embedding input rejected");
                throw new RagQueryUnavailableException("Query embedding failed in QA session");
            }

            byte[] embedBytes;
            try (InputStream is = embedResp.body()) {
                embedBytes = readBoundedBytes(is, 65_536);
            }

            JsonNode embedJson = objectMapper.readTree(embedBytes);
            String respReader = embedJson.path("reader_id").asText();
            if (!session.readerId().toString().equals(respReader)
                    || !"1.0".equals(embedJson.path("contract_version").asText())
                    || !session.nonce().toString().equals(embedJson.path("endpoint_nonce").asText())
                    || embedJson.path("process_id").asLong() != session.process().pid()
                    || embedJson.path("dimensions").asInt() != 384) {
                throw new RagQueryUnavailableException("Mismatched metadata in embed response");
            }

            JsonNode embeddingArray = embedJson.has("embedding") ? embedJson.path("embedding") : embedJson.path("vector");
            if (!embeddingArray.isArray() || embeddingArray.size() != 384) {
                throw new RagQueryUnavailableException("Query embedding returned invalid vector dimensions");
            }

            float[] vector = new float[384];
            for (int i = 0; i < 384; i++) {
                vector[i] = (float) embeddingArray.get(i).asDouble();
            }

            validateVector(vector);
            return vector;

        } catch (IllegalArgumentException | RagQueryUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagQueryUnavailableException("Query embedding failed: " + e.getMessage(), e);
        }
    }

    public RawCandidateGeneration generateCandidate(
            QaSession session,
            String question,
            List<RagQaHistoryEntry> history,
            String responseLanguage,
            List<RagRetrievalMatch> evidence,
            String scopeDigest
    ) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(question, "question must not be null");

        if (!session.process().ownsProcess(session.process().pid(), session.process().creationTimeFileTime())) {
            throw new RagQueryUnavailableException("QA child identity is no longer current");
        }

        try {
            List<Map<String, Object>> evList = new ArrayList<>();
            for (RagRetrievalMatch m : evidence) {
                evList.add(Map.of(
                        "citation_key", m.citationKey(),
                        "excerpt", m.excerpt(),
                        "source_name", m.displayName() != null ? m.displayName() : "unknown.md"
                ));
            }

            List<Map<String, Object>> histList = new ArrayList<>();
            if (history != null) {
                for (RagQaHistoryEntry h : history) {
                    histList.add(Map.of(
                            "role", h.role(),
                            "content", h.content()
                    ));
                }
            }

            String effectiveScopeDigest = scopeDigest;
            if (effectiveScopeDigest == null || effectiveScopeDigest.isBlank()) {
                try {
                    var md = java.security.MessageDigest.getInstance("SHA-256");
                    for (RagRetrievalMatch m : evidence) {
                        md.update(m.citationKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        md.update((byte) 0);
                        md.update(m.excerpt().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        md.update((byte) 0);
                    }
                    effectiveScopeDigest = java.util.HexFormat.of().formatHex(md.digest());
                } catch (java.security.NoSuchAlgorithmException e) {
                    effectiveScopeDigest = "00".repeat(32);
                }
            }

            Map<String, Object> reqBodyMap = new LinkedHashMap<>();
            reqBodyMap.put("reader_id", session.readerId().toString());
            reqBodyMap.put("nonce", session.nonce().toString());
            reqBodyMap.put("question", question);
            reqBodyMap.put("evidence", evList);
            reqBodyMap.put("history", histList);
            reqBodyMap.put("response_language", (responseLanguage != null && !responseLanguage.isBlank()) ? responseLanguage : "vi");
            reqBodyMap.put("scope_digest", effectiveScopeDigest);

            String reqBodyJson = objectMapper.writeValueAsString(reqBodyMap);

            HttpRequest genReq = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + session.port() + "/v1/qa/generate"))
                    .timeout(QA_HTTP_TIMEOUT)
                    .header("X-PPV-Service-Key", session.serviceKey())
                    .header("X-PPV-Reader-Id", session.readerId().toString())
                    .header("X-PPV-Nonce", session.nonce().toString())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqBodyJson))
                    .build();

            HttpResponse<InputStream> genResp = session.transport().send(genReq, HttpResponse.BodyHandlers.ofInputStream());
            if (genResp.statusCode() != 200) {
                String category = "UNCLASSIFIED_FAILURE";
                try (InputStream is = genResp.body()) {
                    category = workerFailureCategory(readBoundedBytes(is, 1024));
                } catch (Exception ignored) {}
                log.warn("QA candidate generation rejected by worker (HTTP {}, category={})", genResp.statusCode(), category);
                if (genResp.statusCode() == 400) throw new IllegalArgumentException("QA generate request rejected");
                throw new RagQueryUnavailableException("QA candidate generation failed (HTTP " + genResp.statusCode() + ")");
            }

            byte[] genBytes;
            try (InputStream is = genResp.body()) {
                genBytes = readBoundedBytes(is, 65_536);
            }

            JsonNode genJson = parseGenerationResponse(genBytes);
            String respReader = genJson.path("reader_id").asText();
            String respNonce = genJson.path("endpoint_nonce").asText();
            long respPid = genJson.path("process_id").asLong();
            String respContract = genJson.path("contract_version").asText();
            String respScope = genJson.path("scope_digest").asText();
            String respProfile = genJson.path("profile").asText();
            String promptProfileHash = genJson.path("prompt_profile_hash").asText();

            if (!session.readerId().toString().equals(respReader)
                    || !session.nonce().toString().equals(respNonce)
                    || respPid != session.process().pid()
                    || !EXPECTED_CONTRACT_VERSION.equals(respContract)
                    || !Objects.equals(effectiveScopeDigest, respScope)
                    || !(cudaTestEnabled ? "C2_OFFLINE_CUDA_TEST_V1"
                            : candidate2 ? "C2_OFFLINE_CPU_V2" : EXPECTED_GENERATION_PROFILE).equals(respProfile)
                    || !(cudaTestEnabled ? "555ee729dc9b2636822f3b18ff5bbafe570f152a94f2cd04bdea24822ff008b6" : candidate2 ? "9dd19d25552ac9d533e45e5ec1fb014d77e6ffdcf32b0ae46c00989ea9578029"
                            : EXPECTED_PROMPT_PROFILE_HASH).equals(promptProfileHash)) {
                throw new RagQueryUnavailableException("Generation response identity, scope digest, or profile mismatch");
            }

            if (!genJson.hasNonNull("outcome")
                    || !genJson.has("claims") || !genJson.path("claims").isArray()
                    || !genJson.has("unanswered_parts") || !genJson.path("unanswered_parts").isArray()
                    || !genJson.has("conflict_pairs") || !genJson.path("conflict_pairs").isArray()) {
                throw new RagQueryUnavailableException("Malformed generation response schema: missing required fields");
            }

            String modelVer = genJson.path("model_version").asText();
            String artifactHash = genJson.path("artifact_hash").asText();
            if (!(candidate2 ? "Qwen2.5-7B-Instruct-Q4_K_M" : EXPECTED_C1_MODEL_VERSION).equals(modelVer)
                    || !(candidate2 ? "dfce12e3862a5283ccfb88221b48480e58745165de856439950d0f22590580db"
                            : EXPECTED_C1_ARTIFACT_HASH).equals(artifactHash)) {
                throw new RagQueryUnavailableException("Mismatched model metadata in QA candidate generation response");
            }

            String outcome = genJson.path("outcome").asText();

            List<RagQaClaim> claims = new ArrayList<>();
            JsonNode claimsArray = genJson.path("claims");
            if (claimsArray.isArray()) {
                for (JsonNode cNode : claimsArray) {
                    String cid = cNode.path("id").asText();
                    String ctext = cNode.path("text").asText();
                    List<RagQaClaimSupport> supports = new ArrayList<>();
                    JsonNode supArray = cNode.path("supports");
                    if (supArray.isArray()) {
                        for (JsonNode sNode : supArray) {
                            supports.add(new RagQaClaimSupport(
                                    sNode.path("citation_key").asText(),
                                    sNode.path("quote").asText()
                            ));
                        }
                    }
                    claims.add(new RagQaClaim(cid, ctext, supports));
                }
            }

            List<String> unansweredParts = new ArrayList<>();
            JsonNode unansArray = genJson.path("unanswered_parts");
            if (unansArray.isArray()) {
                for (JsonNode uNode : unansArray) {
                    unansweredParts.add(uNode.asText());
                }
            }

            List<RagQaConflictPair> conflictPairs = new ArrayList<>();
            JsonNode confArray = genJson.path("conflict_pairs");
            if (confArray.isArray()) {
                for (JsonNode cpNode : confArray) {
                    conflictPairs.add(new RagQaConflictPair(
                            cpNode.path("claim_id_a").asText(),
                            cpNode.path("claim_id_b").asText(),
                            cpNode.path("description").asText()
                    ));
                }
            }

            Map<String, Long> measurements = new LinkedHashMap<>();
            for (String key : List.of("model-load-ns", "generation-ns", "prompt-tokens", "completion-tokens",
                    "evidence-units", "context-omissions")) {
                var header = genResp.headers().firstValue("X-PPV-QA-" + key);
                if (header.isPresent()) {
                    if (!header.get().matches("[0-9]{1,15}")) {
                        throw new RagQueryUnavailableException("Malformed generation measurement");
                    }
                    measurements.put(key, Long.parseLong(header.get()));
                }
            }
            generationMeasurements.put(session.readerId(), Map.copyOf(measurements));
            return new RawCandidateGeneration(outcome, claims, unansweredParts, conflictPairs, modelVer, artifactHash);

        } catch (IllegalArgumentException | RagQueryUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagQueryUnavailableException("Candidate generation failed: " + e.getMessage(), e);
        }
    }

    public void stopWorker(UUID readerId) {
        stopWorker(readerId, System.nanoTime() + Duration.ofSeconds(5).toNanos());
    }

    public void stopWorker(UUID readerId, long stopByNanos) {
        if (readerId == null) return;
        NativeWorkerProcess proc;
        synchronized (launchGuard) {
            stoppedReaders.add(readerId);
            proc = activeProcesses.get(readerId);
        }
        if (proc != null) {
            synchronized (proc) {
                if (activeProcesses.get(readerId) == proc) {
                    boolean terminated = false;
                    try {
                        if (!proc.hasTerminatedTree()) {
                            long remaining = stopByNanos - System.nanoTime();
                            if (remaining <= 0) throw new RagQueryUnavailableException("QA stop deadline exceeded");
                            if (!proc.terminateAndVerify(Duration.ofNanos(remaining))) {
                                throw new RagQueryUnavailableException("QA worker termination is not proven");
                            }
                        }
                        terminated = true;
                        try {
                            resourceUsages.put(readerId, proc.resourceUsage());
                        } catch (Exception e) {
                            log.debug("Resource usage query failed for reader {}: {}", readerId, e.getMessage());
                        }
                        activeProcesses.remove(readerId, proc);
                    } catch (Exception e) {
                        log.warn("Failed to terminate QA process cleanly for reader {}", readerId, e);
                        throw new IllegalStateException("Failed to verify QA worker termination", e);
                    } finally {
                        if (terminated) {
                            try {
                                proc.close();
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }

        HttpClient transport = activeTransports.get(readerId);
        if (transport != null) {
            transport.shutdownNow();
            try {
                if (!transport.awaitTermination(Duration.ofNanos(Math.max(0, stopByNanos - System.nanoTime())))) {
                    throw new RagQueryUnavailableException("QA transport termination is not proven");
                }
                activeTransports.remove(readerId, transport);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RagQueryUnavailableException("QA transport stop interrupted");
            }
        }
        activePorts.remove(readerId);
        activeServiceKeys.remove(readerId);
        activeNonces.remove(readerId);
    }

    public void forgetStopped(UUID readerId) {
        synchronized (launchGuard) {
            if (activeProcesses.containsKey(readerId) || activeTransports.containsKey(readerId)) {
                throw new IllegalStateException("QA ownership remains unresolved");
            }
            stoppedReaders.remove(readerId);
            resourceUsages.remove(readerId);
            generationMeasurements.remove(readerId);
        }
    }

    public boolean hasActiveProcess(UUID readerId) {
        return activeProcesses.containsKey(readerId);
    }

    public NativeWorkerProcess.ResourceUsage resourceUsage(UUID readerId) {
        NativeWorkerProcess.ResourceUsage recorded = resourceUsages.get(readerId);
        if (recorded != null) {
            return recorded;
        }
        NativeWorkerProcess proc = activeProcesses.get(readerId);
        if (proc != null && proc.hasTerminatedTree()) {
            try {
                return proc.resourceUsage();
            } catch (Exception ignored) {}
        }
        return new NativeWorkerProcess.ResourceUsage(0, 0, 0, 0, 0, 0, 0, 0);
    }

    public Map<String, Long> generationMeasurements(UUID readerId) {
        return generationMeasurements.getOrDefault(readerId, Map.of());
    }

    private void validateVector(float[] vector) {
        if (vector == null || vector.length != 384) {
            throw new RagQueryUnavailableException("Invalid embedding vector dimensions");
        }
        double sumSq = 0.0;
        boolean allZeros = true;
        for (float v : vector) {
            if (!Float.isFinite(v)) {
                throw new RagQueryUnavailableException("Embedding vector contains non-finite values");
            }
            if (v != 0.0f) {
                allZeros = false;
            }
            sumSq += (double) v * (double) v;
        }
        if (allZeros) {
            throw new RagQueryUnavailableException("Embedding vector cannot be all zeros");
        }
        double norm = Math.sqrt(sumSq);
        if (Math.abs(norm - 1.0) > 1e-3) {
            throw new RagQueryUnavailableException("Embedding vector is not L2 normalized: norm=" + norm);
        }
    }

    public String formatVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private int allocatePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to allocate local port for QA worker", e);
        }
    }

    private Path resolveDirectory(String configured) {
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) return p;
        }
        Path candidate = Path.of("rag-engine").toAbsolutePath().normalize();
        if (Files.isDirectory(candidate)) return candidate;
        Path parentCandidate = Path.of("../rag-engine").toAbsolutePath().normalize();
        if (Files.isDirectory(parentCandidate)) return parentCandidate;
        throw new IllegalStateException("RAG worker working directory not found");
    }

    private Path resolveModelPath(String configured, Path workDir) {
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured);
            if (p.isAbsolute() && Files.isRegularFile(p)) return p;
            Path direct = p.toAbsolutePath().normalize();
            if (Files.isRegularFile(direct)) return direct;
            Path parentDirect = Path.of("..").resolve(p).toAbsolutePath().normalize();
            if (Files.isRegularFile(parentDirect)) return parentDirect;
            Path inWork = workDir.resolve(p).toAbsolutePath().normalize();
            if (Files.isRegularFile(inWork)) return inWork;
            if (p.getNameCount() > 1 && "rag-engine".equals(p.getName(0).toString())) {
                Path stripped = workDir.resolve(p.subpath(1, p.getNameCount())).toAbsolutePath().normalize();
                if (Files.isRegularFile(stripped)) return stripped;
            }
            throw new IllegalStateException("Configured QA model path does not exist: " + configured);
        }
        Path p1 = Path.of("rag-engine/models/Qwen3-4B-Q4_K_M.gguf").toAbsolutePath().normalize();
        if (Files.isRegularFile(p1)) return p1;
        Path p2 = Path.of("../rag-engine/models/Qwen3-4B-Q4_K_M.gguf").toAbsolutePath().normalize();
        if (Files.isRegularFile(p2)) return p2;
        Path p3 = workDir.resolve("models/Qwen3-4B-Q4_K_M.gguf").toAbsolutePath().normalize();
        if (Files.isRegularFile(p3)) return p3;
        throw new IllegalStateException("Default QA candidate model Qwen3-4B-Q4_K_M.gguf does not exist");
    }

    private Path resolveVenv(String configured) {
        Path qaVenv = Path.of("rag-engine/.venv-qa/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isRegularFile(qaVenv)) return qaVenv;
        Path parentQaVenv = Path.of("../rag-engine/.venv-qa/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isRegularFile(parentQaVenv)) return parentQaVenv;
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured).toAbsolutePath().normalize();
            if (Files.isRegularFile(p)) return p;
        }
        Path candidate = Path.of("rag-engine/.venv-d17/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isRegularFile(candidate)) return candidate;
        Path parentCandidate = Path.of("../rag-engine/.venv-d17/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isRegularFile(parentCandidate)) return parentCandidate;
        throw new IllegalStateException("RAG worker venv launcher not found");
    }

    private Path resolveBinary(String configured, Path venv) {
        if (venv != null) {
            Path pyvenvCfg = venv.getParent().getParent().resolve("pyvenv.cfg");
            if (Files.isRegularFile(pyvenvCfg)) {
                try {
                    for (String line : Files.readAllLines(pyvenvCfg)) {
                        if (line.startsWith("home = ")) {
                            Path baseExe = Path.of(line.substring(7).trim()).resolve("python.exe");
                            if (Files.isRegularFile(baseExe)) return baseExe;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured).toAbsolutePath().normalize();
            if (Files.isRegularFile(p)) return p;
        }
        if (venv != null && Files.isRegularFile(venv)) return venv;
        Path candidate = Path.of("rag-engine/.venv-qa/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isRegularFile(candidate)) return candidate;
        Path parentCandidate = Path.of("../rag-engine/.venv-qa/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isRegularFile(parentCandidate)) return parentCandidate;
        throw new IllegalStateException("RAG worker executable binary not found");
    }

    private byte[] readBoundedBytes(InputStream is, int maxBytes) throws IOException {
        byte[] buffer = new byte[Math.min(maxBytes, 4096)];
        int total = 0;
        int read;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        while ((read = is.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new RagQueryUnavailableException("Response payload exceeds bounded size limit (" + maxBytes + ")");
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }
}
