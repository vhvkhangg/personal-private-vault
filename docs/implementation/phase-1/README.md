# Backend Phase 1 — PostgreSQL/Flyway Schema v1 + Reference & Vault Foundation

Status: **COMPLETE — FROZEN (2026-09-27)**

Phase 1 establishes the executable PostgreSQL schema and implements the dependency-free foundation modules
`reference` and `vault`.


## Completion record

Backend Phase 1 is complete and frozen.

- Reference/vault implementation: complete.
- Final verification: `mvn -f backend/pom.xml clean verify` — **57 tests, 0 failures, 0 errors, 0 skipped**.
- PostgreSQL integration: PostgreSQL 18.6 Testcontainers + Flyway V1 + Hibernate validation.
- Final Codex review: **READY FOR OWNER COMMIT**.
- Final review: [`../../reviews/2026-09-27-backend-phase-1-final-codex-review.md`](../../reviews/2026-09-27-backend-phase-1-final-codex-review.md).
- Evidence: [`test-evidence.md`](test-evidence.md).

Future changes to the foundation are new scoped work and do not silently rewrite this frozen baseline.

## Included scope

- Existing Flyway `V1__create_schema_v1.sql` for frozen Database Schema v1.
- PostgreSQL structural constraints represented by the frozen DBML and approved Phase 1 migration.
- Java/JPA implementation for `reference`.
- Java/JPA + business implementation for `vault`.
- Shared vault identity, favorites, ratings, tags, soft delete, and restore.
- Public module contracts/read models needed by later modules.
- PostgreSQL/Testcontainers validation.
- Lombok for safe compile-time boilerplate reduction.

## Explicitly deferred

- REST controllers / `ApiResponse`.
- Authentication/JWT and settings.
- Other business modules.
- Permanent-delete behavior.
- Large reference-data seed catalogs.
- `pg_trgm`/global-search indexes.
- Unspecified cross-table triggers.
- Frontend, RAG, deployment.

## Workflow used to complete Phase 1 (historical)

This records how Phase 1 was completed. It is not executable next-phase scope; future work requires a new owner-approved slice and follows [`../../agent-development-workflow.md`](../../agent-development-workflow.md).

1. Codex: run `$codex-create-handoff`.
2. Codex writes `handoffs/ACTIVE.md` with `READY_FOR_IMPLEMENTATION`.
3. Antigravity: run `/antigravity-implement-handoff`.
4. Antigravity implements production code + tests, iterates to green, retains evidence, and marks the handoff
   `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
5. Codex: run `$codex-final-review`.
6. If `CHANGES_REQUESTED`, rerun `/antigravity-implement-handoff`.
7. If `READY_FOR_OWNER_COMMIT`, Codex supplies one Conventional Commit message.
8. Owner commits and pushes.

The owner is no longer expected to implement entities/repositories/services manually.

## Implementation targets

See `implementation-targets.md` (legacy filename retained for link stability). All 27 Java foundation files were implemented, tested against PostgreSQL 18.6 Testcontainers, verified green (57 tests, 0 failures), committed/pushed by the owner, and frozen. All Phase 1 implementation TODOs were resolved.

## Flyway V1

File:

`backend/src/main/resources/db/migration/V1__create_schema_v1.sql`

The frozen DBML remains the logical architecture baseline. Do not rewrite V1 after it has been applied to a
shared/persistent database; later accepted physical changes become later Flyway migrations.

No large reference seed dataset is required in Phase 1.

## PostgreSQL enum mapping

Use Hibernate named-enum support for native PostgreSQL ENUM-backed fields rather than silently treating them as varchar.

## Lombok policy

Lombok is available to reduce boilerplate.

For JPA entities:

- do not use `@Data`;
- prefer narrow annotations such as `@Getter`;
- use a protected JPA no-args constructor where appropriate;
- keep identity/equality/state transitions explicit;
- do not generate association-heavy `toString`/`equals`/`hashCode`.

For stateless Spring services, `@RequiredArgsConstructor` with final dependencies is encouraged.

## Reference behavior

Owned tables:

- `countries`
- `languages`
- `currencies`
- `platforms`
- `story_archetypes`
- `world_settings`

Public API is query-oriented and must not expose entities/repositories.

## Vault behavior

Owned tables:

- `vault_entries`
- `favorites`
- `ratings`
- `tags`
- `vault_entry_tags`

Required behavior:

- create/read vault identity;
- soft-delete/restore;
- favorite/unfavorite;
- set/remove rating;
- create/attach/detach global tags;
- explicit fail-closed capability enforcement.

`FILM_CREDIT` is favorite-only and `BRAND` supports global rating.

## Test/evidence expectations

Antigravity should cover at minimum:

- existing Spring Modulith architecture verification;
- fresh PostgreSQL Testcontainers database migrated by Flyway V1;
- JPA validation against migrated schema;
- reference repository/catalog behavior;
- vault identity, soft-delete, restore;
- favorite/rating/tag capability enforcement;
- case-insensitive tag uniqueness / duplicate assignment behavior;
- relevant database constraints.

Use real PostgreSQL, not H2.

## Token/context policy

Graphify is optional but recommended for code navigation. Run `scripts/setup-graphify.ps1` once; then agents
query the local code graph before broad repository reads. Canonical docs/source remain authoritative.

## Completion/freeze criteria (historical)

These criteria were satisfied before Phase 1 was frozen.


1. Active handoff fully implemented.
2. Flyway V1 migrates fresh PostgreSQL.
3. Reference/vault mappings validate.
4. Required domain behavior passes.
5. Module boundaries remain valid.
6. Antigravity evidence is retained.
7. Codex returns `READY FOR OWNER COMMIT`.
8. Codex supplies one Conventional Commit message.
9. After owner commit/push, Phase 1 can be marked `COMPLETE — FROZEN`.


## Post-freeze package-layout refinement (2026-09-28)

The owner approved a behavior-preserving package reorganization before Phase 2:

- public capability contracts moved to semantic `@NamedInterface` subpackages;
- public `*View` records moved to `view/`;
- stable public enums moved to `enums/`;
- internal application implementations mirror capability names;
- no database/Flyway/business-rule change is intended.

The architectural rationale is ADR-0015. This refactor should be verified with the full backend test suite and
Codex review before Phase 2 implementation begins.

See [`package-layout-refactor.md`](package-layout-refactor.md) for the owner-approved maintenance scope and verification gate.
