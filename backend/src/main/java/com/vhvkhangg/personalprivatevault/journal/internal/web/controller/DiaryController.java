package com.vhvkhangg.personalprivatevault.journal.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.journal.diary.DiaryOperations;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.CreateDiaryEntryRequest;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.DiaryEntryResponse;
import com.vhvkhangg.personalprivatevault.journal.internal.web.dto.UpdateDiaryEntryRequest;
import com.vhvkhangg.personalprivatevault.journal.internal.web.mapper.JournalWebMapper;
import com.vhvkhangg.personalprivatevault.journal.view.DiaryEntryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/journal/diary-entries")
@RequiredArgsConstructor
@Validated
@Tag(name = "Journal", description = "Journal diary entry operations: create, read, update, range query, soft-delete, and restore")
public class DiaryController {

    private final DiaryOperations diaryOperations;

    @PostMapping
    @Operation(summary = "Create diary entry", operationId = "createDiaryEntry")
    public ResponseEntity<ApiResponse<DiaryEntryResponse>> create(@Valid @RequestBody CreateDiaryEntryRequest request) {
        DiaryEntryView created = diaryOperations.createDiaryEntry(JournalWebMapper.toCommand(request));
        return ApiResponses.created(JournalWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get diary entry by ID", operationId = "getDiaryEntry")
    public ResponseEntity<ApiResponse<DiaryEntryResponse>> get(@PathVariable Long id) {
        DiaryEntryView view = diaryOperations.findDiaryEntryById(id);
        return ApiResponses.ok(JournalWebMapper.toResponse(view));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update diary entry", operationId = "updateDiaryEntry")
    public ResponseEntity<ApiResponse<DiaryEntryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiaryEntryRequest request) {
        DiaryEntryView updated = diaryOperations.updateDiaryEntry(JournalWebMapper.toCommand(id, request));
        return ApiResponses.ok(JournalWebMapper.toResponse(updated));
    }

    @GetMapping
    @Operation(summary = "Find diary entries", operationId = "findDiaryEntries")
    public ResponseEntity<ApiResponse<List<DiaryEntryResponse>>> find(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<DiaryEntryView> entries = diaryOperations.findDiaryEntries(fromDate, toDate, limit);
        return ApiResponses.ok(entries.stream().map(JournalWebMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete diary entry", operationId = "softDeleteDiaryEntry")
    public ResponseEntity<ApiResponse<DiaryEntryResponse>> softDelete(@PathVariable Long id) {
        DiaryEntryView view = diaryOperations.softDeleteDiaryEntry(id);
        return ApiResponses.ok(JournalWebMapper.toResponse(view));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore soft-deleted diary entry", operationId = "restoreDiaryEntry")
    public ResponseEntity<ApiResponse<DiaryEntryResponse>> restore(@PathVariable Long id) {
        DiaryEntryView view = diaryOperations.restoreDiaryEntry(id);
        return ApiResponses.ok(JournalWebMapper.toResponse(view));
    }
}
