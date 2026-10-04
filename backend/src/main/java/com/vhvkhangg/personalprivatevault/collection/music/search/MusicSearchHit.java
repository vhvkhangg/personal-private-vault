package com.vhvkhangg.personalprivatevault.collection.music.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record MusicSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
