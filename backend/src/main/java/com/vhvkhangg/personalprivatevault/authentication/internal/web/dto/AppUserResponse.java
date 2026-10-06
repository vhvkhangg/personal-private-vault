package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Singleton vault owner user view")
public record AppUserResponse(
        @Schema(description = "User identifier", example = "1")
        Short id,

        @Schema(description = "User email", example = "vault.owner@example.com")
        String email,

        @Schema(description = "User username", example = "vaultowner")
        String username,

        @Schema(description = "Creation timestamp in UTC")
        Instant createdAt,

        @Schema(description = "Last update timestamp in UTC")
        Instant updatedAt
) {}
