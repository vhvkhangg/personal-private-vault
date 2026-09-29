package com.vhvkhangg.personalprivatevault.film.genre;

import com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.command.UpdateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.InvalidFilmGenreException;
import com.vhvkhangg.personalprivatevault.film.view.FilmGenreView;

import java.util.Optional;

/**
 * Public synchronous capability contract for film-owned genres.
 */
public interface FilmGenreOperations {

    /**
     * Creates a new film genre with a case-insensitive unique name.
     *
     * @param command creation command
     * @return immutable view of the created genre
     * @throws FilmGenreNameAlreadyExistsException if a genre with the same name already exists
     * @throws InvalidFilmGenreException if inputs violate validation constraints
     */
    FilmGenreView create(CreateFilmGenreCommand command);

    /**
     * Updates an existing film genre.
     *
     * @param command update command
     * @return immutable view of the updated genre
     * @throws FilmGenreNotFoundException if the genre does not exist
     * @throws FilmGenreNameAlreadyExistsException if the new name conflicts with an existing genre
     * @throws InvalidFilmGenreException if inputs violate validation constraints
     */
    FilmGenreView update(UpdateFilmGenreCommand command);

    /**
     * Finds a film genre by ID.
     *
     * @param genreId genre ID
     * @return optional containing the genre view if found, or empty
     */
    Optional<FilmGenreView> find(Long genreId);

    /**
     * Finds a film genre by name (case-insensitive).
     *
     * @param name genre name
     * @return optional containing the genre view if found, or empty
     */
    Optional<FilmGenreView> findByName(String name);
}
