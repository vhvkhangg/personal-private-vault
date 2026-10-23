package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.node.*;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Real selected Python, pinned model and restricted worker SQL; no synthetic positive stage. */
@Timeout(120)
class RagSlice3CorrectiveIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    RagWorkerSupervisor observedSupervisor;
    @Autowired RagPurgeService purgeService;

    @ParameterizedTest @ValueSource(strings={"sign-flip", "vector-swap", "permuted-components"})
    void trustedInferenceRejectsNormPreservingNativeVectorForgery(String fault) throws Exception {
        var input=source("Vectors.md","# First subject\n\nCats are companions.\n\n# Second subject\n\nAstronomy concerns distant stars.\n");
        var job=stage(input);
        var chunks=(ArrayNode)objectMapper.readTree(jdbcTemplate.queryForObject(
                "SELECT chunks_payload::text FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId()));
        assertThat(chunks.size()).isGreaterThanOrEqualTo(2);
        var original=chunks.get(0).path("embedding").deepCopy();
        if(fault.equals("vector-swap")) {
            var other=chunks.get(chunks.size()-1).path("embedding").deepCopy();
            assertThat(other).isNotEqualTo(original);
            ((ObjectNode)chunks.get(0)).set("embedding",other);
            ((ObjectNode)chunks.get(chunks.size()-1)).set("embedding",original);
        } else {
            var forged=(ArrayNode)chunks.get(0).path("embedding");
            for(int i=0;i<forged.size();i++) forged.set(i,objectMapper.getNodeFactory().numberNode(
                    fault.equals("sign-flip")?-original.get(i).asDouble():original.get((i+1)%forged.size()).asDouble()));
        }
        double before=0,after=0;for(int i=0;i<384;i++){before+=Math.pow(original.get(i).asDouble(),2);after+=Math.pow(chunks.get(0).path("embedding").get(i).asDouble(),2);}
        assertThat(after).isCloseTo(before,within(1e-5));
        admin("UPDATE rag.staged_document_outputs SET chunks_payload=CAST(? AS jsonb) WHERE job_id=?",chunks.toString(),job.jobId());
        admin("UPDATE rag.worker_grants SET output_digest=rag.document_result_digest(job_id,attempt_id) WHERE job_id=?",job.jobId());
        assertThatThrownBy(()->finish(job)).isInstanceOf(IllegalStateException.class);
        noPublication(job);
        assertThat(enabledRag.findSource(input.id()).orElseThrow().snapshot().selectedRevisionId()).isNull();
    }

    RagSourceView domainSource() {
        Long owner=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES('FILM') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) VALUES(?,'Approved DOMAIN title','MOVIE','ANIMATION',false)",owner);
        var value=RagTestCommands.register(enabledRag,RagSourceType.FILM,owner.toString());
        return RagTestCommands.approve(enabledRag,value.id(),Set.of("title"));
    }

    @ParameterizedTest @ValueSource(strings={"# Safe structural Markdown\n\nMaterial.\n", "# Rejected Markdown\n\n<script>unsupported-active-markdown-material</script>\n"})
    void domainClosureCannotReinterpretEitherSafeOrRejectedExternalMarkdown(String text) throws Exception {
        var child=source("ExternalChild.md",text);var independent=source("Independent.md","# Independent approved root\n");
        var parent=domainSource();enabledRag.addDependency(parent.id(),child.id());parent=enabledRag.findSource(parent.id()).orElseThrow();
        var roots=List.of(new RagDocumentRootItem("domain",parent.sourceType(),parent.sourceId(),parent.sourceVersion(),parent.authorizationEpoch(),parent.fieldPolicyVersion(),parent.approvedFields()),
                new RagDocumentRootItem("independent",independent.sourceType(),independent.sourceId(),independent.sourceVersion(),independent.authorizationEpoch(),independent.fieldPolicyVersion(),independent.approvedFields()));
        assertThatThrownBy(()->enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots,Map.of()))).isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isZero();
        assertThat(enabledRag.findSource(child.id()).orElseThrow().snapshot().selectedRevisionId()).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by IS NOT NULL",Integer.class)).isZero();
        org.mockito.Mockito.verify(observedSupervisor,org.mockito.Mockito.never()).begin(org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.any());
    }

    @Test void independentDomainAndMarkdownRootsKeepTheirOwnTypedProcessingContracts() throws Exception {
        var domain=domainSource();var child=source("Child.md","# Child structure\n\nApproved dependency.\n");
        var md=source("Parent.md","# Parent structure\n\n![[Child]]\n");enabledRag.addDependency(md.id(),child.id());md=enabledRag.findSource(md.id()).orElseThrow();
        var roots=List.of(new RagDocumentRootItem("domain",domain.sourceType(),domain.sourceId(),domain.sourceVersion(),domain.authorizationEpoch(),domain.fieldPolicyVersion(),domain.approvedFields()),
                new RagDocumentRootItem("markdown",md.sourceType(),md.sourceId(),md.sourceVersion(),md.authorizationEpoch(),md.fieldPolicyVersion(),md.approvedFields()));
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots,Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(
                new RagDocumentBatchTask("domain",domain.sourceId(),"DOMAIN",null,null,null,List.of("title"),Map.of()),
                new RagDocumentBatchTask("markdown",md.sourceId(),"MD",null,null,null,List.of("content"),Map.of())));
        assertThat(finish(job).publishedCount()).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks WHERE source_identity=?",Integer.class,child.sourceId())).isPositive();
    }

    @ParameterizedTest
    @ValueSource(strings={"cancel-intent-first", "automatic-intent-first", "cancel-finishes-first"})
    void requestedCancellationWinsBothOrderedAutomaticFinishPaths(String order) throws Exception {
        var input=source("CancelIntent.md","# Durable cancellation\n\nNo generation may publish.\n");
        var job=stage(input);
        var cancelReached=new java.util.concurrent.CountDownLatch(1);
        var automaticReached=new java.util.concurrent.CountDownLatch(1);
        var cancelRelease=new java.util.concurrent.CountDownLatch(1);
        var automaticRelease=new java.util.concurrent.CountDownLatch(1);
        var first=java.util.concurrent.ConcurrentHashMap.<String>newKeySet();
        org.mockito.Mockito.doAnswer(call->{
            String name=Thread.currentThread().getName();
            if(first.add(name)) {
                boolean cancel=name.equals("rr-cancel");
                (cancel?cancelReached:automaticReached).countDown();
                if(!(cancel?cancelRelease:automaticRelease).await(20,java.util.concurrent.TimeUnit.SECONDS))
                    throw new IllegalStateException("Ordering barrier timed out");
            }
            return call.callRealMethod();
        }).when(observedSupervisor).proveStopped(job.jobId());
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.Callable<Boolean> cancel=()->{Thread.currentThread().setName("rr-cancel");enabledRag.cancelJob(job.jobId());return true;};
        java.util.concurrent.Callable<Boolean> automatic=()->{Thread.currentThread().setName("rr-automatic");return purgeService.abortJob(job.jobId(),false);};
        try {
            java.util.concurrent.Future<Boolean> cancelling,aborting;
            if(order.equals("automatic-intent-first")) {
                aborting=executor.submit(automatic);assertThat(automaticReached.await(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                cancelling=executor.submit(cancel);assertThat(cancelReached.await(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            } else {
                cancelling=executor.submit(cancel);assertThat(cancelReached.await(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                aborting=executor.submit(automatic);assertThat(automaticReached.await(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            }
            if(order.equals("cancel-finishes-first")) {
                cancelRelease.countDown();assertThat(cancelling.get(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                automaticRelease.countDown();assertThat(aborting.get(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            } else {
                automaticRelease.countDown();assertThat(aborting.get(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                cancelRelease.countDown();assertThat(cancelling.get(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            }
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
        } finally {
            cancelRelease.countDown();automaticRelease.countDown();executor.shutdownNow();
            assertThat(executor.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            org.mockito.Mockito.doCallRealMethod().when(observedSupervisor).proveStopped(job.jobId());
        }
    }

    @Test void ordinaryAbortStaysAbortedWhenLateCancellationIsAnIdempotentNoOp() throws Exception {
        var job=stage(source("OrdinaryFailure.md","# Ordinary failure teardown\n"));
        assertThat(purgeService.abortJob(job.jobId(),false)).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("ABORTED");
        assertThatCode(()->enabledRag.cancelJob(job.jobId())).doesNotThrowAnyException();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("ABORTED");
    }

    @Test void durableCancellationIntentSurvivesUncertainStopAndAutomaticReconciliation() throws Exception {
        var job=stage(source("DurableIntent.md","# Durable cancellation intent\n"));
        org.mockito.Mockito.doReturn(false).when(observedSupervisor).proveStopped(job.jobId());
        org.mockito.Mockito.doReturn(false).when(observedSupervisor).stopAndVerify(job.jobId());
        try {
            assertThatThrownBy(()->enabledRag.cancelJob(job.jobId())).isInstanceOf(IllegalStateException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("RECOVERY_REQUIRED");
            assertThat(jdbcTemplate.queryForObject("SELECT metrics->>'cancel_requested' FROM rag.runs WHERE job_id=?",String.class,job.jobId())).isEqualTo("true");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isPositive();
        } finally {
            org.mockito.Mockito.doCallRealMethod().when(observedSupervisor).proveStopped(job.jobId());
            org.mockito.Mockito.doCallRealMethod().when(observedSupervisor).stopAndVerify(job.jobId());
        }
        assertThat(purgeService.abortJob(job.jobId(),false)).isTrue();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
    }
    @org.junit.jupiter.api.BeforeEach void preserveActualWorkerHttpReceipts() throws Exception {
        if(System.getenv("PPV_S3_EVIDENCE")==null)return;
        Path directory=Path.of(System.getenv("PPV_S3_EVIDENCE")).resolve("worker-http");Files.createDirectories(directory);
        org.mockito.Mockito.doAnswer(call->{
            String response=(String)call.callRealMethod();
            var grant=(com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant)call.getArgument(0);
            Files.writeString(directory.resolve(grant.jobId()+"-"+grant.attemptId()+".json"),response,StandardOpenOption.CREATE_NEW);
            RagSlice3Evidence.capture(jdbcTemplate,objectMapper,"producer-returned-"+grant.jobId());
            return response;
        }).when(client).triggerBoundWorkerBatch(org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.anyList());
    }
    @org.junit.jupiter.api.AfterEach void captureSyntheticBindings(org.junit.jupiter.api.TestInfo test) throws Exception {RagSlice3Evidence.capture(jdbcTemplate,objectMapper,test.getDisplayName());}
    @TempDir Path work;
    @Autowired RagGenerationState generationState;
    @Autowired RagSourceAuthority authority;
    @Autowired RagWorkerSupervisor supervisor;
    @Autowired RagDocumentFinalizationGate gate;
    @Autowired org.springframework.core.env.Environment environment;
    @Autowired RagWorkerGrantService grants;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient client;
    @Autowired com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore store;

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
    void noPublication(RagJobAdmissionResult job) {
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.file_outcomes WHERE job_id=?",Integer.class,job.jobId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isPositive();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE selected_revision_id IS NOT NULL",Integer.class)).isZero();
    }
    void admin(String sql, Object... args) throws Exception {
        try (var c=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword()); var s=c.prepareStatement(sql)) {
            for (int i=0;i<args.length;i++) s.setObject(i+1,args[i]); s.executeUpdate();
        }
    }

    @Test void runtimeCannotForgeOrEraseRealProducerProofAndGenuineGenerationIsCurrent() throws Exception {
        var source=source("Real.md","# Genuine\n\nTiếng Việt 中文 English.\n");
        UUID revision=source.snapshot().pendingRevisionId();
        assertThatThrownBy(() -> enabledRag.admitJob(source.id(),new RagIndexCommand(source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),Map.of())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("snapshot");
        var job=stage(source);
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE rag.staged_document_outputs SET chunks_payload='[]'::jsonb WHERE job_id=?",job.jobId())).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM rag.staged_document_outputs WHERE job_id=?",job.jobId())).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE rag.worker_grants SET output_digest=repeat('0',64) WHERE job_id=?",job.jobId())).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM rag.worker_grants WHERE job_id=?",job.jobId())).isInstanceOf(org.springframework.dao.DataAccessException.class);
        var before=objectMapper.readTree(jdbcTemplate.queryForObject("SELECT chunks_payload::text FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId()));
        var result=finish(job); var generation=result.publishedGenerations().get("root-0");
        assertThat(result.publishedCount()).isEqualTo(1);
        assertThat(generationState.state(generation)).isEqualTo("CURRENT");
        var after=jdbcTemplate.queryForList("SELECT content,source_identity,locator::text AS loc,structure_metadata::text AS metadata FROM rag.document_chunks ORDER BY chunk_id");
        assertThat(after).hasSize(before.size());
        for (int i=0;i<after.size();i++) {
            assertThat(after.get(i).get("content")).isEqualTo(before.get(i).path("content").asText());
            assertThat(after.get(i).get("source_identity")).isEqualTo(source.sourceId());
            assertThat(objectMapper.readTree((String)after.get(i).get("loc"))).isEqualTo(before.get(i).path("locator"));
            assertThat(objectMapper.readTree((String)after.get(i).get("metadata"))).isEqualTo(before.get(i).path("structure_metadata"));
        }
        String manifest=jdbcTemplate.queryForObject("SELECT exact_dependency_manifest->>'manifest_text' FROM rag.generation_roots",String.class);
        var node=objectMapper.readTree(manifest).path("nodes").get(0);
        assertThat(node.path("owner_source_version").asLong()).isEqualTo(2);
        assertThat(node.path("snapshot").path("revision_id").asText()).isEqualTo(revision.toString());
        assertThat(node.path("snapshot").path("expected_selected_revision_id").asText()).isEqualTo(revision.toString());
        admin("UPDATE rag.external_file_sources SET snapshot_integrity_failed=true WHERE file_source_id=?",UUID.fromString(source.sourceId()));
        assertThat(generationState.state(generation)).isEqualTo("UNAVAILABLE");
        enabledRag.revokeSource(source.id());
        assertThat(generationState.state(generation)).isIn("REVOKED","ABSENT");
    }

    @ParameterizedTest @ValueSource(strings={"text","fractional","missing-root","duplicate","reorder","source","vector","omit","coordinates","field","pipeline","nan-string","overflow","boolean","null","normalization","revision","epoch","model","tokenizer","fingerprint","provider","chunker","negative-ref","dangling-ref","default-shadow","duplicate-cell","reference-depth","reference-expansion","unicode-coordinate","extra-field"})
    void corruptActualNativeResultsNeverPublish(String fault) throws Exception {
        var job=stage(source("Fault.md","# Heading\n\nFirst block.\n\n| A | B |\n|---|---|\n| one | two |\n\nFinal block.\n"));
        if (fault.equals("missing-root")) {
            // Deliberate owner/admin corruption bypasses retention solely to test common-output admission.
            admin("ALTER TABLE rag.staged_document_outputs DISABLE TRIGGER trg_document_result_retention");
            admin("DELETE FROM rag.staged_document_outputs WHERE job_id=?",job.jobId());
            admin("ALTER TABLE rag.staged_document_outputs ENABLE TRIGGER trg_document_result_retention");
        } else {
            var chunks=(ArrayNode)objectMapper.readTree(jdbcTemplate.queryForObject("SELECT chunks_payload::text FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId()));
            var first=(ObjectNode)chunks.get(0);
            switch (fault) {
                case "text" -> first.put("content","FORGED_CONTENT_ABSENT_FROM_SOURCE");
                case "source" -> { first.put("source_identity","unauthorized"); ((ObjectNode)first.path("locator")).put("source_version",999); }
                case "fractional" -> ((ObjectNode)first.path("structure_metadata").path("constituent_metadata").get(0).path("nodes").get(0)).put("locator_ref",0.5);
                case "duplicate" -> chunks.add(first.deepCopy());
                case "reorder" -> { assertThat(chunks.size()).isGreaterThan(1); var last=chunks.remove(chunks.size()-1); chunks.insert(0,last); }
                case "vector" -> ((ArrayNode)first.path("embedding")).remove(0);
                case "omit" -> chunks.remove(chunks.size()-1);
                case "coordinates" -> ((ObjectNode)first.path("locator")).put("char_end",8000000);
                case "field" -> ((ObjectNode)first.path("locator")).put("approved_field","secret");
                case "pipeline" -> admin("UPDATE rag.staged_document_outputs SET resource_report=jsonb_set(resource_report,'{pipeline_identity,parser_version}','\"wrong-parser\"') WHERE job_id=?",job.jobId());
                case "nan-string" -> ((ArrayNode)first.path("embedding")).set(0,objectMapper.getNodeFactory().textNode("NaN"));
                case "overflow" -> ((ArrayNode)first.path("embedding")).set(0,objectMapper.getNodeFactory().numberNode(new java.math.BigDecimal("1e1000")));
                case "boolean" -> ((ArrayNode)first.path("embedding")).set(0,objectMapper.getNodeFactory().booleanNode(true));
                case "null" -> ((ArrayNode)first.path("embedding")).set(0,objectMapper.getNodeFactory().nullNode());
                case "normalization" -> ((ArrayNode)first.path("embedding")).set(0,objectMapper.getNodeFactory().numberNode(50));
                case "revision" -> admin("UPDATE rag.staged_document_outputs SET resource_report=jsonb_set(resource_report,'{input_binding,snapshot,revision_id}',to_jsonb(?::text)) WHERE job_id=?",UUID.randomUUID().toString(),job.jobId());
                case "epoch" -> admin("UPDATE rag.staged_document_outputs SET resource_report=jsonb_set(resource_report,'{input_binding,snapshot,authorization_epoch}','999') WHERE job_id=?",job.jobId());
                case "model","tokenizer","fingerprint","provider","chunker" -> {
                    String name=switch(fault) {case "model"->"artifact_hash";case "tokenizer"->"tokenizer_artifact_hash";case "fingerprint"->"pipeline_fingerprint";case "provider"->"provider";default->"chunker_version";};
                    admin("UPDATE rag.staged_document_outputs SET resource_report=jsonb_set(resource_report,ARRAY['pipeline_identity',?],to_jsonb('forged'::text)) WHERE job_id=?",name,job.jobId());
                }
                case "negative-ref","dangling-ref" -> ((ObjectNode)first.path("structure_metadata").path("constituent_metadata").get(0).path("nodes").get(0)).put("locator_ref",fault.equals("negative-ref")?-1:999999);
                case "default-shadow" -> ((ObjectNode)first.path("structure_metadata")).putObject("locator_defaults").put("source_key","different-root").put("source_version",999);
                case "duplicate-cell" -> {var meta=(ObjectNode)first.path("structure_metadata");var cells=meta.putArray("cells");cells.addObject().put("id","same");cells.addObject().put("id","same");}
                case "reference-depth" -> {ObjectNode node=(ObjectNode)first.path("structure_metadata");for(int i=0;i<70;i++)node=node.putObject("recursive_reference");node.put("source_key","fabricated");}
                case "reference-expansion" -> {var refs=((ObjectNode)first.path("structure_metadata")).putArray("locator_table");for(int i=0;i<50001;i++)refs.addObject().put("source_key","fabricated").put("char_start",i);}
                case "unicode-coordinate" -> ((ObjectNode)first.path("locator")).put("char_start",2);
                case "extra-field" -> first.put("unauthorized","value");
                default -> throw new AssertionError(fault);
            }
            admin("UPDATE rag.staged_document_outputs SET chunks_payload=CAST(? AS jsonb) WHERE job_id=?",chunks.toString(),job.jobId());
        }
        // Test-only administrative re-seal simulates an incorrect producer result,
        // beyond ordinary runtime capability, so replay is exercised independently
        // of the protected digest mismatch check.
        admin("UPDATE rag.worker_grants SET output_digest=rag.document_result_digest(job_id,attempt_id) WHERE job_id=?",job.jobId());
        assertThatThrownBy(() -> finish(job)).isInstanceOf(IllegalStateException.class);
        noPublication(job);
    }

    @ParameterizedTest @ValueSource(strings={".markdown",".txt",".pdf",".docx",".xls",".xlsx"})
    void capturedFormatCannotBeSpoofedByMdTask(String extension) throws Exception {
        var v=source("Excluded"+extension,"# Plain bytes do not authorize a format\n");
        assertThatThrownBy(() -> stage(v)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(".md");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isZero();
    }

    @Test void editedManifestNameCannotTurnCapturedTxtIntoMd() throws Exception {
        var v=source("Original.txt","# The actual captured source was TXT\n");
        Path manifest=store.getStorageRoot().resolve("revisions").resolve(v.sourceId())
                .resolve(v.snapshot().pendingRevisionId()+".manifest.json");
        assertThat(manifest.toFile().setWritable(true)).isTrue();
        var changed=(ObjectNode)objectMapper.readTree(Files.readString(manifest)); changed.put("sourceName","Original.md");
        Files.writeString(manifest,objectMapper.writeValueAsString(changed));
        assertThatThrownBy(() -> stage(v)).isInstanceOf(IllegalStateException.class).hasMessageContaining("identity");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isZero();
    }

    @Test void pendingPointerCannotSubstituteForTheApprovedRevisionEvenWithEqualHash() throws Exception {
        var v=source("Revision.md","# Exact revision identity\n"); var job=stage(v);
        // Administrative fault injection models corrupt metadata without granting
        // runtime a reservation bypass. Hash/length and approved R remain unchanged.
        admin("ALTER TABLE rag.external_file_sources DISABLE TRIGGER trg_external_file_sources_guard");
        try { admin("UPDATE rag.external_file_sources SET pending_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),UUID.fromString(v.sourceId())); }
        finally { admin("ALTER TABLE rag.external_file_sources ENABLE TRIGGER trg_external_file_sources_guard"); }
        assertThatThrownBy(() -> finish(job)).isInstanceOf(RuntimeException.class);
        noPublication(job);
    }

    @Test void namedNestedAnchoredChildrenRetainSourceSpecificRevisionAndCurrentness() throws Exception {
        var grand=source("Grand.md","# Leaf\n\nShared bytes. ^block\n");
        var child=source("Child.md","# Section\n\n![[Grand#^block|Display]]\n");
        var parent=source("Parent.md","# Parent\n\n![[Child#Section|Alias]]\n");
        enabledRag.addDependency(child.id(),grand.id()); enabledRag.addDependency(parent.id(),child.id());
        parent=enabledRag.findSource(parent.id()).orElseThrow();
        var job=stage(parent); var result=finish(job);
        assertThat(result.publishedCount()).isEqualTo(1);
        var rows=jdbcTemplate.queryForList("SELECT source_identity,content FROM rag.document_chunks");
        assertThat(rows).anySatisfy(r -> { assertThat(r.get("source_identity")).isEqualTo(grand.sourceId()); assertThat(r.get("content")).isEqualTo("\nShared bytes. ^block\n"); });
        assertThat(generationState.state(result.publishedGenerations().get("root-0"))).isEqualTo("CURRENT");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE selected_revision_id IS NOT NULL",Integer.class)).isEqualTo(3);
    }

    @Test void missingOrAmbiguousDependencyNeverPublishesAndCycleIsDenied() throws Exception {
        var one=source("one/Child.md","# Same\n"); var two=source("two/Child.md","# Same\n");
        var parent=source("Parent.md","![[Child]]\n");
        enabledRag.addDependency(parent.id(),one.id()); enabledRag.addDependency(parent.id(),two.id());
        assertThatThrownBy(() -> enabledRag.addDependency(one.id(),parent.id())).isInstanceOf(IllegalStateException.class);
        var current=enabledRag.findSource(parent.id()).orElseThrow();
        var job=stage(current);
        assertThat(finish(job).publishedCount()).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isZero();
    }

    @Test void folderQualifiedAndFrontmatterAliasesBindExactNestedSnapshotsAfterOriginalDeletion() throws Exception {
        var leaf=source("leaves/Grand.md","---\naliases: [Leaf Alias]\n---\n# Leaf\n\nExact leaf text. ^block\n");
        var one=source("one/Child.md","# Section\n\n![[Leaf Alias#^block|Leaf display]]\n");
        var two=source("two/Child.md","# Section\n\nOther exact child.\n");
        var parent=source("Parent.md","# Parent\n\n![[one/Child#Section|One]]\n\n![[two/Child.md#Section|Two]]\n");
        enabledRag.addDependency(one.id(),leaf.id());
        enabledRag.addDependency(parent.id(),one.id()); enabledRag.addDependency(parent.id(),two.id());
        for (String name:List.of("leaves/Grand.md","one/Child.md","two/Child.md","Parent.md")) Files.delete(work.resolve(name));
        var job=stage(enabledRag.findSource(parent.id()).orElseThrow());
        var chunks=objectMapper.readTree(jdbcTemplate.queryForObject("SELECT chunks_payload::text FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId()));
        var identities=new HashSet<String>(); chunks.forEach(c -> identities.add(c.path("source_identity").asText()));
        assertThat(identities).containsExactlyInAnyOrder(parent.sourceId(),one.sourceId(),two.sourceId(),leaf.sourceId());
        for(var chunk:chunks) {
            String source=chunk.path("source_identity").asText();
            assertThat(chunk.path("locator").path("source_key").asText()).isEqualTo(source);
            assertThat(chunk.path("locator").path("source_version").asLong()).isPositive();
            if(source.equals(leaf.sourceId())) assertThat(chunk.path("content").asText()).isEqualTo("\nExact leaf text. ^block\n");
        }
        var binding=objectMapper.readTree(jdbcTemplate.queryForObject("SELECT resource_report::text FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId())).path("input_binding");
        assertThat(binding.path("dependencies").size()).isEqualTo(3);
        for(var child:List.of(one,two,leaf)) assertThat(binding.path("dependencies").path(child.sourceId()).path("snapshot").path("revision_id").asText()).isEqualTo(child.snapshot().pendingRevisionId().toString());
        var result=finish(job); assertThat(result.publishedCount()).isEqualTo(1);
        assertThat(generationState.state(result.publishedGenerations().get("root-0"))).isEqualTo("CURRENT");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE selected_revision_id IS NOT NULL",Integer.class)).isEqualTo(4);
    }

    @ParameterizedTest @ValueSource(strings={"alias","duplicate-target","traversal","absolute","unauthorized"})
    void ambiguousAndUnauthorizedNamesFailTheWholeRoot(String fault) throws Exception {
        var one=source("one/Child.md","---\naliases: [Shared]\n---\nOne.\n");
        var two=source("two/Child.md","---\naliases: [Shared]\n---\nTwo.\n");
        String target=switch(fault) { case "alias"->"Shared"; case "duplicate-target"->"Child"; case "traversal"->"../one/Child"; case "absolute"->"/one/Child"; default->"unknown/Child"; };
        var parent=source("Parent.md","Parent must not partially publish.\n\n![["+target+"]]\n");
        enabledRag.addDependency(parent.id(),one.id()); enabledRag.addDependency(parent.id(),two.id());
        var job=stage(enabledRag.findSource(parent.id()).orElseThrow());
        assertThat(jdbcTemplate.queryForObject("SELECT jsonb_array_length(chunks_payload) FROM rag.staged_document_outputs WHERE job_id=?",Integer.class,job.jobId())).isZero();
        assertThat(finish(job).publishedCount()).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE selected_revision_id IS NOT NULL",Integer.class)).isZero();
    }

    @ParameterizedTest @ValueSource(strings={"version","selected","epoch","reset","revision","revoked","edge","root-version","root-selected","root-epoch"})
    void concurrentChildAuthorityMismatchAfterVerificationCannotPublish(String fault) throws Exception {
        var child=source("Child.md","# Child\n\nExact authorized child.\n");
        var parent=source("Parent.md","![[Child]]\n");enabledRag.addDependency(parent.id(),child.id());
        var job=stage(enabledRag.findSource(parent.id()).orElseThrow());
        var target=fault.startsWith("root-")?parent:child;fault=fault.replace("root-","");
        var ready=new java.util.concurrent.CountDownLatch(1);var resume=new java.util.concurrent.CountDownLatch(1);
        var field=RagDocumentFinalizationGate.class.getDeclaredField("transaction");field.setAccessible(true);
        var original=(org.springframework.transaction.support.TransactionTemplate)field.get(gate);
        var barrier=new org.springframework.transaction.support.TransactionTemplate(original.getTransactionManager()) {
            @Override public <T> T execute(org.springframework.transaction.support.TransactionCallback<T> callback) {
                ready.countDown();try { if(!resume.await(20,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("test publication barrier expired"); }
                catch(InterruptedException e) {Thread.currentThread().interrupt();throw new IllegalStateException(e);}
                return super.execute(callback);
            }
        };barrier.setTimeout(15);field.set(gate,barrier);
        var execution=java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            var finishing=execution.submit(()->finish(job));
            assertThat(ready.await(30,java.util.concurrent.TimeUnit.SECONDS)).as("real D17 verifier completed before the publication barrier").isTrue();
            assertThatThrownBy(()->enabledRag.capturePendingRevision(child.id(),new RagSnapshotCaptureCommand(work.resolve("Child.md").toString(),child.snapshot()))).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(()->enabledRag.revokeSource(child.id())).isInstanceOf(RuntimeException.class);
            // Corrupt-authority injection is administrative, not a runtime bypass.
            String table=(fault.equals("epoch")||fault.equals("revoked"))?"rag.sources":fault.equals("edge")?"rag.dependencies":"rag.external_file_sources";
            admin("ALTER TABLE "+table+" DISABLE TRIGGER USER");
            try {
                switch(fault) {
                    case "version"->admin("UPDATE rag.external_file_sources SET source_version=source_version+1 WHERE file_source_id=?",UUID.fromString(target.sourceId()));
                    case "selected"->admin("UPDATE rag.external_file_sources SET selected_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),UUID.fromString(target.sourceId()));
                    case "reset"->admin("UPDATE rag.external_file_sources SET source_version=source_version+1,consent_reset_version=consent_reset_version+1 WHERE file_source_id=?",UUID.fromString(target.sourceId()));
                    case "revision"->admin("UPDATE rag.external_file_sources SET pending_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),UUID.fromString(target.sourceId()));
                    case "epoch"->admin("UPDATE rag.sources SET authorization_epoch=authorization_epoch+1 WHERE id=?",target.id());
                    case "revoked"->admin("UPDATE rag.sources SET access_state='REVOKED' WHERE id=?",target.id());
                    case "edge"->admin("DELETE FROM rag.dependencies WHERE parent_source_id=? AND child_source_id=?",parent.id(),target.id());
                }
            } finally {admin("ALTER TABLE "+table+" ENABLE TRIGGER USER");}
            resume.countDown();
            assertThatThrownBy(()->finishing.get(30,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOf(java.util.concurrent.ExecutionException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.file_outcomes WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isEqualTo(2);
        } finally {resume.countDown();execution.shutdownNow();assertThat(execution.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();field.set(gate,original);}
    }

    @ParameterizedTest @ValueSource(strings={"PRODUCING","STAGED","VALIDATING"})
    void actualControllerDeathAndFreshJvmRecoveryPreserveExclusiveSnapshotAuthority(String mode) throws Exception {
        var source=source("Restart.md","# Restart\n\n"+"Bounded actual native model and source validation. ".repeat(500));
        UUID runtime=jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class);
        Process controller=null,replacement=null;UUID job=null;
        try {
            var launched=launchProbe(mode,source.id().toString());controller=launched.getKey();
            var receipt=probeReceipt(controller,launched.getValue());job=UUID.fromString(receipt.path("job_id").asText());
            assertThat(receipt.path("controller_pid").asLong()).isEqualTo(controller.pid());
            assertThat(receipt.path("reserved").asInt()).isEqualTo(1);assertThat(receipt.path("generations").asInt()).isZero();
            if(mode.equals("PRODUCING"))assertThat(receipt.path("staged").asInt()).isZero();else assertThat(receipt.path("staged").asInt()).isEqualTo(1);
            var identity=receipt.path("worker_identity");
            if(mode.equals("VALIDATING")) assertThat(identity.has("verification")).isTrue();
            controller.getOutputStream().write('H');controller.getOutputStream().flush();
            assertThat(controller.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(controller.exitValue()).isEqualTo(73);
            for(var process:List.of(identity,identity.path("verification"))) if(process.has("process_id")) {
                long pid=process.path("process_id").asLong(),creation=Long.parseLong(process.path("creation_file_time").asText());
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).until(()->com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess.isRecordedProcessTerminated(pid,creation));
            }
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=(SELECT attempt_id FROM rag.job_leases WHERE job_id=?)",Integer.class,job)).isEqualTo(1);
            var recovered=launchProbe("RECOVER",job.toString());replacement=recovered.getKey();var after=probeReceipt(replacement,recovered.getValue());
            assertThat(replacement.waitFor(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(replacement.exitValue()).isZero();
            assertThat(after.path("status").asText()).isEqualTo("ABORTED");assertThat(after.path("reserved").asInt()).isZero();assertThat(after.path("staged").asInt()).isZero();assertThat(after.path("grants").size()).isZero();assertThat(after.path("generations").asInt()).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).as("no automatic resend/reparse or new attempt").isEqualTo(1);
            try(var files=Files.list(store.getTempDir())) {assertThat(files.toList()).isEmpty();}
        } finally {
            for(Process process:new Process[]{controller,replacement})if(process!=null&&process.isAlive()){process.destroyForcibly();assertThat(process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}
            // Restore this disposable fixture only after no live controller remains.
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=?",runtime);
            if(job!=null)enabledRag.cancelJob(job);
        }
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

    @ParameterizedTest @ValueSource(strings={"generation","chunks","outcome","promotion","commit"})
    void multiRootPhysicalRollbackPreservesPreviouslySelectedRevisionAndIndependentGeneration(String location) throws Exception {
        var old=source("Replace.md","# Previously selected\n");var oldResult=finish(stage(old));
        UUID selected=enabledRag.findSource(old.id()).orElseThrow().snapshot().selectedRevisionId();
        var independent=source("Independent.md","# Independently current generation\n");var preserved=finish(stage(independent)).publishedGenerations().get("root-0");
        var current=enabledRag.findSource(old.id()).orElseThrow();Files.writeString(work.resolve("Replace.md"),"# Replacement must roll back\n");
        current=enabledRag.capturePendingRevision(old.id(),new RagSnapshotCaptureCommand(work.resolve("Replace.md").toString(),current.snapshot()));
        current=enabledRag.approveSource(current.id(),new RagConsentCommand(Set.of("content"),current.sourceVersion(),current.consentResetVersion(),current.authorizationEpoch(),current.fieldPolicyVersion(),null,current.snapshot()));
        var second=source("Second.md","# Second candidate must also roll back\n");var job=stage(current,second);
        String table=switch(location) {case "generation","commit"->"index_generations";case "chunks"->"document_chunks";case "outcome"->"file_outcomes";default->"external_file_sources";};
        admin("CREATE FUNCTION rag.s3_fault() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'bounded synthetic publication fault' USING ERRCODE='53100'; END $$");
        String trigger=location.equals("commit")?"CREATE CONSTRAINT TRIGGER s3_fault AFTER INSERT ON rag."+table+" DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.s3_fault()":
            "CREATE TRIGGER s3_fault BEFORE "+(location.equals("promotion")?"UPDATE":"INSERT")+" ON rag."+table+" FOR EACH ROW "+(location.equals("promotion")?"WHEN (NEW.selected_revision_id IS DISTINCT FROM OLD.selected_revision_id) ":"")+"EXECUTE FUNCTION rag.s3_fault()";
        admin(trigger);
        try {assertThatThrownBy(()->finish(job)).isInstanceOf(RuntimeException.class);}
        finally {admin("DROP TRIGGER s3_fault ON rag."+table);admin("DROP FUNCTION rag.s3_fault()");}
        // Independent connection: no publication writes, outcome or release leaked.
        try(var c=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var q=c.prepareStatement("SELECT (SELECT count(*) FROM rag.index_generations WHERE job_id=?),(SELECT count(*) FROM rag.file_outcomes WHERE job_id=?),(SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?)")) {
            q.setObject(1,job.jobId());q.setObject(2,job.jobId());q.setObject(3,job.attemptId());try(var rows=q.executeQuery()){assertThat(rows.next()).isTrue();assertThat(rows.getInt(1)).isZero();assertThat(rows.getInt(2)).isZero();assertThat(rows.getInt(3)).isEqualTo(2);}
        }
        assertThat(enabledRag.findSource(old.id()).orElseThrow().snapshot().selectedRevisionId()).isEqualTo(selected);
        assertThat(enabledRag.findSource(second.id()).orElseThrow().snapshot().selectedRevisionId()).isNull();
        assertThat(generationState.state(preserved)).isEqualTo("CURRENT");
        assertThat(oldResult.publishedCount()).isEqualTo(1);
    }

    @ParameterizedTest @ValueSource(strings={"writer-first","seal-first","expired-after-wait"})
    void realV3WriterAndPublicationSealUseOneAttemptFenceThroughCommit(String order) throws Exception {
        var job=stage(source("Fence.md","# Exact real native fence payload\n\nActual pinned vectors and source context.\n"));
        var row=jdbcTemplate.queryForMap("SELECT chunks_payload::text AS chunks,resource_report::text AS resources FROM rag.staged_document_outputs WHERE job_id=?",job.jobId());
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2);
        var field=RagDocumentFinalizationGate.class.getDeclaredField("transaction");field.setAccessible(true);var original=(org.springframework.transaction.support.TransactionTemplate)field.get(gate);
        var callbackDone=new java.util.concurrent.CountDownLatch(1);var commitAllowed=new java.util.concurrent.CountDownLatch(1);
        try(var worker=workerConnection()) {
            int pid;try(var q=worker.createStatement();var r=q.executeQuery("SELECT pg_backend_pid()")){r.next();pid=r.getInt(1);}
            if(order.equals("writer-first")) {
                // Test-only reset to the issued, unsealed state. The payload is the
                // actual native result for this exact S/R, never a fabricated positive.
                resetNativeResult(job);worker.setAutoCommit(false);stageNativeResult(worker,job,row);
                try(var q=worker.prepareStatement("SELECT rag.seal_document_result(?,?,?)")){q.setObject(1,job.jobId());q.setObject(2,job.attemptId());q.setString(3,job.leaseToken());q.execute();}
                var publication=executor.submit(()->finish(job));
                awaitBlockedBy(pid);assertThat(publication.isDone()).isFalse();worker.commit();
                assertThat(publication.get(30,java.util.concurrent.TimeUnit.SECONDS).publishedCount()).isEqualTo(1);
            } else if(order.equals("seal-first")) {
                var barrier=new org.springframework.transaction.support.TransactionTemplate(original.getTransactionManager()) {
                    @Override public <T> T execute(org.springframework.transaction.support.TransactionCallback<T> callback) {
                        return super.execute(status->{T result=callback.doInTransaction(status);callbackDone.countDown();try {if(!commitAllowed.await(10,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("test commit barrier expired");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}return result;});
                    }
                };barrier.setTimeout(15);field.set(gate,barrier);
                var publication=executor.submit(()->finish(job));assertThat(callbackDone.await(30,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                var late=executor.submit(()->{try{stageNativeResult(worker,job,row);return "ACCEPTED";}catch(SQLException denied){return denied.getSQLState();}});
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(2)).until(()->jdbcTemplate.queryForObject("SELECT cardinality(pg_blocking_pids(?))>0",Boolean.class,pid));
                commitAllowed.countDown();assertThat(publication.get(20,java.util.concurrent.TimeUnit.SECONDS).publishedCount()).isEqualTo(1);
                assertThat(late.get(10,java.util.concurrent.TimeUnit.SECONDS)).isEqualTo("23514");
            } else {
                resetNativeResult(job);
                // Bounded administrative clock fault; no public deadline extension.
                admin("ALTER TABLE rag.job_leases DISABLE TRIGGER USER");
                try {admin("UPDATE rag.job_leases SET leased_until=clock_timestamp()+interval '2 seconds' WHERE job_id=?",job.jobId());}finally{admin("ALTER TABLE rag.job_leases ENABLE TRIGGER USER");}
                try(var hold=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var lock=hold.prepareStatement("SELECT 1 FROM rag.job_leases WHERE job_id=? FOR UPDATE")) {
                    hold.setAutoCommit(false);lock.setObject(1,job.jobId());lock.execute();
                    var late=executor.submit(()->{try{stageNativeResult(worker,job,row);return "ACCEPTED";}catch(SQLException denied){return denied.getSQLState();}});
                    org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(3)).until(()->jdbcTemplate.queryForObject("SELECT clock_timestamp()>=leased_until FROM rag.job_leases WHERE job_id=?",Boolean.class,job.jobId()));hold.commit();
                    assertThat(late.get(10,java.util.concurrent.TimeUnit.SECONDS)).isEqualTo("23514");
                }
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=?",Integer.class,job.jobId())).isZero();noPublication(job);
            }
        } finally {commitAllowed.countDown();executor.shutdownNow();assertThat(executor.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();field.set(gate,original);}
    }
    void resetNativeResult(RagJobAdmissionResult job) throws Exception {
        admin("ALTER TABLE rag.staged_document_outputs DISABLE TRIGGER trg_document_result_retention");
        try{admin("DELETE FROM rag.staged_document_outputs WHERE job_id=?",job.jobId());}finally{admin("ALTER TABLE rag.staged_document_outputs ENABLE TRIGGER trg_document_result_retention");}
        admin("UPDATE rag.worker_grants SET output_digest=encode(sha256(convert_to('DOCUMENT_V3:'||input_digest,'UTF8')),'hex') WHERE job_id=?",job.jobId());
    }
    void stageNativeResult(Connection worker,RagJobAdmissionResult job,Map<String,Object> row) throws SQLException {
        try(var q=worker.prepareStatement("SELECT rag.stage_document_result(?,?,?,'root-0',true,'SUCCESS','[]'::jsonb,?::jsonb,?::jsonb)")) {q.setObject(1,job.jobId());q.setObject(2,job.attemptId());q.setString(3,job.leaseToken());q.setString(4,(String)row.get("chunks"));q.setString(5,(String)row.get("resources"));q.execute();}
    }
    void awaitBlockedBy(int pid) throws Exception {
        try(var observer=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var q=observer.prepareStatement("SELECT count(*) FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid))")) {
            q.setInt(1,pid);org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(2)).until(()->{try(var rows=q.executeQuery()){rows.next();return rows.getInt(1)>0;}});
        }
    }

    @Test void actualRuntimeWorkerAndPublicRolesCannotForgeV3AdmissionAndVectorsFailDatabaseTypes() throws Exception {
        var job=stage(source("Privileges.md","# Genuine restricted native output\n"));
        try(var runtime=DriverManager.getConnection(POSTGRES.getJdbcUrl(),"rag_runtime",PASSWORD);var worker=workerConnection()) {
            for(String sql:List.of("UPDATE rag.staged_document_outputs SET reason_code='FORGED'","DELETE FROM rag.staged_document_outputs",
                    "INSERT INTO rag.staged_document_outputs(job_id) VALUES(gen_random_uuid())","UPDATE rag.worker_grants SET output_digest=repeat('0',64)",
                    "INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,output_digest,stage_type,consumed_at) SELECT job_id,attempt_id,session_id,session_until,input_digest,scope_digest,encode(sha256(convert_to('DOCUMENT_V3:'||input_digest,'UTF8')),'hex'),'DOCUMENT_V3',clock_timestamp() FROM rag.worker_grants",
                    "INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,output_digest,stage_type) SELECT job_id,attempt_id,session_id,session_until,input_digest,scope_digest,repeat('0',64),'DOCUMENT_V3' FROM rag.worker_grants",
                    "SELECT rag.seal_document_result(gen_random_uuid(),gen_random_uuid(),'wrong')")) sqlDenied(runtime,sql,"42501");
            for(String sql:List.of("INSERT INTO rag.staged_document_outputs(job_id) VALUES(gen_random_uuid())","UPDATE rag.worker_grants SET output_digest=repeat('0',64)",
                    "SELECT * FROM rag.sources","UPDATE rag.external_file_sources SET selected_revision_id=gen_random_uuid()",
                    "INSERT INTO rag.index_generations(generation_id) VALUES(gen_random_uuid())","DELETE FROM rag.job_leases","CREATE TABLE rag.s3_forbidden(id integer)"))sqlDenied(worker,sql,"42501");
            var actual=jdbcTemplate.queryForMap("SELECT chunks_payload::text AS chunks,resource_report::text AS resources FROM rag.staged_document_outputs WHERE job_id=?",job.jobId());
            assertThatThrownBy(()->stageNativeResult(worker,new RagJobAdmissionResult(job.jobId(),UUID.randomUUID(),job.sourceId(),job.leaseToken(),job.leasedUntil(),job.authorizationEpoch()),actual)).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("23514"));
            assertThatThrownBy(()->stageNativeResult(worker,new RagJobAdmissionResult(job.jobId(),job.attemptId(),job.sourceId(),"wrong-token",job.leasedUntil(),job.authorizationEpoch()),actual)).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("23514"));
            admin("UPDATE rag.worker_grants SET stage_type='SYNTHETIC_TEST' WHERE job_id=?",job.jobId());
            try{assertThatThrownBy(()->stageNativeResult(worker,job,actual)).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("23514"));}
            finally{admin("UPDATE rag.worker_grants SET stage_type='DOCUMENT_V3' WHERE job_id=?",job.jobId());}
            for(String sql:List.of("SELECT '[1,2]'::vector(384)","SELECT '[NaN]'::vector(384)","SELECT '[Infinity]'::vector(384)","SELECT '[1e1000]'::vector(384)")) {
                assertThatThrownBy(()->{try(var statement=runtime.createStatement()){statement.execute(sql);}}).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).startsWith("22"));
            }
        }
        String role="s3_public_"+UUID.randomUUID().toString().replace("-","");String password=UUID.randomUUID().toString();
        admin("CREATE ROLE "+role+" LOGIN PASSWORD '"+password+"'");
        try(var publicOnly=DriverManager.getConnection(POSTGRES.getJdbcUrl(),role,password)) {
            for(String sql:List.of("SELECT * FROM rag.staged_document_outputs","SELECT rag.seal_document_result(gen_random_uuid(),gen_random_uuid(),'wrong')","SELECT rag.document_result_digest(gen_random_uuid(),gen_random_uuid())"))sqlDenied(publicOnly,sql,"42501");
        }finally{admin("DROP ROLE "+role);}
        assertThat(finish(job).publishedCount()).isEqualTo(1);
    }
    void sqlDenied(Connection connection,String sql,String state) {
        assertThatThrownBy(()->{try(var statement=connection.createStatement()){statement.execute(sql);}}).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo(state));
    }

    @Test void realActiveV3AttemptRejectsAbsentUnconsumedAndMixedV2Admission() throws Exception {
        var source=source("Unconsumed.md","# Real source-backed native payload\n");var original=stage(source);
        var payload=jdbcTemplate.queryForMap("SELECT chunks_payload::text AS chunks,resource_report::text AS resources FROM rag.staged_document_outputs WHERE job_id=?",original.jobId());
        enabledRag.cancelJob(original.jobId());source=enabledRag.findSource(source.id()).orElseThrow();
        var item=new RagDocumentRootItem("root-0",source.sourceType(),source.sourceId(),source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),source.approvedFields());var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(item),Map.of()));
        try(var worker=workerConnection()) {
            supervisor.begin(job.jobId(),job.attemptId());
            assertThatThrownBy(()->stageNativeResult(worker,job,payload)).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("23514"));
            var prepared=authority.prepareBatch(job.jobId(),job.attemptId());var grant=grants.issueDocumentBatchGrant(job.jobId(),job.attemptId(),RagDocumentInput.encode(authority.documentInput(job.jobId(),job.attemptId(),prepared)));
            assertThat(jdbcTemplate.queryForObject("SELECT consumed_at IS NULL FROM rag.worker_grants WHERE job_id=?",Boolean.class,job.jobId())).isTrue();
            assertThatThrownBy(()->stageNativeResult(worker,job,payload)).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("23514"));
            try(var claim=worker.prepareStatement("SELECT rag.claim_worker_input(?,?,?,?)")) {claim.setObject(1,job.jobId());claim.setObject(2,job.attemptId());claim.setString(3,grant.token());claim.setObject(4,grants.executionSession(job.jobId(),job.attemptId()));claim.execute();}
            assertThatThrownBy(()->{try(var legacy=worker.prepareStatement("SELECT rag.stage_worker_output(?,?,?,'SYNTHETIC_TEST','{}')")){legacy.setObject(1,job.jobId());legacy.setObject(2,job.attemptId());legacy.setString(3,grant.token());legacy.execute();}}).isInstanceOf(SQLException.class).satisfies(e->assertThat(((SQLException)e).getSQLState()).isEqualTo("23514"));
            noPublication(job);
        }finally{supervisor.end(job.jobId());enabledRag.cancelJob(job.jobId());}
    }

    @Test void freshJvmReloadImmediatelyQualifiesTheExactPublishedExternalGeneration() throws Exception {
        var published=finish(stage(source("Fresh.md","# Fresh independent authority reload\n"))).publishedGenerations().get("root-0");
        UUID runtime=jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class);Process process=null;
        try {
            var launched=launchProbe("CURRENT",published.toString());process=launched.getKey();var receipt=probeReceipt(process,launched.getValue());
            assertThat(receipt.path("state").asText()).isEqualTo("CURRENT");assertThat(process.waitFor(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();assertThat(process.exitValue()).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isEqualTo(1);
        } finally {if(process!=null&&process.isAlive()){process.destroyForcibly();assertThat(process.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=?",runtime);}
    }

    @Test void managedVerificationDiskFailureCannotPublishAndRecoveryCleansTheScope() throws Exception {
        var job=stage(source("Disk.md","# Genuine output before managed temporary disk failure\n"));
        try(var entries=Files.list(store.getTempDir())){assertThat(entries.toList()).isEmpty();}
        Files.delete(store.getTempDir());Files.createFile(store.getTempDir());
        try{assertThatThrownBy(()->finish(job)).isInstanceOf(RuntimeException.class);noPublication(job);}
        finally{Files.delete(store.getTempDir());Files.createDirectory(store.getTempDir());}
        enabledRag.cancelJob(job.jobId());
        try(var entries=Files.list(store.getTempDir())){assertThat(entries.toList()).isEmpty();}
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
    }

    @Test void realNativeTransclusionExpansionFailsTheWholeRootBeforeEmbedding() throws Exception {
        var child=source("Expansion.md","Bounded child expansion. ".repeat(100));
        var parent=source("Parent.md","![[Expansion]]\n\n".repeat(600));enabledRag.addDependency(parent.id(),child.id());
        var job=stage(enabledRag.findSource(parent.id()).orElseThrow());
        assertThat(jdbcTemplate.queryForObject("SELECT reason_code FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId())).isEqualTo("RESOURCE_LIMIT_EXCEEDED");
        assertThat(jdbcTemplate.queryForObject("SELECT jsonb_array_length(chunks_payload) FROM rag.staged_document_outputs WHERE job_id=?",Integer.class,job.jobId())).isZero();
        assertThat(finish(job).publishedCount()).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE selected_revision_id IS NOT NULL",Integer.class)).isZero();
    }

    @Test void cancellationDuringActualSnapshotHttpTransferStopsTheConsumerBeforeRelease() throws Exception {
        var source=source("Transfer.md","# Immutable transfer\n\nPrepared by Java only.\n");
        var item=new RagDocumentRootItem("root-0",source.sourceType(),source.sourceId(),source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),source.approvedFields());
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(item),Map.of()));
        var executor=java.util.concurrent.Executors.newSingleThreadExecutor();
        try(var socket=new java.net.Socket()) {
            supervisor.begin(job.jobId(),job.attemptId());
            var prepared=authority.prepareBatch(job.jobId(),job.attemptId());var roots=authority.documentInput(job.jobId(),job.attemptId(),prepared);
            var grant=grants.issueDocumentBatchGrant(job.jobId(),job.attemptId(),RagDocumentInput.encode(roots));
            UUID session=grants.executionSession(job.jobId(),job.attemptId()),nonce=supervisor.requireReady(job.jobId(),job.attemptId());
            byte[] body=objectMapper.writeValueAsBytes(Map.of("contract_version","3.0","job_id",job.jobId().toString(),"attempt_id",job.attemptId().toString(),"lease_token",grant.token(),"input_digest",grant.inputDigest(),"scope_digest",grant.scopeDigest(),"roots",roots));
            socket.connect(new java.net.InetSocketAddress("127.0.0.1",client.port()),3000);
            String headers="POST /v1/worker/execute-batch HTTP/1.1\r\nHost: 127.0.0.1:"+client.port()+"\r\nContent-Type: application/json\r\nConnection: close\r\nContent-Length: "+body.length+"\r\nX-RAG-Service-Key: "+client.serviceCredential()+"\r\nX-RAG-Job-Id: "+job.jobId()+"\r\nX-RAG-Attempt-Id: "+job.attemptId()+"\r\nX-RAG-Lease-Token: "+grant.token()+"\r\nX-RAG-Session-Id: "+session+"\r\nX-RAG-Endpoint-Nonce: "+nonce+"\r\n\r\n";
            socket.getOutputStream().write(headers.getBytes(StandardCharsets.US_ASCII));socket.getOutputStream().write(body,0,body.length-1);socket.getOutputStream().flush();
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(3)).until(()->jdbcTemplate.queryForObject("SELECT consumed_at IS NOT NULL FROM rag.worker_grants WHERE job_id=?",Boolean.class,job.jobId()));
            var cancelling=executor.submit(()->enabledRag.cancelJob(job.jobId()));
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(3)).until(()->!jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId()).equals("ACTIVE"));
            socket.close();supervisor.end(job.jobId());cancelling.get(20,java.util.concurrent.TimeUnit.SECONDS);
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
        }finally{supervisor.end(job.jobId());executor.shutdownNow();assertThat(executor.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}
    }

    @Test void cancellationDuringRealWorkerStagingDrainsItsPhysicalWriterBeforeRelease() throws Exception {
        var source=source("Staging.md","# Real worker staging cancellation\n");
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2);final long key=71283211L;
        admin("CREATE FUNCTION rag.s3_stage_barrier() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN PERFORM pg_advisory_xact_lock(71283211); RETURN NEW; END $$");
        admin("CREATE TRIGGER s3_stage_barrier BEFORE INSERT ON rag.staged_document_outputs FOR EACH ROW EXECUTE FUNCTION rag.s3_stage_barrier()");
        var item=new RagDocumentRootItem("root-0",source.sourceType(),source.sourceId(),source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),source.approvedFields());var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(item),Map.of()));
        try(var holding=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var lock=holding.createStatement()) {
            holding.setAutoCommit(false);lock.execute("SELECT pg_advisory_xact_lock("+key+")");
            var producing=executor.submit(()->enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(new RagDocumentBatchTask("root-0",source.sourceId(),"MD",null,null,null,List.of("content"),Map.of()))));
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(20)).until(()->{try(var q=holding.createStatement()){q.execute("SELECT pg_stat_clear_snapshot()");try(var r=q.executeQuery("SELECT count(*) FROM pg_stat_activity WHERE usename='rag_worker' AND wait_event='advisory'")){r.next();return r.getInt(1)==1;}}});
            int writer;try(var q=holding.createStatement();var r=q.executeQuery("SELECT pid FROM pg_stat_activity WHERE usename='rag_worker' AND wait_event='advisory'")){r.next();writer=r.getInt(1);}
            var cancellation=executor.submit(()->enabledRag.cancelJob(job.jobId()));awaitBlockedBy(writer);holding.commit();
            cancellation.get(25,java.util.concurrent.TimeUnit.SECONDS);
            try{producing.get(25,java.util.concurrent.TimeUnit.SECONDS);}catch(java.util.concurrent.ExecutionException expectedStoppedSender){assertThat(expectedStoppedSender.getCause()).isInstanceOf(IllegalStateException.class);}
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
        }finally{admin("DROP TRIGGER s3_stage_barrier ON rag.staged_document_outputs");admin("DROP FUNCTION rag.s3_stage_barrier()");executor.shutdownNow();assertThat(executor.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}
    }

    @Test void cancellationDuringMeasuredRealNativeCpuWorkStopsBeforeAnyPublication() throws Exception {
        var source=source("Cpu.md","# Native work\n\n"+"Native CPU model work. ".repeat(12000));
        var item=new RagDocumentRootItem("root-0",source.sourceType(),source.sourceId(),source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),source.approvedFields());var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(item),Map.of()));
        var execution=java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            var producing=execution.submit(()->enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(new RagDocumentBatchTask("root-0",source.sourceId(),"MD",null,null,null,List.of("content"),Map.of()))));
            var sessionsField=RagWorkerSupervisor.class.getDeclaredField("sessions");sessionsField.setAccessible(true);
            org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(20)).until(()-> {
                Object session=((Map<?,?>)sessionsField.get(supervisor)).get(job.jobId());if(session==null)return false;
                var processField=session.getClass().getDeclaredField("process");processField.setAccessible(true);
                var process=(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess)processField.get(session);if(process==null)return false;
                return !producing.isDone()&&ProcessHandle.of(process.pid()).flatMap(p->p.info().totalCpuDuration()).map(cpu->cpu.compareTo(java.time.Duration.ofSeconds(4))>=0).orElse(false);
            });
            enabledRag.cancelJob(job.jobId());
            assertThatThrownBy(()->producing.get(20,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOf(java.util.concurrent.ExecutionException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
        }finally{execution.shutdownNow();assertThat(execution.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();}
    }

    @Test void captureTargetHashesCannotBeGraftedOntoAnUnrelatedApprovedName() throws Exception {
        var source=source("folder/Real.md","# Exact capture name proof\n");
        Path manifest=store.getRevisionsDir().resolve(source.sourceId()).resolve(source.snapshot().pendingRevisionId()+".manifest.json");
        var changed=(ObjectNode)objectMapper.readTree(manifest.toFile());changed.putArray("targetDigests").add("0".repeat(64));Files.setAttribute(manifest,"dos:readonly",false);Files.writeString(manifest,changed.toString());
        assertThatThrownBy(()->stage(source)).isInstanceOf(IllegalStateException.class).hasMessageContaining("identity");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isZero();
    }

    @Test void legacyBasenameProofKeepsItsSelectedGenerationCurrentDuringPendingCapture() throws Exception {
        var source=source("Legacy.md","# Previously supported basename-only snapshot\n");
        Path manifest=store.getRevisionsDir().resolve(source.sourceId()).resolve(source.snapshot().pendingRevisionId()+".manifest.json");
        var original=(ObjectNode)objectMapper.readTree(manifest.toFile());original.remove("targetDigests");Files.setAttribute(manifest,"dos:readonly",false);Files.writeString(manifest,original.toString());
        String canonical=jdbcTemplate.queryForObject("SELECT canonical_identity_digest FROM rag.external_file_sources WHERE file_source_id=?",String.class,UUID.fromString(source.sourceId()));
        admin("UPDATE rag.sources SET file_identity=? WHERE id=?",canonical+RagSourceAuthority.digest("Legacy.md"),source.id());
        var generation=finish(stage(source)).publishedGenerations().get("root-0");assertThat(generationState.state(generation)).isEqualTo("CURRENT");
        var current=enabledRag.findSource(source.id()).orElseThrow();Files.writeString(work.resolve("Legacy.md"),"# Pending new content\n");
        current=enabledRag.capturePendingRevision(current.id(),new RagSnapshotCaptureCommand(work.resolve("Legacy.md").toString(),current.snapshot()));
        assertThat(generationState.state(generation)).isEqualTo("CURRENT");
        current=enabledRag.approveSource(current.id(),new RagConsentCommand(Set.of("content"),current.sourceVersion(),current.consentResetVersion(),current.authorizationEpoch(),current.fieldPolicyVersion(),null,current.snapshot()));
        assertThat(finish(stage(current)).publishedCount()).isEqualTo(1);
    }

    @Test void anExtraRootCannotHideBehindAnAdministrativelyResealedNativeReceipt() throws Exception {
        var job=stage(source("Roster.md","# Actual complete root\n"));
        admin("INSERT INTO rag.staged_document_outputs(job_id,attempt_id,root_key,success,reason_code,warning_codes,chunks_payload,resource_report,worker_receipt) SELECT job_id,attempt_id,'unapproved-extra-root',success,reason_code,warning_codes,chunks_payload,resource_report,worker_receipt FROM rag.staged_document_outputs WHERE job_id=?",job.jobId());
        admin("UPDATE rag.worker_grants SET output_digest=rag.document_result_digest(job_id,attempt_id) WHERE job_id=?",job.jobId());
        assertThatThrownBy(()->finish(job)).isInstanceOf(IllegalStateException.class);noPublication(job);
    }

    @Test void twoFrontmatterAliasesSelectOneLargeApprovedRevisionThroughNativePublication() throws Exception {
        var child=source("Child.md","---\naliases: ['Alpha: part', 'Beta: part']\n---\n# Small\n\nTiny. ^block\n\n# Large\n\n"+"x".repeat(500000)+"\n\n![[Unapproved]]\n");
        var parent=source("Parent.md","![[Alpha: part#Small]]\n\n![[Beta: part#^block]]\n");enabledRag.addDependency(parent.id(),child.id());
        var job=stage(enabledRag.findSource(parent.id()).orElseThrow());
        if(System.getenv("PPV_S3_EVIDENCE")!=null) {
            var staged=jdbcTemplate.queryForMap("SELECT root_key,success,reason_code,chunks_payload::text AS chunks,resource_report::text AS resources FROM rag.staged_document_outputs WHERE job_id=?",job.jobId());
            var candidate=Map.of("roots",authority.documentInput(job.jobId(),job.attemptId(),authority.prepareBatch(job.jobId(),job.attemptId())),"results",List.of(Map.of("root_key",staged.get("root_key"),"success",staged.get("success"),"reason_code",staged.get("reason_code"),"chunks",objectMapper.readTree((String)staged.get("chunks")),"resources",objectMapper.readTree((String)staged.get("resources")))));
            Files.write(Path.of(System.getenv("PPV_S3_EVIDENCE")).resolve("large-alias-candidate-"+job.jobId()+".json"),objectMapper.writeValueAsBytes(candidate),StandardOpenOption.CREATE_NEW);
        }
        var result=finish(job);
        assertThat(result.publishedCount()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForList("SELECT content FROM rag.document_chunks WHERE generation_id=(SELECT id FROM rag.index_generations WHERE generation_id=?) AND source_identity=?",String.class,result.publishedGenerations().get("root-0"),child.sourceId())).isNotEmpty().allSatisfy(text->assertThat(text).doesNotContain("x".repeat(100)));
        var selected=result.publishedGenerations().get("root-0");
        assertThat(generationState.state(selected)).isEqualTo("CURRENT");
        var full=source("Whole.md","![[Child]]\n");enabledRag.addDependency(full.id(),child.id());
        var denied=stage(enabledRag.findSource(full.id()).orElseThrow());
        assertThat(finish(denied).publishedCount()).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT reason_code FROM rag.staged_document_outputs WHERE job_id=?",String.class,denied.jobId())).isEqualTo("MD_UNAPPROVED_EMBED");
        assertThat(generationState.state(selected)).isEqualTo("CURRENT");
    }

    @Test void sourceSpecificRevocationPreservesIndependentSiblingGenerationFromTheSameBatch() throws Exception {
        var first=source("First.md","# First independent root\n");var second=source("Second.md","# Second independent root\n");
        var result=finish(stage(first,second));enabledRag.revokeSource(first.id());
        assertThat(generationState.state(result.publishedGenerations().get("root-0"))).isIn("REVOKED","ABSENT");
        assertThat(generationState.state(result.publishedGenerations().get("root-1"))).isEqualTo("CURRENT");
    }

    @Test void unavailableCudaCannotBeClaimedByAnOtherwiseGenuineNativeReceipt() throws Exception {
        var job=stage(source("Provider.md","# Actual selected CPU model\n"));
        assertThat(jdbcTemplate.queryForObject("SELECT resource_report->'pipeline_identity'->>'provider' FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId())).isEqualTo("CPU");
        admin("UPDATE rag.staged_document_outputs SET resource_report=jsonb_set(resource_report,'{pipeline_identity,provider}','\"CUDA\"') WHERE job_id=?",job.jobId());
        admin("UPDATE rag.worker_grants SET output_digest=rag.document_result_digest(job_id,attempt_id) WHERE job_id=?",job.jobId());
        assertThatThrownBy(()->finish(job)).isInstanceOf(IllegalStateException.class);noPublication(job);
    }

    @Test void explicitFailedRootStillRequiresItsExactImmutableRevisionBinding() throws Exception {
        var job=stage(source("Empty.md",""));
        assertThat(jdbcTemplate.queryForObject("SELECT success FROM rag.staged_document_outputs WHERE job_id=?",Boolean.class,job.jobId())).isFalse();
        admin("UPDATE rag.staged_document_outputs SET resource_report=jsonb_set(resource_report,'{input_binding,snapshot,revision_id}',to_jsonb(?::text)) WHERE job_id=?",UUID.randomUUID().toString(),job.jobId());
        admin("UPDATE rag.worker_grants SET output_digest=rag.document_result_digest(job_id,attempt_id) WHERE job_id=?",job.jobId());
        assertThatThrownBy(()->finish(job)).isInstanceOf(IllegalStateException.class);noPublication(job);
    }

    @Test void nativePostgresFloatCanonicalizationRetainsExactOriginalPropertyText() throws Exception {
        var job=stage(source("Properties.md","---\naliases: [Precise]\nscore: -0.0\n---\n# Property\n\nExact inert property source.\n"));
        assertThat(finish(job).publishedCount()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForList("SELECT content FROM rag.document_chunks",String.class)).anySatisfy(text->assertThat(text).contains("score: -0.0"));
    }

    @Test void byteOffsetsCannotSubstituteForOriginalUnicodeCharacterRanges() throws Exception {
        var job=stage(source("Unicode.md","# Tiếng Việt 😀 中文\n\nExact Unicode ranges.\n"));
        var chunks=(ArrayNode)objectMapper.readTree(jdbcTemplate.queryForObject("SELECT chunks_payload::text FROM rag.staged_document_outputs WHERE job_id=?",String.class,job.jobId()));
        var first=(ObjectNode)chunks.get(0);var locator=(ObjectNode)first.path("locator");int originalEnd=locator.path("char_end").asInt();int byteEnd=locator.path("char_start").asInt()+first.path("content").asText().getBytes(StandardCharsets.UTF_8).length;
        assertThat(byteEnd).isGreaterThan(originalEnd);locator.put("char_end",byteEnd);
        admin("UPDATE rag.staged_document_outputs SET chunks_payload=?::jsonb WHERE job_id=?",chunks.toString(),job.jobId());
        admin("UPDATE rag.worker_grants SET output_digest=rag.document_result_digest(job_id,attempt_id) WHERE job_id=?",job.jobId());
        assertThatThrownBy(()->finish(job)).isInstanceOf(IllegalStateException.class);noPublication(job);
    }

    @Test void identicalChildBytesKeepDifferentSourcesAndUnknownEmbedCannotReadSibling() throws Exception {
        var first=source("One.md","Identical child text.\n"); var second=source("Two.md","Identical child text.\n");
        assertThat(first.snapshot().pendingSha256()).isEqualTo(second.snapshot().pendingSha256());
        assertThat(first.snapshot().pendingRevisionId()).isNotEqualTo(second.snapshot().pendingRevisionId());
        var parent=source("Parent.md","![[One]]\n\n![[Two]]\n");
        enabledRag.addDependency(parent.id(),first.id()); enabledRag.addDependency(parent.id(),second.id());
        var result=finish(stage(enabledRag.findSource(parent.id()).orElseThrow()));
        assertThat(result.publishedCount()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForList("SELECT DISTINCT source_identity FROM rag.document_chunks WHERE source_identity IN (?,?)",String.class,first.sourceId(),second.sourceId()))
                .containsExactlyInAnyOrder(first.sourceId(),second.sourceId());
        var unknown=source("Unknown.md","![[One]]\n"); var sibling=source("Sibling.md","Independent approved sibling.\n");
        var mixed=finish(stage(unknown,sibling));
        assertThat(mixed.publishedCount()).isEqualTo(1); assertThat(mixed.failedCount()).isEqualTo(1);
        assertThat(generationState.state(mixed.publishedGenerations().get("root-1"))).isEqualTo("CURRENT");
        assertThat(enabledRag.findSource(unknown.id()).orElseThrow().snapshot().selectedRevisionId()).isNull();
    }

    @Test void cancellingActualSourceVerificationJoinsItsProcessBeforeReleaseAndCleansManagedTemp() throws Exception {
        var job=stage(source("Cancel.md","# Cancel\n\n"+"A bounded verification sentence. ".repeat(200)));
        var execution=java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            var result=execution.submit(() -> finish(job));
            long end=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
            boolean observed=false;
            while (System.nanoTime()<end && !result.isDone()) {
                try (var files=Files.list(store.getStorageRoot().resolve("temp"))) { observed=files.findAny().isPresent(); }
                if (observed) break;
                Thread.sleep(5);
            }
            assertThat(observed).as("actual validation input exists before cancellation").isTrue();
            enabledRag.cancelJob(job.jobId());
            assertThatThrownBy(() -> result.get(20,java.util.concurrent.TimeUnit.SECONDS)).isInstanceOf(java.util.concurrent.ExecutionException.class);
            assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("CANCELLED");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isZero();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
            try (var files=Files.list(store.getStorageRoot().resolve("temp"))) { assertThat(files.toList()).isEmpty(); }
        } finally { execution.shutdownNow(); assertThat(execution.awaitTermination(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue(); }
    }
}
