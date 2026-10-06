package com.vhvkhangg.personalprivatevault.search.internal.web.dto;

import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.search.enums.SearchMatchKind;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record GlobalSearchResultResponse(
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
