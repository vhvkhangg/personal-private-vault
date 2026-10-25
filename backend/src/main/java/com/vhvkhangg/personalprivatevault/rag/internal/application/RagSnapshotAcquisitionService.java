package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** Finite explicit local-file acquisition under ADR-0023's application threat model. */
@Service
public class RagSnapshotAcquisitionService {
    private static final Logger log = LoggerFactory.getLogger(RagSnapshotAcquisitionService.class);
    private static final Set<String> EXTENSIONS = Set.of(".md");
    private static final Set<String> LOCAL_FILESYSTEMS = Set.of("ntfs", "refs", "ext4", "xfs", "btrfs", "overlay", "tmpfs", "apfs");
    private final RagOriginalBytesStore byteStore;
    private final long maxSourceBytes;
    private final Duration timeout;
    private final com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard localPaths;

    @Autowired
    public RagSnapshotAcquisitionService(RagOriginalBytesStore byteStore,
            @Value("${ppv.rag.limits.max-source-bytes:52428800}") long maxSourceBytes,
            @Value("${ppv.rag.snapshot-capture-timeout-seconds:60}") long timeoutSeconds,
            com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard localPaths) {
        this.byteStore = Objects.requireNonNull(byteStore);
        this.localPaths=Objects.requireNonNull(localPaths);
        this.maxSourceBytes = maxSourceBytes > 0 ? maxSourceBytes : 52428800L;
        if (timeoutSeconds < 1 || timeoutSeconds > 120) throw new IllegalArgumentException("Invalid capture deadline");
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }
    public RagSnapshotAcquisitionService(RagOriginalBytesStore store, long maxSourceBytes, long timeoutSeconds) {
        this(store,maxSourceBytes,timeoutSeconds,new com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard(maxSourceBytes));
    }
    public RagSnapshotAcquisitionService(RagOriginalBytesStore store, long maxSourceBytes) { this(store, maxSourceBytes, 60); }

    public record AcquiredTempSnapshot(UUID captureId, Path tempPath, String sha256, long byteLength,
            Instant capturedAt, String canonicalPath, String canonicalDigest) {
        @Override public String toString() { return "AcquiredTempSnapshot[captureId=" + captureId + "]"; }
    }

    /** One deadline spans copy, seal and the final pre-commit check. */
    public static final class CaptureContext implements AutoCloseable {
        private final long deadline;
        private final BooleanSupplier signal;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private volatile AsynchronousFileChannel channel;
        public CaptureContext(Duration timeout, BooleanSupplier signal) {
            if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofSeconds(120)) > 0)
                throw new IllegalArgumentException("Invalid capture deadline");
            this.deadline = System.nanoTime() + timeout.toNanos();
            this.signal = signal == null ? () -> false : signal;
        }
        public void check() {
            if (cancelled.get() || Thread.currentThread().isInterrupted() || signal.getAsBoolean())
                throw new CancellationException("Snapshot capture cancelled");
            if (remaining() <= 0) throw new CancellationException("Snapshot capture deadline exceeded");
        }
        private long remaining() { return deadline - System.nanoTime(); }
        public void cancel() { cancelled.set(true); close(); }
        @Override public void close() {
            var open = channel;
            if (open != null) try { open.close(); } catch (IOException ignored) { /* Cancellation never commits success. */ }
        }
    }
    public CaptureContext newContext() { return new CaptureContext(timeout, null); }
    public AcquiredTempSnapshot acquire(Path original, BooleanSupplier cancellation) {
        try (var context = new CaptureContext(timeout, cancellation)) { return acquireBounded(original, context); }
    }
    public AcquiredTempSnapshot acquireBounded(Path original, CaptureContext context) {
        Objects.requireNonNull(original);
        context.check();
        Path path = validatePath(original);
        BasicFileAttributes before = attributes(path);
        if (before.size() > maxSourceBytes) throw new IllegalArgumentException("Capture exceeds maximum source bytes");
        UUID capture = UUID.randomUUID();
        byteStore.beginCapture(capture);
        boolean complete = false;
        try {
            RagOriginalBytesStore.TempCapture temp;
            try (var channel = openChannel(path)) {
                context.channel = channel;
                context.check();
                temp = byteStore.writeTemp(capture, new InputStream() {
                    private long position;
                    @Override public int read() throws IOException {
                        byte[] one = new byte[1];
                        return read(one, 0, 1) == -1 ? -1 : Byte.toUnsignedInt(one[0]);
                    }
                    @Override public int read(byte[] bytes, int offset, int length) throws IOException {
                        context.check();
                        if (length == 0) return 0;
                        var future = channel.read(ByteBuffer.wrap(bytes, offset, length), position);
                        try {
                            while (true) {
                                context.check();
                                try {
                                    int read = future.get(Math.max(1, Math.min(context.remaining(), 100_000_000L)), TimeUnit.NANOSECONDS);
                                    context.check();
                                    if (read > 0) position += read;
                                    return read;
                                } catch (TimeoutException poll) { context.check(); }
                            }
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            throw new CancellationException("Snapshot capture cancelled");
                        } catch (ExecutionException failure) { throw new IOException("Snapshot read failed"); }
                        finally { if (!future.isDone()) future.cancel(true); }
                    }
                }, before.size(), null);
            }
            context.check();
            var after = attributes(path);
            if (!after.isRegularFile() || before.size() != after.size()
                    || !before.lastModifiedTime().equals(after.lastModifiedTime())
                    || !Objects.equals(before.fileKey(), after.fileKey()))
                throw new IllegalStateException("Original changed during snapshot capture");
            context.check();
            log.debug("Snapshot capture {} copied (bytes={})", capture, temp.byteLength());
            complete = true;
            return new AcquiredTempSnapshot(capture, temp.tempPath(), temp.sha256(), temp.byteLength(),
                    temp.createdAt(), path.toString(), RagSourceAuthority.digest(path.toString()));
        } catch (IOException failure) {
            // File-system exception causes can contain the original private locator.
            throw new IllegalStateException("Snapshot read failed");
        } finally {
            if (!complete) {
                try { byteStore.cleanupTemp(capture); }
                finally { byteStore.endCapture(capture); }
            }
        }
    }
    /** Narrow I/O seam for deterministic failed/stalled local-read tests. */
    protected AsynchronousFileChannel openChannel(Path path) throws IOException {
        return AsynchronousFileChannel.open(path, StandardOpenOption.READ);
    }
    private Path validatePath(Path original) {
        if (!original.isAbsolute() || original.getFileSystem() != FileSystems.getDefault())
            throw new IllegalArgumentException("An absolute server-local file path is required");
        String locator = original.toString();
        if (locator.startsWith("\\\\") || locator.startsWith("//")
                || (locator.length() > 2 && locator.substring(2).contains(":")))
            throw new IllegalArgumentException("Unsupported local file path");
        Path path = original.normalize();
        try { localPaths.verifyLocalSnapshotDrive(path); }
        catch (RuntimeException unsupported) { throw new IllegalArgumentException("Unsupported or unverified local drive"); }
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) throw new SnapshotMissingException("File does not exist");
        for (Path part = path; part != null; part = part.getParent()) {
            var attrs = attributes(part);
            if (attrs.isSymbolicLink() || attrs.isOther()) throw new IllegalArgumentException("Symlink or reparse path denied");
        }
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) throw new IllegalArgumentException("Target is a directory");
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(path))
            throw new IllegalArgumentException("Readable regular file required");
        try {
            if (!LOCAL_FILESYSTEMS.contains(Files.getFileStore(path).type().toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Unsupported local filesystem");
        } catch (IOException failure) { throw new IllegalArgumentException("Local filesystem unavailable"); }
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || !EXTENSIONS.contains(name.substring(dot).toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("Unsupported document format");
        return path;
    }
    private BasicFileAttributes attributes(Path path) {
        try { return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS); }
        catch (IOException failure) { throw new IllegalStateException("File attributes unavailable"); }
    }
}
