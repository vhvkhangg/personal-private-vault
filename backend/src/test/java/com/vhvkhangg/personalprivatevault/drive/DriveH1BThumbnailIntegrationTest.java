package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.drive.fixture.DriveH1BTestSupport;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleThumbnailServer;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveH1BThumbnailIntegrationTest extends DriveH1BTestSupport {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://lh3.googleusercontent.com/test",
            "https://evil.com/thumb",
            "https://lh3.googleusercontent.com.evil.test/thumb",
            "https://lh4.googleusercontent.com/thumb",
            "https://127.0.0.1/thumb",
            "https://localhost/thumb",
            "https://user:pass@lh3.googleusercontent.com/thumb",
            "https://lh3.googleusercontent.com/thumb#fragment",
            "https://lh3.googleusercontent.com:444/thumb",
            "ftp://lh3.googleusercontent.com/thumb",
            "not-a-valid-url"
    })
    @DisplayName("B-06: Production URI validator strictly rejects unapproved hosts, protocols, and ports")
    void forbiddenDestinationsAreRejectedByValidator(String forbiddenUrl) {
        assertThat(DriveFileHttpClient.isValidProductionThumbnailUrl(forbiddenUrl)).isFalse();
    }

    @Test
    @DisplayName("B-06: Production URI validator accepts exact https://lh3.googleusercontent.com on 443/default")
    void validProductionThumbnailUrlAccepted() {
        assertThat(DriveFileHttpClient.isValidProductionThumbnailUrl("https://lh3.googleusercontent.com/sample_thumbnail")).isTrue();
        assertThat(DriveFileHttpClient.isValidProductionThumbnailUrl("https://lh3.googleusercontent.com:443/sample_thumbnail")).isTrue();
    }

    @Test
    @DisplayName("B-06: Authorized file with valid thumbnail returns safe image bytes and headers")
    void authorizedThumbnailSucceeds() throws Exception {
        connect(1, "SUB_B06");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();

        // Attach thumbnail link in metadata pointing to controlled thumbnail server
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        try (var op = resources.open(false)) {
            var thumbResult = thumbnailService.thumbnail("1", (short) 1, fileId, op);
            assertThat(thumbResult.data().contentType()).isEqualTo("image/png");
            assertThat(thumbResult.data().bytes()).isEqualTo(FakeGoogleThumbnailServer.defaultPng());
        }

        assertThat(thumbnailServer.dispatches).hasValue(1);
        var record = thumbnailServer.requests.poll();
        assertThat(record).isNotNull();
        assertThat(record.authHeader()).startsWith("Bearer ");
    }

    @Test
    @DisplayName("B-06: Unapproved destination in thumbnailLink causes zero bearer dispatch and sanitized 404")
    void unapprovedDestinationCausesZeroBearerDispatch() throws Exception {
        connect(1, "SUB_B06_EVIL");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();

        // Point to evil destination
        google.files.get(fileId).metadata().put("thumbnailLink", "https://evil.com/steal-token");

        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE");
                        assertThat(e.getHttpStatus()).isEqualTo(404);
                    });
        }

        // Assert ZERO bearer dispatch occurred to thumbnail server
        assertThat(thumbnailServer.dispatches).hasValue(0);
    }

    @Test
    @DisplayName("B-06: Invalid image signature, >1 MiB, or 302 redirect yields sanitized 404 fallback")
    void contentControlsEnforceHonestFallback() throws Exception {
        connect(1, "SUB_B06_CONTENT");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        // 1. Invalid signature (HTML payload declaring image/png)
        thumbnailServer.responseBody = "<html><body>Not an image</body></html>".getBytes();
        thumbnailServer.responseContentType = "image/png";
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE"));
        }

        // 2. >1 MiB image
        thumbnailServer.responseBody = new byte[1_048_577];
        thumbnailServer.responseContentType = "image/png";
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE"));
        }

        // 3. 302 redirect
        thumbnailServer.redirect = true;
        thumbnailServer.redirectLocation = "/other";
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE"));
        }
    }

    @Test
    @DisplayName("B-06: Truncated PNG signature-only (8 bytes) is rejected with sanitized 404")
    void truncatedPngMustFallBack() throws Exception {
        connect(1, "SUB_B06_TRUNCATED");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        thumbnailServer.responseBody = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        thumbnailServer.responseContentType = "image/png";

        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE");
                        assertThat(e.getHttpStatus()).isEqualTo(404);
                    });
        }
    }

    @Test
    @DisplayName("B-06: Authorized file with valid complete JPEG thumbnail returns safe image bytes")
    void authorizedJpegThumbnailSucceeds() throws Exception {
        connect(1, "SUB_B06_JPEG");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();

        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        byte[] validJpeg = FakeGoogleThumbnailServer.defaultJpeg();
        thumbnailServer.responseContentType = "image/jpeg";
        thumbnailServer.responseBody = validJpeg;

        try (var op = resources.open(false)) {
            var thumbResult = thumbnailService.thumbnail("1", (short) 1, fileId, op);
            assertThat(thumbResult.data().contentType()).isEqualTo("image/jpeg");
            assertThat(thumbResult.data().bytes()).isEqualTo(validJpeg);
        }
    }

    @Test
    @DisplayName("B-06: Truncated JPEG with premature EOI fails complete image verification and returns 404")
    void truncatedJpegMustFallBack() throws Exception {
        connect(1, "SUB_B06_TRUNC_JPEG");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        var image = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                image.setRGB(x, y, ((x * 37 + y * 13) & 255) << 16 | ((x * 11 + y * 29) & 255) << 8 | ((x * 17 + y * 7) & 255));
            }
        }
        var baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "jpeg", baos);
        byte[] complete = baos.toByteArray();

        int entropyStart = -1;
        for (int p = 2; p < complete.length - 4; p++) {
            if ((complete[p] & 255) == 255 && (complete[p + 1] & 255) == 218) {
                entropyStart = p + 2 + (((complete[p + 2] & 255) << 8) | (complete[p + 3] & 255));
                break;
            }
        }
        assertThat(entropyStart).isPositive();
        byte[] truncated = java.util.Arrays.copyOf(complete, entropyStart + 10);
        truncated[truncated.length - 2] = (byte) 255;
        truncated[truncated.length - 1] = (byte) 217;

        thumbnailServer.responseBody = truncated;
        thumbnailServer.responseContentType = "image/jpeg";

        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE");
                        assertThat(e.getHttpStatus()).isEqualTo(404);
                    });
        }
    }

    @Test
    @DisplayName("B-06: Thumbnail body idle gap (>5s) expires before 6-second pause completes")
    void thumbnailBodyIdleMustExpireBeforeSixSecondGapCompletes() throws Exception {
        connect(1, "SUB_B06_IDLE");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        byte[] validPng = FakeGoogleThumbnailServer.defaultPng();
        byte[] prefix = new byte[8];
        System.arraycopy(validPng, 0, prefix, 0, 8);
        byte[] rest = new byte[validPng.length - 8];
        System.arraycopy(validPng, 8, rest, 0, rest.length);

        thumbnailServer.bodyPrefix = prefix;
        thumbnailServer.bodyPauseMs = 6000;
        thumbnailServer.responseBody = rest;
        thumbnailServer.responseContentType = "image/png";

        long start = System.currentTimeMillis();
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE");
                        assertThat(e.getHttpStatus()).isEqualTo(404);
                    });
            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).as("five-second idle deadline must beat six-second gap").isLessThan(5900L);
        }
    }

    @Test
    @DisplayName("B-06: Progressing stream exceeding 8-second total transfer deadline fails with 404")
    void progressingStreamExceedingEightSecondDeadlineFailsWith404() throws Exception {
        connect(1, "SUB_B06_TOTAL_TIMEOUT");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        byte[] validPng = FakeGoogleThumbnailServer.defaultPng();
        byte[] chunk1 = new byte[4];
        System.arraycopy(validPng, 0, chunk1, 0, 4);
        byte[] chunk2 = new byte[4];
        System.arraycopy(validPng, 4, chunk2, 0, 4);
        byte[] chunk3 = new byte[4];
        System.arraycopy(validPng, 8, chunk3, 0, 4);
        byte[] chunk4 = new byte[validPng.length - 12];
        System.arraycopy(validPng, 12, chunk4, 0, chunk4.length);

        thumbnailServer.progressiveChunks = java.util.List.of(chunk1, chunk2, chunk3, chunk4);
        thumbnailServer.chunkPauseMs = 3000;
        thumbnailServer.responseContentType = "image/png";

        long start = System.currentTimeMillis();
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("THUMBNAIL_UNAVAILABLE");
                        assertThat(e.getHttpStatus()).isEqualTo(404);
                    });
            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).as("8s transfer deadline expires around 8s").isGreaterThanOrEqualTo(7500L).isLessThan(9500L);
        }
    }

    @Test
    @DisplayName("B-06: Thumbnail request aborted via operation resource fails closed")
    void thumbnailRequestAbortedByOperationFailsClosed() throws Exception {
        connect(1, "SUB_B06_ABORT");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        thumbnailServer.responseEntered = new java.util.concurrent.CountDownLatch(1);
        thumbnailServer.responseRelease = new java.util.concurrent.CountDownLatch(1);

        try (var op = resources.open(false);
             var workers = java.util.concurrent.Executors.newFixedThreadPool(1)) {
            var future = workers.submit(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op));

            assertThat(thumbnailServer.responseEntered.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            op.cancel();
            thumbnailServer.responseRelease.countDown();

            assertThatThrownBy(future::get)
                    .hasCauseInstanceOf(DriveFileException.class)
                    .extracting(t -> ((DriveFileException) t.getCause()).getErrorCode())
                    .isIn("THUMBNAIL_UNAVAILABLE", "DRIVE_PROVIDER_TIMEOUT");
        } finally {
            if (thumbnailServer.responseRelease != null) thumbnailServer.responseRelease.countDown();
        }
    }

    @Test
    @DisplayName("B-06: In-flight credential epoch change rejects thumbnail publication with 409")
    void inFlightCredentialEpochChangeRejectsThumbnailPublication() throws Exception {
        connect(1, "SUB_B06_EPOCH");
        ensure(1);
        var upload = upload(1, key(), new byte[]{1, 2, 3});
        String fileId = upload.data().id();
        String thumbUrl = thumbnailServer.origin() + "/thumbnail/" + fileId;
        google.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);

        thumbnailServer.responseEntered = new java.util.concurrent.CountDownLatch(1);
        thumbnailServer.responseRelease = new java.util.concurrent.CountDownLatch(1);

        try (var op = resources.open(false);
             var workers = java.util.concurrent.Executors.newFixedThreadPool(1)) {
            var future = workers.submit(() -> thumbnailService.thumbnail("1", (short) 1, fileId, op));

            assertThat(thumbnailServer.responseEntered.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

            // Advance slot credential epoch during transfer
            jdbc.update("UPDATE drive_connections SET credential_epoch = ? WHERE slot = 1", java.util.UUID.randomUUID());

            thumbnailServer.responseRelease.countDown();

            assertThatThrownBy(future::get)
                    .hasCauseInstanceOf(DriveFileException.class)
                    .extracting(t -> ((DriveFileException) t.getCause()).getErrorCode())
                    .isEqualTo("DRIVE_STALE_AUTHORITY");
        } finally {
            if (thumbnailServer.responseRelease != null) thumbnailServer.responseRelease.countDown();
        }
    }
}
