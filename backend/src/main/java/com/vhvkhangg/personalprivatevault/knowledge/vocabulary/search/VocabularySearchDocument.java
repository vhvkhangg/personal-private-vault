package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record VocabularySearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
