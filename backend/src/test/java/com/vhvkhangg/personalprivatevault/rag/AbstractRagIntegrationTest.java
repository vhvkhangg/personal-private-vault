package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.UUID;

/** Production services use the actual isolated runtime; only fixture reset/migration use admin. */
@org.springframework.context.annotation.Import(AbstractRagIntegrationTest.RuntimeDatasource.class)
@org.springframework.boot.test.context.SpringBootTest(classes=com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class)
@ExtendWith(AbstractRagIntegrationTest.QueryCountInvocation.class)
abstract class AbstractRagIntegrationTest extends AbstractWebIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.core.env.Environment retrievalHttpEnvironment;
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext retrievalFixtureContext;

    protected String retrievalFixtureController() { return fixtureRuntime.instance().toString(); }

    protected static java.util.Map<String,Object> noSenderEvidence(String reason) {
        return java.util.Map.of("reason",reason,"sender_quiescent",true,"native_quiescent",true,
                "transport_quiescent",true,"buffers_discarded",true,"no_sender_started",true,"at",java.time.Instant.now().toString());
    }

    /** Real, sealed synthetic authority for direct SQL lifecycle tests (not a producer/quality substitute). */
    protected java.util.Map<String,Object> completeRetrievalFixtureScope(java.util.Map<String,Object> input, java.util.Collection<UUID> fileIds) {
        try {
            var files=retrievalFixtureContext.getBean(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagExternalFileSourceRepository.class);
            var sources=retrievalFixtureContext.getBean(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagSourceRepository.class);
            var generations=retrievalFixtureContext.getBean(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagIndexGenerationRepository.class);
            var roots=retrievalFixtureContext.getBean(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagGenerationRootRepository.class);
            var authority=retrievalFixtureContext.getBean(com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.class);
            var store=retrievalFixtureContext.getBean(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore.class);
            var pipeline=com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentFinalizationGate.PIPELINE;
            var tuples=new java.util.ArrayList<java.util.Map<String,Object>>();
            var manifests=new java.util.ArrayList<String>();
            var sourceIds=new java.util.ArrayList<Long>();
            var generationIds=new java.util.ArrayList<String>();
            var generationRows=new java.util.ArrayList<Long>();
            for(UUID id:fileIds.stream().sorted().toList()) {
                var file=files.findByFileSourceId(id).orElseThrow();
                byte[] bytes=("Lifecycle fixture "+id+"\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                String hash=com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(new String(bytes,java.nio.charset.StandardCharsets.UTF_8));
                if(!hash.equals(file.getSelectedRevisionHash())) {
                    UUID revision=file.getSelectedRevisionId()!=null?file.getSelectedRevisionId():UUID.randomUUID();
                    UUID capture=UUID.randomUUID();
                    store.writeTemp(capture,new java.io.ByteArrayInputStream(bytes),(long)bytes.length,hash);
                    store.sealRevision(id,revision,capture,hash,(long)bytes.length,"lifecycle.md",java.util.List.of());
                    file.setSelectedRevision(revision,hash,(long)bytes.length,java.time.Instant.now());
                    files.saveAndFlush(file);
                }
                var source=sources.findBySourceTypeAndSourceId(com.vhvkhangg.personalprivatevault.rag.source.RagSourceType.EXTERNAL_FILE,id.toString()).orElse(null);
                if(source==null) {
                    source=new com.vhvkhangg.personalprivatevault.rag.internal.domain.RagSource(
                            com.vhvkhangg.personalprivatevault.rag.source.RagSourceType.EXTERNAL_FILE,id.toString(),
                            com.vhvkhangg.personalprivatevault.rag.source.RagClassification.NON_PRIVATE,file.getAuthorizationEpoch(),
                            file.getFieldPolicyVersion(),java.util.Set.of("content"),com.vhvkhangg.personalprivatevault.rag.source.RagAccessState.APPROVED,
                            hash,"",java.time.Instant.now());
                    source.bindOwnerResetVersion(file.getConsentResetVersion());
                }
                String identity=file.getCanonicalIdentityDigest()+com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest("lifecycle.md");
                if(source.getId()==null || !identity.equals(source.getFileIdentity()) || !hash.equals(source.getSourceRevisionHash())) {
                    source.setFileIdentity(identity);
                    source.updateHashes(hash,source.getDescriptionRevisionHash(),java.time.Instant.now());
                    source=sources.saveAndFlush(source);
                }
                var prepared=authority.prepareSource(source.getId(),java.util.Map.of());
                var generation=generations.findBySourceId(source.getId()).stream().filter(g -> g.isActive() && "PUBLISHED".equals(g.getStatus())).findFirst().orElse(null);
                if(generation==null) {
                    UUID job=UUID.randomUUID(),attempt=UUID.randomUUID();
                    jdbcTemplate.update("""
                            INSERT INTO rag.job_leases(job_id,attempt_id,source_id,status,lease_token,leased_until,reservation_token,
                                staging_closed,dependency_manifest,created_at,updated_at)
                            VALUES(?,?,?,'COMPLETED','fixture',clock_timestamp()+interval '1 hour',?,true,?,clock_timestamp(),clock_timestamp())
                            """,job,attempt,source.getId(),attempt,prepared.manifest());
                    jdbcTemplate.update("""
                            INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,
                                output_digest,stage_type,created_at)
                            VALUES(?,?,?,clock_timestamp()+interval '1 hour',?,?,?,'SYNTHETIC_STAGE',clock_timestamp())
                            """,job,attempt,UUID.randomUUID(),"0".repeat(64),"0".repeat(64),"0".repeat(64));
                    generation=generations.saveAndFlush(new com.vhvkhangg.personalprivatevault.rag.internal.domain.RagIndexGeneration(
                            UUID.randomUUID(),job,source.getId(),"PUBLISHED",source.getAuthorizationEpoch(),hash,
                            com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(prepared.manifest()),true,java.time.Instant.now()));
                    roots.saveAndFlush(new com.vhvkhangg.personalprivatevault.rag.internal.domain.RagGenerationRoot(generation.getId(),"fixture",
                            java.util.Map.of("manifest_text",prepared.manifest()),(String)pipeline.get("artifact_hash"),(String)pipeline.get("model_version"),
                            (String)pipeline.get("tokenizer_version"),(String)pipeline.get("parser_version"),(String)pipeline.get("chunker_version"),
                            (String)pipeline.get("ocr_pipeline_version"),384,java.time.Instant.now()));
                }
                var tuple=new java.util.LinkedHashMap<String,Object>();
                tuple.put("source_id",source.getId()); tuple.put("file_source_id",id.toString());
                tuple.put("revision_id",file.getSelectedRevisionId().toString()); tuple.put("role","TARGET");
                tuple.put("owner_source_version",file.getSourceVersion()); tuple.put("owner_reset_version",file.getConsentResetVersion());
                tuple.put("authorization_epoch",source.getAuthorizationEpoch()); tuple.put("field_policy_version",source.getFieldPolicyVersion());
                tuple.put("approved_fields",source.getApprovedFields().stream().sorted().toList()); tuple.put("file_identity",source.getFileIdentity());
                tuple.put("generation_id",generation.getGenerationId().toString()); tuple.put("generation_row_id",generation.getId());
                tuples.add(tuple); manifests.add(prepared.manifest()); sourceIds.add(source.getId());
                generationIds.add(generation.getGenerationId().toString()); generationRows.add(generation.getId());
            }
            var scope=new java.util.LinkedHashMap<String,Object>(input);
            scope.put("sources",tuples); scope.put("authority_manifests",manifests);
            scope.put("producer_manifest_digests",manifests.stream().map(com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority::digest).toList());
            scope.put("external_file_ids",fileIds.stream().sorted().map(UUID::toString).toList()); scope.put("source_ids",sourceIds);
            scope.put("generation_ids",generationIds); scope.put("generation_row_ids",generationRows); scope.put("pipeline",pipeline);
            scope.putIfAbsent("controller_pid",ProcessHandle.current().pid());
            scope.putIfAbsent("controller_creation_file_time",Long.toString(com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalControllerIdentity.creationFileTime()));
            return scope;
        } catch(Exception failure) { throw new IllegalStateException("Synthetic retrieval authority fixture failed",failure); }
    }

    /** Real pinned-container evidence sender; tests never consume an unowned synchronous result. */
    protected com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalResponse retrieveOverHttp(
            com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalCommand command) throws Exception {
        var json = new com.fasterxml.jackson.databind.ObjectMapper();
        var response=requestRetrievalOverHttp(command);
        org.assertj.core.api.Assertions.assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        var result=json.treeToValue(json.readTree(response.body()).path("data"),
                com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalResponse.class);
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).until(() ->
                jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=? AND state<>'TERMINAL'",
                        Integer.class,command.requestId())==0);
        return result;
    }

    protected java.net.http.HttpResponse<String> requestRetrievalOverHttp(
            com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalCommand command) throws Exception {
        var json=new com.fasterxml.jackson.databind.ObjectMapper();
        int port = retrievalHttpEnvironment.getRequiredProperty("local.server.port", Integer.class);
        try (var client = java.net.http.HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + port + "/api/v1/rag/retrievals"))
                    .timeout(java.time.Duration.ofSeconds(120))
                    .header("Authorization", bearerHeader()).header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return response;
        }
    }

    protected java.net.http.HttpResponse<String> requestQaOverHttp(
            com.vhvkhangg.personalprivatevault.rag.api.RagQaCommand command) throws Exception {
        var json=new com.fasterxml.jackson.databind.ObjectMapper();
        int port = retrievalHttpEnvironment.getRequiredProperty("local.server.port", Integer.class);
        try (var client = java.net.http.HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + port + "/api/v1/rag/qa"))
                    .timeout(java.time.Duration.ofSeconds(120))
                    .header("Authorization", bearerHeader()).header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json.writeValueAsString(command))).build();
            return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        }
    }

    protected java.net.http.HttpResponse<String> requestQaCancelOverHttp(UUID requestId) throws Exception {
        int port = retrievalHttpEnvironment.getRequiredProperty("local.server.port", Integer.class);
        try (var client = java.net.http.HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + port + "/api/v1/rag/qa/" + requestId + "/cancel"))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .header("Authorization", bearerHeader())
                    .POST(java.net.http.HttpRequest.BodyPublishers.noBody()).build();
            return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        }
    }
    /** Only the frozen SessionFactory-wide query measurement needs recovery coordination. */
    static class QueryCountInvocation implements InvocationInterceptor {
        @Override
        public void interceptTestMethod(Invocation<Void> invocation,
                ReflectiveInvocationContext<Method> method, ExtensionContext context) throws Throwable {
            if (!context.getRequiredTestClass().getName().equals(
                    "com.vhvkhangg.personalprivatevault.rag.RagSourceAuthorityIntegrationTest")
                    || !method.getExecutable().getName().equals(
                    "genuinelyFreshBatchProjectionsHaveConstantQueryCountsAndEnforceByteBudget")) {
                invocation.proceed();
                return;
            }
            var recovery=SpringExtension.getApplicationContext(context).getBean(RagRecoveryService.class);
            // Intrinsic monitor release also propagates assertions, exceptions and timeouts.
            synchronized (recovery) {
                invocation.proceed();
            }
        }
    }
    static final java.nio.file.Path SNAPSHOT_TEST_ROOT;
    static {
        try { SNAPSHOT_TEST_ROOT=java.nio.file.Files.createTempDirectory("ppv-rag-integration-store-"); }
        catch (java.io.IOException failure) { throw new ExceptionInInitializerError(failure); }
    }
    static final String PASSWORD=UUID.randomUUID().toString().replace("-","");
    @org.springframework.beans.factory.annotation.Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity fixtureRuntime;

    @org.junit.jupiter.api.BeforeEach
    void bindEmptyIsolatedFixtureToItsCurrentSpringContext() {
        // Cached Spring contexts share only this disposable test database. Never
        // steal an active slot or impersonate recovery of a real consumer.
        int bound=jdbcTemplate.update("""
                INSERT INTO rag.runtime_state(singleton,current_instance)
                SELECT true,CAST(? AS uuid)
                WHERE NOT EXISTS(SELECT 1 FROM rag.job_leases WHERE status IN
                    ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING'))
                  AND NOT EXISTS(SELECT 1 FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL)
                  AND NOT EXISTS(SELECT 1 FROM rag.retrieval_readers WHERE state <> 'TERMINAL')
                ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance
                """,fixtureRuntime.instance());
        org.assertj.core.api.Assertions.assertThat(bound)
                .as("prior fixture must leave no unresolved active slot or source reservation").isEqualTo(1);
    }
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods=false)
    static class RuntimeDatasource {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        javax.sql.DataSource ragRuntimeDatasource(org.springframework.core.env.Environment environment) {
            var config=new com.zaxxer.hikari.HikariConfig();
            config.setJdbcUrl(environment.getProperty("ppv.rag.test-runtime-jdbc-url",POSTGRES.getJdbcUrl()));
            if (environment.containsProperty("ppv.rag.test-runtime-jdbc-url")) config.setConnectionTimeout(5000);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setIdleTimeout(10000);
            config.setUsername("rag_runtime");config.setPassword(PASSWORD);
            return new com.zaxxer.hikari.HikariDataSource(config);
        }
    }
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        var migration=org.flywaydb.core.Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())
                .locations("classpath:db/migration").load();
        org.assertj.core.api.Assertions.assertThat(java.util.Arrays.stream(migration.info().all()).filter(info->info.getVersion()!=null)
                .map(org.flywaydb.core.api.MigrationInfo::getVersion).max(java.util.Comparator.naturalOrder()).orElseThrow().getVersion()).isEqualTo("24");
        var applied=migration.info().applied();
        if(applied.length==0 || applied[applied.length-1].getVersion().compareTo(org.flywaydb.core.api.MigrationVersion.fromVersion("23"))<=0) {
            var baseline=org.flywaydb.core.Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())
                    .locations("classpath:db/migration").target("23").load();
            baseline.migrate(); baseline.validate();
            var history=baseline.info().applied();
            org.assertj.core.api.Assertions.assertThat(history[history.length-1].getVersion().getVersion()).isEqualTo("23");
            System.out.println("RAG_TEST_PRE_V24_HISTORY="+java.util.Arrays.stream(history)
                    .filter(info->info.getVersion()!=null)
                    .map(info->info.getVersion().getVersion()+":"+info.getChecksum()+":"+info.getState()).toList());
        } else {
            org.assertj.core.api.Assertions.assertThat(applied[applied.length-1].getVersion().getVersion()).isEqualTo("24");
        }
        migration.migrate(); migration.validate();
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.createStatement()) {
            statement.execute("ALTER ROLE rag_runtime LOGIN PASSWORD '"+PASSWORD+"'");
        }catch(SQLException failure){throw new IllegalStateException(failure);}
        registry.add("ppv.rag.storage.snapshot-root",SNAPSHOT_TEST_ROOT::toString);
        registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",()->"rag_runtime");
        registry.add("spring.datasource.password",()->PASSWORD);
        registry.add("spring.flyway.enabled",()->false);
        registry.add("ppv.security.jwt.secret",()->"dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
    }
    @Override
    @AfterEach
    protected void cleanUpDatabase() {
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.execute("SELECT pg_advisory_xact_lock("+com.vhvkhangg.personalprivatevault.rag.internal.application.RagDependencyGraph.LOCK_ID+")");
            // Administrative fixture reset only. Production source tombstones must never be truncated.
            statement.execute("TRUNCATE public.vault_rag_source_state,rag.retrieval_inspections,rag.retrieval_readers,rag.external_file_sources,rag.batch_roots,rag.file_outcomes,rag.generation_roots,rag.document_chunks,rag.staged_document_outputs,rag.runs,rag.dependencies,rag.staged_outputs,rag.worker_grants,rag.job_leases,rag.index_generations,rag.sources,public.vault_entries,public.fiction_genres,public.film_genres CASCADE");
            connection.commit();

        }catch(SQLException failure){throw new IllegalStateException(failure);}
    }
}
