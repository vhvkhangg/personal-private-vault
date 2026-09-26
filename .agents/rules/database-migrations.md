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
