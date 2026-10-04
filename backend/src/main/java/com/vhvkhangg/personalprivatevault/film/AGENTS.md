# Film Module Agent Instructions

Applies to `com.vhvkhangg.personalprivatevault.film`.

## Ownership

- `films`
- `film_genres`
- `film_genre_assignments`
- `film_story_archetypes`
- `film_world_settings`
- `film_links`
- `film_credits`

Shared `story_archetypes` and `world_settings` remain owned by `reference`.

## Dependencies

Whole-module architecture allows `vault`, `people`, and `reference`. Phase 5 implementation must narrow the Spring
Modulith descriptor to the named interfaces actually used.

Expected contracts:

- `vault::entry`
- `vault::enums`
- `vault::view`
- `people::person`
- `people::view`
- `reference::catalog`
- `reference::view`

Never import another module's `internal` packages or repositories.

## Guidance

Use:

- `film-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- Film ID equals its `FILM` Vault Entry ID.
- Film Credit ID equals its `FILM_CREDIT` Vault Entry ID.
- Film Credit favorite/rating/tag capability stays owned by Vault; Film must not duplicate it.
- Optional director validates through public People API and does not mutate Person roles.
- Film genre names follow frozen case-insensitive uniqueness.
- Film genre, story-archetype, and world-setting assignment is idempotent set behavior.
- Film links are parent-scoped and do not have a `link_type`.
- Film Credit creation is distinct-record creation; no unsupported dedupe rule.
- Public APIs expose capabilities/views, never JPA entities or repositories.
- Global search/listing and deletion are outside Phase 5 unless an approved handoff says otherwise.

## Phase gate

Phase 5 production changes require:

1. `docs/implementation/phase-5/preparation-review.md` = `READY FOR HANDOFF`;
2. an active approved Phase 5 handoff.

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
