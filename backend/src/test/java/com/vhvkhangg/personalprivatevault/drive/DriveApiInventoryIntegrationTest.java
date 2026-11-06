package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(DriveApiInventoryIntegrationTest.ControlledOAuth.class)
class DriveApiInventoryIntegrationTest extends AbstractWebIntegrationTest {

    private static FakeGoogleOAuthServer fakeServer;
    private static Path tempKeyFile;

    @BeforeAll
    static void initDependencies() throws Exception {
        fakeServer = new FakeGoogleOAuthServer();
        tempKeyFile = Files.createTempFile("ppv-drive-inv-key-", ".bin");
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        Files.write(tempKeyFile, keyBytes);
    }

    @AfterAll
    static void cleanupDependencies() throws Exception {
        if (fakeServer != null) {
            fakeServer.close();
        }
        if (tempKeyFile != null) {
            Files.deleteIfExists(tempKeyFile);
        }
    }

    @DynamicPropertySource
    static void configureDriveProperties(DynamicPropertyRegistry registry) {
        registry.add("ppv.drive.enabled", () -> "true");
        registry.add("ppv.drive.token-key-file", () -> tempKeyFile.toAbsolutePath().toString());
        registry.add("ppv.drive.oauth.client-id", () -> "test-client-id");
        registry.add("ppv.drive.oauth.client-secret", () -> "test-client-secret");
        registry.add("ppv.drive.oauth.redirect-uri", () -> "http://localhost:8080/api/v1/drive/oauth/callback");
    }

    @Test
    @DisplayName("When Drive is enabled, OpenAPI exposes exactly 239 total operations with 4 Drive operations")
    void openApiExposesExact239OperationsWhenDriveEnabled() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode pathsNode = rootNode.path("paths");
        assertThat(pathsNode.isObject()).isTrue();

        List<String> operationIds = new ArrayList<>();
        Iterator<String> fieldNames = pathsNode.fieldNames();
        while (fieldNames.hasNext()) {
            String path = fieldNames.next();
            JsonNode pathItem = pathsNode.path(path);
            Iterator<String> methodNames = pathItem.fieldNames();
            while (methodNames.hasNext()) {
                String method = methodNames.next();
                if (List.of("get", "post", "put", "delete", "patch").contains(method.toLowerCase())) {
                    JsonNode op = pathItem.path(method);
                    String opId = op.path("operationId").asText();
                    operationIds.add(opId);
                }
            }
        }

        // Exact count: 235 existing + 4 new Drive operations = 239 total operations
        assertThat(operationIds)
                .as("OpenAPI must document exactly 239 operations when Drive is enabled")
                .hasSize(239);

        // Verify the 4 exact Drive operations
        assertThat(operationIds).contains(
                "authorizeDriveConnection",
                "completeDriveConnection",
                "listDriveConnections",
                "disconnectDriveConnection"
        );

        // Verify callback OpenAPI specification: text/plain response and empty security requirement
        JsonNode callbackOp = pathsNode.path("/api/v1/drive/oauth/callback").path("get");
        assertThat(callbackOp.isMissingNode()).isFalse();
        JsonNode callbackResponses = callbackOp.path("responses");
        assertThat(callbackResponses.has("200")).isTrue();
        assertThat(callbackResponses.path("200").path("content").has("text/plain")).isTrue();
        assertThat(callbackOp.path("security").isArray()).isTrue();
        assertThat(callbackOp.path("security").size()).isZero();
        for (String code : List.of("400", "409", "500", "502", "503", "504")) {
            assertThat(callbackResponses.path(code).path("content").has("text/plain")).isTrue();
            assertThat(callbackResponses.path(code).path("content").has("application/json")).isFalse();
        }

        // Verify authorize OpenAPI specification: has bearerAuth
        JsonNode authOp = pathsNode.path("/api/v1/drive/connections/{slot}/authorize").path("post");
        assertThat(authOp.isMissingNode()).isFalse();
        JsonNode authSecurity = authOp.path("security");
        assertThat(authSecurity.toString()).contains("bearerAuth");
    }
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    static class ControlledOAuth {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient controlledOAuth(java.time.Clock clock) {
            return new com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient(
                    "test-client-id", "test-client-secret", fakeServer.getTokenEndpoint(), fakeServer.getRevokeEndpoint(),
                    fakeServer.getJwksUri(), clock, null);
        }
    }
}
