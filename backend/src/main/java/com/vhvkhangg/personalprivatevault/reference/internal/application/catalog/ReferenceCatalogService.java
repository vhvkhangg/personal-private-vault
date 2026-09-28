package com.vhvkhangg.personalprivatevault.reference.internal.application.catalog;

import com.vhvkhangg.personalprivatevault.reference.view.CountryView;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.reference.view.LanguageView;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.StoryArchetypeView;
import com.vhvkhangg.personalprivatevault.reference.view.WorldSettingView;
import com.vhvkhangg.personalprivatevault.reference.internal.domain.Country;
import com.vhvkhangg.personalprivatevault.reference.internal.domain.Currency;
import com.vhvkhangg.personalprivatevault.reference.internal.domain.Language;
import com.vhvkhangg.personalprivatevault.reference.internal.domain.Platform;
import com.vhvkhangg.personalprivatevault.reference.internal.domain.StoryArchetype;
import com.vhvkhangg.personalprivatevault.reference.internal.domain.WorldSetting;
import com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence.CountryRepository;
import com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence.CurrencyRepository;
import com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence.LanguageRepository;
import com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence.PlatformRepository;
import com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence.StoryArchetypeRepository;
import com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence.WorldSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReferenceCatalogService implements ReferenceCatalog {

    private final CountryRepository countryRepository;
    private final LanguageRepository languageRepository;
    private final CurrencyRepository currencyRepository;
    private final PlatformRepository platformRepository;
    private final StoryArchetypeRepository storyArchetypeRepository;
    private final WorldSettingRepository worldSettingRepository;

    @Override
    public List<CountryView> countries() {
        return countryRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<CountryView> country(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return countryRepository.findById(code).map(this::toView);
    }

    @Override
    public List<LanguageView> languages() {
        return languageRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<LanguageView> language(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return languageRepository.findById(code).map(this::toView);
    }

    @Override
    public List<CurrencyView> currencies() {
        return currencyRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<CurrencyView> currency(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return currencyRepository.findById(code).map(this::toView);
    }

    @Override
    public List<PlatformView> platforms() {
        return platformRepository.findAllOrderByNameCaseInsensitive().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<PlatformView> platform(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return platformRepository.findById(id).map(this::toView);
    }

    @Override
    public List<StoryArchetypeView> storyArchetypes() {
        return storyArchetypeRepository.findAllOrderByNameCaseInsensitive().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<StoryArchetypeView> storyArchetype(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return storyArchetypeRepository.findById(id).map(this::toView);
    }

    @Override
    public List<WorldSettingView> worldSettings() {
        return worldSettingRepository.findAllOrderByNameCaseInsensitive().stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<WorldSettingView> worldSetting(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return worldSettingRepository.findById(id).map(this::toView);
    }

    private CountryView toView(Country country) {
        return new CountryView(country.getCode(), country.getNameEn(), country.getNameVi());
    }

    private LanguageView toView(Language language) {
        return new LanguageView(language.getCode(), language.getNameEn(), language.getNameVi());
    }

    private CurrencyView toView(Currency currency) {
        return new CurrencyView(currency.getCode(), currency.getName(), currency.getSymbol(), currency.getDecimalPlaces());
    }

    private PlatformView toView(Platform platform) {
        return new PlatformView(platform.getId(), platform.getName(), platform.getKind(), platform.getUrl(), platform.getCreatedAt());
    }

    private StoryArchetypeView toView(StoryArchetype archetype) {
        return new StoryArchetypeView(archetype.getId(), archetype.getName(), archetype.getDescription());
    }

    private WorldSettingView toView(WorldSetting setting) {
        return new WorldSettingView(setting.getId(), setting.getName(), setting.getDescription());
    }
}
