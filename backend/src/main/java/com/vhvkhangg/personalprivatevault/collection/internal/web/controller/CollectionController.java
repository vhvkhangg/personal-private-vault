package com.vhvkhangg.personalprivatevault.collection.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionOperations;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.AddMusicCreditRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateMusicRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.MusicCreditResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.MusicResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.ShoppingItemResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.SoftwareItemResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.SoftwarePlatformResponse;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateMusicRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateShoppingItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateSoftwareItemRequest;
import com.vhvkhangg.personalprivatevault.collection.internal.web.mapper.CollectionWebMapper;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
@Validated
@Tag(name = "Collection", description = "Collection management across music, shopping, and software catalogs")
public class CollectionController {

    private final CollectionOperations collectionOperations;

    // --- Music ---

    @PostMapping("/music")
    @Operation(summary = "Create music track", operationId = "createCollectionMusic")
    public ResponseEntity<ApiResponse<MusicResponse>> createMusic(@Valid @RequestBody CreateMusicRequest request) {
        CollectionMusicView created = collectionOperations.createMusic(CollectionWebMapper.toCommand(request));
        return ApiResponses.created(CollectionWebMapper.toResponse(created));
    }

    @GetMapping("/music/{id}")
    @Operation(summary = "Get music track by ID", operationId = "getCollectionMusic")
    public ResponseEntity<ApiResponse<MusicResponse>> getMusic(@PathVariable Long id) {
        return collectionOperations.findMusicById(id)
                .map(view -> ApiResponses.ok(CollectionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", "Music track not found"));
    }

    @PutMapping("/music/{id}")
    @Operation(summary = "Update music track", operationId = "updateCollectionMusic")
    public ResponseEntity<ApiResponse<MusicResponse>> updateMusic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMusicRequest request) {
        CollectionMusicView updated = collectionOperations.updateMusic(id, CollectionWebMapper.toCommand(request));
        return ApiResponses.ok(CollectionWebMapper.toResponse(updated));
    }

    @PutMapping("/music/{id}/credits")
    @Operation(summary = "Add person credit to music track", operationId = "addCollectionMusicCredit")
    public ResponseEntity<ApiResponse<Void>> addMusicCredit(
            @PathVariable Long id,
            @Valid @RequestBody AddMusicCreditRequest request) {
        collectionOperations.addMusicCredit(id, request.personId(), request.role());
        return ApiResponses.ok(null);
    }

    @GetMapping("/music/{id}/credits")
    @Operation(summary = "Get credits for music track", operationId = "getCollectionMusicCredits")
    public ResponseEntity<ApiResponse<List<MusicCreditResponse>>> getMusicCredits(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<CollectionMusicCreditView> credits = collectionOperations.findMusicCredits(id, limit);
        return ApiResponses.ok(credits.stream().map(CollectionWebMapper::toResponse).toList());
    }

    // --- Shopping ---

    @PostMapping("/shopping")
    @Operation(summary = "Create shopping item", operationId = "createCollectionShoppingItem")
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> createShoppingItem(
            @Valid @RequestBody CreateShoppingItemRequest request) {
        CollectionShoppingItemView created = collectionOperations.createShoppingItem(CollectionWebMapper.toCommand(request));
        return ApiResponses.created(CollectionWebMapper.toResponse(created));
    }

    @GetMapping("/shopping/{id}")
    @Operation(summary = "Get shopping item by ID", operationId = "getCollectionShoppingItem")
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> getShoppingItem(@PathVariable Long id) {
        return collectionOperations.findShoppingItemById(id)
                .map(view -> ApiResponses.ok(CollectionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", "Shopping item not found"));
    }

    @PutMapping("/shopping/{id}")
    @Operation(summary = "Update shopping item", operationId = "updateCollectionShoppingItem")
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> updateShoppingItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShoppingItemRequest request) {
        CollectionShoppingItemView updated = collectionOperations.updateShoppingItem(id, CollectionWebMapper.toCommand(request));
        return ApiResponses.ok(CollectionWebMapper.toResponse(updated));
    }

    // --- Software ---

    @PostMapping("/software")
    @Operation(summary = "Create software item", operationId = "createCollectionSoftwareItem")
    public ResponseEntity<ApiResponse<SoftwareItemResponse>> createSoftwareItem(
            @Valid @RequestBody CreateSoftwareItemRequest request) {
        CollectionSoftwareItemView created = collectionOperations.createSoftwareItem(CollectionWebMapper.toCommand(request));
        return ApiResponses.created(CollectionWebMapper.toResponse(created));
    }

    @GetMapping("/software/{id}")
    @Operation(summary = "Get software item by ID", operationId = "getCollectionSoftwareItem")
    public ResponseEntity<ApiResponse<SoftwareItemResponse>> getSoftwareItem(@PathVariable Long id) {
        return collectionOperations.findSoftwareItemById(id)
                .map(view -> ApiResponses.ok(CollectionWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "COLLECTION_NOT_FOUND", "Software item not found"));
    }

    @PutMapping("/software/{id}")
    @Operation(summary = "Update software item", operationId = "updateCollectionSoftwareItem")
    public ResponseEntity<ApiResponse<SoftwareItemResponse>> updateSoftwareItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSoftwareItemRequest request) {
        CollectionSoftwareItemView updated = collectionOperations.updateSoftwareItem(id, CollectionWebMapper.toCommand(request));
        return ApiResponses.ok(CollectionWebMapper.toResponse(updated));
    }

    @PutMapping("/software/{id}/platforms/{platformId}")
    @Operation(summary = "Assign platform to software item", operationId = "addCollectionSoftwarePlatform")
    public ResponseEntity<ApiResponse<Void>> addSoftwarePlatform(
            @PathVariable Long id,
            @PathVariable Long platformId) {
        collectionOperations.addSoftwarePlatform(id, platformId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/software/{id}/platforms")
    @Operation(summary = "Get platforms for software item", operationId = "getCollectionSoftwarePlatforms")
    public ResponseEntity<ApiResponse<List<SoftwarePlatformResponse>>> getSoftwarePlatforms(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<CollectionSoftwarePlatformView> platforms = collectionOperations.findSoftwarePlatforms(id, limit);
        return ApiResponses.ok(platforms.stream().map(CollectionWebMapper::toResponse).toList());
    }
}
