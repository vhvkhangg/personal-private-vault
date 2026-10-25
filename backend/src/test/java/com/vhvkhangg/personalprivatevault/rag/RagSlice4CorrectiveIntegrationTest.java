package com.vhvkhangg.personalprivatevault.rag;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Timeout(180)
@org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class RagSlice4CorrectiveIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @TempDir Path work;
    @Autowired RagOriginalBytesStore store;
    @Autowired RagGenerationState generationState;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean RagNoChangeService noChange;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean RagSnapshotAcquisitionService acquisition;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean RagDocumentResultVerifier verifier;
    @Autowired RagRecoveryService recovery;
    @Autowired PlatformTransactionManager transactions;
    @Autowired org.springframework.core.env.Environment environment;
    static final RagSlice3ConnectionLossIntegrationTest.PgWireProxy WIRE=new RagSlice3ConnectionLossIntegrationTest.PgWireProxy();
    @DynamicPropertySource static void proxiedRuntime(DynamicPropertyRegistry r) {
        r.add("ppv.rag.test-runtime-jdbc-url",()->"jdbc:postgresql://127.0.0.1:"+WIRE.port()+"/"+POSTGRES.getDatabaseName()+"?sslmode=disable&gssEncMode=disable&socketTimeout=8");
        r.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }
    @AfterAll static void closeProxy() throws Exception {WIRE.listener.close();WIRE.disconnectAll();}
    @AfterEach void evidence(TestInfo test) throws Exception {
        RagSlice3Evidence.capture(jdbcTemplate,objectMapper,test.getDisplayName());
        String output=System.getenv("PPV_S3_EVIDENCE");if(output!=null) {
            Path dir=repository().resolve(output).resolve("cn-bindings");Files.createDirectories(dir);
            var data=Map.of("test",test.getDisplayName(),"operations",jdbcTemplate.queryForList("SELECT run_id,job_id,status,metrics::text,purge_status FROM rag.runs WHERE job_id IS NULL"));
            Files.writeString(dir.resolve(UUID.randomUUID()+".json"),objectMapper.writeValueAsString(data),StandardOpenOption.CREATE_NEW);
        }
    }
    RagSourceView source(String name,String text) throws Exception {
        Path path=work.resolve(name); Files.createDirectories(path.getParent());
        Files.writeString(path,text,StandardCharsets.UTF_8);
        var v=enabledRag.registerSnapshotCandidate(path.toString());
        return enabledRag.approveSource(v.id(),new RagConsentCommand(Set.of("content"),v.sourceVersion(),
                v.consentResetVersion(),v.authorizationEpoch(),v.fieldPolicyVersion(),null,v.snapshot()));
    }
    RagJobAdmissionResult stage(RagSourceView... views) {
        var roots=new ArrayList<RagDocumentRootItem>(); var tasks=new ArrayList<RagDocumentBatchTask>();
        for (int i=0;i<views.length;i++) {
            var v=views[i]; String key="root-"+i;
            roots.add(new RagDocumentRootItem(key,v.sourceType(),v.sourceId(),v.sourceVersion(),v.authorizationEpoch(),v.fieldPolicyVersion(),v.approvedFields()));
            tasks.add(new RagDocumentBatchTask(key,v.sourceId(),"MD",null,null,null,List.of("content"),Map.of()));
        }
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots,Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),tasks); return job;
    }
    RagBatchFinalizationResult finish(RagJobAdmissionResult job) {
        String manifest=jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job.jobId());
        return enabledRag.finalizeDocumentBatch(job.jobId(),job.attemptId(),new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest)));
    }
    void admin(String sql, Object... args) throws Exception {
        try (var c=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword()); var s=c.prepareStatement(sql)) {
            for (int i=0;i<args.length;i++) s.setObject(i+1,args[i]); s.executeUpdate();
        }
    }


    RagDocumentRootItem root(String key,RagSourceView v) {return new RagDocumentRootItem(key,v.sourceType(),v.sourceId(),v.sourceVersion(),v.authorizationEpoch(),v.fieldPolicyVersion(),v.approvedFields());}
    RagDocumentBatchTask task(String key,RagSourceView v) {return new RagDocumentBatchTask(key,v.sourceId(),"MD",null,null,null,List.of("content"),Map.of());}
    RagSourceView publish(String name,String text) throws Exception {var v=source(name,text);finish(stage(v));return enabledRag.findSource(v.id()).orElseThrow();}
    RagSourceView update(RagSourceView v,String name,String text) throws Exception {
        Files.writeString(work.resolve(name),text);v=enabledRag.findSource(v.id()).orElseThrow();
        var c=enabledRag.capturePendingRevision(v.id(),new RagSnapshotCaptureCommand(work.resolve(name).toString(),v.snapshot()));
        return enabledRag.approveSource(c.id(),new RagConsentCommand(Set.of("content"),c.sourceVersion(),c.consentResetVersion(),c.authorizationEpoch(),c.fieldPolicyVersion(),null,c.snapshot()));
    }

    @Test void changedChildCannotQualifyEqualParentAsNoChange() throws Exception {
        var child=source("Child.md","# Child one\n");var parent=source("Parent.md","# Parent\n\n![[Child]]\n");
        enabledRag.addDependency(parent.id(),child.id());parent=enabledRag.findSource(parent.id()).orElseThrow();
        var g=finish(stage(parent)).publishedGenerations().get("root-0");
        child=update(child,"Child.md","# Child two\n\nChanged revision.\n");finish(stage(child));
        assertThat(generationState.state(g)).isEqualTo("OUTDATED");
        var fresh=enabledRag.findSource(parent.id()).orElseThrow();long jobs=jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Long.class);
        assertThatThrownBy(()->enabledRag.capturePendingRevision(fresh.id(),new RagSnapshotCaptureCommand(work.resolve("Parent.md").toString(),fresh.snapshot()))).isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Long.class)).isEqualTo(jobs);
        assertThat(enabledRag.findSource(parent.id()).orElseThrow().snapshot().pendingRevisionId()).isNull();
        assertThat(generationState.state(g)).isEqualTo("OUTDATED");
    }

    @Test void omittedPendingRootIsRejectedBeforeAnyWorkerDispatch() throws Exception {
        var a=publish("A.md","# Selected A\n");a=update(a,"A.md","# Pending different A\n");var b=source("B.md","# B\n");
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(root("a",a),root("b",b)),Map.of()));
        try {
            assertThatThrownBy(()->enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(task("b",b)))).isInstanceOf(IllegalArgumentException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.worker_grants WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(enabledRag.findSource(a.id()).orElseThrow().snapshot().pendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId());
        } finally {enabledRag.cancelJob(job.jobId());}
    }

    @Test void equalCaptureRecordsDurableNonpublicationWithoutJobRevisionOrGeneration() throws Exception {
        var v=publish("Equal.md","# Equal\n\nTi?ng Vi?t ??.\n");long jobs=jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Long.class);
        var result=enabledRag.capturePendingRevision(v.id(),new RagSnapshotCaptureCommand(work.resolve("Equal.md").toString(),v.snapshot()));
        assertThat(result.snapshot()).isEqualTo(v.snapshot());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Long.class)).isEqualTo(jobs);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.runs WHERE job_id IS NULL AND status='COMPLETED' AND metrics->>'disposition'='NO_CHANGE'",Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isEqualTo(1);
        try(var files=Files.list(store.getTempDir())){assertThat(files.toList()).isEmpty();}
    }

    @Test void explicitMixedRosterPublishesSuccessFailureAndNoChangeAndHttpFields() throws Exception {
        var a=publish("A.md","# Retained A\n");var b=source("B.md","# New B\n");var c=source("C.md","<script>forbidden()</script>\n");
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(root("a",a),root("b",b),root("c",c)),Map.of(),Set.of("a")));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(task("b",b),task("c",c)));
        var result=finish(job);assertThat(result.publishedCount()).isEqualTo(1);assertThat(result.failedCount()).isEqualTo(1);
        mockMvc.perform(get("/api/v1/rag/jobs/"+job.jobId()+"/document-report").header("Authorization",bearerHeader()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.outcomes.length()").value(3))
            .andExpect(jsonPath("$.data.outcomes[0].result").value("NO_CHANGE"))
            .andExpect(jsonPath("$.data.outcomes[0].publishedNewGeneration").value(false))
            .andExpect(jsonPath("$.data.outcomes[0].freshnessBasis").value("RAG_SNAPSHOT"))
            .andExpect(jsonPath("$.data.outcomes[0].snapshotIndexState").value("SNAPSHOT_CURRENT"))
            .andExpect(jsonPath("$.data.outcomes[0].originalFreshness").value("UNVERIFIED"))
            .andExpect(jsonPath("$.data.outcomes[0].selectedRevisionId").value(a.snapshot().selectedRevisionId().toString()))
            .andExpect(jsonPath("$.data.outcomes[1].publishedNewGeneration").value(true))
            .andExpect(jsonPath("$.data.outcomes[2].result").value("FAILED"));
    }

    @Test void allUnchangedHttpAdmissionCreatesOperationWithNoIndexingLeaseOrWorker() throws Exception {
        var a=publish("All.md","# Already selected\n");Files.delete(work.resolve("All.md"));
        long jobs=jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Long.class);
        String response=mockMvc.perform(post("/api/v1/rag/jobs/admit-batch").header("Authorization",bearerHeader()).contentType("application/json")
            .content(objectMapper.writeValueAsString(new RagDocumentBatchCommand(List.of(root("a",a)),Map.of(),Set.of("a")))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.jobId").doesNotExist()).andExpect(jsonPath("$.data.leaseToken").doesNotExist())
            .andExpect(jsonPath("$.data.operationDisposition").value("NO_CHANGE")).andReturn().getResponse().getContentAsString();
        String operation=objectMapper.readTree(response).path("data").path("operationId").asText();assertThat(operation).isNotBlank();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Long.class)).isEqualTo(jobs);
        mockMvc.perform(get("/api/v1/rag/jobs/"+operation+"/document-report").header("Authorization",bearerHeader()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").doesNotExist()).andExpect(jsonPath("$.data.summaryOutcome").value("NO_CHANGE"))
            .andExpect(jsonPath("$.data.outcomes[0].publishedNewGeneration").value(false));
    }

    @ParameterizedTest @ValueSource(strings={"CANCELLED","ABORTED","RECOVERY_REQUIRED"})
    void everyAdmittedRootAppearsInTerminalAndUnknownHttpReports(String outcome) throws Exception {
        var v=source("Report.md","# Report\n");var job=stage(v);
        if(outcome.equals("CANCELLED"))enabledRag.cancelJob(job.jobId());
        else if(outcome.equals("ABORTED"))getBean(RagPurgeService.class).abortJob(job.jobId(),false);
        else getBean(RagPurgeService.class).requireRecovery(job.jobId());
        try {
            mockMvc.perform(get("/api/v1/rag/jobs/"+job.jobId()+"/document-report").header("Authorization",bearerHeader()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.outcomes.length()").value(1))
                .andExpect(jsonPath("$.data.outcomes[0].result").value(outcome))
                .andExpect(jsonPath("$.data.outcomes[0].publishedNewGeneration").value(false))
                .andExpect(jsonPath("$.data.outcomes[0].pendingRevisionId").value(v.snapshot().pendingRevisionId().toString()));
        } finally {enabledRag.cancelJob(job.jobId());}
    }

    @Test void reorderedTasksCannotReplaceImmutableWorkerPlan() throws Exception {
        var a=source("A.md","# A\n");var b=source("B.md","# B\n");var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(root("a",a),root("b",b)),Map.of()));
        try {assertThatThrownBy(()->enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(task("b",b),task("a",a)))).isInstanceOf(IllegalArgumentException.class);}
        finally {enabledRag.cancelJob(job.jobId());}
    }

    @Test void noChangeRollbackAtPhysicalCommitCannotRecordSuccess() throws Exception {
        var v=publish("Rollback.md","# Rollback\n");var proofs=Map.of("root",noChange.prepare(v.id()));UUID operation=noChange.begin(proofs,null);
        admin("CREATE FUNCTION rag.fail_cn_commit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.job_id IS NULL AND NEW.status='COMPLETED' THEN RAISE EXCEPTION 'Injected C_N physical commit failure'; END IF; RETURN NEW; END $$");
        admin("CREATE CONSTRAINT TRIGGER fail_cn_commit AFTER UPDATE ON rag.runs DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.fail_cn_commit()");
        try {assertThatThrownBy(()->noChange.finish(operation,proofs)).isInstanceOf(RuntimeException.class);}
        finally {admin("DROP TRIGGER fail_cn_commit ON rag.runs");admin("DROP FUNCTION rag.fail_cn_commit()");}
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.runs WHERE run_id=?",String.class,operation)).isEqualTo("ABORTED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,operation)).isZero();
        assertThat(enabledRag.findSource(v.id()).orElseThrow().snapshot()).isEqualTo(v.snapshot());
    }

    @ParameterizedTest @ValueSource(booleans={false,true})
    void actualNoChangeWireLossReconcilesCommittedAndUncommittedOutcomes(boolean before) throws Exception {
        var v=publish("Wire.md","# Wire outcome\n");var proofs=Map.of("root",noChange.prepare(v.id()));UUID operation=noChange.begin(proofs,null);
        var f=RagNoChangeService.class.getDeclaredField("tx");f.setAccessible(true);var original=f.get(noChange);
        var e=RagNoChangeService.class.getDeclaredField("em");e.setAccessible(true);var entities=(jakarta.persistence.EntityManager)e.get(noChange);
        var once=new java.util.concurrent.atomic.AtomicBoolean();
        var manager=new PlatformTransactionManager() {
            public TransactionStatus getTransaction(TransactionDefinition d){return transactions.getTransaction(d);}
            public void rollback(TransactionStatus s){transactions.rollback(s);}
            public void commit(TransactionStatus s){
                boolean armed=once.compareAndSet(false,true);
                if(armed){int pid=entities.unwrap(org.hibernate.Session.class).doReturningWork(c->{try(var q=c.createStatement();var r=q.executeQuery("SELECT pg_backend_pid()")){r.next();return r.getInt(1);}});WIRE.arm(pid,before);}
                try{transactions.commit(s);}catch(RuntimeException failure){if(before&&armed){WIRE.unavailable.set(true);WIRE.disconnectAll();}throw failure;}
            }
        };
        f.set(noChange,new TransactionTemplate(manager));
        try {
            if(before)assertThatThrownBy(()->noChange.finish(operation,proofs)).isInstanceOf(RuntimeException.class);
            else assertThatCode(()->noChange.finish(operation,proofs)).doesNotThrowAnyException();
            assertThat(WIRE.dropped.get()).isEqualTo(1);assertThat(WIRE.lastTag).isEqualTo(before?"BEFORE_COMMIT":"COMMIT");
            try(var c=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var q=c.prepareStatement("SELECT status FROM rag.runs WHERE run_id=?")) {
                q.setObject(1,operation);try(var r=q.executeQuery()){assertThat(r.next()).isTrue();assertThat(r.getString(1)).isEqualTo(before?"ADMITTED":"COMPLETED");}
            }
        } finally {f.set(noChange,original);WIRE.unavailable.set(false);WIRE.armedPid.set(0);}
        assertThat(noChange.resolve(operation,true)).isEqualTo(!before);
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.runs WHERE run_id=?",String.class,operation)).isEqualTo(before?"ABORTED":"COMPLETED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,operation)).isZero();
    }
    @Test void freshControllerReconcilesUnfinishedNonpublicationWithoutAutomaticIndexing() throws Exception {
        var v=publish("Restart.md","# Durable C_N restart\n");UUID owner=jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class);
        Process controller=null,replacement=null;
        try {
            var launched=launchProbe("CN_WAIT",v.id().toString());controller=launched.getKey();var before=probeReceipt(controller,launched.getValue());
            UUID operation=UUID.fromString(before.path("operation_id").asText());assertThat(before.path("status").asText()).isEqualTo("ADMITTED");assertThat(before.path("reserved").asInt()).isEqualTo(1);
            var live=launchProbe("CN_RECOVER",operation.toString());replacement=live.getKey();var refused=probeReceipt(replacement,live.getValue());
            assertThat(replacement.waitFor(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(replacement.exitValue()).isZero();
            assertThat(refused.path("current_runtime").asBoolean()).isFalse();assertThat(refused.path("status").asText()).isEqualTo("ADMITTED");assertThat(refused.path("reserved").asInt()).isEqualTo(1);
            controller.getOutputStream().write('H');controller.getOutputStream().flush();assertThat(controller.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(controller.exitValue()).isEqualTo(73);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,operation)).isEqualTo(1);
            var next=launchProbe("CN_RECOVER",operation.toString());replacement=next.getKey();var after=probeReceipt(replacement,next.getValue());
            assertThat(replacement.waitFor(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(replacement.exitValue()).isZero();
            assertThat(after.path("current_runtime").asBoolean()).isTrue();assertThat(after.path("status").asText()).isEqualTo("ABORTED");assertThat(after.path("reserved").asInt()).isZero();assertThat(after.path("indexing_jobs").asInt()).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isEqualTo(1);
        } finally {
            for(var process:new Process[]{controller,replacement})if(process!=null&&process.isAlive()){process.destroyForcibly();assertThat(process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=?",owner);
        }
        assertThat(enabledRag.findSource(v.id()).orElseThrow().snapshot()).isEqualTo(v.snapshot());
    }

    @ParameterizedTest @ValueSource(booleans={true,false})
    void noChangeAndRevokeRespectBothDurableOrders(boolean noChangeFirst) throws Exception {
        var v=publish("Order.md","# Ordered authority\n");var proof=noChange.prepare(v.id());var proofs=Map.of("root",proof);
        if(noChangeFirst) {
            UUID operation=noChange.begin(proofs,null);var execution=java.util.concurrent.Executors.newSingleThreadExecutor();
            try {var revoke=execution.submit(()->enabledRag.revokeSource(v.id()));assertThatThrownBy(()->revoke.get(10,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOf(java.util.concurrent.ExecutionException.class);}
            finally {execution.shutdownNow();assertThat(execution.awaitTermination(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.runs WHERE run_id=?",String.class,operation)).isEqualTo("ADMITTED");
            noChange.finish(operation,proofs);enabledRag.revokeSource(v.id());
        } else {
            enabledRag.revokeSource(v.id());assertThatThrownBy(()->noChange.begin(proofs,null)).isInstanceOf(RuntimeException.class);
        }
        assertThat(enabledRag.findSource(v.id()).orElseThrow().accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by IS NOT NULL",Integer.class)).isZero();
        assertThatThrownBy(()->noChange.prepare(v.id())).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest @ValueSource(strings={"child-version","child-selection","child-epoch","edge"})
    void preparedNoChangeCannotOutliveExactChildAuthority(String fault) throws Exception {
        var child=source("Child.md","# Child\n");var parent=source("Parent.md","# Parent\n\n![[Child]]\n");enabledRag.addDependency(parent.id(),child.id());parent=enabledRag.findSource(parent.id()).orElseThrow();finish(stage(parent));
        var proof=noChange.prepare(parent.id());
        switch(fault) {
            case "child-version" -> admin("UPDATE rag.external_file_sources SET source_version=source_version+1 WHERE file_source_id=?",UUID.fromString(child.sourceId()));
            case "child-selection" -> admin("UPDATE rag.external_file_sources SET selected_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),UUID.fromString(child.sourceId()));
            case "child-epoch" -> admin("UPDATE rag.sources SET authorization_epoch=authorization_epoch+1 WHERE id=?",child.id());
            default -> {var extra=source("Other.md","# Other\n");enabledRag.addDependency(parent.id(),extra.id());}
        }
        assertThatThrownBy(()->noChange.begin(Map.of("root",proof),null)).isInstanceOf(RuntimeException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.runs WHERE job_id IS NULL AND status='COMPLETED'",Integer.class)).isZero();
    }

    @ParameterizedTest @ValueSource(strings={"files-restored","database-metadata-restored"})
    void ordinaryRestoreDirectionsNeverReapproveOrAdoptOldRevision(String direction) throws Exception {
        String text="# Restored authority\n\nExplicit approval is required.\n";
        var v=publish("Restore.md",text);UUID old=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=?",UUID.class,v.id());
        String policyImage=jdbcTemplate.queryForObject("SELECT to_jsonb(s)::text FROM rag.sources s WHERE id=?",String.class,v.id());
        String snapshotImage=jdbcTemplate.queryForObject("SELECT to_jsonb(s)::text FROM rag.external_file_sources s WHERE file_source_id=?",String.class,UUID.fromString(v.sourceId()));
        Path dir=store.getRevisionsDir().resolve(v.sourceId());var backup=new LinkedHashMap<String,byte[]>();
        try(var paths=Files.list(dir)){for(Path f:paths.toList())if(Files.isRegularFile(f))backup.put(f.getFileName().toString(),Files.readAllBytes(f));}
        enabledRag.revokeSource(v.id());
        if(direction.equals("files-restored")) {
            Files.createDirectories(dir);for(var file:backup.entrySet())Files.write(dir.resolve(file.getKey()),file.getValue(),StandardOpenOption.CREATE_NEW);
        } else {
            // Controlled database row-image restore, not a claimed physical server/PITR restore.
            // Restore approval as well as snapshot metadata; missing managed bytes must still deny use.
            try(var c=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())) {
                c.setAutoCommit(false);
                try(var q=c.createStatement()){q.execute("SET LOCAL session_replication_role='replica'");}
                try(var q=c.prepareStatement("UPDATE rag.sources s SET (classification,authorization_epoch,field_policy_version,approved_fields_text,access_state,source_revision_hash,description_revision_hash,approved_owner_reset_version,revoked_at)=(r.classification,r.authorization_epoch,r.field_policy_version,r.approved_fields_text,r.access_state,r.source_revision_hash,r.description_revision_hash,r.approved_owner_reset_version,r.revoked_at) FROM jsonb_populate_record(NULL::rag.sources,CAST(? AS jsonb)) r WHERE s.id=r.id")){q.setString(1,policyImage);q.executeUpdate();}
                try(var q=c.prepareStatement("UPDATE rag.external_file_sources s SET (eligible,source_version,consent_reset_version,authorization_epoch,field_policy_version,selected_revision_id,selected_revision_hash,selected_byte_length,pending_revision_id,pending_revision_hash,pending_byte_length,snapshot_integrity_failed)=(r.eligible,r.source_version,r.consent_reset_version,r.authorization_epoch,r.field_policy_version,r.selected_revision_id,r.selected_revision_hash,r.selected_byte_length,r.pending_revision_id,r.pending_revision_hash,r.pending_byte_length,r.snapshot_integrity_failed) FROM jsonb_populate_record(NULL::rag.external_file_sources,CAST(? AS jsonb)) r WHERE s.file_source_id=r.file_source_id")){q.setString(1,snapshotImage);q.executeUpdate();}
                c.commit();
            }
            assertThat(generationState.state(old)).isEqualTo("UNAVAILABLE");
        }
        store.resetRecoveryScan();for(int i=0;i<4;i++)recovery.reconcileSnapshots();
        assertThat(generationState.state(old)).isNotEqualTo("CURRENT");
        assertThatThrownBy(()->noChange.prepare(v.id())).isInstanceOf(RuntimeException.class);
        var fresh=enabledRag.findSource(v.id()).orElseThrow();
        assertThatThrownBy(()->enabledRag.approveSource(fresh.id(),new RagConsentCommand(Set.of("content"),fresh.sourceVersion(),fresh.consentResetVersion(),fresh.authorizationEpoch(),fresh.fieldPolicyVersion(),null,fresh.snapshot()))).isInstanceOf(RuntimeException.class);
        // A new explicit import and approval establishes new S/R; stale restored files are never reused.
        var replacement=publish("Reapproved.md",text);assertThat(replacement.sourceId()).isNotEqualTo(v.sourceId());
        UUID next=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=? AND is_active",UUID.class,replacement.id());assertThat(generationState.state(next)).isEqualTo("CURRENT");assertThat(generationState.state(old)).isNotEqualTo("CURRENT");
    }

    Map.Entry<Process,Path> launchProbe(String mode,String identifier) throws Exception {
        Path directory=repository().resolve(System.getenv().getOrDefault("PPV_S3_EVIDENCE","logs/phase18/s3-final-corrective-20261021")).resolve("recovery-probes");Files.createDirectories(directory);
        String name=UUID.randomUUID().toString();Path receipt=directory.resolve(name+".json"),arguments=directory.resolve(name+".args");
        java.util.function.Function<String,String> quote=v->"\""+v.replace("\\","\\\\").replace("\"","\\\"")+"\"";
        Files.writeString(arguments,"--enable-native-access=ALL-UNNAMED\n--class-path\n"+quote.apply(System.getProperty("surefire.test.class.path",System.getProperty("java.class.path")))+"\n"+RagSlice3RecoveryProbeProcess.class.getName()+"\n"+mode+"\n"+quote.apply(receipt.toString())+"\n"+quote.apply(identifier)+"\n");
        var properties=new LinkedHashMap<String,Object>();
        properties.put("ppv.rag.storage.snapshot-root",store.getStorageRoot().toString());properties.put("server.port","0");properties.put("server.address","127.0.0.1");
        properties.put("spring.datasource.url",POSTGRES.getJdbcUrl());properties.put("spring.datasource.username","rag_runtime");properties.put("spring.datasource.password",PASSWORD);properties.put("spring.flyway.enabled","false");properties.put("logging.level.root","ERROR");properties.put("ppv.rag.reconciliation-delay-ms","3600000");
        properties.put("ppv.security.jwt.secret","dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
        for(String key:List.of("ppv.rag.test-scoped-execution","ppv.rag.engine.url","ppv.rag.engine.service-key","ppv.rag.worker.executable","ppv.rag.worker.venv-launcher","ppv.rag.worker.working-directory","ppv.rag.worker.dsn"))properties.put(key,environment.getRequiredProperty(key));
        var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java.exe").toString(),"@"+arguments);
        builder.directory(repository().resolve("backend").toFile());builder.environment().put("PPV_PROBE_CONFIGURATION",objectMapper.writeValueAsString(properties));builder.redirectErrorStream(true).redirectOutput(directory.resolve(name+".log").toFile());
        return Map.entry(builder.start(),receipt);
    }
    com.fasterxml.jackson.databind.JsonNode probeReceipt(Process process,Path path) throws Exception {
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(45)).pollInterval(java.time.Duration.ofMillis(20)).until(()->Files.isRegularFile(path)||!process.isAlive());
        assertThat(path).isRegularFile();var receipt=objectMapper.readTree(path.toFile());assertThat(receipt.path("success").asBoolean()).as("child receipt: %s",receipt).isTrue();return receipt;
    }

    @ParameterizedTest @ValueSource(strings={"generation","pipeline"})
    void mixedPublicationRechecksEveryUnchangedProofAfterTrustedReplay(String fault) throws Exception {
        var a=publish("A.md","# Retained A\n");var b=source("B.md","# Changed B\n");UUID retained=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=?",UUID.class,a.id());
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(root("a",a),root("b",b)),Map.of(),Set.of("a")));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(task("b",b)));
        org.mockito.Mockito.doAnswer(call -> {
            call.callRealMethod();
            if(fault.equals("generation"))admin("UPDATE rag.index_generations SET is_active=false WHERE generation_id=?",retained);
            else admin("UPDATE rag.generation_roots SET parser_version='fault-after-replay' WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?)",retained);
            return null;
        }).when(verifier).verify(org.mockito.ArgumentMatchers.eq(job.jobId()),org.mockito.ArgumentMatchers.eq(job.attemptId()),org.mockito.ArgumentMatchers.anyList(),org.mockito.ArgumentMatchers.anyList());
        try {
            assertThatThrownBy(()->finish(job)).isInstanceOf(RuntimeException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.file_outcomes WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(enabledRag.findSource(b.id()).orElseThrow().snapshot().selectedRevisionId()).isNull();
        } finally {org.mockito.Mockito.reset(verifier);enabledRag.cancelJob(job.jobId());}
    }

    @Test void transitiveRevokePurgesParentContentAndPreservesIndependentSibling() throws Exception {
        var child=source("Child.md","# Child\n");var parent=source("Parent.md","# Parent\n\n![[Child]]\n");var sibling=source("Sibling.md","# Independent sibling\n");
        enabledRag.addDependency(parent.id(),child.id());parent=enabledRag.findSource(parent.id()).orElseThrow();var result=finish(stage(parent,sibling));UUID pg=result.publishedGenerations().get("root-0"),sg=result.publishedGenerations().get("root-1");
        enabledRag.revokeSource(child.id());assertThat(generationState.state(pg)).isNotEqualTo("CURRENT");assertThat(generationState.state(sg)).isEqualTo("CURRENT");
        // Revocation denies immediately; the existing bounded reconciliation owns physical erasure.
        for(int i=0;i<4;i++)recovery.reconcile();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?)",Integer.class,pg)).isZero();
        Long parentId=parent.id();assertThatThrownBy(()->noChange.prepare(parentId)).isInstanceOf(RuntimeException.class);
        recovery.reconcile();assertThat(generationState.state(pg)).isNotEqualTo("CURRENT");assertThat(generationState.state(sg)).isEqualTo("CURRENT");
    }

    @ParameterizedTest @ValueSource(strings={"before-finish","before-commit"})
    void equalCaptureCancellationCannotCommitNoChangeAndCleansItsOwnedTemp(String point) throws Exception {
        var v=publish("CancelCN.md","# Cancel coherent equality\n");var cancelled=new java.util.concurrent.atomic.AtomicBoolean();
        var context=new RagSnapshotAcquisitionService.CaptureContext(Duration.ofSeconds(60),cancelled::get);
        org.mockito.Mockito.doReturn(context).when(acquisition).newContext();
        org.mockito.Mockito.doAnswer(call -> {if(point.equals("before-finish"))cancelled.set(true);return call.callRealMethod();}).when(noChange).finish(org.mockito.ArgumentMatchers.any(UUID.class),org.mockito.ArgumentMatchers.anyMap(),org.mockito.ArgumentMatchers.any(Runnable.class));
        if(point.equals("before-commit")) {
            var checks=new java.util.concurrent.atomic.AtomicInteger();
            org.mockito.Mockito.doAnswer(call -> {var value=call.callRealMethod();if(checks.incrementAndGet()==2)cancelled.set(true);return value;}).when(noChange).validateLocked(org.mockito.ArgumentMatchers.any(RagNoChangeService.Proof.class));
        }
        try {
            assertThatThrownBy(()->enabledRag.capturePendingRevision(v.id(),new RagSnapshotCaptureCommand(work.resolve("CancelCN.md").toString(),v.snapshot()))).isInstanceOf(java.util.concurrent.CancellationException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.runs WHERE job_id IS NULL",String.class)).isEqualTo("CANCELLED");
            assertThat(enabledRag.findSource(v.id()).orElseThrow().snapshot()).isEqualTo(v.snapshot());
            UUID operation=jdbcTemplate.queryForObject("SELECT run_id FROM rag.runs WHERE job_id IS NULL",UUID.class);
            mockMvc.perform(get("/api/v1/rag/jobs/"+operation+"/document-report").header("Authorization",bearerHeader()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.summaryOutcome").value("CANCELLED")).andExpect(jsonPath("$.data.outcomes[0].result").value("CANCELLED"));
            try(var files=Files.list(store.getTempDir())){assertThat(files.toList()).isEmpty();}
        } finally {org.mockito.Mockito.reset(acquisition,noChange);}
    }

    @Test void failedTransitiveErasureRemainsDeniedAndRetriesWithoutPurgingSibling() throws Exception {
        var child=source("Child.md","# Child\n");var parent=source("Parent.md","# Parent\n\n![[Child]]\n");var sibling=source("Sibling.md","# Sibling\n");
        enabledRag.addDependency(parent.id(),child.id());parent=enabledRag.findSource(parent.id()).orElseThrow();var result=finish(stage(parent,sibling));UUID pg=result.publishedGenerations().get("root-0"),sg=result.publishedGenerations().get("root-1");
        admin("CREATE FUNCTION rag.fail_s4_erasure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Injected controlled erasure failure'; END $$");
        admin("CREATE TRIGGER fail_s4_erasure BEFORE DELETE ON rag.document_chunks FOR EACH ROW EXECUTE FUNCTION rag.fail_s4_erasure()");
        try {
            enabledRag.revokeSource(child.id());for(int i=0;i<2;i++)recovery.reconcile();
            assertThat(generationState.state(pg)).isNotEqualTo("CURRENT");assertThat(generationState.state(sg)).isEqualTo("CURRENT");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?)",Integer.class,pg)).isPositive();
            assertThat(jdbcTemplate.queryForObject("SELECT purge_status FROM rag.runs WHERE job_id=?",String.class,result.jobId())).contains("PENDING");
        } finally {admin("DROP TRIGGER fail_s4_erasure ON rag.document_chunks");admin("DROP FUNCTION rag.fail_s4_erasure()");}
        for(int i=0;i<4;i++)recovery.reconcile();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?)",Integer.class,pg)).isZero();assertThat(generationState.state(sg)).isEqualTo("CURRENT");
    }

    @Test void failedReindexHttpReportNamesTheIndependentlyEligibleRetainedGeneration() throws Exception {
        var v=publish("Retained.md","# Independently retained source\n");UUID retained=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=?",UUID.class,v.id());
        var job=stage(v);admin("UPDATE rag.staged_document_outputs SET chunks_payload='[]'::jsonb WHERE job_id=?",job.jobId());
        assertThatThrownBy(()->finish(job)).isInstanceOf(RuntimeException.class);assertThat(getBean(RagPurgeService.class).abortJob(job.jobId(),false)).isTrue();
        mockMvc.perform(get("/api/v1/rag/jobs/"+job.jobId()+"/document-report").header("Authorization",bearerHeader()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.outcomes[0].result").value("ABORTED"))
            .andExpect(jsonPath("$.data.outcomes[0].candidateGenerationId").value(retained.toString()))
            .andExpect(jsonPath("$.data.outcomes[0].snapshotIndexState").value("SNAPSHOT_CURRENT"))
            .andExpect(jsonPath("$.data.outcomes[0].publishedNewGeneration").value(false));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
    }

    private <T>T getBean(Class<T> type){return ((org.springframework.web.context.WebApplicationContext)mockMvc.getDispatcherServlet().getWebApplicationContext()).getBean(type);}
}
