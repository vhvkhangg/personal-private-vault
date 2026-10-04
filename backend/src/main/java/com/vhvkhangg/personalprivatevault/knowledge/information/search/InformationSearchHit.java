package com.vhvkhangg.personalprivatevault.knowledge.information.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record InformationSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
