package com.vhvkhangg.personalprivatevault.knowledge.study.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StudySearchOperations {
    List<StudySearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, StudySearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
