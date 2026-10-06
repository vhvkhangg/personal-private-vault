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
- [`phase-12/`](phase-12/README.md) — **COMPLETE / FROZEN; MILESTONE_READY** — `search`
- [`phase-13/`](phase-13/README.md) — **READY FOR OWNER COMMIT** — shared REST/API + HTTP exposure; all findings closed, independent 867-test verification passed

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

The active implementation handoff is [`handoffs/ACTIVE.md`](handoffs/ACTIVE.md), ID `phase-13-rest-api`,
status `READY_FOR_OWNER_COMMIT` after final acceptance; all seven findings are closed.

Backend Phases 10–12 are complete/frozen. The Phase 10–12 milestone is `MILESTONE_READY`; M10-12-1 maintenance is
complete/frozen as owner commit `a881540`, the milestone review/status package is owner committed/pushed, and
post-milestone synchronization/reset is complete.

Backend Phase 13 Shared REST/API Contract + Module HTTP Exposure preparation is `READY FOR HANDOFF` after
[Codex acceptance](phase-13/reviews/2026-10-05-phase-13-pre-handoff-codex-acceptance.md).
Accepted preparation is owner committed/pushed as `b3d91a5`; [final acceptance](phase-13/reviews/2026-10-06-phase-13-final-codex-acceptance.md)
closes all findings after independent 867-test clean verification. The earlier boundary-test failure remains
recorded in historical evidence. Next: owner commits/pushes the accepted slice, then gives the latest package
to ChatGPT for Phase 13 closeout/freeze and Phase 14 preparation. No agent commit/push or Phase 14 implementation
is authorized; Phase 13 is not yet owner-committed/frozen.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
