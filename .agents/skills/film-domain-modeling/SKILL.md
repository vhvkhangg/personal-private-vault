---
name: film-domain-modeling
description: Guide Film-module Vault-backed films and credits, Film-owned genres, shared narrative classifications, links, Person references, and race-safe set semantics without persistence leakage.
---

# Film Domain Modeling

Use for Phase 5 Film planning, implementation, testing, or review.

## Ownership

`film` owns:

- `films`
- `film_genres`
- `film_genre_assignments`
- `film_story_archetypes`
- `film_world_settings`
- `film_links`
- `film_credits`

Shared `story_archetypes` and `world_settings` remain owned by `reference`.

## Dependencies

At implementation time narrow Film to the named interfaces actually used. Expected contracts are:

```text
vault::entry
vault::enums
vault::view
people::person
people::view
reference::catalog
reference::view
```

Do not depend on another module's repositories/entities/internal packages. `people::group` is not expected for the
frozen Film schema.

## Film Vault identity

A Film is backed by a Vault Entry with type `FILM` and the same ID. Create both in one transaction.

Favorite/rating/tag/recycle behavior remains Vault-owned.

## Director

`director_person_id` is optional. When present, validate the Person through public `PersonOperations`.

Do not automatically add or mutate a Person's `DIRECTOR` role as a side effect of Film writes.

## Film genres and classifications

Film genres are Film-owned and case-insensitively unique under Flyway V1.

Genre, story-archetype, and world-setting assignments are set semantics:

- duplicate add is idempotent;
- concurrent duplicate add converges to one row;
- PostgreSQL uniqueness is the final race arbiter;
- raw persistence uniqueness errors do not leak.

Reuse proven atomic `ON CONFLICT DO NOTHING` / transaction patterns where appropriate instead of inventing a new
concurrency abstraction.

## Links

Film links are parent-scoped and have no `link_type` column. Multiple links may share language/label/URL values.

Do not invent unsupported uniqueness or a single-primary invariant from `is_primary`.

## Film Credit Vault identity

Each Film Credit is a separate Vault Entry with type `FILM_CREDIT`.

Credit creation must keep the Vault Entry and `film_credits` row transactionally consistent.

Validate the Film through Film-owned state and Person through public `PersonOperations`.

`FILM_CREDIT` capability remains Vault-owned:

- favorite: allowed;
- rating: forbidden;
- tags: forbidden.

Do not duplicate the capability matrix in Film.

## Credit duplicate semantics

Schema v1 has no credit uniqueness constraint. Each successful credit-create call creates a distinct credit/Vault
identity. Do not implement check-then-insert deduplication.

## Reads

Use bounded reads: by Film ID, by credit ID, and parent-scoped genres/classifications/links/credits.

Do not add global Film/credit list/search APIs.

## Public API

Prefer semantic capabilities such as `FilmOperations`, `FilmGenreOperations`, `FilmLinkOperations`, and
`FilmCreditOperations`, plus immutable views and stable enums.

Commands/exceptions may be grouped in child packages under the same logical named interface. Do not expose JPA
entities/repositories or add `Service` / `ServiceImpl` pairs merely for convention.

## Testing

Use PostgreSQL Testcontainers and cover:

- Film/Vault rollback;
- Film Credit/Vault rollback;
- director/nationality/language public-contract validation;
- Film genre case-insensitive uniqueness;
- idempotent genre/archetype/world-setting assignment;
- deterministic concurrent database contention for uniqueness/set writes;
- parent-scoped Film links;
- credit Film/Person validation and bounded reads;
- Vault favorite-only capability for `FILM_CREDIT`;
- exact Spring Modulith named-interface boundaries;
- unchanged Flyway/Hibernate validation.
