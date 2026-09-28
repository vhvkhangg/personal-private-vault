package com.vhvkhangg.personalprivatevault.settings;

import com.vhvkhangg.personalprivatevault.settings.configuration.AppSettingsOperations;
import com.vhvkhangg.personalprivatevault.settings.configuration.InvalidSettingsException;
import com.vhvkhangg.personalprivatevault.settings.configuration.UpdateSettingsCommand;
import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppSettingsIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AppSettingsOperations settingsOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM app_settings");
        // Ensure reference currency fixture exists for testing
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol, decimal_places)
                VALUES ('USD', 'US Dollar', '$', 2)
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol, decimal_places)
                VALUES ('VND', 'Vietnamese Dong', '₫', 0)
                ON CONFLICT (code) DO NOTHING
                """);
    }

    @Test
    @DisplayName("read() returns empty when settings have not been initialized")
    void readReturnsEmptyWhenUninitialized() {
        Optional<AppSettingsView> settings = settingsOperations.read();
        assertThat(settings).isEmpty();
    }

    @Test
    @DisplayName("Initialization fails when reference currency does not exist")
    void initializationFailsWhenCurrencyMissing() {
        assertThatThrownBy(() -> settingsOperations.initialize("EUR"))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("does not exist in reference catalog");
    }

    @Test
    @DisplayName("Initializes singleton settings with Schema v1 defaults and reads back view")
    void initializesWithSchemaDefaultsAndReads() {
        AppSettingsView created = settingsOperations.initialize("usd");

        assertThat(created.defaultCurrencyCode()).isEqualTo("USD");
        assertThat(created.timezone()).isEqualTo("Asia/Ho_Chi_Minh");
        assertThat(created.paginationSize()).isEqualTo(20);
        assertThat(created.privateModeAutoLockMinutes()).isEqualTo(5);
        assertThat(created.backupEnabled()).isFalse();
        assertThat(created.backupIntervalHours()).isNull();
        assertThat(created.updatedAt()).isNotNull();

        Optional<AppSettingsView> readOpt = settingsOperations.read();
        assertThat(readOpt).isPresent();
        AppSettingsView readView = readOpt.get();
        assertThat(readView.defaultCurrencyCode()).isEqualTo(created.defaultCurrencyCode());
        assertThat(readView.timezone()).isEqualTo(created.timezone());
        assertThat(readView.paginationSize()).isEqualTo(created.paginationSize());
        assertThat(readView.privateModeAutoLockMinutes()).isEqualTo(created.privateModeAutoLockMinutes());
        assertThat(readView.backupEnabled()).isEqualTo(created.backupEnabled());
        assertThat(readView.backupIntervalHours()).isEqualTo(created.backupIntervalHours());
        assertThat(readView.updatedAt()).isCloseTo(created.updatedAt(), org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MILLIS));

        // Verify database row
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM app_settings WHERE id = 1", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates existing singleton settings with new values")
    void updatesExistingSettings() {
        settingsOperations.initialize("USD");

        UpdateSettingsCommand updateCommand = new UpdateSettingsCommand(
                "Asia/Tokyo",
                "VND",
                50,
                15,
                true,
                12
        );

        AppSettingsView updated = settingsOperations.update(updateCommand);

        assertThat(updated.timezone()).isEqualTo("Asia/Tokyo");
        assertThat(updated.defaultCurrencyCode()).isEqualTo("VND");
        assertThat(updated.paginationSize()).isEqualTo(50);
        assertThat(updated.privateModeAutoLockMinutes()).isEqualTo(15);
        assertThat(updated.backupEnabled()).isTrue();
        assertThat(updated.backupIntervalHours()).isEqualTo(12);

        Optional<AppSettingsView> readOpt = settingsOperations.read();
        assertThat(readOpt).isPresent();
        assertThat(readOpt.get().defaultCurrencyCode()).isEqualTo("VND");
        assertThat(readOpt.get().timezone()).isEqualTo("Asia/Tokyo");
    }

    @Test
    @DisplayName("Enforces PostgreSQL singleton check constraint: chk_app_settings_singleton_id")
    void enforcesSingletonIdConstraint() {
        assertThatThrownBy(() -> jdbcTemplate.execute("""
                INSERT INTO app_settings (id, timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled)
                VALUES (2, 'Asia/Ho_Chi_Minh', 'USD', 20, 5, false)
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_app_settings_singleton_id");
    }

    @Test
    @DisplayName("Enforces PostgreSQL check constraints on pagination and auto-lock")
    void enforcesPaginationAndAutoLockConstraints() {
        assertThatThrownBy(() -> jdbcTemplate.execute("""
                INSERT INTO app_settings (id, timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled)
                VALUES (1, 'Asia/Ho_Chi_Minh', 'USD', 0, 5, false)
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_app_settings_pagination_size");

        assertThatThrownBy(() -> jdbcTemplate.execute("""
                INSERT INTO app_settings (id, timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled)
                VALUES (1, 'Asia/Ho_Chi_Minh', 'USD', 20, 0, false)
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_app_settings_auto_lock");
    }

    @Test
    @DisplayName("Enforces PostgreSQL check constraints on backup interval")
    void enforcesBackupIntervalConstraints() {
        // backup_enabled true but interval is null
        assertThatThrownBy(() -> jdbcTemplate.execute("""
                INSERT INTO app_settings (id, timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours)
                VALUES (1, 'Asia/Ho_Chi_Minh', 'USD', 20, 5, true, NULL)
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_app_settings_backup_enabled_interval");

        // backup_interval_hours non-positive
        assertThatThrownBy(() -> jdbcTemplate.execute("""
                INSERT INTO app_settings (id, timezone, default_currency_code, pagination_size, private_mode_auto_lock_minutes, backup_enabled, backup_interval_hours)
                VALUES (1, 'Asia/Ho_Chi_Minh', 'USD', 20, 5, false, 0)
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_app_settings_backup_interval");
    }
}
