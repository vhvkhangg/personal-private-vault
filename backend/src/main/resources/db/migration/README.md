# Flyway Migrations

This directory is intentionally empty in Backend Phase 0.

The frozen logical schema is:

`docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`

The repository owner will translate the frozen DBML into the initial PostgreSQL Flyway migration in a later backend phase.

Do **not** create an empty `V1__*.sql` migration merely to make Flyway quiet. Flyway would record it as applied and complicate the real baseline migration.
