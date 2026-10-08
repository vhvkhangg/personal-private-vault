# Phase 15 — Codex Final Re-review 4

- Date: 2026-10-07
- Reviewer: Codex, review-only
- Scope: resubmitted `phase-15-backend-audit-remediation` [active handoff](../../handoffs/ACTIVE.md), entered as `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- Baseline HEAD: `6a89a998c512dda27c3e494a3525bf6118ee981e`
- Verdict: **CHANGES_REQUESTED** — two narrowed **Medium** findings, FR15-7 and FR15-8

This is implementation final review, **not** the comprehensive closure audit. The
[previous re-review](2026-10-07-phase-15-final-codex-rereview-3.md), earlier reviews, initial audit and owner
decisions remain historical. FR15-1–FR15-6 remain closed for their specific defects. No new production defect,
frozen-baseline change or architecture expansion is asserted here. Remediate only the remaining tests/docs below.

## Independent verification

From `backend/`, Codex ran:

```text
mvn -ntp -l ../phase-15-final-rereview-4-verify.log clean verify
```

**BUILD SUCCESS: 1012 tests, 0 failures, 0 errors, 0 skipped; 04:03 min**, finished
`2026-10-07T16:40:19+07:00`. Surefire XML testcase enumeration confirms 1012 cases, including **92 audit tests
across 14 suites**; preserved baseline: 920. Architecture tests pass. The independent log is an ignored local
diagnostic artifact, not a tracked report or proof of assertions that the tests do not execute.
`python .agents/hooks/test_repository_safety.py` passes **13 tests**. Final documentation checks pass:
`git diff --check` and **154 local link targets with 0 missing** across the review/governance/canonical-entry docs.

## Newly accepted progress — do not repeat it

- The related-drift HTTP matrix now covers Shopping/Software/Album 500/501 create/update boundaries;
  Fiction/Film genres and Location categories 150/151 create/update; independent manual SavedResource metadata
  bounds; Import original filename; recurring/subscription names/providers; Settings values above the removed
  competing caps and rejection of zero. Existing successful omitted/null Finance descriptions also count;
  duplicating their controls is not requested.
- Accepted update maxima now cover Account display/owner names, Image metadata, Address fields and direct
  Knowledge Study/Information/Vocabulary/Note. Vocabulary's remaining changed-field rejections are present.
- Generated-schema assertions now protect the affected families, Information/Vocabulary bounds, Shopping's
  replacement/clearing/WISHLIST semantics, Software's replacement/clearing/required type, Finance update semantics,
  recurring/subscription fields and Settings' removed maxima/timezone bound.
- Import parameter assertions now require actual default/bound properties and a limit lower bound excluding zero;
  missing bounds no longer succeed through `asInt()` defaults. Previously accepted complete 100/101-item traversal,
  execution/rollback, actual-writer contention, monetary variants/reloads and Future outcomes remain accepted.
- The revised note correctly removes invented registration/password-update/PIN-rate-limit/timer behavior,
  identifies bootstrap versus login validation, PBKDF2/legacy bcrypt, actual monetary/public-Knowledge mappings,
  null-meta and prior-state rollback semantics, and the startup class. Evidence now corrects the identified
  fields/counts, Import method, monetary value, FK proxy-versus-HTTP distinction and Feed scheduling symbols.
  Retain these improvements and the four canonical architecture-entry-point links.

## FR15-7 — Two accepted update-boundary controls remain absent

Severity: **Medium**, required regression acceptance, not a newly alleged production defect.
Source: `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/WebDtoValidationAuditIntegrationTest.java`.

In `financeRecurringAndSubscriptionBoundaries`, recurring POST accepts a 1000-character description (line 1739),
but the successful recurring PUT uses only blank text (lines 1777–1787), despite its preceding comment claiming
1000 acceptance. Transaction POST also accepts 1000 (line 1879), while its successful PUT is blank
(lines 1901–1909). Both PUTs reject 1001, and generated update schemas assert 1000; neither substitutes for the
remaining successful HTTP update at the true maximum requested in re-review 3.

Add only these two controls, using the existing valid fixtures:

- `PUT /api/v1/finance/recurring-rules/{id}` with a 1000-character description: 200 and the full value retained.
- `PUT /api/v1/finance/transactions/{id}` with a 1000-character description: 200 and the full value retained.

Use response or owner-read assertions to verify the complete value. Keep the existing blank/null, 1001 rejection,
name/provider and schema controls. All other regression/schema requests from re-review 3 are accepted as described
above; no new matrix, runtime feature, generic framework or production validation change is requested.

## FR15-8 — Canonical contracts and pre-fix evidence still contradict their sources

Severity: **Medium**. Correct docs to the implementation and captured evidence; **do not implement false promises**.

1. In the [canonical implementation note](../../../architecture/phase-15-implementation-notes.md):
   - Line 34 names `ApiError(code, message, details)`; the actual wire field is **`fieldErrors`** in root
     `ApiError.java`. Its “all REST endpoints” envelope claim also omits the approved successful Media/Portability
     binary response exceptions. Describe JSON/error envelopes and the existing successful binary exceptions.
   - Lines 68–69 name nonexistent `AccountService`, `guard.checkCanMutate(...)` and `ValidationException`
     (`INVALID_STUDY_ITEM`). Actual `ExternalAccountService.update` (lines 100–120) invokes
     **`ExternalAccountMutationGuard.validateMutation(...)`** synchronously after locking/refreshing.
     `StudyExternalAccountGuard` queries its own Study repository and throws **`ExternalAccountConflictException`**;
     Account's scoped advice maps that to **409 `EXTERNAL_ACCOUNT_CONFLICT`** (lines 38–40).
     Preserve the Account-owned SPI/concrete Study bean distinction and Knowledge→Account direction.
2. [Test evidence](../test-evidence.md), lines 27–44, now labels pre-fix Location concurrency (BA15-9) and
   Account/Study mutation (BA15-13) as “reproduced,” citing the original audit. That historical report explicitly
   says BA15-9 was a **source-derived interleaving, not runtime-executed** (lines 226–232), and BA15-13 a
   **source-derived sequence, not runtime-probed** (lines 281–288). Passing current regressions do not establish
   captured pre-fix failing runs. Classify each historical item according to its actual evidence: executed probe,
   SQL counterexample, source reasoning, or unavailable pre-fix reproduction. Link any genuinely retained later
   failing artifact if one exists; otherwise state that no runtime reproduction was captured. Do not rerun or
   rewrite historical/frozen baselines merely to manufacture retrospective evidence.
3. Evidence line 301's unqualified “eliminating deadlock” exceeds the scoped ordering/contention checks. State
   the particular Account/Study serialization exercised, not a general deadlock-free guarantee. No new lock
   design, dependency edge or alleged production deadlock is requested.

Update ACTIVE's submission claims and evidence accordingly. Aggregate 1012/92/14 counts are confirmed; the older
field/count/symbol/value corrections listed above need not be repeated.

## Other mandatory dimensions and limitations

- **Correctness, persistence, transactions, performance:** preserve accepted owner-local locks/fresh reads,
  authoritative scheduling, atomic rating upsert, bounded Import queries and exact numeric validation. No new
  schema/index/N+1 or concurrency defect is asserted. The two missing positive tests guard compatibility, not
  expanded domain rules.
- **Clean code/SOLID/reuse/pattern fit/extensibility:** the existing owner validators, DTO/advice structure and
  narrow synchronous SPI remain proportionate. Remaining test/docs work needs no new abstraction, hierarchy,
  validation/locking framework or unrelated refactor.
- **Security/privacy/HTTP/OpenAPI:** auth/PIN/JWT/refresh/binary/operational suites and expanded contract schemas
  pass. Correct misleading contract/security documentation without changing existing policies or exposing secrets.
- **Architecture/tree/scope:** no SQL, DBML, module matrix, repository-tree or architecture-diagram change was
  found; migration `.gitkeep` deletion remains authorized. No new module/named-interface boundary or dependency
  direction is requested. Frozen-baseline expansion would require `OWNER_DECISION_REQUIRED`; none is needed here.
- **Diagnostics:** Maven reports existing root API deprecation/unchecked-operation notes, the test-only Jackson 2
  converter removal warning at `MediaBinaryFramingWireIntegrationTest:82`, and Byte Buddy/JVM agent warnings;
  launcher Lombok/Unsafe warnings remain non-blocking. No IDE or comprehensive static/coverage/dependency/spelling
  closure pass is claimed. Those broader gates remain with the later `$codex-backend-audit`.
- **Evidence/review edits:** Codex changes only this formal report, the same handoff and current governance links;
  no production/test remediation, staging, commit or publishing. Initial audit SHA-256 remains
  `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`. Earlier reports/logs are preserved.
  Graphify provided navigation only (expanded tokens `[web, request, validation, schema, contract]`); conclusions
  were verified in source and historical evidence, not its pre-remediation index.

## Final gate

**CHANGES_REQUESTED**: FR15-7 and FR15-8 remain blocking Medium findings only for the bounded remnants above.
Complete the two successful Finance update controls and truthful canonical notes/evidence, run focused verification
and `mvn -ntp clean verify`, then resubmit this same handoff. No new handoff, production scope, commit message,
owner implementation commit or Phase 16/17 preparation is authorized.

**Next step:** Antigravity `/antigravity-implement-handoff`, then `$codex-final-review` again. After later acceptance,
**do not commit yet**: closure `$codex-backend-audit` must return `BACKEND_AUDIT_READY` first.
