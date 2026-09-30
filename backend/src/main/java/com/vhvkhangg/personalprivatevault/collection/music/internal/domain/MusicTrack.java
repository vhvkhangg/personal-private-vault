package com.vhvkhangg.personalprivatevault.collection.music.internal.domain;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;
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
 * Music track entity sharing identity with {@code vault_entries.id} for type {@code MUSIC}.
 */
@Entity
@Table(name = "music_tracks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicTrack implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "version", nullable = false)
    private MusicVersion version;

    @Column(name = "platform_id")
    private Long platformId;

    @Column(name = "url", length = 2048)
    private String url;

    @Transient
    private boolean isNew = false;

    public MusicTrack(
            Long id,
            String title,
            MusicVersion version,
            Long platformId,
            String url
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.platformId = platformId;
        this.url = url;
        this.isNew = true;
    }

    public void update(
            String title,
            MusicVersion version,
            Long platformId,
            String url
    ) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.platformId = platformId;
        this.url = url;
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
        if (!(o instanceof MusicTrack other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
