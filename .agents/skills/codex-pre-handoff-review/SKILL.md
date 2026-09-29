---
name: codex-pre-handoff-review
description: Review next-phase docs, roadmap, agents, skills, rules, hooks, module instructions, and testing expectations before an implementation handoff may be created.
---

# Codex Pre-Handoff Review

Invoke with `$codex-pre-handoff-review`.

This is a review-only preparation gate. Do not write production code and do not create the implementation handoff.

## Preconditions

- Previous implementation phase is committed/pushed and frozen.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- Owner approved the next phase concept.
- Next phase has a `README.md` and `preparation-review.md`.

## Milestone prerequisite

When the immediately preceding implementation phase is 3, 6, 9, 12, or 15, its milestone review must be
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
14. readiness for a precise implementation handoff.

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
