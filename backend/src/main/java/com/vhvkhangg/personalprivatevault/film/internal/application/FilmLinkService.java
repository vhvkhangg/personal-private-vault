package com.vhvkhangg.personalprivatevault.film.internal.application;

import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmLink;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmLinkRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.film.link.FilmLinkOperations;
import com.vhvkhangg.personalprivatevault.film.link.command.CreateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.command.UpdateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.exception.FilmLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.film.link.exception.InvalidFilmLinkException;
import com.vhvkhangg.personalprivatevault.film.view.FilmLinkView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link FilmLinkOperations}.
 */
@Service
public class FilmLinkService implements FilmLinkOperations {

    private final FilmLinkRepository filmLinkRepository;
    private final FilmRepository filmRepository;
    private final ReferenceCatalog referenceCatalog;
    private final Clock clock;

    @Autowired
    public FilmLinkService(
            FilmLinkRepository filmLinkRepository,
            FilmRepository filmRepository,
            ReferenceCatalog referenceCatalog,
            @Autowired(required = false) Clock clock) {
        this.filmLinkRepository = Objects.requireNonNull(filmLinkRepository, "filmLinkRepository must not be null");
        this.filmRepository = Objects.requireNonNull(filmRepository, "filmRepository must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public FilmLinkService(
            FilmLinkRepository filmLinkRepository,
            FilmRepository filmRepository,
            ReferenceCatalog referenceCatalog) {
        this(filmLinkRepository, filmRepository, referenceCatalog, Clock.systemUTC());
    }

    @Override
    @Transactional
    public FilmLinkView create(CreateFilmLinkCommand command) {
        if (command == null) {
            throw new InvalidFilmLinkException("CreateFilmLinkCommand must not be null");
        }
        validateFilmExists(command.filmId());
        String url = validateUrl(command.url());
        String label = validateLabel(command.label());
        String languageCode = validateLanguageCode(command.languageCode());
        boolean isPrimary = command.isPrimary() != null && command.isPrimary();

        FilmLink link = new FilmLink(
                command.filmId(),
                languageCode,
                label,
                url,
                isPrimary,
                clock.instant()
        );
        FilmLink saved = filmLinkRepository.save(link);
        return toView(saved);
    }

    @Override
    @Transactional
    public FilmLinkView update(UpdateFilmLinkCommand command) {
        if (command == null) {
            throw new InvalidFilmLinkException("UpdateFilmLinkCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFilmLinkException("Film link id must not be null");
        }
        if (command.filmId() == null) {
            throw new InvalidFilmLinkException("Film id must not be null");
        }
        validateFilmExists(command.filmId());

        FilmLink link = filmLinkRepository.findById(command.id())
                .orElseThrow(() -> new FilmLinkNotFoundException(command.id()));

        if (!Objects.equals(link.getFilmId(), command.filmId())) {
            throw new FilmLinkNotFoundException("Film link with ID " + command.id() + " does not belong to film " + command.filmId());
        }

        String url = validateUrl(command.url());
        String label = validateLabel(command.label());
        String languageCode = validateLanguageCode(command.languageCode());
        boolean isPrimary = command.isPrimary() != null && command.isPrimary();

        link.update(languageCode, label, url, isPrimary);
        FilmLink saved = filmLinkRepository.save(link);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilmLinkView> findByFilmId(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmLinkException("filmId must not be null");
        }
        validateFilmExists(filmId);
        return filmLinkRepository.findByFilmIdOrderByCreatedAtAscIdAsc(filmId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilmLinkView> findById(Long filmId, Long linkId) {
        if (filmId == null || linkId == null) {
            return Optional.empty();
        }
        return filmLinkRepository.findByIdAndFilmId(linkId, filmId).map(this::toView);
    }

    private void validateFilmExists(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmLinkException("filmId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidFilmLinkException("Link URL must not be blank");
        }
        String trimmed = url.trim();
        if (trimmed.length() > 2048) {
            throw new InvalidFilmLinkException("Link URL must not exceed 2048 characters");
        }
        return trimmed;
    }

    private String validateLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        String trimmed = label.trim();
        if (trimmed.length() > 255) {
            throw new InvalidFilmLinkException("Link label must not exceed 255 characters");
        }
        return trimmed;
    }

    private String validateLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return null;
        }
        String trimmed = languageCode.trim();
        if (referenceCatalog.language(trimmed).isEmpty()) {
            throw new InvalidFilmLinkException("Language code '" + trimmed + "' does not exist in reference catalog");
        }
        return trimmed;
    }

    private FilmLinkView toView(FilmLink link) {
        return new FilmLinkView(
                link.getId(),
                link.getFilmId(),
                link.getLanguageCode(),
                link.getLabel(),
                link.getUrl(),
                link.isPrimary(),
                link.getCreatedAt()
        );
    }
}
