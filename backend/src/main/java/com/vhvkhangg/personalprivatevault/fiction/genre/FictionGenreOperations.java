package com.vhvkhangg.personalprivatevault.fiction.genre;

import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.UpdateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.InvalidFictionGenreException;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;

import java.util.Optional;

/**
 * Public synchronous capability contract for fiction-owned genres.
 */
public interface FictionGenreOperations {

    /**
     * Creates a new fiction genre with a case-insensitive unique name.
     *
     * @param command creation command
     * @return immutable view of the created genre
     * @throws FictionGenreNameAlreadyExistsException if a genre with the same name already exists
     * @throws InvalidFictionGenreException if inputs violate validation constraints
     */
    FictionGenreView create(CreateFictionGenreCommand command);

    /**
     * Updates an existing fiction genre.
     *
     * @param command update command
     * @return immutable view of the updated genre
     * @throws FictionGenreNotFoundException if the genre does not exist
     * @throws FictionGenreNameAlreadyExistsException if the new name conflicts with an existing genre
     * @throws InvalidFictionGenreException if inputs violate validation constraints
     */
    FictionGenreView update(UpdateFictionGenreCommand command);

    /**
     * Finds a fiction genre by ID.
     *
     * @param genreId genre ID
     * @return optional containing the genre view if found, or empty
     */
    Optional<FictionGenreView> find(Long genreId);

    /**
     * Finds a fiction genre by name (case-insensitive).
     *
     * @param name genre name
     * @return optional containing the genre view if found, or empty
     */
    Optional<FictionGenreView> findByName(String name);
}
