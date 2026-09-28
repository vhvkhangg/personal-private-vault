# Reference Module Instructions

Backend Phase 1 behavior is frozen; owner-approved structural changes must preserve that behavior.

- Owned tables: `countries`, `languages`, `currencies`, `platforms`, `story_archetypes`, `world_settings`.
- No application-module dependencies.
- Public capabilities use named subpackages: `catalog`, `view`, and `enums`.
- `ReferenceCatalog` is a capability-oriented public interface; keep `ReferenceCatalogService` as its internal
  implementation. Do not rename the pair to generic `Service` / `ServiceImpl`.
- JPA entities/repositories/services stay under `reference.internal`.
- Do not expose JPA entities or Spring Data repositories.
- Reference lookup behavior remains query-oriented; large real-world seeding is outside the frozen Phase 1 scope.
- PostgreSQL named enums use named-enum mapping, not varchar assumptions.
- Routine read-only catalog lookups do not require per-method application logging.
