# Backend Phase 0 — Maven / Spring Boot / Spring Modulith Bootstrap

Status: **COMPLETE — FROZEN (2026-09-27)**

Phase 0 establishes the backend build/runtime/module baseline only. It intentionally does not implement
business entities, repositories, DTOs, services, controllers, JWT flows, database tables, frontend,
RAG, or deployment.

## Completion record

Backend Phase 0 is complete and frozen.

- Antigravity architecture verification: **PASS** — 1 test, 0 failures, 0 errors, 0 skipped.
- Repository-safety hook regression suite: **PASS** — 10 tests.
- Codex final review: **READY FOR OWNER COMMIT**.
- Final review record: [`../../reviews/2026-09-27-backend-phase-0-final-governance-codex-review.md`](../../reviews/2026-09-27-backend-phase-0-final-governance-codex-review.md).
- Governance test evidence: [`governance-test-evidence.md`](governance-test-evidence.md).

The Phase 0 bootstrap baseline should not be changed merely for cleanup or speculative refactoring.
A later backend phase may deliberately evolve build/runtime configuration when required by an accepted
implementation need; any such change must remain compatible with the frozen architecture and repository
rules.

## Dependency baseline

- Java: **25 LTS**
- Spring Boot: **4.1.1**
- Spring Modulith: **2.1.1**
- Maven: **3.9.x** (**3.9.16 recommended; 3.9.15 is supported**)
- springdoc-openapi: **3.1.1**
- PostgreSQL local-dev image: **18.6**

Spring Boot manages compatible versions for Spring Framework, Hibernate, Jackson, JUnit and most other
framework dependencies. Do not override managed versions merely to chase a newer individual library.

### Testcontainers coordinates

Spring Boot 4.1.1 already manages Testcontainers **2.0.5**. Testcontainers 2.x uses the module artifact
names `testcontainers-postgresql` and `testcontainers-junit-jupiter`; do not use the old 1.x artifact
names `postgresql` and `junit-jupiter` under the `org.testcontainers` group. No additional Testcontainers
BOM is required for this project while Spring Boot manages these coordinates.

## What is already implemented in Phase 0

- Spring Boot `PersonalPrivateVaultApplication` bootstrap.
- Top-level Spring Modulith `@ApplicationModule(allowedDependencies = ...)` metadata.
- Nested `@ApplicationModule` metadata for `knowledge/*` and `collection/*`.
- Detailed `package-info.java` documentation for module ownership, responsibilities, dependencies, and boundaries.
- Maven dependency management and Enforcer baseline.
- Local PostgreSQL Docker Compose configuration.
- Spring/JPA/Flyway/OpenAPI/Actuator baseline configuration.
- Intentionally empty Flyway migration directory for the later database implementation phase.

## What the repository owner writes

The owner starts writing substantial implementation in later backend phases: entities, repositories,
DTOs, services/use cases, controllers, business rules, and related feature code.

There are **no Phase 0 Java TODO files that the owner must implement** after applying this package.

## Local prerequisites

Verify JDK 25, Maven 3.9.x, Docker Desktop/Docker Engine with Compose support, Antigravity CLI, and
Codex CLI are available before the relevant steps.

## First local setup after extracting the package

From the repository root on PowerShell:

```powershell
cd backend
Copy-Item .env.example .env
docker compose -f compose.dev.yml up -d
docker compose -f compose.dev.yml ps
cd ..
```

Creating `.env` is recommended so local DB values are explicit and easy to edit. Docker Compose has
matching fallback defaults, so the file is not technically required, but using it is the project
convention. `.env` is ignored by Git.

Spring Boot's `application.yml` has matching local-development defaults; Java does not rely on Docker
Compose loading `.env` into the IntelliJ process.

## Antigravity CLI permission setup (one time)

Before the first Antigravity run, follow:

`docs/implementation/antigravity-cli-permissions.md`

This lets harmless read-only Git commands and Maven test commands run without repeated approval while
commit/push/tag and destructive operations stay blocked/reviewed.

## Antigravity test phase — completed workflow reference

This section is retained as the reproducible workflow used to complete Phase 0. Do not rerun Phase 0 testing solely because this document exists; run it again only if a relevant Phase 0/build/module-governance file is deliberately changed.


Start Antigravity CLI from the **repository root** so it discovers root `AGENTS.md` and `.agents/skills`.

Invoke:

```text
/antigravity-test-slice
```

For Phase 0, Antigravity should create an architecture verification test equivalent to:

```java
ApplicationModules.of(PersonalPrivateVaultApplication.class).verify();
```

The Phase 0 test should not require a Spring application context or database schema merely to verify
module structure. Antigravity then runs the relevant Maven command once for that explicit test pass, preferably from repo root:

```text
mvn -f backend/pom.xml test
```

If it fails, Antigravity reports the failure and stops. The owner fixes/decides the next action; the
agent does not silently retry.

If that single pass stops **before any test executes** because a build/configuration defect is found, fix the
defect and explicitly start a **new** `/antigravity-test-slice` invocation. "Run exactly once" means once per
requested test pass; it does not mean proceeding to final review with zero executed tests.

## Codex final review — completed workflow reference

The final Phase 0 review has already completed with `READY FOR OWNER COMMIT`. The invocation instructions below are retained for future implementation slices.


After Antigravity has produced tests and one test result, and after the owner has addressed any findings,
run Codex CLI from the repository root.

**Do not use `/codex-final-review`**. Codex skills are invoked with a `$` prefix (or through `/skills`).

Use:

```text
$codex-final-review
```

or open `/skills` and select `codex-final-review`.

Codex reviews the implementation/tests/evidence and writes the formal review record under `docs/reviews/`.
It does not rerun tests by default and does not commit/push.

## Phase 0 completion sequence — historical record

1. Extract this package over the repository root.
2. Ensure JDK 25, Maven 3.9.x, and Docker are available. Maven 3.9.16 is recommended, but 3.9.15 is accepted by the build.
3. From `backend/`, copy `.env.example` to `.env`.
4. From `backend/`, run `docker compose -f compose.dev.yml up -d`.
5. Return to repo root and configure Antigravity project permissions once.
6. From repo root, run `/antigravity-test-slice` in Antigravity CLI.
7. If Antigravity reports a failure, fix it. If zero tests executed, explicitly invoke `/antigravity-test-slice` again for one new test pass.
8. Continue only after the architecture test has actually executed and you have fresh test evidence.
9. From repo root, invoke `$codex-final-review` in Codex CLI (or select it with `/skills`).
10. Read the Codex review. If status is ready, the owner commits and pushes manually.

## Why there is no V1 Flyway migration yet

Database Schema v1 is frozen, but translating the full DBML into PostgreSQL/Flyway is a separate backend
phase. Do not create an empty `V1__*.sql` migration.
