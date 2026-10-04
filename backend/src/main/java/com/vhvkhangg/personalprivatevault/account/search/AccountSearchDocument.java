package com.vhvkhangg.personalprivatevault.account.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record AccountSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
