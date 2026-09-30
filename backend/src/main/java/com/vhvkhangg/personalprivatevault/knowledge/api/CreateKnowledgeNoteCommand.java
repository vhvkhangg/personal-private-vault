package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.util.Map;

/**
 * Command for creating a note via the parent Knowledge API.
 */
public record CreateKnowledgeNoteCommand(
        String title,
        String contentMarkdown,
        String summary,
        String sourceName,
        String sourceUrl,
        String importedFileName,
        String importedFileHash,
        Map<String, Object> frontmatter
) {}
