---
name: database-migration-review
description: Review a Flyway/PostgreSQL migration against the frozen DBML, integrity constraints, data safety, and affected module ownership before it is accepted.
---

# Database Migration Review

Use when reviewing a new or changed Flyway migration.

## Checklist

1. Determine whether the migration:
   - implements the frozen v1 logical schema; or
   - intentionally changes the logical schema.
2. If it changes the frozen logical schema, require the architecture-change workflow and owner approval.
3. Check PostgreSQL syntax and data types.
4. Check primary keys, foreign keys, uniqueness, nullability, checks, and indexes.
5. Check delete/restore semantics where soft delete applies.
6. Check migration ordering and repeatability expectations.
7. Check that seed/reference data contains no secrets or real personal data.
8. Check application-module ownership of affected tables.
9. Check backward/data safety for destructive changes.
10. Check naming consistency with DBML and Java domain language.
11. Report risks without rewriting the frozen DBML unless the owner has approved that change.

Do not substitute H2 assumptions for PostgreSQL behavior.
