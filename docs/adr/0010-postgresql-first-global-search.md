# ADR-0010 — Use PostgreSQL-First Global Search

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

Global search spans many modules, but the application is permanently single-user and there is no measured scale requirement that justifies a separate search cluster.

## Decision

Keep `search` as a separate orchestration module and implement search initially with module search contracts plus PostgreSQL indexes/search features. Use `pg_trgm` and/or full-text search where they fit actual query behavior. Accent-insensitive Vietnamese search is not required.

## Rationale

This preserves module ownership and avoids Elasticsearch/OpenSearch operational complexity before evidence shows it is needed.

## Consequences

- Search is a leaf/orchestration module; feature modules do not depend on it.
- If fan-out becomes inefficient, a central search projection can be added without changing source-data ownership.

## Alternatives considered

- **Search inside `vault`:** rejected because it would reverse foundation dependencies and create cycles.
- **Elasticsearch/OpenSearch from day one:** rejected as premature for the expected scale.
