package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateKnowledgeNoteRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @NotBlank(message = "Content markdown must not be blank")
        String contentMarkdown,
        String summary,
        @Size(max = 255, message = "Source name must not exceed 255 characters")
        String sourceName,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl,
        @Size(max = 255, message = "Imported file name must not exceed 255 characters")
        String importedFileName,
        @Size(max = 64, message = "Imported file hash must not exceed 64 characters")
        String importedFileHash,
        Map<String, Object> frontmatter
) {
}
