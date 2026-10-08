# Personal Private Vault Roadmap

## Structure

The roadmap has **18 implementation phases numbered 0–17**.

For normal implementation phases from Phase 3 onward there is a mandatory **Pre-Handoff Preparation Gate (P-N)**
before each implementation handoff. ChatGPT prepares docs/tooling, Codex reviews them with
`$codex-pre-handoff-review`, and only `READY FOR HANDOFF` allows `$codex-create-handoff`.

**Phase 15 is the deliberate exception:** it is audit-first, not feature-first. `$codex-pre-handoff-review` reviews
its audit design and returns `READY FOR AUDIT` on success; after the owner commits/pushes that preparation,
`$codex-backend-audit` determines whether any remediation handoff is needed.

Phase 0 is excluded from the three-phase milestone cadence. Historical extra milestone reviews occurred after Phases
**3, 6, 9, and 12**. The former post-Phase-15 milestone is superseded by the stronger repository-wide Phase 15
Comprehensive Backend Audit & Remediation Gate.

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
| 10    | `feed` + `importdata` workflows                                                               | P-10 complete                                       | **COMPLETE — FROZEN** | —                    |
| 11    | `finance` + `journal` + `personal`                                                            | P-11 complete                                       | **COMPLETE — FROZEN** | —                    |
| 12    | PostgreSQL-first global `search` orchestration                                                | P-12 complete                                       | **COMPLETE — FROZEN** | **MILESTONE_READY** |
| 13    | Shared REST/API contract + module HTTP exposure + OpenAPI/error/pagination consistency        | P-13 complete                                       | **COMPLETE — FROZEN** | —                    |
| 14    | Non-RAG backend integration hardening, portability/export, object-storage/operational closure | P-14 complete | **COMPLETE — FROZEN** | — |
| 15    | Comprehensive Backend Audit & Remediation Gate over Phases 0–14 | P-15 `READY FOR AUDIT`; closure `BACKEND_AUDIT_READY` | **COMPLETE — FROZEN** (owner commit `0b1ab11`) | Replaces former post-15 milestone |
| 16    | Next.js/TypeScript/shadcn frontend + E2E product workflows | P-16 planned | Not started | — |
| 17    | RAG / semantic retrieval enhancement | P-17 planned | Not started | Final closeout |

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
`785dd7d`; milestone re-review is `MILESTONE_READY`. The owner committed/pushed milestone docs and ChatGPT completed
post-milestone synchronization/reset.
The completed remediation is
[`implementation/maintenance/milestone-7-9-integrity-and-query-shape/README.md`](implementation/maintenance/milestone-7-9-integrity-and-query-shape/README.md). Phase 10 Feed/ImportData is complete/frozen after final acceptance and owner commit/push.

## Phase 10 preparation

The Phase 7–9 milestone is `MILESTONE_READY`, the owner committed/pushed its review/status package, and ChatGPT
completed post-milestone synchronization/reset. Phase 10 preparation review returned `CHANGES_REQUESTED` for
the ImportData same-job transition concurrency contract/tests. Remediation passed Codex re-review: `READY FOR HANDOFF`.
Owner committed/pushed preparation as `0e530f2`. Phase 10 passed final acceptance with independent 685-test
verification, the owner committed/pushed the implementation, and ChatGPT completed Phase 10 closeout.

Phase 10 implements adapter-ready Feed ingestion/SavedResource conversion and transactional ImportData workflows
without live feed HTTP providers, scheduler runtime, REST, frontend, or object-storage I/O.

## Phase 11 preparation

Phase 10 Feed + ImportData is complete/frozen after owner commit/push with 685 passing tests. Phase 11 Finance +
Journal + Personal preparation was accepted and owner committed/pushed as `1fd0031`. Implementation passed [Codex final acceptance](implementation/phase-11/reviews/2026-10-04-phase-11-final-codex-acceptance.md)
and independent 787-test clean verification; FR11-1 through FR11-9 are closed. The owner committed/pushed the
accepted implementation and ChatGPT completed Phase 11 closeout. Phase 12 preparation is now the current gate.

Phase 11 keeps Finance, Journal, and Personal as independent top-level modules over unchanged Schema v1. Recurring
finance scheduler/auto-post runtime remains deferred; Phase 11 establishes ledger/configuration/lifecycle foundations.

## Phase 12 preparation

Phase 11 Finance + Journal + Personal is complete/frozen after owner commit/push with 787 passing tests.

Phase 12 prepares PostgreSQL-first global Search as a no-table leaf/orchestration module. It adds only narrow
read-only search contracts to the frozen searchable modules, Vault-owned tag/trash qualification, deterministic
cross-module ranking, and an append-only pg_trgm/index migration during implementation.

Phase 12 preparation was accepted/owner committed as `44fdaa9`; P12-1/P12-2 closed. Implementation final review returned
[READY FOR OWNER COMMIT](implementation/phase-12/reviews/2026-10-04-phase-12-final-codex-acceptance.md); FR12-1–FR12-6 closed.
Independent clean verify passed 815 tests. The owner committed/pushed the accepted implementation and ChatGPT
closed/froze Phase 12.

The mandatory Phase 10–12 milestone review passed; milestone docs are owner committed/pushed and
post-milestone synchronization/reset is complete.

## Phase 10–12 milestone review

Phases 10–12 are complete/frozen. The completed milestone gate is recorded in
[`implementation/phase-12/milestone-review.md`](implementation/phase-12/milestone-review.md).

Codex [milestone acceptance](implementation/phase-12/reviews/2026-10-05-phase-10-12-milestone-codex-acceptance.md)
returned **MILESTONE_READY**; M10-12-1 closed after accepted maintenance owner commit/push `a881540`.
Fresh milestone clean verification passed 817 tests. The owner committed/pushed milestone review/status docs as
`4220ad4`, and ChatGPT completed post-milestone synchronization/reset and Phase 13 preparation.
The milestone review itself authorizes no broader frozen-module work or Phase 13 implementation.

## Phase 13 preparation

The Phase 10–12 milestone is `MILESTONE_READY`; accepted Search normalization maintenance `a881540` and milestone
review/status docs are owner committed/pushed. ChatGPT post-milestone synchronization/reset is complete.

Phase 13 finalizes the shared REST/JSON contract, `/api/v1` versioning, module-local HTTP adapters, authentication
HTTP exposure, OpenAPI/error consistency and truthful pagination metadata without adding new domain use cases or a
new Spring Modulith application module.

Phase 13 preparation received [Codex acceptance](implementation/phase-13/reviews/2026-10-05-phase-13-pre-handoff-codex-acceptance.md)
on 2026-10-05: **READY FOR HANDOFF**. The owner approved the concept and ADR-0016's narrow root HTTP exception
for preparation review. Accepted preparation is owner committed/pushed as `b3d91a5`; Codex created active handoff
`phase-13-rest-api`. [Final acceptance](implementation/phase-13/reviews/2026-10-06-phase-13-final-codex-acceptance.md)
is **READY FOR OWNER COMMIT**: all seven findings closed; independent 867-test clean verification passed.
The earlier intermittent baseline boundary-test failure remains preserved in historical evidence.
The owner committed/pushed the accepted implementation as `ef92d94`; ChatGPT completed Phase 13 closeout/freeze.

## Phase 14 — complete/frozen

Phase 14 preparation passed [pre-handoff acceptance](implementation/phase-14/reviews/2026-10-06-phase-14-pre-handoff-codex-acceptance.md)
and was owner committed/pushed as `6f1fd00`. The implementation then passed
[final acceptance](implementation/phase-14/reviews/2026-10-06-phase-14-final-codex-acceptance.md): FR14-1–FR14-9 are
closed; independent 920-test clean verification and 49 focused tests passed. The owner committed/pushed the accepted
implementation as `3bb3f2e` (`feat(backend): add portable exports and managed image storage`), and ChatGPT completed Phase 14 closeout/freeze. Its completed handoff is archived
at [phase-14/handoff.md](implementation/phase-14/handoff.md); there is no active Phase 14 handoff.
Its scope covers:

- a read-only `portability` leaf module and portable JSONL/Markdown/media-manifest export;
- S3-compatible Media binary upload/download behind a Media-owned abstraction;
- operational configuration/readiness hardening for object storage.

Production provider/deployment, automated backups, Feed scheduler/provider integrations, recurring Finance posting,
frontend, and RAG remain explicitly deferred.

Phase 15 closeout: **COMPLETE — FROZEN** after owner commit/push
`0b1ab1100084f8c7a4362a42874595b25b2b50ee` (`fix(backend): remediate phase 15 audit findings`) and the
2026-10-08 repository-wide **BACKEND_AUDIT_READY** verdict. All BA15-1–BA15-17 and FR15-9/10/11/12 are closed;
no unresolved actionable finding or owner-accepted debt remains. Fresh full/coverage verification passed 1021 tests,
zero failures/errors/skips (04:07). The single accepted handoff remains archived and `ACTIVE.md` is
`NO_ACTIVE_HANDOFF`. Backend Phases 0–15 are frozen; Phase 16/17 preparation is not authorized.
The owner has authorized and prepared Phase 15 as the
Comprehensive Backend Audit & Remediation Gate. The
[preparation acceptance](implementation/phase-15/reviews/2026-10-06-phase-15-pre-audit-codex-acceptance.md) closes
P15-1 with no blocking preparation findings: **READY FOR AUDIT**. Committed preparation was audited;
the [initial report](implementation/phase-15/reviews/2026-10-06-phase-15-backend-audit.md) returns
**OWNER_DECISION_REQUIRED**, 17 open findings. All 17 now have [bounded owner approvals](implementation/phase-15/owner-decisions.md);
the [authorization re-review](implementation/phase-15/reviews/2026-10-07-phase-15-authorized-remediation-codex-review.md)
returned **REMEDIATION_REQUIRED**. Prior
[final acceptance](implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-acceptance.md) remains historical.
The [closure audit](implementation/phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md) closes 13 findings
and FR15-9; it retained BA15-2, BA15-9, BA15-14 and BA15-15 with 1012-test full/coverage verification. The
[latest 2026-10-08 final acceptance](implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md)
closes FR15-11/12 and retains FR15-10's closure; no blocking final-review findings remain.
That handoff-specific acceptance passed independent clean verification of 1021 tests,
zero failures/errors/skips (03:53), without itself asserting a backend-audit verdict.
The [final closure audit](implementation/phase-15/reviews/2026-10-08-phase-15-closure-backend-audit.md)
now returns **BACKEND_AUDIT_READY**, closes all 17 findings, and passes fresh 1021/0/0/0 full/coverage verification
(04:07). The [accepted handoff](implementation/phase-15/handoff.md) is archived; no active implementation remains.
The owner published the accepted remediation as `0b1ab11`; this closeout freezes Phase 15 without creating a
new handoff. Phase 16/17 preparation remains separately owner-gated and has not started. Phase 14 itself is not a
milestone phase.

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
- Phase 15 performs a comprehensive quality/audit gate over the complete non-RAG backend before new product-surface work.
- frontend begins only after Phase 15 is complete/frozen and the owner separately authorizes Phase 16 preparation.
- RAG remains last as Phase 17, consistent with the accepted architecture.

## Status vocabulary

- `PLANNED`: roadmap only.
- `AWAITING CODEX PRE-HANDOFF REVIEW`: ChatGPT prepared docs/tooling; handoff is blocked.
- `CHANGES_REQUESTED`: Codex found preparation, implementation, or milestone issues; remediation and re-review
  are required before the corresponding workflow gate can advance.
- `READY FOR HANDOFF`: Codex approved preparation; owner commits/pushes preparation, then creates handoff.
- `ACTIVE`: implementation handoff exists.
- `READY FOR OWNER COMMIT`: final implementation review passed.
- `MILESTONE_READY`: historical three-phase cross-review passed for the Phase 1–12 cadence.
- `OWNER_DECISION_REQUIRED`: Phase 15 audit found a remediation that would change a frozen baseline and requires owner disposition before handoff.
- `REMEDIATION_REQUIRED`: Phase 15 audit found authorized actionable defects and created a bounded remediation handoff.
- `BACKEND_AUDIT_READY`: Phase 15 comprehensive audit has no unresolved actionable finding and is ready for owner commit/push.
- `COMPLETE — FROZEN`: owner committed/pushed and closeout is complete.
