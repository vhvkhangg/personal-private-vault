package com.vhvkhangg.personalprivatevault.account;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("External account and follower snapshot lifecycle works over HTTP")
    void accountLifecycleOverHttp() throws Exception {
        Long platformId = jdbcTemplate.queryForObject("SELECT id FROM platforms WHERE name = 'Web Platform'", Long.class);

        // 1. Create account -> 201 Created
        String username = "user_" + System.currentTimeMillis();
        String createAccountPayload = """
                {
                  "platformId": %d,
                  "ownership": "OWNED",
                  "accountType": "SOCIAL",
                  "username": "%s",
                  "displayName": "Display User"
                }
                """.formatted(platformId, username);

        String accountResponse = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.username").value(username))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number accountId = objectMapper.readTree(accountResponse).path("data").path("id").numberValue();

        // 2. Get account by ID -> 200 OK
        mockMvc.perform(get("/api/v1/accounts/{id}", accountId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username));

        // 3. Update account -> 200 OK
        String updatePayload = """
                {
                  "platformId": %d,
                  "ownership": "OWNED",
                  "accountType": "SOCIAL",
                  "username": "%s",
                  "displayName": "Updated Display Name"
                }
                """.formatted(platformId, username);

        mockMvc.perform(put("/api/v1/accounts/{id}", accountId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Updated Display Name"));

        // 4. Record follower snapshot -> 201 Created
        String snapshotPayload = """
                {
                  "capturedAt": "2026-10-05T10:00:00Z",
                  "source": "MANUAL",
                  "reportedTotalCount": 1500
                }
                """;

        mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(snapshotPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reportedTotalCount").value(1500));

        // 5. Query follower snapshots -> 200 OK
        mockMvc.perform(get("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
