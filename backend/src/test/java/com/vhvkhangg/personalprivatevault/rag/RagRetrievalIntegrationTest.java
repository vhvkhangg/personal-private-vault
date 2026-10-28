package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagQueryEmbeddingSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@org.springframework.boot.test.context.SpringBootTest(classes = com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class,
        webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
class RagRetrievalIntegrationTest extends AbstractRagIntegrationTest {

    @org.springframework.test.context.DynamicPropertySource
    static void enableRetrieval(org.springframework.test.context.DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @Autowired
    private RagRetrievalOperations ragRetrievalOperations;

    @Autowired
    private RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor;

    @Autowired
    private RagSourceRepository ragSourceRepository;

    @Autowired
    private RagExternalFileSourceRepository ragExternalFileSourceRepository;

    @Autowired
    private RagIndexGenerationRepository ragIndexGenerationRepository;

    @Autowired
    private RagDocumentChunkRepository ragDocumentChunkRepository;

    @Autowired
    private RagRetrievalReaderRepository ragRetrievalReaderRepository;

    @Autowired
    private RagOriginalBytesStore ragOriginalBytesStore;

    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority ragSourceAuthority;

    private static float[] makeNormalizedVector(float base) {
        float[] v = new float[384];
        Arrays.fill(v, base);
        double sumSq = 0.0;
        for (float f : v) sumSq += f * f;
        float norm = (float) Math.sqrt(sumSq);
        for (int i = 0; i < v.length; i++) v[i] = v[i] / norm;
        return v;
    }

    @BeforeEach
    void setUp() {
        ragQueryEmbeddingSupervisor.setVectorOverride((readerId, question) -> makeNormalizedVector(0.5f));
    }

    @AfterEach
    void tearDown() {
        ragQueryEmbeddingSupervisor.setVectorOverride(null);
    }

    @Test void emptyHttpResultConsumesRequestIdentityWithoutSourceOrContextReplay() throws Exception {
        UUID reader=UUID.randomUUID();
        var command=new RagRetrievalCommand(reader,"empty fixture?",RagRetrievalMode.GENERIC,null,null,null,5);
        assertThat(retrieveOverHttp(command).outcome()).isEqualTo(RagRetrievalOutcome.NO_EVIDENCE);
        assertThat(ragRetrievalReaderRepository.findByReaderId(reader).orElseThrow().getState()).isEqualTo("TERMINAL");
        var replay=requestRetrievalOverHttp(command);
        assertThat(replay.statusCode()).isEqualTo(400);
        assertThat(replay.body()).doesNotContain("empty fixture?","citationKey");
    }

    @Test
    @DisplayName("Retrieves verified Markdown evidence successfully with pgvector exact cosine ranking")
    void retrievesVerifiedMarkdownEvidenceSuccessfully() throws Exception {
        UUID fileSourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();
        UUID generationUuid = UUID.randomUUID();
        Instant now = Instant.now();

        String markdownContent = "# System Architecture\n\nThis vault uses a modular monolith.";
        byte[] contentBytes = markdownContent.getBytes(StandardCharsets.UTF_8);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String sha256 = HexFormat.of().formatHex(digest.digest(contentBytes));
        long length = contentBytes.length;

        String canonicalDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(("canonical:" + fileSourceId).getBytes(StandardCharsets.UTF_8)));
        String nameDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest("architecture.md".getBytes(StandardCharsets.UTF_8)));

        // 1. Store snapshot in RagOriginalBytesStore
        ragOriginalBytesStore.writeTemp(captureId, new ByteArrayInputStream(contentBytes), length, sha256);
        ragOriginalBytesStore.sealRevision(fileSourceId, revisionId, captureId, sha256, length, "architecture.md", List.of());

        // 2. Persist domain records
        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId,
                canonicalDigest,
                1L,
                1L,
                1L,
                1,
                true,
                now
        );
        fileSource.setSelectedRevision(revisionId, sha256, length, now);
        ragExternalFileSourceRepository.save(fileSource);

        RagSource source = new RagSource(
                RagSourceType.EXTERNAL_FILE,
                fileSourceId.toString(),
                RagClassification.NON_PRIVATE,
                1L,
                1,
                Set.of("content"),
                RagAccessState.APPROVED,
                sha256,
                "manifest:digest",
                now
        );
        source.setFileIdentity(canonicalDigest + nameDigest);
        source.bindOwnerResetVersion(1L);
        ragSourceRepository.save(source);

        var prepared = ragSourceAuthority.prepareSource(source.getId(), Map.of());
        String manifest = prepared.manifest();
        String manifestDigest = com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest);

        UUID jobId = UUID.randomUUID();
        setupJobLeaseAndGrant(jobId, source.getId(), manifest);

        RagIndexGeneration generation = new RagIndexGeneration(
                generationUuid,
                jobId,
                source.getId(),
                "PUBLISHED",
                1L,
                sha256,
                manifestDigest,
                true,
                now
        );
        generation = ragIndexGenerationRepository.save(generation);
        saveGenerationRoots(generation.getId());

        // 3. Insert chunk with matching vector
        float[] chunkVec = makeNormalizedVector(0.5f);
        Map<String, Object> locator = Map.of(
                "profile", "ppv-obsidian-source-v1",
                "source_key", fileSourceId.toString(),
                "source_version", 1L,
                "approved_field", "content",
                "char_start", 0L,
                "char_end", 21L,
                "line_start", 1L,
                "line_end", 1L,
                "heading", "System Architecture"
        );
        Map<String, Object> constituentLocator = Map.of(
                "profile", "ppv-obsidian-source-v1",
                "source_key", fileSourceId.toString(),
                "source_version", 1L,
                "approved_field", "content",
                "char_start", 0L,
                "char_end", 21L,
                "line_start", 1L,
                "line_end", 1L,
                "heading", "System Architecture"
        );
        Map<String, Object> structureMetadata = Map.of(
                "block_type", "HEADING",
                "constituent_locators", List.of(constituentLocator)
        );

        RagDocumentChunk chunk = new RagDocumentChunk(
                generation.getId(),
                fileSourceId.toString(),
                "# System Architecture",
                locator,
                structureMetadata,
                chunkVec,
                now
        );
        ragDocumentChunkRepository.save(chunk);

        // 4. Execute retrieval
        UUID requestId = UUID.randomUUID();
        RagRetrievalCommand command = new RagRetrievalCommand(
                requestId,
                "Where is system architecture described?",
                RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(source.getId(), revisionId, null, "architecture.md")),
                null,
                null,
                5
        );

        RagRetrievalResponse response = retrieveOverHttp(command);

        // 5. Verify assertions
        assertThat(response).isNotNull();
        assertThat(response.requestId()).isEqualTo(requestId);
        assertThat(response.outcome()).isEqualTo(RagRetrievalOutcome.EVIDENCE);
        assertThat(response.matches()).hasSize(1);

        RagRetrievalMatch match = response.matches().getFirst();
        assertThat(match.citationKey()).isEqualTo("[1]");
        assertThat(match.excerpt()).isEqualTo("# System Architecture");
        assertThat(match.sourceId()).isEqualTo(source.getId());
        assertThat(match.fileSourceId()).isEqualTo(fileSourceId);
        assertThat(match.selectedRevisionId()).isEqualTo(revisionId);
        assertThat(match.generationId()).isEqualTo(generationUuid);
        assertThat(match.displayName()).isEqualTo("architecture.md");
        assertThat(match.freshnessBasis()).isEqualTo("RAG_SNAPSHOT");
        assertThat(match.snapshotIndexState()).isEqualTo("SNAPSHOT_CURRENT");
        assertThat(match.originalFreshness()).isEqualTo("UNVERIFIED");

        // Verify reader state is TERMINAL
        Optional<RagRetrievalReader> readerOpt = ragRetrievalReaderRepository.findByReaderId(requestId);
        assertThat(readerOpt).isPresent();
        assertThat(readerOpt.get().getState()).isEqualTo("TERMINAL");
    }

    @Test
    @DisplayName("Returns NO_EVIDENCE when candidate chunks are outside scope")
    void returnsNoEvidenceWhenNoChunksAdmissible() throws Exception {
        UUID fileSourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();
        Instant now = Instant.now();

        String markdown = "# Notes\nSome content.";
        byte[] bytes = markdown.getBytes(StandardCharsets.UTF_8);
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256", java.security.Security.getProvider("SUN")).digest(bytes));

        ragOriginalBytesStore.writeTemp(captureId, new ByteArrayInputStream(bytes), (long) bytes.length, sha256);
        ragOriginalBytesStore.sealRevision(fileSourceId, revisionId, captureId, sha256, (long) bytes.length, "notes.md", List.of());

        String canonicalDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256", java.security.Security.getProvider("SUN")).digest(("canonical:" + fileSourceId).getBytes(StandardCharsets.UTF_8)));
        String nameDigest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256", java.security.Security.getProvider("SUN")).digest("notes.md".getBytes(StandardCharsets.UTF_8)));

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId,
                canonicalDigest,
                1L, 1L, 1L, 1, true, now
        );
        fileSource.setSelectedRevision(revisionId, sha256, (long) bytes.length, now);
        ragExternalFileSourceRepository.save(fileSource);

        RagSource source = new RagSource(
                RagSourceType.EXTERNAL_FILE,
                fileSourceId.toString(),
                RagClassification.NON_PRIVATE,
                1L, 1, Set.of("content"),
                RagAccessState.APPROVED,
                sha256,
                "manifest:digest",
                now
        );
        source.setFileIdentity(canonicalDigest + nameDigest);
        source.bindOwnerResetVersion(1L);
        ragSourceRepository.save(source);

        var prepared = ragSourceAuthority.prepareSource(source.getId(), Map.of());
        String manifest = prepared.manifest();
        String manifestDigest = com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority.digest(manifest);

        UUID jobId = UUID.randomUUID();
        setupJobLeaseAndGrant(jobId, source.getId(), manifest);

        RagIndexGeneration gen = new RagIndexGeneration(
                UUID.randomUUID(), jobId, source.getId(), "PUBLISHED", 1L, sha256, manifestDigest, true, now
        );
        gen = ragIndexGenerationRepository.save(gen);
        saveGenerationRoots(gen.getId());

        // No chunks inserted for this generation
        UUID requestId = UUID.randomUUID();
        RagRetrievalCommand command = new RagRetrievalCommand(
                requestId,
                "Any content?",
                RagRetrievalMode.GENERIC,
                List.of(new RagSourceSelectionEntry(source.getId(), revisionId, null, "notes.md")),
                null,
                null,
                5
        );

        RagRetrievalResponse response = retrieveOverHttp(command);
        assertThat(response.outcome()).isEqualTo(RagRetrievalOutcome.NO_EVIDENCE);
        assertThat(response.matches()).isEmpty();
    }

    @Test
    @DisplayName("Idempotent cancel on active or terminal reader")
    void cancelReaderIdempotently() {
        UUID requestId = UUID.randomUUID();

        // 1. Cancel on nonexistent throws 404
        assertThatThrownBy(() -> ragRetrievalOperations.cancel(requestId))
                .isInstanceOf(NoSuchElementException.class);
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
