package com.vhvkhangg.personalprivatevault.knowledge.api;

import java.util.Map;

/**
 * Command for updating an existing note via the parent Knowledge API.
 */
public record UpdateKnowledgeNoteCommand(
        String title,
        String contentMarkdown,
        String summary,
        String sourceName,
        String sourceUrl,
        String importedFileName,
        String importedFileHash,
        Map<String, Object> frontmatter
) {}
