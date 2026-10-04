package com.vhvkhangg.personalprivatevault.feed.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FeedSearchOperations {
    List<FeedSearchHit> search(FeedSearchQuery query);

    Map<Long, FeedSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
