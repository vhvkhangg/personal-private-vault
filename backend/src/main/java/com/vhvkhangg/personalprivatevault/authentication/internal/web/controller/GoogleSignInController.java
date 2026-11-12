package com.vhvkhangg.personalprivatevault.authentication.internal.web.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.RedeemHandoffRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.StartGoogleLinkRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.StartGoogleSignInRequest;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.GoogleSignInDtos.StartGoogleSignInResponse;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.dto.AuthTokensResponse;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth/google")
@RequiredArgsConstructor
@Tag(name = "Google Sign-In", description = "Spring-owned Google Sign-In and singleton Owner identity linking")
@ConditionalOnProperty(name = "ppv.security.google-signin.enabled", havingValue = "true")
public class GoogleSignInController {

    public static final String BIND_COOKIE_NAME = "ppv_google_signin_bind";
    public static final String HANDOFF_COOKIE_NAME = "ppv_google_handoff";
    public static final String BFF_COMPLETE_URL = "http://localhost:3000/api/bff/auth/google/complete";
    private static final int MAX_REQUEST_BODY_BYTES = 4096;

    private static final ObjectMapper STRICT_MAPPER = new ObjectMapper()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private final GoogleSignInService googleSignInService;
    private final Validator validator;

    private volatile String allowedHost = "localhost";
    private volatile int allowedPort = 8080;

    // Test seam for dynamic random-port test environments
    public void setAllowedPort(int port) {
        this.allowedPort = port;
    }

    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start Google sign-in flow", operationId = "startGoogleSignIn", security = {})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Google sign-in start request",
            required = true,
            content = @Content(schema = @Schema(implementation = StartGoogleSignInRequest.class))
    )
    public ResponseEntity<ApiResponse<StartGoogleSignInResponse>> startGoogleSignIn(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        validateHost(request);
        byte[] body = readBoundedBody(request);
        StartGoogleSignInRequest req = parseAndValidate(body, StartGoogleSignInRequest.class);

        var startResult = googleSignInService.startLogin(req.handoffChallenge());

        ResponseCookie bindCookie = ResponseCookie.from(BIND_COOKIE_NAME, startResult.bindingSecret())
                .path("/api/v1/auth/google/callback")
                .maxAge(Duration.ofSeconds(300))
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, bindCookie.toString());

        return ApiResponses.ok(new StartGoogleSignInResponse(
                startResult.attemptId(),
                startResult.authorizationUrl(),
                startResult.expiresIn()
        ));
    }

    @PostMapping(value = "/link/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start Google account linking for singleton Owner", operationId = "startOwnerGoogleLink")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Google account link start request with fresh password",
            required = true,
            content = @Content(schema = @Schema(implementation = StartGoogleLinkRequest.class))
    )
    public ResponseEntity<ApiResponse<StartGoogleSignInResponse>> startOwnerGoogleLink(
            HttpServletRequest request,
            Authentication authentication,
            HttpServletResponse response
    ) {
        validateHost(request);
        if (authentication == null || !"1".equals(authentication.getName())) {
            throw GoogleSignInException.forbidden();
        }
        boolean isPrivate = authentication.getAuthorities().stream()
                .anyMatch(a -> "PROFILE_PRIVATE".equals(a.getAuthority()));
        if (!isPrivate) {
            throw GoogleSignInException.forbidden();
        }

        byte[] body = readBoundedBody(request);
        StartGoogleLinkRequest req = parseAndValidate(body, StartGoogleLinkRequest.class);

        var startResult = googleSignInService.startLink(req.handoffChallenge(), req.password());

        ResponseCookie bindCookie = ResponseCookie.from(BIND_COOKIE_NAME, startResult.bindingSecret())
                .path("/api/v1/auth/google/callback")
                .maxAge(Duration.ofSeconds(300))
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, bindCookie.toString());

        return ApiResponses.ok(new StartGoogleSignInResponse(
                startResult.attemptId(),
                startResult.authorizationUrl(),
                startResult.expiresIn()
        ));
    }

    @GetMapping("/callback")
    @Operation(summary = "Complete Google OAuth callback", operationId = "completeGoogleSignIn", security = {})
    public ResponseEntity<Void> completeGoogleSignIn(HttpServletRequest request) {
        try {
            validateHost(request);
        } catch (Exception e) {
            return failRedirect();
        }

        String query = request.getQueryString();
        if (query != null && query.length() > 8192) {
            return failRedirect();
        }

        // Validate single-valued query parameters
        String[] codeValues = request.getParameterValues("code");
        String[] stateValues = request.getParameterValues("state");
        String[] errorValues = request.getParameterValues("error");
        if ((codeValues != null && codeValues.length > 1)
                || (stateValues != null && stateValues.length > 1)
                || (errorValues != null && errorValues.length > 1)) {
            return failRedirect();
        }

        // Validate single-valued binding cookie
        String bindingSecret = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            int bindCookieCount = 0;
            for (Cookie c : cookies) {
                if (BIND_COOKIE_NAME.equals(c.getName())) {
                    bindCookieCount++;
                    bindingSecret = c.getValue();
                }
            }
            if (bindCookieCount > 1) {
                return failRedirect();
            }
        }

        String state = stateValues != null && stateValues.length > 0 ? stateValues[0] : null;
        String code = codeValues != null && codeValues.length > 0 ? codeValues[0] : null;
        String error = errorValues != null && errorValues.length > 0 ? errorValues[0] : null;

        var outcome = googleSignInService.processCallback(state, code, error, bindingSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(BFF_COMPLETE_URL));
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
        headers.set("Referrer-Policy", "no-referrer");

        ResponseCookie clearBind = ResponseCookie.from(BIND_COOKIE_NAME, "")
                .path("/api/v1/auth/google/callback")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .build();
        headers.add(HttpHeaders.SET_COOKIE, clearBind.toString());

        if (outcome.success()) {
            ResponseCookie handoffCookie = ResponseCookie.from(HANDOFF_COOKIE_NAME, outcome.ticket())
                    .path("/api/bff/auth/google/complete")
                    .maxAge(Duration.ofSeconds(60))
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(false)
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, handoffCookie.toString());
        } else {
            ResponseCookie clearHandoff = ResponseCookie.from(HANDOFF_COOKIE_NAME, "")
                    .path("/api/bff/auth/google/complete")
                    .maxAge(Duration.ZERO)
                    .httpOnly(true)
                    .sameSite("Lax")
                    .secure(false)
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, clearHandoff.toString());
        }

        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    private ResponseEntity<Void> failRedirect() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(BFF_COMPLETE_URL));
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
        headers.set("Referrer-Policy", "no-referrer");
        ResponseCookie clearBind = ResponseCookie.from(BIND_COOKIE_NAME, "")
                .path("/api/v1/auth/google/callback")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .build();
        ResponseCookie clearHandoff = ResponseCookie.from(HANDOFF_COOKIE_NAME, "")
                .path("/api/bff/auth/google/complete")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .build();
        headers.add(HttpHeaders.SET_COOKIE, clearBind.toString());
        headers.add(HttpHeaders.SET_COOKIE, clearHandoff.toString());
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    @PostMapping(value = "/handoff", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Redeem one-use handoff ticket for PPV token pair", operationId = "redeemGoogleSignInHandoff", security = {})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Redeem handoff ticket request",
            required = true,
            content = @Content(schema = @Schema(implementation = RedeemHandoffRequest.class))
    )
    public ResponseEntity<ApiResponse<AuthTokensResponse>> redeemGoogleSignInHandoff(
            HttpServletRequest request
    ) {
        validateHost(request);
        byte[] body = readBoundedBody(request);
        RedeemHandoffRequest req = parseAndValidate(body, RedeemHandoffRequest.class);

        AuthTokensView tokens = googleSignInService.redeemHandoff(req.ticket(), req.handoffVerifier());
        return ApiResponses.ok(new AuthTokensResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.tokenType(),
                tokens.expiresIn()
        ));
    }

    private void validateHost(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            throw GoogleSignInException.invalidRequest("Unexpected X-Forwarded-Host header");
        }
        String forwardedServer = request.getHeader("X-Forwarded-Server");
        if (forwardedServer != null && !forwardedServer.isBlank()) {
            throw GoogleSignInException.invalidRequest("Unexpected X-Forwarded-Server header");
        }
        String forwardedPort = request.getHeader("X-Forwarded-Port");
        if (forwardedPort != null && !forwardedPort.isBlank()) {
            throw GoogleSignInException.invalidRequest("Unexpected X-Forwarded-Port header");
        }
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            throw GoogleSignInException.invalidRequest("Unexpected X-Forwarded-Proto header");
        }

        String host = request.getHeader("Host");
        String hostName;
        int port;

        if (host != null && !host.isBlank()) {
            if (host.contains(":")) {
                String[] parts = host.split(":", 2);
                hostName = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    throw GoogleSignInException.invalidRequest("Invalid Host header port format");
                }
            } else {
                hostName = host;
                // An absent port in HTTP implies default port for the scheme (80 for HTTP, 443 for HTTPS)
                port = request.isSecure() ? 443 : 80;
            }
        } else {
            // Fallback to serverName and serverPort (e.g. internal MockMvc dispatch without Host header)
            hostName = request.getServerName();
            port = request.getServerPort();
        }

        if (hostName == null || hostName.isBlank()) {
            throw GoogleSignInException.invalidRequest("Missing Host header");
        }

        // Literal localhost check (rejects 127.0.0.1, ::1, arbitrary domains)
        if (!allowedHost.equalsIgnoreCase(hostName)) {
            throw GoogleSignInException.invalidRequest("Invalid Host header host: " + hostName);
        }

        // Exact port check: must match allowedPort (production policy is 8080)
        if (port != allowedPort) {
            throw GoogleSignInException.invalidRequest("Invalid Host header port: " + port);
        }

        String serverName = request.getServerName();
        if (serverName != null && !serverName.isBlank() && !allowedHost.equalsIgnoreCase(serverName)) {
            throw GoogleSignInException.invalidRequest("Invalid server name: " + serverName);
        }
    }

    private static byte[] readBoundedBody(HttpServletRequest request) {
        long contentLength = request.getContentLengthLong();
        if (contentLength > MAX_REQUEST_BODY_BYTES) {
            throw GoogleSignInException.invalidRequest("Request body exceeds 4KiB limit");
        }
        try (InputStream is = request.getInputStream()) {
            byte[] buffer = new byte[MAX_REQUEST_BODY_BYTES + 1];
            int totalRead = 0;
            int read;
            while (totalRead < buffer.length && (read = is.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
                totalRead += read;
                if (totalRead > MAX_REQUEST_BODY_BYTES) {
                    throw GoogleSignInException.invalidRequest("Request body exceeds 4KiB limit");
                }
            }
            if (totalRead == 0) {
                throw GoogleSignInException.invalidRequest("Missing request body");
            }
            return Arrays.copyOf(buffer, totalRead);
        } catch (GoogleSignInException e) {
            throw e;
        } catch (Exception e) {
            throw GoogleSignInException.invalidRequest("Failed to read request body: " + e.getMessage());
        }
    }

    private <T> T parseAndValidate(byte[] body, Class<T> clazz) {
        if (body == null || body.length == 0) {
            throw GoogleSignInException.invalidRequest("Missing request body");
        }
        T parsed;
        try {
            parsed = STRICT_MAPPER.readValue(body, clazz);
        } catch (Exception e) {
            throw GoogleSignInException.invalidRequest("Malformed or duplicate JSON: " + e.getMessage());
        }
        if (parsed == null) {
            throw GoogleSignInException.invalidRequest("Missing request body");
        }
        var violations = validator.validate(parsed);
        if (!violations.isEmpty()) {
            throw GoogleSignInException.invalidRequest("Validation failed: " + violations.iterator().next().getMessage());
        }
        return parsed;
    }
}
