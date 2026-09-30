package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.util.Map;

/**
 * Immutable public view of a note exposed by the parent Knowledge API.
 */
public record KnowledgeNoteView(
        Long id,
        String title,
        String contentMarkdown,
        String summary,
        String sourceName,
        String sourceUrl,
        String importedFileName,
        String importedFileHash,
        Map<String, Object> frontmatter
) {}
