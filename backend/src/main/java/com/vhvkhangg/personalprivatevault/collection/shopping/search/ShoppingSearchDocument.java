package com.vhvkhangg.personalprivatevault.collection.shopping.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record ShoppingSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
