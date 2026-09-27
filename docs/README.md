# Documentation

This directory is the canonical documentation baseline for **Personal Private Vault**.

Backend Phase 0 is **complete and frozen**. The database schema, module boundaries, architecture diagrams, repository/package tree, and backend bootstrap baseline are established. Substantial business implementation has not started yet.

## Documentation map

| Area | Purpose |
|---|---|
| [`architecture/`](architecture/README.md) | System architecture, module boundaries, data, API, security, search, integration, storage, and workflow documentation |
| [`adr/`](adr/README.md) | Accepted Architecture Decision Records (ADRs) |
| [`database/`](database/README.md) | Frozen DBML logical schema baseline |
| [`repository/`](repository/repository-package-tree.md) | Frozen repository and Java package organization |
| [`implementation/`](implementation/README.md) | Implementation-phase plans, completion records, test evidence, and operational guidance |
| [`reviews/`](reviews/README.md) | Formal Codex pre-commit review history |

## Frozen baselines

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1
- Repository/Package Tree v1
- Backend Phase 0 bootstrap baseline

A frozen baseline is not immutable forever. It means changes require a concrete new requirement, defect, or accepted architectural reason. Avoid speculative refactoring of frozen baselines.

## Documentation rules

1. Documentation is written in English.
2. Canonical editable diagram sources are committed beside their exports.
3. Do not edit generated/exported diagram files as the source of truth.
4. Accepted architectural changes must update the relevant document and, when they change a decision, add or supersede an ADR.
5. Once Flyway migrations exist, migrations become the executable database source of truth; the DBML remains the logical architecture baseline and must be kept synchronized with accepted schema changes.
6. Never commit credentials, tokens, personal vault data, private backups, or production secrets to this repository.

## Current development workflow

1. The repository owner writes implementation code.
2. Antigravity writes the tests for the completed implementation slice and runs them once.
3. The repository owner fixes issues reported by Antigravity.
4. Codex performs the final review before commit/push.
5. Changes are committed directly to `main`; no pull-request workflow is planned for this personal repository.
