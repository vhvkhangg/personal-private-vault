# Feed Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.feed`.

## Ownership

Feed owns:

- `feed_sources`
- `feed_items`
- `saved_resources`
- `saved_resource_conversions`

Only `saved_resources` is Vault-backed (`SAVED_RESOURCE`).

## Dependencies

Allowed top-level dependencies:

- `vault`
- `knowledge`

Use only public named interfaces / parent Knowledge API. Never import nested Knowledge packages or internals.

## Invariants

- FeedSource schedule checks/defaults match frozen Schema v1.
- Due-source reads are positive-bounded and deterministic.
- Live provider HTTP clients and scheduler runtime are outside Phase 10.
- Feed ingestion accepts normalized items and updates source fetch timestamps only after a successful atomic batch.
- FeedItem URL hash is SHA-256 of the trimmed stored URL; source external-ID/URL-hash ambiguity is rejected.
- JSON config/raw metadata never aliases managed state/public views.
- SavedResource ID equals its Vault Entry ID.
- SavedResource URL-hash duplicates are stable conflicts; losing Vault creation rolls back.
- Social saved resources remain metadata/link-only.
- Conversions create Study/Information/Note only through parent Knowledge and record provenance atomically.
- No Feed-to-Vocabulary conversion.
- Reads remain ID/source/resource-scoped and bounded.

## Guidance

Use:

- `feed-import-workflow-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Phase gate

Phase 10 production changes require:

1. Phase 10 preparation review = `READY FOR HANDOFF`;
2. an active approved Phase 10 handoff.

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

Phase 13 note: `ingestFetch` is not an HTTP endpoint; live/provider ingestion remains deferred.
