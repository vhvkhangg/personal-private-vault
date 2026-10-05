# Settings Module — Phase 2 Instructions

- Production implementation requires the active Phase 2 Codex handoff.
- Owns singleton `app_settings` and may depend only on the exposed public `reference` API.
- Validate default currency through `ReferenceCatalog`; never inject reference repositories/entities.
- Preserve Schema v1 constraints for pagination, private-mode auto-lock, and backup interval.
- Frontend theme/UI settings are deferred.
- Keep entities/repositories under `settings.internal` and expose semantic named-interface public contracts.

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
