package com.vhvkhangg.personalprivatevault.fiction.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record FictionSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
