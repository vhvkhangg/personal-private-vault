package com.vhvkhangg.personalprivatevault.search.view;

import java.util.List;

/**
 * Paginated container of global search results.
 */
public record GlobalSearchPage(
        int offset,
        int limit,
        List<GlobalSearchResult> items,
        boolean hasMore
) {
    public GlobalSearchPage {
        items = items != null ? List.copyOf(items) : List.of();
    }
}
