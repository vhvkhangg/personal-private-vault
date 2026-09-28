# Codex Milestone Review — Phases 1–3

- Date: 2026-09-28
- Review window: frozen Phase 1 (`reference`, `vault`, Schema v1), Phase 2 (`authentication`, `settings`), and Phase 3 (`people`)
- Baseline: `ca5ec5d feat(backend): add people foundation`
- Initial result: **CHANGES_REQUESTED**; final result: **MILESTONE_READY**
- Mode: cross-phase review; no production, test, schema, or frozen architecture changes made

## Blocking findings

### High — Refresh revocation can erase rotation's replacement link

`SessionService.rotate` reads the predecessor with `findByTokenHashWithLock`, then saves its successor and
`replacedByTokenId` (`SessionService.java:75–98`). `revoke` reads the same predecessor through the unlocked
`findByTokenHash` and saves it after changing only `revokedAt` (`SessionService.java:106–123`). The
`RefreshToken` entity has no optimistic version, and its normal Hibernate update is not restricted to changed
columns (`RefreshToken.java:19–49,74–81`). A revocation transaction can therefore read the active predecessor,
rotation can commit a successor and replacement link, and the stale revocation update can write the predecessor
back with `replaced_by_token_id = NULL`. The token remains revoked, but the required replacement tracking is
lost. The Phase 2 contract explicitly requires transactionally consistent rotation/revocation and replacement
tracking (`phase-2/README.md`, `phase-2/handoff.md`). The PostgreSQL test covers competing rotations and
sequential revocation, not this mixed race.

Required correction: serialize revocation with rotation on the predecessor row (or use an equivalently safe
versioned/conditional update), retaining idempotent revocation and fail-closed rotation. Add a deterministic
PostgreSQL race regression that checks the committed predecessor's replacement link and successor count.

Disposition: **separate owner-approved maintenance implementation slice** in frozen Phase 2. Do not silently
change its production code or Schema v1 during this review.

### Medium — Concurrent idempotent Vault metadata inserts can fail with a PK conflict

`VaultMetadataService.favorite` and `attachTag` each check existence and then save a new row
(`VaultMetadataService.java:109–123,204–225`). Two transactions can both observe absence; one insert wins and
the other hits the primary key instead of completing the promised idempotent operation. The frozen Phase 1
handoff requires these operations to be idempotent (`phase-1/handoff.md:47–48`), but its integration tests
exercise sequential repeats, while the targeted concurrency test covers `createTag` only. Phase 3's analogous
set-like role and group-member assignments already use constraint-backed `ON CONFLICT DO NOTHING` in the
caller's transaction, so the cross-phase behavior is inconsistent. Fiction and later domains reuse the public
Vault metadata contract.

Required correction: make the insert-if-absent paths atomic under PostgreSQL uniqueness without swallowing
other integrity errors or splitting the caller's transaction; preserve capability and trash checks. Add
independent-transaction duplicate-race tests for favorite and tag attachment, including one-row final state
and successful callers.

Disposition: **separate owner-approved maintenance implementation slice** in frozen Phase 1. This can be
combined with the Phase 2 correction in one narrowly scoped maintenance handoff if the owner approves.

## Non-blocking documentation drift

`phase-3/README.md:43–48` still says the Phase 3 handoff *must* narrow the People descriptor and is *active*,
although Phase 3 is complete/frozen and the descriptor has been narrowed. Correct those sentences as a
historical account during authorized docs/tooling remediation; do not reopen Phase 3 implementation.

## Verified areas and evidence

- Implemented module descriptors keep `reference`, `vault`, and `authentication` dependency-free, and People
  depends only on the approved Vault/Reference named interfaces. No cross-module `internal` imports or
  repository sharing were found in the implemented modules.
- Person creation and shared Vault identity, People role/member ownership, Reference lookups, and Settings
  constraints have one canonical implementation each. Phase 3's caller-transaction composition regression and
  concurrent set-like writes were resolved before the owner commit.
- Public APIs use views/commands rather than leaking entities or repositories. No further concrete cohesion,
  coupling, SOLID, or design-pattern misuse was established in the implemented Phases 1–3; the two races above
  are the material technical debt to resolve before expanding domain consumers.
- No concrete N+1, unbounded hot-path read, excessive lock scope, repeated I/O, or algorithmic hotspot was
  established for the implemented scope. No speculative performance change is requested.
- JWT validation, token hashing at rest, PIN/credential boundaries, and redacted public secret-bearing values
  were reviewed without another blocking issue. The mixed token race above remains the security-sensitive
  transaction-integrity gap.
- The Phase 3 final-review record documents an independent Java 25 `mvn -f backend/pom.xml clean verify` pass:
  222 tests, zero failures/errors/skips, including Spring Modulith and PostgreSQL/Flyway verification. The
  milestone review did not rerun Maven because production/test code is unchanged; the two missing races are
  not covered by that green result.
- The dirty worktree contains owner Phase 3 closeout/Phase 4 preparation. This review changed only milestone
  status/review documents and roadmap status, preserving those preparations and the frozen baselines.

## Initial gate and next step (completed)

Phase 4 `$codex-pre-handoff-review` was blocked. The owner was asked to approve a narrow maintenance scope
for the two frozen-phase fixes, then use a separate handoff with PostgreSQL regressions and Codex final review.
That maintenance is now complete and the milestone has been rerun; no Phase 4 implementation handoff was
authorized by the initial decision.

## Milestone re-review — 2026-09-28

- The owner-approved maintenance scope was implemented, final-reviewed, and committed/pushed as `3a9294d`.
  `main` matches `origin/main`, and the worktree was clean before this status review.
- **High resolved:** `SessionService.rotate` and `revoke` now lock the same predecessor row. PostgreSQL tests
  observe the competing lock attempt and prove both rotation-first (one successor; replacement link retained)
  and revocation-first (no successor; rotation fails closed) outcomes without raw-token diagnostics.
- **Medium resolved:** Vault favorite/tag attachment use owner-local, caller-transaction `INSERT ... ON CONFLICT
  DO NOTHING` paths. PostgreSQL tests prove concurrent idempotence, one-row final state, and enclosing rollback.
- The Phase 3 README wording that incorrectly called its handoff active is now historical. No DBML, Flyway,
  frozen module-boundary, or Phase 4 production change was made by the maintenance.
- Cross-phase re-check found no further blocking domain-rule duplication, public/internal leakage, module
  cycle, misplaced persistence ownership, security/logging concern, concrete performance hotspot, or
  speculative abstraction requiring remediation. Existing Phase 1–3 invariants and canonical ownership
  remain as assessed above.
- Codex independently ran `mvn -f backend/pom.xml clean verify` on committed `3a9294d`: exit 0,
  `BUILD SUCCESS`, 227 tests, 0 failures/errors/skips. Spring Modulith architecture verification and
  Flyway-backed PostgreSQL/Hibernate validation passed.

Final result: **MILESTONE_READY**. Phase 4 may proceed to its separate `$codex-pre-handoff-review`.
No Phase 4 implementation handoff is created or approved by this milestone review.
