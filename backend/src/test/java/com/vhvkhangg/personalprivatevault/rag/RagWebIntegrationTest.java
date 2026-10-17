package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.junit.jupiter.api.Timeout(90)
class RagWebIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired
    private com.vhvkhangg.personalprivatevault.rag.api.RagOperations rag;

    @Test
    @DisplayName("RAG REST API lifecycle: domain candidate registration, approval, job admit, status, and revocation")
    void ragRestApiLifecycle() throws Exception {
        // 1. Setup domain Fiction work with required vault entries
        Long genreId = jdbcTemplate.queryForObject(
                "INSERT INTO fiction_genres (name) VALUES ('Web-Genre') RETURNING id",
                Long.class
        );
        Long authorVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('PERSON'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update("INSERT INTO persons (id, name) VALUES (?, 'Web Author')", authorVaultId);

        Long fictionVaultId = jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type) VALUES ('FICTION'::vault_entry_type) RETURNING id",
                Long.class
        );
        jdbcTemplate.update(
                """
                INSERT INTO fictions (id, title, format, genre_id, author_person_id, progress_status, consumption_status, description, is_nsfw)
                VALUES (?, 'Web Fiction Title', 'NOVEL'::fiction_format, ?, ?, 'COMPLETED'::progress_status, 'CONSUMED'::consumption_status, 'Web desc', false)
                """,
                fictionVaultId, genreId, authorVaultId
        );

        // 2. Reject unauthenticated request -> 401 Unauthorized
        mockMvc.perform(post("/api/v1/rag/sources/register-domain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceType\": \"FICTION\", \"sourceId\": \"" + fictionVaultId + "\"}"))
                .andExpect(status().isUnauthorized());

        // 3. Register domain candidate -> 201 Created
        String registerPayload = """
                {
                  "sourceType": "FICTION",
                  "sourceId": "%d",
                  "classification": "NON_PRIVATE"
                }
                """.formatted(fictionVaultId);

        String registerResponse = mockMvc.perform(post("/api/v1/rag/sources/register-domain")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.sourceType").value("FICTION"))
                .andExpect(jsonPath("$.data.accessState").value("DENIED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number sourceId = objectMapper.readTree(registerResponse).path("data").path("id").numberValue();

        // 4. Get source by ID -> 200 OK
        mockMvc.perform(get("/api/v1/rag/sources/{id}", sourceId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sourceId.longValue()))
                .andExpect(jsonPath("$.data.accessState").value("DENIED"));

        // 5. List sources -> 200 OK
        mockMvc.perform(get("/api/v1/rag/sources")
                        .header("Authorization", bearerHeader())
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // 6. Approve source with allowed fields -> 200 OK
        var preview=objectMapper.readTree(registerResponse).path("data");
        String approvePayload = objectMapper.writeValueAsString(java.util.Map.of(
                "approvedFields",java.util.List.of("title","description"),
                "expectedSourceVersion",preview.path("sourceVersion").longValue(),
                "expectedConsentResetVersion",preview.path("consentResetVersion").longValue(),
                "expectedAuthorizationEpoch",preview.path("authorizationEpoch").longValue(),
                "expectedFieldPolicyVersion",preview.path("fieldPolicyVersion").intValue(),
                "confirmedDescriptionHash",preview.path("currentDescriptionRevisionHash").asText()));
        String approvalResponse=mockMvc.perform(post("/api/v1/rag/sources/{id}/approve", sourceId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approvePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessState").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvedFields").isArray())
                .andReturn().getResponse().getContentAsString();

        // 7. Admit job for approved source -> 201 Created
        var approved=objectMapper.readTree(approvalResponse).path("data");
        String admitPayload=objectMapper.writeValueAsString(java.util.Map.of(
                "sourceId",sourceId.longValue(),
                "expectedSourceVersion",approved.path("sourceVersion").longValue(),
                "expectedAuthorizationEpoch",approved.path("authorizationEpoch").longValue(),
                "expectedFieldPolicyVersion",approved.path("fieldPolicyVersion").intValue()));

        String admitResponse = mockMvc.perform(post("/api/v1/rag/jobs/admit")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(admitPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.jobId").isString())
                .andExpect(jsonPath("$.data.attemptId").isString())
                .andExpect(jsonPath("$.data.leaseToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jobId = objectMapper.readTree(admitResponse).path("data").path("jobId").asText();
        String attemptId = objectMapper.readTree(admitResponse).path("data").path("attemptId").asText();
        String leaseToken = objectMapper.readTree(admitResponse).path("data").path("leaseToken").asText();

        // 8. Query job status -> 200 OK
        mockMvc.perform(get("/api/v1/rag/jobs/{id}", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobId").value(jobId))
                .andExpect(jsonPath("$.data.status").value("ADMITTED"))
                .andExpect(jsonPath("$.data.indexState").value("ABSENT"));

        // Cancel-first is an actual HTTP conflict; no exclusion mutation commits.
        mockMvc.perform(post("/api/v1/rag/sources/{id}/revoke",sourceId)
                        .header("Authorization",bearerHeader()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("RAG_SOURCE_BUSY"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                        "/api/v1/vault/entries/{id}",fictionVaultId).header("Authorization",bearerHeader()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("RAG_SOURCE_BUSY"));
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM vault_entries WHERE id=?",Boolean.class,fictionVaultId)).isFalse();
        Long unrelated=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id",Long.class);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                        "/api/v1/vault/entries/{id}",unrelated).header("Authorization",bearerHeader()))
                .andExpect(status().isOk());

        // Worker stages output (guarded by commit fence)
        rag.executeSyntheticStage(java.util.UUID.fromString(jobId),java.util.UUID.fromString(attemptId),"SYNTHETIC_TEST");

        // 9a. Finalize job with mismatched digest -> 400 Bad Request
        String badDigestPayload = """
                {
                  "attemptId": "%s",
                  "dependencyDigest": "mismatched-sha256-abc"
                }
                """.formatted(attemptId);

        mockMvc.perform(post("/api/v1/rag/jobs/{id}/finalize", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badDigestPayload))
                .andExpect(status().isBadRequest());

        // 9b. Finalize job -> 200 OK
        String finalizePayload = """
                {
                  "attemptId": "%s",
                  "dependencyDigest": ""
                }
                """.formatted(attemptId);

        mockMvc.perform(post("/api/v1/rag/jobs/{id}/finalize", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(finalizePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generationId").isString())
                .andExpect(jsonPath("$.data.sourceId").value(sourceId.longValue()))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.active").value(true));

        // 10. Revoke source -> 200 OK
        mockMvc.perform(post("/api/v1/rag/sources/{id}/revoke", sourceId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessState").value("REVOKED"));
    }

    @Test
    @DisplayName("Returns 404 for non-existent RAG source")
    void getNonExistentSourceReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/rag/sources/{id}", 999999L)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Returns 400 for invalid domain source candidate payload")
    void registerInvalidCandidateReturns400() throws Exception {
        String invalidPayload = """
                {
                  "sourceType": "EXTERNAL_FILE",
                  "sourceId": "not-allowed"
                }
                """;

        mockMvc.perform(post("/api/v1/rag/sources/register-domain")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
