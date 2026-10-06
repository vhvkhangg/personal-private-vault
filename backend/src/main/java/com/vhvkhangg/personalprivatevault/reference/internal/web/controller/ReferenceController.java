package com.vhvkhangg.personalprivatevault.reference.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.CountryResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.CurrencyResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.LanguageResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.PlatformResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.StoryArchetypeResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.WorldSettingResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.mapper.ReferenceWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference")
@RequiredArgsConstructor
@Tag(name = "Reference", description = "Read-only catalogs for ISO countries, languages, currencies, platforms, story archetypes, and world settings")
public class ReferenceController {

    private final ReferenceCatalog referenceCatalog;

    @GetMapping("/countries")
    @Operation(summary = "List all reference countries", operationId = "listCountries")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> listCountries() {
        List<CountryResponse> response = referenceCatalog.countries().stream()
                .map(ReferenceWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(response);
    }

    @GetMapping("/countries/{code}")
    @Operation(summary = "Get a reference country by ISO code", operationId = "getCountry")
    public ResponseEntity<ApiResponse<CountryResponse>> getCountry(@PathVariable String code) {
        return referenceCatalog.country(code)
                .map(view -> ApiResponses.ok(ReferenceWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "COUNTRY_NOT_FOUND", "Country not found"));
    }

    @GetMapping("/languages")
    @Operation(summary = "List all reference languages", operationId = "listLanguages")
    public ResponseEntity<ApiResponse<List<LanguageResponse>>> listLanguages() {
        List<LanguageResponse> response = referenceCatalog.languages().stream()
                .map(ReferenceWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(response);
    }

    @GetMapping("/languages/{code}")
    @Operation(summary = "Get a reference language by ISO code", operationId = "getLanguage")
    public ResponseEntity<ApiResponse<LanguageResponse>> getLanguage(@PathVariable String code) {
        return referenceCatalog.language(code)
                .map(view -> ApiResponses.ok(ReferenceWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "LANGUAGE_NOT_FOUND", "Language not found"));
    }

    @GetMapping("/currencies")
    @Operation(summary = "List all reference currencies", operationId = "listCurrencies")
    public ResponseEntity<ApiResponse<List<CurrencyResponse>>> listCurrencies() {
        List<CurrencyResponse> response = referenceCatalog.currencies().stream()
                .map(ReferenceWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(response);
    }

    @GetMapping("/currencies/{code}")
    @Operation(summary = "Get a reference currency by ISO code", operationId = "getCurrency")
    public ResponseEntity<ApiResponse<CurrencyResponse>> getCurrency(@PathVariable String code) {
        return referenceCatalog.currency(code)
                .map(view -> ApiResponses.ok(ReferenceWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "CURRENCY_NOT_FOUND", "Currency not found"));
    }

    @GetMapping("/platforms")
    @Operation(summary = "List all reference platforms", operationId = "listPlatforms")
    public ResponseEntity<ApiResponse<List<PlatformResponse>>> listPlatforms() {
        List<PlatformResponse> response = referenceCatalog.platforms().stream()
                .map(ReferenceWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(response);
    }

    @GetMapping("/platforms/{id}")
    @Operation(summary = "Get a reference platform by ID", operationId = "getPlatform")
    public ResponseEntity<ApiResponse<PlatformResponse>> getPlatform(@PathVariable Long id) {
        return referenceCatalog.platform(id)
                .map(view -> ApiResponses.ok(ReferenceWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "PLATFORM_NOT_FOUND", "Platform not found"));
    }

    @GetMapping("/story-archetypes")
    @Operation(summary = "List all story archetypes", operationId = "listStoryArchetypes")
    public ResponseEntity<ApiResponse<List<StoryArchetypeResponse>>> listStoryArchetypes() {
        List<StoryArchetypeResponse> response = referenceCatalog.storyArchetypes().stream()
                .map(ReferenceWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(response);
    }

    @GetMapping("/story-archetypes/{id}")
    @Operation(summary = "Get a story archetype by ID", operationId = "getStoryArchetype")
    public ResponseEntity<ApiResponse<StoryArchetypeResponse>> getStoryArchetype(@PathVariable Long id) {
        return referenceCatalog.storyArchetype(id)
                .map(view -> ApiResponses.ok(ReferenceWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "STORY_ARCHETYPE_NOT_FOUND", "Story archetype not found"));
    }

    @GetMapping("/world-settings")
    @Operation(summary = "List all world settings", operationId = "listWorldSettings")
    public ResponseEntity<ApiResponse<List<WorldSettingResponse>>> listWorldSettings() {
        List<WorldSettingResponse> response = referenceCatalog.worldSettings().stream()
                .map(ReferenceWebMapper::toResponse)
                .toList();
        return ApiResponses.ok(response);
    }

    @GetMapping("/world-settings/{id}")
    @Operation(summary = "Get a world setting by ID", operationId = "getWorldSetting")
    public ResponseEntity<ApiResponse<WorldSettingResponse>> getWorldSetting(@PathVariable Long id) {
        return referenceCatalog.worldSetting(id)
                .map(view -> ApiResponses.ok(ReferenceWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "WORLD_SETTING_NOT_FOUND", "World setting not found"));
    }
}
