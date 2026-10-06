package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User login credentials")
public record LoginRequest(
        @Schema(description = "Username or email identifier", example = "vaultowner")
        @NotBlank @Size(max = 320)
        String identifier,

        @Schema(description = "Password", example = "strong-master-password")
        @NotBlank @Size(max = 128)
        String password
) {
    @Override
    public String toString() {
        return "LoginRequest[identifier=" + identifier + ", password=[REDACTED]]";
    }
}
