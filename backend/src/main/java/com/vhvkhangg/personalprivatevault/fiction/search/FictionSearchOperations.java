package com.vhvkhangg.personalprivatevault.fiction.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FictionSearchOperations {
    List<FictionSearchHit> search(FictionSearchQuery query);

    Map<Long, FictionSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
