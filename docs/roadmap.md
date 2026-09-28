# Personal Private Vault Roadmap

## Structure

The roadmap has **17 implementation phases numbered 0–16**.

For Phase 3 onward there is also a mandatory **Pre-Handoff Preparation Gate (P-N)** before each implementation
handoff. This gate is not production implementation: ChatGPT prepares docs/tooling, Codex reviews them with
`$codex-pre-handoff-review`, and only `READY FOR HANDOFF` allows `$codex-create-handoff`.

Phase 0 is excluded from the three-phase milestone cadence. Extra milestone reviews occur after Phases
**3, 6, 9, 12, and 15**.

| Phase | Scope | Preparation gate | Implementation status | Extra milestone |
|---|---|---|---|---|
| 0 | Spring Boot / Maven / Spring Modulith bootstrap | Historical | **COMPLETE — FROZEN** | Not counted |
| 1 | Flyway Schema v1 + `reference` + `vault` foundations | Historical | **COMPLETE — FROZEN** | — |
| 2 | `authentication` + `settings` foundations | Historical | **COMPLETE — FROZEN** | — |
| 3 | `people`: persons, roles, creator groups/membership | P-3 complete | **COMPLETE — FROZEN** | **CHANGES_REQUESTED — maintenance approved** |
| 4 | `fiction` domain | **P-4 prepared; blocked by Phase 1–3 milestone** | Not started | — |
| 5 | `film` domain + film credits | P-5 planned | Not started | — |
| 6 | `media` + `location` foundations | P-6 planned | Not started | **After completion** |
| 7 | `account` external/social account history | P-7 planned | Not started | — |
| 8 | `knowledge` + study/information/vocabulary/note | P-8 planned | Not started | — |
| 9 | `collection` + music/shopping/software | P-9 planned | Not started | **After completion** |
| 10 | `feed` + `importdata` workflows | P-10 planned | Not started | — |
| 11 | `finance` + `journal` + `personal` | P-11 planned | Not started | — |
| 12 | PostgreSQL-first global `search` orchestration | P-12 planned | Not started | **After completion** |
| 13 | Shared REST/API contract + module HTTP exposure + OpenAPI/error/pagination consistency | P-13 planned | Not started | — |
| 14 | Non-RAG backend integration hardening, portability/export, object-storage/operational closure | P-14 planned | Not started | — |
| 15 | Next.js/TypeScript/shadcn frontend + E2E product workflows | P-15 planned | Not started | **After completion** |
| 16 | RAG / semantic retrieval enhancement | P-16 planned | Not started | Final closeout |

Production deployment provider/topology remains deferred and is not a numbered implementation phase until the
owner explicitly brings deployment into scope.


## Current milestone maintenance

The Phase 1–3 milestone found two concurrency defects in frozen Phase 1/2 behavior. The owner-approved remediation is:

[`implementation/maintenance/milestone-1-3-concurrency/README.md`](implementation/maintenance/milestone-1-3-concurrency/README.md)

Phase 4 remains blocked until this maintenance passes final review, is committed/pushed, and the milestone is rerun
to `MILESTONE_READY`.

## Why this order

- `people` precedes fiction, film, music, and study creator references.
- fiction/film then build on `people`, `vault`, and `reference`.
- `account` precedes `knowledge` because Study may reference YouTube-channel accounts.
- `knowledge` precedes `feed`/`importdata`.
- `search` stays late because it orchestrates public contracts from many feature modules.
- shared REST exposure is delayed until domain/module contracts stabilize.
- frontend begins after the non-RAG backend is sufficiently complete.
- RAG remains last, consistent with the accepted architecture.

## Status vocabulary

- `PLANNED`: roadmap only.
- `AWAITING CODEX PRE-HANDOFF REVIEW`: ChatGPT prepared docs/tooling; handoff is blocked.
- `CHANGES_REQUESTED`: Codex found preparation or milestone issues; the next handoff remains blocked pending
  remediation and re-review.
- `READY FOR HANDOFF`: Codex approved preparation; owner commits/pushes preparation, then creates handoff.
- `ACTIVE`: implementation handoff exists.
- `READY FOR OWNER COMMIT`: final implementation review passed.
- `MILESTONE_READY`: required three-phase cross-review passed; the next phase may proceed to pre-handoff review.
- `COMPLETE — FROZEN`: owner committed/pushed and closeout is complete.
