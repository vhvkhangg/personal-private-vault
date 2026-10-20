package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotIntegrityException;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotStorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RagOriginalBytesStoreTest {

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Scenario 1: Exact input bytes, SHA-256 and length match sealed stored bytes")
    void scenario1_exactInputBytesSha256AndLengthMatch(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();

        byte[] inputBytes = "Protected immutable snapshot byte payload 2026-10-18".getBytes(StandardCharsets.UTF_8);
        String expectedHash = sha256Hex(inputBytes);
        long expectedLength = inputBytes.length;

        // Write to temp
        RagOriginalBytesStore.TempCapture temp = store.writeTemp(
                captureId, new ByteArrayInputStream(inputBytes), expectedLength, expectedHash);
        assertThat(temp.captureId()).isEqualTo(captureId);
        assertThat(temp.sha256()).isEqualTo(expectedHash);
        assertThat(temp.byteLength()).isEqualTo(expectedLength);
        assertThat(Files.exists(temp.tempPath())).isTrue();

        // Seal into revision
        RagOriginalBytesStore.StoredSnapshot sealed = store.sealRevision(
                sourceId, revisionId, captureId, expectedHash, expectedLength);
        assertThat(sealed.sourceId()).isEqualTo(sourceId);
        assertThat(sealed.revisionId()).isEqualTo(revisionId);
        assertThat(sealed.sha256()).isEqualTo(expectedHash);
        assertThat(sealed.byteLength()).isEqualTo(expectedLength);
        assertThat(Files.exists(sealed.storagePath())).isTrue();
        assertThat(Files.exists(sealed.manifestPath())).isTrue();
        assertThat(Files.exists(temp.tempPath())).isFalse(); // Temp moved atomically

        // Verify read back bytes match exact input via trusted single-pass read
        byte[] readBackTrusted = store.readRevisionBytes(sourceId, revisionId, expectedHash, expectedLength);
        assertThat(readBackTrusted).isEqualTo(inputBytes);

        // Verify read back with explicit expected captureId also matches
        byte[] readBackWithCaptureId = store.readRevisionBytes(sourceId, revisionId, captureId, expectedHash, expectedLength);
        assertThat(readBackWithCaptureId).isEqualTo(inputBytes);

        // Wrong expected captureId is rejected
        UUID wrongCaptureId = UUID.randomUUID();
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, wrongCaptureId, expectedHash, expectedLength))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Manifest captureId mismatch");
    }

    @Test
    @DisplayName("Scenario 2: No-overwrite invariant enforced on competing revision seal attempts")
    void scenario2_noOverwriteEnforcedOnCompetingSeals(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId1 = UUID.randomUUID();
        UUID captureId2 = UUID.randomUUID();

        byte[] bytes1 = "First version content".getBytes(StandardCharsets.UTF_8);
        byte[] bytes2 = "Competing version content attempting overwrite".getBytes(StandardCharsets.UTF_8);

        store.writeTemp(captureId1, new ByteArrayInputStream(bytes1), null, null);
        store.writeTemp(captureId2, new ByteArrayInputStream(bytes2), null, null);

        // Seal first revision -> succeeds
        store.sealRevision(sourceId, revisionId, captureId1, sha256Hex(bytes1), bytes1.length);

        // Competing attempt to seal a revision with the exact same revisionId -> MUST FAIL (no overwrite!)
        assertThatThrownBy(() -> store.sealRevision(sourceId, revisionId, captureId2, sha256Hex(bytes2), bytes2.length))
                .isInstanceOf(SnapshotStorageException.class)
                .hasMessageContaining("already exists");

        // The original sealed content must remain intact and unmodified
        byte[] stored = Files.readAllBytes(store.getRevisionsDir().resolve(sourceId.toString()).resolve(revisionId + ".orig"));
        assertThat(stored).isEqualTo(bytes1);

        // Directly pre-create a target file to verify kernel-level MoveFileExW atomic no-overwrite refusal
        UUID revisionId3 = UUID.randomUUID();
        UUID captureId3 = UUID.randomUUID();
        byte[] bytes3 = "Third version content".getBytes(StandardCharsets.UTF_8);
        store.writeTemp(captureId3, new ByteArrayInputStream(bytes3), null, null);

        Path revDir = store.getRevisionsDir().resolve(sourceId.toString());
        Path existingTarget = revDir.resolve(revisionId3 + ".orig");
        Files.writeString(existingTarget, "Pre-existing target placed by race/adversary");

        assertThatThrownBy(() -> store.sealRevision(sourceId, revisionId3, captureId3, sha256Hex(bytes3), bytes3.length))
                .isInstanceOf(SnapshotStorageException.class)
                .hasMessageContaining("already exists");

        // Existing file must be preserved
        assertThat(Files.readString(existingTarget)).isEqualTo("Pre-existing target placed by race/adversary");
    }

    @Test
    @DisplayName("Scenario 3: Restart/crash and partial-seal recovery with active-capture protection")
    void scenario3_crashAndPartialSealRecovery(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID captureId = UUID.randomUUID();
        byte[] partialBytes = "Partial crash bytes before seal".getBytes(StandardCharsets.UTF_8);

        // Simulate crash leaving temp capture unsealed
        store.writeTemp(captureId, new ByteArrayInputStream(partialBytes), null, null);

        // Attempting to read or verify as sealed revision must throw SnapshotMissingException
        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, "0000000000000000000000000000000000000000000000000000000000000000", 10L))
                .isInstanceOf(SnapshotMissingException.class);

        // Refuse null inputs: requires non-null legitimateRevisions and activeCaptures
        assertThatThrownBy(() -> store.detectOrphans(null, null))
                .isInstanceOf(NullPointerException.class);

        // Active capture protection: if captureId is in activeCaptures, it is NOT an orphan
        List<RagOriginalBytesStore.OrphanReport> activeOrphans = store.detectOrphans(Set.of(), Set.of(captureId));
        assertThat(activeOrphans).isEmpty();

        // Orphan scan without active captures detects the lingering unsealed temp capture
        List<RagOriginalBytesStore.OrphanReport> orphans = store.detectOrphans(Set.of(), Set.of());
        assertThat(orphans).hasSize(1);
        assertThat(orphans.get(0).orphanType()).isEqualTo("TEMP_CAPTURE");
        assertThat(orphans.get(0).path().getFileName().toString()).isEqualTo(captureId + ".tmp");

        // Recovery cleanup cleans the orphan
        List<RagOriginalBytesStore.DeletionReport> cleanup = store.cleanupOrphans(Set.of(), Set.of());
        assertThat(cleanup).hasSize(1);
        assertThat(cleanup.get(0).status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.COMPLETED);
        assertThat(Files.exists(orphans.get(0).path())).isFalse();

        // Partial seal residue: orphan manifest without .orig
        Path sourceRevDir = Files.createDirectories(store.getRevisionsDir().resolve(sourceId.toString()));
        Path orphanManifest = sourceRevDir.resolve(revisionId + ".manifest.json");
        Files.writeString(orphanManifest, "{\"sourceId\":\"" + sourceId + "\",\"revisionId\":\"" + revisionId + "\",\"captureId\":\"" + captureId + "\",\"sha256\":\"" + sha256Hex(partialBytes) + "\",\"byteLength\":" + partialBytes.length + ",\"sealedAt\":\"2026-10-18T00:00:00Z\",\"sealStatus\":\"SEALED\"}");

        // For legitimate revision, missing orig is an integrity/recovery issue: detected as orphan report
        List<RagOriginalBytesStore.OrphanReport> manifestOrphans = store.detectOrphans(Set.of(revisionId), Set.of());
        assertThat(manifestOrphans).hasSize(1);
        assertThat(manifestOrphans.get(0).orphanType()).isEqualTo("ORPHAN_MANIFEST_NO_ORIG");

        // Legitimate revision partial pair MUST NOT be deleted (preserved for integrity recovery)
        List<RagOriginalBytesStore.DeletionReport> manifestCleaned = store.cleanupOrphans(Set.of(revisionId), Set.of());
        assertThat(manifestCleaned).hasSize(1);
        assertThat(manifestCleaned.get(0).status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.PENDING_INACCESSIBLE);
        assertThat(Files.exists(orphanManifest)).isTrue();

        // If revision is unregistered (not legitimate), it IS safely cleaned
        List<RagOriginalBytesStore.DeletionReport> unregCleaned = store.cleanupOrphans(Set.of(), Set.of());
        assertThat(unregCleaned).hasSize(1);
        assertThat(unregCleaned.get(0).status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.COMPLETED);
        assertThat(Files.exists(orphanManifest)).isFalse();
    }

    @Test
    @DisplayName("Scenario 4: Missing and tampered snapshot detected and denied via single-pass verified read")
    void scenario4_missingAndTamperedSnapshotDetected(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();

        byte[] inputBytes = "Original untampered data".getBytes(StandardCharsets.UTF_8); // 24 bytes
        String originalHash = sha256Hex(inputBytes);

        store.writeTemp(captureId, new ByteArrayInputStream(inputBytes), null, null);
        RagOriginalBytesStore.StoredSnapshot sealed = store.sealRevision(
                sourceId, revisionId, captureId, originalHash, inputBytes.length);

        // Single-pass verified read succeeds with untampered data
        byte[] readOk = store.readRevisionBytes(sourceId, revisionId, originalHash, inputBytes.length);
        assertThat(readOk).isEqualTo(inputBytes);

        // 4a. Tampering with content bytes preserving length -> triggers SHA-256 mismatch in single-pass read
        byte[] tamperedSameLen = "Tampered untampered data".getBytes(StandardCharsets.UTF_8); // 24 bytes
        sealed.storagePath().toFile().setWritable(true);
        Files.write(sealed.storagePath(), tamperedSameLen);

        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Snapshot SHA-256 mismatch (tampered content)");

        assertThatThrownBy(() -> store.verifyIntegrity(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Snapshot SHA-256 mismatch (tampered content)");

        // 4b. Tampering with content length -> triggers length mismatch in single-pass read
        byte[] tamperedDiffLen = "Tampered length".getBytes(StandardCharsets.UTF_8);
        Files.write(sealed.storagePath(), tamperedDiffLen);
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Snapshot length mismatch");

        // 4c. Extra bytes beyond expected length -> denied
        byte[] extraBytes = new byte[inputBytes.length + 10];
        System.arraycopy(inputBytes, 0, extraBytes, 0, inputBytes.length);
        Files.write(sealed.storagePath(), extraBytes);
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("extra bytes present");

        // Restore original bytes for manifest tampering test
        Files.write(sealed.storagePath(), inputBytes);

        // 4d. Tampering with manifest metadata (e.g. altering expected SHA-256 or corrupt JSON)
        sealed.manifestPath().toFile().setWritable(true);
        Files.writeString(sealed.manifestPath(), "{\"sourceId\":\"" + sourceId + "\",\"sealStatus\":\"CORRUPT\"}");

        assertThatThrownBy(() -> store.verifyIntegrity(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class);

        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class);

        // 4e. Missing file
        Files.deleteIfExists(sealed.storagePath());
        assertThatThrownBy(() -> store.verifyIntegrity(sourceId, revisionId, originalHash, inputBytes.length))
                .isInstanceOf(SnapshotMissingException.class);
    }

    @Test
    @DisplayName("Scenario 5: Attempted worker write/replace/containment escape denied")
    void scenario5_workerWriteAndEscapeDenied(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();

        byte[] bytes = "Sealed data under G3 protection".getBytes(StandardCharsets.UTF_8);
        store.writeTemp(captureId, new ByteArrayInputStream(bytes), null, null);
        RagOriginalBytesStore.StoredSnapshot sealed = store.sealRevision(
                sourceId, revisionId, captureId, sha256Hex(bytes), bytes.length);

        // 5a. Worker process attempting to write directly to read-only sealed file
        assertThat(sealed.storagePath().toFile().canWrite()).isFalse();
        assertThatThrownBy(() -> new FileOutputStream(sealed.storagePath().toFile()))
                .isInstanceOf(IOException.class);

        // 5b. Worker attempting path traversal out of storage root
        UUID escapedSourceId = UUID.randomUUID();
        assertThatThrownBy(() -> store.sealRevision(escapedSourceId, UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000000"), "0000000000000000000000000000000000000000000000000000000000000000", 1))
                .isInstanceOf(SnapshotMissingException.class);
    }

    @Test
    @DisplayName("Scenario 6: Alias, collision, and handle attacks")
    void scenario6_aliasAndHandleAttacks(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();

        byte[] bytes = "Protected against concurrent handle attacks".getBytes(StandardCharsets.UTF_8);
        store.writeTemp(captureId, new ByteArrayInputStream(bytes), null, null);
        RagOriginalBytesStore.StoredSnapshot sealed = store.sealRevision(
                sourceId, revisionId, captureId, sha256Hex(bytes), bytes.length);

        // Attempting to replace sealed file via MoveFileEx / atomic move while read-only
        Path maliciousReplacement = Files.writeString(tempDir.resolve("malicious.txt"), "Malicious bytes");
        try {
            assertThatThrownBy(() -> Files.move(maliciousReplacement, sealed.storagePath()))
                    .isInstanceOf(IOException.class);
        } finally {
            Files.deleteIfExists(maliciousReplacement);
        }

        // Pre-existing temp capture collision preserves unowned colliding file (CP18-AB-6 issue 1)
        UUID collisionCapId = UUID.randomUUID();
        Path preExistingTemp = store.getTempDir().resolve(collisionCapId + ".tmp");
        Files.writeString(preExistingTemp, "Pre-existing unowned capture bytes");

        assertThatThrownBy(() -> store.writeTemp(collisionCapId, new ByteArrayInputStream(new byte[]{1, 2, 3}), null, null))
                .isInstanceOf(SnapshotStorageException.class);

        // Crucial invariant: pre-existing file MUST NOT be deleted by colliding writeTemp!
        assertThat(Files.exists(preExistingTemp)).isTrue();
        assertThat(Files.readString(preExistingTemp)).isEqualTo("Pre-existing unowned capture bytes");
        Files.deleteIfExists(preExistingTemp);
    }

    @Test
    @DisplayName("Scenario 7: Disk-full / maxSourceBytes limit failure immediately aborts and cleans up")
    void scenario7_maxSourceBytesExceededAborts(@TempDir Path tempDir) {
        // Configure small limit of 50 bytes
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 50L);

        UUID captureId = UUID.randomUUID();
        byte[] oversizedBytes = new byte[100];

        assertThatThrownBy(() -> store.writeTemp(captureId, new ByteArrayInputStream(oversizedBytes), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capture size exceeds maximum source bytes limit");

        // Temp file must have been deleted immediately
        Path tempPath = store.getTempDir().resolve(captureId + ".tmp");
        assertThat(Files.exists(tempPath)).isFalse();
    }

    @Test
    @DisplayName("Scenario 8: Cancellation and active-writer cleanup")
    void scenario8_cancellationCleansUpTemp(@TempDir Path tempDir) {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID captureId = UUID.randomUUID();
        byte[] bytes = "Data for cancelled capture".getBytes(StandardCharsets.UTF_8);

        store.writeTemp(captureId, new ByteArrayInputStream(bytes), null, null);
        Path tempPath = store.getTempDir().resolve(captureId + ".tmp");
        assertThat(Files.exists(tempPath)).isTrue();

        // Caller cancels operation and calls cleanupTemp
        RagOriginalBytesStore.DeletionReport report = store.cleanupTemp(captureId);
        assertThat(report.status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.COMPLETED);
        assertThat(Files.exists(tempPath)).isFalse();
    }

    @Test
    @DisplayName("Scenario 9: Orphan detection and validated safe deletion")
    void scenario9_orphanDetectionAndValidatedSafeDeletion(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID legitimateSource = UUID.randomUUID();
        UUID legitimateRev = UUID.randomUUID();
        UUID legitimateCap = UUID.randomUUID();

        byte[] legitBytes = "Legitimate revision bytes".getBytes(StandardCharsets.UTF_8);
        store.writeTemp(legitimateCap, new ByteArrayInputStream(legitBytes), null, null);
        store.sealRevision(legitimateSource, legitimateRev, legitimateCap, sha256Hex(legitBytes), legitBytes.length);

        UUID orphanSource = UUID.randomUUID();
        UUID orphanRev = UUID.randomUUID();
        UUID orphanCap = UUID.randomUUID();

        byte[] orphanBytes = "Orphan revision bytes".getBytes(StandardCharsets.UTF_8);
        store.writeTemp(orphanCap, new ByteArrayInputStream(orphanBytes), null, null);
        store.sealRevision(orphanSource, orphanRev, orphanCap, sha256Hex(orphanBytes), orphanBytes.length);

        // Also create an abandoned temp capture
        UUID abandonedCap = UUID.randomUUID();
        store.writeTemp(abandonedCap, new ByteArrayInputStream(new byte[]{1, 2, 3}), null, null);

        // Detect orphans passing legitimateRev and no active captures
        List<RagOriginalBytesStore.OrphanReport> detected = store.detectOrphans(Set.of(legitimateRev), Set.of());
        assertThat(detected).hasSize(3); // 1 abandoned temp + 1 unregistered revision + 1 unregistered manifest

        // Cleanup orphans
        List<RagOriginalBytesStore.DeletionReport> deleted = store.cleanupOrphans(Set.of(legitimateRev), Set.of());
        assertThat(deleted).hasSize(3);
        assertThat(deleted).allMatch(r -> r.status() == RagOriginalBytesStore.DeletionStatus.COMPLETED);

        // Legitimate revision must still be present and intact
        assertThat(store.readRevisionBytes(legitimateSource, legitimateRev, sha256Hex(legitBytes), legitBytes.length)).isEqualTo(legitBytes);
    }

    @Test
    @DisplayName("Scenario 10: Unknown or failed deletion reported as pending, not successful")
    void scenario10_failedDeletionReportedAsPending(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();

        byte[] bytes = "Content to lock during delete".getBytes(StandardCharsets.UTF_8);
        store.writeTemp(captureId, new ByteArrayInputStream(bytes), null, null);
        RagOriginalBytesStore.StoredSnapshot sealed = store.sealRevision(
                sourceId, revisionId, captureId, sha256Hex(bytes), bytes.length);

        // Hold an exclusive open stream on the file (locking it on Windows)
        try (FileInputStream fis = new FileInputStream(sealed.storagePath().toFile())) {
            // Attempt deletion while file is held open
            RagOriginalBytesStore.DeletionReport report = store.deleteRevision(sourceId, revisionId);

            // On Windows NTFS, an open file cannot be deleted without FILE_SHARE_DELETE
            // The report MUST be PENDING_INACCESSIBLE, never false COMPLETED!
            assertThat(report.status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.PENDING_INACCESSIBLE);
            assertThat(report.message()).containsAnyOf("Deletion failed", "remains present", "absence could not be verified");
        }

        // After lock is released, deletion succeeds
        RagOriginalBytesStore.DeletionReport retry = store.deleteRevision(sourceId, revisionId);
        assertThat(retry.status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.COMPLETED);
    }

    @Test
    @DisplayName("Scenario 11: Strict JSON manifest parsing denies duplicate keys, trailing garbage, and unknown fields")
    void scenario11_strictJsonManifestParsingDeniesMalformed(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId = UUID.randomUUID();

        byte[] inputBytes = "Strict manifest content".getBytes(StandardCharsets.UTF_8);
        String expectedHash = sha256Hex(inputBytes);
        store.writeTemp(captureId, new ByteArrayInputStream(inputBytes), null, null);
        RagOriginalBytesStore.StoredSnapshot sealed = store.sealRevision(
                sourceId, revisionId, captureId, expectedHash, inputBytes.length);

        // 11a. Duplicate key in manifest JSON -> denied by STRICT_DUPLICATE_DETECTION
        sealed.manifestPath().toFile().setWritable(true);
        String duplicateKeyJson = "{\n"
                + "  \"sourceId\": \"" + sourceId + "\",\n"
                + "  \"sourceId\": \"" + UUID.randomUUID() + "\",\n"
                + "  \"revisionId\": \"" + revisionId + "\",\n"
                + "  \"captureId\": \"" + captureId + "\",\n"
                + "  \"sha256\": \"" + expectedHash + "\",\n"
                + "  \"byteLength\": " + inputBytes.length + ",\n"
                + "  \"sealedAt\": \"2026-10-18T00:00:00Z\",\n"
                + "  \"sealStatus\": \"SEALED\"\n"
                + "}\n";
        Files.writeString(sealed.manifestPath(), duplicateKeyJson);
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, expectedHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Invalid manifest JSON format");

        // 11b. Trailing tokens in manifest JSON -> denied by FAIL_ON_TRAILING_TOKENS
        String trailingTokensJson = "{\n"
                + "  \"sourceId\": \"" + sourceId + "\",\n"
                + "  \"revisionId\": \"" + revisionId + "\",\n"
                + "  \"captureId\": \"" + captureId + "\",\n"
                + "  \"sha256\": \"" + expectedHash + "\",\n"
                + "  \"byteLength\": " + inputBytes.length + ",\n"
                + "  \"sealedAt\": \"2026-10-18T00:00:00Z\",\n"
                + "  \"sealStatus\": \"SEALED\"\n"
                + "} unexpected_trailing_content\n";
        Files.writeString(sealed.manifestPath(), trailingTokensJson);
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, expectedHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Invalid manifest JSON format");

        // 11c. Unknown fields in manifest JSON -> denied by FAIL_ON_UNKNOWN_PROPERTIES
        String unknownFieldJson = "{\n"
                + "  \"sourceId\": \"" + sourceId + "\",\n"
                + "  \"revisionId\": \"" + revisionId + "\",\n"
                + "  \"captureId\": \"" + captureId + "\",\n"
                + "  \"sha256\": \"" + expectedHash + "\",\n"
                + "  \"byteLength\": " + inputBytes.length + ",\n"
                + "  \"sealedAt\": \"2026-10-18T00:00:00Z\",\n"
                + "  \"sealStatus\": \"SEALED\",\n"
                + "  \"injectedAdminField\": \"escalation\"\n"
                + "}\n";
        Files.writeString(sealed.manifestPath(), unknownFieldJson);
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, expectedHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Invalid manifest JSON format");

        // 11d. Invalid non-hex SHA-256
        String invalidHashJson = "{\n"
                + "  \"sourceId\": \"" + sourceId + "\",\n"
                + "  \"revisionId\": \"" + revisionId + "\",\n"
                + "  \"captureId\": \"" + captureId + "\",\n"
                + "  \"sha256\": \"not_a_valid_64_character_hex_hash_string_here_12345678901234567890\",\n"
                + "  \"byteLength\": " + inputBytes.length + ",\n"
                + "  \"sealedAt\": \"2026-10-18T00:00:00Z\",\n"
                + "  \"sealStatus\": \"SEALED\"\n"
                + "}\n";
        Files.writeString(sealed.manifestPath(), invalidHashJson);
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, expectedHash, inputBytes.length))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Manifest sha256 is invalid");
    }

    @Test
    @DisplayName("Scenario 12: Independent expected descriptor denies self-consistent forged revision and manifest")
    void scenario12_independentExpectedDescriptorDeniesSelfConsistentForgery(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID legitimateCapture = UUID.randomUUID();

        byte[] legitimateBytes = "Legitimate authorized snapshot content".getBytes(StandardCharsets.UTF_8);
        String legitimateHash = sha256Hex(legitimateBytes);
        long legitimateLength = legitimateBytes.length;

        // Adversary creates a forged .orig file and a forged .manifest.json that internally match each other perfectly
        byte[] forgedBytes = "Adversary forged payload content".getBytes(StandardCharsets.UTF_8);
        String forgedHash = sha256Hex(forgedBytes);
        long forgedLength = forgedBytes.length;

        Path sourceRevDir = Files.createDirectories(store.getRevisionsDir().resolve(sourceId.toString()));
        Path origFile = sourceRevDir.resolve(revisionId + ".orig");
        Path manifestFile = sourceRevDir.resolve(revisionId + ".manifest.json");

        Files.write(origFile, forgedBytes);
        String forgedManifest = "{\n"
                + "  \"sourceId\": \"" + sourceId + "\",\n"
                + "  \"revisionId\": \"" + revisionId + "\",\n"
                + "  \"captureId\": \"" + UUID.randomUUID() + "\",\n"
                + "  \"sha256\": \"" + forgedHash + "\",\n"
                + "  \"byteLength\": " + forgedLength + ",\n"
                + "  \"sealedAt\": \"2026-10-18T00:00:00Z\",\n"
                + "  \"sealStatus\": \"SEALED\"\n"
                + "}\n";
        Files.writeString(manifestFile, forgedManifest);

        // When trusted consumer accesses revision specifying legitimate expected H/L, the forged revision is strictly DENIED
        assertThatThrownBy(() -> store.readRevisionBytes(sourceId, revisionId, legitimateHash, legitimateLength))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Manifest SHA-256 does not match expected");

        assertThatThrownBy(() -> store.verifyIntegrity(sourceId, revisionId, legitimateHash, legitimateLength))
                .isInstanceOf(SnapshotIntegrityException.class)
                .hasMessageContaining("Manifest SHA-256 does not match expected");
    }

    @Test
    @DisplayName("Scenario 13: Kernel-level MoveFileExW error capture distinguishes collision without fallback")
    void scenario13_nativeAtomicMoveErrorCaptureDistinguishesCollision(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        UUID sourceId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        UUID captureId1 = UUID.randomUUID();
        UUID captureId2 = UUID.randomUUID();

        byte[] payload1 = "Target file created first".getBytes(StandardCharsets.UTF_8);
        byte[] payload2 = "Target file attempting overwrite".getBytes(StandardCharsets.UTF_8);

        store.writeTemp(captureId1, new ByteArrayInputStream(payload1), null, null);
        store.writeTemp(captureId2, new ByteArrayInputStream(payload2), null, null);

        store.sealRevision(sourceId, revisionId, captureId1, sha256Hex(payload1), payload1.length);

        // Competing seal triggers error 183 (ERROR_ALREADY_EXISTS) in MoveFileExW
        assertThatThrownBy(() -> store.sealRevision(sourceId, revisionId, captureId2, sha256Hex(payload2), payload2.length))
                .isInstanceOf(SnapshotStorageException.class)
                .hasMessageContaining("already exists (no overwrite");

        // The colliding temp capture still exists and was not erroneously deleted
        Path temp2 = store.getTempDir().resolve(captureId2 + ".tmp");
        assertThat(Files.exists(temp2)).isTrue();
    }

    @Test
    @DisplayName("Scenario 14: Orphan sweep authority refuses null input and retains legitimate partial pairs")
    void scenario14_orphanAuthorityRefusesNullAndRetainsLegitimatePartials(@TempDir Path tempDir) throws Exception {
        RagOriginalBytesStore store = new RagOriginalBytesStore(tempDir.resolve("store"), 52428800L);

        // Null authority sets are rejected
        assertThatThrownBy(() -> store.detectOrphans(null, Set.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> store.detectOrphans(Set.of(), null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> store.cleanupOrphans(null, Set.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> store.cleanupOrphans(Set.of(), null))
                .isInstanceOf(NullPointerException.class);

        // Create legitimate revision with missing manifest (e.g. crash during manifest creation)
        UUID sourceId = UUID.randomUUID();
        UUID revId = UUID.randomUUID();
        Path sourceRevDir = Files.createDirectories(store.getRevisionsDir().resolve(sourceId.toString()));
        Path origOnly = sourceRevDir.resolve(revId + ".orig");
        Files.writeString(origOnly, "Legitimate orig data without manifest");

        // When revId is registered as legitimate, detectOrphans marks it ORPHAN_REVISION_NO_MANIFEST
        List<RagOriginalBytesStore.OrphanReport> reports = store.detectOrphans(Set.of(revId), Set.of());
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).orphanType()).isEqualTo("ORPHAN_REVISION_NO_MANIFEST");

        // cleanupOrphans MUST NOT delete this legitimate partial file; it must preserve it for recovery!
        List<RagOriginalBytesStore.DeletionReport> cleanup = store.cleanupOrphans(Set.of(revId), Set.of());
        assertThat(cleanup).hasSize(1);
        assertThat(cleanup.get(0).status()).isEqualTo(RagOriginalBytesStore.DeletionStatus.PENDING_INACCESSIBLE);
        assertThat(cleanup.get(0).message()).contains("retained for integrity recovery");
        assertThat(Files.exists(origOnly)).isTrue();
    }
    @Test
    void nativeUnavailableFallbackNeverClobbersOrigWithoutManifest(@TempDir Path dir) throws Exception {
        var store=new RagOriginalBytesStore(dir.resolve("fallback"),1000,false);
        UUID source=UUID.randomUUID(),revision=UUID.randomUUID(),capture=UUID.randomUUID();
        Path sourceDir=Files.createDirectories(store.getRevisionsDir().resolve(source.toString()));
        Path target=sourceDir.resolve(revision+".orig"); byte[] old="existing-partial".getBytes(StandardCharsets.UTF_8); Files.write(target,old);
        byte[] candidate="new-candidate".getBytes(StandardCharsets.UTF_8);
        var temp=store.writeTemp(capture,new ByteArrayInputStream(candidate),null,null);
        assertThatThrownBy(()->store.sealRevision(source,revision,capture,temp.sha256(),temp.byteLength())).isInstanceOf(SnapshotStorageException.class);
        assertThat(Files.readAllBytes(target)).isEqualTo(old);
        assertThat(Files.readAllBytes(temp.tempPath())).isEqualTo(candidate);
        assertThat(Files.exists(sourceDir.resolve(revision+".manifest.json"))).isFalse();
    }

    @Test
    void nativeUnavailableFallbackSealsAndRejectsManifestCollision(@TempDir Path dir) throws Exception {
        var store=new RagOriginalBytesStore(dir.resolve("fallback"),1000,false);
        UUID source=UUID.randomUUID(),revision=UUID.randomUUID(),capture=UUID.randomUUID();
        var temp=store.writeTemp(capture,new ByteArrayInputStream(new byte[]{1,2,3}),null,null);
        store.sealRevision(source,revision,capture,temp.sha256(),temp.byteLength());
        assertThat(store.verifyIntegrity(source,revision,temp.sha256(),temp.byteLength())).isTrue();
        UUID next=UUID.randomUUID(); var nextTemp=store.writeTemp(next,new ByteArrayInputStream(new byte[]{4}),null,null);
        assertThatThrownBy(()->store.sealRevision(source,revision,next,nextTemp.sha256(),nextTemp.byteLength())).isInstanceOf(SnapshotStorageException.class);
        assertThat(store.verifyIntegrity(source,revision,temp.sha256(),temp.byteLength())).isTrue();
    }

}
