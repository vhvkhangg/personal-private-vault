# Backend Phase 6 — Test Verification Evidence

- Date: 2026-09-29
- Handoff ID: `backend-phase-6-media-location`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 52.064 s

## Test Counts and Summary

- **Total tests run:** 458
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class                                                                                 | Test Count | Failures | Errors | Result   |
| :----------------------------------------------------------------------------------------- | :--------: | :------: | :----: | :------: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests`                          |     12     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest`       |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest`              |     6      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest`                       |     36     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest`                       |     5      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest`                    |     11     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest`                    |     20     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest`                   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest`                |     18     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest`                      |     16     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest`                    |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest`                    |     4      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest`                     |     2      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest`                     |     3      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` |     4      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest`   |     9      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest`              |     3      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest`     |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.PersonValidationTest`                           |     30     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupValidationTest`                     |     16     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.PersonIntegrationTest`                          |     11     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupIntegrationTest`                    |     15     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest`                         |     37     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest`                  |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest`                   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest`                       |     13     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.film.FilmValidationTest`                               |     44     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.film.FilmGenreIntegrationTest`                         |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.film.FilmLinkIntegrationTest`                          |     6      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.film.FilmCreditIntegrationTest`                        |     5      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.film.FilmIntegrationTest`                              |     11     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest`                             |     23     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest`                            |     10     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest`                       |     32     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest`                     |     16     |    0     |   0    |   PASS   |
| **Total**                                                                                  |  **458**   |  **0**   | **0**  | **PASS** |

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Architecture & Module Boundary Verification

`ApplicationArchitectureTests` passed cleanly via Spring Modulith:
- `media` module allowed dependencies are strictly narrowed to:
  - `vault :: entry` (`VaultEntryOperations`)
  - `vault :: enums` (`VaultEntryType.ALBUM`, `VaultEntryType.IMAGE`)
  - `vault :: view` (`VaultEntryView`)
- `media` exposes deliberate semantic named-interface subpackages:
  - `media.album` (`@NamedInterface("album")`: `AlbumOperations`, commands, domain exceptions)
  - `media.image` (`@NamedInterface("image")`: `ImageOperations`, commands, `ImageConflictException`, domain exceptions)
  - `media.view` (`@NamedInterface("view")`: `AlbumView`, `ImageView`)
- `location` module allowed dependencies are strictly narrowed to:
  - `vault :: entry` (`VaultEntryOperations`)
  - `vault :: enums` (`VaultEntryType.BRAND`, `VaultEntryType.LOCATION`)
  - `vault :: view` (`VaultEntryView`)
  - `reference :: catalog` (`ReferenceCatalog`)
  - `reference :: view` (`CountryView`, `CurrencyView`)
- `location` exposes deliberate semantic named-interface subpackages:
  - `location.address` (`@NamedInterface("address")`: `AddressOperations`, commands, domain exceptions)
  - `location.brand` (`@NamedInterface("brand")`: `BrandOperations`, commands, domain exceptions)
  - `location.category` (`@NamedInterface("category")`: `LocationCategoryOperations`, commands, domain exceptions)
  - `location.enums` (`@NamedInterface("enums")`: `DiningServiceStyle`, `DayOfWeek`)
  - `location.hours` (`@NamedInterface("hours")`: `BusinessHoursOperations`, commands, inputs, domain exceptions)
  - `location.location` (`@NamedInterface("location")`: `LocationOperations`, commands, domain exceptions)
  - `location.view` (`@NamedInterface("view")`: `AddressView`, `BrandView`, `BusinessHoursIntervalView`, `BusinessHoursScheduleView`, `LocationCategoryView`, `LocationView`)
- **Zero cross-module dependencies** between `media` and `location`.
- Internal domain entities, composite keys, repositories, and services reside in `media.internal.*` and `location.internal.*` and are completely encapsulated.

## Flyway Migration & JPA Schema Validation

- **Flyway:** V1 baseline (`backend/src/main/resources/db/migration/V1__create_schema_v1.sql`) cleanly migrated against fresh PostgreSQL 18.6 container instance.
- **Hibernate ORM Core:** 7.4.5.Final
- **Configuration:** `spring.jpa.hibernate.ddl-auto: validate`
- **Tables Validated (Exact Flyway V1 Physical Columns):**
  - `albums` (columns: `id`, `title`, `description`)
  - `images` (columns: `id`, `album_id`, `title`, `image_type`, `object_key`, `source_url`, `mime_type`, `size_bytes`, `width_px`, `height_px`, `checksum_sha256`, `captured_at`, `location_text`)
  - `brands` (columns: `id`, `name`, `logo_url`, `nationality_code`, `description`, `min_price`, `max_price`, `currency_code`, `review`)
  - `addresses` (columns: `id`, `label`, `address_type`, `country_code`, `administrative_area`, `locality`, `sublocality`, `street_address`, `postal_code`)
  - `locations` (columns: `id`, `brand_id`, `address_id`, `name`, `image_url`, `description`, `phone`, `website_url`, `min_price`, `max_price`, `currency_code`, `review`, `business_hours_known`)
  - `location_categories` (columns: `id`, `name`, `description`)
  - `location_category_assignments` (composite PK: `location_id`, `category_id`)
  - `location_dining_service_styles` (composite PK: `location_id`, `service_style`)
  - `location_business_hours` (columns: `id`, `location_id`, `day_of_week`, `sequence`, `open_time`, `close_time`)

## Concurrency & Race Tests Verification

All concurrency tests observe deterministic PostgreSQL contention via `pg_locks` (`NOT l.granted`) and `pg_stat_activity` rather than relying on timing-only sleeps:
1. `MediaIntegrationTest.concurrentDuplicateObjectKeyThrowsDomainConflict`:
   - Competing insert on `images.object_key` observes PostgreSQL unique index contention;
   - Unblocks and cleanly translates PostgreSQL unique violation (`23505`) to `ImageConflictException`;
   - Asserts transactional rollback of the losing Vault entry.
2. `MediaIntegrationTest.concurrentDuplicateChecksumThrowsDomainConflict`:
   - Competing insert on `images.checksum_sha256` observes PostgreSQL unique index contention;
   - Unblocks and cleanly translates PostgreSQL unique violation (`23505`) to `ImageConflictException`;
   - Asserts transactional rollback of the losing Vault entry.
3. `LocationIntegrationTest.concurrentDuplicateLocationCategoryThrowsConflict`:
   - Competing insert on `location_categories(name)` observes `uq_ci_location_categories_name` lock contention;
   - Unblocks and cleanly translates PostgreSQL unique violation (`23505`) to `LocationCategoryNameAlreadyExistsException`.
4. `LocationIntegrationTest.concurrentUpdateDuplicateLocationCategoryThrowsConflict`:
   - Competing update/rename on `location_categories(name)` observes `uq_ci_location_categories_name` lock contention;
   - Unblocks, flushes, and cleanly translates PostgreSQL unique violation (`23505`) to `LocationCategoryNameAlreadyExistsException`.
5. `LocationIntegrationTest.locationCategoryAssignmentIdempotencyAndDeterministicContention`:
   - Competing concurrent insert into `location_category_assignments` observes primary key lock contention;
   - Both complete idempotently, converging to exactly one assignment row.
6. `LocationIntegrationTest.diningServiceStyleAssignmentIdempotencyAndDeterministicContention`:
   - Competing concurrent insert into `location_dining_service_styles` observes primary key lock contention;
   - Both complete idempotently, converging to exactly one style row.
7. `LocationIntegrationTest.concurrentCompetingScheduleReplacementSerializedPerLocation`:
   - Competing schedule replacement executes `findByIdForUpdate` pessimistic write lock on `locations`;
   - Competing thread observes lock contention on `locations`;
   - Thread commits sequentially without schedule interleaving or orphaned rows.
8. `LocationIntegrationTest.concurrentScheduleReadCoordinatesWithReplacementLock`:
   - Reader acquires `findByIdForShare` (`SELECT ... FOR SHARE`) while writer holds `findByIdForUpdate` (`SELECT ... FOR UPDATE`);
   - Reader observes lock contention and blocks until writer commits, reading the fully replaced coherent schedule.
9. `LocationIntegrationTest.concurrentReplacementWaitsForScheduleReaderLock`:
   - Writer acquires `findByIdForUpdate` while reader holds `findByIdForShare`;
   - Writer observes lock contention and waits until reader finishes, preventing torn intermediate reads.

## Codex Review Remediation (2026-09-29)

All five findings from Codex final review (`docs/implementation/phase-6/reviews/2026-09-29-phase-6-final-codex-review.md`) have been fully remediated:
1. **Concurrent Category Update Conflict Translation & Contention Test:**
   - Updated `LocationCategoryService.update` to use `saveAndFlush(category)` and inspect root causes for `uq_ci_location_categories_name`, translating to `LocationCategoryNameAlreadyExistsException`.
   - Verified with unit tests in `LocationValidationTest` and PostgreSQL contention integration test `concurrentUpdateDuplicateLocationCategoryThrowsConflict`.
2. **Coherent Business-Hours Read Coordination:**
   - Added `@Lock(LockModeType.PESSIMISTIC_READ) Optional<Location> findByIdForShare(Long id)` to `LocationRepository`.
   - Updated `BusinessHoursService.getSchedule` to `@Transactional` and acquired `findByIdForShare(locationId)`, synchronizing with `findByIdForUpdate` on `locations`.
   - Verified with bidirectional PostgreSQL contention tests `concurrentScheduleReadCoordinatesWithReplacementLock` and `concurrentReplacementWaitsForScheduleReaderLock`.
3. **Non-sensitive Image Fallback Message:**
   - Updated `ImageService.create` to identify `images_object_key_key` and `images_checksum_sha256_key` via Hibernate `ConstraintViolationException` and provide sanitized domain messages.
   - For all other/unrecognized data integrity violations, provides safe non-sensitive message `"Image metadata conflict occurred during creation"` without leaking raw SQL or exception messages.
   - Verified with unit tests in `MediaValidationTest`.
4. **Deterministic Concurrent Duplicate Checksum Test & Vault Rollback:**
   - Added `MediaIntegrationTest.concurrentDuplicateChecksumThrowsDomainConflict` using `awaitCompetingLock` on `images_checksum_sha256_key`.
   - Verified clean translation to `ImageConflictException` and assertion that the losing Vault entry is rolled back.
5. **Internal Package Descriptors:**
   - Created meaningful, concise `package-info.java` files for all 6 internal packages (`media.internal.application`, `media.internal.domain`, `media.internal.infrastructure.persistence`, `location.internal.application`, `location.internal.domain`, `location.internal.infrastructure.persistence`).

## Known Diagnostics / Non-blocking Warnings

1. `Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK.` (Harmless JVM warning with Java 25).
2. `SpringDoc /v3/api-docs endpoint is enabled by default.` (Standard default banner).
3. Expected PostgreSQL log lines during race conflict tests:
   - `ERROR: duplicate key value violates unique constraint "images_object_key_key"`
   - `ERROR: duplicate key value violates unique constraint "images_checksum_sha256_key"`
   - `ERROR: duplicate key value violates unique constraint "uq_ci_location_categories_name"`
   These verify that database unique indexes properly reject concurrent duplicates as intended.
