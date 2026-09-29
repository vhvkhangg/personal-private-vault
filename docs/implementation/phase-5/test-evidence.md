# Backend Phase 5 — Test Verification Evidence

- Date: 2026-09-29
- Handoff ID: `backend-phase-5-film`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 47.747 s

## Test Counts and Summary

- **Total tests run:** 372
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class                                                                                 | Test Count | Failures | Errors | Result   |
| :----------------------------------------------------------------------------------------- | :--------: | :------: | :----: | :------: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests`                          |     7      |    0     |   0    |   PASS   |
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
| **Total**                                                                                  |  **372**   |  **0**   | **0**  | **PASS** |

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
- `film` module allowed dependencies are strictly narrowed to:
  - `vault :: entry` (`VaultEntryOperations`)
  - `vault :: enums` (`VaultEntryType.FILM`, `VaultEntryType.FILM_CREDIT`)
  - `vault :: view` (`VaultEntryView`)
  - `people :: person` (`PersonOperations`)
  - `people :: view` (`PersonView`)
  - `reference :: catalog` (`ReferenceCatalog`)
  - `reference :: view` (`CountryView`, `LanguageView`, `StoryArchetypeView`, `WorldSettingView`)
- Zero whole-module dependencies allowed; `people :: group` is explicitly excluded; zero access to internal packages of any other module.
- `film` exposes deliberate semantic named-interface subpackages:
  - `film.enums` (`@NamedInterface("enums")`: `FilmFormat`, `FilmProductionStyle`, `FilmCreditRole`, `ProgressStatus`, `ConsumptionStatus`)
  - `film.view` (`@NamedInterface("view")`: `FilmView`, `FilmGenreView`, `FilmLinkView`, `FilmCreditView`, `FilmClassificationsView`)
  - `film.genre` (`@NamedInterface("genre")`: `FilmGenreOperations`, commands, domain exceptions)
  - `film.film` (`@NamedInterface("film")`: `FilmOperations`, commands, domain exceptions)
  - `film.link` (`@NamedInterface("link")`: `FilmLinkOperations`, commands, domain exceptions)
  - `film.credit` (`@NamedInterface("credit")`: `FilmCreditOperations`, commands, domain exceptions)
- Internal domain entities, composite keys, repositories, and services reside in `film.internal.*` and are completely encapsulated.

## Flyway Migration & JPA Schema Validation

- **Flyway:** V1 baseline (`backend/src/main/resources/db/migration/V1__create_schema_v1.sql`) cleanly migrated against fresh PostgreSQL 18.6 container instance.
- **Hibernate ORM Core:** 7.4.5.Final
- **Configuration:** `spring.jpa.hibernate.ddl-auto: validate`
- **Tables Validated (Exact Flyway V1 Physical Columns):**
  - `films` (columns: `id`, `title`, `original_title`, `nationality_code`, `poster_url`, `format`, `production_style`, `is_nsfw`, `director_person_id`, `description`, `total_episodes`, `progress_status`, `consumption_status`, `current_progress_text`, `review`)
  - `film_genres` (columns: `id`, `name`, `description`)
  - `film_genre_assignments` (composite PK: `film_id`, `genre_id`)
  - `film_story_archetypes` (composite PK: `film_id`, `story_archetype_id`)
  - `film_world_settings` (composite PK: `film_id`, `world_setting_id`)
  - `film_links` (columns: `id`, `film_id`, `language_code`, `label`, `url`, `is_primary`, `created_at` — confirms NO `link_type` column per Schema v1)
  - `film_credits` (columns: `id`, `film_id`, `person_id`, `role`, `character_name`, `note` — confirms exact physical columns from `V1__create_schema_v1.sql`)
- All 7 JPA entity mappings validated successfully against unchanged Flyway V1 DDL with zero schema validation errors or mapping mismatches.

## Known Warnings & Diagnostics Summary

The build is not claimed to be completely warning-free, nor were any IDE static code inspections run (this run was performed in headless CLI mode via Maven). Observed diagnostics fall into the following distinct categories:

1. **Toolchain & Compiler Warnings:**
   - **Lombok / JDK 25 internal reflection warning:** During `default-compile`, `javac` reports that a terminally deprecated method in `sun.misc.Unsafe` (`objectFieldOffset`) was called by `lombok.permit.Permit`, noting that dynamic access will be removed in a future JDK release.
   - **Test deprecation notice:** During `default-testCompile`, `javac` notes that `AbstractPostgresIntegrationTest.java` uses or overrides a deprecated API (`-Xlint:deprecation`).
2. **Test Runtime & Agent Diagnostics:**
   - **Byte Buddy / Mockito Java Agent:** During Spring context startup on JDK 25, Byte Buddy emits a warning regarding dynamic agent attachment (`-XX:+EnableDynamicAgentLoading` recommended for future JDKs).
   - **SpringDoc OpenAPI notices:** SpringDoc outputs routine informational configuration warnings that `/v3/api-docs` and `/swagger-ui.html` endpoints are enabled by default.
   - **Expected test-induced SQL constraint warnings:** Hibernate and PostgreSQL emit log entries (e.g. `ERROR: duplicate key value violates unique constraint "uq_ci_film_genres_name"`) during intentional negative concurrency/conflict integration test cases. These are expected and cleanly handled by application exception translation.
3. **Database & Schema Validation Status:**
   - In contrast to toolchain and test runtime warnings above, **Hibernate schema validation (`ddl-auto: validate`) and Flyway migration executed with zero errors and zero schema mismatches**.

## Domain Invariants, Concurrency & Transactional Verification

1. **Shared Vault Identity for Film & Film Credit:**
   - Film creation delegates to `VaultEntryOperations.create(VaultEntryType.FILM)` within the same physical transaction (`films.id == vault_entries.id`).
   - Film Credit creation delegates to `VaultEntryOperations.create(VaultEntryType.FILM_CREDIT)` within the same physical transaction (`film_credits.id == vault_entries.id`).
   - Transactional rollback tests verify that any failure during Film or Film Credit creation leaves zero orphan rows in `vault_entries`.
2. **Film Credit Capabilities & Distinct-Record Semantics:**
   - Film Credit creates a separate distinct record even for repeated identical parameters (Film, Person, role, character name); no artificial deduplication is imposed.
   - Film Credit is favorite-only in Vault: tested `VaultCapabilityMatrix` integration where `setFavorite(true)` succeeds, whereas `setRating` and `attachTag` throw `IllegalStateException` ("does not support ratings" / "does not support tags"). Film does not duplicate Vault's capability checks.
3. **Director Reference & Non-Mutation Invariant:**
   - Optional `director_person_id` is validated via `PersonOperations.find(id)`.
   - Verified that assigning a Person as a director does NOT mutate the Person's roles (no auto-assignment of `DIRECTOR` role).
4. **Film-Owned Genre Lifecycle & Deterministic Concurrency:**
   - Enforces case-insensitive uniqueness (`findByNameIgnoreCase`).
   - Non-blank name validation (length <= 150).
   - Strictly bounded read scope (create, update, find by ID, find by name); no unbounded `findAll`.
   - Deterministic PostgreSQL lock polling test (`concurrentDuplicateGenreCreationRecoversFromUniqueIndexConflict`) uses `awaitCompetingLock("film_genres", ...)` querying `pg_locks` (`NOT l.granted`) and `pg_stat_activity` (`query ILIKE '%film_genres%'`) to confirm actual physical lock contention before releasing the holder transaction. Unblocks cleanly catching `DataIntegrityViolationException` and mapping to `FilmGenreNameAlreadyExistsException` without raw constraint leaks. Exactly 1 genre row is committed.
5. **Classification Idempotency & Native Concurrency:**
   - Genre, story archetype, and world setting assignments use atomic native `INSERT INTO ... ON CONFLICT DO NOTHING` with `@Modifying(flushAutomatically = true)` within caller transactions (`REQUIRED`).
   - Deterministic contention tests for all 3 assignment tables (`genreAssignmentIdempotencyAndDeterministicContention`, `storyArchetypeAssignmentIdempotencyAndDeterministicContention`, `worldSettingAssignmentIdempotencyAndDeterministicContention`) hold PostgreSQL composite primary key locks and verify lock contention via `awaitCompetingLock` without timing sleeps. Both transactions complete cleanly without error or exception leaks; exactly 1 row committed per assignment.
   - Non-existent classification IDs are validated via `ReferenceCatalog` and rejected.
6. **Film External Links:**
   - No `link_type` per Schema v1.
   - Supports repeated URL/language/label without unique constraint.
   - Optional language code validates against `ReferenceCatalog.language(code)`.
   - Parent-scoped lookup and update enforce `film_id` match; throws `FilmLinkNotFoundException` on mismatch.
7. **Code Hygiene & Graphify Knowledge Graph:**
   - Clean `git diff --check` with zero whitespace errors.
   - Graphify AST extraction succeeded with 1764 nodes, 5854 edges, and 177 communities.
