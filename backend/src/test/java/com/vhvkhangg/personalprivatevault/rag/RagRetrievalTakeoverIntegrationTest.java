package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/** Regression proofs for the independently reproduced takeover defects, on real PostgreSQL. */
@org.springframework.boot.test.context.SpringBootTest(classes=com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment=org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
class RagRetrievalTakeoverIntegrationTest extends AbstractRagIntegrationTest {
    @Autowired RagRetrievalAuthority authority;
    @Autowired RagRecoveryService recovery;
    @Autowired RagRetrievalOperations retrieval;
    @Autowired javax.sql.DataSource dataSource;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState generationState;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority sourceAuthority;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagIndexGenerationRepository generations;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagGenerationRootRepository roots;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagDocumentChunkRepository chunks;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagQueryEmbeddingSupervisor embeddings;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.application.RagSnapshotInspections inspections;

    @DynamicPropertySource static void enable(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.retrieval.enabled", () -> true);
        properties.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }

    private record File(UUID id, UUID revision) {}
    private record Reader(UUID id, UUID token, File file) {}

    private File file() {
        var f = new File(UUID.randomUUID(), UUID.randomUUID());
        jdbcTemplate.update("""
                INSERT INTO rag.external_file_sources(file_source_id,canonical_identity_digest,selected_revision_id,
                    selected_revision_hash,selected_byte_length) VALUES(?,?,?,'synthetic',1)
                """, f.id(), "synthetic:" + f.id(), f.revision());
        return f;
    }

    private Reader admit(File f) {
        var reader = new Reader(UUID.randomUUID(), UUID.randomUUID(), f);
        authority.admit(reader.id(),reader.token(),"synthetic-proof", completeRetrievalFixtureScope(Map.of(
                "external_file_ids",List.of(f.id().toString()),
                "sources",List.of(Map.of("file_source_id",f.id().toString(),"revision_id",f.revision().toString())),
                "generation_ids",List.of()),List.of(f.id())),List.of(f.id()),retrievalFixtureController());
        return reader;
    }

    private void finish(Reader reader, String disposition, Collection<UUID> files, Collection<UUID> revisions) {
        authority.finish(reader.id(),reader.token(),disposition,"ABORTED",noSenderEvidence("NO_SENDER_TEST"),files,revisions);
    }

    @Test void readerInspectionRetryReusesOriginalUnresolvedOperation() {
        var reader=admit(file());UUID original=UUID.randomUUID();
        assertThat(authority.beginReaderInspection(original,reader.id(),reader.token(),reader.file().id(),reader.file().revision())).isEqualTo(original);
        assertThat(authority.beginReaderInspection(original,reader.id(),reader.token(),reader.file().id(),reader.file().revision())).isEqualTo(original);
        assertThat(authority.beginReaderInspection(UUID.randomUUID(),reader.id(),reader.token(),reader.file().id(),reader.file().revision())).isEqualTo(original);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_inspections WHERE reader_id=?",Integer.class,reader.id())).isEqualTo(1);
        assertThatThrownBy(()->finish(reader,"EXECUTION_FAILURE",null,null)).hasMessageContaining("Unresolved inspection");
        authority.recordInspection(original,"CLEAN",false,Map.of());
        finish(reader,"EXECUTION_FAILURE",null,null);
    }

    @Test void exactOutstandingInspectionFencesIndexOwnersAndReusesOriginalIdentityWithoutCollateralDenial() {
        var a=file();var b=file();completeRetrievalFixtureScope(Map.of(),List.of(a.id(),b.id()));
        var aIndex=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=(SELECT id FROM rag.sources WHERE source_id=?)",UUID.class,a.id().toString());
        var bIndex=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=(SELECT id FROM rag.sources WHERE source_id=?)",UUID.class,b.id().toString());
        UUID obsolete=UUID.randomUUID();
        authority.beginInspection(obsolete,null,"UNKNOWN_CONTROL","obsolete",a.id(),UUID.randomUUID());
        assertThat(generationState.state(aIndex)).isEqualTo("CURRENT");
        UUID token=UUID.randomUUID();jdbcTemplate.queryForObject("SELECT rag.external_file_reserve(ARRAY[?]::uuid[],?)",Object.class,a.id(),token);
        UUID original=UUID.randomUUID();authority.beginInspection(original,null,"QUERY_PREPARE","original",a.id(),a.revision());
        assertThat(generationState.state(aIndex)).isEqualTo("UNAVAILABLE");
        assertThat(generationState.state(bIndex)).isEqualTo("CURRENT");
        assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_lock_reserved(ARRAY[?]::uuid[],?)",Object.class,a.id(),token)).hasMessageContaining("inspection unresolved");
        assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_release(ARRAY[?]::uuid[],?)",Object.class,a.id(),token)).hasMessageContaining("inspection unresolved");
        assertThatThrownBy(()->jdbcTemplate.update("UPDATE rag.external_file_sources SET selected_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),a.id())).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(()->jdbcTemplate.update("DELETE FROM rag.generation_roots WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?)",aIndex))
                .hasMessageContaining("Pending inspection retains generation artifacts");
        assertThatThrownBy(()->jdbcTemplate.update("DELETE FROM rag.index_generations WHERE generation_id=?",aIndex))
                .hasMessageContaining("Pending inspection retains generation artifacts");
        UUID sibling=UUID.randomUUID();jdbcTemplate.queryForObject("SELECT rag.external_file_reserve(ARRAY[?]::uuid[],?)",Object.class,b.id(),sibling);
        jdbcTemplate.queryForObject("SELECT rag.external_file_release(ARRAY[?]::uuid[],?)",Object.class,b.id(),sibling);
        var descriptor=jdbcTemplate.queryForMap("SELECT selected_revision_hash,selected_byte_length FROM rag.external_file_sources WHERE file_source_id=?",a.id());
        inspections.verify(a.id(),a.revision(),descriptor.get("selected_revision_hash").toString(),((Number)descriptor.get("selected_byte_length")).longValue(),"SOURCE_INTEGRITY_CHECK","different-producer");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_inspections WHERE affected_file_source_id=?",Integer.class,a.id())).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,original)).isEqualTo("CLEAN");
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,obsolete)).isEqualTo("UNRESOLVED");
        assertThat(generationState.state(aIndex)).isEqualTo("CURRENT");
        jdbcTemplate.queryForObject("SELECT rag.external_file_lock_reserved(ARRAY[?]::uuid[],?)",Object.class,a.id(),token);
        jdbcTemplate.queryForObject("SELECT rag.external_file_release(ARRAY[?]::uuid[],?)",Object.class,a.id(),token);
    }

    @Test void boundedCleanInspectionCompactionCannotEvictUnresolvedObligation() throws Exception {
        var file=file();UUID unresolved=UUID.randomUUID();
        authority.beginInspection(unresolved,null,"CAPACITY_TEST","scope",file.id(),file.revision());
        try(var connection=java.sql.DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.prepareStatement("INSERT INTO rag.retrieval_inspections(inspection_id,producer,scope_digest,affected_file_source_id,affected_revision_id,state) SELECT gen_random_uuid(),'CAPACITY_FIXTURE','scope',?,?,'CLEAN' FROM generate_series(1,4095)")) {
            statement.setObject(1,file.id());statement.setObject(2,file.revision());assertThat(statement.executeUpdate()).isEqualTo(4095);
        }
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_inspections",Integer.class)).isEqualTo(4096);
        UUID duplicate=UUID.randomUUID();
        assertThat(authority.beginInspection(duplicate,null,"CAPACITY_TEST","scope",file.id(),file.revision())).isEqualTo(unresolved);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_inspections",Integer.class)).isEqualTo(4096);
        UUID next=UUID.randomUUID();authority.beginInspection(next,null,"CAPACITY_TEST","other-scope",file.id(),file.revision());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_inspections",Integer.class)).isEqualTo(3841);
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,unresolved)).isEqualTo("UNRESOLVED");
        authority.recordInspection(next,"CLEAN",false,Map.of());
        authority.recordInspection(unresolved,"CLEAN",false,Map.of());
    }

    @Test void incidentRevokesOnlyAffectedRootAndTrueDependentWhileIndependentRootRemainsCurrent() throws Exception {
        var a=file();var b=file();var parent=file();var ids=List.of(a.id(),b.id(),parent.id());
        completeRetrievalFixtureScope(Map.of(),ids);
        long aSource=jdbcTemplate.queryForObject("SELECT id FROM rag.sources WHERE source_id=?",Long.class,a.id().toString());
        long parentSource=jdbcTemplate.queryForObject("SELECT id FROM rag.sources WHERE source_id=?",Long.class,parent.id().toString());
        jdbcTemplate.update("INSERT INTO rag.dependencies(parent_source_id,child_source_id,created_at) VALUES(?,?,clock_timestamp())",parentSource,aSource);
        String manifest=sourceAuthority.prepareSource(parentSource,Map.of()).manifest();
        String digest=com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest);
        var old=generations.findBySourceId(parentSource).getFirst();
        jdbcTemplate.update("UPDATE rag.index_generations SET is_active=false,status='OUTDATED' WHERE id=?",old.getId());
        UUID job=UUID.randomUUID(),attempt=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.job_leases(job_id,attempt_id,source_id,status,lease_token,leased_until,reservation_token,staging_closed,dependency_manifest,created_at,updated_at) VALUES(?,?,?,'COMPLETED','fixture',clock_timestamp()+interval '1 hour',?,true,?,clock_timestamp(),clock_timestamp())",job,attempt,parentSource,attempt,manifest);
        jdbcTemplate.update("INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,output_digest,stage_type,created_at) VALUES(?,?,?,clock_timestamp()+interval '1 hour',?,?,?,'SYNTHETIC_STAGE',clock_timestamp())",job,attempt,UUID.randomUUID(),"0".repeat(64),"0".repeat(64),"0".repeat(64));
        var generation=generations.saveAndFlush(new com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration(UUID.randomUUID(),job,parentSource,"PUBLISHED",old.getAuthorizationEpoch(),old.getSourceRevisionHash(),digest,true,java.time.Instant.now()));
        var pipeline=com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentFinalizationGate.PIPELINE;
        roots.saveAndFlush(new com.vhvkhangg.personalprivatevault.rag.internal.domain.RagGenerationRoot(generation.getId(),"dependent",Map.of("manifest_text",manifest),
                (String)pipeline.get("artifact_hash"),(String)pipeline.get("model_version"),(String)pipeline.get("tokenizer_version"),(String)pipeline.get("parser_version"),
                (String)pipeline.get("chunker_version"),(String)pipeline.get("ocr_pipeline_version"),384,java.time.Instant.now()));
        var scope=completeRetrievalFixtureScope(Map.of(),ids);
        long bSource=jdbcTemplate.queryForObject("SELECT id FROM rag.sources WHERE source_id=?",Long.class,b.id().toString());
        var bIndex=generations.findBySourceId(bSource).getFirst();
        String text="Lifecycle fixture "+b.id()+"\n";
        var locator=Map.<String,Object>of("profile","ppv-obsidian-source-v1","source_key",b.id().toString(),"source_version",1L,
                "approved_field","content","char_start",0L,"char_end",(long)text.length(),"line_start",1L,"line_end",1L);
        float[] vector=new float[384];Arrays.fill(vector,(float)(1/Math.sqrt(384)));
        chunks.saveAndFlush(new com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDocumentChunk(bIndex.getId(),b.id().toString(),text,locator,Map.of("constituent_locators",List.of(locator)),vector,java.time.Instant.now()));
        UUID reader=UUID.randomUUID(),token=UUID.randomUUID(),inspection=UUID.randomUUID();
        authority.admit(reader,token,"independent-roots",scope,ids,retrievalFixtureController());
        authority.beginInspection(inspection,reader,"INCIDENT_A","scope",a.id(),a.revision());
        authority.recordInspection(inspection,"INCIDENT_PENDING",true,Map.of());
        authority.finish(reader,token,"CANCELLED","ABORTED",noSenderEvidence("SCOPED_INCIDENT"),null,null);
        for(var file:List.of(a,parent)) {
            assertThat(jdbcTemplate.queryForObject("SELECT g.status FROM rag.index_generations g JOIN rag.sources s ON s.id=g.source_id WHERE s.source_id=? ORDER BY g.id DESC LIMIT 1",String.class,file.id().toString())).isEqualTo("REVOKED");
        }
        UUID bGeneration=jdbcTemplate.queryForObject("SELECT g.generation_id FROM rag.index_generations g JOIN rag.sources s ON s.id=g.source_id WHERE s.source_id=?",UUID.class,b.id().toString());
        assertThat(generationState.state(bGeneration)).isEqualTo("CURRENT");
        embeddings.setVectorOverride((id,question)->vector.clone());
        try {
            var response=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"independent root?",RagRetrievalMode.GENERIC,
                    List.of(new RagSourceSelectionEntry(bSource,b.revision(),null,null)),null,null,5));
            assertThat(response.outcome()).isEqualTo(RagRetrievalOutcome.EVIDENCE);
            assertThat(response.matches()).allSatisfy(m->{assertThat(m.sourceId()).isEqualTo(bSource);assertThat(m.generationId()).isEqualTo(bGeneration);});
        } finally {embeddings.setVectorOverride(null);}
        assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,b.id())).isFalse();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,token)).isZero();
    }

    @Test void newAdmissionAtPhysicalDenialReleaseCommitCannotObserveGap() throws Exception {
        var reader=admit(file());UUID inspection=UUID.randomUUID();
        String scope=jdbcTemplate.queryForObject("SELECT scope_payload::text FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader.id());
        authority.beginInspection(inspection,reader.id(),"BOUNDARY_TEST","scope",reader.file().id(),reader.file().revision());
        authority.recordInspection(inspection,"INCIDENT_PENDING",true,Map.of());
        try(var finishConnection=dataSource.getConnection();var admissionConnection=dataSource.getConnection();
            var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            finishConnection.setAutoCommit(false);
            try(var finish=finishConnection.prepareStatement("SELECT rag.retrieval_reader_finish(?,?,'CANCELLED','ABORTED',?::jsonb,ARRAY[]::uuid[],ARRAY[]::uuid[])")) {
                finish.setObject(1,reader.id());finish.setObject(2,reader.token());
                finish.setString(3,new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(noSenderEvidence("BOUNDARY_TEST")));
                finish.execute();
                // Neither uncommitted terminalization nor denial is visible from another connection.
                assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,reader.file().id())).isEqualTo(reader.token());
                assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,reader.file().id())).isFalse();
                int pid;
                try(var statement=admissionConnection.createStatement();var rs=statement.executeQuery("SELECT pg_backend_pid()")) { rs.next();pid=rs.getInt(1); }
                var attempt=executor.submit(()->{
                    try(var statement=admissionConnection.prepareStatement("SELECT rag.retrieval_reader_admit(?,?,'scope',?::jsonb,ARRAY[?]::uuid[],?)")) {
                        statement.setObject(1,UUID.randomUUID());statement.setObject(2,UUID.randomUUID());statement.setString(3,scope);
                        statement.setObject(4,reader.file().id());statement.setString(5,retrievalFixtureController());
                        statement.execute();return "ADMITTED";
                    } catch(java.sql.SQLException denied) { return denied.getSQLState(); }
                });
                org.awaitility.Awaitility.await().pollInterval(java.time.Duration.ofMillis(20)).atMost(java.time.Duration.ofSeconds(1))
                        .until(()->jdbcTemplate.queryForObject("SELECT cardinality(pg_blocking_pids(?))",Integer.class,pid)>0);
                assertThat(attempt.isDone()).isFalse();finishConnection.commit();
                assertThat(attempt.get(3,java.util.concurrent.TimeUnit.SECONDS)).isNotEqualTo("ADMITTED");
            } finally { finishConnection.rollback(); }
        }
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader.id())).isEqualTo("TERMINAL");
        assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,reader.file().id())).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection)).isEqualTo("DENIED");
    }

    @Test void unresolvedInspectionRetainsReservationForEveryFinishOutcome() {
        var reader=admit(file());
        UUID inspection=UUID.randomUUID();
        authority.beginInspection(inspection,reader.id(),"TAKEOVER_TEST","synthetic-proof",reader.file().id(),reader.file().revision());
        for(String disposition:List.of("COMPLETED","CANCELLED","EXECUTION_FAILURE","RECOVERY_REQUIRED")) {
            assertThatThrownBy(() -> finish(reader,disposition,null,null)).hasMessageContaining("Unresolved inspection");
            assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader.id()))
                    .isNotEqualTo("TERMINAL");
            assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT rag.external_file_require_editable(ARRAY[?]::uuid[])",
                    Object.class,reader.file().id())).hasMessageContaining("RAG_SOURCE_BUSY");
        }
        authority.recordInspection(inspection,"CLEAN",false,Map.of());
        finish(reader,"CANCELLED",null,null);
    }

    @Test void unrelatedObservedIncidentCannotDenyOrReleaseAdmittedSource() {
        var reader=admit(file());
        var other=file();
        UUID inspection=UUID.randomUUID();
        authority.beginInspection(inspection,null,"TAKEOVER_TEST","other-proof",other.id(),other.revision());
        authority.recordInspection(inspection,"INCIDENT_PENDING",true,Map.of());
        assertThatThrownBy(() -> finish(reader,"CANCELLED",List.of(other.id()),List.of(other.revision())))
                .hasMessageContaining("Incident outside admitted observed tuple");
        assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",
                Boolean.class,other.id())).isFalse();
        assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",
                UUID.class,reader.file().id())).isEqualTo(reader.token());
        finish(reader,"CANCELLED",null,null);
    }

    @Test void unknownControllerAndMissingChildPidCannotAuthorizeRecovery() {
        var reader=admit(file());
        recovery.afterSingletonsInstantiated();
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT rag.retrieval_reader_recover_stale(?)",Object.class,
                "synthetic-controller")).hasMessageContaining("positive controller/sender stop proof");
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader.id()))
                .isEqualTo("ADMITTED");
        assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",
                UUID.class,reader.file().id())).isEqualTo(reader.token());
        finish(reader,"CANCELLED",null,null);
    }

    @Test void approvedUnindexedRowsDoNotConsumeEligibleScopeCap() throws Exception {
        for(int n=0;n<101;n++) {
            var f=file();
            jdbcTemplate.update("""
                    INSERT INTO rag.sources(source_type,source_id,classification,authorization_epoch,field_policy_version,
                        approved_fields_text,access_state,source_revision_hash,description_revision_hash,approved_owner_reset_version,file_identity,created_at,updated_at)
                    VALUES('EXTERNAL_FILE',?,'NON_PRIVATE',1,1,'content','APPROVED','synthetic','',1,'synthetic',clock_timestamp(),clock_timestamp())
                    """,f.id().toString());
        }
        var result=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"eligible sources?",RagRetrievalMode.GENERIC,
                null,null,null,5));
        assertThat(result.outcome()).isEqualTo(RagRetrievalOutcome.NO_EVIDENCE);
        assertThat(result.matches()).isEmpty();
    }

    @Test void foreignSecondJvmAndBoundNativeChildMustBothStopBeforeRecoveryRelease() throws Exception {
        var f=file();
        var binary=java.nio.file.Path.of(System.getProperty("java.home"),"bin","java.exe");
        var classes=java.nio.file.Path.of(RagRetrievalRecoveryControllerFixture.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var env=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        for(String key:List.of("SystemRoot","WINDIR","TEMP","TMP","PATH")) {
            if(System.getenv(key)!=null) env.put(key,System.getenv(key));
        }
        var args=List.of("-Xmx64m","-cp",classes.toString(),RagRetrievalRecoveryControllerFixture.class.getName());
        UUID id=UUID.randomUUID(), token=UUID.randomUUID();
        var work=java.nio.file.Path.of("").toAbsolutePath();
        try(var controller=com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess
                .launchSuspended(UUID.randomUUID(),binary,args,env,work,512L*1024*1024,1);
            var child=com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess
                .launchSuspended(UUID.randomUUID(),binary,args,env,work,512L*1024*1024,1)) {
            controller.resume(); child.resume();
            var scope=new LinkedHashMap<String,Object>();
            scope.put("external_file_ids",List.of(f.id().toString()));
            scope.put("sources",List.of(Map.of("file_source_id",f.id().toString(),"revision_id",f.revision().toString())));
            scope.put("generation_ids",List.of());
            scope.put("controller_pid",controller.pid());
            scope.put("controller_creation_file_time",Long.toString(controller.creationTimeFileTime()));
            authority.admit(id,token,"foreign-exact-proof",completeRetrievalFixtureScope(scope,List.of(f.id())),List.of(f.id()),retrievalFixtureController());
            // Disposable ADMIN fixture models durable child binding by the foreign controller.
            try(var connection=java.sql.DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
                var statement=connection.prepareStatement("UPDATE rag.retrieval_readers SET worker_pid=?,worker_created_at=?,worker_creation_file_time=? WHERE reader_id=?")) {
                statement.setLong(1,child.pid()); statement.setObject(2,java.time.OffsetDateTime.ofInstant(child.creationTime(),java.time.ZoneOffset.UTC));
                statement.setLong(3,child.creationTimeFileTime()); statement.setObject(4,id);
                assertThat(statement.executeUpdate()).isEqualTo(1);
            }
            authority.beginEmission(id);
            recovery.afterSingletonsInstantiated();
            assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,id)).isEqualTo("EMITTING");
            controller.terminateAndVerify(java.time.Duration.ofSeconds(5));
            recovery.afterSingletonsInstantiated();
            assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,id)).isEqualTo("EMITTING");
            child.terminateAndVerify(java.time.Duration.ofSeconds(5));
            recovery.afterSingletonsInstantiated();
            assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,id)).isEqualTo("TERMINAL");
            assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,f.id())).isNull();
        }
    }
}
