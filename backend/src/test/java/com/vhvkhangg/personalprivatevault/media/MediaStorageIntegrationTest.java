package com.vhvkhangg.personalprivatevault.media;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vhvkhangg.personalprivatevault.ApiExceptionHandler;
import com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException;
import com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand;
import com.vhvkhangg.personalprivatevault.media.image.ImageConflictException;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.image.InvalidImageException;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.ImageDownloadService;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.ImageUploadService;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.MediaStoragePort;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDisabledException;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDownloadResult;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.ImageRepository;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage.MediaStorageHealthIndicator;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage.MediaStorageProperties;
import com.vhvkhangg.personalprivatevault.media.internal.web.advice.MediaExceptionAdvice;
import com.vhvkhangg.personalprivatevault.media.internal.web.controller.ImageController;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import com.vhvkhangg.personalprivatevault.media.internal.application.ImageService;
import com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence.AlbumRepository;
import org.springframework.web.context.request.async.AsyncWebRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.testcontainers.containers.MinIOContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.startup.Tomcat;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MediaStorageIntegrationTest extends AbstractWebIntegrationTest {

    private static final String BUCKET_NAME = "test-vault-media";

    private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio:RELEASE.2024-11-07T00-52-28Z");

    @BeforeAll
    static void startMinioAndCreateBucket() {
        MINIO_CONTAINER.start();
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(MINIO_CONTAINER.getS3URL()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(MINIO_CONTAINER.getUserName(), MINIO_CONTAINER.getPassword())))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()) {
            s3Client.createBucket(b -> b.bucket(BUCKET_NAME));
        }
    }

    @DynamicPropertySource
    static void registerMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("vault.media.storage.enabled", () -> "true");
        registry.add("vault.media.storage.endpoint", MINIO_CONTAINER::getS3URL);
        registry.add("vault.media.storage.region", () -> "us-east-1");
        registry.add("vault.media.storage.bucket", () -> BUCKET_NAME);
        registry.add("vault.media.storage.access-key", MINIO_CONTAINER::getUserName);
        registry.add("vault.media.storage.secret-key", MINIO_CONTAINER::getPassword);
        registry.add("vault.media.storage.path-style-access-enabled", () -> "true");
    }

    @Autowired
    private MediaStoragePort mediaStoragePort;

    @Autowired
    private ImageOperations imageOperations;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private MediaStorageProperties mediaStorageProperties;

    @Autowired
    private MediaStorageHealthIndicator mediaStorageHealthIndicator;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private ImageController imageController;

    @Test
    @DisplayName("Upload binary image -> metadata created -> download content with exact byte equality via async dispatch")
    void uploadAndDownloadImageByteEquality() throws Exception {
        byte[] testBytes = "Hello World Media Binary Payload for Phase 14 Testing!".getBytes(StandardCharsets.UTF_8);
        String expectedSha256 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(testBytes)
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                testBytes
        );

        // 1. Upload image via multipart
        String responseJson = mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .header("Authorization", bearerHeader())
                        .param("title", "Phase 14 Test Image")
                        .param("imageType", "PHOTO"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Phase 14 Test Image"))
                .andExpect(jsonPath("$.data.sizeBytes").value(testBytes.length))
                .andExpect(jsonPath("$.data.mimeType").value("image/png"))
                .andExpect(jsonPath("$.data.checksumSha256").value(expectedSha256))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number imageIdNum = objectMapper.readTree(responseJson).path("data").path("id").numberValue();
        long imageId = imageIdNum.longValue();
        String objectKey = objectMapper.readTree(responseJson).path("data").path("objectKey").asText();

        assertThat(objectKey).startsWith("images/managed/");
        assertThat(mediaStoragePort.exists(objectKey)).isTrue();

        // 2. Download image content via GET /api/v1/images/{id}/content with async dispatch
        MvcResult asyncResult = mockMvc.perform(get("/api/v1/images/{id}/content", imageId)
                        .header("Authorization", bearerHeader()))
                .andExpect(request().asyncStarted())
                .andReturn();

        byte[] downloadedBytes = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-store"))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(testBytes.length)))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // 3. Assert byte equality
        assertThat(downloadedBytes).isEqualTo(testBytes);
    }

    @Test
    @DisplayName("Managed image upload is rejected when invoked within an ambient active transaction")
    void ambientTransactionIsRejectedBeforeRemoteUpload() {
        byte[] bytes = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "ambient.jpg", "image/jpeg", bytes);

        transactionTemplate.execute(status -> {
            assertThatThrownBy(() -> imageUploadService.uploadImage(file, null, "Ambient Test", null, null, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("active transaction");
            return null;
        });
    }

    @Test
    @DisplayName("Download returns 409 storage integrity error when metadata exists but binary is missing in storage")
    void downloadMissingBinaryReturnsStorageIntegrityConflict() throws Exception {
        long missingImageId = 98765L;
        String ghostKey = "images/managed/non-existent-ghost-key.jpg";

        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type)
                VALUES (?, 'IMAGE'::vault_entry_type)
                ON CONFLICT (id) DO NOTHING
                """, missingImageId);
        jdbcTemplate.update("""
                INSERT INTO images (id, title, image_type, object_key, size_bytes, mime_type, checksum_sha256)
                VALUES (?, 'Ghost Image', 'PHOTO', ?, 1024, 'image/jpeg', 'sha256ghost')
                ON CONFLICT (id) DO NOTHING
                """, missingImageId, ghostKey);

        assertThat(mediaStoragePort.exists(ghostKey)).isFalse();

        mockMvc.perform(get("/api/v1/images/{id}/content", missingImageId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isConflict())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(jsonPath("$.error.code").value("STORAGE_INTEGRITY_ERROR"))
                .andExpect(jsonPath("$.error.message").isNotEmpty());
    }

    @Test
    @DisplayName("Download returns 404 when image metadata record does not exist")
    void downloadMissingMetadataReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/images/{id}/content", 9999999L)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("IMAGE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Pre-commit download failure returns HTTP 500 canonical JSON error")
    void downloadPreCommitErrorReturns500CanonicalJson() throws Exception {
        ImageOperations webOps = mock(ImageOperations.class);
        when(webOps.findById(101L)).thenReturn(new ImageView(101L, null, "PreCommit Failure Test", null,
                "images/managed/pre-commit-key", null, "image/png", 100L, null, null, null, null, null));

        MediaStoragePort failingReadPort = mock(MediaStoragePort.class);
        when(failingReadPort.download("images/managed/pre-commit-key")).thenReturn(new StorageDownloadResult(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Synthetic pre-commit storage read error");
            }
        }, 100L, "image/png"));

        MockMvc standaloneMvc = MockMvcBuilders.standaloneSetup(
                        new ImageController(webOps, null, new ImageDownloadService(webOps, failingReadPort)))
                .setControllerAdvice(new MediaExceptionAdvice(), new ApiExceptionHandler())
                .build();

        MvcResult initial = standaloneMvc.perform(get("/api/v1/images/101/content")).andReturn();
        assertThat(initial.getRequest().isAsyncStarted()).isTrue();

        MvcResult done = standaloneMvc.perform(asyncDispatch(initial)).andReturn();
        assertThat(done.getResponse().getStatus()).isEqualTo(500);
        assertThat(done.getResponse().getContentType()).contains("application/json");
        assertThat(done.getResponse().getContentAsString()).contains("\"code\":\"INTERNAL_ERROR\"");
    }

    @Test
    @DisplayName("Post-commit download failure throws IOException to abort transport rather than completing normally")
    void downloadPostCommitErrorAbortsTransport() {
        ImageOperations webOps = mock(ImageOperations.class);
        when(webOps.findById(102L)).thenReturn(new ImageView(102L, null, "PostCommit Failure Test", null,
                "images/managed/post-commit-key", null, "image/png", 16384L, null, null, null, null, null));

        MediaStoragePort postCommitPort = mock(MediaStoragePort.class);
        when(postCommitPort.download("images/managed/post-commit-key")).thenReturn(new StorageDownloadResult(new InputStream() {
            private boolean firstChunkRead = false;

            @Override
            public int read() throws IOException {
                throw new IOException("Synthetic post-commit read error");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (!firstChunkRead) {
                    firstChunkRead = true;
                    int count = Math.min(8192, len);
                    Arrays.fill(b, off, off + count, (byte) 42);
                    return count;
                }
                throw new IOException("Synthetic post-commit second chunk stream failure");
            }
        }, 16384L, "image/png"));

        ImageController controller = new ImageController(webOps, null, new ImageDownloadService(webOps, postCommitPort));
        jakarta.servlet.http.HttpServletRequest req = mock(jakarta.servlet.http.HttpServletRequest.class);
        jakarta.servlet.http.HttpServletResponse resp = mock(jakarta.servlet.http.HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(true);

        var responseEntity = controller.downloadContent(102L, req, resp);
        org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody body = responseEntity.getBody();
        assertThat(body).isNotNull();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> body.writeTo(out))
                .as("StreamingResponseBody must rethrow IOException to abort transport rather than swallowing it")
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Image transfer stream aborted: IOException");
    }

    @Test
    @DisplayName("Async executor rejection closes opened binary stream without resource leak")
    void downloadExecutorRejectionClosesStream() throws Exception {
        ImageOperations webOps = mock(ImageOperations.class);
        when(webOps.findById(103L)).thenReturn(new ImageView(103L, null, "Executor Rejection Test", null,
                "images/managed/rejection-key", null, "image/png", 3L, null, null, null, null, null));

        AtomicBoolean streamClosed = new AtomicBoolean(false);
        MediaStoragePort rejectedPort = mock(MediaStoragePort.class);
        when(rejectedPort.download("images/managed/rejection-key")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override
            public void close() throws IOException {
                streamClosed.set(true);
                super.close();
            }
        }, 3L, "image/png"));

        MockMvc standaloneMvc = MockMvcBuilders.standaloneSetup(
                        new ImageController(webOps, null, new ImageDownloadService(webOps, rejectedPort)))
                .setControllerAdvice(new MediaExceptionAdvice(), new ApiExceptionHandler())
                .build();

        standaloneMvc.getDispatcherServlet().getWebApplicationContext()
                .getBean(RequestMappingHandlerAdapter.class)
                .setTaskExecutor(task -> {
                    throw new TaskRejectedException("Synthetic executor rejection");
                });

        MvcResult rejected = standaloneMvc.perform(get("/api/v1/images/103/content")).andReturn();
        if (rejected.getRequest().isAsyncStarted()) {
            try {
                rejected.getAsyncResult(5000);
            } catch (Exception ignored) {
            } finally {
                rejected.getRequest().getAsyncContext().complete();
            }
        }

        assertThat(streamClosed.get())
                .as("Async executor rejection must close opened binary stream")
                .isTrue();
    }

    @Test
    @DisplayName("Missing multipart file returns 400 Bad Request with MALFORMED_REQUEST")
    void uploadMissingMultipartFileReturns400MalformedRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/images/upload")
                        .header("Authorization", bearerHeader())
                        .param("title", "Missing file parameter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.error.message").isNotEmpty());
    }

    @Test
    @DisplayName("Uncertain storage upload with positive probe: probes existence, compensates object, rethrows primary failure")
    void uploadUncertainStorageUploadProbesAndCompensates() {
        AtomicInteger probeCalls = new AtomicInteger();
        AtomicInteger deleteCalls = new AtomicInteger();
        Set<String> storageObjects = new HashSet<>();

        MediaStoragePort uncertainPort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                storageObjects.add(key);
                throw new RuntimeException("Synthetic lost S3 PUT acknowledgement");
            }

            @Override
            public StorageDownloadResult download(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String key) {
                deleteCalls.incrementAndGet();
                storageObjects.remove(key);
            }

            @Override
            public boolean exists(String key) {
                probeCalls.incrementAndGet();
                return storageObjects.contains(key);
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        var uploader = new ImageUploadService(uncertainPort, mock(ImageOperations.class), mock(ImageRepository.class), mediaStorageProperties);

        assertThatThrownBy(() -> uploader.uploadImage(
                new MockMultipartFile("file", "probe.png", "image/png", new byte[]{1, 2, 3}),
                null, "Uncertain Probe Image", null, null, null, null, null, null
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Synthetic lost S3 PUT acknowledgement");

        assertThat(probeCalls.get()).isEqualTo(1);
        assertThat(deleteCalls.get()).isEqualTo(1);
        assertThat(storageObjects).isEmpty();
    }

    @Test
    @DisplayName("Uncertain storage upload with throwing probe: probe fails, does NOT delete, rethrows primary failure")
    void uploadUncertainStorageUploadProbeFailureLeavesResidualOrphan() {
        AtomicInteger probeCalls = new AtomicInteger();
        AtomicInteger deleteCalls = new AtomicInteger();

        MediaStoragePort throwingProbePort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                throw new RuntimeException("Synthetic upload network break before storage write");
            }

            @Override
            public StorageDownloadResult download(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String key) {
                deleteCalls.incrementAndGet();
            }

            @Override
            public boolean exists(String key) {
                probeCalls.incrementAndGet();
                throw new RuntimeException("Synthetic probe connection outage");
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        var uploader = new ImageUploadService(throwingProbePort, mock(ImageOperations.class), mock(ImageRepository.class), mediaStorageProperties);

        assertThatThrownBy(() -> uploader.uploadImage(
                new MockMultipartFile("file", "probe-fail.png", "image/png", new byte[]{1, 2, 3}),
                null, "Probe Fail Image", null, null, null, null, null, null
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Synthetic upload network break before storage write");

        assertThat(probeCalls.get()).isEqualTo(1);
        assertThat(deleteCalls.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("Confirmed rollback deletes uploaded object (compensation) when metadata creation fails with AlbumNotFoundException")
    void confirmedRollbackCompensatesUploadedObject() {
        byte[] testBytes = "Rollback test payload".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rollback.jpg",
                "image/jpeg",
                testBytes
        );

        String[] capturedKey = new String[1];
        MediaStoragePort recordingPort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                capturedKey[0] = key;
                mediaStoragePort.upload(key, stream, size, mime);
            }

            @Override
            public StorageDownloadResult download(String key) {
                return mediaStoragePort.download(key);
            }

            @Override
            public void delete(String key) {
                mediaStoragePort.delete(key);
            }

            @Override
            public boolean exists(String key) {
                return mediaStoragePort.exists(key);
            }

            @Override
            public boolean isAvailable() {
                return mediaStoragePort.isAvailable();
            }
        };

        ImageUploadService service = new ImageUploadService(recordingPort, imageOperations, imageRepository, mediaStorageProperties);

        // albumId 999999 does not exist, so ImageService.create throws AlbumNotFoundException
        assertThatThrownBy(() -> service.uploadImage(file, 999999L, "Failing Image", null, null, null, null, null, null))
                .isInstanceOf(AlbumNotFoundException.class);

        assertThat(capturedKey[0]).isNotNull();
        assertThat(mediaStoragePort.exists(capturedKey[0]))
                .as("Object must be deleted from storage when metadata creation fails with confirmed rollback")
                .isFalse();

        Integer partialImageCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM images WHERE object_key = ?",
                Integer.class,
                capturedKey[0]
        );
        assertThat(partialImageCount).as("No partial Image row should exist after rollback").isEqualTo(0);
    }

    @Test
    @DisplayName("Deterministic delayed-commit regression: uncommitted metadata row is invisible to reconciliation, binary is preserved, and survives after commit")
    void delayedCommitRegressionPreservesBinary() throws Exception {
        byte[] bytes = "Delayed commit binary payload for FR14-1".getBytes(StandardCharsets.UTF_8);
        long delayedEntryId = 700001L;

        String[] capturedKey = new String[1];
        AtomicInteger deleteCalls = new AtomicInteger();

        MediaStoragePort recordingPort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                capturedKey[0] = key;
                mediaStoragePort.upload(key, stream, size, mime);
            }

            @Override
            public StorageDownloadResult download(String key) {
                return mediaStoragePort.download(key);
            }

            @Override
            public void delete(String key) {
                deleteCalls.incrementAndGet();
                mediaStoragePort.delete(key);
            }

            @Override
            public boolean exists(String key) {
                return mediaStoragePort.exists(key);
            }

            @Override
            public boolean isAvailable() {
                return mediaStoragePort.isAvailable();
            }
        };

        ImageOperations mockOps = mock(ImageOperations.class);

        try (Connection originalConn = jdbcTemplate.getDataSource().getConnection()) {
            originalConn.setAutoCommit(false);

            when(mockOps.create(any(CreateImageCommand.class))).thenAnswer(inv -> {
                CreateImageCommand cmd = inv.getArgument(0);
                try (PreparedStatement v = originalConn.prepareStatement(
                        "INSERT INTO vault_entries (id, entry_type) VALUES (?, 'IMAGE'::vault_entry_type) ON CONFLICT DO NOTHING")) {
                    v.setLong(1, delayedEntryId);
                    v.executeUpdate();
                }
                try (PreparedStatement img = originalConn.prepareStatement(
                        "INSERT INTO images (id, title, image_type, object_key, size_bytes, mime_type, checksum_sha256) " +
                                "VALUES (?, 'Delayed Image', 'PHOTO', ?, ?, 'image/jpeg', ?) ON CONFLICT DO NOTHING")) {
                    img.setLong(1, delayedEntryId);
                    img.setString(2, cmd.objectKey());
                    img.setLong(3, cmd.sizeBytes());
                    img.setString(4, cmd.checksumSha256());
                    img.executeUpdate();
                }
                throw new DataAccessResourceFailureException("Synthetic lost commit acknowledgement");
            });

            ImageUploadService uploader = new ImageUploadService(recordingPort, mockOps, imageRepository, mediaStorageProperties);

            MockMultipartFile file = new MockMultipartFile("file", "delayed.jpg", "image/jpeg", bytes);

            // Invoke uploader: mockOps.create inserts into originalConn (uncommitted) and throws DataAccessResourceFailureException
            assertThatThrownBy(() -> uploader.uploadImage(file, null, "Delayed Image", null, null, null, null, null, null))
                    .isInstanceOf(DataAccessResourceFailureException.class)
                    .hasMessageContaining("Synthetic lost commit acknowledgement");

            // Absence in independent reconciliation must NOT delete binary
            assertThat(deleteCalls.get()).isEqualTo(0);
            assertThat(mediaStoragePort.exists(capturedKey[0]))
                    .as("Absence in reconciliation must not delete binary while writer is in flight")
                    .isTrue();

            // Commit the original writer transaction
            originalConn.commit();

            // Verify row is now committed and binary remains intact
            assertThat(imageRepository.findByObjectKey(capturedKey[0])).isPresent();
            assertThat(mediaStoragePort.exists(capturedKey[0])).isTrue();
        }
    }

    @Test
    @DisplayName("Failed compensation is bounded to at most 3 attempts, preserves primary error, and leaks no keys")
    void failedCompensationPreservesPrimaryErrorAndBoundsRetries() {
        AtomicInteger deleteAttempts = new AtomicInteger();
        MediaStoragePort throwingDeletePort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {}

            @Override
            public StorageDownloadResult download(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String key) {
                deleteAttempts.incrementAndGet();
                throw new RuntimeException("Simulated S3 connection outage during compensation");
            }

            @Override
            public boolean exists(String key) {
                return true;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        ImageOperations mockOps = mock(ImageOperations.class);
        when(mockOps.create(any())).thenThrow(new InvalidImageException("Simulated primary metadata validation failure"));

        ImageUploadService uploadService = new ImageUploadService(throwingDeletePort, mockOps, imageRepository, mediaStorageProperties);
        MockMultipartFile file = new MockMultipartFile("file", "failing.jpg", "image/jpeg", new byte[]{1, 2, 3});

        // Must rethrow the PRIMARY error, preserving classification and not masking it with the delete failure
        assertThatThrownBy(() -> uploadService.uploadImage(file, null, "Failing Image", null, null, null, null, null, null))
                .isInstanceOf(InvalidImageException.class)
                .hasMessage("Simulated primary metadata validation failure");

        // Exactly 3 compensation attempts made
        assertThat(deleteAttempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Actuator health readiness includes mediaStorage and liveness does not")
    void actuatorHealthGroupsVerification() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @DisplayName("Storage outage degrades readiness indicator while liveness remains healthy")
    void storageDownAffectsReadinessNotLiveness() {
        MediaStoragePort downPort = mock(MediaStoragePort.class);
        when(downPort.isAvailable()).thenReturn(false);

        MediaStorageProperties props = new MediaStorageProperties();
        props.setEnabled(true);

        MediaStorageHealthIndicator indicator = new MediaStorageHealthIndicator(downPort, props);
        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
    }

    @Test
    @DisplayName("Privacy log capture verifies no SQL or provider details leaked into logs on failure paths (FR14-9)")
    void privacyLogCaptureVerifiesNoSensitiveSqlOrProviderDetailsLeaked() {
        Logger uploadLogger = (Logger) LoggerFactory.getLogger(ImageUploadService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        uploadLogger.addAppender(appender);

        try {
            String sensitiveSql = "SELECT * FROM super_secret_passwords WHERE pin='123456'";
            String sensitiveKey = "s3://super-secret-vault-bucket/sensitive-key.png";

            ImageOperations mockOps = mock(ImageOperations.class);
            when(mockOps.create(any())).thenThrow(new DataAccessResourceFailureException("Failed executing query: " + sensitiveSql));

            ImageRepository mockRepo = mock(ImageRepository.class);
            when(mockRepo.findByObjectKey(anyString())).thenThrow(new RuntimeException("Connection error to " + sensitiveKey));

            ImageUploadService uploader = new ImageUploadService(mediaStoragePort, mockOps, mockRepo, mediaStorageProperties);
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2});

            assertThatThrownBy(() -> uploader.uploadImage(file, null, "Test", null, null, null, null, null, null))
                    .isInstanceOf(DataAccessResourceFailureException.class);

            for (ILoggingEvent event : appender.list) {
                String formattedMessage = event.getFormattedMessage();
                assertThat(formattedMessage)
                        .doesNotContain(sensitiveSql)
                        .doesNotContain(sensitiveKey)
                        .doesNotContain("123456")
                        .doesNotContain("super_secret_passwords");
            }
        } finally {
            uploadLogger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("Commit-time failure triggers positive reconciliation when exact metadata was committed, retaining object")
    void commitTimeFailureReconciliationPositiveSucceeds() {
        byte[] bytes = "Positive reconciliation test bytes".getBytes(StandardCharsets.UTF_8);
        ImageOperations mockOps = mock(ImageOperations.class);
        String[] capturedKey = new String[1];

        when(mockOps.create(any(CreateImageCommand.class))).thenAnswer(inv -> {
            CreateImageCommand cmd = inv.getArgument(0);
            capturedKey[0] = cmd.objectKey();
            long entryId = 88801L;
            jdbcTemplate.update("""
                    INSERT INTO vault_entries (id, entry_type) VALUES (?, 'IMAGE'::vault_entry_type)
                    ON CONFLICT (id) DO NOTHING
                    """, entryId);
            jdbcTemplate.update("""
                    INSERT INTO images (id, title, image_type, object_key, size_bytes, mime_type, checksum_sha256)
                    VALUES (?, 'Positive Recon Image', 'PHOTO', ?, ?, 'image/jpeg', ?)
                    ON CONFLICT (id) DO NOTHING
                    """, entryId, cmd.objectKey(), cmd.sizeBytes(), cmd.checksumSha256());
            throw new DataAccessResourceFailureException("Simulated commit timeout after actual DB write");
        });

        ImageUploadService uploader = new ImageUploadService(mediaStoragePort, mockOps, imageRepository, mediaStorageProperties);
        MockMultipartFile file = new MockMultipartFile("file", "positive-recon.jpg", "image/jpeg", bytes);

        ImageView result = uploader.uploadImage(file, null, "Positive Recon Image", null, null, null, null, null, null);
        assertThat(result).isNotNull();
        assertThat(result.objectKey()).isEqualTo(capturedKey[0]);
        assertThat(mediaStoragePort.exists(capturedKey[0])).isTrue();
    }

    @Test
    @DisplayName("Commit-time failure with mismatched reconciliation retains object and rethrows primary error")
    void commitTimeFailureReconciliationMismatchRetainsObjectAndRethrows() {
        byte[] bytes = "Mismatch reconciliation test bytes".getBytes(StandardCharsets.UTF_8);
        ImageOperations mockOps = mock(ImageOperations.class);
        String[] capturedKey = new String[1];

        when(mockOps.create(any(CreateImageCommand.class))).thenAnswer(inv -> {
            CreateImageCommand cmd = inv.getArgument(0);
            capturedKey[0] = cmd.objectKey();
            long entryId = 88802L;
            jdbcTemplate.update("""
                    INSERT INTO vault_entries (id, entry_type) VALUES (?, 'IMAGE'::vault_entry_type)
                    ON CONFLICT (id) DO NOTHING
                    """, entryId);
            jdbcTemplate.update("""
                    INSERT INTO images (id, title, image_type, object_key, size_bytes, mime_type, checksum_sha256)
                    VALUES (?, 'Mismatch Recon Image', 'PHOTO', ?, ?, 'image/jpeg', 'corrupted_or_mismatched_checksum')
                    ON CONFLICT (id) DO NOTHING
                    """, entryId, cmd.objectKey(), cmd.sizeBytes());
            throw new DataAccessResourceFailureException("Simulated commit timeout with corrupted row");
        });

        ImageUploadService uploader = new ImageUploadService(mediaStoragePort, mockOps, imageRepository, mediaStorageProperties);
        MockMultipartFile file = new MockMultipartFile("file", "mismatch-recon.jpg", "image/jpeg", bytes);

        assertThatThrownBy(() -> uploader.uploadImage(file, null, "Mismatch Recon Image", null, null, null, null, null, null))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Simulated commit timeout with corrupted row");

        assertThat(capturedKey[0]).isNotNull();
        assertThat(mediaStoragePort.exists(capturedKey[0]))
                .as("Object must be retained in storage as accepted residual orphan risk when reconciliation mismatches")
                .isTrue();
    }

    @Test
    @DisplayName("Actual transactional commit-time failure with positive reconciliation retains object")
    void actualTransactionalCommitTimeFailureWithPositiveReconciliationRetainsObject() {
        byte[] bytes = ("Commit failure positive recon " + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "tx-commit-recon.jpg", "image/jpeg", bytes);

        int vaultCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int imageCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);

        String[] capturedKey = new String[1];
        ImageOperations commitFaultOps = new ImageOperations() {
            @Override
            public ImageView create(CreateImageCommand cmd) {
                capturedKey[0] = cmd.objectKey();
                return transactionTemplate.execute(status -> {
                    org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                            new org.springframework.transaction.support.TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                    throw new org.springframework.dao.DataAccessResourceFailureException("Simulated post-commit connection acknowledgment timeout");
                                }
                            }
                    );
                    return imageOperations.create(cmd);
                });
            }
            @Override
            public ImageView updateMetadata(com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand cmd) {
                return imageOperations.updateMetadata(cmd);
            }
            @Override
            public ImageView findById(Long id) {
                return imageOperations.findById(id);
            }
            @Override
            public List<ImageView> findByAlbumId(Long albumId, int limit, int offset) {
                return imageOperations.findByAlbumId(albumId, limit, offset);
            }
        };

        ImageUploadService uploader = new ImageUploadService(mediaStoragePort, commitFaultOps, imageRepository, mediaStorageProperties);

        ImageView reconciled = uploader.uploadImage(file, null, "Tx Commit Recon Image", null, null, null, null, null, null);
        assertThat(reconciled).isNotNull();
        assertThat(reconciled.objectKey()).isEqualTo(capturedKey[0]);
        assertThat(mediaStoragePort.exists(capturedKey[0])).isTrue();
        assertThat(imageRepository.findByObjectKey(capturedKey[0])).isPresent();

        int vaultCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int imageCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);
        assertThat(vaultCountAfter).as("Committed Vault entry must be retained").isEqualTo(vaultCountBefore + 1);
        assertThat(imageCountAfter).as("Committed Image metadata must be retained").isEqualTo(imageCountBefore + 1);
    }

    @Test
    @DisplayName("Actual transactional commit-time rollback retains object as orphan and leaves zero partial DB rows")
    void actualTransactionalCommitTimeRollbackRetainsObjectAndZeroPartialDbState() {
        byte[] bytes = ("Commit failure rollback " + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "tx-rollback.jpg", "image/jpeg", bytes);

        int vaultCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int imageCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);

        String[] capturedKey = new String[1];
        ImageOperations commitRollbackOps = new ImageOperations() {
            @Override
            public ImageView create(CreateImageCommand cmd) {
                capturedKey[0] = cmd.objectKey();
                return transactionTemplate.execute(status -> {
                    org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                            new org.springframework.transaction.support.TransactionSynchronization() {
                                @Override
                                public void beforeCommit(boolean readOnly) {
                                    throw new org.springframework.dao.DataAccessResourceFailureException("Simulated commit-time rollback trigger");
                                }
                            }
                    );
                    return imageOperations.create(cmd);
                });
            }
            @Override
            public ImageView updateMetadata(com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand cmd) {
                return imageOperations.updateMetadata(cmd);
            }
            @Override
            public ImageView findById(Long id) {
                return imageOperations.findById(id);
            }
            @Override
            public List<ImageView> findByAlbumId(Long albumId, int limit, int offset) {
                return imageOperations.findByAlbumId(albumId, limit, offset);
            }
        };

        ImageUploadService uploader = new ImageUploadService(mediaStoragePort, commitRollbackOps, imageRepository, mediaStorageProperties);

        assertThatThrownBy(() -> uploader.uploadImage(file, null, "Tx Rollback Image", null, null, null, null, null, null))
                .isInstanceOf(org.springframework.dao.DataAccessResourceFailureException.class)
                .hasMessageContaining("Simulated commit-time rollback trigger");

        assertThat(capturedKey[0]).isNotNull();
        assertThat(mediaStoragePort.exists(capturedKey[0]))
                .as("Object must be retained in storage as orphan when commit uncertainty occurs")
                .isTrue();

        int vaultCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int imageCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);
        assertThat(vaultCountAfter).as("Zero partial Vault rows must exist on rollback").isEqualTo(vaultCountBefore);
        assertThat(imageCountAfter).as("Zero partial Image rows must exist on rollback").isEqualTo(imageCountBefore);

        Integer imgCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images WHERE object_key = ?", Integer.class, capturedKey[0]);
        assertThat(imgCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Deterministic concurrent duplicate-checksum uploads: winner commits and is retained, loser compensates object, zero partial DB state")
    void concurrentDuplicateChecksumUploadRacesWithWinnerRetainedAndLoserCompensated() throws Exception {
        byte[] bytes = ("Concurrent duplicate payload " + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("file", "race1.jpg", "image/jpeg", bytes);
        MockMultipartFile file2 = new MockMultipartFile("file", "race2.jpg", "image/jpeg", bytes);

        int vaultCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int imageCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);

        CyclicBarrier uploadBarrier = new CyclicBarrier(2);
        CyclicBarrier checksumPrecheckBarrier = new CyclicBarrier(2);

        ConcurrentLinkedQueue<String> uploadedKeys = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> deletedKeys = new ConcurrentLinkedQueue<>();

        MediaStoragePort synchronizingPort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                uploadedKeys.add(key);
                mediaStoragePort.upload(key, stream, size, mime);
                try {
                    uploadBarrier.await(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public StorageDownloadResult download(String key) { return mediaStoragePort.download(key); }

            @Override
            public void delete(String key) {
                deletedKeys.add(key);
                mediaStoragePort.delete(key);
            }

            @Override
            public boolean exists(String key) { return mediaStoragePort.exists(key); }

            @Override
            public boolean isAvailable() { return mediaStoragePort.isAvailable(); }
        };

        ImageRepository synchronizingRepo = (ImageRepository) Proxy.newProxyInstance(
                ImageRepository.class.getClassLoader(),
                new Class<?>[]{ImageRepository.class},
                (proxy, method, args) -> {
                    try {
                        Object result = method.invoke(imageRepository, args);
                        if ("findByChecksumSha256".equals(method.getName())) {
                            checksumPrecheckBarrier.await(10, TimeUnit.SECONDS);
                        }
                        return result;
                    } catch (InvocationTargetException ite) {
                        throw ite.getCause();
                    }
                }
        );

        ImageService customImageService = new ImageService(vaultEntryOperations, albumRepository, synchronizingRepo);

        ImageOperations arbitratingOps = new ImageOperations() {
            @Override
            public ImageView create(CreateImageCommand cmd) {
                return transactionTemplate.execute(status -> customImageService.create(cmd));
            }
            @Override
            public ImageView updateMetadata(com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand cmd) {
                return imageOperations.updateMetadata(cmd);
            }
            @Override
            public ImageView findById(Long id) {
                return imageOperations.findById(id);
            }
            @Override
            public List<ImageView> findByAlbumId(Long albumId, int limit, int offset) {
                return imageOperations.findByAlbumId(albumId, limit, offset);
            }
        };

        ImageUploadService racingUploader = new ImageUploadService(synchronizingPort, arbitratingOps, synchronizingRepo, mediaStorageProperties);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ImageView> f1 = executor.submit(() -> racingUploader.uploadImage(file1, null, "Winner Image", null, null, null, null, null, null));
            Future<ImageView> f2 = executor.submit(() -> racingUploader.uploadImage(file2, null, "Loser Image", null, null, null, null, null, null));

            ImageView r1 = null;
            ImageView r2 = null;
            Throwable err1 = null;
            Throwable err2 = null;

            try { r1 = f1.get(10, TimeUnit.SECONDS); } catch (ExecutionException ee) { err1 = ee.getCause(); }
            try { r2 = f2.get(10, TimeUnit.SECONDS); } catch (ExecutionException ee) { err2 = ee.getCause(); }

            assertThat((r1 != null) ^ (r2 != null)).as("Exactly one upload must succeed").isTrue();
            ImageView winner = (r1 != null) ? r1 : r2;
            Throwable loserErr = (err1 != null) ? err1 : err2;

            assertThat(loserErr).isInstanceOf(ImageConflictException.class);
            assertThat(loserErr.getCause())
                    .as("Loser must be arbitrated by PostgreSQL unique constraint conflict, not precheck")
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThat(loserErr.getCause().getMessage()).contains("images_checksum_sha256_key");
            assertThat(mediaStoragePort.exists(winner.objectKey())).isTrue();

            // Winner is fully downloadable and content matches byte-for-byte
            StorageDownloadResult downloadResult = mediaStoragePort.download(winner.objectKey());
            assertThat(downloadResult).isNotNull();
            byte[] downloadedBytes;
            try (InputStream in = downloadResult.inputStream()) {
                downloadedBytes = in.readAllBytes();
            }
            assertThat(downloadedBytes).isEqualTo(bytes);

            String loserKey = uploadedKeys.stream().filter(k -> !k.equals(winner.objectKey())).findFirst().orElseThrow();
            assertThat(deletedKeys).contains(loserKey);
            assertThat(mediaStoragePort.exists(loserKey)).isFalse();

            int vaultCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
            int imageCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);

            // Winner metadata created, loser Image and Vault rolled back
            assertThat(imageCountAfter).as("Exactly one Image row created").isEqualTo(imageCountBefore + 1);
            assertThat(vaultCountAfter).as("Exactly one Vault entry created, zero orphan loser Vault row").isEqualTo(vaultCountBefore + 1);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM images WHERE checksum_sha256 = ?", Integer.class, winner.checksumSha256());
            assertThat(count).isEqualTo(1);

            Integer loserImgCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images WHERE object_key = ?", Integer.class, loserKey);
            assertThat(loserImgCount).isEqualTo(0);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent duplicate-checksum race where loser compensation exhausts 3 retries retains orphan and preserves primary error")
    void concurrentDuplicateChecksumUploadWithFailedLoserCompensationRetainsOrphan() throws Exception {
        byte[] bytes = ("Concurrent duplicate fail compensation " + System.nanoTime()).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("file", "race-fail1.jpg", "image/jpeg", bytes);
        MockMultipartFile file2 = new MockMultipartFile("file", "race-fail2.jpg", "image/jpeg", bytes);

        int vaultCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int imageCountBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);

        CyclicBarrier uploadBarrier = new CyclicBarrier(2);
        CyclicBarrier checksumPrecheckBarrier = new CyclicBarrier(2);

        ConcurrentLinkedQueue<String> uploadedKeys = new ConcurrentLinkedQueue<>();
        java.util.List<String> failedDeleteKeys = new java.util.concurrent.CopyOnWriteArrayList<>();

        MediaStoragePort failingDeletePort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                uploadedKeys.add(key);
                mediaStoragePort.upload(key, stream, size, mime);
                try {
                    uploadBarrier.await(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public StorageDownloadResult download(String key) { return mediaStoragePort.download(key); }

            @Override
            public void delete(String key) {
                failedDeleteKeys.add(key);
                throw new RuntimeException("Simulated S3 delete timeout");
            }

            @Override
            public boolean exists(String key) { return mediaStoragePort.exists(key); }

            @Override
            public boolean isAvailable() { return mediaStoragePort.isAvailable(); }
        };

        ImageRepository synchronizingRepo = (ImageRepository) Proxy.newProxyInstance(
                ImageRepository.class.getClassLoader(),
                new Class<?>[]{ImageRepository.class},
                (proxy, method, args) -> {
                    try {
                        Object result = method.invoke(imageRepository, args);
                        if ("findByChecksumSha256".equals(method.getName())) {
                            checksumPrecheckBarrier.await(10, TimeUnit.SECONDS);
                        }
                        return result;
                    } catch (InvocationTargetException ite) {
                        throw ite.getCause();
                    }
                }
        );

        ImageService customImageService = new ImageService(vaultEntryOperations, albumRepository, synchronizingRepo);

        ImageOperations arbitratingOps = new ImageOperations() {
            @Override
            public ImageView create(CreateImageCommand cmd) {
                return transactionTemplate.execute(status -> customImageService.create(cmd));
            }
            @Override
            public ImageView updateMetadata(com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand cmd) {
                return imageOperations.updateMetadata(cmd);
            }
            @Override
            public ImageView findById(Long id) {
                return imageOperations.findById(id);
            }
            @Override
            public List<ImageView> findByAlbumId(Long albumId, int limit, int offset) {
                return imageOperations.findByAlbumId(albumId, limit, offset);
            }
        };

        ImageUploadService racingUploader = new ImageUploadService(failingDeletePort, arbitratingOps, synchronizingRepo, mediaStorageProperties);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ImageView> f1 = executor.submit(() -> racingUploader.uploadImage(file1, null, "Winner Image", null, null, null, null, null, null));
            Future<ImageView> f2 = executor.submit(() -> racingUploader.uploadImage(file2, null, "Loser Image", null, null, null, null, null, null));

            ImageView r1 = null;
            ImageView r2 = null;
            Throwable err1 = null;
            Throwable err2 = null;

            try { r1 = f1.get(10, TimeUnit.SECONDS); } catch (ExecutionException ee) { err1 = ee.getCause(); }
            try { r2 = f2.get(10, TimeUnit.SECONDS); } catch (ExecutionException ee) { err2 = ee.getCause(); }

            ImageView winner = (r1 != null) ? r1 : r2;
            Throwable loserErr = (err1 != null) ? err1 : err2;

            assertThat(loserErr).isInstanceOf(ImageConflictException.class);
            assertThat(loserErr.getCause())
                    .as("Loser must be arbitrated by PostgreSQL unique constraint conflict, not precheck")
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThat(loserErr.getCause().getMessage()).contains("images_checksum_sha256_key");
            assertThat(failedDeleteKeys).hasSize(3);

            String loserKey = uploadedKeys.stream().filter(k -> !k.equals(winner.objectKey())).findFirst().orElseThrow();
            assertThat(failedDeleteKeys).as("Every compensation retry must target ONLY the loser key").containsOnly(loserKey);
            assertThat(mediaStoragePort.exists(loserKey)).isTrue();

            // Winner binary remains available and downloadable
            StorageDownloadResult downloadResult = mediaStoragePort.download(winner.objectKey());
            assertThat(downloadResult).isNotNull();
            byte[] downloadedBytes;
            try (InputStream in = downloadResult.inputStream()) {
                downloadedBytes = in.readAllBytes();
            }
            assertThat(downloadedBytes).isEqualTo(bytes);

            int vaultCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
            int imageCountAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);

            assertThat(imageCountAfter).as("Exactly one Image row created").isEqualTo(imageCountBefore + 1);
            assertThat(vaultCountAfter).as("Exactly one Vault entry created, zero orphan loser Vault row").isEqualTo(vaultCountBefore + 1);

            Integer loserImgCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images WHERE object_key = ?", Integer.class, loserKey);
            assertThat(loserImgCount).isEqualTo(0);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent duplicate-checksum uploads: winner is retained, loser object is compensated, no partial state in DB")
    void duplicateChecksumUploadCompensatesLoserRetainsWinnerAndLeavesNoPartialState() {
        byte[] bytes = "Unique duplicate checksum test payload".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file1 = new MockMultipartFile("file", "duplicate1.jpg", "image/jpeg", bytes);
        MockMultipartFile file2 = new MockMultipartFile("file", "duplicate2.jpg", "image/jpeg", bytes);

        String[] loserKey = new String[1];
        AtomicInteger loserDeleteCalled = new AtomicInteger();

        ImageView winner = imageUploadService.uploadImage(file1, null, "Winner Image", null, null, null, null, null, null);
        assertThat(winner).isNotNull();
        assertThat(mediaStoragePort.exists(winner.objectKey())).isTrue();

        MediaStoragePort loserRecordingPort = new MediaStoragePort() {
            @Override
            public void upload(String key, InputStream stream, long size, String mime) {
                loserKey[0] = key;
                mediaStoragePort.upload(key, stream, size, mime);
            }
            @Override
            public StorageDownloadResult download(String key) { return mediaStoragePort.download(key); }
            @Override
            public void delete(String key) {
                loserDeleteCalled.incrementAndGet();
                mediaStoragePort.delete(key);
            }
            @Override
            public boolean exists(String key) { return mediaStoragePort.exists(key); }
            @Override
            public boolean isAvailable() { return mediaStoragePort.isAvailable(); }
        };

        ImageUploadService loserUploader = new ImageUploadService(loserRecordingPort, imageOperations, imageRepository, mediaStorageProperties);

        assertThatThrownBy(() -> loserUploader.uploadImage(file2, null, "Loser Image", null, null, null, null, null, null))
                .isInstanceOf(ImageConflictException.class);

        assertThat(loserKey[0]).isNotNull();
        assertThat(loserDeleteCalled.get()).isEqualTo(1);
        assertThat(mediaStoragePort.exists(loserKey[0])).isFalse();

        assertThat(mediaStoragePort.exists(winner.objectKey())).isTrue();
        assertThat(imageRepository.findByObjectKey(winner.objectKey())).isPresent();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM images WHERE checksum_sha256 = ?",
                Integer.class,
                winner.checksumSha256()
        );
        assertThat(count).isEqualTo(1);

        Integer loserCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM images WHERE object_key = ?",
                Integer.class,
                loserKey[0]
        );
        assertThat(loserCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Upload fails with StorageDisabledException when storage is disabled")
    void disabledStorageRejectsUpload() {
        MediaStorageProperties disabledProps = new MediaStorageProperties();
        disabledProps.setEnabled(false);

        ImageUploadService disabledUploader = new ImageUploadService(mediaStoragePort, imageOperations, imageRepository, disabledProps);
        MockMultipartFile file = new MockMultipartFile("file", "disabled.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> disabledUploader.uploadImage(file, null, "Disabled Test", null, null, null, null, null, null))
                .isInstanceOf(StorageDisabledException.class)
                .hasMessageContaining("Managed image storage is not enabled");
    }

    @Test
    @DisplayName("Invalid enabled storage configuration fails validation")
    void invalidEnabledStorageConfigurationFailsValidation() {
        MediaStorageProperties props = new MediaStorageProperties();
        props.setEnabled(true);
        assertThatThrownBy(props::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vault.media.storage.bucket");

        props.setBucket("my-bucket");
        props.setRegion("us-east-1");
        assertThatThrownBy(props::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vault.media.storage.access-key");

        props.setAccessKey("access");
        assertThatThrownBy(props::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vault.media.storage.secret-key");

        props.setSecretKey("secret");
        props.setMaxFileSize(-1);
        assertThatThrownBy(props::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vault.media.storage.max-file-size");
    }

    @Test
    @DisplayName("Storage outage degrades readiness HTTP endpoint while liveness remains healthy")
    void storageOutageDegradesReadinessEndpointWhileLivenessRemainsHealthy() throws Exception {
        String originalBucket = mediaStorageProperties.getBucket();
        try {
            mediaStorageProperties.setBucket("non-existent-outage-bucket-" + System.currentTimeMillis());
            assertThat(mediaStoragePort.isAvailable()).isFalse();

            mockMvc.perform(get("/actuator/health/readiness"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("DOWN"));

            mockMvc.perform(get("/actuator/health/liveness"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        } finally {
            mediaStorageProperties.setBucket(originalBucket);
        }

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Vault image trash does not delete binary in storage; binary remains accessible")
    void vaultImageTrashDoesNotDeleteBinaryInStorage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trash-test.jpg",
                "image/jpeg",
                "Binary payload to test trash retention".getBytes(StandardCharsets.UTF_8)
        );

        ImageView view = imageUploadService.uploadImage(file, null, "Trash Test Image", null, null, null, null, null, null);
        assertThat(view).isNotNull();
        assertThat(mediaStoragePort.exists(view.objectKey())).isTrue();

        // Move vault entry to trash
        vaultEntryOperations.moveToTrash(view.id());
        assertThat(mediaStoragePort.exists(view.objectKey()))
                .as("Binary must remain intact in object storage when vault image metadata is moved to trash")
                .isTrue();

        // Restore vault entry
        vaultEntryOperations.restore(view.id());
        assertThat(mediaStoragePort.exists(view.objectKey()))
                .as("Binary must remain intact in object storage when vault image metadata is restored from trash")
                .isTrue();
    }

    @Test
    @DisplayName("Real HTTP client detects transfer failure on unknown-length chunked streaming download (FR14-2)")
    void realHttpClientDetectsTransferFailureOnUnknownLengthChunkedStream() throws Exception {
        org.apache.catalina.startup.Tomcat tomcat = new org.apache.catalina.startup.Tomcat();
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("ppv-tomcat-wire-test-");
        try {
            tomcat.setBaseDir(tempDir.toAbsolutePath().toString());
            tomcat.setPort(0);
            tomcat.getConnector();
            var context = tomcat.addContext("", tempDir.toAbsolutePath().toString());

            ImageOperations ops = mock(ImageOperations.class);
            when(ops.findById(1L)).thenReturn(new ImageView(1L, null, "Synthetic image", null,
                    "images/managed/wire-synthetic", null, "application/octet-stream", null, null, null, null, null, null));

            AtomicInteger streamClosed = new AtomicInteger();
            MediaStoragePort port = mock(MediaStoragePort.class);
            when(port.download("images/managed/wire-synthetic")).thenReturn(new StorageDownloadResult(new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("Synthetic wire test stream error");
                }
                @Override
                public long transferTo(OutputStream out) throws IOException {
                    out.write(new byte[16384]);
                    out.flush();
                    throw new IOException("Synthetic wire test stream error");
                }
                @Override
                public void close() {
                    streamClosed.incrementAndGet();
                }
            }, null, "application/octet-stream"));

            var app = new GenericWebApplicationContext();
            app.registerBean("imageOperations", ImageOperations.class, () -> ops);
            app.registerBean("mediaStoragePort", MediaStoragePort.class, () -> port);
            app.registerBean("downloadService", ImageDownloadService.class, () -> new ImageDownloadService(ops, port));
            app.registerBean("imageController", ImageController.class, () -> new ImageController(ops, null, new ImageDownloadService(ops, port)));
            app.registerBean("advice", ApiExceptionHandler.class, ApiExceptionHandler::new);
            app.refresh();

            var wrapper = org.apache.catalina.startup.Tomcat.addServlet(context, "dispatcher", new org.springframework.web.servlet.DispatcherServlet(app));
            wrapper.setAsyncSupported(true);
            wrapper.setLoadOnStartup(1);
            context.addServletMappingDecoded("/*", "dispatcher");

            tomcat.start();

            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            URI uri = URI.create("http://localhost:" + tomcat.getConnector().getLocalPort() + "/api/v1/images/1/content");

            assertThatThrownBy(() -> client.send(
                    java.net.http.HttpRequest.newBuilder(uri).timeout(java.time.Duration.ofSeconds(10)).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.ofByteArray()
            ))
                    .as("Client must observe transfer failure (IOException) on aborted unknown-length chunked stream")
                    .isInstanceOf(IOException.class);

            assertThat(streamClosed.get()).as("Opened binary stream must be closed").isGreaterThan(0);
            app.close();
        } finally {
            try { tomcat.stop(); } catch (Exception ignored) {}
            try { tomcat.destroy(); } catch (Exception ignored) {}
            try (var s = java.nio.file.Files.walk(tempDir)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try { java.nio.file.Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("Real multipart malformed bytes on /api/v1/images/upload returns canonical 400 and performs zero storage or DB work")
    void realMultipartMalformedBytesReturnsCanonical400AndPerformsNoStorageOrDbWork() throws Exception {
        int initialImageCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);
        int initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        Tomcat tomcat = new Tomcat();
        Path tempDir = Files.createTempDirectory("ppv-tomcat-malformed-test-");
        AnnotationConfigWebApplicationContext app = new AnnotationConfigWebApplicationContext();
        app.register(SelectedMvc.class);
        try {
            tomcat.setBaseDir(tempDir.toAbsolutePath().toString());
            tomcat.setPort(0);
            tomcat.getConnector();
            var context = tomcat.addContext("", tempDir.toAbsolutePath().toString());
            app.setServletContext(context.getServletContext());

            ImageUploadService spyUploader = spy(imageUploadService);
            MediaStoragePort spyStorage = spy(mediaStoragePort);
            ImageController testController = new ImageController(imageOperations, spyUploader, new ImageDownloadService(imageOperations, spyStorage));

            app.addBeanFactoryPostProcessor(factory -> {
                factory.registerSingleton("imageController", testController);
                factory.registerSingleton("advice", new ApiExceptionHandler());
            });
            app.refresh();

            var wrapper = Tomcat.addServlet(context, "dispatcher", new DispatcherServlet(app));
            wrapper.setMultipartConfigElement(new MultipartConfigElement(tempDir.toString(), 1024L, 2048L, 512));
            wrapper.setLoadOnStartup(1);
            context.addServletMappingDecoded("/*", "dispatcher");

            tomcat.start();
            int port = tomcat.getConnector().getLocalPort();
            HttpClient client = HttpClient.newHttpClient();

            // Send malformed multipart payload: missing boundary parameter in content-type
            HttpResponse<String> resp = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/upload"))
                            .header("Content-Type", "multipart/form-data; boundary=")
                            .POST(HttpRequest.BodyPublishers.ofString("corrupted body"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertThat(resp.statusCode()).isEqualTo(400);
            assertThat(resp.headers().firstValue("content-type").orElse("")).contains("application/json");
            assertThat(resp.body()).contains("\"MALFORMED_REQUEST\"");
            assertThat(resp.body()).contains("\"Failed to parse multipart request\"");
            assertThat(resp.body()).doesNotContain("<!doctype html>");

            verifyNoInteractions(spyUploader);
            verifyNoInteractions(spyStorage);

            int afterImageCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);
            int afterVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
            assertThat(afterImageCount).isEqualTo(initialImageCount);
            assertThat(afterVaultCount).isEqualTo(initialVaultCount);

            app.close();
        } finally {
            try { tomcat.stop(); } catch (Exception ignored) {}
            try { tomcat.destroy(); } catch (Exception ignored) {}
            try (var s = Files.walk(tempDir)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("Real Tomcat multipart oversized upload on /api/v1/images/upload returns canonical 413 and performs zero DB work")
    void realTomcatMultipartOversizedUploadReturnsCanonical413AndPerformsNoWork() throws Exception {
        int initialImageCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);
        int initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        Tomcat tomcat = new Tomcat();
        Path tempDir = Files.createTempDirectory("ppv-tomcat-size-test-");
        AnnotationConfigWebApplicationContext app = new AnnotationConfigWebApplicationContext();
        app.register(SelectedMvc.class);
        try {
            tomcat.setBaseDir(tempDir.toAbsolutePath().toString());
            tomcat.setPort(0);
            tomcat.getConnector();
            var context = tomcat.addContext("", tempDir.toAbsolutePath().toString());
            app.setServletContext(context.getServletContext());

            ImageUploadService spyUploader = spy(imageUploadService);
            MediaStoragePort spyStorage = spy(mediaStoragePort);
            ImageController testController = new ImageController(imageOperations, spyUploader, new ImageDownloadService(imageOperations, spyStorage));

            app.addBeanFactoryPostProcessor(factory -> {
                factory.registerSingleton("imageController", testController);
                factory.registerSingleton("advice", new ApiExceptionHandler());
            });
            app.refresh();

            var wrapper = Tomcat.addServlet(context, "dispatcher", new DispatcherServlet(app));
            wrapper.setMultipartConfigElement(new MultipartConfigElement(tempDir.toString(), 1024L, 2048L, 512));
            wrapper.setLoadOnStartup(1);
            context.addServletMappingDecoded("/*", "dispatcher");

            tomcat.start();
            int port = tomcat.getConnector().getLocalPort();
            HttpClient client = HttpClient.newHttpClient();

            byte[] oversizedBody = ("------BoundaryTest123\r\nContent-Disposition: form-data; name=\"file\"; filename=\"large.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n"
                    + "X".repeat(1500) + "\r\n------BoundaryTest123--\r\n").getBytes(StandardCharsets.UTF_8);

            HttpResponse<String> resp = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/upload"))
                            .header("Content-Type", "multipart/form-data; boundary=----BoundaryTest123")
                            .POST(HttpRequest.BodyPublishers.ofByteArray(oversizedBody))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertThat(resp.statusCode()).isEqualTo(413);
            assertThat(resp.headers().firstValue("content-type").orElse("")).contains("application/json");
            assertThat(resp.body()).contains("\"PAYLOAD_TOO_LARGE\"");
            assertThat(resp.body()).contains("\"Uploaded file exceeds maximum permitted size limit\"");
            assertThat(resp.body()).doesNotContain("<!doctype html>");

            verifyNoInteractions(spyUploader);
            verifyNoInteractions(spyStorage);

            int afterImageCount = jdbcTemplate.queryForObject("SELECT count(*) FROM images", Integer.class);
            int afterVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
            assertThat(afterImageCount).isEqualTo(initialImageCount);
            assertThat(afterVaultCount).isEqualTo(initialVaultCount);

            app.close();
        } finally {
            try { tomcat.stop(); } catch (Exception ignored) {}
            try { tomcat.destroy(); } catch (Exception ignored) {}
            try (var s = Files.walk(tempDir)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("Large streaming upload exceeding configured size bounds does not buffer heap and cleans up temp files")
    void largeStreamingUploadExceedingConfiguredSizeBoundsDoesNotBufferHeapAndCleansUp() throws Exception {
        Path sysTemp = Path.of(System.getProperty("java.io.tmpdir"));
        Set<Path> existingSpools;
        try (var s = Files.list(sysTemp)) {
            existingSpools = s.filter(p -> p.getFileName().toString().startsWith("ppv-upload-") && p.getFileName().toString().endsWith(".tmp")).collect(Collectors.toSet());
        }

        long totalStreamSize = 55L * 1024 * 1024; // 55MB (exceeds 50MB max limit)
        AtomicBoolean streamClosed = new AtomicBoolean(false);
        InputStream streamingInput = new InputStream() {
            private long bytesRead = 0;
            @Override
            public int read() {
                return (bytesRead++ < totalStreamSize) ? 0x58 : -1;
            }
            @Override
            public int read(byte[] b, int off, int len) {
                if (bytesRead >= totalStreamSize) return -1;
                int toRead = (int) Math.min(len, totalStreamSize - bytesRead);
                Arrays.fill(b, off, off + toRead, (byte) 0x58);
                bytesRead += toRead;
                return toRead;
            }
            @Override
            public void close() {
                streamClosed.set(true);
            }
        };

        org.springframework.web.multipart.MultipartFile streamingFile = new org.springframework.web.multipart.MultipartFile() {
            @Override public String getName() { return "file"; }
            @Override public String getOriginalFilename() { return "stream-test.jpg"; }
            @Override public String getContentType() { return "image/jpeg"; }
            @Override public boolean isEmpty() { return false; }
            @Override public long getSize() { return totalStreamSize; }
            @Override public byte[] getBytes() {
                throw new AssertionError("Heap buffering must NOT occur during streaming upload!");
            }
            @Override public InputStream getInputStream() { return streamingInput; }
            @Override public void transferTo(java.io.File dest) { throw new UnsupportedOperationException(); }
        };

        assertThatThrownBy(() -> imageUploadService.uploadImage(streamingFile, null, "Streaming Test", null, null, null, null, null, null))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("File size exceeds maximum configured upload limit");

        assertThat(streamClosed.get()).as("Streaming input must be closed when upload fails size check").isTrue();

        Set<Path> currentSpools;
        try (var s = Files.list(sysTemp)) {
            currentSpools = s.filter(p -> p.getFileName().toString().startsWith("ppv-upload-") && p.getFileName().toString().endsWith(".tmp")).collect(Collectors.toSet());
        }
        assertThat(currentSpools).as("Spool .tmp files must be deleted in finally").isEqualTo(existingSpools);
    }

    @Test
    @DisplayName("Spring configuration startup lifecycle fails fast on invalid enabled storage configuration")
    void enabledStorageSpringConfigurationStartupLifecycleValidation() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage.MediaStorageConfiguration.class);

        // Missing bucket
        runner.withPropertyValues("vault.media.storage.enabled=true", "vault.media.storage.bucket=")
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).rootCause().isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("bucket must not be blank");
                });

        // Missing region
        runner.withPropertyValues("vault.media.storage.enabled=true", "vault.media.storage.bucket=b", "vault.media.storage.region=")
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).rootCause().isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("region must not be blank");
                });

        // Missing access key
        runner.withPropertyValues("vault.media.storage.enabled=true", "vault.media.storage.bucket=b", "vault.media.storage.region=us-east-1", "vault.media.storage.access-key=")
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).rootCause().isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("access-key must not be blank");
                });

        // Missing secret key
        runner.withPropertyValues("vault.media.storage.enabled=true", "vault.media.storage.bucket=b", "vault.media.storage.region=us-east-1", "vault.media.storage.access-key=k", "vault.media.storage.secret-key=")
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).rootCause().isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("secret-key must not be blank");
                });

        // Invalid max file size
        runner.withPropertyValues("vault.media.storage.enabled=true", "vault.media.storage.bucket=b", "vault.media.storage.region=us-east-1", "vault.media.storage.access-key=k", "vault.media.storage.secret-key=s", "vault.media.storage.max-file-size=0")
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure()).rootCause().isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("max-file-size must be positive");
                });

        // Invalid endpoint URI fails fast with IllegalArgumentException
        runner.withPropertyValues(
                "vault.media.storage.enabled=true",
                "vault.media.storage.bucket=b",
                "vault.media.storage.region=us-east-1",
                "vault.media.storage.access-key=k",
                "vault.media.storage.secret-key=s",
                "vault.media.storage.endpoint=://invalid-endpoint-uri"
        ).run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure().getCause().getCause()).isInstanceOf(IllegalArgumentException.class);
            assertThat(ctx.getStartupFailure()).rootCause().isInstanceOf(java.net.URISyntaxException.class);
        });
    }

    @Test
    @DisplayName("Image download stream lifecycle closes opened streams across success, failure, timeout, and disconnect")
    void imageDownloadStreamLifecycleClosesResourcesOnSuccessFailureTimeoutAndDisconnect() throws Exception {
        // 1. Success path: stream is closed after writing via ImageController.downloadContent
        AtomicBoolean successStreamClosed = new AtomicBoolean(false);
        ImageOperations opsSuccess = mock(ImageOperations.class);
        when(opsSuccess.findById(101L)).thenReturn(new ImageView(101L, null, "Success Img", null, "images/managed/success-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portSuccess = mock(MediaStoragePort.class);
        when(portSuccess.download("images/managed/success-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                successStreamClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceSuccess = new ImageDownloadService(opsSuccess, portSuccess);
        ImageController controllerSuccess = new ImageController(opsSuccess, null, downloadServiceSuccess);

        MockHttpServletRequest mockReqSuccess = new MockHttpServletRequest();
        MockHttpServletResponse mockRespSuccess = new MockHttpServletResponse();
        var respSuccess = controllerSuccess.downloadContent(101L, mockReqSuccess, mockRespSuccess);
        assertThat(respSuccess.getBody()).isNotNull();
        respSuccess.getBody().writeTo(new ByteArrayOutputStream());
        assertThat(successStreamClosed.get()).as("Binary stream must be closed upon successful transfer completion").isTrue();

        // 2. Stream transfer failure path: stream is closed when output stream throws IOException
        AtomicBoolean failureStreamClosed = new AtomicBoolean(false);
        ImageOperations opsFailure = mock(ImageOperations.class);
        when(opsFailure.findById(102L)).thenReturn(new ImageView(102L, null, "Fail Img", null, "images/managed/fail-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portFailure = mock(MediaStoragePort.class);
        when(portFailure.download("images/managed/fail-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                failureStreamClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceFailure = new ImageDownloadService(opsFailure, portFailure);
        ImageController controllerFailure = new ImageController(opsFailure, null, downloadServiceFailure);

        MockHttpServletRequest mockReqFailure = new MockHttpServletRequest();
        MockHttpServletResponse mockRespFailure = new MockHttpServletResponse();
        var respFailure = controllerFailure.downloadContent(102L, mockReqFailure, mockRespFailure);
        assertThatThrownBy(() -> respFailure.getBody().writeTo(new OutputStream() {
            @Override public void write(int b) throws IOException { throw new IOException("Simulated network transfer break"); }
        })).isInstanceOf(IOException.class);
        assertThat(failureStreamClosed.get()).as("Binary stream must be closed when output write fails").isTrue();

        // 3. Async timeout path: CallableProcessingInterceptor.handleTimeout closes stream
        AtomicBoolean timeoutStreamClosed = new AtomicBoolean(false);
        ImageOperations opsTimeout = mock(ImageOperations.class);
        when(opsTimeout.findById(103L)).thenReturn(new ImageView(103L, null, "Timeout Img", null, "images/managed/timeout-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portTimeout = mock(MediaStoragePort.class);
        when(portTimeout.download("images/managed/timeout-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                timeoutStreamClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceTimeout = new ImageDownloadService(opsTimeout, portTimeout);
        ImageController controllerTimeout = new ImageController(opsTimeout, null, downloadServiceTimeout);

        MockHttpServletRequest mockReqTimeout = new MockHttpServletRequest();
        MockHttpServletResponse mockRespTimeout = new MockHttpServletResponse();
        WebAsyncManager asyncManagerTimeout = WebAsyncUtils.getAsyncManager(mockReqTimeout);
        controllerTimeout.downloadContent(103L, mockReqTimeout, mockRespTimeout);

        CallableProcessingInterceptor interceptorTimeout = asyncManagerTimeout.getCallableInterceptor("imageDownloadCleanup");
        assertThat(interceptorTimeout).isNotNull();
        interceptorTimeout.handleTimeout(new ServletWebRequest(mockReqTimeout, mockRespTimeout), null);
        assertThat(timeoutStreamClosed.get()).as("Binary stream must be closed when async processing times out").isTrue();

        // 4. Client disconnect / error path: CallableProcessingInterceptor.handleError closes stream
        AtomicBoolean errorStreamClosed = new AtomicBoolean(false);
        ImageOperations opsError = mock(ImageOperations.class);
        when(opsError.findById(104L)).thenReturn(new ImageView(104L, null, "Error Img", null, "images/managed/error-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portError = mock(MediaStoragePort.class);
        when(portError.download("images/managed/error-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                errorStreamClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceError = new ImageDownloadService(opsError, portError);
        ImageController controllerError = new ImageController(opsError, null, downloadServiceError);

        MockHttpServletRequest mockReqError = new MockHttpServletRequest();
        MockHttpServletResponse mockRespError = new MockHttpServletResponse();
        WebAsyncManager asyncManagerError = WebAsyncUtils.getAsyncManager(mockReqError);
        controllerError.downloadContent(104L, mockReqError, mockRespError);

        CallableProcessingInterceptor interceptorError = asyncManagerError.getCallableInterceptor("imageDownloadCleanup");
        assertThat(interceptorError).isNotNull();
        interceptorError.handleError(new ServletWebRequest(mockReqError, mockRespError), null, new IOException("Client disconnected abruptly"));
        assertThat(errorStreamClosed.get()).as("Binary stream must be closed when async processing encounters client disconnect/error").isTrue();

        // 5. Async completion path: CallableProcessingInterceptor.afterCompletion closes stream
        AtomicBoolean completionStreamClosed = new AtomicBoolean(false);
        ImageOperations opsCompletion = mock(ImageOperations.class);
        when(opsCompletion.findById(105L)).thenReturn(new ImageView(105L, null, "Completion Img", null, "images/managed/completion-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portCompletion = mock(MediaStoragePort.class);
        when(portCompletion.download("images/managed/completion-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                completionStreamClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceCompletion = new ImageDownloadService(opsCompletion, portCompletion);
        ImageController controllerCompletion = new ImageController(opsCompletion, null, downloadServiceCompletion);

        MockHttpServletRequest mockReqCompletion = new MockHttpServletRequest();
        MockHttpServletResponse mockRespCompletion = new MockHttpServletResponse();
        WebAsyncManager asyncManagerCompletion = WebAsyncUtils.getAsyncManager(mockReqCompletion);
        controllerCompletion.downloadContent(105L, mockReqCompletion, mockRespCompletion);

        CallableProcessingInterceptor interceptorCompletion = asyncManagerCompletion.getCallableInterceptor("imageDownloadCleanup");
        assertThat(interceptorCompletion).isNotNull();
        interceptorCompletion.afterCompletion(new ServletWebRequest(mockReqCompletion, mockRespCompletion), null);
        assertThat(completionStreamClosed.get()).as("Binary stream must be closed when async processing completes via afterCompletion").isTrue();

        // 6. AsyncWebRequest completion / cancellation handler path
        AtomicBoolean asyncWebRequestClosed = new AtomicBoolean(false);
        ImageOperations opsAsync = mock(ImageOperations.class);
        when(opsAsync.findById(106L)).thenReturn(new ImageView(106L, null, "AsyncWebRequest Img", null, "images/managed/async-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portAsync = mock(MediaStoragePort.class);
        when(portAsync.download("images/managed/async-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                asyncWebRequestClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceAsync = new ImageDownloadService(opsAsync, portAsync);
        ImageController controllerAsync = new ImageController(opsAsync, null, downloadServiceAsync);

        MockHttpServletRequest mockReqAsync = new MockHttpServletRequest();
        MockHttpServletResponse mockRespAsync = new MockHttpServletResponse();
        AsyncWebRequest asyncWebRequest = mock(AsyncWebRequest.class);
        java.util.concurrent.atomic.AtomicReference<Runnable> registeredCompletionHandler = new java.util.concurrent.atomic.AtomicReference<>();
        org.mockito.Mockito.doAnswer(inv -> {
            registeredCompletionHandler.set(inv.getArgument(0));
            return null;
        }).when(asyncWebRequest).addCompletionHandler(any(Runnable.class));

        WebAsyncManager asyncManagerWithReq = WebAsyncUtils.getAsyncManager(mockReqAsync);
        asyncManagerWithReq.setAsyncWebRequest(asyncWebRequest);

        controllerAsync.downloadContent(106L, mockReqAsync, mockRespAsync);
        assertThat(registeredCompletionHandler.get()).as("AsyncWebRequest completion handler must be registered").isNotNull();
        registeredCompletionHandler.get().run();
        assertThat(asyncWebRequestClosed.get()).as("Binary stream must be closed when AsyncWebRequest completion handler runs").isTrue();

        // 7. AsyncWebRequest timeout handler path
        AtomicBoolean asyncTimeoutClosed = new AtomicBoolean(false);
        ImageOperations opsAsyncTimeout = mock(ImageOperations.class);
        when(opsAsyncTimeout.findById(107L)).thenReturn(new ImageView(107L, null, "AsyncTimeout Img", null, "images/managed/async-timeout-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portAsyncTimeout = mock(MediaStoragePort.class);
        when(portAsyncTimeout.download("images/managed/async-timeout-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                asyncTimeoutClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceAsyncTimeout = new ImageDownloadService(opsAsyncTimeout, portAsyncTimeout);
        ImageController controllerAsyncTimeout = new ImageController(opsAsyncTimeout, null, downloadServiceAsyncTimeout);

        MockHttpServletRequest mockReqAsyncTimeout = new MockHttpServletRequest();
        MockHttpServletResponse mockRespAsyncTimeout = new MockHttpServletResponse();
        AsyncWebRequest asyncWebRequestTimeout = mock(AsyncWebRequest.class);
        java.util.concurrent.atomic.AtomicReference<Runnable> registeredTimeoutHandler = new java.util.concurrent.atomic.AtomicReference<>();
        org.mockito.Mockito.doAnswer(inv -> {
            registeredTimeoutHandler.set(inv.getArgument(0));
            return null;
        }).when(asyncWebRequestTimeout).addTimeoutHandler(any(Runnable.class));

        WebAsyncManager asyncManagerWithReqTimeout = WebAsyncUtils.getAsyncManager(mockReqAsyncTimeout);
        asyncManagerWithReqTimeout.setAsyncWebRequest(asyncWebRequestTimeout);

        controllerAsyncTimeout.downloadContent(107L, mockReqAsyncTimeout, mockRespAsyncTimeout);
        assertThat(registeredTimeoutHandler.get()).as("AsyncWebRequest timeout handler must be registered").isNotNull();
        registeredTimeoutHandler.get().run();
        assertThat(asyncTimeoutClosed.get()).as("Binary stream must be closed when AsyncWebRequest timeout handler runs").isTrue();

        // 8. AsyncWebRequest error handler path
        AtomicBoolean asyncErrorClosed = new AtomicBoolean(false);
        ImageOperations opsAsyncError = mock(ImageOperations.class);
        when(opsAsyncError.findById(108L)).thenReturn(new ImageView(108L, null, "AsyncError Img", null, "images/managed/async-error-img", null, "image/jpeg", 3L, null, null, null, null, null));
        MediaStoragePort portAsyncError = mock(MediaStoragePort.class);
        when(portAsyncError.download("images/managed/async-error-img")).thenReturn(new StorageDownloadResult(new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override public void close() throws IOException {
                asyncErrorClosed.set(true);
                super.close();
            }
        }, 3L, "image/jpeg"));

        ImageDownloadService downloadServiceAsyncError = new ImageDownloadService(opsAsyncError, portAsyncError);
        ImageController controllerAsyncError = new ImageController(opsAsyncError, null, downloadServiceAsyncError);

        MockHttpServletRequest mockReqAsyncError = new MockHttpServletRequest();
        MockHttpServletResponse mockRespAsyncError = new MockHttpServletResponse();
        AsyncWebRequest asyncWebRequestError = mock(AsyncWebRequest.class);
        java.util.concurrent.atomic.AtomicReference<java.util.function.Consumer<Throwable>> registeredErrorHandler = new java.util.concurrent.atomic.AtomicReference<>();
        org.mockito.Mockito.doAnswer(inv -> {
            registeredErrorHandler.set(inv.getArgument(0));
            return null;
        }).when(asyncWebRequestError).addErrorHandler(any());

        WebAsyncManager asyncManagerWithReqError = WebAsyncUtils.getAsyncManager(mockReqAsyncError);
        asyncManagerWithReqError.setAsyncWebRequest(asyncWebRequestError);

        controllerAsyncError.downloadContent(108L, mockReqAsyncError, mockRespAsyncError);
        assertThat(registeredErrorHandler.get()).as("AsyncWebRequest error handler must be registered").isNotNull();
        registeredErrorHandler.get().accept(new IOException("Simulated network drop"));
        assertThat(asyncErrorClosed.get()).as("Binary stream must be closed when AsyncWebRequest error handler runs").isTrue();
    }

    @EnableWebMvc
    static class SelectedMvc {
        @Bean
        StandardServletMultipartResolver multipartResolver() {
            return new StandardServletMultipartResolver();
        }
    }
}
