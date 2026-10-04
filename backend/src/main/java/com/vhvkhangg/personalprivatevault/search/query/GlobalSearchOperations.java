package com.vhvkhangg.personalprivatevault.search.query;

import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchPage;

/**
 * Public search operations for global discovery across personal vault modules.
 */
public interface GlobalSearchOperations {

    /**
     * Executes cross-module global search using PostgreSQL-first ranking, Vault qualification,
     * and bounded sequential fan-out.
     *
     * @param query the validated search parameters
     * @return the requested page of global search results
     */
    GlobalSearchPage search(GlobalSearchQuery query);
}
