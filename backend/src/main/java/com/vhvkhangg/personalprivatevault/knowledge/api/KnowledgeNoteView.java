package com.vhvkhangg.personalprivatevault.knowledge.api;

import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot;

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
) {
    public KnowledgeNoteView {
        frontmatter = NoteFrontmatterSnapshot.toUnmodifiableSnapshot(frontmatter);
    }
}
