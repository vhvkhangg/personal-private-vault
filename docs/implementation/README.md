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
- [`phase-9/`](phase-9/README.md) — **COMPLETE / FROZEN; MILESTONE_READY** — `collection`
- [`phase-10/`](phase-10/README.md) — **COMPLETE / FROZEN** — `feed` + `importdata`
- [`phase-11/`](phase-11/README.md) — **COMPLETE / FROZEN** — `finance` + `journal` + `personal`
- [`phase-12/`](phase-12/README.md) — **COMPLETE / FROZEN; MILESTONE REVIEW REQUIRED** — `search`

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

The [active handoff](handoffs/ACTIVE.md) is `READY_FOR_OWNER_COMMIT` for approved M10-12-1 maintenance; FRM10-12-1 closed.

Backend Phases 10, 11, and 12 are complete/frozen after final acceptance and owner commit/push. Phase 12's completed
handoff is archived in [`phase-12/handoff.md`](phase-12/handoff.md), with retained evidence/reviews under `phase-12/`.

The [`Phase 10–12 milestone`](phase-12/milestone-review.md) is **CHANGES_REQUESTED** for M10-12-1 Search
SQL/snippet case-normalization consistency. See the
[formal review](phase-12/reviews/2026-10-04-phase-10-12-milestone-codex-review.md).
Independent clean verification passed 815 tests; a disposable PostgreSQL probe reproduced the uncovered defect.
Maintenance [final acceptance](maintenance/milestone-10-12-search-case-normalization/reviews/2026-10-05-final-codex-acceptance.md)
passed 817 tests independently. Owner commit/push is next, then `$codex-milestone-review`.
The milestone gate remains separate; no Phase 13 scope is opened.

Do **not** prepare Phase 13 and do not create an implementation handoff until the milestone reaches
`MILESTONE_READY`, the owner commits/pushes the milestone review/status package, and ChatGPT performs post-milestone
synchronization/reset.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
