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
}
