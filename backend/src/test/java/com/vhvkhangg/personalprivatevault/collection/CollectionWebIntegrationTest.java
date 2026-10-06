package com.vhvkhangg.personalprivatevault.collection;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CollectionWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Parent Collection HTTP endpoints expose Music, Shopping, and Software items")
    void collectionLifecycleOverHttp() throws Exception {
        // 1. Create music -> 201 Created
        String musicTitle = "Abbey Road " + System.currentTimeMillis();
        String musicPayload = """
                {
                  "title": "%s",
                  "version": "ORIGINAL"
                }
                """.formatted(musicTitle);

        String musicResponse = mockMvc.perform(post("/api/v1/collection/music")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(musicPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(musicTitle))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number musicId = objectMapper.readTree(musicResponse).path("data").path("id").numberValue();

        mockMvc.perform(get("/api/v1/collection/music/{id}", musicId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(musicTitle));

        // 2. Create shopping item -> 201 Created
        String itemName = "Wireless Headphones " + System.currentTimeMillis();
        String shoppingPayload = """
                {
                  "name": "%s",
                  "status": "WISHLIST"
                }
                """.formatted(itemName);

        String shoppingResponse = mockMvc.perform(post("/api/v1/collection/shopping")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shoppingPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(itemName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number shoppingId = objectMapper.readTree(shoppingResponse).path("data").path("id").numberValue();

        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shoppingId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(itemName));

        // 3. Create software item -> 201 Created
        String softwareName = "IntelliJ IDEA " + System.currentTimeMillis();
        String softwarePayload = """
                {
                  "name": "%s",
                  "type": "APPLICATION"
                }
                """.formatted(softwareName);

        String softwareResponse = mockMvc.perform(post("/api/v1/collection/software")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(softwarePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(softwareName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number softwareId = objectMapper.readTree(softwareResponse).path("data").path("id").numberValue();

        mockMvc.perform(get("/api/v1/collection/software/{id}", softwareId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(softwareName));

        // 4. Music person credits set assignment (PUT) and repeated idempotent assignment
        String personResp = mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Musician Person\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number personId = objectMapper.readTree(personResp).path("data").path("id").numberValue();

        String creditPayload = """
                {
                  "personId": %s,
                  "role": "SINGER"
                }
                """.formatted(personId);

        // First assignment -> 200 OK with null data
        mockMvc.perform(put("/api/v1/collection/music/{id}/credits", musicId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(creditPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        // Repeated assignment (idempotent set-assignment) -> 200 OK
        mockMvc.perform(put("/api/v1/collection/music/{id}/credits", musicId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(creditPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        // Assert exactly 1 credit exists
        mockMvc.perform(get("/api/v1/collection/music/{id}/credits", musicId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].personId").value(personId))
                .andExpect(jsonPath("$.data[0].role").value("SINGER"));
    }
}
