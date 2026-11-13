package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.internal.domain.Film;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreAssignmentRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.film.view.PublicFilmView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("B1-01: Both password-login and downgrade persisted PUBLIC sessions reach GET /api/v1/films/{id}")
    void publicSessionsReachFilmDetail() throws Exception {
        Long id = seedFilm(false, false);
        String token1 = publicBearerToken();
        String token2 = publicBearerTokenViaDowngrade();

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
    }

    @Test
    @DisplayName("B1-03: Eligible Film 200 response contains exactly seven scalar keys and omits all forbidden sentinels")
    void eligibleFilmHasExactlySevenKeysAndNoForbiddenSentinels() throws Exception {
        Long id = seedFilm(false, false);
        String token = publicBearerToken();

        String body = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var json = objectMapper.readTree(body);
        assertThat(json.path("error").isNull()).isTrue();
        assertThat(json.path("meta").isNull()).isTrue();

        var data = json.path("data");
        assertThat(data.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "format", "productionStyle", "isNsfw"
        );
        assertThat(data.path("id").asLong()).isEqualTo(id);
        assertThat(data.path("title").asText()).isEqualTo("FilmTitle-" + id);
        assertThat(data.path("originalTitle").asText()).isEqualTo("NativeFilm-" + id);
        assertThat(data.path("nationalityCode").asText()).isEqualTo("VN");
        assertThat(data.path("format").asText()).isEqualTo("MOVIE");
        assertThat(data.path("productionStyle").asText()).isEqualTo("LIVE_ACTION");
        assertThat(data.path("isNsfw").asBoolean()).isFalse();

        // Also verify that when optional originalTitle and nationalityCode are null, all 7 keys are still emitted
        Long nullFieldsId = System.nanoTime();
        Long personId = nullFieldsId + 1_000_000L;
        jdbcTemplate.update("INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at) VALUES (?, 'PERSON', null, now(), now()) ON CONFLICT DO NOTHING", personId);
        jdbcTemplate.update("INSERT INTO persons(id, name) VALUES (?, ?) ON CONFLICT DO NOTHING", personId, "Director");
        jdbcTemplate.update("INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at) VALUES (?, 'FILM', null, now(), now())", nullFieldsId);
        jdbcTemplate.update("INSERT INTO films(id, title, original_title, nationality_code, poster_url, format, production_style, is_nsfw, director_person_id, progress_status, consumption_status) VALUES (?, 'T', null, null, null, 'MOVIE', 'LIVE_ACTION', false, ?, 'ONGOING', 'UNCONSUMED')", nullFieldsId, personId);

        String nullBody = mockMvc.perform(get("/api/v1/films/{id}", nullFieldsId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var nullJson = objectMapper.readTree(nullBody).path("data");
        assertThat(nullJson.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "format", "productionStyle", "isNsfw"
        );
        assertThat(nullJson.path("originalTitle").isNull()).isTrue();
        assertThat(nullJson.path("nationalityCode").isNull()).isTrue();

        // Forbidden sentinels must NOT appear in output
        assertThat(data.has("posterUrl")).isFalse();
        assertThat(data.has("directorPersonId")).isFalse();
        assertThat(data.has("description")).isFalse();
        assertThat(data.has("totalEpisodes")).isFalse();
        assertThat(data.has("progressStatus")).isFalse();
        assertThat(data.has("consumptionStatus")).isFalse();
        assertThat(data.has("currentProgressText")).isFalse();
        assertThat(data.has("review")).isFalse();
        assertThat(data.has("genreIds")).isFalse();
        assertThat(data.has("storyArchetypeIds")).isFalse();
        assertThat(data.has("worldSettingIds")).isFalse();
    }

    @Test
    @DisplayName("B1-04: NSFW film detail is indistinguishable from absent 404 (error body, status, headers)")
    void nsfwDetailIsIndistinguishableFromAbsent404() throws Exception {
        Long nsfwId = seedFilm(true, false);
        Long absentId = 999999999L;
        String token = publicBearerToken();

        var absentResp = mockMvc.perform(get("/api/v1/films/{id}", absentId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Film not found"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.meta").value(org.hamcrest.Matchers.nullValue()))
                .andReturn().getResponse();

        var nsfwResp = mockMvc.perform(get("/api/v1/films/{id}", nsfwId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Film not found"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.meta").value(org.hamcrest.Matchers.nullValue()))
                .andReturn().getResponse();

        assertThat(objectMapper.readTree(absentResp.getContentAsString())).isEqualTo(objectMapper.readTree(nsfwResp.getContentAsString()));
        assertThat(absentResp.getContentType()).isEqualTo(nsfwResp.getContentType());
        assertThat(absentResp.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo(nsfwResp.getHeader(HttpHeaders.CACHE_CONTROL));
        for (var resp : java.util.List.of(absentResp, nsfwResp)) {
            assertThat(resp.getHeader(HttpHeaders.ETAG)).isNull();
            assertThat(resp.getHeader(HttpHeaders.LAST_MODIFIED)).isNull();
            assertThat(resp.getHeader(HttpHeaders.LOCATION)).isNull();
        }
    }

    @Test
    @DisplayName("B1-05: Trash, wrong-type vault entry, and defensive missing-vault injection deny; restore qualifies iff non-NSFW")
    void trashWrongTypeAndMissingVaultDeny() throws Exception {
        String token = publicBearerToken();

        // 1. Trashed film
        Long trashedId = seedFilm(false, true);
        mockMvc.perform(get("/api/v1/films/{id}", trashedId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"));

        // Restore -> qualifies
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = null WHERE id = ?", trashedId);
        mockMvc.perform(get("/api/v1/films/{id}", trashedId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Restore but set NSFW -> denies
        jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", trashedId);
        mockMvc.perform(get("/api/v1/films/{id}", trashedId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"));

        // 2. Wrong vault type (simulated via spy fault injection, as entry_type is DB-immutable)
        Long wrongTypeId = seedFilm(false, false);
        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(wrongTypeId)) {
                return Optional.of(new VaultEntryView(wrongTypeId, VaultEntryType.FICTION, Instant.now(), Instant.now(), null));
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(wrongTypeId);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", wrongTypeId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"));
        } finally {
            reset(vaultEntryOperations);
        }

        // 3. Defensive missing-Vault injection
        Long injectedId = seedFilm(false, false);
        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(injectedId)) {
                return Optional.empty();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(injectedId);

        try {
            mockMvc.perform(get("/api/v1/films/{id}", injectedId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"));
        } finally {
            reset(vaultEntryOperations);
        }
    }

    @Test
    @DisplayName("B1-06: PRIVATE full schemas and behavior retained for qualifying, NSFW, and trashed rows")
    void privateBehaviorRetained() throws Exception {
        Long normalId = seedFilm(false, false);
        Long nsfwId = seedFilm(true, false);
        Long trashedId = seedFilm(false, true);

        String privateHeader = bearerHeader();

        // Normal -> full view
        mockMvc.perform(get("/api/v1/films/{id}", normalId).header("Authorization", privateHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review").value("Film review"))
                .andExpect(jsonPath("$.data.description").value("Film synopsis"));

        // NSFW -> PRIVATE still reads full view
        mockMvc.perform(get("/api/v1/films/{id}", nsfwId).header("Authorization", privateHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNsfw").value(true));

        // Trashed -> PRIVATE still reads
        mockMvc.perform(get("/api/v1/films/{id}", trashedId).header("Authorization", privateHeader))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("B1-07, B1-09, B1-10: Writes, nested reads, and direct credit reads deny PUBLIC with 403 ACCESS_DENIED")
    void writesNestedReadsAndCreditDenyPublic() throws Exception {
        Long id = seedFilm(false, false);
        String token = publicBearerToken();

        // Unselected write denies (PUT, nested credit POST)
        mockMvc.perform(put("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/v1/films/{id}/credits", id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // BF26-DP-B2-W1: POST /api/v1/films is admitted to PUBLIC; invalid empty body returns 400 VALIDATION_ERROR with no-store
        mockMvc.perform(post("/api/v1/films").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Nested reads deny
        mockMvc.perform(get("/api/v1/films/{id}/classifications", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/{id}/credits", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/{id}/genres", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/{id}/links", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/{id}/story-archetypes", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/films/{id}/world-settings", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        // Direct credit read denies
        mockMvc.perform(get("/api/v1/films/credits/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("B1-12: Detail GET yields 401 on anonymous and invalid tokens")
    void anonymousAndInvalidTokensYield401() throws Exception {
        Long id = seedFilm(false, false);

        mockMvc.perform(get("/api/v1/films/{id}", id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("B1-16: Forced before/after NSFW commit obeys snapshot boundary and fresh-request denial")
    void forcedNsfwCommitObeysSnapshotBoundary() throws Exception {
        Long id = seedFilm(false, false);

        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch commitCompleted = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(id)) {
                readStarted.countDown();
                assertThat(commitCompleted.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(id);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmView>> readerFuture = executor.submit(() -> filmOperations.findPublic(id));

            assertThat(readStarted.await(5, TimeUnit.SECONDS)).isTrue();

            // Commit concurrent NSFW update in Actor B
            jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", id);
            commitCompleted.countDown();

            Optional<PublicFilmView> result = readerFuture.get(5, TimeUnit.SECONDS);
            // In REPEATABLE_READ, snapshot was taken at projection query which returned eligible
            assertThat(result).isPresent();

            // Subsequent fresh request begins after commit -> denies
            Optional<PublicFilmView> freshResult = filmOperations.findPublic(id);
            assertThat(freshResult).isEmpty();

        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("B1-17: Forced atomic (non-NSFW, trashed) -> (NSFW, active) transition between reads never causes split-state grant")
    void forcedAtomicTransitionNeverSplitsState() throws Exception {
        Long id = seedFilm(false, true);

        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch commitCompleted = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(id)) {
                readStarted.countDown();
                assertThat(commitCompleted.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(id);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmView>> readerFuture = executor.submit(() -> filmOperations.findPublic(id));

            assertThat(readStarted.await(5, TimeUnit.SECONDS)).isTrue();

            // Atomic transition: (non-NSFW, trashed) -> (NSFW, active)
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                jdbcTemplate.update("UPDATE films SET is_nsfw = true WHERE id = ?", id);
                jdbcTemplate.update("UPDATE vault_entries SET deleted_at = null WHERE id = ?", id);
            });
            commitCompleted.countDown();

            Optional<PublicFilmView> result = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(result).isEmpty();

            // Subsequent fresh request sees is_nsfw = true -> denies
            Optional<PublicFilmView> freshResult = filmOperations.findPublic(id);
            assertThat(freshResult).isEmpty();

        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("B1-22: findPublic suspends weaker outer READ COMMITTED transaction, executes in dedicated physical REPEATABLE READ transaction, and outer resumes")
    void findPublicDemonstratesRepeatableReadTransaction() {
        Long id = seedFilm(false, false);

        AtomicReference<String> projectionIsolation = new AtomicReference<>();
        AtomicReference<String> projectionReadOnly = new AtomicReference<>();
        AtomicReference<Integer> projectionPid = new AtomicReference<>();
        AtomicBoolean innerContainsOuterEntityAtProjection = new AtomicBoolean(true);

        AtomicReference<String> vaultIsolation = new AtomicReference<>();
        AtomicReference<String> vaultReadOnly = new AtomicReference<>();
        AtomicReference<Integer> vaultPid = new AtomicReference<>();
        AtomicBoolean innerContainsOuterEntityAtVault = new AtomicBoolean(true);

        AtomicReference<Film> outerManagedRef = new AtomicReference<>();

        org.aopalliance.intercept.MethodInterceptor repoAdvice = invocation -> {
            if ("findPublicProjectionById".equals(invocation.getMethod().getName())) {
                Long argId = (Long) invocation.getArguments()[0];
                if (argId != null && argId.equals(id)) {
                    projectionReadOnly.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                    projectionIsolation.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    projectionPid.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    Film outerManaged = outerManagedRef.get();
                    if (outerManaged != null) {
                        innerContainsOuterEntityAtProjection.set(entityManager.contains(outerManaged));
                    }
                }
            }
            return invocation.proceed();
        };
        org.springframework.aop.framework.Advised advisedRepo = (org.springframework.aop.framework.Advised) filmRepository;
        advisedRepo.addAdvice(0, repoAdvice);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId != null && argId.equals(id)) {
                vaultReadOnly.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                vaultIsolation.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                vaultPid.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                Film outerManaged = outerManagedRef.get();
                if (outerManaged != null) {
                    innerContainsOuterEntityAtVault.set(entityManager.contains(outerManaged));
                }
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(id);

        TransactionTemplate outerTemplate = new TransactionTemplate(transactionManager);
        outerTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        outerTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);

        try {
            outerTemplate.executeWithoutResult(status -> {
                // 1. Outer transaction active with READ COMMITTED
                String outerIsolation = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer outerPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(outerIsolation).isEqualToIgnoringCase("read committed");

                // Genuinely load managed JPA entity in outer persistence context
                Film managedFilm = entityManager.find(Film.class, id);
                assertThat(managedFilm).isNotNull();
                assertThat(entityManager.contains(managedFilm)).isTrue();
                outerManagedRef.set(managedFilm);

                // 2. Execute findPublic -> suspends outer, runs in dedicated REQUIRES_NEW REPEATABLE READ
                Optional<PublicFilmView> result = filmOperations.findPublic(id);
                assertThat(result).isPresent();

                // Physical connection and isolation checks across owning projection and joined Vault read
                assertThat(projectionPid.get()).isNotNull();
                assertThat(vaultPid.get()).isNotNull();
                assertThat(projectionPid.get()).isEqualTo(vaultPid.get());
                assertThat(projectionPid.get()).isNotEqualTo(outerPid);

                assertThat(projectionIsolation.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(vaultIsolation.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(projectionReadOnly.get()).isEqualToIgnoringCase("on");
                assertThat(vaultReadOnly.get()).isEqualToIgnoringCase("on");

                // Inner context isolation: inner transaction did not manage outer entity
                assertThat(innerContainsOuterEntityAtProjection.get()).isFalse();
                assertThat(innerContainsOuterEntityAtVault.get()).isFalse();

                // 3. Outer transaction resumes unchanged with READ COMMITTED
                String resumedIsolation = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer resumedPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(resumedIsolation).isEqualToIgnoringCase("read committed");
                assertThat(resumedPid).isEqualTo(outerPid);

                // Resumed outer persistence context retains managed entity intact
                assertThat(entityManager.contains(managedFilm)).isTrue();
                assertThat(managedFilm.getId()).isEqualTo(id);
                assertThat(managedFilm.getTitle()).isEqualTo("FilmTitle-" + id);
            });
        } finally {
            advisedRepo.removeAdvice(repoAdvice);
            reset(vaultEntryOperations);
        }
    }

    @Test
    @DisplayName("B1-16: Controlled concurrent soft-delete during read and fresh request denial")
    void concurrentSoftDeleteDuringReadAndFreshRequestDenial() throws Exception {
        Long id = seedFilm(false, false);

        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch deleteCommitted = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(id)) {
                readStarted.countDown();
                assertThat(deleteCommitted.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(id);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFilmView>> readerFuture = executor.submit(() -> filmOperations.findPublic(id));

            assertThat(readStarted.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent actor commits soft-delete while reader is paused
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", id);
            });
            deleteCommitted.countDown();

            Optional<PublicFilmView> readerResult = readerFuture.get(5, TimeUnit.SECONDS);
            // REPEATABLE READ snapshot established before delete allows eligible snapshot to complete
            assertThat(readerResult).isPresent();
            assertThat(readerResult.get().id()).isEqualTo(id);

            // Fresh request beginning AFTER soft-delete commit sees deleted_at != null and denies
            Optional<PublicFilmView> freshResult = filmOperations.findPublic(id);
            assertThat(freshResult).isEmpty();
        } finally {
            reset(vaultEntryOperations);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("B1-15: Hidden and error paths do not invoke full find, classifications or enrichment, and fail closed")
    void findPublicNeverInvokesFullFindOrEnrichment() throws Exception {
        Long id = seedFilm(false, false);
        Long nsfwId = seedFilm(true, false);
        String publicToken = publicBearerToken();

        java.util.concurrent.atomic.AtomicInteger forbiddenRepoCalls = new java.util.concurrent.atomic.AtomicInteger();
        org.aopalliance.intercept.MethodInterceptor forbiddenInterceptor = invocation -> {
            String name = invocation.getMethod().getName();
            if ("findById".equals(name) || "findGenreIdsByFilmId".equals(name) || "findStoryArchetypeIdsByFilmId".equals(name) || "findWorldSettingIdsByFilmId".equals(name)) {
                forbiddenRepoCalls.incrementAndGet();
            }
            return invocation.proceed();
        };

        org.springframework.aop.framework.Advised advisedRepo = (org.springframework.aop.framework.Advised) filmRepository;
        org.springframework.aop.framework.Advised advisedGenre = (org.springframework.aop.framework.Advised) filmGenreAssignmentRepository;
        org.springframework.aop.framework.Advised advisedStory = (org.springframework.aop.framework.Advised) storyArchetypeRepository;
        org.springframework.aop.framework.Advised advisedWorld = (org.springframework.aop.framework.Advised) worldSettingRepository;

        advisedRepo.addAdvice(0, forbiddenInterceptor);
        advisedGenre.addAdvice(0, forbiddenInterceptor);
        advisedStory.addAdvice(0, forbiddenInterceptor);
        advisedWorld.addAdvice(0, forbiddenInterceptor);

        try {
            // Positive control: verify that the interceptor placed at index 0 actually intercepts each observed repository method
            forbiddenRepoCalls.set(0);
            filmRepository.findById(id);
            assertThat(forbiddenRepoCalls.get()).as("Positive control: findById must be intercepted").isEqualTo(1);

            filmGenreAssignmentRepository.findGenreIdsByFilmId(id);
            assertThat(forbiddenRepoCalls.get()).as("Positive control: findGenreIdsByFilmId must be intercepted").isEqualTo(2);

            storyArchetypeRepository.findStoryArchetypeIdsByFilmId(id);
            assertThat(forbiddenRepoCalls.get()).as("Positive control: findStoryArchetypeIdsByFilmId must be intercepted").isEqualTo(3);

            worldSettingRepository.findWorldSettingIdsByFilmId(id);
            assertThat(forbiddenRepoCalls.get()).as("Positive control: findWorldSettingIdsByFilmId must be intercepted").isEqualTo(4);

            // 1. Success path: findPublic returns minimal view and NEVER invokes full find or classification enrichment
            forbiddenRepoCalls.set(0);
            Mockito.clearInvocations(personOperations);
            Optional<PublicFilmView> result = filmOperations.findPublic(id);
            assertThat(result).isPresent();
            assertThat(forbiddenRepoCalls.get()).as("Forbidden repository calls (findById/genres/archetypes/settings) must be 0").isZero();
            verify(personOperations, never()).find(any());

            // HTTP level success returns 200 with exactly the 7 public fields and no sensitive enrichment
            mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.description").doesNotExist())
                    .andExpect(jsonPath("$.data.genreIds").doesNotExist());

            // 2. Hidden path (NSFW): findPublic returns empty and NEVER invokes full find or enrichment
            forbiddenRepoCalls.set(0);
            Mockito.clearInvocations(personOperations);
            Optional<PublicFilmView> hiddenResult = filmOperations.findPublic(nsfwId);
            assertThat(hiddenResult).isEmpty();
            assertThat(forbiddenRepoCalls.get()).isZero();
            verify(personOperations, never()).find(any());

            // HTTP level hidden read returns 404 with standard safe error without leaking private details
            mockMvc.perform(get("/api/v1/films/{id}", nsfwId).header("Authorization", "Bearer " + publicToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("FILM_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").doesNotExist());

            // 3. Failure path: injected DB failure in VaultEntryOperations fails closed, never calls enrichment
            forbiddenRepoCalls.set(0);
            Mockito.clearInvocations(personOperations);
            doThrow(new org.springframework.dao.QueryTimeoutException("Injected DB failure")).when(vaultEntryOperations).find(id);

            try {
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.dao.DataAccessException.class,
                        () -> filmOperations.findPublic(id)
                );
                assertThat(forbiddenRepoCalls.get()).isZero();
                verify(personOperations, never()).find(any());

                // Real HTTP safe failure returns handled 500 error envelope without exposing SQL, stack trace, or private fields
                mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + publicToken))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                        .andExpect(jsonPath("$.data").doesNotExist());
            } finally {
                reset(vaultEntryOperations);
            }
        } finally {
            advisedRepo.removeAdvice(forbiddenInterceptor);
            advisedGenre.removeAdvice(forbiddenInterceptor);
            advisedStory.removeAdvice(forbiddenInterceptor);
            advisedWorld.removeAdvice(forbiddenInterceptor);
        }
    }

    @Test
    @DisplayName("B1-19: PUBLIC success and 404 use no-store, no 304 on conditional headers, and cross-profile reads don't bleed")
    void cacheHeadersAndNoStoreVerification() throws Exception {
        Long id = seedFilm(false, false);
        String token = publicBearerToken();

        // 1. PUBLIC 200 has no-store
        mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        // 2. PUBLIC 404 has no-store
        mockMvc.perform(get("/api/v1/films/{id}", 9999999L).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        // 3. Conditional header does NOT return 304 on PUBLIC or PRIVATE
        mockMvc.perform(get("/api/v1/films/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                        .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(get("/api/v1/films/{id}", id)
                        .header("Authorization", bearerHeader())
                        .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                        .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                .andExpect(status().isOk());

        // 4. PRIVATE response does NOT bleed the controller's exact 'no-store' directive
        var privResp = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(privResp.getHeader(HttpHeaders.CACHE_CONTROL)).isNotEqualTo("no-store");

        // 5. Sequential reads on same URI across profiles do not contaminate or bleed cache
        var pubFirst = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(pubFirst).path("data").fieldNames()).toIterable().hasSize(7);

        var privSecond = mockMvc.perform(get("/api/v1/films/{id}", id).header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(privSecond).path("data").has("description")).isTrue();
    }
}
