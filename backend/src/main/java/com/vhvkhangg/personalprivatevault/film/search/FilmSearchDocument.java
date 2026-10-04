package com.vhvkhangg.personalprivatevault.film.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record FilmSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
