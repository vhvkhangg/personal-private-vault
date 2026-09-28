---
trigger: glob
globs: "backend/src/main/java/com/vhvkhangg/personalprivatevault/reference/**/*.java, backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/**/*.java, backend/src/main/resources/db/migration/*.sql"
description: "Frozen reference/vault foundation constraints established in Backend Phase 1."
---

# Reference + Vault Foundation Rule

- Backend Phase 1 feature behavior is complete/frozen; modify it only with explicit owner-approved scope.
- `reference` and `vault` have no application-module dependencies.
- Public contracts are grouped into explicitly exposed semantic `@NamedInterface` packages.
- JPA entities/repositories remain internal.
- PostgreSQL named ENUM columns use Hibernate named-enum support.
- Do not mutate frozen DBML/Flyway V1 for package-only refactors.
- Vault capability rules remain explicit and fail closed.
- Use Testcontainers PostgreSQL; never H2 for persistence verification.
