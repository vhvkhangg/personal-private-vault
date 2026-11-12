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
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        jdbcTemplate.execute("TRUNCATE TABLE ppv_login_sessions, ppv_public_login_credential, refresh_tokens, app_users CASCADE");
    }

    private String createSession(com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile profile) {
        jdbcTemplate.update("""
                INSERT INTO app_users (id, email, username, password_hash, pin_hash, created_at, updated_at)
                VALUES (1, 'owner@vault.local', 'owner',
                        '$2a$10$w8T9h3n6E3t8P4w8o8l8uu4P/Jq2U4hX1q4Jq2U4hX1q4Jq2U4hX1',
                        '$2a$10$w8T9h3n6E3t8P4w8o8l8uu4P/Jq2U4hX1q4Jq2U4hX1q4Jq2U4hX1',
                        now(), now())
                ON CONFLICT (id) DO NOTHING
                """);
        long epoch = profile == com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC ? 1L : 0L;
        String pubHash = profile == com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC ? "somehash" : null;
        jdbcTemplate.update("""
                INSERT INTO ppv_public_login_credential (owner_id, password_hash, credential_epoch, created_at, updated_at)
                VALUES (1, ?, ?, now(), now())
                ON CONFLICT (owner_id) DO NOTHING
                """, pubHash, epoch);

        UUID sid = UUID.randomUUID();
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofDays(30));
        String raw = UUID.randomUUID().toString();
        String hash = java.util.HexFormat.of().formatHex(
                raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        Long tid = new org.springframework.transaction.support.TransactionTemplate(transactionManager).execute(status -> {
            Long id = jdbcTemplate.queryForObject("""
                    INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at)
                    VALUES (1, ?, ?, 1, ?, ?, ?)
                    RETURNING id
                    """, Long.class, sid, profile.name(), hash, java.sql.Timestamp.from(exp), java.sql.Timestamp.from(now));

            Long sessionEpoch = profile == com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC ? 1L : null;
            jdbcTemplate.update("""
                    INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at)
                    VALUES (?, 1, ?, 1, ?, ?, ?, ?)
                    """, sid, profile.name(), sessionEpoch, id, java.sql.Timestamp.from(now), java.sql.Timestamp.from(exp));
            return id;
        });

        return jwtTokenService.issueAccessToken(profile, sid, 1L);
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
        String validToken = createSession(com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PRIVATE);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("authenticated"));
    }

    @Test
    @DisplayName("Protected endpoint rejects request with valid PUBLIC bearer token with 403 ACCESS_DENIED")
    void rejectsPublicBearerTokenOnProtectedEndpoint() throws Exception {
        String publicToken = createSession(com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("Protected endpoint rejects legacy token missing profile and sid claims with 401")
    void rejectsLegacyTokenMissingProfileAndSid() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet legacyClaims = JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String legacyToken = jwtEncoder.encode(JwtEncoderParameters.from(header, legacyClaims)).getTokenValue();

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + legacyToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("Protected endpoint rejects token with subject other than 1 with 401")
    void rejectsTokenWithSubjectOtherThanOne() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("2")
                .claim("profile", "PRIVATE")
                .claim("sid", UUID.randomUUID().toString())
                .claim("generation", 1L)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("Security filter chain permits valid PUBLIC token on exact B1 GET endpoints and denies unselected")
    void permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected() throws Exception {
        String publicToken = createSession(com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC);

        // Permitted: returns 200 or 404 (data not found), never 401 or 403
        for (String uri : java.util.List.of(
                "/api/v1/fictions/1",
                "/api/v1/films/1",
                "/api/v1/reference/countries",
                "/api/v1/reference/countries/VN",
                "/api/v1/reference/languages",
                "/api/v1/reference/languages/en",
                "/api/v1/reference/currencies",
                "/api/v1/reference/currencies/USD"
        )) {
            mockMvc.perform(get(uri).header("Authorization", "Bearer " + publicToken))
                    .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus()).isIn(200, 404));
        }

        // Denied unselected business endpoints: exactly 403 ACCESS_DENIED
        for (String uri : java.util.List.of(
                "/api/v1/fictions",
                "/api/v1/films",
                "/api/v1/reference/platforms",
                "/api/v1/reference/story-archetypes",
                "/api/v1/reference/world-settings"
        )) {
            mockMvc.perform(get(uri).header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
        }
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor propagated(String token) {
        var securityContext = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwtDecoder.decode(token)));
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext(securityContext);
    }

    @Test
    @DisplayName("Security filter chain enforces ASYNC and ERROR dispatcher priority requiring PRIVATE even on B1 public routes")
    void enforcesAsyncAndErrorDispatcherPriorityOnB1Routes() throws Exception {
        String publicToken = createSession(com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile.PUBLIC);

        for (String uri : java.util.List.of("/api/v1/fictions/1", "/api/v1/films/1", "/api/v1/reference/countries")) {
            // ERROR dispatcher with PUBLIC token (with bearer header and without) -> 403 ACCESS_DENIED
            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(request -> {
                                request.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                                return request;
                            })
                            .header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(request -> {
                                request.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                                return request;
                            }))
                    .andExpect(status().isForbidden());

            // ASYNC dispatcher with PUBLIC token (with bearer header and without) -> 403 ACCESS_DENIED
            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(request -> {
                                request.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return request;
                            })
                            .header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(request -> {
                                request.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return request;
                            }))
                    .andExpect(status().isForbidden());
        }
    }
}
