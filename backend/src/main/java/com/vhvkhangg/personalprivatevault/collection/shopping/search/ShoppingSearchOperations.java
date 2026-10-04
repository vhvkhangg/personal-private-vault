package com.vhvkhangg.personalprivatevault.collection.shopping.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ShoppingSearchOperations {
    List<ShoppingSearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, ShoppingSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
