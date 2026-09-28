---
trigger: glob
globs: "backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/**/*.java, backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/**/*.java, backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/**/*.java, backend/src/test/java/com/vhvkhangg/personalprivatevault/settings/**/*.java"
description: "Backend Phase 2 authentication/settings scope and security invariants."
---

# Backend Phase 2 — Authentication + Settings Rule

- Production implementation requires the active Codex Phase 2 handoff.
- `authentication` owns `app_users` / `refresh_tokens` and has no application-module dependencies.
- `settings` owns `app_settings` and may use only the exposed public `reference` API.
- Permanently single-user: no registration or multi-user CRUD.
- Never persist/log plaintext passwords, PINs, raw tokens, auth headers, or signing secrets.
- PIN is exactly six decimal digits before hashing and is a privacy gate, not encryption.
- Refresh-token rotation/revocation must be transactionally consistent and fail closed.
- Runtime secrets are external configuration, never migrations/source defaults.
- Follow semantic public API packaging from ADR-0015 and the package-organization rule.
- PostgreSQL/Testcontainers only for persistence integration tests; never H2.
- 2FA/passkeys, frontend, deployment hardening, and unrelated feature modules are out of Phase 2 scope.
