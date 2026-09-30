package com.vhvkhangg.personalprivatevault.knowledge.api;

import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot;

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
) {
    public CreateKnowledgeNoteCommand {
        frontmatter = NoteFrontmatterSnapshot.toUnmodifiableSnapshot(frontmatter);
    }
}
