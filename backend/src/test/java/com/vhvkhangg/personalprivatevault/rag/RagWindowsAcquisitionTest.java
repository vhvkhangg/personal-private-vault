package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RagWindowsAcquisitionTest {

    private final WindowsNativeFileGuard guard = new WindowsNativeFileGuard(52428800L);

    @Test
    @DisplayName("Scenario 1: Correct selected object and valid protected capture")
    void scenario1_correctSelectedObjectAndValidProtectedCapture(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("allowed_root"));
        Path subDir = Files.createDirectories(allowedRoot.resolve("nested/dir"));
        Path targetFile = Files.writeString(subDir.resolve("valid-file.txt"), "Authentic snapshot content 2026-10-18");

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String expectedHash = HexFormat.of().formatHex(md.digest("Authentic snapshot content 2026-10-18".getBytes(StandardCharsets.UTF_8)));

        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            assertThat(acquired).isNotNull();
            assertThat(acquired.allowedRoot()).isEqualTo(allowedRoot.toRealPath());
            assertThat(acquired.targetPath()).isEqualTo(targetFile.toRealPath());
            assertThat(acquired.contentHash()).isEqualTo(expectedHash);
            assertThat(acquired.getContentAsUtf8()).isEqualTo("Authentic snapshot content 2026-10-18");
            assertThat(acquired.byteLength()).isEqualTo("Authentic snapshot content 2026-10-18".getBytes(StandardCharsets.UTF_8).length);
            assertThat(acquired.rootIdentity()).isNotNull();
            assertThat(acquired.fileIdentity()).isNotNull();
            assertThat(acquired.ancestorChain()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Scenario 2: Wrong root or wrong ancestor denied")
    void scenario2_wrongRootOrWrongAncestorDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path root1 = Files.createDirectories(tempDir.resolve("root1"));
        Path root2 = Files.createDirectories(tempDir.resolve("root2"));
        Path fileInRoot1 = Files.writeString(root1.resolve("file.txt"), "Root 1 content");

        // Attempting to acquire fileInRoot1 with root2 as allowed root must be denied (containment violation)
        assertThatThrownBy(() -> guard.acquireProtectedSource(root2, fileInRoot1, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target path must be strictly contained within allowed root");

        // Attempting to acquire with mismatched expected root identity must be denied
        WindowsNativeFileGuard.DirectoryIdentity root2Identity = guard.queryDirectoryIdentity(root2);
        assertThatThrownBy(() -> guard.acquireProtectedSource(root1, fileInRoot1, null, root2Identity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Root directory identity mismatch: wrong root");
    }

    @Test
    @DisplayName("Scenario 3: Same-path replacement denied by expected file identity")
    void scenario3_samePathReplacementDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path targetFile = Files.writeString(allowedRoot.resolve("replaced.txt"), "Original version");

        WindowsNativeFileGuard.SourceIdentity originalIdentity;
        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            originalIdentity = acquired.fileIdentity();
        }

        // Simulate adversary replacing file at the exact same path
        Files.delete(targetFile);
        Files.writeString(targetFile, "Substituted version at same path");

        // Acquire with expected identity of original file -> must be denied!
        assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, originalIdentity, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File object identity mismatch");
    }

    @Test
    @DisplayName("Scenario 4: Wrong object with identical content denied")
    void scenario4_wrongObjectWithIdenticalContentDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        String identicalContent = "Identical byte sequence across different objects";
        Path fileA = Files.writeString(allowedRoot.resolve("fileA.txt"), identicalContent);
        Path fileB = Files.writeString(allowedRoot.resolve("fileB.txt"), identicalContent);

        WindowsNativeFileGuard.SourceIdentity identityA;
        try (WindowsNativeFileGuard.AcquiredSource acquiredA = guard.acquireProtectedSource(allowedRoot, fileA, null, null)) {
            identityA = acquiredA.fileIdentity();
        }

        // Trying to acquire fileB while expecting identityA must be denied even though content bytes are identical!
        assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, fileB, identityA, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File object identity mismatch");
    }

    @Test
    @DisplayName("Scenario 5: Reparse point or junction manipulation in ancestor chain denied")
    void scenario5_reparseJunctionManipulationDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path targetDir = Files.createDirectories(tempDir.resolve("real_target"));
        Path targetFile = Files.writeString(targetDir.resolve("payload.txt"), "Junction content");

        Path junctionParent = Files.createDirectories(tempDir.resolve("junction_parent"));
        Path junctionDir = junctionParent.resolve("junction_dir");

        Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J", junctionDir.toString(), targetDir.toString())
                .redirectErrorStream(true)
                .start();
        int exit = mklink.waitFor();
        assumeTrue(exit == 0 && Files.exists(junctionDir), "mklink /J must succeed to exercise junction test");

        Path fileThroughJunction = junctionDir.resolve("payload.txt");
        assertThatThrownBy(() -> guard.acquireProtectedSource(junctionParent, fileThroughJunction, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reparse point or junction detected");
    }

    @Test
    @DisplayName("Scenario 6: Hard-link creation/presence denied (link count != 1)")
    void scenario6_hardLinkPresenceDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path targetFile = Files.writeString(allowedRoot.resolve("linked.txt"), "Content with hard link");
        Path aliasFile = Files.createLink(allowedRoot.resolve("alias.txt"), targetFile);

        try {
            assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Unsupported NTFS hard-link count: 2; exactly one link is required");
        } finally {
            Files.deleteIfExists(aliasFile);
        }

        // After removing alias, link count is 1 -> acquisition succeeds
        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            assertThat(acquired.getContentAsUtf8()).isEqualTo("Content with hard link");
        }
    }

    @Test
    @DisplayName("Scenario 7: Existing and concurrent writers/mappings denied")
    void scenario7_existingAndConcurrentWritersDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path targetFile = Files.writeString(allowedRoot.resolve("concurrent.txt"), "Initial content");

        // 7a. Existing writer holds lock -> acquisition must be denied
        try (FileOutputStream fos = new FileOutputStream(targetFile.toFile(), true)) {
            assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("File is already locked or open by a concurrent writer");
        }

        // 7b. Existing writable memory mapping -> acquisition must be denied
        try (Arena arena = Arena.ofConfined();
             FileChannel channel = FileChannel.open(targetFile, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            channel.map(FileChannel.MapMode.READ_WRITE, 0, Files.size(targetFile), arena);
            assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("File is already locked or open by a concurrent writer");
        }

        // 7c. While acquisition handle is held -> concurrent writer must be denied
        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            assertThatThrownBy(() -> Files.writeString(targetFile, "Unauthorized mutation while handle is open"))
                    .isInstanceOf(IOException.class);
        }
    }

    @Test
    @DisplayName("Scenario 8: Attempted changes and ancestor renames during acquisition blocked by Windows NTFS")
    void scenario8_attemptedChangesDuringAcquisitionBlocked(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path subDir = Files.createDirectories(allowedRoot.resolve("nested_dir"));
        Path targetFile = Files.writeString(subDir.resolve("protected.txt"), "Immutable while held");

        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            // Target file delete blocked
            assertThatThrownBy(() -> Files.delete(targetFile))
                    .isInstanceOf(IOException.class);

            // Target file move / rename blocked
            assertThatThrownBy(() -> Files.move(targetFile, subDir.resolve("renamed.txt")))
                    .isInstanceOf(IOException.class);

            // Target file replace blocked
            Path replacement = Files.writeString(subDir.resolve("replacement.txt"), "Replacement bytes");
            try {
                assertThatThrownBy(() -> Files.move(replacement, targetFile, StandardCopyOption.REPLACE_EXISTING))
                        .isInstanceOf(IOException.class);
            } finally {
                Files.deleteIfExists(replacement);
            }

            // Ancestor directory rename and delete blocked by NTFS kernel (sharing violation on held directory handle)
            assertThatThrownBy(() -> Files.move(subDir, allowedRoot.resolve("renamed_nested_dir")))
                    .isInstanceOf(IOException.class);
            assertThatThrownBy(() -> Files.delete(subDir))
                    .isInstanceOf(IOException.class);
        }

        // Positive control: After acquisition handle is closed, ancestor directory rename succeeds
        Path renamedSubDir = allowedRoot.resolve("renamed_nested_dir");
        Files.move(subDir, renamedSubDir);
        assertThat(Files.exists(renamedSubDir.resolve("protected.txt"))).isTrue();
    }

    @Test
    @DisplayName("Scenario 9: Cancellation during read cleans up native handle cleanly")
    void scenario9_cancellationCleansUpHandleCleanly(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path targetFile = Files.writeString(allowedRoot.resolve("cancellable.txt"), "Content to cancel");

        AtomicBoolean cancelFlag = new AtomicBoolean(true);

        assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, null, null, cancelFlag::get))
                .isInstanceOf(CancellationException.class)
                .hasMessageContaining("Source acquisition cancelled during read");

        // After cancelled acquisition, the file must be immediately writable (no leaked native handle!)
        Files.writeString(targetFile, "Successfully written after cancellation cleanup");
        assertThat(Files.readString(targetFile)).isEqualTo("Successfully written after cancellation cleanup");
    }

    @Test
    @DisplayName("Scenario 10: Original file bytes and permissions unchanged")
    void scenario10_originalBytesAndPermissionsUnchanged(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        byte[] originalBytes = "Original unmodified bytes for capture 2026-10-18".getBytes(StandardCharsets.UTF_8);
        Path targetFile = Files.write(allowedRoot.resolve("unmodified.txt"), originalBytes);

        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            assertThat(acquired.contentBytes()).isEqualTo(originalBytes);
        }

        // Verify bytes on disk are bit-for-bit identical
        assertThat(Files.readAllBytes(targetFile)).isEqualTo(originalBytes);
        // Verify file is still writable by owner
        assertThat(targetFile.toFile().canWrite()).isTrue();
    }

    @Test
    @DisplayName("Scenario 11: Pre-open swap of ancestor component denied by expected ancestor chain")
    void scenario11_preOpenSwapChildComponentDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path subDir = Files.createDirectories(allowedRoot.resolve("sub"));
        Path targetFile = Files.writeString(subDir.resolve("target.txt"), "Target payload bytes");

        WindowsNativeFileGuard.DirectoryIdentity expectedRoot = guard.queryDirectoryIdentity(allowedRoot);
        WindowsNativeFileGuard.DirectoryIdentity expectedSub = guard.queryDirectoryIdentity(subDir);
        var expectedChain = java.util.List.of(expectedRoot, expectedSub);

        // Simulate pre-open swap: adversary replaces 'sub' directory with a newly created directory at same name
        Path swapDir = Files.createDirectories(allowedRoot.resolve("sub_temp"));
        Files.writeString(swapDir.resolve("target.txt"), "Target payload bytes");
        Files.delete(targetFile);
        Files.delete(subDir);
        Files.move(swapDir, subDir);

        // Acquisition with expected ancestor chain must detect the swapped ancestor object and deny!
        assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, null, expectedRoot, expectedChain, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ancestor directory identity mismatch at component 1");
    }

    @Test
    @DisplayName("Scenario 12: Pre-open ABA swap with same bytes denied by expected file identity")
    void scenario12_preOpenAbaSwapDeniedByExpectedIdentity(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path allowedRoot = Files.createDirectories(tempDir.resolve("root"));
        Path targetFile = Files.writeString(allowedRoot.resolve("aba.txt"), "Exact same payload bytes across swap");

        WindowsNativeFileGuard.SourceIdentity originalIdentity;
        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(allowedRoot, targetFile, null, null)) {
            originalIdentity = acquired.fileIdentity();
        }

        // Adversary deletes original and replaces with fresh file having identical bytes
        Files.delete(targetFile);
        Files.writeString(targetFile, "Exact same payload bytes across swap");

        // Acquisition expecting original identity must deny even though content is identical
        assertThatThrownBy(() -> guard.acquireProtectedSource(allowedRoot, targetFile, originalIdentity, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File object identity mismatch");
    }

    @Test
    @DisplayName("Scenario 13: Reparse alias during component resolution detected and denied by NtCreateFile")
    void scenario13_reparseAliasDuringComponentResolutionDenied(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path realDir = Files.createDirectories(tempDir.resolve("real_source"));
        Path realFile = Files.writeString(realDir.resolve("data.txt"), "Data behind junction");

        Path rootDir = Files.createDirectories(tempDir.resolve("root_junction"));
        Path junctionPath = rootDir.resolve("junction_sub");

        Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J", junctionPath.toString(), realDir.toString())
                .redirectErrorStream(true)
                .start();
        int exit = mklink.waitFor();
        assumeTrue(exit == 0 && Files.exists(junctionPath), "mklink /J must succeed to exercise junction test");

        Path targetThroughJunction = junctionPath.resolve("data.txt");
        assertThatThrownBy(() -> guard.acquireProtectedSource(rootDir, targetThroughJunction, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reparse point or junction detected");
    }

    @Test
    @DisplayName("Scenario 14: Held ancestor directory handles block deletion and renaming across entire hierarchy")
    void scenario14_heldAncestorDirectoryBlocksDeleteAndRename(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Applies only to Windows NTFS");

        Path rootDir = Files.createDirectories(tempDir.resolve("root"));
        Path level1 = Files.createDirectories(rootDir.resolve("level1"));
        Path level2 = Files.createDirectories(level1.resolve("level2"));
        Path targetFile = Files.writeString(level2.resolve("document.txt"), "Guarded content");

        try (WindowsNativeFileGuard.AcquiredSource acquired = guard.acquireProtectedSource(rootDir, targetFile, null, null)) {
            // Attempt to rename level2 while handle is held -> must fail with IOException (sharing violation)
            assertThatThrownBy(() -> Files.move(level2, level1.resolve("level2_renamed")))
                    .isInstanceOf(IOException.class);

            // Attempt to delete level2 while handle is held -> must fail with IOException
            assertThatThrownBy(() -> Files.delete(level2))
                    .isInstanceOf(IOException.class);

            // Attempt to rename level1 while handle is held -> must fail with IOException
            assertThatThrownBy(() -> Files.move(level1, rootDir.resolve("level1_renamed")))
                    .isInstanceOf(IOException.class);

            // Attempt to delete level1 while handle is held -> must fail with IOException
            assertThatThrownBy(() -> Files.delete(level1))
                    .isInstanceOf(IOException.class);
        }

        // Positive control: Once acquisition is closed, directories can be renamed cleanly
        Path renamedLevel1 = rootDir.resolve("level1_renamed");
        Files.move(level1, renamedLevel1);
        assertThat(Files.exists(renamedLevel1.resolve("level2/document.txt"))).isTrue();
    }
}
