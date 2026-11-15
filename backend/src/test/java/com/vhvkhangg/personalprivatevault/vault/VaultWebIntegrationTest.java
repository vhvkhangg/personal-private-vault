package com.vhvkhangg.personalprivatevault.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.VaultEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VaultWebIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private VaultEntryOperations vaultEntryOperations;

    @MockitoSpyBean
    private VaultEntryRepository vaultEntryRepository;

    @Test
    @DisplayName("Vault tags, metadata, favorite, rating, trash and restore lifecycle works over HTTP")
    void vaultLifecycleOverHttp() throws Exception {
        // Create a person so we have an underlying vault entry
        PersonView person = personOperations.create(new CreatePersonCommand(
                "Vault Target Person " + System.currentTimeMillis(), null, null, null, null, null, null, null
        ));
        Long entryId = person.id();

        // 1. Create tag -> 201 Created
        String tagName = "vault-tag-" + System.currentTimeMillis();
        String tagResponse = mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + tagName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(tagName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number tagId = objectMapper.readTree(tagResponse).path("data").path("id").numberValue();

        // 2. Attach tag to entry -> 200 OK
        mockMvc.perform(put("/api/v1/vault/entries/{id}/tags/{tagId}", entryId, tagId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 3. Mark favorite -> 200 OK
        mockMvc.perform(put("/api/v1/vault/entries/{id}/favorite", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 4. Set rating -> 200 OK
        mockMvc.perform(put("/api/v1/vault/entries/{id}/rating", entryId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\": \"S\"}"))
                .andExpect(status().isOk());

        // 5. Get metadata -> 200 OK
        mockMvc.perform(get("/api/v1/vault/entries/{id}/metadata", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorite").value(true))
                .andExpect(jsonPath("$.data.rating").value("S"))
                .andExpect(jsonPath("$.data.tags[0].name").value(tagName));

        // 6. Detach tag -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}/tags/{tagId}", entryId, tagId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 7. Unfavorite -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}/favorite", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 8. Remove rating -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}/rating", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 9. Move to trash -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedAt").isNotEmpty());

        // 10. Restore from trash -> 200 OK
        mockMvc.perform(post("/api/v1/vault/entries/{id}/restore", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedAt").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("V1-01, V1-03, V1-15, V1-17: GET /api/v1/vault/trash returns canonical envelope, Cache-Control: no-store, and exact 3-field items")
    void trashHttpSuccessAndEnvelopeWire() throws Exception {
        // Seed a trashed entry
        VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FILM);
        vaultEntryOperations.moveToTrash(entry.id());

        String responseJson = mockMvc.perform(get("/api/v1/vault/trash")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.meta").isEmpty())
                .andExpect(jsonPath("$.data.items").isArray())
                .andReturn().getResponse().getContentAsString();

        System.out.println("ACTUAL_SUCCESS_HTTP_BODY:\n" + responseJson);

        JsonNode rootNode = objectMapper.readTree(responseJson);
        assertThat(rootNode.hasNonNull("error")).isFalse();
        assertThat(rootNode.hasNonNull("meta")).isFalse();

        JsonNode dataNode = rootNode.path("data");
        assertThat(dataNode.has("items")).isTrue();
        assertThat(dataNode.has("nextCursor")).isTrue();

        JsonNode items = dataNode.path("items");
        assertThat(items.size()).isGreaterThanOrEqualTo(1);

        // Verify every item has exactly 3 keys: id, entryType, deletedAt (V1-03, V1-15)
        for (JsonNode item : items) {
            List<String> fieldNames = new ArrayList<>();
            item.fieldNames().forEachRemaining(fieldNames::add);
            assertThat(fieldNames).containsExactlyInAnyOrder("id", "entryType", "deletedAt");

            assertThat(item.path("id").isIntegralNumber()).isTrue();
            assertThat(item.path("id").asLong()).isPositive();
            assertThat(item.path("entryType").isTextual()).isTrue();
            assertThat(item.path("deletedAt").isTextual()).isTrue();

            // Explicit absence of forbidden fields
            assertThat(item.has("title")).isFalse();
            assertThat(item.has("name")).isFalse();
            assertThat(item.has("isNsfw")).isFalse();
            assertThat(item.has("createdAt")).isFalse();
            assertThat(item.has("updatedAt")).isFalse();
            assertThat(item.has("tags")).isFalse();
            assertThat(item.has("favorite")).isFalse();
            assertThat(item.has("rating")).isFalse();
            assertThat(item.has("notes")).isFalse();
        }
    }

    @Test
    @DisplayName("V1-04: Empty trash returns 200 with items: [] and nextCursor: null")
    void emptyTrashHttpReturns200WithEmptyList() throws Exception {
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");

        mockMvc.perform(get("/api/v1/vault/trash")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.meta").isEmpty());
    }

    @Test
    @DisplayName("V1-05, V1-06, V1-13, V1-17: Limit parsing, range bounds, syntax errors, positive query control, no domain write, and Cache-Control: no-store")
    void limitParsingAndRangeValidationHttp() throws Exception {
        // 1. Populated boundaries on 30 trashed items (FR-V1-03)
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        for (int i = 0; i < 30; i++) {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FICTION);
            vaultEntryOperations.moveToTrash(entry.id());
        }

        // Snapshot domain count before queries to verify no domain writes (V1-13)
        long countBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);

        // Positive control: valid request invokes business operations and repository read
        reset(vaultEntryOperations);
        reset(vaultEntryRepository);
        String defaultLimitJson = mockMvc.perform(get("/api/v1/vault/trash")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(25))
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        org.mockito.Mockito.verify(vaultEntryOperations, org.mockito.Mockito.atLeastOnce()).listTrash(25, null);
        org.mockito.Mockito.verify(vaultEntryRepository, org.mockito.Mockito.atLeastOnce()).findTrashPage(any(), any());

        // Explicit limit=1
        reset(vaultEntryOperations);
        mockMvc.perform(get("/api/v1/vault/trash?limit=1")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty());
        org.mockito.Mockito.verify(vaultEntryOperations, org.mockito.Mockito.atLeastOnce()).listTrash(1, null);

        // Explicit limit=100 (fetches all 30 items, nextCursor is null)
        reset(vaultEntryOperations);
        mockMvc.perform(get("/api/v1/vault/trash?limit=100")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.items.length()").value(30))
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()));
        org.mockito.Mockito.verify(vaultEntryOperations, org.mockito.Mockito.atLeastOnce()).listTrash(100, null);

        // Exactly-limit boundary: fixture with exactly 5 items, limit=5 -> nextCursor null
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        for (int i = 0; i < 5; i++) {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.FILM);
            vaultEntryOperations.moveToTrash(entry.id());
        }
        mockMvc.perform(get("/api/v1/vault/trash?limit=5")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(5))
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()));

        // Limit+1 boundary: fixture with 6 items, limit=5 -> page 1 has nextCursor, page 2 has 1 item and nextCursor null
        VaultEntryView extraEntry = vaultEntryOperations.create(VaultEntryType.FILM);
        vaultEntryOperations.moveToTrash(extraEntry.id());
        String page1Json = mockMvc.perform(get("/api/v1/vault/trash?limit=5")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(5))
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String nextCursor = objectMapper.readTree(page1Json).path("data").path("nextCursor").asText();
        String page2Json = mockMvc.perform(get("/api/v1/vault/trash?limit=5&cursor=" + nextCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()))
                .andReturn().getResponse().getContentAsString();

        // Exhausted boundary: query with terminal cursor returns empty array and null nextCursor
        long lastId = objectMapper.readTree(page2Json).path("data").path("items").get(0).path("id").asLong();
        String terminalCursor = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("v1|" + lastId).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/vault/trash?limit=5&cursor=" + terminalCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()));

        // Maximum page boundary with 101 items (FR-V1-03)
        jdbcTemplate.update("DELETE FROM vault_entries WHERE deleted_at IS NOT NULL");
        for (int i = 0; i < 101; i++) {
            VaultEntryView entry = vaultEntryOperations.create(VaultEntryType.NOTE);
            vaultEntryOperations.moveToTrash(entry.id());
        }
        String maxPage1Json = mockMvc.perform(get("/api/v1/vault/trash?limit=100")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.items.length()").value(100))
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String maxCursor = objectMapper.readTree(maxPage1Json).path("data").path("nextCursor").asText();
        mockMvc.perform(get("/api/v1/vault/trash?limit=100&cursor=" + maxCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.nextCursor").value(org.hamcrest.Matchers.nullValue()));

        // 2. Out-of-range integer values -> 400 INVALID_ARGUMENT with no-store; business read skipped (FR-V1-03)
        List<Map<String, Object>> stateBeforeReads = jdbcTemplate.queryForList(
                "SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id"
        );

        String capturedInvalidLimitJson = null;
        for (String invalidLimit : List.of("0", "-1", "101", "200")) {
            reset(vaultEntryOperations);
            reset(vaultEntryRepository);
            String res = mockMvc.perform(get("/api/v1/vault/trash?limit=" + invalidLimit)
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andReturn().getResponse().getContentAsString();
            if (capturedInvalidLimitJson == null) {
                capturedInvalidLimitJson = res;
            }
            org.mockito.Mockito.verify(vaultEntryOperations, org.mockito.Mockito.never()).listTrash(anyInt(), any());
            org.mockito.Mockito.verify(vaultEntryRepository, org.mockito.Mockito.never()).findTrashPage(any(), any());
        }
        System.out.println("ACTUAL_INVALID_LIMIT_HTTP_BODY:\n" + capturedInvalidLimitJson);

        // 3. Malformed / non-integer / decimal / overflow values -> 400 MALFORMED_REQUEST with no-store; business read skipped
        for (String malformedLimit : List.of("abc", "1.5", "", "   ", "9999999999999999999999")) {
            reset(vaultEntryOperations);
            reset(vaultEntryRepository);
            mockMvc.perform(get("/api/v1/vault/trash?limit=" + malformedLimit)
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                    .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"))
                    .andExpect(jsonPath("$.data").isEmpty());
            org.mockito.Mockito.verify(vaultEntryOperations, org.mockito.Mockito.never()).listTrash(anyInt(), any());
            org.mockito.Mockito.verify(vaultEntryRepository, org.mockito.Mockito.never()).findTrashPage(any(), any());
        }

        // 4. Verify domain snapshot unchanged after all read operations (V1-13, FR-V1-03)
        List<Map<String, Object>> stateAfterReads = jdbcTemplate.queryForList(
                "SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id"
        );
        assertThat(stateAfterReads).as("Valid and invalid read requests must perform zero domain writes or modifications").isEqualTo(stateBeforeReads);
    }

    @Test
    @DisplayName("V1-07, V1-17: Cursor validation rejects malformed, blank, padded, noncanonical, overflow cursors without leaking cursor or executing business query")
    void cursorValidationHttp() throws Exception {
        // Valid canonical unpadded Base64url control v1|1 -> "djF8MQ" returns 200 OK
        mockMvc.perform(get("/api/v1/vault/trash?cursor=djF8MQ")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));

        byte[] malformedUtf8 = new byte[] { 'v', '1', '|', (byte) 0xC0, (byte) 0xAF };
        String malformedUtf8Cursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(malformedUtf8);

        String[] badCursors = {
                "", "   ", "djF8MTQy=", "djF8MTQy==", "invalid!base64",
                "djF8MR", // Unpadded ambiguous Base64url decoding to v1|1 with unused bits set (FR-V1-03)
                malformedUtf8Cursor, // Malformed UTF-8 (FR-V1-03)
                "djF8MA", // v1|0
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v2|142".getBytes()),
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|-5".getBytes()),
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|0142".getBytes()),
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|abc".getBytes()),
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|142|extra".getBytes()),
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|9223372036854775808".getBytes()), // 19-digit overflow (FR-V1-03)
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("v1|9999999999999999999999".getBytes()),
                "a".repeat(129)
        };

        List<Map<String, Object>> stateBeforeCursorReads = jdbcTemplate.queryForList(
                "SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id"
        );

        for (String badCursor : badCursors) {
            reset(vaultEntryRepository);
            String res = mockMvc.perform(get("/api/v1/vault/trash?cursor=" + badCursor)
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andReturn().getResponse().getContentAsString();

            // Business repository query is skipped
            org.mockito.Mockito.verify(vaultEntryRepository, org.mockito.Mockito.never()).findTrashPage(any(), any());

            // Verify no raw cursor or parser diagnostic leaked in error message
            if (!badCursor.isBlank()) {
                assertThat(res).doesNotContain(badCursor);
            }
        }

        List<Map<String, Object>> stateAfterCursorReads = jdbcTemplate.queryForList(
                "SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id"
        );
        assertThat(stateAfterCursorReads).as("Invalid cursor requests must perform zero domain writes or modifications").isEqualTo(stateBeforeCursorReads);
    }

    @Test
    @DisplayName("V1-17: Conditional requests do not return 304 or leak cacheable metadata")
    void conditionalHeadersDoNotReturn304() throws Exception {
        mockMvc.perform(get("/api/v1/vault/trash")
                        .header("Authorization", bearerHeader())
                        .header("If-None-Match", "\"dummy-etag\"")
                        .header("If-Modified-Since", "Thu, 01 Jan 1970 00:00:00 GMT"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("V1-13, V1-17: Operational, IllegalState, DataIntegrity, and transaction completion failures return safe 500 INTERNAL_ERROR with no-store")
    void operationalAndCompletionFailuresHttp() throws Exception {
        try {
            // 1. Generic operational failure
            doThrow(new RuntimeException("Simulated operational repository failure"))
                    .when(vaultEntryOperations).listTrash(anyInt(), any());

            mockMvc.perform(get("/api/v1/vault/trash")
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(result -> assertThat(result.getResponse().getContentAsString()).doesNotContain("Simulated operational"));

            // 2. Operational IllegalArgumentException after real read (guarded against 400 translation) (FR-V1-01)
            reset(vaultEntryOperations);
            org.mockito.Mockito.doAnswer(invocation -> {
                invocation.callRealMethod();
                throw new IllegalArgumentException("Unexpected operational argument error");
            }).when(vaultEntryOperations).listTrash(anyInt(), any());

            mockMvc.perform(get("/api/v1/vault/trash")
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").isEmpty());

            // 3. Transaction completion failure injected during transaction completion phase after real read (FR-V1-01)
            reset(vaultEntryOperations);
            java.util.concurrent.atomic.AtomicBoolean readMaterialized = new java.util.concurrent.atomic.AtomicBoolean(false);
            java.util.concurrent.atomic.AtomicBoolean completionHookExecuted = new java.util.concurrent.atomic.AtomicBoolean(false);

            org.springframework.transaction.support.AbstractPlatformTransactionManager aptm =
                    (fixtureTransactions instanceof org.springframework.transaction.support.AbstractPlatformTransactionManager tm) ? tm : null;
            boolean originalRollbackOnCommit = (aptm != null) && aptm.isRollbackOnCommitFailure();
            if (aptm != null) {
                aptm.setRollbackOnCommitFailure(true);
            }

            try {
                org.mockito.Mockito.doAnswer(invocation -> {
                    Object result = invocation.callRealMethod();
                    assertThat(result).isNotNull();
                    readMaterialized.set(true);

                    if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
                        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                                new org.springframework.transaction.support.TransactionSynchronization() {
                                    @Override
                                    public void beforeCommit(boolean readOnly) {
                                        completionHookExecuted.set(true);
                                        throw new org.springframework.transaction.TransactionSystemException("Simulated transaction completion failure in beforeCommit");
                                    }
                                }
                        );
                    } else {
                        completionHookExecuted.set(true);
                        throw new org.springframework.transaction.TransactionSystemException("Simulated commit completion failure");
                    }
                    return result;
                }).when(vaultEntryOperations).listTrash(anyInt(), any());

                String completionErrorJson = mockMvc.perform(get("/api/v1/vault/trash")
                                .header("Authorization", bearerHeader()))
                        .andExpect(status().isInternalServerError())
                        .andExpect(header().string("Cache-Control", "no-store"))
                        .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                        .andExpect(jsonPath("$.data").isEmpty())
                        .andReturn().getResponse().getContentAsString();

                assertThat(readMaterialized.get()).as("Bounded repository acquisition/materialization must have succeeded").isTrue();
                assertThat(completionHookExecuted.get()).as("Transaction completion hook must have executed").isTrue();
                System.out.println("ACTUAL_OPERATIONAL_500_HTTP_BODY:\n" + completionErrorJson);
            } finally {
                if (aptm != null) {
                    aptm.setRollbackOnCommitFailure(originalRollbackOnCommit);
                }
            }

            // 4. IllegalStateException during read (guarded against VaultExceptionAdvice 409)
            reset(vaultEntryOperations);
            doThrow(new IllegalStateException("Simulated illegal state"))
                    .when(vaultEntryOperations).listTrash(anyInt(), any());

            mockMvc.perform(get("/api/v1/vault/trash")
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").isEmpty());

            // 5. DataIntegrityViolationException during read (guarded against VaultExceptionAdvice 409)
            reset(vaultEntryOperations);
            doThrow(new DataIntegrityViolationException("Simulated data integrity violation"))
                    .when(vaultEntryOperations).listTrash(anyInt(), any());

            mockMvc.perform(get("/api/v1/vault/trash")
                            .header("Authorization", bearerHeader()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.data").isEmpty());
        } finally {
            reset(vaultEntryOperations);
        }
    }
}
