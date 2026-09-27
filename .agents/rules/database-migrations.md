---
trigger: glob
globs: "backend/src/main/resources/db/migration/*.sql, docs/database/*.dbml"
description: "PostgreSQL/Flyway migration and frozen-schema integrity rules."
---

# Database Migration Rule

- PostgreSQL is the target database.
- Treat `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` as the frozen logical baseline.
- Do not modify the frozen DBML unless the owner explicitly approves a schema-baseline change.
- Use Flyway for physical migrations.
- Give every migration a single clear purpose.
- Preserve data when a safe migration path exists.
- Add database constraints for invariants that the database can enforce reliably.
- Keep application-only capability rules in the application layer when SQL enforcement would become brittle.
- Do not use H2-specific SQL.
- Do not place secrets or real personal data in seed migrations.
- Review indexes against actual query/filter requirements rather than adding speculative indexes.

## Phase 1 baseline policy

- `V1__create_schema_v1.sql` is the executable structural baseline derived from frozen DBML v1.3.
- V1 contains all 41 PostgreSQL enum types and all 69 frozen tables, DBML structural constraints/FKs/indexes, selected case-insensitive unique indexes, and the active-self partial unique index.
- Cross-table/business invariants whose exact SQL behavior is not fully specified remain in the owning application module or a later focused migration; do not invent triggers.
- No reference/business seed data belongs in V1.
- Once V1 has been applied to a non-disposable database or Phase 1 is frozen, treat it as append-only history: corrections use `V2__...` and later migrations.
- During Phase 1 only, if a V1 defect is found before freeze and every affected database is disposable local/test state, recreate the database rather than using Flyway repair to conceal a changed checksum.
