# Database

## Logical architecture baseline

`personal-private-vault-schema-v1-FROZEN-final.dbml` is the frozen logical Database Schema v1 (v1.3).
Do not edit it for implementation convenience.

## Executable schema baseline

Backend Phase 1 introduces `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`.
Flyway is the executable PostgreSQL schema source of truth. V1 is derived from the frozen DBML and creates
all 41 enum types and all 69 tables plus structural constraints, foreign keys, and DBML indexes. It also
includes clearly specified PostgreSQL-only case-insensitive uniqueness and the active-self partial unique
index from the frozen implementation notes.

Cross-table/business invariants whose exact SQL semantics are not yet fully specified remain in the owning
application module or a later focused migration rather than being guessed in V1.

## Migration discipline

- Never use Hibernate auto-DDL to evolve the schema.
- Do not put real personal data or secrets in migrations.
- After V1 is frozen/applied to durable state, do not edit it; use `V2__...` and later migrations.
- Search-specific `pg_trgm`/FTS indexes are deferred until the search implementation phase.
