# Phase 15 — Codex Final Acceptance

- Date: 2026-10-07
- Reviewer: Codex, review-only
- Scope: resubmitted `phase-15-backend-audit-remediation` [active handoff](../../handoffs/ACTIVE.md), entered as `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- Baseline HEAD: `6a89a998c512dda27c3e494a3525bf6118ee981e`
- Verdict: **READY FOR OWNER COMMIT — DO NOT COMMIT YET; CLOSURE AUDIT REQUIRED**

No blocking final-review findings remain. FR15-1–FR15-6 retain their prior specific closures; the remaining
Medium acceptance requirements in FR15-7 and FR15-8 from [re-review 4](2026-10-07-phase-15-final-codex-rereview-4.md)
are now satisfied. One **Low, non-blocking** documentation observation is recorded below for closure disposition.
This is handoff-specific implementation acceptance, **not** `BACKEND_AUDIT_READY`, owner commit permission,
Phase 15 closeout or a new comprehensive audit. All 17 BA15 findings still require repository-wide closure review.
Earlier audit/review reports and owner decisions remain historical and unchanged.

## Independent verification

Codex ran from `backend/`:

```text
mvn -ntp -l ../phase-15-final-rereview-5-verify.log clean verify
```

**BUILD SUCCESS: 1012 tests, 0 failures, 0 errors, 0 skipped; 03:53 min**, finished
`2026-10-07T17:23:18+07:00`. Surefire XML testcase enumeration confirms **1012 cases**, including **92 audit
cases across 14 suites**. The count is unchanged because the new positive controls were added to an existing
test method, not introduced as separate test methods. The preserved pre-remediation baseline remains 920.

Architecture verification passes: `ApplicationArchitectureTests` **35**, `CollectionArchitectureTests` **3**,
and `KnowledgeArchitectureTests` **3**, all without failures/errors/skips. The new Finance controls execute in
`WebDtoValidationAuditIntegrationTest.financeRecurringAndSubscriptionBoundaries` (Surefire testcase present).
`python .agents/hooks/test_repository_safety.py` also passes **13 tests**.
Final review/governance link checks pass: **158 local file targets, 0 missing**; `git diff --check` passes.
The independent Maven log is a preserved ignored diagnostic artifact, not a tracked release artifact.

Antigravity's [submitted evidence](../test-evidence.md) reports the required `mvn -ntp clean verify`, 1012/0/0/0,
Java 25.0.2, Maven 3.9.15, Boot 4.1.1/Modulith 2.1.1 and PostgreSQL 18.6 via Testcontainers. Its reported durations
are separate from the exact independent run above. PostgreSQL and existing storage/wire tests were used, not H2.

## Closure of the remaining blocking acceptance findings

### FR15-7 — CLOSED

Source: `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/WebDtoValidationAuditIntegrationTest.java`.

- Lines 1785–1796 now submit a **1000-character** description to recurring-rule PUT and assert both HTTP 200
  and exact `$.data.description` equality. This is a successful true-maximum update, not another blank control.
- Lines 1924–1934 do the same for financial-transaction PUT. Existing blank and 1001 rejection controls remain.
- The accepted related-drift matrix, update counterparts, generated non-obvious semantics, strict Import parameter
  bounds, persisted-state/monetary assertions, complete 100/101 traversal and actual-writer contention from earlier
  reviews are preserved. No repeat implementation or new regression matrix is required by this review.

### FR15-8 — Blocking Medium requirements CLOSED

The [canonical implementation note](../../../architecture/phase-15-implementation-notes.md) now describes:

- `ApiError.fieldErrors`, JSON envelopes and the approved successful binary-response exceptions;
- actual `ExternalAccountService.update`, Account-owned `ExternalAccountMutationGuard.validateMutation(...)`,
  concrete Study-owned `StudyExternalAccountGuard`, synchronous callback under Account row locking/refresh,
  and `ExternalAccountConflictException` mapped to **409 `EXTERNAL_ACCOUNT_CONFLICT`** by Account's own advice;
- the exercised bidirectional Account/Study writer serialization, not an unqualified deadlock-free guarantee.

These contract/ownership statements were checked in current source. Corrected bootstrap/login/PIN, password
encoder, error mapping, null-meta, rollback and startup notes from prior reviews remain accepted, as do the four
architecture-entry-point links. No behavior was changed to match erroneous documentation.

The new evidence classification explicitly acknowledges **no captured pre-fix runtime failure** for the
source-derived BA15-9 and BA15-13 scenarios (and the other listed analytical concurrency cases). It no longer
presents the original source reasoning as executed failing tests. It links the preserved original report, which
also retains the actually executed startup/password/HTTP/binary probes and literal SQL numeric checks.
The source-inspection bullets are not a substitute for that report's fuller executed-evidence inventory.
Passing present regressions are accepted as present verification, not manufactured retrospective failing runs.
This is Codex disposition of the disclosed evidence limitation; no historical baseline reconstruction is requested
here. The comprehensive closure audit still has to verify the repairs and any necessary further evidence.

## FR15-9 — Low, non-blocking: incorrect portability controller name in docs

- File: `docs/architecture/phase-15-implementation-notes.md:34`; repeated in ACTIVE's submitted narrative.
- Behavior: the newly added binary exception description names `PortabilityExportController`; the actual class is
  **`PortabilityController`**, at `portability/internal/web/controller/PortabilityController.java:29`, with
  `exportSnapshotArchive` handling the existing `POST /api/v1/portability/exports` route.
- Consequence: misleading source navigation. The raw ZIP route/envelope exception itself is described correctly;
  this is not an HTTP behavior, security or dependency defect and does not block handoff final acceptance.
- Required correction: replace that documentation symbol with the actual class name (or name the existing route).
  **Documentation only; do not rename/rework production code.** Carry this on the same handoff into closure
  disposition under the already authorized BA15 documentation scope. It is not accepted as permanent debt.

Codex did not make this remediation. No second handoff or new owner authorization is needed for this bounded
documentation correction; the next workflow action remains the comprehensive closure audit.

## Mandatory review dimensions and limits

- **Business correctness:** the missing compatibility boundaries now have real HTTP success/full-value assertions.
  Accepted defaults, null/blank/identity behavior, Markdown preservation, precise errors and failure-state checks
  remain intact. No newly demonstrated production defect was found in this resubmission.
- **Clean code/SOLID/reuse/pattern fit/extensibility:** owner validators and existing DTO/advice/application paths
  remain canonical. The narrow synchronous guard protects a concrete invariant without reversing dependencies;
  no speculative framework, generic base, event substitute or unrelated refactor was introduced for the remnants.
- **Performance/persistence/transactions/concurrency:** retain the previously reviewed owner-local locks/fresh state,
  authoritative scheduling, atomic rating upsert, bounded Import queries and exact numeric validation. Full
  PostgreSQL regressions pass; no new N+1, scan, memory-growth or lock-expansion finding is asserted. Current
  tests prove their particular interleavings, not universal absence of deadlocks.
- **Security/privacy/HTTP/OpenAPI:** full authentication/PIN/JWT/refresh, binary/operational and contract suites
  pass. Revised notes no longer invent security features or wrong guard/error contracts. Synthetic descriptions
  and narrow callbacks introduce no secret logging or cross-module persistence access.
- **Architecture/scope/tree/docs:** no changed SQL/DBML, module matrix, repository-tree or architecture-diagram
  baseline was found; the authorized migration `.gitkeep` deletion is not a schema change. Keep approved ownership
  descriptors and the existing Knowledge→Account direction. The remaining Low item requires no architecture
  expansion. Any new frozen-baseline expansion remains `OWNER_DECISION_REQUIRED`.
- **Diagnostics:** Maven still reports root API deprecation/unchecked-operation notes, the test-only Jackson 2
  converter removal warning at `MediaBinaryFramingWireIntegrationTest:82`, and Byte Buddy/JVM agent warnings;
  the launcher emits Lombok/Unsafe warnings. These are not silenced or described as warning-free. No IDE or
  comprehensive static/coverage/dependency/spelling closure pass was run by this invocation.
- **Evidence/review edits:** only this formal report, the same handoff and current governance status are edited by
  Codex; no production/test remediation, staging, commit or publishing. Initial audit SHA-256 remains
  `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`; earlier logs/reports are preserved.
  Graphify was navigation only (verified expansion `[web, request, validation, schema, contract]`); conclusions
  were checked in source and the original evidence, not the pre-remediation graph.

## Final gate and commit message

**READY FOR OWNER COMMIT**, with no blocking final-review findings. Keep ACTIVE as `READY_FOR_OWNER_COMMIT`
and the Phase 15 audit gate pending. **Do not commit yet.** Full/static/coverage/architecture/OpenAPI/repository
closure and FR15-9 disposition belong to `$codex-backend-audit`. Only a later `BACKEND_AUDIT_READY` permits
owner implementation commit/push, handoff archival/reset and Phase 15 closeout. Phase 16/17 remain deferred.

Suggested commit message, usable only after that closure gate:

```text
fix(backend): remediate phase 15 audit findings
```

**Next step:** run `$codex-backend-audit` against this accepted, same Phase 15 handoff. Do not commit/push until
it returns `BACKEND_AUDIT_READY`.
