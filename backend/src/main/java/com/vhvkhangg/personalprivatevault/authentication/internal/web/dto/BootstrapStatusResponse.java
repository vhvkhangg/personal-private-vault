package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bootstrap readiness status response")
public record BootstrapStatusResponse(
        @Schema(description = "Whether the vault singleton user has already been bootstrapped", example = "true")
        boolean bootstrapped
) {}
