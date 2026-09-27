package com.vhvkhangg.personalprivatevault.vault.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "vault_entry_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VaultEntryTag {

    @EmbeddedId
    private VaultEntryTagId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public VaultEntryTag(VaultEntryTagId id, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public VaultEntryTag(Long vaultEntryId, Long tagId, Instant createdAt) {
        this(new VaultEntryTagId(vaultEntryId, tagId), createdAt);
    }
}
