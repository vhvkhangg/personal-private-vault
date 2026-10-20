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
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/**
 * Windows NTFS live file handle guard.
 *
 * <p>Native prerequisite for R7 on Windows 11/NTFS: opens a native Win32 handle with
 * {@code dwShareMode = FILE_SHARE_READ} (disallowing concurrent write and rename/delete),
 * validates that the target is not a reparse point or directory, captures volume serial
 * and 64-bit file index identity, computes deterministic SHA-256 byte hash directly from
 * the open handle without reopening. Callers own its lifetime; this guard alone does
 * not establish continuous namespace safety or publication acceptance.</p>
 */
@Component
public class WindowsNativeFileGuard {

    private static final Logger log = LoggerFactory.getLogger(WindowsNativeFileGuard.class);
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    // Win32 DWORD and FILETIME members are 4-byte aligned, including on Win64.
    private static final MemoryLayout FILE_INFORMATION = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("attributes"),
            MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("creationTime"),
            MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("accessTime"),
            MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("writeTime"),
            ValueLayout.JAVA_INT.withName("volume"),
            ValueLayout.JAVA_INT.withName("sizeHigh"),
            ValueLayout.JAVA_INT.withName("sizeLow"),
            ValueLayout.JAVA_INT.withName("links"),
            ValueLayout.JAVA_INT.withName("indexHigh"),
            ValueLayout.JAVA_INT.withName("indexLow"));
    private static final long LINK_COUNT_OFFSET = FILE_INFORMATION.byteOffset(
            MemoryLayout.PathElement.groupElement("links"));

    private static final MemoryLayout OBJECT_ATTRIBUTES = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("length"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withName("rootDirectory"),
            ValueLayout.ADDRESS.withName("objectName"),
            ValueLayout.JAVA_INT.withName("attributes"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withName("securityDescriptor"),
            ValueLayout.ADDRESS.withName("securityQualityOfService"));

    private static final MemoryLayout UNICODE_STRING = MemoryLayout.structLayout(
            ValueLayout.JAVA_SHORT.withName("length"),
            ValueLayout.JAVA_SHORT.withName("maximumLength"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withName("buffer"));

    private static final MemoryLayout IO_STATUS_BLOCK = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("status"),
            ValueLayout.ADDRESS.withName("information"));

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
    private final MethodHandle ntCreateFile;
    private final MethodHandle rtlNtStatusToDosError;

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
            MethodHandle ntcf = null;
            MethodHandle rtldos = null;
            try {
                Linker linker = Linker.nativeLinker();
                SymbolLookup kernel32 = SymbolLookup.libraryLookup("Kernel32.dll", Arena.global());
                SymbolLookup ntdll = SymbolLookup.libraryLookup("ntdll.dll", Arena.global());

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

                ntcf = linker.downcallHandle(
                        ntdll.find("NtCreateFile").orElseThrow(),
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT, // NTSTATUS
                                ValueLayout.ADDRESS,  // PHANDLE FileHandle
                                ValueLayout.JAVA_INT, // ACCESS_MASK DesiredAccess
                                ValueLayout.ADDRESS,  // POBJECT_ATTRIBUTES ObjectAttributes
                                ValueLayout.ADDRESS,  // PIO_STATUS_BLOCK IoStatusBlock
                                ValueLayout.ADDRESS,  // PLARGE_INTEGER AllocationSize
                                ValueLayout.JAVA_INT, // ULONG FileAttributes
                                ValueLayout.JAVA_INT, // ULONG ShareAccess
                                ValueLayout.JAVA_INT, // ULONG CreateDisposition
                                ValueLayout.JAVA_INT, // ULONG CreateOptions
                                ValueLayout.ADDRESS,  // PVOID EaBuffer
                                ValueLayout.JAVA_INT   // ULONG EaLength
                        )
                );

                rtldos = linker.downcallHandle(
                        ntdll.find("RtlNtStatusToDosError").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
                );
            } catch (Exception e) {
                log.error("Failed to bind Kernel32/Ntdll Win32 handles: {}", e.getMessage(), e);
            }
            this.createFileW = cf;
            this.getFileInformationByHandle = gfi;
            this.readFile = rf;
            this.closeHandle = ch;
            this.getLastError = gle;
            this.getFileAttributesW = gfa;
            this.getDriveTypeW = gdt;
            this.ntCreateFile = ntcf;
            this.rtlNtStatusToDosError = rtldos;
        } else {
            this.createFileW = null;
            this.getFileInformationByHandle = null;
            this.readFile = null;
            this.closeHandle = null;
            this.getLastError = null;
            this.getFileAttributesW = null;
            this.getDriveTypeW = null;
            this.ntCreateFile = null;
            this.rtlNtStatusToDosError = null;
        }
    }

    public void verifyNtfsVolume(Path path) {
        try {
            java.nio.file.FileStore store = Files.getFileStore(path);
            String storeType = store.type();
            if (!"NTFS".equalsIgnoreCase(storeType)) {
                throw new IllegalStateException("Unsupported volume filesystem: " + storeType + "; only local NTFS volumes are supported: " + path.getFileName());
            }
        } catch (IOException e) {
            throw new NoSuchElementException("Cannot determine filesystem volume for path: " + path.getFileName() + ": " + e.getMessage());
        }
    }

    /** Ordinary locality policy only; does not acquire the live-file/R7 handle chain. */
    public void verifyLocalSnapshotDrive(Path path) {
        if (IS_WINDOWS && getDriveTypeW==null) throw new IllegalStateException("Local drive check unavailable");
        verifyDriveType(path);
    }

    public void verifyDriveType(Path path) {
        Path root = path.toAbsolutePath().getRoot();
        if (root != null && IS_WINDOWS && getDriveTypeW != null) {
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
    }

    public void verifyNotReparsePoint(Path path) {
        if (Files.isSymbolicLink(path)) {
            throw new IllegalStateException("Reparse point or symbolic link detected in path: " + path.getFileName());
        }
        if (IS_WINDOWS && getFileAttributesW != null) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment checkWide = arena.allocateFrom(path.toString(), StandardCharsets.UTF_16LE);
                int attrs = (int) getFileAttributesW.invokeExact(checkWide);
                if (attrs == -1) {
                    throw new IllegalStateException("Failed to query Win32 file attributes for path component: " + path.getFileName());
                }
                if ((attrs & 0x0400) != 0) { // FILE_ATTRIBUTE_REPARSE_POINT = 0x0400
                    throw new IllegalStateException("Reparse point or junction detected in path: " + path.getFileName());
                }
            } catch (Throwable t) {
                if (t instanceof RuntimeException re) throw re;
                throw new IllegalStateException("Failed to verify attributes for: " + path.getFileName(), t);
            }
        }
    }

    public ProtectedFileHandle openAndGuard(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        if (IS_WINDOWS && (createFileW == null || getFileInformationByHandle == null || readFile == null
                || closeHandle == null || getFileAttributesW == null || getDriveTypeW == null)) {
            throw new IllegalStateException("Mandatory Windows native downcall handle is not available");
        }

        // 1. Verify filesystem volume is local NTFS
        verifyNtfsVolume(path);

        // 2. Verify drive is a local fixed drive (DRIVE_FIXED = 3)
        verifyDriveType(path);

        // 3. Verify no component or ancestor directory is a reparse point or junction
        Path check = path.toAbsolutePath().normalize();
        while (check != null) {
            verifyNotReparsePoint(check);
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
        MemorySegment acquiredHandle = MemorySegment.NULL;
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
            acquiredHandle = handle;

            // Zero initialized: a successful but incomplete query cannot imply one link.
            MemorySegment fileInfo = arena.allocate(FILE_INFORMATION);
            int success = (int) getFileInformationByHandle.invokeExact(handle, fileInfo);
            if (success == 0) {
                throw new IllegalStateException("Failed to query Win32 file information for: " + canonicalPath.getFileName());
            }

            long linkCount = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, LINK_COUNT_OFFSET));
            if (linkCount != 1) {
                throw new IllegalStateException("Unsupported NTFS hard-link count: " + linkCount + "; exactly one link is required");
            }

            int fileAttributes = fileInfo.get(ValueLayout.JAVA_INT, 0);
            // FILE_ATTRIBUTE_DIRECTORY = 0x0010
            if ((fileAttributes & 0x0010) != 0) {
                throw new IllegalArgumentException("Target path must be a regular file, not a directory: " + canonicalPath.getFileName());
            }
            // FILE_ATTRIBUTE_REPARSE_POINT = 0x0400
            if ((fileAttributes & 0x0400) != 0) {
                throw new IllegalStateException("Reparse point or symbolic link detected; unsafe external file: " + canonicalPath.getFileName());
            }

            long fileSizeHigh = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, 32));
            long fileSizeLow = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, 36));
            long fileSize = (fileSizeHigh << 32) | fileSizeLow;
            if (fileSize > maxSourceBytes) {
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
                    throw new IllegalStateException("Win32 ReadFile failed with error " + lastErr + " for: " + canonicalPath.getFileName());
                }
                int bytesRead = bytesReadSeg.get(ValueLayout.JAVA_INT, 0);
                if (bytesRead <= 0) {
                    break; // EOF reached
                }
                totalRead += bytesRead;
                if (totalRead > maxSourceBytes) {
                    throw new IllegalArgumentException("File size exceeds configured maximum source bytes: " + totalRead + " > " + maxSourceBytes);
                }
                byte[] chunk = buffer.asSlice(0, bytesRead).toArray(ValueLayout.JAVA_BYTE);
                digest.update(chunk);
                contentBaos.write(chunk);
            }

            String contentHash = HexFormat.of().formatHex(digest.digest());
            byte[] contentBytes = contentBaos.toByteArray();

            return new WindowsProtectedFileHandle(canonicalPath, fileIdentity, contentHash, contentBytes, handle, arena, closeHandle, getLastError);
        } catch (Throwable t) {
            if (acquiredHandle.address() != 0) {
                try {
                    int closeResult = (int) closeHandle.invokeExact(acquiredHandle);
                    if (closeResult == 0) {
                        t.addSuppressed(new IllegalStateException("Win32 CloseHandle failed during guard denial"));
                    }
                } catch (Throwable cleanupFailure) {
                    t.addSuppressed(cleanupFailure);
                }
            }
            if (arena.scope().isAlive()) {
                arena.close();
            }
            if (t instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Failed to acquire Windows native file guard on " + canonicalPath.getFileName(), t);
        }
    }

    public DirectoryIdentity queryDirectoryIdentity(Path dirPath) {
        Objects.requireNonNull(dirPath, "dirPath must not be null");
        if (IS_WINDOWS && (createFileW == null || getFileInformationByHandle == null || closeHandle == null
                || getFileAttributesW == null || getDriveTypeW == null)) {
            throw new IllegalStateException("Mandatory Windows native downcall handle is not available");
        }
        Path canonicalDir;
        try {
            canonicalDir = dirPath.toRealPath();
        } catch (IOException e) {
            throw new NoSuchElementException("Cannot resolve real path for directory: " + dirPath.getFileName() + ": " + e.getMessage());
        }
        if (!Files.isDirectory(canonicalDir)) {
            throw new IllegalArgumentException("Target path must be a directory: " + dirPath.getFileName());
        }
        verifyNtfsVolume(canonicalDir);
        verifyDriveType(canonicalDir);
        verifyNotReparsePoint(canonicalDir);

        if (!IS_WINDOWS || createFileW == null) {
            throw new UnsupportedOperationException("Windows native directory query is unavailable");
        }

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment widePath = arena.allocateFrom(canonicalDir.toString(), StandardCharsets.UTF_16LE);
            int dwDesiredAccess = 0x0080; // FILE_READ_ATTRIBUTES
            int dwShareMode = 7; // FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE
            int dwCreationDisposition = 3; // OPEN_EXISTING
            int dwFlagsAndAttributes = 0x02000000; // FILE_FLAG_BACKUP_SEMANTICS

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
                throw new IllegalStateException("Failed to open directory handle for " + canonicalDir.getFileName() + " (Win32 error " + lastErr + ")");
            }
            try {
                MemorySegment fileInfo = arena.allocate(FILE_INFORMATION);
                int success = (int) getFileInformationByHandle.invokeExact(handle, fileInfo);
                if (success == 0) {
                    throw new IllegalStateException("Failed to query directory information for: " + canonicalDir.getFileName());
                }
                int volumeSerial = fileInfo.get(ValueLayout.JAVA_INT, 28);
                int fileIndexHigh = fileInfo.get(ValueLayout.JAVA_INT, 44);
                int fileIndexLow = fileInfo.get(ValueLayout.JAVA_INT, 48);
                return new DirectoryIdentity(canonicalDir, volumeSerial, fileIndexHigh, fileIndexLow);
            } finally {
                int res = (int) closeHandle.invokeExact(handle);
                if (res == 0) {
                    int lastErr = getLastError != null ? (int) getLastError.invokeExact() : -1;
                    log.warn("Win32 CloseHandle returned 0 for directory {}: error {}", canonicalDir.getFileName(), lastErr);
                }
            }
        } catch (Throwable t) {
            if (t instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Failed to query directory identity for " + dirPath.getFileName(), t);
        }
    }

    private static final Set<String> RESERVED_DEVICE_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );

    public AcquiredSource acquireProtectedSource(
            Path allowedRoot,
            Path targetPath,
            SourceIdentity expectedFileIdentity,
            DirectoryIdentity expectedRootIdentity) {
        return acquireProtectedSource(allowedRoot, targetPath, expectedFileIdentity, expectedRootIdentity, null, null);
    }

    public AcquiredSource acquireProtectedSource(
            Path allowedRoot,
            Path targetPath,
            SourceIdentity expectedFileIdentity,
            DirectoryIdentity expectedRootIdentity,
            BooleanSupplier cancelRequested) {
        return acquireProtectedSource(allowedRoot, targetPath, expectedFileIdentity, expectedRootIdentity, null, cancelRequested);
    }

    public AcquiredSource acquireProtectedSource(
            Path allowedRoot,
            Path targetPath,
            SourceIdentity expectedFileIdentity,
            DirectoryIdentity expectedRootIdentity,
            List<DirectoryIdentity> expectedAncestorChain,
            BooleanSupplier cancelRequested) {
        Objects.requireNonNull(allowedRoot, "allowedRoot must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");

        if (IS_WINDOWS && (createFileW == null || getFileInformationByHandle == null || readFile == null
                || closeHandle == null || getFileAttributesW == null || getDriveTypeW == null
                || ntCreateFile == null || rtlNtStatusToDosError == null)) {
            throw new IllegalStateException("Mandatory Windows native downcall handle is not available");
        }

        // Validate syntax: no alternate data streams
        String pathStr = targetPath.toString();
        int firstColon = pathStr.indexOf(':');
        if (firstColon != -1) {
            if (firstColon != 1 || pathStr.indexOf(':', 2) != -1) {
                throw new IllegalArgumentException("Alternate data streams are not supported: " + targetPath);
            }
        }

        // Validate syntax: no reserved device names
        Path targetFileName = targetPath.getFileName();
        if (targetFileName != null) {
            String name = targetFileName.toString().toUpperCase(java.util.Locale.ROOT);
            int dot = name.indexOf('.');
            String base = dot >= 0 ? name.substring(0, dot) : name;
            if (RESERVED_DEVICE_NAMES.contains(base)) {
                throw new IllegalArgumentException("Windows device names are not supported: " + targetPath);
            }
        }

        // Containment check
        Path normRoot = allowedRoot.toAbsolutePath().normalize();
        Path normTarget = targetPath.toAbsolutePath().normalize();
        if (!normTarget.startsWith(normRoot) || normTarget.equals(normRoot)) {
            throw new IllegalArgumentException("Target path must be strictly contained within allowed root: " + targetPath);
        }

        // Verify no component or ancestor directory is a reparse point or junction before following paths
        Path checkNorm = normTarget;
        while (checkNorm != null) {
            verifyNotReparsePoint(checkNorm);
            if (checkNorm.equals(normRoot)) {
                break;
            }
            checkNorm = checkNorm.getParent();
        }

        Path realRoot;
        try {
            realRoot = allowedRoot.toRealPath();
        } catch (IOException e) {
            throw new NoSuchElementException("Allowed root cannot be resolved to real path: " + allowedRoot);
        }

        Path realTarget;
        try {
            realTarget = targetPath.toRealPath();
        } catch (IOException e) {
            throw new NoSuchElementException("Target file cannot be resolved to real path: " + targetPath);
        }

        if (!realTarget.startsWith(realRoot) || realTarget.equals(realRoot)) {
            throw new IllegalArgumentException("Target path must be strictly contained within allowed root: " + targetPath);
        }

        // 1. Collect ancestor hierarchy from realTarget.getParent() up to realRoot
        List<Path> hierarchyFromRoot = new ArrayList<>();
        Path curr = realTarget.getParent();
        while (curr != null) {
            hierarchyFromRoot.add(curr);
            if (curr.equals(realRoot)) {
                break;
            }
            curr = curr.getParent();
        }
        if (hierarchyFromRoot.isEmpty() || !hierarchyFromRoot.get(hierarchyFromRoot.size() - 1).equals(realRoot)) {
            throw new IllegalStateException("Ancestor directory chain does not terminate at allowed root: " + realTarget);
        }
        Collections.reverse(hierarchyFromRoot); // Now ordered: realRoot, child1, child2, ..., realTarget.getParent()

        Arena arena = Arena.ofConfined();
        List<MemorySegment> heldAncestorHandles = new ArrayList<>();
        List<DirectoryIdentity> ancestorChain = new ArrayList<>();
        MemorySegment acquiredHandle = MemorySegment.NULL;

        try {
            // Step 1: Open and verify stable local volume root directory object
            Path rootDir = hierarchyFromRoot.get(0);
            verifyNotReparsePoint(rootDir);
            MemorySegment wideRootDir = arena.allocateFrom(rootDir.toString(), StandardCharsets.UTF_16LE);
            // Open with FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OPEN_REPARSE_POINT and FILE_SHARE_READ | FILE_SHARE_WRITE (NO FILE_SHARE_DELETE!)
            MemorySegment rootHandle = (MemorySegment) createFileW.invokeExact(
                    wideRootDir,
                    0x80000000 | 0x00100000, // GENERIC_READ | SYNCHRONIZE
                    0x00000003,              // FILE_SHARE_READ | FILE_SHARE_WRITE (NO FILE_SHARE_DELETE!)
                    MemorySegment.NULL,
                    3,                       // OPEN_EXISTING
                    0x02000000 | 0x00200000, // FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OPEN_REPARSE_POINT
                    MemorySegment.NULL
            );
            long rawRootHandle = rootHandle.address();
            if (rawRootHandle == -1L || rawRootHandle == 0L) {
                int lastErr = getLastError != null ? (int) getLastError.invokeExact() : -1;
                throw new IllegalStateException("Failed to open held root directory handle for "
                        + rootDir.getFileName() + " (Win32 error " + lastErr + ")");
            }
            heldAncestorHandles.add(rootHandle);

            MemorySegment rootInfo = arena.allocate(FILE_INFORMATION);
            int rootSuccess = (int) getFileInformationByHandle.invokeExact(rootHandle, rootInfo);
            if (rootSuccess == 0) {
                throw new IllegalStateException("Failed to query root directory information for: " + rootDir.getFileName());
            }
            int rootAttrs = rootInfo.get(ValueLayout.JAVA_INT, 0);
            if ((rootAttrs & 0x0400) != 0) {
                throw new IllegalStateException("Reparse point or junction detected at root: " + rootDir.getFileName());
            }
            if ((rootAttrs & 0x0010) == 0) {
                throw new IllegalArgumentException("Root path must be a directory: " + rootDir.getFileName());
            }
            int rootVolSerial = rootInfo.get(ValueLayout.JAVA_INT, 28);
            int rootFileIdxHigh = rootInfo.get(ValueLayout.JAVA_INT, 44);
            int rootFileIdxLow = rootInfo.get(ValueLayout.JAVA_INT, 48);
            DirectoryIdentity actualRootId = new DirectoryIdentity(rootDir, rootVolSerial, rootFileIdxHigh, rootFileIdxLow);
            ancestorChain.add(actualRootId);

            if (expectedRootIdentity != null) {
                if (actualRootId.volumeSerialNumber() != expectedRootIdentity.volumeSerialNumber()
                        || actualRootId.fileIndexHigh() != expectedRootIdentity.fileIndexHigh()
                        || actualRootId.fileIndexLow() != expectedRootIdentity.fileIndexLow()) {
                    throw new IllegalStateException("Root directory identity mismatch: wrong root (expected "
                            + expectedRootIdentity.toIdentityString() + ", observed " + actualRootId.toIdentityString() + ")");
                }
            }
            if (expectedAncestorChain != null && !expectedAncestorChain.isEmpty()) {
                DirectoryIdentity expectedFirst = expectedAncestorChain.get(0);
                if (actualRootId.volumeSerialNumber() != expectedFirst.volumeSerialNumber()
                        || actualRootId.fileIndexHigh() != expectedFirst.fileIndexHigh()
                        || actualRootId.fileIndexLow() != expectedFirst.fileIndexLow()) {
                    throw new IllegalStateException("Root directory does not match expected ancestor chain root: expected "
                            + expectedFirst.toIdentityString() + " but observed " + actualRootId.toIdentityString());
                }
            }

            // Step 2: Open each intermediate directory relative to the held verified parent using NtCreateFile
            MemorySegment currentParentHandle = rootHandle;
            for (int i = 1; i < hierarchyFromRoot.size(); i++) {
                Path childDir = hierarchyFromRoot.get(i);
                String childDirName = childDir.getFileName().toString();
                MemorySegment childDirHandle = openChildRelativeNt(arena, currentParentHandle, childDirName, true, childDir);
                heldAncestorHandles.add(childDirHandle);

                MemorySegment dirInfo = arena.allocate(FILE_INFORMATION);
                int success = (int) getFileInformationByHandle.invokeExact(childDirHandle, dirInfo);
                if (success == 0) {
                    throw new IllegalStateException("Failed to query directory information for: " + childDir.getFileName());
                }
                int dirAttrs = dirInfo.get(ValueLayout.JAVA_INT, 0);
                if ((dirAttrs & 0x0400) != 0) {
                    throw new IllegalStateException("Reparse point or junction detected during traversal: " + childDir.getFileName());
                }
                if ((dirAttrs & 0x0010) == 0) {
                    throw new IllegalArgumentException("Component must be a directory: " + childDir.getFileName());
                }
                int volSerial = dirInfo.get(ValueLayout.JAVA_INT, 28);
                if (volSerial != actualRootId.volumeSerialNumber()) {
                    throw new IllegalStateException("Ancestor directory resides on different volume: " + childDir);
                }
                int fileIdxHigh = dirInfo.get(ValueLayout.JAVA_INT, 44);
                int fileIdxLow = dirInfo.get(ValueLayout.JAVA_INT, 48);
                DirectoryIdentity dirId = new DirectoryIdentity(childDir, volSerial, fileIdxHigh, fileIdxLow);
                ancestorChain.add(dirId);

                if (expectedAncestorChain != null && i < expectedAncestorChain.size()) {
                    DirectoryIdentity expectedDir = expectedAncestorChain.get(i);
                    if (dirId.volumeSerialNumber() != expectedDir.volumeSerialNumber()
                            || dirId.fileIndexHigh() != expectedDir.fileIndexHigh()
                            || dirId.fileIndexLow() != expectedDir.fileIndexLow()) {
                        throw new IllegalStateException("Ancestor directory identity mismatch at component " + i + ": "
                                + childDir.getFileName() + " (expected " + expectedDir.toIdentityString() + ", observed " + dirId.toIdentityString() + ")");
                    }
                }
                currentParentHandle = childDirHandle;
            }

            // Step 3: Open target file relative to the held parent directory handle using NtCreateFile
            String targetFileNameStr = realTarget.getFileName().toString();
            MemorySegment handle = openChildRelativeNt(arena, currentParentHandle, targetFileNameStr, false, realTarget);
            acquiredHandle = handle;

            // Step 4: Query file information via open handle
            MemorySegment fileInfo = arena.allocate(FILE_INFORMATION);
            int success = (int) getFileInformationByHandle.invokeExact(handle, fileInfo);
            if (success == 0) {
                throw new IllegalStateException("Failed to query Win32 file information for: " + normTarget.getFileName());
            }

            int fileAttributes = fileInfo.get(ValueLayout.JAVA_INT, 0);
            if ((fileAttributes & 0x0010) != 0) {
                throw new IllegalArgumentException("Target path must be a regular file, not a directory: " + normTarget.getFileName());
            }
            if ((fileAttributes & 0x0400) != 0) {
                throw new IllegalStateException("Reparse point or symbolic link detected; unsafe external file: " + normTarget.getFileName());
            }

            long linkCount = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, LINK_COUNT_OFFSET));
            if (linkCount != 1) {
                throw new IllegalStateException("Unsupported NTFS hard-link count: " + linkCount + "; exactly one link is required");
            }

            int volumeSerial = fileInfo.get(ValueLayout.JAVA_INT, 28);
            if (volumeSerial != actualRootId.volumeSerialNumber()) {
                throw new IllegalStateException("Target file resides on different volume than allowed root");
            }

            int fileIndexHigh = fileInfo.get(ValueLayout.JAVA_INT, 44);
            int fileIndexLow = fileInfo.get(ValueLayout.JAVA_INT, 48);
            SourceIdentity actualFileIdentity = new SourceIdentity(volumeSerial, fileIndexHigh, fileIndexLow);

            if (expectedFileIdentity != null) {
                if (!actualFileIdentity.equals(expectedFileIdentity)) {
                    throw new IllegalStateException("File object identity mismatch (expected "
                            + expectedFileIdentity.toIdentityString() + ", observed " + actualFileIdentity.toIdentityString()
                            + "): wrong object or same-path replacement");
                }
            }

            long fileSizeHigh = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, 32));
            long fileSizeLow = Integer.toUnsignedLong(fileInfo.get(ValueLayout.JAVA_INT, 36));
            long fileSize = (fileSizeHigh << 32) | fileSizeLow;
            if (fileSize > maxSourceBytes) {
                throw new IllegalArgumentException("File size exceeds configured maximum source bytes: " + fileSize + " > " + maxSourceBytes);
            }

            // Step 5: Read bytes directly through open handle with streaming SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            java.io.ByteArrayOutputStream contentBaos = new java.io.ByteArrayOutputStream();
            int bufSize = 65536;
            MemorySegment buffer = arena.allocate(bufSize);
            MemorySegment bytesReadSeg = arena.allocate(ValueLayout.JAVA_INT);
            long totalRead = 0;

            while (true) {
                if (cancelRequested != null && cancelRequested.getAsBoolean()) {
                    throw new CancellationException("Source acquisition cancelled during read");
                }
                bytesReadSeg.set(ValueLayout.JAVA_INT, 0, 0);
                int readOk = (int) readFile.invokeExact(handle, buffer, bufSize, bytesReadSeg, MemorySegment.NULL);
                if (readOk == 0) {
                    int lastErr = getLastError != null ? (int) getLastError.invokeExact() : -1;
                    throw new IllegalStateException("Win32 ReadFile failed with error " + lastErr + " for: " + normTarget.getFileName());
                }
                int bytesRead = bytesReadSeg.get(ValueLayout.JAVA_INT, 0);
                if (bytesRead <= 0) {
                    break;
                }
                totalRead += bytesRead;
                if (totalRead > maxSourceBytes) {
                    throw new IllegalArgumentException("File size exceeds configured maximum source bytes: " + totalRead + " > " + maxSourceBytes);
                }
                byte[] chunk = buffer.asSlice(0, bytesRead).toArray(ValueLayout.JAVA_BYTE);
                digest.update(chunk);
                contentBaos.write(chunk);
            }

            // Step 6: Post-read verification while handle is STILL open
            MemorySegment postInfo = arena.allocate(FILE_INFORMATION);
            int postSuccess = (int) getFileInformationByHandle.invokeExact(handle, postInfo);
            if (postSuccess == 0) {
                throw new IllegalStateException("Failed to re-query file information after read for: " + normTarget.getFileName());
            }
            int postVol = postInfo.get(ValueLayout.JAVA_INT, 28);
            int postIdxHigh = postInfo.get(ValueLayout.JAVA_INT, 44);
            int postIdxLow = postInfo.get(ValueLayout.JAVA_INT, 48);
            long postLinks = Integer.toUnsignedLong(postInfo.get(ValueLayout.JAVA_INT, LINK_COUNT_OFFSET));
            long postSizeHigh = Integer.toUnsignedLong(postInfo.get(ValueLayout.JAVA_INT, 32));
            long postSizeLow = Integer.toUnsignedLong(postInfo.get(ValueLayout.JAVA_INT, 36));
            long postSize = (postSizeHigh << 32) | postSizeLow;

            if (postVol != volumeSerial || postIdxHigh != fileIndexHigh || postIdxLow != fileIndexLow) {
                throw new IllegalStateException("File identity changed during read operation: torn capture");
            }
            if (postLinks != 1) {
                throw new IllegalStateException("Hard link count changed during read operation: " + postLinks);
            }
            if (postSize != totalRead) {
                throw new IllegalStateException("File size changed during read operation: expected " + totalRead + " but was " + postSize);
            }

            // Re-verify ancestor directory chain directly on held directory handles
            for (int i = 0; i < heldAncestorHandles.size(); i++) {
                MemorySegment dirH = heldAncestorHandles.get(i);
                DirectoryIdentity origId = ancestorChain.get(i);
                MemorySegment recheckInfo = arena.allocate(FILE_INFORMATION);
                int recheckSuccess = (int) getFileInformationByHandle.invokeExact(dirH, recheckInfo);
                if (recheckSuccess == 0) {
                    throw new IllegalStateException("Failed to query ancestor directory information on held handle for: " + origId.path());
                }
                int recheckVol = recheckInfo.get(ValueLayout.JAVA_INT, 28);
                int recheckHigh = recheckInfo.get(ValueLayout.JAVA_INT, 44);
                int recheckLow = recheckInfo.get(ValueLayout.JAVA_INT, 48);
                if (recheckVol != origId.volumeSerialNumber() || recheckHigh != origId.fileIndexHigh() || recheckLow != origId.fileIndexLow()) {
                    throw new IllegalStateException("Ancestor directory identity changed on held handle: " + origId.path());
                }
            }

            String contentHash = HexFormat.of().formatHex(digest.digest());
            byte[] contentBytes = contentBaos.toByteArray();

            WindowsProtectedFileHandle protectedHandle = new WindowsProtectedFileHandle(
                    realTarget, actualFileIdentity.toIdentityString(), contentHash, contentBytes,
                    handle, heldAncestorHandles, arena, closeHandle, getLastError);
            return new AcquiredSource(realRoot, realTarget, actualRootId, Collections.unmodifiableList(ancestorChain),
                    actualFileIdentity, contentHash, contentBytes, contentBytes.length, protectedHandle);
        } catch (Throwable t) {
            if (acquiredHandle.address() != 0 && acquiredHandle.address() != -1L) {
                try {
                    int res = (int) closeHandle.invokeExact(acquiredHandle);
                    if (res == 0) {
                        int err = getLastError != null ? (int) getLastError.invokeExact() : -1;
                        log.warn("Win32 CloseHandle returned 0 for file {}: error {}", normTarget.getFileName(), err);
                    }
                } catch (Throwable cleanupFailure) {
                    t.addSuppressed(cleanupFailure);
                }
            }
            for (int i = heldAncestorHandles.size() - 1; i >= 0; i--) {
                MemorySegment dirH = heldAncestorHandles.get(i);
                if (dirH.address() != 0 && dirH.address() != -1L) {
                    try {
                        int res = (int) closeHandle.invokeExact(dirH);
                        if (res == 0) {
                            int err = getLastError != null ? (int) getLastError.invokeExact() : -1;
                            log.warn("Win32 CloseHandle returned 0 for ancestor directory: error {}", err);
                        }
                    } catch (Throwable cleanupFailure) {
                        t.addSuppressed(cleanupFailure);
                    }
                }
            }
            if (arena.scope().isAlive()) {
                arena.close();
            }
            if (t instanceof RuntimeException re) throw re;
            if (t instanceof Error err) throw err;
            throw new IllegalStateException("Failed to acquire Windows native protected source: " + normTarget.getFileName(), t);
        }
    }

    private MemorySegment openChildRelativeNt(
            Arena arena,
            MemorySegment parentHandle,
            String childName,
            boolean isDirectory,
            Path pathForError) {
        MemorySegment nameWide = arena.allocateFrom(childName, StandardCharsets.UTF_16LE);
        MemorySegment unicodeStr = arena.allocate(UNICODE_STRING);
        short lenBytes = (short) (childName.length() * 2);
        unicodeStr.set(ValueLayout.JAVA_SHORT, 0, lenBytes);
        unicodeStr.set(ValueLayout.JAVA_SHORT, 2, (short) (lenBytes + 2));
        unicodeStr.set(ValueLayout.ADDRESS, 8, nameWide);

        MemorySegment objAttr = arena.allocate(OBJECT_ATTRIBUTES);
        objAttr.set(ValueLayout.JAVA_INT, 0, 48);
        objAttr.set(ValueLayout.ADDRESS, 8, parentHandle);
        objAttr.set(ValueLayout.ADDRESS, 16, unicodeStr);
        objAttr.set(ValueLayout.JAVA_INT, 24, 0x00000040); // OBJ_CASE_INSENSITIVE

        MemorySegment ioStatus = arena.allocate(IO_STATUS_BLOCK);
        MemorySegment childHandlePtr = arena.allocate(ValueLayout.ADDRESS);

        int desiredAccess = 0x80000000 | 0x00100000; // GENERIC_READ | SYNCHRONIZE
        int shareMode = isDirectory ? 0x00000003 : 0x00000001; // Dir: READ|WRITE; File: READ
        int createDisposition = 0x00000001; // FILE_OPEN
        int createOptions = (isDirectory ? 0x00000001 : 0x00000040) // FILE_DIRECTORY_FILE or FILE_NON_DIRECTORY_FILE
                | 0x00200000 // FILE_OPEN_REPARSE_POINT (do not follow reparse points!)
                | 0x00000020; // FILE_SYNCHRONOUS_IO_NONALERT

        try {
            int status = (int) ntCreateFile.invokeExact(
                    childHandlePtr,
                    desiredAccess,
                    objAttr,
                    ioStatus,
                    MemorySegment.NULL,
                    0,
                    shareMode,
                    createDisposition,
                    createOptions,
                    MemorySegment.NULL,
                    0
            );

            if (status != 0) {
                int dosError = (int) rtlNtStatusToDosError.invokeExact(status);
                if (dosError == 32 || dosError == 0) { // ERROR_SHARING_VIOLATION
                    throw new IllegalStateException("File is already locked or open by a concurrent writer: " + pathForError.getFileName());
                } else if (dosError == 2 || dosError == 3) { // ERROR_FILE_NOT_FOUND or ERROR_PATH_NOT_FOUND
                    throw new NoSuchElementException("File does not exist: " + pathForError.getFileName());
                } else if (dosError == 5) { // ERROR_ACCESS_DENIED
                    throw new IllegalStateException("Access denied opening component: " + pathForError.getFileName());
                } else {
                    throw new IllegalStateException("Native NtCreateFile failed with NTSTATUS 0x" + Integer.toHexString(status)
                            + " (Win32 error " + dosError + ") for: " + pathForError.getFileName());
                }
            }
            return childHandlePtr.get(ValueLayout.ADDRESS, 0);
        } catch (Throwable t) {
            if (t instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Failed to call NtCreateFile for " + pathForError.getFileName(), t);
        }
    }

    public record SourceIdentity(int volumeSerialNumber, int fileIndexHigh, int fileIndexLow) {
        public String toIdentityString() {
            return volumeSerialNumber + ":" + Integer.toUnsignedString(fileIndexHigh) + ":" + Integer.toUnsignedString(fileIndexLow);
        }
    }

    public record DirectoryIdentity(Path path, int volumeSerialNumber, int fileIndexHigh, int fileIndexLow) {
        public String toIdentityString() {
            return volumeSerialNumber + ":" + Integer.toUnsignedString(fileIndexHigh) + ":" + Integer.toUnsignedString(fileIndexLow);
        }
    }

    public record AcquiredSource(
            Path allowedRoot,
            Path targetPath,
            DirectoryIdentity rootIdentity,
            List<DirectoryIdentity> ancestorChain,
            SourceIdentity fileIdentity,
            String contentHash,
            byte[] contentBytes,
            long byteLength,
            ProtectedFileHandle handle
    ) implements AutoCloseable {
        public String getContentAsUtf8() {
            return new String(contentBytes, StandardCharsets.UTF_8);
        }

        @Override
        public void close() {
            if (handle != null) {
                handle.close();
            }
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
        private final List<MemorySegment> heldAncestorHandles;
        private final Arena arena;
        private final MethodHandle closeHandle;
        private final MethodHandle getLastError;
        private boolean closed = false;

        WindowsProtectedFileHandle(
                Path path,
                String fileIdentity,
                String contentHash,
                byte[] contentBytes,
                MemorySegment handle,
                Arena arena,
                MethodHandle closeHandle,
                MethodHandle getLastError) {
            this(path, fileIdentity, contentHash, contentBytes, handle, Collections.emptyList(), arena, closeHandle, getLastError);
        }

        WindowsProtectedFileHandle(
                Path path,
                String fileIdentity,
                String contentHash,
                byte[] contentBytes,
                MemorySegment handle,
                List<MemorySegment> heldAncestorHandles,
                Arena arena,
                MethodHandle closeHandle,
                MethodHandle getLastError) {
            this.path = path;
            this.fileIdentity = fileIdentity;
            this.contentHash = contentHash;
            this.contentBytes = contentBytes;
            this.handle = handle;
            this.heldAncestorHandles = heldAncestorHandles;
            this.arena = arena;
            this.closeHandle = closeHandle;
            this.getLastError = getLastError;
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
                List<Throwable> closeErrors = new ArrayList<>();
                try {
                    int res = (int) closeHandle.invokeExact(handle);
                    if (res == 0) {
                        int err = getLastError != null ? (int) getLastError.invokeExact() : -1;
                        log.warn("Win32 CloseHandle returned 0 for file {}: error {}", path.getFileName(), err);
                        closeErrors.add(new IllegalStateException("Win32 CloseHandle returned 0 for file " + path.getFileName() + ": error " + err));
                    }
                } catch (Throwable t) {
                    log.error("Failed to close Windows native file handle for {}", path.getFileName(), t);
                    closeErrors.add(t);
                }
                for (int i = heldAncestorHandles.size() - 1; i >= 0; i--) {
                    MemorySegment dirH = heldAncestorHandles.get(i);
                    try {
                        int res = (int) closeHandle.invokeExact(dirH);
                        if (res == 0) {
                            int err = getLastError != null ? (int) getLastError.invokeExact() : -1;
                            log.warn("Win32 CloseHandle returned 0 for held ancestor directory: error {}", err);
                            closeErrors.add(new IllegalStateException("Win32 CloseHandle returned 0 for ancestor directory: error " + err));
                        }
                    } catch (Throwable t) {
                        log.error("Failed to close held ancestor directory handle", t);
                        closeErrors.add(t);
                    }
                }
                if (arena.scope().isAlive()) {
                    arena.close();
                }
                if (!closeErrors.isEmpty()) {
                    var first = closeErrors.getFirst();
                    for (int i = 1; i < closeErrors.size(); i++) {
                        first.addSuppressed(closeErrors.get(i));
                    }
                    if (first instanceof RuntimeException re) throw re;
                    throw new IllegalStateException("Native handle close failure", first);
                }
            }
        }
    }
}
