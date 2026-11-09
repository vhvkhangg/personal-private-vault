package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class GoogleSignInDtos {

    private GoogleSignInDtos() {}

    @Schema(description = "Request to initiate Google OIDC login flow")
    public record StartGoogleSignInRequest(
            @Schema(description = "SHA-256 lowercase hex of the client-held verifier", example = "a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90")
            @NotBlank
            @Size(min = 64, max = 64)
            @Pattern(regexp = "^[0-9a-f]{64}$", message = "handoffChallenge must be a 64-character lowercase hex string")
            String handoffChallenge
    ) {}

    @Schema(description = "Request to initiate Google OIDC account linking flow for singleton Owner")
    public record StartGoogleLinkRequest(
            @Schema(description = "SHA-256 lowercase hex of the client-held verifier", example = "a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90")
            @NotBlank
            @Size(min = 64, max = 64)
            @Pattern(regexp = "^[0-9a-f]{64}$", message = "handoffChallenge must be a 64-character lowercase hex string")
            String handoffChallenge,

            @Schema(description = "Freshly verified singleton Owner password", example = "secretPassword123")
            @NotBlank
            @Size(min = 1, max = 128)
            String password
    ) {
        @Override
        public String toString() {
            return "StartGoogleLinkRequest[handoffChallenge=" + handoffChallenge + ", password=[REDACTED]]";
        }
    }

    @Schema(description = "Response containing authorization redirect URL and attempt metadata")
    public record StartGoogleSignInResponse(
            @Schema(description = "Internal attempt identifier")
            UUID attemptId,

            @Schema(description = "Google authorization consent URL")
            String authorizationUrl,

            @Schema(description = "Attempt expiration in seconds", example = "300")
            int expiresIn
    ) {
        @Override
        public String toString() {
            return "StartGoogleSignInResponse[attemptId=" + attemptId + ", authorizationUrl=[REDACTED]" +
                    ", expiresIn=" + expiresIn + "]";
        }
    }

    @Schema(description = "Request to redeem one-use handoff ticket for canonical PPV token pair")
    public record RedeemHandoffRequest(
            @Schema(description = "One-use ticket issued by callback", example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v")
            @NotBlank
            @Size(min = 43, max = 43)
            @Pattern(regexp = "^[a-zA-Z0-9_-]{43}$", message = "ticket must be a 43-character base64url string")
            String ticket,

            @Schema(description = "Raw handoff verifier matching the initial handoff challenge", example = "v1u0t9s8r7q6p5o4n3m2l1k0j9i8h7g6f5e4d3c2b1a")
            @NotBlank
            @Size(min = 43, max = 43)
            @Pattern(regexp = "^[a-zA-Z0-9_-]{43}$", message = "handoffVerifier must be a 43-character base64url string")
            String handoffVerifier
    ) {
        @Override
        public String toString() {
            return "RedeemHandoffRequest[ticket=[REDACTED], handoffVerifier=[REDACTED]]";
        }
    }
}
