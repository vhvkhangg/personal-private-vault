package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Field-level validation error representation within {@link ApiError}.
 *
 * @param field field or property path that violated constraints
 * @param message human-readable validation error message
 */
@Schema(description = "Field-level validation error")
public record ApiFieldError(
        @Schema(description = "Name of the invalid property or parameter", example = "name")
        String field,

        @Schema(description = "Validation failure explanation", example = "must not be blank")
        String message
) {}
