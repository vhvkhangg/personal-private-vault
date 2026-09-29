# Agent Development Workflow

## Roles

| Role                               | Responsibility                                                              |
| ---------------------------------- | --------------------------------------------------------------------------- |
| Owner                              | Approves scope, runs necessary local/environment commands, commits/pushes   |
| ChatGPT                            | Phase closeout, docs consolidation, roadmap, next-phase preparation/tooling |
| Codex                              | Pre-handoff review, handoff creation, final code review, milestone review   |
| Antigravity `backend-implementer`  | Production implementation + tests from active handoff                       |
| Antigravity `architecture-auditor` | Optional read-only architecture/persistence audit                           |
| Graphify                           | Optional navigation cache; never source of truth                            |

## Phase 3+ lifecycle

```text
Owner commits/pushes finished phase
        ↓
ChatGPT closes/freezes it + prepares next phase
        ↓
Codex $codex-pre-handoff-review
        ↓
CHANGES_REQUESTED → ChatGPT remediation → re-review
or READY FOR HANDOFF
        ↓
Owner commits/pushes preparation
        ↓
Codex $codex-create-handoff
        ↓
Antigravity /antigravity-implement-handoff
        ↓
IMPLEMENTED_AWAITING_CODEX_REVIEW
        ↓
Codex $codex-final-review
        ↓
CHANGES_REQUESTED → Antigravity remediation → re-review
or READY FOR OWNER COMMIT
        ↓
Owner commit + push
```

After Phases 3/6/9/12/15, insert `$codex-milestone-review` before the next implementation handoff.

See `docs/workflow/owner-phase-workflow.md` and `docs/roadmap.md`.

## Engineering guidance

Use relevant skills through progressive disclosure. Core backend skills are Java/Spring standards, pragmatic SOLID,
reuse/consistency, pattern selection, modular-monolith architecture, JPA/PostgreSQL, backend testing, plus the
phase/domain skill named by the preparation gate/handoff.

## Context discipline

Read approved preparation/handoff first. Use Graphify before broad exploration when helpful, then verify graph facts
in canonical source/docs.

## Git boundary

Agents never commit, push, tag, or create/merge PRs.

## Phase closeout

After owner commit/push, ChatGPT marks the phase `COMPLETE — FROZEN`, archives handoff/reviews/evidence under that
phase, clears `ACTIVE.md`, synchronizes status, and prepares the next phase without implementing it.
