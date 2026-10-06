package com.vhvkhangg.personalprivatevault.film.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.film.credit.FilmCreditOperations;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmCreditRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmLinkRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.CreateFilmRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmClassificationsResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmCreditResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmLinkResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.FilmResponse;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmLinkRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.dto.UpdateFilmRequest;
import com.vhvkhangg.personalprivatevault.film.internal.web.mapper.FilmWebMapper;
import com.vhvkhangg.personalprivatevault.film.link.FilmLinkOperations;
import com.vhvkhangg.personalprivatevault.film.view.FilmClassificationsView;
import com.vhvkhangg.personalprivatevault.film.view.FilmCreditView;
import com.vhvkhangg.personalprivatevault.film.view.FilmLinkView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;
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

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/films")
@RequiredArgsConstructor
@Tag(name = "Film", description = "Film works, credits, classifications, and external links")
public class FilmController {

    private final FilmOperations filmOperations;
    private final FilmCreditOperations filmCreditOperations;
    private final FilmLinkOperations filmLinkOperations;

    @PostMapping
    @Operation(summary = "Create film work", operationId = "createFilm")
    public ResponseEntity<ApiResponse<FilmResponse>> create(@Valid @RequestBody CreateFilmRequest request) {
        FilmView created = filmOperations.create(FilmWebMapper.toCommand(request));
        return ApiResponses.created(FilmWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get film work by ID", operationId = "getFilm")
    public ResponseEntity<ApiResponse<FilmResponse>> get(@PathVariable Long id) {
        return filmOperations.find(id)
                .map(view -> ApiResponses.ok(FilmWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_NOT_FOUND", "Film not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update film work", operationId = "updateFilm")
    public ResponseEntity<ApiResponse<FilmResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFilmRequest request) {
        FilmView updated = filmOperations.update(FilmWebMapper.toCommand(id, request));
        return ApiResponses.ok(FilmWebMapper.toResponse(updated));
    }

    @PutMapping("/{id}/genres/{genreId}")
    @Operation(summary = "Assign genre to film", operationId = "addFilmGenre")
    public ResponseEntity<ApiResponse<Void>> addGenre(@PathVariable Long id, @PathVariable Long genreId) {
        filmOperations.addGenre(id, genreId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/genres")
    @Operation(summary = "Get genres for film", operationId = "getFilmGenres")
    public ResponseEntity<ApiResponse<Set<Long>>> getGenres(@PathVariable Long id) {
        Set<Long> genreIds = filmOperations.getGenres(id);
        return ApiResponses.ok(genreIds);
    }

    @PutMapping("/{id}/story-archetypes/{storyArchetypeId}")
    @Operation(summary = "Assign story archetype to film", operationId = "addFilmStoryArchetype")
    public ResponseEntity<ApiResponse<Void>> addStoryArchetype(@PathVariable Long id, @PathVariable Long storyArchetypeId) {
        filmOperations.addStoryArchetype(id, storyArchetypeId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/story-archetypes")
    @Operation(summary = "Get story archetypes for film", operationId = "getFilmStoryArchetypes")
    public ResponseEntity<ApiResponse<Set<Long>>> getStoryArchetypes(@PathVariable Long id) {
        Set<Long> storyArchetypeIds = filmOperations.getStoryArchetypes(id);
        return ApiResponses.ok(storyArchetypeIds);
    }

    @PutMapping("/{id}/world-settings/{worldSettingId}")
    @Operation(summary = "Assign world setting to film", operationId = "addFilmWorldSetting")
    public ResponseEntity<ApiResponse<Void>> addWorldSetting(@PathVariable Long id, @PathVariable Long worldSettingId) {
        filmOperations.addWorldSetting(id, worldSettingId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/world-settings")
    @Operation(summary = "Get world settings for film", operationId = "getFilmWorldSettings")
    public ResponseEntity<ApiResponse<Set<Long>>> getWorldSettings(@PathVariable Long id) {
        Set<Long> worldSettingIds = filmOperations.getWorldSettings(id);
        return ApiResponses.ok(worldSettingIds);
    }

    @GetMapping("/{id}/classifications")
    @Operation(summary = "Get all classifications for film", operationId = "getFilmClassifications")
    public ResponseEntity<ApiResponse<FilmClassificationsResponse>> getClassifications(@PathVariable Long id) {
        FilmClassificationsView classifications = filmOperations.getClassifications(id);
        return ApiResponses.ok(FilmWebMapper.toResponse(classifications));
    }

    @PostMapping("/{id}/credits")
    @Operation(summary = "Create film credit", operationId = "createFilmCredit")
    public ResponseEntity<ApiResponse<FilmCreditResponse>> createCredit(
            @PathVariable Long id,
            @Valid @RequestBody CreateFilmCreditRequest request) {
        FilmCreditView created = filmCreditOperations.create(FilmWebMapper.toCommand(id, request));
        return ApiResponses.created(FilmWebMapper.toResponse(created));
    }

    @GetMapping("/{id}/credits")
    @Operation(summary = "Get all credits for film", operationId = "getFilmCredits")
    public ResponseEntity<ApiResponse<List<FilmCreditResponse>>> getCredits(@PathVariable Long id) {
        List<FilmCreditResponse> credits = filmCreditOperations.findByFilmId(id).stream()
                .map(FilmWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(credits);
    }

    @GetMapping("/credits/{creditId}")
    @Operation(summary = "Get film credit by ID", operationId = "getFilmCredit")
    public ResponseEntity<ApiResponse<FilmCreditResponse>> getCredit(@PathVariable Long creditId) {
        return filmCreditOperations.find(creditId)
                .map(view -> ApiResponses.ok(FilmWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_CREDIT_NOT_FOUND", "Film credit not found"));
    }

    @PostMapping("/{id}/links")
    @Operation(summary = "Create external link for film", operationId = "createFilmLink")
    public ResponseEntity<ApiResponse<FilmLinkResponse>> createLink(
            @PathVariable Long id,
            @Valid @RequestBody CreateFilmLinkRequest request) {
        FilmLinkView created = filmLinkOperations.create(FilmWebMapper.toCommand(id, request));
        return ApiResponses.created(FilmWebMapper.toResponse(created));
    }

    @GetMapping("/{id}/links")
    @Operation(summary = "Get all links for film", operationId = "getFilmLinks")
    public ResponseEntity<ApiResponse<List<FilmLinkResponse>>> getLinks(@PathVariable Long id) {
        List<FilmLinkResponse> links = filmLinkOperations.findByFilmId(id).stream()
                .map(FilmWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(links);
    }

    @GetMapping("/{id}/links/{linkId}")
    @Operation(summary = "Get specific link for film", operationId = "getFilmLink")
    public ResponseEntity<ApiResponse<FilmLinkResponse>> getLink(@PathVariable Long id, @PathVariable Long linkId) {
        return filmLinkOperations.findById(id, linkId)
                .map(view -> ApiResponses.ok(FilmWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FILM_LINK_NOT_FOUND", "Film link not found"));
    }

    @PutMapping("/{id}/links/{linkId}")
    @Operation(summary = "Update external link for film", operationId = "updateFilmLink")
    public ResponseEntity<ApiResponse<FilmLinkResponse>> updateLink(
            @PathVariable Long id,
            @PathVariable Long linkId,
            @Valid @RequestBody UpdateFilmLinkRequest request) {
        FilmLinkView updated = filmLinkOperations.update(FilmWebMapper.toCommand(linkId, id, request));
        return ApiResponses.ok(FilmWebMapper.toResponse(updated));
    }
}
