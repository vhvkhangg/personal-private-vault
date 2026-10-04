package com.vhvkhangg.personalprivatevault.collection.software.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record SoftwareSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
