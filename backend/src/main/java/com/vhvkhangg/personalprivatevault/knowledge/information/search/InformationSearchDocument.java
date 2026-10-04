package com.vhvkhangg.personalprivatevault.knowledge.information.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record InformationSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
