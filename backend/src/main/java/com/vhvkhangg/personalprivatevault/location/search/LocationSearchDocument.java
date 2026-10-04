package com.vhvkhangg.personalprivatevault.location.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record LocationSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
