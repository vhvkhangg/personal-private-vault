package com.vhvkhangg.personalprivatevault.settings.configuration;

/**
 * Command for initializing or updating application settings.
 */
public record UpdateSettingsCommand(
        String timezone,
        String defaultCurrencyCode,
        Integer paginationSize,
        Integer privateModeAutoLockMinutes,
        Boolean backupEnabled,
        Integer backupIntervalHours
) {
    public static UpdateSettingsCommand initialize(String defaultCurrencyCode) {
        return new UpdateSettingsCommand(null, defaultCurrencyCode, null, null, null, null);
    }
}
