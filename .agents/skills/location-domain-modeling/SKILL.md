---
name: location-domain-modeling
description: Guide Location-module Brands, Addresses, Vault-backed Locations, categories, dining styles, prices, Reference validation, and coherent business-hours scheduling.
---

# Location Domain Modeling

Use for Phase 6 Location planning, implementation, testing, or review.

## Ownership

`location` owns:

- brands;
- location categories;
- addresses;
- locations;
- category assignments;
- dining-service styles;
- business hours.

It may depend only on public Vault and Reference contracts.

## Vault identity

Brand and Location are separate Vault Entry-backed records:

- Brand → `VaultEntryType.BRAND`;
- Location → `VaultEntryType.LOCATION`.

Addresses/categories/hours are not Vault Entries. Vault owns favorite/rating/tag/recycle behavior for Brand/Location.

## References and prices

Validate Country/Currency through `ReferenceCatalog`.

Match frozen price checks exactly: nonnegative values, `min <= max` when both exist, and currency required whenever
any price exists. Do not reject currency-only state because the frozen schema permits it.

Brand and Location names are not unique; do not invent deduplication.

## Categories and dining styles

Location Category names are case-insensitively unique. Category assignment and Dining Service Style assignment are
idempotent sets with PostgreSQL uniqueness as final race arbiter.

Dining styles are `A_LA_CARTE` and `BUFFET`. Do not infer a category-name prerequisite for using them.

## Business hours

Preserve frozen semantics:

- unknown when `business_hours_known=false`;
- with known hours, zero intervals for a weekday means closed;
- multiple intervals allow split schedules;
- sequence is positive/unique per Location/day;
- overnight intervals are legal.

Prefer one parent-scoped atomic schedule replacement capability. When hours are unknown, intervals are empty and old
rows are cleared. Derive sequence from input order and serialize competing replacements per Location so schedules
cannot interleave. Do not invent overlap validation.

## No Media/geospatial coupling

`logo_url` / `image_url` remain strings. Do not depend on Media. Latitude/longitude, maps, geocoding, route/distance
logic are outside Schema v1/Phase 6.

## Public API / reads

Use capability-oriented Brand/Address/Location/Category/Hours operations and immutable views. Keep reads ID- or
parent-bounded. No global unbounded lists/search.

## Testing

Use PostgreSQL Testcontainers. Cover Vault rollback, Reference validation, prices, category uniqueness, idempotent
assignments under real contention, known/unknown/closed/split/overnight hours, atomic competing schedule replacement,
exact Modulith dependencies, and unchanged Flyway/Hibernate validation.
