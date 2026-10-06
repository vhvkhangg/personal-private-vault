package com.vhvkhangg.personalprivatevault.personal.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.CreatePersonalProfileRequest;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.PersonalProfileResponse;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.UpdatePersonalProfileRequest;
import com.vhvkhangg.personalprivatevault.personal.internal.web.mapper.PersonalWebMapper;
import com.vhvkhangg.personalprivatevault.personal.profile.PersonalProfileOperations;
import com.vhvkhangg.personalprivatevault.personal.view.PersonalProfileView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/personal/profiles")
@RequiredArgsConstructor
@Validated
@Tag(name = "PersonalProfile", description = "Personal profile management: self profile, contacts, relationships, soft-delete, and restore")
public class PersonalProfileController {

    private final PersonalProfileOperations profileOperations;

    @PostMapping
    @Operation(summary = "Create personal profile", operationId = "createPersonalProfile")
    public ResponseEntity<ApiResponse<PersonalProfileResponse>> create(@Valid @RequestBody CreatePersonalProfileRequest request) {
        PersonalProfileView created = profileOperations.createProfile(PersonalWebMapper.toCommand(request));
        return ApiResponses.created(PersonalWebMapper.toResponse(created));
    }

    @GetMapping("/self")
    @Operation(summary = "Get sole active self profile", operationId = "getSelfPersonalProfile")
    public ResponseEntity<ApiResponse<PersonalProfileResponse>> getSelf() {
        return profileOperations.findSelfProfile()
                .map(view -> ApiResponses.ok(PersonalWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "PERSONAL_PROFILE_NOT_FOUND", "Self profile not found"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get personal profile by ID", operationId = "getPersonalProfile")
    public ResponseEntity<ApiResponse<PersonalProfileResponse>> get(@PathVariable Long id) {
        PersonalProfileView view = profileOperations.findProfileById(id);
        return ApiResponses.ok(PersonalWebMapper.toResponse(view));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update personal profile", operationId = "updatePersonalProfile")
    public ResponseEntity<ApiResponse<PersonalProfileResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePersonalProfileRequest request) {
        PersonalProfileView updated = profileOperations.updateProfile(PersonalWebMapper.toCommand(id, request));
        return ApiResponses.ok(PersonalWebMapper.toResponse(updated));
    }

    @GetMapping
    @Operation(summary = "Find personal profiles", operationId = "findPersonalProfiles")
    public ResponseEntity<ApiResponse<List<PersonalProfileResponse>>> find(
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<PersonalProfileView> profiles = profileOperations.findProfiles(limit);
        return ApiResponses.ok(profiles.stream().map(PersonalWebMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete personal profile", operationId = "softDeletePersonalProfile")
    public ResponseEntity<ApiResponse<PersonalProfileResponse>> softDelete(@PathVariable Long id) {
        PersonalProfileView view = profileOperations.softDeleteProfile(id);
        return ApiResponses.ok(PersonalWebMapper.toResponse(view));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore soft-deleted personal profile", operationId = "restorePersonalProfile")
    public ResponseEntity<ApiResponse<PersonalProfileResponse>> restore(@PathVariable Long id) {
        PersonalProfileView view = profileOperations.restoreProfile(id);
        return ApiResponses.ok(PersonalWebMapper.toResponse(view));
    }
}
