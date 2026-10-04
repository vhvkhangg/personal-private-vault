package com.vhvkhangg.personalprivatevault.location.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record LocationSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
