package com.vhvkhangg.personalprivatevault.film.link;

import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.link.command.CreateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.command.UpdateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.exception.FilmLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.film.link.exception.InvalidFilmLinkException;
import com.vhvkhangg.personalprivatevault.film.view.FilmLinkView;

import java.util.List;
import java.util.Optional;

/**
 * Public synchronous capability contract for film external links.
 */
public interface FilmLinkOperations {

    /**
     * Creates a new link for a film work.
     *
     * @param command creation command
     * @return immutable view of the created link
     * @throws FilmNotFoundException if the parent film does not exist
     * @throws InvalidFilmLinkException if link attributes or language code are invalid
     */
    FilmLinkView create(CreateFilmLinkCommand command);

    /**
     * Updates an existing link scoped to its parent film.
     *
     * @param command update command
     * @return immutable view of the updated link
     * @throws FilmNotFoundException if the parent film does not exist
     * @throws FilmLinkNotFoundException if the link does not exist or does not belong to the film
     * @throws InvalidFilmLinkException if updated attributes or language code are invalid
     */
    FilmLinkView update(UpdateFilmLinkCommand command);

    /**
     * Retrieves all links for a given film work ordered chronologically.
     *
     * @param filmId ID of the parent film
     * @return immutable list of link views
     * @throws FilmNotFoundException if the parent film does not exist
     * @throws InvalidFilmLinkException if filmId is null
     */
    List<FilmLinkView> findByFilmId(Long filmId);

    /**
     * Finds a specific link by its ID scoped to its parent film.
     *
     * @param filmId ID of the parent film
     * @param linkId ID of the link
     * @return optional containing the link view if found and belonging to the film, or empty
     */
    Optional<FilmLinkView> findById(Long filmId, Long linkId);
}
