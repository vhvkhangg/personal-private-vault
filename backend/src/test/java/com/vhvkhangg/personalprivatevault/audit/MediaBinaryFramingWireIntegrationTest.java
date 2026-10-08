package com.vhvkhangg.personalprivatevault.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiExceptionHandler;
import com.vhvkhangg.personalprivatevault.media.image.ImageOperations;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.ImageDownloadService;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.MediaStoragePort;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageDownloadResult;
import com.vhvkhangg.personalprivatevault.media.internal.application.storage.StorageIntegrityException;
import com.vhvkhangg.personalprivatevault.media.internal.web.advice.MediaExceptionAdvice;
import com.vhvkhangg.personalprivatevault.media.internal.web.controller.ImageController;
import com.vhvkhangg.personalprivatevault.media.view.ImageView;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BA15-7 and BA15-8 regression tests:
 * Verifies real HTTP wire framing behavior:
 * 1. Known provider length (including 0) wins over mutable metadata size.
 * 2. Real downloads after smaller/larger/null metadata edits return exact provider bytes with provider length.
 * 3. Genuinely unknown provider length falls back to metadata size.
 * 4. Pre-commitment storage failure returns complete canonical JSON error.
 * 5. Post-commitment streaming failure aborts connection without appending JSON, and executes stream cleanup.
 */
class MediaBinaryFramingWireIntegrationTest {

    private Tomcat tomcat;
    private Path tempDir;
    private ImageOperations imageOperations;
    private MediaStoragePort mediaStoragePort;
    private GenericWebApplicationContext app;
    private HttpClient httpClient;
    private int port;

    @BeforeEach
    void setUpServer() throws Exception {
        tempDir = Files.createTempDirectory("ppv-media-wire-test-");
        tomcat = new Tomcat();
        tomcat.setBaseDir(tempDir.toAbsolutePath().toString());
        tomcat.setPort(0);
        tomcat.getConnector();
        var context = tomcat.addContext("", tempDir.toAbsolutePath().toString());

        imageOperations = mock(ImageOperations.class);
        mediaStoragePort = mock(MediaStoragePort.class);

        app = new GenericWebApplicationContext();
        app.registerBean("imageOperations", ImageOperations.class, () -> imageOperations);
        app.registerBean("mediaStoragePort", MediaStoragePort.class, () -> mediaStoragePort);
        app.registerBean("downloadService", ImageDownloadService.class, () -> new ImageDownloadService(imageOperations, mediaStoragePort));
        app.registerBean("imageController", ImageController.class, () -> new ImageController(imageOperations, null, new ImageDownloadService(imageOperations, mediaStoragePort)));
        app.registerBean("mediaExceptionAdvice", MediaExceptionAdvice.class, MediaExceptionAdvice::new);
        app.registerBean("apiExceptionHandler", ApiExceptionHandler.class, ApiExceptionHandler::new);
        app.registerBean(org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver.class, () -> {
            var resolver = new org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver();
            resolver.getMessageConverters().add(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter());
            resolver.setApplicationContext(app);
            resolver.afterPropertiesSet();
            return resolver;
        });
        app.refresh();

        var wrapper = Tomcat.addServlet(context, "dispatcher", new DispatcherServlet(app));
        wrapper.setAsyncSupported(true);
        wrapper.setLoadOnStartup(1);
        context.addServletMappingDecoded("/*", "dispatcher");

        tomcat.start();
        port = tomcat.getConnector().getLocalPort();

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void tearDownServer() {
        if (app != null) {
            try { app.close(); } catch (Exception ignored) {}
        }
        if (tomcat != null) {
            try { tomcat.stop(); } catch (Exception ignored) {}
            try { tomcat.destroy(); } catch (Exception ignored) {}
        }
        if (tempDir != null) {
            try (var s = Files.walk(tempDir)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("BA15-8: Known provider length of 0 takes precedence over metadata size of 75")
    void providerLengthZeroTakesPrecedenceOverMetadataSize() throws Exception {
        when(imageOperations.findById(1L)).thenReturn(new ImageView(
                1L, null, "Zero Image", null, "img-zero", null, "image/png", 75L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-zero")).thenReturn(new StorageDownloadResult(
                new ByteArrayInputStream(new byte[0]), 0L, "image/png"
        ));

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/1/content"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Length")).hasValue("0");
        assertThat(response.body()).isEmpty();
    }

    @Test
    @DisplayName("BA15-8: Provider length wins and exact bytes returned when metadata is edited smaller, larger, or null")
    void providerLengthWinsWhenMetadataEdited() throws Exception {
        byte[] providerBytes = new byte[100];
        for (int i = 0; i < 100; i++) {
            providerBytes[i] = (byte) (i & 0xFF);
        }

        // Case A: Metadata edited smaller (50L vs provider 100L)
        when(imageOperations.findById(2L)).thenReturn(new ImageView(
                2L, null, "Image Smaller", null, "img-small", null, "image/jpeg", 50L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-small")).thenReturn(new StorageDownloadResult(
                new ByteArrayInputStream(providerBytes), 100L, "image/jpeg"
        ));

        HttpResponse<byte[]> respA = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/2/content")).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );
        assertThat(respA.statusCode()).isEqualTo(200);
        assertThat(respA.headers().firstValue("Content-Length")).hasValue("100");
        assertThat(respA.body()).isEqualTo(providerBytes);

        // Case B: Metadata edited larger (200L vs provider 100L)
        when(imageOperations.findById(3L)).thenReturn(new ImageView(
                3L, null, "Image Larger", null, "img-large", null, "image/jpeg", 200L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-large")).thenReturn(new StorageDownloadResult(
                new ByteArrayInputStream(providerBytes), 100L, "image/jpeg"
        ));

        HttpResponse<byte[]> respB = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/3/content")).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );
        assertThat(respB.statusCode()).isEqualTo(200);
        assertThat(respB.headers().firstValue("Content-Length")).hasValue("100");
        assertThat(respB.body()).isEqualTo(providerBytes);

        // Case C: Metadata size is null
        when(imageOperations.findById(4L)).thenReturn(new ImageView(
                4L, null, "Image Null Meta", null, "img-null", null, "image/jpeg", null, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-null")).thenReturn(new StorageDownloadResult(
                new ByteArrayInputStream(providerBytes), 100L, "image/jpeg"
        ));

        HttpResponse<byte[]> respC = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/4/content")).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );
        assertThat(respC.statusCode()).isEqualTo(200);
        assertThat(respC.headers().firstValue("Content-Length")).hasValue("100");
        assertThat(respC.body()).isEqualTo(providerBytes);
    }

    @Test
    @DisplayName("BA15-8: Unknown provider length falls back to metadata size")
    void unknownProviderLengthFallsBackToMetadata() throws Exception {
        byte[] providerBytes = new byte[80];
        when(imageOperations.findById(5L)).thenReturn(new ImageView(
                5L, null, "Fallback Image", null, "img-fallback", null, "image/png", 80L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-fallback")).thenReturn(new StorageDownloadResult(
                new ByteArrayInputStream(providerBytes), null, "image/png"
        ));

        HttpResponse<byte[]> response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/5/content")).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Length")).hasValue("80");
        assertThat(response.body()).isEqualTo(providerBytes);
    }

    @Test
    @DisplayName("BA15-7: Pre-commitment storage failure clears headers and returns canonical JSON error")
    void preCommitmentStorageFailureReturnsCanonicalJsonError() throws Exception {
        when(imageOperations.findById(6L)).thenReturn(new ImageView(
                6L, null, "Missing Image", null, "img-missing", null, "image/png", 100L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-missing")).thenThrow(new StorageIntegrityException("Object missing from storage"));

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/6/content")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(ct -> assertThat(ct).contains("application/json"));

        var json = new ObjectMapper().readTree(response.body());
        assertThat(json.path("error").path("code").asText()).isEqualTo("STORAGE_INTEGRITY_ERROR");
    }

    @Test
    @DisplayName("BA15-7: Post-commitment stream failure aborts transfer without appending JSON and executes cleanup")
    void postCommitmentStreamFailureAbortsWithoutJsonAppendAndExecutesCleanup() throws Exception {
        AtomicInteger streamClosed = new AtomicInteger();
        when(imageOperations.findById(7L)).thenReturn(new ImageView(
                7L, null, "Corrupt Image", null, "img-corrupt", null, "image/png", null, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-corrupt")).thenReturn(new StorageDownloadResult(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Stream failure mid-read");
            }
            @Override
            public long transferTo(OutputStream out) throws IOException {
                out.write(new byte[16384]);
                out.flush();
                throw new IOException("Simulated mid-transfer pipe break");
            }
            @Override
            public void close() {
                streamClosed.incrementAndGet();
            }
        }, null, "application/octet-stream"));

        assertThatThrownBy(() -> httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/7/content")).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray()
        )).isInstanceOf(IOException.class);

        assertThat(streamClosed.get()).as("Underlying storage stream must be closed").isGreaterThan(0);
    }

    @Test
    @DisplayName("BA15-7: First-read failure when prepared Content-Length is smaller than error JSON clears binary framing and returns complete JSON")
    void firstReadFailureWithSmallContentLengthClearsHeadersAndReturnsCanonicalJson() throws Exception {
        AtomicInteger streamClosed = new AtomicInteger();
        when(imageOperations.findById(10L)).thenReturn(new ImageView(
                10L, null, "Small Framing Fail", null, "img-small-fail", null, "image/png", 20L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-small-fail")).thenReturn(new StorageDownloadResult(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("First-read storage failure (small)");
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("First-read storage failure (small)");
            }
            @Override
            public void close() {
                streamClosed.incrementAndGet();
            }
        }, 20L, "image/png"));

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/10/content")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(ct -> assertThat(ct).contains("application/json"));
        assertThat(response.headers().firstValue("Content-Length").orElse("")).isNotEqualTo("20");

        var json = new ObjectMapper().readTree(response.body());
        assertThat(json.path("error").path("code").asText()).isEqualTo("INTERNAL_ERROR");
        assertThat(streamClosed.get()).as("Underlying storage stream must be closed").isGreaterThan(0);
    }

    @Test
    @DisplayName("BA15-7: First-read failure when prepared Content-Length is larger than error JSON clears binary framing and returns complete JSON")
    void firstReadFailureWithLargeContentLengthClearsHeadersAndReturnsCanonicalJson() throws Exception {
        AtomicInteger streamClosed = new AtomicInteger();
        when(imageOperations.findById(11L)).thenReturn(new ImageView(
                11L, null, "Large Framing Fail", null, "img-large-fail", null, "image/jpeg", 50000L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-large-fail")).thenReturn(new StorageDownloadResult(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("First-read storage failure (large)");
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("First-read storage failure (large)");
            }
            @Override
            public void close() {
                streamClosed.incrementAndGet();
            }
        }, 50000L, "image/jpeg"));

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/images/11/content")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(ct -> assertThat(ct).contains("application/json"));
        assertThat(response.headers().firstValue("Content-Length").orElse("")).isNotEqualTo("50000");

        var json = new ObjectMapper().readTree(response.body());
        assertThat(json.path("error").path("code").asText()).isEqualTo("INTERNAL_ERROR");
        assertThat(streamClosed.get()).as("Underlying storage stream must be closed").isGreaterThan(0);
    }

    @Test
    @DisplayName("BA15-7: Client abort/disconnect triggers stream cleanup and releases resources")
    void clientAbortOrTimeoutTriggersStreamCleanup() throws Exception {
        AtomicInteger streamClosed = new AtomicInteger();

        when(imageOperations.findById(12L)).thenReturn(new ImageView(
                12L, null, "Disconnect Image", null, "img-disc", null, "application/octet-stream", 100_000_000L, null, null, null, null, null
        ));
        when(mediaStoragePort.download("img-disc")).thenReturn(new StorageDownloadResult(new InputStream() {
            @Override
            public int read() {
                return 42;
            }
            @Override
            public int read(byte[] b, int off, int len) {
                return len;
            }
            @Override
            public void close() {
                streamClosed.incrementAndGet();
            }
        }, 100_000_000L, "application/octet-stream"));

        // Connect via raw socket, read first bytes of the response, and abruptly close
        try (var socket = new java.net.Socket("localhost", port)) {
            var out = socket.getOutputStream();
            out.write(("GET /api/v1/images/12/content HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n").getBytes());
            out.flush();
            var in = socket.getInputStream();
            byte[] buf = new byte[128];
            int read = in.read(buf);
            assertThat(read).isGreaterThan(0);
            // Abrupt close
        }

        // Await server detecting disconnect and running stream cleanup
        long deadline = System.currentTimeMillis() + 5000;
        while (streamClosed.get() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertThat(streamClosed.get()).as("Underlying storage stream must be closed on client disconnect").isGreaterThan(0);
    }
}
