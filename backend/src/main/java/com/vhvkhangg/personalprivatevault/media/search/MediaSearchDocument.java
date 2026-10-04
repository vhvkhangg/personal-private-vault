package com.vhvkhangg.personalprivatevault.media.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record MediaSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
