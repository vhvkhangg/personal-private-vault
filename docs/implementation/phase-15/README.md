# Backend Phase 15 — Comprehensive Backend Audit & Remediation Gate

Status: **BACKEND_AUDIT_READY — OWNER COMMIT/PUSH AND CLOSEOUT PENDING** (2026-10-08)

[Codex preparation acceptance](reviews/2026-10-06-phase-15-pre-audit-codex-acceptance.md) closes P15-1 from the
[initial review](reviews/2026-10-06-phase-15-pre-audit-codex-review.md). No blocking preparation finding remains.
The owner quick checklist separates normal implementation from Phase 15 and requires closure audit before any
implementation commit. Preparation was committed as `6a89a998c512dda27c3e494a3525bf6118ee981e`.
The [initial audit](reviews/2026-10-06-phase-15-backend-audit.md) records 17 open findings and preserved 920-test
verification. The [owner decisions](owner-decisions.md) now approve all 17 within exact bounds;
[authorization re-review](reviews/2026-10-07-phase-15-authorized-remediation-codex-review.md) consolidates them into
the single [now-archived remediation handoff](handoff.md). Prior
[final acceptance](reviews/2026-10-07-phase-15-final-codex-acceptance.md) remains historical.
The [closure audit](reviews/2026-10-07-phase-15-closure-backend-audit.md) closes 13 original findings and FR15-9,
but retained four Medium remnants: BA15-2, BA15-9, BA15-14, BA15-15, with 1012-test full/coverage verification.
The [latest 2026-10-08 final acceptance](reviews/2026-10-08-phase-15-final-codex-acceptance.md) closes FR15-11/12
and preserves FR15-10's closure and all accepted repairs. No blocking final-review findings remain.
Independent clean verification passed 1021 tests, zero failures/errors/skips (03:53).
The [final closure audit](reviews/2026-10-08-phase-15-closure-backend-audit.md) returns **BACKEND_AUDIT_READY**:
all 17 BA15 findings CLOSED, FR15-9/10/11/12 closed, no unresolved actionable finding or owner-accepted debt.
Fresh full/coverage verification passed 1021 tests, zero failures/errors/skips (04:07), with static reports triaged
and architecture/database/storage/wire/live-OpenAPI/repository checks complete.
The [accepted handoff](handoff.md) is archived and [ACTIVE](../handoffs/ACTIVE.md) reset to NO_ACTIVE_HANDOFF.
The owner may commit/push with `fix(backend): remediate phase 15 audit findings`, then give ChatGPT the latest
package for Phase 15 closeout only. Phase 15 is not complete/frozen until publication and closeout.
Earlier final reviews/evidence remain historical; no second handoff or broader approval is created.
See [audit status](audit-status.md).
BA15-13 architecture expansion and BA15-14 additional total ingestion limits remain explicit owner-decision stops.

Owner-approved concept decisions (2026-10-06):

- Phase 15 is the dedicated audit phase; former Frontend Phase 15 becomes Phase 16 and former RAG Phase 16 becomes Phase 17.
- Findings may inspect every frozen Phase 0–14 baseline, but any remediation that changes Database Schema v1,
  module boundaries, ADR/architecture, API compatibility, or frozen behavior requires explicit owner approval before
  a remediation handoff may be created.
- Remediation is conservative: fix concrete defects, duplication, validation/logging/docs/test gaps, harmful
  overengineering, and maintainability/performance risks; do not refactor merely for stylistic preference.
- Audit-only static-analysis tooling may be used. Add tooling permanently to the build/repository only when the audit
  demonstrates durable CI value and the resulting handoff explicitly authorizes it.

Phase 14 implementation and closeout are owner committed/pushed. The Phase 15 preparation baseline is the Phase 14
closeout commit `0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606` (`docs(phase-14): close and freeze backend integration phase`).

This phase is **audit-first**. It does not begin with an implementation handoff. Its preparation reuses
`$codex-pre-handoff-review` in a Phase-15-specific mode: successful preparation returns `READY FOR AUDIT`, not
`READY FOR HANDOFF`, and never creates a handoff. After the accepted preparation is owner committed/pushed, Codex
runs `$codex-backend-audit` against the complete non-RAG backend. If actionable findings exist, Codex creates
one bounded remediation handoff for Antigravity unless a finding requires an owner decision on a frozen baseline.

## Goal

Perform one deliberate, repository-wide quality gate over the complete non-RAG backend produced by Phases 0–14
before frontend or RAG work begins.

The audit must answer two different questions:

1. **Correctness:** is the backend behavior, persistence model, HTTP surface, security boundary, documentation, and
   test evidence internally consistent and free of identified actionable defects?
2. **Engineering quality:** is the implementation proportionate and maintainable, or has duplication, accidental
   complexity, speculative abstraction, stale structure, or avoidable coupling accumulated across phases?

The audit is not a rewrite opportunity. A finding needs evidence and a concrete consequence.

## Scope

Audit all current backend production code, tests, database/migration artifacts, repository governance, and canonical
backend documentation produced or affected by Phases 0–14.

Primary paths include:

- `backend/src/main/java/`
- `backend/src/main/resources/`
- `backend/src/test/java/`
- `backend/pom.xml`
- `backend/compose.dev.yml` and committed backend environment examples/configuration
- `docs/architecture/`
- `docs/adr/`
- `docs/database/`
- `docs/repository/`
- `docs/implementation/phase-0/` through `phase-14/`
- completed maintenance/milestone evidence where it constrains current behavior
- root/backend/module `AGENTS.md`, agent rules/skills, and workflow docs when they describe current backend state

Historical review records are evidence and should not be rewritten merely because current code evolved later.
Current-state canonical docs must not contradict the repository.

## Explicit non-goals

Do not introduce or design:

- frontend implementation;
- RAG/vector/embedding implementation;
- production deployment/provider topology;
- multi-user support;
- 2FA/passkeys;
- unrelated new product features;
- speculative framework/platform layers;
- schema or architecture changes solely to make the design look more elegant;
- broad dependency upgrades unless a concrete defect/security/compatibility finding requires one.

## Audit principles

### Evidence over preference

Every actionable finding must include:

- severity;
- exact file/path and symbol, endpoint, table/query, or documented contract;
- observed behavior or reproducible evidence;
- why it is wrong/risky/costly;
- the smallest proportionate correction;
- whether remediation touches a frozen baseline or owner decision boundary;
- required regression evidence.

Do not report a style preference, hypothetical extensibility concern, or generic best practice as a defect without a
specific impact in this repository.

### Conservative remediation

Prefer deletion/simplification/canonicalization over new layers when the simpler design preserves behavior and module
boundaries. Do not force every repeated line into an abstraction and do not introduce a pattern merely because a
pattern name can be applied.

### Frozen-baseline escalation

Codex may identify findings anywhere in Phases 0–14. It must not authorize a remediation handoff that changes any of
these without explicit owner approval:

- Database Schema v1 / Flyway semantics / frozen DBML contract;
- Module Boundary v1 or approved Spring Modulith dependency directions;
- accepted ADR/architecture decision;
- public REST compatibility or externally visible behavior already frozen;
- a business/domain behavior explicitly frozen by a completed phase.

If any unresolved finding needs such a change, `$codex-backend-audit` returns `OWNER_DECISION_REQUIRED`, records the
tradeoff and smallest viable alternatives, and **does not create the remediation handoff**. After the owner decides,
rerun the audit with the decision recorded so one coherent handoff can be produced.

## Mandatory audit dimensions

### 1. Build, compiler, dependency, and static diagnostics

Check at minimum:

- clean Maven build/test verification using the repository's actual Java/Maven baseline;
- compiler warnings and suspicious suppressions;
- unused/declared-but-unused dependencies and accidental dependency drift;
- static bug analysis (SpotBugs or a compatible equivalent);
- code-rule/static analysis (PMD or a compatible equivalent);
- spelling/terminology scan for current code/docs (codespell/cspell or compatible evidence when available);
- copy/paste duplication analysis (CPD or a compatible equivalent);
- test coverage signal (for example JaCoCo) as a navigation aid, **not** as a numeric quality proxy;
- tracked generated/cache/build artifacts and stale repository files;
- owner-reported IDE warnings if such evidence is supplied.

Audit-only tool failure caused by Java/plugin/environment incompatibility is a tooling limitation, not automatically a
code finding. Record the limitation and use a reasonable substitute when available.

Do not permanently add a plugin, build profile, suppression file, or CI gate unless the audit demonstrates recurring
value and the remediation handoff explicitly includes it.

### 2. Business/domain correctness

Across all modules, verify:

- invariants are implemented once in the owning module;
- state transitions cannot bypass canonical rules;
- repeated operations have intentional idempotency semantics;
- null/empty/time/identity semantics are consistent;
- enum/state combinations are valid;
- cross-module calls use exposed contracts rather than internals;
- no historical implementation path competes with the current source of truth.

### 3. SOLID, cohesion/coupling, and programming principles

Review concrete applications of:

- SRP/cohesion;
- dependency direction and inversion where it actually reduces coupling;
- interface segregation;
- substitutability;
- encapsulation and information hiding;
- DRY without premature abstraction;
- KISS/YAGNI;
- command/query responsibility clarity;
- fail-fast/fail-closed behavior where appropriate.

A SOLID finding must point to a real responsibility/coupling/testability problem, not merely a class size preference.

### 4. Design-pattern fitness

Review Strategy/Factory/Adapter/Facade/State/Specification/Event/Builder/Repository and other patterns for actual need.

Flag both:

- **under-design:** concrete coupling or duplicated invariant that an existing/narrow pattern should solve;
- **pattern-driven design:** a pattern/hierarchy/interface/factory/event layer with no concrete variant, isolation,
  ownership, or complexity benefit.

### 5. Overengineering / accidental complexity

Explicitly inspect for:

- interfaces with only one implementation and no boundary/test/provider reason;
- `*Service`/`*ServiceImpl` pairs created by convention only;
- factories/strategies for one stable branch;
- generic base services/controllers/repositories that obscure domain behavior;
- pass-through application services or wrappers that add no policy/boundary;
- events where direct synchronous calls would be clearer and coupling is already accepted;
- unnecessary DTO/view/mapper layers or duplicate representations;
- helpers/utilities that hide two or three obvious lines rather than centralize an invariant;
- deep package nesting with no ownership benefit;
- speculative configuration/extension points;
- duplicated infrastructure abstractions serving the same capability;
- dependencies introduced for trivial work already handled safely by the JDK/framework.

An overengineering finding is actionable only when simplification can preserve required behavior and architecture while
concretely reducing indirection, maintenance surface, cognitive load, duplicated policy, or test burden.

### 6. Duplicate code and competing implementations

Search for:

- duplicated business rules;
- two methods/services implementing the same semantic operation;
- repeated validation/mapping/error translation that has diverged or is likely to diverge;
- duplicate SQL/query specifications;
- copied constants/regex/normalization rules;
- stale predecessor classes after a refactor/move.

Do not deduplicate intentionally different semantics merely because text is similar.

### 7. Validation and error semantics

Verify validation at the correct boundary for:

- request DTO fields and cross-field rules;
- domain invariants;
- numeric ranges/precision;
- string lengths/blank/null semantics;
- enum/state values;
- pagination/sort bounds;
- dates/time ranges;
- import/file/media type and size constraints;
- object-storage integrity inputs;
- IDs/references and not-found/conflict semantics.

Check alignment between Bean Validation, application/domain validation, database constraints, and documented HTTP
errors. Avoid both missing validation and redundant inconsistent validation.

### 8. Logging, exceptions, and observability

Review:

- correct log level;
- useful event/context without sensitive values;
- no password/PIN/JWT/refresh-token/secret/private-payload leakage;
- no duplicate stack trace emission across layers;
- no swallowed exception or misleading success log;
- key operational failures are observable;
- expected business rejections are not noisy ERROR spam;
- exception mapping preserves the canonical API contract;
- storage/export/transaction failure logs do not create privacy or recovery ambiguity.

Do not require log statements in every method.

### 9. REST/OpenAPI/Swagger correctness

For every exposed endpoint verify:

- HTTP method/path/status semantics;
- request/response DTO ownership and serialization;
- validation annotations;
- canonical success/error envelope rules, including approved binary-stream exceptions;
- pagination conventions;
- authentication/authorization exposure;
- OpenAPI operation summary/description accuracy;
- parameter/request-body semantics where non-obvious;
- response status/schema documentation, including meaningful error cases;
- schema/property descriptions where domain meaning is not self-evident;
- no stale endpoint or DTO documentation.

Generated OpenAPI should be inspected where practical; documentation completeness is semantic, not a requirement to
add redundant prose to every self-evident field.

### 10. Security and privacy

Audit:

- JWT/authentication/PIN/refresh-token behavior;
- authorization assumptions for permanently single-user operation;
- secret/config handling;
- CORS/CSRF/session/resource-server choices against the frozen architecture;
- unsafe deserialization/path/file handling;
- injection/query construction;
- sensitive exception/log/DTO exposure;
- security headers and actuator/operational exposure where currently in scope;
- object-storage credential and readiness behavior;
- dependency vulnerability evidence when a reliable audit tool is available.

### 11. Persistence, database, SQL, and schema fitness

Cross-check Flyway, JPA, DBML, repository queries, and current architecture for:

- entity/table/column type and nullability consistency;
- PK/FK/unique/check/index semantics;
- precision/scale/timezone correctness;
- ownership/cascade/orphan semantics;
- transaction isolation and lock scope;
- concurrency races and lost updates;
- N+1/lazy/eager behavior;
- unbounded or repeated queries;
- appropriate index use for concrete query paths;
- query shape and deterministic ordering;
- JSONB/native SQL correctness;
- portability snapshot safety;
- dead/redundant constraints or indexes;
- normalization/denormalization choices that materially conflict with current invariants or access patterns;
- schema elements that are materially suboptimal for actual current queries.

Do not request schema/index changes from intuition alone. A frozen-schema change requires owner approval and should be
supported by a concrete correctness/query-plan/workload reason. Use `EXPLAIN`/query evidence when a performance
finding depends on planner behavior.

### 12. Transactions, concurrency, and external I/O

Review:

- transaction boundaries and propagation;
- DB/external-storage sequencing;
- compensation/reconciliation semantics;
- lock duration and contention;
- retry/idempotency behavior;
- partial-failure recovery;
- stream/resource lifetime and cleanup;
- cancellation/client disconnect handling;
- race coverage for previously sensitive Phase 14 storage/export flows.

### 13. Performance and resource use

Look for concrete risks only:

- N+1;
- repeated DB/network/storage work in loops;
- accidental full-table/full-vault materialization;
- unbounded collection reads;
- excessive serialization/hash/computation;
- avoidable duplicate parsing/mapping;
- large in-memory buffers;
- blocking I/O held under unnecessarily broad transactions/locks;
- realistic algorithmic growth problems.

Do not create findings for speculative micro-optimization.

### 14. Tests and verification quality

Audit whether tests cover the risks that actually exist, including:

- positive + negative + boundary cases;
- state transitions;
- concurrency/locking when relevant;
- PostgreSQL-specific behavior via Testcontainers;
- S3/MinIO integration where relevant;
- Spring Modulith architecture boundaries;
- REST/validation/error/OpenAPI behavior;
- regressions for previously fixed findings;
- deterministic behavior and isolation;
- brittle timing/order/shared-state patterns;
- duplicate low-value tests versus missing high-value tests.

Coverage percentage is evidence for navigation only; it does not prove adequacy.

### 15. Repository/package/file hygiene

Review:

- capability-oriented package layout;
- meaningful `package-info.java` coverage;
- unnecessary package depth;
- stale `.gitkeep` files;
- duplicate/renamed/moved leftovers;
- unused classes/resources/scripts/config;
- accidentally tracked generated/cache/build/secret files;
- root/module instructions that no longer reflect reality;
- dependency/tooling files with no current use.

Never delete a file merely because it looks unused; prove no current reference/contract requires it.

### 16. Documentation, spelling, and diagrams

Check canonical current-state docs for:

- stale phase/status/commit/test-count text;
- contradictions between README/roadmap/ADR/architecture/package tree;
- broken links;
- spelling/grammar/terminology errors that change clarity/professional quality;
- Mermaid/PlantUML/Draw.io/Structurizr/DOT sources and exported diagrams matching current modules/dependencies;
- diagrams missing current implemented modules or showing nonexistent dependencies;
- stale TODO/FIXME/placeholder text;
- duplicate sources of truth.

Historical accepted review records should remain historical unless factually corrupted.

### 17. Configuration and operational consistency

Review:

- `application.yml` defaults and environment-variable names;
- `.env.example` versus committed runtime config;
- Docker/Compose development services;
- readiness/liveness semantics;
- configuration validation;
- safe defaults;
- no committed real secret;
- consistent naming across code/docs/examples.

## Static/tooling evidence contract

`$codex-backend-audit` should use the strongest compatible evidence available without mutating production code.
Expected categories:

```text
Maven clean verification / full test suite
compiler warnings
maven dependency analysis
SpotBugs or compatible bug detector
PMD or compatible static analyzer
spell/terminology checker when compatible
CPD or compatible duplication detector
JaCoCo or compatible coverage report as navigation evidence
Spring Modulith architecture verification
PostgreSQL/Testcontainers integration evidence
OpenAPI generation/inspection
repository/document/link/whitespace checks
```

The audit report must record exact commands/tool versions/results actually used. Do not claim a tool passed if it
was not run. If a tool cannot run compatibly, record why and what substitute/manual inspection covered the risk.

## Finding classification

Use these severities:

- **Critical** — exploitable security/privacy issue, data loss/corruption, or backend fundamentally unsafe/unusable.
- **High** — serious correctness/security/data-integrity/architecture defect likely to affect normal operation.
- **Medium** — concrete maintainability/performance/validation/API/test/documentation defect with material impact.
- **Low** — real bounded defect/hygiene issue worth fixing, but limited impact.
- **Observation** — non-actionable preference, accepted tradeoff, false positive, environment/tool limitation, or future
  consideration. Observations do not enter the remediation handoff.

Phase 15 is not considered clean while an actionable Critical/High/Medium/Low finding remains unresolved, unless the
owner explicitly accepts it as debt with rationale. Avoid manufacturing Low findings from style preference.

## Audit workflow

### A. Preparation review, then initial audit

First run:

```text
$codex-pre-handoff-review
```

For Phase 15 the success result is `READY FOR AUDIT`. The owner then commits/pushes the preparation slice. Only after
that commit/push run:

```text
$codex-backend-audit
```

Codex writes a dated report under `docs/implementation/phase-15/reviews/` and updates `audit-status.md`.

Possible outcomes:

#### `OWNER_DECISION_REQUIRED`

At least one finding needs a frozen-baseline/architecture/API/business-behavior decision. No handoff is created.
The owner decides each blocked item, the decision is recorded, then `$codex-backend-audit` is rerun.

#### `REMEDIATION_REQUIRED`

Actionable findings exist and none is blocked on owner approval. Codex creates exactly one bounded remediation handoff
in `docs/implementation/handoffs/ACTIVE.md`, status `READY_FOR_IMPLEMENTATION`, grounded in the audit report.

The handoff must:

- contain only proven actionable findings;
- preserve all non-approved frozen baselines;
- group related corrections coherently;
- specify regression evidence per finding;
- forbid unrelated refactor/feature work;
- identify any audit-only tooling that must remain audit-only.

Then run:

```text
/antigravity-implement-handoff
```

followed by:

```text
$codex-final-review
```

For Phase 15 remediation, `READY FOR OWNER COMMIT` from `$codex-final-review` is **not yet permission to commit**.
It means the handoff itself is accepted; rerun `$codex-backend-audit` for the repository-wide closure audit first.

#### `BACKEND_AUDIT_READY`

No unresolved actionable finding remains. Codex records the clean/accepted audit result and provides exactly one
Conventional Commit message for the Phase 15 audit/remediation slice. The owner may then commit/push.

### B. Remediation loop

If Antigravity remediation receives `CHANGES_REQUESTED`, repeat:

```text
/antigravity-implement-handoff
$codex-final-review
```

until the active handoff is accepted, then rerun:

```text
$codex-backend-audit
```

The closure audit must verify all previous findings against current code and may report newly exposed actionable
findings. Phase 15 uses **one evolving remediation handoff**: if the closure audit finds a new/remnant issue, append it
to the same Phase 15 handoff and set that handoff back to `CHANGES_REQUESTED`; do not create a second live handoff.
When the closure audit is clean, archive the completed Phase 15 handoff under `phase-15/handoff.md` and reset
`ACTIVE.md` to `NO_ACTIVE_HANDOFF` before returning `BACKEND_AUDIT_READY`.

## Acceptance criteria

Phase 15 may become `COMPLETE — FROZEN` only when:

1. Phase 14 remains frozen and the Phase 15 audit baseline is traceable;
2. the comprehensive audit report exists with actual tool/command evidence;
3. every actionable finding is closed or explicitly owner-accepted with rationale;
4. all frozen-baseline changes, if any, have explicit owner approval and synchronized ADR/schema/architecture docs;
5. full backend tests/build/architecture checks pass after remediation;
6. no live implementation handoff remains;
7. current-state docs, diagrams, roadmap, package tree, Swagger/OpenAPI contract, and database documentation match the
   accepted implementation;
8. no known duplicate canonical implementation, harmful overengineering finding, stale tracked file, or unresolved
   validation/logging/test gap remains;
9. Codex returns `BACKEND_AUDIT_READY`;
10. the owner commits/pushes the accepted Phase 15 slice and ChatGPT performs closeout/freeze.

## Phase numbering after approval

The roadmap is now:

- Phase 15 — Comprehensive Backend Audit & Remediation Gate;
- Phase 16 — Next.js/TypeScript/shadcn frontend + E2E workflows;
- Phase 17 — RAG / semantic retrieval enhancement.

Phase 16/17 preparation remains explicitly deferred until the owner separately authorizes it. Phase 15 replaces the
old post-Phase-15 three-phase milestone review; running both would duplicate the same quality gate with narrower
coverage.
