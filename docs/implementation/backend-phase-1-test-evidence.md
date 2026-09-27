# Backend Phase 1 — Test Verification Evidence

- Date: 2026-09-27
- Handoff ID: `backend-phase-1-reference-vault-foundation`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 28.399 s

## Test Counts and Summary

- **Total tests run:** 57
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class | Test Count | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest` | 36 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest` | 8 | 0 | 0 | PASS |
| **Total** | **57** | **0** | **0** | **PASS** |

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Flyway Migration Verification

Flyway V1 (`V1__create_schema_v1.sql`) migrated cleanly against a fresh PostgreSQL 18.6 container instance.
Structural verification confirmed the frozen manifest baseline:

- **Named ENUM types:** 41 / 41
- **Application tables:** 69 / 69 (excluding `flyway_schema_history`)
- **Foreign keys:** 103 / 103

## JPA Schema Validation

- **Hibernate ORM Core:** 7.4.5.Final
- **Configuration:** `spring.jpa.hibernate.ddl-auto: validate`
- **Dialect:** `org.hibernate.dialect.PostgreSQLDialect`
- **Result:** Successfully validated all 11 JPA entity mappings (`Country`, `Language`, `Currency`, `Platform`, `StoryArchetype`, `WorldSetting`, `VaultEntry`, `Favorite`, `Rating`, `Tag`, `VaultEntryTag`) with named enums (`platform_kind`, `vault_entry_type`, `rating_grade`) against PostgreSQL 18.6 without errors.

## Remediation & Concurrency Verification

- **Remediation component:** Added `TagCreator` in `vault.internal.application` using `@Transactional(propagation = Propagation.REQUIRES_NEW)` for tag insertion and recovery lookup.
- **Race condition recovery:** `VaultMetadataService.createTag` delegates the insertion to `TagCreator`. Upon a concurrent unique key conflict on index `uq_ci_tags_name`, the inner insertion transaction rolls back independently without poisoning the caller's transaction context. The catch block executes `findByNameIgnoreCaseInNewTransaction` to fetch and return the canonical persisted tag.
- **Concurrent regression test:** `VaultMetadataIntegrationTest.concurrentCaseInsensitiveTagCreationRecoversFromUniqueIndexConflict` forces two independent concurrent transactions to attempt creating the same trimmed tag with different casing (`"  ConcurrentTag  "` vs `"concurrentTAG  "`). The loser encounters PostgreSQL error 23505 (`unique_violation`) on `uq_ci_tags_name`, recovers cleanly, and both callers return the identical canonical persisted tag with exactly one case-insensitive row stored in PostgreSQL.

## Warnings & Diagnostics

- Terminally deprecated `sun.misc.Unsafe` warning from Lombok compile processor on Java 25.
- Dynamic agent loading warning from ByteBuddy / Mockito inline mock maker on Java 25.
- Expected unique constraint violation warning logged by Hibernate during the concurrent race recovery test (`HHH000247: ErrorCode: 0, SQLState: 23505 ... ERROR: duplicate key value violates unique constraint "uq_ci_tags_name"`).
- No application errors or unhandled exceptions.

## Graphify Code Graph Refresh

- **Script:** `.agents/refresh-graphify.ps1` (`graphify extract . --code-only`)
- **Status:** Succeeded
- **Graph summary:** 418 nodes, 1056 edges, 50 communities written to `graphify-out/graph.json`.
