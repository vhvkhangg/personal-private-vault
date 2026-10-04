package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VocabularySearchOperations {
    List<VocabularySearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, VocabularySearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
