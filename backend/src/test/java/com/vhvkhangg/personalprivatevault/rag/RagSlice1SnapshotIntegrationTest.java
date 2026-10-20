package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagConsentCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagExternalFileAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagSourceRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 18 Slice 1 Integration Test:
 * Verifies owner-authorized Snapshot Acquisition, Sealing to Pending Revision,
 * NO_CHANGE candidate detection, Whole-File Consent, Revocation Purge,
 * and Inactive External Indexing.
 */
@Timeout(60)
class RagSlice1SnapshotIntegrationTest extends AbstractRagIntegrationTest {

    @Autowired
    private RagOperations rag;

    @Autowired
    private RagExternalFileAuthority externalFileAuthority;

    @Autowired
    private RagOriginalBytesStore byteStore;

    @Autowired
    private RagSourceAuthority sourceAuthority;

    @Autowired
    private RagSourceRepository sourceRepository;

    @TempDir
    Path workDir;

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    @Test
    void testRegisterFileCandidate_sealsPendingRevision_leavesSelectedRevisionNull_originalUnchanged() throws Exception {
        Path file = workDir.resolve("document.txt");
        byte[] content = "Content for slice 1 testing: original bytes unchanged.".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);
        long originalMtime = Files.getLastModifiedTime(file).toMillis();

        RagSourceView view = rag.registerSnapshotCandidate(file.toString());

        assertThat(view).isNotNull();
        assertThat(view.sourceType()).isEqualTo(RagSourceType.EXTERNAL_FILE);
        assertThat(view.accessState()).isEqualTo(RagAccessState.DENIED);
        assertThat(view.classification()).isEqualTo(RagClassification.NON_PRIVATE);
        assertThat(view.sourceRevisionHash()).isEqualTo(sha256(content));
        assertThat(view.sourceVersion()).isEqualTo(1L);
        assertThat(view.consentResetVersion()).isEqualTo(1L);

        // Verify database state in RagExternalFileSource
        UUID fileSourceId = UUID.fromString(view.sourceId());
        RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);
        assertThat(ext).isNotNull();
        assertThat(ext.isEligible()).isTrue();
        assertThat(ext.getPendingRevisionId()).isNotNull();
        assertThat(ext.getPendingRevisionHash()).isEqualTo(sha256(content));
        assertThat(ext.getPendingByteLength()).isEqualTo(content.length);
        assertThat(ext.getSelectedRevisionId()).isNull(); // CRITICAL INVARIANT: NEVER SELECT IN SLICE 1!
        assertThat(ext.getSourceVersion()).isEqualTo(1L);

        // Verify sealed revision in storage
        assertThat(byteStore.verifyIntegrity(fileSourceId, ext.getPendingRevisionId(), sha256(content), content.length)).isTrue();
        byte[] storedBytes = byteStore.readRevisionBytes(fileSourceId, ext.getPendingRevisionId(), sha256(content), content.length);
        assertThat(storedBytes).isEqualTo(content);

        // Verify original file on disk is completely untouched
        assertThat(Files.readAllBytes(file)).isEqualTo(content);
        assertThat(Files.getLastModifiedTime(file).toMillis()).isEqualTo(originalMtime);
    }

    @Test
    void testNoChangeCandidate_whenIdenticalBytesCaptured() throws Exception {
        Path file = workDir.resolve("identical.txt");
        byte[] content = "Identical bytes candidate test.".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        RagSourceView initialView = rag.registerSnapshotCandidate(file.toString());
        UUID fileSourceId = UUID.fromString(initialView.sourceId());
        RagExternalFileSource ext1 = externalFileAuthority.findByFileSourceId(fileSourceId);
        UUID initialPendingRevId = ext1.getPendingRevisionId();

        // Capture again with identical bytes
        RagSourceView repeatedView = rag.capturePendingRevision(initialView.id(), new com.vhvkhangg.personalprivatevault.rag.api.RagSnapshotCaptureCommand(file.toString(), initialView.snapshot()));

        RagExternalFileSource ext2 = externalFileAuthority.findByFileSourceId(fileSourceId);
        // NO_CHANGE candidate: pending revision not recreated, selected revision still null, versions unchanged
        assertThat(ext2.getPendingRevisionId()).isEqualTo(initialPendingRevId);
        assertThat(ext2.getSelectedRevisionId()).isNull();
        assertThat(ext2.getSourceVersion()).isEqualTo(1L);
    }

    @Test
    void testCapturePendingRevision_changedBytes_sealsNewPendingAndCleansOldPending() throws Exception {
        Path file = workDir.resolve("mutate.txt");
        byte[] v1 = "Version 1 bytes.".getBytes(StandardCharsets.UTF_8);
        Files.write(file, v1);

        RagSourceView initialView = rag.registerSnapshotCandidate(file.toString());
        UUID fileSourceId = UUID.fromString(initialView.sourceId());
        RagExternalFileSource ext1 = externalFileAuthority.findByFileSourceId(fileSourceId);
        UUID oldPendingRevId = ext1.getPendingRevisionId();

        // Modify file on disk
        byte[] v2 = "Version 2 mutated bytes.".getBytes(StandardCharsets.UTF_8);
        Files.write(file, v2);

        // Capture changed revision
        RagSourceView updatedView = rag.capturePendingRevision(initialView.id(), new com.vhvkhangg.personalprivatevault.rag.api.RagSnapshotCaptureCommand(file.toString(), initialView.snapshot()));

        RagExternalFileSource ext2 = externalFileAuthority.findByFileSourceId(fileSourceId);
        UUID newPendingRevId = ext2.getPendingRevisionId();

        assertThat(newPendingRevId).isNotEqualTo(oldPendingRevId);
        assertThat(ext2.getPendingRevisionHash()).isEqualTo(sha256(v2));
        assertThat(ext2.getPendingByteLength()).isEqualTo(v2.length);
        assertThat(ext2.getSelectedRevisionId()).isNull(); // Still null in Slice 1
        assertThat(ext2.getSourceVersion()).isEqualTo(1L); // Still 1 in Slice 1

        // New revision is sealed and readable
        assertThat(byteStore.verifyIntegrity(fileSourceId, newPendingRevId, sha256(v2), v2.length)).isTrue();

        // Old pending revision is deleted
        assertThatThrownBy(() -> byteStore.verifyIntegrity(fileSourceId, oldPendingRevId, sha256(v1), v1.length))
                .isInstanceOf(SnapshotMissingException.class);
    }

    @Test
    void testDistinctSources_identicalBytes_noCrossSourceDeduplication() throws Exception {
        byte[] commonContent = "Shared content across distinct files.".getBytes(StandardCharsets.UTF_8);

        Path fileA = workDir.resolve("docA.txt");
        Files.write(fileA, commonContent);
        Path fileB = workDir.resolve("docB.txt");
        Files.write(fileB, commonContent);

        RagSourceView viewA = rag.registerSnapshotCandidate(fileA.toString());
        RagSourceView viewB = rag.registerSnapshotCandidate(fileB.toString());

        assertThat(viewA.id()).isNotEqualTo(viewB.id());
        assertThat(viewA.sourceId()).isNotEqualTo(viewB.sourceId());

        UUID sourceIdA = UUID.fromString(viewA.sourceId());
        UUID sourceIdB = UUID.fromString(viewB.sourceId());

        RagExternalFileSource extA = externalFileAuthority.findByFileSourceId(sourceIdA);
        RagExternalFileSource extB = externalFileAuthority.findByFileSourceId(sourceIdB);

        // Distinct revision IDs despite identical byte content
        assertThat(extA.getPendingRevisionId()).isNotEqualTo(extB.getPendingRevisionId());

        // Both revisions individually verified in their respective storage directories
        assertThat(byteStore.verifyIntegrity(sourceIdA, extA.getPendingRevisionId(), sha256(commonContent), commonContent.length)).isTrue();
        assertThat(byteStore.verifyIntegrity(sourceIdB, extB.getPendingRevisionId(), sha256(commonContent), commonContent.length)).isTrue();
    }

    @Test
    void testApproveSource_wholeFileConsent_validatesIntegrityAndApproves() throws Exception {
        Path file = workDir.resolve("approved.md");
        byte[] content = "# Approved Document Content".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        RagSourceView view = rag.registerSnapshotCandidate(file.toString());
        UUID fileSourceId = UUID.fromString(view.sourceId());
        RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);

        // Approve with whole-file consent ("content")
        RagConsentCommand cmd = new RagConsentCommand(
                Set.of("content"),
                view.sourceVersion(),
                view.consentResetVersion(),
                view.authorizationEpoch(),
                view.fieldPolicyVersion(),
                null, view.snapshot()
        );
        RagSourceView approvedView = rag.approveSource(view.id(), cmd);

        assertThat(approvedView.accessState()).isEqualTo(RagAccessState.APPROVED);
        assertThat(approvedView.approvedFields()).containsExactly("content");

        // Verify ext entity: sourceVersion is NOT incremented, selectedRevisionId is NOT changed
        RagExternalFileSource extAfter = externalFileAuthority.findByFileSourceId(fileSourceId);
        assertThat(extAfter.getSourceVersion()).isEqualTo(1L);
        assertThat(extAfter.getSelectedRevisionId()).isNull();
    }

    @Test
    void testApproveSource_invalidFieldRejected() throws Exception {
        Path file = workDir.resolve("bad_fields.txt");
        Files.write(file, "Some content".getBytes(StandardCharsets.UTF_8));

        RagSourceView view = rag.registerSnapshotCandidate(file.toString());

        RagConsentCommand invalidCmd = new RagConsentCommand(
                Set.of("content", "unsupported_field"),
                view.sourceVersion(),
                view.consentResetVersion(),
                view.authorizationEpoch(),
                view.fieldPolicyVersion(),
                null, view.snapshot()
        );

        assertThatThrownBy(() -> rag.approveSource(view.id(), invalidCmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not eligible");
    }

    @Test
    void testApproveSource_staleCasRejected() throws Exception {
        Path file = workDir.resolve("stale_cas.txt");
        Files.write(file, "Stale CAS test".getBytes(StandardCharsets.UTF_8));

        RagSourceView view = rag.registerSnapshotCandidate(file.toString());

        RagConsentCommand staleCmd = new RagConsentCommand(
                Set.of("content"),
                999L, // wrong sourceVersion
                view.consentResetVersion(),
                view.authorizationEpoch(),
                view.fieldPolicyVersion(),
                null, view.snapshot()
        );

        assertThatThrownBy(() -> rag.approveSource(view.id(), staleCmd))
                .isInstanceOf(RagSourceChangedException.class);
    }

    @Test
    void testRevokeSource_purgesRevisionsFromDisk() throws Exception {
        Path file = workDir.resolve("revoked.txt");
        byte[] content = "Content to be revoked".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        RagSourceView view = rag.registerSnapshotCandidate(file.toString());
        UUID fileSourceId = UUID.fromString(view.sourceId());
        RagExternalFileSource ext = externalFileAuthority.findByFileSourceId(fileSourceId);
        UUID revId = ext.getPendingRevisionId();

        assertThat(byteStore.verifyIntegrity(fileSourceId, revId, sha256(content), content.length)).isTrue();

        // Revoke
        RagSourceView revokedView = rag.revokeSource(view.id());
        assertThat(revokedView.accessState()).isEqualTo(RagAccessState.REVOKED);

        RagExternalFileSource extRevoked = externalFileAuthority.findByFileSourceId(fileSourceId);
        assertThat(extRevoked.isEligible()).isFalse();
        assertThat(extRevoked.getPendingRevisionId()).isNull();

        // Bytes purged from disk
        assertThatThrownBy(() -> byteStore.verifyIntegrity(fileSourceId, revId, sha256(content), content.length))
                .isInstanceOf(SnapshotMissingException.class);
    }

    @Test
    void testExternalSourceIndexing_failsClosedInSlice1() throws Exception {
        Path file = workDir.resolve("indexing_inactive.txt");
        Files.write(file, "Cannot index yet".getBytes(StandardCharsets.UTF_8));

        RagSourceView view = rag.registerSnapshotCandidate(file.toString());

        // Attempting to prepare source for indexing should fail closed with inactive message
        assertThatThrownBy(() -> sourceAuthority.prepareSource(view.id(), Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("External file indexing capability is inactive in Slice 1");
    }

    @Test
    void testLiveFileCandidateRegistration_remainsDisabled() {
        assertThatThrownBy(() -> rag.registerFileCandidate("C:/some/live/path.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Live external-file capability is disabled");
    }

    @Test
    void testStorageOrphanDetectionAndCleanup() throws Exception {
        UUID legitimateSource = UUID.randomUUID();
        UUID legitimateRev = UUID.randomUUID();
        UUID orphanedRev = UUID.randomUUID();
        UUID capture1 = UUID.randomUUID();
        UUID capture2 = UUID.randomUUID();

        byte[] data = "Orphan test".getBytes(StandardCharsets.UTF_8);
        byteStore.writeTemp(capture1, new ByteArrayInputStream(data), (long) data.length, sha256(data));
        byteStore.sealRevision(legitimateSource, legitimateRev, capture1, sha256(data), data.length);

        byteStore.writeTemp(capture2, new ByteArrayInputStream(data), (long) data.length, sha256(data));
        byteStore.sealRevision(legitimateSource, orphanedRev, capture2, sha256(data), data.length);

        // Register legitimate source in authority
        RagExternalFileSource ext = externalFileAuthority.registerOrGet(legitimateSource, "canonical-test", Instant.now());
        ext.setPendingRevision(legitimateRev, sha256(data), data.length, Instant.now(), null, 1L, Instant.now());
        externalFileAuthority.save(ext);

        // Find legitimate revisions
        Set<UUID> legitimateRevisions = externalFileAuthority.findAllLegitimateRevisionIds();
        assertThat(legitimateRevisions).contains(legitimateRev);
        assertThat(legitimateRevisions).doesNotContain(orphanedRev);

        // Detect orphans
        List<RagOriginalBytesStore.OrphanReport> orphans = byteStore.detectOrphans(legitimateRevisions, Set.of());
        assertThat(orphans).extracting(r -> r.path().getFileName().toString())
                .anyMatch(name -> name.startsWith(orphanedRev.toString()));

        // Cleanup orphans
        List<RagOriginalBytesStore.DeletionReport> cleaned = byteStore.cleanupOrphans(legitimateRevisions, Set.of());
        assertThat(cleaned).isNotEmpty();

        // Orphan is gone, legitimate remains
        assertThatThrownBy(() -> byteStore.verifyIntegrity(legitimateSource, orphanedRev, sha256(data), data.length))
                .isInstanceOf(SnapshotMissingException.class);
        assertThat(byteStore.verifyIntegrity(legitimateSource, legitimateRev, sha256(data), data.length)).isTrue();
    }
}
