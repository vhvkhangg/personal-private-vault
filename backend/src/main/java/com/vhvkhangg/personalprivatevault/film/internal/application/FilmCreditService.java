package com.vhvkhangg.personalprivatevault.film.internal.application;

import com.vhvkhangg.personalprivatevault.film.credit.FilmCreditOperations;
import com.vhvkhangg.personalprivatevault.film.credit.command.CreateFilmCreditCommand;
import com.vhvkhangg.personalprivatevault.film.credit.exception.InvalidFilmCreditException;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmCredit;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmCreditRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.film.view.FilmCreditView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link FilmCreditOperations}.
 */
@Service
public class FilmCreditService implements FilmCreditOperations {

    private final FilmCreditRepository filmCreditRepository;
    private final FilmRepository filmRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final PersonOperations personOperations;

    @Autowired
    public FilmCreditService(
            FilmCreditRepository filmCreditRepository,
            FilmRepository filmRepository,
            VaultEntryOperations vaultEntryOperations,
            PersonOperations personOperations) {
        this.filmCreditRepository = Objects.requireNonNull(filmCreditRepository, "filmCreditRepository must not be null");
        this.filmRepository = Objects.requireNonNull(filmRepository, "filmRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.personOperations = Objects.requireNonNull(personOperations, "personOperations must not be null");
    }

    @Override
    @Transactional
    public FilmCreditView create(CreateFilmCreditCommand command) {
        if (command == null) {
            throw new InvalidFilmCreditException("CreateFilmCreditCommand must not be null");
        }
        if (command.filmId() == null) {
            throw new InvalidFilmCreditException("filmId must not be null");
        }
        if (!filmRepository.existsById(command.filmId())) {
            throw new FilmNotFoundException(command.filmId());
        }

        if (command.personId() == null) {
            throw new InvalidFilmCreditException("personId must not be null");
        }
        if (personOperations.find(command.personId()).isEmpty()) {
            throw new InvalidFilmCreditException("Person with ID " + command.personId() + " does not exist");
        }

        if (command.role() == null) {
            throw new InvalidFilmCreditException("Role must not be null");
        }

        String characterName = validateCharacterName(command.characterName());
        String note = validateNote(command.note());

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.FILM_CREDIT);
        FilmCredit credit = new FilmCredit(
                vaultEntry.id(),
                command.filmId(),
                command.personId(),
                command.role(),
                characterName,
                note
        );
        FilmCredit saved = filmCreditRepository.save(credit);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilmCreditView> find(Long creditId) {
        if (creditId == null) {
            return Optional.empty();
        }
        return filmCreditRepository.findById(creditId).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilmCreditView> findByFilmId(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmCreditException("filmId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        return filmCreditRepository.findByFilmIdOrderByIdAsc(filmId)
                .stream()
                .map(this::toView)
                .toList();
    }

    private String validateCharacterName(String characterName) {
        if (characterName == null || characterName.isBlank()) {
            return null;
        }
        String trimmed = characterName.trim();
        if (trimmed.length() > 255) {
            throw new InvalidFilmCreditException("Character name must not exceed 255 characters");
        }
        return trimmed;
    }

    private String validateNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        return note.trim();
    }

    private FilmCreditView toView(FilmCredit credit) {
        return new FilmCreditView(
                credit.getId(),
                credit.getFilmId(),
                credit.getPersonId(),
                credit.getRole(),
                credit.getCharacterName(),
                credit.getNote()
        );
    }
}
