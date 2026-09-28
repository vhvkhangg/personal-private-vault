# Vault Module Instructions

Backend Phase 1 behavior is frozen; owner-approved structural changes must preserve that behavior.

- Owned tables: `vault_entries`, `favorites`, `ratings`, `tags`, `vault_entry_tags`.
- No application-module dependencies.
- Public capabilities use named subpackages: `entry`, `metadata`, `view`, and `enums`.
- Public interfaces are capability-oriented; internal implementations mirror the capability under
  `vault.internal.application`.
- Entities/repositories/services stay under `vault.internal`.
- Permanent deletion remains outside the frozen Phase 1 baseline.
- Capability checks remain mandatory before metadata writes.
- `FILM_CREDIT` is favorite-only (no rating/tags). `BRAND` supports global rating.
