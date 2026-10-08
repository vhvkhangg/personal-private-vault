# Phase 15 — Codex Final Re-review 2

- Date: 2026-10-07
- Reviewer: Codex, review-only
- Scope: resubmitted `phase-15-backend-audit-remediation` [active handoff](../../handoffs/ACTIVE.md), entered as `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- Baseline HEAD: `6a89a998c512dda27c3e494a3525bf6118ee981e`
- Verdict: **CHANGES_REQUESTED** — two remaining **Medium** findings, FR15-7 and FR15-8

This is the next implementation final review, not the comprehensive closure audit. The
[first review](2026-10-07-phase-15-final-codex-review.md),
[previous re-review](2026-10-07-phase-15-final-codex-rereview.md), initial audit and owner decisions remain historical.
FR15-1–FR15-6 remain closed for their specific defects. Do not repeat completed repairs or restart the audit.
No new production defect or architectural expansion is asserted by this review.

## Independent verification and accepted progress

From `backend/`, Codex ran:

```text
mvn -ntp -l ../phase-15-final-rereview-2-verify.log clean verify
```

**BUILD SUCCESS: 996 tests, 0 failures, 0 errors, 0 skipped; 03:35 min**, finished
`2026-10-07T13:18:40+07:00`. Surefire confirms **76 audit tests across 14 suites**; the preserved baseline is 920.
Application architecture tests (35), Collection architecture tests (3) and Knowledge architecture tests (3) pass.
The count is accurate, but does not prove absent assertions or incorrectly described cases.
Final documentation checks pass: `git diff --check`, 141 local link targets (0 missing), and
`python .agents/hooks/test_repository_safety.py` (13 tests). The initial audit SHA-256 is unchanged:
`5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.

Preserve these substantive improvements; they are no longer the missing cases described in the previous review:

- Boot is actually launched against the migrated PostgreSQL database; fresh startup is also covered without the
  old explicit test migrator. The second startup is a new application instance, not merely `flyway.validate()`.
- Diary create/update, Note create/blank update, Personal create and generated Feed/Diary/Note/Finance/Collection
  schema assertions add real HTTP coverage. Account snapshot fixtures and both Finance null-member update routes
  now use valid payloads and assert the correct `VALIDATION_ERROR` fields.
- Import HTTP execution now exercises public Knowledge 409/422 failures, including rollback of an earlier target
  write. The 101-item service case also fails after a selected item outside the first page.
- Embedded-wire tests now cover first-read failure with prepared lengths 20 and 50000, complete parseable JSON,
  stream closure and a real client disconnect. Keep the existing Media timeout-callback tests as well; do not claim
  the new socket-disconnect case itself induces a servlet timeout.
- Location and Rating now observe an actual PostgreSQL ungranted lock instead of the old sleep-only check.
  Location adds the opposite writer/prior-read order; Journal/Personal now force both writer orders. Feed adds
  enabled scheduling and fetch-first/configuration contention. A separate Rating test captures the original
  createdAt and compares it after an update.
- The category test now reaches a real PostgreSQL FK INSERT failure through a repository test proxy. The safe
  domain exception/rollback and the separate missing-category HTTP 404 are useful, distinct pieces of evidence.
- All five monetary owners now have decimal-preserving HTTP success/update coverage, and real 100-/101-item jobs
  import boundary items with `Optional.isPresent()` assertions. The prior double/non-null Optional issues are fixed.

Java test paths below are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

## FR15-7 — Required regression acceptance remains incomplete

Severity: **Medium**. Consequence: the green run still cannot establish the entire authorized HTTP contract,
writer-serialization invariant, or all-or-nothing persisted state. This is a remaining acceptance finding,
not permission for speculative production changes. Complete only the remaining cases below.

1. **BA15-2 / BA15-15 — affected DTO and generated-semantic matrix.**
   `audit/WebDtoValidationAuditIntegrationTest` has seven tests. It still does not exercise the corrected Account
   identity alternatives, nullable Image metadata, Address nullable fields/type 100/postal 32, other direct Knowledge
   DTOs, Music's omitted-version default, or the other handoff-identified related length/range changes through HTTP
   create/update. Personal covers create only, not its corrected phone/email/update rules. Note/Feed cases do not
   independently reject every changed field at max+1 on the relevant routes.
   The generated-schema method checks a useful subset, but no update schemas, Account/Media/Address optionality,
   Music default, recurring frequency-specific fields, Collection update/default/purchase-clearing descriptions,
   or literal/tag-all Search semantics. Complete the affected-contract matrix identified by BA15-2/15; assert
   actual owning defaults/null/blank/identity semantics and maxima/max+1, not merely route presence. Existing
   ordinary domain/web smoke tests are not the missing boundary/generated-semantic assertions.

2. **BA15-13 — distinguish stale-read interleaving from competing writers.**
   In `audit/StudyAccountInvariantIntegrationTest` lines 441–513 and 517–589, T1 reads the account, waits on
   `t2CommitLatch`, then invokes its assignment/mutation operation **after T2's write has committed**.
   These are useful stale-context update regressions, but would also pass if fresh validation were retained and
   the account-lock serialization removed: the two invariant-changing writes never contend.
   Retain them and the earlier counterexamples. Add deterministic contention where one public writer holds the
   account guard while the other public assignment/mutation reaches the same guard before release; verify the
   chosen order, safe rejection/rollback and final invariant. Cover Study create/update versus invalidating
   type/platform edits as required by the handoff, without a reverse dependency or new locking framework.
   Also propagate all worker failures and assert completion in the changed concurrency suites. Raw `Thread`
   bodies that only rethrow on a worker and unchecked `join(5000)` are not assertions that both requests succeeded;
   use futures or equivalent captured outcomes. Keep the newly proved PostgreSQL lock observations.

3. **BA15-4 / BA15-6 — complete persisted no-write/rollback snapshots.**
   `audit/NullCollectionMemberValidationTest` now correctly tests Account and Finance validation. However, the
   transaction update has no persisted-state comparison, and the recurring update compares the same name sent
   in the request rather than its full unchanged state/entries. Import null-member validation uses job ID 1
   without a job-state fixture. Use existing fixtures and deliberately changed scalar controls, and verify relevant
   parent/child state is unchanged for rejected updates/execution; retain legitimate empty/null-list behavior.
   `audit/FeedImportKnowledgeExceptionIntegrationTest` and the 101-item failure in
   `audit/ImportJobPaginationIntegrationTest` check target counts/job status, but not unchanged item
   status/decision/imported-ID provenance or the corresponding Vault state after the earlier target write rolls back.
   Capture and compare those states as required by the atomicity contract. Keep the actual 409/422 HTTP workflows.
   A 404 failure case is required only where the real public workflow makes it applicable; do not invent a
   not-found classification for owning validation errors.

4. **BA15-12 — finish the affected monetary rejection/round-trip variants.**
   `audit/PriceNumericBoundsIntegrationTest.httpBoundaryRejectsPriceViolationsAndAcceptsMaxValid` fixes exact
   parsing and has successes across all five owners. Only Brand POST exercises both excessive scale and integer
   overflow; the other owners' HTTP rejection cases are update/scale only, and Brand update has no rejection case.
   Independent invalid maxPrice checks for Brand/Location remain absent. Complete the missing create/update
   width/scale error variants and independent min/max fields, and compare persisted reloads after HTTP updates,
   not only the update response. Preserve existing representable-value semantics and authoritative owner validators;
   do not add rounding, normalization or currency-specific rules.

5. **BA15-14 — assert complete ordered inspection and the actual metadata contract.**
   `audit/ImportJobPaginationIntegrationTest` now has real job boundaries and execution of index 100. HTTP pages
   assert size plus first/last indices, not the complete combined sequence with no omissions/duplicates. Add that
   assertion and the actual bounded-list envelope/query contract. The current controller deliberately returns a
   list with `meta == null`; assert/document that behavior rather than requiring new fabricated total/page metadata.
   Verify the generated page/limit defaults and bounds and retain the compatible default first page. No new total
   ingestion limit or pagination response redesign is requested.

These are the remaining portions of the existing finding. No repeat of the now-completed restart, binary first-read,
valid null-member fixture, FK-branch, decimal parsing or true job-boundary repairs is requested.

## FR15-8 — Evidence and implementation-documentation claims remain unreliable

Severity: **Medium**. Sources: [test evidence](../test-evidence.md) and the handoff's submitted-result narrative.
Versions and the 76/996 count table are now correct; other material claims still conflict with source/assertions:

- The fresh-start test is `freshStartupAppliesFlywayMigrationsAndValidatesSchema`, not
  `freshPostgresStartupRunsFlywayAutomatically`. The second instance starts against the already migrated DB;
  the original test application is not closed/restarted by that method. Describe the actual lifecycle accurately.
- `ConvertToStudyRequest` has `currentProgressText`, not `source`. The evidence incorrectly reports a Study
  source annotation. The schema method has no Personal schema assertion and does not prove a Personal 500-character
  bound; name/relationship in that test are 255/100. Do not describe all affected schema/default rules as verified.
- The password below-minimum bootstrap test is correctly recorded as 422, but the added claim of a normal
  registration/login below-minimum 400 test is unsupported. There is no registration API. The audited runtime
  rejected oversized bcrypt input; do not describe that observed issue as silent truncation.
- Null-member field names are **`itemDecisions[0]`** for Import and **`entries[0]`** for Finance/Account, not
  `decisions[0]`/`splits[0]`. Location's null-member test does not assert its precise field name. Valid control
  creates do write rows; the statement that valid lists are accepted “without writes” is false.
- The public Feed/Import invalid-Knowledge handlers use **`INVALID_KNOWLEDGE_ITEM`**, not `KNOWLEDGE_INVALID`
  (the latter belongs to a different owning HTTP error path). The four new cross-module tests execute 409/422,
  not an Import 404. Separate declared handler mappings from actually executed regressions.
- First-read binary failures in the new wire tests return **500 `INTERNAL_ERROR`**, not the reported 409.
  Attribute pre-commit framing cleanup to the actual shared boundary and committed abort/cleanup to the actual
  streaming path, not merely to an advice helper. The controller method is `downloadContent`; `downloadImage`
  is on `ImageDownloadService`, not `ImageController`.
- The real category FK exception is exercised through a constructed service/transaction and test proxy; its
  HTTP assertion is a separate request for an already-missing category. Do not label the latter as HTTP execution
  of the forced FK window. Do not claim read-only query regressions that this suite does not contain.
- Real classes are `ShoppingService` and `SoftwareService`, not `ShoppingItemService`/`SoftwareItemService`.
  Study validation is `validateStudyItem`, not `validateAndLockAccountReference`. Correct both evidence and
  submitted claims, including the competing-writer/complete-rollback/HTTP-reload overstatements above.
- BA15-14 originally exposed only the first bounded page; it did **not** load every job item into the HTTP
  response. Distinguish that original reachability defect from full-job materialization elsewhere.

No exact focused command/result map or retained pre-fix/failing race evidence is supplied beyond the full verification
command. Record what actually ran with symbols/assertions/results and retained artifact references; state unavailable
or unreproduced pre-fix evidence honestly. Do not invent retrospective preflight approval or failing runs.

The actual Account SPI/lock-order paragraph is useful progress, but the handoff/ADR also require directly affected
canonical runtime/security/API/import documentation or precise linked implementation notes. Those were still not
synchronized: changes outside status/governance remain the architecture README hygiene correction. Link an accurate
implementation note from the relevant current runtime/security/API/import entry points, covering Boot-managed
startup before validate, new-password encoding/legacy verification, changed HTTP errors/contracts and bounded Import
review. Document the narrow Account guard's synchronous transaction/lock-order/ownership limits using real symbols.
Preserve frozen structure and historical phase records; this is not a new architecture decision or documentation rewrite.

## Other mandatory review dimensions and limitations

- **Correctness / persistence / transactions:** retain the six accepted corrections and newly demonstrated
  paths above. Remaining acceptance is bounded by FR15-7; no additional speculative production repair is requested.
- **Clean code / SOLID / reuse / pattern fit / extensibility:** owner-local validators, the existing advice/DTO
  approach and narrow synchronous guard remain proportionate. No generic production locking/validation framework,
  speculative hierarchy or persistence leakage is introduced by the reviewed slice.
- **Performance / resources:** existing bounded import query, scoped owner locks and rating upsert remain appropriate.
  Reuse the previous verified Feed ingestion result; no new N+1/index/deadlock allegation is made without evidence.
- **Security / privacy:** approved full-input standard encoding/legacy verification and existing auth/PIN/JWT/refresh,
  storage and operational regressions pass. No real credentials or tokens are reproduced in this report.
- **Architecture / scope / tree:** architecture checks pass. No changed SQL/DBML/module matrix/package-tree/diagram
  baseline was found; the only migration-directory removal is the authorized stale `.gitkeep`. Keep the ten approved
  descriptors, existing dependency direction and portability exception boundary. No new owner decision is required
  by the remaining test/docs work.
- **Static diagnostics:** the compiler reports a removal warning for the test-only Jackson 2 converter in
  `MediaBinaryFramingWireIntegrationTest:82`; Byte Buddy dynamic-agent warnings and the Maven startup Lombok/Unsafe
  warning remain tooling diagnostics. These are not a new blocking production finding. No IDE inspection or full
  compatible SpotBugs/PMD/CPD/dependency/spelling closure pass was performed in this invocation; do not claim
  warning-free or comprehensive audit readiness. Those closure checks remain in the dedicated audit workflow.
- **Review edits / evidence preservation:** Codex changes only this formal report, same active handoff and current
  governance/status links. No production/test remediation, staging, commit or publishing. The initial audit and both
  earlier final-review reports are preserved; independent run logs remain local ignored diagnostics.

## Final gate

**CHANGES_REQUESTED**, FR15-7 and FR15-8 remain blocking Medium findings. Antigravity must finish the bounded
test/evidence/documentation acceptance above, run focused regressions and `mvn -ntp clean verify`, and resubmit the
same handoff. Do not change production behavior merely to satisfy inaccurate evidence. Stop on any expansion beyond
the owner-approved BA15 decisions. No commit message or owner implementation commit is authorized at this gate.

**Next step:** Antigravity `/antigravity-implement-handoff`, then `$codex-final-review` again. If that review accepts
the slice, **do not commit yet**: closure `$codex-backend-audit` must return `BACKEND_AUDIT_READY` first. No new handoff
or Phase 16/17 preparation is authorized.
