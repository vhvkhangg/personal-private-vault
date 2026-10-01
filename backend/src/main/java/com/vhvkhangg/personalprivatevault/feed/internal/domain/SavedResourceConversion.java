package com.vhvkhangg.personalprivatevault.feed.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity mapping conversion provenance in {@code saved_resource_conversions}.
 */
@Entity
@Table(name = "saved_resource_conversions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedResourceConversion implements Persistable<SavedResourceConversionId> {

    @EmbeddedId
    private SavedResourceConversionId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private boolean isNew;

    public SavedResourceConversion(Long savedResourceId, Long targetVaultEntryId) {
        this.id = new SavedResourceConversionId(
                Objects.requireNonNull(savedResourceId, "savedResourceId must not be null"),
                Objects.requireNonNull(targetVaultEntryId, "targetVaultEntryId must not be null")
        );
        this.createdAt = Instant.now();
        this.isNew = true;
    }

    @Override
    public SavedResourceConversionId getId() {
        return id;
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
