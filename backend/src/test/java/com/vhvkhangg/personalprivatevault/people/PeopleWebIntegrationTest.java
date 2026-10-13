package com.vhvkhangg.personalprivatevault.people;

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

class PeopleWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("People and Creator Group HTTP endpoints work end-to-end")
    void peopleAndCreatorGroupLifecycle() throws Exception {
        String name = "Test Person " + System.currentTimeMillis();

        // 1. Create person -> 201 Created
        String personResponse = mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(name))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number personId = objectMapper.readTree(personResponse).path("data").path("id").numberValue();

        // 2. Get person by ID -> 200 OK
        mockMvc.perform(get("/api/v1/people/{id}", personId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(name));

        // 3. Update person -> 200 OK
        String updatedName = name + " Updated";
        mockMvc.perform(put("/api/v1/people/{id}", personId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + updatedName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(updatedName));

        // 4. Assign role -> 200 OK (PUT)
        mockMvc.perform(put("/api/v1/people/{id}/roles", personId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"AUTHOR\"}"))
                .andExpect(status().isOk());

        // 5. Get roles -> 200 OK
        mockMvc.perform(get("/api/v1/people/{id}/roles", personId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 7. Create creator group -> 201 Created
        String groupName = "Test Group " + System.currentTimeMillis();
        String groupResponse = mockMvc.perform(post("/api/v1/people/creator-groups")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + groupName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(groupName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number groupId = objectMapper.readTree(groupResponse).path("data").path("id").numberValue();

        // 8. Add member to group -> 200 OK (PUT)
        mockMvc.perform(put("/api/v1/people/creator-groups/{id}/members/{personId}", groupId, personId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 9. Get group members -> 200 OK
        mockMvc.perform(get("/api/v1/people/creator-groups/{id}/members", groupId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].personId").value(personId));
    }

    @Test
    @DisplayName("SBA-007: POST /api/v1/people with extreme exponent numeric tokens returns 422 PERSON_INVALID without database write")
    void postExtremeExponentsReturn422WithoutDatabaseWrite() throws Exception {
        for (String exponent : java.util.List.of("1E+2147483647", "100E+2147483647", "1000E+2147483647")) {
            // 1. Height create
            String heightJson = String.format("""
                    {
                      "name": "Extreme Height Person",
                      "heightCm": %s
                    }
                    """, exponent);

            mockMvc.perform(post("/api/v1/people")
                            .header("Authorization", bearerHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(heightJson))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error.code").value("PERSON_INVALID"))
                    .andExpect(jsonPath("$.error.message").value("Invalid person data"));

            Integer heightCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM persons WHERE name = 'Extreme Height Person'", Integer.class
            );
            org.assertj.core.api.Assertions.assertThat(heightCount).isZero();

            // 2. Weight create
            String weightJson = String.format("""
                    {
                      "name": "Extreme Weight Person",
                      "weightKg": %s
                    }
                    """, exponent);

            mockMvc.perform(post("/api/v1/people")
                            .header("Authorization", bearerHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(weightJson))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error.code").value("PERSON_INVALID"))
                    .andExpect(jsonPath("$.error.message").value("Invalid person data"));

            Integer weightCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM persons WHERE name = 'Extreme Weight Person'", Integer.class
            );
            org.assertj.core.api.Assertions.assertThat(weightCount).isZero();
        }
    }

    @Test
    @DisplayName("SBA-007: PUT /api/v1/people/{id} with extreme exponent numeric tokens returns 422 PERSON_INVALID leaving row unchanged")
    void putExtremeExponentsReturn422LeavingRowUnchanged() throws Exception {
        String personResponse = mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Valid Before Extreme\", \"heightCm\": 170.00, \"weightKg\": 65.00}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number personId = objectMapper.readTree(personResponse).path("data").path("id").numberValue();

        for (String exponent : java.util.List.of("1E+2147483647", "100E+2147483647", "1000E+2147483647")) {
            // 1. Height update
            String heightJson = String.format("""
                    {
                      "name": "Valid Before Extreme",
                      "heightCm": %s,
                      "weightKg": 65.00
                    }
                    """, exponent);

            mockMvc.perform(put("/api/v1/people/{id}", personId)
                            .header("Authorization", bearerHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(heightJson))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error.code").value("PERSON_INVALID"))
                    .andExpect(jsonPath("$.error.message").value("Invalid person data"));

            // 2. Weight update
            String weightJson = String.format("""
                    {
                      "name": "Valid Before Extreme",
                      "heightCm": 170.00,
                      "weightKg": %s
                    }
                    """, exponent);

            mockMvc.perform(put("/api/v1/people/{id}", personId)
                            .header("Authorization", bearerHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(weightJson))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error.code").value("PERSON_INVALID"))
                    .andExpect(jsonPath("$.error.message").value("Invalid person data"));

            // Assert database row remains completely unmodified
            java.util.Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT height_cm, weight_kg, name FROM persons WHERE id = ?", personId
            );
            org.assertj.core.api.Assertions.assertThat(new java.math.BigDecimal(row.get("height_cm").toString()))
                    .isEqualByComparingTo("170.00");
            org.assertj.core.api.Assertions.assertThat(new java.math.BigDecimal(row.get("weight_kg").toString()))
                    .isEqualByComparingTo("65.00");
            org.assertj.core.api.Assertions.assertThat(row.get("name")).isEqualTo("Valid Before Extreme");
        }
    }
}
