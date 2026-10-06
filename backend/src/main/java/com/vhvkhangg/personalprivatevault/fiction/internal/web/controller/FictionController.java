package com.vhvkhangg.personalprivatevault.fiction.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionLinkRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.CreateFictionRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionClassificationsResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionLinkResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.FictionResponse;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionLinkRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.dto.UpdateFictionRequest;
import com.vhvkhangg.personalprivatevault.fiction.internal.web.mapper.FictionWebMapper;
import com.vhvkhangg.personalprivatevault.fiction.link.FictionLinkOperations;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionClassificationsView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionLinkView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionView;
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
@RequestMapping("/api/v1/fictions")
@RequiredArgsConstructor
@Tag(name = "Fiction", description = "Fiction work catalog, classifications, and external links")
public class FictionController {

    private final FictionOperations fictionOperations;
    private final FictionLinkOperations fictionLinkOperations;

    @PostMapping
    @Operation(summary = "Create fiction work", operationId = "createFiction")
    public ResponseEntity<ApiResponse<FictionResponse>> create(@Valid @RequestBody CreateFictionRequest request) {
        FictionView created = fictionOperations.create(FictionWebMapper.toCommand(request));
        return ApiResponses.created(FictionWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fiction work by ID", operationId = "getFiction")
    public ResponseEntity<ApiResponse<FictionResponse>> get(@PathVariable Long id) {
        return fictionOperations.find(id)
                .map(view -> ApiResponses.ok(FictionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_NOT_FOUND", "Fiction not found"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fiction work", operationId = "updateFiction")
    public ResponseEntity<ApiResponse<FictionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFictionRequest request) {
        FictionView updated = fictionOperations.update(FictionWebMapper.toCommand(id, request));
        return ApiResponses.ok(FictionWebMapper.toResponse(updated));
    }

    @PutMapping("/{id}/story-archetypes/{storyArchetypeId}")
    @Operation(summary = "Assign story archetype to fiction", operationId = "addFictionStoryArchetype")
    public ResponseEntity<ApiResponse<Void>> addStoryArchetype(@PathVariable Long id, @PathVariable Long storyArchetypeId) {
        fictionOperations.addStoryArchetype(id, storyArchetypeId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/story-archetypes")
    @Operation(summary = "Get story archetypes for fiction", operationId = "getFictionStoryArchetypes")
    public ResponseEntity<ApiResponse<Set<Long>>> getStoryArchetypes(@PathVariable Long id) {
        Set<Long> storyArchetypeIds = fictionOperations.getStoryArchetypes(id);
        return ApiResponses.ok(storyArchetypeIds);
    }

    @PutMapping("/{id}/world-settings/{worldSettingId}")
    @Operation(summary = "Assign world setting to fiction", operationId = "addFictionWorldSetting")
    public ResponseEntity<ApiResponse<Void>> addWorldSetting(@PathVariable Long id, @PathVariable Long worldSettingId) {
        fictionOperations.addWorldSetting(id, worldSettingId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/world-settings")
    @Operation(summary = "Get world settings for fiction", operationId = "getFictionWorldSettings")
    public ResponseEntity<ApiResponse<Set<Long>>> getWorldSettings(@PathVariable Long id) {
        Set<Long> worldSettingIds = fictionOperations.getWorldSettings(id);
        return ApiResponses.ok(worldSettingIds);
    }

    @GetMapping("/{id}/classifications")
    @Operation(summary = "Get all narrative classifications for fiction", operationId = "getFictionClassifications")
    public ResponseEntity<ApiResponse<FictionClassificationsResponse>> getClassifications(@PathVariable Long id) {
        FictionClassificationsView classifications = fictionOperations.getClassifications(id);
        return ApiResponses.ok(FictionWebMapper.toResponse(classifications));
    }

    @PostMapping("/{id}/links")
    @Operation(summary = "Create external link for fiction", operationId = "createFictionLink")
    public ResponseEntity<ApiResponse<FictionLinkResponse>> createLink(
            @PathVariable Long id,
            @Valid @RequestBody CreateFictionLinkRequest request) {
        FictionLinkView created = fictionLinkOperations.create(FictionWebMapper.toCommand(id, request));
        return ApiResponses.created(FictionWebMapper.toResponse(created));
    }

    @GetMapping("/{id}/links")
    @Operation(summary = "Get all links for fiction", operationId = "getFictionLinks")
    public ResponseEntity<ApiResponse<List<FictionLinkResponse>>> getLinks(@PathVariable Long id) {
        List<FictionLinkResponse> links = fictionLinkOperations.findByFictionId(id).stream()
                .map(FictionWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(links);
    }

    @GetMapping("/{id}/links/{linkId}")
    @Operation(summary = "Get specific link for fiction", operationId = "getFictionLink")
    public ResponseEntity<ApiResponse<FictionLinkResponse>> getLink(@PathVariable Long id, @PathVariable Long linkId) {
        return fictionLinkOperations.findById(id, linkId)
                .map(view -> ApiResponses.ok(FictionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "FICTION_LINK_NOT_FOUND", "Fiction link not found"));
    }

    @PutMapping("/{id}/links/{linkId}")
    @Operation(summary = "Update external link for fiction", operationId = "updateFictionLink")
    public ResponseEntity<ApiResponse<FictionLinkResponse>> updateLink(
            @PathVariable Long id,
            @PathVariable Long linkId,
            @Valid @RequestBody UpdateFictionLinkRequest request) {
        FictionLinkView updated = fictionLinkOperations.update(FictionWebMapper.toCommand(linkId, id, request));
        return ApiResponses.ok(FictionWebMapper.toResponse(updated));
    }
}
