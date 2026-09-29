package com.vhvkhangg.personalprivatevault.film.internal.domain;

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
 * External link associated with a film.
 */
@Entity
@Table(name = "film_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilmLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "film_id", nullable = false)
    private Long filmId;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "url", length = 2048, nullable = false)
    private String url;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public FilmLink(
            Long filmId,
            String languageCode,
            String label,
            String url,
            boolean isPrimary,
            Instant createdAt
    ) {
        this.filmId = Objects.requireNonNull(filmId, "filmId must not be null");
        this.languageCode = languageCode;
        this.label = label;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.isPrimary = isPrimary;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public void update(
            String languageCode,
            String label,
            String url,
            boolean isPrimary
    ) {
        this.languageCode = languageCode;
        this.label = label;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.isPrimary = isPrimary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilmLink other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
