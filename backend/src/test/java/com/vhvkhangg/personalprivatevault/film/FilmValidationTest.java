package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.film.credit.command.CreateFilmCreditCommand;
import com.vhvkhangg.personalprivatevault.film.credit.exception.FilmCreditNotFoundException;
import com.vhvkhangg.personalprivatevault.film.credit.exception.InvalidFilmCreditException;
import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.film.exception.InvalidFilmException;
import com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.command.UpdateFilmGenreCommand;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.film.genre.exception.InvalidFilmGenreException;
import com.vhvkhangg.personalprivatevault.film.internal.application.FilmCreditService;
import com.vhvkhangg.personalprivatevault.film.internal.application.FilmGenreService;
import com.vhvkhangg.personalprivatevault.film.internal.application.FilmLinkService;
import com.vhvkhangg.personalprivatevault.film.internal.application.FilmService;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmCreditRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreAssignmentRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmGenreRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmLinkRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence.FilmWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.film.link.command.CreateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.command.UpdateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.exception.FilmLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.film.link.exception.InvalidFilmLinkException;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.CountryView;
import com.vhvkhangg.personalprivatevault.reference.view.LanguageView;
import com.vhvkhangg.personalprivatevault.reference.view.StoryArchetypeView;
import com.vhvkhangg.personalprivatevault.reference.view.WorldSettingView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilmValidationTest {

    private FilmRepository filmRepository;
    private FilmGenreRepository filmGenreRepository;
    private FilmGenreAssignmentRepository filmGenreAssignmentRepository;
    private FilmStoryArchetypeRepository storyArchetypeRepository;
    private FilmWorldSettingRepository worldSettingRepository;
    private VaultEntryOperations vaultEntryOperations;
    private PersonOperations personOperations;
    private ReferenceCatalog referenceCatalog;
    private FilmLinkRepository filmLinkRepository;
    private FilmCreditRepository filmCreditRepository;

    private FilmService filmService;
    private FilmGenreService filmGenreService;
    private FilmLinkService filmLinkService;
    private FilmCreditService filmCreditService;

    @BeforeEach
    void setUp() {
        filmRepository = mock(FilmRepository.class);
        filmGenreRepository = mock(FilmGenreRepository.class);
        filmGenreAssignmentRepository = mock(FilmGenreAssignmentRepository.class);
        storyArchetypeRepository = mock(FilmStoryArchetypeRepository.class);
        worldSettingRepository = mock(FilmWorldSettingRepository.class);
        vaultEntryOperations = mock(VaultEntryOperations.class);
        personOperations = mock(PersonOperations.class);
        referenceCatalog = mock(ReferenceCatalog.class);
        filmLinkRepository = mock(FilmLinkRepository.class);
        filmCreditRepository = mock(FilmCreditRepository.class);

        filmService = new FilmService(
                filmRepository,
                filmGenreRepository,
                filmGenreAssignmentRepository,
                storyArchetypeRepository,
                worldSettingRepository,
                vaultEntryOperations,
                personOperations,
                referenceCatalog
        );

        filmGenreService = new FilmGenreService(filmGenreRepository);
        filmLinkService = new FilmLinkService(filmLinkRepository, filmRepository, referenceCatalog);
        filmCreditService = new FilmCreditService(filmCreditRepository, filmRepository, vaultEntryOperations, personOperations);
    }

    @Nested
    @DisplayName("Film Work Validation Tests")
    class FilmWorkValidationTests {

        @Test
        @DisplayName("Rejects null CreateFilmCommand")
        void rejectsNullCreateCommand() {
            assertThatThrownBy(() -> filmService.create(null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("CreateFilmCommand must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank or whitespace title on create")
        void rejectsBlankTitleOnCreate(String title) {
            CreateFilmCommand command = new CreateFilmCommand(
                    title, null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Title must not be blank");
        }

        @Test
        @DisplayName("Rejects title exceeding 500 characters")
        void rejectsTitleExceeding500() {
            String longTitle = "a".repeat(501);
            CreateFilmCommand command = new CreateFilmCommand(
                    longTitle, null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Title must not exceed 500 characters");
        }

        @Test
        @DisplayName("Rejects original title exceeding 500 characters")
        void rejectsOriginalTitleExceeding500() {
            String longTitle = "a".repeat(501);
            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", longTitle, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Original title must not exceed 500 characters");
        }

        @Test
        @DisplayName("Rejects poster URL exceeding 2048 characters")
        void rejectsPosterUrlExceeding2048() {
            String longUrl = "https://example.com/" + "a".repeat(2040);
            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, longUrl,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Poster URL must not exceed 2048 characters");
        }

        @Test
        @DisplayName("Rejects null format")
        void rejectsNullFormat() {
            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    null, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Format must not be null");
        }

        @Test
        @DisplayName("Rejects null production style")
        void rejectsNullProductionStyle() {
            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, null, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Production style must not be null");
        }

        @Test
        @DisplayName("Rejects non-existent director person ID")
        void rejectsNonExistentDirector() {
            when(personOperations.find(999L)).thenReturn(Optional.empty());

            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    999L, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Director person with ID 999 does not exist");
        }

        @Test
        @DisplayName("Rejects invalid nationality country code")
        void rejectsInvalidNationality() {
            when(referenceCatalog.country("ZZ")).thenReturn(Optional.empty());

            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, "ZZ", null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Nationality code 'ZZ' does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects negative total episodes")
        void rejectsNegativeTotalEpisodes() {
            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, -1,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Total episodes must be greater than or equal to 0");
        }

        @Test
        @DisplayName("Rejects current progress text exceeding 255 characters")
        void rejectsCurrentProgressTextExceeding255() {
            String longText = "a".repeat(256);
            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, 10,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, longText, null
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Current progress text must not exceed 255 characters");
        }

        @Test
        @DisplayName("Rejects non-existent genre ID in CreateFilmCommand")
        void rejectsNonExistentGenreInCreateCommand() {
            when(filmGenreRepository.existsById(888L)).thenReturn(false);

            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null,
                    Set.of(888L), Set.of(), Set.of()
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Film genre with ID 888 does not exist");
        }

        @Test
        @DisplayName("Rejects non-existent story archetype ID in CreateFilmCommand")
        void rejectsNonExistentArchetypeInCreateCommand() {
            when(referenceCatalog.storyArchetype(777L)).thenReturn(Optional.empty());

            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null,
                    Set.of(), Set.of(777L), Set.of()
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Story archetype with ID 777 does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects non-existent world setting ID in CreateFilmCommand")
        void rejectsNonExistentWorldSettingInCreateCommand() {
            when(referenceCatalog.worldSetting(666L)).thenReturn(Optional.empty());

            CreateFilmCommand command = new CreateFilmCommand(
                    "Valid Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null,
                    Set.of(), Set.of(), Set.of(666L)
            );
            assertThatThrownBy(() -> filmService.create(command))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("World setting with ID 666 does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects null UpdateFilmCommand or null id")
        void rejectsNullUpdateCommand() {
            assertThatThrownBy(() -> filmService.update(null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("UpdateFilmCommand must not be null");

            UpdateFilmCommand nullIdCmd = new UpdateFilmCommand(
                    null, "Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.update(nullIdCmd))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("Film id must not be null");
        }

        @Test
        @DisplayName("Rejects update for non-existent film")
        void rejectsUpdateNonExistentFilm() {
            when(filmRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateFilmCommand cmd = new UpdateFilmCommand(
                    999L, "Title", null, null, null,
                    FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                    null, null, null,
                    ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
            );
            assertThatThrownBy(() -> filmService.update(cmd))
                    .isInstanceOf(FilmNotFoundException.class)
                    .hasMessageContaining("Film with ID 999 not found");
        }

        @Test
        @DisplayName("Rejects classification assignments on non-existent film")
        void rejectsClassificationOnNonExistentFilm() {
            when(filmRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> filmService.addGenre(999L, 1L))
                    .isInstanceOf(FilmNotFoundException.class);
            assertThatThrownBy(() -> filmService.getGenres(999L))
                    .isInstanceOf(FilmNotFoundException.class);
            assertThatThrownBy(() -> filmService.addStoryArchetype(999L, 1L))
                    .isInstanceOf(FilmNotFoundException.class);
            assertThatThrownBy(() -> filmService.getStoryArchetypes(999L))
                    .isInstanceOf(FilmNotFoundException.class);
            assertThatThrownBy(() -> filmService.addWorldSetting(999L, 1L))
                    .isInstanceOf(FilmNotFoundException.class);
            assertThatThrownBy(() -> filmService.getWorldSettings(999L))
                    .isInstanceOf(FilmNotFoundException.class);
            assertThatThrownBy(() -> filmService.getClassifications(999L))
                    .isInstanceOf(FilmNotFoundException.class);
        }

        @Test
        @DisplayName("Rejects null arguments on classification operations")
        void rejectsNullArgumentsOnClassifications() {
            assertThatThrownBy(() -> filmService.addGenre(null, 1L))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");
            assertThatThrownBy(() -> filmService.addGenre(1L, null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("genreId must not be null");
            assertThatThrownBy(() -> filmService.getGenres(null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");

            assertThatThrownBy(() -> filmService.addStoryArchetype(null, 1L))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");
            assertThatThrownBy(() -> filmService.addStoryArchetype(1L, null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("storyArchetypeId must not be null");
            assertThatThrownBy(() -> filmService.getStoryArchetypes(null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");

            assertThatThrownBy(() -> filmService.addWorldSetting(null, 1L))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");
            assertThatThrownBy(() -> filmService.addWorldSetting(1L, null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("worldSettingId must not be null");
            assertThatThrownBy(() -> filmService.getWorldSettings(null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");

            assertThatThrownBy(() -> filmService.getClassifications(null))
                    .isInstanceOf(InvalidFilmException.class)
                    .hasMessageContaining("filmId must not be null");
        }
    }

    @Nested
    @DisplayName("Film Genre Validation Tests")
    class FilmGenreValidationTests {

        @Test
        @DisplayName("Rejects null CreateFilmGenreCommand")
        void rejectsNullGenreCommand() {
            assertThatThrownBy(() -> filmGenreService.create(null))
                    .isInstanceOf(InvalidFilmGenreException.class)
                    .hasMessageContaining("CreateFilmGenreCommand must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank genre name on create")
        void rejectsBlankGenreName(String name) {
            assertThatThrownBy(() -> filmGenreService.create(new CreateFilmGenreCommand(name, "desc")))
                    .isInstanceOf(InvalidFilmGenreException.class)
                    .hasMessageContaining("Genre name must not be blank");
        }

        @Test
        @DisplayName("Rejects genre name exceeding 150 characters")
        void rejectsLongGenreName() {
            String longName = "g".repeat(151);
            assertThatThrownBy(() -> filmGenreService.create(new CreateFilmGenreCommand(longName, "desc")))
                    .isInstanceOf(InvalidFilmGenreException.class)
                    .hasMessageContaining("Genre name must not exceed 150 characters");
        }

        @Test
        @DisplayName("Rejects duplicate genre name on create (case-insensitive check)")
        void rejectsDuplicateGenreNameOnCreate() {
            when(filmGenreRepository.existsByNameIgnoreCase("Sci-Fi")).thenReturn(true);

            assertThatThrownBy(() -> filmGenreService.create(new CreateFilmGenreCommand("Sci-Fi", "Science fiction")))
                    .isInstanceOf(FilmGenreNameAlreadyExistsException.class)
                    .hasMessageContaining("Sci-Fi");
        }

        @Test
        @DisplayName("Rejects null UpdateFilmGenreCommand or null id")
        void rejectsNullUpdateGenreCommand() {
            assertThatThrownBy(() -> filmGenreService.update(null))
                    .isInstanceOf(InvalidFilmGenreException.class)
                    .hasMessageContaining("UpdateFilmGenreCommand must not be null");

            assertThatThrownBy(() -> filmGenreService.update(new UpdateFilmGenreCommand(null, "Name", null)))
                    .isInstanceOf(InvalidFilmGenreException.class)
                    .hasMessageContaining("Genre id must not be null");
        }

        @Test
        @DisplayName("Rejects update for non-existent genre")
        void rejectsUpdateNonExistentGenre() {
            when(filmGenreRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> filmGenreService.update(new UpdateFilmGenreCommand(999L, "Name", null)))
                    .isInstanceOf(FilmGenreNotFoundException.class)
                    .hasMessageContaining("Film genre with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Film Link Validation Tests")
    class FilmLinkValidationTests {

        @Test
        @DisplayName("Rejects null CreateFilmLinkCommand")
        void rejectsNullLinkCommand() {
            assertThatThrownBy(() -> filmLinkService.create(null))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("CreateFilmLinkCommand must not be null");
        }

        @Test
        @DisplayName("Rejects create link for non-existent film")
        void rejectsCreateLinkNonExistentFilm() {
            when(filmRepository.existsById(999L)).thenReturn(false);

            CreateFilmLinkCommand command = new CreateFilmLinkCommand(
                    999L, "en", "Official", "https://example.com", false
            );
            assertThatThrownBy(() -> filmLinkService.create(command))
                    .isInstanceOf(FilmNotFoundException.class)
                    .hasMessageContaining("Film with ID 999 not found");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank or whitespace URL on create link")
        void rejectsBlankUrlOnCreate(String url) {
            when(filmRepository.existsById(1L)).thenReturn(true);

            CreateFilmLinkCommand command = new CreateFilmLinkCommand(
                    1L, "en", "Official", url, false
            );
            assertThatThrownBy(() -> filmLinkService.create(command))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("Link URL must not be blank");
        }

        @Test
        @DisplayName("Rejects URL exceeding 2048 characters")
        void rejectsUrlExceeding2048() {
            when(filmRepository.existsById(1L)).thenReturn(true);
            String longUrl = "https://example.com/" + "a".repeat(2040);

            CreateFilmLinkCommand command = new CreateFilmLinkCommand(
                    1L, "en", "Official", longUrl, false
            );
            assertThatThrownBy(() -> filmLinkService.create(command))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("Link URL must not exceed 2048 characters");
        }

        @Test
        @DisplayName("Rejects label exceeding 255 characters")
        void rejectsLabelExceeding255() {
            when(filmRepository.existsById(1L)).thenReturn(true);
            String longLabel = "l".repeat(256);

            CreateFilmLinkCommand command = new CreateFilmLinkCommand(
                    1L, "en", longLabel, "https://example.com", false
            );
            assertThatThrownBy(() -> filmLinkService.create(command))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("Link label must not exceed 255 characters");
        }

        @Test
        @DisplayName("Rejects invalid language code on link create")
        void rejectsInvalidLanguageCodeOnCreate() {
            when(filmRepository.existsById(1L)).thenReturn(true);
            when(referenceCatalog.language("zz")).thenReturn(Optional.empty());

            CreateFilmLinkCommand command = new CreateFilmLinkCommand(
                    1L, "zz", "Official", "https://example.com", false
            );
            assertThatThrownBy(() -> filmLinkService.create(command))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("Language code 'zz' does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects null UpdateFilmLinkCommand or null id/filmId")
        void rejectsNullUpdateLinkCommand() {
            assertThatThrownBy(() -> filmLinkService.update(null))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("UpdateFilmLinkCommand must not be null");

            assertThatThrownBy(() -> filmLinkService.update(new UpdateFilmLinkCommand(null, 1L, null, null, "https://example.com", false)))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("Film link id must not be null");

            assertThatThrownBy(() -> filmLinkService.update(new UpdateFilmLinkCommand(1L, null, null, null, "https://example.com", false)))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("Film id must not be null");
        }

        @Test
        @DisplayName("Rejects findByFilmId with null or non-existent filmId")
        void rejectsFindByFilmIdInvalid() {
            assertThatThrownBy(() -> filmLinkService.findByFilmId(null))
                    .isInstanceOf(InvalidFilmLinkException.class)
                    .hasMessageContaining("filmId must not be null");

            when(filmRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> filmLinkService.findByFilmId(999L))
                    .isInstanceOf(FilmNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Film Credit Validation Tests")
    class FilmCreditValidationTests {

        @Test
        @DisplayName("Rejects null CreateFilmCreditCommand")
        void rejectsNullCreditCommand() {
            assertThatThrownBy(() -> filmCreditService.create(null))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("CreateFilmCreditCommand must not be null");
        }

        @Test
        @DisplayName("Rejects null or non-existent filmId on create credit")
        void rejectsNonExistentFilmOnCredit() {
            assertThatThrownBy(() -> filmCreditService.create(new CreateFilmCreditCommand(null, 1L, FilmCreditRole.MAIN, "Hero", null)))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("filmId must not be null");

            when(filmRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> filmCreditService.create(new CreateFilmCreditCommand(999L, 1L, FilmCreditRole.MAIN, "Hero", null)))
                    .isInstanceOf(FilmNotFoundException.class)
                    .hasMessageContaining("Film with ID 999 not found");
        }

        @Test
        @DisplayName("Rejects null or non-existent personId on create credit")
        void rejectsNonExistentPersonOnCredit() {
            when(filmRepository.existsById(1L)).thenReturn(true);

            assertThatThrownBy(() -> filmCreditService.create(new CreateFilmCreditCommand(1L, null, FilmCreditRole.MAIN, "Hero", null)))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("personId must not be null");

            when(personOperations.find(888L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> filmCreditService.create(new CreateFilmCreditCommand(1L, 888L, FilmCreditRole.MAIN, "Hero", null)))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("Person with ID 888 does not exist");
        }

        @Test
        @DisplayName("Rejects null role on create credit")
        void rejectsNullRoleOnCredit() {
            when(filmRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(2L)).thenReturn(Optional.of(mock(PersonView.class)));

            assertThatThrownBy(() -> filmCreditService.create(new CreateFilmCreditCommand(1L, 2L, null, "Hero", null)))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("Role must not be null");
        }

        @Test
        @DisplayName("Rejects character name exceeding 255 characters")
        void rejectsCharacterNameExceeding255() {
            when(filmRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(2L)).thenReturn(Optional.of(mock(PersonView.class)));
            String longName = "c".repeat(256);

            assertThatThrownBy(() -> filmCreditService.create(new CreateFilmCreditCommand(1L, 2L, FilmCreditRole.MAIN, longName, null)))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("Character name must not exceed 255 characters");
        }

        @Test
        @DisplayName("Rejects findByFilmId with null or non-existent filmId on credits")
        void rejectsFindByFilmIdInvalidOnCredits() {
            assertThatThrownBy(() -> filmCreditService.findByFilmId(null))
                    .isInstanceOf(InvalidFilmCreditException.class)
                    .hasMessageContaining("filmId must not be null");

            when(filmRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> filmCreditService.findByFilmId(999L))
                    .isInstanceOf(FilmNotFoundException.class);
        }
    }
}
