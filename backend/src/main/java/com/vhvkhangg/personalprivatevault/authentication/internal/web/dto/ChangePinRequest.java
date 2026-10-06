package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Private PIN change request")
public record ChangePinRequest(
        @Schema(description = "Existing private PIN", example = "123456")
        @NotBlank
        String currentPin,

        @Schema(description = "New private PIN", example = "654321")
        @NotBlank
        String newPin
) {
    @Override
    public String toString() {
        return "ChangePinRequest[currentPin=[REDACTED], newPin=[REDACTED]]";
    }
}
