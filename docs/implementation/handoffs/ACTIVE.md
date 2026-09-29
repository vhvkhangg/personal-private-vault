# Active Implementation Handoff

- Handoff ID: `backend-phase-4-fiction`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: Backend Phase 4 Fiction foundation

## Goal

Implement the Fiction module against frozen Schema v1: Vault Entry-backed fiction records, Fiction-owned genres,
shared narrative classifications, and fiction links. Preserve the existing Vault, People, and Reference boundaries.

## Sources of truth

- `docs/implementation/phase-4/README.md` and `preparation-review.md` — approved scope and test contract.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — physical fields and constraints.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen ownership and dependency direction.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/AGENTS.md` and
  `.agents/rules/backend-phase-4-fiction.md` — scoped implementation guidance.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/`, implement only the five Fiction-owned
  tables: `fictions`, `fiction_genres`, `fiction_story_archetypes`, `fiction_world_settings`, and `fiction_links`.
  Use capability-oriented public operations/immutable views and internal entities, services, and repositories;
  add `package-info.java` to meaningful new packages. The prepared `fiction/`, `genre/`, `link/`, `view/`, and
  `enums/` direction may be refined where a package has no real types. No JPA type crosses the public API.
- Narrow `fiction/package-info.java` from whole-module dependencies to the exact named interfaces actually used.
  Expected contracts to verify: `vault::entry/enums/view`, `people::person/group/view`, and
  `reference::catalog/view`. Do not import another module's `internal` package or repository; keep Modulith
  verification green.
- Provide create/update/find-by-ID Fiction operations, Fiction-genre create/update/find capability, and
  create/update plus parent-scoped read of links. Provide parent-scoped story-archetype/world-setting assignment
  and reads. Keep reads bounded; no global fiction list/search. Validate inputs at the application boundary and
  return domain-level failures rather than raw persistence exceptions.

## Required behavior / invariants

- Create Fiction identity through `VaultEntryOperations.create(FICTION)` in the same transaction as the Fiction
  insert; `fictions.id == vault_entries.id`. Failed creation rolls back both. Vault alone owns favorite, rating,
  tag, trash, and restore behavior.
- Exactly one of `author_person_id` and `author_group_id` is set on create/update. Validate the chosen ID through
  public `PersonOperations.find` or `CreatorGroupOperations.find`; preserve the database XOR and foreign keys.
- Every Fiction has one existing Fiction-owned genre. Respect Schema v1 field types, sizes, nullability, defaults,
  and enums, including `total_chapters == null || total_chapters >= 0`. Genre-name conflicts must respect the
  existing case-insensitive unique index, including races, without leaking raw constraint errors.
- Validate optional nationality, story-archetype, world-setting, and link-language references through public
  `ReferenceCatalog` lookups. Repeated classification assignment is idempotent even under simultaneous adds;
  PostgreSQL composite keys arbitrate races without poisoning the caller transaction or leaking raw uniqueness
  failures. Do not modify Reference-owned catalogs.
- `fiction_links.link_type` remains a bounded free-form string, not a new enum. Persist `is_primary` as a boolean
  without inventing one-primary-per-fiction or language/type uniqueness. Multiple links with the same
  language/type must remain valid; update/read operations must stay scoped to their Fiction parent.

## Non-goals

No Flyway/DBML/schema edits; no changes to frozen Vault, People, or Reference behavior; no chapter/content
hosting, deletion/permanent-delete, global search/list, REST/controller/OpenAPI, frontend, film/media, RAG,
multi-user support, or speculative generic CRUD/pattern framework.

## Test/evidence contract

- Focused JUnit 5 and PostgreSQL Testcontainers tests cover Fiction/Vault rollback, author XOR and missing author,
  required genre and case-insensitive conflicts, optional/invalid reference IDs, classification idempotency and
  concurrent duplicates, chapter bound, link persistence/duplicate language-type and language validation,
  parent-scoped reads/updates, and unchanged Flyway/Hibernate validation. Extend Spring Modulith architecture
  tests for the exact named interfaces and lack of internal-package exposure. Do not use H2.
- Final commands: `mvn -f backend/pom.xml clean verify` on Java 25, then `git diff --check`.
- Record exact commands/exits, environment, test totals, focused results, schema/Modulith verification, and any
  known warnings in `docs/implementation/phase-4/test-evidence.md`.

## Constraints / risks

- If an implementation need conflicts with frozen Schema v1 or a module boundary, stop and report it; do not
  silently revise a frozen baseline. Avoid JPA cross-module associations and transaction self-invocation traps.
- Relevant skills: `fiction-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence`, and `backend-testing`.
- Agents do not commit, push, tag, or create/merge PRs.

## Implementation result

- **Status update:** Remediated and returned to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Implementation & Remediation summary:**
  - **Prior Findings Remediated:**
    - Removed unbounded public `FictionGenreOperations.findAll()`, `FictionGenreService.findAll()`, `FictionGenreRepository.findAllByOrderByNameAsc()`, and `FictionGenreIntegrationTest.findAllOrderedByName`. In `FictionLinkIntegrationTest`, retained the fixture's genre ID directly without global list lookups.
    - Corrected the column inventories in `test-evidence.md` to match exact physical Flyway V1 columns (`fictions`: `poster_url`, `is_nsfw`, `description`, `current_progress_text`, `review`; `fiction_links`: `label`, no `updated_at`) referencing `V1__create_schema_v1.sql`.
  - **Codex Re-Review Finding Remediated (Deterministic Lock Polling):**
    - Replaced timing-dependent `Thread.sleep(200)` calls with deterministic, observable PostgreSQL lock polling (`awaitCompetingLock` adapted from `RefreshTokenLifecycleIntegrationTest`) in `FictionGenreIntegrationTest` and `FictionIntegrationTest`:
      - `concurrentDuplicateGenreCreationRecoversFromUniqueIndexConflict`: Thread 1 holds uncommitted lock in PostgreSQL on `uq_ci_fiction_genres_name`, Thread 2 attempts duplicate creation, blocks on index lock, and actively observed via `awaitCompetingLock("fiction_genres", Duration.ofSeconds(5))` polling `pg_locks` (`NOT l.granted`) and `pg_stat_activity`. Upon Thread 1's commit, Thread 2 unblocks, cleanly catches `DataIntegrityViolationException`, and throws `FictionGenreNameAlreadyExistsException` without raw database error escaping. Exactly one row committed in PostgreSQL.
      - `storyArchetypeAssignmentIdempotencyAndDeterministicContention`: Thread 1 holds uncommitted row/index lock on `pk_fiction_story_archetypes`, Thread 2 executes native `INSERT ... ON CONFLICT DO NOTHING`, blocks waiting for Thread 1, and actively observed via `awaitCompetingLock("fiction_story_archetypes", Duration.ofSeconds(5))`. Upon Thread 1's commit, Thread 2 unblocks and completes cleanly with 0 errors and zero raw persistence exceptions escaping. Exactly one row committed in PostgreSQL.
      - `worldSettingAssignmentIdempotencyAndConcurrency`: Thread 1 holds uncommitted row/index lock on `pk_fiction_world_settings`, Thread 2 executes native `INSERT ... ON CONFLICT DO NOTHING`, blocks waiting for Thread 1, and actively observed via `awaitCompetingLock("fiction_world_settings", Duration.ofSeconds(5))`. Upon Thread 1's commit, Thread 2 unblocks and completes cleanly with 0 errors and zero raw persistence exceptions escaping. Exactly one row committed in PostgreSQL.
    - Asserted observable lock wait, committed single-row state, and domain-level/no-raw-error outcome. Zero production code changed.
  - Narrowed `fiction/package-info.java` allowed module dependencies strictly to `vault::entry/enums/view`, `people::person/group/view`, and `reference::catalog/view`.
  - All public contracts use `@NamedInterface`: `fiction.enums`, `fiction.view`, `fiction.genre`, `fiction.fiction`, and `fiction.link`.
  - All internal entities and repositories remain strictly encapsulated within `fiction.internal.*`.
- **Verification evidence:**
  - `mvn -f backend/pom.xml clean verify`: Exit status `0` (`BUILD SUCCESS`), 297/297 tests passed across the repository.
  - `git diff --check`: Exit status `0` (clean, zero whitespace errors).
  - Graphify AST extraction: Refreshed cleanly (1402 nodes, 4398 edges, 150 communities).
  - Comprehensive evidence updated in `docs/implementation/phase-4/test-evidence.md`.
- **Constraints / Baselines preserved:**
  - Zero modifications to Flyway migrations or DBML frozen Schema v1.
  - Zero JPA associations across module boundaries; no cross-module internal package imports.
  - Zero Git commits or pushes made.

## Codex remediation

All three initial findings were resolved across two Antigravity remediation passes. The final test-only pass
replaced timing-only sleeps with an observed PostgreSQL lock wait before releasing the holder transaction in
the genre-name and both classification duplicate-race tests. See the dated
`docs/implementation/phase-4/reviews/2026-09-29-phase-4-final-codex-review.md` for the review history.

## Final review

Initial Codex review: **CHANGES_REQUESTED** (three findings; 298 tests passed). First remediation re-review:
**CHANGES_REQUESTED** (one test-only finding; 297 tests passed). Final remediation re-review:
**READY_FOR_OWNER_COMMIT** (all findings resolved; independent `clean verify` passed 297 tests and
`git diff --check` passed). Owner commits/pushes; agents do not.
