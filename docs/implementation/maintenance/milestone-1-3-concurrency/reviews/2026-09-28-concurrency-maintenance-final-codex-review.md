# Codex Final Review — Phase 1–3 Concurrency Maintenance

- Date: 2026-09-28
- Handoff: `milestone-1-3-concurrency-maintenance`
- Initial result: **CHANGES_REQUESTED**; final result: **READY_FOR_OWNER_COMMIT**
- Mode: review-only; no production or test implementation changed by Codex

## Blocking findings

### High — New assertion can expose raw refresh tokens in test failure output

`backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/RefreshTokenLifecycleIntegrationTest.java:290–291`
asserts directly on `successorRaw` and compares it with `rawRefresh` using AssertJ. If that assertion fails,
diagnostics can include the raw presented and/or generated refresh-token values. The handoff explicitly forbids
raw-token diagnostics, and Phase 2 previously remediated this same failure mode.

Required correction: compute non-sensitive properties (nonblank, distinct) and assert those booleans with
non-sensitive descriptions. Do not log, interpolate, or directly assert raw token strings.

### Medium — Mixed-race tests do not deterministically establish the competing lock attempt

`RefreshTokenLifecycleIntegrationTest.java:243–279,324–360` counts down `revokeStarted`/`rotateStarted` before
calling the competing service method. The first transaction then sleeps for 150 ms and assumes the other
thread reached `SELECT ... FOR UPDATE`. On a delayed thread, the first transaction can commit before the
competing query starts; both tests can pass as sequential operations, without exercising the race they are
meant to regress. The approved scope requires deterministic PostgreSQL coordination, not a timing-only
assumption as proof of the interleaving.

Required correction: wait for observable confirmation that the competing operation is at the database lock
(or use an equivalently deterministic test mechanism) before releasing the first transaction. Retain both
rotation-first and revocation-first committed-state assertions. Update the evidence description to match the
actual coordination.

## Verified areas

- `SessionService.revoke` now uses the existing pessimistically locked predecessor lookup also used by
  `rotate`. This is a minimal in-scope production correction; no schema, entity, token format, or other
  authentication behavior was changed.
- Vault favorite/tag attachment now use targeted PostgreSQL `ON CONFLICT ... DO NOTHING` inserts within the
  caller transaction. Existing entry/trash/capability/tag checks remain in the owning service. Focused tests
  cover concurrent duplicates and enclosing rollback. No unrelated business rule, public API, module
  dependency, pattern, or concrete performance regression was found in the changed production paths.
- Codex independently ran `mvn -f backend/pom.xml clean verify` on Java 25: exit 0, 227 tests,
  0 failures/errors/skips. Spring Modulith verification and Flyway-backed PostgreSQL/Hibernate validation
  passed. The green build does not establish token secrecy on a failing assertion or deterministic race
  coverage. `git diff --check` passed.
- The implementation did not edit frozen DBML, Flyway V1, or Phase 4 production. Existing owner preparation
  and unrelated dirty-worktree files were preserved.

## Initial next step (completed)

Antigravity was asked to address only the two test findings, rerun focused and final verification, update
`docs/implementation/maintenance/milestone-1-3-concurrency/test-evidence.md`, and return the handoff for
Codex re-review. The maintenance scope and active handoff remained `CHANGES_REQUESTED` at that point;
Phase 4 stayed blocked.
At the initial review, no commit message was provided while findings remained.

## Remediation re-review — 2026-09-28

- **High resolved:** `RefreshTokenLifecycleIntegrationTest` no longer directly asserts on raw refresh-token
  strings. Nonblank/distinct properties are asserted as booleans with non-sensitive descriptions; failure
  diagnostics cannot print either raw token through those assertions.
- **Medium resolved:** Both mixed rotation/revocation tests wait for an ungranted PostgreSQL lock associated
  with a competing `refresh_tokens` query (`pg_locks` joined to `pg_stat_activity`) before the lock holder
  proceeds. The 150 ms scheduling sleeps were removed. Assertions reload committed predecessor/successor
  state for both interleavings; the short polling delay is not used as proof of contention.
- Codex independently ran `mvn -f backend/pom.xml clean verify` on Java 25: exit 0, `BUILD SUCCESS`,
  227 tests, 0 failures/errors/skips. Spring Modulith verification and Flyway-backed PostgreSQL/Hibernate
  validation passed. `git diff --check` passed.
- The correction is test-only relative to the initial review; approved production changes remain minimal
  and unchanged. No DBML, Flyway V1, public API, module boundary, or Phase 4 production change was introduced.

Final result: **READY_FOR_OWNER_COMMIT**. The owner may commit/push this maintenance slice. The Phase 1–3
milestone stays `CHANGES_REQUESTED` until the owner commit/push and a fresh `$codex-milestone-review`;
Phase 4 stays blocked meanwhile.
