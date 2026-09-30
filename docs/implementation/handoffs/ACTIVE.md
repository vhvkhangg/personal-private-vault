# Active Implementation Handoff

- Handoff ID: `backend-phase-9-collection`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: Backend Phase 9 Collection foundation

## Goal

Implement the closed parent `collection` facade and nested `music`, `shopping`, and `software` Spring Modulith
modules against unchanged Schema v1. Deliver Vault-backed identity, the approved scalar operations and assignment
sets, bounded reads, and PostgreSQL-backed concurrency behavior without introducing later Search or integration work.

## Sources of truth

- `docs/implementation/phase-9/README.md` and `preparation-review.md` — owner-approved operation, behavior, and test contract.
- `docs/implementation/phase-9/reviews/2026-09-30-phase-9-pre-handoff-codex-rereview.md` — closed preparation findings.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — exact tables, keys, enums, defaults, checks, and FKs.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen ownership, topology, and dependency direction.
- `.agents/rules/backend-phase-9-collection.md` and
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/collection/AGENTS.md` — scoped guidance.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/collection/`, implement exactly `music_tracks`,
  `music_track_people`, `shopping_items`, `software_items`, and `software_item_platforms` in their owning nested
  modules. Keep entities, repositories, transactions, and validation internal to the owner. Expose narrow nested
  capabilities with immutable commands/views, meaningful named interfaces and `package-info.java`; remove replaced
  Collection `internal/.gitkeep` placeholders.
- Implement a small closed parent `collection.api` facade that delegates/maps the approved operations. Every
  externally consumable signature, including generic and exception types, uses parent-owned Collection API types;
  no nested DTO, entity, repository, or `internal` type leaks. Do not duplicate nested business rules in the parent.
- Required surface: create/update/find-by-ID for Music, Shopping, and Software; add and bounded read for Music
  Person credits and Software supported platforms. No assignment removal/replace-all or browse/search API.
- Add focused tests under `backend/src/test/java/com/vhvkhangg/personalprivatevault/collection/` and evidence at
  `docs/implementation/phase-9/test-evidence.md`.

## Required behavior / invariants

- Music, Shopping, and Software rows share the ID and transaction of Vault Entries `MUSIC`, `SHOPPING`, and
  `SOFTWARE`; failed domain creation leaves no orphan. Vault alone owns favorite/rating/tag/recycle behavior.
- Create/update commands replace scalar state (not PATCH). Null/omitted Music `version` resolves to `ORIGINAL` and
  Shopping `status` to `WISHLIST` on both create and update; required non-defaulted fields must be supplied.
  Nullable fields clear with null. Software `type` is required. Scalar Music/Software updates leave assignment sets
  unchanged.
- Music permits duplicate title/URL/platform/version records; optional platform resolves through public Reference
  without PlatformKind filtering. Credits validate Person via public People contract and form the exact
  `(music_id, person_id, role)` set. `SINGER` and `ARTIST` may coexist for one Person; no Creator Group credit.
- Shopping permits duplicate names/URLs/platforms. Price is nonnegative, requires currency when present, and
  currency without price is valid; optional currency/platform resolve through public Reference without PlatformKind
  filtering. `WISHLIST` requires null `purchased_at`; `PURCHASED` permits null or supplied time. Reject invalid
  combinations, never auto-set the timestamp, and require an explicit null when transitioning back to `WISHLIST`.
- Software permits duplicate name/type/URL records. Apply the same price/currency rule. Supported platforms are a
  zero-or-more `(software_id, platform_id)` set validated through public Reference with no PlatformKind restriction.
- Exact repeated Music-credit and Software-platform adds are idempotent, including concurrent duplicates. Use an
  atomic PostgreSQL composite-key set write (for example `ON CONFLICT DO NOTHING`) or an equally race-safe method;
  a pre-check alone is insufficient. Do not leak raw persistence/vendor detail in expected outcomes or logs.
- Credit/platform reads require a positive explicit `limit`, return at most that many rows, and order respectively
  by `person_id ASC, role ASC` (native enum order `SINGER`, `ARTIST`) and `platform_id ASC`. Other reads remain
  ID-scoped. No unbounded collection read or read-side mutation.

## Non-goals

- No DBML/Flyway/frozen-foundation change; assignment removal/replace-all, global browse/search, Phase 10 Feed or
  ImportData, Phase 11 domains, Phase 12 Search behavior, metadata fetching, marketplace APIs, OAuth/scraping,
  REST/controllers/OpenAPI, frontend, RAG, deletion, generic CRUD bases, speculative adapters/events/strategies, or
  new custom agents/hooks. A test fixture may represent a future top-level Search caller but must not implement its
  workflow.

## Test/evidence contract

- PostgreSQL Testcontainers/JUnit 5 coverage for all five tables and unchanged Flyway/Hibernate validation; all
  three Vault identity/rollback paths; create/update/find and default/full-replacement semantics; nullable-field
  clearing; Reference/People validation; duplicate-allowed items; complete Shopping status/timestamp and
  price/currency matrices; both-role credits; zero/multiple Software platforms; sequential and concurrent exact
  assignment idempotence. Concurrent tests must observe competing PostgreSQL behavior, not rely on sleeps alone.
- Verify positive limits, at-most-limit results and exact ordering for both assignment reads; verify scalar updates
  preserve assignments and no removal/replace-all API exists. Keep expected uniqueness outcomes privacy-safe.
- Verify exact parent/nested Modulith dependencies and named interfaces, no internal-package access, and a
  representative external top-level test consumer importing only parent Collection API types. Derive the supported
  nested `allowedDependencies` declaration from Spring Modulith 2.1.1 and actual imports; do not open modules to
  bypass verification. Whole-module direction remains only `vault`, `people`, `reference`.
- Final commands: `mvn -f backend/pom.xml -ntp clean verify` on Java 25, then `git diff --check`. Record commands,
  exits, test totals, PostgreSQL/architecture and concurrency/privacy results, environment, and known diagnostics in
  `docs/implementation/phase-9/test-evidence.md`. Do not use H2.

## Constraints / risks

- Stop/report any conflict with frozen Schema v1 or module boundaries; do not silently change them. Keep
  validation/transactions in owning nested modules and avoid raw sensitive exception/SQL detail logging.
- Relevant skills: `collection-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence`, and `backend-testing`.
- Agents do not commit, push, tag, or create/merge PRs. Phase 9 is a milestone phase; after final acceptance and
  owner commit/push, run the Phase 7–9 `$codex-milestone-review` before Phase 10 pre-handoff review.

## Implementation result

### 1. Completed Delivery Summary

- **Removed placeholder files:** Deleted all four `.gitkeep` placeholder files (`collection/internal/.gitkeep`, `collection/music/internal/.gitkeep`, `collection/shopping/internal/.gitkeep`, `collection/software/internal/.gitkeep`).
- **Spring Modulith Boundaries:**
  - `collection.package-info`: Exposes `@NamedInterface("api")` containing `collection.api.*`. Declares allowed dependencies to `collection.music`, `collection.shopping`, `collection.software`.
  - `collection.music.package-info`: Exposes `@NamedInterface("music")` containing `collection.music.music.*`, `collection.music.enums.*`, `collection.music.view.*`. Declares allowed dependencies to `vault`, `people`, `reference`.
  - `collection.shopping.package-info`: Exposes `@NamedInterface("shopping")` containing `collection.shopping.shopping.*`, `collection.shopping.enums.*`, `collection.shopping.view.*`. Declares allowed dependencies to `vault`, `reference`.
  - `collection.software.package-info`: Exposes `@NamedInterface("software")` containing `collection.software.software.*`, `collection.software.enums.*`, `collection.software.view.*`. Declares allowed dependencies to `vault`, `reference`.
- **Music Module (`collection.music`):**
  - Enums: `MusicVersion` (`ORIGINAL`, `COVER`, `PARODY`, `MIX`), `MusicCreditRole` (`SINGER`, `ARTIST`).
  - Public Views & Commands: `MusicView`, `MusicCreditView`, `CreateMusicCommand`, `UpdateMusicCommand`, `MusicNotFoundException`, `InvalidMusicException`, `MusicOperations`.
  - Internal Domain & Persistence: `MusicTrack` (persisted to `music_tracks`, `Persistable<Long>` with `MUSIC` vault entry), `MusicTrackPerson` / `MusicTrackPersonId` composite key entity, `MusicTrackRepository`, `MusicTrackPersonRepository`.
  - Application Service: `MusicService` handling atomic Vault entry creation, default `version -> ORIGINAL`, scalar replacement, reference platform validation, `PersonOperations.find(personId)` validation, native PostgreSQL enum casting (`CAST(:role AS music_credit_role)`), atomic idempotence via `INSERT ... ON CONFLICT DO NOTHING`, and bounded credit queries ordered by `person_id ASC, role ASC`.
- **Shopping Module (`collection.shopping`):**
  - Enums: `ShoppingStatus` (`WISHLIST`, `PURCHASED`).
  - Public Views & Commands: `ShoppingItemView`, `CreateShoppingItemCommand`, `UpdateShoppingItemCommand`, `ShoppingItemNotFoundException`, `InvalidShoppingItemException`, `ShoppingOperations`.
  - Internal Domain & Persistence: `ShoppingItem` (persisted to `shopping_items`, `Persistable<Long>` with `SHOPPING` vault entry), `ShoppingItemRepository`.
  - Application Service: `ShoppingService` handling atomic Vault entry creation, default `status -> WISHLIST`, scalar replacement, reference platform validation, status matrix enforcement (`WISHLIST` forbids `purchased_at`, `PURCHASED` allows null or non-null timestamp), atomic price/currency validation (nonnegative price, price requires currency, currency without price is valid).
- **Software Module (`collection.software`):**
  - Enums: `SoftwareType` (`APPLICATION`, `EXTENSION`).
  - Public Views & Commands: `SoftwareItemView`, `SoftwarePlatformView`, `CreateSoftwareItemCommand`, `UpdateSoftwareItemCommand`, `SoftwareItemNotFoundException`, `InvalidSoftwareItemException`, `SoftwareOperations`.
  - Internal Domain & Persistence: `SoftwareItem` (persisted to `software_items`, `Persistable<Long>` with `SOFTWARE` vault entry), `SoftwareItemPlatform` / `SoftwareItemPlatformId` composite key entity, `SoftwareItemRepository`, `SoftwareItemPlatformRepository`.
  - Application Service: `SoftwareService` handling atomic Vault entry creation, mandatory `type` validation on create/update, scalar replacement, reference platform validation, atomic price/currency validation, atomic platform idempotence via `INSERT ... ON CONFLICT DO NOTHING`, and bounded platform queries ordered by `platform_id ASC`.
- **Parent Collection Facade (`collection.api` + `collection.internal.application`):**
  - Public API Types: `CollectionMusicVersion`, `CollectionMusicCreditRole`, `CollectionShoppingStatus`, `CollectionSoftwareType`, `CollectionMusicView`, `CollectionMusicCreditView`, `CollectionShoppingItemView`, `CollectionSoftwareItemView`, `CollectionSoftwarePlatformView`, `CreateCollectionMusicCommand`, `UpdateCollectionMusicCommand`, `CreateCollectionShoppingItemCommand`, `UpdateCollectionShoppingItemCommand`, `CreateCollectionSoftwareItemCommand`, `UpdateCollectionSoftwareItemCommand`, `CollectionNotFoundException`, `InvalidCollectionException`, `CollectionOperations`.
  - Internal Implementation: `CollectionService` delegating to nested module services with complete 1:1 command/view mapping and exception translation. Closed surface with zero leakage of nested module types or internal persistence models.

### 2. Test Verification

- **Search Test Consumer Fixture:** `SearchCollectionConsumerTestFixture.java` in `search` package consuming only `collection.api.*`.
- **Architecture Tests:** `CollectionArchitectureTests.java` (3 tests PASS) verifying Spring Modulith monolith architecture, parent facade named interface (`api`), and nested modules encapsulation.
- **Unit Validation Tests:** `CollectionValidationTest.java` (16 tests PASS) covering validation rules, boundaries, and parent facade delegation/exception mapping.
- **PostgreSQL Integration Tests:** `CollectionIntegrationTest.java` (17 tests PASS) executing against Testcontainers PostgreSQL 18.6:
  - Schema v1 table presence verification (`music_tracks`, `music_track_people`, `shopping_items`, `software_items`, `software_item_platforms`).
  - Atomic Vault Entry rollback for all 3 domain entities (`MUSIC`, `SHOPPING`, `SOFTWARE`).
  - CRUD, defaults, nullable clearing, and scalar replacement semantics.
  - Complete Shopping status/timestamp and price/currency matrices.
  - Music credit support for both roles (`SINGER`, `ARTIST`) on one person and bounded deterministic ordering (`person_id ASC, role ASC`).
  - Software platform support with bounded deterministic ordering (`platform_id ASC`).
  - True PostgreSQL lock contention testing (`awaitCompetingLock` observing `NOT l.granted`) for concurrent duplicate credit and platform insertions, converging safely to 1 row.
  - Preservation of assignment sets during scalar entity updates.
  - Parent facade end-to-end delegation and exception mapping.
- **Total Test Suite:** Full suite verification (`mvn -f backend/pom.xml -ntp clean verify`) passed with **583 tests** (0 failures, 0 errors, 0 skipped).
- **Hygiene & Graphify:** `git diff --check` passed cleanly with exit code 0; `scripts/refresh-graphify.ps1` completed successfully (3311 nodes, 11365 edges, 292 communities).
- **Evidence Reference:** Detailed breakdown documented in `docs/implementation/phase-9/test-evidence.md`.

### 3. Remediation Summary (2026-09-30)

- Addressed blocking finding in `docs/implementation/phase-9/reviews/2026-09-30-phase-9-final-codex-review.md`.
- Rebuilt the "Focused Collection Regression & Invariant Tests" table in `docs/implementation/phase-9/test-evidence.md` strictly from actual test source methods and Surefire XML reports (`CollectionArchitectureTests` [3], `CollectionValidationTest` [16], `CollectionIntegrationTest` [17]).
- Eliminated invented method names, accurately cross-checked all 36 test assertions against test sources and Surefire reports, and corrected false domain claims (removed nonexistent Music duration, confirmed currency without price is valid, and confirmed absence of PlatformKind restrictions). Verified all 583 tests passing cleanly.

## Codex remediation

The evidence-only finding in
`docs/implementation/phase-9/reviews/2026-09-30-phase-9-final-codex-review.md` was corrected and closed by
`docs/implementation/phase-9/reviews/2026-09-30-phase-9-final-codex-rereview.md`.

## Final review

`READY_FOR_OWNER_COMMIT` on 2026-09-30 after Codex re-review. Independent `mvn -f backend/pom.xml -ntp clean verify`
passed (583 tests, zero failures/errors/skips); `git diff --check` passed. Owner commit/push is next, followed by
ChatGPT Phase 9 closeout and the mandatory Phase 7–9 milestone review before Phase 10 pre-handoff review.
