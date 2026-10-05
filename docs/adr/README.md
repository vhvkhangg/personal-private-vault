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
