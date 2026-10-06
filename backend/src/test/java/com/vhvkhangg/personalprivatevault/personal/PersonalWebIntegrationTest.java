package com.vhvkhangg.personalprivatevault.personal;

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

class PersonalWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Personal profile CRUD and self endpoints work over HTTP")
    void personalProfileLifecycleOverHttp() throws Exception {
        // 1. Create personal profile (self) -> 201 Created
        String profileName = "Profile " + System.currentTimeMillis();
        String createPayload = """
                {
                  "name": "%s",
                  "relationship": "Self",
                  "gender": "FEMALE",
                  "birthDate": "1995-05-15",
                  "email": "user%d@example.com",
                  "isSelf": true
                }
                """.formatted(profileName, System.currentTimeMillis());

        String profileResponse = mockMvc.perform(post("/api/v1/personal/profiles")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(profileName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number profileId = objectMapper.readTree(profileResponse).path("data").path("id").numberValue();

        // 2. Get personal profile by ID -> 200 OK
        mockMvc.perform(get("/api/v1/personal/profiles/{id}", profileId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(profileName));

        // 3. Update personal profile -> 200 OK
        String updatedName = profileName + " Updated";
        String updatePayload = """
                {
                  "name": "%s",
                  "relationship": "Self",
                  "gender": "FEMALE",
                  "birthDate": "1995-05-15",
                  "isSelf": true
                }
                """.formatted(updatedName);

        mockMvc.perform(put("/api/v1/personal/profiles/{id}", profileId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(updatedName));

        // 4. Get active self profile -> 200 OK
        mockMvc.perform(get("/api/v1/personal/profiles/self")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(profileId));

        // 5. Soft-delete personal profile -> 200 OK
        mockMvc.perform(delete("/api/v1/personal/profiles/{id}", profileId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 6. Restore personal profile -> 200 OK
        mockMvc.perform(post("/api/v1/personal/profiles/{id}/restore", profileId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(profileId));
    }
}
