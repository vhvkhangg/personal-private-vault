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

Backend Integration Hardening + Portability/Object Storage Closure preparation is `READY FOR HANDOFF` after
2026-10-06 acceptance. P14-1–P14-3 are closed; accepted preparation is owner committed/pushed as `6f1fd00`.
Active handoff [phase-14-portability-storage](../docs/implementation/handoffs/ACTIVE.md) is `READY FOR OWNER COMMIT`
after [final acceptance](../docs/implementation/phase-14/reviews/2026-10-06-phase-14-final-codex-acceptance.md):
all nine findings closed; independent clean verification passed 920 tests plus 49 focused tests.
Owner commits/pushes the accepted implementation, then gives the latest package to ChatGPT for Phase 14 closeout
and Phase 15 preparation. Phase 14 is accepted but not yet committed/frozen; no milestone review is due at Phase 14.

See `../docs/implementation/phase-14/README.md`.
