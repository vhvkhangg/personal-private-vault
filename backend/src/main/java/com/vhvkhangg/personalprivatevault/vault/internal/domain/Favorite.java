package com.vhvkhangg.personalprivatevault.vault.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @Column(name = "vault_entry_id", nullable = false)
    private Long vaultEntryId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Favorite(Long vaultEntryId, Instant createdAt) {
        this.vaultEntryId = Objects.requireNonNull(vaultEntryId, "vaultEntryId must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
