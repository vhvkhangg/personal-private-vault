package com.vhvkhangg.personalprivatevault.knowledge.note.note;

import java.util.Map;

/**
 * Command for updating an existing note.
 */
public record UpdateNoteCommand(
        String title,
        String contentMarkdown,
        String summary,
        String sourceName,
        String sourceUrl,
        String importedFileName,
        String importedFileHash,
        Map<String, Object> frontmatter
) {}
