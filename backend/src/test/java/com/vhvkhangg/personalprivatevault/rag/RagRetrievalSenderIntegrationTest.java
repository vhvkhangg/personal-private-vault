package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagQueryEmbeddingSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.*;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PersonalPrivateVaultApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.context.annotation.Import(RagRetrievalSenderIntegrationTest.BlockedOutputConfiguration.class)
class RagRetrievalSenderIntegrationTest extends AbstractRagIntegrationTest {
    private record OutputBarrier(String key, java.util.concurrent.CountDownLatch entered,
                                 java.util.concurrent.CountDownLatch release,boolean notReady) {
        OutputBarrier(String key,java.util.concurrent.CountDownLatch entered,java.util.concurrent.CountDownLatch release) {this(key,entered,release,false);}
    }
    private static final java.util.concurrent.atomic.AtomicReference<jakarta.servlet.WriteListener> delayedListener=new java.util.concurrent.atomic.AtomicReference<>();
    private static final java.util.concurrent.atomic.AtomicReference<OutputBarrier> blockedOutput = new java.util.concurrent.atomic.AtomicReference<>();

    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods=false)
    static class BlockedOutputConfiguration {
        @org.springframework.context.annotation.Bean
        org.springframework.boot.web.servlet.FilterRegistrationBean<jakarta.servlet.Filter> blockedOutputFilter() {
            jakarta.servlet.Filter filter=(request,response,chain) -> {
                OutputBarrier gate=blockedOutput.get();
                if (!(request instanceof jakarta.servlet.http.HttpServletRequest http) || gate==null
                        || !gate.key().equals(http.getHeader("X-PPV-Test-Output-Barrier"))) {
                    chain.doFilter(request,response); return;
                }
                var wrapped=new jakarta.servlet.http.HttpServletResponseWrapper((jakarta.servlet.http.HttpServletResponse)response) {
                    private jakarta.servlet.ServletOutputStream stream;
                    @Override public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
                        if(stream==null) {
                            var delegate=super.getOutputStream();
                            stream=new jakarta.servlet.ServletOutputStream() {
                                private boolean entered;
                                @Override public void write(int value) throws IOException { write(new byte[]{(byte)value},0,1); }
                                @Override public void write(byte[] bytes,int offset,int length) throws IOException {
                                    if(!entered) {
                                        entered=true;
                                        if("true".equals(http.getHeader("X-PPV-Test-Commit-First-Byte"))) {
                                            delegate.write(bytes,offset,1);delegate.flush();offset++;length--;
                                        }
                                        gate.entered().countDown();
                                        try {
                                            if(!gate.release().await(15,java.util.concurrent.TimeUnit.SECONDS)) throw new IOException("Test output barrier expired");
                                        } catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new IOException("Test output interrupted"); }
                                    }
                                    delegate.write(bytes,offset,length);
                                }
                                @Override public void flush() throws IOException { delegate.flush(); }
                                @Override public boolean isReady() {
                                    if(gate.notReady() && getHeader("Content-Length")!=null && gate.release().getCount()!=0) {gate.entered().countDown();return false;}
                                    return delegate.isReady();
                                }
                                @Override public void setWriteListener(jakarta.servlet.WriteListener listener) { delayedListener.set(listener);delegate.setWriteListener(listener); }
                            };
                        }
                        return stream;
                    }
                };
                var wrappedRequest=new jakarta.servlet.http.HttpServletRequestWrapper(http) {
                    @Override public jakarta.servlet.AsyncContext startAsync() {
                        var context=super.startAsync(this,wrapped);
                        if(!"true".equals(http.getHeader("X-PPV-Test-Short-Deadline"))) return context;
                        return (jakarta.servlet.AsyncContext)java.lang.reflect.Proxy.newProxyInstance(
                                jakarta.servlet.AsyncContext.class.getClassLoader(),new Class<?>[]{jakarta.servlet.AsyncContext.class},
                                (proxy,method,args)->{
                                    if(method.getName().equals("setTimeout")) args=new Object[]{150L};
                                    try { return method.invoke(context,args); }
                                    catch(java.lang.reflect.InvocationTargetException failed) { throw failed.getCause(); }
                                });
                    }
                };
                chain.doFilter(wrappedRequest,wrapped);
            };
            var registration=new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
            registration.setAsyncSupported(true);
            registration.addUrlPatterns("/api/v1/rag/retrievals");
            return registration;
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RagRetrievalOperations ragRetrievalOperations;

    @Autowired
    private RagRetrievalAuthority ragRetrievalAuthority;

    @Autowired
    private RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor;

    @Autowired
    private RagSourceRepository ragSourceRepository;

    @Autowired
    private RagExternalFileSourceRepository ragExternalFileSourceRepository;

    @Autowired
    private RagIndexGenerationRepository ragIndexGenerationRepository;

    @Autowired
    private RagDocumentChunkRepository ragDocumentChunkRepository;

    @Autowired
    private RagRetrievalReaderRepository ragRetrievalReaderRepository;

    @Autowired
    private RagRetrievalInspectionRepository ragRetrievalInspectionRepository;

    @Autowired
    private RagOriginalBytesStore ragOriginalBytesStore;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority ragSourceAuthority;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalCurrentAuthority currentAuthority;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService retrievalService;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity runtime;

    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @DynamicPropertySource
    static void enableRetrieval(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @BeforeEach
    void setUp() {
        ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> makeNormalizedVector(0.5f));
    }

    @AfterEach
    void tearDown() {
        ragQueryEmbeddingSupervisor.setVectorOverride(null);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"deadline","disconnect"})
    void actualDeadlineOrDisconnectCannotReleaseBlockedApplicationWriter(String mode) throws Exception {
        var data=createVerifiedMarkdownDocument("# Writer\nWriter must leave before release.","writer.md");
        var gate=new OutputBarrier(UUID.randomUUID().toString(),new java.util.concurrent.CountDownLatch(1),new java.util.concurrent.CountDownLatch(1),"deadline".equals(mode));
        blockedOutput.set(gate);UUID reader=UUID.randomUUID();
        var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,null)),null,null,5);
        var socket=new Socket("127.0.0.1",port);
        try {
            byte[] payload=json.writeValueAsBytes(command);
            String headers="POST /api/v1/rag/retrievals HTTP/1.1\r\nHost: localhost\r\nAuthorization: "+bearerHeader()
                    +"\r\nContent-Type: application/json\r\nX-PPV-Test-Output-Barrier: "+gate.key()
                    +("deadline".equals(mode)?"\r\nX-PPV-Test-Short-Deadline: true":"")
                    +"\r\nConnection: close\r\nContent-Length: "+payload.length+"\r\n\r\n";
            socket.getOutputStream().write(headers.getBytes(StandardCharsets.UTF_8));socket.getOutputStream().write(payload);socket.getOutputStream().flush();
            assertThat(gate.entered().await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            if("deadline".equals(mode)) {
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(4)).until(()->
                        ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().isStopRequested());
            } else { socket.setSoLinger(true,0);socket.close(); }
            if(!"deadline".equals(mode)) {
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isNotEqualTo("TERMINAL");
                assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_require_editable(ARRAY[?]::uuid[])",Object.class,data.fileSourceId()))
                        .hasMessageContaining("RAG_SOURCE_BUSY");
            }
            gate.release().countDown();
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(8)).untilAsserted(()->{
                retrievalService.reconcileCompletedSenders();
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("TERMINAL");
            });
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNull();
        } finally { gate.release().countDown();blockedOutput.compareAndSet(gate,null);socket.close(); }
    }

    @Test void healthyWritableDeadlineBeforeEmissionReturnsRootErrorAndRetainsReaderUntilJoin() throws Exception {
        var data=createVerifiedMarkdownDocument("# Deadline\nPRE_EMISSION_DEADLINE_EVIDENCE_SENTINEL.","deadline-before.md");
        UUID reader=UUID.randomUUID();
        var entered=new java.util.concurrent.CountDownLatch(1);var release=new java.util.concurrent.CountDownLatch(1);
        var gate=new OutputBarrier(UUID.randomUUID().toString(),new java.util.concurrent.CountDownLatch(1),new java.util.concurrent.CountDownLatch(0),false);
        blockedOutput.set(gate);
        ragQueryEmbeddingSupervisor.setVectorOverride((id,question)->{
            entered.countDown();
            try {if(!release.await(8,java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Deadline test barrier expired");}
            catch(InterruptedException interrupted) {Thread.currentThread().interrupt();throw new IllegalStateException(interrupted);}
            return makeNormalizedVector(0.5f);
        });
        try {
            var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,
                    List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,null)),null,null,5);
            var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                    .timeout(java.time.Duration.ofSeconds(12)).header("Authorization",bearerHeader()).header("Content-Type","application/json")
                    .header("X-PPV-Test-Output-Barrier",gate.key()).header("X-PPV-Test-Short-Deadline","true")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            var response=httpClient.sendAsync(request,HttpResponse.BodyHandlers.ofString());
            assertThat(entered.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().isEmissionStarted()).isFalse();
            assertThat(gate.notReady()).isFalse();assertThat(gate.release().getCount()).isZero();
            var actual=response.get(5,java.util.concurrent.TimeUnit.SECONDS);
            assertThat(actual.statusCode()).isEqualTo(503);
            assertThat(actual.body()).isNotBlank().doesNotContain("PRE_EMISSION_DEADLINE_EVIDENCE_SENTINEL");
            assertThat(gate.entered().getCount()).as("actual ready callback wrote through the unblocked stream").isZero();
            assertThat(json.readTree(actual.body()).path("error").path("code").asText()).isEqualTo("RAG_QUERY_UNAVAILABLE");
            assertThat(actual.headers().firstValue("Content-Type")).hasValueSatisfying(value->assertThat(value).contains("application/json"));
            assertThat(actual.headers().firstValue("Cache-Control")).contains("no-store");
            var stopped=ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow();
            assertThat(stopped.isStopRequested()).isTrue();assertThat(stopped.isEmissionStarted()).isFalse();
            assertThat(stopped.getState()).isNotEqualTo("TERMINAL");
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNotNull();
            release.countDown();
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(6)).untilAsserted(()->{
                var terminal=ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow();
                assertThat(terminal.getState()).isEqualTo("TERMINAL");assertThat(terminal.getEmissionDisposition()).isEqualTo("ABORTED");
                assertThat(terminal.getQuiescenceEvidence()).containsEntry("context_complete",true).containsEntry("sender_quiescent",true)
                        .containsEntry("native_quiescent",true).containsEntry("transport_quiescent",true).containsEntry("buffers_discarded",true);
            });
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNull();
        } finally {
            release.countDown();blockedOutput.compareAndSet(gate,null);delayedListener.set(null);
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(()->{
                retrievalService.reconcileCompletedSenders();
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("TERMINAL");
            });
            ragQueryEmbeddingSupervisor.setVectorOverride((id,question)->makeNormalizedVector(0.5f));
        }
    }

    @Test void deadlineAfterCommittedPrefixCannotAppendErrorOrReleaseActiveWriter() throws Exception {
        var data=createVerifiedMarkdownDocument("# Committed\nAuthorized evidence before deadline.","committed.md");
        UUID reader=UUID.randomUUID();
        var gate=new OutputBarrier(UUID.randomUUID().toString(),new java.util.concurrent.CountDownLatch(1),new java.util.concurrent.CountDownLatch(1));
        blockedOutput.set(gate);
        try(var socket=new Socket("127.0.0.1",port)) {
            socket.setSoTimeout(2500);
            var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,
                    List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,null)),null,null,5);
            byte[] payload=json.writeValueAsBytes(command);
            String headers="POST /api/v1/rag/retrievals HTTP/1.1\r\nHost: localhost\r\nAuthorization: "+bearerHeader()
                    +"\r\nContent-Type: application/json\r\nX-PPV-Test-Output-Barrier: "+gate.key()
                    +"\r\nX-PPV-Test-Short-Deadline: true\r\nX-PPV-Test-Commit-First-Byte: true"
                    +"\r\nConnection: close\r\nContent-Length: "+payload.length+"\r\n\r\n";
            socket.getOutputStream().write(headers.getBytes(StandardCharsets.UTF_8));socket.getOutputStream().write(payload);socket.getOutputStream().flush();
            assertThat(gate.entered().await(5,java.util.concurrent.TimeUnit.SECONDS)).as("success prefix actually flushed before blocking").isTrue();
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().isEmissionStarted()).isTrue();
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(4)).until(()->
                    ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().isStopRequested());
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isNotEqualTo("TERMINAL");
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNotNull();
            gate.release().countDown();
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(()->{
                retrievalService.reconcileCompletedSenders();
                var terminal=ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow();
                assertThat(terminal.getState()).isEqualTo("TERMINAL");assertThat(terminal.getEmissionDisposition()).isEqualTo("ABORTED");
            });
            byte[] wire=socket.getInputStream().readNBytes(128*1024+8192);
            String actual=new String(wire,StandardCharsets.UTF_8);
            assertThat(actual).startsWith("HTTP/1.1 200").doesNotContain("RAG_QUERY_UNAVAILABLE","RAG_RETRIEVAL_CANCELLED");
            assertThat(actual.substring(actual.indexOf("\r\n\r\n")+4)).startsWith("{");
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNull();
        } finally {gate.release().countDown();blockedOutput.compareAndSet(gate,null);delayedListener.set(null);}
    }

    @Test void notReadySenderCancelsWithoutReleasingAWriteBarrierAndLateCallbackCannotReplay() throws Exception {
        var data=createVerifiedMarkdownDocument("# Nonblocking\nNONBLOCKING_EVIDENCE_SENTINEL.","ready.md");
        var gate=new OutputBarrier(UUID.randomUUID().toString(),new java.util.concurrent.CountDownLatch(1),new java.util.concurrent.CountDownLatch(1),true);
        blockedOutput.set(gate);UUID reader=UUID.randomUUID();
        try {
            var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,null)),null,null,5);
            var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                    .header("Authorization",bearerHeader()).header("Content-Type","application/json").header("X-PPV-Test-Output-Barrier",gate.key())
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            var response=httpClient.sendAsync(request,HttpResponse.BodyHandlers.ofString());
            assertThat(gate.entered().await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("EMITTING");
            assertThat(ragRetrievalOperations.cancel(reader).status()).isEqualTo("CANCELLED");
            assertThat(gate.release().getCount()).isEqualTo(1); // No write-barrier release was needed to stop.
            delayedListener.get().onWritePossible();
            assertThat(response.get(5,java.util.concurrent.TimeUnit.SECONDS).body()).doesNotContain("NONBLOCKING_EVIDENCE_SENTINEL");
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("TERMINAL");
        } finally {gate.release().countDown();blockedOutput.compareAndSet(gate,null);delayedListener.set(null);}
    }

    @Test
    @SuppressWarnings("unchecked")
    void falseNativeStopRetainsHandleAndReaderUntilPositiveOwnedReconciliation() throws Exception {
        var data=createVerifiedMarkdownDocument("# Native stop\nRetained evidence.","stop.md");
        UUID reader=UUID.randomUUID();
        var entered=new java.util.concurrent.CountDownLatch(1);
        var release=new java.util.concurrent.CountDownLatch(1);
        ragQueryEmbeddingSupervisor.setVectorOverride((id,q)->{
            entered.countDown();
            try { if(!release.await(10,java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Test barrier expired"); }
            catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
            return makeNormalizedVector(0.5f);
        });
        var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,null)),null,null,5);
        var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                .header("Authorization",bearerHeader()).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
        var response=httpClient.sendAsync(request,HttpResponse.BodyHandlers.ofString());
        assertThat(entered.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        var env=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        for(String key:List.of("SystemRoot","WINDIR","TEMP","TMP","PATH")) if(System.getenv(key)!=null) env.put(key,System.getenv(key));
        var binary=java.nio.file.Path.of(System.getProperty("java.home"),"bin","java.exe");
        var classes=java.nio.file.Path.of(RagRetrievalRecoveryControllerFixture.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var process=org.mockito.Mockito.spy(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess.launchSuspended(
                UUID.randomUUID(),binary,List.of("-Xmx64m","-cp",classes.toString(),RagRetrievalRecoveryControllerFixture.class.getName()),
                env,java.nio.file.Path.of("").toAbsolutePath(),512L*1024*1024,1));
        var field=RagQueryEmbeddingSupervisor.class.getDeclaredField("activeProcesses");field.setAccessible(true);
        var handles=(Map<UUID,com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess>)field.get(ragQueryEmbeddingSupervisor);
        try {
            ragRetrievalAuthority.bindWorker(reader,runtime.instance().toString(),process.pid(),process.creationTime(),process.creationTimeFileTime());
            handles.put(reader,process);process.resume();
            org.mockito.Mockito.doReturn(false).when(process).terminateAndVerify(org.mockito.ArgumentMatchers.any());
            assertThatThrownBy(()->ragRetrievalOperations.cancel(reader)).isInstanceOf(RagQueryUnavailableException.class);
            assertThat(handles).containsKey(reader);assertThat(process.hasTerminatedTree()).isFalse();
            release.countDown();response.get(7,java.util.concurrent.TimeUnit.SECONDS);
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(()->{
                org.mockito.Mockito.verify(process,org.mockito.Mockito.atLeast(2)).terminateAndVerify(org.mockito.ArgumentMatchers.any());
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isNotEqualTo("TERMINAL");
                assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNotNull();
            });
        } finally {
            release.countDown();org.mockito.Mockito.doCallRealMethod().when(process).terminateAndVerify(org.mockito.ArgumentMatchers.any());
            ragQueryEmbeddingSupervisor.stopWorker(reader);
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(()->{
                retrievalService.reconcileCompletedSenders();
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("TERMINAL");
            });
        }
        assertThat(handles).doesNotContainKey(reader);
        assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNull();
    }

    @Test
    void blockedRealHttpWriteRetainsExclusionAndDefersIncidentDenialUntilOwnedFinish() throws Exception {
        var data=createVerifiedMarkdownDocument("# Stall\nEvidence remains guarded.","stall.md");
        var gate=new OutputBarrier(UUID.randomUUID().toString(),new java.util.concurrent.CountDownLatch(1),new java.util.concurrent.CountDownLatch(1));
        blockedOutput.set(gate);
        UUID reader=UUID.randomUUID();
        var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,"stall.md")),null,null,5);
        try {
            var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                    .header("Authorization",bearerHeader()).header("Content-Type","application/json")
                    .header("X-PPV-Test-Output-Barrier",gate.key())
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            var response=httpClient.sendAsync(request,HttpResponse.BodyHandlers.ofString());
            assertThat(gate.entered().await(7,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("EMITTING");
            UUID inspection=UUID.randomUUID();
            ragRetrievalAuthority.beginInspection(inspection,null,"TRUSTED_TEST_INSPECTOR","stall-proof",data.fileSourceId(),data.revisionId());
            ragRetrievalAuthority.recordInspection(inspection,"INCIDENT_PENDING",true,Map.of());
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().isSnapshotIntegrityFailed()).isFalse();
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNotNull();
            // Independent connection can acquire the row lock while output is paused: no I/O-spanning SQL guard.
            assertThat(jdbcTemplate.queryForObject("SELECT file_source_id FROM rag.external_file_sources WHERE file_source_id=? FOR UPDATE NOWAIT",
                    UUID.class,data.fileSourceId())).isEqualTo(data.fileSourceId());
            assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT rag.external_file_require_editable(ARRAY[?]::uuid[])",
                    Object.class,data.fileSourceId())).hasMessageContaining("RAG_SOURCE_BUSY");
            long generation=ragIndexGenerationRepository.findBySourceId(data.source().getId()).getFirst().getId();
            assertThatThrownBy(()->jdbcTemplate.update("DELETE FROM rag.document_chunks WHERE generation_id=?",generation)).hasMessageContaining("active retrieval reader");
            assertThatThrownBy(()->jdbcTemplate.update("DELETE FROM rag.generation_roots WHERE generation_id=?",generation)).hasMessageContaining("active retrieval reader");
            assertThatThrownBy(()->jdbcTemplate.update("UPDATE rag.index_generations SET status='REVOKED' WHERE id=?",generation)).hasMessageContaining("active retrieval reader");
            assertThat(response.isDone()).isFalse();
            gate.release().countDown();
            assertThat(response.get(7,java.util.concurrent.TimeUnit.SECONDS).statusCode()).isEqualTo(200);
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(() -> {
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("TERMINAL");
                assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getFinishDisposition()).isEqualTo("INCIDENT_DENIED");
                assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().isSnapshotIntegrityFailed()).isTrue();
                assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNull();
            });
        } finally { gate.release().countDown(); blockedOutput.compareAndSet(gate,null); }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"source_version","consent_reset_version","authorization_epoch","field_policy_version","revision","pipeline"})
    void mutationBetweenCanonicalPreparationAndAdmissionCannotReuseStaleProof(String mutation) throws Exception {
        TestData data = createVerifiedMarkdownDocument("# Authority\nExact proof.", "authority.md");
        var generation = ragIndexGenerationRepository.findBySourceId(data.source().getId()).getFirst();
        var proof = currentAuthority.prepare(generation);
        switch(mutation) {
            case "source_version" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET source_version=source_version+1 WHERE file_source_id=?",data.fileSourceId());
            case "consent_reset_version" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET source_version=source_version+1,consent_reset_version=source_version+1 WHERE file_source_id=?",data.fileSourceId());
            case "authorization_epoch" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET authorization_epoch=authorization_epoch+1 WHERE file_source_id=?",data.fileSourceId());
            case "field_policy_version" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET field_policy_version=field_policy_version+1 WHERE file_source_id=?",data.fileSourceId());
            case "revision" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET selected_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),data.fileSourceId());
            case "pipeline" -> jdbcTemplate.update("UPDATE rag.generation_roots SET model_version='wrong-pipeline' WHERE generation_id=?",generation.getId());
            default -> throw new IllegalArgumentException("Unknown test mutation");
        }
        UUID reader=UUID.randomUUID();
        assertThatThrownBy(() -> ragRetrievalAuthority.admitPrepared(reader,UUID.randomUUID(),"stale-proof",
                Map.of("external_file_ids",List.of(data.fileSourceId().toString())),List.of(data.fileSourceId()),
                "test-controller",List.of(proof))).isInstanceOf(RuntimeException.class);
        assertThat(ragRetrievalReaderRepository.findByReaderId(reader)).isEmpty();
        assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNull();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"INDEXED TEXT\n", "# Unicode\r\n😀 tiếng Việt 漢字\r\n", "prose Architecture\n"})
    void exactHalfOpenCodePointSpansResolveWithoutUnverifiedHeading(String markdown) throws Exception {
        TestData data=createVerifiedMarkdownDocument(markdown,"span.md");
        var result=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"where?",RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,"span.md")),null,null,5));
        assertThat(result.matches()).hasSize(1);
        var match=result.matches().getFirst();
        assertThat(match.excerpt()).isEqualTo(markdown);
        assertThat(match.locator().charEnd()).isEqualTo((long)markdown.codePointCount(0,markdown.length()));
        assertThat(match.locator().heading()).isNull();
    }

    @Test
    @DisplayName("Normal unmodified response over real HTTP loopback socket with verified evidence")
    void normalRetrievalOverLoopbackSocketSucceeds() throws Exception {
        TestData data = createVerifiedMarkdownDocument("# Architecture\nBounded retrieval verified.", "doc.md");

        UUID requestId = UUID.randomUUID();
        RagRetrievalRequestDto requestDto = new RagRetrievalRequestDto(
                requestId,
                "What is bounded retrieval?",
                RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntryDto(data.source().getId(), data.revisionId(), null, "doc.md")),
                null,
                null,
                5
        );

        String jsonBody = json.writeValueAsString(requestDto);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/rag/retrievals"))
                .header("Content-Type", "application/json")
                .header("Authorization", bearerHeader())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(httpResponse.statusCode()).isEqualTo(200);
        assertThat(httpResponse.headers().firstValue("Content-Type")).isPresent();
        assertThat(httpResponse.headers().firstValue("Content-Type").get()).contains("application/json");
        assertThat(httpResponse.headers().firstValue("Cache-Control")).contains("no-store");

        var tree = json.readTree(httpResponse.body());
        assertThat(tree.path("data").isMissingNode()).isFalse();
        RagRetrievalResponseDto responseDto = json.treeToValue(tree.path("data"), RagRetrievalResponseDto.class);
        assertThat(responseDto.outcome()).isEqualTo(RagRetrievalOutcome.EVIDENCE);
        assertThat(responseDto.matches()).isNotEmpty();
        assertThat(responseDto.matches().get(0).excerpt()).contains("Architecture");

        // Verify reader is in TERMINAL state with EMITTED disposition
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(() -> {
            RagRetrievalReader reader = ragRetrievalReaderRepository.findByReaderId(requestId).orElseThrow();
            assertThat(reader.getState()).isEqualTo("TERMINAL");
            assertThat(reader.getEmissionDisposition()).isEqualTo("EMITTED");
            assertThat(reader.isEmissionStarted()).isTrue();
        });
    }

    @Test void cancelBeforeEmissionReturnsRootErrorWhileComputationStillRetainsReader() throws Exception {
        var data=createVerifiedMarkdownDocument("# Cancel\nPRE_EMISSION_EVIDENCE_SENTINEL.","cancel-before.md");
        UUID reader=UUID.randomUUID();
        var entered=new java.util.concurrent.CountDownLatch(1);var release=new java.util.concurrent.CountDownLatch(1);
        ragQueryEmbeddingSupervisor.setVectorOverride((id,question)->{
            entered.countDown();
            try {if(!release.await(8,java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Cancellation test barrier expired");}
            catch(InterruptedException interrupted) {Thread.currentThread().interrupt();throw new IllegalStateException(interrupted);}
            return makeNormalizedVector(0.5f);
        });
        try {
            var command=new RagRetrievalCommand(reader,"where?",RagRetrievalMode.GENERIC,
                    List.of(new RagSourceSelectionEntry(data.source().getId(),data.revisionId(),null,null)),null,null,5);
            var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                    .timeout(java.time.Duration.ofSeconds(15)).header("Authorization",bearerHeader()).header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            var response=httpClient.sendAsync(request,HttpResponse.BodyHandlers.ofString());
            assertThat(entered.await(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().isEmissionStarted()).isFalse();
            var cancelRequest=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals/"+reader+"/cancel"))
                    .timeout(java.time.Duration.ofSeconds(10)).header("Authorization",bearerHeader())
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            var cancelling=httpClient.sendAsync(cancelRequest,HttpResponse.BodyHandlers.ofString());
            var actual=response.get(5,java.util.concurrent.TimeUnit.SECONDS);
            assertThat(actual.statusCode()).isEqualTo(409);
            assertThat(actual.body()).isNotBlank().doesNotContain("PRE_EMISSION_EVIDENCE_SENTINEL");
            assertThat(json.readTree(actual.body()).path("error").path("code").asText()).isEqualTo("RAG_RETRIEVAL_CANCELLED");
            assertThat(actual.headers().firstValue("Cache-Control")).contains("no-store");
            assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isNotEqualTo("TERMINAL");
            assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNotNull();
            release.countDown();
            assertThat(cancelling.get(6,java.util.concurrent.TimeUnit.SECONDS).statusCode()).isEqualTo(200);
            var terminal=ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow();
            assertThat(terminal.getState()).isEqualTo("TERMINAL");assertThat(terminal.getFinishDisposition()).isEqualTo("CANCELLED");
            assertThat(terminal.isEmissionStarted()).isFalse();assertThat(terminal.getEmissionDisposition()).isEqualTo("ABORTED");
        } finally {release.countDown();ragQueryEmbeddingSupervisor.setVectorOverride((id,question)->makeNormalizedVector(0.5f));}
    }

    @Test
    @DisplayName("R06: Cancel during active socket emission terminates cleanly with ABORTED disposition")
    void cancelDuringActiveEmissionAbortsCleanly() throws Exception {
        TestData data = createVerifiedMarkdownDocument("# Title\nContent for cancellation.", "cancel.md");
        UUID requestId = UUID.randomUUID();

        RagRetrievalRequestDto requestDto = new RagRetrievalRequestDto(
                requestId,
                "Testing cancel?",
                RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntryDto(data.source().getId(), data.revisionId(), null, "cancel.md")),
                null,
                null,
                5
        );

        java.util.concurrent.CountDownLatch inProgressLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch releaseLatch = new java.util.concurrent.CountDownLatch(1);

        ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> {
            inProgressLatch.countDown();
            try {
                releaseLatch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}
            return makeNormalizedVector(0.5f);
        });

        // Raw loopback socket to trigger request and then trigger cancel while in progress
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            String payload = json.writeValueAsString(requestDto);
            String httpRequest = "POST /api/v1/rag/retrievals HTTP/1.1\r\n"
                    + "Host: 127.0.0.1:" + port + "\r\n"
                    + "Authorization: " + bearerHeader() + "\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: " + payload.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Connection: close\r\n\r\n"
                    + payload;

            out.write(httpRequest.getBytes(StandardCharsets.UTF_8));
            out.flush();

            assertThat(inProgressLatch.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

            // Cancel the active retrieval via operations while actively running
            try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                var cancel = executor.submit(() -> ragRetrievalOperations.cancel(requestId));
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(3)).until(() ->
                        ragRetrievalReaderRepository.findByReaderId(requestId).orElseThrow().isStopRequested());
                assertThat(ragRetrievalReaderRepository.findByReaderId(requestId).orElseThrow().getState()).isNotEqualTo("TERMINAL");
                assertThat(ragExternalFileSourceRepository.findByFileSourceId(data.fileSourceId()).orElseThrow().getReservedBy()).isNotNull();
                releaseLatch.countDown();
                assertThat(cancel.get(7, java.util.concurrent.TimeUnit.SECONDS).status()).isEqualTo("CANCELLED");
            }

            // Wait briefly for reader state in DB
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(() -> {
                Optional<RagRetrievalReader> r = ragRetrievalReaderRepository.findByReaderId(requestId);
                assertThat(r).isPresent();
                assertThat(r.get().getState()).isEqualTo("TERMINAL");
                assertThat(r.get().isStopRequested()).isTrue();
            });
        } finally {
            releaseLatch.countDown();
            ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> makeNormalizedVector(0.5f));
        }
    }

    @Test
    @DisplayName("R04: Preregistered inspection incident before emission prevents verified evidence emission")
    void incidentBeforeEmissionYieldsNoEvidence() throws Exception {
        TestData data = createVerifiedMarkdownDocument("# Incident Document\nWill be corrupted.", "corrupt.md");
        UUID requestId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();

        // Preregister blocking inspection on this file source
        ragRetrievalAuthority.beginInspection(
                inspectionId, null, "IncidentTest", "digest:incident", data.fileSourceId(), data.revisionId()
        );
        ragRetrievalAuthority.recordInspection(inspectionId, "INCIDENT_PENDING", true, Map.of("reason", "corruption"));

        RagRetrievalRequestDto requestDto = new RagRetrievalRequestDto(
                requestId,
                "What is in corrupt doc?",
                RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntryDto(data.source().getId(), data.revisionId(), null, "corrupt.md")),
                null,
                null,
                5
        );

        String jsonBody = json.writeValueAsString(requestDto);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/api/v1/rag/retrievals"))
                .header("Content-Type", "application/json")
                .header("Authorization", bearerHeader())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Must fail with 503 or error status because inspection is blocking
        assertThat(httpResponse.statusCode()).isIn(503, 409, 400);
        assertThat(httpResponse.body()).doesNotContain("Will be corrupted.","corrupt.md","citationKey");

        // Clean up inspection
        ragRetrievalAuthority.recordInspection(inspectionId, "DENIED", false, Map.of());
    }

    private record TestData(RagSource source, UUID fileSourceId, UUID revisionId) {}

    private TestData createVerifiedMarkdownDocument(String markdown, String relativePath) throws Exception {
        UUID fileSourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();
        UUID genUuid = UUID.randomUUID();
        Instant now = Instant.now();

        byte[] bytes = markdown.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        String sha256 = HexFormat.of().formatHex(sha.digest(bytes));

        ragOriginalBytesStore.writeTemp(captureId, new ByteArrayInputStream(bytes), (long) bytes.length, sha256);
        ragOriginalBytesStore.sealRevision(fileSourceId, revisionId, captureId, sha256, (long) bytes.length, relativePath, List.of());

        String canonicalDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(("canonical:" + fileSourceId).getBytes(StandardCharsets.UTF_8)));
        String nameDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(relativePath.getBytes(StandardCharsets.UTF_8)));

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, canonicalDigest, 1L, 1L, 1L, 1, true, now
        );
        fileSource.setSelectedRevision(revisionId, sha256, (long) bytes.length, now);
        ragExternalFileSourceRepository.save(fileSource);

        RagSource source = new RagSource(
                RagSourceType.EXTERNAL_FILE,
                fileSourceId.toString(),
                RagClassification.NON_PRIVATE,
                1L, 1, Set.of("content"),
                RagAccessState.APPROVED,
                sha256,
                "manifest:digest",
                now
        );
        source.setFileIdentity(canonicalDigest + nameDigest);
        source.bindOwnerResetVersion(1L);
        ragSourceRepository.save(source);

        var prepared = ragSourceAuthority.prepareSource(source.getId(), Map.of());
        String manifest = prepared.manifest();
        String manifestDigest = com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest);

        UUID jobId = UUID.randomUUID();
        setupJobLeaseAndGrant(jobId, source.getId(), manifest);

        RagIndexGeneration generation = new RagIndexGeneration(
                genUuid, jobId, source.getId(), "PUBLISHED", 1L, sha256, manifestDigest, true, now
        );
        generation = ragIndexGenerationRepository.save(generation);
        saveGenerationRoots(generation.getId());

        float[] chunkVec = makeNormalizedVector(0.5f);
        Map<String, Object> locator = Map.of(
                "profile", "ppv-obsidian-source-v1",
                "source_key", fileSourceId.toString(),
                "source_version", 1L,
                "approved_field", "content",
                "char_start", 0L,
                "char_end", (long) markdown.codePointCount(0, markdown.length()),
                "line_start", 1L,
                "line_end", 1L + markdown.substring(0, markdown.offsetByCodePoints(markdown.length(), -1)).chars().filter(c -> c == '\n').count(),
                "heading", "Architecture"
        );
        Map<String, Object> constituentLocator = Map.of(
                "profile", "ppv-obsidian-source-v1",
                "source_key", fileSourceId.toString(),
                "source_version", 1L,
                "approved_field", "content",
                "char_start", 0L,
                "char_end", (long) markdown.codePointCount(0, markdown.length()),
                "line_start", 1L,
                "line_end", 1L + markdown.substring(0, markdown.offsetByCodePoints(markdown.length(), -1)).chars().filter(c -> c == '\n').count(),
                "heading", "Architecture"
        );
        Map<String, Object> structureMetadata = Map.of(
                "word_count", 5,
                "constituent_locators", List.of(constituentLocator)
        );
        RagDocumentChunk chunk = new RagDocumentChunk(
                generation.getId(),
                fileSourceId.toString(),
                markdown,
                locator,
                structureMetadata,
                chunkVec,
                now
        );
        ragDocumentChunkRepository.save(chunk);

        return new TestData(source, fileSourceId, revisionId);
    }

    private void setupJobLeaseAndGrant(UUID jobId, Long sourceId, String manifest) {
        UUID attemptId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO rag.job_leases(job_id, attempt_id, source_id, status, lease_token, leased_until, reservation_token, staging_closed, dependency_manifest, created_at, updated_at)
                VALUES (?, ?, ?, 'COMPLETED', 'test-token', clock_timestamp() + interval '1 hour', ?, true, ?, clock_timestamp(), clock_timestamp())
                """, jobId, attemptId, sourceId, attemptId, manifest);

        jdbcTemplate.update("""
                INSERT INTO rag.worker_grants(job_id, attempt_id, session_id, session_until, input_digest, scope_digest, output_digest, stage_type, created_at)
                VALUES (?, ?, ?, clock_timestamp() + interval '1 hour', '0000000000000000000000000000000000000000000000000000000000000000', '0000000000000000000000000000000000000000000000000000000000000000', '0000000000000000000000000000000000000000000000000000000000000000', 'SYNTHETIC_STAGE', clock_timestamp())
                """, jobId, attemptId, sessionId);
    }

    private void saveGenerationRoots(Long generationId) {
        jdbcTemplate.update("""
                INSERT INTO rag.generation_roots(generation_id, root_key, exact_dependency_manifest, model_artifact_hash, model_version, tokenizer_version, parser_version, chunker_version, ocr_pipeline_version, vector_dimensions, created_at)
                VALUES (?, 'root-1', jsonb_build_object('manifest_text', (SELECT dependency_manifest FROM rag.job_leases WHERE job_id=(SELECT job_id FROM rag.index_generations WHERE id=?))), '98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f', '2.0.0', 'hf-tokenizers-0.23.3', 'ppv-parser-v18.6-d17-html-container-boundaries', 'structure-first-v18.5-compact-source-context', 'rapidocr-1.4.4-en-hans-script-admission-v18.2', 384, clock_timestamp())
                """, generationId, generationId);
    }

    private float[] makeNormalizedVector(float base) {
        float[] v = new float[384];
        float sum = 0f;
        for (int i = 0; i < 384; i++) {
            v[i] = base + (i * 0.001f);
            sum += v[i] * v[i];
        }
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < 384; i++) {
            v[i] /= norm;
        }
        return v;
    }
}
