# Backend Phase 4 — Test Verification Evidence

- Date: 2026-09-29
- Handoff ID: `backend-phase-4-fiction`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 46.389 s

## Test Counts and Summary

- **Total tests run:** 297
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class                                                                                 | Test Count | Failures | Errors | Result   |
| :----------------------------------------------------------------------------------------- | :--------: | :------: | :----: | :------: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests`                          |     5      |    0     |   0    |   PASS   |
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
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest`                         |     40     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest`                  |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest`                   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest`                       |     13     |    0     |   0    |   PASS   |
| **Total**                                                                                  |  **297**   |  **0**   | **0**  | **PASS** |

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
- `fiction` module allowed dependencies are strictly narrowed to:
  - `vault :: entry` (`VaultEntryOperations`)
  - `vault :: enums` (`VaultEntryType.FICTION`)
  - `vault :: view` (`VaultEntryView`)
  - `people :: person` (`PersonOperations`)
  - `people :: group` (`CreatorGroupOperations`)
  - `people :: view` (`PersonView`, `CreatorGroupView`)
  - `reference :: catalog` (`ReferenceCatalog`)
  - `reference :: view` (`CountryView`, `LanguageView`, `StoryArchetypeView`, `WorldSettingView`)
- Zero whole-module dependencies allowed; zero access to internal packages of any other module.
- `fiction` exposes deliberate semantic named-interface subpackages:
  - `fiction.enums` (`@NamedInterface("enums")`: `FictionFormat`, `ProgressStatus`, `ConsumptionStatus`)
  - `fiction.view` (`@NamedInterface("view")`: `FictionView`, `FictionGenreView`, `FictionLinkView`, `FictionClassificationsView`)
  - `fiction.genre` (`@NamedInterface("genre")`: `FictionGenreOperations`, commands, domain exceptions)
  - `fiction.fiction` (`@NamedInterface("fiction")`: `FictionOperations`, commands, domain exceptions)
  - `fiction.link` (`@NamedInterface("link")`: `FictionLinkOperations`, commands, domain exceptions)
- Internal domain entities, composite keys, repositories, and services reside in `fiction.internal.*` and are completely inaccessible from outside the module.

## Flyway Migration & JPA Schema Validation

- **Flyway:** V1 baseline (`backend/src/main/resources/db/migration/V1__create_schema_v1.sql`) cleanly migrated against fresh PostgreSQL 18.6 container instance.
- **Hibernate ORM Core:** 7.4.5.Final
- **Configuration:** `spring.jpa.hibernate.ddl-auto: validate`
- **Tables Validated:**
  - `fictions` (columns: `id`, `title`, `original_title`, `nationality_code`, `poster_url`, `format`, `is_nsfw`, `genre_id`, `author_person_id`, `author_group_id`, `description`, `total_chapters`, `progress_status`, `consumption_status`, `current_progress_text`, `review`)
  - `fiction_genres` (columns: `id`, `name`, `description`)
  - `fiction_story_archetypes` (composite PK: `fiction_id`, `story_archetype_id`)
  - `fiction_world_settings` (composite PK: `fiction_id`, `world_setting_id`)
  - `fiction_links` (columns: `id`, `fiction_id`, `language_code`, `link_type`, `label`, `url`, `is_primary`, `created_at`)
- All 5 JPA entity mappings validated with zero errors or warnings against unchanged Flyway V1 DDL.

## Domain Invariants, Concurrency & Transactional Verification

1. **Shared Vault Identity:**
   - Fiction creation delegates to `VaultEntryOperations.create(VaultEntryType.FICTION)` within the same physical transaction.
   - Fiction ID exactly matches the created Vault Entry ID (`fictions.id == vault_entries.id`).
   - Transactional rollback test proves that any failure during fiction creation leaves zero orphan rows in `vault_entries`.
2. **Author Source XOR Invariant:**
   - Exactly one of `author_person_id` and `author_group_id` must be provided on creation and update.
   - Setting both or setting neither is rejected with `InvalidFictionException`.
   - The selected author ID is verified against the respective module (`PersonOperations.find` or `CreatorGroupOperations.find`).
3. **Fiction-Owned Genre Lifecycle & Deterministic Concurrency:**
   - Fiction genres enforce case-insensitive uniqueness (`findByNameIgnoreCase`).
   - Creation and renaming validate non-blank names with length <= 150.
   - Public `FictionGenreOperations` strictly adheres to bounded-read scope with create, update, and find by ID/name; unbounded global list (`findAll`) was completely removed.
   - Deterministic database coordination test (`concurrentDuplicateGenreCreationRecoversFromUniqueIndexConflict`): Thread 1 executes insert in an uncommitted transaction, holding PostgreSQL's `uq_ci_fiction_genres_name` unique index lock. Thread 2 attempts creation of the same name (different case), passes application-level check, and blocks on PostgreSQL's unique index. Contention is deterministically verified via active PostgreSQL lock polling (`awaitCompetingLock` querying `pg_locks` for `NOT l.granted` and `pg_stat_activity` for `query ILIKE '%fiction_genres%'`), completely eliminating timing-dependent sleeps. Upon Thread 1's commit, Thread 2 unblocks, catches `DataIntegrityViolationException`, and recovers cleanly by throwing `FictionGenreNameAlreadyExistsException` without raw database constraint violation leaking. Database committed state contains exactly 1 row.
   - Rollback of enclosing transactions cleanly rolls back created genres.
4. **Classification Idempotency & Native Concurrency:**
   - Shared narrative classifications (`fiction_story_archetypes` and `fiction_world_settings`) are assigned via atomic native `INSERT INTO ... ON CONFLICT ... DO NOTHING` with `@Modifying(flushAutomatically = true)`.
   - Operations run directly within the caller's transaction (`@Transactional` with default propagation `REQUIRED`), eliminating `REQUIRES_NEW`.
   - Sequential duplicate additions are verified idempotent.
   - Deterministic database coordination tests (`storyArchetypeAssignmentIdempotencyAndDeterministicContention` and `worldSettingAssignmentIdempotencyAndDeterministicContention`): Thread 1 inserts the assignment within an uncommitted transaction, holding PostgreSQL's primary key row/index lock (`pk_fiction_story_archetypes` / `pk_fiction_world_settings`). Thread 2 calls `addStoryArchetype` / `addWorldSetting` with native `INSERT ... ON CONFLICT DO NOTHING` and blocks on PostgreSQL's row lock. Contention is deterministically verified via active PostgreSQL lock polling (`awaitCompetingLock` querying `pg_locks` for `NOT l.granted` and `pg_stat_activity` for target table query), eliminating timing-dependent sleeps. Upon Thread 1's commit, Thread 2 unblocks and completes cleanly with 0 errors and zero raw persistence exceptions escaping. Exactly one row is committed in the database.
   - Non-existent archetype/setting IDs are validated via `ReferenceCatalog` and rejected.
5. **Fiction External Links:**
   - Bounded free-form `link_type` (up to 50 characters) and valid URL length (up to 2048 characters).
   - Optional language code validates against `ReferenceCatalog.language(code)`.
   - Multiple links with the same language or link type are supported.
   - Link lookups and updates are strictly parent-scoped to `fiction_id`, rejecting mismatched parent queries with `FictionLinkNotFoundException`.
6. **Code Hygiene & Graphify Knowledge Graph:**
   - Zero whitespace issues confirmed via `git diff --check`.
   - Graphify AST extraction completed cleanly with 1402 nodes, 4398 edges, and 150 communities.
