package com.vhvkhangg.personalprivatevault.film.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmSearchOperations {
    List<FilmSearchHit> search(FilmSearchQuery query);

    Map<Long, FilmSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}
