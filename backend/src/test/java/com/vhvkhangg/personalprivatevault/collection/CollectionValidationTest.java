package com.vhvkhangg.personalprivatevault.collection;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionOperations;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.InvalidCollectionException;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.internal.application.CollectionService;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;
import com.vhvkhangg.personalprivatevault.collection.music.internal.application.MusicService;
import com.vhvkhangg.personalprivatevault.collection.music.internal.domain.MusicTrack;
import com.vhvkhangg.personalprivatevault.collection.music.internal.infrastructure.persistence.MusicTrackPersonRepository;
import com.vhvkhangg.personalprivatevault.collection.music.internal.infrastructure.persistence.MusicTrackRepository;
import com.vhvkhangg.personalprivatevault.collection.music.music.CreateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.music.InvalidMusicException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicOperations;
import com.vhvkhangg.personalprivatevault.collection.music.music.UpdateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicView;
import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.shopping.internal.application.ShoppingService;
import com.vhvkhangg.personalprivatevault.collection.shopping.internal.domain.ShoppingItem;
import com.vhvkhangg.personalprivatevault.collection.shopping.internal.infrastructure.persistence.ShoppingItemRepository;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.CreateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.InvalidShoppingItemException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingItemNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingOperations;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.UpdateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.view.ShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;
import com.vhvkhangg.personalprivatevault.collection.software.internal.application.SoftwareService;
import com.vhvkhangg.personalprivatevault.collection.software.internal.domain.SoftwareItem;
import com.vhvkhangg.personalprivatevault.collection.software.internal.infrastructure.persistence.SoftwareItemPlatformRepository;
import com.vhvkhangg.personalprivatevault.collection.software.internal.infrastructure.persistence.SoftwareItemRepository;
import com.vhvkhangg.personalprivatevault.collection.software.software.CreateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.software.InvalidSoftwareItemException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareItemNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareOperations;
import com.vhvkhangg.personalprivatevault.collection.software.software.UpdateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.reference.view.PlatformView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionValidationTest {

    @Mock
    private MusicTrackRepository musicTrackRepository;

    @Mock
    private MusicTrackPersonRepository musicTrackPersonRepository;

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private SoftwareItemRepository softwareItemRepository;

    @Mock
    private SoftwareItemPlatformRepository softwareItemPlatformRepository;

    @Mock
    private VaultEntryOperations vaultEntryOperations;

    @Mock
    private PersonOperations personOperations;

    @Mock
    private ReferenceCatalog referenceCatalog;

    private MusicService musicService;
    private ShoppingService shoppingService;
    private SoftwareService softwareService;
    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        musicService = new MusicService(
                musicTrackRepository,
                musicTrackPersonRepository,
                vaultEntryOperations,
                personOperations,
                referenceCatalog
        );
        shoppingService = new ShoppingService(
                shoppingItemRepository,
                vaultEntryOperations,
                referenceCatalog
        );
        softwareService = new SoftwareService(
                softwareItemRepository,
                softwareItemPlatformRepository,
                vaultEntryOperations,
                referenceCatalog
        );
        collectionService = new CollectionService(
                musicService,
                shoppingService,
                softwareService
        );
    }

    // =========================================================================
    // Music Validation Tests
    // =========================================================================
    @Nested
    @DisplayName("Music validation tests")
    class MusicValidationTests {

        @Test
        @DisplayName("Rejects null create command or blank title")
        void rejectsNullOrBlankTitle() {
            assertThatThrownBy(() -> musicService.create(null))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("must not be null");

            assertThatThrownBy(() -> musicService.create(new CreateMusicCommand(null, null, null, null)))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("title must not be blank");

            assertThatThrownBy(() -> musicService.create(new CreateMusicCommand("   ", null, null, null)))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("title must not be blank");

            assertThatThrownBy(() -> musicService.create(new CreateMusicCommand("a".repeat(501), null, null, null)))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("must not exceed 500 characters");
        }

        @Test
        @DisplayName("Normalizes omitted/null version to ORIGINAL on create and update")
        void normalizesVersionToOriginal() {
            when(vaultEntryOperations.create(VaultEntryType.MUSIC))
                    .thenReturn(new VaultEntryView(100L, VaultEntryType.MUSIC, Instant.now(), Instant.now(), null));
            when(musicTrackRepository.saveAndFlush(any(MusicTrack.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            MusicView created = musicService.create(new CreateMusicCommand("Bohemian Rhapsody", null, null, null));
            assertThat(created.version()).isEqualTo(MusicVersion.ORIGINAL);

            MusicTrack existingTrack = new MusicTrack(100L, "Bohemian Rhapsody", MusicVersion.COVER, null, null);
            when(musicTrackRepository.findById(100L)).thenReturn(Optional.of(existingTrack));

            // Full replacement: omitting version normalizes to ORIGINAL, not left as COVER
            MusicView updated = musicService.update(100L, new UpdateMusicCommand("Bohemian Rhapsody", null, null, null));
            assertThat(updated.version()).isEqualTo(MusicVersion.ORIGINAL);
        }

        @Test
        @DisplayName("Validates optional platform against reference catalog")
        void validatesOptionalPlatform() {
            when(referenceCatalog.platform(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicService.create(new CreateMusicCommand("Song", null, 99L, null)))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("Platform with id 99 does not exist");
        }

        @Test
        @DisplayName("Validates URL length")
        void validatesUrlLength() {
            assertThatThrownBy(() -> musicService.create(new CreateMusicCommand("Song", null, null, "https://example.com/" + "x".repeat(2048))))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("URL must not exceed 2048 characters");
        }

        @Test
        @DisplayName("Rejects credit add with non-existent music or non-existent person")
        void rejectsInvalidCreditAdd() {
            when(musicTrackRepository.existsById(10L)).thenReturn(false);

            assertThatThrownBy(() -> musicService.addCredit(10L, 20L, MusicCreditRole.SINGER))
                    .isInstanceOf(MusicNotFoundException.class);

            when(musicTrackRepository.existsById(10L)).thenReturn(true);
            when(personOperations.find(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicService.addCredit(10L, 20L, MusicCreditRole.SINGER))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("Person with id 20 does not exist");

            assertThatThrownBy(() -> musicService.addCredit(10L, 20L, null))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("Credit role must not be null");
        }

        @Test
        @DisplayName("Rejects find credits with non-positive limit")
        void rejectsNonPositiveCreditLimit() {
            assertThatThrownBy(() -> musicService.findCredits(10L, 0))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("Limit must be positive");

            assertThatThrownBy(() -> musicService.findCredits(10L, -5))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("Limit must be positive");
        }
    }

    // =========================================================================
    // Shopping Validation Tests
    // =========================================================================
    @Nested
    @DisplayName("Shopping validation tests")
    class ShoppingValidationTests {

        @Test
        @DisplayName("Rejects null create command or blank name")
        void rejectsNullOrBlankName() {
            assertThatThrownBy(() -> shoppingService.create(null))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("must not be null");

            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand(null, null, null, null, null, null, null, null, null)))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("name must not be blank");

            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand("   ", null, null, null, null, null, null, null, null)))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("name must not be blank");
        }

        @Test
        @DisplayName("Normalizes omitted/null status to WISHLIST on create and update")
        void normalizesStatusToWishlist() {
            when(vaultEntryOperations.create(VaultEntryType.SHOPPING))
                    .thenReturn(new VaultEntryView(200L, VaultEntryType.SHOPPING, Instant.now(), Instant.now(), null));
            when(shoppingItemRepository.saveAndFlush(any(ShoppingItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ShoppingItemView created = shoppingService.create(new CreateShoppingItemCommand(
                    "Mechanical Keyboard", null, null, null, null, null, null, null, null
            ));
            assertThat(created.status()).isEqualTo(ShoppingStatus.WISHLIST);

            ShoppingItem existing = new ShoppingItem(200L, "Keyboard", null, null, null, null, null, ShoppingStatus.PURCHASED, null, Instant.now());
            when(shoppingItemRepository.findById(200L)).thenReturn(Optional.of(existing));

            // Full replacement: omitting status normalizes to WISHLIST and clears purchasedAt
            ShoppingItemView updated = shoppingService.update(200L, new UpdateShoppingItemCommand(
                    "Keyboard", null, null, null, null, null, null, null, null
            ));
            assertThat(updated.status()).isEqualTo(ShoppingStatus.WISHLIST);
            assertThat(updated.purchasedAt()).isNull();
        }

        @Test
        @DisplayName("Enforces purchase status and timestamp matrix")
        void enforcesPurchaseStatusAndTimestampMatrix() {
            // WISHLIST forbids purchasedAt
            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, null, null, null, ShoppingStatus.WISHLIST, null, Instant.now()
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Wishlist item must not have a purchased_at timestamp");

            // Defaulted WISHLIST forbids purchasedAt
            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, null, null, null, null, null, Instant.now()
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Wishlist item must not have a purchased_at timestamp");

            // PURCHASED allows null purchasedAt
            when(vaultEntryOperations.create(VaultEntryType.SHOPPING))
                    .thenReturn(new VaultEntryView(201L, VaultEntryType.SHOPPING, Instant.now(), Instant.now(), null));
            when(shoppingItemRepository.saveAndFlush(any(ShoppingItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ShoppingItemView purchasedNullTime = shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, null, null, null, ShoppingStatus.PURCHASED, null, null
            ));
            assertThat(purchasedNullTime.status()).isEqualTo(ShoppingStatus.PURCHASED);
            assertThat(purchasedNullTime.purchasedAt()).isNull();

            // PURCHASED allows non-null purchasedAt
            Instant now = Instant.now();
            ShoppingItemView purchasedWithTime = shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, null, null, null, ShoppingStatus.PURCHASED, null, now
            ));
            assertThat(purchasedWithTime.status()).isEqualTo(ShoppingStatus.PURCHASED);
            assertThat(purchasedWithTime.purchasedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Enforces price and currency validation rules")
        void enforcesPriceAndCurrencyRules() {
            // Negative price rejected
            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, new BigDecimal("-1.00"), "USD", null, null, null, null
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Price amount must be nonnegative");

            // Price without currency rejected
            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, new BigDecimal("10.00"), null, null, null, null, null
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Currency code is required when price amount is present");

            // Invalid currency code rejected
            when(referenceCatalog.currency("XYZ")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, new BigDecimal("10.00"), "XYZ", null, null, null, null
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("does not exist in reference catalog");

            // Currency without price allowed
            when(referenceCatalog.currency("USD")).thenReturn(Optional.of(new CurrencyView("USD", "US Dollar", "$", 2)));
            when(vaultEntryOperations.create(VaultEntryType.SHOPPING))
                    .thenReturn(new VaultEntryView(202L, VaultEntryType.SHOPPING, Instant.now(), Instant.now(), null));
            when(shoppingItemRepository.saveAndFlush(any(ShoppingItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ShoppingItemView item = shoppingService.create(new CreateShoppingItemCommand(
                    "Item", null, null, null, "USD", null, null, null, null
            ));
            assertThat(item.priceAmount()).isNull();
            assertThat(item.currencyCode()).isEqualTo("USD");
        }
    }

    // =========================================================================
    // Software Validation Tests
    // =========================================================================
    @Nested
    @DisplayName("Software validation tests")
    class SoftwareValidationTests {

        @Test
        @DisplayName("Requires software type on create and update")
        void requiresSoftwareType() {
            assertThatThrownBy(() -> softwareService.create(new CreateSoftwareItemCommand(
                    "IntelliJ IDEA", null, null, null, null, null, null, null
            )))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Software type is required");

            SoftwareItem existing = new SoftwareItem(300L, "IDEA", SoftwareType.APPLICATION, null, null, null, null, null, null);
            when(softwareItemRepository.findById(300L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> softwareService.update(300L, new UpdateSoftwareItemCommand(
                    "IDEA", null, null, null, null, null, null, null
            )))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Software type is required");
        }

        @Test
        @DisplayName("Enforces price and currency validation rules")
        void enforcesSoftwarePriceAndCurrencyRules() {
            assertThatThrownBy(() -> softwareService.create(new CreateSoftwareItemCommand(
                    "Tool", SoftwareType.APPLICATION, null, null, new BigDecimal("-5.00"), "USD", null, null
            )))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Price amount must be nonnegative");

            assertThatThrownBy(() -> softwareService.create(new CreateSoftwareItemCommand(
                    "Tool", SoftwareType.APPLICATION, null, null, new BigDecimal("50.00"), null, null, null
            )))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Currency code is required when price amount is present");
        }

        @Test
        @DisplayName("Rejects platform add with non-existent software or platform")
        void rejectsInvalidPlatformAdd() {
            when(softwareItemRepository.existsById(300L)).thenReturn(false);

            assertThatThrownBy(() -> softwareService.addPlatform(300L, 400L))
                    .isInstanceOf(SoftwareItemNotFoundException.class);

            when(softwareItemRepository.existsById(300L)).thenReturn(true);
            when(referenceCatalog.platform(400L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> softwareService.addPlatform(300L, 400L))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Platform with id 400 does not exist");
        }

        @Test
        @DisplayName("Rejects find platforms with non-positive limit")
        void rejectsNonPositivePlatformLimit() {
            assertThatThrownBy(() -> softwareService.findPlatforms(300L, 0))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Limit must be positive");

            assertThatThrownBy(() -> softwareService.findPlatforms(300L, -1))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Limit must be positive");
        }
    }

    // =========================================================================
    // Parent Facade Exception Translation & Delegation Tests
    // =========================================================================
    @Nested
    @DisplayName("Parent facade delegation and exception translation tests")
    class FacadeTranslationTests {

        @Test
        @DisplayName("Translates nested NotFoundExceptions to CollectionNotFoundException")
        void translatesNotFoundExceptions() {
            when(musicTrackRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> collectionService.updateMusic(999L, new UpdateCollectionMusicCommand("Song", null, null, null)))
                    .isInstanceOf(CollectionNotFoundException.class)
                    .hasMessageContaining("Music track not found: 999");

            when(shoppingItemRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> collectionService.updateShoppingItem(999L, new UpdateCollectionShoppingItemCommand("Item", null, null, null, null, null, null, null, null)))
                    .isInstanceOf(CollectionNotFoundException.class)
                    .hasMessageContaining("Shopping item not found: 999");

            when(softwareItemRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> collectionService.updateSoftwareItem(999L, new UpdateCollectionSoftwareItemCommand("App", CollectionSoftwareType.APPLICATION, null, null, null, null, null, null)))
                    .isInstanceOf(CollectionNotFoundException.class)
                    .hasMessageContaining("Software item not found: 999");
        }

        @Test
        @DisplayName("Translates nested InvalidExceptions to InvalidCollectionException")
        void translatesInvalidExceptions() {
            assertThatThrownBy(() -> collectionService.createMusic(new CreateCollectionMusicCommand("   ", null, null, null)))
                    .isInstanceOf(InvalidCollectionException.class)
                    .hasMessageContaining("Music title must not be blank");

            assertThatThrownBy(() -> collectionService.createShoppingItem(new CreateCollectionShoppingItemCommand(null, null, null, null, null, null, null, null, null)))
                    .isInstanceOf(InvalidCollectionException.class)
                    .hasMessageContaining("Shopping item name must not be blank");

            assertThatThrownBy(() -> collectionService.createSoftwareItem(new CreateCollectionSoftwareItemCommand("App", null, null, null, null, null, null, null)))
                    .isInstanceOf(InvalidCollectionException.class)
                    .hasMessageContaining("Software type is required");
        }
    }
}
