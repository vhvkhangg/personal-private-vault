package com.vhvkhangg.personalprivatevault.importdata;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class ImportDataWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Import jobs lifecycle works over HTTP: create, parse, validate, commit")
    void importJobLifecycleOverHttp() throws Exception {
        // 1. Create import job -> 201 Created
        String createJobPayload = """
                {
                  "targetType": "NOTE",
                  "format": "MARKDOWN",
                  "originalFileName": "sample.md"
                }
                """;

        String jobResponse = mockMvc.perform(post("/api/v1/imports/jobs")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJobPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number jobId = objectMapper.readTree(jobResponse).path("data").path("id").numberValue();

        // 2. Parse job with rawText -> 200 OK
        String parsePayload = """
                {
                  "rawText": "---\\ntitle: Sample Note via HTTP Import\\n---\\n# Content\\nHere is body content."
                }
                """;

        mockMvc.perform(post("/api/v1/imports/jobs/{id}/parse", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parsePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARSED"))
                .andExpect(jsonPath("$.data.totalItems").value(1));

        // 3. Validate job -> 200 OK
        mockMvc.perform(post("/api/v1/imports/jobs/{id}/validate", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VALIDATED"))
                .andExpect(jsonPath("$.data.validItems").value(1));

        // 4. Query job items -> 200 OK
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 5. Execute job with decisions -> 200 OK
        String executePayload = """
                {
                  "itemDecisions": [
                    {
                      "itemIndex": 0,
                      "decision": "IMPORT"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/imports/jobs/{id}/execute", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IMPORTED"))
                .andExpect(jsonPath("$.data.importedItems").value(1));

        // 6. Get job by ID -> 200 OK
        mockMvc.perform(get("/api/v1/imports/jobs/{id}", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IMPORTED"));
    }

    @Test
    @DisplayName("SBA-006: Trailing JSON object fails parse over HTTP with 422 INVALID_IMPORT_JOB and blocks progression")
    void trailingJsonObjectFailsHttpParseWithSafeMessageAndBlocksProgression(CapturedOutput output) throws Exception {
        String sentinel = "SENTINEL_SECRET_TOKEN_JSON_OBJECT_987654";
        String createJobPayload = """
                {
                  "targetType": "NOTE",
                  "format": "JSON",
                  "originalFileName": "trailing-object.json"
                }
                """;

        String jobResponse = mockMvc.perform(post("/api/v1/imports/jobs")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJobPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number jobId = objectMapper.readTree(jobResponse).path("data").path("id").numberValue();

        // Parse with trailing JSON object containing synthetic sentinel
        String parsePayload = """
                {
                  "rawText": "[{\\"title\\":\\"Note 1\\",\\"contentMarkdown\\":\\"Body 1\\"}]\\n{\\"trailing\\":\\"%s\\"}"
                }
                """.formatted(sentinel);

        String parseErrorResponse = mockMvc.perform(post("/api/v1/imports/jobs/{id}/parse", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parsePayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMPORT_JOB"))
                .andExpect(jsonPath("$.error.message").value("Invalid import job data"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert synthetic sentinel token does NOT appear in HTTP error response or application logs
        org.assertj.core.api.Assertions.assertThat(parseErrorResponse).doesNotContain(sentinel);
        org.assertj.core.api.Assertions.assertThat(output.getAll()).doesNotContain(sentinel);

        // Validate attempt blocked
        mockMvc.perform(post("/api/v1/imports/jobs/{id}/validate", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMPORT_TRANSITION"));

        // Execute attempt blocked
        String executePayload = """
                {
                  "itemDecisions": [
                    {
                      "itemIndex": 0,
                      "decision": "IMPORT"
                    }
                  ]
                }
                """;
        mockMvc.perform(post("/api/v1/imports/jobs/{id}/execute", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMPORT_TRANSITION"));

        // Job query shows state unchanged at CREATED with zero items
        mockMvc.perform(get("/api/v1/imports/jobs/{id}", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalItems").value(0))
                .andExpect(jsonPath("$.data.validItems").value(0))
                .andExpect(jsonPath("$.data.importedItems").value(0));

        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        // Database assertions: zero items, zero notes, zero vault entries
        Integer itemCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM import_job_items WHERE import_job_id = ?",
                Integer.class, jobId.longValue()
        );
        org.assertj.core.api.Assertions.assertThat(itemCount).isZero();

        Integer noteCount = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
        org.assertj.core.api.Assertions.assertThat(noteCount).isZero();

        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'NOTE'", Integer.class
        );
        org.assertj.core.api.Assertions.assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("SBA-006: Trailing garbage tokens fail parse over HTTP with 422 INVALID_IMPORT_JOB and leave database untouched")
    void trailingGarbageTokensFailHttpParseWithSafeMessageAndLeaveDatabaseUntouched(CapturedOutput output) throws Exception {
        String sentinel = "SENTINEL_SECRET_TOKEN_GARBAGE_123456";
        String createJobPayload = """
                {
                  "targetType": "NOTE",
                  "format": "JSON",
                  "originalFileName": "trailing-garbage.json"
                }
                """;

        String jobResponse = mockMvc.perform(post("/api/v1/imports/jobs")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJobPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number jobId = objectMapper.readTree(jobResponse).path("data").path("id").numberValue();

        // Parse with trailing garbage text containing synthetic sentinel
        String parsePayload = """
                {
                  "rawText": "[{\\"title\\":\\"Note 1\\",\\"contentMarkdown\\":\\"Body 1\\"}] some trailing invalid characters %s"
                }
                """.formatted(sentinel);

        String parseErrorResponse = mockMvc.perform(post("/api/v1/imports/jobs/{id}/parse", jobId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parsePayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMPORT_JOB"))
                .andExpect(jsonPath("$.error.message").value("Invalid import job data"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert synthetic sentinel token does NOT appear in HTTP error response or application logs
        org.assertj.core.api.Assertions.assertThat(parseErrorResponse).doesNotContain(sentinel);
        org.assertj.core.api.Assertions.assertThat(output.getAll()).doesNotContain(sentinel);

        Integer itemCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM import_job_items WHERE import_job_id = ?",
                Integer.class, jobId.longValue()
        );
        org.assertj.core.api.Assertions.assertThat(itemCount).isZero();

        Integer noteCount = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
        org.assertj.core.api.Assertions.assertThat(noteCount).isZero();
    }
}
