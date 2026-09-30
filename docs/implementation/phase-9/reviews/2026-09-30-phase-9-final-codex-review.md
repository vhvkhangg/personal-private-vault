# Phase 9 Collection final Codex review — 2026-09-30

Status: **CHANGES_REQUESTED**. Handoff: `backend-phase-9-collection`. This is an implementation review; Codex made no production-code changes.

## Blocking finding

1. **Medium — test evidence contains nonexistent tests and false domain claims.** The "Focused Collection Regression & Invariant Tests" table in `docs/implementation/phase-9/test-evidence.md` lists method names such as `rejectsNegativeMusicDuration`, `rejectsMismatchedShoppingPriceAndCurrency`, and `softwarePlatformsSupportDuplicateHandlingAndIdempotence` that do not exist in the test classes. It also claims a Music duration field, that currency requires price, and that Software rejects non-marketplace platforms. Schema v1 and the approved Phase 9 contract have no Music duration, expressly allow currency without price, and impose no PlatformKind restriction; the implementation follows those rules. This materially misstates the acceptance evidence and could mislead owner closeout. Rebuild the focused evidence table from the actual `CollectionValidationTest`, `CollectionIntegrationTest`, and architecture method names and assertions; remove invented or contradicted behavior, accurately describe the real PostgreSQL contention tests, and verify every row against source/Surefire results. Do not alter correct production behavior to match the erroneous evidence.

## Checks and non-blocking observations

- Independent `mvn -f backend/pom.xml -ntp clean verify` on Java 25 passed: **583 tests, 0 failures, 0 errors, 0 skipped**. PostgreSQL Testcontainers, Flyway/Hibernate validation, and Spring Modulith architecture verification passed. `git diff --check` passed.
- The five approved tables are implemented in their owning nested modules. Vault-backed creates share transactions; composite assignments use atomic PostgreSQL `ON CONFLICT DO NOTHING`; tests observe real ungranted PostgreSQL locks and one-row convergence. The closed parent API uses parent-owned types, and no frozen migration change was observed.
- No unbounded collection read, unexpected agent/hook change, or stale implemented-Collection `.gitkeep` was found. The parent package-info still mentions future search contracts, but no Search behavior was implemented; this comment is non-blocking documentation debt.
- Existing Lombok/JDK `Unsafe` and deprecated test-support compiler warnings remain. No IDE inspection was run, so this review does not claim an IDE-warning-free state.

## Gate

Antigravity should correct the Phase 9 evidence within the active handoff, check it against the actual test reports, and return the slice for Codex re-review. Do not commit/push or begin Phase 7–9 milestone review while `CHANGES_REQUESTED` remains.
