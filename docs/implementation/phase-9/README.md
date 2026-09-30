# Backend Phase 9 — Collection Foundation

Status: **COMPLETE — FROZEN (2026-09-30)**

Preparation: **READY FOR HANDOFF** after the [2026-09-30 Codex re-review](reviews/2026-09-30-phase-9-pre-handoff-codex-rereview.md); owner committed/pushed it as `74c1ec1`.

Implementation final acceptance re-review: **READY_FOR_OWNER_COMMIT**. The owner subsequently committed/pushed Phase 9 on 2026-09-30.

Phase 9 implements the top-level `collection` facade and its three nested Spring Modulith modules:

- `collection.music`
- `collection.shopping`
- `collection.software`

Phase 9 implementation is complete, final-reviewed, committed/pushed by the owner, and frozen.

The preparation and implementation contracts below are retained as historical records and do not authorize further
Phase 9 production changes.

Phase 9 **is a milestone phase**. The Phase **7–9** [milestone review](milestone-review.md) returned
`CHANGES_REQUESTED`; the owner-approved maintenance [handoff](../handoffs/ACTIVE.md) is `READY_FOR_OWNER_COMMIT` after final acceptance.
Antigravity implementation/testing is next. Phase 10 pre-handoff review remains blocked.

## Owned Schema v1 tables

### Music

- `music_tracks`
- `music_track_people`

### Shopping

- `shopping_items`

### Software

- `software_items`
- `software_item_platforms`

No DBML/Flyway/schema change is planned.

## Architecture and nested-module boundary

`collection` is a closed parent facade. External top-level modules use the parent Collection API and do not access
nested repositories/internals directly.

Frozen whole-module direction:

```text
collection -> vault, people, reference
```

Nested responsibilities:

- `music` owns Music persistence and Person credit assignments;
- `shopping` owns Shopping persistence and wishlist/purchase state;
- `software` owns Software persistence and supported-platform assignments;
- parent `collection` owns only stable facade/mapping contracts for future top-level callers such as Search.

Nested modules own validation, transactions, and persistence. The parent facade must not become a second service or
persistence layer.

### Externally consumable parent API boundary

Follow the closed-facade pattern proven by Phase 8 Knowledge.

Preferred direction:

```text
collection/
├── package-info.java
├── api/
│   ├── package-info.java          # parent-owned @NamedInterface("api")
│   └── <small facade contracts + parent-owned immutable API types>
├── internal/
│   └── application/               # mapping/delegation only
├── music/
├── shopping/
└── software/
```

Mandatory rules:

- externally consumable parent method signatures use parent-owned `collection` API types only;
- top-level callers must not need imports from `collection.music.*`, `collection.shopping.*`, or
  `collection.software.*`;
- nested public commands/views may be used by the parent internally but must not leak through external facade
  signatures;
- parent mapping must not duplicate nested validation/business rules;
- do not make the parent module open merely to bypass nested boundaries.

The handoff must add an architecture/compile-time regression with a representative top-level external consumer
(prefer a test fixture representing future `search`) that compiles/architecturally verifies using only parent
Collection API imports. Do not implement Phase 12 Search behavior merely to prove the boundary.

## Shared Vault identity

These rows are Vault Entry-backed with the same ID:

| Collection type | Vault entry type |
| --- | --- |
| `music_tracks` | `MUSIC` |
| `shopping_items` | `SHOPPING` |
| `software_items` | `SOFTWARE` |

Create each Vault Entry and domain row in one transaction. Failure must leave no orphan Vault Entry.

Favorite/rating/tag/recycle behavior remains Vault-owned. `MUSIC`, `SHOPPING`, and `SOFTWARE` already support those
metadata capabilities; Collection must not duplicate the Vault capability matrix.

`music_track_people` and `software_item_platforms` are assignment tables, not Vault Entries.


## Exact Phase 9 operation surface

Phase 9 is a foundation CRUD/capability slice with an intentionally small public surface.

### Music

Required capabilities:

```text
createMusic(command)
updateMusic(id, command)
findMusicById(id)
addMusicCredit(musicId, personId, role)
findMusicCredits(musicId, limit)
```

### Shopping

Required capabilities:

```text
createShoppingItem(command)
updateShoppingItem(id, command)
findShoppingItemById(id)
```

### Software

Required capabilities:

```text
createSoftwareItem(command)
updateSoftwareItem(id, command)
findSoftwareItemById(id)
addSoftwarePlatform(softwareId, platformId)
findSoftwarePlatforms(softwareId, limit)
```

Exact Java names may follow existing repository conventions, but the semantic surface must not expand beyond these
operations without owner approval.

### Create/update command semantics

Phase 9 uses **full replacement semantics for scalar fields**, not PATCH/tri-state semantics.

- Music create: null/omitted `version` resolves to Schema v1 default `ORIGINAL`.
- Music update: null/omitted `version` also resolves to `ORIGINAL`; null never means "leave unchanged".
- Shopping create: null/omitted `status` resolves to Schema v1 default `WISHLIST`.
- Shopping update: null/omitted `status` also resolves to `WISHLIST`; null never means "leave unchanged".
- Required non-defaulted fields (`Music.title`, `Shopping.name`, `Software.name`, `Software.type`) must be supplied
  with valid values; the service must not invent them.
- Nullable scalar fields are replacement values: null clears the existing value wherever Schema v1 permits null.
- Software has no defaulted domain enum; `type` is required on create and update.

Normalize defaults in the domain/service layer rather than relying on ORM omission to trigger database defaults, so
unit and PostgreSQL-backed behavior remain identical.

Music/Software scalar updates do not implicitly add, remove, or replace assignment rows.

### Assignment removal

Assignment removal is explicitly **out of scope** for Phase 9:

- no Music-credit removal API;
- no Software-platform removal API;
- no replace-all assignment command hidden inside Music/Software update.

Phase 9 exposes idempotent add + bounded read only. Removal/replacement requires a later owner-approved feature.

## Music contract

`music_tracks` stores:

- required nonblank title;
- version: `ORIGINAL`, `COVER`, `PARODY`, or `MIX`;
- optional Reference platform;
- optional URL.

If `platform_id` is present, validate it through public `ReferenceCatalog`. Schema v1 does not restrict platform
kind, so do not invent a PlatformKind rule.

Schema v1 defines no title/URL/platform/version uniqueness. Repeated create commands create distinct Music/Vault
records; do not deduplicate them.

### Music Person credits

`music_track_people` is a set keyed by `(music_id, person_id, role)` where role is `SINGER` or `ARTIST`.

Rules:

- validate Person through public `PersonOperations`;
- exact repeated assignment is idempotent;
- concurrent duplicate assignment converges to one row using a race-safe PostgreSQL set write;
- the same Person may hold both `SINGER` and `ARTIST` for the same Music item because `role` is part of the key;
- Creator Groups are not supported by frozen Schema v1 for Music credits;
- raw persistence conflicts must not leak.

Credit reads are Music-scoped and deterministically ordered.

## Shopping contract

`shopping_items` is Vault-backed `SHOPPING` content.

Required:

- nonblank name;
- status: `WISHLIST` or `PURCHASED`.

Optional fields include avatar URL, description, price, currency, Reference platform, URL, and purchase timestamp.

Frozen price/currency rules mean:

- price must be nonnegative;
- a non-null price requires currency;
- currency without price is allowed;
- currency/platform validate through public `ReferenceCatalog` when present;
- no PlatformKind restriction is invented.

### Purchase-state semantics

Frozen Schema v1 requires:

```text
status = 'PURCHASED' OR purchased_at IS NULL
```

Use this deterministic domain contract:

- `WISHLIST` requires `purchased_at = null`; reject an invalid command instead of silently discarding the timestamp;
- `PURCHASED` permits either null or non-null `purchased_at`;
- do not auto-populate `purchased_at = now()`;
- a transition back to `WISHLIST` must clear the timestamp in the requested/resulting state;
- a transition to `PURCHASED` may keep it null or use an explicitly supplied timestamp.

Schema v1 defines no name/URL/platform uniqueness. Do not deduplicate Shopping items.

## Software contract

`software_items` is Vault-backed `SOFTWARE` content.

Required:

- nonblank name;
- type: `APPLICATION` or `EXTENSION`.

Optional fields include logo URL, description, price, currency, URL, and review.

Price/currency semantics match Shopping: price is nonnegative, price requires currency, and currency without price is
allowed.

Schema v1 defines no name/URL/type uniqueness. Repeated creates are distinct Software/Vault records.

### Supported platforms

`software_item_platforms` is a set keyed by `(software_id, platform_id)`.

Rules:

- validate platform through public `ReferenceCatalog`;
- zero, one, or many platforms are allowed;
- exact repeated assignment is idempotent;
- concurrent duplicate assignment converges to one row with a race-safe PostgreSQL set write;
- no PlatformKind restriction is invented;
- reads are Software-scoped and deterministically ordered.

Do not collapse the many-to-many platform set into one field.

## Assignment concurrency strategy

Music-credit and Software-platform assignments are **set semantics**, not domain-conflict semantics.

Prefer an atomic PostgreSQL write such as `INSERT ... ON CONFLICT DO NOTHING` (or an equally race-safe approach)
against the frozen composite primary key. A check-then-insert sequence must not be the final race arbiter.

Tests must cover sequential and real concurrent duplicate assignments and prove one-row convergence. If the atomic
set-write path intentionally produces no constraint exception, do not invent a privacy-log conflict test merely for
symmetry.

## Bounded public reads

All Collection **collection-valued** reads require an explicit positive `limit`; reject `limit <= 0`.

Phase 9 requires these assignment reads:

```text
findMusicCredits(musicId, limit)
findSoftwarePlatforms(softwareId, limit)
```

Deterministic ordering is fixed as:

- Music credits: `person_id ASC`, then `role ASC` using the frozen PostgreSQL enum order (`SINGER`, then `ARTIST`);
- Software platforms: `platform_id ASC`.

Each method returns at most `limit` rows. Do not fall back to an unbounded parent-scoped read.

Single-item reads remain naturally bounded:

- Music by ID;
- Shopping by ID;
- Software by ID.

Phase 9 does not require browse/recent/status/type list APIs. If a future approved slice adds them, they must define
their own positive bound and deterministic ordering.

Do not add global unbounded lists or global search. Global search belongs to Phase 12.

## Proposed package direction

```text
collection/
├── package-info.java
├── api/
│   ├── package-info.java
│   └── <parent facade contracts + immutable parent API types>
├── internal/
│   └── application/
├── music/
│   ├── package-info.java
│   ├── <public Music capability/view/enum packages>
│   └── internal/
├── shopping/
│   ├── package-info.java
│   ├── <public Shopping capability/view/enum packages>
│   └── internal/
└── software/
    ├── package-info.java
    ├── <public Software capability/view/enum packages>
    └── internal/
```

Remove existing `.gitkeep` files as real implementation content is added.

Do not create generic CRUD bases, a cross-nested `common` package, `Service` / `ServiceImpl` pairs, placeholder
events, or speculative strategy/factory hierarchies.

## Expected cross-module contracts

The handoff must derive exact Spring Modulith named-interface dependencies from real imports.

Expected needs:

### Music

- Vault entry/enums/view;
- People person/view;
- Reference catalog/view.

### Shopping

- Vault entry/enums/view;
- Reference catalog/view.

### Software

- Vault entry/enums/view;
- Reference catalog/view.

The parent Collection facade depends on nested public contracts only, never nested internals/repositories. Codex must
verify the exact nested-module dependency declaration supported by Spring Modulith 2.1.1.

## Required invariants for the future handoff

- Music/Shopping/Software preserve transactional Vault identity;
- Vault metadata remains Vault-owned;
- Music create/update/find-by-ID are required; null/omitted `version` normalizes to `ORIGINAL` on create and update;
- Music optional platform and Person credits validate through public contracts;
- Music credit tuple duplicates are idempotent/race-safe, and one Person may have both credit roles;
- Music has no Creator Group credit path or invented natural-key uniqueness;
- Shopping create/update/find-by-ID are required; null/omitted `status` normalizes to `WISHLIST` on create and
  update; price/currency and purchase-state rules match frozen checks exactly;
- Shopping never auto-generates purchase timestamps;
- Software create/update/find-by-ID are required; `type` is required on both create and update;
- Software price/currency semantics match frozen checks;
- Software supported-platform assignments are idempotent/race-safe and may be empty or multiple;
- no nested DTO/internal/repository leaks through the external parent Collection API;
- public views are immutable;
- Music-credit reads require positive `limit` and `person_id ASC, role ASC`; Software-platform reads require
  positive `limit` and `platform_id ASC`;
- assignment removal/replace-all is deferred and scalar update does not mutate assignments;
- all collection reads remain bounded/parent-scoped.

## Testing contract for the future handoff

Require PostgreSQL Testcontainers coverage for:

- all five Collection-owned tables against unchanged Flyway V1/Hibernate validation;
- Vault identity/rollback for Music, Shopping, and Software;
- Music create/update/find-by-ID, including null/omitted `version -> ORIGINAL` on create and update, nullable-field
  clearing, platform validation, and allowed duplicate Music records;
- Music Person validation, both roles for one Person, exact-tuple idempotence, and deterministic concurrent duplicate
  assignment with one-row convergence;
- Music-credit reads reject non-positive limits, return at most `limit`, and order by `person_id ASC` then
  frozen `role ASC`;
- Shopping create/update/find-by-ID, including null/omitted `status -> WISHLIST` on create and update;
- Shopping nonnegative price, price/currency rule, nullable-field clearing, optional platform validation, and allowed
  duplicate items;
- full `WISHLIST`/`PURCHASED` plus `purchased_at` matrix, including no automatic timestamp creation;
- Software create/update/find-by-ID, required `type`, nullable-field clearing, price/currency validation, and allowed
  duplicate Software records;
- Software platform validation, zero/multiple platforms, repeated assignment idempotence, and deterministic concurrent
  duplicate assignment with one-row convergence;
- Software-platform reads reject non-positive limits, return at most `limit`, and order by `platform_id ASC`;
- API/behavior verification that no assignment-removal/replace-all operation exists in Phase 9 and scalar
  Music/Software updates leave assignment rows unchanged;
- parent Collection facade delegation across Music/Shopping/Software;
- representative external top-level consumer uses only parent `collection` API types and no nested package imports;
- exact parent/nested Spring Modulith boundaries and named-interface dependencies;
- bounded deterministic reads;
- no stale `.gitkeep` under implemented Collection packages;
- final `mvn -f backend/pom.xml -ntp clean verify` on Java 25;
- `git diff --check`.

Do not use H2. Concurrency tests must prove the competing PostgreSQL behavior rather than rely only on timing sleeps.

## Out of scope

- Phase 10 Feed/ImportData;
- Phase 11 Finance/Journal/Personal;
- Phase 12 global Search behavior;
- automatic music/product/software metadata fetching;
- marketplace/store API integration;
- OAuth, scraping, browser automation, schedulers;
- generic recommendations/ranking;
- REST/controllers/OpenAPI;
- frontend;
- RAG/embeddings/vector search;
- migrations/schema redesign;
- entity deletion/permanent-delete behavior unless separately approved;
- frozen Vault/People/Reference/Knowledge/Account production changes.

## Preparation tooling

Added for Phase 9:

- `.agents/skills/collection-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-9-collection.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/collection/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 9.


## Completion record

- Owner commit/push: completed 2026-09-30.
- Final Codex result: `READY_FOR_OWNER_COMMIT`.
- Final verification: `mvn -f backend/pom.xml -ntp clean verify` — **583 tests**, 0 failures/errors/skips.
- Collection verification: **36 Collection tests** across architecture, validation, and PostgreSQL integration.
- PostgreSQL: 18.6 via Testcontainers; Java 25; Maven 3.9.15.
- Spring Modulith/Flyway/Hibernate verification: passed.
- Final handoff archive: [`handoff.md`](handoff.md).
- Verification evidence: [`test-evidence.md`](test-evidence.md).
- Final acceptance re-review:
  [`reviews/2026-09-30-phase-9-final-codex-rereview.md`](reviews/2026-09-30-phase-9-final-codex-rereview.md).
- Phase status: **COMPLETE — FROZEN**.
- Milestone result: **CHANGES_REQUESTED**; see [`milestone-review.md`](milestone-review.md).
- Required next gate: owner commit/push of accepted maintenance, then Codex `$codex-milestone-review`.

Future Collection changes require a new owner-approved feature or maintenance slice. Do not reuse the completed
Phase 9 handoff.
