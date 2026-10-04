package com.vhvkhangg.personalprivatevault.film.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record FilmSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
