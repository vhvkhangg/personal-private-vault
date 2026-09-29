# Active Implementation Handoff

- Handoff ID: `backend-phase-5-film`
- Created by: Codex
- Status: `COMPLETE_FROZEN`
- Owner commit/push: completed 2026-09-29
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: Backend Phase 5 Film foundation

## Goal

Implement the Film module against frozen Schema v1: Vault-backed Films and Film Credits, Film-owned genres,
shared narrative assignments, and parent-scoped links. Preserve the existing Vault, People, and Reference
boundaries without changing frozen behavior.

## Sources of truth

- `docs/implementation/phase-5/README.md` and `preparation-review.md` — approved scope and test contract.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — physical fields, enums, constraints.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — ownership and dependency direction.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/AGENTS.md` and
  `.agents/rules/backend-phase-5-film.md` — scoped implementation guidance.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/`, implement only `films`,
  `film_genres`, `film_genre_assignments`, `film_story_archetypes`, `film_world_settings`, `film_links`, and
  `film_credits`. Use capability-oriented public operations and immutable views with internal entities,
  services, and repositories. Add `package-info.java` to meaningful packages and remove `internal/.gitkeep`
  once real content exists. No JPA type crosses the public API.
- Narrow `film/package-info.java` to the exact named interfaces used. Expected contracts to verify are
  `vault::entry/enums/view`, `people::person/view`, and `reference::catalog/view`; do not add `people::group`,
  import another module's `internal` package, or inject its repository. Keep Modulith verification green.
- Provide Film create/update/find-by-ID, Film-genre create/update/find, Film-scoped genre/archetype/world-setting
  add/read, Film-link create/update/parent-scoped read, and Film-Credit create/find-by-ID/Film-scoped read.
  Validate inputs at the application boundary and return domain-level failures rather than raw persistence
  exceptions. Do not expose global unbounded list/search APIs.

## Required behavior / invariants

- Create a `FILM` Vault Entry in the same transaction as its Film row; `films.id == vault_entries.id`. Create
  a separate `FILM_CREDIT` Vault Entry in the same transaction as each credit row;
  `film_credits.id == vault_entries.id`. Failed creation rolls back both sides of either identity.
- Respect Schema v1 types, lengths, nullability, defaults, and enums. Film title is nonblank; required enums
  are nonnull; `total_episodes` is null or nonnegative. Do not invent a Movie-specific episode rule.
- Validate optional director and required credit Person IDs through public People operations, and optional
  nationality/language/classification IDs through public Reference operations. Director assignment must not
  mutate Person roles.
- Film-genre names respect the existing case-insensitive unique index, including races, without leaking raw
  constraint errors. Genre, story-archetype, and world-setting assignments are idempotent sets even under
  concurrent duplicate adds; PostgreSQL composite keys arbitrate races without poisoning caller transactions.
- Film links have no `link_type`; allow repeated URL/language/label and do not infer a single-primary rule.
  Link reads/updates stay scoped to their Film parent.
- Each successful Film-Credit create is a distinct record, including repeated Film/Person/role/character
  combinations; do not invent deduplication. Film Credit may be favorited but not rated/tagged. Vault owns and
  enforces those capabilities; Film must not duplicate its capability matrix.

## Non-goals

No Flyway/DBML/schema edits; no changes to frozen Vault, People, Reference, or Fiction behavior; no Film or
credit deletion/permanent-delete, global search/list, REST/controller/OpenAPI, frontend, media/location/account,
or generic CRUD/event framework. Do not add a new cross-module dependency.

## Test/evidence contract

- Focused JUnit 5 and PostgreSQL Testcontainers tests cover all seven tables, Film/Vault and Credit/Vault
  rollback, input and public-reference validation, genre case-insensitive conflict, classification set
  idempotency, Film-link duplicates/parent scoping, distinct credits and Film/Person validation, bounded
  reads, and Vault-owned favorite-only behavior for `FILM_CREDIT`. Race tests must observe PostgreSQL
  contention before releasing the holder transaction, not infer it from timing-only sleeps.
- Extend Spring Modulith architecture tests for exact named interfaces and no cross-module internal-package
  exposure. Keep unchanged Flyway V1/Hibernate validation green. Do not use H2.
- Final commands: `mvn -f backend/pom.xml clean verify` on Java 25, then `git diff --check`.
- Record exact commands/exits, environment, test totals, focused results, schema/Modulith verification, and
  known warnings in `docs/implementation/phase-5/test-evidence.md`.

## Constraints / risks

- If implementation conflicts with frozen Schema v1 or a module boundary, stop and report it; do not silently
  revise a frozen baseline. Avoid cross-module JPA associations, transaction self-invocation traps, and
  check-then-insert race handling. Reuse proven Fiction genre/set-write techniques where applicable without
  coupling Film to Fiction.
- Relevant skills: `film-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence`, and `backend-testing`.
- Agents do not commit, push, tag, or create/merge PRs.

## Implementation result

- **Status update:** Remediated and returned to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Implementation & Remediation summary:**
  - **Codex Remediation (Medium Evidence Finding):**
    - Corrected the `film_credits` column inventory in `docs/implementation/phase-5/test-evidence.md` to match exact physical Flyway V1 columns (`id`, `film_id`, `person_id`, `role`, `character_name`, `note`), removing the nonexistent `voice_profile`, `is_uncredited`, `notes`, and `created_at` claims.
    - Audited adjacent schema claims across all 7 Film-owned tables against `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`.
    - Added a concise Known Warnings & Diagnostics Summary distinguishing toolchain/compiler warnings (Lombok reflection on `sun.misc.Unsafe` on JDK 25, deprecation notices) and test-runtime diagnostics (Byte Buddy dynamic agent loading, SpringDoc default endpoint notifications, expected negative-test SQL constraint errors) from schema-validation and Flyway execution (which passed with zero errors or warnings). Explicitly noted that no IDE static code inspections were executed.
    - Re-ran full verification (`mvn -f backend/pom.xml clean verify`) and `git diff --check`.
  - **Public Contracts & Views:**
    - `com.vhvkhangg.personalprivatevault.film.enums` (`@NamedInterface("enums")`): `FilmFormat`, `FilmProductionStyle`, `FilmCreditRole`, `ProgressStatus`, `ConsumptionStatus`.
    - `com.vhvkhangg.personalprivatevault.film.view` (`@NamedInterface("view")`): `FilmView`, `FilmGenreView`, `FilmLinkView`, `FilmCreditView`, `FilmClassificationsView`.
    - `com.vhvkhangg.personalprivatevault.film.genre` (`@NamedInterface("genre")`): `FilmGenreOperations`, `CreateFilmGenreCommand`, `UpdateFilmGenreCommand`, domain exceptions (`FilmGenreNotFoundException`, `FilmGenreNameAlreadyExistsException`, `InvalidFilmGenreException`). Bounded operations only; no unbounded list/search.
    - `com.vhvkhangg.personalprivatevault.film.film` (`@NamedInterface("film")`): `FilmOperations`, `CreateFilmCommand`, `UpdateFilmCommand`, domain exceptions (`FilmNotFoundException`, `InvalidFilmException`).
    - `com.vhvkhangg.personalprivatevault.film.link` (`@NamedInterface("link")`): `FilmLinkOperations`, `CreateFilmLinkCommand`, `UpdateFilmLinkCommand`, domain exceptions (`FilmLinkNotFoundException`, `InvalidFilmLinkException`). Strictly parent-scoped to `film_id`.
    - `com.vhvkhangg.personalprivatevault.film.credit` (`@NamedInterface("credit")`): `FilmCreditOperations`, `CreateFilmCreditCommand`, domain exceptions (`FilmCreditNotFoundException`, `InvalidFilmCreditException`). Strictly parent-scoped to `film_id`.
  - **Internal Persistence & Services (`com.vhvkhangg.personalprivatevault.film.internal.*`):**
    - Entities: `Film`, `FilmGenre`, `FilmGenreAssignment` (+ `FilmGenreAssignmentId`), `FilmStoryArchetypeAssignment` (+ `FilmStoryArchetypeId`), `FilmWorldSettingAssignment` (+ `FilmWorldSettingId`), `FilmLink`, `FilmCredit`.
    - Native PostgreSQL enums mapped with `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` on `FilmFormat`, `FilmProductionStyle`, `ProgressStatus`, `ConsumptionStatus`, `FilmCreditRole`.
    - Shared Vault identity implemented via `Persistable<Long>` and `isNew()` for both `Film` and `FilmCredit`.
    - `film_links` correctly matches Schema v1: NO `link_type` column; supports repeated URL/language/label; parent-scoped reads/updates.
    - Repositories: `FilmRepository`, `FilmGenreRepository`, `FilmLinkRepository`, `FilmCreditRepository`, and native idempotent `@Modifying(flushAutomatically = true)` `insertIfAbsent` queries with `ON CONFLICT DO NOTHING` on `FilmGenreAssignmentRepository`, `FilmStoryArchetypeRepository`, and `FilmWorldSettingRepository`.
    - Application services: `FilmService`, `FilmGenreService`, `FilmLinkService`, `FilmCreditService`.
  - **Module Package Descriptor & Modulith Boundaries:**
    - `com.vhvkhangg.personalprivatevault.film.package-info.java` strictly narrowed to allowed dependencies: `vault::entry/enums/view`, `people::person/view` (explicitly excluding `people::group`), and `reference::catalog/view`.
    - Removed `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/.gitkeep`.
    - Added comprehensive Spring Modulith boundary and named interface verification in `ApplicationArchitectureTests` (7 checks total).
  - **Deterministic Concurrency & Invariants:**
    - Genre case-insensitive uniqueness conflict recovery (`concurrentDuplicateGenreCreationRecoversFromUniqueIndexConflict`) uses deterministic PostgreSQL lock polling via `awaitCompetingLock("film_genres", ...)` querying `pg_locks` (`NOT l.granted`) and `pg_stat_activity`, unblocking cleanly with `FilmGenreNameAlreadyExistsException` and leaving exactly 1 committed row.
    - Classification set idempotency across all three tables (`genreAssignmentIdempotencyAndDeterministicContention`, `storyArchetypeAssignmentIdempotencyAndDeterministicContention`, `worldSettingAssignmentIdempotencyAndDeterministicContention`) uses atomic native `INSERT ... ON CONFLICT DO NOTHING` in caller transactions (`REQUIRED`) with deterministic contention verification via `awaitCompetingLock`, eliminating timing sleeps.
    - Distinct-record semantics for credits: repeated identical parameters create separate distinct records.
    - Vault capability integration: verified `FILM_CREDIT` is favorite-only; rating and tagging are rejected by Vault.
    - Director reference validation: `director_person_id` validates via `PersonOperations.find`, and director assignment does not mutate Person roles.
    - Shared Vault rollback: transactional failures on Film or Film Credit creation roll back cleanly with zero orphan entries in `vault_entries`.
- **Verification Evidence:**
  - `mvn -f backend/pom.xml clean verify`: Exit status `0` (`BUILD SUCCESS` in 47.747 s), 372/372 tests passed across the repository (73 new Film domain tests, 2 new Modulith architecture tests).
  - `git diff --check`: Exit status `0` (clean, zero whitespace errors).
  - Graphify AST extraction: Refreshed cleanly (1764 nodes, 5854 edges, 177 communities).
  - Comprehensive evidence documented in `docs/implementation/phase-5/test-evidence.md`.
- **Constraints / Baselines Preserved:**
  - Zero modifications to Flyway migrations or DBML frozen Schema v1.
  - Zero JPA associations across module boundaries; no cross-module internal package imports.
  - Zero Git commits, pushes, or tags.

## Codex remediation

The one Medium evidence finding was resolved by correcting the Film Credit column inventory and documenting
observed build/runtime warnings without an IDE-clean claim. See the dated
`docs/implementation/phase-5/reviews/2026-09-29-phase-5-final-codex-review.md` for the review history.

## Final review

Initial Codex review: **CHANGES_REQUESTED** (one Medium evidence finding). Remediation re-review:
**READY_FOR_OWNER_COMMIT** (finding resolved; independent `clean verify` passed 372 tests and
`git diff --check` passed). Owner commits/pushes; agents do not.
