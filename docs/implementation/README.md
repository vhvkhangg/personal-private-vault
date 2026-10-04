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
- [`phase-11/`](phase-11/README.md) — **READY FOR OWNER COMMIT** — `finance` + `journal` + `personal`

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

The active Phase 11 implementation handoff is `READY_FOR_OWNER_COMMIT` in [`handoffs/ACTIVE.md`](handoffs/ACTIVE.md).

Backend Phase 10 Feed + ImportData is complete/frozen after final acceptance and owner commit/push. Its completed
handoff is archived in [`phase-10/handoff.md`](phase-10/handoff.md), with verification evidence/reviews retained under
`phase-10/`.

Backend Phase 11 Finance + Journal + Personal preparation was owner committed/pushed as `1fd0031`. Implementation
passed [Codex final acceptance](phase-11/reviews/2026-10-04-phase-11-final-codex-acceptance.md) with independent
787-test clean verification. Owner commit/push, then ChatGPT Phase 11 closeout and Phase 12 preparation are next.
Phase 11 is not complete/frozen; the next milestone is after Phase 12 completion.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
