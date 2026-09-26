# ADR-0004 — Use PostgreSQL, JPA/Hibernate, and Flyway

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The data model is relational, highly structured, constraint-heavy, and includes transactions, reference data, JSONB metadata, and PostgreSQL-specific search capabilities.

## Decision

Use PostgreSQL as the primary database, Spring Data JPA/Hibernate for ORM/persistence integration, and Flyway for executable schema migrations. The frozen DBML is the logical baseline; after migrations begin, Flyway is the executable schema source of truth.

## Rationale

PostgreSQL matches the relational/integrity requirements and provides the search/index/JSONB capabilities expected by the design. Flyway makes schema evolution explicit and reviewable rather than relying on automatic ORM mutation.

## Consequences

- JPA entities remain module-internal.
- Database foreign keys do not grant cross-module repository access.
- Integration tests should use PostgreSQL Testcontainers, not H2 as a production substitute.
- DBML must be synchronized when accepted schema changes occur.

## Alternatives considered

- **Single generic document/JSON store:** rejected because it weakens the relational constraints required by the domain.
- **Hibernate auto-DDL as migration strategy:** rejected for controlled schema evolution.
