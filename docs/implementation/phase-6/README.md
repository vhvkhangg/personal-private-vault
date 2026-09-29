# Backend Phase 6 — Media + Location Foundations

Status: **READY FOR HANDOFF — AWAITING OWNER PREPARATION COMMIT/PUSH**

Phase 6 implements two independent top-level modules from frozen Schema v1:

- `media` — albums and image metadata;
- `location` — brands, addresses, places, categories, dining styles, and business hours.

The modules do not depend on each other. Phase 6 is intentionally one roadmap phase, but implementation must preserve
both module boundaries rather than creating a combined Java module.

No Phase 6 production implementation is authorized until:

1. this preparation passes `$codex-pre-handoff-review`;
2. the approved Phase 6 preparation slice is committed/pushed;
3. `$codex-create-handoff` creates an active Phase 6 implementation handoff.

Phase 6 **is a milestone phase**. After its implementation passes final review and the owner commits/pushes it,
ChatGPT closes/freezes Phase 6 and the owner must run `$codex-milestone-review` for Phases 4–6 before Phase 7 can
enter pre-handoff review.

## Frozen ownership

### Media

`media` owns:

- `albums`
- `images`

Allowed whole-module dependency: `vault` only.

### Location

`location` owns:

- `brands`
- `location_categories`
- `addresses`
- `locations`
- `location_category_assignments`
- `location_dining_service_styles`
- `location_business_hours`

Allowed whole-module dependencies:

- `vault`
- `reference`

No schema/Flyway change is planned.

---

# Media foundation

## Exact dependency direction

The future handoff should narrow the Media module descriptor to only the named interfaces actually required by the
implementation. Expected dependencies are:

- `vault::entry`
- `vault::enums`
- `vault::view`

Do not change `media/package-info.java` during preparation. Dependency narrowing is production work for the active
Phase 6 handoff.

## Album identity

An Album is Vault Entry-backed:

```text
albums.id == vault_entries.id
vault entry type == ALBUM
```

Album creation must keep Vault Entry and Album persistence in one transaction. Album title is required/nonblank.

Favorite/rating/tag/recycle behavior remains Vault-owned; Media must not duplicate Vault metadata rules.

## Image identity and metadata

An Image is also Vault Entry-backed:

```text
images.id == vault_entries.id
vault entry type == IMAGE
```

Image creation must keep Vault Entry and Image persistence in one transaction.

Schema v1 rules:

- `album_id` is optional;
- `object_key` is required and unique;
- `checksum_sha256` is optional and unique when present;
- `size_bytes` is null or `>= 0`;
- `width_px` / `height_px` are null or `> 0`;
- `captured_at` is optional `timestamptz`;
- `location_text` remains free text and does **not** create a dependency on the `location` module.

If `album_id` is supplied, validate it within Media before persistence.

### Object-key / checksum duplicate semantics

Creating an Image with a duplicate stored `object_key` is a **conflict**, not an idempotent create. A duplicate
non-null `checksum_sha256` is also a **conflict** because Schema v1 makes it unique.

Sequential and concurrent duplicates must surface stable Media-domain conflicts rather than raw JDBC/Hibernate
constraint errors. PostgreSQL uniqueness is the final race arbiter; correctness must not rely only on a precheck.

Do not invent content-addressed deduplication, automatic file merging, or checksum-based reuse semantics.

## Binary/object-storage boundary

Phase 6 persists metadata and `object_key` only. It does **not** implement S3/local upload, download, delete,
presigned URLs, bucket configuration, or object-existence checks.

Operational object-storage integration remains deferred to the later backend hardening/storage phase. Do not add an
SDK/provider merely because `images.object_key` exists.

## Derived album image count

`image_count` is intentionally not persisted. Album reads may expose a derived `imageCount`, but it must come from a
bounded database `COUNT(images)` query (or equivalent efficient projection), not from loading every Image row and
not from a new stored counter.

## Bounded Media reads

Phase 6 should expose bounded capabilities such as:

- Album by ID;
- Image by ID;
- Images for one Album using a bounded/paginated contract if multiple rows are returned;
- derived image count for one Album.

Do not add a global unbounded Album/Image list or global search API.

## Proposed Media package direction

```text
media/
├── album/
│   ├── package-info.java
│   └── AlbumOperations.java
├── image/
│   ├── package-info.java
│   └── ImageOperations.java
├── view/
│   ├── package-info.java
│   └── *View.java
└── internal/
    ├── application/
    ├── domain/
    └── infrastructure/persistence/
```

Commands/exceptions may live in child packages while remaining part of their parent logical named interface.

---

# Location foundation

## Exact dependency direction

The future handoff should narrow the Location module descriptor to only the named interfaces actually required by
concrete code. Expected dependencies are:

- `vault::entry`
- `vault::enums`
- `vault::view`
- `reference::catalog`
- `reference::view`

Do not add a Media dependency for `logo_url` / `image_url`; those are plain URLs in Schema v1.

Do not change `location/package-info.java` during preparation.

## Brand identity

A Brand is Vault Entry-backed:

```text
brands.id == vault_entries.id
vault entry type == BRAND
```

Brand creation must keep the Vault Entry and Brand row transactionally consistent.

Brand names are **not unique** in Schema v1. Do not invent deduplication by name.

Optional nationality validates through public Reference Country lookup. Price rules must match Schema v1 exactly:

- `min_price` and `max_price` are null or nonnegative;
- if both exist, `min_price <= max_price`;
- if either price exists, `currency_code` is required;
- when `currency_code` is supplied, validate it through public Reference Currency lookup;
- currency without prices is not prohibited by the frozen database constraint, so do not reject it solely for
  being present.

The free-text `review` field is Location-owned content and is distinct from Vault's global rating metadata.

## Address

`addresses` is Location-owned but is **not** a Vault Entry.

- `country_code` is required and validates through public Reference Country lookup;
- other address components are optional;
- no latitude/longitude fields are part of Schema v1;
- create/update/find-by-ID is sufficient for this foundation; do not add global address search/listing.

Addresses may be referenced by Locations; do not assume one-address-per-location uniqueness because the schema does
not impose it.

## Location identity

A Location is Vault Entry-backed:

```text
locations.id == vault_entries.id
vault entry type == LOCATION
```

Location creation must keep Vault Entry and Location persistence transactionally consistent.

A Location requires an existing Address and may optionally reference an existing Brand. Validate both within the
Location module; no cross-module repository is involved.

Location names are **not unique**. Do not deduplicate by name, Brand, or Address.

Location price/currency validation follows the same exact rules as Brand. Favorite/rating/tag/recycle behavior stays
canonical in Vault.

## Location categories

`location_categories` is Location-owned. Names are case-insensitively unique because Flyway V1 includes
`uq_ci_location_categories_name`.

- create/update/find operations remain bounded; do not add an unbounded public `findAll`;
- duplicate category name create/update is a stable domain conflict;
- concurrent conflicting writes must not leak raw persistence exceptions.

`location_category_assignments` is set behavior:

- repeated `(location_id, category_id)` add is idempotent;
- concurrent duplicate adds converge to one row;
- PostgreSQL composite-key uniqueness is the final race arbiter.

## Dining service styles

Frozen values:

- `A_LA_CARTE`
- `BUFFET`

A Location may have both. `location_dining_service_styles` is idempotent set behavior under sequential and concurrent
duplicate add.

Schema v1 does not encode which category names count as food/beverage venues. Do not invent a category-name check such
as requiring `Restaurant` or `Cafe` before assigning a dining style.

## Business hours

Business-hours semantics must follow the frozen note:

- `business_hours_known = false` means unknown/not entered;
- when `business_hours_known = true`, zero rows for a weekday means closed that weekday;
- multiple rows per weekday represent split schedules;
- `sequence > 0` and `(location_id, day_of_week, sequence)` is unique;
- `close_time` may be earlier than `open_time` for an overnight interval.

Use public `DayOfWeek` values matching the PostgreSQL enum.

The Phase 6 public contract should use one bounded, transactional schedule-replacement operation for a single
Location rather than expose raw child-row CRUD:

- input includes `businessHoursKnown` plus ordered intervals grouped by weekday;
- when `businessHoursKnown = false`, the submitted interval set must be empty and existing hour rows are cleared;
- when `businessHoursKnown = true`, an empty interval set for a weekday means closed;
- sequence values are derived per weekday from input order starting at 1 rather than supplied by callers;
- replacing a schedule must be atomic and serialized per Location (row lock or equivalent) so concurrent replacements
  cannot interleave into a mixed schedule;
- do not invent interval-overlap validation or reject overnight intervals because Schema v1 does not define those
  rules.

Schedule reads are parent-scoped to one Location.

## Bounded Location reads

Phase 6 should expose bounded capabilities such as:

- Brand by ID;
- Address by ID;
- Location by ID;
- Category by ID/name;
- categories/dining styles/business-hours for one Location.

Do not add global unbounded Brand/Location/Address lists or global search. Global search remains a later `search`
module responsibility.

## Proposed Location package direction

```text
location/
├── brand/
│   ├── package-info.java
│   └── BrandOperations.java
├── address/
│   ├── package-info.java
│   └── AddressOperations.java
├── category/
│   ├── package-info.java
│   └── LocationCategoryOperations.java
├── location/
│   ├── package-info.java
│   └── LocationOperations.java
├── hours/
│   ├── package-info.java
│   └── BusinessHoursOperations.java
├── view/
│   ├── package-info.java
│   └── *View.java
├── enums/
│   ├── package-info.java
│   └── DiningServiceStyle / DayOfWeek
└── internal/
    ├── application/
    ├── domain/
    └── infrastructure/persistence/
```

The active handoff may refine names where a package has no real responsibility. Avoid generic CRUD bases,
`Service` / `ServiceImpl`, generic money/address frameworks, or speculative events.

---

# Phase 6 testing contract

Use PostgreSQL Testcontainers. Require coverage for:

## Media

- Album/Vault creation rollback;
- Image/Vault creation rollback;
- optional Album reference validation;
- `object_key` uniqueness and optional checksum uniqueness, including deterministic PostgreSQL contention;
- size/width/height checks;
- derived Album image count without a persisted counter or load-all implementation;
- exact Spring Modulith dependencies and no cross-module internal access.

## Location

- Brand/Vault creation rollback;
- Location/Vault creation rollback;
- Address Country validation;
- Brand nationality and Brand/Location currency and price-range rules;
- required Address and optional Brand reference validation;
- case-insensitive Location Category uniqueness with deterministic PostgreSQL contention;
- idempotent category and dining-style assignment with deterministic PostgreSQL contention;
- business-hours unknown/known/closed/split/overnight semantics;
- atomic/serialized schedule replacement for competing writes;
- exact Spring Modulith dependencies and no Media dependency.

## Final verification

- unchanged Flyway V1/Hibernate validation for all nine Phase 6-owned tables;
- Spring Modulith verification;
- `mvn -f backend/pom.xml clean verify` on Java 25;
- `git diff --check`;
- evidence records exact command, environment, totals, focused results, and known warnings/diagnostics.

For database race tests, prove observable PostgreSQL contention (for example `pg_locks`/`pg_stat_activity` polling)
rather than using timing-only sleeps as the sole evidence.

Do not use H2.

## Out of scope

- actual image/media binary upload/download/delete;
- S3/object-storage SDK/provider/configuration;
- media transformations/thumbnails;
- coordinates/geocoding/maps;
- route/distance search;
- global search/list APIs;
- aggregate Brand/Location/Album/Image deletion/permanent-delete behavior;
- REST/controllers/OpenAPI;
- frontend;
- schema/Flyway/DBML changes;
- changes to frozen Vault/Reference/People/Fiction/Film production behavior.

## Preparation tooling

Added for Phase 6:

- `.agents/skills/media-domain-modeling/SKILL.md`
- `.agents/skills/location-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-6-media-location.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/media/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/location/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 6.
