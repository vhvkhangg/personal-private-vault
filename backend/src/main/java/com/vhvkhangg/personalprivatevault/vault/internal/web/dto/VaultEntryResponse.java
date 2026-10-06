package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import java.time.Instant;

public record VaultEntryResponse(
        Long id,
        VaultEntryType entryType,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
