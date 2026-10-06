---
name: codex-backend-audit
description: Perform the Phase 15 comprehensive audit of the complete non-RAG backend (Phases 0–14), including correctness, architecture, SOLID/pattern fit, overengineering, duplication, validation, logging, OpenAPI, persistence, tests, docs, diagrams, static diagnostics, and repository hygiene; create a bounded remediation handoff only when findings are authorized.
---

# Codex Backend Audit

Invoke with `$codex-backend-audit`.

This is the dedicated Phase 15 repository-wide backend quality gate. Codex may inspect broadly and run audit tools,
but must not implement production fixes.

## Canonical scope

Read first:

- `docs/implementation/phase-15/README.md`
- `docs/implementation/phase-15/audit-status.md`
- root `AGENTS.md`
- `docs/roadmap.md`
- `docs/implementation/handoffs/ACTIVE.md`

The Phase 15 README is the canonical audit contract. Do not shrink its mandatory dimensions.

## Preconditions

Before the initial audit:

- Phase 14 is `COMPLETE — FROZEN`;
- implementation commit `3bb3f2e78a38eb66bec955219ced634d3cfddd9d` is present in history;
- closeout commit `0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606` is present in history;
- Phase 15 `preparation-review.md` is `READY FOR AUDIT`;
- Phase 15 preparation is owner committed/pushed;
- `ACTIVE.md` is `NO_ACTIVE_HANDOFF` unless this invocation is the closure re-audit immediately after a Phase 15
  remediation handoff reached final acceptance.

If preparation review is not `READY FOR AUDIT` or preparation is not committed/pushed, stop. Do not silently audit a moving uncommitted preparation baseline.

## Context strategy

This audit is intentionally broader than normal phase work.

- Use Graphify for navigation if available, but verify every material conclusion in source/canonical docs.
- Use targeted subagent/read-only architecture-auditor help when useful.
- It is acceptable to scan all production Java, tests, migrations, current-state docs, agent/governance files, and
  repository metadata needed by the audit.
- Do not treat old review text as current-state authority when a newer canonical source exists.

## Mandatory audit coverage

Complete every dimension in the Phase 15 README, including explicitly:

1. clean build/full tests/compiler/static diagnostics;
2. domain/business correctness;
3. SOLID/cohesion/coupling/programming principles;
4. design-pattern fitness;
5. overengineering/accidental complexity/YAGNI violations;
6. duplicate/competing implementations;
7. validation/error semantics;
8. logging/exceptions/observability/privacy;
9. REST/OpenAPI/Swagger correctness and description completeness;
10. authentication/security/privacy;
11. JPA/Flyway/PostgreSQL/DBML/query/index/schema consistency;
12. transaction/concurrency/external-I/O failure boundaries;
13. concrete performance/resource risks;
14. test sufficiency/reliability;
15. package/repository/file hygiene, including stale `.gitkeep`/generated/duplicate files;
16. docs/spelling/current-state drift and architecture diagrams;
17. configuration/operational consistency.

Do not mark the audit complete from a test run alone.

## Audit tooling

Use the strongest compatible evidence reasonably available. Expected categories include:

- Maven clean verify/full test suite;
- compiler warnings;
- Maven dependency analysis;
- SpotBugs or compatible equivalent;
- PMD or compatible equivalent;
- spelling/terminology checker for current code/docs when compatible;
- CPD or compatible duplication analysis;
- JaCoCo or compatible coverage navigation;
- Spring Modulith verification;
- PostgreSQL/Testcontainers and MinIO/S3 integration evidence;
- generated OpenAPI inspection;
- docs/link/whitespace/repository hygiene checks.

Record exact command and tool/plugin version for every tool actually used. Never claim a tool passed when it was not
run. If a tool is incompatible with Java 25/Spring Boot/current bytecode or unavailable in the environment, classify
that as a tooling limitation and use a compatible substitute/manual review rather than disabling correct code or
adding blanket suppressions.

Do not permanently modify `pom.xml`, CI, suppression files, or production configuration merely to run the audit.
Permanent tooling changes are remediation work and require an explicit finding showing durable value.

## Finding standard

Use `assets/backend-audit-report-template.md`.

Every actionable finding must contain:

- stable ID `BA15-N`;
- Critical/High/Medium/Low severity;
- category;
- exact evidence/path/symbol/contract;
- consequence;
- smallest proportionate correction;
- regression/verification requirement;
- `Frozen baseline impact: none | owner approval required`.

Use `Observation` for non-actionable style preference, accepted tradeoff, tool/environment limitation, or future idea.
Observations never enter the remediation handoff.

Do not inflate severity. Do not manufacture Low findings from formatting or taste.

## Overengineering calibration

A finding needs concrete unnecessary complexity. Examples include a redundant single-implementation interface,
pattern/hierarchy with no current variant/boundary benefit, pass-through layer with no policy, speculative extension
point, generic base abstraction hiding domain behavior, or duplicate infrastructure wrapper.

Do **not** flag an interface merely because it has one implementation when it is a real module/provider/test boundary.
Do not collapse layers that encode transaction, security, ownership, persistence, or external-provider boundaries.

## Frozen-baseline gate

If any actionable finding requires changing frozen Schema v1, module boundary, accepted ADR/architecture, public API
compatibility, or frozen business behavior:

1. record the finding and realistic options;
2. set `audit-status.md` to `OWNER_DECISION_REQUIRED`;
3. write the dated report;
4. do **not** create/modify an implementation handoff;
5. return `OWNER_DECISION_REQUIRED`.

After the owner records a decision, rerun this skill so all authorized findings can be consolidated into one handoff.

## When remediation is required

If actionable findings exist and none is blocked on owner approval:

1. write/update the dated audit report;
2. set `audit-status.md` to `REMEDIATION_REQUIRED`;
3. require `ACTIVE.md` to be `NO_ACTIVE_HANDOFF` before creating the handoff;
4. create exactly one Phase 15 remediation handoff in `docs/implementation/handoffs/ACTIVE.md` using the normal handoff
   template and status `READY_FOR_IMPLEMENTATION`;
5. include only actionable findings from the report;
6. preserve all frozen baselines not explicitly owner-approved;
7. specify per-finding acceptance tests/evidence and explicit non-goals;
8. reference relevant existing engineering skills rather than inventing new agents/patterns.

Return `REMEDIATION_REQUIRED` and direct the owner to Antigravity `/antigravity-implement-handoff`.

Codex still writes no production code.

## Closure re-audit

After Antigravity remediation and `$codex-final-review` accepts the Phase 15 handoff, rerun this skill **before owner
commit/push**.

For closure:

- verify every prior `BA15-N` finding against current code/tests/docs;
- rerun the necessary full/static evidence, not only focused tests;
- inspect for regressions/newly exposed findings;
- keep a single evolving Phase 15 remediation handoff; if new/remnant findings exist, append them to the same
  handoff and set it back to `CHANGES_REQUESTED` rather than creating a second handoff;
- do not declare ready merely because the handoff-specific final review passed.

If new/remnant findings exist, continue the remediation loop through the same handoff.

## BACKEND_AUDIT_READY

Return `BACKEND_AUDIT_READY` only when:

- every mandatory audit dimension was actually covered;
- no unresolved actionable finding remains unless explicitly owner-accepted with recorded rationale;
- all required build/test/static/architecture evidence is green or any unavoidable tool limitation is transparently
  recorded and compensated by other evidence;
- `ACTIVE.md` is `NO_ACTIVE_HANDOFF`;
- current-state docs/diagrams/schema/API/package-tree status match reality.

Then:

1. if a Phase 15 remediation handoff exists, archive its final accepted form as `docs/implementation/phase-15/handoff.md`;
2. reset `docs/implementation/handoffs/ACTIVE.md` to `NO_ACTIVE_HANDOFF`;
3. set `audit-status.md` to `BACKEND_AUDIT_READY`;
4. write the final dated audit/re-audit report;
5. provide exactly one Conventional Commit message for the complete Phase 15 audit/remediation slice;
6. do not commit/push.

## Required final response — next step

Every invocation ends with exactly one clear **Next step:**

- prerequisite failure → state the exact prerequisite;
- `OWNER_DECISION_REQUIRED` → tell the owner to decide the listed frozen-baseline items, record approval/rejection,
  then rerun `$codex-backend-audit`;
- `REMEDIATION_REQUIRED` → tell the owner to run Antigravity `/antigravity-implement-handoff`;
- `BACKEND_AUDIT_READY` → tell the owner to commit/push using the provided commit message, then give ChatGPT the
  latest package for Phase 15 closeout only; Phase 16/17 preparation remains owner-gated.
