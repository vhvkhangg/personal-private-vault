# Active Implementation Handoff

- Handoff ID: `milestone-1-3-concurrency-maintenance`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope type: owner-approved maintenance of frozen Phase 1 Vault and Phase 2 authentication; not Phase 4

## Goal

Resolve the two blocking Phase 1–3 milestone concurrency findings: preserve refresh-token replacement tracking
under concurrent rotation/revocation, and make concurrent favorite/tag attachment fulfill their idempotent
contract. Keep all other frozen behavior unchanged.

## Sources of truth

- `docs/implementation/maintenance/milestone-1-3-concurrency/README.md` — approved scope, race outcomes,
  permitted targets, non-goals, and detailed regression contract.
- `docs/implementation/phase-3/reviews/2026-09-28-phase-1-3-milestone-codex-review.md` — source findings.
- `docs/implementation/phase-3/milestone-review.md` — `CHANGES_REQUESTED` gate.
- `docs/implementation/phase-1/handoff.md` and `docs/implementation/phase-2/handoff.md` — frozen public behavior.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`, `docs/architecture/module-dependency-matrix.md`,
  and `docs/adr/0008-jwt-access-refresh-and-private-pin.md` — frozen schema/module/security constraints.

## Implementation targets

- `authentication/internal/application/session/SessionService.java`: make `revoke` serialize with `rotate` on
  the same predecessor row, preferably reusing the existing locked repository query; preserve idempotent revoke
  and fail-closed rotation. Adjust `RefreshTokenRepository.java` only as needed. Touch `RefreshToken.java` only
  if a minimal safe design genuinely requires it.
- `vault/internal/application/metadata/VaultMetadataService.java`, `FavoriteRepository.java`, and
  `VaultEntryTagRepository.java`: replace the two check-then-insert paths with atomic, constraint-backed
  insert-if-absent behavior in the caller's transaction. Keep validation before writes.
- Add focused PostgreSQL regressions in `RefreshTokenLifecycleIntegrationTest.java` and
  `VaultMetadataIntegrationTest.java`. Scope paths above are relative to the corresponding
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/` or
  `backend/src/test/java/com/vhvkhangg/personalprivatevault/` root.

## Required behavior / invariants

- Rotation wins: one successor, revoked predecessor, replacement link retained after concurrent revoke;
  revocation remains successful/idempotent. Revocation wins: no successor, rotation fails closed.
- Concurrent duplicate `favorite(entryId)` and `attachTag(entryId, tagId)` calls from independent transactions
  both succeed and leave one row each; PostgreSQL uniqueness arbitrates the race.
- Retain entry existence/trash/capability and tag-existence checks; do not swallow unrelated integrity errors
  or isolate these writes with `REQUIRES_NEW`.
- Preserve token hashing, expiry/replay handling, secret redaction, public APIs, module boundaries, UTC times,
  and all other frozen Phase 1–3 behavior.

## Non-goals

No DBML, Flyway, or schema change; no Phase 4 Fiction implementation; no token format/lifetime/crypto change;
no rating, tag-creation, trash, or capability-matrix redesign; no unrelated refactor, generic retry framework,
events, or new agents/hooks.

## Test/evidence contract

- Focused tests: deterministic PostgreSQL/Testcontainers mixed rotate–revoke interleavings (rotation-first and
  revocation-first), concurrent favorite, and concurrent tag attachment. Use barriers/latches or equivalent
  coordination, not timing-only sleeps; reload committed rows and assert outcomes and no raw persistence error.
- Final command: `mvn -f backend/pom.xml clean verify` after focused regressions pass.
- Required evidence: `docs/implementation/maintenance/milestone-1-3-concurrency/test-evidence.md` with exact
  command/exit status, Java/Maven/PostgreSQL/Testcontainers versions, test totals, focused race results,
  Spring Modulith verification, and Flyway/Hibernate validation.

## Constraints / risks

- Keep writes in the caller's transaction. A swallowed uniqueness error inside an aborted PostgreSQL
  transaction is not a valid idempotence fix; the Phase 3 `ON CONFLICT DO NOTHING` set-write pattern is a
  relevant reference, not authorization to change People.
- Avoid a test that merely starts two threads and depends on scheduler luck. Prove the specified outcomes
  against committed PostgreSQL state without logging or asserting raw token values in diagnostics.
- Relevant engineering skills: `authentication-security`, `jpa-postgresql-persistence`, `backend-testing`,
  `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
  `design-pattern-selection`, and `modular-monolith-architecture`.
- Phase 4 pre-handoff remains blocked until this maintenance passes final review, the owner commits/pushes it,
  and a rerun milestone review returns `MILESTONE_READY`.

## Implementation result

Implemented by Antigravity on 2026-09-28:

- **Target 1 — Finding A (Refresh rotation vs. revocation serialization):**
  - Updated `SessionService.revoke` in `authentication/internal/application/session/SessionService.java` to use the existing locked query `RefreshTokenRepository.findByTokenHashWithLock(tokenHash)` (`PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`), serializing concurrent `rotate` and `revoke` operations on the same predecessor row.
  - Zero schema or entity changes; reused existing row-lock contract.
  - Added deterministic PostgreSQL integration tests in `RefreshTokenLifecycleIntegrationTest.java`:
    - `concurrentRotationWinsOverRevocationPreservingReplacementLink`: rotation holds row-lock; competing revoke actively queries `findByTokenHashWithLock` and blocks; rotation observes lock contention via `awaitCompetingLock` (`pg_locks` / `pg_stat_activity`), then commits successor and replacement link; revoke unblocks and completes idempotently without modifying or clearing `replaced_by_token_id`.
    - `concurrentRevocationWinsOverRotationCausingRotationToFailClosed`: revocation holds row-lock; competing rotate actively queries `findByTokenHashWithLock` and blocks; revocation observes lock contention via `awaitCompetingLock` (`pg_locks` / `pg_stat_activity`), revokes predecessor, and commits; rotate unblocks, observes revoked status, and fails closed with `InvalidRefreshTokenException` creating zero successors.
    - Secrecy protection: replaced direct AssertJ assertions on raw token strings with non-sensitive derived boolean assertions (`hasNonBlankSuccessor`, `distinctFromPredecessor`), preventing token leakage in diagnostic failure logs.
- **Target 2 — Finding B (Concurrent Vault favorite/tag attachment):**
  - Replaced check-then-insert paths with atomic PostgreSQL `insertIfAbsent` queries using `ON CONFLICT DO NOTHING` and `@Modifying(flushAutomatically = true)`:
    - `FavoriteRepository.insertIfAbsent(vaultEntryId, createdAt)`: `ON CONFLICT (vault_entry_id) DO NOTHING`.
    - `VaultEntryTagRepository.insertIfAbsent(vaultEntryId, tagId, createdAt)`: `ON CONFLICT (vault_entry_id, tag_id) DO NOTHING`.
  - Updated `VaultMetadataService.favorite` and `attachTag` to run within the caller's `@Transactional` boundary (default `PROPAGATION_REQUIRED`), avoiding `REQUIRES_NEW`.
  - Retained all entry existence, trash status, capability matrix, and tag existence checks before writes.
  - Added focused PostgreSQL integration tests in `VaultMetadataIntegrationTest.java`:
    - `concurrentFavoriteCallsSucceedIdempotentlyAndLeaveSingleRow`: 8 concurrent threads via `CyclicBarrier` both complete without error; PostgreSQL uniqueness arbitrates race, leaving exactly 1 row.
    - `concurrentAttachTagCallsSucceedIdempotentlyAndLeaveSingleRow`: 8 concurrent threads via `CyclicBarrier` both complete without error; PostgreSQL uniqueness arbitrates race, leaving exactly 1 row.
    - `rollbackOfEnclosingTransactionRollsBackFavoriteAndTagAttachment`: proves rollback of enclosing transaction rolls back favorite and tag attachment writes cleanly.
- **Verification Evidence:**
  - Full verification command: `mvn -f backend/pom.xml clean verify` completed with exit status `0` (`BUILD SUCCESS`).
  - Total tests: 227 run, 0 failures, 0 errors, 0 skipped.
  - Spring Modulith module verification and Hibernate schema validation succeeded against unchanged Flyway V1 DDL.
  - Graphify context refreshed via `scripts/refresh-graphify.ps1`.
  - Evidence document created and updated: `docs/implementation/maintenance/milestone-1-3-concurrency/test-evidence.md`.

## Codex remediation

Codex final review on 2026-09-28: `CHANGES_REQUESTED`. See
`docs/implementation/maintenance/milestone-1-3-concurrency/reviews/2026-09-28-concurrency-maintenance-final-codex-review.md`.

1. [x] Remove the direct AssertJ assertion on raw refresh-token strings in
   `RefreshTokenLifecycleIntegrationTest.concurrentRotationWinsOverRevocationPreservingReplacementLink`.
   Failed assertions must not print either raw token; assert derived booleans with non-sensitive descriptions,
   as existing Phase 2 tests do.
2. [x] Make both mixed rotate/revoke tests prove that the competing operation has reached the PostgreSQL lock
   before the first transaction releases it. The current `started` latches fire before the service call and
   `Thread.sleep(150)` is only a scheduling guess. Use deterministic coordination/observable lock state or
   an equivalent test mechanism, then assert the committed outcomes already specified by this handoff.
3. [x] Rerun focused PostgreSQL tests and `mvn -f backend/pom.xml clean verify`; update the maintenance
   `test-evidence.md` with actual results and return this handoff as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Final review

Codex remediation re-review on 2026-09-28: `READY_FOR_OWNER_COMMIT`.

- Both prior test findings are resolved: refresh-token assertions use non-sensitive booleans, and both mixed
  races wait for observable PostgreSQL lock contention before the lock holder proceeds.
- Codex independently ran `mvn -f backend/pom.xml clean verify`: exit 0, 227 tests, 0 failures/errors/skips.
  Spring Modulith and Flyway-backed PostgreSQL/Hibernate validation passed; `git diff --check` passed.
- Production changes remain limited to the approved Phase 1/2 concurrency corrections. DBML, Flyway V1,
  public APIs, module boundaries, and Phase 4 production remain unchanged.
- Formal review: `docs/implementation/maintenance/milestone-1-3-concurrency/reviews/2026-09-28-concurrency-maintenance-final-codex-review.md`.

The owner may commit/push this maintenance slice. The Phase 1–3 milestone remains `CHANGES_REQUESTED`
until that commit/push and a rerun `$codex-milestone-review` returns `MILESTONE_READY`.
