package com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.RagQueryUnavailableException;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentFinalizationGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;

/**
 * Supervises isolated, request-bound query embedding worker processes under Windows Job Object.
 * Enforces authenticated loopback communication, 2 GiB memory cap, strict pinned model verification,
 * and positive process tree termination.
 */
@Service
public class RagQueryEmbeddingSupervisor {

    private static final Logger log = LoggerFactory.getLogger(RagQueryEmbeddingSupervisor.class);
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);
    private static final long MEMORY_LIMIT_BYTES = 2048L * 1024 * 1024; // 2 GiB

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private final boolean retrievalEnabled;
    private final boolean testScopedExecution;
    private final String executable;
    private final String launcher;
    private final String directory;
    private final RagRetrievalAuthority authority;
    private final com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity runtime;

    private final Map<UUID, NativeWorkerProcess> activeProcesses = new ConcurrentHashMap<>();
    private final Map<UUID, HttpClient> activeTransports = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> stoppedReaders = ConcurrentHashMap.newKeySet();
    private final Object launchGuard = new Object();
    private volatile BiFunction<UUID, String, float[]> vectorOverride = null;

    public record QueryEmbeddingResult(
            float[] vector,
            String vectorString,
            NativeWorkerProcess.ResourceUsage resourceUsage,
            long startupNanos,
            long totalNanos,
            int dimensions
    ) {}

    public RagQueryEmbeddingSupervisor(
            @Value("${ppv.rag.retrieval.enabled:false}") boolean retrievalEnabled,
            @Value("${ppv.rag.test-scoped-execution:false}") boolean testScopedExecution,
            @Value("${ppv.rag.worker.executable:}") String executable,
            @Value("${ppv.rag.worker.venv-launcher:}") String launcher,
            @Value("${ppv.rag.worker.working-directory:}") String directory,
            RagRetrievalAuthority authority,
            com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity runtime
    ) {
        this.retrievalEnabled = retrievalEnabled;
        this.testScopedExecution = testScopedExecution;
        this.executable = executable;
        this.launcher = launcher;
        this.directory = directory;
        this.authority = authority;
        this.runtime = runtime;
    }

    public void setVectorOverride(BiFunction<UUID, String, float[]> override) {
        this.vectorOverride = override;
    }

    public boolean isEnabled() {
        return retrievalEnabled || testScopedExecution;
    }

    public void stopWorker(UUID readerId) {
        stopWorker(readerId,System.nanoTime()+Duration.ofSeconds(5).toNanos());
    }

    public void stopWorker(UUID readerId,long stopByNanos) {
        if (readerId == null) return;
        NativeWorkerProcess proc;
        synchronized (launchGuard) {
            stoppedReaders.add(readerId);
            proc = activeProcesses.get(readerId);
        }
        if (proc != null) {
            synchronized (proc) {
                // A concurrent stopper may already have proven stop and closed this handle.
                if (activeProcesses.get(readerId) == proc) {
                    boolean terminated = false;
                    try {
                        if (!proc.hasTerminatedTree()) {
                            long remaining=stopByNanos-System.nanoTime();
                            if(remaining<=0) throw new RagQueryUnavailableException("Query stop deadline exceeded");
                            if (!proc.terminateAndVerify(Duration.ofNanos(remaining))) {
                                throw new RagQueryUnavailableException("Query worker termination is not proven");
                            }
                        }
                        terminated = true;
                        activeProcesses.remove(readerId,proc);
                    } catch (Exception e) {
                        log.warn("Failed to terminate query embedding process cleanly for reader {}", readerId, e);
                        throw new IllegalStateException("Failed to verify query worker termination", e);
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
                if (!transport.awaitTermination(Duration.ofNanos(Math.max(0,stopByNanos-System.nanoTime())))) {
                    throw new RagQueryUnavailableException("Query transport termination is not proven");
                }
                activeTransports.remove(readerId, transport);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RagQueryUnavailableException("Query transport stop interrupted");
            }
        }
    }

    public void forgetStopped(UUID readerId) {
        synchronized (launchGuard) {
            if (activeProcesses.containsKey(readerId) || activeTransports.containsKey(readerId)) {
                throw new IllegalStateException("Query ownership remains unresolved");
            }
            stoppedReaders.remove(readerId);
        }
    }

    public QueryEmbeddingResult computeEmbedding(UUID readerId, String question) {
        if (readerId == null) {
            throw new IllegalArgumentException("readerId must not be null");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        if (!isEnabled()) {
            throw new RagQueryUnavailableException("RAG retrieval is disabled");
        }
        synchronized (launchGuard) {
            if (stoppedReaders.contains(readerId)) throw new RagQueryUnavailableException("Query worker stopped");
        }

        if (vectorOverride != null) {
            float[] vec = vectorOverride.apply(readerId, question);
            if (vec != null) {
                validateVector(vec);
                return new QueryEmbeddingResult(
                        vec,
                        formatVectorString(vec),
                        new NativeWorkerProcess.ResourceUsage(0, 0, 0, 0, 0, 0, 0, 0),
                        0,
                        0,
                        vec.length
                );
            }
        }

        long startNanos = System.nanoTime();

        if (!System.getProperty("os.name").toLowerCase().contains("win")
                || !NativeWorkerProcess.class.getModule().isNativeAccessEnabled()) {
            throw new RagQueryUnavailableException("Native worker execution is not supported on this platform");
        }

        Path workDir = resolveDirectory(directory);
        Path venv = resolveVenv(launcher);
        Path binary = resolveBinary(executable, venv);

        int port = allocatePort();
        String serviceKey = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        UUID nonce = UUID.randomUUID();

        List<String> args = List.of(
                "-m", "uvicorn",
                "ppv_rag.query_embedding:app",
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
        env.put("PYTHONPATH", workDir.resolve("src").toString());
        env.put("PYTHONNOUSERSITE", "1");
        env.put("PYTHONDONTWRITEBYTECODE", "1");
        env.put("PPV_RAG_READER_ID", readerId.toString());
        env.put("PPV_RAG_NONCE", nonce.toString());
        env.put("PPV_RAG_SERVICE_KEY", serviceKey);

        NativeWorkerProcess process = null;
        HttpClient httpClient = null;
        try {
            synchronized (launchGuard) {
                if (stoppedReaders.contains(readerId)) throw new RagQueryUnavailableException("Query worker stopped");
                process = NativeWorkerProcess.launchSuspended(readerId, binary, args, env, workDir, MEMORY_LIMIT_BYTES, 1);
                activeProcesses.put(readerId, process);
                authority.bindWorker(readerId,runtime.instance().toString(),process.pid(),process.creationTime(),process.creationTimeFileTime());
                process.resume();
            }

            long startupStart = System.nanoTime();
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
                    throw new RagQueryUnavailableException("Query embedding worker terminated unexpectedly");
                }
                try {
                    HttpRequest readyReq = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/query/readiness"))
                            .timeout(Duration.ofSeconds(1))
                            .header("X-PPV-Service-Key", serviceKey)
                            .header("X-PPV-Reader-Id", readerId.toString())
                            .header("X-PPV-Nonce", nonce.toString())
                            .GET()
                            .build();

                    HttpResponse<java.io.InputStream> readyResp = httpClient.send(readyReq, HttpResponse.BodyHandlers.ofInputStream());
                    if (readyResp.statusCode() != 200) readyResp.body().close();
                    if (readyResp.statusCode() == 200) {
                        byte[] body;
                        try (java.io.InputStream is = readyResp.body()) {
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
                            if (!Objects.equals(RagDocumentFinalizationGate.PIPELINE.get("tokenizer_artifact_hash"),root.path("tokenizer_artifact_hash").asText())) {
                                throw new RagQueryUnavailableException("Tokenizer artifact identity mismatch");
                            }
                            ready = true;
                            break;
                        }
                    }
                } catch (Exception ignored) {
                    // Waiting for uvicorn to bind port
                }
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
            }

            if (!ready) {
                throw new RagQueryUnavailableException("Query embedding worker readiness deadline exceeded");
            }

            long startupNanos = System.nanoTime() - startupStart;

            // Execute embed
            if (!process.ownsProcess(process.pid(),process.creationTimeFileTime())) {
                throw new RagQueryUnavailableException("Query child identity is no longer current");
            }
            Map<String, Object> reqBodyMap = Map.of(
                    "reader_id", readerId.toString(),
                    "question", question
            );
            String reqBodyJson = objectMapper.writeValueAsString(reqBodyMap);

            HttpRequest embedReq = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/query/embed"))
                    .timeout(HTTP_TIMEOUT)
                    .header("X-PPV-Service-Key", serviceKey)
                    .header("X-PPV-Reader-Id", readerId.toString())
                    .header("X-PPV-Nonce", nonce.toString())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqBodyJson))
                    .build();

            HttpResponse<java.io.InputStream> embedResp = httpClient.send(embedReq, HttpResponse.BodyHandlers.ofInputStream());
            if (embedResp.statusCode() != 200) {
                try {
                    embedResp.body().close();
                } catch (Exception ignored) {}
                if (embedResp.statusCode()==400) throw new QueryInputRejected();
                throw new RagQueryUnavailableException("Query embedding failed");
            }

            byte[] embedBytes;
            try (java.io.InputStream is = embedResp.body()) {
                embedBytes = readBoundedBytes(is, 65_536);
            }

            JsonNode embedJson = objectMapper.readTree(embedBytes);
            String respReader = embedJson.path("reader_id").asText();
            if (!readerId.toString().equals(respReader) || !"1.0".equals(embedJson.path("contract_version").asText())
                    || !nonce.toString().equals(embedJson.path("endpoint_nonce").asText())
                    || embedJson.path("process_id").asLong()!=process.pid()
                    || embedJson.path("dimensions").asInt()!=384
                    || !process.ownsProcess(process.pid(),process.creationTimeFileTime())
                    || !Objects.equals(RagDocumentFinalizationGate.PIPELINE.get("tokenizer_artifact_hash"),embedJson.path("tokenizer_artifact_hash").asText())) {
                throw new RagQueryUnavailableException("Mismatched reader ID in embed response");
            }

            String embedModelVer = embedJson.path("model_version").asText();
            String embedHash = embedJson.path("artifact_hash").asText();
            String embedTokenizerVer = embedJson.path("tokenizer_version").asText();
            String expectedModelVer = String.valueOf(RagDocumentFinalizationGate.PIPELINE.get("model_version"));
            String expectedHash = String.valueOf(RagDocumentFinalizationGate.PIPELINE.get("artifact_hash"));
            String expectedTokenizerVer = String.valueOf(RagDocumentFinalizationGate.PIPELINE.get("tokenizer_version"));

            if (!Objects.equals(expectedModelVer, embedModelVer)
                    || !expectedHash.equalsIgnoreCase(embedHash)
                    || !Objects.equals(expectedTokenizerVer, embedTokenizerVer)) {
                throw new RagQueryUnavailableException("Mismatched model metadata in embed response");
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

            // Terminate and record usage
            process.terminateAndVerify(Duration.ofSeconds(5));
            NativeWorkerProcess.ResourceUsage resourceUsage = process.resourceUsage();
            long totalNanos = System.nanoTime() - startNanos;
            return new QueryEmbeddingResult(
                    vector,
                    formatVectorString(vector),
                    resourceUsage,
                    startupNanos,
                    totalNanos,
                    384
            );

        } catch (QueryInputRejected e) {
            throw e;
        } catch (RagQueryUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RagQueryUnavailableException("Query embedding failed: " + e.getMessage(), e);
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.shutdownNow();
                    if (!httpClient.awaitTermination(Duration.ofSeconds(5))) {
                        throw new RagQueryUnavailableException("Query transport termination is not proven");
                    }
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RagQueryUnavailableException("Query transport stop interrupted");
            } finally { stopWorker(readerId); }
        }
    }

    private static final class QueryInputRejected extends IllegalArgumentException {
        QueryInputRejected() { super("Query exceeds the pinned tokenizer input bound"); }
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

    private String formatVectorString(float[] vector) {
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
            throw new IllegalStateException("Failed to allocate local port for query embedding", e);
        }
    }

    private Path resolveDirectory(String configured) {
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) return p;
        }
        Path candidate = Path.of("rag-engine").toAbsolutePath().normalize();
        if (Files.isDirectory(candidate)) return candidate;
        throw new IllegalStateException("RAG worker working directory not found");
    }

    private Path resolveVenv(String configured) {
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured).toAbsolutePath().normalize();
            if (Files.isExecutable(p)) return p;
        }
        Path candidate = Path.of("rag-engine/.venv-d17/Scripts/python.exe").toAbsolutePath().normalize();
        if (Files.isExecutable(candidate)) return candidate;
        throw new IllegalStateException("RAG worker venv launcher not found");
    }

    private Path resolveBinary(String configured, Path venv) {
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured).toAbsolutePath().normalize();
            if (Files.isExecutable(p)) return p;
        }
        return venv;
    }

    private byte[] readBoundedBytes(java.io.InputStream is, int maxBytes) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int total = 0;
        int read;
        while ((read = is.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new RagQueryUnavailableException("Query embedding response exceeds " + maxBytes + " byte limit");
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }
}
