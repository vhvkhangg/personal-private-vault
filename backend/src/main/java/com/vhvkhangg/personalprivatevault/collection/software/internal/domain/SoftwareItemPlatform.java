package com.vhvkhangg.personalprivatevault.collection.software.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Join entity mapping the {@code software_item_platforms} supported platform assignments.
 */
@Entity
@Table(name = "software_item_platforms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SoftwareItemPlatform {

    @EmbeddedId
    private SoftwareItemPlatformId id;

    public SoftwareItemPlatform(SoftwareItemPlatformId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public SoftwareItemPlatform(Long softwareId, Long platformId) {
        this(new SoftwareItemPlatformId(softwareId, platformId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoftwareItemPlatform other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
