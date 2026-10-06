package com.vhvkhangg.personalprivatevault.film.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.film.genre.FilmGenreOperations;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmGenreRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmGenreResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmGenreRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.mapper.FilmWebMapper;
import com.vhvkhangg.personalprivatevault.film.view.FilmGenreView;
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
@RequestMapping("/api/v1/film-genres")
@RequiredArgsConstructor
@Tag(name = "Film Genres", description = "Film genre catalog management")
public class FilmGenreController {

    private final FilmGenreOperations filmGenreOperations;

    @PostMapping
    @Operation(summary = "Create film genre", operationId = "createFilmGenre")
    public ResponseEntity<ApiResponse<FilmGenreResponse>> create(@Valid @RequestBody CreateFilmGenreRequest request) {
        FilmGenreView created = filmGenreOperations.create(FilmWebMapper.toCommand(request));
        return ApiResponses.created(FilmWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get film genre by ID", operationId = "getFilmGenre")
    public ResponseEntity<ApiResponse<FilmGenreResponse>> get(@PathVariable Long id) {
        return filmGenreOperations.find(id)
                .map(view -> ApiResponses.ok(FilmWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_GENRE_NOT_FOUND", "Film genre not found"));
    }

    @GetMapping("/by-name")
    @Operation(summary = "Get film genre by name", operationId = "getFilmGenreByName")
    public ResponseEntity<ApiResponse<FilmGenreResponse>> getByName(@org.springframework.web.bind.annotation.RequestParam String name) {
        return filmGenreOperations.findByName(name)
                .map(view -> ApiResponses.ok(FilmWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_GENRE_NOT_FOUND", "Film genre not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update film genre", operationId = "updateFilmGenre")
    public ResponseEntity<ApiResponse<FilmGenreResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFilmGenreRequest request) {
        FilmGenreView updated = filmGenreOperations.update(FilmWebMapper.toCommand(id, request));
        return ApiResponses.ok(FilmWebMapper.toResponse(updated));
    }
}
