package com.vhvkhangg.personalprivatevault.authentication.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.InvalidBootstrapException;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.UserAlreadyBootstrappedException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.InvalidPinException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PinVerificationException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.UnauthenticatedAccessException;
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidCredentialsException;
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidRefreshTokenException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.authentication.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionAdvice {

    @ExceptionHandler(UserAlreadyBootstrappedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyBootstrapped(UserAlreadyBootstrappedException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "AUTH_ALREADY_BOOTSTRAPPED", "Vault singleton user has already been bootstrapped");
    }

    @ExceptionHandler(InvalidBootstrapException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBootstrap(InvalidBootstrapException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "AUTH_INVALID_BOOTSTRAP", "Invalid bootstrap data");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ApiResponses.of(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid username/email or password");
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ApiResponses.of(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN", "Invalid, expired, or revoked refresh token");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.authentication.internal.application.session.PublicNotConfiguredException.class)
    public ResponseEntity<ApiResponse<Void>> handlePublicNotConfigured(com.vhvkhangg.personalprivatevault.authentication.internal.application.session.PublicNotConfiguredException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "AUTH_PUBLIC_NOT_CONFIGURED", "Public login profile is not configured");
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.authentication.internal.application.session.PasswordNotDistinctException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordNotDistinct(com.vhvkhangg.personalprivatevault.authentication.internal.application.session.PasswordNotDistinctException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "AUTH_PASSWORD_NOT_DISTINCT", "Public password must be distinct from private password");
    }

    @ExceptionHandler(PinVerificationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePinVerification(PinVerificationException ex) {
        return ApiResponses.of(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_PIN", "PIN verification failed");
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPin(InvalidPinException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "AUTH_INVALID_PIN_FORMAT", "Invalid PIN format");
    }

    @ExceptionHandler(UnauthenticatedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthenticatedAccess(UnauthenticatedAccessException ex) {
        return ApiResponses.of(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Authentication required to access PIN operations");
    }

    @ExceptionHandler(com.fasterxml.jackson.core.JacksonException.class)
    public ResponseEntity<ApiResponse<Void>> handleJacksonException(com.fasterxml.jackson.core.JacksonException ex) {
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request payload is invalid, malformed, or contains unrecognized fields");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponses.of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException.class)
    public ResponseEntity<ApiResponse<Void>> handleGoogleSignIn(
            com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());
        String message = switch (ex.getErrorCode()) {
            case "AUTH_INVALID_CREDENTIALS" -> "Invalid username/email or password";
            case "GOOGLE_HANDOFF_INVALID" -> "Invalid, expired, or replayed Google handoff ticket";
            case "FORBIDDEN" -> "Access denied";
            case "GOOGLE_OWNER_NOT_READY" -> "Google Sign-In is not linked or owner is not bootstrapped";
            case "GOOGLE_IDENTITY_CONFLICT" -> "Conflicting Google identity";
            case "GOOGLE_SIGNIN_RATE_LIMITED" -> "Too many attempts; rate limit exceeded";
            case "GOOGLE_SIGNIN_UNAVAILABLE" -> "Google Sign-In service is temporarily unavailable";
            case "GOOGLE_SIGNIN_FAILURE" -> "Google provider communication failed";
            default -> "Invalid Google sign-in request";
        };
        return ApiResponses.of(status, ex.getErrorCode(), message);
    }
}
