# ADR-0016 — Keep Shared HTTP Contract at the Application Root and Adapters Module-Local

- **Status:** Accepted
- **Date:** 2026-10-05

## Context

ADR-0003 requires one REST/JSON `ApiResponse` family while controllers remain owned by their business modules.
Backend Phase 13 is the first controller phase, so the response contract and package placement must now be concrete.

Creating a new `common`, `shared`, `api`, or `web` top-level package would create or imply another Spring Modulith
application module and expand the frozen module dependency model solely for transport concerns.

Duplicating the envelope/OpenAPI infrastructure inside every business module would allow wire-contract drift.

## Decision

- Keep controllers and module-specific HTTP DTO/mappers/advice under each owning module's `internal/web` adapter.
- Place the very small application-wide HTTP envelope/framework/OpenAPI types directly in
  `com.vhvkhangg.personalprivatevault`.
- Do not create a new top-level Spring Modulith module for HTTP.
- Root-level HTTP types contain no business logic, persistence or module-specific DTOs.
- Module-specific exception translation remains module-local while using the one root response family.
- The frozen business-module dependency matrix remains unchanged.

This is a narrow Phase 13 exception to the earlier bootstrap-only root-package convention.

## Consequences

- Every module can use one envelope without a new application-module dependency edge.
- HTTP adapters remain internal Java implementation details of their owner module.
- The root package gains a small, explicitly bounded cross-cutting transport responsibility.
- Architecture tests must prevent root HTTP support from growing into generic business utilities.

## Alternatives considered

- **New shared `web`/`api` application module:** rejected because it changes the module graph for transport-only
  concerns.
- **Envelope duplicated per module:** rejected because it permits drift.
- **Expose application command/view records directly:** rejected because it couples the wire contract to application
  contracts and weakens future compatibility.
