package com.vhvkhangg.personalprivatevault.authentication.internal.web.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.session.ProfileSessionService;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.SecurityConfiguration;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AppUserResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AuthTokensResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.BootstrapRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.BootstrapStatusResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.ChangePinRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.ElevatePrivateRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.LoginRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.PublicCredentialRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.PublicProfileLoginRequest;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Bootstrap, authentication sessions, and private PIN management")
public class AuthController {

    private static final int MAX_REQUEST_BODY_BYTES = 4096;

    private static final ObjectMapper STRICT_MAPPER = createStrictMapper();

    private static ObjectMapper createStrictMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Array, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Object, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Binary, CoercionAction.Fail);
        return mapper;
    }

    private final BootstrapOperations bootstrapOperations;
    private final SessionOperations sessionOperations;
    private final PrivatePinOperations privatePinOperations;
    private final ProfileSessionService profileSessionService;
    private final Validator validator;

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

    @PostMapping(value = "/profiles/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Log in with fixed PUBLIC profile", operationId = "publicProfileLogin", security = {})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Public profile login request",
            required = true,
            content = @Content(schema = @Schema(implementation = PublicProfileLoginRequest.class))
    )
    public ResponseEntity<ApiResponse<AuthTokensResponse>> publicProfileLogin(
            HttpServletRequest request
    ) throws Exception {
        byte[] body = readBoundedBody(request);
        PublicProfileLoginRequest req = parseAndValidate(body, PublicProfileLoginRequest.class);
        if (!"PUBLIC".equals(req.profile())) {
            throw new IllegalArgumentException("profile must be exactly 'PUBLIC'");
        }
        AuthTokensView tokens = profileSessionService.publicLogin(req.password());
        return ApiResponses.ok(AuthWebMapper.toResponse(tokens));
    }

    @PostMapping(value = "/profiles/public-credential", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Configure or reset public profile credential", operationId = "configureOrResetPublicCredential")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Configure or reset public credential request",
            required = true,
            content = @Content(schema = @Schema(implementation = PublicCredentialRequest.class))
    )
    public ResponseEntity<ApiResponse<Void>> configureOrResetPublicCredential(
            HttpServletRequest request,
            Authentication authentication
    ) throws Exception {
        byte[] body = readBoundedBody(request);
        PublicCredentialRequest req = parseAndValidate(body, PublicCredentialRequest.class);
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
        long generation = ((Number) jwt.getClaim("generation")).longValue();
        profileSessionService.configureOrResetPublicCredential(sessionId, generation, req.privatePassword(), req.publicPassword());
        return ApiResponses.ok(null);
    }

    @PostMapping(value = "/sessions/elevate-private", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Elevate public session to private session", operationId = "elevateToPrivateSession")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Elevate to private session request",
            required = true,
            content = @Content(schema = @Schema(implementation = ElevatePrivateRequest.class))
    )
    public ResponseEntity<ApiResponse<AuthTokensResponse>> elevatePrivate(
            HttpServletRequest request,
            Authentication authentication
    ) throws Exception {
        byte[] body = readBoundedBody(request);
        ElevatePrivateRequest req = parseAndValidate(body, ElevatePrivateRequest.class);
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
        Long genVal = SecurityConfiguration.parseGeneration(jwt.getClaim("generation"));
        if (genVal == null) {
            throw new IllegalArgumentException("generation claim must be positive 64-bit integer");
        }
        long generation = genVal;
        AuthTokensView tokens = profileSessionService.elevatePrivate(sessionId, generation, req.privatePassword());
        return ApiResponses.ok(AuthWebMapper.toResponse(tokens));
    }

    @PostMapping("/sessions/downgrade-public")
    @Operation(summary = "Downgrade private session to public session", operationId = "downgradeToPublicSession")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> downgradePublic(
            Authentication authentication
    ) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
        Long genVal = SecurityConfiguration.parseGeneration(jwt.getClaim("generation"));
        if (genVal == null) {
            throw new IllegalArgumentException("generation claim must be positive 64-bit integer");
        }
        long generation = genVal;
        AuthTokensView tokens = profileSessionService.downgradePublic(sessionId, generation);
        return ApiResponses.ok(AuthWebMapper.toResponse(tokens));
    }

    private byte[] readBoundedBody(HttpServletRequest request) {
        try (InputStream in = request.getInputStream()) {
            byte[] bytes = in.readNBytes(MAX_REQUEST_BODY_BYTES + 1);
            if (bytes.length > MAX_REQUEST_BODY_BYTES) {
                throw new IllegalArgumentException("Request body exceeds maximum allowed size of 4096 bytes");
            }
            return bytes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read request body");
        }
    }

    private <T> T parseAndValidate(byte[] body, Class<T> clazz) throws com.fasterxml.jackson.core.JacksonException {
        if (body == null || body.length == 0) {
            throw new IllegalArgumentException("Request body must not be empty");
        }
        try {
            JsonNode tree = STRICT_MAPPER.readTree(body);
            if (!tree.isObject()) {
                throw new IllegalArgumentException("Request body must be a JSON object");
            }
            var fields = tree.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                JsonNode value = field.getValue();
                if (value.isNull()) {
                    throw new IllegalArgumentException("Field '" + field.getKey() + "' must not be null");
                }
                if (!value.isTextual()) {
                    throw new IllegalArgumentException("Field '" + field.getKey() + "' must be a string");
                }
            }
            T parsed = STRICT_MAPPER.readValue(body, clazz);
            if (parsed == null) {
                throw new IllegalArgumentException("Request body must not be empty");
            }
            var violations = validator.validate(parsed);
            if (!violations.isEmpty()) {
                throw new jakarta.validation.ConstraintViolationException(violations);
            }
            return parsed;
        } catch (com.fasterxml.jackson.core.JacksonException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Failed to read JSON payload: " + e.getMessage(), e);
        }
    }
}
