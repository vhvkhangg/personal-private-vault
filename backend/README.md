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

External Account & Relationship History preparation exists but remains blocked until the `MILESTONE_READY` status docs
are owner committed/pushed and ChatGPT completes post-milestone synchronization/reset.
See `../docs/implementation/phase-7/README.md`.
