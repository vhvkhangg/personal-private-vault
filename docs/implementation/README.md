# Implementation Plans

Each phase owns its scope, archived handoff, test evidence, and formal reviews.

## Phases

- [`phase-0/`](phase-0/README.md) — **COMPLETE / FROZEN** — bootstrap
- [`phase-1/`](phase-1/README.md) — **COMPLETE / FROZEN** — Schema v1 + `reference` + `vault`
- [`phase-2/`](phase-2/README.md) — **COMPLETE / FROZEN** — `authentication` + `settings`
- [`phase-3/`](phase-3/README.md) — **COMPLETE / FROZEN; MILESTONE_READY** — `people`
- [`phase-4/`](phase-4/README.md) — **COMPLETE / FROZEN** — `fiction`
- [`phase-5/`](phase-5/README.md) — **COMPLETE / FROZEN** — `film`
- [`phase-6/`](phase-6/README.md) — **COMPLETE / FROZEN; MILESTONE CHANGES_REQUESTED** — `media` + `location`
- [`phase-7/`](phase-7/README.md) — **PREPARED / BLOCKED BY PHASE 4–6 MILESTONE** — `account`

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

The active [maintenance handoff](handoffs/ACTIVE.md) is `READY_FOR_OWNER_COMMIT` after final re-review.

Backend Phase 6 Media + Location is complete/frozen after owner commit/push. Its completed handoff is archived in
[`phase-6/handoff.md`](phase-6/handoff.md).

The [Phase 4–6 milestone review](phase-6/milestone-review.md) is `CHANGES_REQUESTED`. The owner-approved current
maintenance scope is
[`maintenance/milestone-4-6-privacy-safe-constraint-logging/`](maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md).
The owner commits/pushes that maintenance next, then reruns `$codex-milestone-review`. Phase 7 Account preparation remains blocked until the milestone
reaches `MILESTONE_READY` and the required post-milestone synchronization is complete.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
