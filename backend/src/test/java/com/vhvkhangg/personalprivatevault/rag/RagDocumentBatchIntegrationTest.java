package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagStagedDocumentOutput;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagDocumentChunkRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagIndexGenerationRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagStagedDocumentOutputRepository;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Timeout(40)
class RagDocumentBatchIntegrationTest extends AbstractRagIntegrationTest {

    @Autowired
    private RagOperations rag;

    @Autowired
    private RagStagedDocumentOutputRepository stagedRepo;

    @Autowired
    private RagIndexGenerationRepository indexGenerations;

    @Autowired
    private RagDocumentChunkRepository documentChunks;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor supervisor;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagPurgeService purgeService;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService recovery;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority sourceAuthority;

    private static Map<String, Object> resources(Map<String, Object> measured) {
        var result = new HashMap<String, Object>(measured);
        result.put("pipeline_identity", Map.ofEntries(
                Map.entry("model_name", "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"),
                Map.entry("model_version", "2.0.0"),
                Map.entry("artifact_hash", "98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f"),
                Map.entry("dimensions", 384), Map.entry("tokenizer_version", "hf-tokenizers-0.23.3"),
                Map.entry("tokenizer_artifact_hash", "2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8"),
                Map.entry("parser_version", "ppv-parser-v18.1-pdfplumber-0.11.10"), Map.entry("chunker_version", "structure-first-v18.1"),
                Map.entry("ocr_pipeline_version", "rapidocr-1.4.4"), Map.entry("provider", "CPU")));
        return result;
    }

    private static final List<Float> TEST_EMBEDDING = Collections.nCopies(384, 0.05f);
    private static final String VALID_DIGEST = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private void prepareProvenStopAndGrant(UUID jobId, UUID attemptId) {
        var prepared = sourceAuthority.prepareBatch(jobId, attemptId);
        String input = com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(
                com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentInput.encode(
                        sourceAuthority.documentInput(jobId, attemptId, prepared)));
        String scope = finalizeCommand(jobId).dependencyDigest();
        String output = com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest("DOCUMENT_V3:" + input);
        supervisor.stopAndVerify(jobId);
        jdbcTemplate.update("""
                INSERT INTO rag.worker_grants(job_id, attempt_id, session_id, session_until, input_digest, scope_digest, output_digest, stage_type, consumed_at)
                VALUES (?, ?, gen_random_uuid(), clock_timestamp() + interval '1 hour', ?, ?, ?, 'DOCUMENT_V3', clock_timestamp())
                ON CONFLICT (job_id, attempt_id) DO NOTHING
                """, jobId, attemptId, input, scope, output);
    }

    private RagDocumentBatchFinalizeCommand finalizeCommand(UUID jobId) {
        String manifest = jdbcTemplate.queryForObject(
                "SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?", String.class, jobId);
        return new RagDocumentBatchFinalizeCommand(
                com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest));
    }

    private RagSourceView createFictionCandidate() {
        Long genre = jdbcTemplate.queryForObject(
                "INSERT INTO fiction_genres(name) VALUES (?) RETURNING id",
                Long.class, UUID.randomUUID().toString()
        );
        Long person = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id",
                Long.class
        );
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Batch Author')", person);
        Long id = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id",
                Long.class
        );
        jdbcTemplate.update(
                "INSERT INTO fictions(id,title,format,genre_id,author_person_id,description,is_nsfw) VALUES (?,'Batch Fiction','NOVEL',?,?,'Fiction description',false)",
                id, genre, person
        );
        return rag.registerDomainCandidate(RagSourceType.FICTION, id.toString(), RagClassification.NON_PRIVATE, null, null);
    }

    private static RagConsentCommand consent(RagSourceView view, Set<String> fields) {
        return new RagConsentCommand(
                fields, view.sourceVersion(), view.consentResetVersion(),
                view.authorizationEpoch(), view.fieldPolicyVersion(),
                fields.contains("description") ? view.currentDescriptionRevisionHash() : null
        );
    }

    @Test
    void testExternalFileDefaultContainmentDisabled() {
        assertThatThrownBy(() -> rag.registerFileCandidate("C:/vault/docs/sample.pdf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Live external-file capability is disabled");
    }

    @Test
    void testMultiRootBatchAdmissionAndAtomicUnionReservation() {
        RagSourceView src1 = createFictionCandidate();
        RagSourceView src2 = createFictionCandidate();


        src1 = rag.approveSource(src1.id(), consent(src1, Set.of("title", "description")));
        src2 = rag.approveSource(src2.id(), consent(src2, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-1", src1.sourceType(), src1.sourceId(),
                        src1.sourceVersion(), src1.authorizationEpoch(), src1.fieldPolicyVersion(), src1.approvedFields()),
                new RagDocumentRootItem("root-2", src2.sourceType(), src2.sourceId(),
                        src2.sourceVersion(), src2.authorizationEpoch(), src2.fieldPolicyVersion(), src2.approvedFields())
        );

        RagJobAdmissionResult admission = rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of()));
        assertThat(admission).isNotNull();
        assertThat(admission.jobId()).isNotNull();

        // Verify Vault reservations are locked for both sources
        Integer vaultReserved = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",
                Integer.class
        );
        assertThat(vaultReserved).isEqualTo(2);

        // Attempt second concurrent admission while active -> rejected
        assertThatThrownBy(() -> rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("An active RAG attempt is already in flight");

        // Cleanup by cancelling
        rag.cancelJob(admission.jobId());
    }

    @Test
    void batchReservesCompleteChildUnionAndRequiresExactCas() {
        var parent = createFictionCandidate();
        var child = createFictionCandidate();
        var sibling = createFictionCandidate();
        parent = rag.approveSource(parent.id(), consent(parent, Set.of("title")));
        child = rag.approveSource(child.id(), consent(child, Set.of("title")));
        sibling = rag.approveSource(sibling.id(), consent(sibling, Set.of("title")));
        rag.addDependency(parent.id(), child.id());
        parent = rag.findSource(parent.id()).orElseThrow();
        var stale = new RagDocumentRootItem("parent", parent.sourceType(), parent.sourceId(), null,
                parent.authorizationEpoch(), parent.fieldPolicyVersion(), parent.approvedFields());
        assertThatThrownBy(() -> rag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(stale), Map.of())))
                .isInstanceOf(RagSourceChangedException.class);
        var admitted = rag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(
                new RagDocumentRootItem("parent", parent.sourceType(), parent.sourceId(), parent.sourceVersion(),
                        parent.authorizationEpoch(), parent.fieldPolicyVersion(), parent.approvedFields()),
                new RagDocumentRootItem("sibling", sibling.sourceType(), sibling.sourceId(), sibling.sourceVersion(),
                        sibling.authorizationEpoch(), sibling.fieldPolicyVersion(), sibling.approvedFields())
        ), Map.of()));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by=?",
                Integer.class, admitted.attemptId())).isEqualTo(3);
        rag.cancelJob(admitted.jobId());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",
                Integer.class)).isZero();
    }

    @Test
    void testMixedSuccessBatchFinalizationOnePhysicalCommit() {
        RagSourceView src1 = createFictionCandidate();
        RagSourceView src2 = createFictionCandidate();
        RagSourceView src3 = createFictionCandidate();


        src1 = rag.approveSource(src1.id(), consent(src1, Set.of("title")));
        src2 = rag.approveSource(src2.id(), consent(src2, Set.of("title")));
        src3 = rag.approveSource(src3.id(), consent(src3, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-1", src1.sourceType(), src1.sourceId(),
                        src1.sourceVersion(), src1.authorizationEpoch(), src1.fieldPolicyVersion(), src1.approvedFields()),
                new RagDocumentRootItem("root-2", src2.sourceType(), src2.sourceId(),
                        src2.sourceVersion(), src2.authorizationEpoch(), src2.fieldPolicyVersion(), src2.approvedFields()),
                new RagDocumentRootItem("root-3", src3.sourceType(), src3.sourceId(),
                        src3.sourceVersion(), src3.authorizationEpoch(), src3.fieldPolicyVersion(), src3.approvedFields())
        );

        RagJobAdmissionResult admission = rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of()));
        UUID jobId = admission.jobId();
        UUID attemptId = admission.attemptId();

        // Prepare proven supervisor stop and grant before staging outputs
        prepareProvenStopAndGrant(jobId, attemptId);

        // Stage outputs directly: root-1 SUCCESS (2 chunks), root-2 SUCCESS (1 chunk), root-3 FAILED (0 chunks)
        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-1", true, "SUCCESS", List.of(),
                List.of(
                        Map.of("source_identity", "root-1", "content", "Paragraph 1", "locator", Map.of("line_start", 1, "line_end", 1), "embedding", TEST_EMBEDDING),
                        Map.of("source_identity", "root-1", "content", "Paragraph 2", "locator", Map.of("line_start", 5, "line_end", 5), "embedding", TEST_EMBEDDING)
                ),
                resources(Map.of("peak_system_ram_bytes", 50000000L, "peak_gpu_vram_bytes", 10000000L)),
                Instant.now()
        ));

        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-2", true, "SUCCESS", List.of("NON_MATERIAL_WARNING"),
                List.of(
                        Map.of("source_identity", "root-2", "content", "Heading content", "locator", Map.of("line_start", 1, "line_end", 1), "embedding", TEST_EMBEDDING)
                ),
                resources(Map.of("peak_system_ram_bytes", 52000000L, "peak_gpu_vram_bytes", 11000000L)),
                Instant.now()
        ));

        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-3", false, "CORRUPT_FILE", List.of("Invalid header"),
                List.of(),
                Map.of("peak_system_ram_bytes", 40000000L, "peak_gpu_vram_bytes", 0L),
                Instant.now()
        ));

        // Finalize batch
        RagBatchFinalizationResult finResult = rag.finalizeDocumentBatch(
                jobId, attemptId, finalizeCommand(jobId)
        );

        assertThat(finResult).isNotNull();
        assertThat(finResult.publishedCount()).isEqualTo(2);
        assertThat(finResult.failedCount()).isEqualTo(1);
        assertThat(finResult.jobStatus()).isEqualTo("COMPLETED");
        assertThat(finResult.publishedGenerations()).containsKeys("root-1", "root-2");
        assertThat(finResult.publishedGenerations()).doesNotContainKey("root-3");

        // Verify published chunks
        var chunks = documentChunks.findAll();
        assertThat(chunks).hasSize(3);

        // Verify report
        RagDocumentBatchReport report = rag.getDocumentBatchReport(jobId);
        assertThat(report).isNotNull();
        assertThat(report.jobId()).isEqualTo(jobId);
        assertThat(report.status()).isEqualTo("COMPLETED");
        assertThat(report.summaryOutcome()).isEqualTo("PARTIAL_SUCCESS");
        assertThat(report.outcomes()).hasSize(3);

        // Check individual outcome views
        var outcomes = report.outcomes();
        var o1 = outcomes.stream().filter(o -> "root-1".equals(o.rootKey())).findFirst().orElseThrow();
        assertThat(o1.result()).isEqualTo("SUCCESS");
        assertThat(o1.chunkCount()).isEqualTo(2);
        assertThat(o1.currentIndexState()).isEqualTo("CURRENT");

        var o2 = outcomes.stream().filter(o -> "root-2".equals(o.rootKey())).findFirst().orElseThrow();
        assertThat(o2.result()).isEqualTo("SUCCESS");
        assertThat(o2.chunkCount()).isEqualTo(1);
        assertThat(o2.warningCodes()).contains("NON_MATERIAL_WARNING");

        var o3 = outcomes.stream().filter(o -> "root-3".equals(o.rootKey())).findFirst().orElseThrow();
        assertThat(o3.result()).isEqualTo("FAILED");
        assertThat(o3.errorCode()).isEqualTo("CORRUPT_FILE");

        // Verify entire reservation union is released
        Integer vaultReserved = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",
                Integer.class
        );
        assertThat(vaultReserved).isZero();

        // Multiple reconciliation passes must never reclassify erased B as v2
        // and invalidate unrelated A. These are fixture-produced generations,
        // not proof of a native document producer.
        rag.revokeSource(src2.id());
        for (int pass = 0; pass < 3; pass++) recovery.reconcile();
        var fresh = rag.getDocumentBatchReport(jobId);
        assertThat(fresh.outcomes().stream().filter(o -> "root-1".equals(o.rootKey()))
                .findFirst().orElseThrow().currentIndexState()).isEqualTo("CURRENT");
        Long revokedId = jdbcTemplate.queryForObject(
                "SELECT id FROM rag.index_generations WHERE generation_id=?", Long.class,
                finResult.publishedGenerations().get("root-2"));
        assertThat(documentChunks.countByGenerationId(revokedId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=? AND root_key='root-2'",
                Integer.class, jobId)).isZero();
        assertThat(documentChunks.findAll()).hasSize(2);
    }

    @Test
    void testAllFailBatchFinalizationAborts() {
        RagSourceView src1 = createFictionCandidate();
        RagSourceView src2 = createFictionCandidate();


        src1 = rag.approveSource(src1.id(), consent(src1, Set.of("title")));
        src2 = rag.approveSource(src2.id(), consent(src2, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-1", src1.sourceType(), src1.sourceId(),
                        src1.sourceVersion(), src1.authorizationEpoch(), src1.fieldPolicyVersion(), src1.approvedFields()),
                new RagDocumentRootItem("root-2", src2.sourceType(), src2.sourceId(),
                        src2.sourceVersion(), src2.authorizationEpoch(), src2.fieldPolicyVersion(), src2.approvedFields())
        );

        RagJobAdmissionResult admission = rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of()));
        UUID jobId = admission.jobId();
        UUID attemptId = admission.attemptId();

        prepareProvenStopAndGrant(jobId, attemptId);

        // Stage both as FAILED
        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-1", false, "PARSE_ERROR", List.of("Parse failed"),
                List.of(), Map.of(), Instant.now()
        ));
        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-2", false, "OCR_FAILED", List.of("OCR failed"),
                List.of(), Map.of(), Instant.now()
        ));

        RagBatchFinalizationResult finResult = rag.finalizeDocumentBatch(
                jobId, attemptId, finalizeCommand(jobId)
        );

        assertThat(finResult.publishedCount()).isZero();
        assertThat(finResult.failedCount()).isEqualTo(2);
        assertThat(finResult.jobStatus()).isEqualTo("ABORTED");

        RagDocumentBatchReport report = rag.getDocumentBatchReport(jobId);
        assertThat(report.summaryOutcome()).isEqualTo("ABORTED");
        assertThat(report.status()).isEqualTo("ABORTED");

        // Verify 0 generations published
        assertThat(indexGenerations.findAll()).isEmpty();

        // Verify reservations are released
        Integer vaultReserved = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",
                Integer.class
        );
        assertThat(vaultReserved).isZero();
    }

    @Test
    void testIndependentPerFileGenerationLifetime() {
        RagSourceView src1 = createFictionCandidate();
        RagSourceView src2 = createFictionCandidate();

        src1 = rag.approveSource(src1.id(), consent(src1, Set.of("title")));
        src2 = rag.approveSource(src2.id(), consent(src2, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-A", src1.sourceType(), src1.sourceId(),
                        src1.sourceVersion(), src1.authorizationEpoch(), src1.fieldPolicyVersion(), src1.approvedFields()),
                new RagDocumentRootItem("root-B", src2.sourceType(), src2.sourceId(),
                        src2.sourceVersion(), src2.authorizationEpoch(), src2.fieldPolicyVersion(), src2.approvedFields())
        );

        RagJobAdmissionResult admission = rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of()));
        UUID jobId = admission.jobId();
        UUID attemptId = admission.attemptId();

        prepareProvenStopAndGrant(jobId, attemptId);

        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-A", true, "SUCCESS", List.of(),
                List.of(Map.of("source_identity", "root-A", "content", "A content", "locator", Map.of("line_start", 1, "line_end", 1), "embedding", TEST_EMBEDDING)),
                resources(Map.of()), Instant.now()
        ));
        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-B", true, "SUCCESS", List.of(),
                List.of(Map.of("source_identity", "root-B", "content", "B content", "locator", Map.of("line_start", 1, "line_end", 1), "embedding", TEST_EMBEDDING)),
                resources(Map.of()), Instant.now()
        ));

        rag.finalizeDocumentBatch(jobId, attemptId, finalizeCommand(jobId));

        // Both are CURRENT initially
        RagDocumentBatchReport reportInitial = rag.getDocumentBatchReport(jobId);
        assertThat(reportInitial.outcomes()).allMatch(o -> "CURRENT".equals(o.currentIndexState()));

        // Now revoke consent on root-B only!
        rag.revokeSource(src2.id());

        // Dynamic query-time report: root-A remains CURRENT, root-B is REVOKED
        RagDocumentBatchReport reportAfter = rag.getDocumentBatchReport(jobId);
        var oA = reportAfter.outcomes().stream().filter(o -> "root-A".equals(o.rootKey())).findFirst().orElseThrow();
        assertThat(oA.currentIndexState()).isEqualTo("CURRENT");

        var oB = reportAfter.outcomes().stream().filter(o -> "root-B".equals(o.rootKey())).findFirst().orElseThrow();
        assertThat(oB.currentIndexState()).isEqualTo("REVOKED");
    }

    @Test
    void testInvalidExternalFileUuidRejected() {
        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-ext", RagSourceType.EXTERNAL_FILE, "not-a-valid-uuid",
                        1L, 1L, 1, Set.of("content"))
        );

        assertThatThrownBy(() -> rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid external file UUID");
    }

    @Test
    void testDuplicateCanonicalSourceDenied() {
        RagSourceView src = createFictionCandidate();
        src = rag.approveSource(src.id(), consent(src, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-1", src.sourceType(), src.sourceId(),
                        src.sourceVersion(), src.authorizationEpoch(), src.fieldPolicyVersion(), src.approvedFields()),
                new RagDocumentRootItem("root-2", src.sourceType(), src.sourceId(),
                        src.sourceVersion(), src.authorizationEpoch(), src.fieldPolicyVersion(), src.approvedFields())
        );

        assertThatThrownBy(() -> rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate source admitted under different root key");
    }

    @Test
    void testUnapprovedFieldRejected() {
        RagSourceView src = createFictionCandidate();
        src = rag.approveSource(src.id(), consent(src, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-1", src.sourceType(), src.sourceId(),
                        src.sourceVersion(), src.authorizationEpoch(), src.fieldPolicyVersion(), Set.of("title", "unapproved_field"))
        );

        assertThatThrownBy(() -> rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Source or consent changed");
    }

    @Test
    void testPurgeErasesDocumentChunksAndGenerationRoots() {
        RagSourceView src = createFictionCandidate();
        src = rag.approveSource(src.id(), consent(src, Set.of("title")));

        List<RagDocumentRootItem> items = List.of(
                new RagDocumentRootItem("root-1", src.sourceType(), src.sourceId(),
                        src.sourceVersion(), src.authorizationEpoch(), src.fieldPolicyVersion(), src.approvedFields())
        );

        RagJobAdmissionResult admission = rag.admitDocumentBatch(new RagDocumentBatchCommand(items, Map.of()));
        UUID jobId = admission.jobId();
        UUID attemptId = admission.attemptId();

        prepareProvenStopAndGrant(jobId, attemptId);

        stagedRepo.save(new RagStagedDocumentOutput(
                jobId, attemptId, "root-1", true, "SUCCESS", List.of(),
                List.of(Map.of("source_identity", "root-1", "content", "Purge candidate", "locator", Map.of("line_start", 1, "line_end", 1), "embedding", TEST_EMBEDDING)),
                resources(Map.of()), Instant.now()
        ));

        RagBatchFinalizationResult fin = rag.finalizeDocumentBatch(jobId, attemptId, finalizeCommand(jobId));
        UUID genUuid = fin.publishedGenerations().get("root-1");
        Long genId = jdbcTemplate.queryForObject("SELECT id FROM rag.index_generations WHERE generation_id = ?", Long.class, genUuid);

        // Verify rows exist before purge
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks WHERE generation_id = ?", Integer.class, genId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.generation_roots WHERE generation_id = ?", Integer.class, genId)).isEqualTo(1);

        // Revoke source to make generation eligible for purge
        rag.revokeSource(src.id());

        // Invalidate generation and perform purge via retryCleanup
        purgeService.invalidateGeneration(genUuid, "REVOKED");
        boolean cleaned = purgeService.retryCleanup(jobId);
        assertThat(cleaned).isTrue();

        // Verify document_chunks and generation_roots are completely deleted!
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks WHERE generation_id = ?", Integer.class, genId)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.generation_roots WHERE generation_id = ?", Integer.class, genId)).isZero();
    }
}
