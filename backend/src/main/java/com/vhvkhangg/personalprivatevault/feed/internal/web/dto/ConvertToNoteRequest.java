package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record ConvertToNoteRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String contentMarkdown,
        String summary,
        @Size(max = 255) String sourceName,
        @Size(max = 2048) String sourceUrl,
        @Size(max = 255) String importedFileName,
        @Size(max = 64) String importedFileHash,
        Map<String, Object> frontmatter
) {}
