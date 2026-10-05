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
