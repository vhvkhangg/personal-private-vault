package com.vhvkhangg.personalprivatevault.collection.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record CollectionSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
