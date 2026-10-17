package com.vhvkhangg.personalprivatevault.film.source;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Public capability interface for retrieving authoritative RAG source projections for films.
 */
public interface FilmSourceOperations {

    /**
     * Finds a single authoritative FilmSourceProjection by film ID.
     */
    Optional<FilmSourceProjection> findSource(Long filmId);

    /**
     * Finds multiple authoritative FilmSourceProjections by film IDs.
     */
    List<FilmSourceProjection> findSources(Collection<Long> filmIds);

    /**
     * Bounded enumeration of FilmSourceProjections.
     */
    List<FilmSourceProjection> enumerateSources(int offset, int limit);
}
