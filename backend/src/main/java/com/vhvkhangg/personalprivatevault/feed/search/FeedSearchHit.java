package com.vhvkhangg.personalprivatevault.feed.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record FeedSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
