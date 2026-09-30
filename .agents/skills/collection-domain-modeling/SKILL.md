---
name: collection-domain-modeling
description: Guide the Collection parent facade and nested Music, Shopping, and Software modules with Vault-backed identity, Person/Reference validation, purchase-state rules, and race-safe set assignments.
---

# Collection Domain Modeling

Use for Phase 9 Collection planning, implementation, testing, or review.

## Topology

`collection` is a closed parent facade with nested modules `music`, `shopping`, and `software`.

Nested modules own persistence/business rules. Externally consumable parent signatures use parent-owned Collection
API types; top-level callers must not import nested Collection command/view types.

## Vault identity

Music, Shopping, and Software are Vault Entry-backed with `MUSIC`, `SHOPPING`, and `SOFTWARE`. Create Vault identity
and owned row transactionally. Vault retains favorite/rating/tag/recycle ownership.

## Music

Music requires create/update/find-by-ID. Commands use full scalar replacement semantics: null/omitted `version`
normalizes to `ORIGINAL` on create and update, and nullable optional fields may be cleared with null.

Validate optional platform through Reference without a PlatformKind restriction. Do not deduplicate by title/URL/
platform/version.

Music Person credits are `(music_id, person_id, role)` set semantics with `SINGER`/`ARTIST`. Validate Person via the
public People contract. Exact duplicates are idempotent/race-safe, and one Person may hold both roles. Do not invent
Creator Group credits.

## Shopping

Price is nonnegative and requires currency; currency without price is allowed. Optional platform is Reference-
validated without a kind restriction.

Shopping requires create/update/find-by-ID. Null/omitted `status` normalizes to `WISHLIST` on create and update;
commands use full scalar replacement semantics.

`WISHLIST` requires null `purchased_at`; `PURCHASED` permits null/non-null `purchased_at`. Never invent automatic
purchase timestamps. Do not deduplicate by name/URL/platform.

## Software

Software requires create/update/find-by-ID. `type` is required on create and update; nullable optional fields may
be cleared with null. Type is `APPLICATION` or `EXTENSION`. Price/currency semantics match Shopping.

Supported platforms are a zero-or-more `(software_id, platform_id)` set. Validate Reference platforms, permit any
PlatformKind, and make exact duplicate assignments idempotent/race-safe.

Do not deduplicate Software by name/type/URL.

## Assignment API surface

Phase 9 exposes add + bounded read only for Music credits and Software platforms. Assignment removal and replace-all
are deferred. Scalar Music/Software updates must not mutate assignment rows.

Music-credit reads require positive `limit` and deterministic `person_id ASC`, then frozen `role ASC`.
Software-platform reads require positive `limit` and deterministic `platform_id ASC`. Return at most `limit` and
reject non-positive limits.

## Concurrency

For composite assignment sets, prefer atomic PostgreSQL `INSERT ... ON CONFLICT DO NOTHING` or an equally race-safe
approach. Avoid check-then-insert as the final arbiter.

## Parent API

External callers use parent-owned Collection API types only. Parent mapping may delegate to nested public contracts
but must not duplicate nested validation/persistence.

Add an architecture/compile-time regression for a representative top-level caller using only the parent API.

## Reads and tests

Keep reads ID-/parent-scoped and bounded. Assignment-list reads use the explicit positive limits and ordering above. Use PostgreSQL Testcontainers for Vault rollback, People/Reference
validation, purchase-state matrix, duplicate-allowed semantics, assignment contention/convergence, and exact
parent/nested Modulith boundaries.

Phase 9 is a milestone phase: after owner commit/push, Phase 7–9 `$codex-milestone-review` is mandatory.
