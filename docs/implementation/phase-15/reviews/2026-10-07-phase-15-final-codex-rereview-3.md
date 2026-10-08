# Phase 15 — Codex Final Re-review 3

- Date: 2026-10-07
- Reviewer: Codex, review-only
- Scope: resubmitted `phase-15-backend-audit-remediation` [active handoff](../../handoffs/ACTIVE.md), entered as `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- Baseline HEAD: `6a89a998c512dda27c3e494a3525bf6118ee981e`
- Verdict: **CHANGES_REQUESTED** — two remaining **Medium** findings, FR15-7 and FR15-8

This is implementation final review, **not** the comprehensive closure audit. The
[first review](2026-10-07-phase-15-final-codex-review.md),
[first re-review](2026-10-07-phase-15-final-codex-rereview.md),
[re-review 2](2026-10-07-phase-15-final-codex-rereview-2.md), initial audit and owner decisions remain historical.
FR15-1–FR15-6 remain closed for their specific defects. No new production defect or architecture expansion is
asserted here. Finish only the narrower remaining acceptance below; do not repeat completed remediation.

## Independent verification and accepted progress

From `backend/`, Codex ran:

```text
mvn -ntp -l ../phase-15-final-rereview-3-verify.log clean verify
```

**BUILD SUCCESS: 1007 tests, 0 failures, 0 errors, 0 skipped; 03:48 min**, finished
`2026-10-07T15:21:54+07:00`. Surefire independently confirms **87 audit tests across 14 suites**; preserved
baseline: 920. Architecture tests pass. The independent log is a local ignored diagnostic artifact, not a tracked
report or evidence of unexecuted assertions.
Final documentation checks pass: `git diff --check`, **152 local link targets with 0 missing**, and
`python .agents/hooks/test_repository_safety.py` (**13 tests**).

Preserve these newly accepted improvements:

- Account identity alternatives, nullable Image metadata, nullable Address fields and corrected bounds, direct
  Knowledge create/rejection cases, independent Feed Note max+1 checks, Music's null-version default on create/update,
  and Personal phone/email create/update boundaries now have real HTTP coverage. Generated schemas cover additional
  optionality, update maxima, recurring frequency fields, Music default and literal/tag-all Search descriptions.
- Four new `StudyAccountInvariantIntegrationTest` cases (lines 598, 674, 756, 828) force **actual competing writes**:
  Account type/platform changes versus Study create/update, in both winner directions. The tests observe PostgreSQL
  ungranted locks before releasing the first writer, obtain both Future outcomes, and check safe rejection and the
  final invariant. They address the previous distinction between stale-read tests and writer contention.
- The changed Location, Rating, Journal/Personal/Feed and Study workers now obtain Future outcomes rather than
  relying on unchecked thread joins. Keep the accepted database-lock observations and prior counterexamples.
- Null-member tests now use a real validated Import job and compare rejected Finance updates' deliberately changed
  scalar fields and retained entries. Feed failures compare Vault/target/provenance counts; Import failures compare
  Vault/target state, job state and item status/imported IDs, including failure after an earlier target write.
- Monetary tests now cover HTTP create/update width and scale rejection for all five owners, independent Brand/
  Location min/max cases, exact BigDecimal responses, and persisted reloads of the maximum and four-decimal values.
- Actual 100-/101-item jobs assert the complete combined ordered index sequence without duplicates or omissions,
  import boundary items successfully, and retain beyond-first-page rollback tests. No ingestion cap or fabricated
  total metadata was introduced.
- The canonical-note links from data/security/API/storage-import entry points are present. Focused commands and
  accurate aggregate suite counts are useful evidence improvements; the note and some claims still need correction.

## FR15-7 — Remaining affected-contract regression/schema acceptance

Severity: **Medium**. Source: `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/`
`WebDtoValidationAuditIntegrationTest.java` and `ImportJobPaginationIntegrationTest.java`.
Consequence: green verification still does not protect all explicitly authorized HTTP contracts or all claimed
generated bounds. This is the remaining part of the existing acceptance finding, not a new production repair.

1. **Finish the original BA15-2 related-drift matrix, not just its main examples.** The initial audit and handoff
   expressly include the following corrected families. Their boundary HTTP/create-update and generated-schema
   regressions are absent from the submitted matrix; ordinary pre-existing web smoke/domain tests do not substitute.

   | Affected contracts still missing from that matrix | Existing accepted rule to exercise |
   | --- | --- |
   | Shopping/Software create and update names; Album create and update titles | 500 accepted, 501 rejected; generated create/update maxima |
   | Fiction/Film genres and Location categories, create/update | Name 150 accepted, 151 rejected; generated maxima |
   | Manual SavedResource creation | Title 1000/1001; author/sourceName/externalId 500/501, independently |
   | Import job creation | originalFileName 500/501 and matching generated schema |
   | Finance recurring/subscription create/update; transaction/recurring optional description | Name/provider 500/501 where applicable; description 1000/1001 and accepted null/blank behavior |
   | Settings update | Accepted values above removed HTTP caps (100/1440/8760), owning positive bounds, and generated removal of those competing maxima; corrected timezone length schema |

   Within the newly covered families, Account `displayName`/`ownerName` bounds are still POST-only (lines 646–683);
   Vocabulary PUT only rejects overlong `word`, not the changed pronunciation/sourceName/partOfSpeech bounds
   (lines 1012–1071). Several changed Image/Address/Study/Information/Note bounds have successful maxima on create
   but only rejection cases on update. Complete the missing accepted/rejected update counterparts and generated
   affected-schema assertions, including Information/Vocabulary. Reuse fixtures; do not duplicate already complete
   cases or add a generic production validation framework.

2. **Finish the named generated non-obvious semantics.** The schema method (lines 1219–1360) now verifies recurring
   fields and Search semantics. However, Shopping only checks the `purchasedAt` field text; it does not assert the
   generated full-replacement/null-clearing and omitted-status→WISHLIST descriptions. Software's generated update
   replacement/null-clearing/required-type semantics and the changed Finance update transaction semantics also
   remain unchecked. Protect the accepted descriptions against omission or reintroduction of the rejected default,
   purchase-time or sum-zero claims. Do not change domain behavior to fit the documentation.

3. **Make the Import query-schema bound assertions fail when bounds disappear.** In
   `paginationInputRejectionsAndIncompleteDecisions` (lines 313–343), missing page default/minimum pass through
   `asInt()` returning zero. A limit schema with neither `minimum` nor `exclusiveMinimum` also passes; `minimum=0`
   is accepted without requiring exclusivity. Require the actual parameter/schema properties and a lower bound
   that excludes zero, handling the actual generated representation. Retain the new full sequence/HTTP rejection
   tests and the existing null-meta contract; no response redesign is requested.

The previous actual-writer, monetary-variant, complete-sequence and newly added persisted-state cases above are
accepted progress, no longer requests to redo those repairs.

## FR15-8 — Canonical implementation notes and evidence still misstate behavior

Severity: **Medium**. Primary source: [new canonical implementation note](../../../architecture/phase-15-implementation-notes.md).
It is linked as authoritative from four architecture entry points, so these are operational/security/API promises,
not merely cosmetic typos. Correct documentation to source; **do not implement the invented behavior**.

- **Authentication (lines 21–26):** no registration/password-update API exists. `BootstrapService` enforces creation
  length 12–128; `LoginRequest` has `NotBlank`/max 128, not a new 12-character minimum. `PrivatePinService.verifyPin`
  validates six digits and verifies the hash in an authenticated context; it does not implement rate-limited
  verification or server automatic lock timers. Settings/UI privacy-lock intent is not evidence of such a backend
  mechanism. Name the actual PBKDF2 default and delegating bcrypt verification/fallback without new auth policy.
- **Error contracts (lines 29–42):** the API uses its custom `ApiResponse(data,error,meta)` envelope; do not promise
  an unimplemented Problem Details format. The new monetary HTTP tests themselves assert **422** with
  `BRAND_INVALID`, `LOCATION_INVALID`, `KNOWLEDGE_INVALID` or `INVALID_COLLECTION`, not blanket
  **400 VALIDATION_ERROR**. Public Feed/Import Knowledge handlers return **422 INVALID_KNOWLEDGE_ITEM** and
  **409 KNOWLEDGE_CONFLICT**, with **404 KNOWLEDGE_NOT_FOUND** only where applicable, not the note's generic
  VALIDATION_ERROR/CONFLICT mapping. First-read I/O failure is 500 INTERNAL_ERROR; known storage failures retain
  their specific existing mappings, including 409 STORAGE_DISABLED/STORAGE_INTEGRITY_ERROR. Do not generalize all
  early retrieval failures to 500 or replace the canonical contracts.
- **Import (lines 51–53):** `meta == null` does not mean its JSON property is omitted; the test's `doesNotExist()`
  allows a null value and is not proof of omission. Document the actual null-meta envelope. Execution rollback
  preserves **prior** job/item/decision/imported-ID state, which need not always be VALID/unlinked; those are the
  current test fixtures, not a new universal rollback rule.
- **SPI and startup symbols (lines 13, 61–65):** the startup class is `FlywayStartupIntegrationTest`, not the
  nonexistent `FreshStartupFlywayAuditIntegrationTest`. `ExternalAccountMutationGuard` is the Account-owned SPI;
  `StudyExternalAccountGuard` is its concrete Study-owned Spring bean implementation, not a second interface.
  Explain the actual synchronous callback inside the Account update transaction, Account row lock/refresh before
  guard evaluation, Study-owned reference query and assignment guard, and unchanged Knowledge→Account direction.
  No Account→Knowledge import, shared repository, portability bypass, asynchronous substitute or general
  deadlock-free guarantee is authorized.

Also finish the bounded evidence corrections in [test-evidence.md](../test-evidence.md) and ACTIVE:

- ACTIVE still lists nonexistent Finance `splits[0]`; the actual field is `entries[0]`. Its Media narrative says
  four tests; both source/Surefire and its evidence table show eight. Preserve the truthful 87/1007 aggregate count.
- The Import validation test is `importJobExecutionInvalidKnowledgeItemTriggers422AndRollsBack`, not the listed
  `importJobExecutionKnowledgeValidationErrorTriggers422AndRollsBack`. `FOUR_DECIMALS` is **10.1234**, not 12345.6789.
- The forced category FK branch runs through a constructed service/transaction and repository test proxy;
  the already-missing-category HTTP 404 is separate. Do not label the proxy case as an executed HTTP race.
- `FeedSourceRepository.updateFetchTimestamps` is not the removed method shown by the reviewed diff. Describe the
  actual lock/refresh/authoritative scheduling/`saveAndFlush` correction using existing symbols.
- The original Import HTTP endpoint returned the first bounded window (default 50, selectable up to 100), not
  strictly an unreachable-after-50 rule. Do not confuse original reachability with materialization policy.
- Focused commands are now listed, but retained pre-fix/failing evidence or an honest unavailable/not-reproduced
  statement is still absent, despite ACTIVE claiming truthful pre-fix details. Distinguish captured execution
  evidence from proposed commands and passing current runs. Reference preserved artifacts when available; do not
  invent retrospective runs/approval or rewrite earlier reports.

## Other mandatory review dimensions and limitations

- **Correctness/persistence/transactions/performance:** retain the six accepted production corrections and new
  assertions above. Existing scoped owner locks, bounded Import query, atomic rating upsert and approved exact
  numeric validation remain proportionate. No new schema/index/N+1/deadlock allegation is made here.
- **Clean code/SOLID/reuse/pattern fit/extensibility:** keep owner-local validators, existing DTO/advice structure and
  narrow synchronous guard. Remaining work does not justify a generic locking/validation framework or hierarchy.
- **Security/privacy:** full-input encoding/legacy verification, auth/PIN/JWT/refresh and binary/operational tests
  pass. Correct the misleading security promises; no credentials or sensitive payloads are reproduced in this report.
- **Architecture/scope/tree:** no changed SQL/DBML/module matrix/package-tree/diagram baseline was found. The only
  migration-directory deletion is the authorized stale `.gitkeep`; retain approved ownership descriptors and the
  existing dependency direction. Remaining test/docs work requires no expanded owner authority.
- **Static diagnostics:** the test-only Jackson 2 converter removal warning remains at
  `MediaBinaryFramingWireIntegrationTest:82`; Byte Buddy dynamic-agent and Maven Lombok/Unsafe tooling warnings
  remain non-blocking diagnostics. No IDE or comprehensive SpotBugs/PMD/CPD/dependency/spelling closure pass is
  claimed by this invocation. Full/static/architecture closure belongs to `$codex-backend-audit` after acceptance.
- **Evidence preservation/review edits:** Codex modifies only this report, the same handoff and current governance
  links. No production/test remediation, staging, commit or publishing. The initial audit SHA-256 remains
  `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`; earlier review reports and independent logs
  are preserved. Graphify was navigation only (expanded tokens `[web, request, validation, schema, contract]`);
  conclusions above were verified in current source, not inferred from its pre-remediation index.

## Final gate

**CHANGES_REQUESTED**: FR15-7 and FR15-8 remain blocking Medium findings, with the remaining work narrowed above.
Complete only missing regression/schema assertions and truthful evidence/canonical notes, then run focused tests
and `mvn -ntp clean verify` and resubmit the same handoff. Do not change production behavior to satisfy erroneous
claims. No new handoff, owner implementation commit, commit message or Phase 16/17 preparation is authorized.

**Next step:** Antigravity `/antigravity-implement-handoff`, then `$codex-final-review` again. If accepted later,
**do not commit yet**: closure `$codex-backend-audit` must return `BACKEND_AUDIT_READY` first.
