package com.vhvkhangg.personalprivatevault.settings.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.settings.configuration.UpdateSettingsCommand;
import com.vhvkhangg.personalprivatevault.settings.internal.web.dto.AppSettingsResponse;
import com.vhvkhangg.personalprivatevault.settings.internal.web.dto.UpdateSettingsRequest;
import com.vhvkhangg.personalprivatevault.settings.view.AppSettingsView;

public final class SettingsWebMapper {

    private SettingsWebMapper() {}

    public static UpdateSettingsCommand toCommand(UpdateSettingsRequest request) {
        return new UpdateSettingsCommand(
                request.timezone(),
                request.defaultCurrencyCode(),
                request.paginationSize(),
                request.privateModeAutoLockMinutes(),
                request.backupEnabled(),
                request.backupIntervalHours()
        );
    }

    public static AppSettingsResponse toResponse(AppSettingsView view) {
        return new AppSettingsResponse(
                view.timezone(),
                view.defaultCurrencyCode(),
                view.paginationSize(),
                view.privateModeAutoLockMinutes(),
                view.backupEnabled(),
                view.backupIntervalHours(),
                view.updatedAt()
        );
    }
}
