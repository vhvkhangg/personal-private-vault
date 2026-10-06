package com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto;

import java.util.Map;

public record KnowledgeNoteResponse(
        Long id,
        String title,
        String contentMarkdown,
        String summary,
        String sourceName,
        String sourceUrl,
        String importedFileName,
        String importedFileHash,
        Map<String, Object> frontmatter
) {
}
