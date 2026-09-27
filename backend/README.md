# Backend

Java 25 / Spring Boot 4.1 modular monolith using Spring Modulith.

Backend Phase 0 is **complete and frozen**. Read `../docs/implementation/backend-phase-0.md` for the bootstrap baseline, local setup, retained test evidence, and completed review workflow.

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

Backend Phase 1 is active. `src/main/resources/db/migration/V1__create_schema_v1.sql` provides the complete structural PostgreSQL baseline derived from the frozen DBML. Implementation and test verification of the dependency-free `reference` and `vault` foundation modules are complete (57 tests, 0 failures against PostgreSQL 18.6 Testcontainers) and awaiting owner commit.

Read `../docs/implementation/backend-phase-1.md` and `../docs/implementation/backend-phase-1-test-evidence.md` for architecture details and verification evidence.
