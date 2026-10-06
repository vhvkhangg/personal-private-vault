package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Private PIN verification request")
public record VerifyPinRequest(
        @Schema(description = "Numeric private PIN", example = "123456")
        @NotBlank
        String pin
) {
    @Override
    public String toString() {
        return "VerifyPinRequest[pin=[REDACTED]]";
    }
}
