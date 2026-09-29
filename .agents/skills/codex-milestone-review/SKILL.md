---
name: codex-milestone-review
description: Perform the extra cross-phase architecture, quality, security, maintainability, and performance review after every three implementation phases excluding Phase 0.
---

# Codex Milestone Review

Run after Phase 3, 6, 9, 12, and 15 is committed/pushed/frozen, before the next implementation handoff.

## Scope/status document

When the closing phase contains `milestone-review.md`, treat it as the canonical milestone scope/status document.
Update it to `CHANGES_REQUESTED` or `MILESTONE_READY` together with the formal review record.

Do not create or modify the next implementation handoff.

## Review windows

- after Phase 3: Phases 1–3;
- after Phase 6: Phases 4–6;
- after Phase 9: Phases 7–9;
- after Phase 12: Phases 10–12;
- after Phase 15: Phases 13–15.

## Mandatory checks

- business/domain invariant consistency across phases;
- module dependency direction and accidental coupling;
- duplicate business rules / competing canonical implementations;
- SOLID, cohesion/coupling, and API responsibility drift;
- design-pattern misuse and overengineering;
- package/public-contract consistency;
- N+1 queries, repeated I/O, excessive lock scope, unbounded reads, and realistic algorithmic/memory hotspots;
- transaction/concurrency/data-integrity boundaries;
- authentication/authorization/secrets/logging;
- test reliability and missing regression coverage;
- documentation/roadmap/tooling drift;
- repository/package-tree drift, stale moved files/`.gitkeep`, and owner-reported warning debt;
- accumulated technical debt that materially raises the risk/cost of the next phases.

Do not request speculative micro-optimizations; performance findings need a concrete inefficient path or credible
data-access/complexity risk.

## Result

Write the milestone review in the closing phase's `reviews/` folder.

If blocking findings exist, return `CHANGES_REQUESTED` and identify whether they need docs/tooling remediation or an
owner-approved maintenance implementation slice. Never silently modify a frozen phase.

If no blocking findings remain, return `MILESTONE_READY`.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- `CHANGES_REQUESTED` → tell the owner that the next action is owner approval of a narrow maintenance scope, then
  give the latest package/findings to ChatGPT to prepare that maintenance;
- `MILESTONE_READY` → tell the owner to commit/push the milestone review/status docs, then give the latest package
  to ChatGPT for post-milestone synchronization/reset before running the next phase `$codex-pre-handoff-review`.

Never leave the owner to infer the next workflow action.
