# Personal Private Vault Roadmap

## Structure

The roadmap has **17 implementation phases numbered 0–16**.

For Phase 3 onward there is also a mandatory **Pre-Handoff Preparation Gate (P-N)** before each implementation
handoff. This gate is not production implementation: ChatGPT prepares docs/tooling, Codex reviews them with
`$codex-pre-handoff-review`, and only `READY FOR HANDOFF` allows `$codex-create-handoff`.

Phase 0 is excluded from the three-phase milestone cadence. Extra milestone reviews occur after Phases
**3, 6, 9, 12, and 15**.

| Phase | Scope                                                                                         | Preparation gate                                    | Implementation status | Extra milestone      |
| ----- | --------------------------------------------------------------------------------------------- | --------------------------------------------------- | --------------------- | -------------------- |
| 0     | Spring Boot / Maven / Spring Modulith bootstrap                                               | Historical                                          | **COMPLETE — FROZEN** | Not counted          |
| 1     | Flyway Schema v1 + `reference` + `vault` foundations                                          | Historical                                          | **COMPLETE — FROZEN** | —                    |
| 2     | `authentication` + `settings` foundations                                                     | Historical                                          | **COMPLETE — FROZEN** | —                    |
| 3     | `people`: persons, roles, creator groups/membership                                           | P-3 complete                                        | **COMPLETE — FROZEN** | **MILESTONE_READY**  |
| 4     | `fiction` domain                                                                              | P-4 complete                                        | **COMPLETE — FROZEN** | —                    |
| 5     | `film` domain + film credits                                                                  | P-5 complete                                        | **COMPLETE — FROZEN** | —                    |
| 6     | `media` + `location` foundations                                                              | P-6 complete                                        | **COMPLETE — FROZEN** | **MILESTONE_READY**  |
| 7     | `account` external/social account history                                                     | **P-7 prepared; blocked by post-milestone sync**    | Not started           | —                    |
| 8     | `knowledge` + study/information/vocabulary/note                                               | P-8 planned                                         | Not started           | —                    |
| 9     | `collection` + music/shopping/software                                                        | P-9 planned                                         | Not started           | **After completion** |
| 10    | `feed` + `importdata` workflows                                                               | P-10 planned                                        | Not started           | —                    |
| 11    | `finance` + `journal` + `personal`                                                            | P-11 planned                                        | Not started           | —                    |
| 12    | PostgreSQL-first global `search` orchestration                                                | P-12 planned                                        | Not started           | **After completion** |
| 13    | Shared REST/API contract + module HTTP exposure + OpenAPI/error/pagination consistency        | P-13 planned                                        | Not started           | —                    |
| 14    | Non-RAG backend integration hardening, portability/export, object-storage/operational closure | P-14 planned                                        | Not started           | —                    |
| 15    | Next.js/TypeScript/shadcn frontend + E2E product workflows                                    | P-15 planned                                        | Not started           | **After completion** |
| 16    | RAG / semantic retrieval enhancement                                                          | P-16 planned                                        | Not started           | Final closeout       |

Production deployment provider/topology remains deferred and is not a numbered implementation phase until the
owner explicitly brings deployment into scope.


## Completed milestone maintenance

The Phase 1–3 milestone found two concurrency defects in frozen Phase 1/2 behavior. The owner-approved remediation is:

[`implementation/maintenance/milestone-1-3-concurrency/README.md`](implementation/maintenance/milestone-1-3-concurrency/README.md)

The maintenance passed final review and was owner committed/pushed as `3a9294d`. The milestone re-review is
`MILESTONE_READY`; Phase 4 may enter its separate pre-handoff preparation review.

## Completed pre-Phase-4 hygiene maintenance

The owner-approved
[`implementation/maintenance/pre-phase4-code-hygiene/README.md`](implementation/maintenance/pre-phase4-code-hygiene/README.md)
slice passed final review and was committed/pushed. Its handoff is archived; `ACTIVE.md` was reset to
`NO_ACTIVE_HANDOFF` before Phase 4 handoff creation.

Phase 4 preparation and implementation passed Codex review and were owner committed/pushed. Phase 4 is now
`COMPLETE — FROZEN`. Phase 5 Film preparation is the current gate.

## Phase 4 closeout

Phase 4 Fiction is complete/frozen after owner commit/push. Its final verification recorded 297 passing tests and
its completed handoff is archived under `implementation/phase-4/`.

Phase 5 Film preparation and implementation passed Codex review and were owner committed/pushed. Phase 5 is now
`COMPLETE — FROZEN`. Phase 6 Media + Location implementation has since been owner committed/pushed and frozen.

## Phase 5 closeout

Phase 5 Film is complete/frozen after owner commit/push. Its final verification recorded 372 passing tests and the
completed handoff is archived under `implementation/phase-5/`.

Phase 6 Media + Location preparation and implementation passed Codex review and were owner committed/pushed.
Phase 6 is now `COMPLETE — FROZEN`. The Phase 4–6 milestone is `MILESTONE_READY`; the milestone status-doc
commit/push and post-milestone synchronization/reset remain before Phase 7 pre-handoff review.

## Phase 6 closeout

Phase 6 Media + Location is complete/frozen after owner commit/push. Its final verification recorded 458 passing
tests and the completed handoff is archived under `implementation/phase-6/`.

The initial Phase 4–6 milestone review returned `CHANGES_REQUESTED` for private values in framework
constraint-error logs. Owner-approved maintenance closed the finding, and the re-review returned `MILESTONE_READY`.
Phase 7 Account preparation exists but remains blocked until the owner commits/pushes this milestone review/status
package and ChatGPT completes post-milestone synchronization/reset.

## Completed Phase 4–6 milestone maintenance

The Phase 4–6 milestone identified private business values in Hibernate constraint-error WARN logs. The owner-approved
remediation is:

[`implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md`](implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md)

The maintenance passed final re-review and was owner committed/pushed as `9449b9e`. Its
[completed handoff](implementation/handoffs/ACTIVE.md) awaits archival/reset during post-milestone synchronization.
Phase 7 remains blocked until the milestone review/status docs are owner committed/pushed and that reset is complete.

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
- `CHANGES_REQUESTED`: Codex found preparation, implementation, or milestone issues; remediation and re-review
  are required before the corresponding workflow gate can advance.
- `READY FOR HANDOFF`: Codex approved preparation; owner commits/pushes preparation, then creates handoff.
- `ACTIVE`: implementation handoff exists.
- `READY FOR OWNER COMMIT`: final implementation review passed.
- `MILESTONE_READY`: required three-phase cross-review passed; the next phase may proceed to pre-handoff review.
- `COMPLETE — FROZEN`: owner committed/pushed and closeout is complete.
