# Reference Module — Phase 1 Scope

Antigravity implements this module from the active Codex handoff.

- Owned tables: `countries`, `languages`, `currencies`, `platforms`, `story_archetypes`, `world_settings`.
- No application-module dependencies.
- Public cross-module contracts stay in the `reference` base package.
- JPA entities/repositories/services stay under `reference.internal`.
- Do not expose JPA entities or Spring Data repositories.
- Phase 1 behavior is query-oriented catalog access; large real-world seeding is out of scope.
- PostgreSQL named enums use named-enum mapping, not varchar assumptions.
- Use Lombok only for safe boilerplate; never use `@Data` on JPA entities.
