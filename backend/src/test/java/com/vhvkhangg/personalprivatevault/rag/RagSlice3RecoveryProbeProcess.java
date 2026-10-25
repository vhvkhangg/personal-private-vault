package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.awaitility.Awaitility.await;

/** Abrupt real controller death at native v3 boundaries; never a production entry point. */
public final class RagSlice3RecoveryProbeProcess {
    public static void main(String[] args) throws Exception {
        var watchdog=Executors.newSingleThreadScheduledExecutor();
        watchdog.schedule(()->Runtime.getRuntime().halt(75),150,TimeUnit.SECONDS);
        Path receipt=Path.of(args[1]);var json=new ObjectMapper();
        try {
            Map<String,Object> properties=json.readValue(System.getenv("PPV_PROBE_CONFIGURATION"),new TypeReference<>() {});
            var options=properties.entrySet().stream().map(p->"--"+p.getKey()+"="+p.getValue()).toArray(String[]::new);
            try(var application=new SpringApplicationBuilder(PersonalPrivateVaultApplication.class).run(options)) {
                var jdbc=application.getBean(JdbcTemplate.class);var rag=application.getBean(RagOperations.class);
                UUID job;var result=new LinkedHashMap<String,Object>();
                if(args[0].equals("CN_WAIT") || args[0].equals("CN_RECOVER")) {
                    var service=application.getBean(RagNoChangeService.class);UUID operation;
                    if(args[0].equals("CN_WAIT")) {
                        var proof=service.prepare(Long.parseLong(args[2]));operation=service.begin(Map.of("unchanged",proof),null);
                    } else {operation=UUID.fromString(args[2]);application.getBean(RagRecoveryService.class).reconcile();}
                    result.put("success",true);result.put("operation_id",operation.toString());result.put("controller_pid",ProcessHandle.current().pid());
                    result.put("status",jdbc.queryForObject("SELECT status FROM rag.runs WHERE run_id=? AND job_id IS NULL",String.class,operation));
                    result.put("reserved",jdbc.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,operation));
                    result.put("current_runtime",application.getBean(RagRuntimeSecurity.class).isCurrentRuntime());
                    result.put("indexing_jobs",jdbc.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class));
                    emit(receipt,result);if(args[0].equals("CN_WAIT"))Runtime.getRuntime().halt(System.in.read()=='H'?73:74);return;
                }

                if(args[0].equals("CURRENT")) {
                    result.put("state",application.getBean(RagGenerationState.class).state(UUID.fromString(args[2])));
                    result.put("success",true);emit(receipt,result);return;
                } else if(args[0].equals("RECOVER")) {
                    job=UUID.fromString(args[2]);application.getBean(RagRecoveryService.class).reconcile();
                } else {
                    var source=rag.findSource(Long.parseLong(args[2])).orElseThrow();
                    var root=new RagDocumentRootItem("root",source.sourceType(),source.sourceId(),source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),source.approvedFields());
                    var admitted=rag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(root),Map.of()));job=admitted.jobId();
                    result.put("attempt_id",admitted.attemptId().toString());
                    var task=new RagDocumentBatchTask("root",source.sourceId(),"MD",null,null,null,List.of("content"),Map.of());
                    var executor=Executors.newSingleThreadExecutor();
                    if(args[0].equals("PRODUCING")) {
                        var running=executor.submit(()->rag.executeDocumentBatch(job,admitted.attemptId(),List.of(task)));
                        await().atMost(Duration.ofSeconds(35)).pollInterval(Duration.ofMillis(5)).until(()->!running.isDone()
                            && jdbc.queryForObject("SELECT count(*) FROM rag.worker_grants WHERE job_id=? AND consumed_at IS NOT NULL",Integer.class,job)==1
                            && jdbc.queryForObject("SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=?",Integer.class,job)==0);
                    } else {
                        rag.executeDocumentBatch(job,admitted.attemptId(),List.of(task));
                        if(args[0].equals("VALIDATING")) {
                            String manifest=jdbc.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job);
                            var running=executor.submit(()->rag.finalizeDocumentBatch(job,admitted.attemptId(),new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest))));
                            await().atMost(Duration.ofSeconds(35)).pollInterval(Duration.ofMillis(5)).until(()-> {
                                var identity=json.readTree(jdbc.queryForObject("SELECT worker_identity::text FROM rag.job_leases WHERE job_id=?",String.class,job)).path("verification");
                                return !running.isDone()&&identity.has("process_id")&&!NativeWorkerProcess.isRecordedProcessTerminated(identity.path("process_id").asLong(),Long.parseLong(identity.path("creation_file_time").asText()));
                            });
                        } else if(!args[0].equals("STAGED")) throw new IllegalArgumentException("Unknown test mode");
                    }
                }
                result.put("success",true);result.put("mode",args[0]);result.put("job_id",job.toString());
                result.put("controller_pid",ProcessHandle.current().pid());
                result.put("status",jdbc.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job));
                result.put("worker_identity",json.readTree(jdbc.queryForObject("SELECT COALESCE(worker_identity::text,'null') FROM rag.job_leases WHERE job_id=?",String.class,job)));
                result.put("reserved",jdbc.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=(SELECT attempt_id FROM rag.job_leases WHERE job_id=?)",Integer.class,job));
                result.put("staged",jdbc.queryForObject("SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=?",Integer.class,job));
                result.put("grants",jdbc.queryForList("SELECT input_digest,scope_digest,output_digest FROM rag.worker_grants WHERE job_id=?",job));
                result.put("generations",jdbc.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job));
                emit(receipt,result);
                if(!args[0].equals("RECOVER")) Runtime.getRuntime().halt(System.in.read()=='H'?73:74);
            }
        } catch(Throwable error) {emit(receipt,Map.of("success",false,"mode",args[0],"error_type",error.getClass().getName()));Runtime.getRuntime().halt(76);}
        finally {watchdog.shutdownNow();}
    }
    private static void emit(Path path,Map<String,Object> value) throws Exception {
        Files.createDirectories(path.getParent());Path temp=path.resolveSibling(path.getFileName()+".tmp");
        Files.write(temp,new ObjectMapper().writeValueAsBytes(value));Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE);
    }
}
