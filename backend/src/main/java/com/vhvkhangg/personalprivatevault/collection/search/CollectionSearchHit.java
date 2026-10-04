package com.vhvkhangg.personalprivatevault.collection.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record CollectionSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
