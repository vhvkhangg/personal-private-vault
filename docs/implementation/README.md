# Implementation Plans

Each phase owns its scope, archived handoff, test evidence, and formal reviews.

## Phases

- [`phase-0/`](phase-0/README.md) — **COMPLETE / FROZEN** — bootstrap
- [`phase-1/`](phase-1/README.md) — **COMPLETE / FROZEN** — Schema v1 + `reference` + `vault`
- [`phase-2/`](phase-2/README.md) — **COMPLETE / FROZEN** — `authentication` + `settings`
- [`phase-3/`](phase-3/README.md) — **COMPLETE / FROZEN; MILESTONE_READY** — `people`
- [`phase-4/`](phase-4/README.md) — **COMPLETE / FROZEN** — `fiction`
- [`phase-5/`](phase-5/README.md) — **COMPLETE / FROZEN** — `film`
- [`phase-6/`](phase-6/README.md) — **COMPLETE / FROZEN; MILESTONE_READY** — `media` + `location`
- [`phase-7/`](phase-7/README.md) — **COMPLETE / FROZEN** — `account`
- [`phase-8/`](phase-8/README.md) — **COMPLETE / FROZEN** — `knowledge`
- [`phase-9/`](phase-9/README.md) — **COMPLETE / FROZEN; MILESTONE CHANGES_REQUESTED** — `collection`

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

The active [maintenance handoff](handoffs/ACTIVE.md) is `READY_FOR_OWNER_COMMIT` after final acceptance.

Backend Phase 9 Collection is complete/frozen after owner commit/push. Its completed handoff is archived in
[`phase-9/handoff.md`](phase-9/handoff.md), with final evidence/reviews retained under `phase-9/`.

The [Phase 7–9 milestone review](phase-9/milestone-review.md) returned `CHANGES_REQUESTED`. The owner-approved
maintenance scope is
[`maintenance/milestone-7-9-integrity-and-query-shape/`](maintenance/milestone-7-9-integrity-and-query-shape/README.md).
Owner commit/push is next. Rerun `$codex-milestone-review` after accepted
maintenance is owner committed/pushed.

Phase 10 Feed + ImportData pre-handoff review remains blocked until the milestone reaches `MILESTONE_READY`, the
owner commits/pushes milestone review/status changes, and ChatGPT completes post-milestone synchronization/reset and
Phase 10 preparation.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
