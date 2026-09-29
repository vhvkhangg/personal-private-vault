# Phase 1–3 Milestone Concurrency Maintenance — Test Verification Evidence

- Date: 2026-09-28
- Handoff ID: `milestone-1-3-concurrency-maintenance`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 45.650 s

## Test Counts and Summary

- **Total tests run:** 227
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
| **Total**                                                                                  |  **227**   |  **0**   | **0**  | **PASS** |

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
- Zero cross-module boundary violations.
- `people` module dependencies remain strictly narrowed to the five approved named interfaces (`vault::entry`, `vault::enums`, `vault::view`, `reference::catalog`, `reference::view`).
- No cyclic dependencies, no leaked internal packages or repositories.

## Flyway Migration & JPA Schema Validation

- **Flyway:** V1 baseline (`V1__create_schema_v1.sql`) cleanly migrated against fresh PostgreSQL 18.6 container instance. Zero DDL/schema modifications.
- **Hibernate ORM:** `spring.jpa.hibernate.ddl-auto: validate` succeeded across all entities including `Favorite`, `VaultEntryTag`, and `RefreshToken`.

## Focused Concurrency Defect Regressions

### Finding A — Refresh-Token Rotation vs. Revocation Serialization
1. **Serialization Mechanism:**
   - Both `SessionService.rotate` and `SessionService.revoke` serialize on the predecessor row via `RefreshTokenRepository.findByTokenHashWithLock(tokenHash)` using `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`).
   - Zero schema or entity changes; reuse of existing row-lock contract.
2. **Observable Database Lock Contention (`awaitCompetingLock`):**
   - Eliminated all scheduling sleeps and timing assumptions.
   - Tests dynamically observe PostgreSQL lock manager state by querying:
     ```sql
     SELECT count(*) FROM pg_locks l
     JOIN pg_stat_activity a ON l.pid = a.pid
     WHERE NOT l.granted
       AND a.pid != pg_backend_pid()
       AND a.query ILIKE '%refresh_tokens%'
     ```
   - This deterministically guarantees that the competing transaction has reached the database and is actively blocked on the predecessor row lock before the first transaction proceeds toward commit.
3. **Token Secrecy in Test Assertions:**
   - Replaced direct AssertJ assertions on raw token strings with derived boolean properties (`hasNonBlankSuccessor`, `distinctFromPredecessor`) asserted with non-sensitive descriptions.
   - Diagnostic failure logs will never print, log, or interpolate raw refresh token strings.
4. **Rotation-Wins Interleaving (`concurrentRotationWinsOverRevocationPreservingReplacementLink`):**
   - Thread 1 acquires predecessor row-lock; Thread 2 concurrently attempts revoke.
   - Thread 1 observes via `awaitCompetingLock` that Thread 2 is actively blocked on the PostgreSQL row lock.
   - Thread 1 commits successor and replacement link.
   - Thread 2 unblocks, observes `token.isRevoked() == true`, and completes idempotently without modifying or clearing `replaced_by_token_id`.
   - Reloaded committed database state asserts:
     - Predecessor is revoked (`revoked_at IS NOT NULL`).
     - Predecessor `replaced_by_token_id` is retained and points to the committed successor.
     - Exactly one successor was created (total 2 tokens in DB).
     - Revoke remained successful; no raw persistence exception leaked.
5. **Revocation-Wins Interleaving (`concurrentRevocationWinsOverRotationCausingRotationToFailClosed`):**
   - Thread 1 acquires predecessor row-lock; Thread 2 concurrently attempts rotate.
   - Thread 1 observes via `awaitCompetingLock` that Thread 2 is actively blocked on the PostgreSQL row lock.
   - Thread 1 revokes predecessor and commits.
   - Thread 2 unblocks, observes revoked status, and fails closed with `InvalidRefreshTokenException`.
   - Reloaded committed database state asserts:
     - Predecessor is revoked (`revoked_at IS NOT NULL`).
     - Predecessor `replaced_by_token_id` is `null`.
     - Zero successors created (total 1 token in DB).

### Finding B — Concurrent Vault Favorite and Tag Attachment
1. **Atomic Insertion Mechanism:**
   - Replaced check-then-insert paths with atomic native `INSERT ... ON CONFLICT DO NOTHING` queries annotated with `@Modifying(flushAutomatically = true)`:
     - `FavoriteRepository.insertIfAbsent(vaultEntryId, createdAt)`: `ON CONFLICT (vault_entry_id) DO NOTHING`.
     - `VaultEntryTagRepository.insertIfAbsent(vaultEntryId, tagId, createdAt)`: `ON CONFLICT (vault_entry_id, tag_id) DO NOTHING`.
   - Both execute directly within the caller's `@Transactional` boundary (default propagation `REQUIRED`), eliminating check-then-insert races and avoiding `REQUIRES_NEW`.
   - All precondition validations (entry existence, trash status, capability matrix, and tag existence) execute before the write.
2. **Concurrent Favorite Race (`concurrentFavoriteCallsSucceedIdempotentlyAndLeaveSingleRow`):**
   - 8 concurrent threads calling `favorite(entry.id())` synchronized with `CyclicBarrier`.
   - All 8 callers complete successfully without throwing exceptions.
   - PostgreSQL primary key uniqueness (`favorites_pkey`) arbitrates the race atomically; exactly 1 row exists in `favorites`.
   - `metadata(entry.id()).favorite()` returns `true`.
3. **Concurrent Tag Attachment Race (`concurrentAttachTagCallsSucceedIdempotentlyAndLeaveSingleRow`):**
   - 8 concurrent threads calling `attachTag(entry.id(), tag.id())` synchronized with `CyclicBarrier`.
   - All 8 callers complete successfully without throwing exceptions.
   - PostgreSQL composite primary key uniqueness (`pk_vault_entry_tags`) arbitrates the race atomically; exactly 1 row exists in `vault_entry_tags`.
   - `metadata(entry.id()).tags()` reflects the attached tag.
4. **Enclosing Transaction Rollback (`rollbackOfEnclosingTransactionRollsBackFavoriteAndTagAttachment`):**
   - Proves that rolling back an enclosing transaction rolls back both `favorite` and `attachTag` writes cleanly, confirming that operations participate in caller transactions rather than committing in isolated `REQUIRES_NEW` contexts.
