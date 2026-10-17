package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeSecurity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerGrantService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Owned second-JVM fixture. No invented controller/process identities or production hooks. */
public final class RagRecoveryProbeProcess {
    private RagRecoveryProbeProcess() {}

    public static void main(String[] args) throws Exception {
        var watchdog=Executors.newSingleThreadScheduledExecutor();
        watchdog.schedule(() -> Runtime.getRuntime().halt(75),150,TimeUnit.SECONDS);
        Path receipt=Path.of(args[1]);
        try {
            String configuration=System.getenv("PPV_PROBE_CONFIGURATION");
            Map<String,Object> properties=new ObjectMapper().readValue(configuration,new TypeReference<>() {});
            String[] springArguments=properties.entrySet().stream()
                .map(property -> "--"+property.getKey()+"="+property.getValue()).toArray(String[]::new);
            try(var application=new SpringApplicationBuilder(PersonalPrivateVaultApplication.class)
                    .run(springArguments)) {
                var jdbc=application.getBean(JdbcTemplate.class);
                var rag=application.getBean(RagOperations.class);
                UUID job;
                Socket buffered=null;
                var result=new LinkedHashMap<String,Object>();
                if("RECOVER_EXIT".equals(args[0])) {
                    job=UUID.fromString(args[2]);
                    application.getBean(RagRecoveryService.class).reconcile();
                    boolean current=application.getBean(RagRuntimeSecurity.class).isCurrentRuntime();
                    result.put("current_runtime",current);
                    if(!current) {
                        long source=jdbc.queryForObject("SELECT source_id FROM rag.job_leases WHERE job_id=?",Long.class,job);
                        boolean denied=false;
                        try { RagTestCommands.index(rag,source); }
                        catch(IllegalStateException expected) { denied=true; }
                        if(!denied) throw new IllegalStateException("Live controller exclusion failed");
                        result.put("manual_admission_denied",true);
                    }
                } else {
                    long source=Long.parseLong(args[2]);
                    var attempt=RagTestCommands.index(rag,source);
                    job=attempt.jobId();
                    result.put("attempt_id",attempt.attemptId().toString());
                    if(!"ADMITTED_WAIT".equals(args[0])) {
                        var supervisor=application.getBean(RagWorkerSupervisor.class);
                        supervisor.begin(job,attempt.attemptId());
                        var grants=application.getBean(RagWorkerGrantService.class);
                        var grant=grants.issue(job,attempt.attemptId(),"SYNTHETIC_TEST");
                        if("STAGED_WAIT".equals(args[0])) {
                            application.getBean(RagEngineClient.class).triggerBoundWorkerStage(
                                grant,grants.executionSession(job,attempt.attemptId()),
                                supervisor.requireReady(job,attempt.attemptId()));
                        } else if("BUFFERED_WAIT".equals(args[0])) {
                            var transport=application.getBean(RagEngineClient.class);
                            UUID session=grants.executionSession(job,attempt.attemptId());
                            UUID nonce=supervisor.requireReady(job,attempt.attemptId());
                            byte[] body=new ObjectMapper().writeValueAsBytes(Map.of(
                                "contract_version","2.0","job_id",job.toString(),"attempt_id",attempt.attemptId().toString(),
                                "lease_token",grant.token(),"stage_type",grant.stageType(),"payload",grant.payload(),
                                "input_digest",grant.inputDigest(),"scope_digest",grant.scopeDigest()));
                            buffered=new Socket();
                            buffered.connect(new InetSocketAddress("127.0.0.1",transport.port()),3000);
                            String headers="POST /v1/worker/execute HTTP/1.1\r\nHost: 127.0.0.1:"+transport.port()
                                +"\r\nContent-Type: application/json\r\nConnection: close\r\nContent-Length: "+body.length
                                +"\r\nX-RAG-Service-Key: "+transport.serviceCredential()+"\r\nX-RAG-Job-Id: "+job
                                +"\r\nX-RAG-Attempt-Id: "+attempt.attemptId()+"\r\nX-RAG-Lease-Token: "+grant.token()
                                +"\r\nX-RAG-Session-Id: "+session+"\r\nX-RAG-Endpoint-Nonce: "+nonce+"\r\n\r\n";
                            buffered.getOutputStream().write(headers.getBytes(StandardCharsets.US_ASCII));
                            // All selected source bytes may be buffered, but the typed
                            // body is deliberately incomplete. No final JSON byte follows.
                            buffered.getOutputStream().write(body,0,body.length-1);
                            buffered.getOutputStream().flush();
                            UUID observedJob=job;
                            org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(3))
                                .pollInterval(Duration.ofMillis(10)).until(() -> jdbc.queryForObject(
                                    "SELECT count(*) FROM rag.worker_grants WHERE job_id=? AND consumed_at IS NOT NULL",
                                    Integer.class,observedJob)==1);
                            result.put("body_claim_committed",true);
                            result.put("partial_body_bytes_sent",body.length-1);
                        } else if(!"ISSUED_WAIT".equals(args[0])) {
                            throw new IllegalArgumentException("Unknown owned probe mode");
                        }
                    }
                }
                result.put("success",true);
                result.put("mode",args[0]);
                result.put("job_id",job.toString());
                result.put("controller_pid",ProcessHandle.current().pid());
                result.put("controller_started_at",ProcessHandle.current().info().startInstant().orElseThrow().toString());
                result.put("runtime_id",application.getBean(RagRuntimeIdentity.class).instance().toString());
                result.put("registered_runtime",jdbc.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class).toString());
                result.put("status",jdbc.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job));
                result.put("reserved_sources",jdbc.queryForObject("SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",Integer.class));
                result.put("staged_receipts",jdbc.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,job));
                result.put("issued_grants",jdbc.queryForObject("SELECT count(*) FROM rag.worker_grants WHERE job_id=?",Integer.class,job));
                result.put("publications",jdbc.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job));
                emit(receipt,result);
                if(!"RECOVER_EXIT".equals(args[0])) {
                    // Parent signals a genuine abrupt controller death: no Spring close,
                    // no @PreDestroy, no graceful worker/SQL cleanup and no simulated PID.
                    int command=System.in.read();
                    if(buffered!=null && buffered.isClosed()) throw new IllegalStateException("Buffered transport closed prematurely");
                    Runtime.getRuntime().halt(command=='H'?73:74);
                }
            }
        } catch(Throwable failed) {
            emit(receipt,Map.of("success",false,"error_type",failed.getClass().getName(),"mode",args[0]));
            Runtime.getRuntime().halt(76);
        } finally {
            watchdog.shutdownNow();
        }
    }

    private static void emit(Path receipt,Map<String,Object> control) throws Exception {
        Files.createDirectories(receipt.getParent());
        Path temporary=receipt.resolveSibling(receipt.getFileName()+".tmp");
        byte[] bytes=new ObjectMapper().writeValueAsBytes(control);
        Files.write(temporary,bytes);
        Files.move(temporary,receipt,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }
}
