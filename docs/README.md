# Documentation

This directory is the canonical documentation baseline for **Personal Private Vault**.

Backend Phase 0 and Backend Phase 1 are **complete and frozen**. Backend Phase 2 is implemented, verified, and
ready for owner commit under an active handoff. Phase 1 delivered the executable Flyway Schema v1 plus the implemented
and verified `reference` and `vault` foundation modules. The frozen logical DBML/module baselines remain unchanged;
the owner-approved Repository/Package Tree v1.1 refinement is recorded by ADR-0015.

## Documentation map

| Area | Purpose |
|---|---|
| [`architecture/`](architecture/README.md) | System architecture, module boundaries, data, API, security, search, integration, storage, and workflow documentation |
| [`adr/`](adr/README.md) | Accepted Architecture Decision Records (ADRs) |
| [`database/`](database/README.md) | Frozen DBML logical schema baseline and Flyway executable-schema guidance |
| [`repository/`](repository/repository-package-tree.md) | Frozen repository and Java package organization |
| [`agent-development-workflow.md`](agent-development-workflow.md) | Canonical Codex → Antigravity → Codex implementation workflow |
| [`implementation/`](implementation/README.md) | Implementation-phase plans, completion records, test evidence, and operational guidance |
| [`reviews/`](reviews/README.md) | Formal Codex pre-commit review history |

## Frozen baselines

- Database Schema v1
- Module Boundary v1
- Functional Decomposition Diagram v1
- C4 System Context v1
- C4 Container v1
- Module Dependency Diagram v1
- Repository/Package Tree v1.1
- Backend Phase 0 bootstrap baseline
- Backend Phase 1 reference/vault foundation baseline

A frozen baseline is not immutable forever. It means changes require a concrete new requirement, defect, or accepted architectural reason. Avoid speculative refactoring of frozen baselines.

## Documentation rules

1. Documentation is written in English.
2. Canonical editable diagram sources are committed beside their exports.
3. Do not edit generated/exported diagram files as the source of truth.
4. Accepted architectural changes must update the relevant document and, when they change a decision, add or supersede an ADR.
5. Once Flyway migrations exist, migrations become the executable database source of truth; the DBML remains the logical architecture baseline and must be kept synchronized with accepted schema changes.
6. Never commit credentials, tokens, personal vault data, private backups, or production secrets to this repository.

## Current development workflow

1. Codex creates `implementation/handoffs/ACTIVE.md`.
2. Antigravity implements/tests the active handoff.
3. Codex performs final review and requests remediation or returns `READY FOR OWNER COMMIT`.
4. On success Codex supplies one Conventional Commit message.
5. The owner commits/pushes directly to `main`.

Backend Phase 0 and Backend Phase 1 remain frozen; this workflow applies prospectively and does not reopen either phase.
