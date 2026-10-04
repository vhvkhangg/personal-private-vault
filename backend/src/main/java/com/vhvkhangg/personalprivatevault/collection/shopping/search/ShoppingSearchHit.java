package com.vhvkhangg.personalprivatevault.collection.shopping.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record ShoppingSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
