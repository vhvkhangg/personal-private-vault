package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.internal.domain.Film;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreAssignmentRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.PublicFilmDetailResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.mapper.FilmWebMapper;
import com.vhvkhangg.personalprivatevault.film.view.PublicFilmDetailView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicFilmReadIntegrationTest extends AbstractWebIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FilmOperations filmOperations;

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private FilmGenreRepository filmGenreRepository;

    @Autowired
    private FilmGenreAssignmentRepository filmGenreAssignmentRepository;

    @Autowired
    private FilmStoryArchetypeRepository storyArchetypeRepository;

    @Autowired
    private FilmWorldSettingRepository worldSettingRepository;

    @MockitoSpyBean
    private PersonOperations personOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    private VaultEntryOperations vaultEntryOperations;

    private Long seedFilm(boolean isNsfw, boolean trashed) {
        Long id = System.nanoTime();
        Long personId = id + 1_000_000L;
        jdbcTemplate.update("INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at) VALUES (?, 'PERSON', null, now(), now()) ON CONFLICT DO NOTHING",
                personId);
        jdbcTemplate.update("INSERT INTO persons(id, name) VALUES (?, ?) ON CONFLICT DO NOTHING",
                personId, "Director-" + personId);
        jdbcTemplate.update("""
                INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at)
                VALUES (?, 'FILM', null, now(), now())
                """, id);
        jdbcTemplate.update("""
                INSERT INTO films(id, title, original_title, nationality_code, poster_url, format, production_style,
                                  is_nsfw, director_person_id, description, total_episodes, progress_status,
                                  consumption_status, current_progress_text, review)
                VALUES (?, ?, ?, ?, ?, 'MOVIE', 'LIVE_ACTION', ?, ?, ?, 1, 'COMPLETED', 'CONSUMED', 'Full movie', 'Film review')
                """, id, "FilmTitle-" + id, "NativeFilm-" + id, "VN", "https://secret.cdn/film.png", isNsfw, personId, "Film synopsis");
        if (trashed) {
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", id);
        }
        return id;
    }

    private Long seedFilmWithClassifications(boolean isNsfw, boolean trashed) {
        Long id = seedFilm(isNsfw, trashed);
        Long genreId = id + 2_000_000L;
        Long archId = id + 3_000_000L;
        Long wsId = id + 4_000_000L;
        jdbcTemplate.update("INSERT INTO film_genres (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", genreId, "Genre-" + genreId, "Film Genre Description");
        jdbcTemplate.update("INSERT INTO story_archetypes (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", archId, "Arch-" + archId, "Archetype Description");
        jdbcTemplate.update("INSERT INTO world_settings (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", wsId, "Setting-" + wsId, "Setting Description");
        jdbcTemplate.update("INSERT INTO film_genre_assignments (film_id, genre_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, genreId);
        jdbcTemplate.update("INSERT INTO film_story_archetypes (film_id, story_archetype_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, archId);
        jdbcTemplate.update("INSERT INTO film_world_settings (film_id, world_setting_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, wsId);
        return id;
    }

    private Long seedFilmWithoutDirector(boolean isNsfw, boolean trashed) {
        Long id = System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at)
                VALUES (?, 'FILM', null, now(), now())
                """, id);
        jdbcTemplate.update("""
                INSERT INTO films(id, title, original_title, nationality_code, poster_url, format, production_style,
                                  is_nsfw, director_person_id, description, total_episodes, progress_status,
                                  consumption_status, current_progress_text, review)
                VALUES (?, ?, ?, ?, ?, 'SERIES', 'ANIMATION', ?, null, ?, 24, 'ONGOING', 'UNCONSUMED', 'Episode 1', 'Anime review')
                """, id, "SeriesTitle-" + id, "NativeSeries-" + id, "US", "https://secret.cdn/anime.png", isNsfw, "Anime synopsis");
        if (trashed) {
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", id);
        }
        return id;
    }

    @Test
    @DisplayName("ROB-02: Both password-login and downgrade persisted PUBLIC sessions reach GET /api/v1/films/{id}")
    void publicSessionsReachFilmDetail() throws Exception {
        Long id = seedFilm(false, false);
        String token1 = publicBearerToken();
        String token2 = publicBearerTokenViaDowngrade();

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.directorName").value("Director-" + (id + 1_000_000L)))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.directorName").value("Director-" + (id + 1_000_000L)))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
    }

    @Test
    @DisplayName("ROB-04, ROB-05, ROB-08: Populated Film 200 has exact 19 keys and full parity with PRIVATE 18 keys; PRIVATE writes verify 18 keys (FR-ROB-04)")
    void eligibleFilmHasExactlyNineteenKeysAndResolvedDirector() throws Exception {
        // 1. Populated director work with classifications
        Long id = seedFilmWithClassifications(false, false);
        String pubToken = publicBearerToken();

        String pubBody = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var pubJson = objectMapper.readTree(pubBody);
        assertThat(pubJson.path("error").isNull()).isTrue();
        assertThat(pubJson.path("meta").isNull()).isTrue();

        var pubData = pubJson.path("data");
        List<String> pubFields = new ArrayList<>();
        pubData.fieldNames().forEachRemaining(pubFields::add);
        assertThat(pubFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "productionStyle", "isNsfw",
                "directorPersonId", "description", "totalEpisodes", "progressStatus", "consumptionStatus",
                "currentProgressText", "review", "genreIds", "storyArchetypeIds", "worldSettingIds", "directorName"
        );
        assertThat(pubData.path("id").asLong()).isEqualTo(id);
        assertThat(pubData.path("title").asText()).isEqualTo("FilmTitle-" + id);
        assertThat(pubData.path("originalTitle").asText()).isEqualTo("NativeFilm-" + id);
        assertThat(pubData.path("nationalityCode").asText()).isEqualTo("VN");
        assertThat(pubData.path("posterUrl").asText()).isEqualTo("https://secret.cdn/film.png");
        assertThat(pubData.path("format").asText()).isEqualTo("MOVIE");
        assertThat(pubData.path("productionStyle").asText()).isEqualTo("LIVE_ACTION");
        assertThat(pubData.path("isNsfw").asBoolean()).isFalse();
        assertThat(pubData.path("directorPersonId").asLong()).isEqualTo(id + 1_000_000L);
        assertThat(pubData.path("description").asText()).isEqualTo("Film synopsis");
        assertThat(pubData.path("totalEpisodes").asInt()).isEqualTo(1);
        assertThat(pubData.path("progressStatus").asText()).isEqualTo("COMPLETED");
        assertThat(pubData.path("consumptionStatus").asText()).isEqualTo("CONSUMED");
        assertThat(pubData.path("currentProgressText").asText()).isEqualTo("Full movie");
        assertThat(pubData.path("review").asText()).isEqualTo("Film review");
        assertThat(pubData.path("genreIds")).isNotEmpty();
        assertThat(pubData.path("storyArchetypeIds")).isNotEmpty();
        assertThat(pubData.path("worldSettingIds")).isNotEmpty();
        assertThat(pubData.path("directorName").asText()).isEqualTo("Director-" + (id + 1_000_000L));

        // PRIVATE request on the same work returns exactly 18 keys (NO directorName)
        String privBody = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var privData = objectMapper.readTree(privBody).path("data");
        List<String> privFields = new ArrayList<>();
        privData.fieldNames().forEachRemaining(privFields::add);
        assertThat(privFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "productionStyle", "isNsfw",
                "directorPersonId", "description", "totalEpisodes", "progressStatus", "consumptionStatus",
                "currentProgressText", "review", "genreIds", "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(privData.has("directorName")).isFalse();

        // FR-ROB-04: Full 18 common values parity check on populated fixture
        for (String commonField : privFields) {
            assertThat(pubData.path(commonField))
                    .as("Parity mismatch on field '%s'", commonField)
                    .isEqualTo(privData.path(commonField));
        }

        // 2. Work without director: 19 keys, directorPersonId null, directorName null
        Long noDirectorId = seedFilmWithoutDirector(false, false);
        String noDirectorBody = mockMvc.perform(get("/api/v1/films/{id}", noDirectorId).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var noDirectorData = objectMapper.readTree(noDirectorBody).path("data");
        List<String> noDirFields = new ArrayList<>();
        noDirectorData.fieldNames().forEachRemaining(noDirFields::add);
        assertThat(noDirFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "productionStyle", "isNsfw",
                "directorPersonId", "description", "totalEpisodes", "progressStatus", "consumptionStatus",
                "currentProgressText", "review", "genreIds", "storyArchetypeIds", "worldSettingIds", "directorName"
        );
        assertThat(noDirectorData.path("directorPersonId").isNull()).isTrue();
        assertThat(noDirectorData.path("directorName").isNull()).isTrue();

        // 3. Null optional fields: all 19 keys still emitted with explicit null
        Long nullFieldsId = System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at)
                VALUES (?, 'FILM', null, now(), now())
                """, nullFieldsId);
        jdbcTemplate.update("""
                INSERT INTO films(id, title, original_title, nationality_code, poster_url, format, production_style,
                                  is_nsfw, director_person_id, description, total_episodes, progress_status,
                                  consumption_status, current_progress_text, review)
                VALUES (?, 'Null Film', null, null, null, 'MOVIE', 'LIVE_ACTION', false, null, null, null, 'ONGOING', 'UNCONSUMED', null, null)
                """, nullFieldsId);

        String nullBody = mockMvc.perform(get("/api/v1/films/{id}", nullFieldsId).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var nullJson = objectMapper.readTree(nullBody).path("data");
        List<String> nullFields = new ArrayList<>();
        nullJson.fieldNames().forEachRemaining(nullFields::add);
        assertThat(nullFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "productionStyle", "isNsfw",
                "directorPersonId", "description", "totalEpisodes", "progressStatus", "consumptionStatus",
                "currentProgressText", "review", "genreIds", "storyArchetypeIds", "worldSettingIds", "directorName"
        );
        assertThat(nullJson.path("originalTitle").isNull()).isTrue();
        assertThat(nullJson.path("nationalityCode").isNull()).isTrue();
        assertThat(nullJson.path("posterUrl").isNull()).isTrue();
        assertThat(nullJson.path("directorPersonId").isNull()).isTrue();
        assertThat(nullJson.path("description").isNull()).isTrue();
        assertThat(nullJson.path("totalEpisodes").isNull()).isTrue();
        assertThat(nullJson.path("currentProgressText").isNull()).isTrue();
        assertThat(nullJson.path("review").isNull()).isTrue();

        // 4. FR-ROB-04: Verify PRIVATE POST /api/v1/films produces exactly 18 keys (no directorName)
        String postPayload = """
                {
                  "title": "Private Film %d",
                  "format": "MOVIE",
                  "productionStyle": "LIVE_ACTION",
                  "isNsfw": false,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """.formatted(System.currentTimeMillis());

        String postResp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var postData = objectMapper.readTree(postResp).path("data");
        List<String> postKeys = new ArrayList<>();
        postData.fieldNames().forEachRemaining(postKeys::add);
        assertThat(postKeys).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "productionStyle", "isNsfw",
                "directorPersonId", "description", "totalEpisodes", "progressStatus", "consumptionStatus",
                "currentProgressText", "review", "genreIds", "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(postData.has("directorName")).isFalse();

        Long createdFilmId = postData.path("id").asLong();

        // 5. FR-ROB-04: Verify PRIVATE PUT /api/v1/films/{id} produces exactly 18 keys (no directorName)
        String putPayload = """
                {
                  "title": "Updated Film %d",
                  "format": "MOVIE",
                  "productionStyle": "LIVE_ACTION",
                  "isNsfw": false,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """.formatted(System.currentTimeMillis());

        String putResp = mockMvc.perform(put("/api/v1/films/{id}", createdFilmId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var putData = objectMapper.readTree(putResp).path("data");
        List<String> putKeys = new ArrayList<>();
        putData.fieldNames().forEachRemaining(putKeys::add);
        assertThat(putKeys).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "productionStyle", "isNsfw",
                "directorPersonId", "description", "totalEpisodes", "progressStatus", "consumptionStatus",
                "currentProgressText", "review", "genreIds", "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(putData.has("directorName")).isFalse();
    }

    @Test
    @DisplayName("S4 / ROB-10: Ineligible work yields 404, no-store, data: null, and ZERO People lookups across all negative cases (FR-ROB-03)")
    void ineligibleFilmReturnsFourHundredFourAndZeroPeopleAccess() throws Exception {
        String token = publicBearerToken();

        // 1. Missing film ID
        reset(personOperations, vaultEntryOperations);
        mockMvc.perform(get("/api/v1/films/{id}", 999999999L).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Film not found"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(personOperations, never()).find(any());

        // 2. Trashed film
        Long trashedId = seedFilm(false, true);
        Long trashedPersonId = trashedId + 1_000_000L;
        reset(personOperations, vaultEntryOperations);
        mockMvc.perform(get("/api/v1/films/{id}", trashedId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(personOperations, never()).find(any());
        verify(vaultEntryOperations, never()).find(trashedPersonId);

        // 3. NSFW film
        Long nsfwId = seedFilm(true, false);
        Long nsfwPersonId = nsfwId + 1_000_000L;
        reset(personOperations, vaultEntryOperations);
        mockMvc.perform(get("/api/v1/films/{id}", nsfwId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(personOperations, never()).find(any());
        verify(vaultEntryOperations, never()).find(nsfwPersonId);

        // 4. Wrong-type work Vault entry (e.g. entry_type NOTE instead of FILM)
        Long wrongTypeId = seedFilm(false, false);
        Long wrongTypePersonId = wrongTypeId + 1_000_000L;
        doAnswer(inv -> Optional.of(new VaultEntryView(wrongTypeId, VaultEntryType.NOTE, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(wrongTypeId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", wrongTypeId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(vaultEntryOperations, never()).find(wrongTypePersonId);
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. Missing work Vault entry (empty vaultEntryOperations.find for work)
        Long missingVaultId = seedFilm(false, false);
        Long missingVaultPersonId = missingVaultId + 1_000_000L;
        doAnswer(inv -> Optional.empty()).when(vaultEntryOperations).find(missingVaultId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", missingVaultId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(vaultEntryOperations, never()).find(missingVaultPersonId);
        } finally {
            reset(vaultEntryOperations);
        }

        // 6. Mismatched work Vault ID (returns VaultEntryView with ID != filmId)
        Long mismatchedVaultId = seedFilm(false, false);
        Long mismatchedPersonId = mismatchedVaultId + 1_000_000L;
        doAnswer(inv -> Optional.of(new VaultEntryView(mismatchedVaultId + 999_999L, VaultEntryType.FILM, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(mismatchedVaultId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", mismatchedVaultId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(vaultEntryOperations, never()).find(mismatchedPersonId);
        } finally {
            reset(vaultEntryOperations);
        }
    }

    @Test
    @DisplayName("ROB-08, ROB-09: Qualifies director Person Vault before resolving name: wrong-type, trashed, missing, or mismatched Person Vault yields directorName: null (200 OK)")
    void qualifiesDirectorPersonVaultBeforeResolvingName() throws Exception {
        Long id = seedFilm(false, false);
        Long personId = id + 1_000_000L;
        String token = publicBearerToken();

        // 1. Wrong type director Person Vault (NOTE instead of PERSON) -> directorName: null
        doAnswer(inv -> Optional.of(new VaultEntryView(personId, VaultEntryType.NOTE, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.directorPersonId").value(personId));
        } finally {
            reset(vaultEntryOperations);
        }

        // 2. Trashed director Person Vault -> directorName: null
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", personId);
        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()));

        // 3. Normal recovery: active PERSON Vault -> directorName resolved
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = null WHERE id = ?", personId);
        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.directorName").value("Director-" + personId));

        // 4. Missing director Person Vault result -> directorName: null
        doAnswer(inv -> Optional.empty()).when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.directorPersonId").value(personId));
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. Mismatched director Person Vault ID -> directorName: null
        doAnswer(inv -> Optional.of(new VaultEntryView(personId + 999_999L, VaultEntryType.PERSON, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.directorPersonId").value(personId));
        } finally {
            reset(vaultEntryOperations);
        }
    }

    @Test
    @DisplayName("S4 / ROB-08, ROB-09: Normal absent, trashed, wrong-type director Vault, absent director, or blank name yields directorName: null (200 OK)")
    void referenceAbsenceYieldsPresentNullName() throws Exception {
        String token = publicBearerToken();

        // 1. Trashed director Person Vault -> 200 with directorName: null
        Long filmId1 = seedFilm(false, false);
        Long personId1 = filmId1 + 1_000_000L;
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", personId1);

        mockMvc.perform(get("/api/v1/films/{id}", filmId1).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.directorPersonId").value(personId1));

        // 2. Wrong type director Person Vault (simulated via spy fault injection) -> 200 with directorName: null
        Long filmId2 = seedFilm(false, false);
        Long personId2 = filmId2 + 1_000_000L;
        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(personId2)) {
                return Optional.of(new VaultEntryView(personId2, VaultEntryType.NOTE, Instant.now(), Instant.now(), null));
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(personId2);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", filmId2).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 3. Blank director name -> 200 with directorName: null
        Long filmId3 = seedFilm(false, false);
        Long personId3 = filmId3 + 1_000_000L;
        jdbcTemplate.update("UPDATE persons SET name = '   ' WHERE id = ?", personId3);

        mockMvc.perform(get("/api/v1/films/{id}", filmId3).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()));

        // 4. Absent director ID on work -> 200 with directorName: null, directorPersonId: null
        Long filmId4 = seedFilmWithoutDirector(false, false);
        mockMvc.perform(get("/api/v1/films/{id}", filmId4).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.directorPersonId").value(org.hamcrest.Matchers.nullValue()));

        // 5. Missing Person record (simulated via empty from personOperations.find) -> 200 with directorName: null
        Long filmId5 = seedFilm(false, false);
        Long personId5 = filmId5 + 1_000_000L;
        doAnswer(inv -> Optional.empty()).when(personOperations).find(personId5);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", filmId5).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.directorPersonId").value(personId5));
        } finally {
            reset(personOperations);
        }

        // 6. Injected schema-disallowed null Person name -> 200 with directorName: null
        Long filmId6 = seedFilm(false, false);
        Long personId6 = filmId6 + 1_000_000L;
        doAnswer(inv -> Optional.of(new PersonView(personId6, null, null, null, null, null, null, null, null, null)))
                .when(personOperations).find(personId6);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", filmId6).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.directorPersonId").value(personId6));
        } finally {
            reset(personOperations);
        }
    }

    @Test
    @DisplayName("S1 / ROB-12, ROB-15: Outer READ COMMITTED transaction suspended, dedicated physical REPEATABLE READ snapshot across all query points for director path (FR-ROB-02)")
    void s1PhysicalNewPathIsolationAndContextResumption() {
        Long filmWorkId = seedFilmWithClassifications(false, false);
        Long directorId = filmWorkId + 1_000_000L;

        AtomicReference<Integer> pidAtWorkRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtWorkVault = new AtomicReference<>();
        AtomicReference<Integer> pidAtGenreRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtArchRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtSettingRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtDirectorVault = new AtomicReference<>();
        AtomicReference<Integer> pidAtDirectorRepo = new AtomicReference<>();

        AtomicReference<String> snapAtWorkRepo = new AtomicReference<>();
        AtomicReference<String> snapAtWorkVault = new AtomicReference<>();
        AtomicReference<String> snapAtGenreRepo = new AtomicReference<>();
        AtomicReference<String> snapAtArchRepo = new AtomicReference<>();
        AtomicReference<String> snapAtSettingRepo = new AtomicReference<>();
        AtomicReference<String> snapAtDirectorVault = new AtomicReference<>();
        AtomicReference<String> snapAtDirectorRepo = new AtomicReference<>();

        AtomicReference<String> isoAtWorkRepo = new AtomicReference<>();
        AtomicReference<String> isoAtDirectorRepo = new AtomicReference<>();
        AtomicReference<String> roAtWorkRepo = new AtomicReference<>();
        AtomicReference<String> roAtDirectorRepo = new AtomicReference<>();

        AtomicReference<Film> outerManagedRef = new AtomicReference<>();
        AtomicBoolean innerContainsOuterEntityAtRepo = new AtomicBoolean(true);

        MethodInterceptor workRepoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long argId = (Long) invocation.getArguments()[0];
                if (argId != null && argId.equals(filmWorkId)) {
                    pidAtWorkRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtWorkRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                    isoAtWorkRepo.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    roAtWorkRepo.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                    Film outerManaged = outerManagedRef.get();
                    if (outerManaged != null) {
                        innerContainsOuterEntityAtRepo.set(entityManager.contains(outerManaged));
                    }
                }
            }
            return invocation.proceed();
        };
        Advised advisedWorkRepo = (Advised) filmRepository;
        advisedWorkRepo.addAdvice(0, workRepoAdvice);

        MethodInterceptor genreAdvice = invocation -> {
            pidAtGenreRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
            snapAtGenreRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
            return invocation.proceed();
        };
        Advised advisedGenreRepo = (Advised) filmGenreAssignmentRepository;
        advisedGenreRepo.addAdvice(0, genreAdvice);

        MethodInterceptor archAdvice = invocation -> {
            pidAtArchRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
            snapAtArchRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
            return invocation.proceed();
        };
        Advised advisedArchRepo = (Advised) storyArchetypeRepository;
        advisedArchRepo.addAdvice(0, archAdvice);

        MethodInterceptor settingAdvice = invocation -> {
            pidAtSettingRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
            snapAtSettingRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
            return invocation.proceed();
        };
        Advised advisedSettingRepo = (Advised) worldSettingRepository;
        advisedSettingRepo.addAdvice(0, settingAdvice);

        Advised advisedVaultRepo = (Advised) applicationContext.getBean("vaultEntryRepository");
        MethodInterceptor vaultRepoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long idArg = (Long) invocation.getArguments()[0];
                Integer pid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                String snap = jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class);
                if (idArg != null && idArg.equals(filmWorkId)) {
                    pidAtWorkVault.set(pid);
                    snapAtWorkVault.set(snap);
                } else if (idArg != null && idArg.equals(directorId)) {
                    pidAtDirectorVault.set(pid);
                    snapAtDirectorVault.set(snap);
                }
            }
            return invocation.proceed();
        };
        advisedVaultRepo.addAdvice(0, vaultRepoAdvice);

        Advised advisedPersonRepo = (Advised) applicationContext.getBean("personRepository");
        MethodInterceptor personRepoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long idArg = (Long) invocation.getArguments()[0];
                if (idArg != null && idArg.equals(directorId)) {
                    pidAtDirectorRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtDirectorRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                    isoAtDirectorRepo.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    roAtDirectorRepo.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                }
            }
            return invocation.proceed();
        };
        advisedPersonRepo.addAdvice(0, personRepoAdvice);

        TransactionTemplate outerTemplate = new TransactionTemplate(transactionManager);
        outerTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        outerTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);

        try {
            outerTemplate.executeWithoutResult(status -> {
                String outerIso = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer outerPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(outerIso).isEqualToIgnoringCase("read committed");

                Film managedFilm = entityManager.find(Film.class, filmWorkId);
                assertThat(managedFilm).isNotNull();
                assertThat(entityManager.contains(managedFilm)).isTrue();
                outerManagedRef.set(managedFilm);

                Optional<PublicFilmDetailView> result = filmOperations.findPublicDetail(filmWorkId);
                assertThat(result).isPresent();

                // All 7 query points execute on the same dedicated inner connection distinct from outer
                assertThat(pidAtWorkRepo.get()).isNotNull();
                assertThat(pidAtWorkVault.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtGenreRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtArchRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtSettingRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtDirectorVault.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtDirectorRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtWorkRepo.get()).isNotEqualTo(outerPid);

                // Snapshot identity is identical across all 7 data query points
                assertThat(snapAtWorkRepo.get()).isNotNull();
                assertThat(snapAtWorkVault.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtGenreRepo.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtArchRepo.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtSettingRepo.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtDirectorVault.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtDirectorRepo.get()).isEqualTo(snapAtWorkRepo.get());

                // Isolation is REPEATABLE READ and read_only = ON throughout
                assertThat(isoAtWorkRepo.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(isoAtDirectorRepo.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(roAtWorkRepo.get()).isEqualToIgnoringCase("on");
                assertThat(roAtDirectorRepo.get()).isEqualToIgnoringCase("on");

                // Inner persistence context did not manage outer entity
                assertThat(innerContainsOuterEntityAtRepo.get()).isFalse();

                // Outer transaction resumes with read committed and intact managed entity
                String resumedIso = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer resumedPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(resumedIso).isEqualToIgnoringCase("read committed");
                assertThat(resumedPid).isEqualTo(outerPid);
                assertThat(entityManager.contains(managedFilm)).isTrue();
            });
        } finally {
            advisedWorkRepo.removeAdvice(workRepoAdvice);
            advisedGenreRepo.removeAdvice(genreAdvice);
            advisedArchRepo.removeAdvice(archAdvice);
            advisedSettingRepo.removeAdvice(settingAdvice);
            advisedVaultRepo.removeAdvice(vaultRepoAdvice);
            advisedPersonRepo.removeAdvice(personRepoAdvice);
            reset(vaultEntryOperations, personOperations);
        }
    }

    @Test
    @DisplayName("S2 / ROB-13: Concurrent committed work updates (NSFW/trash vs full values) obey snapshot boundary and fresh request reflects updated state (FR-ROB-02)")
    void s2WorkEligibilityAndFullValuesSnapshotIsolation() throws Exception {
        // CASE A: Work eligibility destroyed concurrently -> held reader gets full earlier 19-field state; fresh request gets 404
        Long idA = seedFilmWithClassifications(false, false);

        CountDownLatch readStartedA = new CountDownLatch(1);
        CountDownLatch commitCompletedA = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(idA)) {
                readStartedA.countDown();
                assertThat(commitCompletedA.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(idA);

        ExecutorService executorA = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmDetailView>> readerFuture = executorA.submit(() -> filmOperations.findPublicDetail(idA));

            assertThat(readStartedA.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction: marks NSFW, trashes work vault, alters progress/review/episodes, changes classifications
            Long newGenreIdA = idA + 5_000_000L;
            jdbcTemplate.update("INSERT INTO film_genres (id, name, description) VALUES (?, 'New Genre A', 'Desc') ON CONFLICT DO NOTHING", newGenreIdA);
            jdbcTemplate.update("INSERT INTO film_genre_assignments (film_id, genre_id) VALUES (?, ?)", idA, newGenreIdA);
            jdbcTemplate.update("DELETE FROM film_world_settings WHERE film_id = ?", idA);
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", idA);
            jdbcTemplate.update("UPDATE films SET is_nsfw = true, progress_status = 'SUSPENDED', total_episodes = 999, review = 'Mutated Review' WHERE id = ?", idA);
            commitCompletedA.countDown();

            Optional<PublicFilmDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            var view = heldResult.get();
            // Snapshot preserves initial eligible values across ALL 19 fields and exact earlier sets
            assertThat(view.id()).isEqualTo(idA);
            assertThat(view.title()).isEqualTo("FilmTitle-" + idA);
            assertThat(view.originalTitle()).isEqualTo("NativeFilm-" + idA);
            assertThat(view.nationalityCode()).isEqualTo("VN");
            assertThat(view.posterUrl()).isEqualTo("https://secret.cdn/film.png");
            assertThat(view.format()).isEqualTo(FilmFormat.MOVIE);
            assertThat(view.productionStyle()).isEqualTo(FilmProductionStyle.LIVE_ACTION);
            assertThat(view.isNsfw()).isFalse();
            assertThat(view.directorPersonId()).isEqualTo(idA + 1_000_000L);
            assertThat(view.description()).isEqualTo("Film synopsis");
            assertThat(view.totalEpisodes()).isEqualTo(1);
            assertThat(view.progressStatus()).isEqualTo(ProgressStatus.COMPLETED);
            assertThat(view.consumptionStatus()).isEqualTo(ConsumptionStatus.CONSUMED);
            assertThat(view.currentProgressText()).isEqualTo("Full movie");
            assertThat(view.review()).isEqualTo("Film review");
            assertThat(view.directorName()).isEqualTo("Director-" + (idA + 1_000_000L));
            assertThat(view.genreIds()).isEqualTo(Set.of(idA + 2_000_000L));
            assertThat(view.storyArchetypeIds()).isEqualTo(Set.of(idA + 3_000_000L));
            assertThat(view.worldSettingIds()).isEqualTo(Set.of(idA + 4_000_000L));

            // Subsequent fresh HTTP request after commit -> 404 Not Found, no-store, data null, ZERO people access
            reset(personOperations);
            mockMvc.perform(get("/api/v1/films/{id}", idA).header("Authorization", "Bearer " + publicBearerToken()))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
        } finally {
            reset(vaultEntryOperations);
            executorA.shutdownNow();
        }

        // CASE B: Eligibility-preserving full values change -> held reader sees old values; fresh HTTP request sees updated 19-field state
        Long idB = seedFilmWithClassifications(false, false);

        CountDownLatch readStartedB = new CountDownLatch(1);
        CountDownLatch commitCompletedB = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(idB)) {
                readStartedB.countDown();
                assertThat(commitCompletedB.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(idB);

        ExecutorService executorB = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmDetailView>> readerFuture = executorB.submit(() -> filmOperations.findPublicDetail(idB));

            assertThat(readStartedB.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction: updates title, originalTitle, progressStatus, totalEpisodes, review, currentProgressText, classification sets
            Long newGenreIdB = idB + 5_000_000L;
            jdbcTemplate.update("INSERT INTO film_genres (id, name, description) VALUES (?, 'New Genre B', 'Desc') ON CONFLICT DO NOTHING", newGenreIdB);
            jdbcTemplate.update("INSERT INTO film_genre_assignments (film_id, genre_id) VALUES (?, ?)", idB, newGenreIdB);
            jdbcTemplate.update("DELETE FROM film_world_settings WHERE film_id = ?", idB);
            jdbcTemplate.update("UPDATE films SET title = 'Evolved Film', original_title = 'Evolved Original', progress_status = 'COMPLETED', total_episodes = 24, review = 'Evolved Review', current_progress_text = 'Episode 10' WHERE id = ?", idB);
            commitCompletedB.countDown();

            Optional<PublicFilmDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            var heldView = heldResult.get();
            assertThat(heldView.id()).isEqualTo(idB);
            assertThat(heldView.title()).isEqualTo("FilmTitle-" + idB);
            assertThat(heldView.originalTitle()).isEqualTo("NativeFilm-" + idB);
            assertThat(heldView.nationalityCode()).isEqualTo("VN");
            assertThat(heldView.posterUrl()).isEqualTo("https://secret.cdn/film.png");
            assertThat(heldView.format()).isEqualTo(FilmFormat.MOVIE);
            assertThat(heldView.productionStyle()).isEqualTo(FilmProductionStyle.LIVE_ACTION);
            assertThat(heldView.isNsfw()).isFalse();
            assertThat(heldView.directorPersonId()).isEqualTo(idB + 1_000_000L);
            assertThat(heldView.description()).isEqualTo("Film synopsis");
            assertThat(heldView.totalEpisodes()).isEqualTo(1);
            assertThat(heldView.progressStatus()).isEqualTo(ProgressStatus.COMPLETED);
            assertThat(heldView.consumptionStatus()).isEqualTo(ConsumptionStatus.CONSUMED);
            assertThat(heldView.currentProgressText()).isEqualTo("Full movie");
            assertThat(heldView.review()).isEqualTo("Film review");
            assertThat(heldView.directorName()).isEqualTo("Director-" + (idB + 1_000_000L));
            assertThat(heldView.genreIds()).isEqualTo(Set.of(idB + 2_000_000L));
            assertThat(heldView.storyArchetypeIds()).isEqualTo(Set.of(idB + 3_000_000L));
            assertThat(heldView.worldSettingIds()).isEqualTo(Set.of(idB + 4_000_000L));

            // Fresh HTTP request sees the new coherent updated values across all 19 properties
            mockMvc.perform(get("/api/v1/films/{id}", idB).header("Authorization", "Bearer " + publicBearerToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(idB))
                    .andExpect(jsonPath("$.data.title").value("Evolved Film"))
                    .andExpect(jsonPath("$.data.originalTitle").value("Evolved Original"))
                    .andExpect(jsonPath("$.data.nationalityCode").value("VN"))
                    .andExpect(jsonPath("$.data.posterUrl").value("https://secret.cdn/film.png"))
                    .andExpect(jsonPath("$.data.format").value("MOVIE"))
                    .andExpect(jsonPath("$.data.productionStyle").value("LIVE_ACTION"))
                    .andExpect(jsonPath("$.data.isNsfw").value(false))
                    .andExpect(jsonPath("$.data.directorPersonId").value(idB + 1_000_000L))
                    .andExpect(jsonPath("$.data.description").value("Film synopsis"))
                    .andExpect(jsonPath("$.data.totalEpisodes").value(24))
                    .andExpect(jsonPath("$.data.progressStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.consumptionStatus").value("CONSUMED"))
                    .andExpect(jsonPath("$.data.currentProgressText").value("Episode 10"))
                    .andExpect(jsonPath("$.data.review").value("Evolved Review"))
                    .andExpect(jsonPath("$.data.directorName").value("Director-" + (idB + 1_000_000L)))
                    .andExpect(jsonPath("$.data.genreIds", org.hamcrest.Matchers.containsInAnyOrder(idB + 2_000_000L, newGenreIdB)))
                    .andExpect(jsonPath("$.data.storyArchetypeIds", org.hamcrest.Matchers.containsInAnyOrder(idB + 3_000_000L)))
                    .andExpect(jsonPath("$.data.worldSettingIds", org.hamcrest.Matchers.empty()));
        } finally {
            reset(vaultEntryOperations);
            executorB.shutdownNow();
        }
    }

    @Test
    @DisplayName("S3 / ROB-14: Controlled concurrent director reference updates (Person name, Person Vault trash) obey snapshot boundary (FR-ROB-02)")
    void s3LinkedSourceSnapshotIsolation() throws Exception {
        // CASE A: Director Person name update race
        Long filmIdA = seedFilm(false, false);
        Long directorIdA = filmIdA + 1_000_000L;

        CountDownLatch readStartedA = new CountDownLatch(1);
        CountDownLatch updateCommittedA = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(directorIdA)) {
                readStartedA.countDown();
                assertThat(updateCommittedA.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(directorIdA);

        ExecutorService executorA = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmDetailView>> readerFuture = executorA.submit(() -> filmOperations.findPublicDetail(filmIdA));

            assertThat(readStartedA.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction updates director person name
            jdbcTemplate.update("UPDATE persons SET name = 'Newly Committed Director' WHERE id = ?", directorIdA);
            updateCommittedA.countDown();

            Optional<PublicFilmDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            // REPEATABLE READ snapshot took initial read; returns initial name
            assertThat(heldResult.get().directorName()).isEqualTo("Director-" + directorIdA);

            // Fresh request sees the newly committed name
            Optional<PublicFilmDetailView> freshResult = filmOperations.findPublicDetail(filmIdA);
            assertThat(freshResult).isPresent();
            assertThat(freshResult.get().directorName()).isEqualTo("Newly Committed Director");
        } finally {
            reset(vaultEntryOperations);
            executorA.shutdownNow();
        }

        // CASE B: Director Person Vault trashed concurrently
        Long filmIdB = seedFilm(false, false);
        Long directorIdB = filmIdB + 1_000_000L;

        CountDownLatch readStartedB = new CountDownLatch(1);
        CountDownLatch trashCommittedB = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(directorIdB)) {
                readStartedB.countDown();
                assertThat(trashCommittedB.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(directorIdB);

        ExecutorService executorB = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmDetailView>> readerFuture = executorB.submit(() -> filmOperations.findPublicDetail(filmIdB));

            assertThat(readStartedB.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction trashes director Person Vault
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", directorIdB);
            trashCommittedB.countDown();

            Optional<PublicFilmDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            // Held snapshot still sees Person Vault as active -> returns directorName
            assertThat(heldResult.get().directorName()).isEqualTo("Director-" + directorIdB);

            // Fresh request sees trashed Person Vault -> 200 OK with directorName: null
            mockMvc.perform(get("/api/v1/films/{id}", filmIdB).header("Authorization", "Bearer " + publicBearerToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.directorName").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
            executorB.shutdownNow();
        }
    }

    @Test
    @DisplayName("S5 / ROB-11: Genuine work, Vault, Person, classification query, and transaction-completion failures yield safe 500/data:null/no-store (FR-ROB-03)")
    void s5OperationalAndCompletionFailuresYieldSafe500() throws Exception {
        Long id = seedFilmWithClassifications(false, false);
        Long personId = id + 1_000_000L;
        String token = publicBearerToken();

        // 1. Work query failure on filmRepository.findById
        MethodInterceptor workQueryFailureAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long argId = (Long) invocation.getArguments()[0];
                if (argId != null && argId.equals(id)) {
                    throw new DataAccessResourceFailureException("Injected DB failure on filmRepository");
                }
            }
            return invocation.proceed();
        };
        Advised advisedWorkRepo = (Advised) filmRepository;
        advisedWorkRepo.addAdvice(0, workQueryFailureAdvice);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            advisedWorkRepo.removeAdvice(workQueryFailureAdvice);
        }

        // 2. Classification query failure on filmGenreAssignmentRepository
        MethodInterceptor genreFailureAdvice = invocation -> {
            throw new QueryTimeoutException("Injected timeout on filmGenreAssignmentRepository");
        };
        Advised advisedGenreRepo = (Advised) filmGenreAssignmentRepository;
        advisedGenreRepo.addAdvice(0, genreFailureAdvice);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            advisedGenreRepo.removeAdvice(genreFailureAdvice);
        }

        // 3. VaultEntryOperations failure on work Vault read
        doThrow(new QueryTimeoutException("Injected DB failure on work vault"))
                .when(vaultEntryOperations).find(id);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 4. VaultEntryOperations failure on director Person Vault read
        doThrow(new QueryTimeoutException("Injected DB failure on director person vault"))
                .when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. PersonOperations failure on Person read
        doThrow(new DataAccessResourceFailureException("Injected Person read failure"))
                .when(personOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(personOperations);
        }

        // 6. Service transaction-completion failure (registered beforeCommit synchronization throws)
        doAnswer(inv -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    throw new DataAccessResourceFailureException("Injected transaction completion failure before commit");
                }
            });
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(id);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 7. Normal recovery after clearing all faults
        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.directorName").value("Director-" + personId));
    }

    @Test
    @DisplayName("S6 / ROB-12: Complete immutable result materialized before return, non-empty sets retain values, no post-return database access (FR-ROB-03)")
    void s6CompleteImmutableResultMaterializedBeforeReturn() throws Exception {
        Long id = seedFilmWithClassifications(false, false);
        Set<Long> expectedGenres = Set.of(id + 2_000_000L);
        Set<Long> expectedArchetypes = Set.of(id + 3_000_000L);
        Set<Long> expectedSettings = Set.of(id + 4_000_000L);

        AtomicInteger workRepoCalls = new AtomicInteger();
        AtomicInteger genreRepoCalls = new AtomicInteger();
        AtomicInteger archRepoCalls = new AtomicInteger();
        AtomicInteger settingRepoCalls = new AtomicInteger();
        AtomicInteger vaultRepoCalls = new AtomicInteger();
        AtomicInteger personRepoCalls = new AtomicInteger();

        MethodInterceptor workAdvice = inv -> {
            workRepoCalls.incrementAndGet();
            return inv.proceed();
        };
        MethodInterceptor genreAdvice = inv -> {
            genreRepoCalls.incrementAndGet();
            return inv.proceed();
        };
        MethodInterceptor archAdvice = inv -> {
            archRepoCalls.incrementAndGet();
            return inv.proceed();
        };
        MethodInterceptor settingAdvice = inv -> {
            settingRepoCalls.incrementAndGet();
            return inv.proceed();
        };
        MethodInterceptor vaultAdvice = inv -> {
            vaultRepoCalls.incrementAndGet();
            return inv.proceed();
        };
        MethodInterceptor personAdvice = inv -> {
            personRepoCalls.incrementAndGet();
            return inv.proceed();
        };

        Advised advisedWorkRepo = (Advised) filmRepository;
        Advised advisedGenreRepo = (Advised) filmGenreAssignmentRepository;
        Advised advisedArchRepo = (Advised) storyArchetypeRepository;
        Advised advisedSettingRepo = (Advised) worldSettingRepository;
        Advised advisedVaultRepo = (Advised) applicationContext.getBean("vaultEntryRepository");
        Advised advisedPersonRepo = (Advised) applicationContext.getBean("personRepository");

        advisedWorkRepo.addAdvice(0, workAdvice);
        advisedGenreRepo.addAdvice(0, genreAdvice);
        advisedArchRepo.addAdvice(0, archAdvice);
        advisedSettingRepo.addAdvice(0, settingAdvice);
        advisedVaultRepo.addAdvice(0, vaultAdvice);
        advisedPersonRepo.addAdvice(0, personAdvice);

        Optional<PublicFilmDetailView> viewOpt;
        try {
            viewOpt = filmOperations.findPublicDetail(id);
        } finally {
            // Positively verify that each repository was queried during acquisition
            assertThat(workRepoCalls.get()).isGreaterThan(0);
            assertThat(genreRepoCalls.get()).isGreaterThan(0);
            assertThat(archRepoCalls.get()).isGreaterThan(0);
            assertThat(settingRepoCalls.get()).isGreaterThan(0);
            assertThat(vaultRepoCalls.get()).isGreaterThan(0);
            assertThat(personRepoCalls.get()).isGreaterThan(0);

            // Reset invocation counts to guard against post-return reads
            workRepoCalls.set(0);
            genreRepoCalls.set(0);
            archRepoCalls.set(0);
            settingRepoCalls.set(0);
            vaultRepoCalls.set(0);
            personRepoCalls.set(0);
        }

        try {
            assertThat(viewOpt).isPresent();
            PublicFilmDetailView view = viewOpt.get();

            // Non-empty immutable sets matching exact expected IDs
            assertThat(view.genreIds()).isEqualTo(expectedGenres);
            assertThat(view.storyArchetypeIds()).isEqualTo(expectedArchetypes);
            assertThat(view.worldSettingIds()).isEqualTo(expectedSettings);
            assertThatThrownBy(() -> view.genreIds().add(99999L))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> view.storyArchetypeIds().add(99999L))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> view.worldSettingIds().add(99999L))
                    .isInstanceOf(UnsupportedOperationException.class);

            // Reset capability spies to also verify Mockito never()
            reset(vaultEntryOperations, personOperations);

            // In-memory mapping to response DTO outside transaction
            PublicFilmDetailResponse response = FilmWebMapper.toPublicDetailResponse(view);
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.directorName()).isEqualTo("Director-" + (id + 1_000_000L));
            assertThat(response.genreIds()).isEqualTo(expectedGenres);
            assertThat(response.storyArchetypeIds()).isEqualTo(expectedArchetypes);
            assertThat(response.worldSettingIds()).isEqualTo(expectedSettings);

            // Serialization outside transaction through configured ObjectMapper
            String serialized = objectMapper.writeValueAsString(response);
            assertThat(serialized).contains("\"directorName\":\"Director-" + (id + 1_000_000L) + "\"");

            // Verify serialized JSON retains exact captured set values
            var jsonNode = objectMapper.readTree(serialized);
            assertThat(jsonNode.path("id").asLong()).isEqualTo(id);
            assertThat(jsonNode.path("directorName").asText()).isEqualTo("Director-" + (id + 1_000_000L));
            List<Long> jsonGenres = new ArrayList<>();
            jsonNode.path("genreIds").forEach(n -> jsonGenres.add(n.asLong()));
            assertThat(jsonGenres).containsExactlyElementsOf(expectedGenres);
            List<Long> jsonArchetypes = new ArrayList<>();
            jsonNode.path("storyArchetypeIds").forEach(n -> jsonArchetypes.add(n.asLong()));
            assertThat(jsonArchetypes).containsExactlyElementsOf(expectedArchetypes);
            List<Long> jsonSettings = new ArrayList<>();
            jsonNode.path("worldSettingIds").forEach(n -> jsonSettings.add(n.asLong()));
            assertThat(jsonSettings).containsExactlyElementsOf(expectedSettings);

            // Positively guard that ZERO repository queries or capability operations occurred post-return
            assertThat(workRepoCalls.get()).isZero();
            assertThat(genreRepoCalls.get()).isZero();
            assertThat(archRepoCalls.get()).isZero();
            assertThat(settingRepoCalls.get()).isZero();
            assertThat(vaultRepoCalls.get()).isZero();
            assertThat(personRepoCalls.get()).isZero();
            verify(vaultEntryOperations, never()).find(any());
            verify(personOperations, never()).find(any());
        } finally {
            advisedWorkRepo.removeAdvice(workAdvice);
            advisedGenreRepo.removeAdvice(genreAdvice);
            advisedArchRepo.removeAdvice(archAdvice);
            advisedSettingRepo.removeAdvice(settingAdvice);
            advisedVaultRepo.removeAdvice(vaultAdvice);
            advisedPersonRepo.removeAdvice(personAdvice);
        }
    }

    @Test
    @DisplayName("ROB-16: PUBLIC 200, 404, and 500 use no-store; conditional headers do not return 304; profile switches isolate data (FR-ROB-04)")
    void publicProfileSwitchAndNoStoreCachingControls() throws Exception {
        Long id = seedFilm(false, false);
        String pubToken = publicBearerToken();
        String privToken = bearerToken();

        // 1. PUBLIC 200 has no-store, no ETag, no Location
        var resp200 = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse();
        assertThat(resp200.getHeader(HttpHeaders.ETAG)).isNull();
        assertThat(resp200.getHeader(HttpHeaders.LOCATION)).isNull();

        // 2. PUBLIC 404 has no-store
        mockMvc.perform(get("/api/v1/films/{id}", 9999999L).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        // 3. Injected 500 has no-store
        doThrow(new DataAccessResourceFailureException("Injected fault for cache check"))
                .when(vaultEntryOperations).find(id);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + pubToken))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        } finally {
            reset(vaultEntryOperations);
        }

        // 4. Conditional headers do NOT return 304 on PUBLIC 200, 404, or 500
        mockMvc.perform(get("/api/v1/films/{id}", id)
                        .header("Authorization", "Bearer " + pubToken)
                        .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                        .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(get("/api/v1/films/{id}", 9999999L)
                        .header("Authorization", "Bearer " + pubToken)
                        .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                        .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        doThrow(new DataAccessResourceFailureException("Injected fault for conditional 500 check"))
                .when(vaultEntryOperations).find(id);
        try {
            mockMvc.perform(get("/api/v1/films/{id}", id)
                            .header("Authorization", "Bearer " + pubToken)
                            .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                            .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. Sequential reads across profiles do not bleed representation or cache
        var pubFirst = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(pubFirst).path("data").fieldNames()).toIterable().hasSize(19);

        var privSecond = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + privToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(privSecond).path("data").fieldNames()).toIterable().hasSize(18);
        assertThat(objectMapper.readTree(privSecond).path("data").has("directorName")).isFalse();
    }

    @Test
    @DisplayName("ROA-01, ROA-02, ROB-20: Public bearer token is denied 403 on POST /api/v1/films before handler")
    void publicTokenDeniedToPostFilmWith403AndNoHandler() throws Exception {
        String pubToken = publicBearerToken();
        String pubDowngradeToken = publicBearerTokenViaDowngrade();

        String payload = """
                {
                    "title": "Interstellar",
                    "originalTitle": "Interstellar Original",
                    "nationalityCode": "US",
                    "format": "MOVIE",
                    "productionStyle": "LIVE_ACTION",
                    "isNsfw": false
                }
                """;

        // Direct PUBLIC login token denied 403
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + pubToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // Downgraded PUBLIC token denied 403
        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", "Bearer " + pubDowngradeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // Anonymous token denied 401
        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ROB-02: Malformed, stale, or revoked tokens fail closed without data (401)")
    void malformedStaleAndRevokedTokensFailClosedWithoutData() throws Exception {
        Long id = seedFilm(false, false);

        mockMvc.perform(get("/api/v1/films/{id}", id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer malformed.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
