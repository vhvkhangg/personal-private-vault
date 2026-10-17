---
name: codex-create-handoff
description: Create a concise implementation handoff for Antigravity from an owner-approved current scope without writing production code.
---

# Codex Create Handoff

Invoke with `$codex-create-handoff`. Codex plans the slice; it does not implement production code here.

## Scope gate

A new handoff requires an **owner-approved current scope**, supplied by the owner's current request or a canonical
phase/slice document explicitly marked active/approved.

If no owner-approved current scope exists, **STOP and ask the owner to define/approve the next phase or slice**.

Never infer next work from frozen phase documents, historical TODOs, archived handoffs, review history, Graphify,
or repository structure. `COMPLETE — FROZEN` phases are historical inputs only.

## Phase 15 audit-remediation exception

Do not use this skill to invent a Phase 15 remediation scope from the Phase 15 README alone. Phase 15 remediation
handoffs are created by `$codex-backend-audit` only after that audit has produced concrete findings and all required
frozen-baseline owner decisions are resolved.

If Phase 15 has not yet produced an authorized audit finding set, stop and instruct the owner to run
`$codex-backend-audit`.

## Pre-handoff preparation gate

### Numbered implementation phase

For Phase 3 and later, the **current owner-approved numbered phase**'s `preparation-review.md` must be `READY FOR HANDOFF`. In the owner-approved RAG-first sequence, P-17 is the foundation only; P-18 through P-22 require completion of the preceding RAG phase and their own independent preparation. Never use P-17 acceptance as authority for later OCR/QA/recommendation work or silently resume deferred Phase 16.

If it is missing, `CHANGES_REQUESTED`, or still awaiting review, stop and instruct the owner to run
`$codex-pre-handoff-review`.

### Owner-approved maintenance slice

A maintenance handoff may be created without a numbered phase pre-handoff gate only when all of these are true:

- a canonical maintenance scope exists under `docs/implementation/maintenance/`;
- its status explicitly says `APPROVED FOR HANDOFF`;
- the owner explicitly approved that maintenance scope;
- the scope names the frozen phase(s), exact defects, permitted targets, non-goals, and test/evidence contract;
- no other live handoff exists.

A maintenance handoff must not become a route around a blocked next feature phase. Keep the next numbered phase
blocked until the maintenance trigger (for example, a milestone review) is resolved.

Do not merge preparation review and handoff creation into one step.

## Procedure

1. Read root `AGENTS.md`.
2. Establish owner-approved scope and classify it as a numbered implementation phase or explicit maintenance slice.
3. Read `docs/implementation/handoffs/README.md` and `ACTIVE.md`.
4. If another live implementation handoff exists, stop and resolve it first.
5. Inspect `git status --short` and `git diff --stat`.
6. Use Graphify for targeted navigation when available; verify facts in canonical files.
7. Read only files needed for the approved slice.
8. Write `docs/implementation/handoffs/ACTIVE.md` using `assets/handoff-template.md`.
9. Set `Status: READY_FOR_IMPLEMENTATION`.
10. Include targets, invariants, non-goals, acceptance criteria, test/evidence contract, frozen references,
    relevant engineering skills, and known risks.
11. Keep the handoff concise.

## Review-to-handoff traceability

Read the latest phase-specific Codex preparation report **and** shared [`codex-review-coverage`](../codex-review-coverage/SKILL.md). Translate applicable risk/quality dimensions, explicit N/A/NOT_VERIFIED limits, source ownership guard and privacy requirements into concrete handoff acceptance cases. Every High/Critical deferred-runtime risk needs exact evidence to be produced in implementation. Do not substitute a vague instruction to "follow best practices" for testable obligations. Ensure scope names package/folder ownership and permitted exception/shared HTTP conventions from ADR-0016 where relevant.

## Minimal handoff and remediation budget — future RAG

Read [proportional delivery policy](../../../docs/workflow/rag-phase-delivery-guardrails.md) and the pre-handoff KEEP/SIMPLIFY/DEFER decisions. Keep the handoff a bounded vertical slice of **that** numbered phase, no runtime features from later phases, no duplicate job engine or speculative adapter framework. List MUST acceptance, explicit optional/deferred items, changed paths, actual skill/agent choices, a few risk-based negative cases and the single final full-suite gate. A generic 29-dimension rubric does not make new features mandatory. Specify 20-minute default and 35-minute future-phase full-suite-only watchdog, and remediation counter initialized at zero. Codex takeover eligibility begins **only after two Antigravity remediation plus independent re-review cycles**, not automatically on the first finding.

## Engineering skills

Reference only relevant skills:
`java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
`design-pattern-selection`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`, plus any phase/domain-specific skill approved by the preparation gate.

Do not commit, push, tag, or modify production implementation.


- Use `authentication-security` when authentication/JWT/PIN/refresh-token work is in scope.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- successful handoff creation → tell the owner to run Antigravity `/antigravity-implement-handoff`;
- blocked by preparation/milestone/maintenance state → name the exact gate and command/action required first;
- conflicting live handoff → state the exact handoff-resolution action required before retrying.

Never leave the owner to infer the next workflow action.
