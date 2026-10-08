# Phase 15 — Codex Final Implementation Re-review

- Date: 2026-10-07
- Verdict: **CHANGES_REQUESTED**
- Blocking findings: **2 Medium — FR15-7 and FR15-8 remain open**

## Scope and gate

Re-reviewed the resubmitted `phase-15-backend-audit-remediation` handoff, which entered this pass as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`. HEAD remains `6a89a998c512dda27c3e494a3525bf6118ee981e`; the remediation
is an uncommitted working-tree slice. Authority remains the [owner decisions](../owner-decisions.md),
[ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md), and the
[same active handoff](../../handoffs/ACTIVE.md). No remediation implementation, new handoff, commit or publishing
was performed by Codex.

This continues the [first final review](2026-10-07-phase-15-final-codex-review.md), without repeating the completed
initial comprehensive audit. That review and its 962-test evidence remain historical. This is **not** the mandatory
repository-wide closure `$codex-backend-audit`; all 17 BA15 findings still await the authorized closure process.

## Independent verification

- From `backend/`: `mvn -ntp -l ../phase-15-final-rereview-verify.log clean verify` — **BUILD SUCCESS**,
  **978 tests, 0 failures, 0 errors, 0 skipped**, 03:50 min, finished 2026-10-07T11:43:37+07:00.
- The 14 submitted audit suites account for 58 tests, matching the submitted count table. Count accuracy does
  **not** establish that each suite proves the acceptance cases described below.
- Architecture checks passed: `ApplicationArchitectureTests` (35), `CollectionArchitectureTests` (3), and
  `KnowledgeArchitectureTests` (3). No new application module, named interface or dependency direction was found.
- Observed toolchain: OpenJDK 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith **2.1.1**,
  Hibernate 7.4.5.Final, Testcontainers 2.0.5, PostgreSQL 18.6.
- Review-only probe: `backend/target/phase15-final-rereview/FeedLockProbe.java`, using the existing Maven test
  classpath, fresh Testcontainers PostgreSQL and ordinary application startup. Logs are ignored
  `phase-15-final-rereview-probe.log` and `phase-15-final-rereview-probe-complete.log`.
  - Forced both distinct-item ingestions past their identity INSERTs before either source lock. Both completed;
    exactly two items committed. Captured Hibernate SQL uses `FOR NO KEY UPDATE`, not `FOR UPDATE`.
    The suspected FK-lock upgrade deadlock was **not reproduced** and is not a finding.
  - Real authenticated HTTP `Accept: application/xml` returned 406, complete canonical JSON,
    `application/json;charset=UTF-8` and `no-store` cache protection, rather than the former empty response.
  - Generated OpenAPI includes login 415 and reference-country 406, corrected Finance/Collection descriptions,
    and the corrected Feed conversion schemas.
- `git diff --check` and all 13 repository-safety policy tests passed. Frozen SQL migrations, DBML, module matrix,
  diagram sources and repository/package-tree baseline are unchanged; the approved migration-directory
  `.gitkeep` removal is not a migration change. The initial audit report SHA-256 remains
  `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.
  All 137 local link targets in the review/current-status documents exist; anchors and external URLs were not checked.
- Build diagnostics include deprecated API use in `ApiExceptionHandler`, unchecked operations in
  `OpenApiConfiguration`, the new wire-test Jackson 2 converter's removal warning, and existing JVM/Mockito/Lombok
  notices. No IDE inspection or new repository-wide static/coverage closure was claimed or run in this pass.

The first disposable probe completed successfully. Its expanded rerun captured actual lock SQL and HTTP/schema
results successfully. A sandbox-denied initial probe compilation was rerun with approved access; it is not a
production compilation failure. No owner credentials or returned tokens were printed.

## Previous finding disposition

These dispositions close the specific previous defects, not the wider BA15 closure gates. Outstanding associated
regression/documentation acceptance is tracked under FR15-7/FR15-8 rather than reopening duplicate findings.

| ID | Disposition | Verified correction |
| --- | --- | --- |
| FR15-1 / BA15-13 | **CLOSED** | `ExternalAccountService.update` and `findAndLock` refresh authoritative state under the Account lock; Study has no unlocked fallback. The two stale managed-read counterexamples now have passing transaction/latch tests. The public Account guard SPI and Study-owned query preserve Knowledge → Account. |
| FR15-2 / BA15-17 | **CLOSED** | Fetch scheduling uses refreshed configuration under the source lock, saves managed timestamps through the entity lifecycle, and no longer uses the stale bulk timestamp updater. The prior-read/120-minute counterexample passes; the forced distinct-ingestion probe also passes. |
| FR15-3 / BA15-5 | **CLOSED** | 415/406 write the canonical JSON envelope without a second negotiation. MockMvc and the real 406 probe pass; applicable example responses appear in generated OpenAPI. |
| FR15-4 / BA15-15 | **CLOSED** | Finance descriptions now say POSTED, optional income/expense category, and opposite-sign/distinct-wallet transfers without a sum-zero rule. Collection descriptions use real enums/defaults and nullable purchasedAt. Generated examples match the corrected prose. |
| FR15-5 / BA15-2 | **CLOSED** | Feed conversion DTOs now use the owning 500-character bounds and non-null, potentially blank Note Markdown. Conversion boundary tests and generated schema inspection confirm the specific fixes. |
| FR15-6 / BA15-8 | **CLOSED** | Known provider length includes zero. Embedded-wire tests cover zero, smaller/larger/null metadata and unknown-length fallback, asserting provider bytes and length. No mismatch-rejection policy was introduced. |
| FR15-7 | **OPEN — Medium** | Required regressions remain incomplete, and several tests do not prove their stated cases. |
| FR15-8 | **OPEN — Medium** | Evidence still invents symbols/behavior and overstates coverage; required actual implementation documentation remains incomplete. |

## FR15-7 — Required acceptance is still not proved by the green tests

**Consequence:** The 978-test result can pass with important authorized regressions absent, and some added tests
would remain green without the repair they claim to cover. This prevents final acceptance, particularly for HTTP
validation and concurrency/atomicity. Do not replace the missing evidence with more speculative production changes.

Java test paths below are under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

- **BA15-1:** `audit/FlywayStartupIntegrationTest.restartAgainstMigratedDatabaseSucceeds` only calls
  `flyway.validate()` and inspects the already-running context. It does not restart the application against the
  migrated database. Add the required actual close/restart verification without a manual migration prerequisite.
- **BA15-2 and BA15-15:** `audit/WebDtoValidationAuditIntegrationTest` covers only three Feed conversions.
  No create/update boundary/default/null/identity-alternative matrix or generated-schema semantic assertions were
  supplied for the other affected DTO families. `web/OpenApiRouteInventoryIntegrationTest` checks route/security/
  response structure, not the corrected field maxima/defaults or Finance/Collection descriptions. Cover the
  handoff-identified affected contracts, including maxima/max+1 and blank Markdown; assert the generated semantics,
  not merely presence of the route or compilation. Also complete the corrected Feed field rejection/schema cases.
- **BA15-4:** `audit/NullCollectionMemberValidationTest.followerSnapshotRejectsNullMember` posts to
  `/api/v1/accounts/followers/snapshots`: `followers` is not a numeric owner ID. The request also lacks the actual
  `capturedAt` and `source` fields. Its accepted `MALFORMED_REQUEST` result is not proof of `entries[0]` validation.
  The recurring-rule case sends `type` instead of required `transactionType`, omits MONTHLY's `dayOfMonth`, and
  never performs the update its name promises. Use otherwise-valid fixtures/payloads, verify the precise null-member
  field error, cover both Finance update variants, and assert unchanged persisted state. Preserve supported list
  null/empty semantics with valid controls; do not change domain contracts to accommodate these test payloads.
- **BA15-6:** `audit/FeedImportKnowledgeExceptionIntegrationTest` now exercises two real Feed conversions
  (duplicate hash 409 and invalid author combination 422), an improvement over direct advice calls. It exercises
  **no Import HTTP execution**, no applicable missing-reference case, and no partial multi-item execution failure.
  Add the required Import public-Knowledge failure/translation and all-or-nothing target/item/job rollback checks.
  Use owning 422/409/404 behavior **as applicable**; do not force missing-author validation into a fabricated 404.
- **BA15-7:** The new embedded Tomcat tests prove provider framing and a committed abort/stream close. Their
  pre-commit case throws during storage opening, **before binary headers are set**. It cannot detect the original
  stale Content-Length bug. Add first-read failures after headers are prepared with lengths below/above the error
  JSON size; assert the complete envelope and cleared binary framing. Complete disconnect/timeout cleanup coverage.
- **BA15-9/10/17:** `audit/LocationConcurrencyIntegrationTest` and
  `audit/VaultRatingFirstSetConcurrencyIntegrationTest` still use `Thread.sleep(300)` plus `Thread.isAlive()` as
  supposed PostgreSQL lock observation. A pre-call latch does not establish that the competing statement reached
  the DB lock. Use actual lock/wait observation or equivalent deterministic checkpoints and propagate worker
  failures reliably. Complete required reader/writer, both-order/prior-read coverage. The lifecycle “ordered races”
  are sequential calls, not forced concurrent commit orders. Feed has one useful prior-read/interval test, but lacks
  the opposite/configuration enable/scheduled cases; `updatedAt != null` cannot prove a timestamp advanced.
  Rating `createdAt <= updatedAt` cannot prove the original createdAt was preserved: capture and compare it.
- **BA15-11:** `audit/LocationCategoryAssignmentIntegrationTest.foreignKeyRaceTranslatesToDomainException`
  deletes the category **before** invoking assignment, so it exercises ordinary prevalidation, not removal between
  existence validation and INSERT. Force the actual removal window and assert the intended safe HTTP error,
  no partial assignments and no private/vendor details. Keep the useful duplicate-assignment coverage.
- **BA15-12:** Command create/update rejection and maximum-value create/reload are now covered for all five
  owners. HTTP coverage is only Brand POST; its maximum assertion converts the decimal to `double`, losing exact
  precision. Complete affected HTTP create/update and exact decimal response/reload checks, and independently
  exercise min/max price fields where relevant. Existing domain validation remains authoritative.
- **BA15-13:** Keep the two repaired stale-read counterexamples. Complete assignment create/update versus
  invalidating type/platform mutation under forced concurrent orders, with unchanged state/rollback assertions.
  The last new Study-update test is sequential, not an assignment/mutation race.
- **BA15-14:** The single pagination test uses a 105-item job; it does not create 100- and 101-item jobs despite
  its description. It imports only indices 0–4 and asserts an `Optional` is non-null instead of present. Add true job
  boundaries, complete ordered/page metadata/default compatibility assertions, review/execution beyond the first
  page, and atomic failure evidence (can share the BA15-6 rollback case). Do not impose a new ingestion cap.

The password tests now add real bootstrap/login min/max/multibyte cases and actual legacy bcrypt login. The numeric
command/update tests, Feed conversions, stale Account counterexamples and real media framing are substantive
improvements. Preserve them; the remaining work is the missing/non-proving evidence above, not a full test rewrite.
Retain or supply the handoff-required pre-fix/failing race evidence honestly. If an alleged baseline race cannot be
reproduced, return that fact for Codex disposition instead of manufacturing a repair. Run focused regressions and
then the required full `mvn -ntp clean verify` after completing this bounded work.

## FR15-8 — Evidence and implementation documentation remain inaccurate

**Sources:** `docs/implementation/phase-15/test-evidence.md` and the active handoff's submitted-result narrative.
The suite/count table is now accurate; much of the per-finding narrative is still not.

Examples that must be corrected from actual source/assertions:

- Spring Modulith is **2.1.1**, not 2.0.0. The removed manual migrator lived in
  `support/AbstractPostgresIntegrationTest`, not a nonexistent `TestPostgresContainer.java`.
- Password lengths 72/73 are encoder checks, not HTTP checks. The below-minimum HTTP assertion is **422
  AUTH_INVALID_BOOTSTRAP**, not the reported canonical 400. Report unchanged credential behavior using the
  actual relevant suites, not an unsupported blanket claim.
- Public Knowledge exceptions are `InvalidKnowledgeItemException`, `KnowledgeConflictException` and
  `KnowledgeNotFoundException`; the listed validation/resource-not-found classes do not exist. The second Feed
  case is invalid-author **422**, not missing-reference **404**. No Import failure workflow is tested by that suite.
- Rating uses `RatingRepository.upsert`, table **ratings**, conflict key **vault_entry_id** and the owning
  `internal/infrastructure/persistence` package; not `saveUpsert`, `vault_ratings` or `vault_item_id`.
- There is no introduced `NumericBoundsValidator`, `ShoppingItemService`, `SoftwareItemService`,
  `ExternalAccountService.updateAccount`, or Feed `lastSuccessfulFetchAt`. Use real symbols/fields. The invariant
  suite covers unreferenced **updates**, not the reported Account deletion behavior.
- The “FK removal race,” actual restart, both-order concurrent lifecycle, 100/101 job-boundary and generated
  semantic-regression claims must match the tests actually completed; annotate remaining limitations until fixed.
- Media's before-header opening failure is not stale-header reset coverage. The committed worker-abort behavior
  must be attributed to the actual controller/shared boundary, not inferred from a helper returning an envelope.

Synchronize directly affected canonical runtime/security/API/import implementation documentation or precise linked
implementation notes as required by the handoff/ADR. Current changes outside governance are only the identified
architecture README hygiene correction; the authorization ADR is not actual implementation documentation.
Document the narrow Account SPI's transaction/lock order and limits accurately. Do not invent earlier preflight
approval or retrospective evidence. No new ADR/edge/schema is requested by this review.

Correct the per-finding command/assertion/result map and the submitted-result claims to actual focused evidence.
Preserve historical initial-audit/review records. Codex has synchronized current review/status documentation here,
but has not rewritten Antigravity's evidence or supplied implementation tests on its behalf.

## Other mandatory review dimensions

- **Business/HTTP correctness:** the six specific prior residual defects above are corrected. Wider acceptance
  confidence is limited by FR15-7; no separate speculative production defect was added.
- **Maintainability/SOLID/reuse/pattern fit:** owner-local validators, narrow guard coordination and existing
  controller/advice patterns remain proportionate; no generic validation/locking framework or speculative hierarchy.
- **Performance/transactions:** reviewed changed lock scopes, fresh state, bounded import queries, rating upsert
  and Feed ingestion. The concrete Feed deadlock hypothesis was tested and rejected for this runtime.
- **Security/privacy:** full-input standard password encoding and legacy verification remain within approved scope;
  existing auth/PIN/token/storage regressions pass. Diagnostics do not print real credentials or returned tokens.
- **Tree/hygiene/docs:** approved Collection descriptors introduce no boundary annotations; only the two approved
  stale placeholders are removed. Documentation/evidence acceptance remains blocked by FR15-8.
- **Architecture/scope:** no frozen schema rewrite or forbidden dependency/internal/JPA/portability bypass found.
  Owner approvals/ADR boundaries remain unchanged. No additional owner decision is currently required.

## Final gate

**CHANGES_REQUESTED**. FR15-1–FR15-6 are closed in this re-review; FR15-7 and FR15-8 remain blocking Medium findings.
Keep the same Phase 15 handoff. No commit message is supplied and no owner implementation commit is authorized.

**Next step:** Antigravity `/antigravity-implement-handoff` for the bounded regression/evidence/documentation work,
then `$codex-final-review` again. A successful final review must still be followed by closure `$codex-backend-audit`;
only `BACKEND_AUDIT_READY` permits owner commit/push and Phase 15 closeout. Phase 16/17 remains owner-gated.
