package com.vhvkhangg.personalprivatevault.reference;

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
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ReferenceModuleIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private ReferenceCatalog referenceCatalog;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private StoryArchetypeRepository storyArchetypeRepository;

    @Autowired
    private WorldSettingRepository worldSettingRepository;

    @Test
    @DisplayName("Countries: persist, retrieve by code, list ordered by code ascending")
    void countryCatalogOperations() {
        countryRepository.save(new Country("VN", "Vietnam", "Việt Nam"));
        countryRepository.save(new Country("US", "United States", "Hoa Kỳ"));
        countryRepository.save(new Country("JP", "Japan", "Nhật Bản"));

        Optional<CountryView> vn = referenceCatalog.country("VN");
        assertThat(vn).isPresent();
        assertThat(vn.get().code()).isEqualTo("VN");
        assertThat(vn.get().nameEn()).isEqualTo("Vietnam");
        assertThat(vn.get().nameVi()).isEqualTo("Việt Nam");

        Optional<CountryView> missing = referenceCatalog.country("XX");
        assertThat(missing).isEmpty();

        assertThat(referenceCatalog.country(null)).isEmpty();
        assertThat(referenceCatalog.country("   ")).isEmpty();

        List<CountryView> all = referenceCatalog.countries();
        assertThat(all).extracting(CountryView::code)
                .containsSubsequence("JP", "US", "VN");
    }

    @Test
    @DisplayName("Languages: persist, retrieve by code, list ordered by code ascending")
    void languageCatalogOperations() {
        languageRepository.save(new Language("vi", "Vietnamese", "Tiếng Việt"));
        languageRepository.save(new Language("en", "English", "Tiếng Anh"));
        languageRepository.save(new Language("ja", "Japanese", "Tiếng Nhật"));

        Optional<LanguageView> vi = referenceCatalog.language("vi");
        assertThat(vi).isPresent();
        assertThat(vi.get().code()).isEqualTo("vi");
        assertThat(vi.get().nameEn()).isEqualTo("Vietnamese");
        assertThat(vi.get().nameVi()).isEqualTo("Tiếng Việt");

        assertThat(referenceCatalog.language("zz")).isEmpty();
        assertThat(referenceCatalog.language(null)).isEmpty();
        assertThat(referenceCatalog.language("")).isEmpty();

        List<LanguageView> all = referenceCatalog.languages();
        assertThat(all).extracting(LanguageView::code)
                .containsSubsequence("en", "ja", "vi");
    }

    @Test
    @DisplayName("Currencies: persist, retrieve by code, list ordered by code ascending")
    void currencyCatalogOperations() {
        currencyRepository.save(new Currency("VND", "Vietnamese Dong", "₫", 0));
        currencyRepository.save(new Currency("USD", "US Dollar", "$", 2));
        currencyRepository.save(new Currency("JPY", "Japanese Yen", "¥", 0));

        Optional<CurrencyView> vnd = referenceCatalog.currency("VND");
        assertThat(vnd).isPresent();
        assertThat(vnd.get().code()).isEqualTo("VND");
        assertThat(vnd.get().name()).isEqualTo("Vietnamese Dong");
        assertThat(vnd.get().symbol()).isEqualTo("₫");
        assertThat(vnd.get().decimalPlaces()).isEqualTo(0);

        assertThat(referenceCatalog.currency("XXX")).isEmpty();
        assertThat(referenceCatalog.currency(null)).isEmpty();

        List<CurrencyView> all = referenceCatalog.currencies();
        assertThat(all).extracting(CurrencyView::code)
                .containsSubsequence("JPY", "USD", "VND");
    }

    @Test
    @DisplayName("Platforms: persist with named enum, retrieve by id, list ordered case-insensitively by name with id tie-breaker")
    void platformCatalogOperations() {
        Instant now = Instant.now();
        Platform p1 = platformRepository.save(new Platform("steam", PlatformKind.GAME, "https://store.steampowered.com", now));
        Platform p2 = platformRepository.save(new Platform("Apple App Store", PlatformKind.MARKETPLACE, "https://apps.apple.com", now));
        Platform p3 = platformRepository.save(new Platform("bilibili", PlatformKind.MEDIA, "https://bilibili.com", now));

        Optional<PlatformView> found = referenceCatalog.platform(p1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(p1.getId());
        assertThat(found.get().name()).isEqualTo("steam");
        assertThat(found.get().kind()).isEqualTo(PlatformKind.GAME);
        assertThat(found.get().url()).isEqualTo("https://store.steampowered.com");

        assertThat(referenceCatalog.platform(999999L)).isEmpty();
        assertThat(referenceCatalog.platform(null)).isEmpty();

        List<PlatformView> all = referenceCatalog.platforms();
        assertThat(all).extracting(PlatformView::name)
                .containsSubsequence("Apple App Store", "bilibili", "steam");
    }

    @Test
    @DisplayName("StoryArchetypes: persist, retrieve by id, list ordered case-insensitively by name with id tie-breaker")
    void storyArchetypeCatalogOperations() {
        StoryArchetype s1 = storyArchetypeRepository.save(new StoryArchetype("xianxia", "Immortal hero cultivation"));
        StoryArchetype s2 = storyArchetypeRepository.save(new StoryArchetype("Cyberpunk", "High tech low life"));
        StoryArchetype s3 = storyArchetypeRepository.save(new StoryArchetype("isekai", "Portal fantasy"));

        Optional<StoryArchetypeView> found = referenceCatalog.storyArchetype(s1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(s1.getId());
        assertThat(found.get().name()).isEqualTo("xianxia");
        assertThat(found.get().description()).isEqualTo("Immortal hero cultivation");

        assertThat(referenceCatalog.storyArchetype(999999L)).isEmpty();
        assertThat(referenceCatalog.storyArchetype(null)).isEmpty();

        List<StoryArchetypeView> all = referenceCatalog.storyArchetypes();
        assertThat(all).extracting(StoryArchetypeView::name)
                .containsSubsequence("Cyberpunk", "isekai", "xianxia");
    }

    @Test
    @DisplayName("WorldSettings: persist, retrieve by id, list ordered case-insensitively by name with id tie-breaker")
    void worldSettingCatalogOperations() {
        WorldSetting w1 = worldSettingRepository.save(new WorldSetting("Solar System", "Near future space"));
        WorldSetting w2 = worldSettingRepository.save(new WorldSetting("ancient china", "Historical fantasy dynasty"));
        WorldSetting w3 = worldSettingRepository.save(new WorldSetting("Post-Apocalyptic", "Ruins of modern civilisation"));

        Optional<WorldSettingView> found = referenceCatalog.worldSetting(w1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(w1.getId());
        assertThat(found.get().name()).isEqualTo("Solar System");
        assertThat(found.get().description()).isEqualTo("Near future space");

        assertThat(referenceCatalog.worldSetting(999999L)).isEmpty();
        assertThat(referenceCatalog.worldSetting(null)).isEmpty();

        List<WorldSettingView> all = referenceCatalog.worldSettings();
        assertThat(all).extracting(WorldSettingView::name)
                .containsSubsequence("ancient china", "Post-Apocalyptic", "Solar System");
    }
}
