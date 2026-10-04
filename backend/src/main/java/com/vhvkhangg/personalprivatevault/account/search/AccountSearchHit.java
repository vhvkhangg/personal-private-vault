package com.vhvkhangg.personalprivatevault.account.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record AccountSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
