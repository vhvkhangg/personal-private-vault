# Backend Phase 9 — Test Verification Evidence

- Date: 2026-09-30
- Handoff ID: `backend-phase-9-collection`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:03 min

```powershell
git diff --check
```

- **Exit status:** `0` (clean, no whitespace warnings/errors)

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Test Counts and Summary

- **Total tests run:** 583
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class | Test Count | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 12 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest` | 36 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest` | 20 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest` | 18 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest` | 2 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest` | 9 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonValidationTest` | 30 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest` | 37 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest` | 13 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmValidationTest` | 44 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmLinkIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmCreditIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest` | 22 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest` | 26 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountValidationTest` | 21 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeValidationTest` | 27 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest` | 18 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionIntegrationTest` | 17 | 0 | 0 | PASS |
| **Total** | **583** | **0** | **0** | **PASS** |

## Focused Collection Regression & Invariant Tests

The 36 Collection tests across architecture, unit validation, and PostgreSQL integration are detailed below with their exact method names, nested suites, and verified behaviors:

| Test Class / Nested Suite | Test Method | Result | Verified Behavior |
| :--- | :--- | :---: | :--- |
| `CollectionArchitectureTests` | `verifiesModularStructure` | PASS | Spring Modulith verifies modular monolithic structure across all modules including closed parent facade `collection` and nested modules `collection.music`, `collection.shopping`, and `collection.software`. |
| `CollectionArchitectureTests` | `verifiesCollectionParentFacadeNamedInterface` | PASS | Collection parent facade exposes only `@NamedInterface("api")` containing public `collection.api.*` types, with zero `.internal.` classes exposed. |
| `CollectionArchitectureTests` | `verifiesNestedCollectionModulesEncapsulation` | PASS | All 3 nested modules (`collection.music`, `collection.shopping`, `collection.software`) conceal `.internal.` packages with zero leak to external modules. |
| `CollectionValidationTest$MusicValidationTests` | `rejectsNullOrBlankTitle` | PASS | Rejects null create command, null title, blank title, or title exceeding 500 characters with `InvalidMusicException`. |
| `CollectionValidationTest$MusicValidationTests` | `normalizesVersionToOriginal` | PASS | Normalizes omitted/null version to `MusicVersion.ORIGINAL` on both create and full-replacement update. |
| `CollectionValidationTest$MusicValidationTests` | `validatesOptionalPlatform` | PASS | Validates optional platform ID against `ReferenceCatalog`, rejecting non-existent platform with `InvalidMusicException`. |
| `CollectionValidationTest$MusicValidationTests` | `validatesUrlLength` | PASS | Rejects URL exceeding 2048 characters with `InvalidMusicException`. |
| `CollectionValidationTest$MusicValidationTests` | `rejectsInvalidCreditAdd` | PASS | Rejects credit addition when music track does not exist (`MusicNotFoundException`), when person does not exist in `PersonOperations` (`InvalidMusicException`), or when role is null (`InvalidMusicException`). |
| `CollectionValidationTest$MusicValidationTests` | `rejectsNonPositiveCreditLimit` | PASS | Enforces positive read limit (`> 0`) on `findCredits`, rejecting 0 and negative values with `InvalidMusicException`. |
| `CollectionValidationTest$ShoppingValidationTests` | `rejectsNullOrBlankName` | PASS | Rejects null create command, null name, or blank name with `InvalidShoppingItemException`. |
| `CollectionValidationTest$ShoppingValidationTests` | `normalizesStatusToWishlist` | PASS | Normalizes omitted/null status to `ShoppingStatus.WISHLIST` on create and full-replacement update, clearing `purchasedAt` timestamp. |
| `CollectionValidationTest$ShoppingValidationTests` | `enforcesPurchaseStatusAndTimestampMatrix` | PASS | Rejects `purchased_at` timestamp when status is `WISHLIST` (explicit or defaulted); allows null or non-null `purchased_at` when status is `PURCHASED`. |
| `CollectionValidationTest$ShoppingValidationTests` | `enforcesPriceAndCurrencyRules` | PASS | Rejects negative price amounts; rejects price amount without currency code; rejects invalid currency code not in `ReferenceCatalog`; allows currency code without price amount. |
| `CollectionValidationTest$SoftwareValidationTests` | `requiresSoftwareType` | PASS | Requires mandatory non-null software `type` on both create and update with `InvalidSoftwareItemException`. |
| `CollectionValidationTest$SoftwareValidationTests` | `enforcesSoftwarePriceAndCurrencyRules` | PASS | Rejects negative price amounts; rejects price amount without currency code with `InvalidSoftwareItemException`. |
| `CollectionValidationTest$SoftwareValidationTests` | `rejectsInvalidPlatformAdd` | PASS | Rejects platform association when software item does not exist (`SoftwareItemNotFoundException`) or when platform ID does not exist in `ReferenceCatalog` (`InvalidSoftwareItemException`). |
| `CollectionValidationTest$SoftwareValidationTests` | `rejectsNonPositivePlatformLimit` | PASS | Enforces positive read limit (`> 0`) on `findPlatforms`, rejecting 0 and negative values with `InvalidSoftwareItemException`. |
| `CollectionValidationTest$FacadeTranslationTests` | `translatesNotFoundExceptions` | PASS | Verifies parent `CollectionService` translates nested `MusicNotFoundException`, `ShoppingItemNotFoundException`, and `SoftwareItemNotFoundException` to `CollectionNotFoundException`. |
| `CollectionValidationTest$FacadeTranslationTests` | `translatesInvalidExceptions` | PASS | Verifies parent `CollectionService` translates nested `InvalidMusicException`, `InvalidShoppingItemException`, and `InvalidSoftwareItemException` to `InvalidCollectionException`. |
| `CollectionIntegrationTest$SchemaValidationTests` | `verifiesCollectionTablesExist` | PASS | Queries PostgreSQL `information_schema.tables` to confirm all 5 Collection-owned tables (`music_tracks`, `music_track_people`, `shopping_items`, `software_items`, `software_item_platforms`) exist in Schema v1. |
| `CollectionIntegrationTest$VaultEntryRollbackTests` | `musicFailureRollsBackVaultEntry` | PASS | Simulated transaction failure after music creation atomically rolls back transaction leaving 0 orphan `vault_entries` records (`MUSIC` type). |
| `CollectionIntegrationTest$VaultEntryRollbackTests` | `shoppingFailureRollsBackVaultEntry` | PASS | Simulated transaction failure after shopping creation atomically rolls back transaction leaving 0 orphan `vault_entries` records (`SHOPPING` type). |
| `CollectionIntegrationTest$VaultEntryRollbackTests` | `softwareFailureRollsBackVaultEntry` | PASS | Simulated transaction failure after software creation atomically rolls back transaction leaving 0 orphan `vault_entries` records (`SOFTWARE` type). |
| `CollectionIntegrationTest$MusicIntegrationTests` | `createUpdateAndAllowDuplicates` | PASS | Creates music track with default `ORIGINAL` version; permits duplicate music records (distinct Vault IDs); applies full scalar replacement update (clearing unsupplied optional fields to null); verifies persisted state. |
| `CollectionIntegrationTest$MusicIntegrationTests` | `musicCreditsSupportBothRolesAndIdempotence` | PASS | Allows both `SINGER` and `ARTIST` credit roles to coexist for the same person on the same track; verifies exact duplicate credit additions are idempotent without error. |
| `CollectionIntegrationTest$MusicIntegrationTests` | `concurrentMusicCreditAdditionConvergesSafely` | PASS | Thread 1 inserts credit row under uncommitted transaction; Thread 2 calls `addCredit` and reaches ungranted lock wait on PostgreSQL composite primary key; `awaitCompetingLock` observes `NOT l.granted` in `pg_locks`; after Thread 1 commits, Thread 2 completes via `ON CONFLICT DO NOTHING` and exactly 1 row persists. |
| `CollectionIntegrationTest$MusicIntegrationTests` | `musicCreditReadsEnforceLimitAndOrdering` | PASS | Rejects non-positive limit; bounds result size to specified limit; returns credits in deterministic `person_id ASC, role ASC` order (matching native PostgreSQL enum order: `SINGER` then `ARTIST`). |
| `CollectionIntegrationTest$ShoppingIntegrationTests` | `createUpdateAndAllowDuplicates` | PASS | Creates shopping item with default `WISHLIST` status; permits duplicate shopping records; applies full scalar replacement update (clearing optional fields to null); verifies persisted state. |
| `CollectionIntegrationTest$ShoppingIntegrationTests` | `enforcesCompletePurchaseStateMatrix` | PASS | Validates complete purchase matrix against PostgreSQL: `WISHLIST` with null `purchased_at` succeeds; `WISHLIST` with timestamp fails; `PURCHASED` with null or non-null timestamp succeeds; transitioning `PURCHASED -> WISHLIST` succeeds when timestamp is explicitly nullified. |
| `CollectionIntegrationTest$ShoppingIntegrationTests` | `validatesPriceAndCurrencyAgainstPostgres` | PASS | Validates price and currency against PostgreSQL: rejects negative price; rejects price without currency; allows currency code without price amount (`priceAmount == null`, `currencyCode == 'USD'`). |
| `CollectionIntegrationTest$SoftwareIntegrationTests` | `createUpdateAndAllowDuplicates` | PASS | Creates software item requiring mandatory `type`; permits duplicate software records; applies full scalar replacement update (clearing optional fields to null); verifies persisted state. |
| `CollectionIntegrationTest$SoftwareIntegrationTests` | `softwarePlatformsSupportZeroOrManyAndIdempotence` | PASS | Supports zero platforms on creation, adding multiple distinct platform associations, and idempotent duplicate platform adds via `ON CONFLICT DO NOTHING`. |
| `CollectionIntegrationTest$SoftwareIntegrationTests` | `concurrentSoftwarePlatformAdditionConvergesSafely` | PASS | Thread 1 inserts platform row under uncommitted transaction; Thread 2 calls `addPlatform` and reaches ungranted lock wait on PostgreSQL composite primary key; `awaitCompetingLock` observes `NOT l.granted` in `pg_locks`; after Thread 1 commits, Thread 2 completes via `ON CONFLICT DO NOTHING` and exactly 1 row persists. |
| `CollectionIntegrationTest$SoftwareIntegrationTests` | `softwarePlatformReadsEnforceLimitAndOrdering` | PASS | Rejects non-positive limit; bounds result size to specified limit; returns platforms in deterministic `platform_id ASC` order. |
| `CollectionIntegrationTest$AssignmentPreservationTests` | `scalarUpdatesPreserveAssignments` | PASS | Confirms that updating scalar fields on `MusicTrack` or `SoftwareItem` leaves associated assignment rows (`music_track_people`, `software_item_platforms`) untouched without mutating or deleting them. |
| `CollectionIntegrationTest$CollectionFacadeIntegrationTests` | `facadeDelegationWorksAcrossAllDomains` | PASS | Verifies parent facade `CollectionOperations` successfully delegates and maps across Music, Shopping, and Software domains end-to-end without leaking nested types. |
