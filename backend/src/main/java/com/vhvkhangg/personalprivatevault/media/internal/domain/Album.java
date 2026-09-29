package com.vhvkhangg.personalprivatevault.media.internal.domain;

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
import org.springframework.data.domain.Persistable;

import java.util.Objects;

/**
 * Album entity sharing identity with {@code vault_entries.id} for type {@code ALBUM}.
 */
@Entity
@Table(name = "albums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Transient
    private boolean isNew = false;

    public Album(Long id, String title, String description, boolean isNew) {
        this.id = Objects.requireNonNull(id, "Album id must not be null");
        this.title = Objects.requireNonNull(title, "Album title must not be null");
        this.description = description;
        this.isNew = isNew;
    }

    public void update(String title, String description) {
        this.title = Objects.requireNonNull(title, "Album title must not be null");
        this.description = description;
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
        if (!(o instanceof Album other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
