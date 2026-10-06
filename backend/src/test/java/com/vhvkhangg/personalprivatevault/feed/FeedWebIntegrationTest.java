package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Feed sources and saved resources endpoints work over HTTP")
    void feedLifecycleOverHttp() throws Exception {
        // 1. Create feed source -> 201 Created
        String sourceName = "Tech Blog " + System.currentTimeMillis();
        String sourceFeedUrl = "https://example.com/feed-" + System.currentTimeMillis() + ".xml";
        String sourcePayload = """
                {
                  "name": "%s",
                  "type": "RSS",
                  "feedUrl": "%s"
                }
                """.formatted(sourceName, sourceFeedUrl);

        String sourceResponse = mockMvc.perform(post("/api/v1/feed/sources")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sourcePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(sourceName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number sourceId = objectMapper.readTree(sourceResponse).path("data").path("id").numberValue();

        // 2. Query items for source -> 200 OK
        mockMvc.perform(get("/api/v1/feed/sources/{sourceId}/items", sourceId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // 3. Create saved resource (manual) -> 201 Created
        String resourceTitle = "Saved Article " + System.currentTimeMillis();
        String resourceUrl = "https://example.com/article-" + System.currentTimeMillis();
        String resourcePayload = """
                {
                  "kind": "ARTICLE",
                  "title": "%s",
                  "resourceUrl": "%s"
                }
                """.formatted(resourceTitle, resourceUrl);

        String savedResponse = mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourcePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(resourceTitle))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number savedResourceId = objectMapper.readTree(savedResponse).path("data").path("id").numberValue();

        // 4. Get saved resource -> 200 OK
        mockMvc.perform(get("/api/v1/saved-resources/{id}", savedResourceId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(resourceTitle));

        // 5. Convert saved resource to note -> 201 Created
        String convertPayload = """
                {
                  "title": "Converted Note %d",
                  "contentMarkdown": "# Converted Note Content"
                }
                """.formatted(System.currentTimeMillis());

        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", savedResourceId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.savedResourceId").value(savedResourceId))
                .andExpect(jsonPath("$.data.targetVaultEntryId").isNumber());

        // 6. Get conversions -> 200 OK
        mockMvc.perform(get("/api/v1/saved-resources/{id}/conversions", savedResourceId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
