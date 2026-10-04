package com.vhvkhangg.personalprivatevault.collection.music.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MusicSearchOperations {
    List<MusicSearchHit> search(String query, Set<Long> requiredTagIds, int limit);

    Map<Long, MusicSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
