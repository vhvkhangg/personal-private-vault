package com.vhvkhangg.personalprivatevault.collection.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CollectionSearchOperations {
    List<CollectionSearchHit> search(CollectionSearchQuery query);

    Map<Long, CollectionSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
