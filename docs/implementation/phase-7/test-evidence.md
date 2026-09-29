# Backend Phase 7 — Test Verification Evidence

- Date: 2026-09-29
- Handoff ID: `backend-phase-7-account`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:01 min

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

- **Total tests run:** 489
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
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest`                             |     22     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest`                            |     10     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest`                       |     26     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest`                     |     16     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest`  |     5      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.account.AccountValidationTest`                         |     21     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest`                        |     15     |    0     |   0    |   PASS   |
| **Total**                                                                                  |  **489**   |  **0**   | **0**  | **PASS** |

## Focused Account & History Regression Tests

| Test Class | Test Method | Result | Verified Behavior |
| :--- | :--- | :---: | :--- |
| `AccountIntegrationTest` | `createAndReadExternalAccountBackedByVaultEntry` | PASS | Creates External Account backed by Vault Entry (`EXTERNAL_ACCOUNT`), matching ID and metadata; asserts `findById` and bounded `findRecentByPlatformId`. |
| `AccountIntegrationTest` | `updateExternalAccountMetadata` | PASS | Updates External Account metadata fields cleanly and verifies persistence. |
| `AccountIntegrationTest` | `rollbackOfExternalAccountRollsBackVaultEntry` | PASS | Enclosing transaction rollback atomically rolls back both External Account and its Vault Entry. |
| `AccountIntegrationTest` | `platformValidationViaReferenceCatalog` | PASS | Rejects non-existent platform ID via `ReferenceCatalog.platform(id)`. |
| `AccountIntegrationTest` | `duplicatePlatformAndExternalIdThrowsConflict` | PASS | Sequential duplicate create on non-null `(platform_id, external_id)` throws `ExternalAccountConflictException`. |
| `AccountIntegrationTest` | `allowedUsernameAndUrlDuplicates` | PASS | Duplicate usernames and URLs on the same or different platform succeed with distinct account IDs. |
| `AccountIntegrationTest` | `concurrentDuplicateExternalIdThrowsDomainConflictWithPrivacySafeLogging` | PASS | Deterministic PostgreSQL contention on `(platform_id, external_id)` with private marker `PPV_PRIVATE_EXT_ID_MARKER_987654`; loser unblocks and receives `ExternalAccountConflictException`, losing Vault Entry is rolled back, and captured logs contain neither marker nor raw `Detail: Key (platform_id, external_id)=(`. |
| `AccountIntegrationTest` | `relationshipSetIdempotentAndUpdatesFields` | PASS | Verifies creation provenance preservation (`source = MANUAL` even when update specifies `SNAPSHOT`) and true idempotence (`updatedAt` unchanged on exact repeat; `updatedAt` advances on mutable field change). |
| `AccountIntegrationTest` | `relationshipRejectsSelfReference` | PASS | Rejects owner account equal to target account with `InvalidExternalAccountRelationshipException`. |
| `AccountIntegrationTest` | `concurrentRelationshipSamePairWritesPreserveOneCoherentCommandState` | PASS | Concurrent writes to same pair preserve single row; mutable fields match either Command 1 or Command 2 completely (never a torn mix); `source` reflects creation provenance from the winning insert. |
| `AccountIntegrationTest` | `followerSnapshotCreationCommitsAtomicallyWithEntries` | PASS | Batch snapshot creates header and entries atomically; preserves historical copies; does not mutate relationship state. |
| `AccountIntegrationTest` | `followerSnapshotCollapsesIdenticalDuplicateEntries` | PASS | Collapses identical normalized duplicate entries for the same target to one committed entry. |
| `AccountIntegrationTest` | `followerSnapshotRejectsConflictingHistoricalCopiesAndCommitsNothing` | PASS | Rejects batch containing conflicting historical copies for the same target with `InvalidFollowerSnapshotException`; commits neither header nor entries. |
| `AccountIntegrationTest` | `followerSnapshotBulkExistenceValidationAndGroupedCountReads` | PASS | Proves snapshot creation performs bulk target account existence validation in a single roundtrip via observable SQL shape assertions (exactly 2 SELECTs on `external_accounts`: 1 owner + 1 bulk with SQL `IN` clause) and atomic missing-target rejection; proves `findRecentByOwner` maps accurate entry counts across multiple snapshots via grouped count projection executing exactly 2 queries total (1 header + 1 `GROUP BY` count projection query) with Hibernate prepared statement count equal to 2. |
| `AccountIntegrationTest` | `boundedAndDeterministicReadsClampUpperLimit` | PASS | Enforces parent-scoped bounded reads and deterministic ordering for accounts, relationships, snapshots, and snapshot entries. |

## Codex Remediation Details

1. **Relationship Provenance and Repeat-Idempotence:**
   - Modified `ExternalAccountRelationshipRepository.upsert`: removed `source` from `DO UPDATE SET` so that `source` remains the original creation provenance as defined in DBML line 2498.
   - Updated `updated_at` assignment in `upsert` to evaluate `CASE WHEN ... IS DISTINCT FROM ... THEN now() ELSE external_account_relationships.updated_at END` across all mutable columns (`follower_status`, `follow_status`, `has_liked_post`, `note`). Exact command repeats leave `updated_at` untouched.
   - Updated `AccountIntegrationTest.relationshipSetIdempotentAndUpdatesFields` to assert `updatedAt` is identical on repeat, advances on mutable changes, and `source` remains `MANUAL`.
   - Updated `concurrentRelationshipSamePairWritesPreserveOneCoherentCommandState` to verify mutable fields are whole-command coherent while `source` reflects the winning insert provenance.
2. **Removed Unbounded Public `findByIds` Batch Read:**
   - Removed `findByIds(List<Long>)` from `ExternalAccountOperations` and `ExternalAccountService`.
   - Replaced caller in `AccountIntegrationTest.createAndReadExternalAccountBackedByVaultEntry` with bounded `findRecentByPlatformId`.
3. **Eliminated N+1 Database Reads in Follower Snapshots:**
   - Added `ExternalAccountRepository.findExistingIds(Collection<Long>)` to validate all unique target accounts in a single bulk roundtrip during `FollowerSnapshotService.createSnapshot`.
   - Added `FollowerSnapshotEntryRepository.countGroupedBySnapshotIds(Collection<Long>)` to fetch grouped counts for recent snapshot headers in a single query during `findRecentByOwner`.
   - Updated `FollowerSnapshotService.toView(FollowerSnapshot, int entryCount)` to be purely in-memory with zero database queries.
4. **Observable SQL Query-Shape and Query-Count Regression Assertions (Re-review Remediation):**
   - Attached Logback `ListAppender<ILoggingEvent>` to `org.hibernate.SQL` and unwrapped Hibernate `Statistics` in `AccountIntegrationTest.followerSnapshotBulkExistenceValidationAndGroupedCountReads`.
   - Measured `createSnapshot` with 3 target accounts: asserted exactly 2 `SELECT` queries on `external_accounts` (1 owner lookup + 1 bulk `IN (:ids)` lookup), preventing any regression to N+1 per-target queries.
   - Measured `findRecentByOwner` with 3 snapshot headers: asserted exactly 2 queries total executed (1 header query + 1 grouped count projection query on `follower_snapshot_entries` with `GROUP BY` and `IN`), asserting Hibernate prepared statement count is strictly 2.

## Privacy-Safe Logging & Absence of Private Markers

- `PPV_PRIVATE_EXT_ID_MARKER_987654`: **ABSENT** from captured logs across competing worker threads.
- `Detail: Key (platform_id, external_id)=(`: **ABSENT** from captured logs.
- `Detail: Key `: **ABSENT** from captured logs.
- `ExternalAccountConflictException` throwable / root-cause messages contain no private external IDs or vendor key details.

## Spring Modulith Architecture & Flyway Invariants

- **Spring Modulith:** `ApplicationArchitectureTests` passed (12/12). Allowed dependencies narrowed to `vault::entry`, `vault::enums`, `vault::view`, `reference::catalog`, `reference::view`. No internal package leaks.
- **Named Interfaces:** `account::account`, `account::relationship`, `account::snapshot`, `account::enums`, `account::view`.
- **Flyway:** V1 schema validated cleanly against PostgreSQL 18.6 Testcontainer.
- **Hibernate:** `spring.jpa.hibernate.ddl-auto: validate` succeeded without discrepancies across all 4 account tables and composite keys.
