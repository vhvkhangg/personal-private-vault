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

## Local PostgreSQL

```bash
docker compose -f compose.dev.yml up -d
```

The initial Flyway schema migration is intentionally deferred to the next database implementation phase.
