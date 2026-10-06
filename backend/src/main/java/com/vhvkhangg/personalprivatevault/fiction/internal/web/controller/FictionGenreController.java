package com.vhvkhangg.personalprivatevault.fiction.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.fiction.genre.FictionGenreOperations;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionGenreRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionGenreResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionGenreRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.mapper.FictionWebMapper;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fiction-genres")
@RequiredArgsConstructor
@Tag(name = "Fiction Genres", description = "Fiction genre catalog management")
public class FictionGenreController {

    private final FictionGenreOperations fictionGenreOperations;

    @PostMapping
    @Operation(summary = "Create fiction genre", operationId = "createFictionGenre")
    public ResponseEntity<ApiResponse<FictionGenreResponse>> create(@Valid @RequestBody CreateFictionGenreRequest request) {
        FictionGenreView created = fictionGenreOperations.create(FictionWebMapper.toCommand(request));
        return ApiResponses.created(FictionWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fiction genre by ID", operationId = "getFictionGenre")
    public ResponseEntity<ApiResponse<FictionGenreResponse>> get(@PathVariable Long id) {
        return fictionGenreOperations.find(id)
                .map(view -> ApiResponses.ok(FictionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_GENRE_NOT_FOUND", "Fiction genre not found"));
    }

    @GetMapping("/by-name")
    @Operation(summary = "Get fiction genre by name", operationId = "getFictionGenreByName")
    public ResponseEntity<ApiResponse<FictionGenreResponse>> getByName(@org.springframework.web.bind.annotation.RequestParam String name) {
        return fictionGenreOperations.findByName(name)
                .map(view -> ApiResponses.ok(FictionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_GENRE_NOT_FOUND", "Fiction genre not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fiction genre", operationId = "updateFictionGenre")
    public ResponseEntity<ApiResponse<FictionGenreResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFictionGenreRequest request) {
        FictionGenreView updated = fictionGenreOperations.update(FictionWebMapper.toCommand(id, request));
        return ApiResponses.ok(FictionWebMapper.toResponse(updated));
    }
}
