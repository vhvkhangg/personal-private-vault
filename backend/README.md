# Backend

Java 25 / Spring Boot 4.1 modular monolith using Spring Modulith.

Backend Phase 0 is **complete and frozen**. Read `../docs/implementation/phase-0/README.md` for the bootstrap baseline, local setup, retained test evidence, and completed review workflow.

## Current baseline

- Java 25 LTS
- Maven 3.9.x (3.9.16 recommended; 3.9.15 is supported)
- Spring Boot 4.1.1
- Spring Modulith 2.1.1
- PostgreSQL 18.x
- Spring Data JPA / Hibernate
- Flyway
- Spring Security
- OpenAPI / Swagger UI
- Lombok (compile-time boilerplate only)
- Spring Boot Testcontainers service connections

## Local PostgreSQL

```bash
docker compose -f compose.dev.yml up -d
```

Docker Compose uses project name `personal-private-vault`; the PostgreSQL container is named `postgres`.

## Phase 1

Backend Phase 1 is **complete and frozen**. `src/main/resources/db/migration/V1__create_schema_v1.sql` provides the complete structural PostgreSQL baseline derived from the frozen DBML. The dependency-free `reference` and `vault` foundation modules are implemented and verified (57 tests, 0 failures against PostgreSQL 18.6 Testcontainers).

Read `../docs/implementation/phase-1/README.md` and `../docs/implementation/phase-1/test-evidence.md` for architecture details and verification evidence.


## Phase 2

Authentication + settings foundation is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-2/README.md` and `../docs/implementation/phase-2/test-evidence.md`.

## Phase 3

People Foundation is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-3/README.md` and `../docs/implementation/phase-3/test-evidence.md`.

## Phase 4

Fiction Foundation is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-4/README.md` and `../docs/implementation/phase-4/test-evidence.md`.

## Phase 5

Film Foundation is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-5/README.md` and `../docs/implementation/phase-5/test-evidence.md`.

## Phase 6

Media + Location Foundations are **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-6/README.md` and `../docs/implementation/phase-6/test-evidence.md`.

## Phase 7

External Account & Relationship History is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-7/README.md` and `../docs/implementation/phase-7/test-evidence.md`.

## Phase 8

Knowledge Foundation is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-8/README.md` and `../docs/implementation/phase-8/test-evidence.md`.

## Phase 9

Collection Foundation is **complete and frozen** after owner commit/push. See
`../docs/implementation/phase-9/README.md` and `../docs/implementation/phase-9/test-evidence.md`.

## Next gate

The Phase 7–9 milestone is `MILESTONE_READY` after accepted maintenance `785dd7d`; milestone docs are owner
committed/pushed and post-milestone synchronization/reset is complete.

## Phase 10

Feed + ImportData is **complete and frozen** after final acceptance and owner commit/push. Independent full
verification passed 685 tests. See
`../docs/implementation/phase-10/README.md`.


## Phase 11

Finance + Journal + Personal is **complete and frozen** after final acceptance and owner commit/push. Independent
full verification passed 787 tests. See `../docs/implementation/phase-11/README.md`.

## Phase 12

PostgreSQL-first Global Search is **complete and frozen** after final acceptance and owner commit/push. Independent
full verification passed 815 tests. See `../docs/implementation/phase-12/README.md`.

## Current milestone gate

The Phase 10–12 milestone is `MILESTONE_READY`; M10-12-1 is closed after accepted maintenance owner commit/push `a881540`.
See `../docs/implementation/phase-12/reviews/2026-10-05-phase-10-12-milestone-codex-acceptance.md`.
Fresh milestone clean verification passed 817 tests. The milestone review/status package is owner committed/pushed
and ChatGPT post-milestone synchronization/reset is complete.

## Phase 13

Shared REST/API Contract + Module HTTP Exposure is **complete and frozen** after final acceptance and owner
commit/push `ef92d94`. Independent final verification passed 867 tests, 0 failures/errors/skips.
See `../docs/implementation/phase-13/README.md`.

## Phase 14

Backend Integration Hardening + Portability/Object Storage Closure is **complete and frozen** after final acceptance,
owner implementation commit `3bb3f2e`, and closeout commit `0a8f3d1`. Independent clean verification retained 920
tests plus 49 focused tests. The completed handoff is archived at
`../docs/implementation/phase-14/handoff.md`.

See `../docs/implementation/phase-14/README.md`.

## Phase 15

Comprehensive Backend Audit & Remediation Gate preparation is **READY FOR AUDIT** after
[Codex preparation acceptance](../docs/implementation/phase-15/reviews/2026-10-06-phase-15-pre-audit-codex-acceptance.md)
(P15-1 closed; no blocking preparation findings). The initial audit covered the complete non-RAG
backend from Phases 0–14 for correctness, SOLID/design-pattern fit, overengineering, duplication, validation,
logging, OpenAPI, persistence/database/query quality, tests, docs/diagrams, static diagnostics, and repository hygiene.
The [initial audit](../docs/implementation/phase-15/reviews/2026-10-06-phase-15-backend-audit.md) returned
**OWNER_DECISION_REQUIRED**. All 17 findings now have [bounded owner approvals](../docs/implementation/phase-15/owner-decisions.md);
the [authorization re-review](../docs/implementation/phase-15/reviews/2026-10-07-phase-15-authorized-remediation-codex-review.md)
is **REMEDIATION_REQUIRED**. Prior
[final acceptance](../docs/implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-acceptance.md) remains historical.
The [closure audit](../docs/implementation/phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md) closes 13 findings
and FR15-9; it retained BA15-2, BA15-9, BA15-14 and BA15-15. The
[latest 2026-10-08 final acceptance](../docs/implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md)
closes FR15-11/12 and retains FR15-10's closure. No blocking final-review findings remain.
Independent clean verification passed 1021 tests, zero failures/errors/skips (03:53).
The [final closure audit](../docs/implementation/phase-15/reviews/2026-10-08-phase-15-closure-backend-audit.md)
returns **BACKEND_AUDIT_READY**: all 17 BA15 findings CLOSED, no actionable remnant/debt; FR15-9/10/11/12 closed.
Fresh full/coverage verification passed 1021 tests, zero failures/errors/skips (04:07), with static reports triaged.
The [accepted handoff](../docs/implementation/phase-15/handoff.md) is archived; ACTIVE is NO_ACTIVE_HANDOFF.
Preserve historical reviews/evidence. Owner commit/push is now permitted with
`fix(backend): remediate phase 15 audit findings`, followed by the latest package to ChatGPT for Phase 15 closeout only.
Phase 15 is not complete/frozen until publication and closeout.
The preserved 920-test evidence is the pre-remediation baseline; frontend/RAG remain deferred as Phases 16/17.
