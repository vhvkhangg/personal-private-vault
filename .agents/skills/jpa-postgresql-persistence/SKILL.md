---
name: jpa-postgresql-persistence
description: Guide JPA/Hibernate/PostgreSQL/Flyway mappings, entity identity, native enums, associations, fetching, transactions, constraints, migrations, and repository design.
---

# JPA + PostgreSQL Persistence

- Frozen DBML is the logical baseline; Flyway is the executable physical schema.
- JPA must validate against migrated PostgreSQL; Hibernate does not own schema creation.
- Map names and PostgreSQL native enums deliberately.
- Use UTC-capable types for instants and `LocalDate` for date-only concepts.
- Prefer LAZY associations unless evidence requires otherwise.
- Avoid unnecessary bidirectional associations.
- Keep transaction boundaries in application services/use cases.
- Repositories remain internal to the owning module.
- Never substitute H2 for PostgreSQL.
- After a migration is applied to persistent/shared data, append later migrations instead of rewriting history.
