package com.vhvkhangg.personalprivatevault.knowledge.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KnowledgeSearchOperations {
    List<KnowledgeSearchHit> search(KnowledgeSearchQuery query);

    Map<Long, KnowledgeSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
