package com.vhvkhangg.personalprivatevault.reference.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.CountryResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.CurrencyResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.LanguageResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.PlatformResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.StoryArchetypeResponse;
import com.vhvkhangg.personalprivatevault.reference.internal.web.dto.WorldSettingResponse;
import com.vhvkhangg.personalprivatevault.reference.view.CountryView;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.reference.view.LanguageView;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.reference.view.StoryArchetypeView;
import com.vhvkhangg.personalprivatevault.reference.view.WorldSettingView;

public final class ReferenceWebMapper {

    private ReferenceWebMapper() {}

    public static CountryResponse toResponse(CountryView view) {
        if (view == null) return null;
        return new CountryResponse(view.code(), view.nameEn(), view.nameVi());
    }

    public static LanguageResponse toResponse(LanguageView view) {
        if (view == null) return null;
        return new LanguageResponse(view.code(), view.nameEn(), view.nameVi());
    }

    public static CurrencyResponse toResponse(CurrencyView view) {
        if (view == null) return null;
        return new CurrencyResponse(view.code(), view.name(), view.symbol(), view.decimalPlaces());
    }

    public static PlatformResponse toResponse(PlatformView view) {
        if (view == null) return null;
        return new PlatformResponse(view.id(), view.name(), view.kind(), view.url(), view.createdAt());
    }

    public static StoryArchetypeResponse toResponse(StoryArchetypeView view) {
        if (view == null) return null;
        return new StoryArchetypeResponse(view.id(), view.name(), view.description());
    }

    public static WorldSettingResponse toResponse(WorldSettingView view) {
        if (view == null) return null;
        return new WorldSettingResponse(view.id(), view.name(), view.description());
    }
}
