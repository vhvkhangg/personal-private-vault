# Location Module Agent Instructions

Applies to `com.vhvkhangg.personalprivatevault.location`.

## Ownership

- `brands`
- `location_categories`
- `addresses`
- `locations`
- `location_category_assignments`
- `location_dining_service_styles`
- `location_business_hours`

## Dependencies

Whole-module architecture allows `vault` and `reference`. The Phase 6 implementation must narrow the descriptor to
only the named interfaces actually used, expected to be Vault entry/enums/view plus Reference catalog/view.

Never import another module's `internal` packages or repositories. Do not add a Media dependency.

## Guidance

Use:

- `location-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- Brand ID equals its `BRAND` Vault Entry ID.
- Location ID equals its `LOCATION` Vault Entry ID.
- Address Country and optional Brand/Location currency/nationality use public Reference validation.
- Brand/Location names are not unique.
- Category names are case-insensitively unique.
- category/dining-style assignment is idempotent set behavior.
- business-hours unknown/closed/split/overnight semantics match frozen Schema v1.
- schedule replacement is atomic and serialized per Location.
- no coordinates/geocoding/maps or Media coupling.
- public APIs expose capabilities/views, never JPA entities/repositories.
- global list/search and aggregate deletion are outside scope.

## Phase gate

Phase 6 production changes require `READY FOR HANDOFF` plus an active approved Phase 6 handoff.
