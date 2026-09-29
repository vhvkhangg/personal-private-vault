package com.vhvkhangg.personalprivatevault.film.credit;

import com.vhvkhangg.personalprivatevault.film.credit.command.CreateFilmCreditCommand;
import com.vhvkhangg.personalprivatevault.film.credit.exception.InvalidFilmCreditException;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.view.FilmCreditView;

import java.util.List;
import java.util.Optional;

/**
 * Public synchronous capability contract for film credits.
 */
public interface FilmCreditOperations {

    /**
     * Creates a new film credit backed by a newly created Vault Entry with type FILM_CREDIT.
     * Each successful invocation creates a distinct record.
     *
     * @param command creation command
     * @return immutable view of the created film credit
     * @throws FilmNotFoundException if the parent film does not exist
     * @throws InvalidFilmCreditException if the person does not exist or inputs violate validation rules
     */
    FilmCreditView create(CreateFilmCreditCommand command);

    /**
     * Finds a film credit by its ID (identical to the underlying vault entry ID).
     *
     * @param creditId credit ID
     * @return optional containing the credit view if found, or empty
     */
    Optional<FilmCreditView> find(Long creditId);

    /**
     * Retrieves all credits for a given film work.
     *
     * @param filmId ID of the parent film
     * @return immutable list of credit views
     * @throws FilmNotFoundException if the parent film does not exist
     * @throws InvalidFilmCreditException if filmId is null
     */
    List<FilmCreditView> findByFilmId(Long filmId);
}
