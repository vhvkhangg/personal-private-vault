package com.vhvkhangg.personalprivatevault.vault;

import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VaultWebIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private PersonOperations personOperations;

    @Test
    @DisplayName("Vault tags, metadata, favorite, rating, trash and restore lifecycle works over HTTP")
    void vaultLifecycleOverHttp() throws Exception {
        // Create a person so we have an underlying vault entry
        PersonView person = personOperations.create(new CreatePersonCommand(
                "Vault Target Person " + System.currentTimeMillis(), null, null, null, null, null, null, null
        ));
        Long entryId = person.id();

        // 1. Create tag -> 201 Created
        String tagName = "vault-tag-" + System.currentTimeMillis();
        String tagResponse = mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + tagName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(tagName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number tagId = objectMapper.readTree(tagResponse).path("data").path("id").numberValue();

        // 2. Attach tag to entry -> 200 OK
        mockMvc.perform(put("/api/v1/vault/entries/{id}/tags/{tagId}", entryId, tagId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 3. Mark favorite -> 200 OK
        mockMvc.perform(put("/api/v1/vault/entries/{id}/favorite", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 4. Set rating -> 200 OK
        mockMvc.perform(put("/api/v1/vault/entries/{id}/rating", entryId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grade\": \"S\"}"))
                .andExpect(status().isOk());

        // 5. Get metadata -> 200 OK
        mockMvc.perform(get("/api/v1/vault/entries/{id}/metadata", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorite").value(true))
                .andExpect(jsonPath("$.data.rating").value("S"))
                .andExpect(jsonPath("$.data.tags[0].name").value(tagName));

        // 6. Detach tag -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}/tags/{tagId}", entryId, tagId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 7. Unfavorite -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}/favorite", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 8. Remove rating -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}/rating", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 9. Move to trash -> 200 OK
        mockMvc.perform(delete("/api/v1/vault/entries/{id}", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedAt").isNotEmpty());

        // 10. Restore from trash -> 200 OK
        mockMvc.perform(post("/api/v1/vault/entries/{id}/restore", entryId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedAt").value(org.hamcrest.Matchers.nullValue()));
    }
}
