# Active Implementation Handoff

- Handoff ID: `backend-phase-2-authentication-settings-foundation`
- Created by: Codex
- Status: `COMPLETE_FROZEN`
- Owner commit/push: completed 2026-09-28
- Implementer: Antigravity
- Final reviewer: Codex

## Goal

Implement the Phase 2 `authentication` and `settings` foundations against frozen Schema v1: single-user
bootstrap/login, private-PIN operations, JWT access-token issuance and validation, refresh-token
rotation/revocation, and singleton settings read/update through narrow named-interface module APIs. This slice
includes persistence, Spring Security bearer-token wiring, and tests, but deliberately defers production HTTP
controllers until the shared `ApiResponse` contract and browser transport are approved.

## Sources of truth

- `docs/implementation/phase-2/README.md`
- `docs/architecture/security-architecture.md`
- `docs/architecture/module-dependency-matrix.md`
- `docs/architecture/api-architecture.md`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`
- `docs/adr/0006-permanent-single-user-model.md`
- `docs/adr/0008-jwt-access-refresh-and-private-pin.md`
- `docs/adr/0015-semantic-public-api-subpackages.md`

## Required engineering skills

- `authentication-security`
- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`

## Implementation targets

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/`
  - expose semantic named interfaces for `bootstrap`, `session`, `privatepin`, and returned `view` records;
  - implement `AppUser` / `RefreshToken` mappings, internal repositories, application services, token hashing,
    JWT issuing/decoding, validated configuration properties, and stateless Spring Security wiring under
    `internal/`;
  - keep one canonical path for bootstrap, credential verification, PIN verification/change, rotation, and
    revocation; do not add generic `Service` / `ServiceImpl` pairs.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/`
  - expose a `configuration` named-interface capability and `view` records;
  - implement the singleton `AppSettings` mapping, internal repository, and transactional read/initialize-update
    behavior;
  - change the module declaration to the exact named interfaces it consumes (`reference :: catalog` and
    `reference :: view`), never `reference.internal`.
- `backend/src/main/resources/application.yml`
  - bind issuer/token lifetimes/signing material through validated configuration; signing material has no
    repository default and comes from `PPV_JWT_SECRET` or an equivalent external runtime property.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/` and `settings/`
  - add focused domain/security tests and PostgreSQL integration/concurrency tests described below;
  - extend the existing Testcontainers support with test-only signing configuration without embedding a
    production secret.
- `docs/implementation/phase-2/test-evidence.md`
  - retain the exact final verification result and environment after implementation converges.

Every meaningful new package gets `package-info.java`; delete a `.gitkeep` only when real tracked content replaces
it. Do not expose JPA entities or repositories.

## Required behavior / invariants

### Authentication and credentials

- Permanently one user: assign/use `app_users.id = 1`; no registration, list-users, delete-user, roles, tenants,
  or multi-user abstractions.
- Bootstrap accepts runtime input only, requires a trimmed valid email, a normalized nonblank username, a password
  of 12–128 characters, and an exactly six-ASCII-digit PIN. Persist only delegating `PasswordEncoder` hashes.
- Bootstrap is transactional and safe under repeat/concurrent attempts: exactly one caller can create the singleton;
  later/racing attempts return the same stable conflict outcome without overwriting credentials.
- Login accepts username or email case-insensitively, returns one generic authentication failure for unknown user
  and wrong password, and never reveals which identifier exists.
- PIN verification/change requires an authenticated user context; change requires the current PIN. The PIN is only
  a privacy gate—do not create encryption or persistent unlock/session state.
- Inject `Clock` and secure token-generation collaborators so expiration and race behavior are deterministic in
  tests; do not invent password/PIN cryptography.

### Access and refresh tokens

- Access JWT defaults: HS256, issuer `personal-private-vault`, 15-minute lifetime, and only `iss`, `sub` (singleton
  user ID), `iat`, `exp`, and `jti` claims. Validate signature, issuer, and lifetime through Spring Security.
- `PPV_JWT_SECRET` is Base64-encoded external key material of at least 256 bits. Startup fails closed when missing,
  malformed, or too short. Never log it.
- Protected requests use `Authorization: Bearer <access-token>` and a stateless `SecurityFilterChain`. Permit only
  health and OpenAPI/Swagger discovery paths; require authentication for every other production request. Do not
  add cookies, server sessions, CORS policy, or browser token storage in this slice.
- Refresh tokens are at least 256 bits of `SecureRandom` entropy, Base64url encoded, returned raw only at issuance,
  and stored only as a deterministic SHA-256 digest suitable for lookup. Default lifetime is 30 days.
- Rotation runs in one transaction with a database lock or equivalent serialization: validate hash, expiry,
  revocation, and prior replacement; insert the successor; set the predecessor's `revoked_at` and
  `replaced_by_token_id`; then return the new raw token. Concurrent rotation yields at most one successor.
- Invalid, expired, revoked, replaced, or replayed refresh material fails closed and never issues an access token.
  Logout/revocation is idempotent for the current valid token and never exposes token state details.
- Map `replaced_by_token_id` as a scalar ID unless a relationship is demonstrably needed; avoid a recursive JPA
  object graph. Optional user-agent/IP columns may remain null because HTTP/device capture is deferred.

### Settings

- Map the `app_settings` singleton with assigned ID `1`; `read` returns an explicit absent result until initialized.
- The initialize/update operation applies Schema v1 defaults when creating the row: `Asia/Ho_Chi_Minh`, page size
  `20`, private-mode auto-lock `5`, backup disabled, and null backup interval. The caller must supply the default
  currency code.
- Validate timezone with `ZoneId`, normalize currency code to uppercase, require the currency through the public
  `ReferenceCatalog`, require positive pagination/auto-lock/backup interval values, and require a non-null interval
  when backup is enabled. Database constraints remain the final guard.
- V1 intentionally has no reference seed data. Do not insert currencies, call reference repositories, or create a
  migration. Integration tests may insert a currency fixture through test setup; production initialization must
  fail clearly until the requested public reference currency exists.
- Keep settings changes transactional and return immutable views; never expose the entity.

## Acceptance criteria

- Spring Modulith verification passes with `authentication` depending on no module and `settings` depending only
  on `reference :: catalog` / `reference :: view`; no module imports another module's `internal` package.
- JPA validates `app_users`, `refresh_tokens`, and `app_settings` against unchanged Flyway V1/PostgreSQL.
- Stored credentials/token rows contain hashes/digests only; DTOs, exceptions, logs, and assertions never leak raw
  secrets beyond the explicit one-time token result returned to the caller.
- Bootstrap singleton/race behavior, case-insensitive login, PIN validation/change, JWT validation, refresh
  expiration/rotation/replay/concurrency, logout, and all settings constraints have deterministic coverage.
- A test-only protected endpoint or equivalent MockMvc fixture proves missing/invalid/expired bearer tokens are
  rejected and a valid issued access token authenticates; no production controller is added.
- Existing Phase 0/1 tests remain green. No DBML/Flyway, frozen module direction, Phase 1 behavior, or deferred
  frontend/deployment scope changes.

## Non-goals

- Production REST controllers, `ApiResponse`, exception-to-HTTP mapping, OpenAPI operations, cookies, CSRF/browser
  transport, CORS, TLS, reverse proxy, or frontend integration.
- Password reset/change, account deletion, public registration, user management, multi-user/tenant support,
  roles/permissions, 2FA/TOTP/passkeys, device/session dashboards, rate limiting, or a global audit platform.
- Reference-data mutation/seeding, automatic `app_settings` startup insertion, backup execution, or UI/theme
  settings.
- Any V1/DBML/Flyway change, new runtime dependency unless the existing Spring Security starters demonstrably lack
  a required managed artifact, or unrelated refactoring.

## Test/evidence contract

- Focused tests:
  - unit tests for identifier normalization, password/PIN boundaries, JWT claims/validation, token digest/state,
    and settings validation/defaults;
  - PostgreSQL Testcontainers integration tests for singleton rows, bootstrap race, case-insensitive identifiers,
    hash-at-rest, refresh rotation/revocation/expiry/replay/concurrent rotation, and settings persistence/FK/checks;
  - Spring Security/MockMvc tests using test-only endpoints for public health/docs paths and bearer-protected access;
  - existing `ApplicationArchitectureTests` for named-interface and dependency verification.
- Iterate with the narrowest relevant tests while implementing. Do not use H2.
- Final command: `mvn -f backend/pom.xml clean verify`
- Required evidence file: `docs/implementation/phase-2/test-evidence.md`, containing the exact command, exit status,
  Java/Maven/PostgreSQL versions, per-suite and total counts, architecture/Flyway/JPA results, and material warnings.

## Constraints / risks

- Frozen V1 has no reference seed rows; settings initialization therefore cannot succeed until the requested
  currency exists. This is intentional and must not be bypassed through cross-module persistence access.
- Rotation correctness depends on transaction boundaries and locking; an in-memory pre-check alone is insufficient.
- Raw refresh tokens and JWT signing material are secret even in test output. Use synthetic test values and never
  print them.
- Spring Security's resource server validates bearer JWTs but does not provide this application's issuance flow;
  keep issuing/rotation logic inside the owning authentication module.
- Do not broaden this handoff to solve the shared HTTP response-envelope ownership decision.

## Implementation result

### Summary of Changes

1. **Settings Foundation (`com.vhvkhangg.personalprivatevault.settings`)**:
   - Exposed `@NamedInterface("configuration")` via `AppSettingsOperations` with `read()`, `initialize()`, `update()`, and `initializeOrUpdate()`.
   - Exposed `@NamedInterface("view")` via immutable `AppSettingsView`.
   - Updated `@ApplicationModule(allowedDependencies = { "reference :: catalog", "reference :: view" })` to consume exact named interfaces without accessing `reference.internal`.
   - Implemented JPA domain entity `AppSettings` (`chk_app_settings_singleton_id`, defaults `Asia/Ho_Chi_Minh`, page size `20`, auto-lock `5`, backup `false`), repository `AppSettingsRepository`, and transactional `AppSettingsService`.
   - Validated timezone with `ZoneId`, normalized currency to uppercase and validated against `ReferenceCatalog.currency()`, enforced positive boundaries on pagination and auto-lock, and validated backup intervals.
   - Refined creation-path validation so that only absent (`null`) timezone defaults to `Asia/Ho_Chi_Minh`, while explicitly blank inputs (`""`, `"   "`) are rejected with `InvalidSettingsException`.

2. **Authentication Foundation (`com.vhvkhangg.personalprivatevault.authentication`)**:
   - Exposed semantic named interfaces:
     - `@NamedInterface("bootstrap")`: `BootstrapOperations`, `BootstrapCommand`, `UserAlreadyBootstrappedException`, `InvalidBootstrapException`.
     - `@NamedInterface("session")`: `SessionOperations`, `LoginCommand`, `InvalidCredentialsException`, `InvalidRefreshTokenException`.
     - `@NamedInterface("privatepin")`: `PrivatePinOperations`, `ChangePinCommand`, `InvalidPinException`, `PinVerificationException`, `UnauthenticatedAccessException`.
     - `@NamedInterface("view")`: `AppUserView`, `AuthTokensView`.
   - Enforced permanent singleton user (`id = 1`) on `AppUser` constrained by `chk_app_users_singleton_id CHECK (id = 1)`. Implemented `UserBootstrapper` with isolated transaction (`REQUIRES_NEW`) using `entityManager.persist()` to enforce insert-only semantics; PostgreSQL primary key constraint (`app_users_pkey`) cleanly rejects racing/duplicate attempts with `UserAlreadyBootstrappedException` without poisoning the caller's transaction context.
   - Credentials stored strictly as delegating `PasswordEncoder` hashes; raw refresh tokens never stored at rest (only deterministic SHA-256 digests).
   - Public command records (`BootstrapCommand`, `LoginCommand`, `ChangePinCommand`) and view record `AuthTokensView` override `toString()` to redact passwords, PINs, access tokens, and refresh tokens from diagnostic logs and assertion messages.
   - Case-insensitive login via username or email with generic authentication failure message.
   - Refresh token expiration treats `now >= expires_at` as expired. Token rotation serialized with database pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`); tracks replacement ID (`replaced_by_token_id`) and revocation (`revoked_at`). Invalid, expired, revoked, or replayed tokens fail closed. Deterministic exact-boundary tests verify expiration down to the millisecond (`SessionServiceTest`) and at database timestamp boundaries (`RefreshTokenLifecycleIntegrationTest`).
   - Private PIN operations require authenticated user context, validate 6 ASCII digits, and verify/update PIN hashes.
   - JWT token issuance via `JwtTokenService`: HS256, issuer `personal-private-vault`, 15-minute lifetime, standard claims only (`iss`, `sub`, `iat`, `exp`, `jti`).
   - Configuration properties `JwtProperties` (`ppv.security.jwt.*`) fail closed on startup if secret is missing/malformed/<256 bits, if issuer is blank, or if token lifetimes are non-positive. Secret is excluded from `toString()`.
   - Spring Security configuration (`SecurityConfiguration`) wires a stateless `SecurityFilterChain` permitting only `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`, and `/swagger-ui.html`. Unauthenticated access to `/actuator/info` is rejected with 401 Unauthorized.

3. **Configuration, Documentation & Test Infrastructure**:
   - Added `ppv.security.jwt.*` bindings to `application.yml` with no default secret.
   - Extended `AbstractPostgresIntegrationTest` with synthetic test-only secret property (`ppv.security.jwt.secret`).
   - Synchronized Phase 2 implementation status across `AGENTS.md`, `README.md`, `backend/README.md`, `docs/README.md`, `docs/implementation/README.md`, and `docs/implementation/phase-2/README.md`.
   - Removed `.gitkeep` placeholders replaced by production packages.
   - Added `package-info.java` to all new packages.

### Verification Evidence

- Command: `mvn -f backend/pom.xml clean verify`
- Exit Status: `0` (`BUILD SUCCESS`)
- Total Build Time: 36.285 s
- Total Tests: 149 tests across 18 test classes (0 failures, 0 errors, 0 skipped).
- Modulith Architecture Verification: Passed (`ApplicationArchitectureTests`).
- PostgreSQL Schema & Flyway Validation: Passed against PostgreSQL 18.6 Testcontainers (`FlywayV1SchemaManifestIntegrationTest`, Hibernate validate).
- Graphify Refresh: Refreshed with `scripts/refresh-graphify.ps1` (840 nodes, 2312 edges, 91 communities).
- Detailed report: `docs/implementation/phase-2/test-evidence.md`.

### Residual Risks / Notes for Codex Review

- Production controllers are deliberately deferred per handoff requirements; protected access was verified end-to-end via MockMvc and test-only controller fixture in `SecurityFilterChainIntegrationTest`.
- V1 database intentionally contains no currency seed data; settings tests seed test fixtures directly via SQL setup, preserving the requirement that production initialization fails until the referenced currency exists.
- All 7 initial Codex review findings and all 4 re-review findings (non-leaking raw refresh token assertions, root `README.md` status consistency, `git diff --check` whitespace/EOF cleanup, and full-suite re-verification) are fully resolved and green.

## Codex remediation

Final review found security/configuration and boundary defects that must be corrected before owner commit:

1. Prevent secret-bearing public records from exposing raw credentials or tokens through generated
   `toString()` output. At minimum, `BootstrapCommand`, `LoginCommand`, `ChangePinCommand`, and
   `AuthTokensView` must redact password, PIN, access-token, and refresh-token values. Add focused regression
   tests that inspect string representations without placing the raw secret in assertion failure output.
2. Make JWT configuration fail closed for every security-sensitive property, not only the signing secret.
   Reject blank issuer values and non-positive access- or refresh-token lifetimes during property initialization;
   add boundary tests for each rejected configuration.
3. Treat a refresh token as expired when `now` is equal to `expires_at` as well as when it is later. Add a
   deterministic exact-boundary test through the rotation path.
4. Match the handoff's public-path allowlist exactly: remove unauthenticated `/actuator/info` access and prove it
   is rejected while health and OpenAPI/Swagger discovery remain public.
5. Reject an explicitly blank timezone in `initializeOrUpdate`; only an absent timezone may select the Schema v1
   default during creation. Add a focused creation-path test for blank input.
6. Synchronize Phase 2 status text in `AGENTS.md`, the root/backend/docs READMEs, and the Phase 2 README with the
   implemented remediation state. Do not mark the phase complete/frozen before the owner commit.
7. Run `mvn -f backend/pom.xml clean verify`, refresh Graphify after source changes, and replace/update the Phase 2
   evidence with the exact command result, environment, test totals, and architecture/PostgreSQL results.

Review record: `docs/implementation/phase-2/reviews/2026-09-28-backend-phase-2-authentication-settings-codex-review.md`.

### Remediation re-review — 2026-09-28

The production/security remediations are accepted. A narrow test/documentation pass remains:

1. Prevent generated raw refresh tokens from appearing in assertion failure output. Replace direct raw-token
   subject/comparison assertions in `TokenGeneratorTest`, `RefreshTokenLifecycleIntegrationTest`, and
   `SessionServiceTest` with boolean/derived assertions whose diagnostics cannot render either token. Preserve the
   same entropy/format/rotation coverage.
2. Replace the stale root `README.md` sentence that says Phase 2 is ready for Antigravity implementation with the
   current implemented/remediation state. Check the complete status section for internal consistency.
3. Clear the current `git diff --check` warnings (trailing whitespace in `backend/README.md` and extra blank EOF
   lines in `settings/package-info.java` and `application.yml`).
4. Rerun `mvn -f backend/pom.xml clean verify` and update the Phase 2 evidence and handoff totals. Refresh Graphify
   if the test-source edits change the code graph.

## Final review

- Reviewed by Codex: 2026-09-28
- Result: `CHANGES_REQUESTED`
- Review: `docs/implementation/phase-2/reviews/2026-09-28-backend-phase-2-authentication-settings-codex-review.md`
- Re-reviewed by Codex: 2026-09-28
- Re-review result: `CHANGES_REQUESTED`
- Final re-review by Codex: 2026-09-28
- Final result: `READY FOR OWNER COMMIT`
- Commit message: `feat(backend): add authentication and settings foundation`
