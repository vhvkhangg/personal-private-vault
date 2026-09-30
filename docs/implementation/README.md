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
- [`phase-9/`](phase-9/README.md) — **ACTIVE / READY_FOR_OWNER_COMMIT** — `collection`

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

The active [Phase 9 Collection handoff](handoffs/ACTIVE.md) is `READY_FOR_OWNER_COMMIT` after Codex final acceptance re-review.

Backend Phase 8 Knowledge is complete/frozen after owner commit/push. Its completed handoff is archived in
[`phase-8/handoff.md`](phase-8/handoff.md), with final evidence/reviews retained under `phase-8/`.

Backend Phase 9 Collection preparation was owner committed/pushed. Its evidence finding is closed; owner
commit/push of the accepted implementation is next.

Phase 9 is a milestone phase. After its eventual implementation final review and owner commit/push, run the required
Phase 7–9 `$codex-milestone-review` before Phase 10 pre-handoff review.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
