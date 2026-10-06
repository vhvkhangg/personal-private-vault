package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token revocation request")
public record RevokeTokenRequest(
        @Schema(description = "Opaque refresh token to revoke")
        @NotBlank
        String refreshToken
) {
    @Override
    public String toString() {
        return "RevokeTokenRequest[refreshToken=[REDACTED]]";
    }
}
