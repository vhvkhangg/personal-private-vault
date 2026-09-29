# Architecture

This directory describes the frozen **v1 architecture baseline** for Personal Private Vault.

## Architecture at a glance

- **Style:** backend-first modular monolith
- **Modularity:** Spring Modulith application modules with explicit ownership and allowed dependencies
- **Persistence:** PostgreSQL + JPA/Hibernate; Flyway will own executable schema migrations once implementation starts
- **API:** REST/JSON + OpenAPI; a consistent `ApiResponse` contract is required, but its exact JSON shape is intentionally deferred to backend bootstrap
- **Media:** structured metadata in PostgreSQL; binary media in an S3-compatible object-storage abstraction
- **Search:** dedicated orchestration module; PostgreSQL-first implementation
- **Authentication:** permanent single-user account, password login, JWT access/refresh tokens, 6-digit private-mode PIN
- **Frontend:** deferred until backend completion
- **RAG:** deferred
- **Deployment topology:** intentionally not frozen

## Documents

| Document                                                     | Scope                                                                                                   |
| ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------- |
| [`architecture-overview.md`](architecture-overview.md)       | System goals, runtime view, architectural style, and high-level responsibilities                        |
| [`quality-attributes.md`](quality-attributes.md)             | Quality priorities and non-goals                                                                        |
| [`module-boundaries.md`](module-boundaries.md)               | Application modules, ownership, allowed dependencies, nested modules, and public API rules              |
| [`module-dependency-matrix.md`](module-dependency-matrix.md) | Frozen module dependency matrix                                                                         |
| [`data-architecture.md`](data-architecture.md)               | Relational model, ownership, shared identity, soft deletion, finance ledger, timestamps, and migrations |
| [`api-architecture.md`](api-architecture.md)                 | REST API principles, validation, OpenAPI, filtering, pagination, and response consistency               |
| [`security-architecture.md`](security-architecture.md)       | Current authentication/privacy boundary and deferred hardening                                          |
| [`integration-and-eventing.md`](integration-and-eventing.md) | Synchronous module APIs, events, scheduled jobs, and external integrations                              |
| [`search-architecture.md`](search-architecture.md)           | Global search behavior and PostgreSQL-first implementation strategy                                     |
| [`storage-backup-import.md`](storage-backup-import.md)       | Object storage, import pipeline, export, backup, and portability                                        |
| [`testing-and-review.md`](testing-and-review.md)             | Planned test stack and owner/Antigravity/Codex workflow                                                 |
| [`deferred-decisions.md`](deferred-decisions.md)             | Explicitly deferred architecture decisions                                                              |

## Diagrams

### Canonical sources

- `diagrams/source/functional-decomposition.drawio`
- `diagrams/source/module-dependencies.drawio`
- `structurizr/workspace.dsl`

### Review exports

- `diagrams/exported/functional-decomposition.svg`
- `diagrams/exported/system-context.svg`
- `diagrams/exported/container.svg`
- `diagrams/exported/module-dependency-overview.svg`
- `diagrams/exported/module-dependencies.svg`

Use SVG exports for documentation/review. Edit the canonical source and regenerate exports after changes.

## Architecture Decision Records

Accepted decisions are under [`../adr/`](../adr/README.md). ADRs explain **why** a decision exists; these architecture documents explain **how the accepted architecture fits together**.
