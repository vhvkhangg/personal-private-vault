package com.vhvkhangg.personalprivatevault.fiction.source;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Public capability interface for retrieving authoritative RAG source projections for fiction.
 */
public interface FictionSourceOperations {

    /**
     * Finds a single authoritative FictionSourceProjection by fiction ID.
     */
    Optional<FictionSourceProjection> findSource(Long fictionId);

    /**
     * Finds multiple authoritative FictionSourceProjections by fiction IDs.
     */
    List<FictionSourceProjection> findSources(Collection<Long> fictionIds);

    /**
     * Bounded enumeration of FictionSourceProjections.
     */
    List<FictionSourceProjection> enumerateSources(int offset, int limit);
}
