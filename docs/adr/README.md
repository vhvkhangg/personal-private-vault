**D21 current scope:** [Slice 2 `.md`-only applicability](../implementation/phase-18/slice-2-markdown-only-d21-scope-and-acceptance.md) is Owner-approved; [ADR-0024](0024-phase-18-slice-profile-acceptance-boundaries.md) is an **ACCEPTED (D22)** higher-contract slice-applicability qualification, **not** an approved overall Phase 18 scope change or implementation authority. Earlier execution/status lines below are historical where superseded.

> **Current Phase 18 amendment:** [ADR-0023](0023-phase-18-application-level-snapshot-worker-trust.md) is accepted (2026-10-19) and takes precedence over ADR-0022 for same-Windows-user worker trust and G1/G3 OS-level requirements. ADR-0022 remains the underlying whole-file snapshot/NO_CHANGE contract.

# Architecture Decision Records

ADRs capture **why** important architectural decisions were accepted.

## Lifecycle

- `Accepted`: current decision.
- `Superseded`: replaced by a newer ADR; do not rewrite the old rationale.
- `Deprecated`: retained for history but no longer recommended.

When a frozen architectural decision changes materially, add a new ADR and mark the previous ADR as superseded rather than silently editing history.

## Index

| ADR                                                       | Decision                                                              | Status   |
| --------------------------------------------------------- | --------------------------------------------------------------------- | -------- |
| [ADR-0001](0001-modular-monolith-with-spring-modulith.md) | Use a modular monolith with Spring Modulith                           | Accepted |
| [ADR-0002](0002-package-by-business-capability.md)        | Package by business capability with explicit module boundaries        | Accepted |
| [ADR-0003](0003-rest-json-openapi-api.md)                 | Use REST/JSON with OpenAPI and a consistent response contract         | Accepted |
| [ADR-0004](0004-postgresql-jpa-hibernate-flyway.md)       | Use PostgreSQL, JPA/Hibernate, and Flyway                             | Accepted |
| [ADR-0005](0005-vault-entry-shared-identity.md)           | Use a shared vault-entry identity for content capabilities            | Accepted |
| [ADR-0006](0006-permanent-single-user-model.md)           | Model the application as permanently single-user                      | Accepted |
| [ADR-0007](0007-hybrid-module-communication.md)           | Use synchronous module APIs plus events for side effects              | Accepted |
| [ADR-0008](0008-jwt-access-refresh-and-private-pin.md)    | Use access/refresh JWTs and a separate private-mode PIN               | Accepted |
| [ADR-0009](0009-s3-compatible-object-storage.md)          | Keep media binaries in S3-compatible object storage                   | Accepted |
| [ADR-0010](0010-postgresql-first-global-search.md)        | Use a PostgreSQL-first global search strategy                         | Accepted |
| [ADR-0011](0011-ledger-based-finance.md)                  | Use ledger entries as the finance source of truth                     | Accepted |
| [ADR-0012](0012-import-and-data-portability.md)           | Treat import/export and data portability as first-class capabilities  | Accepted |
| [ADR-0013](0013-stable-compatible-version-policy.md)      | Use latest stable mutually compatible versions                        | Accepted |
| [ADR-0014](0014-defer-frontend-rag-and-deployment.md)     | Defer frontend, RAG, and deployment-specific architecture             | Accepted |
| [ADR-0015](0015-semantic-public-api-subpackages.md)       | Organize public module APIs into semantic named-interface subpackages | Accepted |
| [ADR-0016](0016-root-http-contract-module-local-adapters.md) | Keep shared HTTP contract at root; adapters module-local              | Accepted |
| [ADR-0017](0017-portability-readonly-snapshot-module.md)     | Use a read-only portability module for lossless export snapshots      | Accepted |
| [ADR-0018](0018-phase-15-bounded-backend-remediation.md) | Authorize bounded Phase 15 audit remediation without schema/edge expansion | Accepted |
| [ADR-0019](0019-local-rag-before-frontend.md) | RAG-first sequencing; Phase 17 clauses 2–5 amended by ADR-0020 | Accepted / amended |
| [ADR-0020](0020-phase-17-single-job-reservations-e1.md) | Alternative B durable reservations, single job, consent CAS, E1 and independent completed-index lifetime | Accepted / frozen Phase 17; scoped external applicability amended by ADR-0022 |

## Phase 18 prospective contracts

- [ADR-0021](0021-phase-18-bounded-multi-file-verified-indexing.md) – accepted design, **partially superseded for external snapshots** by ADR-0022; historical rationale and unaffected D18-A guarantees preserved.
- [ADR-0022](0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) – **OWNER_APPROVED / CANONICALLY_SYNCHRONIZED**, 2026-10-18: consolidated Immutable Snapshot + Whole-File Consent + SHA-256 NO_CHANGE, exact external-only applicability and C_N/C_D/C_R/C_P. Independent documentation synchronization review pending.
- [ADR-0024](0024-phase-18-slice-profile-acceptance-boundaries.md) – **ACCEPTED under D22 (2026-10-21)** for D21 applicability only; broad Phase 18 remains open.

**Current execution handoff:** [P18-S1-IMPORT-SNAPSHOT](../implementation/phase-18/slice-1-import-snapshot-handoff.md) through [ACTIVE](../implementation/handoffs/ACTIVE.md), owner-approved D13 for **Slice 1 A+B+C only**, Antigravity implementation and Codex independent review. Prior `P18-DOCUMENT-INDEXING` reviews remain historical/CHANGES_REQUESTED. Original-live indexing DISABLED/DEFERRED; snapshot indexing inactive; G1–G4 and FR18-1–11 remain OPEN. Contract approval is not runtime acceptance.
