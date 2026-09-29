package com.vhvkhangg.personalprivatevault.film.film;

import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.film.exception.InvalidFilmException;
import com.vhvkhangg.personalprivatevault.film.view.FilmClassificationsView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;

import java.util.Optional;
import java.util.Set;

/**
 * Public synchronous capability contract for film works and classification assignments.
 */
public interface FilmOperations {

    /**
     * Creates a new film work backed by a newly created Vault Entry with type FILM.
     *
     * @param command creation command containing film attributes
     * @return immutable view of the created film
     * @throws InvalidFilmException if attributes or references violate domain validation rules
     */
    FilmView create(CreateFilmCommand command);

    /**
     * Finds an existing film by its ID.
     *
     * @param filmId film ID (identical to vault entry ID)
     * @return optional containing the film view if found, or empty
     */
    Optional<FilmView> find(Long filmId);

    /**
     * Updates an existing film's attributes.
     *
     * @param command update command
     * @return immutable view of the updated film
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if updated attributes violate domain validation rules
     */
    FilmView update(UpdateFilmCommand command);

    /**
     * Idempotently assigns a film genre to a film.
     *
     * @param filmId film ID
     * @param genreId film genre ID owned by the film module
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if the genre does not exist or inputs are invalid
     */
    void addGenre(Long filmId, Long genreId);

    /**
     * Retrieves the set of assigned film genre IDs for a film.
     *
     * @param filmId film ID
     * @return immutable set of film genre IDs
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if inputs are invalid
     */
    Set<Long> getGenres(Long filmId);

    /**
     * Idempotently assigns a story archetype to a film.
     *
     * @param filmId film ID
     * @param storyArchetypeId story archetype ID from the reference module
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if the reference ID does not exist or inputs are invalid
     */
    void addStoryArchetype(Long filmId, Long storyArchetypeId);

    /**
     * Retrieves the set of assigned story archetype IDs for a film.
     *
     * @param filmId film ID
     * @return immutable set of story archetype IDs
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if inputs are invalid
     */
    Set<Long> getStoryArchetypes(Long filmId);

    /**
     * Idempotently assigns a world setting to a film.
     *
     * @param filmId film ID
     * @param worldSettingId world setting ID from the reference module
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if the reference ID does not exist or inputs are invalid
     */
    void addWorldSetting(Long filmId, Long worldSettingId);

    /**
     * Retrieves the set of assigned world setting IDs for a film.
     *
     * @param filmId film ID
     * @return immutable set of world setting IDs
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if inputs are invalid
     */
    Set<Long> getWorldSettings(Long filmId);

    /**
     * Retrieves all assigned genres and narrative classifications for a film.
     *
     * @param filmId film ID
     * @return immutable view of the film's classifications
     * @throws FilmNotFoundException if the film does not exist
     * @throws InvalidFilmException if inputs are invalid
     */
    FilmClassificationsView getClassifications(Long filmId);
}
