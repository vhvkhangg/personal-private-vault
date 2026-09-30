# Collection Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.collection`.

## Topology

Parent `collection` is a closed facade/mapping boundary.

Nested modules:

- `collection.music` — `music_tracks`, `music_track_people`
- `collection.shopping` — `shopping_items`
- `collection.software` — `software_items`, `software_item_platforms`

Nested modules own validation/persistence. Externally consumable parent methods use parent-owned Collection API types.

## Dependencies

Frozen top-level dependencies are `vault`, `people`, and `reference`.

Expected nested needs:

- Music → Vault + People + Reference public contracts;
- Shopping → Vault + Reference public contracts;
- Software → Vault + Reference public contracts.

Never import another module's or nested module's `internal` package/repository.

## Invariants

- Music/Shopping/Software IDs equal their Vault Entry IDs.
- Each domain requires create/update/find-by-ID; update is full scalar replacement, not PATCH.
- Null/omitted Music version -> `ORIGINAL`; null/omitted Shopping status -> `WISHLIST` on create and update;
  Software type remains required on both.
- Vault metadata remains Vault-owned.
- Music has no invented natural-key uniqueness.
- Music credits are Person-only exact tuple set semantics; both roles for one Person are allowed.
- Shopping price requires currency; currency without price is valid.
- `WISHLIST` has null `purchased_at`; `PURCHASED` may have null/non-null timestamp; never auto-generate it.
- Shopping has no invented natural-key uniqueness.
- Software price/currency follows the same rule and has no invented natural-key uniqueness.
- Software platforms are a zero-or-more idempotent/race-safe set with no PlatformKind restriction.
- Music credits and Software platforms expose add + bounded read only; removal/replace-all is outside Phase 9.
- Music-credit reads require positive limit and `person_id ASC, role ASC`; Software-platform reads require positive
  limit and `platform_id ASC`.
- Public contracts/views are immutable and contain no JPA entities/repositories.
- Collection reads are parent-scoped and bounded.
- Remove nested `.gitkeep` files when implementation replaces them.

## Guidance

Use:

- `collection-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Phase gate

Phase 9 production changes require:

1. Phase 9 preparation review = `READY FOR HANDOFF`;
2. an active approved Phase 9 handoff.

After Phase 9 owner commit/push, run the Phase 7–9 milestone review before Phase 10.
