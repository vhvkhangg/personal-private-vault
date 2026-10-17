package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Windows NTFS live file handle guard.
 *
 * <p>Enforces R7 invariant on Windows 11/NTFS: opens a native Win32 handle with
 * {@code dwShareMode = FILE_SHARE_READ} (disallowing concurrent write and rename/delete),
 * validates that the target is not a reparse point or directory, captures volume serial
 * and 64-bit file index identity, computes deterministic SHA-256 byte hash directly from
 * the open handle without reopening, and maintains the lock through Java database commit.</p>
 */
@Component
public class WindowsNativeFileGuard {

    private static final Logger log = LoggerFactory.getLogger(WindowsNativeFileGuard.class);
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    private final long maxSourceBytes;
    private final MethodHandle createFileW;
    private final MethodHandle getFileInformationByHandle;
    private final MethodHandle readFile;
    private final MethodHandle closeHandle;
    private final MethodHandle getLastError;
    private final MethodHandle getFileAttributesW;
    private final MethodHandle getDriveTypeW;

    @org.springframework.beans.factory.annotation.Autowired
    public WindowsNativeFileGuard(@Value("${ppv.rag.limits.max-source-bytes:52428800}") long maxSourceBytes) {
        this.maxSourceBytes = maxSourceBytes > 0 ? maxSourceBytes : 52428800L;
        if (IS_WINDOWS) {
            MethodHandle cf = null;
            MethodHandle gfi = null;
            MethodHandle rf = null;
            MethodHandle ch = null;
            MethodHandle gle = null;
            MethodHandle gfa = null;
            MethodHandle gdt = null;
            try {
                Linker linker = Linker.nativeLinker();
                SymbolLookup kernel32 = SymbolLookup.libraryLookup("Kernel32.dll", Arena.global());

                cf = linker.downcallHandle(
                        kernel32.find("CreateFileW").orElseThrow(),
                        FunctionDescriptor.of(
                                ValueLayout.ADDRESS,
                                ValueLayout.ADDRESS, // lpFileName
                                ValueLayout.JAVA_INT, // dwDesiredAccess
                                ValueLayout.JAVA_INT, // dwShareMode
                                ValueLayout.ADDRESS,  // lpSecurityAttributes
                                ValueLayout.JAVA_INT, // dwCreationDisposition
                                ValueLayout.JAVA_INT, // dwFlagsAndAttributes
                                ValueLayout.ADDRESS   // hTemplateFile
                        )
                );

                gfi = linker.downcallHandle(
                        kernel32.find("GetFileInformationByHandle").orElseThrow(),
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, // hFile
                                ValueLayout.ADDRESS  // lpFileInformation
                        )
                );

                rf = linker.downcallHandle(
                        kernel32.find("ReadFile").orElseThrow(),
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT, // BOOL
                                ValueLayout.ADDRESS,  // hFile
                                ValueLayout.ADDRESS,  // lpBuffer
                                ValueLayout.JAVA_INT, // nNumberOfBytesToRead
                                ValueLayout.ADDRESS,  // lpNumberOfBytesRead
                                ValueLayout.ADDRESS   // lpOverlapped
                        )
                );

                ch = linker.downcallHandle(
                        kernel32.find("CloseHandle").orElseThrow(),
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS // hObject
                        )
                );

                gle = linker.downcallHandle(
                        kernel32.find("GetLastError").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT)
                );

                gfa = linker.downcallHandle(
                        kernel32.find("GetFileAttributesW").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                );

                gdt = linker.downcallHandle(
                        kernel32.find("GetDriveTypeW").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                );
            } catch (Exception e) {
                log.error("Failed to bind Kernel32 Win32 handles: {}", e.getMessage(), e);
            }
            this.createFileW = cf;
            this.getFileInformationByHandle = gfi;
            this.readFile = rf;
            this.closeHandle = ch;
            this.getLastError = gle;
            this.getFileAttributesW = gfa;
            this.getDriveTypeW = gdt;
        } else {
            this.createFileW = null;
            this.getFileInformationByHandle = null;
            this.readFile = null;
            this.closeHandle = null;
            this.getLastError = null;
            this.getFileAttributesW = null;
            this.getDriveTypeW = null;
        }
    }

    public ProtectedFileHandle openAndGuard(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        if (IS_WINDOWS && (createFileW == null || getFileInformationByHandle == null || readFile == null
                || closeHandle == null || getFileAttributesW == null || getDriveTypeW == null)) {
            throw new IllegalStateException("Mandatory Windows native downcall handle is not available");
        }

        // 1. Verify filesystem volume is local NTFS
        try {
            java.nio.file.FileStore store = Files.getFileStore(path);
            String storeType = store.type();
            if (!"NTFS".equalsIgnoreCase(storeType)) {
                throw new IllegalStateException("Unsupported volume filesystem: " + storeType + "; only local NTFS volumes are supported: " + path.getFileName());
            }
        } catch (IOException e) {
            throw new NoSuchElementException("Cannot determine filesystem volume for path: " + path.getFileName() + ": " + e.getMessage());
        }

        // 2. Verify drive is a local fixed drive (DRIVE_FIXED = 3)
        Path root = path.toAbsolutePath().getRoot();
        if (root != null && IS_WINDOWS) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment rootWide = arena.allocateFrom(root.toString(), StandardCharsets.UTF_16LE);
                int driveType = (int) getDriveTypeW.invokeExact(rootWide);
                if (driveType != 3) { // 3 = DRIVE_FIXED
                    throw new IllegalStateException("Only local fixed NTFS drives are supported (got driveType=" + driveType + "): " + path.getFileName());
                }
            } catch (Throwable t) {
                if (t instanceof RuntimeException re) throw re;
                throw new IllegalStateException("Failed to check drive type for root of: " + path.getFileName(), t);
            }
        }

        // 3. Verify no component or ancestor directory is a reparse point or junction
        Path check = path.toAbsolutePath().normalize();
        while (check != null) {
            if (Files.isSymbolicLink(check)) {
                throw new IllegalStateException("Reparse point or symbolic link detected in path: " + check.getFileName());
            }
            if (IS_WINDOWS) {
                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment checkWide = arena.allocateFrom(check.toString(), StandardCharsets.UTF_16LE);
                    int attrs = (int) getFileAttributesW.invokeExact(checkWide);
                    if (attrs == -1) {
                        throw new IllegalStateException("Failed to query Win32 file attributes for path component: " + check.getFileName());
                    }
                    if ((attrs & 0x0400) != 0) { // FILE_ATTRIBUTE_REPARSE_POINT = 0x0400
                        throw new IllegalStateException("Reparse point or junction detected in path: " + check.getFileName());
                    }
                } catch (Throwable t) {
                    if (t instanceof RuntimeException re) throw re;
                    throw new IllegalStateException("Failed to verify attributes for: " + check.getFileName(), t);
                }
            }
            check = check.getParent();
        }

        Path canonicalPath;
        try {
            canonicalPath = path.toRealPath();
        } catch (IOException e) {
            throw new NoSuchElementException("Cannot resolve real path for: " + path.getFileName() + ": " + e.getMessage());
        }

        if (Files.isDirectory(canonicalPath)) {
            throw new IllegalArgumentException("Target path must be a regular file, not a directory: " + path.getFileName());
        }

        if (IS_WINDOWS && createFileW != null && readFile != null) {
            return openWindowsNative(canonicalPath);
        } else {
            throw new UnsupportedOperationException("Windows native NTFS file guard is required and unavailable on this environment");
        }
    }

    private ProtectedFileHandle openWindowsNative(Path canonicalPath) {
        Arena arena = Arena.ofConfined();
        try {
            // Null-terminated UTF-16LE string for CreateFileW
            MemorySegment widePath = arena.allocateFrom(canonicalPath.toString(), StandardCharsets.UTF_16LE);

            // GENERIC_READ = 0x80000000
            int dwDesiredAccess = 0x80000000;
            // FILE_SHARE_READ = 0x00000001 (NO FILE_SHARE_WRITE (0x02), NO FILE_SHARE_DELETE (0x04))
            int dwShareMode = 0x00000001;
            // OPEN_EXISTING = 3
            int dwCreationDisposition = 3;
            // FILE_ATTRIBUTE_NORMAL = 0x80
            int dwFlagsAndAttributes = 0x80;

            MemorySegment handle = (MemorySegment) createFileW.invokeExact(
                    widePath,
                    dwDesiredAccess,
                    dwShareMode,
                    MemorySegment.NULL,
                    dwCreationDisposition,
                    dwFlagsAndAttributes,
                    MemorySegment.NULL
            );

            long rawHandle = handle.address();
            if (rawHandle == -1L || rawHandle == 0L) {
                int lastErr = getLastError != null ? (int) getLastError.invokeExact() : -1;
                if (lastErr == 32 || lastErr == 0) { // ERROR_SHARING_VIOLATION (or captured 0 when FFM thread resets GetLastError)
                    throw new IllegalStateException("File is already locked or open by a concurrent writer: " + canonicalPath.getFileName());
                } else if (lastErr == 2 || lastErr == 3) {
                    throw new NoSuchElementException("File does not exist: " + canonicalPath.getFileName());
                } else {
                    throw new IllegalStateException("Win32 CreateFileW failed with error " + lastErr + " for: " + canonicalPath.getFileName());
                }
            }

            // BY_HANDLE_FILE_INFORMATION structure size is 52 bytes
            MemorySegment fileInfo = arena.allocate(52);
            int success = (int) getFileInformationByHandle.invokeExact(handle, fileInfo);
            if (success == 0) {
                int closeRes = (int) closeHandle.invokeExact(handle);
                if (closeRes == 0) {
                    log.warn("Win32 CloseHandle returned 0 in cleanup for: {}", canonicalPath.getFileName());
                }
                throw new IllegalStateException("Failed to query Win32 file information for: " + canonicalPath.getFileName());
            }

            int fileAttributes = fileInfo.get(ValueLayout.JAVA_INT, 0);
            // FILE_ATTRIBUTE_DIRECTORY = 0x0010
            if ((fileAttributes & 0x0010) != 0) {
                int closeRes = (int) closeHandle.invokeExact(handle);
                if (closeRes == 0) {
                    log.warn("Win32 CloseHandle returned 0 in cleanup for: {}", canonicalPath.getFileName());
                }
                throw new IllegalArgumentException("Target path must be a regular file, not a directory: " + canonicalPath.getFileName());
            }
            // FILE_ATTRIBUTE_REPARSE_POINT = 0x0400
            if ((fileAttributes & 0x0400) != 0) {
                int closeRes = (int) closeHandle.invokeExact(handle);
                if (closeRes == 0) {
                    log.warn("Win32 CloseHandle returned 0 in cleanup for: {}", canonicalPath.getFileName());
                }
                throw new IllegalStateException("Reparse point or symbolic link detected; unsafe external file: " + canonicalPath.getFileName());
            }

            long fileSizeHigh = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, 32));
            long fileSizeLow = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, 36));
            long fileSize = (fileSizeHigh << 32) | fileSizeLow;
            if (fileSize > maxSourceBytes) {
                int closeRes = (int) closeHandle.invokeExact(handle);
                if (closeRes == 0) {
                    log.warn("Win32 CloseHandle returned 0 in cleanup for: {}", canonicalPath.getFileName());
                }
                throw new IllegalArgumentException("File size exceeds configured maximum source bytes: " + fileSize + " > " + maxSourceBytes);
            }

            int volumeSerial = fileInfo.get(ValueLayout.JAVA_INT, 28);
            int fileIndexHigh = fileInfo.get(ValueLayout.JAVA_INT, 44);
            int fileIndexLow = fileInfo.get(ValueLayout.JAVA_INT, 48);
            String fileIdentity = volumeSerial + ":" + Integer.toUnsignedString(fileIndexHigh) + ":" + Integer.toUnsignedString(fileIndexLow);

            // Read bytes directly through the open handle via Win32 ReadFile, accumulate, and compute SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            java.io.ByteArrayOutputStream contentBaos = new java.io.ByteArrayOutputStream();
            int bufSize = 65536;
            MemorySegment buffer = arena.allocate(bufSize);
            MemorySegment bytesReadSeg = arena.allocate(ValueLayout.JAVA_INT);
            long totalRead = 0;

            while (true) {
                bytesReadSeg.set(ValueLayout.JAVA_INT, 0, 0);
                int readOk = (int) readFile.invokeExact(handle, buffer, bufSize, bytesReadSeg, MemorySegment.NULL);
                if (readOk == 0) {
                    int lastErr = getLastError != null ? (int) getLastError.invokeExact() : -1;
                    int closeRes = (int) closeHandle.invokeExact(handle);
                    if (closeRes == 0) {
                        log.warn("Win32 CloseHandle returned 0 in cleanup for: {}", canonicalPath.getFileName());
                    }
                    throw new IllegalStateException("Win32 ReadFile failed with error " + lastErr + " for: " + canonicalPath.getFileName());
                }
                int bytesRead = bytesReadSeg.get(ValueLayout.JAVA_INT, 0);
                if (bytesRead <= 0) {
                    break; // EOF reached
                }
                totalRead += bytesRead;
                if (totalRead > maxSourceBytes) {
                    int closeRes = (int) closeHandle.invokeExact(handle);
                    if (closeRes == 0) {
                        log.warn("Win32 CloseHandle returned 0 in cleanup for: {}", canonicalPath.getFileName());
                    }
                    throw new IllegalArgumentException("File size exceeds configured maximum source bytes: " + totalRead + " > " + maxSourceBytes);
                }
                byte[] chunk = buffer.asSlice(0, bytesRead).toArray(ValueLayout.JAVA_BYTE);
                digest.update(chunk);
                contentBaos.write(chunk);
            }

            String contentHash = HexFormat.of().formatHex(digest.digest());
            byte[] contentBytes = contentBaos.toByteArray();

            return new WindowsProtectedFileHandle(canonicalPath, fileIdentity, contentHash, contentBytes, handle, arena, closeHandle);
        } catch (Throwable t) {
            if (arena.scope().isAlive()) {
                arena.close();
            }
            if (t instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Failed to acquire Windows native file guard on " + canonicalPath.getFileName(), t);
        }
    }

    public interface ProtectedFileHandle extends AutoCloseable {
        Path getPath();
        String getFileIdentity();
        String getContentHash();
        byte[] getBytes();
        String getContentAsUtf8();
        @Override
        void close();
    }

    private static final class WindowsProtectedFileHandle implements ProtectedFileHandle {
        private final Path path;
        private final String fileIdentity;
        private final String contentHash;
        private final byte[] contentBytes;
        private final MemorySegment handle;
        private final Arena arena;
        private final MethodHandle closeHandle;
        private boolean closed = false;

        WindowsProtectedFileHandle(
                Path path,
                String fileIdentity,
                String contentHash,
                byte[] contentBytes,
                MemorySegment handle,
                Arena arena,
                MethodHandle closeHandle) {
            this.path = path;
            this.fileIdentity = fileIdentity;
            this.contentHash = contentHash;
            this.contentBytes = contentBytes;
            this.handle = handle;
            this.arena = arena;
            this.closeHandle = closeHandle;
        }

        @Override public Path getPath() { return path; }
        @Override public String getFileIdentity() { return fileIdentity; }
        @Override public String getContentHash() { return contentHash; }
        @Override public byte[] getBytes() { return contentBytes.clone(); }
        @Override public String getContentAsUtf8() { return new String(contentBytes, StandardCharsets.UTF_8); }

        @Override
        public synchronized void close() {
            if (!closed) {
                closed = true;
                try {
                    int res = (int) closeHandle.invokeExact(handle);
                    if (res == 0) {
                        log.warn("Win32 CloseHandle returned 0 for {}", path.getFileName());
                    }
                } catch (Throwable t) {
                    log.error("Failed to close Windows native handle for {}", path.getFileName(), t);
                } finally {
                    arena.close();
                }
            }
        }
    }
}
