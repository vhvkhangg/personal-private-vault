package com.vhvkhangg.personalprivatevault.knowledge.study.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record StudySearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
