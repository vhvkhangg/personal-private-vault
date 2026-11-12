package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileRouteAuthorizationIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter handlerAdapter;

    @Autowired(required = false)
    private org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder;

    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
    private com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations vaultEntryOperations;

    @Test
    @DisplayName("DPA-12 / B1: Complete sweep of all registered business endpoints reconciles exact 8-route exception set, denies remaining mappings, and verifies no handler invocation / domain mutation")
    void registeredRoutesDeniedToPublicTokenFromBothOrigins() throws Exception {
        String tokenConfigured = publicBearerToken();
        String tokenDowngraded = publicBearerTokenViaDowngrade();

        Set<String> declaredAllowedPairs = Set.of(
                "GET /api/v1/fictions/{id}",
                "GET /api/v1/films/{id}",
                "GET /api/v1/reference/countries",
                "GET /api/v1/reference/countries/{code}",
                "GET /api/v1/reference/languages",
                "GET /api/v1/reference/languages/{code}",
                "GET /api/v1/reference/currencies",
                "GET /api/v1/reference/currencies/{code}"
        );
        Set<String> discoveredAllowedPairs = new HashSet<>();
        Set<String> testedEndpoints = new HashSet<>();

        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var valuesBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(valuesBefore.keySet()).contains("vault_entries", "persons", "external_accounts", "wallets", "diary_entries", "app_settings", "fictions", "films");
        org.mockito.Mockito.clearInvocations(handlerAdapter);

        handlerMapping.getHandlerMethods().forEach((info, method) -> {
            Set<String> patterns = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            Set<RequestMethod> targetMethods = methods.isEmpty() ? Set.of(RequestMethod.GET) : methods;

            for (String pattern : patterns) {
                // Ignore swagger / error / auth endpoints
                if (pattern.startsWith("/api/v1/auth/") || pattern.startsWith("/v3/api-docs") || pattern.startsWith("/swagger-ui") || pattern.startsWith("/error")) {
                    continue;
                }

                for (RequestMethod requestMethod : targetMethods) {
                    String pairKey = requestMethod.name() + " " + pattern;

                    if (declaredAllowedPairs.contains(pairKey)) {
                        discoveredAllowedPairs.add(pairKey);
                        Class<?> beanType = method.getBeanType();
                        if (pattern.startsWith("/api/v1/fictions/")) {
                            assertThat(beanType).isEqualTo(com.vhvkhangg.personalprivatevault.fiction.internal.web.controller.FictionController.class);
                        } else if (pattern.startsWith("/api/v1/films/")) {
                            assertThat(beanType).isEqualTo(com.vhvkhangg.personalprivatevault.film.internal.web.controller.FilmController.class);
                        } else if (pattern.startsWith("/api/v1/reference/")) {
                            assertThat(beanType).isEqualTo(com.vhvkhangg.personalprivatevault.reference.internal.web.controller.ReferenceController.class);
                        }
                        String allowedUri = pattern;
                        if (pattern.equals("/api/v1/fictions/{id}")) {
                            allowedUri = "/api/v1/fictions/904";
                        } else if (pattern.equals("/api/v1/films/{id}")) {
                            allowedUri = "/api/v1/films/905";
                        } else if (pattern.equals("/api/v1/reference/countries/{code}")) {
                            allowedUri = "/api/v1/reference/countries/VN";
                        } else if (pattern.equals("/api/v1/reference/languages/{code}")) {
                            allowedUri = "/api/v1/reference/languages/en";
                        } else if (pattern.equals("/api/v1/reference/currencies/{code}")) {
                            allowedUri = "/api/v1/reference/currencies/USD";
                        }

                        try {
                            // Test with configured PUBLIC token -> 200
                            mockMvc.perform(get(allowedUri).header("Authorization", "Bearer " + tokenConfigured))
                                    .andExpect(status().isOk())
                                    .andExpect(jsonPath("$.data").isNotEmpty());

                            // Test with downgraded PUBLIC token -> 200
                            mockMvc.perform(get(allowedUri).header("Authorization", "Bearer " + tokenDowngraded))
                                    .andExpect(status().isOk())
                                    .andExpect(jsonPath("$.data").isNotEmpty());

                            // Test anonymous -> 401
                            mockMvc.perform(get(allowedUri))
                                    .andExpect(status().isUnauthorized())
                                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

                            // Test with PRIVATE token -> 200
                            mockMvc.perform(get(allowedUri).header("Authorization", "Bearer " + bearerToken()))
                                    .andExpect(status().isOk());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed positive verification on allowed route: " + pairKey, e);
                        }
                        continue;
                    }

                    // For every other registered business mapping:
                    String concreteUri = pattern.replaceAll("\\{[^}]+\\}", "1");
                    if (pattern.contains("sid") || pattern.contains("uuid") || pattern.contains("SessionId") || pattern.contains("id") && pattern.contains("image")) {
                        concreteUri = pattern.replaceAll("\\{[^}]+\\}", "00000000-0000-0000-0000-000000000001");
                    }

                    String key = requestMethod.name() + " " + concreteUri;
                    if (!testedEndpoints.add(key)) {
                        continue;
                    }

                    try {
                        // Scope spy observation: reset before denial call
                        org.mockito.Mockito.clearInvocations(handlerAdapter);

                        // Test with configured PUBLIC token -> 403 ACCESS_DENIED
                        MockHttpServletRequestBuilder req1 = request(HttpMethod.valueOf(requestMethod.name()), concreteUri)
                                .header("Authorization", "Bearer " + tokenConfigured)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}");
                        mockMvc.perform(req1)
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                                .andExpect(result -> assertThat(result.getResponse().getHeader("Content-Disposition")).isNull());

                        org.mockito.Mockito.verify(handlerAdapter, org.mockito.Mockito.never())
                                .handle(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

                        // Test with downgraded PUBLIC token -> 403 ACCESS_DENIED
                        org.mockito.Mockito.clearInvocations(handlerAdapter);
                        MockHttpServletRequestBuilder req2 = request(HttpMethod.valueOf(requestMethod.name()), concreteUri)
                                .header("Authorization", "Bearer " + tokenDowngraded)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}");
                        mockMvc.perform(req2)
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                                .andExpect(result -> assertThat(result.getResponse().getHeader("Content-Disposition")).isNull());

                        org.mockito.Mockito.verify(handlerAdapter, org.mockito.Mockito.never())
                                .handle(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

                        // Anonymous check
                        mockMvc.perform(request(HttpMethod.valueOf(requestMethod.name()), concreteUri)
                                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

                    } catch (Exception e) {
                        throw new RuntimeException("Failed testing denied route: " + key, e);
                    }
                }
            }
        });

        // Exact membership reconciliation
        assertThat(discoveredAllowedPairs)
                .as("Exact declared 8-pair exception set must be registered and discovered")
                .isEqualTo(declaredAllowedPairs);

        assertThat(com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate))
                .as("Every domain row and column must survive the sweep unchanged").isEqualTo(valuesBefore);

        assertThat(testedEndpoints)
                .as("Sweep must have tested non-empty set of registered business endpoints across all methods")
                .hasSizeGreaterThan(20);
    }

    @Test
    void exactAuthMethodsAndInfrastructureDoNotGrantPublicBusinessAccess() throws Exception {
        String token = publicBearerToken();
        var declarations = Map.ofEntries(
                Map.entry("bootstrap/status", "GET"), Map.entry("bootstrap", "POST"), Map.entry("login", "POST"),
                Map.entry("profiles/login", "POST"), Map.entry("refresh", "POST"), Map.entry("revoke", "POST"),
                Map.entry("profiles/public-credential", "POST"), Map.entry("sessions/elevate-private", "POST"),
                Map.entry("sessions/downgrade-public", "POST"), Map.entry("private-pin/verify", "POST"), Map.entry("private-pin", "PUT"));
        for (var path : declarations.entrySet()) for (HttpMethod method : List.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)) {
            if (method.name().equals(path.getValue())) continue;
            mockMvc.perform(request(method, "/api/v1/auth/" + path.getKey()).header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
            mockMvc.perform(request(method, "/api/v1/auth/" + path.getKey()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized());
        }
        for (String path : List.of("/actuator/health", "/v3/api-docs", "/api/v1/auth/bootstrap/status")) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
            mockMvc.perform(get(path).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        }
        mockMvc.perform(options("/api/v1/personal/profiles").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/auth/not-granted").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DPA-12, DPA-13: PUBLIC bearer token receives 403 ACCESS_DENIED on unknown/unclassified routes")
    void publicProfileDeniedOnUnknownRoutes() throws Exception {
        String publicToken = publicBearerToken();

        mockMvc.perform(get("/api/v1/unknown-endpoint-path")
                        .header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/v1/unknown-post-path")
                        .header("Authorization", "Bearer " + publicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // HEAD request on protected path returns 403
        mockMvc.perform(head("/api/v1/personal/profile")
                        .header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DPA-13: PRIVATE bearer token receives 404 NOT_FOUND on unknown routes (not 403)")
    void privateProfileReceives404OnUnknownRoutes() throws Exception {
        String privateToken = bearerToken();

        mockMvc.perform(get("/api/v1/unknown-endpoint-path")
                        .header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DPA-13: Anonymous request on protected routes yields 401 AUTHENTICATION_REQUIRED")
    void anonymousRequestYields401() throws Exception {
        mockMvc.perform(get("/api/v1/personal/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("DPA-13: PUBLIC auth endpoints allow access without 403")
    void publicAuthEndpointsAccessible() throws Exception {
        // Bootstrap status is anonymous accessible
        mockMvc.perform(get("/api/v1/auth/bootstrap/status"))
                .andExpect(status().isOk());

        // Public login endpoint is accessible anonymously
        mockMvc.perform(post("/api/v1/auth/profiles/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profile\":\"PUBLIC\",\"password\":\"wrong-password-1234\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("DPA-10, DPA-13: ASYNC and ERROR dispatcher redispatch requires PRIVATE authority and revalidates stale context")
    void errorAndAsyncDispatcherRequiresPrivate() throws Exception {
        String publicToken = publicBearerToken();

        // 1. ERROR dispatcher with PUBLIC token -> 403 Forbidden
        mockMvc.perform(get("/error")
                        .with(propagated(publicToken))
                        .with(request -> {
                            request.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                            return request;
                        })
                        .header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden());

        // 2. ERROR dispatcher with Anonymous -> 401 Unauthorized
        mockMvc.perform(get("/error")
                        .with(request -> {
                            request.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                            return request;
                        }))
                .andExpect(status().isUnauthorized());

        // 3. ASYNC dispatcher with propagated live PUBLIC context -> exactly 403
        mockMvc.perform(get("/api/v1/personal/profile")
                        .with(propagated(publicToken))
                        .with(request -> {
                            request.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                            return request;
                        })
                        .header("Authorization", "Bearer " + publicToken))
                .andExpect(result -> {
                    assertThat(result.getResponse().getStatus()).isEqualTo(403);
                });

        // 4. ASYNC dispatcher with Anonymous -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/personal/profile")
                        .with(request -> {
                            request.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                            return request;
                        }))
                .andExpect(status().isUnauthorized());

        // 5. Stale PRIVATE context redispatch after downgrade -> rejected at admission
        String privateToken = bearerToken();
        mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/personal/profile")
                        .with(propagated(privateToken))
                        .with(request -> {
                            request.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                            return request;
                        })
                        .header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor propagated(String token) {
        var securityContext = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwtDecoder.decode(token)));
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext(securityContext);
    }

    public static class AsyncProbe {
        org.springframework.security.core.context.SecurityContext captured;
        @org.springframework.web.bind.annotation.ResponseBody
        public java.util.concurrent.Callable<Map<String,String>> start() {
            captured = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            captured.setAuthentication(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
            return () -> Map.of("value", "synthetic-async-data");
        }
    }

    @Test
    void actualAsyncRedispatchRevalidatesPropagatedContextWithoutBearer() throws Exception {
        publicBearerToken(); // configure PUBLIC using a separate PRIVATE-only session
        var info = org.springframework.web.servlet.mvc.method.RequestMappingInfo.paths("/api/v1/dpa-test/async")
                .methods(RequestMethod.GET).options(handlerMapping.getBuilderConfiguration()).build();
        var probe = new AsyncProbe();
        handlerMapping.registerMapping(info, probe, AsyncProbe.class.getMethod("start"));
        try {
            for (boolean switchBeforeDispatch : List.of(false, true)) {
                var pair = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand(DEFAULT_OWNER_USERNAME, DEFAULT_OWNER_PASSWORD));
                var result = mockMvc.perform(get("/api/v1/dpa-test/async").header("Authorization", "Bearer " + pair.accessToken()))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.request().asyncStarted()).andReturn();
                result.getAsyncResult(5000);
                assertThat(probe.captured.getAuthentication()).isInstanceOf(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class);
                if (switchBeforeDispatch) mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + pair.accessToken())).andExpect(status().isOk());
                result.getRequest().removeHeader("Authorization");
                org.springframework.test.web.servlet.RequestBuilder dispatch = servletContext ->
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext(probe.captured)
                                .postProcessRequest(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(result).buildRequest(servletContext));
                mockMvc.perform(dispatch).andExpect(status().is(switchBeforeDispatch ? 401 : 200));
                if (switchBeforeDispatch) mockMvc.perform(get("/error").with(propagated(pair.accessToken()))
                        .with(r -> { r.setDispatcherType(jakarta.servlet.DispatcherType.ERROR); return r; }))
                        .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
            }
        } finally { handlerMapping.unregisterMapping(info); }
    }

    @Test
    @DisplayName("DPA-12, DPA-14: Local-ON Drive and Google endpoints denied to PUBLIC token with 403 ACCESS_DENIED")
    void localOnDriveAndGoogleRoutesDeniedToPublicToken() throws Exception {
        String direct = publicBearerToken();
        String downgraded = publicBearerTokenViaDowngrade();
        var key = java.nio.file.Files.createTempFile("dpa-on-", ".key");
        var spool = java.nio.file.Files.createTempDirectory("dpa-on-spool-");
        byte[] bytes = new byte[32]; new java.security.SecureRandom().nextBytes(bytes);
        java.nio.file.Files.write(key, bytes);
        try (var app = new org.springframework.boot.builder.SpringApplicationBuilder(
                com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class, OnProbeConfig.class).run(
                "--server.address=127.0.0.1", "--server.port=0",
                "--spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                "--spring.datasource.username=" + POSTGRES.getUsername(),
                "--spring.datasource.password=" + POSTGRES.getPassword(),
                "--ppv.security.jwt.secret=" + context.getEnvironment().getProperty("ppv.security.jwt.secret"),
                "--ppv.drive.enabled=true", "--ppv.drive.files.enabled=true",
                "--ppv.drive.files.spool-dir=" + spool, "--ppv.drive.token-key-file=" + key,
                "--ppv.drive.oauth.client-id=test-client", "--ppv.drive.oauth.client-secret=test-secret",
                "--ppv.drive.oauth.redirect-uri=http://localhost:8080/api/v1/drive/oauth/callback",
                "--ppv.security.google-signin.enabled=true",
                "--ppv.security.google-signin.client-id=test-client", "--ppv.security.google-signin.client-secret=test-secret",
                "--ppv.security.google-signin.txn-key-file=" + key)) {
            var web = (org.springframework.web.context.WebApplicationContext) app;
            var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(web)
                    .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
            var mapping = app.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            var registrations = mapping.getHandlerMethods();
            var paths = registrations.keySet().stream().flatMap(i -> i.getPatternValues().stream()).collect(java.util.stream.Collectors.toSet());
            assertThat(paths).contains("/api/v1/drive/connections", "/api/v1/drive/connections/{slot}/files",
                    "/api/v1/drive/connections/{slot}/files/{fileId}/download", "/api/v1/auth/google/link/start");
            mvc.perform(get("/api/v1/drive/connections").header("Authorization", "Bearer " + bearerToken()))
                    .andExpect(status().isOk());
            OnProbeConfig.admissions.set(0);
            var before = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
            int count = 0;
            for (var info : registrations.keySet()) for (String path : info.getPatternValues()) {
                if (!(path.startsWith("/api/v1/drive/") && !path.endsWith("/oauth/callback"))
                        && !path.equals("/api/v1/auth/google/link/start")) continue;
                for (var method : info.getMethodsCondition().getMethods()) for (String token : List.of(direct, downgraded)) {
                    mvc.perform(request(HttpMethod.valueOf(method.name()), path.replaceAll("\\{[^}]+\\}", "1"))
                                    .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{}"))
                            .andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                            .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                            .andExpect(r -> assertThat(r.getResponse().getHeader("Content-Disposition")).isNull());
                    count++;
                }
            }
            assertThat(count).isGreaterThan(20);
            assertThat(OnProbeConfig.admissions).hasValue(0);
            org.mockito.Mockito.verifyNoInteractions(app.getBean("onDriveOAuth"), app.getBean("onGoogleProvider"), app.getBean("onDriveFiles"));
            assertThat(com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate)).isEqualTo(before);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_google_login_attempts", Integer.class)).isZero();
            try (var contents = java.nio.file.Files.list(spool)) { assertThat(contents.count()).isZero(); }
            assertProfileOpenApi(objectMapper.readTree(mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()));
            var docs = objectMapper.readTree(mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString()).path("paths");
            assertThat(docs.path("/api/v1/auth/google/callback").path("get").path("responses").has("303")).isTrue();
            assertThat(docs.path("/api/v1/auth/google/callback").path("get").path("responses").has("200")).isFalse();
            for (String path : List.of("/api/v1/auth/google/start", "/api/v1/auth/google/handoff")) {
                assertThat(docs.path(path).path("post").path("responses").has("503")).isTrue();
                assertThat(docs.path(path).path("post").path("responses").has("200")).isFalse();
            }
            proveD4NoIssuance(app, mvc, direct);
        } finally {
            java.nio.file.Files.delete(key);
            java.nio.file.Files.delete(spool);
        }
    }

    @Autowired private org.springframework.web.context.WebApplicationContext context;

    @Test
    void dpAOpenApiSchemasAndErrorsMatchActualContract() throws Exception {
        assertProfileOpenApi(objectMapper.readTree(mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()));
    }

    private void assertProfileOpenApi(com.fasterxml.jackson.databind.JsonNode root) {
        var schemas = root.path("components").path("schemas");
        var fields = Map.of("PublicProfileLoginRequest", List.of("profile", "password"),
                "PublicCredentialRequest", List.of("privatePassword", "publicPassword"), "ElevatePrivateRequest", List.of("privatePassword"));
        fields.forEach((name, names) -> {
            var schema = schemas.path(name);
            assertThat(schema.path("additionalProperties").isBoolean()).isTrue();
            assertThat(schema.path("additionalProperties").asBoolean()).isFalse();
            assertThat(schema.path("properties").fieldNames()).toIterable().containsExactlyInAnyOrderElementsOf(names);
            var required = new java.util.ArrayList<String>(); schema.path("required").forEach(n -> required.add(n.asText()));
            assertThat(required).containsExactlyInAnyOrderElementsOf(names);
            for (String field : names) assertThat(schema.path("properties").path(field).path("type").asText()).isEqualTo("string");
        });
        assertThat(schemas.path("PublicProfileLoginRequest").path("properties").path("profile").path("pattern").asText()).isEqualTo("^PUBLIC$");
        var paths = root.path("paths");
        assertThat(paths.path("/api/v1/auth/profiles/login").path("post").path("security")).isEmpty();
        for (String path : List.of("/api/v1/auth/profiles/public-credential", "/api/v1/auth/sessions/elevate-private", "/api/v1/auth/sessions/downgrade-public")) {
            var op = paths.path(path).path("post");
            assertThat(op.path("security").toString() + root.path("security")).contains("bearerAuth");
            for (String code : List.of("400", "401", "403")) assertThat(op.path("responses").path(code).path("content").path("application/json").path("schema").path("$ref").asText())
                    .isEqualTo("#/components/schemas/ErrorResponse");
        }
        assertThat(paths.path("/api/v1/auth/profiles/login").path("post").path("responses").path("401").path("description").asText()).contains("AUTH_INVALID_CREDENTIALS");
        assertThat(paths.path("/api/v1/auth/profiles/public-credential").path("post").path("responses").path("422").path("description").asText()).contains("AUTH_PASSWORD_NOT_DISTINCT");
        assertThat(paths.path("/api/v1/auth/sessions/downgrade-public").path("post").path("responses").path("409").path("description").asText()).contains("AUTH_PUBLIC_NOT_CONFIGURED");
        assertThat(root.path("components").path("securitySchemes").path("bearerAuth").path("description").asText()).contains("PUBLIC", "PRIVATE", "persisted");
    }

    private void proveD4NoIssuance(org.springframework.context.ConfigurableApplicationContext app, org.springframework.test.web.servlet.MockMvc mvc, String publicToken) throws Exception {
        var service = app.getBean(com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService.class);
        var store = app.getBean(com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInStore.class);
        var crypto = app.getBean(com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInTransactionCrypto.class);
        String verifier = "V".repeat(43);
        String challenge = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
        long sessions = jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_login_sessions", Long.class);
        for (String token : List.of("", publicToken, bearerToken())) {
            var start = post("/api/v1/auth/google/start").header("Host", "localhost:8080").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"handoffChallenge\":\"" + challenge + "\"}");
            if (!token.isEmpty()) start.header("Authorization", "Bearer " + token);
            mvc.perform(start).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.error.code").value("GOOGLE_SIGNIN_UNAVAILABLE"));
        }
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.startLogin(challenge))
                .isInstanceOfSatisfying(com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException.class, ex -> assertThat(ex.getHttpStatus()).isEqualTo(503));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_google_login_attempts", Integer.class)).isZero();
        jdbcTemplate.update("INSERT INTO ppv_owner_google_identities(owner_id,issuer,google_sub,linked_at) VALUES (1,'https://accounts.google.com','synthetic-sub',now())");
        var id = java.util.UUID.randomUUID();
        String state = "S".repeat(43), binding = "B".repeat(43);
        var encrypted = crypto.encryptPkce(verifier, id, "LOGIN", null, challenge, "test-client");
        store.prepareLoginStart(id, sha(state), sha(binding), sha("nonce"), challenge, encrypted, java.time.Instant.now());
        mvc.perform(get("/api/v1/auth/google/callback").header("Host", "localhost:8080").param("state", state).param("code", "never-exchanged")
                        .cookie(new jakarta.servlet.http.Cookie("ppv_google_signin_bind", binding)))
                .andExpect(status().isSeeOther()).andExpect(r -> assertThat(r.getResponse().getContentAsByteArray()).isEmpty());
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM ppv_google_login_attempts WHERE attempt_id=?", String.class, id)).isEqualTo("FAILED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_google_handoff_tickets", Integer.class)).isZero();
        // A genuine pending historical LINK ticket must survive service/store/HTTP redemption denial.
        var linkId = java.util.UUID.randomUUID();
        String linkState = "L".repeat(43), linkBinding = "C".repeat(43), ticket = "T".repeat(43);
        var linkCrypto = crypto.encryptPkce(verifier, linkId, "LINK", (short) 1, challenge, "test-client");
        store.prepareLinkStart(linkId, sha(linkState), sha(linkBinding), sha("link-nonce"), challenge, linkCrypto, DEFAULT_OWNER_PASSWORD, java.time.Instant.now());
        var claim = store.claimAttempt(sha(linkState), sha(linkBinding), java.time.Instant.now());
        store.finalizeCompletion(linkId, claim.claimEpoch(), "https://accounts.google.com", "synthetic-sub", sha(ticket));
        var before = jdbcTemplate.queryForList("SELECT to_jsonb(t)::text FROM ppv_google_handoff_tickets t ORDER BY ticket_hash");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> store.redeemTicket(sha(ticket), verifier))
                .isInstanceOfSatisfying(com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException.class, ex -> assertThat(ex.getHttpStatus()).isEqualTo(503));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.redeemHandoff(ticket, verifier))
                .isInstanceOfSatisfying(com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException.class, ex -> assertThat(ex.getHttpStatus()).isEqualTo(503));
        mvc.perform(post("/api/v1/auth/google/handoff").header("Host", "localhost:8080").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticket\":\"" + ticket + "\",\"handoffVerifier\":\"" + verifier + "\"}"))
                .andExpect(status().isServiceUnavailable());
        assertThat(jdbcTemplate.queryForList("SELECT to_jsonb(t)::text FROM ppv_google_handoff_tickets t ORDER BY ticket_hash")).isEqualTo(before);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_login_sessions", Long.class)).isEqualTo(sessions);
        org.mockito.Mockito.verifyNoInteractions(app.getBean("onDriveOAuth"), app.getBean("onGoogleProvider"), app.getBean("onDriveFiles"));
    }

    private static String sha(String value) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
    }

    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    public static class OnProbeConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
        static final java.util.concurrent.atomic.AtomicInteger admissions = new java.util.concurrent.atomic.AtomicInteger();
        @Override public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
            registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor() {
                @Override public boolean preHandle(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res, Object handler) {
                    admissions.incrementAndGet(); return true;
                }
            });
        }
        @org.springframework.context.annotation.Bean @org.springframework.context.annotation.Primary
        com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient onDriveOAuth() {
            return org.mockito.Mockito.mock(com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient.class);
        }
        @org.springframework.context.annotation.Bean @org.springframework.context.annotation.Primary
        com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient onGoogleProvider() {
            return org.mockito.Mockito.mock(com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient.class);
        }
        @org.springframework.context.annotation.Bean @org.springframework.context.annotation.Primary
        com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient onDriveFiles() {
            return org.mockito.Mockito.mock(com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient.class);
        }
    }

    @Test
    @DisplayName("B1-20: ASYNC and ERROR redispatch on newly accessible public routes requires PRIVATE authority")
    void sameUriAsyncAndErrorDispatcherDeniesPublicSession() throws Exception {
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        String publicToken = publicBearerToken();
        String privateToken = bearerToken();

        for (String uri : List.of("/api/v1/fictions/904", "/api/v1/films/905", "/api/v1/reference/countries")) {
            // 1. Normal request with PUBLIC token succeeds (200) with and without bearer header
            mockMvc.perform(get(uri).header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isOk());

            mockMvc.perform(get(uri).with(propagated(publicToken)))
                    .andExpect(status().isOk());

            // 2. ERROR dispatcher with live propagated PUBLIC context (both with bearer header and without) -> 403 Forbidden
            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                                return req;
                            })
                            .header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                                return req;
                            }))
                    .andExpect(status().isForbidden());

            // 3. ASYNC dispatcher with live propagated PUBLIC context (both with bearer header and without) -> 403 Forbidden
            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return req;
                            })
                            .header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get(uri)
                            .with(propagated(publicToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return req;
                            }))
                    .andExpect(status().isForbidden());

            // 4. ASYNC and ERROR without token -> 401 Unauthorized
            mockMvc.perform(get(uri)
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return req;
                            }))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get(uri)
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                                return req;
                            }))
                    .andExpect(status().isUnauthorized());

            // 5. ASYNC and ERROR with live propagated PRIVATE context (both with and without bearer header) -> permitted past ASYNC/ERROR guard
            mockMvc.perform(get(uri)
                            .with(propagated(privateToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return req;
                            })
                            .header("Authorization", "Bearer " + privateToken))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));

            mockMvc.perform(get(uri)
                            .with(propagated(privateToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ASYNC);
                                return req;
                            }))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));

            mockMvc.perform(get(uri)
                            .with(propagated(privateToken))
                            .with(req -> {
                                req.setDispatcherType(jakarta.servlet.DispatcherType.ERROR);
                                return req;
                            }))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));
        }
    }

    @Test
    @DisplayName("B1-20: Method and path variants on public routes deny non-GET and subpaths")
    void methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths() throws Exception {
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        String publicToken = publicBearerToken();

        for (String uri : List.of("/api/v1/fictions/904", "/api/v1/films/905", "/api/v1/reference/countries")) {
            for (HttpMethod m : List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.OPTIONS, HttpMethod.HEAD)) {
                mockMvc.perform(request(m, uri)
                                .header("Authorization", "Bearer " + publicToken)
                                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
            }
        }

        // Subpaths and traversal
        for (String sub : List.of("/api/v1/fictions/904/extra", "/api/v1/films/905/extra", "/api/v1/reference/countries/VN/extra")) {
            mockMvc.perform(get(sub).header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
        }

        // Trailing slash variants
        for (String trailing : List.of("/api/v1/fictions/904/", "/api/v1/films/905/", "/api/v1/reference/countries/", "/api/v1/reference/countries/VN/")) {
            mockMvc.perform(get(trailing).header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
        }

        // Suffix variants - unmapped suffix denies via 403 default-deny, non-numeric segment fails conversion with 400, and non-existent code returns 404
        for (String suffix : List.of("/api/v1/fictions/904.json", "/api/v1/films/905.json", "/api/v1/reference/countries.json", "/api/v1/reference/countries/VN.json")) {
            mockMvc.perform(get(suffix).header("Authorization", "Bearer " + publicToken))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(400, 403, 404));
        }

        // Traversal / encoded paths
        for (String norm : List.of("/api/v1/fictions/904/..", "/api/v1/fictions/../fictions/904", "/api/v1/films/905/..", "/api/v1/reference/countries/..")) {
            mockMvc.perform(get(norm).header("Authorization", "Bearer " + publicToken))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(400, 403, 404));
        }

        // Token negatives on newly accessible routes
        java.time.Instant now = java.time.Instant.now();
        org.springframework.security.oauth2.jwt.JwtClaimsSet expiredClaims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("1")
                .issuedAt(now.minus(java.time.Duration.ofHours(2)))
                .expiresAt(now.minus(java.time.Duration.ofHours(1)))
                .id(java.util.UUID.randomUUID().toString())
                .claim("profile", "PUBLIC")
                .claim("sid", java.util.UUID.randomUUID().toString())
                .claim("generation", 1L)
                .build();
        org.springframework.security.oauth2.jwt.JwsHeader jwsHeader = org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        String expiredToken = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(jwsHeader, expiredClaims)).getTokenValue();

        org.springframework.security.oauth2.jwt.JwtClaimsSet unknownSessionClaims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("personal-private-vault")
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plus(java.time.Duration.ofHours(1)))
                .id(java.util.UUID.randomUUID().toString())
                .claim("profile", "PUBLIC")
                .claim("sid", java.util.UUID.randomUUID().toString())
                .claim("generation", 1L)
                .build();
        String unknownSessionToken = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(jwsHeader, unknownSessionClaims)).getTokenValue();

        for (String uri : List.of("/api/v1/fictions/904", "/api/v1/films/905", "/api/v1/reference/countries")) {
            // Expired token -> 401
            mockMvc.perform(get(uri).header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

            // Unknown session token -> 401
            mockMvc.perform(get(uri).header("Authorization", "Bearer " + unknownSessionToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

            // Malformed token -> 401
            mockMvc.perform(get(uri).header("Authorization", "Bearer not-a-valid-token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        }
    }

    @Test
    @DisplayName("B1-10: Direct Film Credit real and absent ID denies PUBLIC even with safe parent; permits PRIVATE")
    void directAndNestedFilmCreditDeniedToPublicWithPopulatedParent() throws Exception {
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        String publicToken = publicBearerToken();
        String privateToken = bearerToken();

        // Direct credit with populated safe parent (Film 905, Credit 901)
        mockMvc.perform(get("/api/v1/films/credits/901").header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/credits/901").header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(901));

        // Direct absent credit (999999L) -> denies PUBLIC with 403 (no oracle)
        mockMvc.perform(get("/api/v1/films/credits/999999").header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/credits/999999").header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FILM_CREDIT_NOT_FOUND"));

        // Nested credits on film 905
        mockMvc.perform(get("/api/v1/films/905/credits").header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/905/credits").header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("B1-18: Session lifecycle, controlled downgrade, rotation and revocation on newly accessible detail routes")
    void sessionLifecycleAndMutationOnNewlyAccessibleDetailRoutes() throws Exception {
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        publicBearerToken(); // Ensure public credential is legitimately configured
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        String loginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + DEFAULT_OWNER_USERNAME + "\",\"password\":\"" + DEFAULT_OWNER_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String privAccess = mapper.readTree(loginResp).path("data").path("accessToken").asText();
        String refreshToken = mapper.readTree(loginResp).path("data").path("refreshToken").asText();

        // 1. Initial private access to detail -> 200 full
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + privAccess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressStatus").value("COMPLETED"));

        // 2. Controlled downgrade to public
        String downgradeResp = mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + privAccess))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String downgradedPublicToken = mapper.readTree(downgradeResp).path("data").path("accessToken").asText();

        // 3. Newly accessible detail with downgraded token -> 200 minimal (6 fields, no progressStatus or authorPersonId)
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + downgradedPublicToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(904))
                .andExpect(jsonPath("$.data.progressStatus").doesNotExist())
                .andExpect(jsonPath("$.data.authorPersonId").doesNotExist());

        // 4. Stale private access token rejected after downgrade
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + privAccess))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // 5. Elevate back to private
        String elevateResp = mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                        .header("Authorization", "Bearer " + downgradedPublicToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privatePassword\":\"" + DEFAULT_OWNER_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String elevatedPrivToken = mapper.readTree(elevateResp).path("data").path("accessToken").asText();
        String elevatedRefreshToken = mapper.readTree(elevateResp).path("data").path("refreshToken").asText();

        // Earlier generation public token is now rejected after elevation
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + downgradedPublicToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Elevated private token works and returns full view
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + elevatedPrivToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressStatus").value("COMPLETED"));

        // Stale pre-elevation refresh token is rejected
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        // 6. Rotate session via refresh
        String refreshResp = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + elevatedRefreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String rotatedPrivToken = mapper.readTree(refreshResp).path("data").path("accessToken").asText();
        String nextRefreshToken = mapper.readTree(refreshResp).path("data").path("refreshToken").asText();

        // Rotated token works
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + rotatedPrivToken))
                .andExpect(status().isOk());

        // 7. Revoke session
        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + nextRefreshToken + "\"}"))
                .andExpect(status().isOk());

        // Revoked token is rejected
        mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + rotatedPrivToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // 8. B1-18: Concurrent in-flight read request completes while fresh request after session mutation is denied
        String newLoginResp = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + DEFAULT_OWNER_USERNAME + "\",\"password\":\"" + DEFAULT_OWNER_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newPrivAccess = mapper.readTree(newLoginResp).path("data").path("accessToken").asText();

        String newDowngradeResp = mockMvc.perform(post("/api/v1/auth/sessions/downgrade-public")
                        .header("Authorization", "Bearer " + newPrivAccess))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String activePublicToken = mapper.readTree(newDowngradeResp).path("data").path("accessToken").asText();

        java.util.concurrent.CountDownLatch readInFlight = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch mutationCommitted = new java.util.concurrent.CountDownLatch(1);

        org.mockito.Mockito.doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId != null && argId.equals(904L)) {
                readInFlight.countDown();
                assertThat(mutationCommitted.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(904L);

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            java.util.concurrent.Future<org.springframework.test.web.servlet.MvcResult> inFlightReadFuture = executor.submit(() ->
                    mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + activePublicToken))
                            .andReturn()
            );

            // Wait until read passes security filter and enters findPublic
            assertThat(readInFlight.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

            // While read is in-flight, mutate session (elevate to private)
            mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                            .header("Authorization", "Bearer " + activePublicToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"privatePassword\":\"" + DEFAULT_OWNER_PASSWORD + "\"}"))
                    .andExpect(status().isOk());
            mutationCommitted.countDown();

            // In-flight read was already admitted and completes successfully with 200 OK
            org.springframework.test.web.servlet.MvcResult inFlightResult = inFlightReadFuture.get(5, java.util.concurrent.TimeUnit.SECONDS);
            assertThat(inFlightResult.getResponse().getStatus()).isEqualTo(200);
            assertThat(inFlightResult.getResponse().getContentAsString()).contains("\"id\":904");

            // Fresh request arriving AFTER session mutation commit is denied with 401 AUTHENTICATION_REQUIRED
            mockMvc.perform(get("/api/v1/fictions/904").header("Authorization", "Bearer " + activePublicToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

            // Redispatch with stale propagated context without bearer is also denied
            mockMvc.perform(get("/api/v1/fictions/904").with(propagated(activePublicToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        } finally {
            org.mockito.Mockito.reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("DPA-12, DPA-16: Sensitive export denied to PUBLIC token with 403, permitted to PRIVATE token")
    void sensitiveExportDeniedToPublicPermittedToPrivate() throws Exception {
        String publicToken = publicBearerToken();
        mockMvc.perform(post("/api/v1/portability/exports")
                        .header("Authorization", "Bearer " + publicToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                .andExpect(result -> {
                    assertThat(result.getResponse().getHeader("Content-Disposition")).isNull();
                });

        String privateToken = bearerToken();
        mockMvc.perform(post("/api/v1/portability/exports")
                        .header("Authorization", "Bearer " + privateToken))
                .andExpect(status().isOk());
    }
}
