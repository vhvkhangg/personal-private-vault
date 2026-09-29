package com.vhvkhangg.personalprivatevault.fiction.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code fiction_world_settings} join table.
 */
@Entity
@Table(name = "fiction_world_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FictionWorldSettingAssignment {

    @EmbeddedId
    private FictionWorldSettingId id;

    public FictionWorldSettingAssignment(FictionWorldSettingId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public FictionWorldSettingAssignment(Long fictionId, Long worldSettingId) {
        this(new FictionWorldSettingId(fictionId, worldSettingId));
    }
}
