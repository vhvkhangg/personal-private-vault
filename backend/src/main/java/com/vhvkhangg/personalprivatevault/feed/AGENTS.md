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
