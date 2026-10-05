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

## Phase 12 read-only search extension

An accepted Phase 12 handoff may modify this otherwise-frozen module **only** to add the read-only global-search
contract/query support defined by `docs/implementation/phase-12/README.md`.

Allowed:

- semantic public `search` named interface;
- owner-local search query/application/repository methods;
- search-only package descriptors/tests;
- Vault batch qualification/tag calls where this module already legally depends on Vault.

Not allowed:

- changing existing mutation/validation/lifecycle semantics;
- exposing entities/repositories/internals;
- importing the top-level `search` module;
- reading another module's repository/table directly;
- adding unbounded lists or per-hit cross-module calls.

The Phase 12 active handoff, when present, is the authority for this narrow exception to the original phase gate.

## M10-12-1 maintenance exception

A Codex handoff created from
`docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md` may modify this frozen module
only for the Search SQL/query-folding and snippet-offset defect described there.

Do not use the maintenance handoff to change mutation behavior, ownership, public Search contracts, ranking, source
bounds, schema/index definitions, or unrelated code. The maintenance handoff is the temporary authority; otherwise
the frozen phase rules remain in force.
