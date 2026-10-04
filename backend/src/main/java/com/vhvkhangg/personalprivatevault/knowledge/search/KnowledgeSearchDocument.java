package com.vhvkhangg.personalprivatevault.knowledge.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record KnowledgeSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
