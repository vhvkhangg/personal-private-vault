package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSnapshotAcquisitionService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSnapshotAcquisitionService.AcquiredTempSnapshot;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(20)
class RagSnapshotAcquisitionServiceTest {

    @TempDir
    Path workDir;

    private RagOriginalBytesStore byteStore;
    private RagSnapshotAcquisitionService acquisitionService;

    @BeforeEach
    void setUp() {
        Path storeDir = workDir.resolve("byte-store");
        byteStore = new RagOriginalBytesStore(storeDir, 10 * 1024 * 1024L);
        acquisitionService = new RagSnapshotAcquisitionService(byteStore, 10 * 1024 * 1024L);
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(bytes));
    }

    @Test
    void testAcquireValidTextFile() throws Exception {
        Path txtFile = workDir.resolve("sample.txt");
        byte[] content = "Hello World! Bounded application acquisition test content.".getBytes(StandardCharsets.UTF_8);
        Files.write(txtFile, content);

        long originalMtime = Files.getLastModifiedTime(txtFile).toMillis();

        AcquiredTempSnapshot result = acquisitionService.acquire(txtFile, null);

        assertThat(result).isNotNull();
        assertThat(result.byteLength()).isEqualTo(content.length);
        assertThat(result.sha256()).isEqualTo(sha256Hex(content));
        assertThat(result.canonicalDigest()).isNotBlank();
        assertThat(result.captureId()).isNotNull();

        // Verify captured temp file in byte store matches content
        Path tempPath = result.tempPath();
        assertThat(Files.exists(tempPath)).isTrue();
        assertThat(Files.readAllBytes(tempPath)).isEqualTo(content);

        // Verify original file was not modified
        assertThat(Files.readAllBytes(txtFile)).isEqualTo(content);
        assertThat(Files.getLastModifiedTime(txtFile).toMillis()).isEqualTo(originalMtime);

        // Cleanup
        byteStore.cleanupTemp(result.captureId());
        assertThat(Files.exists(tempPath)).isFalse();
    }

    @Test
    void testAcquireValidBinaryDocxFile() throws Exception {
        Path docxFile = workDir.resolve("sample.docx");
        byte[] binaryContent = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00, 0x08, 0x00, 0x2A, 0x3B};
        Files.write(docxFile, binaryContent);

        AcquiredTempSnapshot result = acquisitionService.acquire(docxFile, null);

        assertThat(result.byteLength()).isEqualTo(binaryContent.length);
        assertThat(result.sha256()).isEqualTo(sha256Hex(binaryContent));

        byteStore.cleanupTemp(result.captureId());
    }

    @Test
    void testAcquireValidPdfFile() throws Exception {
        Path pdfFile = workDir.resolve("sample.pdf");
        byte[] pdfContent = "%PDF-1.7 sample test document\n%%EOF".getBytes(StandardCharsets.UTF_8);
        Files.write(pdfFile, pdfContent);

        AcquiredTempSnapshot result = acquisitionService.acquire(pdfFile, null);
        assertThat(result.byteLength()).isEqualTo(pdfContent.length);
        assertThat(result.sha256()).isEqualTo(sha256Hex(pdfContent));

        byteStore.cleanupTemp(result.captureId());
    }

    @Test
    void testAcquireAbsentFileThrows() {
        Path absent = workDir.resolve("non_existent_file.txt");
        assertThatThrownBy(() -> acquisitionService.acquire(absent, null))
                .isInstanceOf(SnapshotMissingException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void testAcquireDirectoryThrows() throws IOException {
        Path dir = workDir.resolve("subdir");
        Files.createDirectories(dir);

        assertThatThrownBy(() -> acquisitionService.acquire(dir, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("directory");
    }

    @Test
    void testAcquireUnsupportedExtensionThrows() throws IOException {
        Path exeFile = workDir.resolve("malware.exe");
        Files.write(exeFile, new byte[]{0x4D, 0x5A});

        assertThatThrownBy(() -> acquisitionService.acquire(exeFile, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported document format");
    }

    @Test
    void testAcquireOversizeFileThrows() throws IOException {
        RagSnapshotAcquisitionService smallLimitService = new RagSnapshotAcquisitionService(byteStore, 100L);

        Path largeFile = workDir.resolve("large.txt");
        Files.write(largeFile, new byte[256]);

        assertThatThrownBy(() -> smallLimitService.acquire(largeFile, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    void testAcquisitionPreCancelledThrowsAndCleansUp() throws IOException {
        Path txtFile = workDir.resolve("cancelled.txt");
        Files.write(txtFile, "Content to be cancelled".getBytes(StandardCharsets.UTF_8));

        AtomicBoolean cancelToken = new AtomicBoolean(true);

        assertThatThrownBy(() -> acquisitionService.acquire(txtFile, cancelToken::get))
                .isInstanceOf(CancellationException.class)
                .hasMessageContaining("cancelled");

        // Verify no leftover temp files in temp dir
        assertThat(Files.list(workDir.resolve("byte-store").resolve("temp")).count()).isEqualTo(0);
    }

    @Test
    void testCanonicalDigestDeterministic() throws Exception {
        Path file1 = workDir.resolve("deterministic.md");
        Files.write(file1, "# Header".getBytes(StandardCharsets.UTF_8));

        AcquiredTempSnapshot res1 = acquisitionService.acquire(file1, null);
        AcquiredTempSnapshot res2 = acquisitionService.acquire(file1, null);

        assertThat(res1.canonicalDigest()).isEqualTo(res2.canonicalDigest());

        byteStore.cleanupTemp(res1.captureId());
        byteStore.cleanupTemp(res2.captureId());

        Path file2 = workDir.resolve("other.md");
        Files.write(file2, "# Header".getBytes(StandardCharsets.UTF_8));
        AcquiredTempSnapshot resOther = acquisitionService.acquire(file2, null);

        // Different path yields different canonical identity digest
        assertThat(resOther.canonicalDigest()).isNotEqualTo(res1.canonicalDigest());
        byteStore.cleanupTemp(resOther.captureId());
    }
    @Test
    void slowReadHasProductionDeadlineAndCleansTemp() throws Exception {
        Path file=workDir.resolve("slow.txt"); Files.writeString(file,"data");
        var channel=org.mockito.Mockito.mock(java.nio.channels.AsynchronousFileChannel.class);
        var stalled=new java.util.concurrent.CompletableFuture<Integer>();
        org.mockito.Mockito.when(channel.read(org.mockito.ArgumentMatchers.any(java.nio.ByteBuffer.class),org.mockito.ArgumentMatchers.anyLong())).thenReturn(stalled);
        var service=new RagSnapshotAcquisitionService(byteStore,1000,1) {
            @Override protected java.nio.channels.AsynchronousFileChannel openChannel(Path path) { return channel; }
        };
        long start=System.nanoTime();
        assertThatThrownBy(()->service.acquire(file,null)).isInstanceOf(CancellationException.class).hasMessageContaining("deadline");
        assertThat(java.time.Duration.ofNanos(System.nanoTime()-start)).isLessThan(java.time.Duration.ofSeconds(4));
        assertThat(stalled.isCancelled()).isTrue();
        org.mockito.Mockito.verify(channel,org.mockito.Mockito.atLeastOnce()).close();
        try(var files=Files.list(byteStore.getTempDir())) { assertThat(files.count()).isZero(); }
    }

    @Test
    void failedReadSanitizesOriginalExceptionAndCleansTemp() throws Exception {
        Path file=workDir.resolve("sensitive-read.txt"); Files.writeString(file,"data");
        var channel=org.mockito.Mockito.mock(java.nio.channels.AsynchronousFileChannel.class);
        org.mockito.Mockito.when(channel.read(org.mockito.ArgumentMatchers.any(java.nio.ByteBuffer.class),org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new IOException(file.toString())));
        var service=new RagSnapshotAcquisitionService(byteStore,1000) {
            @Override protected java.nio.channels.AsynchronousFileChannel openChannel(Path path) { return channel; }
        };
        assertThatThrownBy(()->service.acquire(file,null)).isInstanceOf(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotStorageException.class).satisfies(failure -> { var stack=new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(stack)); assertThat(stack.toString()).doesNotContain(file.toString()); });
        try(var files=Files.list(byteStore.getTempDir())) { assertThat(files.count()).isZero(); }
    }

    @Test
    void practicalWindowsJunctionIsRejectedByOrdinaryAcquisition() throws Exception {
        Path directory=Files.createDirectory(workDir.resolve("private-target"));
        Files.writeString(directory.resolve("document.txt"),"data");
        Path junction=workDir.resolve("junction");
        // Directory junctions require no developer-mode symlink privilege on Windows.
        var process=new ProcessBuilder("cmd.exe","/c","mklink","/J",junction.toString(),directory.toString()).redirectErrorStream(true).start();
        assertThat(process.waitFor(5,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        assertThat(process.exitValue()).as(new String(process.getInputStream().readAllBytes())).isZero();
        try {
            assertThatThrownBy(()->acquisitionService.acquire(junction.resolve("document.txt"),null))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("reparse");
        } finally { Files.delete(junction); }
    }

    @Test
    void unsupportedAndRemotePathsAreDenied() throws Exception {
        assertThatThrownBy(()->acquisitionService.acquire(Path.of("relative.txt"),null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->acquisitionService.acquire(Path.of("//localhost/private/document.txt"),null)).isInstanceOf(IllegalArgumentException.class);
        try(var zip=java.nio.file.FileSystems.newFileSystem(java.net.URI.create("jar:"+workDir.resolve("data.zip").toUri()),java.util.Map.of("create","true"))) {
            Path file=zip.getPath("/document.txt"); Files.writeString(file,"data");
            assertThatThrownBy(()->acquisitionService.acquire(file,null)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void unverifiedOrMappedDriveFailsClosedWithoutOpeningOriginal() throws Exception {
        Path file=workDir.resolve("private-mapped.txt"); Files.writeString(file,"data");
        var paths=org.mockito.Mockito.mock(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard.class);
        org.mockito.Mockito.doThrow(new IllegalStateException(file.toString())).when(paths).verifyLocalSnapshotDrive(file);
        var service=new RagSnapshotAcquisitionService(byteStore,1000,60,paths);
        assertThatThrownBy(()->service.acquire(file,null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContaining(file.toString()).hasNoCause();
        try(var files=Files.list(byteStore.getTempDir())) { assertThat(files.count()).isZero(); }
    }

    @Test
    void debugLogsAndErrorsContainNoPrivateLocatorOrSensitiveDigest() throws Exception {
        String name="PRIVATE-owner-secret-journal.txt";
        Path file=workDir.resolve(name); byte[] content="sensitive whole-file content".getBytes(StandardCharsets.UTF_8); Files.write(file,content);
        var logger=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(RagSnapshotAcquisitionService.class);
        var old=logger.getLevel(); var events=new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        events.start(); logger.addAppender(events); logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        try {
            var acquired=acquisitionService.acquire(file,null);
            assertThat(events.list).isNotEmpty();
            String logs=events.list.stream().map(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage).collect(java.util.stream.Collectors.joining());
            assertThat(logs).doesNotContain(name,file.toString(),acquired.canonicalDigest(),acquired.sha256());
            assertThat(acquired.toString()).doesNotContain(name,acquired.canonicalDigest(),acquired.sha256());
            byteStore.cleanupTemp(acquired.captureId()); byteStore.endCapture(acquired.captureId());
            Files.delete(file);
            assertThatThrownBy(()->acquisitionService.acquire(file,null)).satisfies(failure -> { var stack=new java.io.StringWriter(); failure.printStackTrace(new java.io.PrintWriter(stack)); assertThat(stack.toString()).doesNotContain(file.toString()); }).hasMessageNotContaining(name);
        } finally { logger.detachAppender(events); logger.setLevel(old); events.stop(); }
    }

}
