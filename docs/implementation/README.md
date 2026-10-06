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
- [`phase-13/`](phase-13/README.md) — **COMPLETE / FROZEN** — shared REST/API + HTTP exposure
- [`phase-14/`](phase-14/README.md) — **READY FOR HANDOFF** — accepted preparation; P14-1–P14-3 closed

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed and current owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

There is no active implementation handoff. [`handoffs/ACTIVE.md`](handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF`.

Backend Phases 10–12 are complete/frozen. The Phase 10–12 milestone is `MILESTONE_READY`; M10-12-1 maintenance is
complete/frozen as owner commit `a881540`, the milestone review/status package is owner committed/pushed, and
post-milestone synchronization/reset is complete.

Backend Phase 13 Shared REST/API Contract + Module HTTP Exposure passed
[final acceptance](phase-13/reviews/2026-10-06-phase-13-final-codex-acceptance.md), was owner committed/pushed as
`ef92d94`, and is complete/frozen with 867 tests retained.

Backend Phase 14 Backend Integration Hardening + Portability/Object Storage Closure preparation is
`READY FOR HANDOFF` after [2026-10-06 acceptance](phase-14/reviews/2026-10-06-phase-14-pre-handoff-codex-acceptance.md).
P14-1–P14-3 are closed; owner concept/ADR-0017 approval remains preparation-only.
Current gate: owner commits/pushes accepted preparation first, then runs:

```text
$codex-create-handoff
```

No Phase 14 implementation handoff is active. Handoff creation must wait for owner preparation commit/push;
implementation must wait for the active approved handoff.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
