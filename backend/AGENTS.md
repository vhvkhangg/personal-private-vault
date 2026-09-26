# Backend Scope Instructions

These instructions apply to `backend/` and refine the repository-level `AGENTS.md`.

## Ownership

The repository owner writes production implementation code by default.
Only write production code when the owner explicitly asks for it.

## Package structure

Keep code inside the owning business module.

Expected top-level module packages are documented in:

- `docs/architecture/module-boundaries.md`
- `docs/architecture/module-dependency-matrix.md`
- `docs/repository/repository-package-tree.md`

Do not create global root packages such as:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`

Each module may organize its internals using application/domain/infrastructure/web concerns as needed, but those are internal to the business capability.

## Module access

- Do not access another module's JPA repository or entity directly.
- Do not import another module's `internal` package.
- Cross-module access goes through exposed module APIs or named interfaces.
- Validate new dependency directions against the frozen dependency matrix.
- Prefer a direct synchronous call for immediate business requirements.
- Prefer an event for a downstream side effect.

## Persistence

- PostgreSQL is the production database.
- Flyway migrations define the physical schema.
- The frozen DBML is the logical v1 baseline; do not mutate it for an implementation convenience.
- Do not introduce H2-based persistence assumptions.
- Keep transactional boundaries in application services/use cases rather than controllers.
- Avoid lazy-loading behavior leaking into HTTP serialization.

## API

- REST/JSON.
- Controllers map transport DTOs to application calls.
- Never serialize persistence entities as the public API contract.
- Add OpenAPI descriptions for public endpoints and non-obvious fields.
- Keep validation at the boundary and business invariants in the owning domain/application layer.
- Preserve one consistent error/response model.

## Security and logging

- Treat authentication, finance, personal profiles, external accounts, and private notes as sensitive domains.
- Do not log secrets or full sensitive payloads.
- Password/PIN/token material must never be stored in plaintext.
- Do not weaken JWT/token invalidation semantics for convenience.
- Use parameterized SLF4J logging.

## Build

- Maven is the backend build tool.
- Prefer Spring Boot dependency management/BOM versions over arbitrary per-library overrides.
- Use the latest stable mutually compatible release set when the backend is initialized.
