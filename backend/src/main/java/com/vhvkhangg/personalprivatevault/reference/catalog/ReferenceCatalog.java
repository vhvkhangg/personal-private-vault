package com.vhvkhangg.personalprivatevault.reference.catalog;

import com.vhvkhangg.personalprivatevault.reference.view.CountryView;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.reference.view.LanguageView;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.reference.view.StoryArchetypeView;
import com.vhvkhangg.personalprivatevault.reference.view.WorldSettingView;

import java.util.List;
import java.util.Optional;

/** Public read-only contract for stable reference data used by other modules. */
public interface ReferenceCatalog {
    List<CountryView> countries();
    Optional<CountryView> country(String code);
    List<LanguageView> languages();
    Optional<LanguageView> language(String code);
    List<CurrencyView> currencies();
    Optional<CurrencyView> currency(String code);
    List<PlatformView> platforms();
    Optional<PlatformView> platform(Long id);
    List<StoryArchetypeView> storyArchetypes();
    Optional<StoryArchetypeView> storyArchetype(Long id);
    List<WorldSettingView> worldSettings();
    Optional<WorldSettingView> worldSetting(Long id);
}
