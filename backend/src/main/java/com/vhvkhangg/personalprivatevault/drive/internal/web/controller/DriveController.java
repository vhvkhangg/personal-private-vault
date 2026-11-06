package com.vhvkhangg.personalprivatevault.drive.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveAuthorizeResponse;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveConnectionListResponse;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveDisconnectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drive")
@ConditionalOnProperty(prefix = "ppv.drive", name = "enabled", havingValue = "true")
@Tag(name = "Drive", description = "Google Drive connection and account authority management")
public class DriveController {

    public static final String CALLBACK_SUCCESS_MESSAGE = "Google Drive connection completed. Return to PPV.";
    public static final String CALLBACK_FAILURE_MESSAGE = "Google Drive connection could not be completed. Start again from PPV.";

    private final DriveConnectionService connectionService;

    public DriveController(DriveConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/connections/{slot}/authorize")
    @Operation(
            operationId = "authorizeDriveConnection",
            summary = "Initiate Google Drive OAuth authorization for connection slot",
            description = "Creates a short-lived browser-bound OAuth attempt for slot 1 or 2, setting a binding cookie and returning the consent URL.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<DriveAuthorizeResponse>> authorizeDriveConnection(
            @Parameter(description = "Target connection slot (1 or 2)", required = true)
            @PathVariable("slot") int slot,
            @AuthenticationPrincipal Jwt owner,
            HttpServletResponse response) {

        DriveConnectionService.AuthorizeResult result = connectionService.authorize(slot, owner.getSubject());

        ResponseCookie cookie = ResponseCookie.from(result.cookieName(), result.cookieValue())
                .path("/api/v1/drive/oauth/callback")
                .maxAge(300)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(result.secure())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        ApiResponse<DriveAuthorizeResponse> body = ApiResponses.success(result.response());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    @GetMapping(value = "/oauth/callback", produces = MediaType.TEXT_PLAIN_VALUE)
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Completed", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid proof or identity", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stale or fenced authority", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Local persistence failure", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Provider refusal or malformed response", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Bounded capacity exhausted", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = "Provider deadline", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    @Operation(
            operationId = "completeDriveConnection",
            summary = "Complete Google Drive OAuth authorization callback",
            description = "Public browser navigation endpoint verifying one-use state, browser cookie, identity token, and scopes to establish connection authority.",
            security = {},
            parameters = {
                    @Parameter(name = "state", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY, required = true),
                    @Parameter(name = "code", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
                    @Parameter(name = "error", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY),
                    @Parameter(name = "error_description", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)
            }
    )
    public ResponseEntity<String> completeDriveConnection(HttpServletRequest request, HttpServletResponse response) {
        for (String name : new String[]{"state", "code", "error", "error_description", "iss"}) {
            String[] values = request.getParameterValues(name);
            if (values != null && values.length != 1) return callbackResponse(400, false);
        }
        String state = request.getParameter("state");
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        if (state == null || state.isBlank() || state.length() > 128
                || (code == null) == (error == null)
                || (code != null && (code.isBlank() || code.length() > 4096))
                || (error != null && (error.isBlank() || error.length() > 256))) {
            return callbackResponse(400, false);
        }
        // State selects the slot server-side. Cookie ordering never selects account authority.
        var attempt = connectionService.findCallbackAttempt(state).orElse(null);
        String proof = null;
        if (attempt != null && request.getCookies() != null) {
            String expected = "PPV_DRIVE_BINDING_" + attempt.getSlot();
            for (Cookie cookie : request.getCookies()) {
                if (expected.equals(cookie.getName())) {
                    if (proof != null) return callbackResponse(400, false);
                    proof = cookie.getValue();
                }
            }
        }
        try {
            var result = connectionService.callback(state, code, error, null, proof);
            return callbackResponse(result.httpStatus(), result.success());
        } catch (DriveException e) {
            return callbackResponse(e.getHttpStatus(), false);
        } catch (RuntimeException e) {
            return callbackResponse(502, false);
        } finally {
            // Unknown/replayed/processing/replaced callbacks cannot erase a newer slot proof.
            if (connectionService.shouldClearCookie(attempt, proof)) clearBindingCookie(response, attempt.getSlot());
        }
    }

    private ResponseEntity<String> callbackResponse(int status, boolean success) {
        return ResponseEntity.status(status).contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Referrer-Policy", "no-referrer")
                .body(success ? CALLBACK_SUCCESS_MESSAGE : CALLBACK_FAILURE_MESSAGE);
    }

    @GetMapping("/connections")
    @Operation(
            operationId = "listDriveConnections",
            summary = "List status of both Google Drive connection slots",
            description = "Returns the status and display email for slots 1 and 2 without exposing secrets or making remote calls.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<DriveConnectionListResponse>> listDriveConnections() {
        DriveConnectionListResponse response = connectionService.listConnections();
        ApiResponse<DriveConnectionListResponse> body = ApiResponses.success(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    @DeleteMapping("/connections/{slot}")
    @Operation(
            operationId = "disconnectDriveConnection",
            summary = "Disconnect Google Drive connection slot and revoke remote credential",
            description = "Removes local connection credentials immediately and attempts one bounded best-effort remote token revocation.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<DriveDisconnectResponse>> disconnectDriveConnection(
            @Parameter(description = "Target connection slot to disconnect (1 or 2)", required = true)
            @PathVariable("slot") int slot) {

        DriveDisconnectResponse response = connectionService.disconnect(slot);
        ApiResponse<DriveDisconnectResponse> body = ApiResponses.success(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }

    private void clearBindingCookie(HttpServletResponse response, short slot) {
        ResponseCookie clearCookie = ResponseCookie.from("PPV_DRIVE_BINDING_" + slot, "")
                .path("/api/v1/drive/oauth/callback")
                .maxAge(0)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }
}
