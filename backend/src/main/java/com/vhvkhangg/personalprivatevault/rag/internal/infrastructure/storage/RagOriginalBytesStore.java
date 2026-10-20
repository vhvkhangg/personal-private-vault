package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Protected RAG-managed ORIGINAL_BYTES storage.
 *
 * <p>Implements ADR-0022 G3 immutable revision storage:
 * <ul>
 *   <li>Private managed root outside source trees, workspace, and worker scratch</li>
 *   <li>Opaque S/R/attempt UUID paths with strict hierarchy containment and non-reparse verification</li>
 *   <li>Exclusive temporary capture writing with streaming SHA-256 and length limits</li>
 *   <li>Atomic no-overwrite sealing into immutable revision storage via Win32 MoveFileExW</li>
 *   <li>Durable flush to persistent storage (force/sync)</li>
 *   <li>Immutable JSON manifest with cryptographic integrity validation</li>
 *   <li>Detection and denial of missing, tampered, or swapped objects</li>
 *   <li>Explicit cleanup-pending states when deletion cannot be verified</li>
 * </ul>
 * </p>
 */
@Component
public class RagOriginalBytesStore {

    private static final Logger log = LoggerFactory.getLogger(RagOriginalBytesStore.class);
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");

    private static final ObjectMapper STRICT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .build();

    private static final Linker LINKER = Linker.nativeLinker();
    private static final MemoryLayout CAPTURE_LAYOUT = Linker.Option.captureStateLayout();
    private static final long LAST_ERROR_OFFSET = CAPTURE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("GetLastError"));

    private final Path storageRoot;
    private final Path tempDir;
    private final Path revisionsDir;
    private final long maxSourceBytes;

    private final MethodHandle moveFileExW;
    private final Set<UUID> activeCaptures = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Set<UUID> activeRevisions = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private Stream<Path> recoveryScan;
    private java.util.Iterator<Path> recoveryIterator;

    public void beginCapture(UUID id) { activeCaptures.add(id); }
    public void endCapture(UUID id) { activeCaptures.remove(id); }
    public void beginRevision(UUID id) { activeRevisions.add(id); }
    public void endRevision(UUID id) { activeRevisions.remove(id); }
    public boolean active(StorageEntry entry) {
        return entry.temporary() ? activeCaptures.contains(entry.artifactId()) : activeRevisions.contains(entry.artifactId());
    }

    public record StorageEntry(Path path, UUID sourceId, UUID artifactId, boolean temporary) {}

    /** Lazy bounded traversal. Files left after failed deletion are the durable retry inventory. */
    public synchronized List<StorageEntry> nextRecoveryBatch(int limit) {
        if (limit < 1 || limit > 100) throw new IllegalArgumentException("Invalid recovery batch size");
        List<StorageEntry> batch = new ArrayList<>();
        try {
            if (recoveryScan == null) {
                verifyNoReparseInHierarchy(storageRoot, revisionsDir);
                verifyNoReparseInHierarchy(storageRoot, tempDir);
                recoveryScan = Files.walk(storageRoot, 3);
                recoveryIterator = recoveryScan.iterator();
            }
            int visited = 0;
            while (visited++ < limit && recoveryIterator.hasNext()) {
                Path path = recoveryIterator.next();
                if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) continue;
                verifyContainment(path);
                String name = path.getFileName().toString();
                try {
                    if (path.getParent().equals(tempDir) && name.endsWith(".tmp")) {
                        batch.add(new StorageEntry(path, null, UUID.fromString(name.substring(0, name.length()-4)), true));
                    } else if (path.getParent().getParent().equals(revisionsDir)) {
                        String id = name.endsWith(".orig") ? name.substring(0, name.length()-5)
                                : name.endsWith(".manifest.json") ? name.substring(0, name.length()-14) : null;
                        if (id != null) batch.add(new StorageEntry(path, UUID.fromString(path.getParent().getFileName().toString()), UUID.fromString(id), false));
                    }
                } catch (IllegalArgumentException invalidName) {
                    // Unknown names confer no authority. Retain for explicit operator disposition.
                    log.warn("Unrecognized managed snapshot artifact; recovery required");
                }
            }
            if (!recoveryIterator.hasNext()) resetRecoveryScan();
            return List.copyOf(batch);
        } catch (IOException | java.io.UncheckedIOException failure) {
            resetRecoveryScan();
            throw new SnapshotStorageException("Snapshot inventory unavailable");
        }
    }

    public synchronized void resetRecoveryScan() {
        if (recoveryScan != null) recoveryScan.close();
        recoveryScan = null;
        recoveryIterator = null;
    }

    @Autowired
    public RagOriginalBytesStore(
            @Value("${ppv.rag.storage.snapshot-root:}") String configuredRoot,
            @Value("${ppv.rag.limits.max-source-bytes:52428800}") long maxSourceBytes) {
        this(resolveRoot(configuredRoot), maxSourceBytes);
    }

    public RagOriginalBytesStore(Path storageRoot, long maxSourceBytes) {
        this(storageRoot, maxSourceBytes, true);
    }

    public RagOriginalBytesStore(Path storageRoot, long maxSourceBytes, boolean nativeMoveAvailable) {
        this.storageRoot = Objects.requireNonNull(storageRoot, "storageRoot must not be null").toAbsolutePath().normalize();
        this.maxSourceBytes = maxSourceBytes > 0 ? maxSourceBytes : 52428800L;
        this.tempDir = this.storageRoot.resolve("temp");
        this.revisionsDir = this.storageRoot.resolve("revisions");
        try {
            Files.createDirectories(this.tempDir);
            Files.createDirectories(this.revisionsDir);
        } catch (IOException e) {
            throw new SnapshotStorageException("Failed to initialize RAG storage root at: " + this.storageRoot, e);
        }

        MethodHandle mfe = null;
        if (IS_WINDOWS && nativeMoveAvailable) {
            try {
                SymbolLookup kernel32 = SymbolLookup.libraryLookup("Kernel32.dll", Arena.global());
                mfe = LINKER.downcallHandle(
                        kernel32.find("MoveFileExW").orElseThrow(),
                        FunctionDescriptor.of(
                                ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS,
                                ValueLayout.ADDRESS,
                                ValueLayout.JAVA_INT
                        ),
                        Linker.Option.captureCallState("GetLastError")
                );
            } catch (Throwable t) {
                log.warn("Failed to bind Win32 MoveFileExW via FFM: {}", t.getMessage());
            }
        }
        this.moveFileExW = mfe;
    }

    private static Path resolveRoot(String configuredRoot) {
        if (configuredRoot != null && !configuredRoot.isBlank()) {
            return Path.of(configuredRoot);
        }
        String userHome = System.getProperty("user.home", ".");
        return Path.of(userHome, ".ppv-vault", "rag-store");
    }

    public Path getStorageRoot() {
        return storageRoot;
    }

    public Path getTempDir() {
        return tempDir;
    }

    public Path getRevisionsDir() {
        return revisionsDir;
    }

    public long getMaxSourceBytes() {
        return maxSourceBytes;
    }

    private void verifyContainment(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(storageRoot) || normalized.equals(storageRoot)) {
            throw new SnapshotStorageException("Path containment violation: path escapes storage root: " + path);
        }
        verifyNoReparseInHierarchy(storageRoot, normalized);
    }

    private void verifyNoReparseInHierarchy(Path root, Path target) {
        Path current = root;
        checkReparse(current);
        Path relative = root.relativize(target);
        for (Path part : relative) {
            current = current.resolve(part);
            checkReparse(current);
        }
    }

    private void checkReparse(Path path) {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attrs.isSymbolicLink() || attrs.isOther()) {
                throw new SnapshotStorageException("Reparse point or symlink detected in storage hierarchy: " + path);
            }
        } catch (IOException e) {
            throw new SnapshotStorageException("Failed to verify attributes for containment: " + path, e);
        }
    }

    /**
     * Exclusively writes bytes to a temporary capture file, computing streaming SHA-256 and enforcing byte limits.
     * Prevents deletion of unowned/colliding pre-existing files on failure.
     */
    public TempCapture writeTemp(UUID captureId, InputStream inputStream, Long expectedLength, String expectedSha256) {
        Objects.requireNonNull(captureId, "captureId must not be null");
        Objects.requireNonNull(inputStream, "inputStream must not be null");

        Path targetTemp = tempDir.resolve(captureId + ".tmp");
        verifyContainment(targetTemp);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 message digest algorithm not available", e);
        }

        long totalBytes = 0;
        boolean fileCreated = false;
        try (FileChannel channel = FileChannel.open(targetTemp, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            fileCreated = true;
            byte[] buffer = new byte[65536];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                totalBytes += read;
                if (totalBytes > maxSourceBytes) {
                    throw new IllegalArgumentException("Capture size exceeds maximum source bytes limit: "
                            + totalBytes + " > " + maxSourceBytes);
                }
                digest.update(buffer, 0, read);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, read);
                while (byteBuffer.hasRemaining()) {
                    channel.write(byteBuffer);
                }
            }
            channel.force(true);
        } catch (Throwable t) {
            if (fileCreated) {
                try {
                    Files.deleteIfExists(targetTemp);
                } catch (IOException ignored) {}
            }
            if (t instanceof RuntimeException re) throw re;
            throw new SnapshotStorageException("Failed to write temporary capture: " + captureId, t);
        }

        String actualSha256 = HexFormat.of().formatHex(digest.digest());

        if (expectedLength != null && totalBytes != expectedLength) {
            cleanupTemp(captureId);
            throw new IllegalStateException("Temp capture length mismatch: expected " + expectedLength + " but captured " + totalBytes);
        }
        if (expectedSha256 != null && !actualSha256.equalsIgnoreCase(expectedSha256)) {
            cleanupTemp(captureId);
            throw new IllegalStateException("Temp capture SHA-256 mismatch");
        }

        return new TempCapture(captureId, targetTemp, actualSha256, totalBytes, Instant.now());
    }

    /**
     * Atomically seals a temporary capture into an immutable revision with verified durability.
     * Enforces kernel-level no-overwrite atomic move via Win32 MoveFileExW without replace flag.
     */
    public StoredSnapshot sealRevision(UUID sourceId, UUID revisionId, UUID captureId, String sha256, long byteLength) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(revisionId, "revisionId must not be null");
        Objects.requireNonNull(captureId, "captureId must not be null");
        Objects.requireNonNull(sha256, "sha256 must not be null");

        Path sourceTemp = tempDir.resolve(captureId + ".tmp");
        verifyContainment(sourceTemp);

        if (!Files.exists(sourceTemp, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotMissingException("Temporary capture not found for sealing: " + captureId);
        }

        // Verify temp size and hash before sealing to prevent temp tampering
        try {
            long tempSize = Files.size(sourceTemp);
            if (tempSize != byteLength) {
                throw new IllegalStateException("Temporary capture size does not match expected length: "
                        + tempSize + " != " + byteLength);
            }
            MessageDigest verifyDigest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(sourceTemp)) {
                byte[] buf = new byte[65536];
                int r;
                while ((r = is.read(buf)) != -1) {
                    verifyDigest.update(buf, 0, r);
                }
            }
            String tempSha = HexFormat.of().formatHex(verifyDigest.digest());
            if (!tempSha.equalsIgnoreCase(sha256)) {
                throw new IllegalStateException("Temporary capture SHA-256 does not match expected seal digest");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        } catch (IOException e) {
            throw new SnapshotStorageException("Failed to verify temporary capture before sealing: " + sourceTemp, e);
        }

        Path sourceRevDir = revisionsDir.resolve(sourceId.toString());
        verifyContainment(sourceRevDir);
        try {
            Files.createDirectories(sourceRevDir);
        } catch (IOException e) {
            throw new SnapshotStorageException("Failed to create revision directory for source: " + sourceId, e);
        }

        Path targetRevisionFile = sourceRevDir.resolve(revisionId + ".orig");
        Path targetManifestFile = sourceRevDir.resolve(revisionId + ".manifest.json");
        verifyContainment(targetRevisionFile);
        verifyContainment(targetManifestFile);

        if (Files.exists(targetManifestFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotStorageException("Atomic seal denied: manifest file already exists (no overwrite): " + targetManifestFile);
        }

        Instant sealedAt = Instant.now();

        // 1. Atomic no-overwrite move into immutable revision path
        atomicMoveNoOverwrite(sourceTemp, targetRevisionFile);

        // 2. Create immutable manifest JSON
        String manifestJson = "{\n"
                + "  \"sourceId\": \"" + sourceId + "\",\n"
                + "  \"revisionId\": \"" + revisionId + "\",\n"
                + "  \"captureId\": \"" + captureId + "\",\n"
                + "  \"sha256\": \"" + sha256 + "\",\n"
                + "  \"byteLength\": " + byteLength + ",\n"
                + "  \"sealedAt\": \"" + sealedAt + "\",\n"
                + "  \"sealStatus\": \"SEALED\"\n"
                + "}\n";

        byte[] manifestBytes = manifestJson.getBytes(StandardCharsets.UTF_8);
        try (FileChannel channel = FileChannel.open(targetManifestFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            ByteBuffer buf = ByteBuffer.wrap(manifestBytes);
            while (buf.hasRemaining()) {
                channel.write(buf);
            }
            channel.force(true);
        } catch (IOException e) {
            // Manifest write failed or target already exists; clean up targetRevisionFile to prevent partial seal
            try {
                targetRevisionFile.toFile().setWritable(true);
                Files.deleteIfExists(targetRevisionFile);
            } catch (IOException ignored) {}
            throw new SnapshotStorageException("Failed to create immutable manifest: " + targetManifestFile, e);
        }

        // 3. Mark read-only on filesystem
        try {
            targetRevisionFile.toFile().setReadOnly();
            targetManifestFile.toFile().setReadOnly();
        } catch (Exception e) {
            log.warn("Could not apply read-only attribute to sealed files: {}", e.getMessage());
        }

        return new StoredSnapshot(sourceId, revisionId, captureId, sha256, byteLength, targetRevisionFile, targetManifestFile, sealedAt);
    }

    private void atomicMoveNoOverwrite(Path source, Path target) {
        if (IS_WINDOWS && moveFileExW != null) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment capture = arena.allocate(CAPTURE_LAYOUT);
                MemorySegment srcSeg = arena.allocateFrom(source.toAbsolutePath().normalize().toString() + "\0", StandardCharsets.UTF_16LE);
                MemorySegment dstSeg = arena.allocateFrom(target.toAbsolutePath().normalize().toString() + "\0", StandardCharsets.UTF_16LE);
                // dwFlags = 0 means NO REPLACE_EXISTING (kernel fails if destination exists)
                int success = (int) moveFileExW.invokeExact(capture, srcSeg, dstSeg, 0);
                if (success == 0) {
                    int err = capture.get(ValueLayout.JAVA_INT, LAST_ERROR_OFFSET);
                    if (err == 183 || err == 80) { // ERROR_ALREADY_EXISTS (183) or ERROR_FILE_EXISTS (80)
                        throw new SnapshotStorageException("Atomic seal denied: revision file already exists (no overwrite, error " + err + "): " + target);
                    }
                    if (err == 32) { // ERROR_SHARING_VIOLATION
                        throw new SnapshotStorageException("Atomic seal denied: sharing violation (error 32) for target: " + target);
                    }
                    if (err == 5) { // ERROR_ACCESS_DENIED
                        throw new SnapshotStorageException("Atomic seal denied: access denied (error 5) for target: " + target);
                    }
                    throw new SnapshotStorageException("Win32 MoveFileExW failed with error code: " + err + " for target: " + target);
                }
                return;
            } catch (SnapshotStorageException sse) {
                throw sse;
            } catch (Throwable t) {
                throw new SnapshotStorageException("Native atomic move failed for: " + target, t);
            }
        }

        // A link publishes the already closed/verified file with CREATE_NEW semantics.
        // Unlike ATOMIC_MOVE, createLink never replaces a colliding target. Unsupported => deny.
        try {
            Files.createLink(target, source);
            Files.delete(source);
        } catch (IOException | UnsupportedOperationException failure) {
            throw new SnapshotStorageException("No-clobber seal unavailable or target already exists");
        }
    }

    public record ManifestPayload(
            UUID sourceId,
            UUID revisionId,
            UUID captureId,
            String sha256,
            Long byteLength,
            Instant sealedAt,
            String sealStatus
    ) {}

    /**
     * Strictly parses and validates manifest file content against expected values.
     */
    public ManifestMetadata validateManifest(
            Path manifestFile,
            UUID expectedSourceId,
            UUID expectedRevisionId,
            UUID expectedCaptureId,
            String expectedSha256,
            Long expectedLength) {
        verifyContainment(manifestFile);
        if (!Files.exists(manifestFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotMissingException("Manifest file not found: " + manifestFile);
        }

        String manifestContent;
        try {
            manifestContent = Files.readString(manifestFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SnapshotIntegrityException("Failed to read manifest file: " + manifestFile, e);
        }

        ManifestPayload payload;
        try {
            payload = STRICT_MAPPER.readValue(manifestContent, ManifestPayload.class);
        } catch (JacksonException e) {
            throw new SnapshotIntegrityException("Invalid manifest JSON format");
        }

        if (payload == null
                || payload.sourceId() == null
                || payload.revisionId() == null
                || payload.captureId() == null
                || payload.sha256() == null
                || payload.byteLength() == null
                || payload.sealedAt() == null
                || payload.sealStatus() == null) {
            throw new SnapshotIntegrityException("Manifest missing required field(s): " + manifestFile);
        }

        if (!SHA256_HEX_PATTERN.matcher(payload.sha256()).matches()) {
            throw new SnapshotIntegrityException("Manifest sha256 is invalid");
        }

        if (payload.byteLength() < 0 || payload.byteLength() > maxSourceBytes) {
            throw new SnapshotIntegrityException("Manifest byteLength out of valid bounds: " + payload.byteLength());
        }

        if (!"SEALED".equals(payload.sealStatus())) {
            throw new SnapshotIntegrityException("Manifest sealStatus is not SEALED: " + payload.sealStatus());
        }

        if (expectedSourceId != null && !payload.sourceId().equals(expectedSourceId)) {
            throw new SnapshotIntegrityException("Manifest sourceId mismatch: expected " + expectedSourceId + " but found " + payload.sourceId());
        }
        if (expectedRevisionId != null && !payload.revisionId().equals(expectedRevisionId)) {
            throw new SnapshotIntegrityException("Manifest revisionId mismatch: expected " + expectedRevisionId + " but found " + payload.revisionId());
        }
        if (expectedCaptureId != null && !payload.captureId().equals(expectedCaptureId)) {
            throw new SnapshotIntegrityException("Manifest captureId mismatch: expected " + expectedCaptureId + " but found " + payload.captureId());
        }
        if (expectedSha256 != null && !payload.sha256().equalsIgnoreCase(expectedSha256)) {
            throw new SnapshotIntegrityException("Manifest SHA-256 does not match expected");
        }
        if (expectedLength != null && payload.byteLength().longValue() != expectedLength.longValue()) {
            throw new SnapshotIntegrityException("Manifest byteLength does not match expected: " + payload.byteLength() + " != " + expectedLength);
        }

        return new ManifestMetadata(
                payload.sourceId(),
                payload.revisionId(),
                payload.captureId(),
                payload.sha256(),
                payload.byteLength(),
                payload.sealedAt(),
                payload.sealStatus()
        );
    }

    public ManifestMetadata validateManifest(
            Path manifestFile,
            UUID expectedSourceId,
            UUID expectedRevisionId,
            String expectedSha256,
            Long expectedLength) {
        return validateManifest(manifestFile, expectedSourceId, expectedRevisionId, null, expectedSha256, expectedLength);
    }

    public ManifestMetadata readAndValidateManifest(UUID sourceId, UUID revisionId) {
        Path sourceRevDir = revisionsDir.resolve(sourceId.toString());
        Path targetManifestFile = sourceRevDir.resolve(revisionId + ".manifest.json");
        return validateManifest(targetManifestFile, sourceId, revisionId, null, null);
    }

    /**
     * Verifies cryptographic integrity and metadata continuity of a sealed revision.
     */
    public boolean verifyIntegrity(UUID sourceId, UUID revisionId, UUID expectedCaptureId, String expectedSha256, long expectedLength) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(revisionId, "revisionId must not be null");
        Objects.requireNonNull(expectedSha256, "expectedSha256 must not be null");

        Path sourceRevDir = revisionsDir.resolve(sourceId.toString());
        Path targetRevisionFile = sourceRevDir.resolve(revisionId + ".orig");
        Path targetManifestFile = sourceRevDir.resolve(revisionId + ".manifest.json");
        verifyContainment(targetRevisionFile);
        verifyContainment(targetManifestFile);

        if (!Files.exists(targetRevisionFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotMissingException("Sealed snapshot file is missing for revision: " + revisionId);
        }
        if (!Files.exists(targetManifestFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotMissingException("Manifest file is missing for revision: " + revisionId);
        }

        // 1. Strict manifest validation
        validateManifest(targetManifestFile, sourceId, revisionId, expectedCaptureId, expectedSha256, expectedLength);

        // 2. Compute actual stored byte length and SHA-256
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }

        long actualLength = 0;
        try (InputStream is = Files.newInputStream(targetRevisionFile)) {
            byte[] buffer = new byte[65536];
            int read;
            while ((read = is.read(buffer)) != -1) {
                actualLength += read;
                digest.update(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new SnapshotIntegrityException("Failed to read sealed revision bytes for: " + revisionId, e);
        }

        if (actualLength != expectedLength) {
            throw new SnapshotIntegrityException("Snapshot length mismatch (tampered or truncated): expected "
                    + expectedLength + " but computed " + actualLength);
        }

        String actualSha256 = HexFormat.of().formatHex(digest.digest());
        if (!actualSha256.equalsIgnoreCase(expectedSha256)) {
            throw new SnapshotIntegrityException("Snapshot SHA-256 mismatch (tampered content)");
        }

        return true;
    }

    public boolean verifyIntegrity(UUID sourceId, UUID revisionId, String expectedSha256, long expectedLength) {
        return verifyIntegrity(sourceId, revisionId, null, expectedSha256, expectedLength);
    }

    /**
     * Reads sealed revision bytes in a single pass while verifying SHA-256 and length
     * against independently trusted expected values and strict manifest metadata.
     * Eliminates verify-then-reopen TOCTOU gap.
     */
    public byte[] readRevisionBytes(UUID sourceId, UUID revisionId, UUID expectedCaptureId, String expectedSha256, long expectedLength) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(revisionId, "revisionId must not be null");
        Objects.requireNonNull(expectedSha256, "expectedSha256 must not be null");

        Path sourceRevDir = revisionsDir.resolve(sourceId.toString());
        Path targetRevisionFile = sourceRevDir.resolve(revisionId + ".orig");
        Path targetManifestFile = sourceRevDir.resolve(revisionId + ".manifest.json");
        verifyContainment(targetRevisionFile);
        verifyContainment(targetManifestFile);

        if (!Files.exists(targetRevisionFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotMissingException("Sealed snapshot file is missing for revision: " + revisionId);
        }
        if (!Files.exists(targetManifestFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new SnapshotMissingException("Manifest file is missing for revision: " + revisionId);
        }

        // 1. Strict manifest validation against expected parameters
        validateManifest(targetManifestFile, sourceId, revisionId, expectedCaptureId, expectedSha256, expectedLength);

        // 2. Single-pass stream read and hash verification directly from opened stream
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }

        byte[] resultBytes;
        try (InputStream is = Files.newInputStream(targetRevisionFile)) {
            if (expectedLength > maxSourceBytes) {
                throw new SnapshotIntegrityException("Expected length exceeds configured maxSourceBytes: "
                        + expectedLength + " > " + maxSourceBytes);
            }
            byte[] buffer = new byte[(int) expectedLength];
            int totalRead = 0;
            int r;
            while (totalRead < buffer.length && (r = is.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
                totalRead += r;
            }
            if (totalRead != expectedLength) {
                throw new SnapshotIntegrityException("Snapshot length mismatch (truncated): expected "
                        + expectedLength + " but read " + totalRead);
            }
            if (is.read() != -1) {
                throw new SnapshotIntegrityException("Snapshot length mismatch (extra bytes present beyond expected length "
                        + expectedLength + ")");
            }
            digest.update(buffer, 0, totalRead);
            resultBytes = buffer;
        } catch (SnapshotIntegrityException sie) {
            throw sie;
        } catch (IOException e) {
            throw new SnapshotStorageException("Failed to read sealed revision bytes for: " + revisionId, e);
        }

        String actualSha256 = HexFormat.of().formatHex(digest.digest());
        if (!actualSha256.equalsIgnoreCase(expectedSha256)) {
            throw new SnapshotIntegrityException("Snapshot SHA-256 mismatch (tampered content)");
        }

        return resultBytes;
    }

    public byte[] readRevisionBytes(UUID sourceId, UUID revisionId, String expectedSha256, long expectedLength) {
        return readRevisionBytes(sourceId, revisionId, null, expectedSha256, expectedLength);
    }

    /**
     * Deletes a sealed revision and its manifest, returning explicit completion or pending status.
     * Proves absence using Files.notExists rather than uncertain !Files.exists.
     */
    public DeletionReport deleteRevision(UUID sourceId, UUID revisionId) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(revisionId, "revisionId must not be null");

        Path sourceRevDir = revisionsDir.resolve(sourceId.toString());
        Path targetRevisionFile = sourceRevDir.resolve(revisionId + ".orig");
        Path targetManifestFile = sourceRevDir.resolve(revisionId + ".manifest.json");
        verifyContainment(targetRevisionFile);
        verifyContainment(targetManifestFile);

        if (Files.notExists(targetRevisionFile) && Files.notExists(targetManifestFile)) {
            return new DeletionReport(sourceId, revisionId, DeletionStatus.COMPLETED, "Files already absent");
        }

        try {
            // Unset read-only attribute if set
            targetRevisionFile.toFile().setWritable(true);
            targetManifestFile.toFile().setWritable(true);

            Files.deleteIfExists(targetRevisionFile);
            Files.deleteIfExists(targetManifestFile);

            // Clean up source directory if empty
            try (Stream<Path> stream = Files.list(sourceRevDir)) {
                if (stream.findFirst().isEmpty()) {
                    Files.deleteIfExists(sourceRevDir);
                }
            } catch (Exception ignored) {}

            if (Files.notExists(targetRevisionFile) && Files.notExists(targetManifestFile)) {
                return new DeletionReport(sourceId, revisionId, DeletionStatus.COMPLETED, "Deleted successfully");
            } else {
                return new DeletionReport(sourceId, revisionId, DeletionStatus.PENDING_INACCESSIBLE,
                        "Files remain present or absence could not be verified after delete attempt");
            }
        } catch (Throwable t) {
            log.warn("Snapshot revision {} deletion pending", revisionId);
            return new DeletionReport(sourceId, revisionId, DeletionStatus.PENDING_INACCESSIBLE,
                    "Deletion failed; retry pending");
        }
    }

    /**
     * Deletes a temporary capture file if present, reporting verified deletion.
     */
    public DeletionReport cleanupTemp(UUID captureId) {
        if (captureId == null) {
            return new DeletionReport(null, null, DeletionStatus.COMPLETED, "Null captureId");
        }
        Path tempFile = tempDir.resolve(captureId + ".tmp");
        verifyContainment(tempFile);
        if (Files.notExists(tempFile)) {
            return new DeletionReport(null, captureId, DeletionStatus.COMPLETED, "Temp file absent");
        }
        try {
            tempFile.toFile().setWritable(true);
            Files.deleteIfExists(tempFile);
            if (Files.notExists(tempFile)) {
                return new DeletionReport(null, captureId, DeletionStatus.COMPLETED, "Temp deleted successfully");
            } else {
                return new DeletionReport(null, captureId, DeletionStatus.PENDING_INACCESSIBLE, "Temp file remained after delete");
            }
        } catch (IOException e) {
            log.warn("Snapshot capture {} deletion pending", captureId);
            return new DeletionReport(null, captureId, DeletionStatus.PENDING_INACCESSIBLE, "Temp deletion pending");
        }
    }

    /**
     * Scans for orphan temporary captures, unregistered revisions, missing orig, or missing manifest.
     * Requires explicit legitimate revisions and active captures; refuses null authority input.
     */
    public List<OrphanReport> detectOrphans(Set<UUID> legitimateRevisions, Set<UUID> activeCaptures) {
        Objects.requireNonNull(legitimateRevisions, "legitimateRevisions must not be null for orphan detection");
        Objects.requireNonNull(activeCaptures, "activeCaptures must not be null for orphan detection");

        List<OrphanReport> orphans = new ArrayList<>();

        // 1. Scan temp directory for lingering .tmp files
        if (Files.exists(tempDir, LinkOption.NOFOLLOW_LINKS)) {
            checkReparse(tempDir);
            try (Stream<Path> files = Files.list(tempDir)) {
                files.filter(p -> p.getFileName().toString().endsWith(".tmp")).forEach(p -> {
                    checkReparse(p);
                    String name = p.getFileName().toString();
                    String capStr = name.substring(0, name.indexOf(".tmp"));
                    try {
                        UUID capId = UUID.fromString(capStr);
                        if (!activeCaptures.contains(capId)) {
                            orphans.add(new OrphanReport(p, "TEMP_CAPTURE", Files.size(p), Files.getLastModifiedTime(p).toInstant()));
                        }
                    } catch (IllegalArgumentException e) {
                        orphans.add(new OrphanReport(p, "INVALID_TEMP_NAME", 0, Instant.now()));
                    } catch (IOException ignored) {}
                });
            } catch (IOException e) {
                log.warn("Failed to list temp directory for orphan detection", e);
            }
        }

        // 2. Scan revisions directory for unregistered revisions, missing orig, or missing manifest
        if (Files.exists(revisionsDir, LinkOption.NOFOLLOW_LINKS)) {
            checkReparse(revisionsDir);
            try (Stream<Path> sourceDirs = Files.list(revisionsDir)) {
                sourceDirs.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)).forEach(sourceDir -> {
                    checkReparse(sourceDir);
                    try (Stream<Path> revFiles = Files.list(sourceDir)) {
                        List<Path> allInDir = revFiles.toList();
                        Set<String> fileNames = allInDir.stream().map(p -> p.getFileName().toString()).collect(Collectors.toSet());

                        for (Path p : allInDir) {
                            checkReparse(p);
                            String fname = p.getFileName().toString();
                            if (fname.endsWith(".orig")) {
                                String revStr = fname.substring(0, fname.indexOf(".orig"));
                                try {
                                    UUID revId = UUID.fromString(revStr);
                                    if (!legitimateRevisions.contains(revId)) {
                                        orphans.add(new OrphanReport(p, "UNREGISTERED_REVISION", Files.size(p), Files.getLastModifiedTime(p).toInstant()));
                                    } else {
                                        String manifestName = revStr + ".manifest.json";
                                        if (!fileNames.contains(manifestName)) {
                                            orphans.add(new OrphanReport(p, "ORPHAN_REVISION_NO_MANIFEST", Files.size(p), Files.getLastModifiedTime(p).toInstant()));
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    orphans.add(new OrphanReport(p, "INVALID_REVISION_NAME", 0, Instant.now()));
                                } catch (IOException ignored) {}
                            } else if (fname.endsWith(".manifest.json")) {
                                String revStr = fname.substring(0, fname.indexOf(".manifest.json"));
                                try {
                                    UUID revId = UUID.fromString(revStr);
                                    if (!legitimateRevisions.contains(revId)) {
                                        orphans.add(new OrphanReport(p, "UNREGISTERED_MANIFEST", Files.size(p), Files.getLastModifiedTime(p).toInstant()));
                                    } else {
                                        String origName = revStr + ".orig";
                                        if (!fileNames.contains(origName)) {
                                            orphans.add(new OrphanReport(p, "ORPHAN_MANIFEST_NO_ORIG", Files.size(p), Files.getLastModifiedTime(p).toInstant()));
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    orphans.add(new OrphanReport(p, "INVALID_MANIFEST_NAME", 0, Instant.now()));
                                } catch (IOException ignored) {}
                            }
                        }
                    } catch (IOException e) {
                        log.warn("Failed to list source directory for orphan detection: {}", sourceDir, e);
                    }
                });
            } catch (IOException e) {
                log.warn("Failed to list revisions directory for orphan detection", e);
            }
        }

        return orphans;
    }

    /**
     * Safely cleans up orphan temporary files and unreferenced revisions with containment checks.
     * Refuses to delete legitimate revision partial pairs (retains them for integrity recovery).
     */
    public List<DeletionReport> cleanupOrphans(Set<UUID> legitimateRevisions, Set<UUID> activeCaptures) {
        List<OrphanReport> orphans = detectOrphans(legitimateRevisions, activeCaptures);
        List<DeletionReport> reports = new ArrayList<>();

        for (OrphanReport orphan : orphans) {
            verifyContainment(orphan.path());

            // Missing partners of legitimate R are integrity/recovery problems, not orphan deletion permission
            if ("ORPHAN_REVISION_NO_MANIFEST".equals(orphan.orphanType()) || "ORPHAN_MANIFEST_NO_ORIG".equals(orphan.orphanType())) {
                reports.add(new DeletionReport(null, null, DeletionStatus.PENDING_INACCESSIBLE,
                        "Legitimate revision partial pair retained for integrity recovery: " + orphan.path().getFileName()));
                continue;
            }

            try {
                orphan.path().toFile().setWritable(true);
                Files.deleteIfExists(orphan.path());
                if ("UNREGISTERED_REVISION".equals(orphan.orphanType())) {
                    String origName = orphan.path().getFileName().toString();
                    String manifestName = origName.replace(".orig", ".manifest.json");
                    Path manifestPath = orphan.path().getParent().resolve(manifestName);
                    if (Files.exists(manifestPath, LinkOption.NOFOLLOW_LINKS)) {
                        verifyContainment(manifestPath);
                        manifestPath.toFile().setWritable(true);
                        Files.deleteIfExists(manifestPath);
                    }
                }
                reports.add(new DeletionReport(null, null, DeletionStatus.COMPLETED,
                        "Cleaned orphan: " + orphan.path().getFileName()));
            } catch (Exception e) {
                reports.add(new DeletionReport(null, null, DeletionStatus.PENDING_INACCESSIBLE,
                        "Failed to clean orphan " + orphan.path().getFileName() + ": " + e.getMessage()));
            }
        }

        return reports;
    }

    public record ManifestMetadata(
            UUID sourceId,
            UUID revisionId,
            UUID captureId,
            String sha256,
            long byteLength,
            Instant sealedAt,
            String sealStatus
    ) {}

    public record StoredSnapshot(
            UUID sourceId,
            UUID revisionId,
            UUID captureId,
            String sha256,
            long byteLength,
            Path storagePath,
            Path manifestPath,
            Instant sealedAt
    ) {}

    public record TempCapture(
            UUID captureId,
            Path tempPath,
            String sha256,
            long byteLength,
            Instant createdAt
    ) {}

    public enum DeletionStatus {
        COMPLETED,
        PENDING_INACCESSIBLE
    }

    public record DeletionReport(
            UUID sourceId,
            UUID revisionId,
            DeletionStatus status,
            String message
    ) {}

    public record OrphanReport(
            Path path,
            String orphanType,
            long byteLength,
            Instant lastModified
    ) {}
}
