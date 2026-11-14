package com.vhvkhangg.personalprivatevault.film;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicFilmCreateIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private FilmOperations filmOperations;

    @Autowired
    private FilmRepository filmRepository;

    @MockitoSpyBean
    private VaultEntryOperations vaultEntryOperations;

    @MockitoSpyBean
    private com.vhvkhangg.personalprivatevault.authentication.internal.application.session.SessionAuthorityService sessionAuthorityService;

    @MockitoSpyBean
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDownTriggersAndMocks() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_test_film_insert ON films");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS trg_fail_sql_insert()");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_test_film_commit ON films");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS trg_fail_deferred_commit()");
        reset(vaultEntryOperations);
        if (sessionAuthorityService != null) {
            reset(sessionAuthorityService);
        }
        if (handlerAdapter != null) {
            reset(handlerAdapter);
        }
    }

    @Test
    @DisplayName("ROA-01: Public token from profiles login denied with 403 ACCESS_DENIED, zero handler execution, and zero domain mutation")
    void publicTokenFromProfilesLoginDeniedWith403AndNoMutation() throws Exception {
        String token = publicBearerToken();

        String payload = """
                {
                    "title": "Inception",
                    "originalTitle": "Inception Original",
                    "nationalityCode": "US",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION"
                }
                """;

        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);
        reset(handlerAdapter);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.error.message").value("Access is denied"))
                .andExpect(jsonPath("$.error.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.meta").isEmpty());

        verify(vaultEntryOperations, never()).create(any());
        verify(handlerAdapter, never()).handle(any(), any(), any());
        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-02: Public token from downgrade denied with 403 ACCESS_DENIED while allowed GET succeeds, zero handler execution, and zero domain mutation")
    void publicTokenFromDowngradeDeniedWith403AndNoMutation() throws Exception {
        // Ensure public credential exists on owner account before downgrade
        publicBearerToken();
        String token = publicBearerTokenViaDowngrade();

        // Verify live allowed GET succeeds with 200
        mockMvc.perform(get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());

        String payload = """
                {
                    "title": "Spirited Away",
                    "originalTitle": null,
                    "nationalityCode": null,
                    "format": "MOVIE",
                    "productionStyle": "ANIMATION"
                }
                """;

        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);
        reset(handlerAdapter);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.error.message").value("Access is denied"))
                .andExpect(jsonPath("$.error.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.meta").isEmpty());

        verify(vaultEntryOperations, never()).create(any());
        verify(handlerAdapter, never()).handle(any(), any(), any());
        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-03: PUBLIC sends broad private fields, malformed/duplicate JSON, and validation violations: all denied 403 before parsing or business execution")
    void publicPayloadVariantsDeniedWith403BeforeParsingOrValidation() throws Exception {
        String token = publicBearerToken();

        List<String> payloads = List.of(
                // Broad private fields (isNsfw, description, posterUrl, directorPersonId, review)
                """
                {"title":"Broad Film","format":"MOVIE","productionStyle":"LIVE_ACTION","isNsfw":false,"description":"Desc","posterUrl":"https://example.com/p.png","directorPersonId":1}
                """,
                // Malformed / incomplete JSON
                "{\"title\":\"Unclosed Film\",",
                // Duplicate JSON keys
                "{\"title\":\"First\",\"title\":\"Duplicate\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // Non-object JSON roots
                "[]",
                "\"just a string\"",
                "12345",
                // Blank title
                "{\"title\":\"   \",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // Title length overflow > 500
                "{\"title\":\"" + "T".repeat(501) + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // Non-existent nationality code
                "{\"title\":\"Valid\",\"nationalityCode\":\"ZZ\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"
        );

        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);

        for (String payload : payloads) {
            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                    .andExpect(jsonPath("$.error.message").value("Access is denied"));
        }

        verify(vaultEntryOperations, never()).create(any());
        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-03: PUBLIC request with missing Content-Type or text/plain denied 403 before parsing or media negotiation")
    void publicMissingOrTextPlainContentTypeDeniedWith403() throws Exception {
        String token = publicBearerToken();

        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);

        // Missing Content-Type
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .content("{\"title\":\"No CT Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // text/plain Content-Type
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{\"title\":\"Text Plain Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        verify(vaultEntryOperations, never()).create(any());
        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-04: Anonymous request to create film is rejected with 401 AUTHENTICATION_REQUIRED and safe error envelope")
    void anonymousRequestToCreateFilmReturns401() throws Exception {
        String payload = """
                {
                    "title": "Anon Film",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION"
                }
                """;

        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);
        reset(handlerAdapter);

        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.error.message").value("Full authentication is required to access this resource"))
                .andExpect(jsonPath("$.error.fieldErrors").isEmpty())
                .andExpect(jsonPath("$.meta").isEmpty());

        verify(vaultEntryOperations, never()).create(any());
        verify(handlerAdapter, never()).handle(any(), any(), any());
        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-05: PRIVATE profile creation retains full 18-key response, relations, committed rows, and standard headers (no Location, not no-store)")
    void privateProfileCreateRetainsFullEighteenKeyBehavior() throws Exception {
        String token = bearerToken();

        String privatePayload = """
                {
                    "title": "Private Complete Film",
                    "originalTitle": "Private Native",
                    "nationalityCode": "VN",
                    "posterUrl": "https://cdn.example.com/poster.png",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION",
                    "isNsfw": false,
                    "description": "Private Description",
                    "totalEpisodes": 1,
                    "progressStatus": "COMPLETED",
                    "consumptionStatus": "CONSUMED",
                    "currentProgressText": "Done",
                    "review": "Masterpiece"
                }
                """;

        reset(handlerAdapter);
        String rawResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privatePayload))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andReturn().getResponse().getContentAsString();

        // Verify handlerAdapter specifically observed FilmController.create
        ArgumentCaptor<Object> handlerCaptor = ArgumentCaptor.forClass(Object.class);
        verify(handlerAdapter, atLeast(1)).handle(any(), any(), handlerCaptor.capture());
        boolean foundFilmCreate = handlerCaptor.getAllValues().stream()
                .filter(h -> h instanceof org.springframework.web.method.HandlerMethod)
                .map(h -> (org.springframework.web.method.HandlerMethod) h)
                .anyMatch(hm -> hm.getBeanType().equals(com.vhvkhangg.personalprivatevault.film.internal.web.controller.FilmController.class)
                        && hm.getMethod().getName().equals("create"));
        assertThat(foundFilmCreate).isTrue();

        // PRIVATE response must NOT bleed no-store
        String cacheControl = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Private Second Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.CACHE_CONTROL);
        assertThat(cacheControl).isNotEqualTo("no-store");

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode data = root.path("data");
        Long filmId = data.get("id").asLong();

        assertThat(filmId).isPositive();
        assertThat(data.get("title").asText()).isEqualTo("Private Complete Film");
        assertThat(data.get("originalTitle").asText()).isEqualTo("Private Native");
        assertThat(data.get("nationalityCode").asText()).isEqualTo("VN");
        assertThat(data.get("posterUrl").asText()).isEqualTo("https://cdn.example.com/poster.png");
        assertThat(data.get("format").asText()).isEqualTo("MOVIE");
        assertThat(data.get("productionStyle").asText()).isEqualTo("LIVE_ACTION");
        assertThat(data.get("isNsfw").asBoolean()).isFalse();
        assertThat(data.get("description").asText()).isEqualTo("Private Description");
        assertThat(data.get("totalEpisodes").asInt()).isEqualTo(1);
        assertThat(data.get("progressStatus").asText()).isEqualTo("COMPLETED");
        assertThat(data.get("consumptionStatus").asText()).isEqualTo("CONSUMED");
        assertThat(data.get("currentProgressText").asText()).isEqualTo("Done");
        assertThat(data.get("review").asText()).isEqualTo("Masterpiece");
        assertThat(data.has("genreIds")).isTrue();
        assertThat(data.has("storyArchetypeIds")).isTrue();
        assertThat(data.has("worldSettingIds")).isTrue();

        // Verify exact 18 keys
        List<String> keys = new ArrayList<>();
        data.fieldNames().forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl",
                "format", "productionStyle", "isNsfw", "directorPersonId", "description",
                "totalEpisodes", "progressStatus", "consumptionStatus", "currentProgressText",
                "review", "genreIds", "storyArchetypeIds", "worldSettingIds"
        );

        // Independent SQL verification
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM films WHERE id = ? AND title = 'Private Complete Film'", Long.class, filmId)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries WHERE id = ? AND entry_type = 'FILM' AND deleted_at IS NULL", Long.class, filmId)).isEqualTo(1L);
    }

    @Test
    @DisplayName("ROA-06: PRIVATE profile accepts vendor JSON (application/vnd.ppv+json)")
    void privateVendorJsonContentTypeAccepted() throws Exception {
        String token = bearerToken();
        long filmsBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Long.class);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/vnd.ppv+json")
                        .content("{\"title\":\"Vendor JSON Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Vendor JSON Film"));

        long filmsAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Long.class);
        assertThat(filmsAfter - filmsBefore).isEqualTo(1L);
    }

    @Test
    @DisplayName("ROA-06: PRIVATE profile decodes ISO-8859-1 charset correctly")
    void privateIso88591CharsetDecodedCorrectly() throws Exception {
        String token = bearerToken();
        String jsonString = "{\"title\":\"Café\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}";
        byte[] isoBytes = jsonString.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json;charset=ISO-8859-1")
                        .content(isoBytes))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Café"));

        Long filmId = jdbcTemplate.queryForObject("SELECT id FROM films WHERE title = 'Café'", Long.class);
        assertThat(filmId).isNotNull();
    }

    @Test
    @DisplayName("ROA-06: PRIVATE profile rejects unknown property with 400 MALFORMED_REQUEST and no mutation")
    void privateUnknownKeyRejectedWith400() throws Exception {
        String token = bearerToken();
        long filmsBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Long.class);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Private Probe\",\"legacyExtra\":1,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        long filmsAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Long.class);
        assertThat(filmsAfter).isEqualTo(filmsBefore);
    }

    @Test
    @DisplayName("ROA-06: PRIVATE create without Content-Type returns 415 and makes zero database mutations")
    void privateMissingContentTypeReturns415() throws Exception {
        String token = bearerToken();
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .content("{\"title\":\"Private No CT Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));

        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-06: PRIVATE profile rejects unsupported media type (text/plain) with 415")
    void privateUnsupportedMediaTypeReturns415() throws Exception {
        String token = bearerToken();
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{\"title\":\"Plain Text Film\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("ROA-06: PRIVATE request failing Bean Validation returns 400 VALIDATION_ERROR with safe envelope and no mutation")
    void privateStructurallyInvalidRequestFailsBeanValidationWith400() throws Exception {
        String token = bearerToken();
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);

        // Blank title fails @NotBlank on CreateFilmRequest
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Request validation failed"))
                .andExpect(jsonPath("$.meta").isEmpty());

        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-06: PRIVATE request failing Film domain validation returns 422 FILM_INVALID with safe envelope and no mutation")
    void privateDomainValidationFailureReturns422FilmInvalid() throws Exception {
        String token = bearerToken();
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);

        // Nonexistent nationality code "ZZ" passes Bean Validation size=2 but fails reference catalog in FilmService
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Domain Invalid Film\",\"nationalityCode\":\"ZZ\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("FILM_INVALID"))
                .andExpect(jsonPath("$.error.message").value("Invalid film data"))
                .andExpect(jsonPath("$.meta").isEmpty());

        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("ROA-10: Follow-up B1 GET on film created by PRIVATE succeeds with identical 7 keys and no-store for PUBLIC token")
    void followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys() throws Exception {
        String privToken = bearerToken();
        String pubToken = publicBearerToken();

        String createPayload = """
                {
                    "title": "Interstellar",
                    "originalTitle": "Interstellar Original",
                    "nationalityCode": "US",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION",
                    "isNsfw": false
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + privToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long filmId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // Perform B1 GET on created ID with PUBLIC token
        String getResponse = mockMvc.perform(get("/api/v1/films/{id}", filmId)
                        .header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();

        JsonNode getData = objectMapper.readTree(getResponse).path("data");
        List<String> getFields = new ArrayList<>();
        getData.fieldNames().forEachRemaining(getFields::add);

        assertThat(getFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "format", "productionStyle", "isNsfw"
        );
        assertThat(getData.get("id").asLong()).isEqualTo(filmId);
        assertThat(getData.get("title").asText()).isEqualTo("Interstellar");
        assertThat(getData.get("originalTitle").asText()).isEqualTo("Interstellar Original");
        assertThat(getData.get("nationalityCode").asText()).isEqualTo("US");
        assertThat(getData.get("format").asText()).isEqualTo("MOVIE");
        assertThat(getData.get("productionStyle").asText()).isEqualTo("LIVE_ACTION");
        assertThat(getData.get("isNsfw").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("ROA-11: Atomicity proof: exactly 1 Film row and 1 Vault entry with matching ID, no orphan relations")
    void filmCreationProvesAtomicityAndDatabaseIntegrity() throws Exception {
        String token = bearerToken();

        String rawResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Atomic Film\",\"originalTitle\":\"Native\",\"nationalityCode\":\"VN\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"isNsfw\":false}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long filmId = objectMapper.readTree(rawResponse).path("data").path("id").asLong();

        // 1. Exactly 1 row in films table
        Integer filmCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM films WHERE id = ?", Integer.class, filmId);
        assertThat(filmCount).isEqualTo(1);

        Boolean isNsfw = jdbcTemplate.queryForObject("SELECT is_nsfw FROM films WHERE id = ?", Boolean.class, filmId);
        assertThat(isNsfw).isFalse();

        // 2. Exactly 1 row in vault_entries table with entry_type = 'FILM' and deleted_at is null
        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE id = ? AND entry_type = 'FILM' AND deleted_at IS NULL",
                Integer.class, filmId);
        assertThat(vaultCount).isEqualTo(1);

        // 3. Exactly 0 relationship rows
        Integer genreCount = jdbcTemplate.queryForObject("SELECT count(*) FROM film_genre_assignments WHERE film_id = ?", Integer.class, filmId);
        assertThat(genreCount).isZero();

        Integer creditCount = jdbcTemplate.queryForObject("SELECT count(*) FROM film_credits WHERE film_id = ?", Integer.class, filmId);
        assertThat(creditCount).isZero();

        Integer linkCount = jdbcTemplate.queryForObject("SELECT count(*) FROM film_links WHERE film_id = ?", Integer.class, filmId);
        assertThat(linkCount).isZero();
    }

    @Test
    @DisplayName("ROA-11: Observer barrier: uncommitted rows are invisible to independent transaction until commit")
    void concurrencyObserverBarrierDuringUncommittedState() throws Exception {
        String token = bearerToken();

        CountDownLatch vaultCreated = new CountDownLatch(1);
        CountDownLatch observerVerified = new CountDownLatch(1);
        AtomicLong createdVaultId = new AtomicLong();

        doAnswer(inv -> {
            var view = (com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView) inv.callRealMethod();
            createdVaultId.set(view.id());
            vaultCreated.countDown();
            assertThat(observerVerified.await(5, TimeUnit.SECONDS)).isTrue();
            return view;
        }).when(vaultEntryOperations).create(VaultEntryType.FILM);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> createFuture = executor.submit(() -> mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Barrier Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString());

            assertThat(vaultCreated.await(5, TimeUnit.SECONDS)).isTrue();
            Long id = createdVaultId.get();
            assertThat(id).isPositive();

            // Independent transaction checks visibility of uncommitted state
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                Integer vCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries WHERE id = ?", Integer.class, id);
                assertThat(vCount).as("Uncommitted vault entry must not be visible to independent transaction").isZero();

                Integer fCount = jdbcTemplate.queryForObject("SELECT count(*) FROM films WHERE id = ?", Integer.class, id);
                assertThat(fCount).as("Uncommitted film must not be visible to independent transaction").isZero();
            });

            // Signal writer to proceed and commit
            observerVerified.countDown();

            String responseRaw = createFuture.get(5, TimeUnit.SECONDS);
            Long responseId = objectMapper.readTree(responseRaw).path("data").path("id").asLong();
            assertThat(responseId).isEqualTo(id);

            // After commit: rows ARE visible
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                Integer vCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries WHERE id = ?", Integer.class, id);
                assertThat(vCount).isEqualTo(1);

                Integer fCount = jdbcTemplate.queryForObject("SELECT count(*) FROM films WHERE id = ?", Integer.class, id);
                assertThat(fCount).isEqualTo(1);
            });

        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("ROA-11: SQL insertion failure rolls back completely leaving zero orphan rows and returns 500")
    void sqlInsertionFailureRollsBackCompletely() throws Exception {
        String token = bearerToken();

        // Install PostgreSQL trigger failing on title = 'TRIGGER_FAIL'
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION trg_fail_sql_insert() RETURNS trigger AS $$
                BEGIN
                    IF NEW.title = 'TRIGGER_FAIL' THEN
                        RAISE EXCEPTION 'Simulated SQL insert failure';
                    END IF;
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;

                CREATE TRIGGER trg_test_film_insert
                BEFORE INSERT ON films
                FOR EACH ROW
                EXECUTE FUNCTION trg_fail_sql_insert();
                """);

        int vaultEntriesBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int filmsBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Integer.class);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"TRIGGER_FAIL\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));

        int vaultEntriesAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int filmsAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Integer.class);

        assertThat(vaultEntriesAfter).as("Vault entries must be completely rolled back, leaving zero orphan rows").isEqualTo(vaultEntriesBefore);
        assertThat(filmsAfter).as("Films table must have zero added rows").isEqualTo(filmsBefore);

        Integer orphanVaultEntries = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'FILM' AND id NOT IN (SELECT id FROM films)", Integer.class);
        assertThat(orphanVaultEntries).isZero();
    }

    @Test
    @DisplayName("ROA-11: Deferred commit failure rolls back completely leaving zero orphan rows and returns 500")
    void deferredCommitFailureRollsBackCompletely() throws Exception {
        String token = bearerToken();

        // Install deferrable constraint trigger executed at commit time
        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION trg_fail_deferred_commit() RETURNS trigger AS $$
                BEGIN
                    IF NEW.title = 'COMMIT_FAIL' THEN
                        RAISE EXCEPTION 'Simulated deferred commit failure';
                    END IF;
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;

                CREATE CONSTRAINT TRIGGER trg_test_film_commit
                AFTER INSERT ON films
                DEFERRABLE INITIALLY DEFERRED
                FOR EACH ROW
                EXECUTE FUNCTION trg_fail_deferred_commit();
                """);

        int vaultEntriesBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int filmsBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Integer.class);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"COMMIT_FAIL\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));

        int vaultEntriesAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        int filmsAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Integer.class);

        assertThat(vaultEntriesAfter).as("Vault entries must be completely rolled back on deferred commit failure").isEqualTo(vaultEntriesBefore);
        assertThat(filmsAfter).as("Films count must be unchanged").isEqualTo(filmsBefore);

        Integer orphanVaultEntries = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'FILM' AND id NOT IN (SELECT id FROM films)", Integer.class);
        assertThat(orphanVaultEntries).isZero();
    }

    @Test
    @DisplayName("ROA-08: D3 admission race: in-flight request admitted before revocation completes once, fresh request with revoked token fails 401")
    void d3AdmissionRaceInFlightCreateDuringRevocation() throws Exception {
        String token = bearerToken();
        java.util.UUID sessionId = java.util.UUID.fromString(jwtDecoder.decode(token).getClaimAsString("sid"));

        CountDownLatch admittedLatch = new CountDownLatch(1);
        CountDownLatch revocationLatch = new CountDownLatch(1);

        doAnswer(inv -> {
            admittedLatch.countDown();
            assertThat(revocationLatch.await(5, TimeUnit.SECONDS)).isTrue();
            return inv.callRealMethod();
        }).when(vaultEntryOperations).create(VaultEntryType.FILM);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> inFlightCreateFuture = executor.submit(() -> mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Admitted Before Revocation\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString());

            assertThat(admittedLatch.await(5, TimeUnit.SECONDS)).isTrue();

            // Revoke the session in the database
            jdbcTemplate.update("UPDATE ppv_login_sessions SET revoked_at = now() WHERE id = ?", sessionId);

            // Release the in-flight create request
            revocationLatch.countDown();

            String responseRaw = inFlightCreateFuture.get(5, TimeUnit.SECONDS);
            Long createdId = objectMapper.readTree(responseRaw).path("data").path("id").asLong();
            assertThat(createdId).isPositive();

            // Subsequent fresh request using the revoked token is rejected with 401 AUTHENTICATION_REQUIRED
            var snapshotBeforeFresh = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
            reset(vaultEntryOperations);

            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Fresh Revoked Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
                    .andExpect(jsonPath("$.error.message").value("Full authentication is required to access this resource"));

            var snapshotAfterFresh = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
            assertThat(snapshotAfterFresh).isEqualTo(snapshotBeforeFresh);
            verifyNoInteractions(vaultEntryOperations);

        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("ROA-08: D3 admission race: request paused before authority snapshot is invalidated fails 401 without mutation")
    void d3AdmissionRacePauseBeforeAuthoritySnapshot() throws Exception {
        String token = publicBearerToken();
        java.util.UUID sessionId = java.util.UUID.fromString(jwtDecoder.decode(token).getClaimAsString("sid"));

        CountDownLatch beforeLookupLatch = new CountDownLatch(1);
        CountDownLatch invalidationLatch = new CountDownLatch(1);

        doAnswer(inv -> {
            beforeLookupLatch.countDown();
            assertThat(invalidationLatch.await(5, TimeUnit.SECONDS)).isTrue();
            return inv.callRealMethod();
        }).when(sessionAuthorityService).findAuthoritySnapshot(any());

        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> responseStatusFuture = executor.submit(() -> mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Denied Before Authority\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
                    .andExpect(jsonPath("$.error.message").value("Full authentication is required to access this resource"))
                    .andReturn().getResponse().getStatus());

            assertThat(beforeLookupLatch.await(5, TimeUnit.SECONDS)).isTrue();

            // Revoke the session before authority lookup returns
            jdbcTemplate.update("UPDATE ppv_login_sessions SET revoked_at = now() WHERE id = ?", sessionId);

            invalidationLatch.countDown();

            int status = responseStatusFuture.get(5, TimeUnit.SECONDS);
            assertThat(status).isEqualTo(401);

            var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
            assertThat(snapshotAfter).isEqualTo(snapshotBefore);
            verifyNoInteractions(vaultEntryOperations);
        } finally {
            reset(sessionAuthorityService);
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("ROA-08: Authority lookup unavailable fails closed without handler execution or mutation; restored PRIVATE succeeds 201, PUBLIC denied 403")
    void d3AuthorityLookupUnavailableFailsClosed() throws Exception {
        String token = publicBearerToken();

        doAnswer(inv -> {
            throw new org.springframework.dao.DataAccessResourceFailureException("Simulated authority DB connection failure");
        }).when(sessionAuthorityService).findAuthoritySnapshot(any());

        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        reset(vaultEntryOperations);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Unavailable Authority Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.error.message").value("Full authentication is required to access this resource"));

        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
        verifyNoInteractions(vaultEntryOperations);

        // Restore normal operation
        reset(sessionAuthorityService);

        // Restored: PRIVATE token succeeds with 201 Created
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Restored Authority Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Restored Authority Film"));

        // Restored: PUBLIC token denied with 403 ACCESS_DENIED
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Restored Authority Film Public\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }
}
