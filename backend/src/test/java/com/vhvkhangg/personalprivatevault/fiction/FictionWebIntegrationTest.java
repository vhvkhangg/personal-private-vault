package com.vhvkhangg.personalprivatevault.fiction;

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

class FictionWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Fiction genres and Fiction items CRUD lifecycle works over HTTP")
    void fictionLifecycleOverHttp() throws Exception {
        // 1. Create fiction genre -> 201 Created
        String genreName = "Fantasy-" + System.currentTimeMillis();
        String genreResponse = mockMvc.perform(post("/api/v1/fiction-genres")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + genreName + "\", \"description\": \"Fantasy stories\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(genreName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/v1/fiction-genres/by-name")
                        .param("name", genreName)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(genreName));

        Number genreId = objectMapper.readTree(genreResponse).path("data").path("id").numberValue();

        // 2. Create author person -> 201 Created
        String personResponse = mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Fiction Author\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number authorPersonId = objectMapper.readTree(personResponse).path("data").path("id").numberValue();

        // 3. Create fiction -> 201 Created
        String fictionTitle = "The Epic Tale " + System.currentTimeMillis();
        String createFictionPayload = """
                {
                  "title": "%s",
                  "format": "NOVEL",
                  "genreId": %d,
                  "authorPersonId": %d,
                  "progressStatus": "ONGOING",
                  "consumptionStatus": "UNCONSUMED"
                }
                """.formatted(fictionTitle, genreId, authorPersonId);

        String fictionResponse = mockMvc.perform(post("/api/v1/fictions")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFictionPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(fictionTitle))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number fictionId = objectMapper.readTree(fictionResponse).path("data").path("id").numberValue();

        // 4. Get fiction by ID -> 200 OK
        mockMvc.perform(get("/api/v1/fictions/{id}", fictionId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(fictionTitle));

        // 5. Update fiction -> 200 OK
        String updatedTitle = fictionTitle + " (Revised)";
        String updatePayload = """
                {
                  "title": "%s",
                  "format": "NOVEL",
                  "genreId": %d,
                  "authorPersonId": %d,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """.formatted(updatedTitle, genreId, authorPersonId);

        mockMvc.perform(put("/api/v1/fictions/{id}", fictionId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(updatedTitle));

        // 6. Create link -> 201 Created
        String linkPayload = """
                {
                  "linkType": "OFFICIAL_SITE",
                  "url": "https://example.com/fiction"
                }
                """;

        mockMvc.perform(post("/api/v1/fictions/{id}/links", fictionId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(linkPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber());
    }
}
