# Settings Module — Phase 2 Instructions

- Production implementation requires the active Phase 2 Codex handoff.
- Owns singleton `app_settings` and may depend only on the exposed public `reference` API.
- Validate default currency through `ReferenceCatalog`; never inject reference repositories/entities.
- Preserve Schema v1 constraints for pagination, private-mode auto-lock, and backup interval.
- Frontend theme/UI settings are deferred.
- Keep entities/repositories under `settings.internal` and expose semantic named-interface public contracts.
