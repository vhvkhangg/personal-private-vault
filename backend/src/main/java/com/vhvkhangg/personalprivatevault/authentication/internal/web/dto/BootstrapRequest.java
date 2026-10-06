package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Singleton vault owner bootstrap request")
public record BootstrapRequest(
        @Schema(description = "Primary owner email address", example = "vault.owner@example.com")
        @NotBlank @Size(max = 320)
        String email,

        @Schema(description = "Owner username", example = "vaultowner")
        @NotBlank @Size(max = 100)
        String username,

        @Schema(description = "Master password", example = "strong-master-password")
        @NotBlank @Size(max = 128)
        String password,

        @Schema(description = "Numeric private PIN", example = "123456")
        @NotBlank
        String pin
) {
    @Override
    public String toString() {
        return "BootstrapRequest[email=" + email + ", username=" + username
                + ", password=[REDACTED], pin=[REDACTED]]";
    }
}
