package com.vhvkhangg.personalprivatevault.film;

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

class FilmWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Film genres and Film items CRUD lifecycle works over HTTP")
    void filmLifecycleOverHttp() throws Exception {
        // 1. Create film genre -> 201 Created
        String genreName = "SciFi-" + System.currentTimeMillis();
        String genreResponse = mockMvc.perform(post("/api/v1/film-genres")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + genreName + "\", \"description\": \"Science fiction films\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(genreName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number genreId = objectMapper.readTree(genreResponse).path("data").path("id").numberValue();

        // 2. Create film -> 201 Created
        String filmTitle = "Interstellar Film " + System.currentTimeMillis();
        String createFilmPayload = """
                {
                  "title": "%s",
                  "format": "MOVIE",
                  "productionStyle": "LIVE_ACTION",
                  "isNsfw": false,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """.formatted(filmTitle);

        String filmResponse = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFilmPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(filmTitle))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number filmId = objectMapper.readTree(filmResponse).path("data").path("id").numberValue();

        // 3. Get film by ID -> 200 OK
        mockMvc.perform(get("/api/v1/films/{id}", filmId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(filmTitle));

        // 4. Update film -> 200 OK
        String updatedTitle = filmTitle + " (IMAX Edition)";
        String updatePayload = """
                {
                  "title": "%s",
                  "format": "MOVIE",
                  "productionStyle": "LIVE_ACTION",
                  "isNsfw": false,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """.formatted(updatedTitle);

        mockMvc.perform(put("/api/v1/films/{id}", filmId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(updatedTitle));

        // 5. Assign genre via path variable -> 200 OK (PUT)
        mockMvc.perform(put("/api/v1/films/{id}/genres/{genreId}", filmId, genreId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 6. Get genres -> 200 OK
        mockMvc.perform(get("/api/v1/films/{id}/genres", filmId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 7. Get genre by name -> 200 OK
        mockMvc.perform(get("/api/v1/film-genres/by-name")
                        .param("name", genreName)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(genreName));

        // 8. Create person, add credit, and fetch credit by ID
        String personResp = mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Director Person\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number personId = objectMapper.readTree(personResp).path("data").path("id").numberValue();

        String creditResponse = mockMvc.perform(post("/api/v1/films/{id}/credits", filmId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"personId\": " + personId + ", \"role\": \"MAIN\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number creditId = objectMapper.readTree(creditResponse).path("data").path("id").numberValue();
        mockMvc.perform(get("/api/v1/films/credits/{creditId}", creditId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(creditId));
    }
}
