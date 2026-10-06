package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Issued access and refresh token pair")
public record AuthTokensResponse(
        @Schema(description = "JWT Bearer access token")
        String accessToken,

        @Schema(description = "Opaque refresh token")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token validity in seconds", example = "900")
        long expiresIn
) {
    @Override
    public String toString() {
        return "AuthTokensResponse[accessToken=[REDACTED], refreshToken=[REDACTED], tokenType="
                + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
