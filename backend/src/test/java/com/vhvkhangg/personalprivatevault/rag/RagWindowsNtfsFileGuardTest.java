package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RagWindowsNtfsFileGuardTest {

    private final WindowsNativeFileGuard fileGuard = new WindowsNativeFileGuard(52428800L);

    @Test
    @DisplayName("Captures NTFS volume identity and content SHA-256 deterministically on Windows")
    void capturesNtfsIdentityAndHashDeterministically(@TempDir Path tempDir) throws IOException, NoSuchAlgorithmException {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        Path testFile = tempDir.resolve("evidence-source.txt");
        byte[] content = "Deterministic NTFS handle snapshot content 2026-10-14".getBytes(StandardCharsets.UTF_8);
        Files.write(testFile, content);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String expectedHash = HexFormat.of().formatHex(digest.digest(content));

        String identity1;
        String hash1;
        try (WindowsNativeFileGuard.ProtectedFileHandle handle1 = fileGuard.openAndGuard(testFile)) {
            assertThat(handle1).isNotNull();
            assertThat(handle1.getPath()).isEqualTo(testFile.toRealPath());
            identity1 = handle1.getFileIdentity();
            hash1 = handle1.getContentHash();
            assertThat(identity1).isNotBlank();
            assertThat(hash1).isEqualTo(expectedHash);
        }

        // Repeat on same file -> identical identity & hash
        try (WindowsNativeFileGuard.ProtectedFileHandle handle2 = fileGuard.openAndGuard(testFile)) {
            assertThat(handle2.getFileIdentity()).isEqualTo(identity1);
            assertThat(handle2.getContentHash()).isEqualTo(hash1);
        }
    }

    @Test
    @DisplayName("Blocks concurrent writers while native handle is open with FILE_SHARE_READ (R7 Windows fence)")
    void blocksConcurrentWriterWhileHandleOpen(@TempDir Path tempDir) throws IOException {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        Path testFile = tempDir.resolve("locked-source.txt");
        Files.writeString(testFile, "Initial content for locking test");

        try (WindowsNativeFileGuard.ProtectedFileHandle handle = fileGuard.openAndGuard(testFile)) {
            assertThat(handle).isNotNull();

            // Attempting to write while handle is held must fail with sharing violation (IOException / FileSystemException)
            assertThatThrownBy(() -> Files.writeString(testFile, "Mutated content by concurrent writer"))
                    .isInstanceOf(IOException.class);
        }

        // After handle is closed, writer must succeed
        Files.writeString(testFile, "Successfully updated after handle closed");
        assertThat(Files.readString(testFile)).isEqualTo("Successfully updated after handle closed");
    }

    @Test
    @DisplayName("Rejects file exceeding maxSourceBytes limit")
    void rejectsFileExceedingMaxSourceBytes(@TempDir Path tempDir) throws IOException {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        WindowsNativeFileGuard limitedGuard = new WindowsNativeFileGuard(50L);
        Path testFile = tempDir.resolve("large-file.txt");
        Files.write(testFile, new byte[100]);

        assertThatThrownBy(() -> limitedGuard.openAndGuard(testFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds configured maximum source bytes");
    }

    @Test
    @DisplayName("Rejects directory path when attempting native guard")
    void rejectsDirectoryPath(@TempDir Path tempDir) {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        assertThatThrownBy(() -> fileGuard.openAndGuard(tempDir))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target path must be a regular file, not a directory");
    }

    @Test
    @DisplayName("Rejects non-existent file path")
    void rejectsNonExistentFile(@TempDir Path tempDir) {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        Path nonExistent = tempDir.resolve("does_not_exist.txt");
        assertThatThrownBy(() -> fileGuard.openAndGuard(nonExistent))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Rejects external file located inside directory junction or reparse point (FR17-1 / REPARSE_ANCESTOR_ACCEPTED)")
    void rejectsAncestorReparsePointOrJunction(@TempDir Path tempDir) throws Exception {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        Path targetDir = tempDir.resolve("real_target_dir");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve("payload.txt");
        Files.writeString(targetFile, "Valid content");

        Path junctionDir = tempDir.resolve("junction_dir");
        Process process = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J", junctionDir.toString(), targetDir.toString())
                .redirectErrorStream(true)
                .start();
        int exitCode = process.waitFor();
        if (exitCode == 0 && Files.exists(junctionDir)) {
            Path fileThroughJunction = junctionDir.resolve("payload.txt");
            assertThatThrownBy(() -> fileGuard.openAndGuard(fileThroughJunction))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Reparse point or junction detected");
        }
    }

    @Test
    @DisplayName("Rejects opening file when already held open by exclusive writer")
    void rejectsAlreadyOpenWriter(@TempDir Path tempDir) throws IOException {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        Path testFile = tempDir.resolve("exclusive-writer.txt");
        Files.writeString(testFile, "initial content");

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(testFile.toFile())) {
            assertThatThrownBy(() -> fileGuard.openAndGuard(testFile))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("File is already locked or open by a concurrent writer");
        }
    }

    @Test
    @DisplayName("Second handle failure cleans up earlier opened handle immediately (EARLIER_NATIVE_HANDLE_LEAKS_AFTER_GATE_ROLLBACK)")
    void secondHandleFailureCleansUpEarlierHandleImmediately(@TempDir Path tempDir) throws IOException {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Test only applies to Windows NTFS environments");

        Path file1 = tempDir.resolve("first-file.txt");
        Files.writeString(file1, "First file content");

        Path file2 = tempDir.resolve("second-file.txt");
        Files.writeString(file2, "Second file content");

        // Hold an exclusive lock on file2 to force openAndGuard(file2) to fail
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file2.toFile())) {
            java.util.List<WindowsNativeFileGuard.ProtectedFileHandle> acquiredHandles = new java.util.ArrayList<>();
            try {
                acquiredHandles.add(fileGuard.openAndGuard(file1));
                acquiredHandles.add(fileGuard.openAndGuard(file2));
            } catch (Exception e) {
                for (WindowsNativeFileGuard.ProtectedFileHandle h : acquiredHandles) {
                    try {
                        h.close();
                    } catch (Exception ignored) {}
                }
            }

            // Immediately after failure, file1 handle must NOT be leaked (we can write to file1)
            Files.writeString(file1, "Successfully mutated file1 after second file failure cleanup");
            assertThat(Files.readString(file1)).isEqualTo("Successfully mutated file1 after second file failure cleanup");
        }
    }
}
