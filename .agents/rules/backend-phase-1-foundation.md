---
trigger: glob
globs: "backend/src/main/java/com/vhvkhangg/personalprivatevault/reference/**/*.java, backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/**/*.java, backend/src/main/resources/db/migration/*.sql"
description: "Backend Phase 1 persistence/reference/vault implementation constraints."
---

# Backend Phase 1 Foundation Rule

- Active scope: PostgreSQL/Flyway Schema v1 + `reference` + `vault`.
- Antigravity may implement production code only from the active Codex handoff.
- Do not add controllers, authentication/JWT, settings implementation, other feature modules, frontend, RAG, or deployment.
- `reference` and `vault` have no application-module dependencies.
- Keep JPA entities/repositories internal.
- PostgreSQL named ENUM columns use Hibernate named-enum support.
- Do not mutate frozen DBML for convenience.
- Vault capability rules are explicit and fail closed.
- Use Testcontainers PostgreSQL; never H2.
- Lombok may reduce boilerplate but must not hide entity identity/association/state-transition semantics.
