---
name: codex-pre-handoff-review
description: Review next-phase docs, roadmap, agents, skills, rules, hooks, module instructions, and testing expectations before an implementation handoff may be created.
---

# Codex Pre-Handoff Review

**FORWARD SCOPE GATE (2026-10-25):** Follow [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md). Phase 19 is NOT AUTHORIZED. Before declaring READY FOR HANDOFF, compare **all Owner-visible functions** against the simplest reusable mechanism and list KEEP/REUSE/SIMPLIFY/REMOVE/DEFER, implementation/verification effort, required approvals and explicit non-goals. Ask material decisions together before implementation; no automatic R7/OCR/GPU/judge, Markdown-note recommendation, or Ideaverse integration. Plan small separately committable slices; Antigravity self-review is mandatory; slice Codex review after each slice; phase final Codex review after all slices; milestone after RAG P19/P22; then two future audits. CRITICAL/HIGH/MEDIUM open findings block. New workflow has **three** Antigravity remediation/re-review rounds. Historical phase documents are not active handoff authority.


Invoke with `$codex-pre-handoff-review`.

This is a review-only preparation gate. Do not write production code and do not create the implementation handoff.

## Historical Phase 15 is already closed

The Phase 15 audit-first special path is **historical**, not a current phase-review mode. Its original `READY FOR AUDIT` and `$codex-backend-audit` steps are preserved in `docs/implementation/phase-15/`, `docs/workflow/owner-phase-workflow.md` and the dedicated `$codex-backend-audit` skill. **Do not reopen Phase 15 or reissue `READY FOR AUDIT` during a new RAG phase.** The old branch appeared here to support the initial Phase 15 audit workflow; it is now removed from this active pre-handoff skill to prevent choosing the wrong gate.

## Preconditions — select the approved review mode first

### Numbered implementation phase

- Prior required implementation phase is committed/pushed and frozen. **RAG sequence exception:** P-17 needs frozen Phase 15 `BACKEND_AUDIT_READY` (Phase 16 deferred); P-18 through P-22 each require the immediately preceding **RAG phase** frozen. Phase 16 Frontend is reviewed only when the owner separately starts it after Phase 22. Verify proposed ADR-0019, the [RAG multi-phase plan](../../../docs/implementation/rag-phase-plan.md), and no simulated Phase 16 completion. The roadmap's active approved phase governs which phase gets the next review.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- Owner approved the next phase concept.
- The approved next phase has a `README.md` and `preparation-review.md`.
- At a milestone boundary, including completed RAG Phase 19 or 22, the immediately preceding milestone review must be `MILESTONE_READY`. Otherwise stop and route to `$codex-milestone-review`.

### Explicit Owner-approved maintenance preparation

- Verify the Owner's actual approval and the canonical scope under `docs/implementation/maintenance/`; inspect its `README.md` or decision record, permitted files, exclusions, expected evidence and named documentation/implementation owner. Do not infer authorization from a historical finding or a future phase plan.
- Review only that maintenance scope. A **documentation/governance-only** maintenance review may run with `ACTIVE.md = NO_ACTIVE_HANDOFF`; it must not create an implementation handoff or make Phase 19 active. If the scope calls for production/tooling implementation, keep its eventual implementation handoff separately subject to `$codex-create-handoff` authorization and the active-handoff rules.
- Numbered-phase `README.md`/`preparation-review.md`, predecessor-phase completion and milestone prerequisites **do not apply** to a maintenance-only review. Reject scope conflicts or an unrelated live handoff rather than inventing a numbered phase.

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

Distinguish mandatory current-phase acceptance from end-state Phase 22 features, and from an independent reviewer finding unrelated to the phase. Review model/parser choices ONLY for Owner-approved capability; OCR/GPU work is not automatically in scope. If such a capability is separately approved, use narrowly measured target-host evidence rather than speculative stacks. Prefer latest **stable mutually compatible** release set (ADR-0013); don't demand every numerically newest major, runtime download or untested CUDA wheel. Use the pinned Phase 17 baseline as evidence, not permission for incidental upgrades.

Set a bounded focused verification gate for each slice and a separate phase final review; reference the currently implemented watchdog (do not claim pending adaptive behavior) and three-round escalation policy. A 29-dimension rubric is 29 **review considerations**, not 29 required subsystems or 29 independent benchmark builds. If the proposed phase is too wide, mark `CHANGES_REQUESTED` with a **finite owner decision**, not an implementation plan for every possible future feature.

## CHANGES_REQUESTED

- **Numbered phase:** set its `preparation-review.md` to `CHANGES_REQUESTED`; write findings under that phase's `reviews/`.
- **Maintenance:** update the canonical maintenance scope's status to `CHANGES_REQUESTED`; write the dated review under that scope's `reviews/`. Do not touch a numbered phase's preparation status or `ACTIVE.md` for docs-only maintenance.
- Do not create or activate a handoff. Route document-only corrections to ChatGPT (the approved documentation owner); route production/tooling corrections only through a separately approved maintenance implementation handoff.

## READY FOR HANDOFF / MAINTENANCE PREPARATION READY

- **Numbered phase:** set its `preparation-review.md` to `READY FOR HANDOFF` and write the review under that phase's `reviews/`. After the Owner commits/pushes the approved preparation, `$codex-create-handoff` may prepare the next approved implementation slice.
- **Maintenance:** record the review under the canonical maintenance scope's `reviews/` and set that scope's status to `READY_FOR_HANDOFF` only if its Owner-approved next step is an implementation handoff. For documentation-only maintenance, record `READY_FOR_OWNER_COMMIT` when independently ready and direct the Owner to ordinary commit/push; **do not require** `$codex-create-handoff` or make `ACTIVE.md` live.
- Provide one Conventional Commit message when ready; do not create the implementation handoff or publish automatically.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement consistent with the selected review mode.

- Prerequisite failure → state the exact missing approval or canonical scope, without inventing Phase 19.
- `CHANGES_REQUESTED` → direct documentation-maintenance findings to ChatGPT for a bounded docs correction and independent maintenance re-review; implementation findings follow the separately authorized handoff's owner. Numbered-phase preparation findings follow their existing phase correction route.
- Numbered-phase `READY FOR HANDOFF` → Owner commits/pushes preparation, then runs `$codex-create-handoff` for the approved slice.
- Maintenance ready → Owner commits/pushes reviewed docs-only changes, or invokes the explicitly approved maintenance implementation handoff if applicable. Do not impose a new phase gate.

Never leave the owner to infer the next workflow action.

## Documentation synchronization checkpoint — Maintenance B3 amendment

Follow the shared [Documentation synchronization contract](../../../docs/workflow/documentation-synchronization.md) **at this skill's handoff/review boundary**, within Owner-approved scope and without a repository-wide cleanup. Identify affected current docs, authority/conflicts and likely documentation ownership before READY_FOR_HANDOFF. Place existing unrelated stale notices in a deferred ledger, not a new project-wide fix; route approved docs-only corrections to ChatGPT. Record the concise **Docs Sync Check** in the existing handback/review artifact (or identify why no docs are affected). Keep the **Next step** routed to the exact remaining documentation action when necessary.

## Forward next action and interruption recovery — Maintenance B3

At the end of **every completed, blocked or interrupted invocation**, output one concrete line with all fields:

`Next step: <application/person> | <exact skill command or Owner action> | <scope + canonical artifact> | <gate/prerequisite> | <remediation or takeover counter, if applicable>`

**Route for this skill:** READY_FOR_HANDOFF for an approved implementation scope -> Owner publishes reviewed preparation, then Codex `$codex-create-handoff`. READY_FOR_OWNER_COMMIT for docs-only maintenance -> Owner commit/push without handoff. CHANGES_REQUESTED docs -> ChatGPT bounded repair + independent re-review. Missing Owner decision -> STOP and ask Owner; do not open ACTIVE.

**Interrupted session:** the Owner may paste the English [resume prompt](../../../docs/workflow/resume-prompts/codex-pre-handoff-review.md). Resume only from inspected files/evidence, with the current approved scope and existing counter; capacity/tool errors do not authorize a new round or reset evidence. Do not overwrite logs or bypass native safety hooks. A standalone skill command (without a special prompt) remains sufficient for an uninterrupted run.
