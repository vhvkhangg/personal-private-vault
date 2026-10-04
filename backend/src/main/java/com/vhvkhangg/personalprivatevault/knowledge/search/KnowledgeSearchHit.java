package com.vhvkhangg.personalprivatevault.knowledge.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record KnowledgeSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
