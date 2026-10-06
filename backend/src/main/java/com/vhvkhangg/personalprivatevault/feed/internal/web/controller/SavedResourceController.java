package com.vhvkhangg.personalprivatevault.feed.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.feed.conversion.SavedResourceConversionOperations;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToInformationRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToNoteRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToStudyRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSavedResourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateManualSavedResourceRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.SavedResourceConversionResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.SavedResourceResponse;
import com.vhvkhangg.personalprivatevault.feed.internal.web.mapper.FeedWebMapper;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceConversionView;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/saved-resources")
@RequiredArgsConstructor
@Validated
@Tag(name = "SavedResource", description = "Saved web resources and knowledge conversion provenance")
public class SavedResourceController {

    private final SavedResourceOperations savedResourceOperations;
    private final SavedResourceConversionOperations conversionOperations;

    @PostMapping("/feed-item")
    @Operation(summary = "Save feed item as a resource", operationId = "saveFeedItemResource")
    public ResponseEntity<ApiResponse<SavedResourceResponse>> saveFeedItem(@Valid @RequestBody CreateFeedSavedResourceRequest request) {
        SavedResourceView saved = savedResourceOperations.saveFeedItem(FeedWebMapper.toCommand(request));
        return ApiResponses.created(FeedWebMapper.toResponse(saved));
    }

    @PostMapping("/manual")
    @Operation(summary = "Manually save a web resource", operationId = "saveManualResource")
    public ResponseEntity<ApiResponse<SavedResourceResponse>> saveManual(@Valid @RequestBody CreateManualSavedResourceRequest request) {
        SavedResourceView saved = savedResourceOperations.saveManual(FeedWebMapper.toCommand(request));
        return ApiResponses.created(FeedWebMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get saved resource by ID", operationId = "getSavedResource")
    public ResponseEntity<ApiResponse<SavedResourceResponse>> get(@PathVariable Long id) {
        return savedResourceOperations.findSavedResourceById(id)
                .map(view -> ApiResponses.ok(FeedWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "SAVED_RESOURCE_NOT_FOUND", "Saved resource not found"));
    }

    @GetMapping
    @Operation(summary = "Query saved resources", operationId = "findSavedResources")
    public ResponseEntity<ApiResponse<List<SavedResourceResponse>>> findSavedResources(
            @RequestParam(required = false) String urlHash,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        if (urlHash != null && !urlHash.isBlank()) {
            return savedResourceOperations.findSavedResourceByUrlHash(urlHash)
                    .map(view -> ApiResponses.ok(List.of(FeedWebMapper.toResponse(view))))
                    .orElseGet(() -> ApiResponses.ok(List.of()));
        }
        List<SavedResourceView> resources = savedResourceOperations.findRecentSavedResources(limit);
        return ApiResponses.ok(resources.stream().map(FeedWebMapper::toResponse).toList());
    }

    // --- Conversions ---

    @PostMapping("/{id}/convert/study")
    @Operation(summary = "Convert saved resource to Study item", operationId = "convertSavedResourceToStudy")
    public ResponseEntity<ApiResponse<SavedResourceConversionResponse>> convertToStudy(
            @PathVariable Long id,
            @Valid @RequestBody ConvertToStudyRequest request) {
        SavedResourceConversionView conversion = conversionOperations.convertSavedResourceToStudy(
                id, FeedWebMapper.toStudyCommand(request));
        return ApiResponses.created(FeedWebMapper.toResponse(conversion));
    }

    @PostMapping("/{id}/convert/information")
    @Operation(summary = "Convert saved resource to Information item", operationId = "convertSavedResourceToInformation")
    public ResponseEntity<ApiResponse<SavedResourceConversionResponse>> convertToInformation(
            @PathVariable Long id,
            @Valid @RequestBody ConvertToInformationRequest request) {
        SavedResourceConversionView conversion = conversionOperations.convertSavedResourceToInformation(
                id, FeedWebMapper.toInformationCommand(request));
        return ApiResponses.created(FeedWebMapper.toResponse(conversion));
    }

    @PostMapping("/{id}/convert/note")
    @Operation(summary = "Convert saved resource to Note", operationId = "convertSavedResourceToNote")
    public ResponseEntity<ApiResponse<SavedResourceConversionResponse>> convertToNote(
            @PathVariable Long id,
            @Valid @RequestBody ConvertToNoteRequest request) {
        SavedResourceConversionView conversion = conversionOperations.convertSavedResourceToNote(
                id, FeedWebMapper.toNoteCommand(request));
        return ApiResponses.created(FeedWebMapper.toResponse(conversion));
    }

    @GetMapping("/{id}/conversions")
    @Operation(summary = "Get conversions for a saved resource", operationId = "getSavedResourceConversions")
    public ResponseEntity<ApiResponse<List<SavedResourceConversionResponse>>> getConversions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<SavedResourceConversionView> conversions = conversionOperations.findConversionsBySavedResourceId(id, limit);
        return ApiResponses.ok(conversions.stream().map(FeedWebMapper::toResponse).toList());
    }
}
