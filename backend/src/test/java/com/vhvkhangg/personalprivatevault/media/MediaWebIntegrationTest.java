package com.vhvkhangg.personalprivatevault.media;

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

class MediaWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Albums and Images CRUD lifecycle works over HTTP")
    void mediaLifecycleOverHttp() throws Exception {
        // 1. Create album -> 201 Created
        String albumTitle = "Album-" + System.currentTimeMillis();
        String albumResponse = mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"" + albumTitle + "\", \"description\": \"Test album\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(albumTitle))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number albumId = objectMapper.readTree(albumResponse).path("data").path("id").numberValue();

        // 2. Create image -> 201 Created
        String sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String imagePayload = """
                {
                  "albumId": %d,
                  "title": "Sample Image",
                  "imageType": "PHOTO",
                  "objectKey": "images/test-%d.jpg",
                  "sizeBytes": 1024,
                  "mimeType": "image/jpeg",
                  "checksumSha256": "%s"
                }
                """.formatted(albumId, System.currentTimeMillis(), sha256);

        String imageResponse = mockMvc.perform(post("/api/v1/images")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(imagePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number imageId = objectMapper.readTree(imageResponse).path("data").path("id").numberValue();

        // 3. Get image by ID -> 200 OK
        mockMvc.perform(get("/api/v1/images/{id}", imageId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Sample Image"));

        // 4. Query images in album -> 200 OK
        mockMvc.perform(get("/api/v1/albums/{id}/images", albumId)
                        .header("Authorization", bearerHeader())
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
