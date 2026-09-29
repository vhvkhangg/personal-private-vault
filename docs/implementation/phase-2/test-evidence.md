# Backend Phase 2 — Test Verification Evidence

- Date: 2026-09-28
- Handoff ID: `backend-phase-2-authentication-settings-foundation`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 36.285 s

## Test Counts and Summary

- **Total tests run:** 149
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class                                                                                 | Test Count | Failures | Errors | Result   |
| :----------------------------------------------------------------------------------------- | :--------: | :------: | :----: | :------: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests`                          |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest`       |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest`              |     6      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest`                       |     36     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest`                       |     5      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest`                    |     8      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest`                    |     20     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest`                   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest`                |     18     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest`                      |     16     |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest`                    |     1      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest`                    |     4      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest`                     |     2      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest`                     |     3      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` |     4      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest`   |     7      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest`              |     3      |    0     |   0    |   PASS   |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest`     |     7      |    0     |   0    |   PASS   |
| **Total**                                                                                  |  **149**   |  **0**   | **0**  | **PASS** |

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Security:** 7.1.1
- **Spring Modulith:** 2.1.1
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Architecture & Module Boundary Verification

`ApplicationArchitectureTests.verifiesModularStructure` passed cleanly via Spring Modulith:
- `authentication` has zero application module dependencies (`allowedDependencies = {}`).
- `settings` depends exclusively on `reference :: catalog` and `reference :: view` (`allowedDependencies = { "reference :: catalog", "reference :: view" }`).
- Internal packages (`*.internal.*`) are fully encapsulated and never exposed across module boundaries.
- No repository or entity is exposed outside its owning module.

## Flyway Migration & JPA Schema Validation

- **Flyway:** V1 baseline (`V1__create_schema_v1.sql`) cleanly migrated against fresh PostgreSQL 18.6 container instance (41 named enums, 69 application tables, 103 foreign keys).
- **Hibernate ORM Core:** 7.4.5.Final
- **Configuration:** `spring.jpa.hibernate.ddl-auto: validate`
- **Validated Entities:** All JPA entity mappings (`AppUser`, `RefreshToken`, `AppSettings`, plus existing Phase 1 entities) validated successfully against unchanged PostgreSQL Schema v1 without errors or ddl mismatch.

## Concurrency, Security & Remediation Verification

- **Bootstrap race safety:** Singleton user row is enforced by check constraint `chk_app_users_singleton_id CHECK (id = 1)`. Under concurrent bootstrap attempts, insert-only execution (`entityManager.persist()`) in an isolated transaction (`REQUIRES_NEW`) guarantees that the primary key constraint (`app_users_pkey`) cleanly rejects the concurrent attempt; the loser throws `UserAlreadyBootstrappedException` without poisoning the caller's transaction context.
- **Refresh rotation serialization:** Concurrent rotation requests using the same token hash were serialized via pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`); exactly one successor token was created and the concurrent attempt failed closed with `InvalidRefreshTokenException`.
- **Exact expiration boundary:** Refresh token expiration treats `now >= expires_at` as expired. Deterministic boundary behavior was verified down to the millisecond using a fixed clock (`SessionServiceTest`) and against current database timestamp boundaries (`RefreshTokenLifecycleIntegrationTest`).
- **Replay attack detection:** Attempting to rotate an already replaced refresh token immediately fails closed.
- **Credential hashing & secret redaction:** Passwords and PINs are stored strictly as delegating `PasswordEncoder` hashes; raw refresh tokens are never persisted in the database (only deterministic SHA-256 digests). Secret-bearing public records (`BootstrapCommand`, `LoginCommand`, `ChangePinCommand`, `AuthTokensView`, `JwtProperties`) redact passwords, PINs, access tokens, and refresh tokens from `toString()` representations. In test suites (`TokenGeneratorTest`, `RefreshTokenLifecycleIntegrationTest`, `SessionServiceTest`, `AuthenticationBootstrapIntegrationTest`, `SecretRedactionTest`), raw refresh tokens are never asserted directly as subjects or expected comparison values; boolean and derived assertions prevent raw credentials or tokens from ever rendering in test diagnostics or assertion failure messages.
- **Fail-closed JWT configuration:** `JwtProperties` validates not only the 256-bit Base64 secret, but also non-blank `issuer` and strictly positive `accessTokenLifetime` and `refreshTokenLifetime` durations during property initialization (`JwtPropertiesTest`).
- **Strict public endpoint allowlist:** Anonymous access is permitted solely for `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`, and `/swagger-ui.html`. Unauthenticated requests to `/actuator/info` are rejected with 401 Unauthorized (`SecurityFilterChainIntegrationTest`).
- **Settings timezone validation:** Explicitly blank timezone inputs (`""`, `"   "`) on creation fail validation with `InvalidSettingsException`; Schema v1 default (`Asia/Ho_Chi_Minh`) applies only when the input timezone is absent (`null`), matching update-path validation (`AppSettingsValidationTest`).

## Warnings & Diagnostics

- Terminally deprecated `sun.misc.Unsafe` warning from Lombok compile processor on Java 25.
- Dynamic agent loading warning from ByteBuddy / Mockito on Java 25.
- Expected unique constraint violation warnings logged during concurrency race tests (`duplicate key value violates unique constraint "uq_ci_tags_name"` and `app_users_pkey`).
- No unhandled exceptions or leaks of secret material in logs or assertion outputs.

## Graphify Code Graph Refresh

- **Script:** `scripts/refresh-graphify.ps1` (`graphify extract . --code-only`)
- **Status:** Succeeded
- **Graph Summary:** 840 nodes, 2312 edges, 91 communities written to `graphify-out/graph.json`.
