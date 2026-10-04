package com.vhvkhangg.personalprivatevault.fiction.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record FictionSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
