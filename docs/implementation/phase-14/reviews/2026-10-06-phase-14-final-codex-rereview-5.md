# Phase 14 — Codex Final Re-Review 5

- Date: 2026-10-06
- Reviewer: Codex
- Scope: active `phase-14-portability-storage`, resubmitted after the second test-only remediation pass.
- Verdict: **CHANGES_REQUESTED** — FR14-4 remains open (1 High, further narrowed); all eight other findings remain closed.
- Accepted preparation / HEAD / `origin/main`: `6f1fd00d89a2d0674677cf4c38b814bc434ec253`.
- Phase 14 remains unaccepted/unfrozen; no commit message while this blocker remains.

[Re-review 4](2026-10-06-phase-14-final-codex-rereview-4.md) and earlier reports remain historical evidence.
The new 917-test submission materially improves risk coverage. Only the remaining gaps below require remediation;
previously accepted production behavior and completed tests must be preserved, not repeated or reopened.

## Accepted improvements

- **Commit-boundary tests accepted.** Media tests at `:838` and `:892` now execute real metadata creation inside
  TransactionTemplate with actual Spring transaction synchronization. `afterCommit` throws after PostgreSQL commit,
  proving positive reconciliation and retention; `beforeCommit` throws after real writes and triggers rollback,
  proving conservative retention, the primary failure and unchanged Image/Vault counts. These are controlled Spring
  transaction-completion faults, not a real network ACK loss, but satisfy the previously missing commit-boundary regression.
- **Parser/error-contract tests accepted.** Tests at `:1443` and `:1514` now use real Tomcat, selected MVC and
  StandardServletMultipartResolver, actual ImageController/root advice and HTTP clients. Malformed boundary and
  configured oversized bytes return JSON 400/MALFORMED_REQUEST and 413/PAYLOAD_TOO_LARGE rather than HTML.
  Uploader/storage spies see no calls; Image/Vault counts remain unchanged. Prior HTML/mock-Part gaps are closed.
- **Upload/configuration assertions accepted.** The generated 55 MiB bound test at `:1587` now checks input closure
  and no new residual upload spool files. Enabled startup at `:1644` now checks the malformed endpoint URI in addition
  to blank bucket/region/credentials and invalid size. These previously missing assertions are closed.
- **Race aftermath / worker hygiene accepted.** Both concurrent variants now verify exactly one additional
  Image/Vault row, winner download bytes, loser Image absence and bounded worker waits/finally shutdown. The failed
  compensation variant proves all three attempted delete keys are the loser key. The successful variant retains its
  loser deletion/winner availability assertions. The still-missing proof is *database arbitration*, not these outcomes.
- **Actual image cleanup hooks accepted as branch regressions.** Test at `:1705` invokes the actual controller/
  download service for body success/failure, obtains its registered `imageDownloadCleanup` interceptor, and checks
  stream closure on direct timeout/error callbacks. It no longer tests unrelated handwritten lambdas. Direct callbacks
  are correctly distinguishable from an actual in-flight cancellation/completion event.
- **Early snapshot failure cleanup accepted.** New Portability test at `:846` uses the real service/adapter with an
  instrumented PostgreSQL connection. It proves early callback failure rolls back, closes the connection and schema-
  validation statements, and deletes the temporary archive. It does not yet reach active archive generation.
- Prior read-only SQLState enforcement/audit samples, exact JSONB fidelity, actual committed-export HTTP abort and
  temporary archive deletion, delayed-commit retention, privacy, architecture/schema and 214-route contracts remain green.

## FR14-4 — High: two remaining execution-path gaps and corresponding evidence

These requirements are already in the accepted [testing contract](../README.md#testing-contract) and
[active handoff](../../handoffs/ACTIVE.md#testevidence-contract); no new production scope or framework is requested.

### 1. The second barrier still does not guarantee PostgreSQL uniqueness arbitration

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:952` and `:1087`.

In each `arbitratingOps.create`, thread 1 executes `imageOperations.create` inside an outer transaction, waits at
`metadataOverlapBarrier`, then returns so that transaction commits. Thread 2 waits at the same barrier **before**
calling `imageOperations.create` or starting its metadata transaction. When the barrier releases, thread 1 can commit
before thread 2 starts the checksum SELECT. Thread 2 then takes ImageService's known-duplicate precheck path.
Both paths produce ImageConflictException and satisfy every current count/key/download assertion. No assertion
distinguishes the precheck exception from the wrapped PostgreSQL unique-constraint exception.

Consequently the statement that thread 2 enters its precheck while thread 1 is held, then necessarily attempts an
insert arbitrated by PostgreSQL, is not established. A barrier before the second call does not order that call's
precheck ahead of the first commit. Concurrent uploads and verified aftermath are useful, but this test can remain
green even when the actual database-arbitrated loser rollback path is never exercised.

**Required correction:** use test-only instrumentation to make both relevant checksum prechecks observe absence
before either insert can commit, or authoritatively observe the second insert contending on the uncommitted winner
before allowing commit. Verify the loser really came from PostgreSQL uniqueness arbitration (not only exception
class), then retain all current Image/Vault, winner-byte, loser-only key and max-three-failed-delete assertions.
A barrier after actual empty precheck results is one small option; no production hooks, schema changes, sleeps-as-
proof, generic concurrency framework or frozen ImageService rewrite is needed. Keep bounded/unconditional worker cleanup.

### 2. Export lifecycle test fails before any ZIP or active row cursor is opened

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java:846`;
adapter `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java:190`.

The test throws `RuntimeException("Simulated snapshot generation failure or cancellation")` from
`onSnapshotEstablished`. In the actual adapter that callback runs after schema validation but **before** the output/
ZipOutputStream try-with-resources, table JSONL reads and PreparedStatement/ResultSet consumption. The test tracks
only Statement closure and connection rollback/close; no ResultSet/ZIP is instrumented. Calling an ordinary early
exception “cancellation” does not trigger timeout/cancel or disconnect. It cannot detect broken cleanup in the row/
ZIP consumption paths that it never enters.

The image hook test directly calls timeout/error methods, but still has no cancellation/completion case. Keep its
accepted real-controller hook assertions; do not replace them with handmade stream code or reimplement the callback.

**Required correction:** add bounded execution-path regressions with actual archive generation/row consumption in
progress. Observe nonzero active PreparedStatement/ResultSet work and closure, transaction release, ZIP/output and
temporary-file cleanup on generation failure, the selected timeout/cancellation strategy and simulated disconnect,
alongside successful release. Retain the already accepted real committed-export transfer-failure test; it is not
proof of generation cancellation. Complete the missing image cancellation/completion cleanup case through the actual
registered application/async path. No extra network test is required merely to repeat the now-proven parser, transfer
abort, commit-boundary, startup or size-limit behavior.

### Evidence / authority

`docs/implementation/phase-14/test-evidence.md`, section 11, retains unsupported claims of deterministic database
arbitration and “all requirements” lifecycle proof. Correct current coverage statements to match actual assertions
and distinguish early failure/direct callback invocation from active-consumption cancellation. Its header still
describes the previous 916-test/re-review-4 state; current status is synchronized by this review, and section 12
records genuine independent commands. Historical reports and reported submissions remain qualified, not rewritten.

Remediation is **test/evidence-only** via `/antigravity-test-slice`. If these tests expose a production defect, report
it and route through `/antigravity-implement-handoff` within this handoff; do not change production in the test-only
pass, weaken assertions, suppress tests or broaden frozen boundaries.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify` — exit 0, **BUILD SUCCESS**, **917 tests**, zero failures/errors/skips;
  **03:08** elapsed, finished **2026-10-06T17:42:35+07:00**. Before focused rerun, 81 Surefire XML reports contain
  exactly 917 testcase elements. Media 33, Portability 12, architecture 35, schema manifest 2 and OpenAPI 1 pass.
- `mvn -f backend/pom.xml -ntp "-Dtest=MediaStorageIntegrationTest,PortabilityIntegrationTest,OpenApiRouteInventoryIntegrationTest" test`
  — exit 0, **BUILD SUCCESS**, **46 tests** (33 Media + 12 Portability + 1 OpenAPI), zero failures/errors/skips;
  **52.001 s**, finished **2026-10-06T17:46:50+07:00**. Green totals do not imply execution of the missing paths.
- Windows/Java 25.0.2/Spring Boot 4.1.1/PostgreSQL 18.6/MinIO Testcontainers, Tomcat 11.0.24 wire regressions.
  Approved escalation for dependencies/Docker; no failed/cancelled Maven command or verification retry. No new
  standalone diagnostic or production/test implementation was created by Codex in this review.
- HEAD/`origin/main` unchanged; frozen DBML/migrations/architecture/ADR and protected `.agents` show no diff.
  Codex changes only review/governance/evidence docs; no commit/push/tag/PR.
- `git diff --check` passes after synchronization; 106 local links across 11 documents resolve and their
  trailing-whitespace scan passes. The three checked focused-run review-owned containers are no longer running.

## Mandatory review dimensions / scope

This is incremental re-review of the test/evidence slice, retaining prior production review conclusions. No new
production correctness, privacy, persistence-ownership or architecture defect is reproduced. Canonical Image/Vault
behavior, conservative storage/commit decisions, sanitized error/transfer handling, immutable public contracts,
read-only leaf isolation and original 211 HTTP operations remain intact under fresh full verification.

Clean-code/cohesion, reuse, dependency direction and Media's concrete port/adapter remain appropriate; no speculative
Strategy/event/hierarchy or duplicate domain path is required. Disk/cursor processing remains bounded in source;
generated input supports the upload bound, while export lifecycle assurance remains the precise finding above.
No new measured N+1/scan/lock-contention defect; the earlier extra count traversal remains nonblocking.

Package descriptors/tree and schema inventory remain consistent with the approved delta. Compiler warnings remain
Lombok Unsafe, deprecated APIs and unchecked OpenAPI operations; Mockito agent/CDS and Tomcat inspection-access
warnings occur. No IDE inspection or warning-free claim. Status/evidence docs follow this verdict; accepted
preparation and historical reviews remain unchanged. Phase 14 is not a milestone gate.

## Next step

Run Antigravity `/antigravity-test-slice` for only the remaining FR14-4 race/lifecycle tests and evidence, then
`$codex-final-review`. No owner commit/push or Phase 14 closeout until acceptance; uncovered production defects
require the implementation-remediation route.
