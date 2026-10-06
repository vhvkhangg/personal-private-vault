package com.vhvkhangg.personalprivatevault.authentication.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AppUserResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AuthTokensResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.BootstrapRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.BootstrapStatusResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.ChangePinRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.LoginRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.RefreshTokenRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.RevokeTokenRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.VerifyPinRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.VerifyPinResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.mapper.AuthWebMapper;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PrivatePinOperations;
import com.vhvkhangg.personalprivatevault.authentication.session.SessionOperations;
import com.vhvkhangg.personalprivatevault.authentication.view.AppUserView;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Bootstrap, authentication sessions, and private PIN management")
public class AuthController {

    private final BootstrapOperations bootstrapOperations;
    private final SessionOperations sessionOperations;
    private final PrivatePinOperations privatePinOperations;

    @GetMapping("/bootstrap/status")
    @Operation(summary = "Check bootstrap status", operationId = "checkBootstrapStatus", security = {})
    public ResponseEntity<ApiResponse<BootstrapStatusResponse>> getBootstrapStatus() {
        boolean bootstrapped = bootstrapOperations.isBootstrapped();
        return ApiResponses.ok(new BootstrapStatusResponse(bootstrapped));
    }

    @PostMapping("/bootstrap")
    @Operation(summary = "Bootstrap singleton vault owner", operationId = "bootstrapVault", security = {})
    public ResponseEntity<ApiResponse<AppUserResponse>> bootstrap(@Valid @RequestBody BootstrapRequest request) {
        AppUserView userView = bootstrapOperations.bootstrap(AuthWebMapper.toCommand(request));
        return ApiResponses.created(AuthWebMapper.toResponse(userView));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username or email", operationId = "login", security = {})
    public ResponseEntity<ApiResponse<AuthTokensResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthTokensView tokens = sessionOperations.login(AuthWebMapper.toCommand(request));
        return ApiResponses.ok(AuthWebMapper.toResponse(tokens));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token for new access and refresh tokens", operationId = "refreshToken", security = {})
    public ResponseEntity<ApiResponse<AuthTokensResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokensView tokens = sessionOperations.rotate(request.refreshToken());
        return ApiResponses.ok(AuthWebMapper.toResponse(tokens));
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revoke active refresh token", operationId = "revokeToken", security = {})
    public ResponseEntity<ApiResponse<Void>> revoke(@Valid @RequestBody RevokeTokenRequest request) {
        sessionOperations.revoke(request.refreshToken());
        return ApiResponses.ok(null);
    }

    @PostMapping("/private-pin/verify")
    @Operation(summary = "Verify private-mode PIN", operationId = "verifyPrivatePin")
    public ResponseEntity<ApiResponse<VerifyPinResponse>> verifyPin(@Valid @RequestBody VerifyPinRequest request) {
        boolean verified = privatePinOperations.verifyPin(request.pin());
        return ApiResponses.ok(new VerifyPinResponse(verified));
    }

    @PutMapping("/private-pin")
    @Operation(summary = "Change private-mode PIN", operationId = "changePrivatePin")
    public ResponseEntity<ApiResponse<Void>> changePin(@Valid @RequestBody ChangePinRequest request) {
        privatePinOperations.changePin(AuthWebMapper.toCommand(request));
        return ApiResponses.ok(null);
    }
}
