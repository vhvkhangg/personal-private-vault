package com.vhvkhangg.personalprivatevault.knowledge.study.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record StudySearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
