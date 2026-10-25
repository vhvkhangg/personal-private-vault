package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Loopback-only bounded transport. Only a typed, issued synthetic receipt may succeed.
 */
@Component
public class RagEngineClient {
    private final URI base;
    private final String key;
    private final HttpClient client;
    private final java.util.concurrent.ConcurrentHashMap<UUID, HttpClient> transports = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<UUID> stoppedTransports = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final ObjectMapper json = new ObjectMapper().enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private final Duration timeout;

    public RagEngineClient(@Value("${ppv.rag.engine.url:http://127.0.0.1:8001}") String url,
                           @Value("${ppv.rag.engine.service-key:}") String key,
                           @Value("${ppv.rag.engine.timeout-seconds:5}") int seconds) {
        base = URI.create(url);
        if (!"http".equals(base.getScheme()) || !Set.of("127.0.0.1", "[::1]", "::1").contains(base.getHost())
            || base.getUserInfo() != null || base.getQuery() != null || base.getFragment() != null
            || (base.getPath() != null && !base.getPath().isEmpty())) {
            throw new IllegalArgumentException("RAG engine requires an exact loopback HTTP origin");
        }
        this.key = key;
        timeout = Duration.ofSeconds(Math.clamp(seconds, 1, 10));
        client = HttpClient.newBuilder().connectTimeout(timeout).followRedirects(HttpClient.Redirect.NEVER)
            .proxy(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return List.of(Proxy.NO_PROXY);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, java.io.IOException failure) {
                }
            }).build();
    }

    private HttpRequest.Builder request(String path) {
        if (key == null || key.length() < 32) throw new IllegalStateException("RAG service credential is unavailable");
        return HttpRequest.newBuilder(base.resolve(path)).timeout(timeout)
            .header("X-RAG-Service-Key", key).header("Content-Type", "application/json");
    }

    public String triggerWorkerStage(RagWorkerGrant grant) {
        RagWorkerGrant.requireProvenConsumptionFence();
        throw new IllegalStateException("Unsupervised execution is prohibited");
    }

    public int port() { return base.getPort() == -1 ? 80 : base.getPort(); }
    public String serviceCredential() {
        if (key == null || key.length() < 32) throw new IllegalStateException("RAG service credential is unavailable");
        return key;
    }

    private synchronized HttpClient transport(UUID job) {
        if (stoppedTransports.contains(job)) throw new IllegalStateException("Worker transport is stopped");
        return transports.computeIfAbsent(job, ignored -> HttpClient.newBuilder().connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NEVER).proxy(new ProxySelector() {
                    @Override public List<Proxy> select(URI uri) { return List.of(Proxy.NO_PROXY); }
                    @Override public void connectFailed(URI uri, SocketAddress address, java.io.IOException failure) { }
                }).build());
    }

    public boolean isBoundReady(UUID job, UUID attempt, UUID nonce, long pid) {
        return isBoundReady(job,attempt,nonce,pid,timeout);
    }

    public boolean isBoundReady(UUID job, UUID attempt, UUID nonce, long pid, Duration budget) {
        try {
            var response=sendBounded(transport(job),request("/v1/worker/readiness")
                    .timeout(budget).header("X-RAG-Endpoint-Nonce",nonce.toString()).GET().build(),4096,budget);
            var node=json.readTree(response.body());
            return response.statusCode()==200 && node.isObject() && node.size()==7
                    && node.path("version").isTextual() && !node.path("version").asText().isBlank()
                    && "READY".equals(node.path("status").asText()) && "2.0".equals(node.path("contract_version").asText())
                    && node.path("process_id").isIntegralNumber() && node.path("process_id").longValue()==pid
                    && job.toString().equals(node.path("job_id").asText()) && attempt.toString().equals(node.path("attempt_id").asText())
                    && nonce.toString().equals(node.path("endpoint_nonce").asText());
        } catch(java.io.IOException | IllegalStateException failure) { return false; }
        catch(InterruptedException failure) { Thread.currentThread().interrupt(); return false; }
    }

    public boolean stopAndVerifyTransport(UUID job) {
        HttpClient active;
        synchronized(this) { stoppedTransports.add(job); active=transports.get(job); }
        if(active==null) return true;
        active.shutdownNow();
        try { return active.awaitTermination(Duration.ofMillis(250)); }
        catch(InterruptedException failure) { Thread.currentThread().interrupt(); return false; }
    }

    public synchronized void forgetTransport(UUID job) {
        HttpClient active=transports.get(job);
        if(active!=null && !active.isTerminated()) throw new IllegalStateException("HTTP sender termination is not proven");
        transports.remove(job); stoppedTransports.remove(job);
    }

    public String triggerBoundWorkerStage(RagWorkerGrant grant, UUID session, UUID nonce) {
        try {
            var body = new TreeMap<String, Object>();
            body.put("contract_version", "2.0");
            body.put("job_id", grant.jobId().toString());
            body.put("attempt_id", grant.attemptId().toString());
            body.put("lease_token", grant.token());
            body.put("stage_type", grant.stageType());
            body.put("payload", grant.payload());
            body.put("input_digest", grant.inputDigest());
            body.put("scope_digest", grant.scopeDigest());
            var response = sendBounded(transport(grant.jobId()),request("/v1/worker/execute")
                    .header("X-RAG-Job-Id",grant.jobId().toString()).header("X-RAG-Attempt-Id",grant.attemptId().toString())
                    .header("X-RAG-Lease-Token",grant.token()).header("X-RAG-Session-Id",session.toString())
                    .header("X-RAG-Endpoint-Nonce",nonce.toString())
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), 8192);
            {
                byte[] bytes = response.body();
                if (response.statusCode() != 200 || bytes.length > 8192)
                    throw new IllegalStateException("Worker execution unavailable or rejected");
                var root = json.readTree(bytes);
                var receipt = root.path("receipt");
                for (String field : List.of("contract_version", "job_id", "attempt_id", "status"))
                    if (!root.path(field).isTextual())
                        throw new IllegalStateException("Invalid worker result provenance");
                if (!receipt.isObject() || java.util.stream.StreamSupport.stream(receipt.spliterator(), false).anyMatch(value -> !value.isTextual()))
                    throw new IllegalStateException("Invalid worker result provenance");
                if (root.size() != 6 || !"2.0".equals(root.path("contract_version").asText())
                    || !grant.jobId().toString().equals(root.path("job_id").asText())
                    || !grant.attemptId().toString().equals(root.path("attempt_id").asText())
                    || !"STAGED".equals(root.path("status").asText())
                    || !root.path("staged_row_id").isIntegralNumber() || !root.path("staged_row_id").canConvertToLong()
                    || root.path("staged_row_id").longValue() <= 0
                    || receipt.size() != 9 || !"2.0".equals(receipt.path("contract_version").asText())
                    || !"STAGED".equals(receipt.path("status").asText())
                    || !"SYNTHETIC_DIGEST".equals(receipt.path("output_kind").asText())
                    || !grant.jobId().toString().equals(receipt.path("job_id").asText())
                    || !grant.attemptId().toString().equals(receipt.path("attempt_id").asText())
                    || !grant.stageType().equals(receipt.path("stage_type").asText())
                    || !grant.inputDigest().equals(receipt.path("input_digest").asText())
                    || !grant.scopeDigest().equals(receipt.path("scope_digest").asText())
                    || !grant.outputDigest().equals(receipt.path("output_digest").asText())) {
                    throw new IllegalStateException("Invalid worker result provenance");
                }
                return json.writeValueAsString(receipt);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Worker execution interrupted", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Worker execution unavailable", e);
        }
    }

    private HttpResponse<byte[]> sendBounded(HttpRequest request, int limit) throws java.io.IOException, InterruptedException {
        return sendBounded(client,request,limit);
    }

    private HttpResponse<byte[]> sendBounded(HttpClient transport, HttpRequest request, int limit) throws java.io.IOException, InterruptedException {
        return sendBounded(transport,request,limit,timeout);
    }

    private HttpResponse<byte[]> sendBounded(HttpClient transport, HttpRequest request, int limit, Duration budget) throws java.io.IOException, InterruptedException {
        var subscriber = new BoundedBody(limit);
        var exchange = transport.sendAsync(request, info -> subscriber);
        try {
            return exchange.get(Math.max(1,budget.toNanos()), java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException failure) {
            throw new java.io.IOException("Worker response unavailable");
        } finally {
            subscriber.cancel();
            exchange.cancel(true);
        }
    }

    /**
     * Includes the entire body in the deadline; slow/chunked responses cannot hang a guard.
     */
    private static final class BoundedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final int limit;
        private final java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        private final java.util.concurrent.CompletableFuture<byte[]> completed = new java.util.concurrent.CompletableFuture<>();
        private volatile java.util.concurrent.Flow.Subscription subscription;

        BoundedBody(int limit) {
            this.limit = limit;
        }

        @Override
        public java.util.concurrent.CompletionStage<byte[]> getBody() {
            return completed;
        }

        @Override
        public void onSubscribe(java.util.concurrent.Flow.Subscription incoming) {
            subscription = incoming;
            if (completed.isDone()) incoming.cancel();
            else incoming.request(1);
        }

        @Override
        public void onNext(List<java.nio.ByteBuffer> buffers) {
            for (var buffer : buffers) {
                if (buffer.remaining() > limit - bytes.size()) {
                    completed.completeExceptionally(new java.io.IOException("Worker response byte budget exceeded"));
                    cancel();
                    return;
                }
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk);
                bytes.writeBytes(chunk);
            }
            var current = subscription;
            if (current != null) current.request(1);
        }

        @Override
        public void onError(Throwable failure) {
            completed.completeExceptionally(failure);
        }

        @Override
        public void onComplete() {
            completed.complete(bytes.toByteArray());
        }

        void cancel() {
            completed.cancel(true);
            var current = subscription;
            if (current != null) current.cancel();
        }
    }

    public boolean isJobQuiescent(UUID job) {
        try {
            var response = sendBounded(request("/quiescence?job_id=" + job).GET().build(), 4096);
            {
                byte[] bytes = response.body();
                if (response.statusCode() != 200 || bytes.length > 4096) return false;
                var node = json.readTree(bytes);
                return node.path("quiescent").isBoolean() && node.path("quiescent").booleanValue()
                    && job.toString().equals(node.path("job_id").asText())
                    && node.path("cleanup_verified").isBoolean() && node.path("cleanup_verified").booleanValue()
                    && node.path("active_workers").isIntegralNumber() && node.path("active_workers").canConvertToLong()
                    && node.path("active_workers").longValue() == 0;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (java.io.IOException | IllegalStateException e) {
            return false;
        }
    }

    public boolean cancelWorkerJob(UUID job) {
        try {
            var response = sendBounded(request("/v1/worker/cancel").POST(HttpRequest.BodyPublishers.ofString(
                json.writeValueAsString(Map.of("job_id", job.toString())))).build(), 4096);
            {
                byte[] bytes = response.body();
                if (response.statusCode() != 200 || bytes.length > 4096) return false;
                var node = json.readTree(bytes);
                return "CANCELLED".equals(node.path("status").asText()) && job.toString().equals(node.path("job_id").asText());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (java.io.IOException | IllegalStateException e) {
            return false;
        }
    }

    public String triggerBoundWorkerBatch(RagWorkerGrant grant, UUID session, UUID nonce, List<Map<String, Object>> rootTasks) {
        try {
            var body = new TreeMap<String, Object>();
            body.put("contract_version", "3.0");
            body.put("job_id", grant.jobId().toString());
            body.put("attempt_id", grant.attemptId().toString());
            body.put("lease_token", grant.token());
            body.put("input_digest", grant.inputDigest());
            body.put("scope_digest", grant.scopeDigest());
            body.put("roots", rootTasks);

            // Computation may use the existing per-root 60s budget; control/v2 requests remain short.
            Duration executionBudget = Duration.ofSeconds(Math.min(900, 15L + 60L * rootTasks.size()));
            var response = sendBounded(transport(grant.jobId()), request("/v1/worker/execute-batch").timeout(executionBudget)
                    .header("X-RAG-Job-Id", grant.jobId().toString())
                    .header("X-RAG-Attempt-Id", grant.attemptId().toString())
                    .header("X-RAG-Lease-Token", grant.token())
                    .header("X-RAG-Session-Id", session.toString())
                    .header("X-RAG-Endpoint-Nonce", nonce.toString())
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), 33554432, executionBudget);

            byte[] bytes = response.body();
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Worker batch execution unavailable or rejected, status=" + response.statusCode());
            }
            var root = json.readTree(bytes);
            if (!"3.0".equals(root.path("contract_version").asText())
                    || !grant.jobId().toString().equals(root.path("job_id").asText())
                    || !grant.attemptId().toString().equals(root.path("attempt_id").asText())
                    || !"STAGED".equals(root.path("status").asText())) {
                throw new IllegalStateException("Invalid worker batch result provenance");
            }
            return json.writeValueAsString(root);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Worker execution interrupted", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Worker execution unavailable", e);
        }
    }
}
