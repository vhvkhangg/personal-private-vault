# Architecture

This directory describes the frozen **v1 architecture baseline** for Personal Private Vault.

## Architecture at a glance

- **Style:** backend-first modular monolith
- **Modularity:** Spring Modulith application modules with explicit ownership and allowed dependencies
- **Persistence:** PostgreSQL + JPA/Hibernate; Flyway owns executable schema migrations
- **API:** REST/JSON + OpenAPI; consistent `ApiResponse` envelope contract with OpenAPI documentation
- **Media:** structured metadata in PostgreSQL; binary media in an S3-compatible object-storage abstraction
- **Search:** dedicated orchestration module; PostgreSQL-first implementation
- **Authentication:** permanent single-user account, password login, JWT access/refresh tokens, 6-digit private-mode PIN
- **Frontend:** [H1-F source was Owner-published and is paused](../implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/owner-publication-closeout-h1-f-2026-11-11.md). Final independent acceptance remains outstanding (`CHANGES_REQUESTED`), with FR-13 `UPSTREAM_DEPENDENCY_BLOCKED`. Broader Phase 16 remains deferred and Owner-gated.
- **RAG:** Phase 17 Foundation COMPLETE — FROZEN; Phase 18 CLOSED (Obsidian `.md` CPU-only); Phase 19 S19-1 ACCEPTED/PUBLISHED (`5e84e709`); S19-2 SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY. External indexing is INACTIVE; live external originals are DISABLED/DEFERRED. [CSA26 audit remediation](../implementation/maintenance/2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md) is complete, independently accepted and Owner-published. Its diagrams record the reviewed runtime container and module representations.
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
| [`rag-architecture.md`](rag-architecture.md)                 | Approved Alternative B Foundation; later ingestion/query/recommendation remain allocated, not enabled |
| [`storage-backup-import.md`](storage-backup-import.md)       | Object storage, import pipeline, export, backup, and portability                                        |
| [`google-drive-connection-authority.md`](google-drive-connection-authority.md) | Google Drive connection authority, OAuth endpoints, AES-256-GCM refresh token custody, and PH-03 revocation fence ([ADR-0026](../adr/0026-google-drive-connection-authority.md); additive schema in [`google-drive-connections.dbml`](../database/google-drive-connections.dbml)) |
| [`google-drive-connection-operations.md`](google-drive-connection-operations.md) | Operational runbook for Google Drive connection authority: localhost flow, default OFF, master key management, and uncertainty recovery |
| [`google-drive-files.md`](google-drive-files.md) | S2 private root/direct binary file APIs, durable intent recovery and bounded resources |
| [`google-drive-file-operations.md`](google-drive-file-operations.md) | S2 default-OFF flags, protected spool, stable action keys and failure/restart handling |
| [`testing-and-review.md`](testing-and-review.md)             | Planned test stack and owner/Antigravity/Codex workflow                                                 |
| [`deferred-decisions.md`](deferred-decisions.md)             | Explicitly deferred architecture decisions                                                              |

## Current Drive lifecycle

Drive backend milestones on `main` were independently accepted and Owner-published. [S1](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/owner-publication-closeout-2026-11-06.md)
delivered connection authority and encrypted token custody. [S2](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/owner-publication-closeout-2026-11-07.md) added the app-owned root and
direct binary file operations. [H1-B](../implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/owner-publication-closeout-h1-b-2026-11-08.md) added bounded child-folder Explorer operations.
Drive, Drive files and Google Sign-In default OFF. The published H1-F frontend pause and its open review gate are
recorded in the [H1-F closeout](../implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/owner-publication-closeout-h1-f-2026-11-11.md).

## Current module inventory

The 21 top-level backend module descriptors declare `account`, `authentication`, `collection`,
`drive`, `feed`, `fiction`, `film`, `finance`, `importdata`, `journal`, `knowledge`, `location`,
`media`, `people`, `personal`, `portability`, `rag`, `reference`, `search`, `settings` and `vault`.

See the [frozen package tree](../repository/repository-package-tree.md) and its
[Drive package delta](../repository/repository-package-tree.md#maintenance-gdi26-s1-additive-package-inventory--2026-11-06).
The frontend is separately published and paused as described above.

## Diagrams

### Canonical sources

- `diagrams/source/functional-decomposition.drawio`
- `diagrams/source/module-dependencies.drawio`
- `structurizr/workspace.dsl`
- `diagrams/source/container.dot` and `diagrams/source/module-dependencies.dot`

### Review exports

- `diagrams/exported/functional-decomposition.svg`
- `diagrams/exported/system-context.svg`
- `diagrams/exported/container.svg`
- `diagrams/exported/module-dependency-overview.svg`
- `diagrams/exported/module-dependencies.svg`

Use SVG exports for documentation/review. Edit the canonical source and synchronize applicable exports after changes.

The Drive labels in these diagram sources and exports describe the S1 and S2 review-era
snapshots from 2026-11-06 to 2026-11-07. Earlier `REVIEW_PENDING` badges belong to the S1 checkpoint.
See the [current Drive lifecycle](#current-drive-lifecycle) for the later publications and
default-OFF state. The [S1 handback](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/implementation-handback-s1-2026-11-06.md)
and [S2 handback](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/implementation-handback-s2-2026-11-06.md)
record the source/export deltas and generator limitations. S1 adds the Drive leaf and Google
OAuth/OIDC integration, and S2 adds file-operation descriptions without changing the topology.
Reproducible Graphviz/Structurizr regeneration and formal DSL validation remain `NOT_VERIFIED`.
Dated SAD snapshots remain historical.

The current C4 container and module dependency views reflect accepted architecture (Phase 17 frozen, Phase 18 closed Obsidian `.md` CPU-only, S19-1 accepted query embedding process). Java owns source authority, immutable ORIGINAL_BYTES storage, and PostgreSQL publication, while Python runs restricted staging/digest operations and query embeddings. Grounded QA is SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY. External indexing remains INACTIVE; live originals DISABLED/DEFERRED. See [CSA26 publication closeout](../implementation/maintenance/2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md).

## Architecture Decision Records

Accepted decisions are under [`../adr/`](../adr/README.md). ADRs explain **why** a decision exists; these architecture documents explain **how the accepted architecture fits together**.
