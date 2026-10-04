package com.vhvkhangg.personalprivatevault.location.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocationSearchOperations {
    List<LocationSearchHit> search(LocationSearchQuery query);

    Map<Long, LocationSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
