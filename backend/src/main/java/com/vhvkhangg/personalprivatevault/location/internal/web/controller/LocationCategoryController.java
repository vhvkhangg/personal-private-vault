package com.vhvkhangg.personalprivatevault.location.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationCategoryRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.LocationCategoryResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationCategoryRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.mapper.LocationWebMapper;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/location-categories")
@RequiredArgsConstructor
@Tag(name = "Location Categories", description = "Location category catalog management")
public class LocationCategoryController {

    private final LocationCategoryOperations locationCategoryOperations;

    @PostMapping
    @Operation(summary = "Create location category", operationId = "createLocationCategory")
    public ResponseEntity<ApiResponse<LocationCategoryResponse>> create(@Valid @RequestBody CreateLocationCategoryRequest request) {
        LocationCategoryView created = locationCategoryOperations.create(LocationWebMapper.toCommand(request));
        return ApiResponses.created(LocationWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location category by ID", operationId = "getLocationCategory")
    public ResponseEntity<ApiResponse<LocationCategoryResponse>> get(@PathVariable Long id) {
        LocationCategoryView category = locationCategoryOperations.findById(id);
        return ApiResponses.ok(LocationWebMapper.toResponse(category));
    }

    @GetMapping("/by-name")
    @Operation(summary = "Get location category by name", operationId = "getLocationCategoryByName")
    public ResponseEntity<ApiResponse<LocationCategoryResponse>> getByName(@org.springframework.web.bind.annotation.RequestParam String name) {
        LocationCategoryView category = locationCategoryOperations.findByName(name);
        return ApiResponses.ok(LocationWebMapper.toResponse(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update location category", operationId = "updateLocationCategory")
    public ResponseEntity<ApiResponse<LocationCategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationCategoryRequest request) {
        LocationCategoryView updated = locationCategoryOperations.update(LocationWebMapper.toCommand(id, request));
        return ApiResponses.ok(LocationWebMapper.toResponse(updated));
    }
}
