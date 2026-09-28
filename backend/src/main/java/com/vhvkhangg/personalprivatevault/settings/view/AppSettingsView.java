package com.vhvkhangg.personalprivatevault.settings.view;

import java.time.Instant;

/**
 * Public immutable view representing application settings.
 */
public record AppSettingsView(
        String timezone,
        String defaultCurrencyCode,
        int paginationSize,
        int privateModeAutoLockMinutes,
        boolean backupEnabled,
        Integer backupIntervalHours,
        Instant updatedAt
) {
}
