package com.vhvkhangg.personalprivatevault.rag.internal.application.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRetrievalReader;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRetrievalReaderRepository;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not enable or deploy.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 */
@ConditionalOnProperty(name = "ppv.rag.test-scoped-execution", havingValue = "true")
@Service
public class RagQaService implements RagQaOperations {

    private static final Logger log = LoggerFactory.getLogger(RagQaService.class);
    private static final int MAX_RESPONSE_BYTES = 128 * 1024; // 128 KiB

    private final RagRetrievalService ragRetrievalService;
    private final RagRetrievalAuthority ragRetrievalAuthority;
    private final RagRetrievalReaderRepository ragRetrievalReaderRepository;
    private final RagQaSupervisor ragQaSupervisor;
    private final RagQaValidator ragQaValidator;
    private final RagQaContextPacker ragQaContextPacker;
    private final RagRuntimeIdentity ragRuntimeIdentity;

    private final boolean qaEnabled;
    private final boolean retrievalEnabled;
    private final boolean testScopedExecution;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<UUID, AsyncQaSenderContext> activeSenders = new ConcurrentHashMap<>();

    private static final class AsyncQaSenderContext {
        final UUID readerId;
        final UUID reservationToken;
        final CountDownLatch completionLatch = new CountDownLatch(1);
        final CountDownLatch contextCompleted = new CountDownLatch(1);
        final CountDownLatch outputCompleted = new CountDownLatch(1);
        final AtomicInteger outputCallbacks = new AtomicInteger();
        volatile boolean outputClosed;
        volatile boolean outputRegistered;
        volatile boolean errorOutput;
        volatile boolean transportAborted;
        volatile WriteListener outputListener;
        volatile byte[] responseBody;
        boolean detached;
        volatile boolean stopRequested = false;
        volatile boolean cancelled = false;
        volatile boolean producerReturned = false;
        volatile boolean emitted = false;
        volatile Map<String, Object> nativeMetrics = Map.of();
        final long startedNanos = System.nanoTime();
        final AtomicLong stopBy = new AtomicLong();
        volatile boolean finished = false;
        volatile Thread senderThread;
        final AsyncContext asyncContext;

        AsyncQaSenderContext(UUID readerId, UUID reservationToken, AsyncContext asyncContext) {
            this.readerId = readerId;
            this.reservationToken = reservationToken;
            this.asyncContext = asyncContext;
        }

        long stopDeadline() {
            stopBy.compareAndSet(0, System.nanoTime() + TimeUnit.SECONDS.toNanos(5));
            return stopBy.get();
        }
    }

    public RagQaService(
            RagRetrievalService ragRetrievalService,
            RagRetrievalAuthority ragRetrievalAuthority,
            RagRetrievalReaderRepository ragRetrievalReaderRepository,
            RagQaSupervisor ragQaSupervisor,
            RagQaValidator ragQaValidator,
            RagQaContextPacker ragQaContextPacker,
            RagRuntimeIdentity ragRuntimeIdentity,
            @Value("${ppv.rag.qa.enabled:false}") boolean qaEnabled,
            @Value("${ppv.rag.retrieval.enabled:false}") boolean retrievalEnabled,
            @Value("${ppv.rag.test-scoped-execution:false}") boolean testScopedExecution
    ) {
        this.ragRetrievalService = ragRetrievalService;
        this.ragRetrievalAuthority = ragRetrievalAuthority;
        this.ragRetrievalReaderRepository = ragRetrievalReaderRepository;
        this.ragQaSupervisor = ragQaSupervisor;
        this.ragQaValidator = ragQaValidator;
        this.ragQaContextPacker = ragQaContextPacker;
        this.ragRuntimeIdentity = ragRuntimeIdentity;
        this.qaEnabled = qaEnabled;
        this.retrievalEnabled = retrievalEnabled;
        this.testScopedExecution = testScopedExecution;
    }

    public boolean isEnabled() {
        return qaEnabled || testScopedExecution;
    }

    public boolean hasActiveSender() {
        return !activeSenders.isEmpty();
    }

    public boolean hasSender(UUID readerId) {
        return activeSenders.containsKey(readerId);
    }

    public boolean isQuiescentReader(UUID readerId) {
        var ctx = activeSenders.get(readerId);
        return ctx != null && ctx.producerReturned && ctx.completionLatch.getCount() == 0
                && ctx.contextCompleted.getCount() == 0 && ctx.outputCompleted.getCount() == 0
                && ctx.outputCallbacks.get() == 0;
    }

    public void reconcileCompletedSenders() {
        for (var ctx : activeSenders.values()) {
            if (!isQuiescentReader(ctx.readerId)) continue;
            try {
                ragQaSupervisor.stopWorker(ctx.readerId);
                discardOutput(ctx);
                finishQuiescentSender(ctx);
            } catch (RuntimeException unknown) {
                log.warn("Owned QA finish remains unproven; exclusion retained");
            }
        }
    }

    @Override
    public RagQaResponse answer(RagQaCommand command) {
        if (!isEnabled()) throw new RagQueryUnavailableException("RAG QA is disabled");
        validateCommand(command);
        throw new RagQueryUnavailableException("QA requires an owned asynchronous sender");
    }

    @Override
    public RagQaCancelResult cancel(UUID requestId) {
        if (requestId == null) throw new IllegalArgumentException("requestId must not be null");
        AsyncQaSenderContext ctx = activeSenders.get(requestId);
        var initial = ragRetrievalReaderRepository.findByReaderId(requestId);
        if (initial.isEmpty() && ctx == null) throw new NoSuchElementException("RAG resource unavailable");
        if (initial.isPresent() && "TERMINAL".equals(initial.get().getState())) {
            return new RagQaCancelResult(requestId, initial.get().getState(), "Reader is already terminal");
        }
        if (ctx != null) {
            ctx.cancelled = true;
            ctx.stopRequested = true;
            if (ctx.senderThread != null) {
                ctx.senderThread.interrupt();
            }
        }
        long stopBy = ctx != null ? ctx.stopDeadline() : System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        RuntimeException stopFailure = null;
        try {
            ragRetrievalAuthority.requestStop(requestId, "USER_CANCELLED");
        } catch (RuntimeException unknownCommit) {
            stopFailure = unknownCommit;
        }
        if (ctx == null) throw new RagQueryUnavailableException("Reader stop requires recovery");
        try {
            ragQaSupervisor.stopWorker(requestId, stopBy);
        } catch (RuntimeException unknownStop) {
            stopFailure = unknownStop;
        } finally {
            stopOrReply(ctx);
        }
        if (stopFailure != null) throw new RagQueryUnavailableException("Reader stop remains unproven");
        try {
            if (!ctx.completionLatch.await(Math.max(0, stopBy - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                throw new RagQueryUnavailableException("Reader quiescence is not proven");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RagQueryUnavailableException("Reader stop interrupted");
        }
        RagRetrievalReader reader = ragRetrievalReaderRepository.findByReaderId(requestId).orElseThrow();
        if (!"TERMINAL".equals(reader.getState())) {
            throw new RagQueryUnavailableException("Reader finish requires recovery");
        }
        return new RagQaCancelResult(requestId, reader.getFinishDisposition(), "QA reader stopped");
    }

    public void dispatchAsync(RagQaCommand command, AsyncContext asyncContext, Function<RagQaResponse, ?> wireAdapter) {
        if (!isEnabled()) {
            handleAsyncFailure(asyncContext, new RagQueryUnavailableException("RAG QA is disabled"));
            return;
        }
        try {
            validateCommand(command);
        } catch (RuntimeException invalid) {
            handleAsyncFailure(asyncContext, invalid);
            return;
        }
        if (ragRetrievalReaderRepository.findByReaderId(command.requestId()).isPresent()) {
            handleAsyncFailure(asyncContext, new IllegalArgumentException("Request identity was already used"));
            return;
        }

        var ctx = new AsyncQaSenderContext(command.requestId(), UUID.randomUUID(), asyncContext);
        boolean busy;
        synchronized (activeSenders) {
            busy = !activeSenders.isEmpty() || ragRetrievalService.hasActiveSender();
            if (!busy) activeSenders.put(command.requestId(), ctx);
        }
        if (busy) {
            handleAsyncFailure(asyncContext, new RagReaderBusyException());
            return;
        }

        asyncContext.addListener(new AsyncListener() {
            @Override public void onComplete(AsyncEvent event) { ctx.contextCompleted.countDown(); }
            @Override public void onTimeout(AsyncEvent event) { stop("TIMEOUT", false); }
            @Override public void onError(AsyncEvent event) { stop("TRANSPORT_ERROR", true); }
            @Override public void onStartAsync(AsyncEvent event) { ctx.stopRequested = true; }

            private void stop(String reason, boolean transportFailure) {
                ctx.stopRequested = true;
                if (transportFailure) {
                    ctx.transportAborted = true;
                    stopOutput(ctx);
                }
                long stopBy = ctx.stopDeadline();
                try {
                    ragRetrievalAuthority.requestStop(ctx.readerId, reason);
                    ragQaSupervisor.stopWorker(ctx.readerId, stopBy);
                } catch (RuntimeException unknownStop) {
                    log.warn("QA stop remains unproven");
                } finally {
                    stopOrReply(ctx);
                    if (!transportFailure) finishDeadlineOutput(ctx);
                }
            }
        });

        try {
            initializeOutput(ctx);
        } catch (IOException | RuntimeException failed) {
            ctx.stopRequested = true;
            ctx.transportAborted = true;
            stopOutput(ctx);
            completeContext(ctx);
        }

        Thread.ofVirtual().start(() -> runQaSender(command, ctx, wireAdapter));
    }

    private void runQaSender(RagQaCommand command, AsyncQaSenderContext ctx, Function<RagQaResponse, ?> wireAdapter) {
        ctx.senderThread = Thread.currentThread();
        byte[] body = null;
        try {
            if (ctx.stopRequested) throw new RagRetrievalCancelledException();

            RagRetrievalService.PlanExecution plan = ragRetrievalService.prepareAndAdmit(command.toRetrievalCommand(), ctx.reservationToken);
            boolean admitted = !plan.targets().isEmpty();

            RagQaResponse qaResponse;
            if (ragQaSupervisor.hasGenerationOverride()) {
                RagQaSupervisor.RawCandidateGeneration rawGen = ragQaSupervisor.applyGenerationOverride(command.requestId(), command);
                if (ctx.stopRequested) throw new RagRetrievalCancelledException();
                var validated = ragQaValidator.validate(
                        command.requestId(),
                        rawGen.outcome(),
                        rawGen.claims(),
                        rawGen.unansweredParts(),
                        rawGen.conflictPairs(),
                        List.of(),
                        command.responseLanguage()
                );
                qaResponse = new RagQaResponse(
                        command.requestId(),
                        validated.outcome(),
                        validated.answer(),
                        validated.claims(),
                        validated.unansweredParts(),
                        validated.conflictPairs(),
                        validated.citations()
                );
            } else if (!admitted) {
                var validated = ragQaValidator.validate(
                        command.requestId(),
                        "ABSTAIN",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        command.responseLanguage()
                );
                qaResponse = new RagQaResponse(
                        command.requestId(),
                        validated.outcome(),
                        validated.answer(),
                        validated.claims(),
                        validated.unansweredParts(),
                        validated.conflictPairs(),
                        validated.citations()
                );
            } else {
                if (ctx.stopRequested) throw new RagRetrievalCancelledException();

                String combinedQuery = ragQaContextPacker.buildQueryEmbeddingText(command.question(), command.history());
                float[] vector;
                RagQaSupervisor.QaSession session = null;

                if (ragQaSupervisor.hasGenerationOverride()) {
                    vector = ragQaSupervisor.applyVectorOverride(command.requestId(), combinedQuery);
                    if (vector == null) {
                        float[] v = new float[384];
                        Arrays.fill(v, 0.051f);
                        vector = v;
                    }
                } else {
                    session = ragQaSupervisor.startSession(command.requestId());
                    vector = ragQaSupervisor.computeEmbedding(session, combinedQuery);
                }

                if (ctx.stopRequested) throw new RagRetrievalCancelledException();

                List<RagRetrievalMatch> matches = ragRetrievalService.searchAndVerifyMatches(
                        plan,
                        ragQaSupervisor.formatVectorString(vector),
                        plan.topK()
                );


                if (matches.isEmpty()) {
                    ragQaSupervisor.stopWorker(command.requestId(), ctx.stopDeadline());
                    var validated = ragQaValidator.validate(
                            command.requestId(),
                            "ABSTAIN",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of(),
                            command.responseLanguage()
                    );
                    qaResponse = new RagQaResponse(
                            command.requestId(),
                            validated.outcome(),
                            validated.answer(),
                            validated.claims(),
                            validated.unansweredParts(),
                            validated.conflictPairs(),
                            validated.citations()
                    );
                } else {
                    if (ctx.stopRequested) throw new RagRetrievalCancelledException();

                    List<RagRetrievalMatch> packedMatches = ragQaContextPacker.packEvidence(command.question(), command.history(), matches);

                    RagQaSupervisor.RawCandidateGeneration rawGen;
                    if (ragQaSupervisor.hasGenerationOverride()) {
                        rawGen = ragQaSupervisor.applyGenerationOverride(command.requestId(), command);
                    } else {
                        rawGen = ragQaSupervisor.generateCandidate(
                                session,
                                command.question(),
                                command.history(),
                                command.responseLanguage(),
                                packedMatches,
                                null
                        );
                    }

                    // Positively terminate child worker before emission
                    ragQaSupervisor.stopWorker(command.requestId(), ctx.stopDeadline());
                    var usage = ragQaSupervisor.resourceUsage(command.requestId());
                    ctx.nativeMetrics = Map.of(
                            "peak_memory_bytes", usage != null ? usage.peakProcessMemoryBytes() : 0L
                    );

                    var validated = ragQaValidator.validate(
                            command.requestId(),
                            rawGen.outcome(),
                            rawGen.claims(),
                            rawGen.unansweredParts(),
                            rawGen.conflictPairs(),
                            packedMatches,
                            command.responseLanguage()
                    );

                    qaResponse = new RagQaResponse(
                            command.requestId(),
                            validated.outcome(),
                            validated.answer(),
                            validated.claims(),
                            validated.unansweredParts(),
                            validated.conflictPairs(),
                            validated.citations()
                    );
                }
            }

            body = objectMapper.writeValueAsBytes(ApiResponses.success(wireAdapter.apply(qaResponse)));
            if (body.length > MAX_RESPONSE_BYTES) {
                throw new RagQueryUnavailableException("QA response exceeds 128 KiB protocol limit");
            }

            if (ctx.stopRequested) throw new RagRetrievalCancelledException();

            if (admitted) {
                ragRetrievalAuthority.beginPreparedEmission(command.requestId(), plan.proofs());
            } else {
                ragRetrievalAuthority.beginEmission(command.requestId());
            }

            startOutput(ctx, body, HttpServletResponse.SC_OK, true);
            body = null;
            long remaining = TimeUnit.SECONDS.toNanos(240) - (System.nanoTime() - ctx.startedNanos);
            if (!ctx.outputCompleted.await(Math.max(0, remaining), TimeUnit.NANOSECONDS)) {
                throw new RagQueryUnavailableException("QA output completion remains unproven");
            }

        } catch (Exception failed) {
            log.warn("QA sender failed; category={}", failed.getClass().getSimpleName());
            try {
                if (ctx.stopRequested) {
                    stopOrReply(ctx);
                } else if (!ctx.outputRegistered) {
                    var failure = failureResponse(failed);
                    startOutput(ctx, objectMapper.writeValueAsBytes(failure.envelope()), failure.status(), false);
                }
                if (ctx.errorOutput && !ctx.transportAborted
                        && !ctx.outputCompleted.await(Math.max(0, ctx.stopDeadline() - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                    ctx.transportAborted = true;
                }
            } catch (Exception unavailable) {
                ctx.stopRequested = true;
                ctx.transportAborted = true;
            }
        } finally {
            if (body != null) Arrays.fill(body, (byte) 0);
            body = null;
            stopOutput(ctx);
            ctx.emitted = ctx.emitted && !ctx.stopRequested;
            completeContext(ctx);
            try {
                long stopBy = ctx.stopDeadline();
                ragQaSupervisor.stopWorker(ctx.readerId, stopBy);
                if (!ctx.outputCompleted.await(Math.max(0, stopBy - System.nanoTime()), TimeUnit.NANOSECONDS)
                        || ctx.outputCallbacks.get() != 0) {
                    throw new RagQueryUnavailableException("Output callback remains active");
                }
                if (!ctx.contextCompleted.await(Math.max(0, stopBy - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                    throw new RagQueryUnavailableException("Container completion remains unproven");
                }
                discardOutput(ctx);
                finishQuiescentSender(ctx);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                log.warn("QA finish interrupted; exclusion retained");
            } catch (RuntimeException uncertainFinish) {
                Throwable cause = uncertainFinish;
                while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
                log.warn("QA finish unproven; exclusion retained; category={}", cause.getClass().getSimpleName());
            } finally {
                ctx.senderThread = null;
                ctx.producerReturned = true;
                ctx.completionLatch.countDown();
            }
        }
    }

    private void finishQuiescentSender(AsyncQaSenderContext ctx) {
        String state = ragRetrievalAuthority.observe(ctx.readerId, ctx.reservationToken);
        if (state == null) {
            ragRetrievalAuthority.admitEmpty(ctx.readerId, ctx.reservationToken, ragRuntimeIdentity.instance().toString());
            state = "ADMITTED";
        }
        if (!"FOREIGN".equals(state) && !"TERMINAL".equals(state)) {
            var evidence = new LinkedHashMap<String, Object>(ctx.nativeMetrics);
            evidence.put("reason", "OWNED_SENDER_JOINED");
            evidence.put("sender_quiescent", true);
            evidence.put("context_complete", true);
            evidence.put("native_quiescent", true);
            evidence.put("transport_quiescent", true);
            evidence.put("buffers_discarded", true);
            evidence.put("request_total_nanos", System.nanoTime() - ctx.startedNanos);
            evidence.put("at", Instant.now().toString());
            ragRetrievalAuthority.finish(
                    ctx.readerId,
                    ctx.reservationToken,
                    ctx.cancelled ? "CANCELLED" : ctx.emitted ? "COMPLETED" : "EXECUTION_FAILURE",
                    ctx.emitted ? "EMITTED" : "ABORTED",
                    evidence,
                    null,
                    null
            );
        }
        ctx.finished = true;
        ragQaSupervisor.forgetStopped(ctx.readerId);
        activeSenders.remove(ctx.readerId, ctx);
    }

    private void startOutput(AsyncQaSenderContext ctx, byte[] body, int status, boolean success) throws IOException {
        synchronized (ctx) {
            if (ctx.outputRegistered || ctx.outputClosed || ctx.transportAborted || (success && ctx.stopRequested)) {
                Arrays.fill(body, (byte) 0);
                return;
            }
            var response = (HttpServletResponse) ctx.asyncContext.getResponse();
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.setContentLength(body.length);
            ctx.errorOutput = !success;
            ctx.responseBody = body;
            ctx.outputRegistered = true;
        }
        var listener = ctx.outputListener;
        if (listener == null) throw new IOException("Response readiness owner unavailable");
        listener.onWritePossible();
    }

    private void initializeOutput(AsyncQaSenderContext ctx) throws IOException {
        ServletOutputStream output = ((HttpServletResponse) ctx.asyncContext.getResponse()).getOutputStream();
        var listener = new WriteListener() {
            private int offset;
            private final AtomicInteger writeSignals = new AtomicInteger();

            @Override public void onWritePossible() {
                ctx.outputCallbacks.incrementAndGet();
                try {
                    if (writeSignals.getAndIncrement() != 0) return;
                    int pending = 1;
                    do {
                        try {
                            while (!ctx.outputClosed && (!ctx.stopRequested || ctx.errorOutput)) {
                                byte[] body = ctx.responseBody;
                                if (body == null) break;
                                if (!output.isReady()) break;
                                if (offset < body.length) {
                                    int length = Math.min(4096, body.length - offset);
                                    output.write(body, offset, length);
                                    offset += length;
                                } else {
                                    output.flush();
                                    ctx.emitted = !ctx.errorOutput;
                                    stopOutput(ctx);
                                    completeContext(ctx);
                                    break;
                                }
                            }
                        } catch (Exception failed) {
                            onError(failed);
                        }
                        pending = writeSignals.addAndGet(-pending);
                    } while (pending != 0);
                } finally {
                    if (ctx.outputCallbacks.decrementAndGet() == 0 && ctx.outputClosed) {
                        ctx.outputCompleted.countDown();
                        if (ctx.detached) discardOutput(ctx);
                    }
                }
            }

            @Override public void onError(Throwable t) {
                ctx.transportAborted = true;
                stopOutput(ctx);
                completeContext(ctx);
            }
        };

        synchronized (ctx) {
            ctx.outputListener = listener;
        }
        output.setWriteListener(listener);
    }

    private void stopOutput(AsyncQaSenderContext ctx) {
        synchronized (ctx) {
            ctx.outputClosed = true;
            if (ctx.outputCallbacks.get() == 0) ctx.outputCompleted.countDown();
        }
    }

    private void discardOutput(AsyncQaSenderContext ctx) {
        synchronized (ctx) {
            ctx.detached = true;
            if (ctx.outputCallbacks.get() != 0) throw new IllegalStateException("Output remains active");
            byte[] bytes = ctx.responseBody;
            ctx.responseBody = null;
            if (bytes != null) Arrays.fill(bytes, (byte) 0);
        }
    }

    private void stopOrReply(AsyncQaSenderContext ctx) {
        boolean reply;
        synchronized (ctx) {
            if (!ctx.outputRegistered && !ctx.outputClosed && !ctx.transportAborted && ctx.outputListener == null) return;
            reply = !ctx.outputRegistered && !ctx.outputClosed && !ctx.transportAborted
                    && !((HttpServletResponse) ctx.asyncContext.getResponse()).isCommitted();
            if (ctx.errorOutput && !ctx.transportAborted) return;
            if (!reply) stopOutput(ctx);
        }
        if (reply) {
            try {
                var failure = failureResponse(ctx.cancelled
                        ? new RagRetrievalCancelledException() : new RagQueryUnavailableException("QA stopped"));
                startOutput(ctx, objectMapper.writeValueAsBytes(failure.envelope()), failure.status(), false);
                return;
            } catch (Exception unavailable) {
                ctx.transportAborted = true;
                stopOutput(ctx);
            }
        }
        completeContext(ctx);
    }

    private void completeContext(AsyncQaSenderContext ctx) {
        try {
            if (ctx.stopRequested && (!ctx.errorOutput || ctx.transportAborted) && ctx.outputCallbacks.get() == 0) {
                var response = (HttpServletResponse) ctx.asyncContext.getResponse();
                if (!response.isCommitted()) {
                    response.resetBuffer();
                    response.setContentLength(0);
                    response.setStatus(ctx.cancelled ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    response.setHeader("Cache-Control", "no-store");
                }
            }
            ctx.asyncContext.complete();
        } catch (IllegalStateException alreadyCompleting) {
            // complete() returning or throwing is not proof; await the container event.
        }
    }

    private void finishDeadlineOutput(AsyncQaSenderContext ctx) {
        var listener = ctx.errorOutput ? ctx.outputListener : null;
        if (listener != null && !ctx.outputClosed) {
            try {
                listener.onWritePossible();
            } catch (IOException failed) {
                listener.onError(failed);
            }
        }
        if (!ctx.outputClosed) {
            ctx.transportAborted = true;
            stopOutput(ctx);
        }
        completeContext(ctx);
    }

    private record FailureResponse(int status, com.vhvkhangg.personalprivatevault.ApiResponse<Void> envelope) {}

    private FailureResponse failureResponse(Throwable e) {
        int status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        String code = "RAG_QUERY_UNAVAILABLE";
        String msg = "QA service unavailable";

        if (e instanceof RagReaderBusyException) {
            status = HttpServletResponse.SC_CONFLICT;
            code = "RAG_READER_BUSY";
            msg = "A retrieval or QA request is already active in this single-user vault";
        } else if (e instanceof RagRetrievalCancelledException) {
            status = HttpServletResponse.SC_CONFLICT;
            code = "RAG_RETRIEVAL_CANCELLED";
            msg = "QA cancelled before response emission";
        } else if (e instanceof RagQueryScopeLimitException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            code = "RAG_QUERY_SCOPE_LIMIT";
            msg = "Complete query scope exceeds the maximum allowable document limit";
        } else if (e instanceof NoSuchElementException) {
            status = HttpServletResponse.SC_NOT_FOUND;
            code = "RAG_NOT_FOUND";
            msg = "RAG resource unavailable";
        } else if (e instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            code = "RAG_BAD_REQUEST";
            msg = "Invalid RAG request";
        }

        return new FailureResponse(status, ApiResponses.error(code, msg));
    }

    private void handleAsyncFailure(AsyncContext asyncContext, Throwable e) {
        log.warn("QA async failure; category={}", e.getClass().getSimpleName());
        try {
            var response = (HttpServletResponse) asyncContext.getResponse();
            if (!response.isCommitted()) {
                FailureResponse failure = failureResponse(e);
                response.setStatus(failure.status());
                response.setContentType("application/json;charset=UTF-8");
                response.setHeader("Cache-Control", "no-store");
                byte[] bytes = objectMapper.writeValueAsBytes(failure.envelope());
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes);
                response.getOutputStream().flush();
            }
        } catch (IOException ignored) {
        } finally {
            try {
                asyncContext.complete();
            } catch (IllegalStateException ignored) {}
        }
    }

    private void validateCommand(RagQaCommand command) {
        if (command == null) throw new IllegalArgumentException("command must not be null");
        if (command.requestId() == null) throw new IllegalArgumentException("requestId must not be null");
        if (command.question() == null || command.question().isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        int cp = command.question().codePointCount(0, command.question().length());
        if (cp > 4096) throw new IllegalArgumentException("question exceeds 4096 code points limit");
        if (command.topK() != null && (command.topK() < 1 || command.topK() > 50)) {
            throw new IllegalArgumentException("topK must be between 1 and 50");
        }
        if (command.responseLanguage() != null && !command.responseLanguage().isBlank()) {
            String lang = command.responseLanguage().trim().toLowerCase();
            if (!Set.of("vi", "en", "zh", "zh-hans", "zh-hant").contains(lang)) {
                throw new IllegalArgumentException("responseLanguage must be vi, en, zh, zh-Hans, or zh-Hant");
            }
        }
        if (command.history() != null) {
            if (command.history().size() > 4) {
                throw new IllegalArgumentException("History exceeds maximum allowed turns (4)");
            }
            int totalCp = 0;
            for (RagQaHistoryEntry h : command.history()) {
                if (h == null) throw new IllegalArgumentException("History entry must not be null");
                String role = h.role();
                if (!"user".equalsIgnoreCase(role) && !"assistant".equalsIgnoreCase(role)) {
                    throw new IllegalArgumentException("History role must be user or assistant");
                }
                String content = h.content();
                if (content != null) {
                    int c = content.codePointCount(0, content.length());
                    if (c > 1024) throw new IllegalArgumentException("History turn exceeds 1024 code points");
                    totalCp += c;
                }
            }
            if (totalCp > 3072) {
                throw new IllegalArgumentException("History total content exceeds 3072 code points");
            }
        }
    }
}
