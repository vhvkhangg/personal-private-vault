package com.vhvkhangg.personalprivatevault.feed.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record FeedSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
