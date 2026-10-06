package com.vhvkhangg.personalprivatevault.settings;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettingsWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Settings initialize, update, and retrieval lifecycle works over HTTP, pairing uninitialized 404 with OpenAPI documentation")
    void settingsLifecycle() throws Exception {
        // 1. Unauthenticated request -> 401
        mockMvc.perform(get("/api/v1/settings"))
                .andExpect(status().isUnauthorized());

        // 1b. Uninitialized settings -> 404 SETTINGS_NOT_FOUND paired with OpenAPI documentation
        mockMvc.perform(get("/api/v1/settings")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SETTINGS_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Application settings have not been initialized"));

        String openApiJson = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode openApiRoot = objectMapper.readTree(openApiJson);
        JsonNode settingsGetResponses = openApiRoot.path("paths").path("/api/v1/settings").path("get").path("responses");
        assertThat(settingsGetResponses.has("404")).as("GET /api/v1/settings must document 404").isTrue();
        assertThat(settingsGetResponses.path("404").path("content").has("application/json")).isTrue();
        assertThat(settingsGetResponses.path("404").path("content").path("application/json").path("schema").path("$ref").asText())
                .isEqualTo("#/components/schemas/ErrorResponse");

        // 2. Initialize / update settings -> 200 OK
        String updatePayload = """
                {
                  "defaultCurrencyCode": "USD",
                  "timezone": "UTC",
                  "paginationSize": 50,
                  "privateModeAutoLockMinutes": 15,
                  "backupEnabled": true,
                  "backupIntervalHours": 24
                }
                """;

        mockMvc.perform(put("/api/v1/settings")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.paginationSize").value(50));

        // 3. Get initialized settings -> 200 OK
        mockMvc.perform(get("/api/v1/settings")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.paginationSize").value(50))
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
