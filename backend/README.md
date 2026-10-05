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

The Phase 10–12 milestone is `CHANGES_REQUESTED` for M10-12-1 Search SQL/snippet case-normalization consistency.
See `../docs/implementation/phase-12/reviews/2026-10-04-phase-10-12-milestone-codex-review.md`.
Independent milestone clean verification passed 815 tests. Maintenance final acceptance independently passed 817 tests
and is `READY FOR OWNER COMMIT`; FRM10-12-1 is closed. Owner commit/push is next, then `$codex-milestone-review`.
The milestone gate remains separate; no broader frozen-module work is authorized.

Phase 13 preparation remains blocked until the milestone reaches `MILESTONE_READY`, the owner commits/pushes the
milestone review/status package, and ChatGPT completes post-milestone synchronization/reset.
