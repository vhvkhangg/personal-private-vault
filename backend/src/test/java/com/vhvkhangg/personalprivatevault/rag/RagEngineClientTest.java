package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;
import org.junit.jupiter.api.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;

@Timeout(10)
class RagEngineClientTest {
    private HttpServer server;
    private final AtomicReference<String> reply=new AtomicReference<>("{}");
    private RagEngineClient client;
    private final Set<UUID> transports=new HashSet<>();
    @BeforeEach void start() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{
            exchange.getRequestBody().close();
            byte[] body=reply.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,body.length);
            try(var out=exchange.getResponseBody()){out.write(body);}
        });
        server.start();
        client=new RagEngineClient("http://127.0.0.1:"+server.getAddress().getPort(),"synthetic-client-test-credential-000000000",1);
    }
    @AfterEach void stop() {
        server.stop(0);
        for(UUID job:transports) {
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(2))
                    .until(() -> client.stopAndVerifyTransport(job));
            client.forgetTransport(job);
        }
    }
    @Test void malformedUnknownAndUnavailableQuiescenceNeverEstablishCleanup() {
        UUID job=UUID.randomUUID();
        for(String response:List.of("{}","{\"quiescent\":true}","{\"quiescent\":\"true\"}",
                "{\"quiescent\":true,\"active_workers\":0,\"cleanup_verified\":true,\"job_id\":\"wrong\"}")) {
            reply.set(response);assertThat(client.isJobQuiescent(job)).as(response).isFalse();
        }
        server.stop(0);
        assertThat(client.isJobQuiescent(job)).isFalse();
    }
    @Test void wrongStageDigestScopeJobAndUncommittedResponseAreRejected() throws Exception {
        var grant=new RagWorkerGrant(UUID.randomUUID(),UUID.randomUUID(),"a".repeat(64),"SYNTHETIC_TEST","Synthetic","b".repeat(64),"c".repeat(64),"d".repeat(64));
        transports.add(grant.jobId());
        Map<String,Object> receipt=new TreeMap<>(Map.of("contract_version","2.0","status","STAGED","job_id",grant.jobId().toString(),
                "attempt_id",grant.attemptId().toString(),"stage_type",grant.stageType(),"input_digest",grant.inputDigest(),
                "scope_digest",grant.scopeDigest(),"output_digest",grant.outputDigest(),"output_kind","SYNTHETIC_DIGEST"));
        for(String field:List.of("stage_type","output_digest","scope_digest","job_id","contract_version","output_kind")) {
            var invalid=new TreeMap<>(receipt);invalid.put(field,"invalid");
            reply.set(new ObjectMapper().writeValueAsString(Map.of("contract_version","2.0","status","STAGED",
                    "job_id",grant.jobId().toString(),"attempt_id",grant.attemptId().toString(),"staged_row_id",1,"receipt",invalid)));
            assertThatThrownBy(()->client.triggerBoundWorkerStage(grant,UUID.randomUUID(),UUID.randomUUID())).isInstanceOf(IllegalStateException.class).hasMessageContaining("provenance");
        }
        reply.set(new ObjectMapper().writeValueAsString(Map.of("status","STAGED","receipt",receipt)));
        assertThatThrownBy(()->client.triggerBoundWorkerStage(grant,UUID.randomUUID(),UUID.randomUUID())).isInstanceOf(IllegalStateException.class);
    }
    @Test void batchComputationCanOutlastTheShortControlTimeout() throws Exception {
        var grant=new RagWorkerGrant(UUID.randomUUID(),UUID.randomUUID(),"a".repeat(64),"DOCUMENT_BATCH","Synthetic","b".repeat(64),"c".repeat(64),"d".repeat(64));
        transports.add(grant.jobId());server.removeContext("/");
        byte[] body=new ObjectMapper().writeValueAsBytes(Map.of("contract_version","3.0","status","STAGED",
                "job_id",grant.jobId().toString(),"attempt_id",grant.attemptId().toString()));
        server.createContext("/",exchange -> {
            exchange.getRequestBody().close();
            try {Thread.sleep(1250);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();}
            exchange.sendResponseHeaders(200,body.length);
            try(var out=exchange.getResponseBody()){out.write(body);}
        });
        long started=System.nanoTime();
        assertThat(client.triggerBoundWorkerBatch(grant,UUID.randomUUID(),UUID.randomUUID(),List.of(Map.of("root_key","root")))).contains("STAGED");
        assertThat(java.time.Duration.ofNanos(System.nanoTime()-started)).isGreaterThan(java.time.Duration.ofSeconds(1));
    }
    @Test void rejectsRemoteDnsProxyCredentialsAndKnownDevelopmentSecret() {
        for(String origin:List.of("http://example.com","http://localhost:8001","http://user@127.0.0.1:8001",
                "https://127.0.0.1:8001","http://127.0.0.1:8001/path")) {
            assertThatThrownBy(()->new RagEngineClient(origin,"x".repeat(32),1)).isInstanceOf(IllegalArgumentException.class);
        }
        var noCredential=new RagEngineClient("http://127.0.0.1:8001","",1);
        assertThat(noCredential.isJobQuiescent(UUID.randomUUID())).isFalse();
    }

    @Test void responseBodyStallAndOversizeHaveBoundedFailClosedResults() throws Exception {
        reply.set("x".repeat(10000));
        assertThat(client.isJobQuiescent(UUID.randomUUID())).isFalse();
        server.removeContext("/");
        var release=new java.util.concurrent.CountDownLatch(1);
        server.createContext("/",exchange -> {
            exchange.getRequestBody().close();exchange.sendResponseHeaders(200,100);
            exchange.getResponseBody().write('{');exchange.getResponseBody().flush();
            try {release.await(3,java.util.concurrent.TimeUnit.SECONDS);}
            catch(InterruptedException interrupted) {Thread.currentThread().interrupt();}
            finally {exchange.close();}
        });
        long start=System.nanoTime();
        try {assertThat(client.isJobQuiescent(UUID.randomUUID())).isFalse();}
        finally {release.countDown();}
        assertThat(java.time.Duration.ofNanos(System.nanoTime()-start)).isLessThan(java.time.Duration.ofSeconds(2));
    }
}
