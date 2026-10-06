package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard error representation inside {@link ApiResponse}.
 *
 * @param code stable uppercase snake_case semantic error code
 * @param message safe summary message without sensitive details or stack traces
 * @param fieldErrors list of field-level validation errors, empty when not applicable
 */
@Schema(description = "Standard API error payload")
public record ApiError(
        @Schema(description = "Stable machine-readable error code", example = "VALIDATION_ERROR")
        String code,

        @Schema(description = "Safe human-readable error description", example = "Request validation failed")
        String message,

        @Schema(description = "Detailed list of field validation errors")
        List<ApiFieldError> fieldErrors
) {
    public ApiError {
        fieldErrors = fieldErrors != null ? List.copyOf(fieldErrors) : List.of();
    }
}
