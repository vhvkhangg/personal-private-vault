package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityFilterChainIntegrationTest.TestEndpointConfiguration.class)
class SecurityFilterChainIntegrationTest extends AbstractPostgresIntegrationTest {

    @TestConfiguration
    static class TestEndpointConfiguration {
        @RestController
        @RequestMapping("/api/test")
        static class TestProtectedController {
            @GetMapping("/protected")
            public Map<String, String> protectedEndpoint() {
                return Map.of("status", "authenticated");
            }
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtEncoder jwtEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Actuator health endpoint permits unauthenticated access")
    void permitsHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator info endpoint rejects unauthenticated access with 401")
    void rejectsInfoEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OpenAPI docs endpoint permits unauthenticated access")
    void permitsOpenApiDocsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint rejects request without Authorization header with 401")
    void rejectsMissingBearerToken() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint rejects request with invalid bearer token with 401")
    void rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer invalid-token-string"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint rejects request with expired bearer token with 401")
    void rejectsExpiredBearerToken() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet expiredClaims = JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("1")
                .issuedAt(now.minus(Duration.ofHours(2)))
                .expiresAt(now.minus(Duration.ofHours(1)))
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(header, expiredClaims)).getTokenValue();

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint authenticates successfully with valid issued bearer token")
    void authenticatesWithValidBearerToken() throws Exception {
        String validToken = jwtTokenService.issueAccessToken();

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("authenticated"));
    }
}
