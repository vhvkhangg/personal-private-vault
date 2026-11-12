package com.vhvkhangg.personalprivatevault.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.fixture.FakeGoogleSignInServer;
import com.vhvkhangg.personalprivatevault.authentication.fixture.GoogleSignInTestSupport;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInResources;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.controller.GoogleSignInController;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Import(GoogleSignInTestSupport.GoogleSignInTestConfig.class)
class GoogleSignInWebIntegrationTest extends AbstractWebIntegrationTest {

    @DynamicPropertySource
    static void configureGoogleProps(DynamicPropertyRegistry registry) {
        GoogleSignInTestSupport.configureGoogleSignInProperties(registry);
    }

    @Autowired(required = false)
    private BootstrapOperations bootstrapOperations;

    @Autowired(required = false)
    private GoogleSignInResources googleSignInResources;

    private static FakeGoogleSignInServer fakeGoogle() {
        return GoogleSignInTestSupport.fakeGoogle();
    }

    private void ensureOwnerBootstrapped() {
        if (bootstrapOperations != null && !bootstrapOperations.isBootstrapped()) {
            bootstrapOperations.bootstrap(new BootstrapCommand(
                    GoogleSignInTestSupport.DEFAULT_EMAIL,
                    GoogleSignInTestSupport.DEFAULT_USERNAME,
                    GoogleSignInTestSupport.DEFAULT_RAW_PASSWORD,
                    GoogleSignInTestSupport.DEFAULT_PIN
            ));
        }
    }

    private void seedOwnerGoogleIdentity(String issuer, String sub) {
        ensureOwnerBootstrapped();
        jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, ?, ?, NOW())
                ON CONFLICT (owner_id) DO NOTHING
                """, issuer, sub);
    }

    private static String generate43CharToken() {
        return GoogleSignInTestSupport.generate43CharToken();
    }

    private static String sha256Hex(String input) {
        return GoogleSignInTestSupport.sha256Hex(input);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    @BeforeEach
    void setUp() {
        this.mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .defaultRequest(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/").header("Host", "localhost:8080"))
                .build();
        if (googleSignInResources != null) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) org.springframework.test.util.ReflectionTestUtils.getField(googleSignInResources, "routeRateLimits");
            if (map != null) {
                map.clear();
            }
        }
        cleanupGoogleTables();
        ensureOwnerBootstrapped();
    }

    @AfterEach
    void tearDown() {
        cleanupGoogleTables();
    }

    private void cleanupGoogleTables() {
        if (jdbcTemplate != null) {
            jdbcTemplate.execute("TRUNCATE TABLE ppv_google_handoff_tickets, ppv_google_login_attempts, ppv_owner_google_identities CASCADE");
        }
    }

    @Test
    @DisplayName("D4: POST /api/v1/auth/google/start returns 503 GOOGLE_SIGNIN_UNAVAILABLE before attempt creation")
    void startGoogleSignInReturns503() throws Exception {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-web-owner");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        String payload = mapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("handoffChallenge", challenge);
        }});

        mockMvc.perform(post("/api/v1/auth/google/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("GOOGLE_SIGNIN_UNAVAILABLE"));

        Integer attempts = jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_google_login_attempts", Integer.class);
        assertThat(attempts).isZero();
    }

    @Test
    @DisplayName("A-01: POST /api/v1/auth/google/link/start requires authentication; anonymous returns 401")
    void linkStartRequiresAuthentication() throws Exception {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        String payload = mapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("handoffChallenge", challenge);
            put("password", GoogleSignInTestSupport.DEFAULT_RAW_PASSWORD);
        }});

        mockMvc.perform(post("/api/v1/auth/google/link/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("A-02: POST /api/v1/auth/google/link/start with valid owner bearer and correct password succeeds")
    void linkStartWithBearerAndCorrectPasswordSucceeds() throws Exception {
        String token = bearerToken();
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);

        String payload = mapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("handoffChallenge", challenge);
            put("password", GoogleSignInTestSupport.DEFAULT_RAW_PASSWORD);
        }});

        mockMvc.perform(post("/api/v1/auth/google/link/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.data.authorizationUrl").isString());
    }

    @Test
    @DisplayName("D4: LINK start and callback redirect 303 to BFF completion with handoff cookie; redemption fails 503")
    void callbackRedirects303WithHandoffCookie() throws Exception {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-web-owner");

        // 1. Start link
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);
        String startPayload = mapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("handoffChallenge", challenge);
            put("password", GoogleSignInTestSupport.DEFAULT_RAW_PASSWORD);
        }});

        var startResp = mockMvc.perform(post("/api/v1/auth/google/link/start")
                        .header("Authorization", "Bearer " + bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startPayload))
                .andExpect(status().isOk())
                .andReturn();

        Cookie bindCookie = startResp.getResponse().getCookie(GoogleSignInController.BIND_COOKIE_NAME);
        assertThat(bindCookie).isNotNull();

        JsonNode startJson = mapper.readTree(startResp.getResponse().getContentAsString());
        String authUrl = startJson.path("data").path("authorizationUrl").asText();
        String state = extractQueryParam(authUrl, "state");
        String nonce = extractQueryParam(authUrl, "nonce");

        // 2. Register code in fake Google
        String code = "code-web-callback";
        fakeGoogle().forcedSubject = "sub-web-owner";
        fakeGoogle().registerCode(code, nonce);

        // 3. Perform callback
        var callbackResp = mockMvc.perform(get("/api/v1/auth/google/callback")
                        .param("code", code)
                        .param("state", state)
                        .cookie(bindCookie))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", GoogleSignInController.BFF_COMPLETE_URL))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();

        // 4. Assert cookies: bind cleared (maxAge=0), handoff set (maxAge=60)
        Cookie clearedBind = callbackResp.getResponse().getCookie(GoogleSignInController.BIND_COOKIE_NAME);
        assertThat(clearedBind).isNotNull();
        assertThat(clearedBind.getMaxAge()).isZero();

        Cookie handoffCookie = callbackResp.getResponse().getCookie(GoogleSignInController.HANDOFF_COOKIE_NAME);
        assertThat(handoffCookie).isNotNull();
        assertThat(handoffCookie.getValue()).isNotBlank();
        assertThat(handoffCookie.getMaxAge()).isEqualTo(60);
        assertThat(handoffCookie.getPath()).isEqualTo("/api/bff/auth/google/complete");

        // 5. Redeem handoff ticket fails closed 503 before ticket consumption or token issuance
        String handoffPayload = mapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("ticket", handoffCookie.getValue());
            put("handoffVerifier", verifier);
        }});

        mockMvc.perform(post("/api/v1/auth/google/handoff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(handoffPayload))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("GOOGLE_SIGNIN_UNAVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/google/callback on error redirects 303 to BFF completion and clears cookies")
    void callbackOnErrorRedirects303AndClearsCookies() throws Exception {
        var callbackResp = mockMvc.perform(get("/api/v1/auth/google/callback")
                        .param("error", "access_denied"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", GoogleSignInController.BFF_COMPLETE_URL))
                .andReturn();

        Cookie clearedBind = callbackResp.getResponse().getCookie(GoogleSignInController.BIND_COOKIE_NAME);
        assertThat(clearedBind).isNotNull();
        assertThat(clearedBind.getMaxAge()).isZero();

        Cookie clearedHandoff = callbackResp.getResponse().getCookie(GoogleSignInController.HANDOFF_COOKIE_NAME);
        assertThat(clearedHandoff).isNotNull();
        assertThat(clearedHandoff.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("D4: Two concurrent HTTP handoff redemptions fail-closed with 503 GOOGLE_SIGNIN_UNAVAILABLE")
    void concurrentHandoffRedemptionsYield503() throws Exception {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-web-concurrent");

        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);
        String startPayload = mapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("handoffChallenge", challenge);
            put("password", GoogleSignInTestSupport.DEFAULT_RAW_PASSWORD);
        }});

        var startResp = mockMvc.perform(post("/api/v1/auth/google/link/start")
                        .header("Authorization", "Bearer " + bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startPayload))
                .andExpect(status().isOk())
                .andReturn();

        Cookie bindCookie = startResp.getResponse().getCookie(GoogleSignInController.BIND_COOKIE_NAME);
        JsonNode startJson = mapper.readTree(startResp.getResponse().getContentAsString());
        String authUrl = startJson.path("data").path("authorizationUrl").asText();
        String state = extractQueryParam(authUrl, "state");
        String nonce = extractQueryParam(authUrl, "nonce");

        String code = "code-web-concurrent-handoff";
        fakeGoogle().forcedSubject = "sub-web-concurrent";
        fakeGoogle().registerCode(code, nonce);

        var callbackResp = mockMvc.perform(get("/api/v1/auth/google/callback")
                        .param("code", code)
                        .param("state", state)
                        .cookie(bindCookie))
                .andExpect(status().isSeeOther())
                .andReturn();

        Cookie handoffCookie = callbackResp.getResponse().getCookie(GoogleSignInController.HANDOFF_COOKIE_NAME);
        assertThat(handoffCookie).isNotNull();
        String ticket = handoffCookie.getValue();

        String handoffPayload = mapper.writeValueAsString(java.util.Map.of(
                "ticket", ticket,
                "handoffVerifier", verifier
        ));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger unavailableCount = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    fire.await();
                    int status = mockMvc.perform(post("/api/v1/auth/google/handoff")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(handoffPayload))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                    if (status == 503) {
                        unavailableCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            });
        }

        ready.await();
        fire.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(unavailableCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("AUTH-FR-09: Duplicate JSON field rejected with 400 Bad Request")
    void duplicateJsonFieldRejected() throws Exception {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);
        String duplicateJson = "{\"handoffChallenge\":\"" + challenge + "\",\"handoffChallenge\":\"" + challenge + "\"}";

        mockMvc.perform(post("/api/v1/auth/google/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AUTH-FR-09: Request body larger than 4KiB rejected with 400 Bad Request")
    void oversizedRequestBodyRejected() throws Exception {
        String padding = "a".repeat(4097);
        String oversizedJson = "{\"handoffChallenge\":\"" + padding + "\"}";

        mockMvc.perform(post("/api/v1/auth/google/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AUTH-FR-09: Untrusted Host, 127.0.0.1, wrong port, or X-Forwarded-Host rejected with 400 Bad Request")
    void untrustedHostRejected() throws Exception {
        seedOwnerGoogleIdentity("https://accounts.google.com", "sub-host-test");
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier);
        String payload = mapper.writeValueAsString(java.util.Map.of("handoffChallenge", challenge));

        // Bad Host header
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("Host", "attacker.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // 127.0.0.1 is rejected (production pilot requires literal localhost)
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("Host", "127.0.0.1:8080")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // IPv6 loopback ::1 is rejected
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("Host", "::1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // Wrong port 9999 is rejected
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("Host", "localhost:9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // Absent port in Host header: Host: localhost (implied port 80 != 8080) is rejected
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("Host", "localhost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // Malicious X-Forwarded-Host header is rejected
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("X-Forwarded-Host", "evil-proxy.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // Malicious X-Forwarded-Port header is rejected
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("X-Forwarded-Port", "8080")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // Malicious X-Forwarded-Proto header is rejected
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("X-Forwarded-Proto", "https")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // Literal localhost:8080 is accepted by host validator, reaching service which throws 503 under D4
        mockMvc.perform(post("/api/v1/auth/google/start")
                        .header("Host", "localhost:8080")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("AUTH-FR-09: OpenAPI schema documents exact DTO instead of string")
    void openApiSchemaDocumentsExactDto() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/google/start'].post.requestBody.content['application/json'].schema.$ref")
                        .value("#/components/schemas/StartGoogleSignInRequest"));
    }

    @Test
    @DisplayName("AUTH-FR-09: Uppercase hex challenge rejected with 400 Bad Request")
    void uppercaseHexChallengeRejected() throws Exception {
        String verifier = generate43CharToken();
        String challenge = sha256Hex(verifier).toUpperCase();
        String payload = mapper.writeValueAsString(java.util.Map.of("handoffChallenge", challenge));

        mockMvc.perform(post("/api/v1/auth/google/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AUTH-FR-09: Duplicate callback query parameters redirect to failure without provider dispatch")
    void duplicateCallbackQueryParamsRedirectToFailure() throws Exception {
        var callbackResp = mockMvc.perform(get("/api/v1/auth/google/callback")
                        .param("code", "code1")
                        .param("code", "code2")
                        .param("state", "state1"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", GoogleSignInController.BFF_COMPLETE_URL))
                .andReturn();

        // Check that handoff cookie was cleared / not issued
        Cookie handoffCookie = callbackResp.getResponse().getCookie(GoogleSignInController.HANDOFF_COOKIE_NAME);
        assertThat(handoffCookie).isNotNull();
        assertThat(handoffCookie.getMaxAge()).isZero();
    }

    private static String extractQueryParam(String url, String param) {
        for (String pair : url.substring(url.indexOf('?') + 1).split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0 && pair.substring(0, idx).equals(param)) {
                return pair.substring(idx + 1);
            }
        }
        return null;
    }
}
