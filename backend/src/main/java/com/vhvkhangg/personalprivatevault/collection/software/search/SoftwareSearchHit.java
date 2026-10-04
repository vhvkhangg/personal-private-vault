package com.vhvkhangg.personalprivatevault.collection.software.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record SoftwareSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
