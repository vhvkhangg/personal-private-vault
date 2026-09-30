# Personal Private Vault

Private, single-user personal information vault built as a backend-first modular monolith.

## Current status

Backend Phase 0 and Backend Phase 1 are **complete and frozen**. Phase 1 delivered the executable PostgreSQL/Flyway Schema v1 plus the implemented and verified `reference` and `vault` foundation modules (57 tests, 0 failures against PostgreSQL 18.6 Testcontainers).

Backend Phases 2 (`authentication` + `settings`), 3 (`people`), 4 (`fiction`), 5 (`film`), 6
(`media` + `location`), and 7 (`account`) are **complete and frozen** after owner commit/push. The Phase 1–3 milestone remains
`MILESTONE_READY`. The Phase 4–6 milestone is also `MILESTONE_READY` after committed privacy-safe constraint-logging maintenance. Phase 7 (`account`) is complete/frozen after owner commit/push.

Development is agent-driven: Codex creates the handoff, Antigravity implements/tests it, Codex performs final review, and the owner commits/pushes.

Frozen baselines:

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1
- Repository/Package Tree v1.1
- Backend Phase 0 bootstrap baseline

Frozen implementation phases:

- Backend Phase 0 — bootstrap baseline
- Backend Phase 1 — PostgreSQL/Flyway Schema v1 + Reference & Vault Foundation
- Backend Phase 2 — Authentication + Settings Foundation
- Backend Phase 3 — People Foundation
- Backend Phase 4 — Fiction Foundation
- Backend Phase 5 — Film Foundation
- Backend Phase 6 — Media + Location Foundations
- Backend Phase 7 — External Account & Relationship History Foundation

Current gate:

- Backend Phase 8 — Knowledge Foundation preparation
- preparation is `READY FOR HANDOFF`; owner commit/push is next
- no Phase 8 implementation handoff until preparation is approved and owner committed/pushed

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

1. ChatGPT closes/freezes a committed phase and prepares the next phase.
2. Codex runs `$codex-pre-handoff-review`.
3. After `READY FOR HANDOFF` and preparation commit/push, Codex runs `$codex-create-handoff`.
4. Antigravity implements/tests with `/antigravity-implement-handoff`.
5. Codex runs `$codex-final-review`.
6. Owner commits/pushes after `READY FOR OWNER COMMIT`.

See [`docs/workflow/owner-phase-workflow.md`](docs/workflow/owner-phase-workflow.md) and [`docs/roadmap.md`](docs/roadmap.md).

Graphify is optional for token-efficient code navigation; see `.agents/README.md`.

Workflow details: [`docs/workflow/agent-development-workflow.md`](docs/workflow/agent-development-workflow.md).
