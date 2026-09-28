# Implementation Plans

Each backend phase owns a dedicated folder. Completed-phase evidence stays with that phase; cross-cutting agent
workflow/governance documents remain at `docs/implementation/`.

## Phases

- [`phase-0/`](phase-0/README.md) — **COMPLETE / FROZEN** — Maven / Spring Boot / Spring Modulith bootstrap
- [`phase-1/`](phase-1/README.md) — **COMPLETE / FROZEN** — executable Schema v1 + `reference` / `vault` foundation
  - [owner-approved package-layout refinement](phase-1/package-layout-refactor.md) — **READY FOR OWNER COMMIT**
- [`phase-2/`](phase-2/README.md) — **PREPARED / BLOCKED ON FOUNDATION-REFACTOR REVIEW** — authentication + settings foundation

Future phases must follow the same `phase-N/` folder convention.

## Current implementation state

There is no active production handoff. The owner has approved Phase 2 planning, but Codex must create the Phase 2
implementation handoff before Antigravity writes production code.

## Operational guidance

- [Canonical agent development workflow](../agent-development-workflow.md)
- [Active handoff state](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Antigravity CLI project permissions](antigravity-cli-permissions.md)
