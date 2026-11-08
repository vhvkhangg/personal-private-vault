package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.drive.fixture.DriveS2HttpTestSupport;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleThumbnailServer;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileAuthority;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient;
import com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot.PortabilitySnapshotAdapter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DriveH1BWebIntegrationTest extends DriveS2HttpTestSupport {

    @Test
    @DisplayName("B-08: Complete HTTP endpoints (folders, items, quota, child upload, thumbnail) succeed with ApiResponse and proper status")
    void httpEndpointsSucceedWithProperStatus() throws Exception {
        ensure(1);

        // 1. Storage quota: GET /api/v1/drive/connections/1/storage-quota
        var quotaRes = send(request(path(1) + "/storage-quota").GET());
        assertThat(quotaRes.statusCode()).isEqualTo(200);
        JsonNode quotaData = data(quotaRes);
        assertThat(quotaData.hasNonNull("usageBytes")).isTrue();
        assertThat(quotaData.hasNonNull("usageInDriveBytes")).isTrue();
        assertThat(quotaData.hasNonNull("observedAt")).isTrue();

        // 2. Child folder: POST /api/v1/drive/connections/1/folders
        String folderAction = key();
        var folderRes = postFolder(1, folderAction, "WebChildFolder");
        assertThat(folderRes.statusCode()).isEqualTo(201);
        JsonNode folderData = data(folderRes);
        String childId = folderData.path("id").asText();
        assertThat(childId).isNotBlank();
        assertThat(folderData.path("name").asText()).isEqualTo("WebChildFolder");
        assertThat(folderData.path("kind").asText()).isEqualTo("folder");

        // Replay of same folder action -> 200
        var folderReplay = postFolder(1, folderAction, "WebChildFolder");
        assertThat(folderReplay.statusCode()).isEqualTo(200);

        // 3. Child upload: POST /api/v1/drive/connections/1/folders/{childId}/files
        String uploadAction = key();
        byte[] payload = new byte[]{1, 2, 3, 4, 5};
        var uploadRes = uploadChildFile(1, childId, uploadAction, payload, "web-doc.bin", "application/octet-stream");
        assertThat(uploadRes.statusCode()).isEqualTo(201);
        JsonNode fileData = data(uploadRes);
        String fileId = fileData.path("id").asText();
        assertThat(fileId).isNotBlank();
        assertThat(fileData.path("name").asText()).isEqualTo("web-doc.bin");

        // 4. Items listing: GET /api/v1/drive/connections/1/items
        var itemsRes = send(request(path(1) + "/items?scope=all").GET());
        assertThat(itemsRes.statusCode()).isEqualTo(200);
        JsonNode itemsData = data(itemsRes);
        assertThat(itemsData.path("items").isArray()).isTrue();
        assertThat(itemsData.path("items").size()).isGreaterThanOrEqualTo(2); // folder + file

        // 5. Download child file: GET /api/v1/drive/connections/1/files/{fileId}/download
        var dlRes = send(request(path(1) + "/files/" + fileId + "/download").GET());
        assertThat(dlRes.statusCode()).isEqualTo(200);
        assertThat(dlRes.headers().firstValue("Content-Type").orElse("")).isEqualTo("application/octet-stream");
        assertThat(dlRes.headers().firstValue("Cache-Control").orElse("")).isEqualTo("no-store");
        assertThat(dlRes.body()).isEqualTo(payload);

        // 6. Thumbnail: GET /api/v1/drive/connections/1/files/{fileId}/thumbnail
        String thumbUrl = THUMBNAIL.origin() + "/thumbnail/" + fileId;
        GOOGLE.files.get(fileId).metadata().put("thumbnailLink", thumbUrl);
        var thumbRes = send(request(path(1) + "/files/" + fileId + "/thumbnail").GET());
        assertThat(thumbRes.statusCode()).isEqualTo(200);
        assertThat(thumbRes.headers().firstValue("Content-Type").orElse("")).isEqualTo("image/png");
        assertThat(thumbRes.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
        assertThat(thumbRes.body()).isEqualTo(FakeGoogleThumbnailServer.defaultPng());

        // File without thumbnailLink -> 404
        var noThumbRes = send(request(path(1) + "/files/missing-thumb-id/thumbnail").GET());
        assertThat(noThumbRes.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("B-08: Authentication and authorization deny invalid tokens and non-owner subjects")
    void authAndSecurityDenials() throws Exception {
        // Unauthenticated request without bearer -> 401
        var unauth = send(HttpRequest.newBuilder(uri(path(1) + "/storage-quota")).GET());
        assertThat(unauth.statusCode()).isEqualTo(401);

        // Authenticated non-owner (sub=2) -> 403
        String nonOwnerToken = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer("personal-private-vault").subject("2").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build())).getTokenValue();
        var nonOwner = send(HttpRequest.newBuilder(uri(path(1) + "/storage-quota")).header("Authorization", "Bearer " + nonOwnerToken).GET());
        assertThat(nonOwner.statusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("B-08: OpenAPI document contains exactly 248 operations including all 13 Drive operations")
    void openApiHasExactOperationInventory() throws Exception {
        var response = send(HttpRequest.newBuilder(uri("/v3/api-docs")).GET());
        assertThat(response.statusCode()).isEqualTo(200);
        var document = mapper.readTree(response.body());
        var paths = document.path("paths");

        int count = 0;
        Set<String> operationIds = new HashSet<>();
        for (var item : paths) {
            for (var names = item.fieldNames(); names.hasNext(); ) {
                String method = names.next();
                if (Set.of("get", "post", "put", "patch", "delete").contains(method)) {
                    count++;
                    String opId = item.path(method).path("operationId").asText("");
                    if (!opId.isBlank()) {
                        operationIds.add(opId);
                    }
                }
            }
        }

        assertThat(count).isEqualTo(248);

        // Verify all 13 Drive operation IDs are present
        Set<String> expectedDriveOpIds = Set.of(
                "ensureDriveRootFolder",
                "listDriveRootFiles",
                "uploadDriveRootFile",
                "downloadDriveRootFile",
                "getDriveStorageQuota",
                "createDriveChildFolder",
                "listDriveItems",
                "uploadDriveChildFile",
                "downloadDriveFileThumbnail",
                "authorizeDriveConnection",
                "completeDriveConnection",
                "listDriveConnections",
                "disconnectDriveConnection"
        );
        assertThat(operationIds).containsAll(expectedDriveOpIds);
    }

    @Test
    @DisplayName("B-08: Portability excluded security tables contains drive_child_folder_intents and Modulith verification succeeds")
    void portabilityAndModulithArchitectureVerification() {
        assertThat(PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES)
                .contains("drive_child_folder_intents");

        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        modules.verify();
    }

    private HttpResponse<byte[]> postFolder(int slot, String key, String name) throws Exception {
        return send(request(path(slot) + "/folders")
                .header("Idempotency-Key", key)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"" + name + "\"}")));
    }

    private HttpResponse<byte[]> uploadChildFile(int slot, String childId, String key, byte[] bytes, String name, String mime) throws Exception {
        String boundary = "h1b_web_boundary";
        var prefix = HttpRequest.BodyPublishers.ofByteArray(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + name + "\"\r\nContent-Type: " + mime + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        var suffix = HttpRequest.BodyPublishers.ofByteArray(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII));
        var combined = HttpRequest.BodyPublishers.concat(prefix, HttpRequest.BodyPublishers.ofByteArray(bytes), suffix);
        return send(request(path(slot) + "/folders/" + childId + "/files")
                .header("Idempotency-Key", key)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(combined));
    }
}
