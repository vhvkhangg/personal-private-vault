package com.vhvkhangg.personalprivatevault.collection.music.internal.domain;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Join entity mapping the {@code music_track_people} credit assignments.
 */
@Entity
@Table(name = "music_track_people")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicTrackPerson {

    @EmbeddedId
    private MusicTrackPersonId id;

    public MusicTrackPerson(MusicTrackPersonId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public MusicTrackPerson(Long musicId, Long personId, MusicCreditRole role) {
        this(new MusicTrackPersonId(musicId, personId, role));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicTrackPerson other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
