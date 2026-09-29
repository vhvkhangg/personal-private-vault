package com.vhvkhangg.personalprivatevault.film.internal.application;

import com.vhvkhangg.personalprivatevault.film.genre.FilmGenreOperations;
import com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.command.UpdateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.InvalidFilmGenreException;
import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmGenre;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreRepository;
import com.vhvkhangg.personalprivatevault.film.view.FilmGenreView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link FilmGenreOperations}.
 */
@Service
public class FilmGenreService implements FilmGenreOperations {

    private final FilmGenreRepository filmGenreRepository;

    @Autowired
    public FilmGenreService(FilmGenreRepository filmGenreRepository) {
        this.filmGenreRepository = Objects.requireNonNull(filmGenreRepository, "filmGenreRepository must not be null");
    }

    @Override
    @Transactional
    public FilmGenreView create(CreateFilmGenreCommand command) {
        if (command == null) {
            throw new InvalidFilmGenreException("CreateFilmGenreCommand must not be null");
        }
        String name = validateName(command.name());
        String description = validateDescription(command.description());

        if (filmGenreRepository.existsByNameIgnoreCase(name)) {
            throw new FilmGenreNameAlreadyExistsException(name);
        }

        try {
            FilmGenre created = filmGenreRepository.saveAndFlush(
                    new FilmGenre(name, description)
            );
            return toView(created);
        } catch (DataIntegrityViolationException ex) {
            throw new FilmGenreNameAlreadyExistsException(name, ex);
        }
    }

    @Override
    @Transactional
    public FilmGenreView update(UpdateFilmGenreCommand command) {
        if (command == null) {
            throw new InvalidFilmGenreException("UpdateFilmGenreCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFilmGenreException("Genre id must not be null");
        }
        String name = validateName(command.name());
        String description = validateDescription(command.description());

        FilmGenre genre = filmGenreRepository.findById(command.id())
                .orElseThrow(() -> new FilmGenreNotFoundException(command.id()));

        filmGenreRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), command.id())) {
                throw new FilmGenreNameAlreadyExistsException(name);
            }
        });

        genre.update(name, description);
        try {
            FilmGenre saved = filmGenreRepository.saveAndFlush(genre);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new FilmGenreNameAlreadyExistsException(name, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilmGenreView> find(Long genreId) {
        if (genreId == null) {
            return Optional.empty();
        }
        return filmGenreRepository.findById(genreId).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilmGenreView> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return filmGenreRepository.findByNameIgnoreCase(name.trim()).map(this::toView);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidFilmGenreException("Genre name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 150) {
            throw new InvalidFilmGenreException("Genre name must not exceed 150 characters");
        }
        return trimmed;
    }

    private String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private FilmGenreView toView(FilmGenre genre) {
        return new FilmGenreView(genre.getId(), genre.getName(), genre.getDescription());
    }
}
