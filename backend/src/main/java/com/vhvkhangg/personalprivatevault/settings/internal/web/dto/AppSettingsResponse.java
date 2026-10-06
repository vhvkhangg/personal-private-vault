package com.vhvkhangg.personalprivatevault.settings.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Application settings view")
public record AppSettingsResponse(
        @Schema(description = "IANA timezone ID", example = "Asia/Ho_Chi_Minh")
        String timezone,

        @Schema(description = "Default ISO 4217 currency code", example = "VND")
        String defaultCurrencyCode,

        @Schema(description = "Default pagination page size", example = "50")
        int paginationSize,

        @Schema(description = "Inactivity minutes before private-mode auto-locks", example = "15")
        int privateModeAutoLockMinutes,

        @Schema(description = "Whether automated backups are enabled", example = "true")
        boolean backupEnabled,

        @Schema(description = "Interval in hours between automated backups", example = "24")
        Integer backupIntervalHours,

        @Schema(description = "Last update timestamp in UTC")
        Instant updatedAt
) {}
