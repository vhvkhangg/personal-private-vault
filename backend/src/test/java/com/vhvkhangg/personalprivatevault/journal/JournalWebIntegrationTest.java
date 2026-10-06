package com.vhvkhangg.personalprivatevault.journal;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JournalWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Diary entries CRUD lifecycle works over HTTP")
    void diaryLifecycleOverHttp() throws Exception {
        // 1. Create diary entry -> 201 Created
        String diaryPayload = """
                {
                  "entryDate": "2026-10-05",
                  "contentMarkdown": "# Today's Thoughts\\n\\nProductive day working on Phase 13."
                }
                """;

        String diaryResponse = mockMvc.perform(post("/api/v1/journal/diary-entries")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(diaryPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.entryDate").value("2026-10-05"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number diaryId = objectMapper.readTree(diaryResponse).path("data").path("id").numberValue();

        // 2. Get diary entry -> 200 OK
        mockMvc.perform(get("/api/v1/journal/diary-entries/{id}", diaryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entryDate").value("2026-10-05"));

        // 3. Update diary entry -> 200 OK
        String updatePayload = """
                {
                  "entryDate": "2026-10-05",
                  "contentMarkdown": "# Today's Thoughts (Evening Update)\\n\\nAll tests passing."
                }
                """;

        mockMvc.perform(put("/api/v1/journal/diary-entries/{id}", diaryId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk());

        // 4. Soft delete diary entry -> 200 OK (no 204)
        mockMvc.perform(delete("/api/v1/journal/diary-entries/{id}", diaryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());
    }
}
