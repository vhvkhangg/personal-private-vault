package com.vhvkhangg.personalprivatevault.film.internal.domain;

import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;
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
 * Film credit entity sharing identity with {@code vault_entries.id} for type {@code FILM_CREDIT}.
 */
@Entity
@Table(name = "film_credits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilmCredit implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "film_id", nullable = false)
    private Long filmId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private FilmCreditRole role;

    @Column(name = "character_name", length = 255)
    private String characterName;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Transient
    private boolean isNew = false;

    public FilmCredit(
            Long id,
            Long filmId,
            Long personId,
            FilmCreditRole role,
            String characterName,
            String note
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.filmId = Objects.requireNonNull(filmId, "filmId must not be null");
        this.personId = Objects.requireNonNull(personId, "personId must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.characterName = characterName;
        this.note = note;
        this.isNew = true;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilmCredit other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
