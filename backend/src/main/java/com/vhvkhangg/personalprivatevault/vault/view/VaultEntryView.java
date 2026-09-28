package com.vhvkhangg.personalprivatevault.vault.view;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import java.time.Instant;

/** Public read model for shared vault identity and recycle-bin state. */
public record VaultEntryView(Long id, VaultEntryType entryType, Instant createdAt, Instant updatedAt, Instant deletedAt) {
}
