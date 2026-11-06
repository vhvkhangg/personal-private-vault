package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(DriveWebIntegrationTest.ControlledOAuth.class)
class DriveWebIntegrationTest extends AbstractWebIntegrationTest {

    private static FakeGoogleOAuthServer fakeServer;
    private static Path tempKeyFile;

    @BeforeAll
    static void initDependencies() throws Exception {
        fakeServer = new FakeGoogleOAuthServer();
        tempKeyFile = Files.createTempFile("ppv-drive-key-", ".bin");
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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vhvkhangg.personalprivatevault.drive.internal.oauth.SubjectLifecycleManager lifecycleManager;

    @BeforeEach
    void resetDriveDb() {
        fakeServer.reset();
        jdbcTemplate.update("DELETE FROM public.drive_connections");
        if (lifecycleManager != null) {
            lifecycleManager.initializeFromDatabase(java.util.List.of());
        }
    }

    @Test
    @DisplayName("Exact callback matcher: GET callback is public; POST, PUT, DELETE to callback return 401")
    void callbackMatcherPermitsOnlyGet() throws Exception {
        // Non-GET to callback requires authentication and fails with 401
        mockMvc.perform(post("/api/v1/drive/oauth/callback"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(put("/api/v1/drive/oauth/callback"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(delete("/api/v1/drive/oauth/callback"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // GET callback is public and evaluated by DriveController (missing params -> 400 failure text)
        mockMvc.perform(get("/api/v1/drive/oauth/callback"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Google Drive connection could not be completed. Start again from PPV."))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    @DisplayName("Invalid slot numbers (0, 3) return 400 DRIVE_INVALID_SLOT")
    void invalidSlotNumbersReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/drive/connections/0/authorize")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DRIVE_INVALID_SLOT"));

        mockMvc.perform(post("/api/v1/drive/connections/3/authorize")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DRIVE_INVALID_SLOT"));

        mockMvc.perform(delete("/api/v1/drive/connections/0")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DRIVE_INVALID_SLOT"));
    }

    @Test
    @DisplayName("Full lifecycle: Connect U to slot 1, V to slot 2, list connections, and disconnect slot 1")
    void fullConnectAndDisconnectLifecycle() throws Exception {
        // 1. Authorize slot 1
        MvcResult authResult1 = mockMvc.perform(post("/api/v1/drive/connections/1/authorize")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.data.slot").value(1))
                .andExpect(jsonPath("$.data.authorizationUrl").isString())
                .andExpect(jsonPath("$.data.expiresAt").isString())
                .andReturn();

        Cookie bindingCookie1 = authResult1.getResponse().getCookie("PPV_DRIVE_BINDING_1");
        assertThat(bindingCookie1).isNotNull();
        assertThat(bindingCookie1.isHttpOnly()).isTrue();
        assertThat(bindingCookie1.getPath()).isEqualTo("/api/v1/drive/oauth/callback");

        String authUrl1 = objectMapper.readTree(authResult1.getResponse().getContentAsString())
                .path("data").path("authorizationUrl").asText();
        String state1 = authUrl1.split("state=")[1].split("&")[0];
        fakeServer.currentNonce = authUrl1.split("nonce=")[1].split("&")[0];

        // 2. Callback slot 1 with user U
        fakeServer.forcedSubject = "user-U-subject";
        fakeServer.forcedEmail = "userU@example.com";

        mockMvc.perform(get("/api/v1/drive/oauth/callback")
                        .param("state", state1)
                        .param("code", "code-u1")
                        .cookie(bindingCookie1))
                .andExpect(status().isOk())
                .andExpect(content().string("Google Drive connection completed. Return to PPV."))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));

        // 3. Authorize slot 2
        MvcResult authResult2 = mockMvc.perform(post("/api/v1/drive/connections/2/authorize")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slot").value(2))
                .andReturn();

        Cookie bindingCookie2 = authResult2.getResponse().getCookie("PPV_DRIVE_BINDING_2");
        assertThat(bindingCookie2).isNotNull();

        String authUrl2 = objectMapper.readTree(authResult2.getResponse().getContentAsString())
                .path("data").path("authorizationUrl").asText();
        String state2 = authUrl2.split("state=")[1].split("&")[0];
        fakeServer.currentNonce = authUrl2.split("nonce=")[1].split("&")[0];

        // 4. Callback slot 2 with user V
        fakeServer.forcedSubject = "user-V-subject";
        fakeServer.forcedEmail = "userV@example.com";

        mockMvc.perform(get("/api/v1/drive/oauth/callback")
                        .param("state", state2)
                        .param("code", "code-v2")
                        .cookie(bindingCookie2))
                .andExpect(status().isOk())
                .andExpect(content().string("Google Drive connection completed. Return to PPV."));

        // 5. List connections: both slots CONNECTED with display emails and no secrets
        MvcResult listResult = mockMvc.perform(get("/api/v1/drive/connections")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.data.connections.length()").value(2))
                .andExpect(jsonPath("$.data.connections[0].slot").value(1))
                .andExpect(jsonPath("$.data.connections[0].status").value("CONNECTED"))
                .andExpect(jsonPath("$.data.connections[0].displayEmail").value("userU@example.com"))
                .andExpect(jsonPath("$.data.connections[1].slot").value(2))
                .andExpect(jsonPath("$.data.connections[1].status").value("CONNECTED"))
                .andExpect(jsonPath("$.data.connections[1].displayEmail").value("userV@example.com"))
                .andReturn();

        String listContent = listResult.getResponse().getContentAsString();
        assertThat(listContent).doesNotContain("refresh_token")
                .doesNotContain("access_token")
                .doesNotContain("ciphertext")
                .doesNotContain("user-U-subject")
                .doesNotContain("user-V-subject");

        // 6. Disconnect slot 1
        mockMvc.perform(delete("/api/v1/drive/connections/1")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.data.slot").value(1))
                .andExpect(jsonPath("$.data.status").value("DISCONNECTED"))
                .andExpect(jsonPath("$.data.remoteRevocation").value("REQUEST_ACCEPTED"));

        // 7. Verify slot 1 is DISCONNECTED and slot 2 remains CONNECTED
        mockMvc.perform(get("/api/v1/drive/connections")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.connections[0].slot").value(1))
                .andExpect(jsonPath("$.data.connections[0].status").value("DISCONNECTED"))
                .andExpect(jsonPath("$.data.connections[0].displayEmail").isEmpty())
                .andExpect(jsonPath("$.data.connections[0].remoteRevocation").value("REQUEST_ACCEPTED"))
                .andExpect(jsonPath("$.data.connections[1].slot").value(2))
                .andExpect(jsonPath("$.data.connections[1].status").value("CONNECTED"));
    }
    private record BrowserConsent(String state, String code, Cookie cookie) {
        @Override public String toString() { return "BrowserConsent[REDACTED]"; }
    }

    private BrowserConsent browserStart(int slot, String sub) throws Exception {
        var result = mockMvc.perform(post("/api/v1/drive/connections/" + slot + "/authorize")
                .header("Authorization", bearerHeader())).andExpect(status().isOk()).andReturn();
        String url = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("authorizationUrl").asText();
        String code = java.util.UUID.randomUUID().toString();
        fakeServer.issueCode(code, sub, url.split("nonce=")[1].split("&")[0], "refresh-" + code);
        return new BrowserConsent(url.split("state=")[1].split("&")[0], code,
                result.getResponse().getCookie("PPV_DRIVE_BINDING_" + slot));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans={false, true})
    void simultaneousSlotCookiesWorkInBothOrders(boolean reverse) throws Exception {
        var one = browserStart(1, "U");
        var two = browserStart(2, "V");
        Cookie[] cookies = reverse ? new Cookie[]{two.cookie(), one.cookie()} : new Cookie[]{one.cookie(), two.cookie()};
        for (var consent : java.util.List.of(two, one)) {
            mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", consent.state()).param("code", consent.code()).cookie(cookies))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.startsWith(consent.cookie().getName() + "=")))
                    .andExpect(content().string("Google Drive connection completed. Return to PPV."));
        }
        assertThat(fakeServer.tokenExchangeCount).hasValue(2);
        assertThat(fakeServer.refreshTokenCount).hasValue(2);
    }

    @Test void unknownReplacedAndWrongProofCallbacksDoNotEraseCurrentCookie() throws Exception {
        var old = browserStart(1, "U"); var current = browserStart(1, "V");
        for (String state : java.util.List.of(old.state(), "unknown-state", current.state())) {
            Cookie cookie = state.equals(current.state()) ? new Cookie(current.cookie().getName(), "wrong-proof") : current.cookie();
            mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", state).param("code", old.code()).cookie(cookie))
                    .andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        }
        assertThat(fakeServer.tokenExchangeCount).hasValue(0);
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", current.state()).param("code", current.code()).cookie(current.cookie()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", current.state()).param("code", current.code()).cookie(current.cookie()))
                .andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        assertThat(fakeServer.tokenExchangeCount).hasValue(1);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"state", "code", "error", "error_description", "iss", "code-plus-error"})
    void malformedSecurityParametersNeverExchangeOrClearCookie(String duplicate) throws Exception {
        var c = browserStart(1, "U");
        var request = get("/api/v1/drive/oauth/callback").param("state", c.state()).param("code", c.code()).cookie(c.cookie());
        if (duplicate.equals("code-plus-error")) request.param("error", "access_denied");
        else if (duplicate.equals("state")) request.param("state", "other-state");
        else if (duplicate.equals("code")) request.param("code", "other-code");
        else request.param(duplicate, "one", "two");
        mockMvc.perform(request).andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        assertThat(fakeServer.tokenExchangeCount).hasValue(0);
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", c.state()).param("code", c.code()).cookie(c.cookie()))
                .andExpect(status().isOk());
    }

    @Test void guardedDenialConsumesAttemptAndClearsOnlyItsCookieWithoutReflectingProviderText() throws Exception {
        var c = browserStart(2, "U");
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", c.state()).param("error", "access_denied")
                        .param("error_description", "SYNTHETIC_SECRET_MARKER").cookie(c.cookie()))
                .andExpect(status().isBadRequest()).andExpect(content().string("Google Drive connection could not be completed. Start again from PPV."))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.startsWith("PPV_DRIVE_BINDING_2=")));
        assertThat(fakeServer.tokenExchangeCount).hasValue(0);
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", c.state()).param("code", c.code()).cookie(c.cookie()))
                .andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test void overlongStateAndCodeAreRejectedBeforeExchange() throws Exception {
        var c = browserStart(1, "U");
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", "x".repeat(129)).param("code", c.code()).cookie(c.cookie()))
                .andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        mockMvc.perform(get("/api/v1/drive/oauth/callback").param("state", c.state()).param("code", "x".repeat(4097)).cookie(c.cookie()))
                .andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        assertThat(fakeServer.tokenExchangeCount).hasValue(0);
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
