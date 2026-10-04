package com.vhvkhangg.personalprivatevault.media.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MediaSearchOperations {
    List<MediaSearchHit> search(MediaSearchQuery query);

    Map<Long, MediaSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
