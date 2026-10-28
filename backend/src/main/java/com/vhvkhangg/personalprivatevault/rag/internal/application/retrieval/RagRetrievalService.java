package com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RagRetrievalService implements RagRetrievalOperations {

    private static final Logger log = LoggerFactory.getLogger(RagRetrievalService.class);

    private static final int MAX_QUERY_CODE_POINTS = 4096;
    private static final int MAX_UNION_SOURCES = 100;
    private static final int MAX_EXCERPT_CODE_POINTS = 12000;
    private static final int MAX_RESPONSE_BYTES = 128 * 1024; // 128 KiB
    private static final int MAX_SCANNED_CHUNKS = 10000;

    private final RagSourceRepository ragSourceRepository;
    private final RagExternalFileSourceRepository ragExternalFileSourceRepository;
    private final RagIndexGenerationRepository ragIndexGenerationRepository;
    private final RagDocumentChunkRepository ragDocumentChunkRepository;
    private final RagRetrievalReaderRepository ragRetrievalReaderRepository;
    private final RagRetrievalInspectionRepository ragRetrievalInspectionRepository;
    private final RagDependencyRepository ragDependencyRepository;
    private final RagRetrievalAuthority ragRetrievalAuthority;
    private final RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor;
    private final RagOriginalBytesStore ragOriginalBytesStore;
    private final RagRuntimeIdentity ragRuntimeIdentity;
    private final RagRetrievalCurrentAuthority currentAuthority;
    private final boolean retrievalEnabled;
    private final boolean testScopedExecution;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Active async senders for cancellation and serialized completion coordination
    private final Map<UUID, AsyncSenderContext> activeSenders = new ConcurrentHashMap<>();

    private static final class AsyncSenderContext {
        final UUID readerId;
        final UUID reservationToken;
        final CountDownLatch completionLatch = new CountDownLatch(1);
        final CountDownLatch contextCompleted = new CountDownLatch(1);
        final CountDownLatch outputCompleted = new CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicInteger outputCallbacks = new java.util.concurrent.atomic.AtomicInteger();
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
        volatile Map<String,Object> nativeMetrics = Map.of();
        final long startedNanos = System.nanoTime();
        final java.util.concurrent.atomic.AtomicLong stopBy = new java.util.concurrent.atomic.AtomicLong();
        volatile boolean finished = false;
        final AsyncContext asyncContext;

        AsyncSenderContext(UUID readerId, UUID reservationToken, AsyncContext asyncContext) {
            this.readerId = readerId;
            this.reservationToken = reservationToken;
            this.asyncContext = asyncContext;
        }

        long stopDeadline() {
            stopBy.compareAndSet(0,System.nanoTime()+TimeUnit.SECONDS.toNanos(5));
            return stopBy.get();
        }
    }

    public RagRetrievalService(
            RagSourceRepository ragSourceRepository,
            RagExternalFileSourceRepository ragExternalFileSourceRepository,
            RagIndexGenerationRepository ragIndexGenerationRepository,
            RagDocumentChunkRepository ragDocumentChunkRepository,
            RagRetrievalReaderRepository ragRetrievalReaderRepository,
            RagRetrievalInspectionRepository ragRetrievalInspectionRepository,
            RagDependencyRepository ragDependencyRepository,
            RagRetrievalAuthority ragRetrievalAuthority,
            RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor,
            RagOriginalBytesStore ragOriginalBytesStore,
            RagRuntimeIdentity ragRuntimeIdentity,
            RagRetrievalCurrentAuthority currentAuthority,
            @Value("${ppv.rag.retrieval.enabled:false}") boolean retrievalEnabled,
            @Value("${ppv.rag.test-scoped-execution:false}") boolean testScopedExecution
    ) {
        this.ragSourceRepository = ragSourceRepository;
        this.ragExternalFileSourceRepository = ragExternalFileSourceRepository;
        this.ragIndexGenerationRepository = ragIndexGenerationRepository;
        this.ragDocumentChunkRepository = ragDocumentChunkRepository;
        this.ragRetrievalReaderRepository = ragRetrievalReaderRepository;
        this.ragRetrievalInspectionRepository = ragRetrievalInspectionRepository;
        this.ragDependencyRepository = ragDependencyRepository;
        this.ragRetrievalAuthority = ragRetrievalAuthority;
        this.ragQueryEmbeddingSupervisor = ragQueryEmbeddingSupervisor;
        this.ragOriginalBytesStore = ragOriginalBytesStore;
        this.ragRuntimeIdentity = ragRuntimeIdentity;
        this.currentAuthority = currentAuthority;
        this.retrievalEnabled = retrievalEnabled;
        this.testScopedExecution = testScopedExecution;
    }

    private boolean isEnabled() {
        return retrievalEnabled || testScopedExecution;
    }

    @Override
    public RagRetrievalResponse retrieve(RagRetrievalCommand command) {
        if (!isEnabled()) throw new RagQueryUnavailableException("RAG retrieval is disabled");
        validateCommand(command);
        // Returning evidence would create an unowned consumer after reservation release.
        throw new RagQueryUnavailableException("Retrieval requires an owned asynchronous sender");
    }

    @Override
    public RagRetrievalCancelResult cancel(UUID requestId) {
        if (requestId == null) throw new IllegalArgumentException("requestId must not be null");
        AsyncSenderContext ctx = activeSenders.get(requestId);
        var initial = ragRetrievalReaderRepository.findByReaderId(requestId);
        if (initial.isEmpty() && ctx==null) throw new NoSuchElementException("RAG resource unavailable");
        if (initial.isPresent() && "TERMINAL".equals(initial.get().getState())) {
            return new RagRetrievalCancelResult(requestId, initial.get().getState(), "Reader is already terminal");
        }
        if(ctx!=null) { ctx.cancelled=true; ctx.stopRequested=true; }
        long stopBy=ctx!=null?ctx.stopDeadline():System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
        RuntimeException stopFailure = null;
        try { ragRetrievalAuthority.requestStop(requestId, "USER_CANCELLED"); }
        catch (RuntimeException unknownCommit) { stopFailure = unknownCommit; }
        if (ctx == null) throw new RagQueryUnavailableException("Reader stop requires recovery");
        try { ragQueryEmbeddingSupervisor.stopWorker(requestId,stopBy); }
        catch (RuntimeException unknownStop) { stopFailure = unknownStop; }
        finally { stopOrReply(ctx); }
        if (stopFailure != null) throw new RagQueryUnavailableException("Reader stop remains unproven");
        try {
            if (!ctx.completionLatch.await(Math.max(0,stopBy-System.nanoTime()), TimeUnit.NANOSECONDS)) {
                throw new RagQueryUnavailableException("Reader quiescence is not proven");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RagQueryUnavailableException("Reader stop interrupted");
        }
        var reader = ragRetrievalReaderRepository.findByReaderId(requestId).orElseThrow();
        if (!"TERMINAL".equals(reader.getState())) {
            throw new RagQueryUnavailableException("Reader finish requires recovery");
        }
        return new RagRetrievalCancelResult(requestId, reader.getFinishDisposition(), "Retrieval reader stopped");
    }

    private void completeContext(AsyncSenderContext ctx) {
        try {
            if(ctx.stopRequested && (!ctx.errorOutput || ctx.transportAborted) && ctx.outputCallbacks.get()==0) {
                var response=(HttpServletResponse)ctx.asyncContext.getResponse();
                if(!response.isCommitted()) {
                    response.resetBuffer();response.setContentLength(0);
                    response.setStatus(ctx.cancelled?HttpServletResponse.SC_CONFLICT:HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    response.setHeader("Cache-Control","no-store");
                }
            }
            ctx.asyncContext.complete();
        }
        catch (IllegalStateException alreadyCompleting) {
            // complete() returning or throwing is not proof; await the container event.
        }
    }

    /** A logical cancel is not a broken transport. A healthy unused cursor can
     * still send the canonical non-evidence error, with the same callback owner. */
    private void stopOrReply(AsyncSenderContext ctx) {
        boolean reply;
        synchronized (ctx) {
            // A cancel may arrive between publishing the sender and registering
            // readiness. Its producer will select the error once setup returns.
            if (!ctx.outputRegistered && !ctx.outputClosed && !ctx.transportAborted && ctx.outputListener==null) return;
            reply = !ctx.outputRegistered && !ctx.outputClosed && !ctx.transportAborted
                    && !((HttpServletResponse) ctx.asyncContext.getResponse()).isCommitted();
            if (ctx.errorOutput && !ctx.transportAborted) return;
            if (!reply) stopOutput(ctx);
        }
        if (reply) {
            try {
                var failure = failureResponse(ctx.cancelled
                        ? new RagRetrievalCancelledException() : new RagQueryUnavailableException("Retrieval stopped"));
                startOutput(ctx, objectMapper.writeValueAsBytes(failure.envelope()), failure.status(), false);
                return;
            } catch (Exception unavailable) { ctx.transportAborted = true;stopOutput(ctx); }
        }
        completeContext(ctx);
    }

    public void dispatchAsync(RagRetrievalCommand command, AsyncContext asyncContext,
            java.util.function.Function<RagRetrievalResponse,?> wireAdapter) {
        if (!isEnabled()) {
            handleAsyncFailure(asyncContext, new RagQueryUnavailableException("RAG retrieval is disabled"));
            return;
        }
        try { validateCommand(command); }
        catch (RuntimeException invalid) { handleAsyncFailure(asyncContext, invalid); return; }
        if (ragRetrievalReaderRepository.findByReaderId(command.requestId()).isPresent()) {
            handleAsyncFailure(asyncContext,new IllegalArgumentException("Request identity was already used"));
            return;
        }
        var ctx = new AsyncSenderContext(command.requestId(), UUID.randomUUID(), asyncContext);
        boolean busy;
        synchronized(activeSenders) {
            busy=!activeSenders.isEmpty();
            if(!busy) activeSenders.put(command.requestId(),ctx);
        }
        if(busy) { handleAsyncFailure(asyncContext,new RagReaderBusyException()); return; }
        asyncContext.addListener(new AsyncListener() {
            @Override public void onComplete(AsyncEvent event) { ctx.contextCompleted.countDown(); }
            @Override public void onTimeout(AsyncEvent event) { stop("TIMEOUT",false); }
            @Override public void onError(AsyncEvent event) { stop("TRANSPORT_ERROR",true); }
            @Override public void onStartAsync(AsyncEvent event) { ctx.stopRequested = true; }
            private void stop(String reason,boolean transportFailure) {
                ctx.stopRequested = true;
                if (transportFailure) {ctx.transportAborted = true;stopOutput(ctx);}
                long stopBy=ctx.stopDeadline();
                try {
                    ragRetrievalAuthority.requestStop(ctx.readerId, reason);
                    ragQueryEmbeddingSupervisor.stopWorker(ctx.readerId,stopBy);
                } catch (RuntimeException unknownStop) {
                    log.warn("Retrieval stop remains unproven");
                } finally {
                    stopOrReply(ctx);
                    if (!transportFailure) finishDeadlineOutput(ctx);
                }
            }
        });
        // Register readiness while the request is running, before any deadline.
        // No headers/body are written until the producer selects one bounded payload.
        try { initializeOutput(ctx); }
        catch (IOException | RuntimeException failed) {
            ctx.stopRequested=true;ctx.transportAborted=true;stopOutput(ctx);completeContext(ctx);
        }
        Thread.ofVirtual().start(() -> runSender(command, ctx, wireAdapter));
    }

    /** The Servlet timeout must be handled before its listener returns. Drain
     * only ready error bytes through the same single-writer callback; never wait
     * for readiness or revive an evidence cursor. Otherwise abort, retaining all
     * sender/native/context quiescence requirements before guarded release. */
    private void finishDeadlineOutput(AsyncSenderContext ctx) {
        var listener = ctx.errorOutput ? ctx.outputListener : null;
        if (listener != null && !ctx.outputClosed) {
            try { listener.onWritePossible(); }
            catch (IOException failed) { listener.onError(failed); }
        }
        if (!ctx.outputClosed) {ctx.transportAborted=true;stopOutput(ctx);}
        completeContext(ctx);
    }

    /** The producer computes and finishes; the owned callback alone writes bounded output. */
    private void runSender(RagRetrievalCommand command, AsyncSenderContext ctx,
            java.util.function.Function<RagRetrievalResponse,?> wireAdapter) {
        byte[] body = null;
        try {
            if (ctx.stopRequested) throw new RagRetrievalCancelledException();
            PlanExecution plan = prepareAndAdmit(command, ctx.reservationToken);
            boolean admitted = !plan.targets.isEmpty();
            List<RagRetrievalMatch> matches = List.of();
            if (admitted) {
                if (ctx.stopRequested) throw new RagRetrievalCancelledException();
                var embedding = ragQueryEmbeddingSupervisor.computeEmbedding(command.requestId(), command.question());
                ctx.nativeMetrics = Map.of("startup_nanos",embedding.startupNanos(),"embedding_total_nanos",embedding.totalNanos(),
                        "peak_memory_bytes",embedding.resourceUsage()!=null?embedding.resourceUsage().peakProcessMemoryBytes():0L);
                if (ctx.stopRequested) throw new RagRetrievalCancelledException();
                matches = searchAndVerifyMatches(plan, embedding.vectorString(), plan.topK);
                ragQueryEmbeddingSupervisor.stopWorker(command.requestId());
            }
            var boundedMatches = new ArrayList<>(matches);
            do {
                var outcome = boundedMatches.isEmpty() ? RagRetrievalOutcome.NO_EVIDENCE : RagRetrievalOutcome.EVIDENCE;
                body = objectMapper.writeValueAsBytes(ApiResponses.success(
                        wireAdapter.apply(new RagRetrievalResponse(command.requestId(), outcome, List.copyOf(boundedMatches)))));
                if (body.length <= MAX_RESPONSE_BYTES) break;
                boundedMatches.removeLast();
            } while (true);
            if (ctx.stopRequested) throw new RagRetrievalCancelledException();
            if (admitted) {
                ragRetrievalAuthority.beginPreparedEmission(command.requestId(), plan.proofs);
            } else {
                ragRetrievalAuthority.beginEmission(command.requestId());
            }
            startOutput(ctx,body,HttpServletResponse.SC_OK,true);
            body=null; // The owned callback retains this buffer until positive quiescence.
            long remaining=TimeUnit.SECONDS.toNanos(120)-(System.nanoTime()-ctx.startedNanos);
            if(!ctx.outputCompleted.await(Math.max(0,remaining),TimeUnit.NANOSECONDS))
                throw new RagQueryUnavailableException("Output completion remains unproven");
        } catch (Exception failed) {
            try {
                if (ctx.stopRequested) stopOrReply(ctx);
                else if (!ctx.outputRegistered) {
                    var failure=failureResponse(failed);
                    startOutput(ctx,objectMapper.writeValueAsBytes(failure.envelope()),failure.status(),false);
                }
                if (ctx.errorOutput && !ctx.transportAborted
                        && !ctx.outputCompleted.await(Math.max(0,ctx.stopDeadline()-System.nanoTime()),TimeUnit.NANOSECONDS))
                    ctx.transportAborted=true;
            } catch(Exception unavailable) { ctx.stopRequested=true;ctx.transportAborted=true; }
        } finally {
            if (body != null) Arrays.fill(body, (byte) 0);
            body = null;
            stopOutput(ctx);
            ctx.emitted = ctx.emitted && !ctx.stopRequested;
            completeContext(ctx);
            try {
                long stopBy=ctx.stopDeadline();
                ragQueryEmbeddingSupervisor.stopWorker(ctx.readerId,stopBy);
                if(!ctx.outputCompleted.await(Math.max(0,stopBy-System.nanoTime()),TimeUnit.NANOSECONDS)
                        || ctx.outputCallbacks.get()!=0) throw new RagQueryUnavailableException("Output callback remains active");
                if (!ctx.contextCompleted.await(Math.max(0,stopBy-System.nanoTime()), TimeUnit.NANOSECONDS)) {
                    throw new RagQueryUnavailableException("Container completion remains unproven");
                }
                discardOutput(ctx);
                // Read back admission: a thrown COMMIT acknowledgement does not prove rollback.
                finishQuiescentSender(ctx);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                log.warn("Retrieval finish interrupted; exclusion retained");
            } catch (RuntimeException uncertainFinish) {
                Throwable cause = uncertainFinish;
                while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
                log.warn("Retrieval finish unproven; exclusion retained; category={}", cause.getClass().getSimpleName());
            } finally { ctx.producerReturned=true; ctx.completionLatch.countDown(); }
        }
    }

    private void finishQuiescentSender(AsyncSenderContext ctx) {
        String state=ragRetrievalAuthority.observe(ctx.readerId,ctx.reservationToken);
        if(state==null) {
            ragRetrievalAuthority.admitEmpty(ctx.readerId,ctx.reservationToken,ragRuntimeIdentity.instance().toString());
            state="ADMITTED";
        }
        if(!"FOREIGN".equals(state) && !"TERMINAL".equals(state)) {
            var evidence=new LinkedHashMap<String,Object>(ctx.nativeMetrics);
            evidence.put("reason","OWNED_SENDER_JOINED"); evidence.put("sender_quiescent",true);
            evidence.put("context_complete",true); evidence.put("native_quiescent",true);
            evidence.put("transport_quiescent",true); evidence.put("buffers_discarded",true);
            evidence.put("request_total_nanos",System.nanoTime()-ctx.startedNanos);
            evidence.put("at",Instant.now().toString());
            ragRetrievalAuthority.finish(ctx.readerId,ctx.reservationToken,
                    ctx.cancelled?"CANCELLED":ctx.emitted?"COMPLETED":"EXECUTION_FAILURE",
                    ctx.emitted?"EMITTED":"ABORTED",evidence,null,null);
        }
        ctx.finished=true;
        ragQueryEmbeddingSupervisor.forgetStopped(ctx.readerId);
        activeSenders.remove(ctx.readerId,ctx);
    }

    /** Existing reconciler retries only locally owned, joined senders, never replays a response. */
    public void reconcileCompletedSenders() {
        for(var ctx:activeSenders.values()) {
            if(!isQuiescentReader(ctx.readerId)) continue;
            try {
                ragQueryEmbeddingSupervisor.stopWorker(ctx.readerId);
                discardOutput(ctx);
                finishQuiescentSender(ctx);
            } catch(RuntimeException unknown) {
                log.warn("Owned retrieval finish remains unproven; exclusion retained");
            }
        }
    }

    public boolean isQuiescentReader(UUID reader) {
        var ctx=activeSenders.get(reader);
        return ctx!=null && ctx.producerReturned && ctx.completionLatch.getCount()==0
                && ctx.contextCompleted.getCount()==0 && ctx.outputCompleted.getCount()==0 && ctx.outputCallbacks.get()==0;
    }

    private void startOutput(AsyncSenderContext ctx,byte[] body,int status,boolean success) throws IOException {
        synchronized (ctx) {
            if(ctx.outputRegistered || ctx.outputClosed || ctx.transportAborted || (success && ctx.stopRequested)) {
                Arrays.fill(body,(byte)0);return;
            }
            var response=(HttpServletResponse)ctx.asyncContext.getResponse();
            response.setStatus(status);response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control","no-store");response.setContentLength(body.length);
            ctx.errorOutput=!success;
            ctx.responseBody=body;
            ctx.outputRegistered=true;
        }
        var listener=ctx.outputListener;
        if(listener==null) throw new IOException("Response readiness owner unavailable");
        listener.onWritePossible();
    }

    private void initializeOutput(AsyncSenderContext ctx) throws IOException {
        ServletOutputStream output=((HttpServletResponse)ctx.asyncContext.getResponse()).getOutputStream();
        var listener = new WriteListener() {
            private int offset;
            private final java.util.concurrent.atomic.AtomicInteger writeSignals = new java.util.concurrent.atomic.AtomicInteger();
            @Override public void onWritePossible() {
                ctx.outputCallbacks.incrementAndGet();
                try {
                    // A timeout may request the ready error drain while the container
                    // is delivering its callback. Coalesce signals, preserving one
                    // cursor owner and any readiness event that arrives concurrently.
                    if (writeSignals.getAndIncrement()!=0) return;
                    int pending=1;
                    do {
                        try {
                            while(!ctx.outputClosed && (!ctx.stopRequested || ctx.errorOutput)) {
                                byte[] body=ctx.responseBody;
                                if(body==null) break;
                                if(!output.isReady()) break;
                                if(offset<body.length) {
                                    int length=Math.min(4096,body.length-offset);
                                    output.write(body,offset,length);offset+=length;
                                } else {
                                    output.flush();ctx.emitted=!ctx.errorOutput;stopOutput(ctx);completeContext(ctx);break;
                                }
                            }
                        } catch(Exception failed) { onError(failed); }
                        pending=writeSignals.addAndGet(-pending);
                    } while(pending!=0);
                } finally {
                    if(ctx.outputCallbacks.decrementAndGet()==0 && ctx.outputClosed) {
                        ctx.outputCompleted.countDown();
                        if(ctx.detached) discardOutput(ctx);
                    }
                }
            }
            @Override public void onError(Throwable failure) {
                ctx.stopRequested=true;ctx.transportAborted=true;stopOutput(ctx);completeContext(ctx);
            }
        };
        output.setWriteListener(listener);
        ctx.outputListener=listener;
    }

    private static void stopOutput(AsyncSenderContext ctx) {
        ctx.outputClosed=true;
        if(ctx.outputCallbacks.get()==0) ctx.outputCompleted.countDown();
    }

    private static void discardOutput(AsyncSenderContext ctx) {
        if(ctx.outputCallbacks.get()!=0) throw new IllegalStateException("Output remains active");
        byte[] bytes=ctx.responseBody;ctx.responseBody=null;
        if(bytes!=null) Arrays.fill(bytes,(byte)0);
    }

    private void handleAsyncFailure(AsyncContext asyncContext, Throwable e) {
        var ctx=new AsyncSenderContext(UUID.randomUUID(),UUID.randomUUID(),asyncContext);
        ctx.detached=true;
        asyncContext.addListener(new AsyncListener() {
            private void stop() { ctx.stopRequested=true;stopOutput(ctx);if(ctx.outputCallbacks.get()==0) discardOutput(ctx); }
            @Override public void onComplete(AsyncEvent event) { stop(); }
            @Override public void onTimeout(AsyncEvent event) { stop();completeContext(ctx); }
            @Override public void onError(AsyncEvent event) { stop();completeContext(ctx); }
            @Override public void onStartAsync(AsyncEvent event) { stop(); }
        });
        try {
            initializeOutput(ctx);
            var failure=failureResponse(e);
            startOutput(ctx,objectMapper.writeValueAsBytes(failure.envelope()),failure.status(),false);
        } catch(Exception unavailable) { stopOutput(ctx);completeContext(ctx); }
    }

    private record FailureResponse(int status,ApiResponse<?> envelope) {}

    private FailureResponse failureResponse(Throwable e) {
                int status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                String code = "RAG_QUERY_UNAVAILABLE";
                String msg = "Query service unavailable";

                if (e instanceof RagReaderBusyException) {
                    status = HttpServletResponse.SC_CONFLICT;
                    code = "RAG_READER_BUSY";
                    msg = "An active retrieval reader or source mutation is in progress";
                } else if (e instanceof RagRetrievalCancelledException) {
                    status = HttpServletResponse.SC_CONFLICT;
                    code = "RAG_RETRIEVAL_CANCELLED";
                    msg = "Retrieval cancelled before response emission";
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

                return new FailureResponse(status,ApiResponses.error(code,msg));
    }

    private record AdmittedSourceTarget(
            RagSource source,
            RagExternalFileSource fileSource,
            RagIndexGeneration generation,
            String group,
            String relativePath
    ) {}

    private record ResolvedDepTarget(
            RagSource source,
            RagExternalFileSource fileSource
    ) {}

    private record PlanExecution(
            List<AdmittedSourceTarget> targets,
            Map<Long, ResolvedDepTarget> dependencies,
            Set<UUID> allReservedFileIds,
            Set<Long> allReservedSourceIds,
            List<RagRetrievalCurrentAuthority.Proof> proofs,
            int topK,
            RagRetrievalMode mode,
            UUID readerId,
            UUID reservationToken
    ) {}

    private record Candidate(RagRetrievalCurrentAuthority.Proof proof, Set<Long> sources, Set<UUID> files,
                             Map<Long,ResolvedDepTarget> dependencies) {}

    private Candidate prepareCandidate(RagSource source, RagIndexGeneration generation) {
        var sourceIds=new HashSet<Long>(); sourceIds.add(source.getId());
        var fileIds=new HashSet<UUID>(); fileIds.add(UUID.fromString(source.getSourceId()));
        var dependencies=new LinkedHashMap<Long,ResolvedDepTarget>();
        resolveTransitiveDependencies(source.getId(),sourceIds,fileIds,dependencies,true);
        var inspected=new HashMap<UUID,com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.SelectedSnapshot>();
        for(UUID fileId:fileIds.stream().sorted().toList()) {
            var file=ragExternalFileSourceRepository.findByFileSourceId(fileId).orElseThrow();
            if(file.getSelectedRevisionId()==null || file.getApprovedPendingRevisionId()!=null) throw new NoSuchElementException("RAG resource unavailable");
            var tuple=new com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.SelectedSnapshot(
                    file.getSelectedRevisionId(),file.getSelectedRevisionHash(),file.getSelectedByteLength(),file.getSourceVersion(),file.getConsentResetVersion());
            try {
                ragRetrievalAuthority.verifySnapshot(fileId,tuple.revision(),tuple.hash(),tuple.length(),"QUERY_PREPARE",
                        com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(fileId+":"+tuple.revision()));
                inspected.put(fileId,tuple);
            } catch(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotIntegrityException
                    | com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException corrupt) {
                throw new NoSuchElementException("RAG resource unavailable");
            }
        }
        try { return new Candidate(currentAuthority.prepare(generation,inspected),sourceIds,fileIds,dependencies); }
        catch(RagSourceChangedException stale) { throw new NoSuchElementException("RAG resource unavailable"); }
    }

    private void resolveTransitiveDependencies(
            Long rootSourceId,
            Set<Long> allReservedSourceIds,
            Set<UUID> allReservedFileIds,
            Map<Long, ResolvedDepTarget> dependencies,
            boolean strictValidation
    ) {
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(rootSourceId);
        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<RagDependency> deps = ragDependencyRepository.findByParentSourceId(currentId);
            for (RagDependency dep : deps) {
                Long childId = dep.getChildSourceId();
                if (allReservedSourceIds.add(childId)) {
                    if (allReservedSourceIds.size() > MAX_UNION_SOURCES) throw new RagQueryScopeLimitException("Complete query scope exceeds the maximum allowable document limit");
                    Optional<RagSource> childSrcOpt = ragSourceRepository.findById(childId);
                    if (childSrcOpt.isEmpty()) {
                        if (strictValidation) {
                            throw new NoSuchElementException("RAG resource unavailable");
                        }
                        continue;
                    }
                    RagSource childSrc = childSrcOpt.get();
                    if (childSrc.getSourceType() != RagSourceType.EXTERNAL_FILE || childSrc.getAccessState() != RagAccessState.APPROVED) {
                        if (strictValidation) {
                            throw new NoSuchElementException("RAG resource unavailable");
                        }
                        continue;
                    }
                    RagExternalFileSource childFileSrc = null;
                    if (childSrc.getSourceType() == RagSourceType.EXTERNAL_FILE) {
                        UUID childFileId = UUID.fromString(childSrc.getSourceId());
                        Optional<RagExternalFileSource> childFileOpt = ragExternalFileSourceRepository.findByFileSourceId(childFileId);
                        if (childFileOpt.isEmpty() || !childFileOpt.get().isEligible() || childFileOpt.get().isSnapshotIntegrityFailed()) {
                            if (strictValidation) {
                                throw new NoSuchElementException("RAG resource unavailable");
                            }
                            continue;
                        }
                        childFileSrc = childFileOpt.get();
                        allReservedFileIds.add(childFileId);
                    }

                    if (childFileSrc != null) {
                        dependencies.put(childId, new ResolvedDepTarget(childSrc, childFileSrc));
                    }
                    queue.add(childId);
                }
            }
        }
    }

    private PlanExecution prepareAndAdmit(RagRetrievalCommand command, UUID reservationToken) {
        validateCommand(command);

        int topK = command.topK() != null ? command.topK() : 5;
        RagRetrievalMode mode = command.mode() != null ? command.mode() : RagRetrievalMode.GENERIC;

        List<AdmittedSourceTarget> targets = new ArrayList<>();
        List<RagRetrievalCurrentAuthority.Proof> proofs = new ArrayList<>();
        Map<Long, ResolvedDepTarget> dependencies = new LinkedHashMap<>();
        Set<UUID> allReservedFileIds = new HashSet<>();
        Set<Long> allReservedSourceIds = new HashSet<>();
        List<RagSourceSelectionEntry> selection = command.selection();

        if (selection != null && !selection.isEmpty()) {
            for (RagSourceSelectionEntry entry : selection) {
                RagSource src = ragSourceRepository.findById(entry.sourceId())
                        .orElseThrow(() -> new NoSuchElementException("RAG resource unavailable"));

                if (src.getSourceType() != RagSourceType.EXTERNAL_FILE
                        || src.getAccessState() != RagAccessState.APPROVED
                        || src.getFileIdentity() == null) {
                    throw new NoSuchElementException("RAG resource unavailable");
                }

                UUID fileSourceId = UUID.fromString(src.getSourceId());
                RagExternalFileSource fileSrc = ragExternalFileSourceRepository.findByFileSourceId(fileSourceId)
                        .orElseThrow(() -> new NoSuchElementException("RAG resource unavailable"));

                if (!fileSrc.isEligible() || fileSrc.isSnapshotIntegrityFailed()) {
                    throw new NoSuchElementException("RAG resource unavailable");
                }

                if (!src.getFileIdentity().startsWith(fileSrc.getCanonicalIdentityDigest())) {
                    throw new NoSuchElementException("RAG resource unavailable");
                }

                if (!entry.selectedRevisionId().equals(fileSrc.getSelectedRevisionId())) {
                    throw new NoSuchElementException("RAG resource unavailable");
                }

                List<RagIndexGeneration> genList = ragIndexGenerationRepository.findBySourceId(src.getId());
                RagIndexGeneration activeGen = genList.stream()
                        .filter(g -> g.isActive() && "PUBLISHED".equals(g.getStatus()))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("RAG resource unavailable"));

                var candidate=prepareCandidate(src,activeGen);
                proofs.add(candidate.proof());

                targets.add(new AdmittedSourceTarget(
                        src,
                        fileSrc,
                        activeGen,
                        entry.group(),
                        entry.relativePath()
                ));
                allReservedFileIds.addAll(candidate.files());
                allReservedSourceIds.addAll(candidate.sources());
                dependencies.putAll(candidate.dependencies());
            }
        } else {
            // Omitted selection in GENERIC mode: discover all eligible indexed Markdown
            if (mode != RagRetrievalMode.GENERIC) {
                throw new IllegalArgumentException("Ideaverse modes require an explicit selection roster");
            }

            long afterId = 0;
            int scannedCandidates = 0;
            while (true) {
            List<RagSource> approved = ragSourceRepository.findRetrievalCandidates(afterId,
                    org.springframework.data.domain.PageRequest.of(0, MAX_UNION_SOURCES + 1));
            if (approved.isEmpty()) break;
            afterId = approved.getLast().getId();
            scannedCandidates += approved.size();
            if (scannedCandidates > MAX_SCANNED_CHUNKS) {
                throw new RagQueryUnavailableException("Eligible metadata scan exceeds bounded capacity");
            }

            for (RagSource src : approved) {
                if (src.getFileIdentity() == null) continue;
                try {
                    UUID fileId = UUID.fromString(src.getSourceId());
                    Optional<RagExternalFileSource> fileOpt = ragExternalFileSourceRepository.findByFileSourceId(fileId);
                    if (fileOpt.isEmpty() || !fileOpt.get().isEligible() || fileOpt.get().isSnapshotIntegrityFailed()) {
                        continue;
                    }
                    RagExternalFileSource fileSrc = fileOpt.get();
                    if (!src.getFileIdentity().startsWith(fileSrc.getCanonicalIdentityDigest())) {
                        continue;
                    }

                    List<RagIndexGeneration> genList = ragIndexGenerationRepository.findBySourceId(src.getId());
                    Optional<RagIndexGeneration> activeGen = genList.stream()
                            .filter(g -> g.isActive() && "PUBLISHED".equals(g.getStatus()))
                            .findFirst();
                    if (activeGen.isPresent()) {
                        var candidate=prepareCandidate(src,activeGen.get());
                        proofs.add(candidate.proof());
                        allReservedSourceIds.addAll(candidate.sources());
                        allReservedFileIds.addAll(candidate.files());
                        dependencies.putAll(candidate.dependencies());
                        targets.add(new AdmittedSourceTarget(
                                src,
                                fileSrc,
                                activeGen.get(),
                                null,
                                null
                        ));
                        allReservedFileIds.add(fileId);
                        allReservedSourceIds.add(src.getId());


                        if (targets.size() > MAX_UNION_SOURCES || allReservedFileIds.size() > MAX_UNION_SOURCES || allReservedSourceIds.size() > MAX_UNION_SOURCES) {
                            throw new RagQueryScopeLimitException("Complete query scope exceeds the maximum allowable document limit");
                        }
                    }
                } catch (RagQueryScopeLimitException e) {
                    throw e;
                } catch (NoSuchElementException ineligibleClosure) {
                    // An incomplete closure is not an eligible root and consumes no scope slot.
                }
            }
            if (approved.size() < MAX_UNION_SOURCES + 1) break;
            }
        }

        if (targets.isEmpty()) {
            // Empty results consume their opaque ID through the same owned sender lifecycle.
            ragRetrievalAuthority.admitEmpty(command.requestId(),reservationToken,ragRuntimeIdentity.instance().toString());
            return new PlanExecution(List.of(), Map.of(), Set.of(), Set.of(), List.of(), topK, mode, command.requestId(), reservationToken);
        }

        if (allReservedFileIds.size() > MAX_UNION_SOURCES || targets.size() > MAX_UNION_SOURCES || allReservedSourceIds.size() > MAX_UNION_SOURCES) {
            throw new RagQueryScopeLimitException("Complete query scope exceeds the maximum allowable document limit");
        }

        List<UUID> sortedFileIds = allReservedFileIds.stream().sorted().toList();

        Set<Long> rootTargetSourceIds = targets.stream().map(t -> t.source.getId()).collect(Collectors.toSet());
        List<Long> transitiveDependencyIds = allReservedSourceIds.stream()
                .filter(id -> !rootTargetSourceIds.contains(id))
                .sorted()
                .toList();

        List<Map<String, Object>> allSourcesPayload = new ArrayList<>();
        List<String> allRevisionIds = new ArrayList<>();
        List<String> allGenerationIds = new ArrayList<>();
        List<Long> allGenerationRowIds = new ArrayList<>();

        for (AdmittedSourceTarget t : targets) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("source_id", t.source.getId());
            map.put("file_source_id", t.fileSource.getFileSourceId().toString());
            map.put("revision_id", t.fileSource.getSelectedRevisionId().toString());
            map.put("generation_id", t.generation.getGenerationId().toString());
            map.put("generation_row_id", t.generation.getId());
            map.put("authorization_epoch", t.source.getAuthorizationEpoch());
            map.put("field_policy_version", t.source.getFieldPolicyVersion());
            map.put("owner_source_version", t.fileSource.getSourceVersion());
            map.put("owner_reset_version", t.fileSource.getConsentResetVersion());
            map.put("approved_fields", t.source.getApprovedFields().stream().sorted().toList());
            map.put("file_identity", t.source.getFileIdentity());
            map.put("role", "TARGET");
            allSourcesPayload.add(map);

            allRevisionIds.add(t.fileSource.getSelectedRevisionId().toString());
            allGenerationIds.add(t.generation.getGenerationId().toString());
            allGenerationRowIds.add(t.generation.getId());
        }

        for (ResolvedDepTarget dep : dependencies.values()) {
            if (rootTargetSourceIds.contains(dep.source.getId())) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("source_id", dep.source.getId());
            if (dep.fileSource != null) {
                map.put("file_source_id", dep.fileSource.getFileSourceId().toString());
                map.put("revision_id", dep.fileSource.getSelectedRevisionId().toString());
                allRevisionIds.add(dep.fileSource.getSelectedRevisionId().toString());
            }
            map.put("authorization_epoch", dep.source.getAuthorizationEpoch());
            map.put("field_policy_version", dep.source.getFieldPolicyVersion());
            map.put("owner_source_version", dep.fileSource.getSourceVersion());
            map.put("owner_reset_version", dep.fileSource.getConsentResetVersion());
            map.put("approved_fields", dep.source.getApprovedFields().stream().sorted().toList());
            map.put("file_identity", dep.source.getFileIdentity());
            map.put("role", "DEPENDENCY");
            allSourcesPayload.add(map);
        }

        Map<String, Object> scopePayload = new LinkedHashMap<>();
        scopePayload.put("sources", allSourcesPayload);
        scopePayload.put("external_file_ids", sortedFileIds.stream().map(UUID::toString).toList());
        scopePayload.put("source_ids", allReservedSourceIds.stream().sorted().toList());
        scopePayload.put("revision_ids", allRevisionIds.stream().distinct().sorted().toList());
        scopePayload.put("transitive_dependency_ids", transitiveDependencyIds);
        scopePayload.put("generation_ids", allGenerationIds.stream().distinct().sorted().toList());
        scopePayload.put("generation_row_ids", allGenerationRowIds.stream().distinct().sorted().toList());
        scopePayload.put("mode", mode.name());
        scopePayload.put("authority_manifests", proofs.stream().map(p -> p.source().manifest()).toList());
        scopePayload.put("producer_manifest_digests", proofs.stream().map(p ->
                com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(p.producerManifest())).toList());
        scopePayload.put("pipeline", com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentFinalizationGate.PIPELINE);
        scopePayload.put("controller_pid", ProcessHandle.current().pid());
        scopePayload.put("controller_started_at", ProcessHandle.current().info().startInstant().orElseThrow().toString());
        scopePayload.put("controller_creation_file_time", Long.toString(RagRetrievalControllerIdentity.creationFileTime()));
        String scopeDigest;
        try {
            scopeDigest = com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(
                    objectMapper.writeValueAsString(scopePayload));
        } catch (com.fasterxml.jackson.core.JsonProcessingException invalidProof) {
            throw new RagQueryUnavailableException("Scope proof unavailable");
        }

        String controllerInstance = ragRuntimeIdentity.instance().toString();

        try {
            ragRetrievalAuthority.admitPrepared(
                    command.requestId(),
                    reservationToken,
                    scopeDigest,
                    scopePayload,
                    sortedFileIds,
                    controllerInstance,
                    proofs
            );
        } catch (org.springframework.dao.DataAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("RAG_READER_BUSY")) {
                throw new RagReaderBusyException();
            }
            if (e.getMessage() != null && e.getMessage().contains("RAG_SOURCE_UNVERIFIED")) {
                throw new RagQueryUnavailableException("RAG authority unavailable");
            }
            throw new RagQueryUnavailableException("Admission failed", e);
        }

        return new PlanExecution(targets, dependencies, allReservedFileIds, allReservedSourceIds, proofs, topK, mode, command.requestId(), reservationToken);
    }

    private void validateRelativePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("relative traversal-free path required in Ideaverse mode");
        }
        int cp = path.codePointCount(0, path.length());
        if (cp > 512) {
            throw new IllegalArgumentException("relativePath exceeds 512 code points");
        }
        if (path.startsWith("/") || path.startsWith("\\") || path.contains(":") || path.contains("..")) {
            throw new IllegalArgumentException("Invalid relative path in selection roster: relative traversal-free path required");
        }
    }

    private List<RagRetrievalMatch> searchAndVerifyMatches(
            PlanExecution plan,
            String queryVectorString,
            int topK
    ) {
        List<Long> genDbIds = plan.targets.stream().map(t -> t.generation.getId()).toList();
        long chunkCount = ragDocumentChunkRepository.countByGenerationIdIn(genDbIds);
        if (chunkCount > MAX_SCANNED_CHUNKS) {
            throw new RagQueryUnavailableException("Admitted chunk scan exceeds bounded capacity");
        }

        Set<String> allowedIdentities = new HashSet<>();
        Map<String, AdmittedSourceTarget> targetByIdentity = new HashMap<>();
        Map<Long, RagRetrievalCurrentAuthority.Proof> proofByGeneration = new HashMap<>();

        for (AdmittedSourceTarget t : plan.targets) {
            String fileIdStr = t.fileSource.getFileSourceId().toString();
            String srcIdStr = t.source.getId().toString();
            allowedIdentities.add(fileIdStr);
            targetByIdentity.put(fileIdStr, t);
            plan.proofs.stream().filter(p -> p.generationId().equals(t.generation.getGenerationId())).findFirst()
                    .ifPresent(p -> proofByGeneration.put(t.generation.getId(),p));
        }

        // Page provenance first, then rank only admissible IDs using PostgreSQL exact cosine.
        List<Long> admissibleIds = new ArrayList<>();
        int page = 0;
        while (true) {
            Map<UUID,String> pageSnapshots = new HashMap<>();
            var chunks = ragDocumentChunkRepository.findByGenerationIdIn(genDbIds,
                    org.springframework.data.domain.PageRequest.of(page++, 64,
                            org.springframework.data.domain.Sort.by("chunkId")));
            for (var chunk : chunks) {
                if (isChunkAdmissible(chunk, allowedIdentities)) {
                    var target = targetByIdentity.get(chunk.getSourceIdentity());
                    if (target != null && verifySnapshotExcerpt(target, chunk, proofByGeneration.get(chunk.getGenerationId()),plan,pageSnapshots) != null)
                        admissibleIds.add(chunk.getChunkId());
                }
            }
            pageSnapshots.clear();
            if (chunks.size() < 64) break;
        }
        var ranked = admissibleIds.isEmpty() ? List.<RagDocumentChunkRepository.ChunkRankProjection>of()
                : ragDocumentChunkRepository.findRankedByCosineDistance(admissibleIds, queryVectorString, MAX_SCANNED_CHUNKS);

        List<RagRetrievalMatch> matches = new ArrayList<>();
        int citationIndex = 1;
        int totalExcerptCodePoints = 0;
        Map<UUID,String> selectedSnapshots = new HashMap<>();

        for (var pair : ranked) {
            if (matches.size() >= topK) {
                break;
            }
            RagDocumentChunk chunk = ragDocumentChunkRepository.findById(pair.getChunkId()).orElseThrow();
            AdmittedSourceTarget target = targetByIdentity.get(chunk.getSourceIdentity());
            if (target == null) continue;

            // Extract and verify excerpt against snapshot bytes (F08)
            String verifiedExcerpt = verifySnapshotExcerpt(target, chunk, proofByGeneration.get(chunk.getGenerationId()),plan,selectedSnapshots);
            if (verifiedExcerpt == null || verifiedExcerpt.isBlank()) {
                continue;
            }

            int excerptCp = verifiedExcerpt.codePointCount(0, verifiedExcerpt.length());
            if (totalExcerptCodePoints + excerptCp > MAX_EXCERPT_CODE_POINTS) {
                if (matches.isEmpty()) {
                    continue; // Skip single oversized excerpt
                }
                break; // Stop adding further candidates
            }
            totalExcerptCodePoints += excerptCp;

            RagLocator locator = extractLocator(chunk.getLocator());
            if (locator == null) {
                continue;
            }
            String citationKey = "[" + citationIndex++ + "]";
            String displayName = target.relativePath != null ? target.relativePath : ragOriginalBytesStore.sourceName(
                    target.fileSource.getFileSourceId(), target.fileSource.getSelectedRevisionId());
            UUID publishedGeneration = plan.targets.stream().filter(t -> t.generation.getId().equals(chunk.getGenerationId()))
                    .map(t -> t.generation.getGenerationId()).findFirst().orElseThrow();

            matches.add(new RagRetrievalMatch(
                    citationKey,
                    verifiedExcerpt,
                    target.source.getId(),
                    target.fileSource.getFileSourceId(),
                    target.fileSource.getSelectedRevisionId(),
                    publishedGeneration,
                    displayName,
                    locator,
                    target.group,
                    "RAG_SNAPSHOT",
                    "SNAPSHOT_CURRENT",
                    "UNVERIFIED"
            ));
        }

        return matches;
    }

    private boolean isChunkAdmissible(RagDocumentChunk chunk, Set<String> allowedIdentities) {
        if (!allowedIdentities.contains(chunk.getSourceIdentity())) {
            return false;
        }
        Map<String, Object> meta = chunk.getStructureMetadata();
        if (meta == null || !meta.containsKey("constituent_locators")) {
            return false;
        }
        Object constituentsObj = meta.get("constituent_locators");
        if (!(constituentsObj instanceof List<?> constituents) || constituents.isEmpty()) {
            return false;
        }
        for (Object item : constituents) {
            if (!(item instanceof Map<?, ?> locMap)) {
                return false;
            }
            Object profile = locMap.get("profile");
            if (!"ppv-obsidian-source-v1".equals(profile)) {
                return false;
            }
            Object sourceKey = locMap.get("source_key");
            if (sourceKey == null) {
                sourceKey = locMap.get("source_identity");
            }
            if (sourceKey == null || !allowedIdentities.contains(sourceKey.toString())) {
                return false;
            }
        }
        return true;
    }

    private String readAdmittedSnapshot(PlanExecution plan, AdmittedSourceTarget target) {
        try {
            return ragRetrievalAuthority.readSnapshot(plan.readerId(),plan.reservationToken(),target.fileSource.getFileSourceId(),
                    target.fileSource.getSelectedRevisionId(),target.fileSource.getSelectedRevisionHash(),target.fileSource.getSelectedByteLength());
        } catch(RuntimeException unknown) {
            throw new RagQueryUnavailableException("Snapshot inspection remains unproven");
        }
    }

    private String verifySnapshotExcerpt(AdmittedSourceTarget target, RagDocumentChunk chunk, RagRetrievalCurrentAuthority.Proof proof,
            PlanExecution plan, Map<UUID,String> snapshots) {
        try {
            if (proof == null) return null;
            // P18 locators retain the producer's admitted S, while successful promotion
            // advances current S. Resolve that S through its sealed ORIGINAL manifest,
            // never by subtracting one from a current version or accepting either value.
            Long locatorVersion = null;
            for (var node : objectMapper.readTree(proof.producerManifest()).path("nodes")) {
                if (target.fileSource.getFileSourceId().toString().equals(node.path("domain_id").asText())
                        && target.fileSource.getSelectedRevisionId().toString().equals(node.path("snapshot").path("revision_id").asText())
                        && target.fileSource.getSelectedRevisionHash().equals(node.path("snapshot").path("sha256").asText())
                        && target.fileSource.getSelectedByteLength() == node.path("snapshot").path("byte_length").asLong()) {
                    locatorVersion = node.path("owner_source_version").asLong();
                }
            }
            if (locatorVersion == null || locatorVersion <= 0) return null;
            Map<String, Object> locMap = chunk.getLocator();
            if (locMap == null || locMap.isEmpty()) {
                return null;
            }

            // Require accepted profile (F08)
            Object profileObj = locMap.get("profile");
            if (!"ppv-obsidian-source-v1".equals(profileObj)) {
                return null;
            }
            String identity = target.fileSource.getFileSourceId().toString();
            if (!identity.equals(locMap.get("source_key"))
                    || !Objects.equals(getLong(locMap, "source_version", null), locatorVersion)
                    || !"content".equals(locMap.get("approved_field"))) return null;

            Long startChar = getLong(locMap, "char_start", "start_char");
            Long endChar = getLong(locMap, "char_end", "end_char");
            if (startChar == null || endChar == null || startChar < 0 || endChar <= startChar) {
                return null;
            }

            String fullText = snapshots.computeIfAbsent(target.fileSource.getFileSourceId(), id -> readAdmittedSnapshot(plan,target));
            int totalCp = fullText.codePointCount(0, fullText.length());
            if (endChar > totalCp) {
                return null;
            }

            int utf16Start = fullText.offsetByCodePoints(0, startChar.intValue());
            int utf16End = fullText.offsetByCodePoints(0, endChar.intValue());
            String slice = fullText.substring(utf16Start, utf16End);

            if (!slice.equals(chunk.getContent())) {
                return null;
            }
            // Every constituent must cover this exact contributor/revision/field/span.
            // Inseparable or ambiguous mixed content is rejected as a whole.
            Object rawParts = chunk.getStructureMetadata().get("constituent_locators");
            if (!(rawParts instanceof List<?> parts) || parts.isEmpty()) return null;
            long cursor = startChar;
            for (Object raw : parts) {
                if (!(raw instanceof Map<?, ?> part)
                        || !identity.equals(part.get("source_key"))
                        || !locatorVersion.equals(exactLong(part.get("source_version")))
                        || !"content".equals(part.get("approved_field"))
                        || exactLong(part.get("char_start")) == null
                        || exactLong(part.get("char_end")) == null
                        || exactLong(part.get("char_start")) != cursor || exactLong(part.get("char_end")) <= cursor
                        || exactLong(part.get("char_end")) > endChar) return null;
                cursor = exactLong(part.get("char_end"));
            }
            if (cursor != endChar) return null;

            // Verify line numbers if present in locator
            Long startLine = getLong(locMap, "line_start", "start_line");
            Long endLine = getLong(locMap, "line_end", "end_line");
            if ((locMap.containsKey("line_start") || locMap.containsKey("start_line")) && startLine == null
                    || (locMap.containsKey("line_end") || locMap.containsKey("end_line")) && endLine == null) return null;
            if (startLine != null || endLine != null) {
                int actualStartLine = countLines(fullText, utf16Start);
                int actualEndLine = countLines(fullText, fullText.offsetByCodePoints(utf16End, -1));
                if (startLine != null && startLine != actualStartLine) {
                    return null;
                }
                if (endLine != null && endLine != actualEndLine) {
                    return null;
                }
            }

            return slice;
        } catch (RagQueryUnavailableException unverified) {
            throw unverified;
        } catch (Exception e) {
            log.warn("Snapshot verification failed for source {}", target.source.getId());
            return null;
        }
    }

    private int countLines(String text, int charOffset) {
        int line = 1;
        for (int i = 0; i < charOffset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private RagLocator extractLocator(Map<String, Object> locMap) {
        if (locMap == null || locMap.isEmpty()) return null;
        Object profileObj = locMap.get("profile");
        if (!"ppv-obsidian-source-v1".equals(profileObj)) {
            return null;
        }
        Long startChar = getLong(locMap, "char_start", "start_char");
        Long endChar = getLong(locMap, "char_end", "end_char");
        if (startChar == null || endChar == null) {
            return null;
        }
        Long startLine = getLong(locMap, "line_start", "start_line");
        Long endLine = getLong(locMap, "line_end", "end_line");
        // A label's occurrence in the bytes does not establish parser heading provenance.
        // The precise verified span remains resolvable without this optional metadata.
        String heading = null;
        String profile = (String) profileObj;

        Map<String, Object> safeDetails = new LinkedHashMap<>();
        safeDetails.put("char_start", startChar);
        safeDetails.put("char_end", endChar);
        if (startLine != null) safeDetails.put("line_start", startLine);
        if (endLine != null) safeDetails.put("line_end", endLine);
        if (heading != null) safeDetails.put("heading", heading);
        safeDetails.put("profile", profile);

        return new RagLocator(profile, startChar, endChar, startLine, endLine, heading, Collections.unmodifiableMap(safeDetails));
    }

    private Long getLong(Map<String, Object> map, String key1, String key2) {
        Object val = map.get(key1);
        if (val == null && key2 != null) {
            val = map.get(key2);
        }
        return exactLong(val);
    }

    private Long exactLong(Object val) {
        if (!(val instanceof Number n)) return null;
        try { return new java.math.BigDecimal(n.toString()).longValueExact(); }
        catch (ArithmeticException | NumberFormatException invalid) { return null; }
    }

    private void validateCommand(RagRetrievalCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.requestId() == null) {
            throw new IllegalArgumentException("requestId must not be null");
        }
        if (command.question() == null || command.question().isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        int cp = command.question().codePointCount(0, command.question().length());
        if (cp > MAX_QUERY_CODE_POINTS) {
            throw new IllegalArgumentException("question exceeds 4096 Unicode code points");
        }
        if (command.topK() != null && (command.topK() < 1 || command.topK() > 10)) {
            throw new IllegalArgumentException("topK must be between 1 and 10");
        }
        if (command.collectionId() != null) {
            int collCp = command.collectionId().codePointCount(0, command.collectionId().length());
            if (collCp > 128) {
                throw new IllegalArgumentException("collectionId exceeds 128 code points");
            }
        }
        if (command.rosterVersion() != null) {
            int rosterCp = command.rosterVersion().codePointCount(0, command.rosterVersion().length());
            if (rosterCp > 128) {
                throw new IllegalArgumentException("rosterVersion exceeds 128 code points");
            }
        }
        if (command.selection() != null) {
            if (command.selection().size() > MAX_UNION_SOURCES) {
                throw new RagQueryScopeLimitException("Complete query scope exceeds the maximum allowable document limit");
            }
            for (RagSourceSelectionEntry entry : command.selection()) {
                if(entry==null || entry.sourceId()==null || entry.sourceId()<=0 || entry.selectedRevisionId()==null) {
                    throw new IllegalArgumentException("Invalid source selection");
                }
                if (entry.group() != null) {
                    int grpCp = entry.group().codePointCount(0, entry.group().length());
                    if (grpCp > 512) {
                        throw new IllegalArgumentException("group exceeds 512 code points");
                    }
                }
                if (entry.relativePath() != null) {
                    validateRelativePath(entry.relativePath());
                    int pathCp = entry.relativePath().codePointCount(0, entry.relativePath().length());
                    if (pathCp > 512) {
                        throw new IllegalArgumentException("relativePath exceeds 512 code points");
                    }
                }
            }
        }
        RagRetrievalMode mode=command.mode()!=null?command.mode():RagRetrievalMode.GENERIC;
        var selection=command.selection();
        if (mode == RagRetrievalMode.IDEAVERSE_CANON || mode == RagRetrievalMode.IDEAVERSE_NONCANON) {
            if (command.collectionId() == null || command.collectionId().isBlank()
                    || command.rosterVersion() == null || command.rosterVersion().isBlank()) {
                throw new IllegalArgumentException("Ideaverse mode requires collectionId and rosterVersion");
            }
            if (selection == null || selection.isEmpty()) {
                throw new IllegalArgumentException("Ideaverse mode requires explicit selection roster");
            }
        }
        if(selection!=null && !selection.isEmpty()) {
            Set<Long> seenSources=new HashSet<>();
            for(var entry:selection) {
                if(!seenSources.add(entry.sourceId())) throw new IllegalArgumentException("Duplicate source ID in selection roster");
                if(mode==RagRetrievalMode.IDEAVERSE_CANON && !"IDEAVERSE_CORE".equals(entry.group()))
                    throw new IllegalArgumentException("Canon mode permits only IDEAVERSE_CORE entries");
                if(mode==RagRetrievalMode.IDEAVERSE_NONCANON && !"IDEAVERSE_CORE".equals(entry.group()) && !"IDEAVERSE_NONCORE".equals(entry.group()))
                    throw new IllegalArgumentException("Noncanon mode requires IDEAVERSE_CORE or IDEAVERSE_NONCORE group");
                if(mode!=RagRetrievalMode.GENERIC) validateRelativePath(entry.relativePath());
            }
        }

    }

}
