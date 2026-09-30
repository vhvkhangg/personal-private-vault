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
| 7     | `account` external/social account history                                                     | P-7 complete                                        | **COMPLETE — FROZEN** | —                    |
| 8     | `knowledge` + study/information/vocabulary/note                                               | P-8 complete                                        | **COMPLETE — FROZEN** | —                    |
| 9     | `collection` + music/shopping/software                                                        | P-9 complete                                        | **COMPLETE — FROZEN** | **MILESTONE_READY** |
| 10    | `feed` + `importdata` workflows                                                               | **BLOCKED BY PHASE 7–9 MILESTONE**                  | Not started           | —                    |
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
Phase 6 is `COMPLETE — FROZEN`, and the Phase 4–6 milestone is `MILESTONE_READY`. Phase 7 Account preparation and
implementation passed Codex review and were owner committed/pushed. Phase 7 is now `COMPLETE — FROZEN`; Phase 8
Knowledge preparation is the current gate.

## Phase 6 closeout

Phase 6 Media + Location is complete/frozen after owner commit/push. Its final verification recorded 458 passing
tests and the completed handoff is archived under `implementation/phase-6/`.

The initial Phase 4–6 milestone review returned `CHANGES_REQUESTED` for private values in framework
constraint-error logs. Owner-approved maintenance closed the finding, and the re-review returned `MILESTONE_READY`.
The owner committed/pushed the Phase 4–6 milestone review/status package and ChatGPT completed post-milestone
synchronization/reset. Phase 7 preparation and implementation findings were remediated, final acceptance re-review
returned `READY FOR OWNER COMMIT`, and the owner committed/pushed Phase 7. Phase 8 Knowledge preparation's three
findings were remediated; Codex re-review returned `READY FOR HANDOFF`, the owner committed/pushed preparation,
and Phase 8 implementation passed final re-review and was owner committed/pushed. Phase 8 is now `COMPLETE — FROZEN`; Phase 9 Collection preparation is the current gate.

## Phase 7 closeout

Phase 7 Account is complete/frozen after owner commit/push. Final verification recorded 489 passing tests across 38
suites, including 15 Account integration tests and 12 architecture tests. Its completed handoff is archived under
`implementation/phase-7/`.

Phase 8 Knowledge preparation findings were closed; Codex re-review returned `READY FOR HANDOFF`, the owner
committed/pushed preparation as `b3f3cd9`, implementation remediation passed final re-review, and the owner has now
committed/pushed Phase 8. Phase 9 Collection preparation's two contract findings are closed; Codex re-review returned `READY FOR HANDOFF`, the owner committed/pushed preparation as `74c1ec1`, and the implementation handoff is active.

## Phase 8 closeout

Phase 8 Knowledge is complete/frozen after owner commit/push. Final verification recorded 547 passing tests with
PostgreSQL Testcontainers, Flyway/Hibernate validation, and Spring Modulith architecture checks. Its completed
handoff is archived under `implementation/phase-8/`.

Phase 9 Collection preparation was owner committed/pushed. The inaccurate test-evidence finding was corrected and
Codex final acceptance re-review returned `READY_FOR_OWNER_COMMIT`. Phase 9 was owner committed/pushed as `b0aa742`
and frozen; the subsequent milestone result and maintenance closure are recorded below.

## Phase 9 closeout

Phase 9 Collection is complete/frozen after owner commit/push. Final verification recorded **583 passing tests**,
including 36 Collection architecture/validation/PostgreSQL integration tests. Its completed handoff is archived
under `implementation/phase-9/`.

The Phase 7–9 milestone review returned `CHANGES_REQUESTED` for Note frontmatter isolation, fresh locked Vocabulary
state, and per-entry snapshot merge queries. Maintenance passed final acceptance and was owner committed/pushed as
`785dd7d`; milestone re-review is `MILESTONE_READY`. Owner commits/pushes milestone docs and ChatGPT reset are next.
The completed remediation is
[`implementation/maintenance/milestone-7-9-integrity-and-query-shape/README.md`](implementation/maintenance/milestone-7-9-integrity-and-query-shape/README.md). Phase 10 Feed/ImportData remains blocked until
the owner commits/pushes the milestone review/status changes, and ChatGPT
performs post-milestone synchronization/reset and Phase 10 preparation.

## Completed Phase 4–6 milestone maintenance

The Phase 4–6 milestone identified private business values in Hibernate constraint-error WARN logs. The owner-approved
remediation is:

[`implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md`](implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md)

The maintenance passed final re-review and was owner committed/pushed as `9449b9e`. Its completed handoff is
archived under `implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/`. The Phase 4–6 milestone
is `MILESTONE_READY`. Phase 7 pre-handoff re-review accepted the remediated preparation.

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
