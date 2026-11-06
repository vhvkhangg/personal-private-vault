package com.vhvkhangg.personalprivatevault.drive.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Response returned after initiating Google Drive OAuth authorization")
public record DriveAuthorizeResponse(
        @Schema(description = "Target connection slot (1 or 2)", example = "1")
        int slot,

        @Schema(description = "Generated Google OAuth consent URL", example = "https://accounts.google.com/o/oauth2/v2/auth?...")
        String authorizationUrl,

        @Schema(description = "ISO-8601 UTC timestamp when the attempt expires", example = "2026-11-06T00:05:00Z")
        Instant expiresAt
) {
    @Override public String toString() { return "DriveAuthorizeResponse[REDACTED]"; }
}
