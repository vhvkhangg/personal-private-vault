# Authentication Module — Phase 2 Instructions

- Production implementation requires the active Phase 2 Codex handoff.
- Owns `app_users` and `refresh_tokens`; no application-module dependencies.
- Permanently single-user; no public registration or multi-user CRUD.
- Use `authentication-security`, `jpa-postgresql-persistence`, `backend-testing`, and package-organization guidance.
- Never log/persist plaintext passwords, PINs, raw tokens, auth headers, or signing secrets.
- Keep entities/repositories under `authentication.internal`.
- Public contracts use semantic named-interface subpackages when non-trivial.

## Phase 13 HTTP adapter exception

An accepted active Phase 13 handoff may modify this otherwise-frozen module only to add the REST/JSON adapter work
authorized by `docs/implementation/phase-13/README.md`.

Allowed:

- owner `internal/web` controller/request/response DTO/mapper/advice packages;
- mapping to existing owner operations/facades;
- HTTP Bean Validation and OpenAPI annotations;
- module exception-to-HTTP translation;
- tests needed for this HTTP surface.

Not allowed:

- changing existing domain/application invariants or persistence behavior;
- importing another module's internal/repository/entity;
- exposing application entities or internal Search contracts;
- inventing new business operations solely for HTTP convenience;
- schema/Flyway changes;
- Phase 14+ work.

Phase 13 may also update the existing `SecurityConfiguration` only for approved public auth request matchers and
shared JSON authentication-entry-point/access-denied handling. JWT/domain behavior stays frozen.

The active Phase 13 handoff, when present, is the temporary authority for this narrow adapter exception. Otherwise
the frozen-module rules remain in force.

## GDI26-S3-H1-AUTH Google Sign-In exception (2026-11-08)

An active handoff for `GDI26-S3-H1-AUTH` (see [ACTIVE.md](../../../../../../../../docs/implementation/handoffs/ACTIVE.md) and [executable contract](../../../../../../../../docs/implementation/maintenance/2026-11-08-gdi26-s3-h1-preparation/executable-handoff-h1-auth-2026-11-08.md)) may modify this module strictly to implement Spring-owned Google OIDC login and singleton Owner linking:

Allowed:
- internal shared token issuance seam in `SessionService` for verified singleton Owner;
- new internal entities/repositories for `ppv_owner_google_identities`, `ppv_google_login_attempts`, and `ppv_google_handoff_tickets` (V28);
- internal services, crypto, resources, and managed OAuth2 client in `authentication.internal`;
- internal web controller and DTOs under `/api/v1/auth/google` with conditional feature-flag gating;
- conditional public request matchers in `SecurityConfiguration` for start, callback, and handoff;
- focused unit, integration, and browser tests within module scope.

Not allowed:
- multi-user registration, unlinking/rebind, or raw ID-token login;
- changing PPV HS256 JWT validation, clock behavior, or legacy password/refresh/PIN invariants;
- exposing internal entities or Google credentials;
- Phase 16 or general auth refactoring.
