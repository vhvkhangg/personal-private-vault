package com.vhvkhangg.personalprivatevault.settings.internal.application.configuration;

import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.settings.configuration.AppSettingsOperations;
import com.vhvkhangg.personalprivatevault.settings.configuration.InvalidSettingsException;
import com.vhvkhangg.personalprivatevault.settings.configuration.UpdateSettingsCommand;
import com.vhvkhangg.personalprivatevault.settings.internal.domain.AppSettings;
import com.vhvkhangg.personalprivatevault.settings.internal.infrastructure.persistence.AppSettingsRepository;
import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppSettingsService implements AppSettingsOperations {

    private final AppSettingsRepository appSettingsRepository;
    private final ReferenceCatalog referenceCatalog;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Optional<AppSettingsView> read() {
        return appSettingsRepository.findById(AppSettings.SINGLETON_ID)
                .map(this::toView);
    }

    @Override
    @Transactional
    public AppSettingsView initialize(String defaultCurrencyCode) {
        return initializeOrUpdate(UpdateSettingsCommand.initialize(defaultCurrencyCode));
    }

    @Override
    @Transactional
    public AppSettingsView update(UpdateSettingsCommand command) {
        return initializeOrUpdate(command);
    }

    @Override
    @Transactional
    public AppSettingsView initializeOrUpdate(UpdateSettingsCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        Optional<AppSettings> existingOpt = appSettingsRepository.findById(AppSettings.SINGLETON_ID);
        Instant now = clock.instant();

        if (existingOpt.isEmpty()) {
            // Initializing new singleton row - apply Schema v1 defaults for unspecified fields
            String timezone = command.timezone() == null
                    ? AppSettings.DEFAULT_TIMEZONE
                    : command.timezone().trim();
            validateTimezone(timezone);

            String currencyCode = validateAndNormalizeCurrencyCode(command.defaultCurrencyCode());

            int paginationSize = command.paginationSize() != null
                    ? command.paginationSize()
                    : AppSettings.DEFAULT_PAGINATION_SIZE;
            validatePaginationSize(paginationSize);

            int autoLockMinutes = command.privateModeAutoLockMinutes() != null
                    ? command.privateModeAutoLockMinutes()
                    : AppSettings.DEFAULT_AUTO_LOCK_MINUTES;
            validateAutoLock(autoLockMinutes);

            boolean backupEnabled = command.backupEnabled() != null
                    ? command.backupEnabled()
                    : AppSettings.DEFAULT_BACKUP_ENABLED;

            Integer backupIntervalHours = command.backupIntervalHours();
            validateBackupInterval(backupEnabled, backupIntervalHours);

            AppSettings newSettings = new AppSettings(
                    timezone,
                    currencyCode,
                    paginationSize,
                    autoLockMinutes,
                    backupEnabled,
                    backupIntervalHours,
                    now
            );

            return toView(appSettingsRepository.save(newSettings));
        }

        // Updating existing singleton row
        AppSettings settings = existingOpt.get();

        String timezone = command.timezone() != null ? command.timezone().trim() : settings.getTimezone();
        validateTimezone(timezone);

        String currencyCode = command.defaultCurrencyCode() != null
                ? validateAndNormalizeCurrencyCode(command.defaultCurrencyCode())
                : settings.getDefaultCurrencyCode();

        int paginationSize = command.paginationSize() != null
                ? command.paginationSize()
                : settings.getPaginationSize();
        validatePaginationSize(paginationSize);

        int autoLockMinutes = command.privateModeAutoLockMinutes() != null
                ? command.privateModeAutoLockMinutes()
                : settings.getPrivateModeAutoLockMinutes();
        validateAutoLock(autoLockMinutes);

        boolean backupEnabled = command.backupEnabled() != null
                ? command.backupEnabled()
                : settings.isBackupEnabled();

        Integer backupIntervalHours = command.backupIntervalHours() != null
                ? command.backupIntervalHours()
                : (command.backupEnabled() != null && !command.backupEnabled() ? null : settings.getBackupIntervalHours());
        validateBackupInterval(backupEnabled, backupIntervalHours);

        settings.update(
                timezone,
                currencyCode,
                paginationSize,
                autoLockMinutes,
                backupEnabled,
                backupIntervalHours,
                now
        );

        return toView(appSettingsRepository.save(settings));
    }

    private void validateTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            throw new InvalidSettingsException("Timezone must not be null or blank");
        }
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            throw new InvalidSettingsException("Invalid timezone: " + timezone, ex);
        }
    }

    private String validateAndNormalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidSettingsException("Default currency code must not be null or blank");
        }
        String normalized = currencyCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 3) {
            throw new InvalidSettingsException("Currency code must be at most 3 characters: " + normalized);
        }
        if (referenceCatalog.currency(normalized).isEmpty()) {
            throw new InvalidSettingsException("Currency code '" + normalized + "' does not exist in reference catalog");
        }
        return normalized;
    }

    private void validatePaginationSize(int paginationSize) {
        if (paginationSize <= 0) {
            throw new InvalidSettingsException("Pagination size must be positive: " + paginationSize);
        }
    }

    private void validateAutoLock(int minutes) {
        if (minutes <= 0) {
            throw new InvalidSettingsException("Private mode auto-lock minutes must be positive: " + minutes);
        }
    }

    private void validateBackupInterval(boolean backupEnabled, Integer backupIntervalHours) {
        if (backupEnabled && backupIntervalHours == null) {
            throw new InvalidSettingsException("Backup interval hours must be specified when backup is enabled");
        }
        if (backupIntervalHours != null && backupIntervalHours <= 0) {
            throw new InvalidSettingsException("Backup interval hours must be positive: " + backupIntervalHours);
        }
    }

    private AppSettingsView toView(AppSettings settings) {
        return new AppSettingsView(
                settings.getTimezone(),
                settings.getDefaultCurrencyCode(),
                settings.getPaginationSize(),
                settings.getPrivateModeAutoLockMinutes(),
                settings.isBackupEnabled(),
                settings.getBackupIntervalHours(),
                settings.getUpdatedAt()
        );
    }
}
