# Personal Private Vault

Private, single-user personal information vault built as a backend-first modular monolith.

## Current status

The project is intentionally still in the architecture/bootstrap stage. No application implementation has been started yet.

Frozen baselines:

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1

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

1. The repository owner writes implementation code.
2. Antigravity writes and runs tests once for the completed implementation slice.
3. The repository owner fixes issues reported by Antigravity.
4. Codex performs the final review before commit/push.
5. Changes are committed directly to `main`; no pull-request workflow is planned for this private repository.
