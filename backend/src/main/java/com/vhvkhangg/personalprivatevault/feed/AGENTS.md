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

## M10-12-1 maintenance exception

A Codex handoff created from
`docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md` may modify this frozen module
only for the Search SQL/query-folding and snippet-offset defect described there.

Do not use the maintenance handoff to change mutation behavior, ownership, public Search contracts, ranking, source
bounds, schema/index definitions, or unrelated code. The maintenance handoff is the temporary authority; otherwise
the frozen phase rules remain in force.
