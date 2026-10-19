package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.api.RagBatchFinalizationResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchFinalizeCommand;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.*;

/**
 * Trusted Java-owned atomic physical finalization for multi-root document batches (ADR-0021 Clause 6).
 * After positive worker stop and writer drain/seal:
 * Publishes ALL verified successful root generations or NONE, records outcomes, and releases
 * the exact entire reserved union in ONE physical COMMIT.
 */
@Service
public class RagDocumentFinalizationGate {

    private static final Logger log = LoggerFactory.getLogger(RagDocumentFinalizationGate.class);
    private static final Map<String, Object> PIPELINE = Map.ofEntries(
            Map.entry("model_name", "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"),
            Map.entry("model_version", "2.0.0"),
            Map.entry("artifact_hash", "98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f"),
            Map.entry("dimensions", 384), Map.entry("tokenizer_version", "hf-tokenizers-0.23.3"),
            Map.entry("tokenizer_artifact_hash", "2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8"),
            Map.entry("parser_version", "ppv-parser-v18.1-pdfplumber-0.11.10"), Map.entry("chunker_version", "structure-first-v18.1"),
            Map.entry("ocr_pipeline_version", "rapidocr-1.4.4"));

    private final RagSourceAuthority authority;
    private final RagJobLeaseRepository leases;
    private final RagBatchRootRepository batchRoots;
    private final RagFileOutcomeRepository fileOutcomes;
    private final RagStagedDocumentOutputRepository stagedOutputs;
    private final RagIndexGenerationRepository indexGenerations;
    private final RagGenerationRootRepository generationRoots;
    private final RagDocumentChunkRepository documentChunks;
    private final RagSourceRepository sources;
    private final RagRunRepository runs;
    private final SourceIndexReservations vaultReservations;
    private final RagExternalFileAuthority externalFileAuthority;
    private final RagWorkerSupervisor supervisor;
    private final RagPurgeService purge;
    private final EntityManager entityManager;
    private final TransactionTemplate transaction;

    public RagDocumentFinalizationGate(
            RagSourceAuthority authority,
            RagJobLeaseRepository leases,
            RagBatchRootRepository batchRoots,
            RagFileOutcomeRepository fileOutcomes,
            RagStagedDocumentOutputRepository stagedOutputs,
            RagIndexGenerationRepository indexGenerations,
            RagGenerationRootRepository generationRoots,
            RagDocumentChunkRepository documentChunks,
            RagSourceRepository sources,
            RagRunRepository runs,
            SourceIndexReservations vaultReservations,
            RagExternalFileAuthority externalFileAuthority,
            RagWorkerSupervisor supervisor,
            RagPurgeService purge,
            EntityManager entityManager,
            PlatformTransactionManager transactions
    ) {
        this.authority = authority;
        this.leases = leases;
        this.batchRoots = batchRoots;
        this.fileOutcomes = fileOutcomes;
        this.stagedOutputs = stagedOutputs;
        this.indexGenerations = indexGenerations;
        this.generationRoots = generationRoots;
        this.documentChunks = documentChunks;
        this.sources = sources;
        this.runs = runs;
        this.vaultReservations = vaultReservations;
        this.externalFileAuthority = externalFileAuthority;
        this.supervisor = supervisor;
        this.purge = purge;
        this.entityManager = entityManager;
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setTimeout(15);
    }

    public RagBatchFinalizationResult finalizeDocumentBatch(
            UUID jobId,
            UUID attemptId,
            RagDocumentBatchFinalizeCommand command
    ) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Batch finish must enter outside an ambient transaction");
        }
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(attemptId, "attemptId must not be null");

        // 1. Initial lease verification
        var initial = leases.findByJobIdAndAttemptId(jobId, attemptId)
                .orElseThrow(() -> new NoSuchElementException("Job attempt lease not found: " + attemptId));

        if ("COMPLETED".equals(initial.getStatus()) || "ABORTED".equals(initial.getStatus())) {
            // Already finalized; return durable outcomes
            var existingOutcomes = fileOutcomes.findByJobId(jobId);
            Map<String, UUID> published = new LinkedHashMap<>();
            int pubCount = 0;
            int failCount = 0;
            for (var fo : existingOutcomes) {
                if ("SUCCESS".equals(fo.getResult()) && fo.getCandidateGenerationId() != null) {
                    published.put(fo.getRootKey(), fo.getCandidateGenerationId());
                    pubCount++;
                } else {
                    failCount++;
                }
            }
            return new RagBatchFinalizationResult(jobId, initial.getStatus(), pubCount, failCount, published);
        }

        Objects.requireNonNull(command, "command must not be null");
        if (!Objects.equals(command.dependencyDigest(), RagSourceAuthority.digest(initial.getDependencyManifest())))
            throw new IllegalArgumentException("Document scope digest mismatch");
        var prepared = authority.prepareBatch(jobId, attemptId);
        String expectedInput = RagSourceAuthority.digest(
                RagDocumentInput.encode(authority.documentInput(jobId, attemptId, prepared)));
        String expectedScope = RagSourceAuthority.digest(initial.getDependencyManifest());
        String expectedOutput = RagSourceAuthority.digest("DOCUMENT_V3:" + expectedInput);
        var generationDigests = new HashMap<Long, String>();
        prepared.forEach((id, proof) -> generationDigests.put(id, RagSourceAuthority.digest(proof.manifest())));

        // 2. Positive supervisor verification of stopped worker
        if (!supervisor.isSupervised(jobId)) {
            throw new IllegalStateException("Consumer termination is not proven: job is not supervised: " + jobId);
        }
        if (!supervisor.proveStopped(jobId) && !supervisor.stopAndVerify(jobId)) {
            throw new IllegalStateException("Consumer termination is not proven for job: " + jobId);
        }

        RagBatchFinalizationResult result;
        try {
            result = transaction.execute(status -> {
                Instant now = Instant.now();

                var guarded = authority.lockPreparedBatchAttempt(jobId, attemptId, prepared);
                var lease = guarded.lease();
                List<RagBatchRoot> roots = batchRoots.findByJobId(jobId);
                var issuedScope = RagManifestScope.scope(lease.getDependencyManifest());
                var vaultOwnerIds = issuedScope.vaultOwners();
                var externalFileIds = issuedScope.externalFiles();
                var policyMap = new HashMap<Long, RagSource>();
                sources.findAllById(prepared.keySet()).forEach(source -> policyMap.put(source.getId(), source));

                if (lease.isStagingClosed()) {
                    throw new IllegalStateException("Staging is already closed");
                }
                lease.sealStaging();

                if (lease.getLeasedUntil().isBefore(now)) {
                    throw new IllegalStateException("Execution expired before finalization commit");
                }

                // Verify supervisor one more time under lock
                if (!supervisor.isSupervised(jobId) || !supervisor.proveStopped(jobId)) {
                    throw new IllegalStateException("Consumer termination is not proven");
                }

                // Require genuine consumed DOCUMENT_V3 worker grant
                Number grantCount = (Number) entityManager.createNativeQuery("""
                        SELECT count(*) FROM rag.worker_grants
                        WHERE job_id = :job AND attempt_id = :attempt AND stage_type = 'DOCUMENT_V3' AND consumed_at IS NOT NULL
                          AND input_digest=:input AND scope_digest=:scope AND output_digest=:output
                        """).setParameter("job", jobId)
                        .setParameter("attempt", attemptId)
                        .setParameter("input", expectedInput).setParameter("scope", expectedScope)
                        .setParameter("output", expectedOutput).getSingleResult();
                if (grantCount.longValue() != 1) {
                    throw new IllegalStateException("Valid consumed DOCUMENT_V3 worker grant is missing for job: " + jobId);
                }

                // (8) Generation / staging / run
                List<RagStagedDocumentOutput> staged = stagedOutputs.findByJobIdAndAttemptId(jobId, attemptId);
                Map<String, RagStagedDocumentOutput> stagedByRoot = new HashMap<>();
                for (var s : staged) {
                    if (stagedByRoot.putIfAbsent(s.getRootKey(), s) != null)
                        throw new IllegalStateException("Duplicate document output");
                }

                Map<String, UUID> publishedGenerations = new LinkedHashMap<>();
                int publishedCount = 0;
                int failedCount = 0;

                for (RagBatchRoot root : roots) {
                    RagSource source = policyMap.get(root.getSourceId());
                    if (source == null) {
                        throw new IllegalStateException("Missing locked policy source for: " + root.getRootKey());
                    }

                    RagStagedDocumentOutput stageOutput = stagedByRoot.get(root.getRootKey());
                    if (stageOutput != null && stageOutput.isSuccess()) {
                        if (stageOutput.getChunksPayload().isEmpty())
                            throw new IllegalStateException("Successful document output is empty");
                        var pipeline = pipelineIdentity(stageOutput);
                        // Deactivate previous active generations for this source
                        var existing = indexGenerations.findBySourceIdForUpdate(source.getId());
                        for (var gen : existing) {
                            if (gen.isActive()) {
                                gen.supersede(now);
                            }
                        }
                        indexGenerations.saveAll(existing);

                        // Create new generation
                        UUID genUuid = UUID.randomUUID();
                        String digest = generationDigests.get(source.getId());
                        RagIndexGeneration newGen = new RagIndexGeneration(
                                genUuid, jobId, source.getId(), "PUBLISHED",
                                source.getAuthorizationEpoch(), source.getSourceRevisionHash(),
                                digest, true, now
                        );
                        newGen = indexGenerations.save(newGen);

                        // Save generation root with exact metadata
                        Map<String, Object> exactManifest = new LinkedHashMap<>();
                        exactManifest.put("manifest_text", prepared.get(source.getId()).manifest());
                        exactManifest.put("source_id", source.getId());
                        exactManifest.put("domain_id", source.getSourceId());
                        exactManifest.put("source_type", source.getSourceType().name());
                        exactManifest.put("epoch", source.getAuthorizationEpoch());
                        exactManifest.put("source_version", root.getExpectedSourceVersion());
                        exactManifest.put("consent_reset_version", source.getApprovedOwnerResetVersion() != null ? source.getApprovedOwnerResetVersion() : 1L);
                        exactManifest.put("field_policy_version", source.getFieldPolicyVersion());
                        exactManifest.put("approved_fields", source.getApprovedFields().stream().sorted().toList());
                        exactManifest.put("source_revision_hash", source.getSourceRevisionHash());
                        exactManifest.put("description_revision_hash", source.getDescriptionRevisionHash());

                        RagGenerationRoot genRoot = new RagGenerationRoot(
                                newGen.getId(), root.getRootKey(),
                                exactManifest,
                                (String) pipeline.get("artifact_hash"), (String) pipeline.get("model_version"),
                                (String) pipeline.get("tokenizer_version"), (String) pipeline.get("parser_version"),
                                (String) pipeline.get("chunker_version"), (String) pipeline.get("ocr_pipeline_version"),
                                384, now
                        );
                        generationRoots.save(genRoot);

                        // Save published chunks
                        int chunkIdx = 0;
                        Set<Integer> visitedPages = new HashSet<>();
                        for (Map<String, Object> chunkData : stageOutput.getChunksPayload()) {
                            String content = (String) chunkData.getOrDefault("content", "");
                            String srcId = (String) chunkData.getOrDefault("source_identity", root.getRootKey());
                            @SuppressWarnings("unchecked")
                            Map<String, Object> locator = (Map<String, Object>) chunkData.getOrDefault("locator", Map.of());
                            @SuppressWarnings("unchecked")
                            Map<String, Object> struct = (Map<String, Object>) chunkData.getOrDefault("structure_metadata", Map.of());

                            List<Float> floats = extractAndValidateFloats(chunkData.get("embedding"));
                            validateChunk(content, locator, floats);

                            if (locator.containsKey("page") && locator.get("page") instanceof Number pNum) {
                                visitedPages.add(pNum.intValue());
                            }

                            float[] vector = new float[floats.size()];
                            for (int i = 0; i < vector.length; i++) vector[i] = floats.get(i);
                            documentChunks.save(new RagDocumentChunk(
                                    newGen.getId(), srcId, content, locator, struct, vector, now));
                            chunkIdx++;
                        }
                        int totalPageCount = Math.max(1, visitedPages.size());

                        // Record SUCCESS outcome
                        RagFileOutcome outcome = new RagFileOutcome(
                                jobId, root.getRootKey(), "SUCCESS", genUuid,
                                stageOutput.getWarningCodes(), chunkIdx, totalPageCount,
                                null, null, now
                        );
                        fileOutcomes.save(outcome);
                        publishedGenerations.put(root.getRootKey(), genUuid);
                        publishedCount++;
                    } else {
                        // Record FAILED outcome (no new generation installed)
                        String reason = stageOutput != null ? stageOutput.getReasonCode() : "UNPROCESSED";
                        List<String> warnings = stageOutput != null ? stageOutput.getWarningCodes() : List.of();
                        RagFileOutcome outcome = new RagFileOutcome(
                                jobId, root.getRootKey(), "FAILED", null,
                                warnings, 0, 0,
                                reason, "Document extraction or validation failed", now
                        );
                        fileOutcomes.save(outcome);
                        failedCount++;
                    }
                }

                // Determine terminal status
                String terminalStatus = publishedCount > 0 ? "COMPLETED" : "ABORTED";
                lease.updateStatus(terminalStatus, now);
                leases.save(lease);

                var run = runs.findByJobId(jobId).orElseThrow();
                entityManager.refresh(run, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                run.updatePurgeStatus("ACTIVE", now);
                runs.save(run);

                // Atomically release the entire reservation union!
                if (!vaultOwnerIds.isEmpty()) {
                    vaultReservations.release(vaultOwnerIds, lease.getReservationToken());
                }
                if (!externalFileIds.isEmpty()) {
                    externalFileAuthority.release(externalFileIds, lease.getReservationToken());
                }

                entityManager.flush();
                entityManager.createNativeQuery("UPDATE rag.runs SET status=:status WHERE job_id=:job")
                        .setParameter("status", terminalStatus)
                        .setParameter("job", jobId)
                        .executeUpdate();

                return new RagBatchFinalizationResult(jobId, terminalStatus, publishedCount, failedCount, publishedGenerations);
            });
        } catch (RuntimeException ex) {
            log.error("Document batch finalization physical COMMIT failed for job: {}", jobId);
            purge.requireRecovery(jobId);
            throw ex;
        }

        if (supervisor.isSupervised(jobId)) {
            supervisor.forget(jobId);
        }
        return result;
    }

    private List<Float> extractAndValidateFloats(Object embObj) {
        if (!(embObj instanceof List<?> list)) {
            throw new IllegalStateException("Published chunk must contain 384-dimensional embedding");
        }
        if (list.size() != 384) {
            throw new IllegalStateException("Published chunk must contain 384-dimensional embedding, found: " + list.size());
        }
        List<Float> floats = new ArrayList<>(384);
        for (Object val : list) {
            if (!(val instanceof Number num)) {
                throw new IllegalStateException("Embedding vector contains non-numeric value");
            }
            float f = num.floatValue();
            if (!Float.isFinite(f)) {
                throw new IllegalStateException("Embedding vector contains non-finite float value");
            }
            floats.add(f);
        }
        return floats;
    }

    private Map<?, ?> pipelineIdentity(RagStagedDocumentOutput stage) {
        if (!(stage.getResourceReport().get("pipeline_identity") instanceof Map<?, ?> identity)
                || !PIPELINE.entrySet().stream().allMatch(e -> Objects.equals(e.getValue(), identity.get(e.getKey())))
                || !Set.of("CPU", "CUDA").contains(identity.get("provider")))
            throw new IllegalStateException("Unverified document pipeline identity");
        return identity;
    }

    private void validateChunk(String content, Map<String, Object> locator, List<Float> floats) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Document chunk content must not be blank");
        }
        if (locator == null || locator.isEmpty()) {
            throw new IllegalStateException("Document chunk must have non-empty locator");
        }
        if (locator.containsKey("page")) {
            Object pageObj = locator.get("page");
            if (!(pageObj instanceof Number num) || num.intValue() < 1) {
                throw new IllegalStateException("PDF locator page must be >= 1");
            }
            if (!locator.containsKey("bbox")) {
                throw new IllegalStateException("PDF locator must include bbox");
            }
            Object bboxObj = locator.get("bbox");
            if (!(bboxObj instanceof List<?> coords) || coords.size() != 4) {
                throw new IllegalStateException("PDF locator bbox must have 4 coordinates");
            }
            float[] box = new float[4];
            for (int i = 0; i < 4; i++) {
                if (!(coords.get(i) instanceof Number c)) {
                    throw new IllegalStateException("PDF bbox coordinates must be numbers");
                }
                box[i] = c.floatValue();
                if (!Float.isFinite(box[i]) || box[i] < 0) {
                    throw new IllegalStateException("PDF bbox coordinates must be non-negative finite numbers");
                }
            }
            if (box[0] >= box[2] || box[1] >= box[3]) {
                throw new IllegalStateException("PDF bbox must have x0 < x1 and y0 < y1");
            }
        } else if (locator.containsKey("worksheet")) {
            Object ws = locator.get("worksheet");
            Object a1 = locator.get("a1_range");
            if (!(ws instanceof String wsStr) || wsStr.isBlank()) {
                throw new IllegalStateException("Spreadsheet locator must have non-blank worksheet");
            }
            if (!(a1 instanceof String a1Str) || a1Str.isBlank()) {
                throw new IllegalStateException("Spreadsheet locator must have non-blank a1_range");
            }
        } else if (locator.containsKey("paragraph_index") || locator.containsKey("table_index")) {
            boolean valid = false;
            if (locator.get("paragraph_index") instanceof Number num && num.intValue() >= 0) {
                valid = true;
            }
            if (locator.get("table_index") instanceof Number num && num.intValue() >= 0) {
                valid = true;
            }
            if (!valid) {
                throw new IllegalStateException("DOCX locator must have non-negative paragraph_index or table_index");
            }
        } else if (locator.containsKey("line_start")) {
            if (!(locator.get("line_start") instanceof Number num) || num.intValue() < 1) {
                throw new IllegalStateException("Markdown/TXT locator line_start must be >= 1");
            }
            if (!(locator.get("line_end") instanceof Number endNum) || endNum.intValue() < num.intValue()) {
                throw new IllegalStateException("Markdown/TXT locator line_end must be >= line_start");
            }
        } else {
            throw new IllegalStateException("Document chunk locator must specify page, worksheet, paragraph_index, or line_start");
        }

        if (floats == null || floats.size() != 384) {
            throw new IllegalStateException("Published chunk must contain 384-dimensional embedding");
        }
    }

}
