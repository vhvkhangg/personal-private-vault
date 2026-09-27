# Vault Module — Phase 1 Scope

Antigravity implements this module from the active Codex handoff.

- Owned tables: `vault_entries`, `favorites`, `ratings`, `tags`, `vault_entry_tags`.
- No application-module dependencies.
- Public APIs stay in `vault`; entities/repositories/services stay under `vault.internal`.
- Phase 1 implements shared identity, favorite/rating/tag metadata, soft delete, and restore.
- Permanent deletion is out of scope.
- Capability checks are mandatory before metadata writes.
- `FILM_CREDIT` is favorite-only (no rating/tags). `BRAND` supports global rating.
- Use Lombok only for safe boilerplate; never use `@Data` on JPA entities or association-heavy generated equality.
