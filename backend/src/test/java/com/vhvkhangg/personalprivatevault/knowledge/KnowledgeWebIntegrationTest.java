package com.vhvkhangg.personalprivatevault.knowledge;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KnowledgeWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Parent Knowledge HTTP endpoints expose notes, studies, information, vocabulary, and SRS review")
    void knowledgeLifecycleOverHttp() throws Exception {
        // 1. Create note -> 201 Created
        String noteTitle = "Knowledge Architecture Note " + System.currentTimeMillis();
        String notePayload = """
                {
                  "title": "%s",
                  "contentMarkdown": "# Markdown content\\n\\nHere is some knowledge note content."
                }
                """.formatted(noteTitle);

        String noteResponse = mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(noteTitle))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number noteId = objectMapper.readTree(noteResponse).path("data").path("id").numberValue();

        // 2. Get note by ID -> 200 OK
        mockMvc.perform(get("/api/v1/knowledge/notes/{id}", noteId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(noteTitle));

        // 3. Update note -> 200 OK
        String updatedTitle = noteTitle + " (Updated)";
        String updateNotePayload = """
                {
                  "title": "%s",
                  "contentMarkdown": "# Updated content"
                }
                """.formatted(updatedTitle);

        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateNotePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(updatedTitle));

        // 4. Create study -> 201 Created
        String studyTitle = "Spring Modulith Deep Dive " + System.currentTimeMillis();
        String studyPayload = """
                {
                  "title": "%s",
                  "type": "COURSE",
                  "learningStatus": "IN_PROGRESS"
                }
                """.formatted(studyTitle);

        mockMvc.perform(post("/api/v1/knowledge/study")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studyPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(studyTitle));

        // 5. Create information -> 201 Created
        String infoTitle = "PostgreSQL Indexes " + System.currentTimeMillis();
        String infoPayload = """
                {
                  "title": "%s",
                  "type": "TECHNOLOGY",
                  "contentMarkdown": "B-Tree is default"
                }
                """.formatted(infoTitle);

        mockMvc.perform(post("/api/v1/knowledge/information")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(infoPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value(infoTitle));

        // 6. Create vocabulary -> 201 Created
        String vocabWord = "ephemeral-" + System.currentTimeMillis();
        String vocabPayload = """
                {
                  "word": "%s",
                  "languageCode": "en",
                  "meaning": "lasting for a very short time"
                }
                """.formatted(vocabWord);

        mockMvc.perform(post("/api/v1/knowledge/vocabulary")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vocabPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.word").value(vocabWord));
    }
}
