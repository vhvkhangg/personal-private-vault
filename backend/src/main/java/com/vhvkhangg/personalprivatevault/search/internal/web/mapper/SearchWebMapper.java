package com.vhvkhangg.personalprivatevault.search.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.search.internal.web.dto.GlobalSearchResultResponse;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchResult;

public final class SearchWebMapper {

    private SearchWebMapper() {}

    public static GlobalSearchResultResponse toResponse(GlobalSearchResult result) {
        return new GlobalSearchResultResponse(
                result.vaultEntryId(),
                result.entryType(),
                result.domain(),
                result.primaryText(),
                result.secondaryText(),
                result.snippet(),
                result.matchKind(),
                result.rankBucket(),
                result.similarity()
        );
    }
}
