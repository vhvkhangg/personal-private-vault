package com.vhvkhangg.personalprivatevault.fiction.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.genre.FictionGenreOperations;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.UpdateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.InvalidFictionGenreException;
import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionGenre;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionGenreRepository;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link FictionGenreOperations}.
 */
@Service
public class FictionGenreService implements FictionGenreOperations {

    private final FictionGenreRepository fictionGenreRepository;

    @Autowired
    public FictionGenreService(FictionGenreRepository fictionGenreRepository) {
        this.fictionGenreRepository = Objects.requireNonNull(fictionGenreRepository, "fictionGenreRepository must not be null");
    }

    @Override
    @Transactional
    public FictionGenreView create(CreateFictionGenreCommand command) {
        if (command == null) {
            throw new InvalidFictionGenreException("CreateFictionGenreCommand must not be null");
        }
        String name = validateName(command.name());
        String description = validateDescription(command.description());

        if (fictionGenreRepository.existsByNameIgnoreCase(name)) {
            throw new FictionGenreNameAlreadyExistsException(name);
        }

        try {
            FictionGenre created = fictionGenreRepository.saveAndFlush(
                    new FictionGenre(name, description)
            );
            return toView(created);
        } catch (DataIntegrityViolationException ex) {
            throw new FictionGenreNameAlreadyExistsException(name, ex);
        }
    }

    @Override
    @Transactional
    public FictionGenreView update(UpdateFictionGenreCommand command) {
        if (command == null) {
            throw new InvalidFictionGenreException("UpdateFictionGenreCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFictionGenreException("Genre id must not be null");
        }
        String name = validateName(command.name());
        String description = validateDescription(command.description());

        FictionGenre genre = fictionGenreRepository.findById(command.id())
                .orElseThrow(() -> new FictionGenreNotFoundException(command.id()));

        fictionGenreRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), command.id())) {
                throw new FictionGenreNameAlreadyExistsException(name);
            }
        });

        genre.update(name, description);
        try {
            FictionGenre saved = fictionGenreRepository.saveAndFlush(genre);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new FictionGenreNameAlreadyExistsException(name, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FictionGenreView> find(Long genreId) {
        if (genreId == null) {
            return Optional.empty();
        }
        return fictionGenreRepository.findById(genreId).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FictionGenreView> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return fictionGenreRepository.findByNameIgnoreCase(name.trim()).map(this::toView);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidFictionGenreException("Genre name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 150) {
            throw new InvalidFictionGenreException("Genre name must not exceed 150 characters");
        }
        return trimmed;
    }

    private String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private FictionGenreView toView(FictionGenre genre) {
        return new FictionGenreView(
                genre.getId(),
                genre.getName(),
                genre.getDescription()
        );
    }
}
