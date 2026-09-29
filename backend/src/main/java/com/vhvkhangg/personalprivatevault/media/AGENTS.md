# Media Module Agent Instructions

Applies to `com.vhvkhangg.personalprivatevault.media`.

## Ownership

- `albums`
- `images`

## Dependencies

Whole-module architecture allows only `vault`. The Phase 6 implementation must narrow the descriptor to the exact
Vault named interfaces actually used, expected to be `vault::entry`, `vault::enums`, and `vault::view`.

Never import another module's `internal` packages or repositories.

## Guidance

Use:

- `media-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- Album ID equals its `ALBUM` Vault Entry ID.
- Image ID equals its `IMAGE` Vault Entry ID.
- optional Album membership is validated inside Media.
- duplicate object key / non-null checksum is a domain conflict, including under races.
- `image_count` is derived, never stored.
- actual binary/object-storage I/O is outside Phase 6.
- `location_text` is free text and does not create a Location dependency.
- public APIs expose capabilities/views, never JPA entities/repositories.
- global list/search and aggregate deletion are outside scope.

## Phase gate

Phase 6 production changes require `READY FOR HANDOFF` plus an active approved Phase 6 handoff.
