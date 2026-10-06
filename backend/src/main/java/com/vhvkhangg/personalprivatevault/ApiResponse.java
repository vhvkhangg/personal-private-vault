package com.vhvkhangg.personalprivatevault;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Canonical unified API response envelope for all REST endpoints under {@code /api/v1/**}.
 *
 * @param <T> data payload type
 * @param data payload on successful operations; null on errors
 * @param error error details on failed operations; null on success
 * @param meta metadata such as truthful pagination; null when not applicable
 */
@Schema(description = "Standard API response envelope")
public record ApiResponse<T>(
        @Schema(description = "Data payload on success; null on error")
        T data,

        @Schema(description = "Error details on failure; null on success")
        ApiError error,

        @Schema(description = "Response metadata such as pagination; null if not applicable")
        ApiMeta meta
) {}
