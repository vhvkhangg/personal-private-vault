package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagReaderBusyException;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalOperations;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagQueryEmbeddingSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagRetrievalIncidentIntegrationTest extends AbstractRagIntegrationTest {

    @org.springframework.test.context.DynamicPropertySource
    static void enableRetrieval(org.springframework.test.context.DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.retrieval.enabled", () -> "true");
    }

    @Autowired
    private RagRetrievalAuthority ragRetrievalAuthority;

    @Autowired
    private RagRetrievalReaderRepository ragRetrievalReaderRepository;

    @Autowired
    private RagRetrievalInspectionRepository ragRetrievalInspectionRepository;

    @Autowired
    private RagSourceRepository ragSourceRepository;

    @Autowired
    private RagExternalFileSourceRepository ragExternalFileSourceRepository;

    @Autowired
    private RagIndexGenerationRepository ragIndexGenerationRepository;

    @Autowired
    private RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor;

    @Autowired
    private RagOriginalBytesStore ragOriginalBytesStore;

    @Autowired
    private RagRetrievalOperations ragRetrievalOperations;

    @Autowired
    private RagRecoveryService ragRecoveryService;

    private static final Map<String, Object> VALID_QUIESCENCE = noSenderEvidence("SQL_LIFECYCLE_ONLY");
    private static final Map<String, Object> ABORT_QUIESCENCE = noSenderEvidence("SQL_LIFECYCLE_ABORT");

    @BeforeEach
    void setUp() {
        ragQueryEmbeddingSupervisor.setVectorOverride((r, q) -> new float[384]);
    }

    @AfterEach
    void tearDown() {
        ragQueryEmbeddingSupervisor.setVectorOverride(null);
    }

    @Test
    @DisplayName("R10: Competing reader admissions enforce exactly one reader, denying second with RAG_READER_BUSY")
    void competingAdmissionsEnforceSingleReader() {
        UUID reader1 = UUID.randomUUID();
        UUID token1 = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(reader1, token1, "digest:1", scope, List.of(fileSourceId), "controller:test");

        Optional<RagRetrievalReader> active = ragRetrievalReaderRepository.findActiveReader();
        assertThat(active).isPresent();
        assertThat(active.get().getReaderId()).isEqualTo(reader1);

        UUID reader2 = UUID.randomUUID();
        UUID token2 = UUID.randomUUID();

        assertThatThrownBy(() -> admitFixture(reader2, token2, "digest:2", scope, List.of(fileSourceId), "controller:test"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("RAG_READER_BUSY");

        ragRetrievalAuthority.finish(reader1, token1, "COMPLETED", "EMITTED", VALID_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("R04: Inspection incident before emission CAS blocks beginEmission")
    void incidentBeforeEmissionBlocksBeginEmission() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:test", scope, List.of(fileSourceId), "controller:test");

        beginInspectionFixture(inspectionId, readerId, "IncidentTest", "digest:test", fileSourceId, null);
        ragRetrievalAuthority.recordInspection(inspectionId, "INCIDENT_PENDING", true, Map.of("reason", "corruption_detected"));

        assertThatThrownBy(() -> ragRetrievalAuthority.beginEmission(readerId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("stopped or terminal");

        ragRetrievalAuthority.finish(readerId, token, "EXECUTION_FAILURE", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("R05: Cannot complete reader with unresolved source inspections (must be clean or incident-denied)")
    void cannotCompleteReaderWithUnresolvedInspections() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:r05", scope, List.of(fileSourceId), "controller:test");

        beginInspectionFixture(inspectionId, readerId, "IncidentTest", "digest:r05", fileSourceId, null);

        assertThatThrownBy(() -> ragRetrievalAuthority.finish(readerId, token, "COMPLETED", "EMITTED", VALID_QUIESCENCE, null, null))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Unresolved inspection");

        assertThatThrownBy(() -> ragRetrievalAuthority.finish(readerId, token, "EXECUTION_FAILURE", "ABORTED", ABORT_QUIESCENCE, null, null))
                .hasMessageContaining("Unresolved inspection");
        assertThat(ragRetrievalReaderRepository.findByReaderId(readerId).orElseThrow().getState()).isNotEqualTo("TERMINAL");
        assertThat(ragExternalFileSourceRepository.findByFileSourceId(fileSourceId).orElseThrow().getReservedBy()).isEqualTo(token);
        ragRetrievalAuthority.recordInspection(inspectionId, "CLEAN", false, Map.of());
        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("R09: Wrong reservation token on finish is denied")
    void wrongReservationTokenOnFinishDenied() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID wrongToken = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:r09", scope, List.of(fileSourceId), "controller:test");

        assertThatThrownBy(() -> ragRetrievalAuthority.finish(readerId, wrongToken, "COMPLETED", "EMITTED", VALID_QUIESCENCE, null, null))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("authority denied");

        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("F07: Direct UPDATE on reserved_by in external_file_sources is prevented by trigger")
    void directReleaseBypassPreventedByTrigger() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:f07", scope, List.of(fileSourceId), "controller:test");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE rag.external_file_sources SET reserved_by = NULL WHERE file_source_id = ?",
                fileSourceId
        ))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Cannot release or change reservation held by active retrieval reader");

        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("F07: Direct UPDATE of reader state to TERMINAL without quiescence evidence is rejected")
    void directTerminalStateBypassPreventedByTrigger() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:f07_term", scope, List.of(fileSourceId), "controller:test");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE rag.retrieval_readers SET state = 'TERMINAL' WHERE reader_id = ?",
                readerId
        ))
                .isInstanceOf(Exception.class);

        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("F04: Reader cancellation with pending incident transitions to INCIDENT_DENIED and marks snapshot_integrity_failed")
    void cancellationPreservesIncidentAndMarksIncidentDenied() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        fileSource.setSelectedRevision(revisionId, "hash1", 100L, now);
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L),
                "revision_ids", List.of(revisionId.toString())
        );

        admitFixture(readerId, token, "digest:f04", scope, List.of(fileSourceId), "controller:test");

        beginInspectionFixture(inspectionId, readerId, "CorruptionInspection", "digest:f04", fileSourceId, revisionId);
        ragRetrievalAuthority.recordInspection(inspectionId, "INCIDENT_PENDING", true, Map.of("defect", "bit_rot"));

        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);

        Optional<RagRetrievalReader> readerOpt = ragRetrievalReaderRepository.findByReaderId(readerId);
        assertThat(readerOpt).isPresent();
        assertThat(readerOpt.get().getState()).isEqualTo("TERMINAL");
        assertThat(readerOpt.get().getFinishDisposition()).isEqualTo("INCIDENT_DENIED");

        Optional<RagExternalFileSource> extOpt = ragExternalFileSourceRepository.findByFileSourceId(fileSourceId);
        assertThat(extOpt).isPresent();
        assertThat(extOpt.get().isSnapshotIntegrityFailed()).isTrue();

        Optional<RagRetrievalInspection> inspOpt = ragRetrievalInspectionRepository.findById(inspectionId);
        assertThat(inspOpt).isPresent();
        assertThat(inspOpt.get().getState()).isEqualTo("DENIED");
        assertThat(inspOpt.get().isDenyNewAdmission()).isTrue();
    }

    @Test
    @DisplayName("F07: Direct DELETE on retrieval_readers and retrieval_inspections is rejected by triggers")
    void directDeleteOnReadersAndInspectionsPrevented() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:f07_del", scope, List.of(fileSourceId), "controller:test");
        beginInspectionFixture(inspectionId, readerId, "DeleteTest", "digest:f07_del", fileSourceId, null);

        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM rag.retrieval_readers WHERE reader_id = ?", readerId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("retrieval_readers");

        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM rag.retrieval_inspections WHERE inspection_id = ?", inspectionId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("retrieval_inspections");

        ragRetrievalAuthority.recordInspection(inspectionId, "CLEAN", false, Map.of());
        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("F03/R01: Two-thread concurrent admission, inspection, emission, and finish execute without 40P01 deadlock")
    void twoThreadConcurrentExecutionDeadlockFree() throws Exception {
        UUID fileSourceId1 = UUID.randomUUID();
        UUID fileSourceId2 = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource1 = new RagExternalFileSource(
                fileSourceId1, "canon:" + fileSourceId1, 1L, 1L, 1L, 1, true, now
        );
        RagExternalFileSource fileSource2 = new RagExternalFileSource(
                fileSourceId2, "canon:" + fileSourceId2, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource1);
        ragExternalFileSourceRepository.save(fileSource2);

        List<UUID> fileIds = List.of(fileSourceId1, fileSourceId2).stream().sorted().toList();

        int iterations = 10;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < iterations; i++) {
                UUID readerId = UUID.randomUUID();
                UUID token = UUID.randomUUID();
                UUID inspectionId = UUID.randomUUID();

                Map<String, Object> scope = Map.of(
                        "external_file_ids", fileIds.stream().map(UUID::toString).toList(),
                        "source_ids", List.of(1L, 2L)
                );

                CountDownLatch startSignal = new CountDownLatch(1);
                AtomicReference<Throwable> thread1Error = new AtomicReference<>();
                AtomicReference<Throwable> thread2Error = new AtomicReference<>();

                // Thread 1: admit -> beginEmission -> finish
                Future<?> f1 = executor.submit(() -> {
                    try {
                        startSignal.await();
                        admitFixture(readerId, token, "digest:" + readerId, scope, fileIds, "controller:test");
                        try {
                            ragRetrievalAuthority.beginEmission(readerId);
                        } catch (Exception ignored) {}
                        ragRetrievalAuthority.finish(readerId, token, "COMPLETED", "EMITTED", VALID_QUIESCENCE, null, null);
                    } catch (Throwable t) {
                        thread1Error.set(t);
                    }
                });

                // Thread 2: beginInspection -> recordInspection
                Future<?> f2 = executor.submit(() -> {
                    try {
                        startSignal.await();
                        beginInspectionFixture(inspectionId, readerId, "ConcurrentCheck", "digest:" + readerId, fileSourceId1, null);
                        ragRetrievalAuthority.recordInspection(inspectionId, "CLEAN", false, Map.of());
                    } catch (Throwable t) {
                        thread2Error.set(t);
                    }
                });

                startSignal.countDown();
                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                // Neither thread must fail with 40P01 deadlock
                if (thread1Error.get() != null) {
                    assertThat(thread1Error.get().getMessage()).doesNotContain("40P01").doesNotContain("deadlock");
                }
                if (thread2Error.get() != null) {
                    assertThat(thread2Error.get().getMessage()).doesNotContain("40P01").doesNotContain("deadlock");
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("F04: Inspection monotonicity prevents demoting DENIED or INCIDENT_PENDING to CLEAN")
    void inspectionMonotonicityPreventsDemotion() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:mono", scope, List.of(fileSourceId), "controller:test");
        beginInspectionFixture(inspectionId, readerId, "MonotonicityTest", "digest:mono", fileSourceId, null);
        ragRetrievalAuthority.recordInspection(inspectionId, "DENIED", true, Map.of("reason", "integrity_flaw"));

        // Attempt to demote from DENIED to CLEAN via authority must fail
        assertThatThrownBy(() -> ragRetrievalAuthority.recordInspection(inspectionId, "CLEAN", false, Map.of()))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Cannot demote denied inspection");

        // Attempt direct UPDATE to demote state to CLEAN must fail via trigger or permission
        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE rag.retrieval_inspections SET state = 'CLEAN' WHERE inspection_id = ?",
                inspectionId
        ))
                .isInstanceOf(Exception.class);

        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);
    }

    @Test
    @DisplayName("F04: Revision-bound authority ensures incident on old revision A does not invalidate revision B")
    void revisionBoundIncidentDoesNotInvalidateOtherRevision() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        UUID revisionA = UUID.randomUUID();
        UUID revisionB = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        fileSource.setSelectedRevision(revisionB, "hashB", 200L, now);
        ragExternalFileSourceRepository.save(fileSource);

        // Scope includes revisionB
        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L),
                "revision_ids", List.of(revisionB.toString())
        );

        admitFixture(readerId, token, "digest:rev", scope, List.of(fileSourceId), "controller:test");

        // Record incident on older revision A (not revision B)
        beginInspectionFixture(inspectionId, null, "RevTest", "digest:rev", fileSourceId, revisionA);
        ragRetrievalAuthority.recordInspection(inspectionId, "DENIED", true, Map.of("reason", "old_rev_corrupt"));

        // Finishing reader for revision B should NOT be marked INCIDENT_DENIED because incident was on revision A
        ragRetrievalAuthority.finish(readerId, token, "COMPLETED", "EMITTED", VALID_QUIESCENCE, null, null);

        Optional<RagRetrievalReader> readerOpt = ragRetrievalReaderRepository.findByReaderId(readerId);
        assertThat(readerOpt).isPresent();
        assertThat(readerOpt.get().getFinishDisposition()).isEqualTo("COMPLETED");
        assertThat(readerOpt.get().getEmissionDisposition()).isEqualTo("EMITTED");

        // And fileSource with selected revision B must NOT have snapshot_integrity_failed marked true
        Optional<RagExternalFileSource> extOpt = ragExternalFileSourceRepository.findByFileSourceId(fileSourceId);
        assertThat(extOpt).isPresent();
        assertThat(extOpt.get().isSnapshotIntegrityFailed()).isFalse();
    }

    @Test
    @DisplayName("F06/R07: Unknown old readers remain excluded without positive controller/sender stop proof")
    void recoveryRetainsUnknownActiveReaders() {
        UUID staleReaderId = UUID.randomUUID();
        UUID staleToken = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(staleReaderId, staleToken, "digest:stale", scope, List.of(fileSourceId), "controller:crashed");

        // Verify reader is ADMITTED and source is reserved by token
        assertThat(ragRetrievalReaderRepository.findByReaderId(staleReaderId).get().getState()).isEqualTo("ADMITTED");
        assertThat(ragExternalFileSourceRepository.findByFileSourceId(fileSourceId).get().getReservedBy()).isEqualTo(staleToken);

        // Run recovery lifecycle
        ragRecoveryService.afterSingletonsInstantiated();

        // A label claiming a crashed controller is not stop proof.
        Optional<RagRetrievalReader> recReader = ragRetrievalReaderRepository.findByReaderId(staleReaderId);
        assertThat(recReader).isPresent();
        assertThat(recReader.get().getState()).isEqualTo("ADMITTED");
        assertThat(recReader.get().getFinishDisposition()).isNull();

        // Preserve the exact source exclusion.
        Optional<RagExternalFileSource> recSource = ragExternalFileSourceRepository.findByFileSourceId(fileSourceId);
        assertThat(recSource).isPresent();
        assertThat(recSource.get().getReservedBy()).isEqualTo(staleToken);
    }

    @Test
    @DisplayName("F06: Snapshot integrity failure during reconciliation registers and records inspection")
    void reconciliationRecordsInspectionOnIntegrityFailure() {
        UUID fileSourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        fileSource.setSelectedRevision(revisionId, "tampered_hash", 999L, now);
        ragExternalFileSourceRepository.save(fileSource);

        // Reconcile snapshots
        ragRecoveryService.reconcileSnapshots();

        // Verify external file source has snapshot_integrity_failed = true
        Optional<RagExternalFileSource> extOpt = ragExternalFileSourceRepository.findByFileSourceId(fileSourceId);
        assertThat(extOpt).isPresent();
        assertThat(extOpt.get().isSnapshotIntegrityFailed()).isTrue();

        // Verify inspection record was created
        List<RagRetrievalInspection> inspections = ragRetrievalInspectionRepository.findByAffectedFileSourceId(fileSourceId);
        assertThat(inspections).isNotEmpty();
        assertThat(inspections.get(0).getState()).isEqualTo("DENIED");
        assertThat(inspections.get(0).isDenyNewAdmission()).isTrue();
    }

    @Test
    void selectedAndPendingSnapshotChecksBindSeparateRevisionObligations() {
        UUID file=UUID.randomUUID(),pending=UUID.randomUUID();
        ragExternalFileSourceRepository.saveAndFlush(new RagExternalFileSource(file,"tuple-test:"+file,1L,1L,1L,1,true,Instant.now()));
        completeRetrievalFixtureScope(Map.of(),List.of(file));
        var row=ragExternalFileSourceRepository.findByFileSourceId(file).orElseThrow();
        UUID selected=row.getSelectedRevisionId();
        row.setPendingRevision(pending,"0".repeat(64),1L,Instant.now(),selected,row.getSourceVersion(),Instant.now());
        ragExternalFileSourceRepository.saveAndFlush(row);
        ragRecoveryService.reconcileSnapshots();
        var observations=ragRetrievalInspectionRepository.findByAffectedFileSourceId(file);
        assertThat(observations).anySatisfy(i->{assertThat(i.getAffectedRevisionId()).isEqualTo(pending);assertThat(i.getState()).isEqualTo("DENIED");});
        assertThat(observations.stream().filter(i->selected.equals(i.getAffectedRevisionId())).toList())
                .isNotEmpty().allSatisfy(i->assertThat(i.getState()).isEqualTo("CLEAN"));
    }

    @Test
    @DisplayName("F08/R08: Re-approval or owner reset does not revive terminal reader")
    void reapprovalResetNeverRevivesOldReader() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID fileSourceId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        RagSource source = new RagSource(
                RagSourceType.EXTERNAL_FILE,
                fileSourceId.toString(),
                RagClassification.NON_PRIVATE,
                1L, 1, Set.of("content"),
                RagAccessState.APPROVED,
                "hash1",
                "manifest:digest",
                now
        );
        source.bindOwnerResetVersion(1L);
        ragSourceRepository.save(source);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(source.getId())
        );

        admitFixture(readerId, token, "digest:r08", scope, List.of(fileSourceId), "controller:test");
        ragRetrievalAuthority.finish(readerId, token, "CANCELLED", "ABORTED", ABORT_QUIESCENCE, null, null);

        Optional<RagRetrievalReader> r = ragRetrievalReaderRepository.findByReaderId(readerId);
        assertThat(r).isPresent();
        assertThat(r.get().getState()).isEqualTo("TERMINAL");

        // Owner resets / re-approves source with bumped epoch
        source.bindOwnerResetVersion(2L);
        ragSourceRepository.save(source);

        // Attempting to begin emission on terminal reader must fail
        assertThatThrownBy(() -> ragRetrievalAuthority.beginEmission(readerId))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("stopped or terminal");

        // Attempting to re-admit the same reader ID must fail
        assertThatThrownBy(() -> admitFixture(readerId, UUID.randomUUID(), "digest:new", scope, List.of(fileSourceId), "controller:test"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("F04: Child revision incident during EMITTING is discovered and applied on finish")
    void childRevisionIncidentDuringEmittingAppliedOnFinish() {
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        UUID parentFileId = UUID.randomUUID();
        UUID parentRevId = UUID.randomUUID();
        UUID childFileId = UUID.randomUUID();
        UUID childRevId = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource parentFile = new RagExternalFileSource(
                parentFileId, "canon:" + parentFileId, 1L, 1L, 1L, 1, true, now
        );
        parentFile.setSelectedRevision(parentRevId, "parent_hash", 100L, now);
        ragExternalFileSourceRepository.save(parentFile);

        RagExternalFileSource childFile = new RagExternalFileSource(
                childFileId, "canon:" + childFileId, 2L, 2L, 1L, 1, true, now
        );
        childFile.setSelectedRevision(childRevId, "child_hash", 200L, now);
        ragExternalFileSourceRepository.save(childFile);

        List<UUID> fileIds = List.of(parentFileId, childFileId).stream().sorted().toList();

        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("sources", List.of(
                Map.of(
                        "source_id", 10L,
                        "file_source_id", parentFileId.toString(),
                        "revision_id", parentRevId.toString(),
                        "role", "TARGET"
                ),
                Map.of(
                        "source_id", 20L,
                        "file_source_id", childFileId.toString(),
                        "revision_id", childRevId.toString(),
                        "role", "DEPENDENCY"
                )
        ));
        scope.put("external_file_ids", fileIds.stream().map(UUID::toString).toList());
        scope.put("source_ids", List.of(10L, 20L));
        scope.put("revision_ids", List.of(parentRevId.toString(), childRevId.toString()));
        scope.put("transitive_dependency_ids", List.of(20L));

        admitFixture(readerId, token, "digest:dep", scope, fileIds, "controller:test");

        // Begin emission CAS
        ragRetrievalAuthority.beginEmission(readerId);
        Optional<RagRetrievalReader> readerBefore = ragRetrievalReaderRepository.findByReaderId(readerId);
        assertThat(readerBefore).isPresent();
        assertThat(readerBefore.get().getState()).isEqualTo("EMITTING");

        // While in EMITTING, record an incident on child revision
        beginInspectionFixture(inspectionId, null, "ChildIntegrity", "digest:dep", childFileId, childRevId);
        ragRetrievalAuthority.recordInspection(inspectionId, "INCIDENT_PENDING", true, Map.of("reason", "child_corrupt"));

        // Now reader completes or cancels finish
        ragRetrievalAuthority.finish(readerId, token, "COMPLETED", "EMITTED", VALID_QUIESCENCE, null, null);

        // Finish must discover the child incident and convert finish disposition to INCIDENT_DENIED
        Optional<RagRetrievalReader> readerAfter = ragRetrievalReaderRepository.findByReaderId(readerId);
        assertThat(readerAfter).isPresent();
        assertThat(readerAfter.get().getState()).isEqualTo("TERMINAL");
        assertThat(readerAfter.get().getFinishDisposition()).isEqualTo("INCIDENT_DENIED");

        // And child file must have snapshot_integrity_failed marked true
        Optional<RagExternalFileSource> childAfter = ragExternalFileSourceRepository.findByFileSourceId(childFileId);
        assertThat(childAfter).isPresent();
        assertThat(childAfter.get().isSnapshotIntegrityFailed()).isTrue();
    }

    @Test
    @DisplayName("F03: Deterministic lock order ensures recovery and inspection never deadlock 40P01")
    void deterministicLockOrderBetweenRecoveryAndInspection() throws Exception {
        UUID fileSourceId = UUID.randomUUID();
        UUID readerId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        Instant now = Instant.now();

        RagExternalFileSource fileSource = new RagExternalFileSource(
                fileSourceId, "canon:" + fileSourceId, 1L, 1L, 1L, 1, true, now
        );
        ragExternalFileSourceRepository.save(fileSource);

        Map<String, Object> scope = Map.of(
                "external_file_ids", List.of(fileSourceId.toString()),
                "source_ids", List.of(1L)
        );

        admitFixture(readerId, token, "digest:lock", scope, List.of(fileSourceId), "controller:old");

        // Run recovery and inspection concurrently across iterations
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 5; i++) {
                UUID inspectionId = UUID.randomUUID();
                CountDownLatch readyLatch = new CountDownLatch(2);
                CountDownLatch goLatch = new CountDownLatch(1);
                AtomicReference<Throwable> err1 = new AtomicReference<>();
                AtomicReference<Throwable> err2 = new AtomicReference<>();

                Future<?> f1 = executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        goLatch.await();
                        ragRecoveryService.afterSingletonsInstantiated();
                    } catch (Throwable t) {
                        err1.set(t);
                    }
                });

                Future<?> f2 = executor.submit(() -> {
                    readyLatch.countDown();
                    try {
                        goLatch.await();
                        beginInspectionFixture(inspectionId, null, "LockOrderTest", "digest:lock", fileSourceId, null);
                        ragRetrievalAuthority.recordInspection(inspectionId, "CLEAN", false, Map.of());
                    } catch (Throwable t) {
                        err2.set(t);
                    }
                });

                readyLatch.await(5, TimeUnit.SECONDS);
                goLatch.countDown();
                f1.get(5, TimeUnit.SECONDS);
                f2.get(5, TimeUnit.SECONDS);

                if (err1.get() != null) {
                    assertThat(err1.get().getMessage()).doesNotContain("40P01").doesNotContain("deadlock");
                }
                if (err2.get() != null) {
                    assertThat(err2.get().getMessage()).doesNotContain("40P01").doesNotContain("deadlock");
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /** Minimal SQL lifecycle fixtures have exact tuples; they are not canonical retrieval proof. */
    private void admitFixture(UUID reader, UUID token, String digest, Map<String,Object> scope,
            Collection<UUID> files, String controller) {
        var complete = new LinkedHashMap<String,Object>(scope);
        var tuples = new ArrayList<Map<String,Object>>();
        for (UUID file : files) {
            var row = ragExternalFileSourceRepository.findByFileSourceId(file).orElseThrow();
            if (row.getSelectedRevisionId() == null) {
                row.setSelectedRevision(UUID.randomUUID(), "synthetic-hash", 1L, Instant.now());
                ragExternalFileSourceRepository.saveAndFlush(row);
            }
            tuples.add(Map.of("file_source_id",file.toString(),"revision_id",row.getSelectedRevisionId().toString()));
        }
        complete.putIfAbsent("sources",tuples);
        complete.putIfAbsent("generation_ids",List.of());
        ragRetrievalAuthority.admit(reader,token,digest,completeRetrievalFixtureScope(complete,files),files,retrievalFixtureController());
    }

    private void beginInspectionFixture(UUID inspection, UUID reader, String producer, String digest,
            UUID file, UUID revision) {
        UUID exact = revision != null ? revision : ragExternalFileSourceRepository.findByFileSourceId(file)
                .map(RagExternalFileSource::getSelectedRevisionId).orElse(null);
        ragRetrievalAuthority.beginInspection(inspection,reader,producer,digest,file,exact);
    }
}
