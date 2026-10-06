package com.vhvkhangg.personalprivatevault.location.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations;
import com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursOperations;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.BusinessHoursScheduleResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.LocationCategoryResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.LocationResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.ReplaceBusinessHoursScheduleRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.mapper.LocationWebMapper;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;
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

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Physical locations, categories, dining service styles, and business hours")
public class LocationController {

    private final LocationOperations locationOperations;
    private final LocationCategoryOperations locationCategoryOperations;
    private final BusinessHoursOperations businessHoursOperations;

    @PostMapping
    @Operation(summary = "Create location", operationId = "createLocation")
    public ResponseEntity<ApiResponse<LocationResponse>> create(@Valid @RequestBody CreateLocationRequest request) {
        LocationView created = locationOperations.create(LocationWebMapper.toCommand(request));
        return ApiResponses.created(LocationWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID", operationId = "getLocation")
    public ResponseEntity<ApiResponse<LocationResponse>> get(@PathVariable Long id) {
        LocationView location = locationOperations.findById(id);
        return ApiResponses.ok(LocationWebMapper.toResponse(location));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update location", operationId = "updateLocation")
    public ResponseEntity<ApiResponse<LocationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request) {
        LocationView updated = locationOperations.update(LocationWebMapper.toCommand(id, request));
        return ApiResponses.ok(LocationWebMapper.toResponse(updated));
    }

    @PutMapping("/{id}/dining-service-styles/{style}")
    @Operation(summary = "Assign dining service style to location", operationId = "assignLocationDiningServiceStyle")
    public ResponseEntity<ApiResponse<Void>> assignDiningServiceStyle(
            @PathVariable Long id,
            @PathVariable DiningServiceStyle style) {
        locationOperations.assignDiningServiceStyle(id, style);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/dining-service-styles")
    @Operation(summary = "Get dining service styles for location", operationId = "getLocationDiningServiceStyles")
    public ResponseEntity<ApiResponse<Set<DiningServiceStyle>>> getDiningServiceStyles(@PathVariable Long id) {
        Set<DiningServiceStyle> styles = locationOperations.findDiningServiceStylesByLocationId(id);
        return ApiResponses.ok(styles);
    }

    @PutMapping("/{id}/categories/{categoryId}")
    @Operation(summary = "Assign category to location", operationId = "assignLocationCategory")
    public ResponseEntity<ApiResponse<Void>> assignCategory(
            @PathVariable Long id,
            @PathVariable Long categoryId) {
        locationCategoryOperations.assignCategoryToLocation(id, categoryId);
        return ApiResponses.ok(null);
    }

    @GetMapping("/{id}/categories")
    @Operation(summary = "Get categories for location", operationId = "getLocationCategories")
    public ResponseEntity<ApiResponse<List<LocationCategoryResponse>>> getCategories(@PathVariable Long id) {
        List<LocationCategoryView> categories = locationCategoryOperations.findCategoriesByLocationId(id);
        List<LocationCategoryResponse> responseList = categories.stream().map(LocationWebMapper::toResponse).toList();
        return ApiResponses.ok(responseList);
    }

    @GetMapping("/{id}/business-hours")
    @Operation(summary = "Get business hours schedule for location", operationId = "getLocationBusinessHours")
    public ResponseEntity<ApiResponse<BusinessHoursScheduleResponse>> getBusinessHours(@PathVariable Long id) {
        BusinessHoursScheduleView schedule = businessHoursOperations.getSchedule(id);
        return ApiResponses.ok(LocationWebMapper.toResponse(schedule));
    }

    @PutMapping("/{id}/business-hours")
    @Operation(summary = "Replace business hours schedule for location", operationId = "replaceLocationBusinessHours")
    public ResponseEntity<ApiResponse<BusinessHoursScheduleResponse>> replaceBusinessHours(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceBusinessHoursScheduleRequest request) {
        BusinessHoursScheduleView schedule = businessHoursOperations.replaceSchedule(LocationWebMapper.toCommand(id, request));
        return ApiResponses.ok(LocationWebMapper.toResponse(schedule));
    }
}
