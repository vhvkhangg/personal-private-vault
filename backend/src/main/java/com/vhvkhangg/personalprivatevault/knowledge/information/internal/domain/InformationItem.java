package com.vhvkhangg.personalprivatevault.knowledge.information.internal.domain;

import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.util.Objects;

/**
 * Information item entity sharing identity with {@code vault_entries.id} for type {@code INFORMATION}.
 */
@Entity
@Table(name = "information_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InformationItem implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private InformationType type;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "content_markdown", columnDefinition = "text")
    private String contentMarkdown;

    @Column(name = "example", columnDefinition = "text")
    private String example;

    @Column(name = "source_name", length = 500)
    private String sourceName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Transient
    private boolean isNew = false;

    public InformationItem(
            Long id,
            String title,
            InformationType type,
            String description,
            String contentMarkdown,
            String example,
            String sourceName,
            String sourceUrl,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "InformationItem id must not be null");
        this.title = Objects.requireNonNull(title, "InformationItem title must not be null");
        this.type = Objects.requireNonNull(type, "InformationItem type must not be null");
        this.description = description;
        this.contentMarkdown = contentMarkdown;
        this.example = example;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.isNew = isNew;
    }

    public void update(
            String title,
            InformationType type,
            String description,
            String contentMarkdown,
            String example,
            String sourceName,
            String sourceUrl
    ) {
        this.title = Objects.requireNonNull(title, "InformationItem title must not be null");
        this.type = Objects.requireNonNull(type, "InformationItem type must not be null");
        this.description = description;
        this.contentMarkdown = contentMarkdown;
        this.example = example;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
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
