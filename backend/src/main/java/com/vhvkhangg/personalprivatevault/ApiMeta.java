package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Metadata container within {@link ApiResponse}.
 *
 * @param page pagination metadata when present
 */
@Schema(description = "API metadata envelope")
public record ApiMeta(
        @Schema(description = "Pagination metadata")
        ApiPageMeta page
) {}
