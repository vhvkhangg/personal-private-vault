package com.vhvkhangg.personalprivatevault.vault.internal.domain;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "vault_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "entry_type", nullable = false)
    private VaultEntryType entryType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public VaultEntry(VaultEntryType entryType, Instant createdAt) {
        this.entryType = Objects.requireNonNull(entryType, "entryType must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean moveToTrash(Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (isDeleted()) {
            return false;
        }
        this.deletedAt = timestamp;
        this.updatedAt = timestamp;
        return true;
    }

    public boolean restore(Instant timestamp) {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (!isDeleted()) {
            return false;
        }
        this.deletedAt = null;
        this.updatedAt = timestamp;
        return true;
    }
}
