package com.vhvkhangg.personalprivatevault.people.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record PeopleSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
