# Backend Phase 1 — Flyway V1 Manifest

Source logical baseline: `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`

- DBML SHA-256: `a40a8ab9392ce7da2966a5f2c11bbc177596071125e46b9c871e5fb93bd9c91f`
- DBML enum types: **41**
- DBML tables: **69**
- DBML inline references / generated foreign keys: **103**
- DBML named checks: **63**

Executable migration: `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`

- Flyway V1 SHA-256 at package creation: `25d0423e15490f558900b3df3d6fc6176840f820fa36e59ecc78cf4fcdbce686`
- `CREATE TYPE`: **41**
- `CREATE TABLE`: **69**
- Foreign keys: **103**
- Table columns represented: **495**

V1 is a mechanical structural translation of the frozen DBML plus only the clearly specified PostgreSQL-only invariants documented in the frozen implementation notes (selected case-insensitive uniqueness and the one-active-self partial unique index). It intentionally does not guess cross-table business triggers.

This hash is package-review evidence, not a forever checksum contract while Phase 1 is still active. After Phase 1 is frozen/applied to durable database state, V1 becomes append-only migration history.
