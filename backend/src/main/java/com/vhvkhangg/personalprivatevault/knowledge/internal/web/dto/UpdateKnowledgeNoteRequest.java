package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateKnowledgeNoteRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 500, message = "Title must not exceed 500 characters")
        String title,
        @NotNull(message = "Content markdown must not be null")
        String contentMarkdown,
        String summary,
        @Size(max = 500, message = "Source name must not exceed 500 characters")
        String sourceName,
        @Size(max = 2048, message = "Source URL must not exceed 2048 characters")
        String sourceUrl,
        @Size(max = 500, message = "Imported file name must not exceed 500 characters")
        String importedFileName,
        @Size(max = 64, message = "Imported file hash must not exceed 64 characters")
        String importedFileHash,
        Map<String, Object> frontmatter
) {
}
