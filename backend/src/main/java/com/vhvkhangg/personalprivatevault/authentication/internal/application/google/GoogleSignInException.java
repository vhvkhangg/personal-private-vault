package com.vhvkhangg.personalprivatevault.authentication.internal.application.google;

import lombok.Getter;

@Getter
public class GoogleSignInException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public GoogleSignInException(String errorCode, int httpStatus) {
        super(errorCode);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static GoogleSignInException invalidRequest(String detail) {
        return new GoogleSignInException("GOOGLE_SIGNIN_INVALID_REQUEST", 400);
    }

    public static GoogleSignInException invalidCredentials() {
        return new GoogleSignInException("AUTH_INVALID_CREDENTIALS", 401);
    }

    public static GoogleSignInException invalidHandoff() {
        return new GoogleSignInException("GOOGLE_HANDOFF_INVALID", 401);
    }

    public static GoogleSignInException forbidden() {
        return new GoogleSignInException("FORBIDDEN", 403);
    }

    public static GoogleSignInException ownerNotReady() {
        return new GoogleSignInException("GOOGLE_OWNER_NOT_READY", 409);
    }

    public static GoogleSignInException identityConflict() {
        return new GoogleSignInException("GOOGLE_IDENTITY_CONFLICT", 409);
    }

    public static GoogleSignInException rateLimited() {
        return new GoogleSignInException("GOOGLE_SIGNIN_RATE_LIMITED", 429);
    }

    public static GoogleSignInException unavailable() {
        return new GoogleSignInException("GOOGLE_SIGNIN_UNAVAILABLE", 503);
    }

    public static GoogleSignInException providerFailure() {
        return new GoogleSignInException("GOOGLE_SIGNIN_FAILURE", 502);
    }
}
