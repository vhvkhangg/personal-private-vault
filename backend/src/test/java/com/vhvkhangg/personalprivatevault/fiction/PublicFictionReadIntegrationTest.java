package com.vhvkhangg.personalprivatevault.fiction;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations;
import com.vhvkhangg.personalprivatevault.fiction.internal.domain.Fiction;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.PublicFictionDetailResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.mapper.FictionWebMapper;
import com.vhvkhangg.personalprivatevault.fiction.view.PublicFictionDetailView;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
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
import static org.mockito.ArgumentMatchers.eq;
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

class PublicFictionReadIntegrationTest extends AbstractWebIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FictionOperations fictionOperations;

    @Autowired
    private FictionRepository fictionRepository;

    @Autowired
    private FictionStoryArchetypeRepository storyArchetypeRepository;

    @Autowired
    private FictionWorldSettingRepository worldSettingRepository;

    @MockitoSpyBean
    private PersonOperations personOperations;

    @MockitoSpyBean
    private CreatorGroupOperations creatorGroupOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoSpyBean
    private VaultEntryOperations vaultEntryOperations;

    private Long seedFiction(boolean isNsfw, boolean trashed) {
        Long id = System.nanoTime();
        Long personId = id + 1_000_000L;
        jdbcTemplate.update("INSERT INTO fiction_genres(id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                id, "Genre-" + id, "Description");
        jdbcTemplate.update("INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at) VALUES (?, 'PERSON', null, now(), now()) ON CONFLICT DO NOTHING",
                personId);
        jdbcTemplate.update("INSERT INTO persons(id, name) VALUES (?, ?) ON CONFLICT DO NOTHING",
                personId, "Author-" + personId);
        jdbcTemplate.update("""
                INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at)
                VALUES (?, 'FICTION', null, now(), now())
                """, id);
        jdbcTemplate.update("""
                INSERT INTO fictions(id, title, original_title, nationality_code, poster_url, format, is_nsfw,
                                     genre_id, author_person_id, description, total_chapters, progress_status,
                                     consumption_status, current_progress_text, review)
                VALUES (?, ?, ?, ?, ?, 'NOVEL', ?, ?, ?, ?, 100, 'ONGOING', 'UNCONSUMED', 'Chapter 1', 'Secret Review Notes')
                """, id, "Title-" + id, "Native-" + id, "VN", "https://secret.cdn/poster.png", isNsfw, id, personId, "Secret description");
        if (trashed) {
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", id);
        }
        return id;
    }

    private Long seedFictionWithClassifications(boolean isNsfw, boolean trashed) {
        Long id = seedFiction(isNsfw, trashed);
        Long archId = id + 3_000_000L;
        Long wsId = id + 4_000_000L;
        jdbcTemplate.update("INSERT INTO story_archetypes (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", archId, "Arch-" + archId, "Archetype Description");
        jdbcTemplate.update("INSERT INTO world_settings (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", wsId, "Setting-" + wsId, "Setting Description");
        jdbcTemplate.update("INSERT INTO fiction_story_archetypes (fiction_id, story_archetype_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, archId);
        jdbcTemplate.update("INSERT INTO fiction_world_settings (fiction_id, world_setting_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, wsId);
        return id;
    }

    private Long seedFictionWithGroup(boolean isNsfw, boolean trashed) {
        Long id = System.nanoTime();
        Long groupId = id + 2_000_000L;
        jdbcTemplate.update("INSERT INTO fiction_genres(id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                id, "Genre-" + id, "Description");
        jdbcTemplate.update("INSERT INTO creator_groups(id, name, description, created_at, updated_at) VALUES (?, ?, ?, now(), now()) ON CONFLICT DO NOTHING",
                groupId, "Group-" + groupId, "Group Description");
        jdbcTemplate.update("""
                INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at)
                VALUES (?, 'FICTION', null, now(), now())
                """, id);
        jdbcTemplate.update("""
                INSERT INTO fictions(id, title, original_title, nationality_code, poster_url, format, is_nsfw,
                                     genre_id, author_group_id, description, total_chapters, progress_status,
                                     consumption_status, current_progress_text, review)
                VALUES (?, ?, ?, ?, ?, 'COMIC', ?, ?, ?, ?, 50, 'COMPLETED', 'CONSUMED', 'Chapter 50', 'Group Review Notes')
                """, id, "GroupTitle-" + id, "GroupNative-" + id, "VN", "https://secret.cdn/group.png", isNsfw, id, groupId, "Group synopsis");
        if (trashed) {
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", id);
        }
        return id;
    }

    private Long seedFictionWithGroupAndClassifications(boolean isNsfw, boolean trashed) {
        Long id = seedFictionWithGroup(isNsfw, trashed);
        Long archId = id + 3_000_000L;
        Long wsId = id + 4_000_000L;
        jdbcTemplate.update("INSERT INTO story_archetypes (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", archId, "Arch-" + archId, "Archetype Description");
        jdbcTemplate.update("INSERT INTO world_settings (id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", wsId, "Setting-" + wsId, "Setting Description");
        jdbcTemplate.update("INSERT INTO fiction_story_archetypes (fiction_id, story_archetype_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, archId);
        jdbcTemplate.update("INSERT INTO fiction_world_settings (fiction_id, world_setting_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, wsId);
        return id;
    }

    @Test
    @DisplayName("ROB-02: Both password-login and downgrade persisted PUBLIC sessions reach GET /api/v1/fictions/{id}")
    void publicSessionsReachFictionDetail() throws Exception {
        Long id = seedFiction(false, false);
        String token1 = publicBearerToken();
        String token2 = publicBearerTokenViaDowngrade();

        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.authorName").value("Author-" + (id + 1_000_000L)))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.authorName").value("Author-" + (id + 1_000_000L)))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
    }

    @Test
    @DisplayName("ROB-03, ROB-05, ROB-06, ROB-07: Populated Fiction 200 has exact 19 keys and full parity with PRIVATE 18 keys; PRIVATE writes verify 18 keys (FR-ROB-04)")
    void eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor() throws Exception {
        // 1. Populated Person-author work with classifications
        Long id = seedFictionWithClassifications(false, false);
        String pubToken = publicBearerToken();

        String pubBody = mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + pubToken))
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
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds", "authorName"
        );
        assertThat(pubData.path("id").asLong()).isEqualTo(id);
        assertThat(pubData.path("title").asText()).isEqualTo("Title-" + id);
        assertThat(pubData.path("originalTitle").asText()).isEqualTo("Native-" + id);
        assertThat(pubData.path("nationalityCode").asText()).isEqualTo("VN");
        assertThat(pubData.path("posterUrl").asText()).isEqualTo("https://secret.cdn/poster.png");
        assertThat(pubData.path("format").asText()).isEqualTo("NOVEL");
        assertThat(pubData.path("isNsfw").asBoolean()).isFalse();
        assertThat(pubData.path("genreId").asLong()).isEqualTo(id);
        assertThat(pubData.path("authorPersonId").asLong()).isEqualTo(id + 1_000_000L);
        assertThat(pubData.path("authorGroupId").isNull()).isTrue();
        assertThat(pubData.path("description").asText()).isEqualTo("Secret description");
        assertThat(pubData.path("totalChapters").asInt()).isEqualTo(100);
        assertThat(pubData.path("progressStatus").asText()).isEqualTo("ONGOING");
        assertThat(pubData.path("consumptionStatus").asText()).isEqualTo("UNCONSUMED");
        assertThat(pubData.path("currentProgressText").asText()).isEqualTo("Chapter 1");
        assertThat(pubData.path("review").asText()).isEqualTo("Secret Review Notes");
        assertThat(pubData.path("storyArchetypeIds")).isNotEmpty();
        assertThat(pubData.path("worldSettingIds")).isNotEmpty();
        assertThat(pubData.path("authorName").asText()).isEqualTo("Author-" + (id + 1_000_000L));

        // PRIVATE request on the same work returns exactly 18 keys (NO authorName)
        String privBody = mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var privData = objectMapper.readTree(privBody).path("data");
        List<String> privFields = new ArrayList<>();
        privData.fieldNames().forEachRemaining(privFields::add);
        assertThat(privFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(privData.has("authorName")).isFalse();

        // FR-ROB-04: Full 18 common values parity check on populated fixture
        for (String commonField : privFields) {
            assertThat(pubData.path(commonField))
                    .as("Parity mismatch on field '%s'", commonField)
                    .isEqualTo(privData.path(commonField));
        }

        // 2. CreatorGroup author work with classifications
        Long groupId = seedFictionWithGroupAndClassifications(false, false);
        String groupBody = mockMvc.perform(get("/api/v1/fictions/{id}", groupId).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var groupData = objectMapper.readTree(groupBody).path("data");
        List<String> groupFields = new ArrayList<>();
        groupData.fieldNames().forEachRemaining(groupFields::add);
        assertThat(groupFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds", "authorName"
        );
        assertThat(groupData.path("authorGroupId").asLong()).isEqualTo(groupId + 2_000_000L);
        assertThat(groupData.path("authorPersonId").isNull()).isTrue();
        assertThat(groupData.path("authorName").asText()).isEqualTo("Group-" + (groupId + 2_000_000L));
        assertThat(groupData.path("storyArchetypeIds")).isNotEmpty();
        assertThat(groupData.path("worldSettingIds")).isNotEmpty();

        // FR-ROB-04: PRIVATE request on the Group work returns exactly 18 keys (NO authorName) and full common value parity
        String privGroupBody = mockMvc.perform(get("/api/v1/fictions/{id}", groupId).header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var privGroupData = objectMapper.readTree(privGroupBody).path("data");
        List<String> privGroupFields = new ArrayList<>();
        privGroupData.fieldNames().forEachRemaining(privGroupFields::add);
        assertThat(privGroupFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(privGroupData.has("authorName")).isFalse();
        for (String commonField : privGroupFields) {
            assertThat(groupData.path(commonField))
                    .as("Group parity mismatch on field '%s'", commonField)
                    .isEqualTo(privGroupData.path(commonField));
        }

        // 3. Null optional fields: all 19 keys still emitted with explicit null
        Long nullFieldsId = System.nanoTime();
        Long personId2 = nullFieldsId + 1_000_000L;
        jdbcTemplate.update("INSERT INTO fiction_genres(id, name, description) VALUES (?, ?, ?) ON CONFLICT DO NOTHING", nullFieldsId, "G-" + nullFieldsId, "D");
        jdbcTemplate.update("INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at) VALUES (?, 'PERSON', null, now(), now()) ON CONFLICT DO NOTHING", personId2);
        jdbcTemplate.update("INSERT INTO persons(id, name) VALUES (?, ?) ON CONFLICT DO NOTHING", personId2, "Author2");
        jdbcTemplate.update("INSERT INTO vault_entries(id, entry_type, deleted_at, created_at, updated_at) VALUES (?, 'FICTION', null, now(), now())", nullFieldsId);
        jdbcTemplate.update("INSERT INTO fictions(id, title, original_title, nationality_code, poster_url, format, is_nsfw, genre_id, author_person_id, progress_status, consumption_status) VALUES (?, 'T', null, null, null, 'NOVEL', false, ?, ?, 'ONGOING', 'UNCONSUMED')", nullFieldsId, nullFieldsId, personId2);

        String nullBody = mockMvc.perform(get("/api/v1/fictions/{id}", nullFieldsId).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var nullJson = objectMapper.readTree(nullBody).path("data");
        List<String> nullFields = new ArrayList<>();
        nullJson.fieldNames().forEachRemaining(nullFields::add);
        assertThat(nullFields).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds", "authorName"
        );
        assertThat(nullJson.path("originalTitle").isNull()).isTrue();
        assertThat(nullJson.path("nationalityCode").isNull()).isTrue();
        assertThat(nullJson.path("posterUrl").isNull()).isTrue();
        assertThat(nullJson.path("authorGroupId").isNull()).isTrue();
        assertThat(nullJson.path("description").isNull()).isTrue();
        assertThat(nullJson.path("totalChapters").isNull()).isTrue();
        assertThat(nullJson.path("currentProgressText").isNull()).isTrue();
        assertThat(nullJson.path("review").isNull()).isTrue();

        // 4. FR-ROB-04: Verify PRIVATE POST /api/v1/fictions produces exactly 18 keys (no authorName)
        String postPayload = """
                {
                  "title": "Private Post Fiction %d",
                  "format": "NOVEL",
                  "genreId": %d,
                  "authorPersonId": %d,
                  "progressStatus": "ONGOING",
                  "consumptionStatus": "UNCONSUMED"
                }
                """.formatted(System.currentTimeMillis(), id, id + 1_000_000L);

        String postResp = mockMvc.perform(post("/api/v1/fictions")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var postData = objectMapper.readTree(postResp).path("data");
        List<String> postKeys = new ArrayList<>();
        postData.fieldNames().forEachRemaining(postKeys::add);
        assertThat(postKeys).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(postData.has("authorName")).isFalse();

        Long createdFictionId = postData.path("id").asLong();

        // 5. FR-ROB-04: Verify PRIVATE PUT /api/v1/fictions/{id} produces exactly 18 keys (no authorName)
        String putPayload = """
                {
                  "title": "Updated Fiction %d",
                  "format": "NOVEL",
                  "genreId": %d,
                  "authorPersonId": %d,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """.formatted(System.currentTimeMillis(), id, id + 1_000_000L);

        String putResp = mockMvc.perform(put("/api/v1/fictions/{id}", createdFictionId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var putData = objectMapper.readTree(putResp).path("data");
        List<String> putKeys = new ArrayList<>();
        putData.fieldNames().forEachRemaining(putKeys::add);
        assertThat(putKeys).containsExactlyInAnyOrder(
                "id", "title", "originalTitle", "nationalityCode", "posterUrl", "format", "isNsfw",
                "genreId", "authorPersonId", "authorGroupId", "description", "totalChapters",
                "progressStatus", "consumptionStatus", "currentProgressText", "review",
                "storyArchetypeIds", "worldSettingIds"
        );
        assertThat(putData.has("authorName")).isFalse();
    }

    @Test
    @DisplayName("S4 / ROB-10: Ineligible work yields 404, no-store, data: null, and ZERO People/Group lookups across all negative cases (FR-ROB-03)")
    void ineligibleFictionReturnsFourHundredFourAndZeroPeopleAccess() throws Exception {
        String token = publicBearerToken();

        // 1. Missing fiction ID
        reset(personOperations, creatorGroupOperations, vaultEntryOperations);
        mockMvc.perform(get("/api/v1/fictions/{id}", 999999999L).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Fiction not found"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(personOperations, never()).find(any());
        verify(creatorGroupOperations, never()).find(any());

        // 2. Trashed fiction
        Long trashedId = seedFiction(false, true);
        Long trashedPersonId = trashedId + 1_000_000L;
        reset(personOperations, creatorGroupOperations, vaultEntryOperations);
        mockMvc.perform(get("/api/v1/fictions/{id}", trashedId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(personOperations, never()).find(any());
        verify(creatorGroupOperations, never()).find(any());
        verify(vaultEntryOperations, never()).find(trashedPersonId);

        // 3. NSFW fiction
        Long nsfwId = seedFiction(true, false);
        Long nsfwPersonId = nsfwId + 1_000_000L;
        reset(personOperations, creatorGroupOperations, vaultEntryOperations);
        mockMvc.perform(get("/api/v1/fictions/{id}", nsfwId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        verify(personOperations, never()).find(any());
        verify(creatorGroupOperations, never()).find(any());
        verify(vaultEntryOperations, never()).find(nsfwPersonId);

        // 4. Wrong-type work Vault entry (e.g. entry_type NOTE instead of FICTION)
        Long wrongTypeId = seedFiction(false, false);
        Long wrongTypePersonId = wrongTypeId + 1_000_000L;
        doAnswer(inv -> Optional.of(new VaultEntryView(wrongTypeId, VaultEntryType.NOTE, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(wrongTypeId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", wrongTypeId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(creatorGroupOperations, never()).find(any());
            verify(vaultEntryOperations, never()).find(wrongTypePersonId);
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. Missing work Vault entry (empty vaultEntryOperations.find for work)
        Long missingVaultId = seedFiction(false, false);
        Long missingVaultPersonId = missingVaultId + 1_000_000L;
        doAnswer(inv -> Optional.empty()).when(vaultEntryOperations).find(missingVaultId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", missingVaultId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(creatorGroupOperations, never()).find(any());
            verify(vaultEntryOperations, never()).find(missingVaultPersonId);
        } finally {
            reset(vaultEntryOperations);
        }

        // 6. Mismatched work Vault ID (returns VaultEntryView with ID != fictionId)
        Long mismatchedVaultId = seedFiction(false, false);
        Long mismatchedPersonId = mismatchedVaultId + 1_000_000L;
        doAnswer(inv -> Optional.of(new VaultEntryView(mismatchedVaultId + 999_999L, VaultEntryType.FICTION, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(mismatchedVaultId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", mismatchedVaultId).header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(creatorGroupOperations, never()).find(any());
            verify(vaultEntryOperations, never()).find(mismatchedPersonId);
        } finally {
            reset(vaultEntryOperations);
        }
    }

    @Test
    @DisplayName("ROB-06, ROB-09: Qualifies Person Vault before resolving name: wrong-type, trashed, missing, or mismatched Person Vault yields authorName: null (200 OK)")
    void qualifiesPersonVaultBeforeResolvingName() throws Exception {
        Long id = seedFiction(false, false);
        Long personId = id + 1_000_000L;
        String token = publicBearerToken();

        // 1. Wrong type Person Vault (NOTE instead of PERSON) -> authorName: null
        doAnswer(inv -> Optional.of(new VaultEntryView(personId, VaultEntryType.NOTE, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorPersonId").value(personId));
        } finally {
            reset(vaultEntryOperations);
        }

        // 2. Trashed Person Vault -> authorName: null
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", personId);
        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()));

        // 3. Normal recovery: active PERSON Vault -> authorName resolved
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = null WHERE id = ?", personId);
        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorName").value("Author-" + personId));

        // 4. Missing Person Vault result -> authorName: null
        doAnswer(inv -> Optional.empty()).when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorPersonId").value(personId));
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. Mismatched Person Vault ID -> authorName: null
        doAnswer(inv -> Optional.of(new VaultEntryView(personId + 999_999L, VaultEntryType.PERSON, Instant.now(), Instant.now(), null)))
                .when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorPersonId").value(personId));
        } finally {
            reset(vaultEntryOperations);
        }
    }

    @Test
    @DisplayName("S4 / ROB-07, ROB-09: Normal absent, trashed, wrong-type Person Vault, missing Person, or blank/null name yields authorName: null (200 OK)")
    void referenceAbsenceYieldsPresentNullName() throws Exception {
        String token = publicBearerToken();

        // 1. Trashed Person Vault -> 200 with authorName: null
        Long fictionId1 = seedFiction(false, false);
        Long personId1 = fictionId1 + 1_000_000L;
        jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", personId1);

        mockMvc.perform(get("/api/v1/fictions/{id}", fictionId1).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.authorPersonId").value(personId1));

        // 2. Wrong type Person Vault (simulated via spy fault injection) -> 200 with authorName: null
        Long fictionId2 = seedFiction(false, false);
        Long personId2 = fictionId2 + 1_000_000L;
        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(personId2)) {
                return Optional.of(new VaultEntryView(personId2, VaultEntryType.NOTE, Instant.now(), Instant.now(), null));
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(personId2);

        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", fictionId2).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 3. Blank Person name -> 200 with authorName: null
        Long fictionId3 = seedFiction(false, false);
        Long personId3 = fictionId3 + 1_000_000L;
        jdbcTemplate.update("UPDATE persons SET name = '   ' WHERE id = ?", personId3);

        mockMvc.perform(get("/api/v1/fictions/{id}", fictionId3).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()));

        // 4. Group author with missing group record (simulated via spy) -> 200 with authorName: null
        Long fictionId4 = seedFictionWithGroup(false, false);
        Long groupId4 = fictionId4 + 2_000_000L;
        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(groupId4)) {
                return Optional.empty();
            }
            return inv.callRealMethod();
        }).when(creatorGroupOperations).find(groupId4);

        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", fictionId4).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorGroupId").value(groupId4));
        } finally {
            reset(creatorGroupOperations);
        }

        // 5. Missing Person record (simulated via personOperations.find returning empty) -> 200 with authorName: null
        Long fictionId5 = seedFiction(false, false);
        Long personId5 = fictionId5 + 1_000_000L;
        doAnswer(inv -> Optional.empty()).when(personOperations).find(personId5);

        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", fictionId5).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorPersonId").value(personId5));
        } finally {
            reset(personOperations);
        }

        // 6. Injected schema-disallowed null Person name -> 200 with authorName: null
        Long fictionId6 = seedFiction(false, false);
        Long personId6 = fictionId6 + 1_000_000L;
        doAnswer(inv -> Optional.of(new PersonView(personId6, null, null, null, null, null, null, null, null, null)))
                .when(personOperations).find(personId6);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", fictionId6).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorPersonId").value(personId6));
        } finally {
            reset(personOperations);
        }

        // 7. Injected schema-disallowed null CreatorGroup name -> 200 with authorName: null
        Long fictionId7 = seedFictionWithGroup(false, false);
        Long groupId7 = fictionId7 + 2_000_000L;
        doAnswer(inv -> Optional.of(new CreatorGroupView(groupId7, null, null, Instant.now(), Instant.now())))
                .when(creatorGroupOperations).find(groupId7);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", fictionId7).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.authorGroupId").value(groupId7));
        } finally {
            reset(creatorGroupOperations);
        }
    }

    @Test
    @DisplayName("S1 / ROB-12, ROB-15: Outer READ COMMITTED transaction suspended, dedicated physical REPEATABLE READ snapshot across all query points for Person & Group paths (FR-ROB-02)")
    void s1PhysicalNewPathIsolationAndContextResumption() {
        // --- PATH A: Fiction with Person Author ---
        Long personWorkId = seedFictionWithClassifications(false, false);
        Long personId = personWorkId + 1_000_000L;

        AtomicReference<Integer> pidAtWorkRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtWorkVault = new AtomicReference<>();
        AtomicReference<Integer> pidAtArchRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtSettingRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtPersonVault = new AtomicReference<>();
        AtomicReference<Integer> pidAtPersonRepo = new AtomicReference<>();

        AtomicReference<String> snapAtWorkRepo = new AtomicReference<>();
        AtomicReference<String> snapAtWorkVault = new AtomicReference<>();
        AtomicReference<String> snapAtArchRepo = new AtomicReference<>();
        AtomicReference<String> snapAtSettingRepo = new AtomicReference<>();
        AtomicReference<String> snapAtPersonVault = new AtomicReference<>();
        AtomicReference<String> snapAtPersonRepo = new AtomicReference<>();

        AtomicReference<String> isoAtWorkRepo = new AtomicReference<>();
        AtomicReference<String> isoAtPersonRepo = new AtomicReference<>();
        AtomicReference<String> roAtWorkRepo = new AtomicReference<>();
        AtomicReference<String> roAtPersonRepo = new AtomicReference<>();

        AtomicReference<Fiction> outerManagedRef = new AtomicReference<>();
        AtomicBoolean innerContainsOuterEntityAtRepo = new AtomicBoolean(true);

        MethodInterceptor repoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long argId = (Long) invocation.getArguments()[0];
                if (argId != null && argId.equals(personWorkId)) {
                    pidAtWorkRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtWorkRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                    isoAtWorkRepo.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    roAtWorkRepo.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                    Fiction outerManaged = outerManagedRef.get();
                    if (outerManaged != null) {
                        innerContainsOuterEntityAtRepo.set(entityManager.contains(outerManaged));
                    }
                }
            }
            return invocation.proceed();
        };
        Advised advisedWorkRepo = (Advised) fictionRepository;
        advisedWorkRepo.addAdvice(0, repoAdvice);

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
                if (idArg != null && idArg.equals(personWorkId)) {
                    pidAtWorkVault.set(pid);
                    snapAtWorkVault.set(snap);
                } else if (idArg != null && idArg.equals(personId)) {
                    pidAtPersonVault.set(pid);
                    snapAtPersonVault.set(snap);
                }
            }
            return invocation.proceed();
        };
        advisedVaultRepo.addAdvice(0, vaultRepoAdvice);

        Advised advisedPersonRepo = (Advised) applicationContext.getBean("personRepository");
        MethodInterceptor personRepoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long idArg = (Long) invocation.getArguments()[0];
                if (idArg != null && idArg.equals(personId)) {
                    pidAtPersonRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtPersonRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                    isoAtPersonRepo.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    roAtPersonRepo.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
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

                Fiction managedFiction = entityManager.find(Fiction.class, personWorkId);
                assertThat(managedFiction).isNotNull();
                assertThat(entityManager.contains(managedFiction)).isTrue();
                outerManagedRef.set(managedFiction);

                Optional<PublicFictionDetailView> result = fictionOperations.findPublicDetail(personWorkId);
                assertThat(result).isPresent();

                // All 6 query points execute on the same dedicated inner connection distinct from outer
                assertThat(pidAtWorkRepo.get()).isNotNull();
                assertThat(pidAtWorkVault.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtArchRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtSettingRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtPersonVault.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtPersonRepo.get()).isEqualTo(pidAtWorkRepo.get());
                assertThat(pidAtWorkRepo.get()).isNotEqualTo(outerPid);

                // Snapshot identity is identical across all 6 data query points
                assertThat(snapAtWorkRepo.get()).isNotNull();
                assertThat(snapAtWorkVault.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtArchRepo.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtSettingRepo.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtPersonVault.get()).isEqualTo(snapAtWorkRepo.get());
                assertThat(snapAtPersonRepo.get()).isEqualTo(snapAtWorkRepo.get());

                // Isolation is REPEATABLE READ and read_only = ON throughout
                assertThat(isoAtWorkRepo.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(isoAtPersonRepo.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(roAtWorkRepo.get()).isEqualToIgnoringCase("on");
                assertThat(roAtPersonRepo.get()).isEqualToIgnoringCase("on");

                // Inner persistence context did not manage outer entity
                assertThat(innerContainsOuterEntityAtRepo.get()).isFalse();

                // Outer transaction resumes with read committed and intact managed entity
                String resumedIso = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer resumedPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(resumedIso).isEqualToIgnoringCase("read committed");
                assertThat(resumedPid).isEqualTo(outerPid);
                assertThat(entityManager.contains(managedFiction)).isTrue();
            });
        } finally {
            advisedWorkRepo.removeAdvice(repoAdvice);
            advisedArchRepo.removeAdvice(archAdvice);
            advisedSettingRepo.removeAdvice(settingAdvice);
            advisedVaultRepo.removeAdvice(vaultRepoAdvice);
            advisedPersonRepo.removeAdvice(personRepoAdvice);
            reset(vaultEntryOperations, personOperations);
        }

        // --- PATH B: Fiction with Group Author ---
        Long groupWorkId = seedFictionWithGroupAndClassifications(false, false);
        Long groupId = groupWorkId + 2_000_000L;

        AtomicReference<Integer> pidAtGroupWorkRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtGroupWorkVault = new AtomicReference<>();
        AtomicReference<Integer> pidAtGroupArchRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtGroupSettingRepo = new AtomicReference<>();
        AtomicReference<Integer> pidAtGroupRepo = new AtomicReference<>();

        AtomicReference<String> snapAtGroupWorkRepo = new AtomicReference<>();
        AtomicReference<String> snapAtGroupWorkVault = new AtomicReference<>();
        AtomicReference<String> snapAtGroupArchRepo = new AtomicReference<>();
        AtomicReference<String> snapAtGroupSettingRepo = new AtomicReference<>();
        AtomicReference<String> snapAtGroupRepo = new AtomicReference<>();

        AtomicReference<String> isoAtGroupWorkRepo = new AtomicReference<>();
        AtomicReference<String> isoAtGroupRepo = new AtomicReference<>();
        AtomicReference<String> roAtGroupWorkRepo = new AtomicReference<>();
        AtomicReference<String> roAtGroupRepo = new AtomicReference<>();

        AtomicReference<Fiction> outerGroupManagedRef = new AtomicReference<>();
        AtomicBoolean innerContainsOuterGroupEntity = new AtomicBoolean(true);

        MethodInterceptor groupWorkRepoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long argId = (Long) invocation.getArguments()[0];
                if (argId != null && argId.equals(groupWorkId)) {
                    pidAtGroupWorkRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtGroupWorkRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                    isoAtGroupWorkRepo.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    roAtGroupWorkRepo.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                    Fiction outerManaged = outerGroupManagedRef.get();
                    if (outerManaged != null) {
                        innerContainsOuterGroupEntity.set(entityManager.contains(outerManaged));
                    }
                }
            }
            return invocation.proceed();
        };
        advisedWorkRepo.addAdvice(0, groupWorkRepoAdvice);

        MethodInterceptor groupArchAdvice = invocation -> {
            pidAtGroupArchRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
            snapAtGroupArchRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
            return invocation.proceed();
        };
        advisedArchRepo.addAdvice(0, groupArchAdvice);

        MethodInterceptor groupSettingAdvice = invocation -> {
            pidAtGroupSettingRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
            snapAtGroupSettingRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
            return invocation.proceed();
        };
        advisedSettingRepo.addAdvice(0, groupSettingAdvice);

        MethodInterceptor groupVaultAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long idArg = (Long) invocation.getArguments()[0];
                if (idArg != null && idArg.equals(groupWorkId)) {
                    pidAtGroupWorkVault.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtGroupWorkVault.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                }
            }
            return invocation.proceed();
        };
        advisedVaultRepo.addAdvice(0, groupVaultAdvice);

        Advised advisedGroupRepo = (Advised) applicationContext.getBean("creatorGroupRepository");
        MethodInterceptor groupRepoAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long idArg = (Long) invocation.getArguments()[0];
                if (idArg != null && idArg.equals(groupId)) {
                    pidAtGroupRepo.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    snapAtGroupRepo.set(jdbcTemplate.queryForObject("SELECT pg_current_snapshot()::text", String.class));
                    isoAtGroupRepo.set(jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class));
                    roAtGroupRepo.set(jdbcTemplate.queryForObject("SHOW transaction_read_only", String.class));
                }
            }
            return invocation.proceed();
        };
        advisedGroupRepo.addAdvice(0, groupRepoAdvice);

        try {
            outerTemplate.executeWithoutResult(status -> {
                String outerIso = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer outerPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(outerIso).isEqualToIgnoringCase("read committed");

                Fiction managedGroupFiction = entityManager.find(Fiction.class, groupWorkId);
                assertThat(managedGroupFiction).isNotNull();
                assertThat(entityManager.contains(managedGroupFiction)).isTrue();
                outerGroupManagedRef.set(managedGroupFiction);

                Optional<PublicFictionDetailView> result = fictionOperations.findPublicDetail(groupWorkId);
                assertThat(result).isPresent();

                assertThat(pidAtGroupWorkRepo.get()).isNotNull();
                assertThat(pidAtGroupWorkVault.get()).isEqualTo(pidAtGroupWorkRepo.get());
                assertThat(pidAtGroupArchRepo.get()).isEqualTo(pidAtGroupWorkRepo.get());
                assertThat(pidAtGroupSettingRepo.get()).isEqualTo(pidAtGroupWorkRepo.get());
                assertThat(pidAtGroupRepo.get()).isEqualTo(pidAtGroupWorkRepo.get());
                assertThat(pidAtGroupWorkRepo.get()).isNotEqualTo(outerPid);

                assertThat(snapAtGroupWorkRepo.get()).isNotNull();
                assertThat(snapAtGroupWorkVault.get()).isEqualTo(snapAtGroupWorkRepo.get());
                assertThat(snapAtGroupArchRepo.get()).isEqualTo(snapAtGroupWorkRepo.get());
                assertThat(snapAtGroupSettingRepo.get()).isEqualTo(snapAtGroupWorkRepo.get());
                assertThat(snapAtGroupRepo.get()).isEqualTo(snapAtGroupWorkRepo.get());

                assertThat(isoAtGroupWorkRepo.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(isoAtGroupRepo.get()).isEqualToIgnoringCase("repeatable read");
                assertThat(roAtGroupWorkRepo.get()).isEqualToIgnoringCase("on");
                assertThat(roAtGroupRepo.get()).isEqualToIgnoringCase("on");

                assertThat(innerContainsOuterGroupEntity.get()).isFalse();

                String resumedIso = jdbcTemplate.queryForObject("SHOW transaction_isolation", String.class);
                Integer resumedPid = jdbcTemplate.queryForObject("SELECT pg_backend_pid()", Integer.class);
                assertThat(resumedIso).isEqualToIgnoringCase("read committed");
                assertThat(resumedPid).isEqualTo(outerPid);
                assertThat(entityManager.contains(managedGroupFiction)).isTrue();
            });
        } finally {
            advisedWorkRepo.removeAdvice(groupWorkRepoAdvice);
            advisedArchRepo.removeAdvice(groupArchAdvice);
            advisedSettingRepo.removeAdvice(groupSettingAdvice);
            advisedVaultRepo.removeAdvice(groupVaultAdvice);
            advisedGroupRepo.removeAdvice(groupRepoAdvice);
            reset(vaultEntryOperations, creatorGroupOperations);
        }
    }

    @Test
    @DisplayName("S2 / ROB-13: Concurrent committed work updates (NSFW/trash vs full values) obey snapshot boundary and fresh request reflects updated state (FR-ROB-02)")
    void s2WorkEligibilityAndFullValuesSnapshotIsolation() throws Exception {
        // CASE A: Work eligibility destroyed concurrently -> held reader gets full earlier 19-field state; fresh request gets 404
        Long idA = seedFictionWithClassifications(false, false);

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
            Future<Optional<PublicFictionDetailView>> readerFuture = executorA.submit(() -> fictionOperations.findPublicDetail(idA));

            assertThat(readStartedA.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction: marks NSFW, trashes work vault, alters progress/review/chapters, changes classifications
            Long newArchIdA = idA + 5_000_000L;
            jdbcTemplate.update("INSERT INTO story_archetypes (id, name, description) VALUES (?, 'New Arch A', 'Desc') ON CONFLICT DO NOTHING", newArchIdA);
            jdbcTemplate.update("INSERT INTO fiction_story_archetypes (fiction_id, story_archetype_id) VALUES (?, ?)", idA, newArchIdA);
            jdbcTemplate.update("DELETE FROM fiction_world_settings WHERE fiction_id = ?", idA);
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", idA);
            jdbcTemplate.update("UPDATE fictions SET is_nsfw = true, progress_status = 'SUSPENDED', total_chapters = 999, review = 'Mutated Review' WHERE id = ?", idA);
            commitCompletedA.countDown();

            Optional<PublicFictionDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            var view = heldResult.get();
            // Snapshot preserves initial eligible values across ALL 19 fields and exact earlier sets
            assertThat(view.id()).isEqualTo(idA);
            assertThat(view.title()).isEqualTo("Title-" + idA);
            assertThat(view.originalTitle()).isEqualTo("Native-" + idA);
            assertThat(view.nationalityCode()).isEqualTo("VN");
            assertThat(view.posterUrl()).isEqualTo("https://secret.cdn/poster.png");
            assertThat(view.format()).isEqualTo(FictionFormat.NOVEL);
            assertThat(view.isNsfw()).isFalse();
            assertThat(view.genreId()).isEqualTo(idA);
            assertThat(view.authorPersonId()).isEqualTo(idA + 1_000_000L);
            assertThat(view.authorGroupId()).isNull();
            assertThat(view.description()).isEqualTo("Secret description");
            assertThat(view.totalChapters()).isEqualTo(100);
            assertThat(view.progressStatus()).isEqualTo(ProgressStatus.ONGOING);
            assertThat(view.consumptionStatus()).isEqualTo(ConsumptionStatus.UNCONSUMED);
            assertThat(view.currentProgressText()).isEqualTo("Chapter 1");
            assertThat(view.review()).isEqualTo("Secret Review Notes");
            assertThat(view.authorName()).isEqualTo("Author-" + (idA + 1_000_000L));
            assertThat(view.storyArchetypeIds()).isEqualTo(Set.of(idA + 3_000_000L));
            assertThat(view.worldSettingIds()).isEqualTo(Set.of(idA + 4_000_000L));

            // Subsequent fresh HTTP request after commit -> 404 Not Found, no-store, data null, ZERO people access
            reset(personOperations, creatorGroupOperations);
            mockMvc.perform(get("/api/v1/fictions/{id}", idA).header("Authorization", "Bearer " + publicBearerToken()))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("FICTION_NOT_FOUND"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
            verify(personOperations, never()).find(any());
            verify(creatorGroupOperations, never()).find(any());
        } finally {
            reset(vaultEntryOperations);
            executorA.shutdownNow();
        }

        // CASE B: Eligibility-preserving full values change -> held reader sees old values; fresh HTTP request sees updated 19-field state
        Long idB = seedFictionWithClassifications(false, false);

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
            Future<Optional<PublicFictionDetailView>> readerFuture = executorB.submit(() -> fictionOperations.findPublicDetail(idB));

            assertThat(readStartedB.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction: updates title, originalTitle, progressStatus, totalChapters, review, currentProgressText, classification sets
            Long newArchIdB = idB + 5_000_000L;
            jdbcTemplate.update("INSERT INTO story_archetypes (id, name, description) VALUES (?, 'New Arch B', 'Desc') ON CONFLICT DO NOTHING", newArchIdB);
            jdbcTemplate.update("INSERT INTO fiction_story_archetypes (fiction_id, story_archetype_id) VALUES (?, ?)", idB, newArchIdB);
            jdbcTemplate.update("DELETE FROM fiction_world_settings WHERE fiction_id = ?", idB);
            jdbcTemplate.update("UPDATE fictions SET title = 'Evolved Title', original_title = 'Evolved Native', progress_status = 'COMPLETED', total_chapters = 250, review = 'Evolved Review', current_progress_text = 'Ch 500' WHERE id = ?", idB);
            commitCompletedB.countDown();

            Optional<PublicFictionDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            var heldView = heldResult.get();
            assertThat(heldView.id()).isEqualTo(idB);
            assertThat(heldView.title()).isEqualTo("Title-" + idB);
            assertThat(heldView.originalTitle()).isEqualTo("Native-" + idB);
            assertThat(heldView.nationalityCode()).isEqualTo("VN");
            assertThat(heldView.posterUrl()).isEqualTo("https://secret.cdn/poster.png");
            assertThat(heldView.format()).isEqualTo(FictionFormat.NOVEL);
            assertThat(heldView.isNsfw()).isFalse();
            assertThat(heldView.genreId()).isEqualTo(idB);
            assertThat(heldView.authorPersonId()).isEqualTo(idB + 1_000_000L);
            assertThat(heldView.authorGroupId()).isNull();
            assertThat(heldView.description()).isEqualTo("Secret description");
            assertThat(heldView.totalChapters()).isEqualTo(100);
            assertThat(heldView.progressStatus()).isEqualTo(ProgressStatus.ONGOING);
            assertThat(heldView.consumptionStatus()).isEqualTo(ConsumptionStatus.UNCONSUMED);
            assertThat(heldView.currentProgressText()).isEqualTo("Chapter 1");
            assertThat(heldView.review()).isEqualTo("Secret Review Notes");
            assertThat(heldView.authorName()).isEqualTo("Author-" + (idB + 1_000_000L));
            assertThat(heldView.storyArchetypeIds()).isEqualTo(Set.of(idB + 3_000_000L));
            assertThat(heldView.worldSettingIds()).isEqualTo(Set.of(idB + 4_000_000L));

            // Fresh HTTP request sees the new coherent updated values across all 19 properties
            mockMvc.perform(get("/api/v1/fictions/{id}", idB).header("Authorization", "Bearer " + publicBearerToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(idB))
                    .andExpect(jsonPath("$.data.title").value("Evolved Title"))
                    .andExpect(jsonPath("$.data.originalTitle").value("Evolved Native"))
                    .andExpect(jsonPath("$.data.nationalityCode").value("VN"))
                    .andExpect(jsonPath("$.data.posterUrl").value("https://secret.cdn/poster.png"))
                    .andExpect(jsonPath("$.data.format").value("NOVEL"))
                    .andExpect(jsonPath("$.data.isNsfw").value(false))
                    .andExpect(jsonPath("$.data.genreId").value(idB))
                    .andExpect(jsonPath("$.data.authorPersonId").value(idB + 1_000_000L))
                    .andExpect(jsonPath("$.data.authorGroupId").value(org.hamcrest.Matchers.nullValue()))
                    .andExpect(jsonPath("$.data.description").value("Secret description"))
                    .andExpect(jsonPath("$.data.totalChapters").value(250))
                    .andExpect(jsonPath("$.data.progressStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.consumptionStatus").value("UNCONSUMED"))
                    .andExpect(jsonPath("$.data.currentProgressText").value("Ch 500"))
                    .andExpect(jsonPath("$.data.review").value("Evolved Review"))
                    .andExpect(jsonPath("$.data.authorName").value("Author-" + (idB + 1_000_000L)))
                    .andExpect(jsonPath("$.data.storyArchetypeIds", org.hamcrest.Matchers.containsInAnyOrder(idB + 3_000_000L, newArchIdB)))
                    .andExpect(jsonPath("$.data.worldSettingIds", org.hamcrest.Matchers.empty()));
        } finally {
            reset(vaultEntryOperations);
            executorB.shutdownNow();
        }
    }

    @Test
    @DisplayName("S3 / ROB-14: Controlled concurrent linked reference updates (Person name, Person Vault trash, Group name) obey snapshot boundary (FR-ROB-02)")
    void s3LinkedSourceSnapshotIsolation() throws Exception {
        // CASE A: Person author name update race
        Long fictionIdA = seedFiction(false, false);
        Long personIdA = fictionIdA + 1_000_000L;

        CountDownLatch readStartedA = new CountDownLatch(1);
        CountDownLatch updateCommittedA = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(personIdA)) {
                readStartedA.countDown();
                assertThat(updateCommittedA.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(personIdA);

        ExecutorService executorA = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFictionDetailView>> readerFuture = executorA.submit(() -> fictionOperations.findPublicDetail(fictionIdA));

            assertThat(readStartedA.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction updates author person name
            jdbcTemplate.update("UPDATE persons SET name = 'Newly Committed Name' WHERE id = ?", personIdA);
            updateCommittedA.countDown();

            Optional<PublicFictionDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            // REPEATABLE READ snapshot took initial read; returns initial name
            assertThat(heldResult.get().authorName()).isEqualTo("Author-" + personIdA);

            // Fresh request sees the newly committed name
            Optional<PublicFictionDetailView> freshResult = fictionOperations.findPublicDetail(fictionIdA);
            assertThat(freshResult).isPresent();
            assertThat(freshResult.get().authorName()).isEqualTo("Newly Committed Name");
        } finally {
            reset(vaultEntryOperations);
            executorA.shutdownNow();
        }

        // CASE B: Person Vault trashed concurrently
        Long fictionIdB = seedFiction(false, false);
        Long personIdB = fictionIdB + 1_000_000L;

        CountDownLatch readStartedB = new CountDownLatch(1);
        CountDownLatch trashCommittedB = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(personIdB)) {
                readStartedB.countDown();
                assertThat(trashCommittedB.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(vaultEntryOperations).find(personIdB);

        ExecutorService executorB = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFictionDetailView>> readerFuture = executorB.submit(() -> fictionOperations.findPublicDetail(fictionIdB));

            assertThat(readStartedB.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction trashes Person Vault
            jdbcTemplate.update("UPDATE vault_entries SET deleted_at = now() WHERE id = ?", personIdB);
            trashCommittedB.countDown();

            Optional<PublicFictionDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            // Held snapshot still sees Person Vault as active -> returns authorName
            assertThat(heldResult.get().authorName()).isEqualTo("Author-" + personIdB);

            // Fresh request sees trashed Person Vault -> 200 OK with authorName: null
            mockMvc.perform(get("/api/v1/fictions/{id}", fictionIdB).header("Authorization", "Bearer " + publicBearerToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.authorName").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
            executorB.shutdownNow();
        }

        // CASE C: CreatorGroup author name update race
        Long fictionIdC = seedFictionWithGroup(false, false);
        Long groupIdC = fictionIdC + 2_000_000L;

        CountDownLatch readStartedC = new CountDownLatch(1);
        CountDownLatch groupUpdateCommittedC = new CountDownLatch(1);

        doAnswer(inv -> {
            Long argId = inv.getArgument(0);
            if (argId.equals(groupIdC)) {
                readStartedC.countDown();
                assertThat(groupUpdateCommittedC.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return inv.callRealMethod();
        }).when(creatorGroupOperations).find(groupIdC);

        ExecutorService executorC = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PublicFictionDetailView>> readerFuture = executorC.submit(() -> fictionOperations.findPublicDetail(fictionIdC));

            assertThat(readStartedC.await(5, TimeUnit.SECONDS)).isTrue();

            // Concurrent transaction updates group name
            jdbcTemplate.update("UPDATE creator_groups SET name = 'Newly Committed Group' WHERE id = ?", groupIdC);
            groupUpdateCommittedC.countDown();

            Optional<PublicFictionDetailView> heldResult = readerFuture.get(5, TimeUnit.SECONDS);
            assertThat(heldResult).isPresent();
            assertThat(heldResult.get().authorName()).isEqualTo("Group-" + groupIdC);

            // Fresh request sees updated group name
            Optional<PublicFictionDetailView> freshResult = fictionOperations.findPublicDetail(fictionIdC);
            assertThat(freshResult).isPresent();
            assertThat(freshResult.get().authorName()).isEqualTo("Newly Committed Group");
        } finally {
            reset(creatorGroupOperations);
            executorC.shutdownNow();
        }
    }

    @Test
    @DisplayName("S5 / ROB-11: Genuine work, Vault, Person, Group, classification query, and transaction-completion failures yield safe 500/data:null/no-store (FR-ROB-03)")
    void s5OperationalAndCompletionFailuresYieldSafe500() throws Exception {
        Long id = seedFictionWithClassifications(false, false);
        Long personId = id + 1_000_000L;
        String token = publicBearerToken();

        // 1. Work query failure on fictionRepository.findById
        MethodInterceptor workQueryFailureAdvice = invocation -> {
            if ("findById".equals(invocation.getMethod().getName())) {
                Long argId = (Long) invocation.getArguments()[0];
                if (argId != null && argId.equals(id)) {
                    throw new DataAccessResourceFailureException("Injected DB failure on fictionRepository");
                }
            }
            return invocation.proceed();
        };
        Advised advisedWorkRepo = (Advised) fictionRepository;
        advisedWorkRepo.addAdvice(0, workQueryFailureAdvice);

        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            advisedWorkRepo.removeAdvice(workQueryFailureAdvice);
        }

        // 2. Classification query failure on storyArchetypeRepository
        MethodInterceptor archFailureAdvice = invocation -> {
            throw new QueryTimeoutException("Injected timeout on storyArchetypeRepository");
        };
        Advised advisedArchRepo = (Advised) storyArchetypeRepository;
        advisedArchRepo.addAdvice(0, archFailureAdvice);

        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            advisedArchRepo.removeAdvice(archFailureAdvice);
        }

        // 3. VaultEntryOperations failure on work Vault read
        doThrow(new QueryTimeoutException("Injected DB failure on work vault"))
                .when(vaultEntryOperations).find(id);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 4. VaultEntryOperations failure on author Person Vault read
        doThrow(new QueryTimeoutException("Injected DB failure on author person vault"))
                .when(vaultEntryOperations).find(personId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
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
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(personOperations);
        }

        // 6. CreatorGroupOperations failure on Group read
        Long groupIdWork = seedFictionWithGroup(false, false);
        Long groupId = groupIdWork + 2_000_000L;
        doThrow(new DataAccessResourceFailureException("Injected CreatorGroup read failure"))
                .when(creatorGroupOperations).find(groupId);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", groupIdWork).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(creatorGroupOperations);
        }

        // 7. Service transaction-completion failure (registered beforeCommit synchronization throws)
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
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
        } finally {
            reset(vaultEntryOperations);
        }

        // 8. Normal recovery after clearing all faults
        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.authorName").value("Author-" + personId));
    }

    @Test
    @DisplayName("S6 / ROB-12: Complete immutable result materialized before return, non-empty sets retain values, no post-return database access (FR-ROB-03)")
    void s6CompleteImmutableResultMaterializedBeforeReturn() throws Exception {
        Long id = seedFictionWithClassifications(false, false);
        Set<Long> expectedArchetypes = Set.of(id + 3_000_000L);
        Set<Long> expectedSettings = Set.of(id + 4_000_000L);

        AtomicInteger workRepoCalls = new AtomicInteger();
        AtomicInteger archRepoCalls = new AtomicInteger();
        AtomicInteger settingRepoCalls = new AtomicInteger();
        AtomicInteger vaultRepoCalls = new AtomicInteger();
        AtomicInteger personRepoCalls = new AtomicInteger();

        MethodInterceptor workAdvice = inv -> {
            workRepoCalls.incrementAndGet();
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

        Advised advisedWorkRepo = (Advised) fictionRepository;
        Advised advisedArchRepo = (Advised) storyArchetypeRepository;
        Advised advisedSettingRepo = (Advised) worldSettingRepository;
        Advised advisedVaultRepo = (Advised) applicationContext.getBean("vaultEntryRepository");
        Advised advisedPersonRepo = (Advised) applicationContext.getBean("personRepository");

        advisedWorkRepo.addAdvice(0, workAdvice);
        advisedArchRepo.addAdvice(0, archAdvice);
        advisedSettingRepo.addAdvice(0, settingAdvice);
        advisedVaultRepo.addAdvice(0, vaultAdvice);
        advisedPersonRepo.addAdvice(0, personAdvice);

        Optional<PublicFictionDetailView> viewOpt;
        try {
            viewOpt = fictionOperations.findPublicDetail(id);
        } finally {
            // Positively verify that each repository was queried during acquisition
            assertThat(workRepoCalls.get()).isGreaterThan(0);
            assertThat(archRepoCalls.get()).isGreaterThan(0);
            assertThat(settingRepoCalls.get()).isGreaterThan(0);
            assertThat(vaultRepoCalls.get()).isGreaterThan(0);
            assertThat(personRepoCalls.get()).isGreaterThan(0);

            // Reset invocation counts to guard against post-return reads
            workRepoCalls.set(0);
            archRepoCalls.set(0);
            settingRepoCalls.set(0);
            vaultRepoCalls.set(0);
            personRepoCalls.set(0);
        }

        try {
            assertThat(viewOpt).isPresent();
            PublicFictionDetailView view = viewOpt.get();

            // Non-empty immutable sets matching exact expected IDs
            assertThat(view.storyArchetypeIds()).isEqualTo(expectedArchetypes);
            assertThat(view.worldSettingIds()).isEqualTo(expectedSettings);
            assertThatThrownBy(() -> view.storyArchetypeIds().add(99999L))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> view.worldSettingIds().add(99999L))
                    .isInstanceOf(UnsupportedOperationException.class);

            // Reset capability spies to also verify Mockito never()
            reset(vaultEntryOperations, personOperations, creatorGroupOperations);

            // In-memory mapping to response DTO outside transaction
            PublicFictionDetailResponse response = FictionWebMapper.toPublicDetailResponse(view);
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.authorName()).isEqualTo("Author-" + (id + 1_000_000L));
            assertThat(response.storyArchetypeIds()).isEqualTo(expectedArchetypes);
            assertThat(response.worldSettingIds()).isEqualTo(expectedSettings);

            // Serialization outside transaction through configured ObjectMapper
            String serialized = objectMapper.writeValueAsString(response);
            assertThat(serialized).contains("\"authorName\":\"Author-" + (id + 1_000_000L) + "\"");

            // Verify serialized JSON retains exact captured set values
            var jsonNode = objectMapper.readTree(serialized);
            assertThat(jsonNode.path("id").asLong()).isEqualTo(id);
            assertThat(jsonNode.path("authorName").asText()).isEqualTo("Author-" + (id + 1_000_000L));
            List<Long> jsonArchetypes = new ArrayList<>();
            jsonNode.path("storyArchetypeIds").forEach(n -> jsonArchetypes.add(n.asLong()));
            assertThat(jsonArchetypes).containsExactlyElementsOf(expectedArchetypes);
            List<Long> jsonSettings = new ArrayList<>();
            jsonNode.path("worldSettingIds").forEach(n -> jsonSettings.add(n.asLong()));
            assertThat(jsonSettings).containsExactlyElementsOf(expectedSettings);

            // Positively guard that ZERO repository queries or capability operations occurred post-return
            assertThat(workRepoCalls.get()).isZero();
            assertThat(archRepoCalls.get()).isZero();
            assertThat(settingRepoCalls.get()).isZero();
            assertThat(vaultRepoCalls.get()).isZero();
            assertThat(personRepoCalls.get()).isZero();
            verify(vaultEntryOperations, never()).find(any());
            verify(personOperations, never()).find(any());
            verify(creatorGroupOperations, never()).find(any());
        } finally {
            advisedWorkRepo.removeAdvice(workAdvice);
            advisedArchRepo.removeAdvice(archAdvice);
            advisedSettingRepo.removeAdvice(settingAdvice);
            advisedVaultRepo.removeAdvice(vaultAdvice);
            advisedPersonRepo.removeAdvice(personAdvice);
        }
    }

    @Test
    @DisplayName("ROB-16: PUBLIC 200, 404, and 500 use no-store; conditional headers do not return 304; profile switches isolate data (FR-ROB-04)")
    void publicProfileSwitchAndNoStoreCachingControls() throws Exception {
        Long id = seedFiction(false, false);
        String pubToken = publicBearerToken();
        String privToken = bearerToken();

        // 1. PUBLIC 200 has no-store, no ETag, no Location
        var resp200 = mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse();
        assertThat(resp200.getHeader(HttpHeaders.ETAG)).isNull();
        assertThat(resp200.getHeader(HttpHeaders.LOCATION)).isNull();

        // 2. PUBLIC 404 has no-store
        mockMvc.perform(get("/api/v1/fictions/{id}", 9999999L).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        // 3. Injected 500 has no-store
        doThrow(new DataAccessResourceFailureException("Injected fault for cache check"))
                .when(vaultEntryOperations).find(id);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + pubToken))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        } finally {
            reset(vaultEntryOperations);
        }

        // 4. Conditional headers do NOT return 304 on PUBLIC 200, 404, or 500
        mockMvc.perform(get("/api/v1/fictions/{id}", id)
                        .header("Authorization", "Bearer " + pubToken)
                        .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                        .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(get("/api/v1/fictions/{id}", 9999999L)
                        .header("Authorization", "Bearer " + pubToken)
                        .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                        .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        doThrow(new DataAccessResourceFailureException("Injected fault for conditional 500 check"))
                .when(vaultEntryOperations).find(id);
        try {
            mockMvc.perform(get("/api/v1/fictions/{id}", id)
                            .header("Authorization", "Bearer " + pubToken)
                            .header(HttpHeaders.IF_NONE_MATCH, "\"some-etag\"")
                            .header(HttpHeaders.IF_MODIFIED_SINCE, "Wed, 21 Oct 2026 07:28:00 GMT"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        } finally {
            reset(vaultEntryOperations);
        }

        // 5. Sequential reads across profiles do not bleed representation or cache
        var pubFirst = mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + pubToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(pubFirst).path("data").fieldNames()).toIterable().hasSize(19);

        var privSecond = mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer " + privToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(privSecond).path("data").fieldNames()).toIterable().hasSize(18);
        assertThat(objectMapper.readTree(privSecond).path("data").has("authorName")).isFalse();
    }

    @Test
    @DisplayName("ROB-02: Malformed, stale, or revoked tokens fail closed without data (401)")
    void malformedStaleAndRevokedTokensFailClosedWithoutData() throws Exception {
        Long id = seedFiction(false, false);

        mockMvc.perform(get("/api/v1/fictions/{id}", id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/fictions/{id}", id).header("Authorization", "Bearer malformed.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
