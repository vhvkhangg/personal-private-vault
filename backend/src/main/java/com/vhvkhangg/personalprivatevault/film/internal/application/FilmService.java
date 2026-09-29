package com.vhvkhangg.personalprivatevault.film.internal.application;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.film.exception.InvalidFilmException;
import com.vhvkhangg.personalprivatevault.film.internal.domain.Film;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreAssignmentRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.film.view.FilmClassificationsView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Application service implementing {@link FilmOperations}.
 */
@Service
public class FilmService implements FilmOperations {

    private final FilmRepository filmRepository;
    private final FilmGenreRepository filmGenreRepository;
    private final FilmGenreAssignmentRepository filmGenreAssignmentRepository;
    private final FilmStoryArchetypeRepository storyArchetypeRepository;
    private final FilmWorldSettingRepository worldSettingRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final PersonOperations personOperations;
    private final ReferenceCatalog referenceCatalog;

    @Autowired
    public FilmService(
            FilmRepository filmRepository,
            FilmGenreRepository filmGenreRepository,
            FilmGenreAssignmentRepository filmGenreAssignmentRepository,
            FilmStoryArchetypeRepository storyArchetypeRepository,
            FilmWorldSettingRepository worldSettingRepository,
            VaultEntryOperations vaultEntryOperations,
            PersonOperations personOperations,
            ReferenceCatalog referenceCatalog) {
        this.filmRepository = Objects.requireNonNull(filmRepository, "filmRepository must not be null");
        this.filmGenreRepository = Objects.requireNonNull(filmGenreRepository, "filmGenreRepository must not be null");
        this.filmGenreAssignmentRepository = Objects.requireNonNull(filmGenreAssignmentRepository, "filmGenreAssignmentRepository must not be null");
        this.storyArchetypeRepository = Objects.requireNonNull(storyArchetypeRepository, "storyArchetypeRepository must not be null");
        this.worldSettingRepository = Objects.requireNonNull(worldSettingRepository, "worldSettingRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.personOperations = Objects.requireNonNull(personOperations, "personOperations must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
    }

    @Override
    @Transactional
    public FilmView create(CreateFilmCommand command) {
        if (command == null) {
            throw new InvalidFilmException("CreateFilmCommand must not be null");
        }
        ValidatedFilm validated = validateFilm(
                command.title(),
                command.originalTitle(),
                command.nationalityCode(),
                command.posterUrl(),
                command.format(),
                command.productionStyle(),
                command.isNsfw(),
                command.directorPersonId(),
                command.description(),
                command.totalEpisodes(),
                command.progressStatus(),
                command.consumptionStatus(),
                command.currentProgressText(),
                command.review()
        );

        if (command.genreIds() != null) {
            for (Long genreId : command.genreIds()) {
                validateGenreExists(genreId);
            }
        }
        if (command.storyArchetypeIds() != null) {
            for (Long archetypeId : command.storyArchetypeIds()) {
                validateStoryArchetypeExists(archetypeId);
            }
        }
        if (command.worldSettingIds() != null) {
            for (Long settingId : command.worldSettingIds()) {
                validateWorldSettingExists(settingId);
            }
        }

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.FILM);
        Film film = new Film(
                vaultEntry.id(),
                validated.title(),
                validated.originalTitle(),
                validated.nationalityCode(),
                validated.posterUrl(),
                validated.format(),
                validated.productionStyle(),
                validated.isNsfw(),
                validated.directorPersonId(),
                validated.description(),
                validated.totalEpisodes(),
                validated.progressStatus(),
                validated.consumptionStatus(),
                validated.currentProgressText(),
                validated.review()
        );
        Film saved = filmRepository.save(film);

        if (command.genreIds() != null) {
            for (Long genreId : command.genreIds()) {
                filmGenreAssignmentRepository.insertIfAbsent(saved.getId(), genreId);
            }
        }
        if (command.storyArchetypeIds() != null) {
            for (Long archetypeId : command.storyArchetypeIds()) {
                storyArchetypeRepository.insertIfAbsent(saved.getId(), archetypeId);
            }
        }
        if (command.worldSettingIds() != null) {
            for (Long settingId : command.worldSettingIds()) {
                worldSettingRepository.insertIfAbsent(saved.getId(), settingId);
            }
        }

        return toView(
                saved,
                loadGenres(saved.getId()),
                loadStoryArchetypes(saved.getId()),
                loadWorldSettings(saved.getId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilmView> find(Long filmId) {
        if (filmId == null) {
            return Optional.empty();
        }
        return filmRepository.findById(filmId).map(film ->
                toView(
                        film,
                        loadGenres(filmId),
                        loadStoryArchetypes(filmId),
                        loadWorldSettings(filmId)
                )
        );
    }

    @Override
    @Transactional
    public FilmView update(UpdateFilmCommand command) {
        if (command == null) {
            throw new InvalidFilmException("UpdateFilmCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFilmException("Film id must not be null");
        }

        Film film = filmRepository.findById(command.id())
                .orElseThrow(() -> new FilmNotFoundException(command.id()));

        ValidatedFilm validated = validateFilm(
                command.title(),
                command.originalTitle(),
                command.nationalityCode(),
                command.posterUrl(),
                command.format(),
                command.productionStyle(),
                command.isNsfw(),
                command.directorPersonId(),
                command.description(),
                command.totalEpisodes(),
                command.progressStatus(),
                command.consumptionStatus(),
                command.currentProgressText(),
                command.review()
        );

        film.update(
                validated.title(),
                validated.originalTitle(),
                validated.nationalityCode(),
                validated.posterUrl(),
                validated.format(),
                validated.productionStyle(),
                validated.isNsfw(),
                validated.directorPersonId(),
                validated.description(),
                validated.totalEpisodes(),
                validated.progressStatus(),
                validated.consumptionStatus(),
                validated.currentProgressText(),
                validated.review()
        );
        Film saved = filmRepository.save(film);

        return toView(
                saved,
                loadGenres(saved.getId()),
                loadStoryArchetypes(saved.getId()),
                loadWorldSettings(saved.getId())
        );
    }

    @Override
    @Transactional
    public void addGenre(Long filmId, Long genreId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (genreId == null) {
            throw new InvalidFilmException("genreId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        validateGenreExists(genreId);
        filmGenreAssignmentRepository.insertIfAbsent(filmId, genreId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getGenres(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        return loadGenres(filmId);
    }

    @Override
    @Transactional
    public void addStoryArchetype(Long filmId, Long storyArchetypeId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (storyArchetypeId == null) {
            throw new InvalidFilmException("storyArchetypeId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        validateStoryArchetypeExists(storyArchetypeId);
        storyArchetypeRepository.insertIfAbsent(filmId, storyArchetypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getStoryArchetypes(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        return loadStoryArchetypes(filmId);
    }

    @Override
    @Transactional
    public void addWorldSetting(Long filmId, Long worldSettingId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (worldSettingId == null) {
            throw new InvalidFilmException("worldSettingId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        validateWorldSettingExists(worldSettingId);
        worldSettingRepository.insertIfAbsent(filmId, worldSettingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getWorldSettings(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        return loadWorldSettings(filmId);
    }

    @Override
    @Transactional(readOnly = true)
    public FilmClassificationsView getClassifications(Long filmId) {
        if (filmId == null) {
            throw new InvalidFilmException("filmId must not be null");
        }
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        return new FilmClassificationsView(
                filmId,
                loadGenres(filmId),
                loadStoryArchetypes(filmId),
                loadWorldSettings(filmId)
        );
    }

    private void validateGenreExists(Long genreId) {
        if (genreId == null) {
            throw new InvalidFilmException("genreId must not be null");
        }
        if (!filmGenreRepository.existsById(genreId)) {
            throw new InvalidFilmException("Film genre with ID " + genreId + " does not exist");
        }
    }

    private void validateStoryArchetypeExists(Long storyArchetypeId) {
        if (storyArchetypeId == null) {
            throw new InvalidFilmException("storyArchetypeId must not be null");
        }
        if (referenceCatalog.storyArchetype(storyArchetypeId).isEmpty()) {
            throw new InvalidFilmException("Story archetype with ID " + storyArchetypeId + " does not exist in reference catalog");
        }
    }

    private void validateWorldSettingExists(Long worldSettingId) {
        if (worldSettingId == null) {
            throw new InvalidFilmException("worldSettingId must not be null");
        }
        if (referenceCatalog.worldSetting(worldSettingId).isEmpty()) {
            throw new InvalidFilmException("World setting with ID " + worldSettingId + " does not exist in reference catalog");
        }
    }

    private Set<Long> loadGenres(Long filmId) {
        return Set.copyOf(filmGenreAssignmentRepository.findGenreIdsByFilmId(filmId));
    }

    private Set<Long> loadStoryArchetypes(Long filmId) {
        return Set.copyOf(storyArchetypeRepository.findStoryArchetypeIdsByFilmId(filmId));
    }

    private Set<Long> loadWorldSettings(Long filmId) {
        return Set.copyOf(worldSettingRepository.findWorldSettingIdsByFilmId(filmId));
    }

    private record ValidatedFilm(
            String title,
            String originalTitle,
            String nationalityCode,
            String posterUrl,
            FilmFormat format,
            FilmProductionStyle productionStyle,
            boolean isNsfw,
            Long directorPersonId,
            String description,
            Integer totalEpisodes,
            ProgressStatus progressStatus,
            ConsumptionStatus consumptionStatus,
            String currentProgressText,
            String review
    ) {
    }

    private ValidatedFilm validateFilm(
            String title,
            String originalTitle,
            String nationalityCode,
            String posterUrl,
            FilmFormat format,
            FilmProductionStyle productionStyle,
            Boolean isNsfw,
            Long directorPersonId,
            String description,
            Integer totalEpisodes,
            ProgressStatus progressStatus,
            ConsumptionStatus consumptionStatus,
            String currentProgressText,
            String review
    ) {
        if (title == null || title.isBlank()) {
            throw new InvalidFilmException("Title must not be blank");
        }
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() > 500) {
            throw new InvalidFilmException("Title must not exceed 500 characters");
        }

        String trimmedOriginalTitle = null;
        if (originalTitle != null && !originalTitle.isBlank()) {
            trimmedOriginalTitle = originalTitle.trim();
            if (trimmedOriginalTitle.length() > 500) {
                throw new InvalidFilmException("Original title must not exceed 500 characters");
            }
        }

        String validatedNationalityCode = null;
        if (nationalityCode != null && !nationalityCode.isBlank()) {
            String upper = nationalityCode.trim().toUpperCase(Locale.ROOT);
            if (referenceCatalog.country(upper).isEmpty()) {
                throw new InvalidFilmException("Nationality code '" + nationalityCode + "' does not exist in reference catalog");
            }
            validatedNationalityCode = upper;
        }

        String trimmedPosterUrl = null;
        if (posterUrl != null && !posterUrl.isBlank()) {
            trimmedPosterUrl = posterUrl.trim();
            if (trimmedPosterUrl.length() > 2048) {
                throw new InvalidFilmException("Poster URL must not exceed 2048 characters");
            }
        }

        if (format == null) {
            throw new InvalidFilmException("Format must not be null");
        }

        if (productionStyle == null) {
            throw new InvalidFilmException("Production style must not be null");
        }

        boolean resolvedIsNsfw = isNsfw != null && isNsfw;

        if (directorPersonId != null) {
            if (personOperations.find(directorPersonId).isEmpty()) {
                throw new InvalidFilmException("Director person with ID " + directorPersonId + " does not exist");
            }
        }

        String trimmedDescription = null;
        if (description != null && !description.isBlank()) {
            trimmedDescription = description.trim();
        }

        if (totalEpisodes != null && totalEpisodes < 0) {
            throw new InvalidFilmException("Total episodes must be greater than or equal to 0");
        }

        ProgressStatus resolvedProgress = progressStatus != null ? progressStatus : ProgressStatus.ONGOING;
        ConsumptionStatus resolvedConsumption = consumptionStatus != null ? consumptionStatus : ConsumptionStatus.UNCONSUMED;

        String trimmedProgressText = null;
        if (currentProgressText != null && !currentProgressText.isBlank()) {
            trimmedProgressText = currentProgressText.trim();
            if (trimmedProgressText.length() > 255) {
                throw new InvalidFilmException("Current progress text must not exceed 255 characters");
            }
        }

        String trimmedReview = null;
        if (review != null && !review.isBlank()) {
            trimmedReview = review.trim();
        }

        return new ValidatedFilm(
                trimmedTitle,
                trimmedOriginalTitle,
                validatedNationalityCode,
                trimmedPosterUrl,
                format,
                productionStyle,
                resolvedIsNsfw,
                directorPersonId,
                trimmedDescription,
                totalEpisodes,
                resolvedProgress,
                resolvedConsumption,
                trimmedProgressText,
                trimmedReview
        );
    }

    private FilmView toView(
            Film film,
            Set<Long> genreIds,
            Set<Long> storyArchetypeIds,
            Set<Long> worldSettingIds
    ) {
        return new FilmView(
                film.getId(),
                film.getTitle(),
                film.getOriginalTitle(),
                film.getNationalityCode(),
                film.getPosterUrl(),
                film.getFormat(),
                film.getProductionStyle(),
                film.isNsfw(),
                film.getDirectorPersonId(),
                film.getDescription(),
                film.getTotalEpisodes(),
                film.getProgressStatus(),
                film.getConsumptionStatus(),
                film.getCurrentProgressText(),
                film.getReview(),
                genreIds,
                storyArchetypeIds,
                worldSettingIds
        );
    }
}
