---
name: codex-pre-handoff-review
description: Review next-phase docs, roadmap, agents, skills, rules, hooks, module instructions, and testing expectations before an implementation handoff may be created.
---

# Codex Pre-Handoff Review

Invoke with `$codex-pre-handoff-review`.

This is a review-only preparation gate. Do not write production code and do not create the implementation handoff.

## Historical Phase 15 is already closed

The Phase 15 audit-first special path is **historical**, not a current phase-review mode. Its original `READY FOR AUDIT` and `$codex-backend-audit` steps are preserved in `docs/implementation/phase-15/`, `docs/workflow/owner-phase-workflow.md` and the dedicated `$codex-backend-audit` skill. **Do not reopen Phase 15 or reissue `READY FOR AUDIT` during a new RAG phase.** The old branch appeared here to support the initial Phase 15 audit workflow; it is now removed from this active pre-handoff skill to prevent choosing the wrong gate.

## Preconditions

- Prior required implementation phase is committed/pushed and frozen. **RAG sequence exception:** P-17 needs frozen Phase 15 `BACKEND_AUDIT_READY` (Phase 16 deferred); P-18 through P-22 each require the immediately preceding **RAG phase** frozen. Phase 16 Frontend is reviewed only when the owner separately starts it after Phase 22. Verify proposed ADR-0019, the [RAG multi-phase plan](../../../docs/implementation/rag-phase-plan.md), and no simulated Phase 16 completion. The roadmap's active approved phase governs which phase gets the next review.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- Owner approved the next phase concept.
- Next phase has a `README.md` and `preparation-review.md`.

## Milestone prerequisite

When the immediately preceding implementation phase is 3, 6, 9, or 12, its milestone review must be
`MILESTONE_READY` before this pre-handoff review may run. Otherwise **STOP** and instruct the owner to run or
remediate `$codex-milestone-review`.

## Review dimensions

Review the next-phase preparation for:

1. scope clarity and explicit non-goals;
2. frozen architecture/database compatibility;
3. module ownership/dependency-matrix correctness;
4. proposed public API/package strategy without premature implementation design;
5. test/evidence contract;
6. security/data-integrity/concurrency risks;
7. docs/status/roadmap/link consistency;
8. necessity and non-duplication of new skills/rules/module instructions;
9. whether a new custom agent is really justified;
10. whether hook changes are genuinely required and narrow;
11. tooling/design overengineering;
12. repository/package-tree hygiene: package grouping, `package-info.java`, stale `.gitkeep`, duplicate/stray files,
    and consistency with repository/package architecture docs;
13. static diagnostics and reported IDE warnings: classify each as actionable, configuration-dependent/false positive,
    or intentionally accepted; do not claim IDE-clean status unless an IDE inspection was actually run;
14. readiness for a precise implementation handoff and its bounded current RAG phase scope;
15. for RAG Phases 17–22, validate the narrow source classification, actual last-check-to-COMMIT guard, offline/privacy gates, measured resource plan and no unapproved frozen boundary/schema changes;
16. **all applicable categories in [`codex-review-coverage`](../codex-review-coverage/SKILL.md)**, with evidence/status per category: include folder structure, cross-module shared API/exception placement, whole workflow completeness/efficiency and specific future runtime test gates. Report `N/A` or `NOT_VERIFIED` explicitly, never omit a category silently. A draft handoff must contain measured vs proposed acceptance distinctions.

## Mandatory proportionality and complete preparation audit — RAG Phases 18–22

Apply [owner-approved efficient delivery policy](../../../docs/workflow/rag-phase-delivery-guardrails.md). Inventory and review **all newly added or modified** docs, skills, rules, hooks and agents **plus every existing applicable item**: root/scoped `AGENTS.md`, `.agents/`, `.codex/`, relevant module code/tests, build/lockfiles, current/historical ADRs, phase plan and owner hardware constraints. Name the reviewed paths/categories; do not read irrelevant historic phases merely to maximize context. Explicitly determine whether `python-rag-coding`, `bounded-command-execution`, `backend-implementer` and an actual Codex takeover path are sufficient before proposing more agents.

Do not merely mention "overengineering": produce a short **KEEP / SIMPLIFY / DEFER / REJECT / OWNER DECISION REQUIRED** matrix for each major feature, API, dependency, evaluation and benchmark obligation. Trace every mandatory check to owner needs, approved phase scope or a reproducible defect. A hypothetical multi-user/cluster/enterprise threat is not a blocker in this single-user desktop unless a genuine local failure applies. Preserve all authorized privacy/consent/source and OS/process bounds. Never silently relax a frozen/security contract; show exact minimal amendment for owner approval instead.

Distinguish mandatory current-phase acceptance from end-state Phase 22 features, and from an independent reviewer finding unrelated to the phase. Identify proposed model/OCR/parser selection via small Windows+8 GB VRAM measurements, not a full speculative ML stack. Prefer latest **stable mutually compatible** release set (ADR-0013); don't demand every numerically newest major, runtime download or untested CUDA wheel. Use the pinned Phase 17 baseline as evidence, not permission for incidental upgrades.

Set one named final verification gate and bounded focused slices; reference the watchdog and two-round escalation policy. A 29-dimension rubric is 29 **review considerations**, not 29 required subsystems or 29 independent benchmark builds. If the proposed phase is too wide, mark `CHANGES_REQUESTED` with a **finite owner decision**, not an implementation plan for every possible future feature.

## CHANGES_REQUESTED

- Set the phase's `preparation-review.md` to `CHANGES_REQUESTED`.
- Record findings in that phase's `reviews/` folder.
- Do not create a handoff.

## READY FOR HANDOFF

- Set `preparation-review.md` to `READY FOR HANDOFF`.
- Record the review in that phase's `reviews/` folder.
- Return `READY FOR HANDOFF`.
- Provide exactly one Conventional Commit message for the preparation/docs/tooling slice.
- Do not create the implementation handoff automatically.

The owner commits/pushes preparation first, then invokes `$codex-create-handoff`.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- prerequisite/gate failure → name the exact prerequisite/action to resolve before rerunning this skill;
- `CHANGES_REQUESTED` → tell the owner to give the findings/latest package to ChatGPT for preparation remediation,
  then rerun `$codex-pre-handoff-review`;
- normal `READY FOR HANDOFF` → tell the owner to commit/push the preparation slice using the provided commit message,
  then run `$codex-create-handoff`.

Never leave the owner to infer the next workflow action.
