package com.vhvkhangg.personalprivatevault.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileSessionIntegrationTest extends AbstractWebIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private JwtDecoder jwtDecoder;

    private static final String DEFAULT_PRIVATE_PASS = "master-vault-password-1234";
    private static final String DEFAULT_PUBLIC_PASS = "public-profile-password-5678";

    private org.springframework.security.oauth2.jwt.Jwt validLineage;

    private String createCustomToken(java.util.function.Consumer<org.springframework.security.oauth2.jwt.JwtClaimsSet.Builder> customizer) throws Exception {
        if (validLineage == null) validLineage = jwtDecoder.decode(loginPrivate());
        java.time.Instant now = java.time.Instant.now();
        var builder = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer(validLineage.getClaimAsString("iss"))
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .id(UUID.randomUUID().toString())
                .claim("profile", "PRIVATE")
                .claim("sid", validLineage.getClaimAsString("sid"))
                .claim("generation", 1L);
        customizer.accept(builder);
        var claims = new java.util.HashMap<>(builder.build().getClaims());
        claims.replaceAll((k, v) -> v instanceof java.time.Instant instant ? instant.getEpochSecond() : v);
        var signed = new com.nimbusds.jose.JWSObject(new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256),
                new com.nimbusds.jose.Payload(objectMapper.writeValueAsString(claims)));
        signed.sign(new com.nimbusds.jose.crypto.MACSigner(signingKey));
        return signed.serialize();
    }

    @org.springframework.beans.factory.annotation.Autowired private javax.crypto.SecretKey signingKey;

    private void bootstrapVault() throws Exception {
        String bootstrapPayload = """
                {
                  "email": "owner@vault.local",
                  "username": "owner",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bootstrapPayload))
                .andExpect(status().isCreated());
    }

    private String loginPrivate() throws Exception {
        String payload = """
                {
                  "identifier": "owner",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String resp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resp);
        return json.path("data").path("accessToken").asText();
    }

    private String securityValues() {
        return jdbcTemplate.queryForObject("""
                SELECT jsonb_build_object(
                 'credentials',(SELECT jsonb_agg(to_jsonb(c)) FROM ppv_public_login_credential c),
                 'sessions',(SELECT jsonb_agg(to_jsonb(s) ORDER BY id) FROM ppv_login_sessions s),
                 'refresh',(SELECT jsonb_agg(to_jsonb(r) ORDER BY id) FROM refresh_tokens r))::text
                """, String.class);
    }

    @org.springframework.beans.factory.annotation.Autowired private com.vhvkhangg.personalprivatevault.authentication.privatepin.PrivatePinOperations privatePinOperations;

    @Test
    void genuineProfilesPreservePrivatePinBooleanFormatProofAndServiceBoundary() throws Exception {
        bootstrapVault();
        String privateToken = loginPrivate();
        var jwt = jwtDecoder.decode(privateToken);
        profileSessionService.configureOrResetPublicCredential(UUID.fromString(jwt.getClaimAsString("sid")), 1, DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);
        String publicToken = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASS).accessToken();
        mockMvc.perform(post("/api/v1/auth/private-pin/verify").header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"654321\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.verified").value(false));
        mockMvc.perform(post("/api/v1/auth/private-pin/verify").header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"bad\"}"))
                .andExpect(status().isUnprocessableEntity());
        var pinBefore = jdbcTemplate.queryForObject("SELECT pin_hash FROM app_users", String.class);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/auth/private-pin")
                        .header("Authorization", "Bearer " + privateToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\":\"000000\",\"newPin\":\"654321\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/private-pin/verify").header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"pin\":\"123456\"}"))
                .andExpect(status().isForbidden());
        assertThat(jdbcTemplate.queryForObject("SELECT pin_hash FROM app_users", String.class)).isEqualTo(pinBefore);
        try {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> privatePinOperations.verifyPin("123456"))
                    .isInstanceOf(com.vhvkhangg.personalprivatevault.authentication.privatepin.UnauthenticatedAccessException.class);
            for (String token : java.util.List.of(publicToken, privateToken)) {
                var decoded = jwtDecoder.decode(token);
                var auth = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(decoded,
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("PROFILE_" + decoded.getClaimAsString("profile"))), "1");
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                if (token.equals(publicToken)) org.assertj.core.api.Assertions.assertThatThrownBy(() -> privatePinOperations.verifyPin("123456"))
                        .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
                else assertThat(privatePinOperations.verifyPin("123456")).isTrue();
            }
        } finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }

    @Test
    void negativeTransitionsPreserveAllAuthorityAndRedactNewProofs() throws Exception {
        bootstrapVault();
        String privateToken = loginPrivate();
        String before = securityValues();
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public").header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("AUTH_PUBLIC_NOT_CONFIGURED"));
        for (String proof : java.util.List.of("wrong-private-proof", DEFAULT_PRIVATE_PASS)) {
            mockMvc.perform(post("/api/v1/auth/profiles/public-credential").header("Authorization", "Bearer " + privateToken)
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of(
                                    "privatePassword", proof, "publicPassword", DEFAULT_PRIVATE_PASS))))
                    .andExpect(status().is(proof.equals(DEFAULT_PRIVATE_PASS) ? 422 : 401))
                    .andExpect(r -> assertThat(r.getResponse().getContentAsString()).doesNotContain(proof, DEFAULT_PUBLIC_PASS));
            assertThat(securityValues()).isEqualTo(before);
        }
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential").header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of(
                                "privatePassword", DEFAULT_PRIVATE_PASS, "publicPassword", DEFAULT_PUBLIC_PASS))))
                .andExpect(status().isOk());
        String publicToken = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASS).accessToken();
        before = securityValues();
        for (String body : java.util.List.of("{\"privatePassword\":\"123456\"}", "{\"privatePassword\":\"wrong-proof\"}",
                "{\"privatePassword\":\"" + DEFAULT_PRIVATE_PASS + "\",\"profile\":\"PRIVATE\"}")) {
            mockMvc.perform(post("/api/v1/auth/sessions/elevate-private").header("Authorization", "Bearer " + publicToken)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().is(body.contains("profile") ? 400 : 401))
                    .andExpect(r -> assertThat(r.getResponse().getContentAsString()).doesNotContain("123456", DEFAULT_PRIVATE_PASS, "wrong-proof"));
            assertThat(securityValues()).isEqualTo(before);
        }
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public").header("Authorization", "Bearer " + publicToken)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private").header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        assertThat(securityValues()).isEqualTo(before);
        assertThat(new com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.PublicCredentialRequest(DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS).toString())
                .doesNotContain(DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);
        assertThat(new com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.ElevatePrivateRequest(DEFAULT_PRIVATE_PASS).toString()).doesNotContain(DEFAULT_PRIVATE_PASS);
        assertThat(new com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.PublicProfileLoginRequest("PUBLIC", DEFAULT_PUBLIC_PASS).toString()).doesNotContain(DEFAULT_PUBLIC_PASS);
    }

    @Test
    void resetInvalidatesCallerAndAllEverPublicDevicesWhilePrivateOnlySurvives() throws Exception {
        bootstrapVault();
        String privateOnly = loginPrivate();
        var privateJwt = jwtDecoder.decode(privateOnly);
        profileSessionService.configureOrResetPublicCredential(UUID.fromString(privateJwt.getClaimAsString("sid")), 1, DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);
        var deviceA = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASS);
        var deviceB = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASS);
        var deviceC = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASS);
        var jwtA = jwtDecoder.decode(deviceA.accessToken());
        var jwtC = jwtDecoder.decode(deviceC.accessToken());
        var caller = profileSessionService.elevatePrivate(UUID.fromString(jwtA.getClaimAsString("sid")), 1, DEFAULT_PRIVATE_PASS);
        var elevatedDevice = profileSessionService.elevatePrivate(UUID.fromString(jwtC.getClaimAsString("sid")), 1, DEFAULT_PRIVATE_PASS);
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential").header("Authorization", "Bearer " + caller.accessToken())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("privatePassword", DEFAULT_PRIVATE_PASS, "publicPassword", "new-public-secret-1234"))))
                .andExpect(status().isOk());
        for (var pair : java.util.List.of(caller, deviceB, elevatedDevice)) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.accessToken()))
                    .andExpect(status().isUnauthorized());
            mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("refreshToken", pair.refreshToken()))))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries").header("Authorization", "Bearer " + privateOnly))
                .andExpect(status().isOk());
        var independent = loginPrivate();
        var down = profileSessionService.downgradePublic(UUID.fromString(jwtDecoder.decode(independent).getClaimAsString("sid")), 1);
        sessionOperations.revoke(down.refreshToken());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries").header("Authorization", "Bearer " + privateOnly))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DPA-02: Public login unconfigured yields 401 AUTH_INVALID_CREDENTIALS")
    void publicLoginUnconfiguredYields401() throws Exception {
        bootstrapVault();

        String payload = """
                {
                  "profile": "PUBLIC",
                  "password": "any-password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("DPA-02: Public login with extra identifier or invalid body yields 400 VALIDATION_ERROR")
    void publicLoginWithIdentifierYields400() throws Exception {
        bootstrapVault();

        // Extra identifier field rejected
        String payloadWithIdentifier = """
                {
                  "profile": "PUBLIC",
                  "password": "valid-length-password-1234",
                  "identifier": "owner"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithIdentifier))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Wrong profile name rejected
        String payloadWrongProfile = """
                {
                  "profile": "PRIVATE",
                  "password": "valid-length-password-1234"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWrongProfile))
                .andExpect(status().isBadRequest());

        // Explicit null profile rejected (FR-DPA-03)
        String payloadNullProfile = """
                {
                  "profile": null,
                  "password": "valid-length-password-1234"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNullProfile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Missing profile field rejected
        String payloadMissingProfile = """
                {
                  "password": "valid-length-password-1234"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadMissingProfile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Numeric password rejected with 400 VALIDATION_ERROR (not 401 credentials failure)
        String payloadNumericPassword = """
                {
                  "profile": "PUBLIC",
                  "password": 1234567890123
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNumericPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Boolean password rejected with 400 VALIDATION_ERROR
        String payloadBooleanPassword = """
                {
                  "profile": "PUBLIC",
                  "password": true
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBooleanPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Null password rejected with 400 VALIDATION_ERROR
        String payloadNullPassword = """
                {
                  "profile": "PUBLIC",
                  "password": null
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNullPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Numeric profile rejected with 400 VALIDATION_ERROR
        String payloadNumericProfile = """
                {
                  "profile": 123,
                  "password": "valid-length-password-1234"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadNumericProfile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Duplicate JSON keys rejected with 400 VALIDATION_ERROR
        String payloadDuplicateKeys = "{\"profile\":\"PUBLIC\",\"profile\":\"PUBLIC\",\"password\":\"valid-length-password-1234\"}";
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadDuplicateKeys))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("DPA-04: Malformed JWT claims (fractional generation, missing exp, 2^63 overflow, invalid types) rejected with 401 at admission")
    void malformedJwtClaimsRejectedAtAdmissionWith401() throws Exception {
        bootstrapVault();

        // All negatives below change an otherwise admissible, genuinely issued persisted lineage.
        String valid = createCustomToken(b -> { });
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/unknown-route")
                        .header("Authorization", "Bearer " + valid)).andExpect(status().isNotFound());

        java.util.List<java.util.function.Consumer<org.springframework.security.oauth2.jwt.JwtClaimsSet.Builder>> mismatches = java.util.List.of(
                b -> b.claim("generation", 2L), b -> b.claim("generation", "1"), b -> b.claim("generation", true),
                b -> b.claim("profile", "PUBLIC"), b -> b.claim("sid", UUID.randomUUID().toString()),
                b -> b.claims(c -> c.remove("jti")), b -> b.claims(c -> c.remove("sid")),
                b -> b.claims(c -> c.remove("profile")), b -> b.claims(c -> c.remove("generation")),
                b -> b.issuer("incorrect-issuer"), b -> b.expiresAt(java.time.Instant.now().minusSeconds(120)));
        for (var mismatch : mismatches) mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + createCustomToken(mismatch)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        String[] parts = valid.split("\\.");
        parts[2] = (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + String.join(".", parts))).andExpect(status().isUnauthorized());

        // 1. Fractional generation (1.5) must be rejected with 401 (not 404 on unknown route) (FR-DPA-04)
        String fracToken = createCustomToken(b -> b.claim("generation", 1.5));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/unknown-route")
                        .header("Authorization", "Bearer " + fracToken))
                .andExpect(status().isUnauthorized());

        // 1b. FR-DPA-04: Precise fractional generation (1.0000000000000001) rejected with 401 at admission
        String preciseFracToken = createCustomToken(b -> b.claim("generation", new java.math.BigDecimal("1.0000000000000001")));
        assertThat(new String(java.util.Base64.getUrlDecoder().decode(preciseFracToken.split("\\.")[1]), java.nio.charset.StandardCharsets.UTF_8))
                .contains("\"generation\":1.0000000000000001");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/unknown-route")
                        .header("Authorization", "Bearer " + preciseFracToken))
                .andExpect(status().isUnauthorized());

        // 1c. FR-DPA-04: Floating-point integer representation (1.0) rejected with 401 at admission
        String floatOneToken = createCustomToken(b -> b.claim("generation", 1.0));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/unknown-route")
                        .header("Authorization", "Bearer " + floatOneToken))
                .andExpect(status().isUnauthorized());

        // 2. FR-DPA-04: 2^63 overflow generation (9223372036854775808.0) rejected with 401 at admission
        String overflowDoubleToken = createCustomToken(b -> b.claim("generation", 9223372036854775808.0));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + overflowDoubleToken))
                .andExpect(status().isUnauthorized());

        // 3. FR-DPA-04: Negative generation (-1) rejected with 401
        String negativeGenToken = createCustomToken(b -> b.claim("generation", -1L));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + negativeGenToken))
                .andExpect(status().isUnauthorized());

        // 4. Missing exp claim must be rejected with 401 (not 404 on unknown route) (FR-DPA-04)
        String noExpToken = createCustomToken(b -> b.claims(claims -> claims.remove("exp")));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/unknown-route")
                        .header("Authorization", "Bearer " + noExpToken))
                .andExpect(status().isUnauthorized());

        // 5. Non-positive generation (0)
        String zeroGenToken = createCustomToken(b -> b.claim("generation", 0L));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + zeroGenToken))
                .andExpect(status().isUnauthorized());

        // 6. Malformed sid (not a UUID)
        String badSidToken = createCustomToken(b -> b.claim("sid", "not-a-uuid"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + badSidToken))
                .andExpect(status().isUnauthorized());

        // 7. Invalid profile claim
        String badProfileToken = createCustomToken(b -> b.claim("profile", "INVALID_PROFILE"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + badProfileToken))
                .andExpect(status().isUnauthorized());

        // 8. Non-owner subject ("2")
        String nonOwnerToken = createCustomToken(b -> b.subject("2"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + nonOwnerToken))
                .andExpect(status().isUnauthorized());

        var statePair = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand("owner", DEFAULT_PRIVATE_PASS));
        sessionOperations.revoke(statePair.refreshToken());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + statePair.accessToken())).andExpect(status().isUnauthorized());
        var expiredPair = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand("owner", DEFAULT_PRIVATE_PASS));
        jdbcTemplate.update("UPDATE ppv_login_sessions SET created_at=now()-interval '20 seconds', expires_at=now()-interval '10 seconds' WHERE id=?",
                UUID.fromString(jwtDecoder.decode(expiredPair.accessToken()).getClaimAsString("sid")));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + expiredPair.accessToken())).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DPA-03: Setup public credential requires PRIVATE bearer and distinct password")
    void setupPublicCredentialLifecycle() throws Exception {
        bootstrapVault();
        String privateToken = loginPrivate();

        // 1. Setup with non-distinct password yields 422
        String identicalPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS, DEFAULT_PRIVATE_PASS);

        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(identicalPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("AUTH_PASSWORD_NOT_DISTINCT"));

        // 2. Setup with wrong private password yields 401
        String wrongPrivatePayload = """
                {
                  "privatePassword": "wrong-private-pass",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PUBLIC_PASS);

        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPrivatePayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));

        // FR-DPA-03: Non-string privatePassword rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":12345,\"publicPassword\":\"" + DEFAULT_PUBLIC_PASS + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Non-string publicPassword rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":\"" + DEFAULT_PRIVATE_PASS + "\",\"publicPassword\":12345}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Null privatePassword rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":null,\"publicPassword\":\"" + DEFAULT_PUBLIC_PASS + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Null publicPassword rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":\"" + DEFAULT_PRIVATE_PASS + "\",\"publicPassword\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 3. Setup with correct proof succeeds 200
        String validPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);

        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + privateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isOk());

        // Epoch is now 1 in database
        Long epoch = jdbcTemplate.queryForObject(
                "SELECT credential_epoch FROM ppv_public_login_credential WHERE owner_id = 1", Long.class);
        assertThat(epoch).isEqualTo(1L);
    }

    @Test
    @DisplayName("DPA-02, DPA-05, DPA-06: Configured public login, elevation, downgrade, and rolling TTL")
    void publicLoginElevationAndDowngradeFlow() throws Exception {
        bootstrapVault();
        String initialPrivateToken = loginPrivate();

        // Configure public credential
        String setupPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + initialPrivateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setupPayload))
                .andExpect(status().isOk());

        // 1. Public login succeeds -> returns PUBLIC token
        String publicLoginPayload = """
                {
                  "profile": "PUBLIC",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PUBLIC_PASS);

        String publicResp = mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicLoginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andReturn().getResponse().getContentAsString();

        JsonNode publicJson = objectMapper.readTree(publicResp);
        String publicToken = publicJson.path("data").path("accessToken").asText();

        var jwt = jwtDecoder.decode(publicToken);
        assertThat(jwt.getClaimAsString("profile")).isEqualTo("PUBLIC");
        assertThat(jwt.getClaimAsString("sub")).isEqualTo("1");
        String sidStr = jwt.getClaimAsString("sid");
        UUID sessionId = UUID.fromString(sidStr);
        Long generation = jwt.getClaim("generation");
        assertThat(generation).isEqualTo(1L);

        // 2. Elevate to PRIVATE requires valid private password
        // Wrong password -> 401
        String wrongElevate = """
                {
                  "privatePassword": "wrong-password"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongElevate))
                .andExpect(status().isUnauthorized());

        // FR-DPA-03: Non-string privatePassword rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":12345}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Null privatePassword rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // FR-DPA-03: Unknown member rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":\"" + DEFAULT_PRIVATE_PASS + "\",\"unknownField\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Correct password -> 200 replacement PRIVATE pair in same lineage, generation 2
        String correctElevate = """
                {
                  "privatePassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);

        String elevateResp = mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctElevate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn().getResponse().getContentAsString();

        JsonNode elevateJson = objectMapper.readTree(elevateResp);
        String elevatedToken = elevateJson.path("data").path("accessToken").asText();
        var elevatedJwt = jwtDecoder.decode(elevatedToken);
        assertThat(elevatedJwt.getClaimAsString("profile")).isEqualTo("PRIVATE");
        assertThat(elevatedJwt.getClaimAsString("sid")).isEqualTo(sidStr);
        assertThat((Long) elevatedJwt.getClaim("generation")).isEqualTo(2L);

        // Old PUBLIC token is immediately rejected because generation mismatch
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctElevate))
                .andExpect(status().isUnauthorized());

        // Calling elevate from PRIVATE profile yields 403 ACCESS_DENIED
        mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + elevatedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctElevate))
                .andExpect(status().isForbidden());

        // 3. Downgrade to PUBLIC from PRIVATE succeeds with no body
        String downgradeResp = mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + elevatedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn().getResponse().getContentAsString();

        JsonNode downgradeJson = objectMapper.readTree(downgradeResp);
        String downgradedToken = downgradeJson.path("data").path("accessToken").asText();
        var downgradedJwt = jwtDecoder.decode(downgradedToken);
        assertThat(downgradedJwt.getClaimAsString("profile")).isEqualTo("PUBLIC");
        assertThat(downgradedJwt.getClaimAsString("sid")).isEqualTo(sidStr);
        assertThat((Long) downgradedJwt.getClaim("generation")).isEqualTo(3L);

        // Calling downgrade from PUBLIC yields 403 ACCESS_DENIED
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + downgradedToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DPA-05: Downgrade when public credential unconfigured returns 409 AUTH_PUBLIC_NOT_CONFIGURED")
    void downgradeUnconfiguredYields409() throws Exception {
        bootstrapVault();
        String privateToken = loginPrivate();

        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("AUTH_PUBLIC_NOT_CONFIGURED"));
    }

    @Test
    @DisplayName("DPA-07: Credential reset invalidates ever-PUBLIC sessions while pure PRIVATE survives")
    void credentialResetInvalidatesEverPublicSessions() throws Exception {
        bootstrapVault();
        String initialPrivateToken = loginPrivate();

        // 1. Configure public credential (epoch = 1)
        String setupPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS, DEFAULT_PUBLIC_PASS);
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + initialPrivateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setupPayload))
                .andExpect(status().isOk());

        // 2. Create session A: pure PRIVATE login
        String sessionAPrivateToken = loginPrivate();

        // 3. Create session B: PUBLIC login
        String publicLoginPayload = """
                {
                  "profile": "PUBLIC",
                  "password": "%s"
                }
                """.formatted(DEFAULT_PUBLIC_PASS);

        String sessionBResp = mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicLoginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionBPublicToken = objectMapper.readTree(sessionBResp).path("data").path("accessToken").asText();

        // 4. Elevate session B to PRIVATE
        String elevateBPayload = """
                {
                  "privatePassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS);
        String sessionBElevatedResp = mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + sessionBPublicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(elevateBPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String sessionBElevatedToken = objectMapper.readTree(sessionBElevatedResp).path("data").path("accessToken").asText();

        // 5. Reset public credential using Session A (epoch moves from 1 to 2)
        String newPublicPass = "new-public-password-9999";
        String resetPayload = """
                {
                  "privatePassword": "%s",
                  "publicPassword": "%s"
                }
                """.formatted(DEFAULT_PRIVATE_PASS, newPublicPass);
        mockMvc.perform(post("/api/v1/auth/profiles/public-credential")
                        .header("Authorization", "Bearer " + sessionAPrivateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetPayload))
                .andExpect(status().isOk());

        // 6. Verify Session B (which had ever-PUBLIC epoch=1) is now rejected (401)
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + sessionBElevatedToken))
                .andExpect(status().isUnauthorized());

        // 7. Pure PRIVATE session A remains valid (epoch is null)
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + sessionAPrivateToken))
                .andExpect(status().isOk());
    }
}
