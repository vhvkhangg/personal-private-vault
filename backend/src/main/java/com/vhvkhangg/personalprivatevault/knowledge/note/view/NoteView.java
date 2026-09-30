package com.vhvkhangg.personalprivatevault.knowledge.note.view;

import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot;

import java.util.Map;

/**
 * Immutable view of a note.
 */
public record NoteView(
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
    public NoteView {
        frontmatter = NoteFrontmatterSnapshot.toUnmodifiableSnapshot(frontmatter);
    }
}
