# Phase 7–9 Milestone Integrity and Query-Shape Maintenance — Test Evidence

- Date: 2026-09-30
- Handoff ID: `maintenance-milestone-7-9-integrity-and-query-shape`
- Implementer: Antigravity

## Final Verification Commands

### 1. Focused Regressions Verification
```powershell
mvn -f backend/pom.xml -ntp test "-Dtest=AccountIntegrationTest,KnowledgeIntegrationTest,KnowledgeValidationTest"
```
- **Exit status:** `0` (`BUILD SUCCESS`)
- **Tests run:** 71
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Time elapsed:** 43.715 s

### 2. Full Test Suite & Package Verification
```powershell
mvn -f backend/pom.xml -ntp clean verify
```
- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:15 min
- **Total tests run:** 594
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### 3. Git Diff Whitespace Check
```powershell
git diff --check
```
- **Exit status:** `0` (clean, no trailing whitespace or formatting warnings)

### 4. Graphify Knowledge Graph Refresh
```powershell
powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1
```
- **Exit status:** `0` (graph re-extracted and synchronized: 3361 nodes, 11522 edges, 300 communities)

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Build & Compiler Diagnostics

During `mvn -f backend/pom.xml -ntp clean verify`, the following build diagnostics were observed and verified harmless:
1. **Lombok restricted method warning under JDK 25:**
   `WARNING: A restricted method in java.lang.System has been called`
   `WARNING: java.lang.System::loadLibrary has been called by lombok.launch.ShadowClassLoader ...`
   `WARNING: sun.misc.Unsafe::...`
   This is expected when the Lombok annotation processor runs under JDK 25.
2. **Compiler deprecation notice in test support:**
   `Note: backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractPostgresIntegrationTest.java uses or overrides a deprecated API.`
   This stems from Testcontainers / Spring DynamicPropertyRegistry lifecycle methods in the test support base class.
3. **IDE inspections note:**
   No IDE-specific inspection runner was executed; verification was conducted strictly and deterministically using the Maven CLI build lifecycle (`mvn clean verify`), Surefire test execution reports, and `git diff --check`.

## Complete Test Breakdown by Test Suite (594 Tests)

Counts reflect actual `<testcase>` execution aggregated across top-level and `@Nested` suites in Maven Surefire XML reports:

| Test Class | Test Count | Failures | Errors | Result |
| :--------- | :--------: | :------: | :----: | :----: |
| `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountValidationTest` | 21 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 12 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest` | 18 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest` | 9 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest` | 2 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionIntegrationTest` | 17 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest` | 13 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest` | 40 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmCreditIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmLinkIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmValidationTest` | 44 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest` | 29 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeValidationTest` | 27 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest` | 32 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest` | 23 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonValidationTest` | 30 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest` | 20 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest` | 36 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest` | 11 | 0 | 0 | PASS |
| **Total** | **594** | **0** | **0** | **PASS** |

## Focused Maintenance Regressions and Remediation Verifications

### 1. Finding 1 Remediation: Exact Immutable Numeric Classes & Subclass Rejection
- **Class:** `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest$NoteIntegrationTests`
- **Utility:** `com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot`
- **Verified behaviors:**
  - `validateAndNormalizeNumber` enforces exact known immutable class equality (`clazz == BigDecimal.class`, `clazz == BigInteger.class`, `clazz == Integer.class`, etc.) rather than open `instanceof` checks.
  - Normalizes mutable leaf inputs without precision loss: `AtomicInteger` $\to$ `Integer`, `AtomicLong` $\to$ `Long`, and `AtomicBoolean` $\to$ `Boolean`.
  - Mutable subclasses of both `BigDecimal` and `BigInteger` (`MutableBigDecimal`, `MutableBigInteger`) are rejected with `InvalidNoteException("Unsupported numeric type in note frontmatter")` across:
    - Direct snapshot helpers: `NoteFrontmatterSnapshot.deepCopy` and `NoteFrontmatterSnapshot.toUnmodifiableSnapshot`.
    - Nested Note commands: `CreateNoteCommand` and `UpdateNoteCommand`.
    - Parent Knowledge commands: `CreateKnowledgeNoteCommand` and `UpdateKnowledgeNoteCommand`.
  - Proven stable no-write rejection under active write transactions (`TransactionTemplate` with `PROPAGATION_REQUIRES_NEW`), confirming 0 notes written to PostgreSQL (`SELECT count(*) FROM notes` = 0).
  - Authentic `BigDecimal` and `BigInteger` instances retain exact fidelity across nested and parent creation, updating, and reload.

### 2. Finding 2 Remediation: Centralized Frontmatter Snapshot Policy & Graph Validation
- **Public Interface Location:** `com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot` (in legal public named interface `note` of nested module `knowledge.note`).
- **Unified Policy Across Module Boundary:**
  - `CreateNoteCommand`, `UpdateNoteCommand`, `NoteView`, `CreateKnowledgeNoteCommand`, `UpdateKnowledgeNoteCommand`, and `KnowledgeNoteView` delegate frontmatter validation and copying to `NoteFrontmatterSnapshot.toUnmodifiableSnapshot(frontmatter)`.
  - Removed all duplicate, ad-hoc copier implementations.
- **Cycle & Graph Safety Tests in `KnowledgeIntegrationTest` & `KnowledgeValidationTest`:**
  - `cyclicFrontmatterGraphIsRejectedWithoutStackOverflow`: Cyclic maps/lists are detected via `IdentityHashMap` visited set and rejected with `InvalidNoteException("Cyclic reference detected in note frontmatter")`.
  - `nonStringAndNullKeysAreRejectedWithoutClassCastException`: Map keys that are null or not `String` (e.g., `Integer` keys) are rejected with `InvalidNoteException` rather than `ClassCastException`.
  - `unsupportedLeafTypesAreRejectedWithInvalidNoteException`: Arbitrary unsupported leaf types (e.g., `Thread`, custom POJOs) are rejected with `InvalidNoteException("Unsupported value type in note frontmatter")` without exposing object internals or payload data.

### 3. Finding 3 Remediation: Parent Knowledge Facade Flow for Vocabulary Stale-Refresh Under Lock
- **Class:** `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest$VocabularyIntegrationTests`
- **Application Flow:** Tested entirely through parent `KnowledgeOperations` facade interface:
  - `createVocabularyItem`: Tx A seeds vocabulary item with interval 0, ease 2.50.
  - `findVocabularyItemById`: Tx A preloads the item into its Hibernate L1 persistence context.
  - Independent worker Tx B executes `knowledgeOperations.reviewVocabularyItem` (rating 5), committing interval 10, ease 2.70.
  - Tx A invokes `knowledgeOperations.reviewVocabularyItem` (rating 4) under `PESSIMISTIC_WRITE` row lock; `entityManager.refresh(item)` refreshes stale L1 state from PostgreSQL under lock.
  - Resulting review transition accurately captures `previousIntervalDays = 10` and `previousEaseFactor = 2.70` rather than stale values.
  - `findVocabularyReviews`: Tx A validates the contiguous history chain (Tx B first, Tx A second) and verifies final item state.
  - Retained all existing concurrent contention and rollback safety tests.

### 4. Follower Snapshot Known-New Entry Persistence & Query Shape
- **Class:** `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest`
- **Domain Entity:** `FollowerSnapshotEntry` implementing `Persistable<FollowerSnapshotEntryId>` with `@Transient private boolean isNew = true` and `@PostPersist`/`@PostLoad` callbacks.
- **Application Service:** `FollowerSnapshotService.createSnapshot`
- **Verified behaviors:**
  - Multi-batch query-shape validation across batch sizes 1, 3, and 5 distinct targets:
    - `SELECT` queries on `external_accounts`: **exactly 2** (1 owner existence check + 1 bulk `IN` check for targets).
    - `SELECT` queries referencing `follower_snapshot_entries`: **exactly 0** during snapshot creation across all batch sizes, confirming known-new entity semantics bypass existence checks.
  - Recent snapshot reads (`findRecentByOwner`):
    - Executed **exactly 2** total queries across multiple snapshot headers: 1 for headers and 1 grouped count projection (`GROUP BY snapshot_id` with SQL `IN` clause).
    - Hibernate prepared statement count confirmed as exactly 2.

## Modulith Architecture & Frozen Schema Invariants

- **Flyway:** V1 physical migration (`V1__create_schema_v1.sql`) remains strictly unchanged and applied cleanly. Zero DDL/schema modifications.
- **Spring Modulith:** All 12 architecture tests in `ApplicationArchitectureTests` passed cleanly. Module boundaries between `account`, `knowledge` (including nested `information`, `note`, `study`, `vocabulary`), and all other modules remain strictly encapsulated without illegal internal package imports.
