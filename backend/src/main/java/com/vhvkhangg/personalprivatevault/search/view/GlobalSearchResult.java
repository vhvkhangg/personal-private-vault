package com.vhvkhangg.personalprivatevault.search.view;

import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.search.enums.SearchMatchKind;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

/**
 * Normalized navigation result representing a match in global search.
 */
public record GlobalSearchResult(
        Long vaultEntryId,
        VaultEntryType entryType,
        SearchDomain domain,
        String primaryText,
        String secondaryText,
        String snippet,
        SearchMatchKind matchKind,
        int rankBucket,
        double similarity
) {}
