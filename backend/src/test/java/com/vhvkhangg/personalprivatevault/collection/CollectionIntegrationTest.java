package com.vhvkhangg.personalprivatevault.collection;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicVersion;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionOperations;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareType;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.api.UpdateCollectionSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;
import com.vhvkhangg.personalprivatevault.collection.music.music.CreateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.music.InvalidMusicException;
import com.vhvkhangg.personalprivatevault.collection.music.music.MusicOperations;
import com.vhvkhangg.personalprivatevault.collection.music.music.UpdateMusicCommand;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicView;
import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.CreateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.InvalidShoppingItemException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingOperations;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.UpdateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.view.ShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;
import com.vhvkhangg.personalprivatevault.collection.software.software.CreateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.software.InvalidSoftwareItemException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareOperations;
import com.vhvkhangg.personalprivatevault.collection.software.software.UpdateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MusicOperations musicOperations;

    @Autowired
    private ShoppingOperations shoppingOperations;

    @Autowired
    private SoftwareOperations softwareOperations;

    @Autowired
    private CollectionOperations collectionOperations;

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long spotifyPlatformId;
    private Long steamPlatformId;
    private Long appStorePlatformId;

    @BeforeEach
    void setUp() {
        tearDown();

        // Seed currencies
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

        // Seed platforms
        spotifyPlatformId = jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind) VALUES ('Spotify Collection Test', 'MEDIA'::platform_kind) RETURNING id",
                Long.class
        );
        steamPlatformId = jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind) VALUES ('Steam Collection Test', 'GAME'::platform_kind) RETURNING id",
                Long.class
        );
        appStorePlatformId = jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind) VALUES ('App Store Collection Test', 'MARKETPLACE'::platform_kind) RETURNING id",
                Long.class
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM music_track_people");
        jdbcTemplate.update("DELETE FROM music_tracks");
        jdbcTemplate.update("DELETE FROM shopping_items");
        jdbcTemplate.update("DELETE FROM software_item_platforms");
        jdbcTemplate.update("DELETE FROM software_items");
        jdbcTemplate.update("DELETE FROM persons");
        jdbcTemplate.update("DELETE FROM vault_entries WHERE entry_type IN ('MUSIC', 'SHOPPING', 'SOFTWARE', 'PERSON')");
        jdbcTemplate.update("DELETE FROM platforms WHERE name LIKE '%Collection Test%'");
    }

    // =========================================================================
    // 1. Schema Validation Tests
    // =========================================================================
    @Nested
    @DisplayName("Schema validation tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Verifies all 5 Collection-owned tables exist in Schema v1")
        void verifiesCollectionTablesExist() {
            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN (" +
                    "'music_tracks', 'music_track_people', 'shopping_items', 'software_items', 'software_item_platforms')",
                    String.class
            );
            assertThat(tables).containsExactlyInAnyOrder(
                    "music_tracks",
                    "music_track_people",
                    "shopping_items",
                    "software_items",
                    "software_item_platforms"
            );
        }
    }

    // =========================================================================
    // 2. 3-Way Vault Entry Rollback Tests
    // =========================================================================
    @Nested
    @DisplayName("Vault entry rollback tests")
    class VaultEntryRollbackTests {

        @Test
        @DisplayName("Failed music creation leaves zero orphan vault entries")
        void musicFailureRollsBackVaultEntry() {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            assertThatThrownBy(() -> txTemplate.execute(status -> {
                musicOperations.create(new CreateMusicCommand("Temporary Music", MusicVersion.ORIGINAL, null, null));
                throw new RuntimeException("Simulated failure after music creation");
            })).hasMessageContaining("Simulated failure");

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'MUSIC'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Failed shopping creation leaves zero orphan vault entries")
        void shoppingFailureRollsBackVaultEntry() {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            assertThatThrownBy(() -> txTemplate.execute(status -> {
                shoppingOperations.create(new CreateShoppingItemCommand(
                        "Temporary Item", null, null, null, null, null, ShoppingStatus.WISHLIST, null, null
                ));
                throw new RuntimeException("Simulated failure after shopping creation");
            })).hasMessageContaining("Simulated failure");

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'SHOPPING'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Failed software creation leaves zero orphan vault entries")
        void softwareFailureRollsBackVaultEntry() {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            assertThatThrownBy(() -> txTemplate.execute(status -> {
                softwareOperations.create(new CreateSoftwareItemCommand(
                        "Temporary Software", SoftwareType.APPLICATION, null, null, null, null, null, null
                ));
                throw new RuntimeException("Simulated failure after software creation");
            })).hasMessageContaining("Simulated failure");

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM vault_entries WHERE entry_type = 'SOFTWARE'",
                    Integer.class
            );
            assertThat(count).isEqualTo(0);
        }
    }

    // =========================================================================
    // 3. Music Integration Tests
    // =========================================================================
    @Nested
    @DisplayName("Music integration tests")
    class MusicIntegrationTests {

        @Test
        @DisplayName("Creates, updates, normalizes defaults, clears fields, and allows duplicate music tracks")
        void createUpdateAndAllowDuplicates() {
            // Null version normalizes to ORIGINAL on create
            MusicView track1 = musicOperations.create(new CreateMusicCommand(
                    "Bohemian Rhapsody", null, spotifyPlatformId, "https://open.spotify.com/track/1"
            ));
            assertThat(track1.id()).isNotNull();
            assertThat(track1.version()).isEqualTo(MusicVersion.ORIGINAL);
            assertThat(track1.platformId()).isEqualTo(spotifyPlatformId);
            assertThat(track1.url()).isEqualTo("https://open.spotify.com/track/1");

            // Allows duplicate music records with identical fields (distinct Vault IDs)
            MusicView track2 = musicOperations.create(new CreateMusicCommand(
                    "Bohemian Rhapsody", null, spotifyPlatformId, "https://open.spotify.com/track/1"
            ));
            assertThat(track2.id()).isNotNull();
            assertThat(track2.id()).isNotEqualTo(track1.id());

            // Full replacement update: null version normalizes to ORIGINAL, null url/platform clears them
            MusicView updated = musicOperations.update(track1.id(), new UpdateMusicCommand(
                    "Bohemian Rhapsody (2011 Remaster)", null, null, null
            ));
            assertThat(updated.title()).isEqualTo("Bohemian Rhapsody (2011 Remaster)");
            assertThat(updated.version()).isEqualTo(MusicVersion.ORIGINAL);
            assertThat(updated.platformId()).isNull();
            assertThat(updated.url()).isNull();

            Optional<MusicView> found = musicOperations.findById(track1.id());
            assertThat(found).isPresent();
            assertThat(found.get().title()).isEqualTo("Bohemian Rhapsody (2011 Remaster)");
            assertThat(found.get().url()).isNull();
        }

        @Test
        @DisplayName("Music credits support both roles for one person and idempotent duplicate adds")
        void musicCreditsSupportBothRolesAndIdempotence() {
            MusicView track = musicOperations.create(new CreateMusicCommand("Yesterday", MusicVersion.ORIGINAL, null, null));
            PersonView person = personOperations.create(new CreatePersonCommand("Paul McCartney", null, null, null, null, null, null, null));

            // Adds SINGER role
            musicOperations.addCredit(track.id(), person.id(), MusicCreditRole.SINGER);

            // Adds ARTIST role for the same person on the same track (both roles coexist)
            musicOperations.addCredit(track.id(), person.id(), MusicCreditRole.ARTIST);

            // Exact duplicate add is idempotent
            musicOperations.addCredit(track.id(), person.id(), MusicCreditRole.SINGER);

            List<MusicCreditView> credits = musicOperations.findCredits(track.id(), 10);
            assertThat(credits).hasSize(2);
            // SINGER before ARTIST in enum order
            assertThat(credits.get(0).role()).isEqualTo(MusicCreditRole.SINGER);
            assertThat(credits.get(1).role()).isEqualTo(MusicCreditRole.ARTIST);
        }

        @Test
        @DisplayName("Concurrent duplicate credit addition converges safely to one row via PostgreSQL composite key")
        void concurrentMusicCreditAdditionConvergesSafely() throws Exception {
            MusicView track = musicOperations.create(new CreateMusicCommand("Let It Be", MusicVersion.ORIGINAL, null, null));
            PersonView person = personOperations.create(new CreatePersonCommand("John Lennon", null, null, null, null, null, null, null));

            Long musicId = track.id();
            Long personId = person.id();
            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, inserts credit and holds uncommitted lock
                Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    jdbcTemplate.update(
                            "INSERT INTO music_track_people (music_id, person_id, role) " +
                            "VALUES (?, ?, 'SINGER'::music_credit_role)",
                            musicId, personId
                    );
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return null;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                // Thread 2: Calls musicOperations.addCredit.
                // It issues INSERT ... ON CONFLICT DO NOTHING, which blocks on Thread 1's uncommitted composite primary key!
                Future<Void> thread2Future = executor.submit(() -> {
                    musicOperations.addCredit(musicId, personId, MusicCreditRole.SINGER);
                    return null;
                });

                // Observe PostgreSQL lock contention on music_track_people table
                awaitCompetingLock("music_track_people", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                thread1Future.get(10, TimeUnit.SECONDS);
                thread2Future.get(10, TimeUnit.SECONDS);

                // Both succeed and exactly 1 row exists
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM music_track_people WHERE music_id = ? AND person_id = ? AND role = 'SINGER'::music_credit_role",
                        Integer.class,
                        musicId, personId
                );
                assertThat(count).isEqualTo(1);
            } finally {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("Music credit reads enforce positive limit, bounded results, and person_id ASC, role ASC ordering")
        void musicCreditReadsEnforceLimitAndOrdering() {
            MusicView track = musicOperations.create(new CreateMusicCommand("Hey Jude", MusicVersion.ORIGINAL, null, null));
            PersonView p1 = personOperations.create(new CreatePersonCommand("Person A", null, null, null, null, null, null, null));
            PersonView p2 = personOperations.create(new CreatePersonCommand("Person B", null, null, null, null, null, null, null));

            musicOperations.addCredit(track.id(), p2.id(), MusicCreditRole.ARTIST);
            musicOperations.addCredit(track.id(), p2.id(), MusicCreditRole.SINGER);
            musicOperations.addCredit(track.id(), p1.id(), MusicCreditRole.ARTIST);
            musicOperations.addCredit(track.id(), p1.id(), MusicCreditRole.SINGER);

            // Rejects non-positive limit
            assertThatThrownBy(() -> musicOperations.findCredits(track.id(), 0))
                    .isInstanceOf(InvalidMusicException.class)
                    .hasMessageContaining("Limit must be positive");

            // Bounded read: limit 2
            List<MusicCreditView> bounded = musicOperations.findCredits(track.id(), 2);
            assertThat(bounded).hasSize(2);
            // Ordered by person_id ASC, role ASC (SINGER before ARTIST)
            assertThat(bounded.get(0).personId()).isEqualTo(p1.id());
            assertThat(bounded.get(0).role()).isEqualTo(MusicCreditRole.SINGER);
            assertThat(bounded.get(1).personId()).isEqualTo(p1.id());
            assertThat(bounded.get(1).role()).isEqualTo(MusicCreditRole.ARTIST);

            // Full read: limit 10
            List<MusicCreditView> all = musicOperations.findCredits(track.id(), 10);
            assertThat(all).hasSize(4);
            assertThat(all.get(2).personId()).isEqualTo(p2.id());
            assertThat(all.get(2).role()).isEqualTo(MusicCreditRole.SINGER);
            assertThat(all.get(3).personId()).isEqualTo(p2.id());
            assertThat(all.get(3).role()).isEqualTo(MusicCreditRole.ARTIST);
        }
    }

    // =========================================================================
    // 4. Shopping Integration Tests
    // =========================================================================
    @Nested
    @DisplayName("Shopping integration tests")
    class ShoppingIntegrationTests {

        @Test
        @DisplayName("Creates, updates, normalizes defaults, clears fields, and allows duplicate shopping items")
        void createUpdateAndAllowDuplicates() {
            // Null status normalizes to WISHLIST on create
            ShoppingItemView item1 = shoppingOperations.create(new CreateShoppingItemCommand(
                    "Wireless Mouse", "https://img.com/mouse.png", "Ergonomic mouse",
                    new BigDecimal("49.9900"), "USD", appStorePlatformId, null, "https://store.com/mouse", null
            ));
            assertThat(item1.id()).isNotNull();
            assertThat(item1.status()).isEqualTo(ShoppingStatus.WISHLIST);
            assertThat(item1.purchasedAt()).isNull();

            // Allows duplicate shopping records
            ShoppingItemView item2 = shoppingOperations.create(new CreateShoppingItemCommand(
                    "Wireless Mouse", null, null, null, null, null, null, null, null
            ));
            assertThat(item2.id()).isNotNull();
            assertThat(item2.id()).isNotEqualTo(item1.id());

            // Full replacement update: null clears optional fields
            ShoppingItemView updated = shoppingOperations.update(item1.id(), new UpdateShoppingItemCommand(
                    "Gaming Mouse", null, null, null, null, null, null, null, null
            ));
            assertThat(updated.name()).isEqualTo("Gaming Mouse");
            assertThat(updated.avatarUrl()).isNull();
            assertThat(updated.description()).isNull();
            assertThat(updated.priceAmount()).isNull();
            assertThat(updated.currencyCode()).isNull();
            assertThat(updated.platformId()).isNull();
            assertThat(updated.status()).isEqualTo(ShoppingStatus.WISHLIST);
            assertThat(updated.url()).isNull();
            assertThat(updated.purchasedAt()).isNull();
        }

        @Test
        @DisplayName("Enforces complete WISHLIST / PURCHASED plus purchased_at state matrix")
        void enforcesCompletePurchaseStateMatrix() {
            // WISHLIST + null purchasedAt -> valid
            ShoppingItemView wishlistItem = shoppingOperations.create(new CreateShoppingItemCommand(
                    "Wishlist Item", null, null, null, null, null, ShoppingStatus.WISHLIST, null, null
            ));
            assertThat(wishlistItem.status()).isEqualTo(ShoppingStatus.WISHLIST);
            assertThat(wishlistItem.purchasedAt()).isNull();

            // WISHLIST + non-null purchasedAt -> invalid
            assertThatThrownBy(() -> shoppingOperations.create(new CreateShoppingItemCommand(
                    "Invalid Wishlist Item", null, null, null, null, null, ShoppingStatus.WISHLIST, null, Instant.now()
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Wishlist item must not have a purchased_at timestamp");

            // PURCHASED + null purchasedAt -> valid
            ShoppingItemView purchasedNullTime = shoppingOperations.create(new CreateShoppingItemCommand(
                    "Purchased Item No Time", null, null, null, null, null, ShoppingStatus.PURCHASED, null, null
            ));
            assertThat(purchasedNullTime.status()).isEqualTo(ShoppingStatus.PURCHASED);
            assertThat(purchasedNullTime.purchasedAt()).isNull();

            // PURCHASED + non-null purchasedAt -> valid
            Instant purchaseTime = Instant.now().minus(Duration.ofDays(2));
            ShoppingItemView purchasedWithTime = shoppingOperations.create(new CreateShoppingItemCommand(
                    "Purchased Item With Time", null, null, null, null, null, ShoppingStatus.PURCHASED, null, purchaseTime
            ));
            assertThat(purchasedWithTime.status()).isEqualTo(ShoppingStatus.PURCHASED);
            assertThat(purchasedWithTime.purchasedAt()).isEqualTo(purchaseTime);

            // Transition: PURCHASED to WISHLIST with null timestamp -> valid and clears timestamp
            ShoppingItemView transitionedToWishlist = shoppingOperations.update(purchasedWithTime.id(), new UpdateShoppingItemCommand(
                    "Purchased Item With Time", null, null, null, null, null, ShoppingStatus.WISHLIST, null, null
            ));
            assertThat(transitionedToWishlist.status()).isEqualTo(ShoppingStatus.WISHLIST);
            assertThat(transitionedToWishlist.purchasedAt()).isNull();

            // Transition: WISHLIST to PURCHASED with timestamp -> valid
            Instant newPurchaseTime = Instant.now();
            ShoppingItemView transitionedToPurchased = shoppingOperations.update(transitionedToWishlist.id(), new UpdateShoppingItemCommand(
                    "Purchased Item With Time", null, null, null, null, null, ShoppingStatus.PURCHASED, null, newPurchaseTime
            ));
            assertThat(transitionedToPurchased.status()).isEqualTo(ShoppingStatus.PURCHASED);
            assertThat(transitionedToPurchased.purchasedAt()).isEqualTo(newPurchaseTime);
        }

        @Test
        @DisplayName("Validates price and currency rules against PostgreSQL constraints")
        void validatesPriceAndCurrencyAgainstPostgres() {
            // Negative price rejected
            assertThatThrownBy(() -> shoppingOperations.create(new CreateShoppingItemCommand(
                    "Bad Price", null, null, new BigDecimal("-10.00"), "USD", null, null, null, null
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Price amount must be nonnegative");

            // Price without currency rejected
            assertThatThrownBy(() -> shoppingOperations.create(new CreateShoppingItemCommand(
                    "No Currency", null, null, new BigDecimal("10.00"), null, null, null, null, null
            )))
                    .isInstanceOf(InvalidShoppingItemException.class)
                    .hasMessageContaining("Currency code is required when price amount is present");

            // Currency without price allowed
            ShoppingItemView currencyOnly = shoppingOperations.create(new CreateShoppingItemCommand(
                    "Currency Only", null, null, null, "USD", null, null, null, null
            ));
            assertThat(currencyOnly.priceAmount()).isNull();
            assertThat(currencyOnly.currencyCode()).isEqualTo("USD");
        }
    }

    // =========================================================================
    // 5. Software Integration Tests
    // =========================================================================
    @Nested
    @DisplayName("Software integration tests")
    class SoftwareIntegrationTests {

        @Test
        @DisplayName("Creates, updates, requires type, clears fields, and allows duplicate software items")
        void createUpdateAndAllowDuplicates() {
            SoftwareItemView app1 = softwareOperations.create(new CreateSoftwareItemCommand(
                    "VS Code", SoftwareType.APPLICATION, "https://img.com/vscode.png", "Code editor",
                    BigDecimal.ZERO, "USD", "https://code.visualstudio.com", "Excellent editor"
            ));
            assertThat(app1.id()).isNotNull();
            assertThat(app1.type()).isEqualTo(SoftwareType.APPLICATION);

            // Allows duplicate software records
            SoftwareItemView app2 = softwareOperations.create(new CreateSoftwareItemCommand(
                    "VS Code", SoftwareType.APPLICATION, null, null, null, null, null, null
            ));
            assertThat(app2.id()).isNotNull();
            assertThat(app2.id()).isNotEqualTo(app1.id());

            // Full replacement update: null clears optional fields
            SoftwareItemView updated = softwareOperations.update(app1.id(), new UpdateSoftwareItemCommand(
                    "Visual Studio Code", SoftwareType.APPLICATION, null, null, null, null, null, null
            ));
            assertThat(updated.name()).isEqualTo("Visual Studio Code");
            assertThat(updated.logoUrl()).isNull();
            assertThat(updated.description()).isNull();
            assertThat(updated.priceAmount()).isNull();
            assertThat(updated.currencyCode()).isNull();
            assertThat(updated.url()).isNull();
            assertThat(updated.review()).isNull();
        }

        @Test
        @DisplayName("Software platforms support zero or many platforms and idempotent duplicate adds")
        void softwarePlatformsSupportZeroOrManyAndIdempotence() {
            SoftwareItemView app = softwareOperations.create(new CreateSoftwareItemCommand(
                    "Obsidian", SoftwareType.APPLICATION, null, null, null, null, null, null
            ));

            // Zero platforms initially
            List<SoftwarePlatformView> initial = softwareOperations.findPlatforms(app.id(), 10);
            assertThat(initial).isEmpty();

            // Adds steam platform
            softwareOperations.addPlatform(app.id(), steamPlatformId);

            // Adds app store platform
            softwareOperations.addPlatform(app.id(), appStorePlatformId);

            // Exact duplicate add is idempotent
            softwareOperations.addPlatform(app.id(), steamPlatformId);

            List<SoftwarePlatformView> platforms = softwareOperations.findPlatforms(app.id(), 10);
            assertThat(platforms).hasSize(2);
        }

        @Test
        @DisplayName("Concurrent duplicate platform addition converges safely to one row via PostgreSQL composite key")
        void concurrentSoftwarePlatformAdditionConvergesSafely() throws Exception {
            SoftwareItemView app = softwareOperations.create(new CreateSoftwareItemCommand(
                    "Postman", SoftwareType.APPLICATION, null, null, null, null, null, null
            ));

            Long softwareId = app.id();
            Long platformId = steamPlatformId;
            CountDownLatch thread1Inserted = new CountDownLatch(1);
            CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            try {
                // Thread 1: In new transaction, inserts platform and holds uncommitted lock
                Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                    jdbcTemplate.update(
                            "INSERT INTO software_item_platforms (software_id, platform_id) " +
                            "VALUES (?, ?)",
                            softwareId, platformId
                    );
                    thread1Inserted.countDown();
                    try {
                        boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                        assertThat(awaited).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return null;
                }));

                assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

                // Thread 2: Calls softwareOperations.addPlatform.
                // It issues INSERT ... ON CONFLICT DO NOTHING, which blocks on Thread 1's uncommitted composite primary key!
                Future<Void> thread2Future = executor.submit(() -> {
                    softwareOperations.addPlatform(softwareId, platformId);
                    return null;
                });

                // Observe PostgreSQL lock contention on software_item_platforms table
                awaitCompetingLock("software_item_platforms", Duration.ofSeconds(5));

                // Release Thread 1 to commit
                thread2ReadyToCommit.countDown();

                thread1Future.get(10, TimeUnit.SECONDS);
                thread2Future.get(10, TimeUnit.SECONDS);

                // Both succeed and exactly 1 row exists
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM software_item_platforms WHERE software_id = ? AND platform_id = ?",
                        Integer.class,
                        softwareId, platformId
                );
                assertThat(count).isEqualTo(1);
            } finally {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("Software platform reads enforce positive limit, bounded results, and platform_id ASC ordering")
        void softwarePlatformReadsEnforceLimitAndOrdering() {
            SoftwareItemView app = softwareOperations.create(new CreateSoftwareItemCommand(
                    "Docker Desktop", SoftwareType.APPLICATION, null, null, null, null, null, null
            ));

            Long lowerPlatformId = Math.min(spotifyPlatformId, Math.min(steamPlatformId, appStorePlatformId));
            Long higherPlatformId = Math.max(spotifyPlatformId, Math.max(steamPlatformId, appStorePlatformId));

            softwareOperations.addPlatform(app.id(), higherPlatformId);
            softwareOperations.addPlatform(app.id(), lowerPlatformId);

            // Rejects non-positive limit
            assertThatThrownBy(() -> softwareOperations.findPlatforms(app.id(), 0))
                    .isInstanceOf(InvalidSoftwareItemException.class)
                    .hasMessageContaining("Limit must be positive");

            // Bounded read: limit 1
            List<SoftwarePlatformView> bounded = softwareOperations.findPlatforms(app.id(), 1);
            assertThat(bounded).hasSize(1);
            assertThat(bounded.get(0).platformId()).isEqualTo(lowerPlatformId);

            // Full read: limit 10
            List<SoftwarePlatformView> all = softwareOperations.findPlatforms(app.id(), 10);
            assertThat(all).hasSize(2);
            assertThat(all.get(0).platformId()).isEqualTo(lowerPlatformId);
            assertThat(all.get(1).platformId()).isEqualTo(higherPlatformId);
        }
    }

    // =========================================================================
    // 6. Assignment Preservation Tests
    // =========================================================================
    @Nested
    @DisplayName("Assignment preservation tests")
    class AssignmentPreservationTests {

        @Test
        @DisplayName("Scalar updates preserve assignment rows without mutating them")
        void scalarUpdatesPreserveAssignments() {
            // Music: scalar update leaves credits untouched
            MusicView track = musicOperations.create(new CreateMusicCommand("Track", MusicVersion.ORIGINAL, null, null));
            PersonView person = personOperations.create(new CreatePersonCommand("Musician", null, null, null, null, null, null, null));
            musicOperations.addCredit(track.id(), person.id(), MusicCreditRole.SINGER);

            musicOperations.update(track.id(), new UpdateMusicCommand("Updated Track", MusicVersion.COVER, null, null));
            List<MusicCreditView> credits = musicOperations.findCredits(track.id(), 10);
            assertThat(credits).hasSize(1);
            assertThat(credits.get(0).personId()).isEqualTo(person.id());

            // Software: scalar update leaves platforms untouched
            SoftwareItemView app = softwareOperations.create(new CreateSoftwareItemCommand("App", SoftwareType.APPLICATION, null, null, null, null, null, null));
            softwareOperations.addPlatform(app.id(), steamPlatformId);

            softwareOperations.update(app.id(), new UpdateSoftwareItemCommand("Updated App", SoftwareType.APPLICATION, null, null, null, null, null, null));
            List<SoftwarePlatformView> platforms = softwareOperations.findPlatforms(app.id(), 10);
            assertThat(platforms).hasSize(1);
            assertThat(platforms.get(0).platformId()).isEqualTo(steamPlatformId);
        }
    }

    // =========================================================================
    // 7. Parent Facade Integration Tests
    // =========================================================================
    @Nested
    @DisplayName("Collection parent facade integration tests")
    class CollectionFacadeIntegrationTests {

        @Test
        @DisplayName("Facade successfully delegates operations across Music, Shopping, and Software domains")
        void facadeDelegationWorksAcrossAllDomains() {
            // Music through facade
            CollectionMusicView music = collectionOperations.createMusic(new CreateCollectionMusicCommand(
                    "Facade Song", CollectionMusicVersion.ORIGINAL, spotifyPlatformId, "https://spotify.com/song"
            ));
            assertThat(music.id()).isNotNull();
            assertThat(collectionOperations.findMusicById(music.id())).isPresent();

            PersonView person = personOperations.create(new CreatePersonCommand("Facade Singer", null, null, null, null, null, null, null));
            collectionOperations.addMusicCredit(music.id(), person.id(), CollectionMusicCreditRole.SINGER);

            List<CollectionMusicCreditView> credits = collectionOperations.findMusicCredits(music.id(), 10);
            assertThat(credits).hasSize(1);
            assertThat(credits.get(0).role()).isEqualTo(CollectionMusicCreditRole.SINGER);

            // Shopping through facade
            CollectionShoppingItemView shopping = collectionOperations.createShoppingItem(new CreateCollectionShoppingItemCommand(
                    "Facade Item", null, "Shopping through facade", new BigDecimal("19.9900"), "USD", null, CollectionShoppingStatus.WISHLIST, null, null
            ));
            assertThat(shopping.id()).isNotNull();
            assertThat(collectionOperations.findShoppingItemById(shopping.id())).isPresent();

            // Software through facade
            CollectionSoftwareItemView software = collectionOperations.createSoftwareItem(new CreateCollectionSoftwareItemCommand(
                    "Facade Software", CollectionSoftwareType.EXTENSION, null, "Extension through facade", null, null, null, null
            ));
            assertThat(software.id()).isNotNull();
            assertThat(collectionOperations.findSoftwareItemById(software.id())).isPresent();

            collectionOperations.addSoftwarePlatform(software.id(), steamPlatformId);
            List<CollectionSoftwarePlatformView> platforms = collectionOperations.findSoftwarePlatforms(software.id(), 10);
            assertThat(platforms).hasSize(1);
            assertThat(platforms.get(0).platformId()).isEqualTo(steamPlatformId);
        }
    }

    private void awaitCompetingLock(String tablePattern, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l " +
                    "JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted " +
                    "  AND a.pid != pg_backend_pid() " +
                    "  AND a.query ILIKE ?",
                    Integer.class,
                    "%" + tablePattern + "%"
            );
            if (count != null && count > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
