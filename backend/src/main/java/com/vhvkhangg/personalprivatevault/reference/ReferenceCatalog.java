package com.vhvkhangg.personalprivatevault.reference;

import java.util.List;
import java.util.Optional;

/**
 * Public read-only contract for stable reference data used by other modules.
 *
 * <p>This interface is query-oriented in Phase 1. Reference-data mutation is not exposed cross-module yet.</p>
 */
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
