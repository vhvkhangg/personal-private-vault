# Active Implementation Handoff

- Handoff ID: `backend-phase-7-account`
- Created by: Codex
- Status: `COMPLETE_FROZEN`
- Owner commit/push: completed 2026-09-30
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: Backend Phase 7 Account foundation

## Goal

Implement the `account` Spring Modulith module for Vault-backed external accounts, current follow/follower
relationships, and historical follower snapshots against unchanged Schema v1. Keep Account persistence internal and
use only public Vault/Reference contracts.

## Sources of truth

- `docs/implementation/phase-7/README.md` and `preparation-review.md` — approved behavior and test contract.
- `docs/implementation/phase-7/reviews/2026-09-29-phase-7-pre-handoff-codex-rereview.md` — closed preparation findings.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — exact fields, enums, checks, and keys.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen ownership and package direction.
- `.agents/rules/backend-phase-7-account.md` and
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/AGENTS.md` — scoped guidance.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/`, implement only
  `external_accounts`, `external_account_relationships`, `follower_snapshots`, and `follower_snapshot_entries`.
  Expose capability-oriented Account, Relationship, and Snapshot operations with immutable commands/views; keep
  entities, application services, and repositories internal. Add meaningful `package-info.java` named interfaces,
  narrow the module descriptor to the Vault/Reference interfaces actually used (expected `vault::entry/enums/view`
  and `reference::catalog/view`), and remove `internal/.gitkeep` when real content exists.
- Support account create/update/find-by-ID, one canonical relationship set/create-or-update and pair/owner-bounded
  reads, and transactional batch snapshot creation with snapshot/owner-bounded reads. Do not add a separate public
  snapshot-entry append operation or an unbounded global list/search API.
- Place focused tests under `backend/src/test/java/com/vhvkhangg/personalprivatevault/account/` and record final
  evidence in `docs/implementation/phase-7/test-evidence.md`.

## Required behavior / invariants

- External Account and its `EXTERNAL_ACCOUNT` Vault Entry share one ID and transaction; failure leaves no orphan
  Vault Entry. Validate Platform through public `ReferenceCatalog.platform(id)`. Require at least one non-null
  username/external ID/URL per Schema v1. Vault alone owns favorite/rating/tag/recycle behavior.
- Non-null `(platform_id, external_id)` duplicates, sequential or concurrent, yield a stable Account-domain conflict
  with PostgreSQL uniqueness as final arbiter; username/URL duplicates remain allowed. Do not leak raw persistence
  exceptions or sensitive External IDs in diagnostics.
- One relationship row exists per owner/target pair; both Account IDs resolve and differ. Keep `follower_status`
  (target follows owner) separate from `follow_status` (owner follows target), with independent source/provenance.
  Repeated identical set commands are idempotent. Concurrent differing commands leave one complete command state,
  never a torn field mix or raw unique-key error.
- Snapshot header and submitted entries commit atomically and never silently update current relationship state.
  Preserve submitted historical username/display-name/external-ID/profile-URL copies, not live Account joins.
  In one batch, identical normalized copies for a repeated target collapse; differing copies reject the entire
  command with a stable Account-domain validation error and no committed header/entries. Completed snapshots are
  immutable in Phase 7. Do not require reported total count to equal stored entry count.
- Reads return immutable views, remain ID-/owner-/snapshot-scoped and explicitly bounded for collections, and do
  not expose JPA entities, repositories, or other modules' internals.

## Non-goals

No Flyway/DBML/schema or frozen Vault/Reference production change; live platform API/OAuth/scraping/browser
automation/scheduler, implicit snapshot-to-relationship sync, public snapshot append, Knowledge/feed/importdata,
global search, account deletion, REST/controllers/OpenAPI, frontend, or speculative adapters/events/CRUD bases.

## Test/evidence contract

- JUnit 5 and PostgreSQL Testcontainers cover all four tables; Account/Vault rollback; Platform and identifier
  validation; allowed username/URL duplicates; external-ID sequential/concurrent conflicts; relationship
  direction/self-check/idempotence/same-pair concurrency; snapshot atomicity, historical copies, identical
  duplicate collapse, conflicting-copy rejection, and bounded deterministic reads. Race tests must demonstrate
  actual PostgreSQL contention/serialization, not rely on timing-only sleeps; do not use H2.
- The concurrent external-ID test uses a distinctive private marker, gets the loser to the PostgreSQL unique
  constraint, captures logs across worker threads, asserts no marker or raw `Detail: Key` vendor text, and still
  observes the stable domain conflict. Preserve the existing privacy-safe logger configuration and non-sensitive
  diagnostics.
- Verify exact Spring Modulith named-interface dependencies, no internal-package leakage, and unchanged Flyway
  V1/Hibernate validation. Final commands: `mvn -f backend/pom.xml -ntp clean verify` on Java 25, then
  `git diff --check`. Record commands/exits, test totals, PostgreSQL/architecture results, privacy evidence, and
  known diagnostics in `docs/implementation/phase-7/test-evidence.md`.

## Constraints / risks

- Stop and report any conflict with frozen Schema v1 or module boundaries; do not silently alter baselines.
  Keep transaction boundaries in Account application services, use the database as final uniqueness arbiter, and
  avoid check-then-insert as the only race protection. Do not log raw throwable/root-cause messages containing
  private Account values.
- Relevant skills: `account-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence`, and `backend-testing`.
- Agents do not commit, push, tag, or create/merge PRs.

## Implementation result

1. **Domain Model & Persistence:**
   - Implemented `com.vhvkhangg.personalprivatevault.account` module with four Schema v1 tables:
     - `external_accounts`: mapped by `ExternalAccount` (implements `Persistable<Long>`), sharing ID with `vault_entries (id)` for entry type `EXTERNAL_ACCOUNT`.
     - `external_account_relationships`: mapped by `ExternalAccountRelationship` with identity PK, separate `follower_status` and `follow_status`, `source`, `has_liked_post`, and self-relationship check.
     - `follower_snapshots`: mapped by `FollowerSnapshot` with identity PK, owner account reference, `captured_at`, `source`, `reported_total_count`, and `imported_file_name`.
     - `follower_snapshot_entries`: mapped by `FollowerSnapshotEntry` with composite PK `FollowerSnapshotEntryId` (`snapshot_id`, `target_account_id`) holding historical string copies.
   - Enums: `ExternalAccountOwnership`, `ExternalAccountType`, `FollowerStatus`, `RelationshipSource`, `FollowStatus`, `FollowerSnapshotSource` with native PostgreSQL `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` mappings.

2. **Public API & Named Interfaces:**
   - Subpackages with `@NamedInterface`:
     - `account::account`: `ExternalAccountOperations`, `CreateExternalAccountCommand`, `UpdateExternalAccountCommand`, and exceptions `ExternalAccountNotFoundException`, `ExternalAccountConflictException`, `InvalidExternalAccountException`.
     - `account::relationship`: `ExternalAccountRelationshipOperations`, `SetExternalAccountRelationshipCommand`, and exceptions `InvalidExternalAccountRelationshipException`, `ExternalAccountRelationshipNotFoundException`.
     - `account::snapshot`: `FollowerSnapshotOperations`, `CreateFollowerSnapshotCommand`, `CreateFollowerSnapshotEntryCommand`, and exceptions `InvalidFollowerSnapshotException`, `FollowerSnapshotNotFoundException`.
     - `account::enums`: all Account enums.
     - `account::view`: `ExternalAccountView`, `ExternalAccountRelationshipView`, `FollowerSnapshotView`, `FollowerSnapshotEntryView`.
   - Narrowed module descriptor in `account/package-info.java` to exact named interfaces: `vault::entry`, `vault::enums`, `vault::view`, `reference::catalog`, `reference::view`. Removed `internal/.gitkeep`.

3. **Application Services & Invariant Enforcement:**
   - `ExternalAccountService`:
     - Creates Vault Entry and External Account in one transaction; failure leaves no orphan Vault Entry.
     - Validates platform existence via `ReferenceCatalog.platform(id)`.
     - Requires at least one non-null/non-blank identifier (`username`, `external_id`, `url`).
     - Detects sequential and concurrent non-null `(platform_id, external_id)` duplicate creations, translating PostgreSQL unique index violation to `ExternalAccountConflictException`.
     - Preserves privacy-safe constraint logging: does not emit private External ID marker or raw `Detail: Key` lines to captured logs; throwable and root cause contain no sensitive values.
     - Allows duplicate usernames and URLs across distinct accounts.
   - `ExternalAccountRelationshipService`:
     - Canonical, idempotent `setRelationship` operation using atomic PostgreSQL upsert (`INSERT ... ON CONFLICT (owner_account_id, target_account_id) DO UPDATE`).
     - Enforces `ownerAccountId != targetAccountId` and verifies both accounts exist.
     - Concurrent writes to same pair result in exactly one row with a coherent complete command state, leaking no raw persistence exceptions.
   - `FollowerSnapshotService`:
     - Transactional batch snapshot creation committing header and entries atomically.
     - Collapses identical duplicate entries for the same target in one batch.
     - Rejects batches with conflicting historical copies for the same target with `InvalidFollowerSnapshotException`, committing neither header nor entries.
     - Preserves historical snapshot string copies without silently mutating current relationship state.
     - Completed snapshots are immutable; no post-creation append operation is exposed.
   - Bounded reads:
     - `findRecentByPlatformId`, `findRecentByOwner` (relationships), `findRecentByOwner` (snapshots), `findEntriesBySnapshotId` clamp limits and enforce deterministic ordering.

4. **Verification & Evidence:**
   - `AccountValidationTest`: 21 tests covering command validation, identifier constraints, and snapshot duplicate/conflict logic.
   - `AccountIntegrationTest`: 15 tests covering PostgreSQL persistence, Vault rollback, Reference validation, external ID contention with privacy-safe log assertions, relationship concurrency, relationship provenance and repeat-idempotence, snapshot atomicity, and bulk existence validation / grouped count snapshot reads.
   - `ApplicationArchitectureTests`: 12 tests passed, verifying Spring Modulith module boundaries and named interface isolation.
   - Full verification: `mvn -f backend/pom.xml -ntp clean verify` passed with 489 tests run, 0 failures, 0 errors, 0 skipped (`BUILD SUCCESS`).
   - `git diff --check` passed cleanly with 0 whitespace errors.
   - Refreshed Graphify AST with `scripts/refresh-graphify.ps1`.
   - Evidence recorded in `docs/implementation/phase-7/test-evidence.md`.

## Codex remediation

The [2026-09-29 final Codex review](reviews/2026-09-29-phase-7-final-codex-review.md) found three blocking issues, now fully resolved:

1. **Relationship creation provenance and repeat-idempotence:**
   - Modified `ExternalAccountRelationshipRepository.upsert`: removed `source` from `DO UPDATE SET`, preserving the initial creation provenance as defined in DBML line 2498.
   - Conditional `updated_at` assignment: updates timestamp with `now()` only when mutable fields change (`follower_status`, `follow_status`, `has_liked_post`, `note` using `IS DISTINCT FROM`), leaving `updated_at` untouched on exact repeats.
   - Updated `AccountIntegrationTest.relationshipSetIdempotentAndUpdatesFields` to assert `updatedAt` is identical on repeat, advances on mutable changes, and `source` remains `MANUAL`.
   - Updated `concurrentRelationshipSamePairWritesPreserveOneCoherentCommandState` to verify whole-command coherence for mutable fields and winning insert provenance for `source`.
2. **Removed unbounded public batch read `findByIds`:**
   - Removed `findByIds(List<Long>)` from `ExternalAccountOperations` and `ExternalAccountService`.
   - Replaced caller in `AccountIntegrationTest` with bounded `findRecentByPlatformId`.
3. **Eliminated N+1 database reads in follower snapshot paths:**
   - Added `ExternalAccountRepository.findExistingIds(Collection<Long>)` to perform single bulk existence check across all unique targets in `FollowerSnapshotService.createSnapshot`.
   - Added `FollowerSnapshotEntryRepository.countGroupedBySnapshotIds(Collection<Long>)` to perform a single grouped count query across recent snapshot headers in `findRecentByOwner`.
   - Made `toView(FollowerSnapshot, int entryCount)` purely in-memory.
   - Added `followerSnapshotBulkExistenceValidationAndGroupedCountReads` in `AccountIntegrationTest` verifying bulk target validation and grouped count projection mapping without per-item queries.

Final verification: `mvn -f backend/pom.xml -ntp clean verify` passed on Java 25 (489 tests, 0 failures, 0 errors, 0 skipped). `git diff --check` passed cleanly. Graphify refreshed.

## Final review

Initial result: `CHANGES_REQUESTED` on 2026-09-29. Antigravity corrected the three production-code findings. The [final re-review](reviews/2026-09-29-phase-7-final-codex-rereview.md) requested test-only observable SQL query-shape regression assertions. Test-only remediation completed by Antigravity; status restored to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Codex test-only remediation

The test-only regression finding from the [final re-review](reviews/2026-09-29-phase-7-final-codex-rereview.md) is now fully resolved:

1. In `AccountIntegrationTest.followerSnapshotBulkExistenceValidationAndGroupedCountReads`, attached Logback `ListAppender<ILoggingEvent>` to `org.hibernate.SQL` and unwrapped Hibernate `Statistics`.
2. Verified multi-target snapshot creation: asserted exactly 2 `SELECT` queries on `external_accounts` (1 owner existence lookup + 1 bulk `IN (:ids)` lookup across 3 target accounts), asserting the bulk query contains ` in `.
3. Verified multi-header recent snapshots read: asserted exactly 2 queries total executed (1 header query on `follower_snapshots` + 1 grouped count projection query on `follower_snapshot_entries` with `GROUP BY` and `IN`), and asserted Hibernate `getPrepareStatementCount()` is strictly 2.
4. Preserved all functional and atomicity assertions (batch rejection without orphan rows, correct entry counts, capturedAt ordering).
5. Full verification: `mvn -f backend/pom.xml -ntp clean verify` passed on Java 25 (489 tests, 0 failures, 0 errors, 0 skipped). `git diff --check` passed cleanly. Graphify refreshed. Updated `docs/implementation/phase-7/test-evidence.md`. Antigravity returned the handoff to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

Codex final acceptance re-review: [READY FOR OWNER COMMIT](reviews/2026-09-29-phase-7-final-codex-acceptance-review.md). All blocking findings were closed at review time. Historical note: owner commit/push was pending when that review completed; the owner subsequently committed/pushed Phase 7 on 2026-09-30, and this archived handoff is now `COMPLETE_FROZEN`.
