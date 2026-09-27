# Personal Private Vault

Private, single-user personal information vault built as a backend-first modular monolith.

## Current status

Backend Phase 0 is **complete and frozen**. Backend Phase 1 is **active**: implementation and verification of the executable PostgreSQL/Flyway Schema v1 and the `reference` and `vault` foundation modules are complete (57 tests, 0 failures against PostgreSQL 18.6 Testcontainers) and awaiting owner commit.

Development is agent-driven: Codex creates the handoff, Antigravity implements/tests it, Codex performs final review, and the owner commits/pushes.

Frozen baselines:

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1
- Repository/Package Tree v1
- Backend Phase 0 bootstrap baseline

Active (not frozen yet):

- Backend Phase 1 — PostgreSQL/Flyway Schema v1 + Reference & Vault Foundation (implementation & verification complete; awaiting owner commit)

## Planned stack

- Backend: Java + Spring Boot + Spring Modulith + Spring Data JPA/Hibernate
- Database: PostgreSQL
- Build: Maven
- Frontend (deferred until backend completion): Next.js + TypeScript + shadcn/ui + pnpm
- RAG (deferred): Python
- API: REST/JSON + OpenAPI
- Media: S3-compatible object storage abstraction

Version policy: use the latest stable, mutually compatible release set at implementation time; prefer LTS runtimes when appropriate and framework-managed dependency versions over arbitrary overrides.

## Repository layout

See [`docs/repository/repository-package-tree.md`](docs/repository/repository-package-tree.md).

## Architecture

Architecture sources and exports are under [`docs/architecture`](docs/architecture).

The frozen database schema is under [`docs/database`](docs/database).

## Development workflow

1. Codex creates the active implementation handoff with `$codex-create-handoff`.
2. Antigravity implements production code + tests with `/antigravity-implement-handoff`.
3. Codex performs `$codex-final-review`.
4. If ready, Codex supplies one Conventional Commit message.
5. The owner commits and pushes to `main`.

Graphify is optional for token-efficient code navigation; see `.agents/README.md`.
