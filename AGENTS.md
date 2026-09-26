# Personal Private Vault — Agent Operating Contract

This file defines repository-wide instructions for coding agents.

## Project state

- The project is a private, permanently single-user personal vault.
- Backend development comes first.
- Frontend, RAG, and deployment are deferred until the backend scope is complete.
- The architecture is a modular monolith built with Spring Boot and Spring Modulith.
- PostgreSQL is the database.
- Maven is the backend build tool.
- The repository uses package-by-business-capability, not root-level technical-layer packages.

Read these baselines before making architecture-sensitive changes:

- `docs/architecture/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `docs/repository/repository-package-tree.md`
- `docs/adr/`

## Human / agent responsibilities

The repository owner writes production implementation code.

Agents must not proactively write or rewrite production implementation unless the owner explicitly asks for implementation help.

### Antigravity

After the owner finishes an implementation slice:

1. inspect the implementation and relevant architecture documentation;
2. write the required tests;
3. run the relevant test command exactly once;
4. report failures precisely;
5. do not automatically retry the test suite;
6. do not modify production code merely to make tests pass unless explicitly asked.

### Codex

Codex performs the final review after the owner has addressed Antigravity findings.

Default Codex final-review mode is review-only:

- do not modify production code;
- do not rewrite tests;
- do not rerun tests unless explicitly requested;
- do not commit, push, tag, or create/merge pull requests;
- record the final review in `docs/reviews/` when performing the formal pre-commit review.

## Git workflow

- The repository has one branch: `main`.
- There is no pull-request workflow.
- The repository owner performs commits and pushes.
- Agents must never run `git commit`, `git push`, `git tag`, or equivalent publishing commands unless the owner explicitly overrides this rule.
- Use Conventional Commit style when suggesting a commit message, for example `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.

## Frozen baselines

The following are frozen v1 baselines:

- Database Schema v1.
- Module Boundary v1.
- Architecture Diagrams v1.
- Repository / Package Tree v1.

Do not silently change a frozen baseline.

A deliberate baseline change requires:

1. explicit owner approval;
2. an ADR update or new ADR when the architectural decision changes;
3. all affected architecture/data documentation to be synchronized.

## Module architecture invariants

- Each application module owns its entities, repositories, and internal implementation.
- Other modules may use only the owning module's exposed API / named interfaces.
- Never import another module's `internal` package.
- Never share JPA repositories across module boundaries.
- `vault` is shared core and must not depend back on content modules.
- `reference` is stable reference data and must not depend on business modules.
- `search` is an orchestration/leaf module; business modules must not depend on it.
- `finance` and `journal` remain independent.
- Use synchronous module APIs when the caller needs an immediate result.
- Use application/domain events for cross-module side effects.
- Avoid events for ordinary direct lookups or commands.
- Prevent module dependency cycles.

Consult `docs/architecture/module-dependency-matrix.md` before adding a cross-module dependency.

## Backend conventions

- REST/JSON API with OpenAPI documentation.
- Do not expose JPA entities from controllers.
- Use explicit request/response DTOs at API boundaries.
- Use the project-standard `ApiResponse` envelope once defined; do not create competing envelope formats.
- Use SLF4J for application logging.
- Never log passwords, PINs, JWTs, refresh tokens, personal secrets, or sensitive finance/personal payloads.
- Persist schema changes through Flyway migrations.
- Do not use Hibernate auto-DDL as the production schema source of truth.
- Store timestamps in UTC; use `DATE` for date-only concepts such as birthdays.
- Media binaries belong in S3-compatible object storage; PostgreSQL stores metadata/object keys.
- Authentication uses access + refresh JWTs; refresh tokens are revocable/rotatable.
- The private-mode PIN is a six-digit UI gate and is stored hashed.
- Never commit credentials, tokens, real personal data, or production secrets.

## Testing baseline

When tests are requested, prefer the appropriate level:

- JUnit 5
- AssertJ
- Mockito
- Spring Boot Test
- Spring Modulith Test / `@ApplicationModuleTest`
- Testcontainers with PostgreSQL
- MockMvc for HTTP/API integration tests

Do not substitute H2 for PostgreSQL integration tests.

Frontend E2E / Playwright is deferred with the frontend.

## Documentation

- Repository documentation is written in English.
- Keep documentation factual and synchronized with accepted decisions.
- Prefer source-controlled editable diagram sources (`.drawio`, Structurizr DSL, DBML) over image-only documentation.
- Do not alter frozen architecture artifacts just to make documentation look cleaner.

## Scope discipline

Do not implement deferred scope without an explicit request:

- frontend;
- RAG;
- production deployment;
- multi-user support;
- 2FA/passkeys;
- media playback/readers;
- unrelated refactors.

Prefer the smallest change that fully solves the requested task.
