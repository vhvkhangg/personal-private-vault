# Flyway Migrations

Backend Phase 1 introduces the initial executable PostgreSQL baseline:

`V1__create_schema_v1.sql`

It is derived from `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` (logical schema v1.3) and creates all 41 PostgreSQL enum types and 69 frozen tables plus DBML structural constraints, foreign keys, and indexes.

V1 contains no seed data. Cross-table business invariants whose exact SQL behavior is not fully specified remain application-owned or are deferred to later focused migrations instead of being guessed.

Once Phase 1 is frozen or V1 has been applied to non-disposable state, do not edit V1. Use append-only `V2__...` and later migrations.
