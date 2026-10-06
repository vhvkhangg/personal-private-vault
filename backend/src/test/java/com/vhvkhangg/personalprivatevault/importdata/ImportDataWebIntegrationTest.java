package com.vhvkhangg.personalprivatevault.importdata;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
