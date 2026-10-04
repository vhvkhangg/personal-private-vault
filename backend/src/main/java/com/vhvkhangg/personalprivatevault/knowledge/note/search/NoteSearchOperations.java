package com.vhvkhangg.personalprivatevault.knowledge.note.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NoteSearchOperations {
    List<NoteSearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, NoteSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
