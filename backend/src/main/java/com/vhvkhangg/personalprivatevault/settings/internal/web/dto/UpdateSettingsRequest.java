package com.vhvkhangg.personalprivatevault.settings.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Initialize or update application settings request")
public record UpdateSettingsRequest(
        @Schema(description = "IANA timezone ID", example = "Asia/Ho_Chi_Minh")
        @Size(max = 64)
        String timezone,

        @Schema(description = "Default ISO 4217 currency code", example = "VND")
        @Size(min = 3, max = 3)
        String defaultCurrencyCode,

        @Schema(description = "Default pagination page size", example = "50")
        @Min(1)
        Integer paginationSize,

        @Schema(description = "Inactivity minutes before private-mode auto-locks", example = "15")
        @Min(1)
        Integer privateModeAutoLockMinutes,

        @Schema(description = "Whether automated backups are enabled", example = "true")
        Boolean backupEnabled,

        @Schema(description = "Interval in hours between automated backups", example = "24")
        @Min(1)
        Integer backupIntervalHours
) {}
