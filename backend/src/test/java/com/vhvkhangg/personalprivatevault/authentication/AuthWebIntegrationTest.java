package com.vhvkhangg.personalprivatevault.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthWebIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    @DisplayName("GET /api/v1/auth/bootstrap/status permits anonymous access and returns bootstrap status")
    void getBootstrapStatusPermitsAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/auth/bootstrap/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bootstrapped").isBoolean())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.meta").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("Complete auth lifecycle: bootstrap with boundary username/email, login, rotation, replay rejection, PIN operations, and token revocation")
    void completeAuthLifecycleOverHttp() throws Exception {
        // 1. Bootstrap with 2-char username "ab" and email > 255 chars (FR13-3 domain alignment)
        String longEmail = "a".repeat(260) + "@example.com";
        String bootstrapPayload = """
                {
                  "email": "%s",
                  "username": "ab",
                  "password": "master-vault-password-1234",
                  "pin": "123456"
                }
                """.formatted(longEmail);

        String bootstrapResp = mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bootstrapPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("ab"))
                .andExpect(jsonPath("$.data.email").value(longEmail))
                .andReturn().getResponse().getContentAsString();

        JsonNode bootstrapJson = objectMapper.readTree(bootstrapResp);
        assertThat(bootstrapJson.path("data").path("username").asText()).isEqualTo("ab");

        // 2. Second bootstrap attempt fails with 409 Conflict
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bootstrapPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("AUTH_ALREADY_BOOTSTRAPPED"));

        // 3. Login with username "ab" -> 200 OK
        String loginPayloadUsername = """
                {
                  "identifier": "ab",
                  "password": "master-vault-password-1234"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        // 4. Login with long email identifier -> 200 OK
        String loginPayloadEmail = """
                {
                  "identifier": "%s",
                  "password": "master-vault-password-1234"
                }
                """.formatted(longEmail);

        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadEmail))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResp);
        String accessToken = loginJson.path("data").path("accessToken").asText();
        String initialRefreshToken = loginJson.path("data").path("refreshToken").asText();

        // 5. Refresh token rotation -> 200 OK
        String refreshPayload = "{\"refreshToken\": \"" + initialRefreshToken + "\"}";
        String refreshResp = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode refreshJson = objectMapper.readTree(refreshResp);
        String rotatedRefreshToken = refreshJson.path("data").path("refreshToken").asText();
        assertThat(rotatedRefreshToken).isNotEqualTo(initialRefreshToken);

        // 6. Replay attack: using previous refresh token fails with 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));

        // 7. Verify PIN with authenticated bearer -> 200 OK
        mockMvc.perform(post("/api/v1/auth/private-pin/verify")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));

        // 8. Verify PIN with wrong PIN -> 200 OK with verified: false
        mockMvc.perform(post("/api/v1/auth/private-pin/verify")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": \"999999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(false));

        // 9. Change PIN with wrong current PIN -> 401 AUTH_INVALID_PIN
        mockMvc.perform(put("/api/v1/auth/private-pin")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\": \"999999\", \"newPin\": \"654321\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_PIN"));

        // 10. Change PIN with valid format -> 200 OK
        mockMvc.perform(put("/api/v1/auth/private-pin")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\": \"123456\", \"newPin\": \"654321\"}"))
                .andExpect(status().isOk());

        // 10. Verify changed PIN -> 200 OK
        mockMvc.perform(post("/api/v1/auth/private-pin/verify")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": \"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));

        // 11. Revoke refresh token -> 200 OK
        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + rotatedRefreshToken + "\"}"))
                .andExpect(status().isOk());

        // 12. Attempt to use revoked refresh token -> 401 AUTH_INVALID_REFRESH_TOKEN
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + rotatedRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login rejects invalid credentials with 401")
    void loginWithInvalidCredentialsReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\": \"unknown-user\", \"password\": \"wrong-password-12345\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.error.fieldErrors", empty()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh rejects invalid token format with 401")
    void refreshWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"invalid-token-value-12345\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.error.fieldErrors", empty()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/revoke permits anonymous access and returns 200 OK")
    void revokePermitsAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"some-token-value\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/private-pin/verify rejects missing, malformed, and expired Bearer tokens with 401")
    void pinVerifyRejectsUnauthenticated() throws Exception {
        // Missing token
        mockMvc.perform(post("/api/v1/auth/private-pin/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": \"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Malformed token
        mockMvc.perform(post("/api/v1/auth/private-pin/verify")
                        .header("Authorization", "Bearer invalid.malformed.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": \"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Expired token
        Instant now = Instant.now();
        org.springframework.security.oauth2.jwt.JwsHeader headers = org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("vaultowner")
                .issuedAt(now.minus(2, ChronoUnit.HOURS))
                .expiresAt(now.minus(1, ChronoUnit.HOURS))
                .build();
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

        mockMvc.perform(post("/api/v1/auth/private-pin/verify")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": \"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("PUT /api/v1/auth/private-pin rejects missing, malformed, and expired Bearer tokens with 401")
    void pinChangeRejectsUnauthenticated() throws Exception {
        // Missing token
        mockMvc.perform(put("/api/v1/auth/private-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\": \"123456\", \"newPin\": \"654321\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Malformed token
        mockMvc.perform(put("/api/v1/auth/private-pin")
                        .header("Authorization", "Bearer invalid.malformed.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\": \"123456\", \"newPin\": \"654321\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Expired token
        Instant now = Instant.now();
        org.springframework.security.oauth2.jwt.JwsHeader headers = org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("vaultowner")
                .issuedAt(now.minus(2, ChronoUnit.HOURS))
                .expiresAt(now.minus(1, ChronoUnit.HOURS))
                .build();
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

        mockMvc.perform(put("/api/v1/auth/private-pin")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\": \"123456\", \"newPin\": \"654321\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("Protected routes reject missing, malformed, and expired Bearer tokens with 401")
    void protectedRoutesEnforceValidBearerTokens() throws Exception {
        // 1. Missing Bearer token -> 401
        mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // 2. Malformed Bearer token -> 401
        mockMvc.perform(get("/api/v1/settings")
                        .header("Authorization", "Bearer invalid.malformed.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // 3. Expired Bearer token -> 401
        Instant now = Instant.now();
        org.springframework.security.oauth2.jwt.JwsHeader headers = org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("vaultowner")
                .issuedAt(now.minus(2, ChronoUnit.HOURS))
                .expiresAt(now.minus(1, ChronoUnit.HOURS))
                .build();
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

        mockMvc.perform(get("/api/v1/settings")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("Anonymous route matrix proves only the exact public endpoints and Swagger are accessible anonymously")
    void anonymousRouteMatrix_verifiesOnlyAllowedPathsArePublicAndSwaggerIsAccessible() throws Exception {
        // 1. Operational & Swagger endpoints permit anonymous access
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(200, 302));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        // 2. The 5 exact public auth operations permit anonymous access
        mockMvc.perform(get("/api/v1/auth/bootstrap/status"))
                .andExpect(status().isOk());

        // Reaching controller without 401 (fails with 400 for bad body rather than 401 AUTHENTICATION_REQUIRED)
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 3. Any other HTTP method on public paths requires authentication -> 401
        mockMvc.perform(get("/api/v1/auth/bootstrap"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/auth/revoke"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(post("/api/v1/auth/bootstrap/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // 4. Other application routes require authentication -> 401
        mockMvc.perform(get("/api/v1/auth/private-pin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/reference/countries"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/vault/entries/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/search"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }
}
