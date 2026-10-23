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

@Timeout(120)
class RagDocumentBatchIntegrationTest extends AbstractEnabledRagIntegrationTest {

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

    private void prepareProvenStopAndGrant(UUID jobId, UUID attemptId) {
        var tasks=jdbcTemplate.queryForList("SELECT b.root_key,s.source_id,s.approved_fields_text FROM rag.batch_roots b JOIN rag.sources s ON s.id=b.source_id WHERE b.job_id=? ORDER BY b.root_key",jobId)
                .stream().map(row -> new RagDocumentBatchTask((String)row.get("root_key"),(String)row.get("source_id"),"DOMAIN",null,null,null,
                    Arrays.asList(((String)row.get("approved_fields_text")).split(",")),Map.of())).toList();
        enabledRag.executeDocumentBatch(jobId,attemptId,tasks);
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


        src1 = rag.approveSource(src1.id(), consent(src1, Set.of("title", "description")));
        src2 = rag.approveSource(src2.id(), consent(src2, Set.of("title")));
        jdbcTemplate.update("UPDATE fictions SET description='' WHERE id=?",Long.valueOf(src3.sourceId()));
        src3=rag.findSource(src3.id()).orElseThrow();
        src3 = rag.approveSource(src3.id(), consent(src3, Set.of("description")));

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

        // Real DOMAIN input: title+description, title, and empty description.






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
        assertThat(o2.warningCodes()).isEmpty();

        var o3 = outcomes.stream().filter(o -> "root-3".equals(o.rootKey())).findFirst().orElseThrow();
        assertThat(o3.result()).isEqualTo("FAILED");
        assertThat(o3.errorCode()).isEqualTo("EMPTY_CONTENT");

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


        jdbcTemplate.update("UPDATE fictions SET description='' WHERE id=?",Long.valueOf(src1.sourceId()));
        src1=rag.findSource(src1.id()).orElseThrow();
        src1 = rag.approveSource(src1.id(), consent(src1, Set.of("description")));
        jdbcTemplate.update("UPDATE fictions SET description='' WHERE id=?",Long.valueOf(src2.sourceId()));
        src2=rag.findSource(src2.id()).orElseThrow();
        src2 = rag.approveSource(src2.id(), consent(src2, Set.of("description")));

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

        // The actual producer returns two EMPTY_CONTENT failures.



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
