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
Implementation phase final review = READY FOR OWNER COMMIT
        ↓
Owner commit + push
        ↓
ChatGPT closes/freezes phase + prepares next phase
        ↓
Is the completed phase 3 / 6 / 9 / 12 / 15?
        ├─ no ───────────────────────────────────────────────┐
        │                                                   ↓
        │                                  Codex $codex-pre-handoff-review
        │
        └─ yes
             ↓
        Codex $codex-milestone-review
             ↓
        CHANGES_REQUESTED?
             ├─ yes → owner-approved maintenance scope
             │        → Codex handoff
             │        → Antigravity implementation/tests
             │        → Codex final review
             │        → Owner commit/push
             │        → rerun $codex-milestone-review
             │
             └─ no / after remediation
                      ↓
                 MILESTONE_READY
                      ↓
        Owner commit/push milestone review/status docs
                      ↓
        ChatGPT post-milestone synchronization:
        archive/reset completed maintenance handoff,
        ensure ACTIVE.md = NO_ACTIVE_HANDOFF,
        synchronize roadmap/status,
        unblock prepared next phase
                      ↓
        Codex $codex-pre-handoff-review
                      ↓
        CHANGES_REQUESTED → ChatGPT remediation → re-review
        or READY FOR HANDOFF
                      ↓
        Owner commit/push preparation
                      ↓
        Codex $codex-create-handoff
                      ↓
        Antigravity /antigravity-implement-handoff
```

At milestone boundaries, `MILESTONE_READY` alone is not the final transition into the next pre-handoff review:
the owner first commits/pushes the milestone status changes, then returns the latest package to ChatGPT for the
post-milestone synchronization/reset step.

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

After a milestone returns `MILESTONE_READY` and the owner commits/pushes its review/status docs, ChatGPT performs
a second synchronization pass: close/reset any completed maintenance handoff, verify `ACTIVE.md = NO_ACTIVE_HANDOFF`,
synchronize roadmap/current-status docs, and unblock the already-prepared next phase for pre-handoff review.
