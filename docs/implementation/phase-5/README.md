# Backend Phase 5 — Film Foundation

Status: **READY FOR OWNER COMMIT**

Phase 5 implements the `film` module after the frozen `vault`, `reference`, `people`, and `fiction` foundations.

The preparation gate is complete:

1. this preparation passes `$codex-pre-handoff-review`;
2. the approved Phase 5 preparation slice is committed/pushed;
3. `$codex-create-handoff` creates an active Phase 5 implementation handoff.

The active contract is [`../handoffs/ACTIVE.md`](../handoffs/ACTIVE.md). Antigravity implements/tests only that
scope; this preparation document remains its approved reference.

The implementation and evidence remediation passed [Codex final review](reviews/2026-09-29-phase-5-final-codex-review.md).
Owner commit/push and Phase 5 closeout remain.

Phase 5 is **not** a milestone phase. The next cross-phase milestone occurs after Phase 6.

## Owned Schema v1 tables

- `films`
- `film_genres`
- `film_genre_assignments`
- `film_story_archetypes`
- `film_world_settings`
- `film_links`
- `film_credits`

No schema/Flyway change is planned.

## Frozen dependency direction

At whole-module architecture level, `film` may depend only on:

- `vault`
- `people`
- `reference`

The future handoff must narrow the Spring Modulith descriptor to only the named interfaces actually required by
the concrete implementation.

Expected named-interface dependencies to verify:

- `vault::entry`
- `vault::enums`
- `vault::view`
- `people::person`
- `people::view`
- `reference::catalog`
- `reference::view`

`people::group` is not expected in Phase 5 because Schema v1 models Film director/credits through Person IDs only.

Do not change `film/package-info.java` during preparation. Dependency narrowing is production work for the active
Phase 5 handoff.

## Film shared Vault identity

A Film is Vault Entry-backed:

```text
films.id == vault_entries.id
vault entry type == FILM
```

Create Film identity through public `VaultEntryOperations` and preserve Film/Vault transactional consistency.

Film favorite/rating/tag/recycle behavior remains owned by `vault`; do not duplicate it in Film.

## Film fields and state

Respect frozen Schema v1:

- `format`: `MOVIE` or `SERIES`;
- `production_style`: `LIVE_ACTION` or `ANIMATION`;
- `is_nsfw`: boolean;
- optional nationality;
- optional poster URL;
- optional director Person;
- optional total episodes (`>= 0`);
- progress status;
- consumption status;
- optional current-progress text;
- optional description/review.

Do not derive extra schema rules such as “MOVIE must have exactly one episode.” No such constraint exists in
Schema v1.

An optional `director_person_id` must resolve through public `PersonOperations`. Film must not automatically mutate
the Person's `DIRECTOR` role merely because a director is assigned.

## Film genres

`film_genres` is Film-owned.

- genre names follow the existing case-insensitive uniqueness index `uq_ci_film_genres_name`;
- create/update/find operations remain bounded; do not add an unbounded public `findAll`;
- a Film may have multiple genres through `film_genre_assignments`;
- repeated `(film_id, genre_id)` assignment is idempotent set behavior;
- PostgreSQL uniqueness is the final race arbiter and raw uniqueness errors must not leak.

The future handoff should reuse the proven Fiction genre/set-write approach where it fits rather than inventing a
second concurrency design.

## Shared narrative classifications

`story_archetypes` and `world_settings` remain Reference-owned.

A Film may have multiple assignments through:

- `film_story_archetypes`
- `film_world_settings`

Repeated assignment is idempotent set behavior. Validate referenced IDs through public `ReferenceCatalog` and use
PostgreSQL composite-key uniqueness as the final race arbiter.

Do not move shared Reference tables into Film.

## Film links

`film_links` supports Film-scoped external links with:

- optional `language_code`;
- optional label;
- URL;
- `is_primary`;
- created timestamp.

Planned behavior:

- create/read/update Film links;
- optional language validates through public `ReferenceCatalog`;
- reads are parent-scoped to one Film;
- no uniqueness constraint exists for URL/language/label combinations, so do not invent one;
- do not infer a stronger single-primary rule from `is_primary` alone.

Unlike Fiction links, Film links do **not** have `link_type` in Schema v1. Do not copy Fiction's `link_type`
contract into Film.

## Film credits

A Film Credit is its own Vault Entry-backed record:

```text
film_credits.id == vault_entries.id
vault entry type == FILM_CREDIT
```

Creating a credit must create the `FILM_CREDIT` Vault Entry and Film Credit row in one transaction. Failure must not
leave an orphan Vault Entry.

A credit references:

- one existing Film owned by this module;
- one existing Person validated through public `PersonOperations`;
- one required role: `MAIN`, `SUPPORTING`, `CAMEO`, `GUEST`, or `VOICE`;
- optional `character_name`;
- optional note.

Vault capability ownership remains unchanged:

- `FILM_CREDIT` may be favorited;
- `FILM_CREDIT` may **not** be rated;
- `FILM_CREDIT` may **not** be tagged.

Film must not duplicate those capability checks; they remain canonical in Vault.

### Credit duplicate semantics

Schema v1 intentionally has **no unique constraint** on `(film_id, person_id, role, character_name)` and explicitly
allows multiple credits for the same Film/Person when characters differ.

Therefore Phase 5 must not invent idempotent/deduplicating credit creation. Each successful credit-create operation
creates a distinct `FILM_CREDIT` Vault Entry and Film Credit row.

If exact-credit deduplication becomes a requirement later, it needs an explicit owner-approved rule and a
race-safe persistence design rather than a check-then-insert convention.

## Bounded public reads

Phase 5 should expose bounded reads such as:

- Film by Film ID;
- genre/classification assignments for one Film;
- links for one Film;
- Film Credit by credit ID;
- credits for one Film.

Do not add a global unbounded Film/credit list or global search API. Global search belongs to the later `search`
module.

## Proposed public package direction

```text
film/
├── film/
│   ├── package-info.java
│   └── FilmOperations.java
├── genre/
│   ├── package-info.java
│   └── FilmGenreOperations.java
├── link/
│   ├── package-info.java
│   └── FilmLinkOperations.java
├── credit/
│   ├── package-info.java
│   └── FilmCreditOperations.java
├── view/
│   ├── package-info.java
│   └── *View.java
├── enums/
│   ├── package-info.java
│   └── FilmFormat / FilmProductionStyle / FilmCreditRole / shared state enums as needed
└── internal/
    ├── application/
    ├── domain/
    └── infrastructure/persistence/
```

Commands/exceptions may use child packages while remaining part of the same logical named interface, following the
People/Fiction package organization already verified by Spring Modulith.

Do not introduce generic `Service` / `ServiceImpl`, CRUD base classes, event abstractions, or hierarchies merely for
symmetry.

## Required invariants for the future handoff

- Film/Vault Entry identity is transactionally consistent;
- Film Credit/Vault Entry identity is transactionally consistent;
- Film title is nonblank; required enums are nonnull;
- optional total episodes is nonnegative;
- optional nationality/language IDs validate through Reference public API;
- optional director and credit Person IDs validate through People public API;
- Film genre name uniqueness follows frozen case-insensitive PostgreSQL semantics;
- genre/archetype/world-setting assignment duplicates are idempotent, including real concurrent contention;
- Film links remain parent-scoped and do not gain unsupported uniqueness;
- credit creation remains distinct-record creation, not an idempotent set operation;
- public APIs expose immutable views, never JPA entities/repositories;
- no cross-module `internal` import or repository access.

## Testing contract for the future handoff

Require PostgreSQL Testcontainers coverage for:

- all seven Film-owned tables against unchanged Flyway V1/Hibernate validation;
- Film/Vault creation rollback;
- Film Credit/Vault creation rollback;
- Film input validation and `total_episodes >= 0`;
- optional director/nationality validation through public contracts;
- case-insensitive Film genre uniqueness, including deterministic PostgreSQL contention;
- idempotent Film genre/story-archetype/world-setting assignments with deterministic PostgreSQL contention;
- Film link persistence, parent scoping, and optional language validation;
- Film Credit Film/Person validation and bounded reads;
- `FILM_CREDIT` Vault integration showing favorite succeeds while rating/tagging remain rejected by Vault;
- exact Spring Modulith named-interface dependencies;
- final `mvn -f backend/pom.xml clean verify` on Java 25;
- `git diff --check`.

For race tests, use observable PostgreSQL contention (for example lock/activity polling) rather than timing-only
`sleep` as proof.

Do not use H2.

## Out of scope

- media/location/account/knowledge implementation;
- global search;
- REST/controllers/OpenAPI;
- frontend;
- migrations/schema redesign;
- automatic Person-role mutation from Film director/credit assignment;
- Film/credit deletion or permanent-delete behavior unless separately approved;
- changes to frozen Vault/People/Reference/Fiction production behavior.

## Preparation tooling

Added for Phase 5:

- `.agents/skills/film-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-5-film.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 5.
