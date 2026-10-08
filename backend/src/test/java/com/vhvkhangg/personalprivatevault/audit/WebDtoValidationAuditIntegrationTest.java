package com.vhvkhangg.personalprivatevault.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateExternalAccountRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotEntryRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.SetExternalAccountRelationshipRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.UpdateExternalAccountRequest;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateMusicRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateMusicRequest;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToInformationRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToNoteRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToStudyRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.UpdateFeedSourceRequest;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.journal.diary.DiaryOperations;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.CreateDiaryEntryRequest;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.UpdateDiaryEntryRequest;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.information.information.InformationItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeInformationRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeNoteRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeVocabularyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeInformationRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeNoteRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeVocabularyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateAddressRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateAddressRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationRequest;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateManualSavedResourceRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionGenreRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionGenreRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmGenreRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmGenreRequest;
import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateFinancialTransactionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateRecurringTransactionRuleRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateSubscriptionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.FinancialTransactionEntryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.RecurringRuleEntryRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateFinancialTransactionRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateRecurringTransactionRuleRequest;
import com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateSubscriptionRequest;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.CreateImportJobRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationCategoryRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationCategoryRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.CreateAlbumRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.UpdateAlbumRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.CreateImageRequest;
import com.vhvkhangg.personalprivatevault.media.internal.web.dto.UpdateImageRequest;
import com.vhvkhangg.personalprivatevault.personal.enums.Gender;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.CreatePersonalProfileRequest;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.UpdatePersonalProfileRequest;
import com.vhvkhangg.personalprivatevault.personal.profile.PersonalProfileOperations;
import com.vhvkhangg.personalprivatevault.settings.internal.web.dto.UpdateSettingsRequest;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-2 and BA15-15 regression tests:
 * Verifies Web DTO validation boundaries across modules matching authoritative domain contracts:
 * - 500-character title, prompt, sourceName, and importedFileName bounds (and 501-character rejection)
 * - Blank Markdown preservation in Note, Diary, and Personal Profile creation/update
 * - Null Markdown rejection when required
 * - External Account identity alternatives (username, externalId, url) and bounds
 * - Media Image nullable metadata and character bounds
 * - Location Address nullable fields and bounds
 * - Direct Knowledge Study, Information, Vocabulary, and Note DTO boundaries
 * - Collection Music default ORIGINAL version and title bounds
 * - Personal Profile phone and email boundaries
 * - Generated OpenAPI schema semantics (maxima, field descriptions, defaults, and optionality).
 */
class WebDtoValidationAuditIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private SavedResourceOperations savedResourceOperations;

    @Autowired
    private StudyItemOperations studyItemOperations;

    @Autowired
    private InformationItemOperations informationItemOperations;

    @Autowired
    private NoteOperations noteOperations;

    @Autowired
    private DiaryOperations diaryOperations;

    @Autowired
    private PersonalProfileOperations profileOperations;

    @Autowired
    private WalletOperations walletOperations;

    private Long createTestSavedResource(String title) {
        SavedResourceView view = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                SavedResourceKind.ARTICLE,
                title,
                "https://example.com/article-" + System.nanoTime(),
                "Author",
                "Summary",
                null,
                "Source",
                "https://example.com",
                null,
                null
        ));
        return view.id();
    }

    // =========================================================================
    // 1. Feed Conversions (ConvertToStudyRequest, ConvertToInformationRequest, ConvertToNoteRequest)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-5: ConvertToStudyRequest accepts title & currentProgressText up to 500 chars and rejects 501 chars")
    void convertToStudyRequestBoundaries() throws Exception {
        Long resId = createTestSavedResource("Resource for Study");

        // 500 characters title & currentProgressText -> valid
        String title500 = "T".repeat(500);
        String progressText500 = "P".repeat(500);
        ConvertToStudyRequest validRequest = new ConvertToStudyRequest(
                title500, null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, progressText500
        );
        var result = mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetVaultEntryId").isNumber())
                .andReturn();

        long targetId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("targetVaultEntryId").asLong();
        assertThat(studyItemOperations.findById(targetId).orElseThrow().title()).isEqualTo(title500);

        // 501 characters title -> 400 validation error
        Long resId2 = createTestSavedResource("Resource for Study 2");
        ConvertToStudyRequest invalidTitle = new ConvertToStudyRequest(
                "T".repeat(501), null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 501 characters currentProgressText -> 400 validation error
        ConvertToStudyRequest invalidProgress = new ConvertToStudyRequest(
                "Valid Title", null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, "P".repeat(501)
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProgress)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("BA15-2 / FR15-5: ConvertToInformationRequest accepts title/sourceName up to 500 chars and rejects 501 chars")
    void convertToInformationRequestBoundaries() throws Exception {
        Long resId = createTestSavedResource("Resource for Info");

        // 500 characters title & sourceName -> valid
        String title500 = "I".repeat(500);
        String source500 = "S".repeat(500);
        ConvertToInformationRequest validRequest = new ConvertToInformationRequest(
                title500, KnowledgeInformationType.TECHNOLOGY, "Description", "Markdown", "Example", source500, "https://example.com"
        );
        var result = mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/information", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetVaultEntryId").isNumber())
                .andReturn();

        long targetId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("targetVaultEntryId").asLong();
        assertThat(informationItemOperations.findById(targetId).orElseThrow().title()).isEqualTo(title500);

        // 501 characters title -> 400 validation error
        Long resId2 = createTestSavedResource("Resource for Info 2");
        ConvertToInformationRequest invalidTitle = new ConvertToInformationRequest(
                "I".repeat(501), KnowledgeInformationType.TECHNOLOGY, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/information", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 501 characters sourceName -> 400 validation error
        ConvertToInformationRequest invalidSource = new ConvertToInformationRequest(
                "Valid Title", KnowledgeInformationType.TECHNOLOGY, null, null, null, "S".repeat(501), null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/information", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("BA15-2 / FR15-5: ConvertToNoteRequest preserves blank Markdown and honors 500-character bounds")
    void convertToNoteRequestBoundariesAndBlankMarkdown() throws Exception {
        Long resId = createTestSavedResource("Resource for Note");

        // Blank markdown "" is preserved and accepted; title, sourceName, importedFileName up to 500 chars
        String title500 = "N".repeat(500);
        String source500 = "S".repeat(500);
        String file500 = "F".repeat(500);
        ConvertToNoteRequest validBlankRequest = new ConvertToNoteRequest(
                title500, "", "Summary", source500, "https://example.com", file500, "hash123", null
        );
        var result = mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBlankRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetVaultEntryId").isNumber())
                .andReturn();

        long targetId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("targetVaultEntryId").asLong();
        var note = noteOperations.findById(targetId).orElseThrow();
        assertThat(note.title()).isEqualTo(title500);
        assertThat(note.contentMarkdown()).isEqualTo("");

        // 501 characters importedFileName -> 400 validation error
        Long resId2 = createTestSavedResource("Resource for Note 2");
        ConvertToNoteRequest invalidFileName = new ConvertToNoteRequest(
                "Valid Title", "Content", null, null, null, "F".repeat(501), null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFileName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Null markdown -> 400 validation error
        ConvertToNoteRequest nullMarkdown = new ConvertToNoteRequest(
                "Valid Title", null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullMarkdown)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("BA15-2 / BA15-15: Feed note conversion independently rejects title, sourceName, and importedFileName at 501 chars")
    void feedNoteConversionIndependentMaxPlusOneRejections() throws Exception {
        Long resId = createTestSavedResource("Feed Note Conversion Bound Checks");

        // Independent 501 title
        ConvertToNoteRequest req1 = new ConvertToNoteRequest(
                "N".repeat(501), "Markdown", null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Independent 501 sourceName
        ConvertToNoteRequest req2 = new ConvertToNoteRequest(
                "Valid Title", "Markdown", null, "S".repeat(501), null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Independent 501 importedFileName
        ConvertToNoteRequest req3 = new ConvertToNoteRequest(
                "Valid Title", "Markdown", null, null, null, "F".repeat(501), null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 2. Journal Diary Entry (CreateDiaryEntryRequest & UpdateDiaryEntryRequest)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Journal Diary entry validates 500-char title boundary, accepts blank Markdown, rejects null")
    void journalDiaryEntryValidationBoundaries() throws Exception {
        String title500 = "D".repeat(500);

        // Create with 500-char title and blank "" Markdown -> valid (201)
        CreateDiaryEntryRequest validCreate = new CreateDiaryEntryRequest(LocalDate.of(2026, 10, 7), title500, "");
        var res = mockMvc.perform(post("/api/v1/journal/diary-entries")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        long diaryId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        var diary = diaryOperations.findDiaryEntryById(diaryId);
        assertThat(diary.title()).isEqualTo(title500);
        assertThat(diary.contentMarkdown()).isEqualTo("");

        // Create with 501-char title -> 400 validation error
        CreateDiaryEntryRequest invalidTitle = new CreateDiaryEntryRequest(LocalDate.of(2026, 10, 7), "D".repeat(501), "content");
        mockMvc.perform(post("/api/v1/journal/diary-entries")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Create with null markdown -> 400 validation error
        CreateDiaryEntryRequest nullMarkdown = new CreateDiaryEntryRequest(LocalDate.of(2026, 10, 7), "Valid Title", null);
        mockMvc.perform(post("/api/v1/journal/diary-entries")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullMarkdown)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Update with 500-char title and blank "" Markdown -> valid (200)
        UpdateDiaryEntryRequest validUpdate = new UpdateDiaryEntryRequest(LocalDate.of(2026, 10, 8), title500, "");
        mockMvc.perform(put("/api/v1/journal/diary-entries/{id}", diaryId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk());

        // Update with 501-char title -> 400 validation error
        UpdateDiaryEntryRequest invalidUpdateTitle = new UpdateDiaryEntryRequest(LocalDate.of(2026, 10, 8), "D".repeat(501), "content");
        mockMvc.perform(put("/api/v1/journal/diary-entries/{id}", diaryId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Update with null markdown -> 400 validation error
        UpdateDiaryEntryRequest nullUpdateMarkdown = new UpdateDiaryEntryRequest(LocalDate.of(2026, 10, 8), "Valid Title", null);
        mockMvc.perform(put("/api/v1/journal/diary-entries/{id}", diaryId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullUpdateMarkdown)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 3. Knowledge Note (CreateKnowledgeNoteRequest & UpdateKnowledgeNoteRequest)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Knowledge Note validates 500-char title/source/file boundaries, accepts blank Markdown, rejects null")
    void knowledgeNoteValidationBoundaries() throws Exception {
        String title500 = "K".repeat(500);
        String source500 = "S".repeat(500);
        String file500 = "F".repeat(500);

        // Create with 500-char fields and blank "" Markdown -> valid (201)
        CreateKnowledgeNoteRequest validCreate = new CreateKnowledgeNoteRequest(
                title500, "", "Summary", source500, "https://example.com", file500, "hash999", null
        );
        var res = mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        long noteId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        var note = noteOperations.findById(noteId).orElseThrow();
        assertThat(note.title()).isEqualTo(title500);
        assertThat(note.contentMarkdown()).isEqualTo("");

        // Create with 501-char title -> 400 validation error
        CreateKnowledgeNoteRequest invalidTitle = new CreateKnowledgeNoteRequest(
                "K".repeat(501), "content", null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Create with null markdown -> 400 validation error
        CreateKnowledgeNoteRequest nullMarkdown = new CreateKnowledgeNoteRequest(
                "Valid Note", null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullMarkdown)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Update with blank "" Markdown -> valid (200)
        UpdateKnowledgeNoteRequest validUpdate = new UpdateKnowledgeNoteRequest(
                "Updated Note", "", "Summary", null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk());

        // Update with null markdown -> 400 validation error
        UpdateKnowledgeNoteRequest nullUpdateMarkdown = new UpdateKnowledgeNoteRequest(
                "Updated Note", null, "Summary", null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullUpdateMarkdown)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 4. Personal Profile (CreatePersonalProfileRequest & UpdatePersonalProfileRequest)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Personal profile validates name/relationship boundaries and preserves blank notes Markdown")
    void personalProfileValidationBoundaries() throws Exception {
        // Name max 255 valid, relationship max 100 valid, blank notesMarkdown preserved
        CreatePersonalProfileRequest validProfile = new CreatePersonalProfileRequest(
                "P".repeat(255), "R".repeat(100), false, Gender.FEMALE, LocalDate.of(1990, 1, 1),
                "VN", "+84123456789", "profile@example.com", null, "Developer", ""
        );
        var res = mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        long profileId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        var profile = profileOperations.findProfileById(profileId);
        assertThat(profile.name()).isEqualTo("P".repeat(255));
        assertThat(profile.relationship()).isEqualTo("R".repeat(100));
        assertThat(profile.notesMarkdown()).isEqualTo("");

        // 256 chars name -> 400 validation error
        CreatePersonalProfileRequest invalidName = new CreatePersonalProfileRequest(
                "P".repeat(256), "Friend", false, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("BA15-2 / BA15-15: Personal profile phone and email boundaries on POST & PUT")
    void personalProfileCreateAndUpdateBoundaries() throws Exception {
        String phone64 = "1".repeat(64);
        String phone65 = "1".repeat(65);

        String emailLocal = "a".repeat(64);
        String emailDomain = "b".repeat(60) + "." + "c".repeat(60) + "." + "d".repeat(60) + "." + "e".repeat(60) + "." + "f".repeat(7) + ".com";
        String email320 = emailLocal + "@" + emailDomain;
        assertThat(email320.length()).isEqualTo(320);

        CreatePersonalProfileRequest validCreate = new CreatePersonalProfileRequest(
                "Profile 1", "Friend", false, null, null, null, phone64, email320, null, null, null
        );
        var res = mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        long profileId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Phone 65 -> 400
        CreatePersonalProfileRequest badPhone = new CreatePersonalProfileRequest(
                "Profile Bad Phone", "Friend", false, null, null, null, phone65, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPhone)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Email 321 -> 400
        CreatePersonalProfileRequest badEmail = new CreatePersonalProfileRequest(
                "Profile Bad Email", "Friend", false, null, null, null, null, email320 + "x", null, null, null
        );
        mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badEmail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // PUT phone 64 valid, phone 65 -> 400
        UpdatePersonalProfileRequest putValid = new UpdatePersonalProfileRequest(
                "Profile Updated", "Friend", false, null, null, null, phone64, email320, null, null, null
        );
        mockMvc.perform(put("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putValid)))
                .andExpect(status().isOk());

        UpdatePersonalProfileRequest putBadPhone = new UpdatePersonalProfileRequest(
                "Profile Updated", "Friend", false, null, null, null, phone65, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putBadPhone)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdatePersonalProfileRequest putBadEmail = new UpdatePersonalProfileRequest(
                "Profile Updated", "Friend", false, null, null, null, null, email320 + "x", null, null, null
        );
        mockMvc.perform(put("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putBadEmail)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 5. External Account Validation & Identity Alternatives (BA15-2 / BA15-15)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Account identity alternatives, bounds, and neither-provided rejection")
    void accountIdentityAlternativesAndBounds() throws Exception {
        Long platformId = jdbcTemplate.queryForObject("SELECT id FROM platforms LIMIT 1", Long.class);

        // Alternative 1: only username -> 201 created, PUT -> 200 ok
        CreateExternalAccountRequest onlyUsername = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "user_only", null, null, null, null, null, null, null, null
        );
        var res1 = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyUsername)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("user_only"))
                .andReturn();
        long id1 = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("id").asLong();

        UpdateExternalAccountRequest putUsername = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "user_updated", null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putUsername)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("user_updated"));

        // Alternative 2: only externalId -> 201 created, PUT -> 200 ok
        CreateExternalAccountRequest onlyExternalId = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, null, "ext_123", null, null, null, null, null, null, null
        );
        var res2 = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyExternalId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.externalId").value("ext_123"))
                .andReturn();
        long id2 = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data").path("id").asLong();

        UpdateExternalAccountRequest putExternalId = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, null, "ext_updated", null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putExternalId)))
                .andExpect(status().isOk());

        // Alternative 3: only url -> 201 created, PUT -> 200 ok
        CreateExternalAccountRequest onlyUrl = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, null, null, null, null, null, null, null, "https://example.com/user3", null
        );
        var res3 = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlyUrl)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.url").value("https://example.com/user3"))
                .andReturn();
        long id3 = objectMapper.readTree(res3.getResponse().getContentAsString()).path("data").path("id").asLong();

        UpdateExternalAccountRequest putUrl = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, null, null, null, null, null, null, null, "https://example.com/user3_updated", null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id3)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putUrl)))
                .andExpect(status().isOk());

        // Neither username, externalId, nor url -> 422 ACCOUNT_INVALID
        CreateExternalAccountRequest neitherPost = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(neitherPost)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_EXTERNAL_ACCOUNT"));

        UpdateExternalAccountRequest neitherPut = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(neitherPut)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_EXTERNAL_ACCOUNT"));

        // displayName 500 valid / 501 -> 400
        CreateExternalAccountRequest displayName500 = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "disp_user", null, "D".repeat(500), null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(displayName500)))
                .andExpect(status().isCreated());

        CreateExternalAccountRequest displayName501 = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "disp_user_invalid", null, "D".repeat(501), null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(displayName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // ownerName 500 valid / 501 -> 400
        CreateExternalAccountRequest ownerName500 = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "owner_user", null, null, null, null, null, "O".repeat(500), null, null
        );
        mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerName500)))
                .andExpect(status().isCreated());

        CreateExternalAccountRequest ownerName501 = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "owner_user_invalid", null, null, null, null, null, "O".repeat(501), null, null
        );
        mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // displayName & ownerName PUT boundaries (500 valid, 501 -> 400)
        UpdateExternalAccountRequest putDisplayName500 = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "user_put_disp", null, "D".repeat(500), null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDisplayName500)))
                .andExpect(status().isOk());

        UpdateExternalAccountRequest putDisplayName501 = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "user_put_disp_inv", null, "D".repeat(501), null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDisplayName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateExternalAccountRequest putOwnerName500 = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "user_put_owner", null, null, null, null, null, "O".repeat(500), null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putOwnerName500)))
                .andExpect(status().isOk());

        UpdateExternalAccountRequest putOwnerName501 = new UpdateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "user_put_owner_inv", null, null, null, null, null, "O".repeat(501), null, null
        );
        mockMvc.perform(put("/api/v1/accounts/{id}", id1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putOwnerName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 6. Media Image Validation Boundaries (BA15-2 / BA15-15)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Image nullable metadata and character boundaries on POST & PUT")
    void imageNullableMetadataAndBounds() throws Exception {
        // POST with all nullable metadata null -> 201
        String key1 = "img-" + System.nanoTime();
        CreateImageRequest allNullMeta = new CreateImageRequest(
                null, null, null, key1, null, null, null, null, null, null, null, null
        );
        var res = mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allNullMeta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        long imgId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // PUT with all nullable metadata null -> 200
        UpdateImageRequest allNullUpdate = new UpdateImageRequest(
                null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/images/{id}", imgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allNullUpdate)))
                .andExpect(status().isOk());

        // title 500 valid / 501 -> 400
        String key2 = "img-t500-" + System.nanoTime();
        CreateImageRequest title500 = new CreateImageRequest(
                null, "T".repeat(500), null, key2, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(title500)))
                .andExpect(status().isCreated());

        CreateImageRequest title501 = new CreateImageRequest(
                null, "T".repeat(501), null, "key-t501-" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(title501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateImageRequest putTitle501 = new UpdateImageRequest(
                null, "T".repeat(501), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/images/{id}", imgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // imageType 100 valid / 101 -> 400
        CreateImageRequest type100 = new CreateImageRequest(
                null, null, "Y".repeat(100), "img-y100-" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(type100)))
                .andExpect(status().isCreated());

        CreateImageRequest type101 = new CreateImageRequest(
                null, null, "Y".repeat(101), "img-y101-" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(type101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateImageRequest putType101 = new UpdateImageRequest(
                null, null, "Y".repeat(101), null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/images/{id}", imgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putType101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // locationText 500 valid / 501 -> 400
        CreateImageRequest loc500 = new CreateImageRequest(
                null, null, null, "img-loc500-" + System.nanoTime(), null, null, null, null, null, null, null, "L".repeat(500)
        );
        mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loc500)))
                .andExpect(status().isCreated());

        CreateImageRequest loc501 = new CreateImageRequest(
                null, null, null, "img-loc501-" + System.nanoTime(), null, null, null, null, null, null, null, "L".repeat(501)
        );
        mockMvc.perform(post("/api/v1/images")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateImageRequest putLoc501 = new UpdateImageRequest(
                null, null, null, null, null, null, null, null, null, "L".repeat(501)
        );
        mockMvc.perform(put("/api/v1/images/{id}", imgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putLoc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // PUT with valid maxima (title 500, imageType 100, locationText 500) -> 200 OK
        UpdateImageRequest putValidMaxima = new UpdateImageRequest(
                null, "T".repeat(500), "Y".repeat(100), null, null, null, null, null, null, "L".repeat(500)
        );
        mockMvc.perform(put("/api/v1/images/{id}", imgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putValidMaxima)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // 7. Location Address Boundaries (BA15-2 / BA15-15)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Address nullable fields and bounds on POST & PUT")
    void addressNullableFieldsAndBounds() throws Exception {
        // POST with null locality and null streetAddress -> 201
        CreateAddressRequest nullLocStreet = new CreateAddressRequest(
                "Home", "RESIDENTIAL", "VN", "Hanoi", null, null, null, "10000"
        );
        var res = mockMvc.perform(post("/api/v1/addresses")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullLocStreet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        long addrId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // PUT with null locality and null streetAddress -> 200
        UpdateAddressRequest putNullLocStreet = new UpdateAddressRequest(
                "Office", "OFFICE", "VN", "Hanoi", null, null, null, "10001"
        );
        mockMvc.perform(put("/api/v1/addresses/{id}", addrId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putNullLocStreet)))
                .andExpect(status().isOk());

        // addressType 100 valid / 101 -> 400
        CreateAddressRequest type100 = new CreateAddressRequest(
                "Label", "A".repeat(100), "VN", null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/addresses")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(type100)))
                .andExpect(status().isCreated());

        CreateAddressRequest type101 = new CreateAddressRequest(
                "Label", "A".repeat(101), "VN", null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/addresses")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(type101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateAddressRequest putType101 = new UpdateAddressRequest(
                "Label", "A".repeat(101), "VN", null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/addresses/{id}", addrId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putType101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // postalCode 32 valid / 33 -> 400
        CreateAddressRequest post32 = new CreateAddressRequest(
                "Label", null, "VN", null, null, null, null, "P".repeat(32)
        );
        mockMvc.perform(post("/api/v1/addresses")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post32)))
                .andExpect(status().isCreated());

        CreateAddressRequest post33 = new CreateAddressRequest(
                "Label", null, "VN", null, null, null, null, "P".repeat(33)
        );
        mockMvc.perform(post("/api/v1/addresses")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post33)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateAddressRequest putPost33 = new UpdateAddressRequest(
                "Label", null, "VN", null, null, null, null, "P".repeat(33)
        );
        mockMvc.perform(put("/api/v1/addresses/{id}", addrId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putPost33)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // PUT with valid maxima (addressType 100, postalCode 32) -> 200 OK
        UpdateAddressRequest putValidMaxima = new UpdateAddressRequest(
                "Office", "A".repeat(100), "VN", "Hanoi", null, null, null, "P".repeat(32)
        );
        mockMvc.perform(put("/api/v1/addresses/{id}", addrId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putValidMaxima)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // 8. Knowledge Direct DTOs Boundaries (BA15-2 / BA15-15)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Direct Study, Information, Vocabulary, and Note DTO boundaries on POST & PUT")
    void knowledgeDirectDtosBoundaries() throws Exception {
        // Study: title 500 / 501, currentProgressText 500 / 501
        CreateKnowledgeStudyRequest study500 = new CreateKnowledgeStudyRequest(
                "S".repeat(500), null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, "P".repeat(500)
        );
        var sRes = mockMvc.perform(post("/api/v1/knowledge/study")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(study500)))
                .andExpect(status().isCreated())
                .andReturn();
        long studyId = objectMapper.readTree(sRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateKnowledgeStudyRequest studyTitle501 = new CreateKnowledgeStudyRequest(
                "S".repeat(501), null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/study")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studyTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeStudyRequest putStudyTitle501 = new UpdateKnowledgeStudyRequest(
                "S".repeat(501), null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/study/{id}", studyId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putStudyTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeStudyRequest studyProg501 = new CreateKnowledgeStudyRequest(
                "Valid Title", null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, "P".repeat(501)
        );
        mockMvc.perform(post("/api/v1/knowledge/study")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studyProg501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeStudyRequest putStudyProg501 = new UpdateKnowledgeStudyRequest(
                "Valid Title", null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, "P".repeat(501)
        );
        mockMvc.perform(put("/api/v1/knowledge/study/{id}", studyId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putStudyProg501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Study PUT valid maxima (title 500, currentProgressText 500) -> 200 OK
        UpdateKnowledgeStudyRequest putStudy500 = new UpdateKnowledgeStudyRequest(
                "S".repeat(500), null, KnowledgeStudyType.BOOK, null, null, null, null, null, null, null, null, null, null, null, null, "P".repeat(500)
        );
        mockMvc.perform(put("/api/v1/knowledge/study/{id}", studyId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putStudy500)))
                .andExpect(status().isOk());

        // Information: title 500 / 501, sourceName 500 / 501
        CreateKnowledgeInformationRequest info500 = new CreateKnowledgeInformationRequest(
                "I".repeat(500), KnowledgeInformationType.TECHNOLOGY, "Desc", "Md", "Ex", "S".repeat(500), "https://example.com"
        );
        var iRes = mockMvc.perform(post("/api/v1/knowledge/information")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(info500)))
                .andExpect(status().isCreated())
                .andReturn();
        long infoId = objectMapper.readTree(iRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateKnowledgeInformationRequest infoTitle501 = new CreateKnowledgeInformationRequest(
                "I".repeat(501), KnowledgeInformationType.TECHNOLOGY, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/information")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(infoTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeInformationRequest putInfoTitle501 = new UpdateKnowledgeInformationRequest(
                "I".repeat(501), KnowledgeInformationType.TECHNOLOGY, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/information/{id}", infoId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putInfoTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeInformationRequest infoSrc501 = new CreateKnowledgeInformationRequest(
                "Valid Title", KnowledgeInformationType.TECHNOLOGY, null, null, null, "S".repeat(501), null
        );
        mockMvc.perform(post("/api/v1/knowledge/information")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(infoSrc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeInformationRequest putInfoSrc501 = new UpdateKnowledgeInformationRequest(
                "Valid Title", KnowledgeInformationType.TECHNOLOGY, null, null, null, "S".repeat(501), null
        );
        mockMvc.perform(put("/api/v1/knowledge/information/{id}", infoId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putInfoSrc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Information PUT valid maxima (title 500, sourceName 500) -> 200 OK
        UpdateKnowledgeInformationRequest putInfo500 = new UpdateKnowledgeInformationRequest(
                "I".repeat(500), KnowledgeInformationType.TECHNOLOGY, "Desc", "Md", "Ex", "S".repeat(500), "https://example.com"
        );
        mockMvc.perform(put("/api/v1/knowledge/information/{id}", infoId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putInfo500)))
                .andExpect(status().isOk());

        // Vocabulary: word 500 / 501, pronunciation 500 / 501, partOfSpeech 100 / 101, sourceName 500 / 501
        CreateKnowledgeVocabularyRequest vocab500 = new CreateKnowledgeVocabularyRequest(
                "W".repeat(500), "en", "Meaning", "Ex", "P".repeat(500), null, "N".repeat(100), "S".repeat(500), null, null, null, null, null, null, null
        );
        var vRes = mockMvc.perform(post("/api/v1/knowledge/vocabulary")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vocab500)))
                .andExpect(status().isCreated())
                .andReturn();
        long vocabId = objectMapper.readTree(vRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateKnowledgeVocabularyRequest vWord501 = new CreateKnowledgeVocabularyRequest(
                "W".repeat(501), "en", "Meaning", null, null, null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/vocabulary")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vWord501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeVocabularyRequest putVWord501 = new UpdateKnowledgeVocabularyRequest(
                "W".repeat(501), "en", "Meaning", null, null, null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/vocabulary/{id}", vocabId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putVWord501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeVocabularyRequest vPron501 = new CreateKnowledgeVocabularyRequest(
                "Word", "en", "Meaning", null, "P".repeat(501), null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/vocabulary")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vPron501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeVocabularyRequest vPos101 = new CreateKnowledgeVocabularyRequest(
                "Word", "en", "Meaning", null, null, null, "P".repeat(101), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/vocabulary")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vPos101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeVocabularyRequest vSrc501 = new CreateKnowledgeVocabularyRequest(
                "Word", "en", "Meaning", null, null, null, null, "S".repeat(501), null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/vocabulary")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vSrc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Vocabulary PUT valid maxima (word 500, pronunciation 500, partOfSpeech 100, sourceName 500) -> 200 OK
        UpdateKnowledgeVocabularyRequest putVocab500 = new UpdateKnowledgeVocabularyRequest(
                "W".repeat(500), "en", "Meaning", "Ex", "P".repeat(500), null, "N".repeat(100), "S".repeat(500), null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/vocabulary/{id}", vocabId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putVocab500)))
                .andExpect(status().isOk());

        UpdateKnowledgeVocabularyRequest putVPron501 = new UpdateKnowledgeVocabularyRequest(
                "Word", "en", "Meaning", null, "P".repeat(501), null, null, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/vocabulary/{id}", vocabId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putVPron501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeVocabularyRequest putVPos101 = new UpdateKnowledgeVocabularyRequest(
                "Word", "en", "Meaning", null, null, null, "P".repeat(101), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/vocabulary/{id}", vocabId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putVPos101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeVocabularyRequest putVSrc501 = new UpdateKnowledgeVocabularyRequest(
                "Word", "en", "Meaning", null, null, null, null, "S".repeat(501), null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/vocabulary/{id}", vocabId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putVSrc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Note create & update independent field rejections at 501
        CreateKnowledgeNoteRequest noteTitle501 = new CreateKnowledgeNoteRequest(
                "T".repeat(501), "Markdown", null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeNoteRequest noteSrc501 = new CreateKnowledgeNoteRequest(
                "Valid Title", "Markdown", null, "S".repeat(501), null, null, null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteSrc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeNoteRequest noteFile501 = new CreateKnowledgeNoteRequest(
                "Valid Title", "Markdown", null, null, null, "F".repeat(501), null, null
        );
        mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteFile501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateKnowledgeNoteRequest validNoteReq = new CreateKnowledgeNoteRequest(
                "Note Base", "Markdown", null, null, null, null, null, null
        );
        var nRes = mockMvc.perform(post("/api/v1/knowledge/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNoteReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long noteId = objectMapper.readTree(nRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        UpdateKnowledgeNoteRequest putNoteTitle501 = new UpdateKnowledgeNoteRequest(
                "T".repeat(501), "Markdown", null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putNoteTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeNoteRequest putNoteSrc501 = new UpdateKnowledgeNoteRequest(
                "Valid Title", "Markdown", null, "S".repeat(501), null, null, null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putNoteSrc501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateKnowledgeNoteRequest putNoteFile501 = new UpdateKnowledgeNoteRequest(
                "Valid Title", "Markdown", null, null, null, "F".repeat(501), null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putNoteFile501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Note PUT valid maxima (title 500, sourceName 500, importedFileName 500) -> 200 OK
        UpdateKnowledgeNoteRequest putNote500 = new UpdateKnowledgeNoteRequest(
                "T".repeat(500), "Markdown", null, "S".repeat(500), null, "F".repeat(500), null, null
        );
        mockMvc.perform(put("/api/v1/knowledge/notes/{id}", noteId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putNote500)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // 9. Collection Music Defaults & Boundaries (BA15-2 / BA15-15)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Collection music omitted version defaults to ORIGINAL and title validates 500/501 bounds")
    void collectionMusicOmittedVersionDefaultsAndBounds() throws Exception {
        // POST with version omitted / null -> defaults to ORIGINAL
        CreateMusicRequest createReq = new CreateMusicRequest("Title 1", null, null, null);
        var res = mockMvc.perform(post("/api/v1/collection/music")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.version").value("ORIGINAL"))
                .andReturn();

        long musicId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();
        String dbVersion = jdbcTemplate.queryForObject("SELECT version FROM music_tracks WHERE id = ?", String.class, musicId);
        assertThat(dbVersion).isEqualTo("ORIGINAL");

        // POST title 500 valid / 501 -> 400
        CreateMusicRequest title500 = new CreateMusicRequest("M".repeat(500), null, null, null);
        mockMvc.perform(post("/api/v1/collection/music")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(title500)))
                .andExpect(status().isCreated());

        CreateMusicRequest title501 = new CreateMusicRequest("M".repeat(501), null, null, null);
        mockMvc.perform(post("/api/v1/collection/music")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(title501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // PUT with version null -> defaults to ORIGINAL
        UpdateMusicRequest updateReq = new UpdateMusicRequest("Updated Music", null, null, null);
        mockMvc.perform(put("/api/v1/collection/music/{id}", musicId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value("ORIGINAL"));

        String updatedDbVersion = jdbcTemplate.queryForObject("SELECT version FROM music_tracks WHERE id = ?", String.class, musicId);
        assertThat(updatedDbVersion).isEqualTo("ORIGINAL");

        // PUT title 500 valid / 501 -> 400
        UpdateMusicRequest putTitle500 = new UpdateMusicRequest("U".repeat(500), null, null, null);
        mockMvc.perform(put("/api/v1/collection/music/{id}", musicId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putTitle500)))
                .andExpect(status().isOk());

        UpdateMusicRequest putTitle501 = new UpdateMusicRequest("U".repeat(501), null, null, null);
        mockMvc.perform(put("/api/v1/collection/music/{id}", musicId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putTitle501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 10. Shopping, Software, and Album Boundaries (BA15-2 / FR15-7)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-7: Shopping, Software, and Album create and update title/name 500 valid and 501 rejected")
    void shoppingSoftwareAndAlbumBoundaries() throws Exception {
        // Shopping name 500 valid / 501 -> 400
        CreateShoppingItemRequest shopReq500 = new CreateShoppingItemRequest(
                "S".repeat(500), null, null, null, null, null, null, null, null
        );
        var sRes = mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shopReq500)))
                .andExpect(status().isCreated())
                .andReturn();
        long shopId = objectMapper.readTree(sRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateShoppingItemRequest shopReq501 = new CreateShoppingItemRequest(
                "S".repeat(501), null, null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shopReq501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateShoppingItemRequest putShop500 = new UpdateShoppingItemRequest(
                "S".repeat(500), null, null, null, null, null, CollectionShoppingStatus.WISHLIST, null, null
        );
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putShop500)))
                .andExpect(status().isOk());

        UpdateShoppingItemRequest putShop501 = new UpdateShoppingItemRequest(
                "S".repeat(501), null, null, null, null, null, CollectionShoppingStatus.WISHLIST, null, null
        );
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putShop501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Software name 500 valid / 501 -> 400
        CreateSoftwareItemRequest softReq500 = new CreateSoftwareItemRequest(
                "W".repeat(500), CollectionSoftwareType.APPLICATION, null, null, null, null, null, null
        );
        var softRes = mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(softReq500)))
                .andExpect(status().isCreated())
                .andReturn();
        long softId = objectMapper.readTree(softRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateSoftwareItemRequest softReq501 = new CreateSoftwareItemRequest(
                "W".repeat(501), CollectionSoftwareType.APPLICATION, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(softReq501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateSoftwareItemRequest putSoft500 = new UpdateSoftwareItemRequest(
                "W".repeat(500), CollectionSoftwareType.APPLICATION, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putSoft500)))
                .andExpect(status().isOk());

        UpdateSoftwareItemRequest putSoft501 = new UpdateSoftwareItemRequest(
                "W".repeat(501), CollectionSoftwareType.APPLICATION, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putSoft501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Album title 500 valid / 501 -> 400
        CreateAlbumRequest album500 = new CreateAlbumRequest("A".repeat(500), "Description");
        var aRes = mockMvc.perform(post("/api/v1/albums")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(album500)))
                .andExpect(status().isCreated())
                .andReturn();
        long albumId = objectMapper.readTree(aRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateAlbumRequest album501 = new CreateAlbumRequest("A".repeat(501), "Description");
        mockMvc.perform(post("/api/v1/albums")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(album501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateAlbumRequest putAlbum500 = new UpdateAlbumRequest("A".repeat(500), "Updated Description");
        mockMvc.perform(put("/api/v1/albums/{id}", albumId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putAlbum500)))
                .andExpect(status().isOk());

        UpdateAlbumRequest putAlbum501 = new UpdateAlbumRequest("A".repeat(501), "Updated Description");
        mockMvc.perform(put("/api/v1/albums/{id}", albumId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putAlbum501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 11. Fiction/Film Genres and Location Categories Boundaries (BA15-2 / FR15-7)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-7: Fiction/Film genres and Location categories create and update name 150 valid and 151 rejected")
    void fictionFilmGenresAndLocationCategoriesBoundaries() throws Exception {
        // Fiction genre
        CreateFictionGenreRequest fGenre150 = new CreateFictionGenreRequest("F".repeat(150), "Desc");
        var fgRes = mockMvc.perform(post("/api/v1/fiction-genres")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fGenre150)))
                .andExpect(status().isCreated())
                .andReturn();
        long fgId = objectMapper.readTree(fgRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateFictionGenreRequest fGenre151 = new CreateFictionGenreRequest("F".repeat(151), "Desc");
        mockMvc.perform(post("/api/v1/fiction-genres")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fGenre151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateFictionGenreRequest putFg150 = new UpdateFictionGenreRequest("U".repeat(150), "Updated");
        mockMvc.perform(put("/api/v1/fiction-genres/{id}", fgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putFg150)))
                .andExpect(status().isOk());

        UpdateFictionGenreRequest putFg151 = new UpdateFictionGenreRequest("U".repeat(151), "Updated");
        mockMvc.perform(put("/api/v1/fiction-genres/{id}", fgId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putFg151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Film genre
        CreateFilmGenreRequest filmGenre150 = new CreateFilmGenreRequest("M".repeat(150), "Desc");
        var filmGRes = mockMvc.perform(post("/api/v1/film-genres")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmGenre150)))
                .andExpect(status().isCreated())
                .andReturn();
        long filmGId = objectMapper.readTree(filmGRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateFilmGenreRequest filmGenre151 = new CreateFilmGenreRequest("M".repeat(151), "Desc");
        mockMvc.perform(post("/api/v1/film-genres")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmGenre151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateFilmGenreRequest putFilmG150 = new UpdateFilmGenreRequest("V".repeat(150), "Updated");
        mockMvc.perform(put("/api/v1/film-genres/{id}", filmGId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putFilmG150)))
                .andExpect(status().isOk());

        UpdateFilmGenreRequest putFilmG151 = new UpdateFilmGenreRequest("V".repeat(151), "Updated");
        mockMvc.perform(put("/api/v1/film-genres/{id}", filmGId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putFilmG151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Location category
        CreateLocationCategoryRequest locCat150 = new CreateLocationCategoryRequest("L".repeat(150), "Desc");
        var locCatRes = mockMvc.perform(post("/api/v1/location-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locCat150)))
                .andExpect(status().isCreated())
                .andReturn();
        long locCatId = objectMapper.readTree(locCatRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateLocationCategoryRequest locCat151 = new CreateLocationCategoryRequest("L".repeat(151), "Desc");
        mockMvc.perform(post("/api/v1/location-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locCat151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateLocationCategoryRequest putLocCat150 = new UpdateLocationCategoryRequest("K".repeat(150), "Updated");
        mockMvc.perform(put("/api/v1/location-categories/{id}", locCatId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putLocCat150)))
                .andExpect(status().isOk());

        UpdateLocationCategoryRequest putLocCat151 = new UpdateLocationCategoryRequest("K".repeat(151), "Updated");
        mockMvc.perform(put("/api/v1/location-categories/{id}", locCatId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putLocCat151)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 12. Manual SavedResource and Import Job Creation Boundaries (BA15-2 / FR15-7)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-7: Manual SavedResource (title 1000/1001, author/sourceName/externalId 500/501) and Import job (originalFileName 500/501) boundaries")
    void manualSavedResourceAndImportJobCreationBoundaries() throws Exception {
        // Manual SavedResource title 1000 valid / 1001 -> 400
        CreateManualSavedResourceRequest title1000 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "T".repeat(1000), "https://example.com/res1", null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(title1000)))
                .andExpect(status().isCreated());

        CreateManualSavedResourceRequest title1001 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "T".repeat(1001), "https://example.com/res2", null, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(title1001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // author 500 valid / 501 -> 400
        CreateManualSavedResourceRequest author500 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "Valid Title", "https://example.com/res3", "A".repeat(500), null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author500)))
                .andExpect(status().isCreated());

        CreateManualSavedResourceRequest author501 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "Valid Title", "https://example.com/res4", "A".repeat(501), null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // sourceName 500 valid / 501 -> 400
        CreateManualSavedResourceRequest src500 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "Valid Title", "https://example.com/res5", null, null, null, "S".repeat(500), null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(src500)))
                .andExpect(status().isCreated());

        CreateManualSavedResourceRequest src501 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "Valid Title", "https://example.com/res6", null, null, null, "S".repeat(501), null, null, null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(src501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // externalId 500 valid / 501 -> 400
        CreateManualSavedResourceRequest ext500 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "Valid Title", "https://example.com/res7", null, null, null, null, null, "E".repeat(500), null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ext500)))
                .andExpect(status().isCreated());

        CreateManualSavedResourceRequest ext501 = new CreateManualSavedResourceRequest(
                SavedResourceKind.ARTICLE, "Valid Title", "https://example.com/res8", null, null, null, null, null, "E".repeat(501), null
        );
        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ext501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Import job originalFileName 500 valid / 501 -> 400
        CreateImportJobRequest job500 = new CreateImportJobRequest(
                ImportTargetType.NOTE, ImportFormat.CSV, "F".repeat(500), null
        );
        mockMvc.perform(post("/api/v1/imports/jobs")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job500)))
                .andExpect(status().isCreated());

        CreateImportJobRequest job501 = new CreateImportJobRequest(
                ImportTargetType.NOTE, ImportFormat.CSV, "F".repeat(501), null
        );
        mockMvc.perform(post("/api/v1/imports/jobs")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 13. Finance Recurring Rule, Subscription, and Transaction Boundaries (BA15-2 / FR15-7)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-7: Finance recurring rule, subscription, and transaction name/provider 500/501, description 1000/1001 and blank/null acceptance")
    void financeRecurringAndSubscriptionBoundaries() throws Exception {
        var wallet = walletOperations.createWallet(new CreateWalletCommand(
                "AuditWallet-" + System.nanoTime(),
                WalletType.BANK_ACCOUNT,
                "USD",
                java.math.BigDecimal.ZERO,
                null,
                true
        ));

        // 1. Recurring Rule: name 500 valid / 501 -> 400; description 1000 valid / 1001 -> 400; null & blank description valid
        var validEntry = new RecurringRuleEntryRequest(wallet.id(), java.math.BigDecimal.valueOf(-10));
        CreateRecurringTransactionRuleRequest recName500 = new CreateRecurringTransactionRuleRequest(
                "R".repeat(500), FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                "D".repeat(1000), null, true, null, List.of(validEntry)
        );
        var recRes = mockMvc.perform(post("/api/v1/finance/recurring-rules")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recName500)))
                .andExpect(status().isCreated())
                .andReturn();
        long recId = objectMapper.readTree(recRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateRecurringTransactionRuleRequest recName501 = new CreateRecurringTransactionRuleRequest(
                "R".repeat(501), FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                null, null, true, null, List.of(validEntry)
        );
        mockMvc.perform(post("/api/v1/finance/recurring-rules")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateRecurringTransactionRuleRequest recDesc1001 = new CreateRecurringTransactionRuleRequest(
                "Valid Name", FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                "D".repeat(1001), null, true, null, List.of(validEntry)
        );
        mockMvc.perform(post("/api/v1/finance/recurring-rules")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recDesc1001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Recurring Rule PUT: name 500 valid / 501 -> 400; description 1000 valid / 1001 -> 400; blank description valid
        UpdateRecurringTransactionRuleRequest putRec500 = new UpdateRecurringTransactionRuleRequest(
                "R".repeat(500), FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                "  ", null, true, null, List.of(validEntry)
        );
        mockMvc.perform(put("/api/v1/finance/recurring-rules/{id}", recId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putRec500)))
                .andExpect(status().isOk());

        String recDesc1000 = "D".repeat(1000);
        UpdateRecurringTransactionRuleRequest putRecDesc1000 = new UpdateRecurringTransactionRuleRequest(
                "Valid Name", FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                recDesc1000, null, true, null, List.of(validEntry)
        );
        mockMvc.perform(put("/api/v1/finance/recurring-rules/{id}", recId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putRecDesc1000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value(recDesc1000));

        UpdateRecurringTransactionRuleRequest putRecDesc1001 = new UpdateRecurringTransactionRuleRequest(
                "Valid Name", FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                "D".repeat(1001), null, true, null, List.of(validEntry)
        );
        mockMvc.perform(put("/api/v1/finance/recurring-rules/{id}", recId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putRecDesc1001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateRecurringTransactionRuleRequest putRecName501 = new UpdateRecurringTransactionRuleRequest(
                "R".repeat(501), FinancialTransactionType.EXPENSE, null, RecurringPostingMode.AUTO_POST,
                RecurrenceFrequency.DAILY, null, null, null, LocalDate.now(), null, null, null,
                null, null, true, null, List.of(validEntry)
        );
        mockMvc.perform(put("/api/v1/finance/recurring-rules/{id}", recId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putRecName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 2. Subscription: name 500 valid / 501 -> 400; provider 500 valid / 501 -> 400 on POST & PUT
        CreateSubscriptionRequest sub500 = new CreateSubscriptionRequest(
                "S".repeat(500), "P".repeat(500), java.math.BigDecimal.TEN, "USD",
                BillingCycle.MONTHLY, 1, null, LocalDate.now(), true, wallet.id(), null, null, null, true
        );
        var subRes = mockMvc.perform(post("/api/v1/finance/subscriptions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sub500)))
                .andExpect(status().isCreated())
                .andReturn();
        long subId = objectMapper.readTree(subRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateSubscriptionRequest subName501 = new CreateSubscriptionRequest(
                "S".repeat(501), "Valid Provider", java.math.BigDecimal.TEN, "USD",
                BillingCycle.MONTHLY, 1, null, LocalDate.now(), true, wallet.id(), null, null, null, true
        );
        mockMvc.perform(post("/api/v1/finance/subscriptions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        CreateSubscriptionRequest subProv501 = new CreateSubscriptionRequest(
                "Valid Name", "P".repeat(501), java.math.BigDecimal.TEN, "USD",
                BillingCycle.MONTHLY, 1, null, LocalDate.now(), true, wallet.id(), null, null, null, true
        );
        mockMvc.perform(post("/api/v1/finance/subscriptions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subProv501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateSubscriptionRequest putSub500 = new UpdateSubscriptionRequest(
                "S".repeat(500), "P".repeat(500), java.math.BigDecimal.TEN, "USD",
                BillingCycle.MONTHLY, 1, null, LocalDate.now(), true, wallet.id(), null, null, null, true
        );
        mockMvc.perform(put("/api/v1/finance/subscriptions/{id}", subId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putSub500)))
                .andExpect(status().isOk());

        UpdateSubscriptionRequest putSubName501 = new UpdateSubscriptionRequest(
                "S".repeat(501), "Provider", java.math.BigDecimal.TEN, "USD",
                BillingCycle.MONTHLY, 1, null, LocalDate.now(), true, wallet.id(), null, null, null, true
        );
        mockMvc.perform(put("/api/v1/finance/subscriptions/{id}", subId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putSubName501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateSubscriptionRequest putSubProv501 = new UpdateSubscriptionRequest(
                "Name", "P".repeat(501), java.math.BigDecimal.TEN, "USD",
                BillingCycle.MONTHLY, 1, null, LocalDate.now(), true, wallet.id(), null, null, null, true
        );
        mockMvc.perform(put("/api/v1/finance/subscriptions/{id}", subId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putSubProv501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // 3. Financial Transaction: description 1000 valid, 1001 -> 400; null & blank accepted
        var txEntry = new FinancialTransactionEntryRequest(wallet.id(), java.math.BigDecimal.valueOf(-10));
        CreateFinancialTransactionRequest txDesc1000 = new CreateFinancialTransactionRequest(
                FinancialTransactionType.EXPENSE, null, null, "D".repeat(1000), null,
                java.time.Instant.now(), java.math.BigDecimal.ONE, List.of(txEntry)
        );
        var txRes = mockMvc.perform(post("/api/v1/finance/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txDesc1000)))
                .andExpect(status().isCreated())
                .andReturn();
        long txId = objectMapper.readTree(txRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateFinancialTransactionRequest txDesc1001 = new CreateFinancialTransactionRequest(
                FinancialTransactionType.EXPENSE, null, null, "D".repeat(1001), null,
                java.time.Instant.now(), java.math.BigDecimal.ONE, List.of(txEntry)
        );
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txDesc1001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateFinancialTransactionRequest putTxBlank = new UpdateFinancialTransactionRequest(
                FinancialTransactionType.EXPENSE, null, null, "   ", null,
                java.time.Instant.now(), java.math.BigDecimal.ONE, List.of(txEntry)
        );
        mockMvc.perform(put("/api/v1/finance/transactions/{id}", txId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putTxBlank)))
                .andExpect(status().isOk());

        String putTxDesc1000Val = "D".repeat(1000);
        UpdateFinancialTransactionRequest putTxDesc1000 = new UpdateFinancialTransactionRequest(
                FinancialTransactionType.EXPENSE, null, null, putTxDesc1000Val, null,
                java.time.Instant.now(), java.math.BigDecimal.ONE, List.of(txEntry)
        );
        mockMvc.perform(put("/api/v1/finance/transactions/{id}", txId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putTxDesc1000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value(putTxDesc1000Val));

        UpdateFinancialTransactionRequest putTx1001 = new UpdateFinancialTransactionRequest(
                FinancialTransactionType.EXPENSE, null, null, "D".repeat(1001), null,
                java.time.Instant.now(), java.math.BigDecimal.ONE, List.of(txEntry)
        );
        mockMvc.perform(put("/api/v1/finance/transactions/{id}", txId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putTx1001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 14. Settings Update Boundaries and Removed Caps (BA15-2 / FR15-7)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-7: Settings update accepts values above removed caps (100/1440/8760), enforces positive bounds, and validates timezone 64/65")
    void settingsUpdateBoundariesAndRemovedCaps() throws Exception {
        // Values above removed caps (paginationSize 200 > 100, autoLock 2000 > 1440, backupInterval 10000 > 8760) -> 200 OK
        UpdateSettingsRequest validAboveCaps = new UpdateSettingsRequest(
                "Asia/Ho_Chi_Minh", "VND", 200, 2000, true, 10000
        );
        mockMvc.perform(put("/api/v1/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAboveCaps)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paginationSize").value(200))
                .andExpect(jsonPath("$.data.privateModeAutoLockMinutes").value(2000))
                .andExpect(jsonPath("$.data.backupIntervalHours").value(10000));

        // Timezone: valid timezone accepted, 65 chars rejected by @Size(max = 64)
        UpdateSettingsRequest validTz = new UpdateSettingsRequest(
                "America/Argentina/ComodRivadavia", "VND", 50, 15, true, 24
        );
        mockMvc.perform(put("/api/v1/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTz)))
                .andExpect(status().isOk());

        UpdateSettingsRequest tz65 = new UpdateSettingsRequest(
                "A".repeat(65), "VND", 50, 15, true, 24
        );
        mockMvc.perform(put("/api/v1/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tz65)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Positive bounds: 0 rejected with 400
        UpdateSettingsRequest zeroPage = new UpdateSettingsRequest(
                "Asia/Ho_Chi_Minh", "VND", 0, 15, true, 24
        );
        mockMvc.perform(put("/api/v1/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroPage)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateSettingsRequest zeroLock = new UpdateSettingsRequest(
                "Asia/Ho_Chi_Minh", "VND", 50, 0, true, 24
        );
        mockMvc.perform(put("/api/v1/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroLock)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        UpdateSettingsRequest zeroBackup = new UpdateSettingsRequest(
                "Asia/Ho_Chi_Minh", "VND", 50, 15, true, 0
        );
        mockMvc.perform(put("/api/v1/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroBackup)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // =========================================================================
    // 15. Location Name & Phone Create/Update Boundaries (BA15-2)
    // =========================================================================

    @Test
    @DisplayName("BA15-2: Location name 500/501, phone 64/65, and padded boundary validation on POST and PUT")
    void locationNameAndPhoneCreateAndUpdateBoundaries() throws Exception {
        CreateAddressRequest addrReq = new CreateAddressRequest("Loc Test Addr", "hq", "VN", "SG", "HCM", null, "123 Main St", "70000");
        var addrResult = mockMvc.perform(post("/api/v1/addresses")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addrReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long addressId = objectMapper.readTree(addrResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 500 chars name and 64 chars phone -> valid 201 Created
        String name500 = "L".repeat(500);
        String phone64 = "1".repeat(64);
        CreateLocationRequest req500 = new CreateLocationRequest(
                null, addressId, name500, null, null, phone64, null, null, null, null, null
        );
        var locResult = mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req500)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value(name500))
                .andExpect(jsonPath("$.data.phone").value(phone64))
                .andReturn();
        long locId = objectMapper.readTree(locResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Padded 500 name and padded 64 phone -> wire payload contains 506 and 70 chars -> trimmed in server compact constructor -> valid 201
        Map<String, Object> rawPaddedLocReq = Map.of(
                "addressId", addressId,
                "name", "   " + "P".repeat(500) + "   ",
                "phone", "   " + "2".repeat(64) + "   "
        );
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPaddedLocReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("P".repeat(500)))
                .andExpect(jsonPath("$.data.phone").value("2".repeat(64)));

        // 501 chars name -> 400 validation error, no database row written
        long locCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM locations", Long.class);
        long vaultCountBeforeLoc501 = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawReq501 = Map.of(
                "addressId", addressId,
                "name", "   " + "L".repeat(501) + "   "
        );
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawReq501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM locations", Long.class)).isEqualTo(locCountBefore501);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeLoc501);

        // 65 chars phone -> 400 validation error, no database row written
        long locCountBeforePhone65 = jdbcTemplate.queryForObject("SELECT count(*) FROM locations", Long.class);
        long vaultCountBeforeLocPhone65 = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawReqPhone65 = Map.of(
                "addressId", addressId,
                "name", "Valid Name",
                "phone", "1".repeat(65)
        );
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawReqPhone65)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM locations", Long.class)).isEqualTo(locCountBeforePhone65);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeLocPhone65);

        // PUT update name 500 and phone 64 -> 200 OK
        UpdateLocationRequest putReq500 = new UpdateLocationRequest(
                null, addressId, "U".repeat(500), null, null, "3".repeat(64), null, null, null, null, null
        );
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putReq500)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("U".repeat(500)))
                .andExpect(jsonPath("$.data.phone").value("3".repeat(64)));

        // PUT update padded name and phone -> wire payload contains 506 and 70 chars -> trimmed -> 200 OK
        Map<String, Object> rawPutPadded = Map.of(
                "addressId", addressId,
                "name", "   " + "Q".repeat(500) + "   ",
                "phone", "   " + "4".repeat(64) + "   "
        );
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutPadded)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Q".repeat(500)))
                .andExpect(jsonPath("$.data.phone").value("4".repeat(64)));

        // PUT 501 chars name -> 400, prior state remains unchanged
        String nameBeforePut501 = jdbcTemplate.queryForObject("SELECT name FROM locations WHERE id = ?", String.class, locId);
        String phoneBeforePut501 = jdbcTemplate.queryForObject("SELECT phone FROM locations WHERE id = ?", String.class, locId);
        Map<String, Object> rawPutReq501 = Map.of(
                "addressId", addressId,
                "name", "   " + "U".repeat(501) + "   "
        );
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutReq501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM locations WHERE id = ?", String.class, locId)).isEqualTo(nameBeforePut501);
        assertThat(jdbcTemplate.queryForObject("SELECT phone FROM locations WHERE id = ?", String.class, locId)).isEqualTo(phoneBeforePut501);

        // PUT 65 chars phone -> 400, prior state remains unchanged
        String nameBeforePutPhone65 = jdbcTemplate.queryForObject("SELECT name FROM locations WHERE id = ?", String.class, locId);
        String phoneBeforePutPhone65 = jdbcTemplate.queryForObject("SELECT phone FROM locations WHERE id = ?", String.class, locId);
        Map<String, Object> rawPutPhone65 = Map.of(
                "addressId", addressId,
                "name", "Valid Name",
                "phone", "3".repeat(65)
        );
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutPhone65)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM locations WHERE id = ?", String.class, locId)).isEqualTo(nameBeforePutPhone65);
        assertThat(jdbcTemplate.queryForObject("SELECT phone FROM locations WHERE id = ?", String.class, locId)).isEqualTo(phoneBeforePutPhone65);

        // Location Unicode blank phone (65 U+2003 characters) is normalized to null in DTO/owner -> valid 201/200
        String unicodeBlankPhone65 = "\u2003".repeat(65);
        Map<String, Object> rawUBlankPhoneReq = Map.of(
                "addressId", addressId,
                "name", "Location UBlank Phone",
                "phone", unicodeBlankPhone65
        );
        mockMvc.perform(post("/api/v1/locations")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawUBlankPhoneReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.phone").isEmpty());

        Map<String, Object> rawPutUBlankPhone = Map.of(
                "addressId", addressId,
                "name", "Location PUT UBlank Phone",
                "phone", unicodeBlankPhone65
        );
        mockMvc.perform(put("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutUBlankPhone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").isEmpty());

        mockMvc.perform(get("/api/v1/locations/{id}", locId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").isEmpty());
    }

    // =========================================================================
    // 16. Follower Snapshot File Name & Entry Display Name (BA15-2)
    // =========================================================================

    @Test
    @DisplayName("BA15-2: Follower snapshot importedFileName 500/501 and entry displayNameSnapshot 500/501 boundaries")
    void followerSnapshotFileNameAndDisplayNameBoundaries() throws Exception {
        Long platformId = jdbcTemplate.queryForObject("SELECT id FROM platforms LIMIT 1", Long.class);
        CreateExternalAccountRequest acctReq = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "snap_user_" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        var acctRes = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acctReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long accountId = objectMapper.readTree(acctRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateExternalAccountRequest targetAcctReq = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL, "snap_target_" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        var targetAcctRes = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetAcctReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long targetAccountId = objectMapper.readTree(targetAcctRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 500 filename & 500 entry displayName -> valid 201 Created
        String file500 = "F".repeat(500);
        String disp500 = "D".repeat(500);
        Map<String, Object> rawEntry500 = Map.of(
                "targetAccountId", targetAccountId,
                "usernameSnapshot", "f_uname_1",
                "displayNameSnapshot", disp500,
                "externalIdSnapshot", "f_id_1",
                "profileUrlSnapshot", "https://example.com/f1"
        );
        Map<String, Object> rawReq500 = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", file500,
                "entries", List.of(rawEntry500)
        );
        mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawReq500)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.importedFileName").value(file500));

        // Padded filename & entry displayName -> wire payload contains 506 chars each -> trimmed -> valid 201 Created
        Map<String, Object> rawPaddedEntry = Map.of(
                "targetAccountId", targetAccountId,
                "usernameSnapshot", "f_uname_2",
                "displayNameSnapshot", "   " + "P".repeat(500) + "   ",
                "externalIdSnapshot", "f_id_2",
                "profileUrlSnapshot", "https://example.com/f2"
        );
        Map<String, Object> rawPaddedReq = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", "   " + "Q".repeat(500) + "   ",
                "entries", List.of(rawPaddedEntry)
        );
        var paddedRes = mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPaddedReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.importedFileName").value("Q".repeat(500)))
                .andReturn();
        long paddedSnapshotId = objectMapper.readTree(paddedRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Read entries via HTTP to confirm normalized displayNameSnapshot
        mockMvc.perform(get("/api/v1/accounts/snapshots/{snapshotId}/entries", paddedSnapshotId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].displayNameSnapshot").value("P".repeat(500)));

        // 501 filename -> 400 validation error, no database row written
        long snapCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots", Long.class);
        long entryCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshot_entries", Long.class);
        Map<String, Object> rawReq501 = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", "   " + "F".repeat(501) + "   "
        );
        mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawReq501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots", Long.class)).isEqualTo(snapCountBefore501);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshot_entries", Long.class)).isEqualTo(entryCountBefore501);

        // 501 entry displayName -> 400 validation error, no database row written
        long snapCountBeforeBadEntry = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots", Long.class);
        long entryCountBeforeBadEntry = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshot_entries", Long.class);
        Map<String, Object> rawEntry501 = Map.of(
                "targetAccountId", targetAccountId,
                "usernameSnapshot", "f_uname_3",
                "displayNameSnapshot", "   " + "D".repeat(501) + "   "
        );
        Map<String, Object> rawReqBadEntry = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", "valid_file.json",
                "entries", List.of(rawEntry501)
        );
        mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawReqBadEntry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots", Long.class)).isEqualTo(snapCountBeforeBadEntry);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshot_entries", Long.class)).isEqualTo(entryCountBeforeBadEntry);

        // Exact historical value U+2003 in filename and displayNameSnapshot is retained and read back
        Map<String, Object> rawUEntry = new HashMap<>();
        rawUEntry.put("targetAccountId", targetAccountId);
        rawUEntry.put("usernameSnapshot", "f_uname_u");
        rawUEntry.put("displayNameSnapshot", "\u2003");
        Map<String, Object> rawUReq = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", "\u2003",
                "entries", List.of(rawUEntry)
        );
        var uRes = mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawUReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.importedFileName").value("\u2003"))
                .andReturn();
        long uSnapshotId = objectMapper.readTree(uRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/accounts/snapshots/{snapshotId}/entries", uSnapshotId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].displayNameSnapshot").value("\u2003"));

        // Conflicting duplicate historical copies (null vs U+2003) for the same target remain rejected atomically (422), no rows written
        long snapCountBeforeConflict = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots", Long.class);
        long entryCountBeforeConflict = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshot_entries", Long.class);
        Map<String, Object> conflictEntry1 = new HashMap<>();
        conflictEntry1.put("targetAccountId", targetAccountId);
        conflictEntry1.put("usernameSnapshot", "f_uname_c");
        conflictEntry1.put("displayNameSnapshot", null);
        Map<String, Object> conflictEntry2 = new HashMap<>();
        conflictEntry2.put("targetAccountId", targetAccountId);
        conflictEntry2.put("usernameSnapshot", "f_uname_c");
        conflictEntry2.put("displayNameSnapshot", "\u2003");
        Map<String, Object> rawConflictReq = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", "conflict.json",
                "entries", List.of(conflictEntry1, conflictEntry2)
        );
        mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawConflictReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_SNAPSHOT"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots", Long.class)).isEqualTo(snapCountBeforeConflict);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshot_entries", Long.class)).isEqualTo(entryCountBeforeConflict);

        // Matching duplicate historical copies (both U+2003) collapse and succeed -> produces exactly one entry with retained U+2003
        Map<String, Object> matchEntry1 = new HashMap<>();
        matchEntry1.put("targetAccountId", targetAccountId);
        matchEntry1.put("usernameSnapshot", "f_uname_m");
        matchEntry1.put("displayNameSnapshot", "\u2003");
        Map<String, Object> matchEntry2 = new HashMap<>();
        matchEntry2.put("targetAccountId", targetAccountId);
        matchEntry2.put("usernameSnapshot", "f_uname_m");
        matchEntry2.put("displayNameSnapshot", "\u2003");
        Map<String, Object> rawMatchReq = Map.of(
                "capturedAt", Instant.now().toString(),
                "source", "MANUAL",
                "reportedTotalCount", 100,
                "importedFileName", "match.json",
                "entries", List.of(matchEntry1, matchEntry2)
        );
        var matchRes = mockMvc.perform(post("/api/v1/accounts/{ownerAccountId}/snapshots", accountId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawMatchReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long matchSnapshotId = objectMapper.readTree(matchRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/accounts/snapshots/{snapshotId}/entries", matchSnapshotId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].displayNameSnapshot").value("\u2003"));
    }

    // =========================================================================
    // 17. External Account Relationship Defaults & Uncapped Note (BA15-2)
    // =========================================================================

    @Test
    @DisplayName("BA15-2: External account relationship supports optional followerStatus (nullable), followStatus defaulting to UNKNOWN, MANUAL source default, and uncapped note > 2048")
    void accountRelationshipOptionalDefaultsAndUncappedNoteBoundaries() throws Exception {
        Long platformId = jdbcTemplate.queryForObject("SELECT id FROM platforms LIMIT 1", Long.class);
        CreateExternalAccountRequest ownerReq = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.OWNED, ExternalAccountType.SOCIAL, "rel_owner_" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        var ownerRes = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long ownerId = objectMapper.readTree(ownerRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        CreateExternalAccountRequest targetReq = new CreateExternalAccountRequest(
                platformId, ExternalAccountOwnership.TRACKED, ExternalAccountType.SOCIAL, "rel_target_" + System.nanoTime(), null, null, null, null, null, null, null, null
        );
        var targetRes = mockMvc.perform(post("/api/v1/accounts")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(targetReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long targetId = objectMapper.readTree(targetRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Null followerStatus, null followStatus, null source, note with length 2500 (> 2048) -> 200 OK
        String note2500 = "N".repeat(2500);
        Map<String, Object> rawRelReq = new HashMap<>();
        rawRelReq.put("followerStatus", null);
        rawRelReq.put("source", null);
        rawRelReq.put("followStatus", null);
        rawRelReq.put("hasLikedPost", null);
        rawRelReq.put("note", note2500);
        mockMvc.perform(put("/api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId}", ownerId, targetId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawRelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerStatus").isEmpty())
                .andExpect(jsonPath("$.data.followStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.source").value("MANUAL"))
                .andExpect(jsonPath("$.data.note").value(note2500));

        // GET confirms persisted defaults and full 2500-character note
        mockMvc.perform(get("/api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId}", ownerId, targetId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerStatus").isEmpty())
                .andExpect(jsonPath("$.data.followStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.source").value("MANUAL"))
                .andExpect(jsonPath("$.data.note").value(note2500));

        // Blank note is normalized to null
        Map<String, Object> rawBlankNoteReq = Map.of(
                "followerStatus", "CURRENT_FOLLOWER",
                "source", "IMPORT",
                "followStatus", "FOLLOWED",
                "hasLikedPost", true,
                "note", "    "
        );
        mockMvc.perform(put("/api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId}", ownerId, targetId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawBlankNoteReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerStatus").value("CURRENT_FOLLOWER"))
                .andExpect(jsonPath("$.data.followStatus").value("FOLLOWED"))
                .andExpect(jsonPath("$.data.source").value("MANUAL"))
                .andExpect(jsonPath("$.data.note").isEmpty());

        mockMvc.perform(get("/api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId}", ownerId, targetId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.note").isEmpty());

        // Exact historical note U+2003 is retained
        Map<String, Object> rawUNoteReq = Map.of(
                "followerStatus", "CURRENT_FOLLOWER",
                "source", "IMPORT",
                "followStatus", "FOLLOWED",
                "hasLikedPost", true,
                "note", "\u2003"
        );
        mockMvc.perform(put("/api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId}", ownerId, targetId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawUNoteReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.note").value("\u2003"));

        mockMvc.perform(get("/api/v1/accounts/{ownerAccountId}/relationships/{targetAccountId}", ownerId, targetId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.note").value("\u2003"));
    }

    // =========================================================================
    // 18. Feed Source Optional Feed URL (BA15-2)
    // =========================================================================

    @Test
    @DisplayName("BA15-2: Feed source feedUrl is optional on POST and PUT, normalizing blank strings to null")
    void feedSourceOptionalUrlBoundaries() throws Exception {
        // POST with null feedUrl -> 201 Created
        Map<String, Object> rawNullUrlReq = new HashMap<>();
        rawNullUrlReq.put("name", "Feed No URL " + System.nanoTime());
        rawNullUrlReq.put("type", "RSS");
        rawNullUrlReq.put("feedUrl", null);
        rawNullUrlReq.put("enabled", true);
        rawNullUrlReq.put("scheduledRefreshEnabled", false);
        rawNullUrlReq.put("refreshIntervalMinutes", 60);

        var postRes = mockMvc.perform(post("/api/v1/feed/sources")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawNullUrlReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty())
                .andReturn();
        long sourceId = objectMapper.readTree(postRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // POST with blank feedUrl -> normalized to null, 201 Created
        Map<String, Object> rawBlankUrlReq = Map.of(
                "name", "Feed Blank URL " + System.nanoTime(),
                "type", "RSS",
                "feedUrl", "   ",
                "enabled", true,
                "scheduledRefreshEnabled", false,
                "refreshIntervalMinutes", 60
        );
        mockMvc.perform(post("/api/v1/feed/sources")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawBlankUrlReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty());

        // PUT with blank feedUrl -> normalized to null, 200 OK
        Map<String, Object> rawPutBlankUrl = Map.of(
                "name", "Feed Updated Name",
                "type", "RSS",
                "sourceUrl", "https://site.updated.com",
                "feedUrl", "   ",
                "enabled", true,
                "scheduledRefreshEnabled", false,
                "refreshIntervalMinutes", 60
        );
        mockMvc.perform(put("/api/v1/feed/sources/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutBlankUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty());

        mockMvc.perform(get("/api/v1/feed/sources/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty());

        // Oversized Unicode blank URL (2,049 U+2003 characters) -> 400 VALIDATION_ERROR on POST and PUT, no rows modified
        String oversizedBlankUrl2049 = "\u2003".repeat(2049);
        long feedCountBeforeOverPost = jdbcTemplate.queryForObject("SELECT count(*) FROM feed_sources", Long.class);
        Map<String, Object> rawOverPostReq = Map.of(
                "name", "Feed Over URL",
                "type", "RSS",
                "feedUrl", oversizedBlankUrl2049,
                "enabled", true,
                "scheduledRefreshEnabled", false,
                "refreshIntervalMinutes", 60
        );
        mockMvc.perform(post("/api/v1/feed/sources")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawOverPostReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM feed_sources", Long.class)).isEqualTo(feedCountBeforeOverPost);

        String feedNameBeforeOverPut = jdbcTemplate.queryForObject("SELECT name FROM feed_sources WHERE id = ?", String.class, sourceId);
        String feedUrlBeforeOverPut = jdbcTemplate.queryForObject("SELECT feed_url FROM feed_sources WHERE id = ?", String.class, sourceId);
        Map<String, Object> rawOverPutReq = Map.of(
                "name", "Feed Over PUT URL",
                "type", "RSS",
                "feedUrl", oversizedBlankUrl2049,
                "enabled", true,
                "scheduledRefreshEnabled", false,
                "refreshIntervalMinutes", 60
        );
        mockMvc.perform(put("/api/v1/feed/sources/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawOverPutReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM feed_sources WHERE id = ?", String.class, sourceId)).isEqualTo(feedNameBeforeOverPut);
        assertThat(jdbcTemplate.queryForObject("SELECT feed_url FROM feed_sources WHERE id = ?", String.class, sourceId)).isEqualTo(feedUrlBeforeOverPut);

        // Short Unicode blank URL (5 U+2003 characters) is <= 2048 and normalized to null by owner -> 201/200 OK
        String shortBlankUrl5 = "\u2003".repeat(5);
        Map<String, Object> rawShortPostReq = Map.of(
                "name", "Feed Short UBlank " + System.nanoTime(),
                "type", "RSS",
                "feedUrl", shortBlankUrl5,
                "enabled", true,
                "scheduledRefreshEnabled", false,
                "refreshIntervalMinutes", 60
        );
        mockMvc.perform(post("/api/v1/feed/sources")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShortPostReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty());

        Map<String, Object> rawShortPutReq = Map.of(
                "name", "Feed Short PUT UBlank",
                "type", "RSS",
                "feedUrl", shortBlankUrl5,
                "enabled", true,
                "scheduledRefreshEnabled", false,
                "refreshIntervalMinutes", 60
        );
        mockMvc.perform(put("/api/v1/feed/sources/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShortPutReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty());

        mockMvc.perform(get("/api/v1/feed/sources/{id}", sourceId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedUrl").isEmpty());
    }

    // =========================================================================
    // 19. Personal Profile Freeform Email & Blank Nationality (BA15-2)
    // =========================================================================

    @Test
    @DisplayName("BA15-2: Personal profile accepts free-form email up to 320 chars and normalizes blank nationality code")
    void personalProfileFreeFormEmailAndNationalityBoundaries() throws Exception {
        // Free-form email up to 320 characters without RFC strict validation
        String freeFormEmail320 = "user.name+tag#special" + "a".repeat(320 - 21);
        assertThat(freeFormEmail320.length()).isEqualTo(320);

        // Blank nationality normalized to null -> 201 Created
        Map<String, Object> rawCreateReq = Map.of(
                "name", "Freeform Profile",
                "relationship", "Friend",
                "isSelf", false,
                "nationalityCode", "   ",
                "email", freeFormEmail320
        );
        var postRes = mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawCreateReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(freeFormEmail320))
                .andExpect(jsonPath("$.data.nationalityCode").isEmpty())
                .andReturn();
        long profileId = objectMapper.readTree(postRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // PUT with valid nationality -> 200 OK
        Map<String, Object> rawPutNat = Map.of(
                "name", "Updated Profile",
                "relationship", "Friend",
                "isSelf", false,
                "nationalityCode", "VN",
                "email", freeFormEmail320
        );
        mockMvc.perform(put("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutNat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nationalityCode").value("VN"));

        mockMvc.perform(get("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nationalityCode").value("VN"));

        // PUT with blank nationality -> normalized to null, 200 OK
        Map<String, Object> rawPutBlankNat = Map.of(
                "name", "Updated Profile 2",
                "relationship", "Friend",
                "isSelf", false,
                "nationalityCode", "   ",
                "email", freeFormEmail320
        );
        mockMvc.perform(put("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawPutBlankNat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nationalityCode").isEmpty());

        mockMvc.perform(get("/api/v1/personal/profiles/{id}", profileId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nationalityCode").isEmpty());

        // Nationality code length > 2 -> 400 validation error, no rows written
        long profileCountBeforeBadLen = jdbcTemplate.queryForObject("SELECT count(*) FROM personal_profiles", Long.class);
        Map<String, Object> rawBadNatLen = Map.of(
                "name", "Bad Nat Len",
                "relationship", "Friend",
                "isSelf", false,
                "nationalityCode", "VNN"
        );
        mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawBadNatLen)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM personal_profiles", Long.class)).isEqualTo(profileCountBeforeBadLen);

        // Nationality code length 2 but invalid country -> 422, no rows written
        long profileCountBeforeInvalid = jdbcTemplate.queryForObject("SELECT count(*) FROM personal_profiles", Long.class);
        Map<String, Object> rawInvalidCountry = Map.of(
                "name", "Invalid Country",
                "relationship", "Friend",
                "isSelf", false,
                "nationalityCode", "ZZ"
        );
        mockMvc.perform(post("/api/v1/personal/profiles")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawInvalidCountry)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_PERSONAL_PROFILE"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM personal_profiles", Long.class)).isEqualTo(profileCountBeforeInvalid);
    }

    // =========================================================================
    // 20. Shopping, Software, and Feed Conversion Currency & Padded Identity (BA15-2)
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / FR15-10 / FR15-11: Shopping, Software, and Feed ConvertToStudy currency normalization, padded identity boundaries, and PUT/persistence controls")
    void shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity() throws Exception {
        // 1. Shopping:
        // Padded name max 500 (506 chars raw) and blank currency without price -> valid 201 Created
        Map<String, Object> rawShopBlankCurr = new HashMap<>();
        rawShopBlankCurr.put("name", "   " + "S".repeat(500) + "   ");
        rawShopBlankCurr.put("currencyCode", "   ");
        rawShopBlankCurr.put("status", "WISHLIST");

        var sRes = mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopBlankCurr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("S".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty())
                .andReturn();
        long shopId = objectMapper.readTree(sRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Padded name max+1 (507 chars raw -> 501 trimmed) -> 400 VALIDATION_ERROR, no row written
        long shopCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM shopping_items", Long.class);
        long vaultCountBeforeShop501 = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawShopOverName = Map.of(
                "name", "   " + "S".repeat(501) + "   ",
                "status", "WISHLIST"
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopOverName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM shopping_items", Long.class)).isEqualTo(shopCountBefore501);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeShop501);

        // Unknown Unicode currency without price ("\u2003\u2003\u2003") -> retained by trimOrNull -> 422 INVALID_COLLECTION, no writes
        long shopCountBeforeUnicodeCurr = jdbcTemplate.queryForObject("SELECT count(*) FROM shopping_items", Long.class);
        long vaultCountBeforeShopUnicodeCurr = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawShopUnicodeCurr = Map.of(
                "name", "Shopping Unicode Curr",
                "currencyCode", "\u2003\u2003\u2003",
                "status", "WISHLIST"
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopUnicodeCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM shopping_items", Long.class)).isEqualTo(shopCountBeforeUnicodeCurr);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeShopUnicodeCurr);

        // Padded valid currency is trimmed and accepted -> 201 Created
        Map<String, Object> rawShopPaddedCurr = Map.of(
                "name", "Shopping Padded Curr",
                "currencyCode", " VND ",
                "status", "WISHLIST"
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPaddedCurr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Price with blank currency -> 422 INVALID_COLLECTION, no writes
        long shopCountBeforePriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM shopping_items", Long.class);
        long vaultCountBeforeShopPriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawShopPriceBlankCurr = Map.of(
                "name", "Shopping Price Blank Curr",
                "priceAmount", new BigDecimal("10.00"),
                "currencyCode", "   ",
                "status", "WISHLIST"
        );
        mockMvc.perform(post("/api/v1/collection/shopping")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPriceBlankCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM shopping_items", Long.class)).isEqualTo(shopCountBeforePriceBlank);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeShopPriceBlank);

        // Shopping PUT controls on shopId:
        // Seed non-null valid currency ("VND") on shopId
        Map<String, Object> rawShopSeed = new HashMap<>();
        rawShopSeed.put("name", "Shopping Item Baseline");
        rawShopSeed.put("currencyCode", " VND ");
        rawShopSeed.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopSeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Non-null -> null clearing 1: Send currencyCode as null
        Map<String, Object> rawShopPutNull = new HashMap<>();
        rawShopPutNull.put("name", "Shopping Clear Null");
        rawShopPutNull.put("currencyCode", null);
        rawShopPutNull.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPutNull)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Re-seed non-null valid currency
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopSeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Non-null -> null clearing 2: Send single ASCII space " "
        Map<String, Object> rawShopPutSingleSpace = new HashMap<>();
        rawShopPutSingleSpace.put("name", "Shopping Clear Single Space");
        rawShopPutSingleSpace.put("currencyCode", " ");
        rawShopPutSingleSpace.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPutSingleSpace)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Re-seed non-null valid currency
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopSeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Non-null -> null clearing 3: Send multiple ASCII spaces "   " with padded name 500 (506 raw)
        Map<String, Object> rawShopPutBlank = new HashMap<>();
        rawShopPutBlank.put("name", "   " + "U".repeat(500) + "   ");
        rawShopPutBlank.put("currencyCode", "   ");
        rawShopPutBlank.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPutBlank)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("U".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("U".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Seed definitive baseline state for negative PUT assertions: name="Shopping Baseline", price=15.00, currency="VND"
        Map<String, Object> rawShopBaseline = new HashMap<>();
        rawShopBaseline.put("name", "Shopping Baseline");
        rawShopBaseline.put("priceAmount", new BigDecimal("15.00"));
        rawShopBaseline.put("currencyCode", "VND");
        rawShopBaseline.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopBaseline)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shopping Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(15.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Capture baseline state before negative PUTs
        String shopNameBefore = jdbcTemplate.queryForObject("SELECT name FROM shopping_items WHERE id = ?", String.class, shopId);
        BigDecimal shopPriceBefore = jdbcTemplate.queryForObject("SELECT price_amount FROM shopping_items WHERE id = ?", BigDecimal.class, shopId);
        String shopCurrBefore = jdbcTemplate.queryForObject("SELECT currency_code FROM shopping_items WHERE id = ?", String.class, shopId);

        // Negative PUT 1: Padded name 501 (507 raw -> 501 trimmed) -> 400 VALIDATION_ERROR, no writes
        Map<String, Object> rawShopPutOverName = new HashMap<>();
        rawShopPutOverName.put("name", "   " + "U".repeat(501) + "   ");
        rawShopPutOverName.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPutOverName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Verify fresh GET and DB state unchanged
        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shopping Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(15.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM shopping_items WHERE id = ?", String.class, shopId)).isEqualTo(shopNameBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT price_amount FROM shopping_items WHERE id = ?", BigDecimal.class, shopId)).isEqualByComparingTo(shopPriceBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT currency_code FROM shopping_items WHERE id = ?", String.class, shopId)).isEqualTo(shopCurrBefore);

        // Negative PUT 2: Price present and blank currency -> 422 INVALID_COLLECTION, no writes
        Map<String, Object> rawShopPutPriceNoCurr = new HashMap<>();
        rawShopPutPriceNoCurr.put("name", "Shopping Price No Curr");
        rawShopPutPriceNoCurr.put("priceAmount", new BigDecimal("19.99"));
        rawShopPutPriceNoCurr.put("currencyCode", "   ");
        rawShopPutPriceNoCurr.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPutPriceNoCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shopping Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(15.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM shopping_items WHERE id = ?", String.class, shopId)).isEqualTo(shopNameBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT price_amount FROM shopping_items WHERE id = ?", BigDecimal.class, shopId)).isEqualByComparingTo(shopPriceBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT currency_code FROM shopping_items WHERE id = ?", String.class, shopId)).isEqualTo(shopCurrBefore);

        // Negative PUT 3: Unknown Unicode currency without price -> 422 INVALID_COLLECTION, no writes
        Map<String, Object> rawShopPutUnicodeCurr = new HashMap<>();
        rawShopPutUnicodeCurr.put("name", "Shopping PUT Unicode Curr");
        rawShopPutUnicodeCurr.put("currencyCode", "\u2003\u2003\u2003");
        rawShopPutUnicodeCurr.put("status", "WISHLIST");
        mockMvc.perform(put("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawShopPutUnicodeCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        mockMvc.perform(get("/api/v1/collection/shopping/{id}", shopId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shopping Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(15.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM shopping_items WHERE id = ?", String.class, shopId)).isEqualTo(shopNameBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT price_amount FROM shopping_items WHERE id = ?", BigDecimal.class, shopId)).isEqualByComparingTo(shopPriceBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT currency_code FROM shopping_items WHERE id = ?", String.class, shopId)).isEqualTo(shopCurrBefore);

        // 2. Software:
        // Padded name max 500 (506 chars raw) and blank currency without price -> valid 201 Created
        Map<String, Object> rawSoftBlankCurr = new HashMap<>();
        rawSoftBlankCurr.put("name", "   " + "W".repeat(500) + "   ");
        rawSoftBlankCurr.put("type", "APPLICATION");
        rawSoftBlankCurr.put("currencyCode", "   ");

        var wRes = mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftBlankCurr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("W".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty())
                .andReturn();
        long softId = objectMapper.readTree(wRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Padded name max+1 (507 chars raw -> 501 trimmed) -> 400 VALIDATION_ERROR, no writes
        long softCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM software_items", Long.class);
        long vaultCountBeforeSoft501 = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawSoftOverName = Map.of(
                "name", "   " + "W".repeat(501) + "   ",
                "type", "APPLICATION"
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftOverName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM software_items", Long.class)).isEqualTo(softCountBefore501);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeSoft501);

        // Unknown Unicode currency without price ("\u2003\u2003\u2003") -> 422 INVALID_COLLECTION, no writes
        long softCountBeforeUnicodeCurr = jdbcTemplate.queryForObject("SELECT count(*) FROM software_items", Long.class);
        long vaultCountBeforeSoftUnicodeCurr = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawSoftUnicodeCurr = Map.of(
                "name", "Software Unicode Curr",
                "type", "APPLICATION",
                "currencyCode", "\u2003\u2003\u2003"
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftUnicodeCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM software_items", Long.class)).isEqualTo(softCountBeforeUnicodeCurr);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeSoftUnicodeCurr);

        // Padded valid currency is trimmed and accepted -> 201 Created
        Map<String, Object> rawSoftPaddedCurr = Map.of(
                "name", "Software Padded Curr",
                "type", "APPLICATION",
                "currencyCode", " USD "
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPaddedCurr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currencyCode").value("USD"));

        // Price with blank currency -> 422 INVALID_COLLECTION, no writes
        long softCountBeforePriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM software_items", Long.class);
        long vaultCountBeforeSoftPriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        Map<String, Object> rawSoftPriceBlankCurr = Map.of(
                "name", "Software Price Blank Curr",
                "type", "APPLICATION",
                "priceAmount", new BigDecimal("29.99"),
                "currencyCode", "   "
        );
        mockMvc.perform(post("/api/v1/collection/software")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPriceBlankCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM software_items", Long.class)).isEqualTo(softCountBeforePriceBlank);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeSoftPriceBlank);

        // Software PUT controls on softId:
        // Seed non-null valid currency ("VND") on softId
        Map<String, Object> rawSoftSeed = new HashMap<>();
        rawSoftSeed.put("name", "Software Item Baseline");
        rawSoftSeed.put("type", "APPLICATION");
        rawSoftSeed.put("currencyCode", " VND ");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftSeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Non-null -> null clearing 1: Send currencyCode as null
        Map<String, Object> rawSoftPutNull = new HashMap<>();
        rawSoftPutNull.put("name", "Software Clear Null");
        rawSoftPutNull.put("type", "APPLICATION");
        rawSoftPutNull.put("currencyCode", null);
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPutNull)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        mockMvc.perform(get("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Re-seed non-null valid currency
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftSeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Non-null -> null clearing 2: Send single ASCII space " "
        Map<String, Object> rawSoftPutSingleSpace = new HashMap<>();
        rawSoftPutSingleSpace.put("name", "Software Clear Single Space");
        rawSoftPutSingleSpace.put("type", "APPLICATION");
        rawSoftPutSingleSpace.put("currencyCode", " ");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPutSingleSpace)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        mockMvc.perform(get("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Re-seed non-null valid currency
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftSeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Non-null -> null clearing 3: Send multiple ASCII spaces "   " with padded name 500 (506 raw)
        Map<String, Object> rawSoftPutBlank = new HashMap<>();
        rawSoftPutBlank.put("name", "   " + "V".repeat(500) + "   ");
        rawSoftPutBlank.put("type", "APPLICATION");
        rawSoftPutBlank.put("currencyCode", "   ");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPutBlank)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("V".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        mockMvc.perform(get("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("V".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Seed definitive baseline state for negative PUT assertions: name="Software Baseline", price=25.00, currency="VND"
        Map<String, Object> rawSoftBaseline = new HashMap<>();
        rawSoftBaseline.put("name", "Software Baseline");
        rawSoftBaseline.put("type", "APPLICATION");
        rawSoftBaseline.put("priceAmount", new BigDecimal("25.00"));
        rawSoftBaseline.put("currencyCode", "VND");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftBaseline)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Software Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(25.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));

        // Capture baseline state before negative PUTs
        String softNameBefore = jdbcTemplate.queryForObject("SELECT name FROM software_items WHERE id = ?", String.class, softId);
        BigDecimal softPriceBefore = jdbcTemplate.queryForObject("SELECT price_amount FROM software_items WHERE id = ?", BigDecimal.class, softId);
        String softCurrBefore = jdbcTemplate.queryForObject("SELECT currency_code FROM software_items WHERE id = ?", String.class, softId);

        // Negative PUT 1: Padded name 501 (507 raw -> 501 trimmed) -> 400 VALIDATION_ERROR, no writes
        Map<String, Object> rawSoftPutOverName = new HashMap<>();
        rawSoftPutOverName.put("name", "   " + "V".repeat(501) + "   ");
        rawSoftPutOverName.put("type", "APPLICATION");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPutOverName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Verify fresh GET and DB state unchanged
        mockMvc.perform(get("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Software Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(25.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM software_items WHERE id = ?", String.class, softId)).isEqualTo(softNameBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT price_amount FROM software_items WHERE id = ?", BigDecimal.class, softId)).isEqualByComparingTo(softPriceBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT currency_code FROM software_items WHERE id = ?", String.class, softId)).isEqualTo(softCurrBefore);

        // Negative PUT 2: Price present and blank currency -> 422 INVALID_COLLECTION, no writes
        Map<String, Object> rawSoftPutPriceNoCurr = new HashMap<>();
        rawSoftPutPriceNoCurr.put("name", "Software Price No Curr");
        rawSoftPutPriceNoCurr.put("type", "APPLICATION");
        rawSoftPutPriceNoCurr.put("priceAmount", new BigDecimal("39.99"));
        rawSoftPutPriceNoCurr.put("currencyCode", "   ");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPutPriceNoCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        mockMvc.perform(get("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Software Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(25.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM software_items WHERE id = ?", String.class, softId)).isEqualTo(softNameBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT price_amount FROM software_items WHERE id = ?", BigDecimal.class, softId)).isEqualByComparingTo(softPriceBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT currency_code FROM software_items WHERE id = ?", String.class, softId)).isEqualTo(softCurrBefore);

        // Negative PUT 3: Unknown Unicode currency without price -> 422 INVALID_COLLECTION, no writes
        Map<String, Object> rawSoftPutUnicodeCurr = new HashMap<>();
        rawSoftPutUnicodeCurr.put("name", "Software PUT Unicode Curr");
        rawSoftPutUnicodeCurr.put("type", "APPLICATION");
        rawSoftPutUnicodeCurr.put("currencyCode", "\u2003\u2003\u2003");
        mockMvc.perform(put("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawSoftPutUnicodeCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_COLLECTION"));

        mockMvc.perform(get("/api/v1/collection/software/{id}", softId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Software Baseline"))
                .andExpect(jsonPath("$.data.priceAmount").value(25.00))
                .andExpect(jsonPath("$.data.currencyCode").value("VND"));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM software_items WHERE id = ?", String.class, softId)).isEqualTo(softNameBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT price_amount FROM software_items WHERE id = ?", BigDecimal.class, softId)).isEqualByComparingTo(softPriceBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT currency_code FROM software_items WHERE id = ?", String.class, softId)).isEqualTo(softCurrBefore);

        // 3. ConvertToStudy:
        // Padded title max 500 (506 raw) and blank currency without price -> valid 201, read back via GET /api/v1/knowledge/study/{id}
        Long resId = createTestSavedResource("Feed Resource Currency Test");
        Map<String, Object> rawStudyBlankCurr = new HashMap<>();
        rawStudyBlankCurr.put("title", "   " + "T".repeat(500) + "   ");
        rawStudyBlankCurr.put("type", "BOOK");
        rawStudyBlankCurr.put("currencyCode", "   ");
        var convRes1 = mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawStudyBlankCurr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetVaultEntryId").isNumber())
                .andReturn();
        long studyTargetId1 = objectMapper.readTree(convRes1.getResponse().getContentAsString()).path("data").path("targetVaultEntryId").asLong();

        // Read converted Study via public HTTP capability: asserts normalized title and cleared currency
        mockMvc.perform(get("/api/v1/knowledge/study/{id}", studyTargetId1)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("T".repeat(500)))
                .andExpect(jsonPath("$.data.currencyCode").isEmpty());

        // Padded title max+1 (507 raw -> 501 trimmed) -> 400 VALIDATION_ERROR, no writes to study_items, vault_entries, saved_resource_conversions
        long studyCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Long.class);
        long vaultCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        long convCountBefore501 = jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Long.class);
        Map<String, Object> rawStudyOverTitle = Map.of(
                "title", "   " + "T".repeat(501) + "   ",
                "type", "BOOK"
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawStudyOverTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Long.class)).isEqualTo(studyCountBefore501);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBefore501);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Long.class)).isEqualTo(convCountBefore501);

        // Unknown Unicode currency without price ("\u2003\u2003\u2003") -> 422 INVALID_KNOWLEDGE_ITEM, no writes
        Long resIdUnicode = createTestSavedResource("Feed Resource Unicode Curr Test");
        long studyCountBeforeUnicode = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Long.class);
        long vaultCountBeforeUnicode = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        long convCountBeforeUnicode = jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Long.class);
        Map<String, Object> rawStudyUnicodeCurr = Map.of(
                "title", "Study Unicode Curr",
                "type", "BOOK",
                "currencyCode", "\u2003\u2003\u2003"
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resIdUnicode)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawStudyUnicodeCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_KNOWLEDGE_ITEM"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Long.class)).isEqualTo(studyCountBeforeUnicode);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforeUnicode);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Long.class)).isEqualTo(convCountBeforeUnicode);

        // Padded valid currency is trimmed and accepted -> 201 Created, read back asserts "USD"
        Long resId2 = createTestSavedResource("Feed Resource Currency Test 2");
        Map<String, Object> rawStudyPaddedCurr = Map.of(
                "title", "Study Padded Curr",
                "type", "BOOK",
                "currencyCode", " USD "
        );
        var convRes2 = mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawStudyPaddedCurr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetVaultEntryId").isNumber())
                .andReturn();
        long studyTargetId2 = objectMapper.readTree(convRes2.getResponse().getContentAsString()).path("data").path("targetVaultEntryId").asLong();

        mockMvc.perform(get("/api/v1/knowledge/study/{id}", studyTargetId2)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currencyCode").value("USD"));

        // Price with blank currency -> 422 INVALID_KNOWLEDGE_ITEM, no writes
        Long resId3 = createTestSavedResource("Feed Resource Currency Test 3");
        long studyCountBeforePriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Long.class);
        long vaultCountBeforePriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class);
        long convCountBeforePriceBlank = jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Long.class);
        Map<String, Object> rawStudyPriceBlankCurr = Map.of(
                "title", "Study Price Blank Curr",
                "type", "BOOK",
                "priceAmount", new BigDecimal("49.99"),
                "currencyCode", "   "
        );
        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId3)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawStudyPriceBlankCurr)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_KNOWLEDGE_ITEM"));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Long.class)).isEqualTo(studyCountBeforePriceBlank);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Long.class)).isEqualTo(vaultCountBeforePriceBlank);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Long.class)).isEqualTo(convCountBeforePriceBlank);
    }

    // =========================================================================
    // 21. OpenAPI Generated Schema Semantic Assertions
    // =========================================================================

    @Test
    @DisplayName("BA15-2 / BA15-15: Generated OpenAPI schema semantics confirm field maxima, defaults, and corrected descriptions")
    void openApiGeneratedSchemaSemanticsConfirmCorrectedContracts() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(json);
        JsonNode schemas = root.path("components").path("schemas");

        // --- Feed conversion schemas maxima ---
        JsonNode studyConvSchema = schemas.path("ConvertToStudyRequest");
        assertThat(studyConvSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(studyConvSchema.path("properties").path("currentProgressText").path("maxLength").asInt()).isEqualTo(500);

        JsonNode infoConvSchema = schemas.path("ConvertToInformationRequest");
        assertThat(infoConvSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(infoConvSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);

        JsonNode noteConvSchema = schemas.path("ConvertToNoteRequest");
        assertThat(noteConvSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(noteConvSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(noteConvSchema.path("properties").path("importedFileName").path("maxLength").asInt()).isEqualTo(500);

        // --- Journal Diary Entry schema maxima ---
        JsonNode diaryCreateSchema = schemas.path("CreateDiaryEntryRequest");
        assertThat(diaryCreateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);

        // --- Knowledge Note schema maxima ---
        JsonNode noteCreateSchema = schemas.path("CreateKnowledgeNoteRequest");
        assertThat(noteCreateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(noteCreateSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(noteCreateSchema.path("properties").path("importedFileName").path("maxLength").asInt()).isEqualTo(500);

        // --- Finance transaction schema descriptions ---
        JsonNode finTxCreateSchema = schemas.path("CreateFinancialTransactionRequest");
        String typeDesc = finTxCreateSchema.path("properties").path("type").path("description").asText();
        assertThat(typeDesc).contains("TRANSFER (must not have category; two distinct wallets with opposite signs)");

        String statusDesc = finTxCreateSchema.path("properties").path("status").path("description").asText();
        assertThat(statusDesc).contains("defaults to POSTED if omitted");

        String categoryDesc = finTxCreateSchema.path("properties").path("categoryId").path("description").asText();
        assertThat(categoryDesc).contains("optional for EXPENSE and INCOME; must be null for TRANSFER");

        String entriesDesc = finTxCreateSchema.path("properties").path("entries").path("description").asText();
        assertThat(entriesDesc).contains("distinct wallets (one negative source, one positive destination)");

        // --- Collection Shopping schema ---
        JsonNode shopCreateSchema = schemas.path("CreateShoppingItemRequest");
        assertThat(shopCreateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(shopCreateSchema.path("properties").has("purchasedAt")).isTrue();

        JsonNode shopUpdateSchema = schemas.path("UpdateShoppingItemRequest");
        assertThat(shopUpdateSchema.path("description").asText())
                .contains("Full replacement update for a shopping item. Nullable fields set to null or omitted will be cleared.");
        assertThat(shopUpdateSchema.path("properties").path("status").path("description").asText())
                .contains("Defaults to WISHLIST if omitted. If changed to WISHLIST, purchasedAt must be null.");
        assertThat(shopUpdateSchema.path("properties").path("purchasedAt").path("description").asText())
                .contains("Must be null if status is WISHLIST");
        assertThat(shopUpdateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);

        // --- Collection Software schema ---
        JsonNode softCreateSchema = schemas.path("CreateSoftwareItemRequest");
        assertThat(softCreateSchema.path("required").toString()).contains("\"type\"");
        JsonNode createTypeProp = softCreateSchema.path("properties").path("type");
        assertThat(createTypeProp.path("description").asText())
                .contains("Software type: APPLICATION or EXTENSION");
        List<String> createTypeEnums = new ArrayList<>();
        createTypeProp.path("enum").forEach(e -> createTypeEnums.add(e.asText()));
        assertThat(createTypeEnums).containsExactlyInAnyOrder("APPLICATION", "EXTENSION");
        assertThat(createTypeProp.has("default")).isFalse();
        assertThat(softCreateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);

        JsonNode softUpdateSchema = schemas.path("UpdateSoftwareItemRequest");
        assertThat(softUpdateSchema.path("description").asText())
                .contains("Full replacement update for a software item. Nullable fields omitted or null will be cleared.");
        assertThat(softUpdateSchema.path("required").toString()).contains("\"type\"");
        JsonNode updateTypeProp = softUpdateSchema.path("properties").path("type");
        assertThat(updateTypeProp.path("description").asText())
                .contains("Software type: APPLICATION or EXTENSION. Required.");
        List<String> updateTypeEnums = new ArrayList<>();
        updateTypeProp.path("enum").forEach(e -> updateTypeEnums.add(e.asText()));
        assertThat(updateTypeEnums).containsExactlyInAnyOrder("APPLICATION", "EXTENSION");
        assertThat(updateTypeProp.has("default")).isFalse();
        assertThat(softUpdateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);

        // --- External Account schemas ---
        JsonNode acctCreateSchema = schemas.path("CreateExternalAccountRequest");
        assertThat(acctCreateSchema.path("required").toString()).contains("platformId", "ownership", "accountType");
        assertThat(acctCreateSchema.path("required").toString()).doesNotContain("username", "externalId", "url");
        assertThat(acctCreateSchema.path("properties").path("displayName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(acctCreateSchema.path("properties").path("ownerName").path("maxLength").asInt()).isEqualTo(500);

        JsonNode acctUpdateSchema = schemas.path("UpdateExternalAccountRequest");
        assertThat(acctUpdateSchema.path("properties").path("displayName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(acctUpdateSchema.path("properties").path("ownerName").path("maxLength").asInt()).isEqualTo(500);

        // --- Media Image schemas ---
        JsonNode imgCreateSchema = schemas.path("CreateImageRequest");
        assertThat(imgCreateSchema.path("required").toString()).contains("objectKey");
        assertThat(imgCreateSchema.path("required").toString()).doesNotContain("title", "imageType", "locationText");
        assertThat(imgCreateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(imgCreateSchema.path("properties").path("imageType").path("maxLength").asInt()).isEqualTo(100);
        assertThat(imgCreateSchema.path("properties").path("locationText").path("maxLength").asInt()).isEqualTo(500);

        JsonNode imgUpdateSchema = schemas.path("UpdateImageRequest");
        assertThat(imgUpdateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(imgUpdateSchema.path("properties").path("imageType").path("maxLength").asInt()).isEqualTo(100);
        assertThat(imgUpdateSchema.path("properties").path("locationText").path("maxLength").asInt()).isEqualTo(500);

        // --- Location Address schemas ---
        JsonNode addrCreateSchema = schemas.path("CreateAddressRequest");
        assertThat(addrCreateSchema.path("required").toString()).contains("countryCode");
        assertThat(addrCreateSchema.path("required").toString()).doesNotContain("locality", "streetAddress");
        assertThat(addrCreateSchema.path("properties").path("addressType").path("maxLength").asInt()).isEqualTo(100);
        assertThat(addrCreateSchema.path("properties").path("postalCode").path("maxLength").asInt()).isEqualTo(32);

        JsonNode addrUpdateSchema = schemas.path("UpdateAddressRequest");
        assertThat(addrUpdateSchema.path("properties").path("addressType").path("maxLength").asInt()).isEqualTo(100);
        assertThat(addrUpdateSchema.path("properties").path("postalCode").path("maxLength").asInt()).isEqualTo(32);

        // --- Knowledge schemas ---
        JsonNode studyUpdateSchema = schemas.path("UpdateKnowledgeStudyRequest");
        assertThat(studyUpdateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(studyUpdateSchema.path("properties").path("currentProgressText").path("maxLength").asInt()).isEqualTo(500);

        JsonNode noteUpdateSchema = schemas.path("UpdateKnowledgeNoteRequest");
        assertThat(noteUpdateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(noteUpdateSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(noteUpdateSchema.path("properties").path("importedFileName").path("maxLength").asInt()).isEqualTo(500);

        JsonNode infoCreateSchema = schemas.path("CreateKnowledgeInformationRequest");
        assertThat(infoCreateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(infoCreateSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);

        JsonNode infoUpdateSchema = schemas.path("UpdateKnowledgeInformationRequest");
        assertThat(infoUpdateSchema.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(infoUpdateSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);

        JsonNode vocabCreateSchema = schemas.path("CreateKnowledgeVocabularyRequest");
        assertThat(vocabCreateSchema.path("properties").path("word").path("maxLength").asInt()).isEqualTo(500);
        assertThat(vocabCreateSchema.path("properties").path("pronunciation").path("maxLength").asInt()).isEqualTo(500);
        assertThat(vocabCreateSchema.path("properties").path("partOfSpeech").path("maxLength").asInt()).isEqualTo(100);
        assertThat(vocabCreateSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);

        JsonNode vocabUpdateSchema = schemas.path("UpdateKnowledgeVocabularyRequest");
        assertThat(vocabUpdateSchema.path("properties").path("word").path("maxLength").asInt()).isEqualTo(500);
        assertThat(vocabUpdateSchema.path("properties").path("pronunciation").path("maxLength").asInt()).isEqualTo(500);
        assertThat(vocabUpdateSchema.path("properties").path("partOfSpeech").path("maxLength").asInt()).isEqualTo(100);
        assertThat(vocabUpdateSchema.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);

        // --- Collection Music schemas ---
        JsonNode musicUpdateSchema = schemas.path("UpdateMusicRequest");
        assertThat(musicUpdateSchema.path("description").asText()).contains("defaults to ORIGINAL if null");

        // --- Finance Recurring Rule and Transaction schemas ---
        JsonNode recCreateSchema = schemas.path("CreateRecurringTransactionRuleRequest");
        assertThat(recCreateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(recCreateSchema.path("properties").path("description").path("maxLength").asInt()).isEqualTo(1000);
        assertThat(recCreateSchema.path("properties").path("transactionType").path("description").asText())
                .contains("INCOME requires exactly one entry with positive amountDelta")
                .contains("EXPENSE requires exactly one entry with negative amountDelta")
                .contains("TRANSFER requires exactly two entries with distinct wallets, one negative source and one positive destination, and must have no category");
        assertThat(recCreateSchema.path("properties").path("categoryId").path("description").asText())
                .contains("Optional for INCOME and EXPENSE")
                .contains("Must be null for TRANSFER");
        assertThat(recCreateSchema.path("properties").path("entries").path("description").asText())
                .contains("Each wallet may appear at most once")
                .contains("TRANSFER requires exactly two entries with opposite signs");
        assertThat(recCreateSchema.path("properties").path("dayOfMonth").path("description").asText())
                .contains("Required for MONTHLY and YEARLY; must be null for DAILY and WEEKLY");
        assertThat(recCreateSchema.path("properties").path("monthOfYear").path("description").asText())
                .contains("Required for YEARLY; must be null for DAILY, WEEKLY, and MONTHLY");
        assertThat(recCreateSchema.path("properties").path("weekdays").path("description").asText())
                .contains("Required for WEEKLY; must be null/empty for DAILY, MONTHLY, and YEARLY");

        JsonNode recUpdateSchema = schemas.path("UpdateRecurringTransactionRuleRequest");
        assertThat(recUpdateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(recUpdateSchema.path("properties").path("description").path("maxLength").asInt()).isEqualTo(1000);
        assertThat(recUpdateSchema.path("properties").path("transactionType").path("description").asText())
                .contains("INCOME requires exactly one entry with positive amountDelta")
                .contains("EXPENSE requires exactly one entry with negative amountDelta")
                .contains("TRANSFER requires exactly two entries with distinct wallets, one negative source and one positive destination, and must have no category");
        assertThat(recUpdateSchema.path("properties").path("categoryId").path("description").asText())
                .contains("Optional for INCOME and EXPENSE")
                .contains("Must be null for TRANSFER");
        assertThat(recUpdateSchema.path("properties").path("entries").path("description").asText())
                .contains("Each wallet may appear at most once")
                .contains("TRANSFER requires exactly two entries with opposite signs");
        assertThat(recUpdateSchema.path("properties").path("dayOfMonth").path("description").asText())
                .contains("Required for MONTHLY and YEARLY; must be null for DAILY and WEEKLY");
        assertThat(recUpdateSchema.path("properties").path("monthOfYear").path("description").asText())
                .contains("Required for YEARLY; must be null for DAILY, WEEKLY, and MONTHLY");
        assertThat(recUpdateSchema.path("properties").path("weekdays").path("description").asText())
                .contains("Required for WEEKLY; must be null/empty for DAILY, MONTHLY, and YEARLY");

        JsonNode recRuleEntrySchema = schemas.path("RecurringRuleEntryRequest");
        assertThat(recRuleEntrySchema.path("properties").path("walletId").path("description").asText())
                .contains("Target wallet ID")
                .contains("Must be distinct across all entries in the recurring rule");
        assertThat(recRuleEntrySchema.path("properties").path("amountDelta").path("description").asText())
                .contains("Amount delta for the wallet")
                .contains("Positive for income or transfer destination; negative for expense or transfer source");

        JsonNode finTxUpdateSchema = schemas.path("UpdateFinancialTransactionRequest");
        String updateTypeDesc = finTxUpdateSchema.path("properties").path("type").path("description").asText();
        assertThat(updateTypeDesc).contains("TRANSFER (must not have category; two distinct wallets with opposite signs)");

        String updateStatusDesc = finTxUpdateSchema.path("properties").path("status").path("description").asText();
        assertThat(updateStatusDesc).contains("defaults to POSTED if omitted");

        String updateCategoryDesc = finTxUpdateSchema.path("properties").path("categoryId").path("description").asText();
        assertThat(updateCategoryDesc).contains("optional for EXPENSE and INCOME; must be null for TRANSFER");

        String updateEntriesDesc = finTxUpdateSchema.path("properties").path("entries").path("description").asText();
        assertThat(updateEntriesDesc).contains("distinct wallets (one negative source, one positive destination)");

        assertThat(finTxCreateSchema.path("properties").path("description").path("maxLength").asInt()).isEqualTo(1000);
        assertThat(finTxUpdateSchema.path("properties").path("description").path("maxLength").asInt()).isEqualTo(1000);

        JsonNode subCreateSchema = schemas.path("CreateSubscriptionRequest");
        JsonNode subUpdateSchema = schemas.path("UpdateSubscriptionRequest");
        assertThat(subCreateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(subUpdateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(subCreateSchema.path("properties").path("provider").path("maxLength").asInt()).isEqualTo(500);
        assertThat(subUpdateSchema.path("properties").path("provider").path("maxLength").asInt()).isEqualTo(500);

        // --- Fiction / Film Genres and Location Categories schemas ---
        JsonNode ficGenCreate = schemas.path("CreateFictionGenreRequest");
        JsonNode ficGenUpdate = schemas.path("UpdateFictionGenreRequest");
        assertThat(ficGenCreate.path("properties").path("name").path("maxLength").asInt()).isEqualTo(150);
        assertThat(ficGenUpdate.path("properties").path("name").path("maxLength").asInt()).isEqualTo(150);

        JsonNode filmGenCreate = schemas.path("CreateFilmGenreRequest");
        JsonNode filmGenUpdate = schemas.path("UpdateFilmGenreRequest");
        assertThat(filmGenCreate.path("properties").path("name").path("maxLength").asInt()).isEqualTo(150);
        assertThat(filmGenUpdate.path("properties").path("name").path("maxLength").asInt()).isEqualTo(150);

        JsonNode locCatCreate = schemas.path("CreateLocationCategoryRequest");
        JsonNode locCatUpdate = schemas.path("UpdateLocationCategoryRequest");
        assertThat(locCatCreate.path("properties").path("name").path("maxLength").asInt()).isEqualTo(150);
        assertThat(locCatUpdate.path("properties").path("name").path("maxLength").asInt()).isEqualTo(150);

        // --- Media Album schemas ---
        JsonNode albumCreate = schemas.path("CreateAlbumRequest");
        JsonNode albumUpdate = schemas.path("UpdateAlbumRequest");
        assertThat(albumCreate.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);
        assertThat(albumUpdate.path("properties").path("title").path("maxLength").asInt()).isEqualTo(500);

        // --- Feed Manual SavedResource & Import Job schemas ---
        JsonNode manualResCreate = schemas.path("CreateManualSavedResourceRequest");
        assertThat(manualResCreate.path("properties").path("title").path("maxLength").asInt()).isEqualTo(1000);
        assertThat(manualResCreate.path("properties").path("author").path("maxLength").asInt()).isEqualTo(500);
        assertThat(manualResCreate.path("properties").path("sourceName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(manualResCreate.path("properties").path("externalId").path("maxLength").asInt()).isEqualTo(500);

        JsonNode importJobCreate = schemas.path("CreateImportJobRequest");
        assertThat(importJobCreate.path("properties").path("originalFileName").path("maxLength").asInt()).isEqualTo(500);

        // --- Settings update schema ---
        JsonNode settingsUpdate = schemas.path("UpdateSettingsRequest");
        assertThat(settingsUpdate.path("properties").path("timezone").path("maxLength").asInt()).isEqualTo(64);
        assertThat(settingsUpdate.path("properties").path("paginationSize").path("minimum").asInt()).isEqualTo(1);
        assertThat(settingsUpdate.path("properties").path("paginationSize").has("maximum")).isFalse();
        assertThat(settingsUpdate.path("properties").path("privateModeAutoLockMinutes").path("minimum").asInt()).isEqualTo(1);
        assertThat(settingsUpdate.path("properties").path("privateModeAutoLockMinutes").has("maximum")).isFalse();
        assertThat(settingsUpdate.path("properties").path("backupIntervalHours").path("minimum").asInt()).isEqualTo(1);
        assertThat(settingsUpdate.path("properties").path("backupIntervalHours").has("maximum")).isFalse();

        // --- Personal Profile update schema ---
        JsonNode profileCreateSchema = schemas.path("CreatePersonalProfileRequest");
        assertThat(profileCreateSchema.path("properties").path("phone").path("maxLength").asInt()).isEqualTo(64);
        assertThat(profileCreateSchema.path("properties").path("email").path("maxLength").asInt()).isEqualTo(320);
        assertThat(profileCreateSchema.path("properties").path("email").has("format")).isFalse();
        assertThat(profileCreateSchema.path("properties").path("nationalityCode").path("maxLength").asInt()).isEqualTo(2);
        assertThat(profileCreateSchema.path("properties").path("nationalityCode").path("minLength").asInt()).isNotEqualTo(2);

        JsonNode profileUpdateSchema = schemas.path("UpdatePersonalProfileRequest");
        assertThat(profileUpdateSchema.path("properties").path("phone").path("maxLength").asInt()).isEqualTo(64);
        assertThat(profileUpdateSchema.path("properties").path("email").path("maxLength").asInt()).isEqualTo(320);
        assertThat(profileUpdateSchema.path("properties").path("email").has("format")).isFalse();
        assertThat(profileUpdateSchema.path("properties").path("nationalityCode").path("maxLength").asInt()).isEqualTo(2);
        assertThat(profileUpdateSchema.path("properties").path("nationalityCode").path("minLength").asInt()).isNotEqualTo(2);

        // --- Location schemas ---
        JsonNode locCreateSchema = schemas.path("CreateLocationRequest");
        JsonNode locUpdateSchema = schemas.path("UpdateLocationRequest");
        assertThat(locCreateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(locCreateSchema.path("properties").path("phone").path("maxLength").asInt()).isEqualTo(64);
        assertThat(locUpdateSchema.path("properties").path("name").path("maxLength").asInt()).isEqualTo(500);
        assertThat(locUpdateSchema.path("properties").path("phone").path("maxLength").asInt()).isEqualTo(64);

        // --- Follower Snapshot schemas ---
        JsonNode snapCreateSchema = schemas.path("CreateFollowerSnapshotRequest");
        JsonNode snapEntryCreateSchema = schemas.path("CreateFollowerSnapshotEntryRequest");
        assertThat(snapCreateSchema.path("properties").path("importedFileName").path("maxLength").asInt()).isEqualTo(500);
        assertThat(snapEntryCreateSchema.path("properties").path("displayNameSnapshot").path("maxLength").asInt()).isEqualTo(500);

        // --- Relationship schema ---
        JsonNode relSchema = schemas.path("SetExternalAccountRelationshipRequest");
        assertThat(relSchema.path("required").toString()).doesNotContain("\"followerStatus\"", "\"followStatus\"", "\"source\"");
        assertThat(relSchema.path("properties").path("followStatus").path("default").asText()).isEqualTo("UNKNOWN");
        assertThat(relSchema.path("properties").path("source").path("default").asText()).isEqualTo("MANUAL");
        assertThat(relSchema.path("properties").path("note").has("maxLength")).isFalse();

        // --- Feed Source schemas ---
        JsonNode feedSourceCreate = schemas.path("CreateFeedSourceRequest");
        JsonNode feedSourceUpdate = schemas.path("UpdateFeedSourceRequest");
        assertThat(feedSourceCreate.path("required").toString()).doesNotContain("\"feedUrl\"");
        assertThat(feedSourceUpdate.path("required").toString()).doesNotContain("\"feedUrl\"");

        // --- Global Search endpoint description ---
        JsonNode searchGetOp = root.path("paths").path("/api/v1/search").path("get");
        String searchDesc = searchGetOp.path("description").asText();
        assertThat(searchDesc).contains("case-insensitive literal and trigram similarity search");
        assertThat(searchDesc).contains("AND semantics");
        assertThat(searchDesc).contains("1 to 200 characters");
    }
}
