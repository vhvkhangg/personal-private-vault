# Phase 4–6 Milestone Privacy-Safe Constraint Logging Maintenance — Test Evidence

- Date: 2026-09-29
- Handoff ID: `milestone-4-6-privacy-safe-constraint-logging`
- Implementer: Antigravity

## Final Verification Commands

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:11 min

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

- **Total tests run:** 463
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
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest`  |     5      |    0     |   0    |   PASS   |
| **Total**                                                                                  |  **463**   |  **0**   | **0**  | **PASS** |

## Focused Privacy-Safe Logging Regression Tests

All privacy-safe constraint logging regressions verify that expected PostgreSQL constraint races do not emit private business values or vendor details to application logs:

| Test Class                                                                   | Test Method                                                                     | Result | Verified Behavior                                                                                                                                           |
| :--------------------------------------------------------------------------- | :------------------------------------------------------------------------------ | :----: | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | `effectiveHibernateJdbcErrorLoggerLevelIsError`                                 |  PASS  | Asserts `org.hibernate.orm.jdbc.error` has effective level `ERROR` while root remains `INFO`.                                                              |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | `imageObjectKeyConflictDoesNotLogPrivateMarkerAcrossWorkerThreads`              |  PASS  | PostgreSQL lock contention on `images.object_key` throws `ImageConflictException`, rolls back Vault entry; logs contain neither `PPV_PRIVATE_OBJECT_KEY_MARKER` nor `Detail: Key (object_key)=(`. |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | `imageChecksumConflictDoesNotLogPrivateChecksumAcrossWorkerThreads`             |  PASS  | PostgreSQL lock contention on `images.checksum_sha256` throws `ImageConflictException`, rolls back Vault entry; logs contain neither checksum nor `Detail: Key (checksum_sha256)=(`. |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | `locationCategoryNameConflictDoesNotLogPrivateCategoryName`                     |  PASS  | PostgreSQL lock contention on `location_categories.name` throws `LocationCategoryNameAlreadyExistsException`; logs contain neither `PPV_PRIVATE_CATEGORY_NAME_MARKER` nor `Detail: Key`. |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | `unexpectedHibernatePersistenceFailureRemainsObservableAndPrivacySafe`          |  PASS  | Unexpected Hibernate/JPA persistence failure violating foreign key `fk_images_id_vault_entries` throws `ConstraintViolationException` (carrying constraint name `fk_images_id_vault_entries` and SQLState `23503`) and fails visibly; logs contain neither `PPV_UNEXPECTED_FAILURE_PRIVATE_MARKER` nor raw `Detail: Key (id)=(999999)`. |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest`              | `concurrentDuplicateObjectKeyThrowsDomainConflict`                              |  PASS  | Captured worker thread logs contain neither `PPV_PRIVATE_OBJECT_KEY_MARKER` nor `Detail: Key (object_key)=(`.                                              |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest`              | `concurrentDuplicateChecksumThrowsDomainConflict`                               |  PASS  | Captured worker thread logs contain neither distinctive sha256 checksum nor `Detail: Key (checksum_sha256)=(`.                                            |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest`         | `concurrentDuplicateLocationCategoryThrowsConflict`                             |  PASS  | Captured worker thread logs contain neither `PPV_PRIVATE_CATEGORY_NAME_MARKER` nor raw `Detail: Key`.                                                      |

## Logging Configuration & Effective Levels

- **Configuration:** `backend/src/main/resources/application.yml`
  ```yaml
  logging:
    level:
      root: INFO
      com.vhvkhangg.personalprivatevault: INFO
      org.hibernate.orm.jdbc.error: ERROR
  ```
- **Effective Levels:**
  - `org.hibernate.orm.jdbc.error`: `ERROR` (suppresses Hibernate `SqlExceptionHelper` WARN output that prints PostgreSQL `Detail: Key (...) already exists.`)
  - `root`: `INFO` (all other Hibernate and application loggers remain at standard logging levels)
  - `com.vhvkhangg.personalprivatevault`: `INFO`

## Absence of Private Markers and Vendor Details

- `PPV_PRIVATE_OBJECT_KEY_MARKER`: **ABSENT** from captured logs across worker threads.
- Distinctive Checksum (`e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`): **ABSENT** from captured logs.
- `PPV_PRIVATE_CATEGORY_NAME_MARKER`: **ABSENT** from captured logs.
- `PPV_UNEXPECTED_FAILURE_PRIVATE_MARKER`: **ABSENT** from captured logs during unexpected Hibernate/JPA persistence failure.
- `Detail: Key (object_key)=(`: **ABSENT** from captured logs.
- `Detail: Key (checksum_sha256)=(`: **ABSENT** from captured logs.
- `Detail: Key (name)=(` / `Detail: Key (lower(name::text))=(`: **ABSENT** from captured logs.
- `Detail: Key (id)=(999999)` / `Detail: Key `: **ABSENT** from captured logs.

## Schema Validation & Modulith Invariants

- **Flyway:** V1 physical migration (`backend/src/main/resources/db/migration/V1__create_schema_v1.sql`) validated and applied cleanly against PostgreSQL 18.6 Testcontainer.
- **Hibernate:** `spring.jpa.hibernate.ddl-auto: validate` succeeded without warnings or schema mismatches across all entities.
- **Spring Modulith:** 12 architecture tests in `ApplicationArchitectureTests` passed cleanly; zero cross-module dependency leaks, module encapsulation preserved.
