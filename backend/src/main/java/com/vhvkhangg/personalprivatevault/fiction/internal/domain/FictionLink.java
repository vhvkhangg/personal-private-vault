package com.vhvkhangg.personalprivatevault.fiction.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * Fiction link entity mapping the {@code fiction_links} table.
 */
@Entity
@Table(name = "fiction_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FictionLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fiction_id", nullable = false)
    private Long fictionId;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "link_type", length = 50, nullable = false)
    private String linkType;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "url", length = 2048, nullable = false)
    private String url;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FictionLink(
            Long fictionId,
            String languageCode,
            String linkType,
            String label,
            String url,
            boolean isPrimary,
            Instant createdAt) {
        this.fictionId = Objects.requireNonNull(fictionId, "fictionId must not be null");
        this.languageCode = languageCode;
        this.linkType = Objects.requireNonNull(linkType, "linkType must not be null");
        this.label = label;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.isPrimary = isPrimary;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public void update(
            String languageCode,
            String linkType,
            String label,
            String url,
            boolean isPrimary) {
        this.languageCode = languageCode;
        this.linkType = Objects.requireNonNull(linkType, "linkType must not be null");
        this.label = label;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.isPrimary = isPrimary;
    }
}
