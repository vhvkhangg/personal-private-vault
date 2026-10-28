package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagQueryEmbeddingSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@org.springframework.boot.test.context.SpringBootTest(classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
class RagRetrievalEmbeddingNativeIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @Autowired
    private RagRetrievalOperations ragRetrievalOperations;

    @Autowired
    private RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority retrievalAuthority;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity runtime;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalCurrentAuthority currentAuthority;

    @Autowired
    private RagSourceRepository ragSourceRepository;

    @Autowired
    private RagExternalFileSourceRepository ragExternalFileSourceRepository;

    @Autowired
    private RagIndexGenerationRepository ragIndexGenerationRepository;

    @Autowired
    private RagDocumentChunkRepository ragDocumentChunkRepository;

    @Autowired
    private RagOriginalBytesStore ragOriginalBytesStore;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority ragSourceAuthority;

    @DynamicPropertySource
    static void enableNativeRetrieval(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @BeforeEach
    void ensureNoVectorOverride() {
        ragQueryEmbeddingSupervisor.setVectorOverride(null);
    }

    @AfterEach
    void cleanUpOverride() {
        ragQueryEmbeddingSupervisor.setVectorOverride(null);
    }

    @org.junit.jupiter.api.io.TempDir java.nio.file.Path producerInput;

    @Test
    @DisplayName("Actual P18 Markdown producer/publication -> native CPU query -> PostgreSQL ranking -> HTTP, VI/EN/Han")
    void realWindowsCpuNativeEmbeddingFlowSucceeds() throws Exception {
        String[] notes={
                "# Kho lưu trữ\r\n\r\nThành phố được chọn cho kho lưu trữ là Hà Nội. 🌿 ^archive-block\r\n",
                "---\nprivate: true\nnsfw: true\n---\n# Archive\n\nThe archive keeps its silver telescope in the observatory.\n",
                "# 图书馆\n\n火星图书馆的开放时间是每周二上午九点。\n"
        };
        String[] questions={"Kho lưu trữ được đặt ở thành phố nào?","Where is the archive's silver telescope kept?","火星图书馆什么时候开放？"};
        String[] expected={"Hà Nội","silver telescope","每周二"};
        var views=new ArrayList<RagSourceView>();
        var roots=new ArrayList<RagDocumentRootItem>();
        var tasks=new ArrayList<RagDocumentBatchTask>();
        for(int i=0;i<notes.length;i++) {
            var path=producerInput.resolve("native-"+i+".md");
            java.nio.file.Files.writeString(path,notes[i],StandardCharsets.UTF_8);
            var candidate=enabledRag.registerSnapshotCandidate(path.toString());
            var approved=enabledRag.approveSource(candidate.id(),new RagConsentCommand(Set.of("content"),candidate.sourceVersion(),
                    candidate.consentResetVersion(),candidate.authorizationEpoch(),candidate.fieldPolicyVersion(),null,candidate.snapshot()));
            views.add(approved);
            roots.add(new RagDocumentRootItem("native-"+i,approved.sourceType(),approved.sourceId(),approved.sourceVersion(),
                    approved.authorizationEpoch(),approved.fieldPolicyVersion(),approved.approvedFields()));
            tasks.add(new RagDocumentBatchTask("native-"+i,approved.sourceId(),"MD",null,null,null,List.of("content"),Map.of()));
        }
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots,Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),tasks);
        String manifest=jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job.jobId());
        var publication=enabledRag.finalizeDocumentBatch(job.jobId(),job.attemptId(),
                new RagDocumentBatchFinalizeCommand(com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest)));
        assertThat(publication.publishedCount()).isEqualTo(3);
        for (var chunk : ragDocumentChunkRepository.findAll()) {
            var original = views.stream().filter(v -> v.sourceId().equals(chunk.getSourceIdentity())).findFirst().orElseThrow();
            assertThat(chunk.getLocator()).as("actual P18 locator: %s; parts: %s",chunk.getLocator(),chunk.getStructureMetadata())
                    .containsEntry("source_key",chunk.getSourceIdentity());
            assertThat(((Number)chunk.getLocator().get("source_version")).longValue()).isEqualTo(original.sourceVersion());
        }
        var selection=views.stream().map(v -> {
            var selected=enabledRag.findSource(v.id()).orElseThrow();
            return new RagSourceSelectionEntry(selected.id(),selected.snapshot().selectedRevisionId(),null,null);
        }).toList();
        for(int i=0;i<questions.length;i++) {
            var result=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),questions[i],RagRetrievalMode.GENERIC,
                    selection,null,null,5));
            assertThat(result.outcome()).isEqualTo(RagRetrievalOutcome.EVIDENCE);
            String passage=expected[i];
            assertThat(result.matches()).anySatisfy(match -> assertThat(match.excerpt()).contains(passage));
            var metrics=jdbcTemplate.queryForMap("SELECT (quiescence_evidence->>'startup_nanos')::bigint startup,(quiescence_evidence->>'embedding_total_nanos')::bigint total,(quiescence_evidence->>'peak_memory_bytes')::bigint peak FROM rag.retrieval_readers WHERE reader_id=?",result.requestId());
            assertThat(((Number)metrics.get("startup")).longValue()).isPositive();
            assertThat(((Number)metrics.get("total")).longValue()).isBetween(1L,120_000_000_000L);
            assertThat(((Number)metrics.get("peak")).longValue()).isBetween(1L,2L*1024*1024*1024);
            System.out.println("S19_NATIVE_HTTP_METRICS language="+List.of("VI","EN","HAN").get(i)+" "+metrics);
        }
    }

    @Test void duplicateNamesWholeFileConsentAndExplicitNoncanonRemainDistinctOverActualProducer() throws Exception {
        String coreText="---\nprivate: true\nnsfw: true\n---\n# Core\n\nThe cobalt archive contains CORE_WHOLE_FILE_SENTINEL.\n";
        var core=approveFixture("one/Shared.md",coreText);
        var noncore=approveFixture("two/Shared.md","# Noncanon\n\nThe amber museum contains NONCORE_SENTINEL.\n");
        publishFixtures(core,noncore);
        var coreEntry=selection(core,"IDEAVERSE_CORE","one/Shared.md");
        var noncoreEntry=selection(noncore,"IDEAVERSE_NONCORE","two/Shared.md");
        var canon=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"What does the cobalt archive contain?",RagRetrievalMode.IDEAVERSE_CANON,
                List.of(coreEntry),"roster-1","synthetic-collection",10));
        assertThat(canon.matches()).isNotEmpty().allSatisfy(m->{
            assertThat(m.sourceId()).isEqualTo(core.id());assertThat(m.group()).isEqualTo("IDEAVERSE_CORE");
            assertThat(m.displayName()).isEqualTo("one/Shared.md");assertThat(m.excerpt()).doesNotContain("NONCORE_SENTINEL");
        });
        assertThat(canon.matches()).anySatisfy(m->assertThat(m.excerpt()).contains("CORE_WHOLE_FILE_SENTINEL"));
        var optedIn=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"What do the archive and museum contain?",RagRetrievalMode.IDEAVERSE_NONCANON,
                List.of(coreEntry,noncoreEntry),"roster-1","synthetic-collection",10));
        assertThat(optedIn.matches()).anySatisfy(m->{assertThat(m.sourceId()).isEqualTo(noncore.id());assertThat(m.group()).isEqualTo("IDEAVERSE_NONCORE");assertThat(m.excerpt()).contains("NONCORE_SENTINEL");});
        var unapprovedPath=producerInput.resolve("unapproved.md");java.nio.file.Files.writeString(unapprovedPath,coreText);
        var unapproved=enabledRag.registerSnapshotCandidate(unapprovedPath.toString());
        var denied=requestRetrievalOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"show private-marked file",RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(unapproved.id(),unapproved.snapshot().pendingRevisionId(),null,null)),null,null,5));
        assertThat(denied.statusCode()).isEqualTo(404);assertThat(denied.body()).doesNotContain("CORE_WHOLE_FILE_SENTINEL");
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(7)).untilAsserted(()->
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE state<>'TERMINAL'",Integer.class)).isZero());
        var waypoint=approveFixture("Waypoint.md","# Waypoint\n\n[[unapproved|Private archive link]] is a navigation waypoint.\n");
        publishFixtures(waypoint);
        var navigation=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"What is inside the private archive?",RagRetrievalMode.GENERIC,
                List.of(selection(waypoint,null,"Waypoint.md")),null,null,5));
        assertThat(navigation.matches()).allSatisfy(m->{assertThat(m.sourceId()).isEqualTo(waypoint.id());assertThat(m.excerpt()).doesNotContain("CORE_WHOLE_FILE_SENTINEL");});
    }

    @Test void transcludedContributorRequiresItsOwnEvidenceSelectionAndGroup() throws Exception {
        var grandchild=approveFixture("Grandchild.md","# Grandchild\n\nGRANDCHILD_ONLY_SENTINEL is a distant archive detail.\n");
        var child=approveFixture("Child.md","# Child\n\nCHILD_ONLY_SENTINEL is an amber archive detail.\n\n![[Grandchild]]\n");
        enabledRag.addDependency(child.id(),grandchild.id());child=enabledRag.findSource(child.id()).orElseThrow();
        var parent=approveFixture("Parent.md","# Parent\n\nPARENT_ONLY_SENTINEL is a cobalt archive detail.\n\n![[Child]]\n");
        enabledRag.addDependency(parent.id(),child.id());parent=enabledRag.findSource(parent.id()).orElseThrow();
        publishFixtures(parent,child,grandchild);
        var parentEntry=selection(parent,"IDEAVERSE_CORE","Parent.md");
        var childEntry=selection(child,"IDEAVERSE_NONCORE","Child.md");
        var canon=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"What is the archive detail?",RagRetrievalMode.IDEAVERSE_CANON,
                List.of(parentEntry),"roster-1","synthetic-collection",10));
        Long parentId=parent.id();
        assertThat(canon.matches()).isNotEmpty().allSatisfy(m->{assertThat(m.sourceId()).isEqualTo(parentId);assertThat(m.excerpt()).doesNotContain("CHILD_ONLY_SENTINEL");});
        var noncanon=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"What is the amber archive detail?",RagRetrievalMode.IDEAVERSE_NONCANON,
                List.of(parentEntry,childEntry),"roster-1","synthetic-collection",10));
        Long childId=child.id();
        assertThat(noncanon.matches()).anySatisfy(m->{assertThat(m.sourceId()).isEqualTo(childId);assertThat(m.group()).isEqualTo("IDEAVERSE_NONCORE");assertThat(m.excerpt()).contains("CHILD_ONLY_SENTINEL");});
        var childCore=selection(child,"IDEAVERSE_CORE","Child.md");
        var reverse=retrieveOverHttp(new RagRetrievalCommand(UUID.randomUUID(),"What is the amber detail?",RagRetrievalMode.IDEAVERSE_CANON,
                List.of(childCore),"roster-2","synthetic-collection",10));
        assertThat(reverse.matches()).isNotEmpty().allSatisfy(m->{assertThat(m.sourceId()).isEqualTo(childId);assertThat(m.excerpt()).doesNotContain("PARENT_ONLY_SENTINEL","GRANDCHILD_ONLY_SENTINEL");});
        var parentGeneration=ragIndexGenerationRepository.findBySourceId(parent.id()).stream().filter(g->g.isActive()).findFirst().orElseThrow();
        var proof=currentAuthority.prepare(parentGeneration);
        jdbcTemplate.update("UPDATE rag.external_file_sources SET source_version=source_version+1 WHERE file_source_id=?",UUID.fromString(grandchild.sourceId()));
        UUID reader=UUID.randomUUID();
        assertThatThrownBy(()->retrievalAuthority.admitPrepared(reader,UUID.randomUUID(),"stale-grandchild",Map.of(),List.of(),runtime.instance().toString(),List.of(proof)))
                .isInstanceOf(RuntimeException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.retrieval_readers WHERE reader_id=?",Integer.class,reader)).isZero();
    }

    private RagSourceView approveFixture(String name,String text) throws Exception {
        var path=producerInput.resolve(name);java.nio.file.Files.createDirectories(path.getParent());java.nio.file.Files.writeString(path,text,StandardCharsets.UTF_8);
        var candidate=enabledRag.registerSnapshotCandidate(path.toString());
        return enabledRag.approveSource(candidate.id(),new RagConsentCommand(Set.of("content"),candidate.sourceVersion(),candidate.consentResetVersion(),
                candidate.authorizationEpoch(),candidate.fieldPolicyVersion(),null,candidate.snapshot()));
    }
    private RagSourceSelectionEntry selection(RagSourceView original,String group,String path) {
        var current=enabledRag.findSource(original.id()).orElseThrow();
        return new RagSourceSelectionEntry(current.id(),current.snapshot().selectedRevisionId(),group,path);
    }
    private void publishFixtures(RagSourceView... views) {
        var roots=new ArrayList<RagDocumentRootItem>();var tasks=new ArrayList<RagDocumentBatchTask>();
        for(int i=0;i<views.length;i++) {
            var v=views[i];String key="fixture-"+i;
            roots.add(new RagDocumentRootItem(key,v.sourceType(),v.sourceId(),v.sourceVersion(),v.authorizationEpoch(),v.fieldPolicyVersion(),v.approvedFields()));
            tasks.add(new RagDocumentBatchTask(key,v.sourceId(),"MD",null,null,null,List.of("content"),Map.of()));
        }
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots,Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),tasks);
        String manifest=jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job.jobId());
        assertThat(enabledRag.finalizeDocumentBatch(job.jobId(),job.attemptId(),new RagDocumentBatchFinalizeCommand(
                com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest))).publishedCount()).isEqualTo(views.length);
    }

    @Test
    @DisplayName("Vector validation rejects invalid vectors (NaN, wrong dimension, zero norm)")
    void vectorValidationRejectsInvalidVectors() {
        UUID readerId = UUID.randomUUID();

        // 1. Wrong dimension
        ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> new float[128]);
        assertThatThrownBy(() -> ragQueryEmbeddingSupervisor.computeEmbedding(readerId, "test question"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("Invalid embedding vector dimensions");

        // 2. Zero vector
        ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> new float[384]);
        assertThatThrownBy(() -> ragQueryEmbeddingSupervisor.computeEmbedding(readerId, "test question"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("all zeros");

        // 3. NaN vector
        float[] nanVec = new float[384];
        nanVec[0] = Float.NaN;
        ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> nanVec);
        assertThatThrownBy(() -> ragQueryEmbeddingSupervisor.computeEmbedding(readerId, "test question"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("non-finite");
    }

    @Test
    @DisplayName("Negative: Missing or null reader ID is rejected")
    void missingOrNullReaderIdRejected() {
        assertThatThrownBy(() -> ragQueryEmbeddingSupervisor.computeEmbedding(null, "test question"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Worker termination cleanly terminates native process and is idempotent")
    void workerTerminationCleansProcessHandle() {
        UUID readerId = UUID.randomUUID();
        UUID file=UUID.randomUUID(), token=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.external_file_sources(file_source_id,canonical_identity_digest) VALUES(?,?)",file,"synthetic-native:"+file);
        retrievalAuthority.admit(readerId,token,"native-test",completeRetrievalFixtureScope(Map.of("external_file_ids",List.of(file.toString())),List.of(file)),
                List.of(file),runtime.instance().toString());
        var result = ragQueryEmbeddingSupervisor.computeEmbedding(readerId, "test question for process termination");
        assertThat(result.dimensions()).isEqualTo(384);
        System.out.println("S19_NATIVE_USAGE startup_nanos="+result.startupNanos()+" total_nanos="+result.totalNanos()+" usage="+result.resourceUsage());
        assertThat(jdbcTemplate.queryForObject("SELECT worker_creation_file_time FROM rag.retrieval_readers WHERE reader_id=?",Long.class,readerId)).isPositive();

        // Stop worker must terminate child process and succeed
        long stopStarted=System.nanoTime();
        ragQueryEmbeddingSupervisor.stopWorker(readerId);
        long stopNanos=System.nanoTime()-stopStarted;
        assertThat(stopNanos).isLessThan(java.time.Duration.ofSeconds(5).toNanos());
        System.out.println("S19_NATIVE_STOP nanos="+stopNanos+" (compute already positively stopped child and transport)");
        retrievalAuthority.finish(readerId,token,"CANCELLED","ABORTED",noSenderEvidence("NATIVE_TEST_JOINED"),null,null);
        ragQueryEmbeddingSupervisor.forgetStopped(readerId);

        // Second call must be idempotent without throwing
        ragQueryEmbeddingSupervisor.stopWorker(readerId);
    }

    @Test
    @DisplayName("PostgreSQL <=> vector distance ranks most similar chunk first")
    void vectorDistanceRankingSelectsClosestMatch() {
        float[] targetVector = new float[384];
        targetVector[0] = 1.0f;

        float[] closeVector = new float[384];
        closeVector[0] = 0.95f;
        closeVector[1] = 0.05f;

        float[] farVector = new float[384];
        farVector[1] = 1.0f;

        // Verify cosine distance in database using <=> operator
        List<Double> distances = jdbcTemplate.query(
                "SELECT (?::vector <=> ?::vector) AS dist_close, (?::vector <=> ?::vector) AS dist_far",
                (rs, rowNum) -> List.of(rs.getDouble("dist_close"), rs.getDouble("dist_far")),
                Arrays.toString(targetVector), Arrays.toString(closeVector),
                Arrays.toString(targetVector), Arrays.toString(farVector)
        ).get(0);

        assertThat(distances.get(0)).isLessThan(distances.get(1));
    }

    @Test
    @DisplayName("Negative: Blank question or unknown stop is rejected or idempotent")
    void blankQuestionRejectedAndUnknownStopIdempotent() {
        UUID readerId = UUID.randomUUID();
        ragQueryEmbeddingSupervisor.stopWorker(UUID.randomUUID());
        assertThatThrownBy(() -> ragQueryEmbeddingSupervisor.computeEmbedding(readerId, "   "))
                .isInstanceOf(IllegalArgumentException.class);
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
}
