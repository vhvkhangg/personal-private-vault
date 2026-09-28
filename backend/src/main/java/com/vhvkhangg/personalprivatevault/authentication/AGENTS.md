# Authentication Module — Phase 2 Instructions

- Production implementation requires the active Phase 2 Codex handoff.
- Owns `app_users` and `refresh_tokens`; no application-module dependencies.
- Permanently single-user; no public registration or multi-user CRUD.
- Use `authentication-security`, `jpa-postgresql-persistence`, `backend-testing`, and package-organization guidance.
- Never log/persist plaintext passwords, PINs, raw tokens, auth headers, or signing secrets.
- Keep entities/repositories under `authentication.internal`.
- Public contracts use semantic named-interface subpackages when non-trivial.
