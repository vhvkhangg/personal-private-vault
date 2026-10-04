package com.vhvkhangg.personalprivatevault.collection.software.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SoftwareSearchOperations {
    List<SoftwareSearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, SoftwareSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
