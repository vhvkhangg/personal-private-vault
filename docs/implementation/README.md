# Implementation Plans

Each phase owns its scope, archived handoff, test evidence, and formal reviews.

## Phases

- [`phase-0/`](phase-0/README.md) — **COMPLETE / FROZEN** — bootstrap
- [`phase-1/`](phase-1/README.md) — **COMPLETE / FROZEN** — Schema v1 + `reference` + `vault`
- [`phase-2/`](phase-2/README.md) — **COMPLETE / FROZEN** — `authentication` + `settings`
- [`phase-3/`](phase-3/README.md) — **COMPLETE / FROZEN; MILESTONE_READY** — `people`
- [`phase-4/`](phase-4/README.md) — **READY FOR HANDOFF / AWAITING OWNER PREPARATION COMMIT/PUSH** — `fiction`

Maintenance:

- [`maintenance/`](maintenance/README.md) — completed owner-approved frozen-phase corrections

See [`../roadmap.md`](../roadmap.md).

## Current state

There is no active implementation handoff.

The Phase 1–3 milestone is `MILESTONE_READY`. Both milestone concurrency maintenance and pre-Phase-4 hygiene
maintenance are complete/frozen and committed/pushed.

The owner commits/pushes Phase 4 preparation next, then runs `$codex-create-handoff`. Do **not** create a
Phase 4 implementation handoff before that commit/push.

## Operational guidance

- [Owner phase workflow](../workflow/owner-phase-workflow.md)
- [Roadmap](../roadmap.md)
- [Agent development workflow](../workflow/agent-development-workflow.md)
- [Active handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI permissions](antigravity-cli-permissions.md)
