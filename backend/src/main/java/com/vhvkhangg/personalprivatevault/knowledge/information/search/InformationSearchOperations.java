package com.vhvkhangg.personalprivatevault.knowledge.information.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InformationSearchOperations {
    List<InformationSearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, InformationSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
