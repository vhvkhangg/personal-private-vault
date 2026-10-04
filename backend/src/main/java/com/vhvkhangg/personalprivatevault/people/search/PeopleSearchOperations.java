package com.vhvkhangg.personalprivatevault.people.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PeopleSearchOperations {
    List<PeopleSearchHit> search(PeopleSearchQuery query);

    Map<Long, PeopleSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
