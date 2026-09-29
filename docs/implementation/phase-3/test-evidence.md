# Backend Phase 3 — Test Verification Evidence

- Date: 2026-09-28
- Handoff ID: `backend-phase-3-people-foundation`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 43.976 s

## Test Counts and Summary

- **Total tests run:** 222
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class                                                                                 | Test Count | Failures | Errors | Result   |
| :----------------------------------------------------------------------------------------- | :--------: | :------: | :----: | :------: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests`                          |     2      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest`       |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest`              |     6      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest`                       |     36     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest`                       |     5      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest`                    |     8      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest`                    |     20     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest`                   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest`                |     18     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest`                      |     16     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest`                    |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest`                    |     4      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest`                     |     2      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest`                     |     3      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` |     4      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest`   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest`              |     3      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest`     |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.PersonValidationTest`                           |     30     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupValidationTest`                     |     16     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.PersonIntegrationTest`                          |     11     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupIntegrationTest`                    |     15     |    0     |   0    |   PASS   |
| **Total**                                                                                  |  **222**   |  **0**   | **0**  | **PASS** |

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
- `people` module allowed dependencies are strictly narrowed to:
  - `vault :: entry` (`VaultEntryOperations`)
  - `vault :: enums` (`VaultEntryType.PERSON`)
  - `vault :: view` (`VaultEntryView`)
  - `reference :: catalog` (`ReferenceCatalog`)
  - `reference :: view` (`CountryView`)
- Zero whole-module dependencies allowed; zero access to internal packages of any other module.
- `people` exposes deliberate semantic named-interface subpackages:
  - `people.enums` (`@NamedInterface("enums")`: `PersonRole`, `Gender`)
  - `people.view` (`@NamedInterface("view")`: `PersonView`, `CreatorGroupView`, `CreatorGroupMemberView`)
  - `people.person` (`@NamedInterface("person")`: `PersonOperations`, commands, domain exceptions)
  - `people.group` (`@NamedInterface("group")`: `CreatorGroupOperations`, commands, domain exceptions)
- Internal domain entities, composite keys, repositories, and services reside in `people.internal.*` and are completely inaccessible from outside the module.

## Flyway Migration & JPA Schema Validation

- **Flyway:** V1 baseline (`V1__create_schema_v1.sql`) cleanly migrated against fresh PostgreSQL 18.6 container instance.
- **Hibernate ORM Core:** 7.4.5.Final
- **Configuration:** `spring.jpa.hibernate.ddl-auto: validate`
- **Tables Validated:**
  - `persons` (columns: `id`, `name`, `avatar_url`, `gender`, `birth_date`, `height_cm`, `weight_kg`, `nationality_code`, `notes`)
  - `person_roles` (composite PK: `person_id`, `role`)
  - `creator_groups` (columns: `id`, `name`, `description`, `created_at`, `updated_at`)
  - `creator_group_members` (composite PK: `creator_group_id`, `person_id`)
- All 4 JPA entity mappings validated with zero errors or warnings against unchanged Flyway V1 DDL.

## Domain Invariants, Concurrency & Transactional Verification

1. **Shared Vault Identity:**
   - Person creation delegates to `VaultEntryOperations.create(VaultEntryType.PERSON)` within the same physical transaction.
   - Person ID exactly matches the created Vault Entry ID (`persons.id == vault_entries.id`).
   - Transactional rollback test proves that any failure during person creation leaves zero orphan rows in `vault_entries`.
2. **Profile Validation & Reference Integrity:**
   - Names are strictly non-blank with length <= 255.
   - Avatar URLs validate length <= 2048.
   - Heights and weights validate strictly positive values and fit `numeric(6,2)`.
   - Nationality codes validate 2-character format and resolve against `ReferenceCatalog.country(code)`.
3. **Role Management & Enclosing Transaction Semantics:**
   - All five frozen roles (`ACTOR`, `SINGER`, `DIRECTOR`, `AUTHOR`, `ARTIST`) map to PostgreSQL native enum `person_role`.
   - Added via atomic native `INSERT INTO person_roles (person_id, role) VALUES (?, ?::person_role) ON CONFLICT (person_id, role) DO NOTHING` with `@Modifying(flushAutomatically = true)`.
   - Runs directly within caller transaction (`@Transactional` with default propagation `REQUIRED`), eliminating `REQUIRES_NEW`.
   - Newly created Person rows in an uncommitted enclosing transaction are visible immediately when adding roles.
   - Rollback of the enclosing transaction cleanly rolls back newly added roles.
   - Sequential re-addition of an existing role is idempotent.
   - Under 8 concurrent competing threads adding the same `(person_id, role)`, PostgreSQL uniqueness constraint (`pk_person_roles`) arbitrates the race, and all callers complete successfully with exactly 1 row remaining.
4. **Creator Group Exact Name Uniqueness & Enclosing Transaction Semantics:**
   - Creating a group with an exact duplicate stored name throws `CreatorGroupNameAlreadyExistsException`.
   - Managed within the caller's transaction (`@Transactional`), eliminating `REQUIRES_NEW`.
   - Under 8 concurrent competing threads creating the same group name, exactly 1 thread succeeds and 7 threads receive `CreatorGroupNameAlreadyExistsException` without raw database exceptions leaking.
   - Updating group name to one already taken by another group throws `CreatorGroupNameAlreadyExistsException`.
   - Rollback of enclosing transaction cleanly rolls back created groups and group updates.
5. **Creator Group Membership & Enclosing Transaction Semantics:**
   - Added via atomic native `INSERT INTO creator_group_members (creator_group_id, person_id) VALUES (?, ?) ON CONFLICT (creator_group_id, person_id) DO NOTHING` with `@Modifying(flushAutomatically = true)`.
   - Runs directly within caller transaction (`@Transactional` with default propagation `REQUIRED`), eliminating `REQUIRES_NEW`.
   - Newly created Creator Group and Person rows in an uncommitted enclosing transaction are visible immediately when adding memberships.
   - Rollback of enclosing transaction cleanly rolls back newly added memberships.
   - Sequential re-addition of an existing `(group_id, person_id)` membership is idempotent.
   - Under 8 concurrent competing threads adding the same membership, PostgreSQL uniqueness constraint (`pk_creator_group_members`) arbitrates the race, and all callers complete successfully with exactly 1 row remaining.
   - Bounded member reads via `CreatorGroupOperations.getMembers` return an unmodifiable snapshot `List.copyOf(...)`. Callers attempting to mutate the returned collection receive `UnsupportedOperationException`.
