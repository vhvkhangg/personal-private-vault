package com.vhvkhangg.personalprivatevault.media.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record MediaSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
