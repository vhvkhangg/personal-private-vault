package com.vhvkhangg.personalprivatevault.settings;

import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.settings.configuration.AppSettingsOperations;
import com.vhvkhangg.personalprivatevault.settings.configuration.InvalidSettingsException;
import com.vhvkhangg.personalprivatevault.settings.configuration.UpdateSettingsCommand;
import com.vhvkhangg.personalprivatevault.settings.internal.application.configuration.AppSettingsService;
import com.vhvkhangg.personalprivatevault.settings.internal.domain.AppSettings;
import com.vhvkhangg.personalprivatevault.settings.internal.infrastructure.persistence.AppSettingsRepository;
import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppSettingsValidationTest {

    private AppSettingsRepository appSettingsRepository;
    private ReferenceCatalog referenceCatalog;
    private Clock clock;
    private AppSettingsOperations settingsOperations;

    private static final Instant FIXED_NOW = Instant.parse("2026-09-28T08:00:00Z");

    @BeforeEach
    void setUp() {
        appSettingsRepository = mock(AppSettingsRepository.class);
        referenceCatalog = mock(ReferenceCatalog.class);
        clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        settingsOperations = new AppSettingsService(appSettingsRepository, referenceCatalog, clock);

        when(referenceCatalog.currency("USD")).thenReturn(Optional.of(new CurrencyView("USD", "US Dollar", "$", 2)));
        when(referenceCatalog.currency("VND")).thenReturn(Optional.of(new CurrencyView("VND", "Vietnamese Dong", "₫", 0)));
    }

    @Nested
    @DisplayName("Initialization default tests")
    class InitializationDefaults {

        @Test
        @DisplayName("Initializes with Schema v1 defaults when only currency is provided")
        void initializesWithSchemaDefaults() {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());
            when(appSettingsRepository.save(any(AppSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AppSettingsView view = settingsOperations.initialize("usd");

            assertThat(view.defaultCurrencyCode()).isEqualTo("USD");
            assertThat(view.timezone()).isEqualTo("Asia/Ho_Chi_Minh");
            assertThat(view.paginationSize()).isEqualTo(20);
            assertThat(view.privateModeAutoLockMinutes()).isEqualTo(5);
            assertThat(view.backupEnabled()).isFalse();
            assertThat(view.backupIntervalHours()).isNull();
            assertThat(view.updatedAt()).isEqualTo(FIXED_NOW);

            ArgumentCaptor<AppSettings> captor = ArgumentCaptor.forClass(AppSettings.class);
            verify(appSettingsRepository).save(captor.capture());
            AppSettings saved = captor.getValue();
            assertThat(saved.getId()).isEqualTo(AppSettings.SINGLETON_ID);
        }
    }

    @Nested
    @DisplayName("Timezone validation tests")
    class TimezoneValidation {

        @Test
        @DisplayName("Accepts valid standard ZoneId timezone")
        void acceptsValidTimezone() {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());
            when(appSettingsRepository.save(any(AppSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USD", 25, 10, false, null);
            AppSettingsView view = settingsOperations.initializeOrUpdate(command);

            assertThat(view.timezone()).isEqualTo("UTC");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Invalid/Timezone", "Unknown", "GMT+99"})
        @DisplayName("Rejects malformed or unrecognized timezone")
        void rejectsInvalidTimezone(String invalidTimezone) {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand(invalidTimezone, "USD", 20, 5, false, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("Invalid timezone");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Rejects explicitly blank timezone on creation")
        void rejectsBlankTimezoneOnCreation(String blankTimezone) {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand(blankTimezone, "USD", 20, 5, false, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("Timezone must not be null or blank");
        }
    }

    @Nested
    @DisplayName("Currency validation tests")
    class CurrencyValidation {

        @Test
        @DisplayName("Rejects currency code not found in ReferenceCatalog")
        void rejectsNonExistentCurrency() {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());
            when(referenceCatalog.currency("EUR")).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "EUR", 20, 5, false, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("does not exist in reference catalog");
        }

        @Test
        @DisplayName("Rejects currency code exceeding 3 characters")
        void rejectsCurrencyCodeTooLong() {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USDT", 20, 5, false, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("Currency code must be at most 3 characters");
        }
    }

    @Nested
    @DisplayName("Pagination and auto-lock boundary tests")
    class NumericConstraints {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -50})
        @DisplayName("Rejects zero or negative pagination size")
        void rejectsNonPositivePaginationSize(int paginationSize) {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USD", paginationSize, 5, false, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("Pagination size must be positive");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Rejects zero or negative private mode auto-lock minutes")
        void rejectsNonPositiveAutoLock(int autoLockMinutes) {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USD", 20, autoLockMinutes, false, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("auto-lock minutes must be positive");
        }
    }

    @Nested
    @DisplayName("Backup configuration validation tests")
    class BackupConstraints {

        @Test
        @DisplayName("Rejects backup enabled when backup interval is null")
        void rejectsBackupEnabledWithoutInterval() {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USD", 20, 5, true, null);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("Backup interval hours must be specified when backup is enabled");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -24})
        @DisplayName("Rejects non-positive backup interval")
        void rejectsNonPositiveBackupInterval(int interval) {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USD", 20, 5, true, interval);

            assertThatThrownBy(() -> settingsOperations.initializeOrUpdate(command))
                    .isInstanceOf(InvalidSettingsException.class)
                    .hasMessageContaining("Backup interval hours must be positive");
        }

        @Test
        @DisplayName("Accepts backup enabled with valid positive interval")
        void acceptsValidBackupConfiguration() {
            when(appSettingsRepository.findById(AppSettings.SINGLETON_ID)).thenReturn(Optional.empty());
            when(appSettingsRepository.save(any(AppSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateSettingsCommand command = new UpdateSettingsCommand("UTC", "USD", 20, 5, true, 24);
            AppSettingsView view = settingsOperations.initializeOrUpdate(command);

            assertThat(view.backupEnabled()).isTrue();
            assertThat(view.backupIntervalHours()).isEqualTo(24);
        }
    }
}
