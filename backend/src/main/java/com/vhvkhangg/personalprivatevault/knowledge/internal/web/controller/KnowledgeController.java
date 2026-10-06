package com.vhvkhangg.personalprivatevault.knowledge.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyReviewTransitionResultView;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeInformationRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeNoteRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeVocabularyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeInformationResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeNoteResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeStudyResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeVocabularyResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.KnowledgeVocabularyReviewResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeInformationRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeNoteRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeStudyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeVocabularyRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.VocabularyReviewRequest;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.VocabularyReviewResultResponse;
import com.vhvkhangg.personalprivatevault.knowledge.internal.web.mapper.KnowledgeWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
@Validated
@Tag(name = "Knowledge", description = "Parent facade for study items, information snippets, vocabulary SRS, and notes")
public class KnowledgeController {

    private final KnowledgeOperations knowledgeOperations;

    // Study
    @PostMapping("/study")
    @Operation(summary = "Create study item", operationId = "createStudyItem")
    public ResponseEntity<ApiResponse<KnowledgeStudyResponse>> createStudyItem(@Valid @RequestBody CreateKnowledgeStudyRequest request) {
        var created = knowledgeOperations.createStudyItem(KnowledgeWebMapper.toCommand(request));
        return ApiResponses.created(KnowledgeWebMapper.toResponse(created));
    }

    @GetMapping("/study/{id}")
    @Operation(summary = "Get study item by ID", operationId = "getStudyItem")
    public ResponseEntity<ApiResponse<KnowledgeStudyResponse>> getStudyItem(@PathVariable Long id) {
        return knowledgeOperations.findStudyItemById(id)
                .map(view -> ApiResponses.ok(KnowledgeWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "KNOWLEDGE_STUDY_NOT_FOUND", "Study item not found"));
    }

    @PutMapping("/study/{id}")
    @Operation(summary = "Update study item", operationId = "updateStudyItem")
    public ResponseEntity<ApiResponse<KnowledgeStudyResponse>> updateStudyItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeStudyRequest request) {
        var updated = knowledgeOperations.updateStudyItem(id, KnowledgeWebMapper.toCommand(request));
        return ApiResponses.ok(KnowledgeWebMapper.toResponse(updated));
    }

    // Information
    @PostMapping("/information")
    @Operation(summary = "Create information item", operationId = "createInformationItem")
    public ResponseEntity<ApiResponse<KnowledgeInformationResponse>> createInformationItem(@Valid @RequestBody CreateKnowledgeInformationRequest request) {
        var created = knowledgeOperations.createInformationItem(KnowledgeWebMapper.toCommand(request));
        return ApiResponses.created(KnowledgeWebMapper.toResponse(created));
    }

    @GetMapping("/information/{id}")
    @Operation(summary = "Get information item by ID", operationId = "getInformationItem")
    public ResponseEntity<ApiResponse<KnowledgeInformationResponse>> getInformationItem(@PathVariable Long id) {
        return knowledgeOperations.findInformationItemById(id)
                .map(view -> ApiResponses.ok(KnowledgeWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "KNOWLEDGE_INFORMATION_NOT_FOUND", "Information item not found"));
    }

    @PutMapping("/information/{id}")
    @Operation(summary = "Update information item", operationId = "updateInformationItem")
    public ResponseEntity<ApiResponse<KnowledgeInformationResponse>> updateInformationItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeInformationRequest request) {
        var updated = knowledgeOperations.updateInformationItem(id, KnowledgeWebMapper.toCommand(request));
        return ApiResponses.ok(KnowledgeWebMapper.toResponse(updated));
    }

    // Vocabulary
    @PostMapping("/vocabulary")
    @Operation(summary = "Create vocabulary item", operationId = "createVocabularyItem")
    public ResponseEntity<ApiResponse<KnowledgeVocabularyResponse>> createVocabularyItem(@Valid @RequestBody CreateKnowledgeVocabularyRequest request) {
        var created = knowledgeOperations.createVocabularyItem(KnowledgeWebMapper.toCommand(request));
        return ApiResponses.created(KnowledgeWebMapper.toResponse(created));
    }

    @GetMapping("/vocabulary/{id}")
    @Operation(summary = "Get vocabulary item by ID", operationId = "getVocabularyItem")
    public ResponseEntity<ApiResponse<KnowledgeVocabularyResponse>> getVocabularyItem(@PathVariable Long id) {
        return knowledgeOperations.findVocabularyItemById(id)
                .map(view -> ApiResponses.ok(KnowledgeWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "KNOWLEDGE_VOCABULARY_NOT_FOUND", "Vocabulary item not found"));
    }

    @PutMapping("/vocabulary/{id}")
    @Operation(summary = "Update vocabulary item", operationId = "updateVocabularyItem")
    public ResponseEntity<ApiResponse<KnowledgeVocabularyResponse>> updateVocabularyItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeVocabularyRequest request) {
        var updated = knowledgeOperations.updateVocabularyItem(id, KnowledgeWebMapper.toCommand(request));
        return ApiResponses.ok(KnowledgeWebMapper.toResponse(updated));
    }

    @PostMapping("/vocabulary/{id}/reviews")
    @Operation(summary = "Review vocabulary item with SRS transition", operationId = "reviewVocabularyItem")
    public ResponseEntity<ApiResponse<VocabularyReviewResultResponse>> reviewVocabularyItem(
            @PathVariable Long id,
            @Valid @RequestBody VocabularyReviewRequest request) {
        KnowledgeVocabularyReviewTransitionResultView result = knowledgeOperations.reviewVocabularyItem(id, KnowledgeWebMapper.toCommand(request));
        return ApiResponses.ok(KnowledgeWebMapper.toResponse(result));
    }

    @GetMapping("/vocabulary/due")
    @Operation(summary = "Get due vocabulary items for review", operationId = "getDueVocabularyItems")
    public ResponseEntity<ApiResponse<List<KnowledgeVocabularyResponse>>> getDueVocabularyItems(
            @RequestParam(required = false) Instant cutoff,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        Instant effectiveCutoff = cutoff != null ? cutoff : Instant.now();
        List<KnowledgeVocabularyResponse> responseList = knowledgeOperations.findDueVocabularyItems(effectiveCutoff, limit).stream()
                .map(KnowledgeWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(responseList);
    }

    @GetMapping("/vocabulary/{id}/reviews")
    @Operation(summary = "Get review history for vocabulary item", operationId = "getVocabularyReviews")
    public ResponseEntity<ApiResponse<List<KnowledgeVocabularyReviewResponse>>> getVocabularyReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<KnowledgeVocabularyReviewResponse> responseList = knowledgeOperations.findVocabularyReviews(id, limit).stream()
                .map(KnowledgeWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(responseList);
    }

    // Notes
    @PostMapping("/notes")
    @Operation(summary = "Create note", operationId = "createNote")
    public ResponseEntity<ApiResponse<KnowledgeNoteResponse>> createNote(@Valid @RequestBody CreateKnowledgeNoteRequest request) {
        var created = knowledgeOperations.createNote(KnowledgeWebMapper.toCommand(request));
        return ApiResponses.created(KnowledgeWebMapper.toResponse(created));
    }

    @GetMapping("/notes/{id}")
    @Operation(summary = "Get note by ID", operationId = "getNote")
    public ResponseEntity<ApiResponse<KnowledgeNoteResponse>> getNote(@PathVariable Long id) {
        return knowledgeOperations.findNoteById(id)
                .map(view -> ApiResponses.ok(KnowledgeWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "KNOWLEDGE_NOTE_NOT_FOUND", "Note not found"));
    }

    @PutMapping("/notes/{id}")
    @Operation(summary = "Update note", operationId = "updateNote")
    public ResponseEntity<ApiResponse<KnowledgeNoteResponse>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeNoteRequest request) {
        var updated = knowledgeOperations.updateNote(id, KnowledgeWebMapper.toCommand(request));
        return ApiResponses.ok(KnowledgeWebMapper.toResponse(updated));
    }
}
