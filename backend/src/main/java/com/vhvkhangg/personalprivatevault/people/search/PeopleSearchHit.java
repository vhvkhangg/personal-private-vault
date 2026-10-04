package com.vhvkhangg.personalprivatevault.people.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record PeopleSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
