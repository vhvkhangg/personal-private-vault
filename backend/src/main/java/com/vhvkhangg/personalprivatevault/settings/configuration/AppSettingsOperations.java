package com.vhvkhangg.personalprivatevault.settings.configuration;

import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;

import java.util.Optional;

/**
 * Public synchronous capability contract for reading, initializing, and updating application settings.
 */
public interface AppSettingsOperations {

    Optional<AppSettingsView> read();

    AppSettingsView initialize(String defaultCurrencyCode);

    AppSettingsView update(UpdateSettingsCommand command);

    AppSettingsView initializeOrUpdate(UpdateSettingsCommand command);
}
