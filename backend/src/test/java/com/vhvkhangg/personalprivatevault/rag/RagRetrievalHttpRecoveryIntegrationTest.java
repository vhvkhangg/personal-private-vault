package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagDocumentChunk;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagDocumentChunkRepository;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/** Production startup/reconciliation after a genuine HTTP-owning JVM dies with its original inspection outstanding. */
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class RagRetrievalHttpRecoveryIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @TempDir Path work;
    @Autowired Environment environment;
    @Autowired RagDocumentChunkRepository chunks;
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("ppv.rag.retrieval.enabled",()->true);
        registry.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }

    @ParameterizedTest @ValueSource(strings={"prepare","reader"})
    void actualHttpControllerCrashReconcilesOriginalInspectionThroughNewProductionOwner(String phase) throws Exception {
        UUID file=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.external_file_sources(file_source_id,canonical_identity_digest) VALUES(?,?)",file,"http-recovery:"+file);
        var scope=completeRetrievalFixtureScope(Map.of(),List.of(file));
        @SuppressWarnings("unchecked") var tuple=((List<Map<String,Object>>)scope.get("sources")).getFirst();
        long source=((Number)tuple.get("source_id")).longValue();UUID revision=UUID.fromString((String)tuple.get("revision_id"));
        String content="Lifecycle fixture "+file+"\n";
        var locator=Map.<String,Object>of("profile","ppv-obsidian-source-v1","source_key",file.toString(),"source_version",tuple.get("owner_source_version"),
                "approved_field","content","char_start",0L,"char_end",(long)content.length(),"line_start",1L,"line_end",1L);
        float[] vector=new float[384];Arrays.fill(vector,(float)(1/Math.sqrt(384)));
        chunks.saveAndFlush(new RagDocumentChunk(((Number)tuple.get("generation_row_id")).longValue(),file.toString(),content,locator,
                Map.of("constituent_locators",List.of(locator)),vector,Instant.now()));
        var before=startController("before",phase);
        try(var client=HttpClient.newHttpClient()) {
            UUID reader=UUID.randomUUID();
            var request=client.sendAsync(request(before.port(),new RagRetrievalCommand(reader,"fixture evidence?",RagRetrievalMode.GENERIC,
                    List.of(new RagSourceSelectionEntry(source,revision,null,null)),null,null,5)),HttpResponse.BodyHandlers.ofString());
            org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(45)).until(()->Files.exists(before.pause()) || before.process().hasTerminatedTree());
            assertThat(before.pause()).as("production HTTP controller reached committed inspection pause").exists();
            String producer="prepare".equals(phase)?"QUERY_PREPARE":"QUERY_EVIDENCE";
            UUID inspection=jdbcTemplate.queryForObject("SELECT inspection_id FROM rag.retrieval_inspections WHERE affected_file_source_id=? AND producer=? AND state='UNRESOLVED'",UUID.class,file,producer);
            String proof=jdbcTemplate.queryForObject("SELECT inspection_proof::text FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection);
            var metadata=new ObjectMapper().readTree(proof);
            assertThat(metadata.path("controller_pid").asLong()).isEqualTo(before.process().pid());
            assertThat(metadata.path("controller_creation_file_time").asText()).isEqualTo(Long.toString(before.process().creationTimeFileTime()));
            assertThat(request.isDone()).isFalse();
            if("reader".equals(phase)) {
                assertThat(jdbcTemplate.queryForObject("SELECT reader_id FROM rag.retrieval_inspections WHERE inspection_id=?",UUID.class,inspection)).isEqualTo(reader);
                assertThat(jdbcTemplate.queryForObject("SELECT worker_pid FROM rag.retrieval_readers WHERE reader_id=?",Long.class,reader)).isPositive();
            } else assertThat(jdbcTemplate.queryForObject("SELECT reader_id FROM rag.retrieval_inspections WHERE inspection_id=?",UUID.class,inspection)).isNull();
            assertThat(before.process().terminateAndVerify(Duration.ofSeconds(5))).isTrue();
            assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection)).isEqualTo("UNRESOLVED");
            assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_reserve(ARRAY[?]::uuid[],?)",Object.class,file,UUID.randomUUID()))
                    .hasMessageContaining("inspection unresolved");
            var after=startController("after","none");
            try {
                org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(()->{
                    assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection)).isEqualTo("CLEAN");
                    assertThat(jdbcTemplate.queryForObject("SELECT reserved_by FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,file)).isNull();
                });
                assertThat(jdbcTemplate.queryForObject("SELECT inspection_proof::text FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,inspection)).isEqualTo(proof);
                if("reader".equals(phase)) assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_readers WHERE reader_id=?",String.class,reader)).isEqualTo("TERMINAL");
                var response=client.send(request(after.port(),new RagRetrievalCommand(UUID.randomUUID(),"fixture evidence?",RagRetrievalMode.GENERIC,
                        List.of(new RagSourceSelectionEntry(source,revision,null,null)),null,null,5)),HttpResponse.BodyHandlers.ofString());
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(new ObjectMapper().readTree(response.body()).path("data").path("outcome").asText()).isEqualTo("EVIDENCE");
                org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(8)).until(()->jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE state<>'TERMINAL'",Integer.class)==0);
                System.out.println("S19_HTTP_CONTROLLER_RECOVERY phase="+phase+" original_operation_reconciled=true new_owner_http_evidence=true");
            } finally {assertThat(after.process().terminateAndVerify(Duration.ofSeconds(5))).isTrue();after.process().close();}
        } finally {
            before.process().terminateAndVerify(Duration.ofSeconds(5));before.process().close();
        }
    }

    private HttpRequest request(int port,RagRetrievalCommand command) throws Exception {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/v1/rag/retrievals"))
                .timeout(Duration.ofSeconds(60)).header("Authorization",bearerHeader()).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(command))).build();
    }
    private record Controller(NativeWorkerProcess process,int port,Path pause) {}
    private Controller startController(String name,String phase) throws Exception {
        var properties=new Properties();
        properties.setProperty("spring.datasource.url",POSTGRES.getJdbcUrl());properties.setProperty("spring.datasource.username","rag_runtime");
        properties.setProperty("spring.datasource.password",PASSWORD);properties.setProperty("spring.flyway.enabled","false");
        properties.setProperty("ppv.security.jwt.secret",environment.getRequiredProperty("ppv.security.jwt.secret"));
        properties.setProperty("ppv.rag.storage.snapshot-root",SNAPSHOT_TEST_ROOT.toString());
        properties.setProperty("ppv.rag.retrieval.enabled","true");properties.setProperty("ppv.rag.test-scoped-execution","true");
        properties.setProperty("ppv.rag.reconciliation-delay-ms","3600000");
        for(String key:List.of("ppv.rag.worker.executable","ppv.rag.worker.venv-launcher","ppv.rag.worker.working-directory"))
            properties.setProperty(key,environment.getRequiredProperty(key));
        properties.setProperty("server.port","0");properties.setProperty("server.address","127.0.0.1");
        properties.setProperty("spring.main.banner-mode","off");properties.setProperty("logging.level.root","WARN");
        Path port=work.resolve(name+"-port"),pause=work.resolve(name+"-paused"),configuration=work.resolve(name+".properties");
        properties.setProperty("fixture.port-file",port.toString());properties.setProperty("fixture.pause-file",pause.toString());properties.setProperty("fixture.phase",phase);
        try(var output=Files.newOutputStream(configuration,StandardOpenOption.CREATE_NEW)) {properties.store(output,"Disposable HTTP recovery fixture");}
        String classpath=System.getProperty("surefire.test.class.path",System.getProperty("java.class.path"));
        Path arguments=work.resolve(name+".args");
        Files.writeString(arguments,"--enable-native-access=ALL-UNNAMED\n-Xmx512m\n-cp\n"+quote(classpath)+"\n"
                +RagRetrievalHttpControllerFixture.class.getName()+"\n"+quote(configuration.toString())+"\n",StandardOpenOption.CREATE_NEW);
        var env=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        for(String key:List.of("SystemRoot","WINDIR","TEMP","TMP","PATH","LOCALAPPDATA")) if(System.getenv(key)!=null) env.put(key,System.getenv(key));
        var process=NativeWorkerProcess.launchSuspended(UUID.randomUUID(),Path.of(System.getProperty("java.home"),"bin","java.exe"),
                List.of("@"+arguments),env,repository(),3L*1024*1024*1024,3);
        process.resume();
        try {
            org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(50)).until(()->Files.exists(port) || process.hasTerminatedTree());
            String failure=Files.exists(Path.of(port+".failure"))?Files.readString(Path.of(port+".failure")):"no category";
            assertThat(port).as("owned HTTP controller startup; category=%s",failure).exists();
            return new Controller(process,Integer.parseInt(Files.readString(port)),pause);
        } catch(Throwable failed) {process.terminateAndVerify(Duration.ofSeconds(5));process.close();throw failed;}
    }
    private static String quote(String value) {return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";}
}
