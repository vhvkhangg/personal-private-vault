# Active Implementation Handoff

- Handoff ID: `backend-phase-6-media-location`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: Backend Phase 6 Media + Location foundations

## Goal

Implement two independent Spring Modulith modules against frozen Schema v1: Media-owned Album/Image metadata and
Location-owned Brand/Address/Location/category/dining-style/business-hours capabilities. Preserve Vault and
Reference ownership; Media and Location must not depend on each other.

## Sources of truth

- `docs/implementation/phase-6/README.md` and `preparation-review.md` — approved behavior and test contract.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — fields, enums, constraints, and notes.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen module ownership and package direction.
- Scoped `media/AGENTS.md`, `location/AGENTS.md`, and `.agents/rules/backend-phase-6-media-location.md`.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/media/`, implement only `albums` and
  `images`: public Album/Image operations and immutable views, internal entities/services/repositories, and
  meaningful `package-info.java` files. Narrow the module descriptor to the Vault named interfaces actually
  used (expected `vault::entry/enums/view`); remove `internal/.gitkeep` when code exists.
- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/location/`, implement only `brands`,
  `location_categories`, `addresses`, `locations`, `location_category_assignments`,
  `location_dining_service_styles`, and `location_business_hours`. Expose capability-oriented Brand, Address,
  Category, Location, and Hours operations with immutable views; keep JPA and repositories internal. Narrow
  dependencies to the Vault and Reference named interfaces actually used (expected
  `vault::entry/enums/view` and `reference::catalog/view`); remove `internal/.gitkeep` when code exists.
- Provide create/update/find-by-ID where approved, parent-scoped category/dining/hours reads, and bounded or
  paginated Image-by-Album reads. No global unbounded list/search or cross-module entity/repository access.

## Required behavior / invariants

- Album, Image, Brand, and Location each create their own Vault Entry (`ALBUM`, `IMAGE`, `BRAND`, `LOCATION`) in
  the same transaction as the subtype row; IDs match and failures leave no orphan entry. Vault alone owns
  favorite/rating/tag/recycle behavior.
- Media validates optional Image `album_id` internally. Respect required/unique `object_key`, optional unique
  `checksum_sha256`, nonnegative size, positive optional dimensions, and other Schema v1 fields. Sequential
  and concurrent object-key/checksum duplicates return stable Media-domain conflicts, not idempotent reuse or
  raw persistence errors. `location_text` stays free text; no Location dependency.
- `image_count` is derived, never stored; use a bounded database count/projection rather than loading all
  Images. Store metadata and object key only—no binary/object-storage I/O or existence check.
- Location validates Address Country and optional Brand nationality/currencies through public Reference
  contracts; Location requires an existing Address and may reference an existing Brand. Brand/Location names
  remain nonunique. Price rules exactly match Schema v1: nonnegative values, min ≤ max when both present,
  currency required when any price exists, and currency-only state allowed.
- Location Category names respect the case-insensitive unique index, including concurrent conflicts.
  Category and dining-style assignments are idempotent sets under concurrent duplicate adds. Dining styles
  are `A_LA_CARTE` and `BUFFET`; do not infer a category-name prerequisite.
- Business hours distinguish unknown (`business_hours_known=false`, no rows), known/closed weekday (zero rows),
  split intervals, and overnight intervals. Replace one Location's entire schedule atomically and serialize
  concurrent replacements per Location; derive positive per-day sequence from input order. Unknown input
  must have no intervals and clears old rows. Do not invent overlap validation.

## Non-goals

No Flyway/DBML/schema or frozen-module changes; no Media↔Location dependency; no image upload/download/delete,
object-storage SDK/provider, thumbnails, coordinates/geocoding/maps, global search/list, aggregate deletion,
REST/controllers/OpenAPI, frontend, or generic CRUD/money/address framework.

## Test/evidence contract

- JUnit 5 and PostgreSQL Testcontainers cover all nine owned tables, rollback for all four Vault-backed
  aggregates, Image validation and unique-key races, derived count without load-all, Reference/price/address
  validation, Category name races, idempotent category/dining sets, and unknown/closed/split/overnight hours.
  Test competing schedule replacements for atomicity/serialization. Race tests must observe PostgreSQL
  contention rather than infer it from timing-only sleeps. Do not use H2.
- Extend Spring Modulith tests for exact named-interface dependencies, no Media↔Location dependency, and no
  exposure of internal packages. Keep unchanged Flyway V1/Hibernate validation green.
- Final commands: `mvn -f backend/pom.xml clean verify` on Java 25, then `git diff --check`. Record commands,
  exits, environment, totals, focused results, schema/Modulith verification, and known diagnostics in
  `docs/implementation/phase-6/test-evidence.md`.

## Constraints / risks

- Stop on a conflict with frozen Schema v1 or module boundaries; do not silently change the baseline. Keep
  transaction boundaries in application services and avoid check-then-insert as sole uniqueness protection.
  Schedule replacement must not expose a mixed state under concurrent writers. Do not add a storage SDK merely
  because the schema contains `object_key`.
- Relevant skills: `media-domain-modeling`, `location-domain-modeling`, `java-spring-coding-standards`,
  `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection`,
  `modular-monolith-architecture`, `jpa-postgresql-persistence`, and `backend-testing`.
- Agents do not commit, push, tag, or create/merge PRs. After Phase 6 final review and owner commit/push,
  ChatGPT closes/freezes Phase 6; `$codex-milestone-review` for Phases 4–6 is required before Phase 7.

## Implementation result

Backend Phase 6 (`media` and `location`) implementation and test suite have been completed:
1. **Media module (`com.vhvkhangg.personalprivatevault.media`):**
   - Implemented `media.album` (`AlbumOperations`, commands, exceptions, `@NamedInterface("album")`).
   - Implemented `media.image` (`ImageOperations`, commands, `ImageConflictException`, exceptions, `@NamedInterface("image")`).
   - Implemented `media.view` (`AlbumView`, `ImageView`, `@NamedInterface("view")`).
   - Internal JPA entities `Album` and `Image` (`Persistable<Long>` sharing IDs with `vault_entries` type `ALBUM` and `IMAGE`).
   - Unpersisted derived album `image_count` via count query without loading images.
   - Stable domain race conflict translation for `object_key` and optional `checksum_sha256` duplicate inserts.
   - Module descriptor narrowed to `vault::entry`, `vault::enums`, `vault::view`. Removed `internal/.gitkeep`.
2. **Location module (`com.vhvkhangg.personalprivatevault.location`):**
   - Implemented `location.brand` (`BrandOperations`, commands, exceptions, `@NamedInterface("brand")`).
   - Implemented `location.address` (`AddressOperations`, commands, exceptions, `@NamedInterface("address")`).
   - Implemented `location.category` (`LocationCategoryOperations`, commands, exceptions, `@NamedInterface("category")`).
   - Implemented `location.location` (`LocationOperations`, commands, exceptions, `@NamedInterface("location")`).
   - Implemented `location.hours` (`BusinessHoursOperations`, commands, inputs, exceptions, `@NamedInterface("hours")`).
   - Implemented `location.enums` (`DiningServiceStyle`, `DayOfWeek`, `@NamedInterface("enums")`).
   - Implemented `location.view` (`AddressView`, `BrandView`, `BusinessHoursIntervalView`, `BusinessHoursScheduleView`, `LocationCategoryView`, `LocationView`, `@NamedInterface("view")`).
   - Internal entities for `Brand`, `Address`, `Location`, `LocationCategory`, `LocationCategoryAssignment`, `LocationDiningServiceStyleAssignment`, `LocationBusinessHour`.
   - Brand and Location backed by Vault entries (`BRAND`, `LOCATION`) with transactional rollback safety.
   - Reference catalog validation for Address country, Brand nationality, and Brand/Location currencies.
   - Exact Schema v1 price constraints (nonnegative, min <= max, currency required if any price present, currency-only allowed).
   - Case-insensitive unique Category name race conflict translation to `LocationCategoryNameAlreadyExistsException`.
   - Idempotent category and dining service style set assignments under concurrent duplicate inserts.
   - Business hours unknown/known/closed/split/overnight semantics with atomic per-Location replacement serialized via pessimistic write lock.
   - Module descriptor narrowed to `vault::entry`, `vault::enums`, `vault::view`, `reference::catalog`, `reference::view`. Removed `internal/.gitkeep`.
3. **Architecture and Modulith isolation:**
    - 12 architecture tests in `ApplicationArchitectureTests` verify exact named interfaces, internal encapsulation, and zero cross-module dependency between `media` and `location`.
4. **Remediation of Codex Review Findings (2026-09-29):**
   - **Finding 1 (Category update race):** `LocationCategoryService.update` uses `saveAndFlush` and translates `uq_ci_location_categories_name` constraint violations into `LocationCategoryNameAlreadyExistsException`.
   - **Finding 2 (Coherent business-hours reads):** `BusinessHoursService.getSchedule` acquired `@Lock(LockModeType.PESSIMISTIC_READ)` via `findByIdForShare(locationId)` under `@Transactional`, serializing with concurrent replacements.
   - **Finding 3 (Non-sensitive image fallback):** `ImageService.create` identifies `images_object_key_key` and `images_checksum_sha256_key` and returns sanitized `"Image metadata conflict occurred during creation"` fallback for other integrity violations without leaking raw SQL or message strings.
   - **Finding 4 (Concurrent checksum contention test):** Added `MediaIntegrationTest.concurrentDuplicateChecksumThrowsDomainConflict` using `awaitCompetingLock` to test `images_checksum_sha256_key` PostgreSQL lock contention and assert losing Vault entry rollback.
   - **Finding 5 (Internal package descriptors):** Added meaningful `package-info.java` files for all six internal packages in `media` and `location`.
5. **Testing and Verification:**
   - Unit tests: `MediaValidationTest` (23 tests), `LocationValidationTest` (32 tests).
   - PostgreSQL Testcontainers integration tests: `MediaIntegrationTest` (10 tests), `LocationIntegrationTest` (16 tests) with observable PostgreSQL contention (`awaitCompetingLock`).
   - Verification commands:
     - `mvn -f backend/pom.xml clean verify` passed with 458 tests run, 0 failures, 0 errors, 0 skipped (`BUILD SUCCESS`).
     - `git diff --check` passed with 0 warnings/errors.
   - Detailed test evidence recorded in `docs/implementation/phase-6/test-evidence.md`.

## Codex remediation

Remediation of all five findings from [the 2026-09-29 final review](../phase-6/reviews/2026-09-29-phase-6-final-codex-review.md) is complete. Independent Codex re-review is [READY FOR OWNER COMMIT](../phase-6/reviews/2026-09-29-phase-6-final-codex-rereview.md).

## Final review

`READY_FOR_OWNER_COMMIT` on 2026-09-29. Suggested commit message: `feat(backend): implement phase 6 media and location foundations`. Owner commits/pushes; agents do not.
