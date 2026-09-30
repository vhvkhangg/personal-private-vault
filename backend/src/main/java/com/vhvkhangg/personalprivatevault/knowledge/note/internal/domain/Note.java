package com.vhvkhangg.personalprivatevault.knowledge.note.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot;

import java.util.Map;
import java.util.Objects;

/**
 * Note entity sharing identity with {@code vault_entries.id} for type {@code NOTE}.
 */
@Entity
@Table(name = "notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "content_markdown", columnDefinition = "text", nullable = false)
    private String contentMarkdown;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "source_name", length = 500)
    private String sourceName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "imported_file_name", length = 500)
    private String importedFileName;

    @Column(name = "imported_file_hash", length = 64, unique = true)
    private String importedFileHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "frontmatter", columnDefinition = "jsonb")
    private Map<String, Object> frontmatter;

    @Transient
    private boolean isNew = false;

    public Note(
            Long id,
            String title,
            String contentMarkdown,
            String summary,
            String sourceName,
            String sourceUrl,
            String importedFileName,
            String importedFileHash,
            Map<String, Object> frontmatter,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "Note id must not be null");
        this.title = Objects.requireNonNull(title, "Note title must not be null");
        this.contentMarkdown = Objects.requireNonNull(contentMarkdown, "Note contentMarkdown must not be null");
        this.summary = summary;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.importedFileName = importedFileName;
        this.importedFileHash = importedFileHash;
        this.frontmatter = NoteFrontmatterSnapshot.deepCopy(frontmatter);
        this.isNew = isNew;
    }

    public void update(
            String title,
            String contentMarkdown,
            String summary,
            String sourceName,
            String sourceUrl,
            String importedFileName,
            String importedFileHash,
            Map<String, Object> frontmatter
    ) {
        this.title = Objects.requireNonNull(title, "Note title must not be null");
        this.contentMarkdown = Objects.requireNonNull(contentMarkdown, "Note contentMarkdown must not be null");
        this.summary = summary;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.importedFileName = importedFileName;
        this.importedFileHash = importedFileHash;
        this.frontmatter = NoteFrontmatterSnapshot.deepCopy(frontmatter);
    }

    public Map<String, Object> getFrontmatter() {
        return NoteFrontmatterSnapshot.toUnmodifiableSnapshot(this.frontmatter);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
