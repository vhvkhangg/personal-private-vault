# Backend Phase 2 — Authentication + Settings Foundation

Status: **COMPLETE — FROZEN (2026-09-28)**

Phase 2 implements the two foundational modules that precede feature-domain work: `authentication` and `settings`.
The Phase 1 package-layout maintenance slice and Phase 2 implementation are committed/pushed by the owner.
Phase 2 passed final Codex review with `READY FOR OWNER COMMIT`, retains its final verification evidence, and
is now frozen. Future authentication/settings work requires a new owner-approved slice.

## Goals

### Authentication

Own `app_users` and `refresh_tokens` and provide:

- one-time single-user bootstrap; no public registration/user-management CRUD;
- login by username or email + password;
- password hashing and verification;
- exactly 6-digit private-mode PIN validation before hashing plus PIN verification/change capability;
- short-lived JWT access-token issuance;
- refresh-token expiration, secure server-side representation, rotation, replacement tracking, and revocation;
- logout/revocation behavior;
- Spring Security integration needed to authenticate protected backend requests.

### Settings

Own singleton `app_settings` and provide:

- read/update application settings;
- timezone, default currency, pagination size, private-mode auto-lock minutes, and backup configuration;
- validation matching frozen database constraints;
- default-currency validation through the public `reference` API, never through reference repositories/entities;
- no frontend theme/UI settings in this phase.

## Architecture and dependencies

- `authentication` has no application-module dependencies.
- `settings` may depend only on the exposed public API of `reference`.
- JPA entities/repositories remain internal to their owner.
- Public APIs follow ADR-0015: semantic capability packages + explicit `@NamedInterface`; public read models go under
  `view/`, stable public enums under `enums/` when needed.
- Avoid generic `Service` / `ServiceImpl` pairs; public interfaces are named for capabilities.

## Security invariants

- Never persist or log plaintext passwords, PINs, raw access tokens, raw refresh tokens, signing keys, or auth headers.
- Password/PIN verification uses Spring Security password hashing abstractions; do not invent cryptography.
- PIN must be exactly six decimal digits before hashing.
- The PIN is an application privacy gate, not database encryption.
- No registration endpoint and no multi-user expansion.
- Refresh rotation/revocation must be transactionally consistent and fail closed.
- Signing secrets/keys and bootstrap credentials come from runtime configuration, never repository defaults/migrations.
- Token lifetime and transport details must follow ADR-0008/security architecture and be made explicit in the Codex
  handoff before implementation.

## Logging

Log only meaningful authentication/security lifecycle events and operational failures with minimal non-sensitive
metadata. Never log credentials/token material. Routine settings/reference reads do not need per-method logs.

## HTTP/API scope

Phase 2 may introduce the minimal REST endpoints needed for bootstrap/authentication/token lifecycle and settings
read/update. Controllers expose DTOs, never JPA entities. OpenAPI annotations/contracts should follow ADR-0003.

Browser-specific cookie/storage strategy remains deferred until the frontend/security-deployment decision requires it.

## Testing contract for the Codex handoff

The handoff should require at least:

- unit tests for credential/PIN/token rules;
- PostgreSQL Testcontainers integration tests for singleton-user/settings constraints and refresh rotation/revocation;
- Spring Security/MockMvc tests for protected/unprotected endpoint behavior when HTTP endpoints are implemented;
- architecture verification for module dependencies/named interfaces;
- negative tests proving no registration/multi-user path and no invalid settings values;
- one final `mvn -f backend/pom.xml clean verify` evidence record on Java 25.

Do not use H2.

## Out of scope

- frontend;
- 2FA/TOTP/passkeys;
- multi-user support;
- production deployment/TLS/reverse-proxy decisions;
- global audit-log platform;
- unrelated feature modules;
- Schema v1 changes unless a real defect is discovered and separately approved.

## Start sequence

1. Verify/review the owner-approved package-layout refactor from the Phase 1 foundation.
2. Run `$codex-create-handoff` with this document as the owner-approved Phase 2 scope.
3. Select `/agents` → `backend-implementer` in Antigravity.
4. Run `/antigravity-implement-handoff`.
5. Run `$codex-final-review`.
6. Owner commits/pushes only after `READY FOR OWNER COMMIT`.


## Completion record

- Owner commit/push: completed 2026-09-28.
- Final retained verification: `mvn -f backend/pom.xml clean verify` — 149 tests, 0 failures/errors/skips.
- Final implementation handoff: [`handoff.md`](handoff.md).
- Final Codex review history: [`reviews/`](reviews/README.md).
- Phase status: **COMPLETE — FROZEN**.
