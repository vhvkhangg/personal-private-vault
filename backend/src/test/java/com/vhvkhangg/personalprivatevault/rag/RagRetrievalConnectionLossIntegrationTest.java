package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.*;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDocumentChunk;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagDocumentChunkRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Real PostgreSQL wire faults; reuses the frozen, already-proven test-only wire forwarder. */
@SpringBootTest(classes=PersonalPrivateVaultApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
@Timeout(45)
class RagRetrievalConnectionLossIntegrationTest extends AbstractRagIntegrationTest {
    static final RagSlice3ConnectionLossIntegrationTest.PgWireProxy WIRE=new RagSlice3ConnectionLossIntegrationTest.PgWireProxy();
    @LocalServerPort int port;
    @Autowired RagRetrievalAuthority authority;
    @Autowired RagRecoveryService recovery;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState current;
    @Autowired RagQueryEmbeddingSupervisor embeddings;
    @Autowired EntityManager entities;
    @Autowired RagDocumentChunkRepository chunks;
    @MockitoSpyBean PlatformTransactionManager transactions;
    private final ObjectMapper json=new ObjectMapper();
    private final AtomicReference<Fault> fault=new AtomicReference<>();
    private final AtomicInteger vectors=new AtomicInteger();
    private record Fault(UUID reader,UUID inspection,String state,boolean before,AtomicBoolean fired) {}
    private record Fixture(UUID file,UUID revision,long source,Map<String,Object> scope) {}

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("ppv.rag.test-runtime-jdbc-url",()->"jdbc:postgresql://127.0.0.1:"+WIRE.port()+"/"+POSTGRES.getDatabaseName()+"?sslmode=disable&gssEncMode=disable&socketTimeout=8");
        registry.add("ppv.rag.retrieval.enabled",()->true);
        registry.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }
    @AfterAll static void closeWire() throws Exception { WIRE.listener.close();WIRE.disconnectAll(); }

    @BeforeEach void observeActualTargetCommit() {
        vectors.set(0);
        embeddings.setVectorOverride((id,question)->{vectors.incrementAndGet();float[] vector=new float[384];Arrays.fill(vector,(float)(1/Math.sqrt(384)));return vector;});
        doAnswer(invocation->{
            Fault target=fault.get();
            if(target!=null && !target.fired().get()) {
                int pid=entities.unwrap(org.hibernate.Session.class).doReturningWork(connection->{
                    String sql=target.inspection()!=null?"SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?":"SELECT state FROM rag.retrieval_readers WHERE reader_id=?";
                    boolean match;
                    try(var statement=connection.prepareStatement(sql)) {
                        statement.setObject(1,target.inspection()!=null?target.inspection():target.reader());
                        try(var rows=statement.executeQuery()) { match=rows.next() && target.state().equals(rows.getString(1)); }
                    }
                    if(!match) return 0;
                    try(var statement=connection.createStatement();var rows=statement.executeQuery("SELECT pg_backend_pid()")) {rows.next();return rows.getInt(1);}
                });
                if(pid!=0 && target.fired().compareAndSet(false,true)) WIRE.arm(pid,target.before());
            }
            return invocation.callRealMethod();
        }).when(transactions).commit(any(TransactionStatus.class));
    }
    @AfterEach void resetFault() {
        fault.set(null); WIRE.armedPid.set(0); embeddings.setVectorOverride(null);
        doCallRealMethod().when(transactions).commit(any(TransactionStatus.class));
    }

    private Fixture fixture() {
        UUID file=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.external_file_sources(file_source_id,canonical_identity_digest) VALUES(?,?)",file,"wire-fixture:"+file);
        var scope=completeRetrievalFixtureScope(Map.of(),List.of(file));
        @SuppressWarnings("unchecked") var tuple=((List<Map<String,Object>>)scope.get("sources")).getFirst();
        long source=((Number)tuple.get("source_id")).longValue();
        UUID revision=UUID.fromString((String)tuple.get("revision_id"));
        String content="Lifecycle fixture "+file+"\n";
        var locator=Map.<String,Object>of("profile","ppv-obsidian-source-v1","source_key",file.toString(),"source_version",tuple.get("owner_source_version"),
                "approved_field","content","char_start",0L,"char_end",(long)content.length(),"line_start",1L,"line_end",1L);
        float[] vector=new float[384];Arrays.fill(vector,(float)(1/Math.sqrt(384)));
        chunks.saveAndFlush(new RagDocumentChunk(((Number)tuple.get("generation_row_id")).longValue(),file.toString(),content,locator,
                Map.of("constituent_locators",List.of(locator)),vector,Instant.now()));
        return new Fixture(file,revision,source,scope);
    }

    @ParameterizedTest
    @CsvSource({"ADMITTED,true","ADMITTED,false","EMITTING,true","EMITTING,false","TERMINAL,true","TERMINAL,false"})
    void actualAdmissionEmissionAndFinishRollbackOrLostCommitAck(String phase,boolean before) throws Exception {
        var data=fixture(); UUID reader=UUID.randomUUID();
        var target=new Fault(reader,null,phase,before,new AtomicBoolean());fault.set(target);
        var command=new RagRetrievalCommand(reader,"wire fixture?",RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(data.source(),data.revision(),null,null)),null,null,5);
        try(var client=HttpClient.newHttpClient()) {
            var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                    .timeout(Duration.ofSeconds(25)).header("Authorization",bearerHeader()).header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            var response=client.send(request,HttpResponse.BodyHandlers.ofString());
            org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(10)).until(()->WIRE.dropped.get()==1 && target.fired().get());
            assertThat(WIRE.lastTag).isEqualTo(before?"BEFORE_COMMIT":"COMMIT");
            if(phase.equals("TERMINAL")) {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(json.readTree(response.body()).path("data").path("outcome").asText()).isEqualTo("EVIDENCE");
                if(before) {
                    assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader)).isEqualTo("EMITTING");
                    assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_require_editable(ARRAY[?]::uuid[])",Object.class,data.file())).hasMessageContaining("RAG_SOURCE_BUSY");
                }
            } else {
                assertThat(response.statusCode()).isEqualTo(503);
                assertThat(response.body()).doesNotContain(data.file().toString(),"Lifecycle fixture");
                assertThat(json.readTree(response.body()).path("error").path("code").asText()).isEqualTo("RAG_QUERY_UNAVAILABLE");
                if(phase.equals("ADMITTED")) assertThat(vectors).hasValue(0);
            }
            org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(12)).untilAsserted(()->{
                recovery.reconcile();
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state='TERMINAL'",Integer.class,reader)).isEqualTo(1);
                assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,data.file())).isNull();
            });
        }
    }

    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(booleans={true,false})
    void preregistrationRollbackAndLostAckNeverInventAnIncidentOrReleaseUnknownInspection(boolean before) {
        var data=fixture();UUID reader=UUID.randomUUID(),token=UUID.randomUUID(),inspection=UUID.randomUUID();
        authority.admit(reader,token,"wire-inspection",data.scope(),List.of(data.file()),retrievalFixtureController());
        var target=new Fault(reader,inspection,"UNRESOLVED",before,new AtomicBoolean());fault.set(target);
        assertThatThrownBy(()->authority.beginInspection(inspection,reader,"WIRE_INSPECTOR","wire-inspection",data.file(),data.revision())).isInstanceOf(RuntimeException.class);
        assertThat(WIRE.dropped).hasValue(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_inspections WHERE inspection_id=? AND state='UNRESOLVED'",Integer.class,inspection)).isEqualTo(before?0:1);
        assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,data.file())).isFalse();
        if(!before) {
            assertThatThrownBy(()->authority.finish(reader,token,"CANCELLED","ABORTED",noSenderEvidence("NO_READ_STARTED"),null,null)).hasMessageContaining("Unresolved inspection");
            assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_require_editable(ARRAY[?]::uuid[])",Object.class,data.file())).hasMessageContaining("RAG_SOURCE_BUSY");
            authority.recordInspection(inspection,"CLEAN",false,Map.of("reason","NO_READ_STARTED"));
        }
        authority.finish(reader,token,"CANCELLED","ABORTED",noSenderEvidence("NO_READ_STARTED"),null,null);
    }

    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(booleans={true,false})
    void incidentRollbackOrLostCommitAckRetainsClassificationUntilExactGuardedFinish(boolean before) {
        var data=fixture();UUID reader=UUID.randomUUID(),token=UUID.randomUUID(),inspection=UUID.randomUUID();
        authority.admit(reader,token,"wire-incident",data.scope(),List.of(data.file()),retrievalFixtureController());
        authority.beginInspection(inspection,reader,"WIRE_INCIDENT","wire-incident",data.file(),data.revision());
        var target=new Fault(reader,inspection,"INCIDENT_PENDING",before,new AtomicBoolean());fault.set(target);
        assertThatThrownBy(()->authority.recordInspection(inspection,"INCIDENT_PENDING",true,Map.of("reason","SYNTHETIC_CONFIRMED_INCIDENT")))
                .isInstanceOf(RuntimeException.class);
        assertThat(WIRE.dropped).hasValue(1);
        assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection))
                .isEqualTo(before?"UNRESOLVED":"INCIDENT_PENDING");
        assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,data.file())).isEqualTo(token);
        assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,data.file())).isFalse();
        if(before) {
            assertThatThrownBy(()->authority.finish(reader,token,"EXECUTION_FAILURE","ABORTED",noSenderEvidence("WIRE_INCIDENT"),null,null))
                    .hasMessageContaining("Unresolved inspection");
            authority.recordInspection(inspection,"INCIDENT_PENDING",true,Map.of("reason","SYNTHETIC_CONFIRMED_INCIDENT"));
        }
        authority.finish(reader,token,"EXECUTION_FAILURE","ABORTED",noSenderEvidence("WIRE_INCIDENT"),null,null);
        assertThat(jdbcTemplate.queryForObject("SELECT finish_disposition FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader))
                .isEqualTo("INCIDENT_DENIED");
        assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,data.file())).isTrue();
    }

    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(booleans={true,false})
    void productionRecoveryReconcilesOriginalReaderFreeInspectionAfterRollbackOrLostAck(boolean before) {
        var data=fixture();UUID inspection=UUID.randomUUID();
        var target=new Fault(null,inspection,"UNRESOLVED",before,new AtomicBoolean());fault.set(target);
        assertThatThrownBy(()->authority.beginInspection(inspection,null,"QUERY_PREPARE","reader-free-recovery",data.file(),data.revision()))
                .isInstanceOf(RuntimeException.class);
        UUID generation=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=?",UUID.class,data.source());
        if(!before) {
            assertThat(current.state(generation)).isEqualTo("UNAVAILABLE");
            assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_reserve(ARRAY[?]::uuid[],?)",Object.class,data.file(),UUID.randomUUID()))
                    .hasMessageContaining("inspection unresolved");
            assertThatThrownBy(()->jdbcTemplate.update("DELETE FROM rag.document_chunks WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?)",generation))
                    .hasMessageContaining("Pending inspection");
        }
        recovery.reconcileSnapshots();
        if(!before) assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection)).isEqualTo("CLEAN");
        assertThat(current.state(generation)).isEqualTo("CURRENT");
        UUID token=UUID.randomUUID();
        jdbcTemplate.queryForObject("SELECT rag.external_file_reserve(ARRAY[?]::uuid[],?)",Object.class,data.file(),token);
        jdbcTemplate.queryForObject("SELECT rag.external_file_release(ARRAY[?]::uuid[],?)",Object.class,data.file(),token);
    }
}
