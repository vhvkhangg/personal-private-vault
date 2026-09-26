# Database Schema v1 — Frozen

Canonical logical schema:

- [`personal-private-vault-schema-v1-FROZEN-final.dbml`](personal-private-vault-schema-v1-FROZEN-final.dbml)

## Status

**Database Schema v1 is frozen.**

Changes require a concrete new requirement, discovered integrity defect, or accepted architecture decision. Avoid speculative normalization/denormalization after freeze.

## Implementation rule

Before backend implementation, this DBML is the logical source baseline.

After Flyway migrations are introduced:

- Flyway migrations become the executable database source of truth;
- applied migrations are append-only history;
- JPA/Hibernate mappings must match the migrated schema;
- this DBML must be updated when an accepted migration changes the logical model.

## Database-specific constraints

Some PostgreSQL-specific constraints/indexes are intentionally implemented in Flyway rather than represented fully in DBML. Examples may include partial unique indexes, case-insensitive uniqueness strategies, `pg_trgm`/search indexes, and complex capability/ledger invariants.
