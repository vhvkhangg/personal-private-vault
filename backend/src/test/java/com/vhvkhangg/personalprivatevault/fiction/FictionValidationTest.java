package com.vhvkhangg.personalprivatevault.fiction;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.fiction.exception.InvalidFictionException;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.UpdateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.genre.exception.InvalidFictionGenreException;
import com.vhvkhangg.personalprivatevault.fiction.internal.application.FictionGenreService;
import com.vhvkhangg.personalprivatevault.fiction.internal.application.FictionLinkService;
import com.vhvkhangg.personalprivatevault.fiction.internal.application.FictionService;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionGenreRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionLinkRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionStoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence.FictionWorldSettingRepository;
import com.vhvkhangg.personalprivatevault.fiction.link.command.CreateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.command.UpdateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.FictionLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.InvalidFictionLinkException;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
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

class FictionValidationTest {

    private FictionRepository fictionRepository;
    private FictionGenreRepository fictionGenreRepository;
    private FictionStoryArchetypeRepository storyArchetypeRepository;
    private FictionWorldSettingRepository worldSettingRepository;
    private VaultEntryOperations vaultEntryOperations;
    private PersonOperations personOperations;
    private CreatorGroupOperations creatorGroupOperations;
    private ReferenceCatalog referenceCatalog;
    private FictionLinkRepository fictionLinkRepository;

    private FictionService fictionService;
    private FictionGenreService fictionGenreService;
    private FictionLinkService fictionLinkService;

    @BeforeEach
    void setUp() {
        fictionRepository = mock(FictionRepository.class);
        fictionGenreRepository = mock(FictionGenreRepository.class);
        storyArchetypeRepository = mock(FictionStoryArchetypeRepository.class);
        worldSettingRepository = mock(FictionWorldSettingRepository.class);
        vaultEntryOperations = mock(VaultEntryOperations.class);
        personOperations = mock(PersonOperations.class);
        creatorGroupOperations = mock(CreatorGroupOperations.class);
        referenceCatalog = mock(ReferenceCatalog.class);
        fictionLinkRepository = mock(FictionLinkRepository.class);

        fictionService = new FictionService(
                fictionRepository,
                fictionGenreRepository,
                storyArchetypeRepository,
                worldSettingRepository,
                vaultEntryOperations,
                personOperations,
                creatorGroupOperations,
                referenceCatalog
        );

        fictionGenreService = new FictionGenreService(fictionGenreRepository);
        fictionLinkService = new FictionLinkService(
                fictionLinkRepository,
                fictionRepository,
                referenceCatalog
        );
    }

    @Nested
    @DisplayName("Fiction Creation Validation")
    class CreateFictionValidation {

        @Test
        @DisplayName("Rejects null create fiction command")
        void rejectsNullCommand() {
            assertThatThrownBy(() -> fictionService.create(null))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank fiction title")
        void rejectsBlankTitle(String title) {
            CreateFictionCommand command = new CreateFictionCommand(
                    title, null, null, null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Fiction title must not be blank");
        }

        @Test
        @DisplayName("Rejects fiction title exceeding 500 characters")
        void rejectsOverlyLongTitle() {
            String longTitle = "T".repeat(501);
            CreateFictionCommand command = new CreateFictionCommand(
                    longTitle, null, null, null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("exceed 500 characters");
        }

        @Test
        @DisplayName("Rejects null fiction format")
        void rejectsNullFormat() {
            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, null, false, 1L, 10L, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("format must not be null");
        }

        @Test
        @DisplayName("Rejects null genre ID")
        void rejectsNullGenreId() {
            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, null, 10L, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Fiction genreId must not be null");
        }

        @Test
        @DisplayName("Rejects non-existent genre ID")
        void rejectsNonExistentGenreId() {
            when(fictionGenreRepository.existsById(99L)).thenReturn(false);
            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 99L, 10L, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Fiction genre with ID 99 does not exist");
        }

        @Test
        @DisplayName("Rejects missing author source (both null)")
        void rejectsMissingAuthorSource() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, null, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Exactly one author source must be provided: authorPersonId XOR authorGroupId");
        }

        @Test
        @DisplayName("Rejects dual author source (both non-null)")
        void rejectsDualAuthorSource() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, 10L, 20L,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Exactly one author source must be provided: authorPersonId XOR authorGroupId");
        }

        @Test
        @DisplayName("Rejects non-existent author person")
        void rejectsNonExistentAuthorPerson() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(999L)).thenReturn(Optional.empty());

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, 999L, null,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Author person with ID 999 does not exist");
        }

        @Test
        @DisplayName("Rejects non-existent author creator group")
        void rejectsNonExistentAuthorGroup() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(creatorGroupOperations.find(888L)).thenReturn(Optional.empty());

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, null, 888L,
                    null, null, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Author creator group with ID 888 does not exist");
        }

        @Test
        @DisplayName("Rejects negative chapter count")
        void rejectsNegativeChapters() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(10L)).thenReturn(Optional.of(mock(com.vhvkhangg.personalprivatevault.people.view.PersonView.class)));

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, -1, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Total chapters must be greater than or equal to 0: -1");
        }

        @Test
        @DisplayName("Rejects invalid nationality code length")
        void rejectsInvalidNationalityLength() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(10L)).thenReturn(Optional.of(mock(com.vhvkhangg.personalprivatevault.people.view.PersonView.class)));

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, "USA", null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, 10, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Nationality code must be a 2-character ISO country code");
        }

        @Test
        @DisplayName("Rejects non-existent nationality code")
        void rejectsNonExistentNationalityCode() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(10L)).thenReturn(Optional.of(mock(com.vhvkhangg.personalprivatevault.people.view.PersonView.class)));
            when(referenceCatalog.country("ZZ")).thenReturn(Optional.empty());

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, "ZZ", null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, 10, null, null, null, null
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Nationality code 'ZZ' does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects non-existent initial story archetype ID")
        void rejectsNonExistentInitialStoryArchetype() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(10L)).thenReturn(Optional.of(mock(com.vhvkhangg.personalprivatevault.people.view.PersonView.class)));
            when(referenceCatalog.storyArchetype(101L)).thenReturn(Optional.empty());

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, 10, null, null, null, null, Set.of(101L), Set.of()
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Story archetype with ID 101 does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects non-existent initial world setting ID")
        void rejectsNonExistentInitialWorldSetting() {
            when(fictionGenreRepository.existsById(1L)).thenReturn(true);
            when(personOperations.find(10L)).thenReturn(Optional.of(mock(com.vhvkhangg.personalprivatevault.people.view.PersonView.class)));
            when(referenceCatalog.worldSetting(202L)).thenReturn(Optional.empty());

            CreateFictionCommand command = new CreateFictionCommand(
                    "Valid Title", null, null, null, FictionFormat.NOVEL, false, 1L, 10L, null,
                    null, 10, null, null, null, null, Set.of(), Set.of(202L)
            );
            assertThatThrownBy(() -> fictionService.create(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("World setting with ID 202 does not exist in reference catalog");
        }
    }

    @Nested
    @DisplayName("Fiction Update Validation")
    class UpdateFictionValidation {

        @Test
        @DisplayName("Rejects null update command")
        void rejectsNullCommand() {
            assertThatThrownBy(() -> fictionService.update(null))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("UpdateFictionCommand must not be null");
        }

        @Test
        @DisplayName("Rejects update command with null id")
        void rejectsNullId() {
            UpdateFictionCommand command = new UpdateFictionCommand(
                    null, "Updated Title", null, null, null, FictionFormat.NOVEL, false, 1L, 10L,
                    null, null, 10, ProgressStatus.ONGOING, ConsumptionStatus.BEING_CONSUMED, null, null
            );
            assertThatThrownBy(() -> fictionService.update(command))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Fiction id must not be null");
        }

        @Test
        @DisplayName("Throws FictionNotFoundException when updating non-existent fiction")
        void throwsNotFoundOnNonExistentFiction() {
            when(fictionRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateFictionCommand command = new UpdateFictionCommand(
                    999L, "Updated Title", null, null, null, FictionFormat.NOVEL, false, 1L, 10L,
                    null, null, 10, ProgressStatus.ONGOING, ConsumptionStatus.BEING_CONSUMED, null, null
            );
            assertThatThrownBy(() -> fictionService.update(command))
                    .isInstanceOf(FictionNotFoundException.class)
                    .hasMessageContaining("Fiction with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Classification Assignment Validation")
    class ClassificationValidation {

        @Test
        @DisplayName("Rejects addStoryArchetype on missing fiction")
        void rejectsAddStoryArchetypeOnMissingFiction() {
            when(fictionRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> fictionService.addStoryArchetype(999L, 1L))
                    .isInstanceOf(FictionNotFoundException.class);
        }

        @Test
        @DisplayName("Rejects addStoryArchetype with missing reference ID")
        void rejectsAddStoryArchetypeWithMissingReference() {
            when(fictionRepository.existsById(1L)).thenReturn(true);
            when(referenceCatalog.storyArchetype(555L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fictionService.addStoryArchetype(1L, 555L))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("Story archetype with ID 555 does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects addWorldSetting on missing fiction")
        void rejectsAddWorldSettingOnMissingFiction() {
            when(fictionRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> fictionService.addWorldSetting(999L, 1L))
                    .isInstanceOf(FictionNotFoundException.class);
        }

        @Test
        @DisplayName("Rejects addWorldSetting with missing reference ID")
        void rejectsAddWorldSettingWithMissingReference() {
            when(fictionRepository.existsById(1L)).thenReturn(true);
            when(referenceCatalog.worldSetting(777L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fictionService.addWorldSetting(1L, 777L))
                    .isInstanceOf(InvalidFictionException.class)
                    .hasMessageContaining("World setting with ID 777 does not exist in reference catalog");
        }
    }

    @Nested
    @DisplayName("Fiction Genre Validation")
    class GenreValidation {

        @Test
        @DisplayName("Rejects null genre create command")
        void rejectsNullCommand() {
            assertThatThrownBy(() -> fictionGenreService.create(null))
                    .isInstanceOf(InvalidFictionGenreException.class)
                    .hasMessageContaining("CreateFictionGenreCommand must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank genre name")
        void rejectsBlankGenreName(String name) {
            CreateFictionGenreCommand command = new CreateFictionGenreCommand(name, "Desc");
            assertThatThrownBy(() -> fictionGenreService.create(command))
                    .isInstanceOf(InvalidFictionGenreException.class)
                    .hasMessageContaining("Genre name must not be blank");
        }

        @Test
        @DisplayName("Rejects genre name exceeding 150 characters")
        void rejectsOverlyLongGenreName() {
            String longName = "G".repeat(151);
            CreateFictionGenreCommand command = new CreateFictionGenreCommand(longName, "Desc");
            assertThatThrownBy(() -> fictionGenreService.create(command))
                    .isInstanceOf(InvalidFictionGenreException.class)
                    .hasMessageContaining("Genre name must not exceed 150 characters");
        }

        @Test
        @DisplayName("Rejects duplicate genre name on create (pre-check)")
        void rejectsDuplicateGenreNameOnCreate() {
            when(fictionGenreRepository.existsByNameIgnoreCase("Fantasy")).thenReturn(true);
            CreateFictionGenreCommand command = new CreateFictionGenreCommand("Fantasy", "Desc");
            assertThatThrownBy(() -> fictionGenreService.create(command))
                    .isInstanceOf(FictionGenreNameAlreadyExistsException.class)
                    .hasMessageContaining("Fantasy");
        }

        @Test
        @DisplayName("Rejects update genre with null ID")
        void rejectsUpdateWithNullId() {
            UpdateFictionGenreCommand command = new UpdateFictionGenreCommand(null, "Fantasy", "Desc");
            assertThatThrownBy(() -> fictionGenreService.update(command))
                    .isInstanceOf(InvalidFictionGenreException.class)
                    .hasMessageContaining("Genre id must not be null");
        }

        @Test
        @DisplayName("Throws FictionGenreNotFoundException on missing genre update")
        void throwsNotFoundOnMissingGenreUpdate() {
            when(fictionGenreRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateFictionGenreCommand command = new UpdateFictionGenreCommand(999L, "Fantasy", "Desc");
            assertThatThrownBy(() -> fictionGenreService.update(command))
                    .isInstanceOf(FictionGenreNotFoundException.class)
                    .hasMessageContaining("Fiction genre with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Fiction Link Validation")
    class LinkValidation {

        @Test
        @DisplayName("Rejects null link create command")
        void rejectsNullCommand() {
            assertThatThrownBy(() -> fictionLinkService.create(null))
                    .isInstanceOf(InvalidFictionLinkException.class)
                    .hasMessageContaining("CreateFictionLinkCommand must not be null");
        }

        @Test
        @DisplayName("Rejects link creation on missing fiction")
        void rejectsLinkOnMissingFiction() {
            when(fictionRepository.existsById(999L)).thenReturn(false);
            CreateFictionLinkCommand command = new CreateFictionLinkCommand(
                    999L, "en", "TRANSLATION", "Eng link", "https://example.com", false
            );
            assertThatThrownBy(() -> fictionLinkService.create(command))
                    .isInstanceOf(FictionNotFoundException.class)
                    .hasMessageContaining("Fiction with ID 999 not found");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Rejects blank link type")
        void rejectsBlankLinkType(String type) {
            when(fictionRepository.existsById(1L)).thenReturn(true);
            CreateFictionLinkCommand command = new CreateFictionLinkCommand(
                    1L, null, type, "Label", "https://example.com", false
            );
            assertThatThrownBy(() -> fictionLinkService.create(command))
                    .isInstanceOf(InvalidFictionLinkException.class)
                    .hasMessageContaining("linkType must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Rejects blank url")
        void rejectsBlankUrl(String url) {
            when(fictionRepository.existsById(1L)).thenReturn(true);
            CreateFictionLinkCommand command = new CreateFictionLinkCommand(
                    1L, null, "ORIGINAL", "Label", url, false
            );
            assertThatThrownBy(() -> fictionLinkService.create(command))
                    .isInstanceOf(InvalidFictionLinkException.class)
                    .hasMessageContaining("url must not be blank");
        }

        @Test
        @DisplayName("Rejects invalid language code on link")
        void rejectsInvalidLanguageCode() {
            when(fictionRepository.existsById(1L)).thenReturn(true);
            when(referenceCatalog.language("klingon")).thenReturn(Optional.empty());

            CreateFictionLinkCommand command = new CreateFictionLinkCommand(
                    1L, "klingon", "TRANSLATION", "Label", "https://example.com", false
            );
            assertThatThrownBy(() -> fictionLinkService.create(command))
                    .isInstanceOf(InvalidFictionLinkException.class)
                    .hasMessageContaining("Language code 'klingon' does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects update when link does not belong to specified fiction")
        void rejectsUpdateOnMismatchedParentFiction() {
            when(fictionRepository.existsById(10L)).thenReturn(true);
            var mockLink = mock(com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionLink.class);
            when(mockLink.getFictionId()).thenReturn(99L);
            when(mockLink.getId()).thenReturn(1L);
            when(fictionLinkRepository.findById(1L)).thenReturn(Optional.of(mockLink));

            UpdateFictionLinkCommand command = new UpdateFictionLinkCommand(
                    1L, 10L, null, "ORIGINAL", null, "https://example.com", false
            );
            assertThatThrownBy(() -> fictionLinkService.update(command))
                    .isInstanceOf(FictionLinkNotFoundException.class)
                    .hasMessageContaining("does not belong to fiction 10");
        }
    }
}
