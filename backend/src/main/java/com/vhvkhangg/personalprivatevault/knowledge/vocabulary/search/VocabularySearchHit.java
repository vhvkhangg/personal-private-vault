package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record VocabularySearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
