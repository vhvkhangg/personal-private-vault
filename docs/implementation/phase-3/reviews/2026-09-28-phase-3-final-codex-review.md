# Codex Final Review — Backend Phase 3 People Foundation

- Date: 2026-09-28
- Handoff: `backend-phase-3-people-foundation`
- Baseline: `8f75b6f docs(phase-3): prepare people foundation handoff`
- Mode: implementation review; production and test code left unchanged by Codex
- Initial result: `CHANGES_REQUESTED`; final result: `READY_FOR_OWNER_COMMIT`

## Findings

### High — Isolated writes break enclosing transaction consistency

- Files: `people/internal/application/CreatorGroupManager.java:24-36`,
  `PersonRoleManager.java:23-33`, `CreatorGroupMembershipManager.java:23-33`, and their public-service callers.
- Behavior: all three helpers use `REQUIRES_NEW`. Group creation/update, role insertion, and membership insertion
  commit independently of any transaction that calls the public People API. If a caller rolls back after one of
  these operations, the People write remains. If that caller created a Person or Group in its uncommitted
  transaction, a new transaction cannot see that parent row, so role/membership insertion can block on the
  foreign key or fail and leak `DataIntegrityViolationException`.
- Consequence: cross-module workflows can leave partial state or fail valid create-and-assign sequences, despite
  the handoff's transaction/no-partial-state contract. Existing concurrency tests call each API outside an
  enclosing transaction, so their green result does not prove composition safety.
- Required correction: preserve the caller's transaction for these writes while retaining database-backed
  idempotent/conflict outcomes. Prove outer rollback and uncommitted-parent visibility with PostgreSQL tests,
  then retain/re-run independent-transaction concurrency tests. Do not rely on catching a PostgreSQL constraint
  failure inside an already-aborted transaction.

### Medium — Group member result does not honor its immutable collection contract

- Files: `people/internal/application/CreatorGroupService.java:160-169`,
  `people/group/CreatorGroupOperations.java:48-59`.
- Behavior: `getMembers` returns the repository's mutable `List` directly, though the public method promises an
  immutable list and the handoff requires immutable public results.
- Consequence: callers can change their returned result after the operation, contrary to the API contract.
- Required correction: return an immutable snapshot and add a focused assertion for rejected mutation.

### Critical

None.

### Low

None requiring separate remediation.

## Verified areas

- Four People entities/repositories map the unchanged Flyway V1 tables and native enums; the module descriptor
  lists the five approved Vault/Reference named interfaces, and no cross-module internal imports were found.
- Person creation uses the public Vault Entry operation in one transaction; its rollback case is covered.
- Exact group-name conflicts, role/membership duplicate races, nationality lookup, bounded reads, and module
  verification have meaningful PostgreSQL-backed tests for the currently isolated call paths.
- Existing Surefire XML reports show 22 suites, 215 tests, 0 failures/errors/skips, matching the evidence file.
  Codex did not rerun Maven during this review; the failing composition cases are absent from those reports.
- The authentication JWT test edit adjusts a time-sensitive test fixture; it does not change frozen Phase 2
  production behavior.
- `git diff --check` passed. No DBML, Flyway, or Phase 0–2 production files changed.

## Required next step

Antigravity addresses the two findings in `ACTIVE.md`, reruns final verification, updates evidence, and returns
the handoff for Codex review. No commit message is provided while findings remain.

## Remediation re-review — 2026-09-28

- **High resolved:** Group create/update, role add, and member add now join the caller's transaction. The
  `REQUIRES_NEW` helpers were removed. Role/member insertions use PostgreSQL `ON CONFLICT DO NOTHING` on their
  composite keys, so duplicate calls remain idempotent without aborting the transaction.
- **Medium resolved:** `getMembers` returns `List.copyOf(...)` and an integration test proves mutation is rejected.
- PostgreSQL tests now prove role assignment to an uncommitted new Person, membership assignment to an
  uncommitted new Group, and enclosing-transaction rollback for group create/update, role add, and member add.
  Existing concurrent duplicate tests remain green.
- Codex independently ran `mvn -f backend/pom.xml clean verify` on Java 25: exit 0, `BUILD SUCCESS`, 222 tests,
  0 failures/errors/skips. Surefire XML has 22 suites and the same totals. Spring Modulith and Flyway-backed JPA
  validation passed. `git diff --check` passed.
- The implementation stays within the Phase 3 handoff. No Flyway/DBML change, other module production change,
  cross-module internal import, or new dependency was introduced. The Phase 2 JWT test fixture adjustment remains
  test-only and prevents a date-expiration failure; it does not change frozen production behavior.

Final result: `READY_FOR_OWNER_COMMIT`. The owner may commit and push this Phase 3 implementation slice.
