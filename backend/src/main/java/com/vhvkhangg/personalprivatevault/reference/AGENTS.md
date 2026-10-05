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

The active Phase 13 handoff, when present, is the temporary authority for this narrow adapter exception. Otherwise
the frozen-module rules remain in force.
