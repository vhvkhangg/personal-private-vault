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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
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
    }

    @Test
    @DisplayName("W1-01: Public token from profiles login creates film with exact 7 keys, server isNsfw=false, no-store, and no Location")
    void publicTokenFromProfilesLoginCreatesFilmWithExactSevenKeysAndNoStore() throws Exception {
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

        String rawResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.meta").isEmpty())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode data = root.path("data");
        assertThat(data.isObject()).isTrue();

        List<String> fieldNames = new ArrayList<>();
        data.fieldNames().forEachRemaining(fieldNames::add);
        assertThat(fieldNames).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "format", "productionStyle", "isNsfw"
        );

        assertThat(data.get("id").asLong()).isPositive();
        assertThat(data.get("title").asText()).isEqualTo("Inception");
        assertThat(data.get("originalTitle").asText()).isEqualTo("Inception Original");
        assertThat(data.get("nationalityCode").asText()).isEqualTo("US");
        assertThat(data.get("format").asText()).isEqualTo("MOVIE");
        assertThat(data.get("productionStyle").asText()).isEqualTo("LIVE_ACTION");
        assertThat(data.get("isNsfw").asBoolean()).isFalse();

        // Sensitive/private fields must not exist
        assertThat(data.has("description")).isFalse();
        assertThat(data.has("posterUrl")).isFalse();
        assertThat(data.has("directorPersonId")).isFalse();
        assertThat(data.has("review")).isFalse();
        assertThat(data.has("totalEpisodes")).isFalse();
        assertThat(data.has("progressStatus")).isFalse();
        assertThat(data.has("consumptionStatus")).isFalse();
        assertThat(data.has("currentProgressText")).isFalse();
        assertThat(data.has("genreIds")).isFalse();
        assertThat(data.has("storyArchetypeIds")).isFalse();
        assertThat(data.has("worldSettingIds")).isFalse();
    }

    @Test
    @DisplayName("W1-02: Public token from downgrade creates film with exact 7 keys, nullable optionals, and no-store")
    void publicTokenFromDowngradeCreatesFilmWithExactSevenKeysAndNoStore() throws Exception {
        // Ensure public credential exists on owner account before downgrade
        publicBearerToken();
        String token = publicBearerTokenViaDowngrade();

        String payload = """
                {
                    "title": "Spirited Away",
                    "originalTitle": null,
                    "nationalityCode": null,
                    "format": "MOVIE",
                    "productionStyle": "ANIMATION"
                }
                """;

        String rawResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode data = root.path("data");

        List<String> fieldNames = new ArrayList<>();
        data.fieldNames().forEachRemaining(fieldNames::add);
        assertThat(fieldNames).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "format", "productionStyle", "isNsfw"
        );

        assertThat(data.get("title").asText()).isEqualTo("Spirited Away");
        assertThat(data.get("originalTitle").isNull()).isTrue();
        assertThat(data.get("nationalityCode").isNull()).isTrue();
        assertThat(data.get("format").asText()).isEqualTo("MOVIE");
        assertThat(data.get("productionStyle").asText()).isEqualTo("ANIMATION");
        assertThat(data.get("isNsfw").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("W1-03: Anonymous request to create film is rejected with 401 AUTHENTICATION_REQUIRED")
    void anonymousRequestToCreateFilmReturns401() throws Exception {
        String payload = """
                {
                    "title": "Anon Film",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION"
                }
                """;

        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("W1-04: Strict parser duplicate keys rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsDuplicateKeys() throws Exception {
        String token = publicBearerToken();

        String rawDuplicateKey = "{\"title\":\"First Title\",\"title\":\"Duplicate Title\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}";

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawDuplicateKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
    }

    @Test
    @DisplayName("W1-05: Strict parser Unicode escape duplicate keys rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsUnicodeEscapeDuplicateKeys() throws Exception {
        String token = publicBearerToken();

        // Raw bytes with escaped "title": \u0074\u0069\u0074\u006c\u0065
        String rawEscapedDuplicate = "{\"title\":\"Original\",\"\\u0074\\u0069\\u0074\\u006c\\u0065\":\"Escaped Duplicate\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}";

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawEscapedDuplicate.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
    }

    @Test
    @DisplayName("W1-06: Strict parser trailing tokens rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsTrailingTokens() throws Exception {
        String token = publicBearerToken();

        String rawTrailing = "{\"title\":\"Valid Title\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"} trailing_junk";

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawTrailing.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
    }

    @Test
    @DisplayName("W1-07: Strict parser non-object root rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsNonObjectRoot() throws Exception {
        String token = publicBearerToken();

        List<String> invalidRoots = List.of(
                "[]",
                "[{\"title\":\"Array Item\"}]",
                "\"just a string\"",
                "12345",
                "true",
                "null",
                "   "
        );

        for (String payload : invalidRoots) {
            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
        }
    }

    @Test
    @DisplayName("W1-08: Strict parser unknown and forbidden properties rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsUnknownAndForbiddenProperties() throws Exception {
        String token = publicBearerToken();

        List<String> forbiddenBodies = List.of(
                // isNsfw cannot be client-supplied
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"isNsfw\":false}",
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"isNsfw\":true}",
                // directorPersonId forbidden
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"directorPersonId\":1}",
                // description forbidden
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"description\":\"Some text\"}",
                // posterUrl forbidden
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"posterUrl\":\"https://cdn.example.com/p.png\"}",
                // review forbidden
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"review\":\"Great movie\"}",
                // genreIds forbidden
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"genreIds\":[1]}",
                // unknown property
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"customProp\":\"val\"}"
        );

        for (String body : forbiddenBodies) {
            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
        }
    }

    @Test
    @DisplayName("W1-09: Strict parser scalar coercion rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsScalarCoercion() throws Exception {
        String token = publicBearerToken();

        List<String> coercedBodies = List.of(
                // number for title
                "{\"title\":12345,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // boolean for title
                "{\"title\":true,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // number for nationalityCode
                "{\"title\":\"Valid\",\"nationalityCode\":84,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // boolean for nationalityCode
                "{\"title\":\"Valid\",\"nationalityCode\":false,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // number for originalTitle
                "{\"title\":\"Valid\",\"originalTitle\":999,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}",
                // boolean for originalTitle
                "{\"title\":\"Valid\",\"originalTitle\":true,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"
        );

        for (String body : coercedBodies) {
            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
        }
    }

    @Test
    @DisplayName("W1-10: Strict parser enum ordinals, lowercase, and invalid enums rejected with 400 MALFORMED_REQUEST and no-store")
    void strictParserRejectsEnumOrdinalsAndInvalidEnums() throws Exception {
        String token = publicBearerToken();

        List<String> invalidEnumBodies = List.of(
                // numeric ordinal for format
                "{\"title\":\"A\",\"format\":0,\"productionStyle\":\"LIVE_ACTION\"}",
                // numeric ordinal for productionStyle
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":1}",
                // lowercase format
                "{\"title\":\"A\",\"format\":\"movie\",\"productionStyle\":\"LIVE_ACTION\"}",
                // lowercase productionStyle
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"live_action\"}",
                // invalid format name
                "{\"title\":\"A\",\"format\":\"DOCUMENTARY\",\"productionStyle\":\"LIVE_ACTION\"}",
                // invalid productionStyle name
                "{\"title\":\"A\",\"format\":\"MOVIE\",\"productionStyle\":\"CGI\"}"
        );

        for (String body : invalidEnumBodies) {
            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
        }
    }

    @Test
    @DisplayName("W1-11: Bean Validation rejects blank title and length violations with 400 VALIDATION_ERROR and fieldErrors")
    void beanValidationRejectsBlankTitleAndLengthViolations() throws Exception {
        String token = publicBearerToken();

        // 1. Blank title
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("title"));

        // 2. Title exceeding 500 characters
        String longTitle = "A".repeat(501);
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("title"));

        // 3. NationalityCode exceeding 2 characters
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A\",\"nationalityCode\":\"USA\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("nationalityCode"));

        // 4. OriginalTitle exceeding 500 characters
        String longOriginalTitle = "B".repeat(501);
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A\",\"originalTitle\":\"" + longOriginalTitle + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("originalTitle"));
    }

    @Test
    @DisplayName("W1-12: Domain validation rejects non-existent nationalityCode with 422 FILM_INVALID and no-store")
    void domainValidationRejectsInvalidNationalityCode() throws Exception {
        String token = publicBearerToken();

        // "ZZ" is a valid 2-letter string, but not present in reference countries table
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A\",\"nationalityCode\":\"ZZ\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FILM_INVALID"));
    }

    @Test
    @DisplayName("W1-13: Atomicity proof: exactly 1 Film row and 1 Vault entry with matching ID, no orphan relations")
    void filmCreationProvesAtomicityAndDatabaseIntegrity() throws Exception {
        String token = publicBearerToken();

        String rawResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Atomic Film\",\"originalTitle\":\"Native\",\"nationalityCode\":\"VN\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long filmId = objectMapper.readTree(rawResponse).path("data").path("id").asLong();

        // 1. Exactly 1 row in films table
        Integer filmCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM films WHERE id = ?", Integer.class, filmId);
        assertThat(filmCount).isEqualTo(1);

        // Verify server defaults and nulls in PostgreSQL
        Boolean isNsfw = jdbcTemplate.queryForObject("SELECT is_nsfw FROM films WHERE id = ?", Boolean.class, filmId);
        assertThat(isNsfw).isFalse();

        Long directorPersonId = jdbcTemplate.queryForObject("SELECT director_person_id FROM films WHERE id = ?", Long.class, filmId);
        assertThat(directorPersonId).isNull();

        String description = jdbcTemplate.queryForObject("SELECT description FROM films WHERE id = ?", String.class, filmId);
        assertThat(description).isNull();

        String posterUrl = jdbcTemplate.queryForObject("SELECT poster_url FROM films WHERE id = ?", String.class, filmId);
        assertThat(posterUrl).isNull();

        String review = jdbcTemplate.queryForObject("SELECT review FROM films WHERE id = ?", String.class, filmId);
        assertThat(review).isNull();

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
    @DisplayName("W1-14a: Observer barrier: uncommitted rows are invisible to independent transaction until commit")
    void concurrencyObserverBarrierDuringUncommittedState() throws Exception {
        String token = publicBearerToken();

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
    @DisplayName("W1-14b: SQL insertion failure rolls back completely leaving zero orphan rows and returns 500 with no-store")
    void sqlInsertionFailureRollsBackCompletely() throws Exception {
        String token = publicBearerToken();

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
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
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
    @DisplayName("W1-14c: Deferred commit failure rolls back completely leaving zero orphan rows and returns 500 with no-store")
    void deferredCommitFailureRollsBackCompletely() throws Exception {
        String token = publicBearerToken();

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
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
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
    @DisplayName("W1-15: Follow-up B1 GET on created film succeeds with identical 7 keys and no-store")
    void followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys() throws Exception {
        String token = publicBearerToken();

        String createPayload = """
                {
                    "title": "Interstellar",
                    "originalTitle": "Interstellar Original",
                    "nationalityCode": "US",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION"
                }
                """;

        String createResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();

        Long filmId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // Perform B1 GET on created ID
        String getResponse = mockMvc.perform(get("/api/v1/films/{id}", filmId)
                        .header("Authorization", "Bearer " + token))
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
    @DisplayName("W1-16: D3 admission race: in-flight request admitted before elevation completes once, fresh request with old token fails 401")
    void d3AdmissionRaceInFlightCreateDuringElevation() throws Exception {
        String token = publicBearerToken();

        CountDownLatch admittedLatch = new CountDownLatch(1);
        CountDownLatch elevationLatch = new CountDownLatch(1);

        doAnswer(inv -> {
            admittedLatch.countDown();
            assertThat(elevationLatch.await(5, TimeUnit.SECONDS)).isTrue();
            return inv.callRealMethod();
        }).when(vaultEntryOperations).create(VaultEntryType.FILM);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> inFlightCreateFuture = executor.submit(() -> mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Admitted InFlight Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString());

            assertThat(admittedLatch.await(5, TimeUnit.SECONDS)).isTrue();

            // While in-flight create is held after security admission, elevate session to PRIVATE
            mockMvc.perform(post("/api/v1/auth/sessions/elevate-private")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"privatePassword\":\"" + DEFAULT_OWNER_PASSWORD + "\"}"))
                    .andExpect(status().isOk());

            // Release the in-flight create request
            elevationLatch.countDown();

            String responseRaw = inFlightCreateFuture.get(5, TimeUnit.SECONDS);
            Long createdId = objectMapper.readTree(responseRaw).path("data").path("id").asLong();
            assertThat(createdId).isPositive();

            // Subsequent fresh request using the OLD public token is rejected with 401 AUTHENTICATION_REQUIRED
            mockMvc.perform(post("/api/v1/films")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Fresh Disallowed Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("W1-07b: PRIVATE profile creation retains full 18-key response, relations, and standard cache behavior")
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

        String rawResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privatePayload))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andReturn().getResponse().getContentAsString();

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
    }

    @Test
    @DisplayName("W1-12: D3 admission race: in-flight request admitted before revocation completes once, fresh request with revoked token fails 401")
    void d3AdmissionRaceInFlightCreateDuringRevocation() throws Exception {
        String token = publicBearerToken();
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
            org.mockito.Mockito.verifyNoInteractions(vaultEntryOperations);

        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("W1-12: D3 admission race: request paused before authority snapshot is invalidated fails 401 without mutation")
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
            org.mockito.Mockito.verifyNoInteractions(vaultEntryOperations);
        } finally {
            reset(sessionAuthorityService);
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("W1-10: Authority lookup unavailable fails closed without handler execution or mutation")
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
        org.mockito.Mockito.verifyNoInteractions(vaultEntryOperations);

        // Restore normal operation
        reset(sessionAuthorityService);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Restored Authority Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Restored Authority Film"));
    }

    @Test
    @DisplayName("W1-06: Strict boundary tests for required/optional fields, length bounds, whitespace padding, and zero mutation")
    void publicBoundaryTestingRequiredAndOptionalFields() throws Exception {
        String token = publicBearerToken();

        long filmsBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Long.class);
        long vaultBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);

        // a) Missing required fields
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid Title\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid Title\",\"format\":\"MOVIE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // b) Explicit nulls for required fields
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid Title\",\"format\":null,\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Valid Title\",\"format\":\"MOVIE\",\"productionStyle\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // c) Blank title
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // d) Exact 500 length title accepted, 501 rejected
        String title500 = "T".repeat(500);
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title500 + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(title500));

        String title501 = "T".repeat(501);
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title501 + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // e) Original title bounds: 500 accepted, 501 rejected
        String ot500 = "O".repeat(500);
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title With OT500\",\"originalTitle\":\"" + ot500 + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originalTitle").value(ot500));

        String ot501 = "O".repeat(501);
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title With OT501\",\"originalTitle\":\"" + ot501 + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // f) Nationality code: 1 char nonblank "V" fails 422 FILM_INVALID (catalog validation), 3 chars fails @Size(max=2) 400 VALIDATION_ERROR, 2 chars unknown fails 422, 2 chars lowercase valid normalized to 201
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"One Char Country\",\"nationalityCode\":\"V\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("FILM_INVALID"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Three Char Country\",\"nationalityCode\":\"VNN\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Space Country\",\"nationalityCode\":\" VN\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Unknown Catalog Country\",\"nationalityCode\":\"ZZ\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("FILM_INVALID"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Normalized Lowercase Country\",\"nationalityCode\":\"vn\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nationalityCode").value("VN"));

        // g) Padded whitespace around JSON syntax
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("  \n\t  {\"title\":\"Padded Syntax Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}  \n\t  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Padded Syntax Film"));

        // h) Omitted/null optional fields emit 7 keys with nulls
        String nullsResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nulls Optional Film\",\"originalTitle\":null,\"nationalityCode\":null,\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode nullsData = objectMapper.readTree(nullsResponse).path("data");
        assertThat(nullsData.get("originalTitle").isNull()).isTrue();
        assertThat(nullsData.get("nationalityCode").isNull()).isTrue();
        assertThat(nullsData.size()).isEqualTo(7);

        // i) Optional empty/blank values normalized to null
        String blankOtResp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blank OT Film\",\"originalTitle\":\"\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(blankOtResp).path("data").get("originalTitle").isNull()).isTrue();

        String wsOtResp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Whitespace OT Film\",\"originalTitle\":\"   \",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(wsOtResp).path("data").get("originalTitle").isNull()).isTrue();

        String blankNatResp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blank Country Film\",\"nationalityCode\":\"\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(blankNatResp).path("data").get("nationalityCode").isNull()).isTrue();

        String wsNatResp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Whitespace Country Film\",\"nationalityCode\":\"  \",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(wsNatResp).path("data").get("nationalityCode").isNull()).isTrue();

        // j) Padded raw originalTitle overflow before trimming rejected with 400 VALIDATION_ERROR
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Padded Raw OT Over\",\"originalTitle\":\"   " + "O".repeat(500) + "\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Padded Raw OT Trailing\",\"originalTitle\":\"" + "O".repeat(500) + " \",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // k) Forbidden null/empty options rejected with 400 MALFORMED_REQUEST
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Forbidden isNsfw False\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"isNsfw\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Forbidden isNsfw Null\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"isNsfw\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Forbidden Director Null\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"directorPersonId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Forbidden Genre Empty Array\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"genreIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Forbidden Description Null\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\",\"description\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        // Verify that the negative test cases caused exactly zero extra mutations beyond the 9 successful creates
        long filmsAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM films", Long.class);
        long vaultAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        assertThat(filmsAfter - filmsBefore).isEqualTo(9L);
        assertThat(vaultAfter - vaultBefore).isEqualTo(9L);
    }

    @Test
    @DisplayName("W1-07g: Public create without Content-Type returns 415 and makes zero database mutations")
    void publicMissingContentTypeReturns415() throws Exception {
        String token = publicBearerToken();
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .content("{\"title\":\"No CT Public Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));

        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("W1-07h: Public create with text/plain Content-Type returns 415 and makes zero database mutations")
    void publicTextPlainContentTypeReturns415() throws Exception {
        String token = publicBearerToken();
        var snapshotBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{\"title\":\"Text Plain Public Film\",\"format\":\"MOVIE\",\"productionStyle\":\"LIVE_ACTION\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));

        var snapshotAfter = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        assertThat(snapshotAfter).isEqualTo(snapshotBefore);
    }

    @Test
    @DisplayName("W1-07i: Private create without Content-Type returns 415 and makes zero database mutations")
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
    @DisplayName("W1-07c: PRIVATE profile accepts vendor JSON (application/vnd.ppv+json)")
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
    @DisplayName("W1-07d: PRIVATE profile decodes ISO-8859-1 charset correctly")
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
    @DisplayName("W1-07e: PRIVATE profile rejects unknown property with 400 MALFORMED_REQUEST and no mutation")
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
    @DisplayName("W1-07f: PRIVATE profile rejects unsupported media type (text/plain) with 415")
    void privateUnsupportedMediaTypeReturns415() throws Exception {
        String token = bearerToken();
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{\"title\":\"Plain Text Film\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
