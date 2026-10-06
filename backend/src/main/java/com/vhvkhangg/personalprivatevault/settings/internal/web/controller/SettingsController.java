package com.vhvkhangg.personalprivatevault.settings.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.settings.configuration.AppSettingsOperations;
import com.vhvkhangg.personalprivatevault.settings.internal.web.dto.AppSettingsResponse;
import com.vhvkhangg.personalprivatevault.settings.internal.web.dto.UpdateSettingsRequest;
import com.vhvkhangg.personalprivatevault.settings.internal.web.mapper.SettingsWebMapper;
import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Application-wide configuration and preferences")
public class SettingsController {

    private final AppSettingsOperations appSettingsOperations;

    @GetMapping
    @Operation(summary = "Get application settings", operationId = "getSettings")
    public ResponseEntity<ApiResponse<AppSettingsResponse>> getSettings() {
        Optional<AppSettingsView> settings = appSettingsOperations.read();
        return settings.map(view -> ApiResponses.ok(SettingsWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "SETTINGS_NOT_FOUND", "Application settings have not been initialized"));
    }

    @PutMapping
    @Operation(summary = "Initialize or update application settings", operationId = "updateSettings")
    public ResponseEntity<ApiResponse<AppSettingsResponse>> updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
        AppSettingsView updated = appSettingsOperations.initializeOrUpdate(SettingsWebMapper.toCommand(request));
        return ApiResponses.ok(SettingsWebMapper.toResponse(updated));
    }
}
