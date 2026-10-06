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

## Phase 14 object-storage exception

An accepted active Phase 14 handoff may extend frozen Media only for the S3-compatible binary workflow in
`docs/implementation/phase-14/README.md`:

- Media-owned storage port/infrastructure adapter;
- managed upload + compensation;
- binary download;
- storage configuration/health;
- focused tests and HTTP/OpenAPI updates.

Do not change existing Image metadata invariants, Vault trash semantics, schema/Flyway, hard-delete behavior, or
expose provider SDK types through public/domain/web DTO contracts.
