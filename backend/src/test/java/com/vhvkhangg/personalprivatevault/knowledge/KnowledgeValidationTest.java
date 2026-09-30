package com.vhvkhangg.personalprivatevault.knowledge;

import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeSrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.api.UpdateKnowledgeVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.enums.InformationType;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.CreateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InvalidInformationItemException;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.UpdateInformationItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.information.internal.application.InformationItemService;
import com.vhvkhangg.personalprivatevault.knowledge.information.internal.domain.InformationItem;
import com.vhvkhangg.personalprivatevault.knowledge.information.internal.infrastructure.persistence.InformationItemRepository;
import com.vhvkhangg.personalprivatevault.knowledge.internal.application.KnowledgeFacadeService;
import com.vhvkhangg.personalprivatevault.knowledge.note.internal.application.NoteService;
import com.vhvkhangg.personalprivatevault.knowledge.note.internal.domain.Note;
import com.vhvkhangg.personalprivatevault.knowledge.note.internal.infrastructure.persistence.NoteRepository;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.CreateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.InvalidNoteException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteOperations;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.UpdateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.study.enums.StudyType;
import com.vhvkhangg.personalprivatevault.knowledge.study.internal.application.StudyItemService;
import com.vhvkhangg.personalprivatevault.knowledge.study.internal.domain.StudyItem;
import com.vhvkhangg.personalprivatevault.knowledge.study.internal.infrastructure.persistence.StudyItemRepository;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.CreateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.InvalidStudyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyConflictException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.UpdateStudyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.SrsReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.enums.VocabularyLearningStatus;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.application.VocabularyService;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.domain.VocabularyItem;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.infrastructure.persistence.VocabularyItemRepository;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.infrastructure.persistence.VocabularyReviewRepository;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyItemView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.view.VocabularyReviewView;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.CreateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.InvalidVocabularyItemException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.UpdateVocabularyItemCommand;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyItemNotFoundException;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyOperations;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.vocabulary.VocabularyReviewTransitionCommand;
import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.enums.PlatformKind;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.reference.view.LanguageView;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeValidationTest {

    @Nested
    @DisplayName("Study validation tests")
    class StudyValidationTests {

        private StudyItemRepository studyRepository;
        private VaultEntryOperations vaultEntryOperations;
        private PersonOperations personOperations;
        private CreatorGroupOperations creatorGroupOperations;
        private ReferenceCatalog referenceCatalog;
        private ExternalAccountOperations externalAccountOperations;
        private StudyItemService studyItemService;

        @BeforeEach
        void setUp() {
            studyRepository = mock(StudyItemRepository.class);
            vaultEntryOperations = mock(VaultEntryOperations.class);
            personOperations = mock(PersonOperations.class);
            creatorGroupOperations = mock(CreatorGroupOperations.class);
            referenceCatalog = mock(ReferenceCatalog.class);
            externalAccountOperations = mock(ExternalAccountOperations.class);

            studyItemService = new StudyItemService(
                    studyRepository,
                    vaultEntryOperations,
                    personOperations,
                    creatorGroupOperations,
                    referenceCatalog,
                    externalAccountOperations
            );

            when(vaultEntryOperations.create(VaultEntryType.STUDY))
                    .thenReturn(new VaultEntryView(100L, VaultEntryType.STUDY, Instant.now(), Instant.now(), null));
        }

        @Test
        @DisplayName("Rejects null create command")
        void rejectsNullCreateCommand() {
            assertThatThrownBy(() -> studyItemService.create(null))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("Rejects blank title and title exceeding 500 characters")
        void rejectsInvalidTitle() {
            var blankTitle = new CreateStudyItemCommand("   ", null, StudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(blankTitle))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("title must not be blank");

            var longTitle = new CreateStudyItemCommand("a".repeat(501), null, StudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(longTitle))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("must not exceed 500");
        }

        @Test
        @DisplayName("Rejects null study type")
        void rejectsNullType() {
            var nullType = new CreateStudyItemCommand("Java Concurrency", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(nullType))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("type must not be null");
        }

        @Test
        @DisplayName("Rejects negative progress percent or exceeding 100")
        void rejectsInvalidProgressPercent() {
            var negProgress = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, new BigDecimal("-1"), null);
            assertThatThrownBy(() -> studyItemService.create(negProgress))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Progress percent must be between 0 and 100");

            var highProgress = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, new BigDecimal("100.5"), null);
            assertThatThrownBy(() -> studyItemService.create(highProgress))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Progress percent must be between 0 and 100");
        }

        @Test
        @DisplayName("Rejects negative price amount")
        void rejectsNegativePrice() {
            var negPrice = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, new BigDecimal("-5.00"), "USD", null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(negPrice))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Price amount must be nonnegative");
        }

        @Test
        @DisplayName("Rejects price without currency, but allows currency without price")
        void rejectsPriceWithoutCurrency() {
            var priceNoCurrency = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, new BigDecimal("19.99"), null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(priceNoCurrency))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Currency code is required when price is present");

            when(referenceCatalog.currency("USD")).thenReturn(Optional.of(new CurrencyView("USD", "US Dollar", "$", 2)));
            var currencyNoPrice = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, null, "USD", null, null, null, null, null, null);
            var result = studyItemService.create(currencyNoPrice);
            assertThat(result.currencyCode()).isEqualTo("USD");
            assertThat(result.priceAmount()).isNull();
        }

        @Test
        @DisplayName("Rejects non-existent currency code")
        void rejectsNonExistentCurrency() {
            when(referenceCatalog.currency("XYZ")).thenReturn(Optional.empty());
            var invalidCurrency = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, null, "XYZ", null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(invalidCurrency))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("does not exist in reference catalog");
        }

        @Test
        @DisplayName("Disallows both Person and Group authors, but allows zero authors")
        void enforcesAuthorInvariants() {
            var bothAuthors = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, 1L, 2L, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(bothAuthors))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("cannot have both Person and Creator Group");

            when(personOperations.find(99L)).thenReturn(Optional.empty());
            var nonExistentPerson = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, 99L, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(nonExistentPerson))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Author person with ID 99 does not exist");

            when(creatorGroupOperations.find(88L)).thenReturn(Optional.empty());
            var nonExistentGroup = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, 88L, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(nonExistentGroup))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Author creator group with ID 88 does not exist");

            var zeroAuthors = new CreateStudyItemCommand("Java", null, StudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null);
            var result = studyItemService.create(zeroAuthors);
            assertThat(result.authorPersonId()).isNull();
            assertThat(result.authorGroupId()).isNull();
        }

        @Test
        @DisplayName("Enforces WEBSITE invariants: site_domain and url required, domain normalized to lower-case")
        void enforcesWebsiteInvariants() {
            var websiteNoDomain = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, null, null, null, null, null, null, null, null, "https://spring.io", null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(websiteNoDomain))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Site domain is required for WEBSITE");

            var websiteNoUrl = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, "spring.io", null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(websiteNoUrl))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("URL is required for WEBSITE");

            var websiteWithAccount = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, "spring.io", 10L, null, null, null, null, null, null, "https://spring.io", null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(websiteWithAccount))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("YouTube channel account must be null for WEBSITE");

            var validWebsite = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, "  SPRING.IO  ", null, null, null, null, null, null, null, "https://spring.io", null, null, null, null);
            var result = studyItemService.create(validWebsite);
            assertThat(result.siteDomain()).isEqualTo("spring.io");
        }

        @Test
        @DisplayName("Enforces WEBSITE site_domain hostname validation: rejects scheme, path, port, and invalid boundaries; accepts valid hostnames")
        void enforcesWebsiteHostnameValidation() {
            List<String> invalidDomains = List.of(
                    "https://example.com/course",
                    "http://example.com",
                    "ftp://example.com",
                    "example.com/path",
                    "example.com/",
                    "example.com:8080",
                    "example.com?query=1",
                    "example.com#section",
                    "example..com",
                    ".example.com",
                    "example.com.",
                    "-example.com",
                    "example-.com",
                    "user@example.com",
                    "exa_mple.com"
            );

            for (String invalidDomain : invalidDomains) {
                var cmd = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, invalidDomain, null, null, null, null, null, null, null, "https://example.com", null, null, null, null);
                assertThatThrownBy(() -> studyItemService.create(cmd))
                        .isInstanceOf(InvalidStudyItemException.class)
                        .hasMessageContaining("Site domain must be a valid hostname");
            }

            var longDomain = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, "a".repeat(256), null, null, null, null, null, null, null, "https://example.com", null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(longDomain))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("must not exceed 255 characters");

            List<String> validDomains = List.of(
                    "spring.io",
                    "docs.spring.io",
                    "sub-domain.example.co.uk",
                    "localhost",
                    "127.0.0.1",
                    "a.com"
            );

            for (String validDomain : validDomains) {
                var cmd = new CreateStudyItemCommand("Spring Guides", null, StudyType.WEBSITE, validDomain, null, null, null, null, null, null, null, "https://example.com", null, null, null, null);
                var created = studyItemService.create(cmd);
                assertThat(created.siteDomain()).isEqualTo(validDomain);
            }
        }

        @Test
        @DisplayName("Enforces YOUTUBE_CHANNEL invariants: account required, accountType YOUTUBE_CHANNEL, platform YouTube, uniqueness")
        void enforcesYouTubeChannelInvariants() {
            var ytNoAccount = new CreateStudyItemCommand("Fireship", null, StudyType.YOUTUBE_CHANNEL, null, null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(ytNoAccount))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("account ID is required for YOUTUBE_CHANNEL");

            var ytWithDomain = new CreateStudyItemCommand("Fireship", null, StudyType.YOUTUBE_CHANNEL, "youtube.com", 10L, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(ytWithDomain))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Site domain must be null for YOUTUBE_CHANNEL");

            when(externalAccountOperations.findById(10L)).thenReturn(Optional.empty());
            var nonExistentAccount = new CreateStudyItemCommand("Fireship", null, StudyType.YOUTUBE_CHANNEL, null, 10L, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(nonExistentAccount))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("does not exist");

            when(externalAccountOperations.findById(11L)).thenReturn(Optional.of(
                    new ExternalAccountView(11L, 1L, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL, "user", null, null, null, null, null, null, null, null)
            ));
            var wrongAccountType = new CreateStudyItemCommand("Fireship", null, StudyType.YOUTUBE_CHANNEL, null, 11L, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(wrongAccountType))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("must be of type YOUTUBE_CHANNEL");

            when(externalAccountOperations.findById(12L)).thenReturn(Optional.of(
                    new ExternalAccountView(12L, 2L, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL, "user", null, null, null, null, null, null, null, null)
            ));
            when(referenceCatalog.platform(2L)).thenReturn(Optional.of(
                    new PlatformView(2L, "Vimeo", PlatformKind.MEDIA, "https://vimeo.com", Instant.now())
            ));
            var nonYouTubePlatform = new CreateStudyItemCommand("Fireship", null, StudyType.YOUTUBE_CHANNEL, null, 12L, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(nonYouTubePlatform))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("must belong to the YouTube platform");

            when(externalAccountOperations.findById(13L)).thenReturn(Optional.of(
                    new ExternalAccountView(13L, 3L, ExternalAccountOwnership.TRACKED, ExternalAccountType.YOUTUBE_CHANNEL, "fireship", null, null, null, null, null, null, null, null)
            ));
            when(referenceCatalog.platform(3L)).thenReturn(Optional.of(
                    new PlatformView(3L, "  YouTube  ", PlatformKind.MEDIA, "https://youtube.com", Instant.now())
            ));
            var existingStudy = mock(StudyItem.class);
            when(existingStudy.getId()).thenReturn(999L);
            when(studyRepository.findByYoutubeChannelAccountId(13L)).thenReturn(Optional.of(existingStudy));
            var duplicateAccount = new CreateStudyItemCommand("Fireship", null, StudyType.YOUTUBE_CHANNEL, null, 13L, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(duplicateAccount))
                    .isInstanceOf(StudyConflictException.class)
                    .hasMessageContaining("already exists for the YouTube channel account");
        }

        @Test
        @DisplayName("Rejects site_domain or youtubeChannelAccountId on other types (BOOK, COURSE, GITHUB_REPOSITORY)")
        void enforcesNullDomainAndAccountOnOtherTypes() {
            var bookWithDomain = new CreateStudyItemCommand("Book", null, StudyType.BOOK, "domain.com", null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(bookWithDomain))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("Site domain must be null for BOOK");

            var repoWithAccount = new CreateStudyItemCommand("Repo", null, StudyType.GITHUB_REPOSITORY, null, 10L, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> studyItemService.create(repoWithAccount))
                    .isInstanceOf(InvalidStudyItemException.class)
                    .hasMessageContaining("YouTube channel account must be null for GITHUB_REPOSITORY");
        }
    }

    @Nested
    @DisplayName("Information validation tests")
    class InformationValidationTests {

        private InformationItemRepository informationRepository;
        private VaultEntryOperations vaultEntryOperations;
        private InformationItemService informationItemService;

        @BeforeEach
        void setUp() {
            informationRepository = mock(InformationItemRepository.class);
            vaultEntryOperations = mock(VaultEntryOperations.class);
            informationItemService = new InformationItemService(informationRepository, vaultEntryOperations);

            when(vaultEntryOperations.create(VaultEntryType.INFORMATION))
                    .thenReturn(new VaultEntryView(200L, VaultEntryType.INFORMATION, Instant.now(), Instant.now(), null));
        }

        @Test
        @DisplayName("Rejects null command and blank title")
        void rejectsInvalidCommandAndTitle() {
            assertThatThrownBy(() -> informationItemService.create(null))
                    .isInstanceOf(InvalidInformationItemException.class);

            var blankTitle = new CreateInformationItemCommand("  ", InformationType.TECHNOLOGY, null, null, null, null, null);
            assertThatThrownBy(() -> informationItemService.create(blankTitle))
                    .isInstanceOf(InvalidInformationItemException.class)
                    .hasMessageContaining("title must not be blank");
        }

        @Test
        @DisplayName("Rejects null information type")
        void rejectsNullType() {
            var nullType = new CreateInformationItemCommand("Title", null, null, null, null, null, null);
            assertThatThrownBy(() -> informationItemService.create(nullType))
                    .isInstanceOf(InvalidInformationItemException.class)
                    .hasMessageContaining("type must not be null");
        }

        @Test
        @DisplayName("Rejects sourceName exceeding 500 and sourceUrl exceeding 2048 chars")
        void rejectsOverlongMetadata() {
            var longSourceName = new CreateInformationItemCommand("Title", InformationType.FINANCE, null, null, null, "s".repeat(501), null);
            assertThatThrownBy(() -> informationItemService.create(longSourceName))
                    .isInstanceOf(InvalidInformationItemException.class)
                    .hasMessageContaining("Source name must not exceed 500");

            var longSourceUrl = new CreateInformationItemCommand("Title", InformationType.FINANCE, null, null, null, null, "https://".concat("u".repeat(2048)));
            assertThatThrownBy(() -> informationItemService.create(longSourceUrl))
                    .isInstanceOf(InvalidInformationItemException.class)
                    .hasMessageContaining("Source URL must not exceed 2048");
        }
    }

    @Nested
    @DisplayName("Vocabulary validation tests")
    class VocabularyValidationTests {

        private VocabularyItemRepository vocabularyRepository;
        private VocabularyReviewRepository vocabularyReviewRepository;
        private VaultEntryOperations vaultEntryOperations;
        private ReferenceCatalog referenceCatalog;
        private VocabularyService vocabularyService;

        @BeforeEach
        void setUp() {
            vocabularyRepository = mock(VocabularyItemRepository.class);
            vocabularyReviewRepository = mock(VocabularyReviewRepository.class);
            vaultEntryOperations = mock(VaultEntryOperations.class);
            referenceCatalog = mock(ReferenceCatalog.class);

            vocabularyService = new VocabularyService(
                    vocabularyRepository,
                    vocabularyReviewRepository,
                    vaultEntryOperations,
                    referenceCatalog
            );

            when(vaultEntryOperations.create(VaultEntryType.VOCABULARY))
                    .thenReturn(new VaultEntryView(300L, VaultEntryType.VOCABULARY, Instant.now(), Instant.now(), null));
            when(referenceCatalog.language("en"))
                    .thenReturn(Optional.of(new LanguageView("en", "English", "English")));
        }

        @Test
        @DisplayName("Rejects blank word and blank meaning")
        void rejectsBlankWordAndMeaning() {
            var blankWord = new CreateVocabularyItemCommand("  ", "en", "meaning", null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> vocabularyService.create(blankWord))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("word must not be blank");

            var blankMeaning = new CreateVocabularyItemCommand("word", "en", "   ", null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> vocabularyService.create(blankMeaning))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("meaning must not be blank");
        }

        @Test
        @DisplayName("Validates language code through reference catalog")
        void validatesLanguageCode() {
            when(referenceCatalog.language("xx")).thenReturn(Optional.empty());
            var invalidLang = new CreateVocabularyItemCommand("word", "xx", "meaning", null, null, null, null, null, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> vocabularyService.create(invalidLang))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects negative SRS intervals and non-positive ease factor")
        void rejectsInvalidSrsNumericValues() {
            when(referenceCatalog.language("en")).thenReturn(Optional.of(new LanguageView("en", "English", "Tiếng Anh")));

            var negInterval = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, -1, null, null, null);
            assertThatThrownBy(() -> vocabularyService.create(negInterval))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Interval days must be nonnegative");

            var zeroEase = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, BigDecimal.ZERO, null, null);
            assertThatThrownBy(() -> vocabularyService.create(zeroEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Ease factor must be greater than zero");

            var roundsToZeroEase = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("0.001"), null, null);
            assertThatThrownBy(() -> vocabularyService.create(roundsToZeroEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Ease factor must be greater than zero");

            var overflowEase = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("1000.00"), null, null);
            assertThatThrownBy(() -> vocabularyService.create(overflowEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Ease factor must not exceed 999.99");

            var roundOverflowEase = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("999.995"), null, null);
            assertThatThrownBy(() -> vocabularyService.create(roundOverflowEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Ease factor must not exceed 999.99");

            var negRepetition = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("2.50"), -1, null);
            assertThatThrownBy(() -> vocabularyService.create(negRepetition))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Repetition count must be nonnegative");

            var negLapse = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("2.50"), 0, -1);
            assertThatThrownBy(() -> vocabularyService.create(negLapse))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Lapse count must be nonnegative");

            // Boundaries that succeed
            var minValidEase = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("0.005"), 0, 0);
            var minResult = vocabularyService.create(minValidEase);
            assertThat(minResult.easeFactor()).isEqualByComparingTo(new BigDecimal("0.01"));

            var maxValidEase = new CreateVocabularyItemCommand("word", "en", "meaning", null, null, null, null, null, null, null, null, 0, new BigDecimal("999.99"), 0, 0);
            var maxResult = vocabularyService.create(maxValidEase);
            assertThat(maxResult.easeFactor()).isEqualByComparingTo(new BigDecimal("999.99"));
        }

        @Test
        @DisplayName("Validates review transition inputs including ease factor boundaries")
        void validatesReviewTransitionInputs() {
            var nullResponse = new VocabularyReviewTransitionCommand(null, VocabularyLearningStatus.REVIEW, Instant.now(), 1, new BigDecimal("2.50"), 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, nullResponse))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("response must not be null");

            var nullStatus = new VocabularyReviewTransitionCommand(SrsReviewResponse.GOOD, null, Instant.now(), 1, new BigDecimal("2.50"), 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, nullStatus))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Learning status must not be null");

            var negInterval = new VocabularyReviewTransitionCommand(SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now(), -1, new BigDecimal("2.50"), 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, negInterval))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New interval days must be nonnegative");

            var zeroEase = new VocabularyReviewTransitionCommand(SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now(), 1, BigDecimal.ZERO, 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, zeroEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New ease factor must be greater than zero");

            var roundsToZeroEase = new VocabularyReviewTransitionCommand(SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now(), 1, new BigDecimal("0.001"), 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, roundsToZeroEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New ease factor must be greater than zero");

            var overflowEase = new VocabularyReviewTransitionCommand(SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now(), 1, new BigDecimal("1000.00"), 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, overflowEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New ease factor must not exceed 999.99");

            var roundOverflowEase = new VocabularyReviewTransitionCommand(SrsReviewResponse.GOOD, VocabularyLearningStatus.REVIEW, Instant.now(), 1, new BigDecimal("999.995"), 1, 0, null);
            assertThatThrownBy(() -> vocabularyService.reviewTransition(1L, roundOverflowEase))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("New ease factor must not exceed 999.99");
        }

        @Test
        @DisplayName("findDue and findReviews require positive limit and non-null parameters")
        void validatesReadParameters() {
            assertThatThrownBy(() -> vocabularyService.findDue(null, 10))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Cutoff must not be null");

            assertThatThrownBy(() -> vocabularyService.findDue(Instant.now(), 0))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Limit must be positive");

            assertThatThrownBy(() -> vocabularyService.findReviewsByVocabularyId(null, 10))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Vocabulary ID must not be null");

            assertThatThrownBy(() -> vocabularyService.findReviewsByVocabularyId(1L, -1))
                    .isInstanceOf(InvalidVocabularyItemException.class)
                    .hasMessageContaining("Limit must be positive");
        }
    }

    @Nested
    @DisplayName("Note validation tests")
    class NoteValidationTests {

        private NoteRepository noteRepository;
        private VaultEntryOperations vaultEntryOperations;
        private NoteService noteService;

        @BeforeEach
        void setUp() {
            noteRepository = mock(NoteRepository.class);
            vaultEntryOperations = mock(VaultEntryOperations.class);
            noteService = new NoteService(noteRepository, vaultEntryOperations);

            when(vaultEntryOperations.create(VaultEntryType.NOTE))
                    .thenReturn(new VaultEntryView(400L, VaultEntryType.NOTE, Instant.now(), Instant.now(), null));
        }

        @Test
        @DisplayName("Rejects blank title and null content markdown")
        void rejectsBlankTitleAndNullContent() {
            var blankTitle = new CreateNoteCommand("  ", "# Heading", null, null, null, null, null, null);
            assertThatThrownBy(() -> noteService.create(blankTitle))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("title must not be blank");

            var nullContent = new CreateNoteCommand("Title", null, null, null, null, null, null, null);
            assertThatThrownBy(() -> noteService.create(nullContent))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("Content markdown must not be null");
        }

        @Test
        @DisplayName("Rejects overlong imported file hash")
        void rejectsOverlongHash() {
            var longHash = new CreateNoteCommand("Title", "Content", null, null, null, null, "h".repeat(65), null);
            assertThatThrownBy(() -> noteService.create(longHash))
                    .isInstanceOf(InvalidNoteException.class)
                    .hasMessageContaining("hash must not exceed 64 characters");
        }

        @Test
        @DisplayName("Rejects duplicate imported file hash on create and update without leaking hash in message")
        void rejectsDuplicateHash() {
            var existingNote = mock(Note.class);
            when(existingNote.getId()).thenReturn(999L);
            when(noteRepository.findByImportedFileHash("known-hash")).thenReturn(Optional.of(existingNote));

            var duplicateCreate = new CreateNoteCommand("Title", "Content", null, null, null, null, "known-hash", null);
            assertThatThrownBy(() -> noteService.create(duplicateCreate))
                    .isInstanceOf(NoteConflictException.class)
                    .hasMessage("A note with the specified imported file hash already exists");

            when(noteRepository.findById(1L)).thenReturn(Optional.of(mock(Note.class)));
            var duplicateUpdate = new UpdateNoteCommand("Title", "Content", null, null, null, null, "known-hash", null);
            assertThatThrownBy(() -> noteService.update(1L, duplicateUpdate))
                    .isInstanceOf(NoteConflictException.class)
                    .hasMessage("A note with the specified imported file hash already exists");
        }
    }

    @Nested
    @DisplayName("Knowledge facade mapping and exception translation tests")
    class KnowledgeFacadeMappingTests {

        private StudyItemOperations studyItemOperations;
        private InformationItemOperations informationItemOperations;
        private VocabularyOperations vocabularyOperations;
        private NoteOperations noteOperations;
        private KnowledgeFacadeService facadeService;

        @BeforeEach
        void setUp() {
            studyItemOperations = mock(StudyItemOperations.class);
            informationItemOperations = mock(InformationItemOperations.class);
            vocabularyOperations = mock(VocabularyOperations.class);
            noteOperations = mock(NoteOperations.class);

            facadeService = new KnowledgeFacadeService(
                    studyItemOperations,
                    informationItemOperations,
                    vocabularyOperations,
                    noteOperations
            );
        }

        @Test
        @DisplayName("Translates Study exceptions to parent Knowledge exceptions")
        void translatesStudyExceptions() {
            when(studyItemOperations.create(any()))
                    .thenThrow(new InvalidStudyItemException("Invalid study item"));
            var createCmd = new CreateKnowledgeStudyItemCommand("Title", null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, KnowledgeStudyStatus.PLANNED, null, null);
            assertThatThrownBy(() -> facadeService.createStudyItem(createCmd))
                    .isInstanceOf(InvalidKnowledgeItemException.class)
                    .hasMessage("Invalid study item");

            when(studyItemOperations.update(any(), any()))
                    .thenThrow(new StudyItemNotFoundException(1L));
            var updateCmd = new UpdateKnowledgeStudyItemCommand("Title", null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, KnowledgeStudyStatus.PLANNED, null, null);
            assertThatThrownBy(() -> facadeService.updateStudyItem(1L, updateCmd))
                    .isInstanceOf(KnowledgeNotFoundException.class)
                    .hasMessage("Study item with id 1 does not exist");

            doThrow(new StudyConflictException("Conflict"))
                    .when(studyItemOperations).update(any(), any());
            assertThatThrownBy(() -> facadeService.updateStudyItem(1L, updateCmd))
                    .isInstanceOf(KnowledgeConflictException.class)
                    .hasMessage("Conflict");
        }

        @Test
        @DisplayName("Translates Information exceptions to parent Knowledge exceptions")
        void translatesInformationExceptions() {
            when(informationItemOperations.create(any()))
                    .thenThrow(new InvalidInformationItemException("Invalid info"));
            var createCmd = new CreateKnowledgeInformationItemCommand("Title", KnowledgeInformationType.TECHNOLOGY, null, null, null, null, null);
            assertThatThrownBy(() -> facadeService.createInformationItem(createCmd))
                    .isInstanceOf(InvalidKnowledgeItemException.class)
                    .hasMessage("Invalid info");

            when(informationItemOperations.update(any(), any()))
                    .thenThrow(new InformationItemNotFoundException(2L));
            var updateCmd = new UpdateKnowledgeInformationItemCommand("Title", KnowledgeInformationType.TECHNOLOGY, null, null, null, null, null);
            assertThatThrownBy(() -> facadeService.updateInformationItem(2L, updateCmd))
                    .isInstanceOf(KnowledgeNotFoundException.class)
                    .hasMessage("Information item with id 2 does not exist");
        }

        @Test
        @DisplayName("Translates Vocabulary exceptions to parent Knowledge exceptions")
        void translatesVocabularyExceptions() {
            when(vocabularyOperations.create(any()))
                    .thenThrow(new InvalidVocabularyItemException("Invalid vocab"));
            var createCmd = new CreateKnowledgeVocabularyItemCommand("Word", "en", "Meaning", null, null, null, null, null, null, KnowledgeVocabularyLearningStatus.NEW, null, 0, new BigDecimal("2.50"), 0, 0);
            assertThatThrownBy(() -> facadeService.createVocabularyItem(createCmd))
                    .isInstanceOf(InvalidKnowledgeItemException.class)
                    .hasMessage("Invalid vocab");

            when(vocabularyOperations.update(any(), any()))
                    .thenThrow(new VocabularyItemNotFoundException(3L));
            var updateCmd = new UpdateKnowledgeVocabularyItemCommand("Word", "en", "Meaning", null, null, null, null, null, null, KnowledgeVocabularyLearningStatus.NEW, null, 0, new BigDecimal("2.50"), 0, 0);
            assertThatThrownBy(() -> facadeService.updateVocabularyItem(3L, updateCmd))
                    .isInstanceOf(KnowledgeNotFoundException.class)
                    .hasMessage("Vocabulary item with id 3 does not exist");

            var transitionCmd = new KnowledgeVocabularyReviewTransitionCommand(KnowledgeSrsReviewResponse.GOOD, KnowledgeVocabularyLearningStatus.REVIEW, Instant.now(), 1, new BigDecimal("2.50"), 1, 0, null);
            when(vocabularyOperations.reviewTransition(any(), any()))
                    .thenThrow(new VocabularyItemNotFoundException(3L));
            assertThatThrownBy(() -> facadeService.reviewVocabularyItem(3L, transitionCmd))
                    .isInstanceOf(KnowledgeNotFoundException.class);
        }

        @Test
        @DisplayName("Translates Note exceptions to parent Knowledge exceptions")
        void translatesNoteExceptions() {
            when(noteOperations.create(any()))
                    .thenThrow(new NoteConflictException("Duplicate hash"));
            var createCmd = new CreateKnowledgeNoteCommand("Title", "Content", null, null, null, null, "hash", null);
            assertThatThrownBy(() -> facadeService.createNote(createCmd))
                    .isInstanceOf(KnowledgeConflictException.class)
                    .hasMessage("Duplicate hash");

            when(noteOperations.update(any(), any()))
                    .thenThrow(new NoteNotFoundException(4L));
            var updateCmd = new UpdateKnowledgeNoteCommand("Title", "Content", null, null, null, null, "hash", null);
            assertThatThrownBy(() -> facadeService.updateNote(4L, updateCmd))
                    .isInstanceOf(KnowledgeNotFoundException.class)
                    .hasMessage("Note with id 4 does not exist");
        }
    }
}
