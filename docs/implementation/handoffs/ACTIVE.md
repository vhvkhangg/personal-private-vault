# Active Implementation Handoff

- Handoff ID: `backend-phase-1-reference-vault-foundation`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex

## Goal

Complete Backend Phase 1 by implementing the existing `reference` and `vault` skeletons against the approved Flyway V1 PostgreSQL schema, adding focused PostgreSQL/Testcontainers coverage, and retaining one green final verification result. Keep entities and repositories module-internal and expose only the existing public contracts/read models.

## Sources of truth

- [`docs/implementation/backend-phase-1.md`](../backend-phase-1.md)
- [`docs/implementation/backend-phase-1-owner-files.md`](../backend-phase-1-owner-files.md) — exhaustive 27-file implementation list
- [`docs/implementation/backend-phase-1-flyway-manifest.md`](../backend-phase-1-flyway-manifest.md)
- [`docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`](../../database/personal-private-vault-schema-v1-FROZEN-final.dbml)
- [`backend/src/main/resources/db/migration/V1__create_schema_v1.sql`](../../../backend/src/main/resources/db/migration/V1__create_schema_v1.sql)
- [`docs/adr/0004-postgresql-jpa-hibernate-flyway.md`](../../adr/0004-postgresql-jpa-hibernate-flyway.md)
- [`docs/adr/0005-vault-entry-shared-identity.md`](../../adr/0005-vault-entry-shared-identity.md)
- [`docs/architecture/module-dependency-matrix.md`](../../architecture/module-dependency-matrix.md)
- Scoped `AGENTS.md` files under `reference`, `vault`, and `backend/src/test`

## Implementation targets

- Implement every `TODO(antigravity)` in the 27 Java files listed by `backend-phase-1-owner-files.md`; remove the completed TODO placeholders.
- `reference.internal.domain`: map the six entities exactly to `countries`, `languages`, `currencies`, `platforms`, `story_archetypes`, and `world_settings`.
- `reference.internal.infrastructure.persistence`: implement the six Spring Data repositories with only the queries needed by `ReferenceCatalogService`.
- `ReferenceCatalogService`: implement all existing `ReferenceCatalog` methods, returning public view records only. Order country/language/currency lists by code; order platform/archetype/world-setting lists case-insensitively by name with ID as the tie-breaker.
- `vault.internal.domain`: implement `VaultEntry`, `Favorite`, `Rating`, `Tag`, `VaultEntryTagId`, `VaultEntryTag`, and the explicit `VaultCapabilityMatrix`.
- `vault.internal.infrastructure.persistence`: implement the five repositories and minimal existence/lookup/delete queries required by the services.
- `VaultEntryService` and `VaultMetadataService`: implement the existing public interfaces with transaction boundaries, public-view mapping, and a test-injectable UTC `Clock`. Do not add a cross-module/shared utility package.
- Validate the existing Flyway V1 rather than rewriting it. Change V1 only for a confirmed pre-freeze translation defect, keep the DBML frozen, and update the Flyway manifest/evidence if its checksum or counts change.
- Add focused tests under `backend/src/test/java`; production additions outside the listed skeletons require a direct Phase 1 need and must remain inside `reference` or `vault`.

## Required behavior / invariants

- Use explicit `@Table`/`@Column` mappings matching Flyway V1. PostgreSQL `platform_kind`, `vault_entry_type`, and `rating_grade` fields must use Hibernate named-enum support (for example `@Enumerated(EnumType.STRING)` plus `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`), not varchar emulation.
- Use safe Lombok only: narrow annotations such as `@Getter`, protected JPA no-args construction, and `@RequiredArgsConstructor` for stateless services where it fits. Never use entity `@Data`; keep constructors/factories, state transitions, identity, and association semantics explicit. Only the embeddable composite key should use value-based equality/hash semantics.
- Scalar FK identifiers are acceptable for the vault metadata entities; do not introduce broad cascade/orphan-removal behavior or expose entity associations through public views.
- Persist timestamps as `Instant`/UTC. Vault writes must obtain one timestamp per operation from the service clock. The normal Spring construction path may use `Clock.systemUTC()`, but tests must be able to supply a fixed clock without adding a global shared module.
- `VaultEntryOperations.create`: reject null type, create an active identity, and set `createdAt == updatedAt` with `deletedAt == null`.
- `find`: return `Optional.empty()` only when the ID does not exist. Mutating a missing vault entry or missing tag throws `NoSuchElementException`; null/blank arguments throw `IllegalArgumentException`.
- `moveToTrash` and `restore` are idempotent. Repeated trash must preserve the original `deletedAt`; repeated restore must not alter `updatedAt`. Real transitions update `updatedAt`. Soft delete must retain favorite/rating/tag rows, and metadata writes against a trashed entry must fail with `IllegalStateException` until restored.
- Capability matrix: explicitly enumerate every current `VaultEntryType`; do not use a permissive default. Every current type is favoriteable. Every current type except `FILM_CREDIT` is rateable and taggable. `FILM_CREDIT` is favorite-only. `BRAND` must therefore support rating. Unsupported metadata writes throw `IllegalStateException`, and any future/unmapped type must fail closed.
- `favorite`/`unfavorite`, `removeRating`, `attachTag`, and `detachTag` are idempotent. `setRating` is create-or-update; an update preserves `createdAt` and changes `updatedAt`. Capability checks occur before writes.
- `createTag` trims surrounding whitespace, rejects an empty result, preserves case/diacritics otherwise, and returns the existing tag for a case-insensitive duplicate. Tag assignment must never create a duplicate `(vault_entry_id, tag_id)` row.
- `metadata` must verify the entry exists, return a deterministic tag order (case-insensitive name then ID), use `null` for no rating, and never expose internal entities or mutable collections.
- Reference catalog access is read-only and deterministic; do not add mutation APIs or large seed data.
- `reference` and `vault` retain `allowedDependencies = {}` and must not import another application module's API or `internal` packages.

## Non-goals

- Controllers, HTTP DTOs, `ApiResponse`, authentication/JWT, settings, or any other business module.
- Permanent deletion, automatic trash purge, subtype creation/orchestration, or cross-table triggers not already in V1.
- Large reference catalogs, global search/`pg_trgm`, frontend, RAG, deployment, or Phase 2 work.
- Changes to the frozen DBML, module dependency matrix, ADR decisions, or existing public API signatures unless Codex first updates this handoff.

## Test/evidence contract

- Focused tests:
  - keep `ApplicationArchitectureTests` green;
  - migrate a fresh PostgreSQL 18.6 Testcontainer with Flyway V1 and assert the manifest baseline (41 named enums, 69 application tables, 103 foreign keys);
  - start JPA against that migrated PostgreSQL schema with `ddl-auto=validate`;
  - verify all reference mappings, public view conversion, optional lookup behavior, and deterministic ordering;
  - verify vault create/find/trash/restore timestamps, idempotency, missing-target behavior, and metadata retention across trash/restore;
  - verify the full capability matrix, including `FILM_CREDIT` favorite-only and `BRAND` rating support;
  - verify favorite/rating/tag upsert/removal/idempotency, trimmed tag validation, case-insensitive tag reuse/uniqueness, duplicate assignment prevention, and relevant PK/FK/unique constraints;
  - use real PostgreSQL only; do not add H2.
- Final command: `mvn -f backend/pom.xml clean verify`
- Required evidence file: `docs/implementation/backend-phase-1-test-evidence.md` containing the exact final command, exit status, test counts, Testcontainers/PostgreSQL version, Flyway migration result, JPA validation result, and any material warnings.
- After implementation, refresh the code-only Graphify graph using the repository refresh script and record whether it succeeded; Graphify refresh is not a substitute for Maven evidence.

## Constraints / risks

- Flyway V1 currently matches its manifest hashes/counts; it is a large pre-freeze baseline, so distinguish an actual translation defect from an ORM mismatch before editing it.
- Native PostgreSQL enum mapping and PK-as-FK metadata tables are the highest ORM-risk areas; tests must exercise persistence, not only mocks.
- Case-insensitive tag creation can race with the database unique index. Resolve duplicates deterministically (re-query the canonical row after a uniqueness conflict) rather than weakening the index.
- Docker must be available for the final verification. If external dependency resolution or Docker is unavailable, record the blocker precisely and do not substitute H2 or weaken tests.

## Implementation result

- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Implementation summary:**
  - Implemented all 27 Java files designated in [`backend-phase-1-owner-files.md`](../backend-phase-1-owner-files.md). All `TODO(antigravity)` items resolved and removed.
  - `reference.internal.domain`: 6 JPA entities (`Country`, `Language`, `Currency`, `Platform`, `StoryArchetype`, `WorldSetting`) mapped with explicit column definitions matching Flyway V1. PostgreSQL native `platform_kind` mapped via `@Enumerated(EnumType.STRING)` and `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`.
  - `reference.internal.infrastructure.persistence`: 6 Spring Data JPA repositories implemented with required catalog queries (ordered by code ascending for countries, languages, currencies; ordered case-insensitively by name with ID tie-breaker for platforms, story archetypes, and world settings).
  - `reference.internal.application`: `ReferenceCatalogService` implemented returning public read-only views with deterministic ordering and optional lookup handling.
  - `vault.internal.domain`: implemented `VaultCapabilityMatrix` (all 17 types favoriteable, all except `FILM_CREDIT` rateable/taggable, fail-closed validation), `VaultEntry` (shared identity, soft delete, restore, idempotency), `Favorite`, `Rating` (updateGrade preserves createdAt and updates updatedAt), `Tag` (whitespace trimming, case/diacritics preservation), `VaultEntryTagId` (`@Embeddable` composite key), and `VaultEntryTag`.
  - `vault.internal.infrastructure.persistence`: 5 Spring Data JPA repositories implemented (`VaultEntryRepository`, `FavoriteRepository`, `RatingRepository`, `TagRepository` with case-insensitive name lookup, `VaultEntryTagRepository` with ordered tag query).
  - `vault.internal.application`: `VaultEntryService` and `VaultMetadataService` implemented with transaction boundaries, test-injectable UTC `Clock`, capability matrix enforcement, metadata retention across trash/restore, idempotent operations, and public view mapping.
  - Remediated concurrent case-insensitive tag creation: introduced [`TagCreator.java`](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/TagCreator.java) (`@Transactional(propagation = Propagation.REQUIRES_NEW)`) so insert attempts conflicting on `uq_ci_tags_name` roll back independently without marking caller transactions rollback-only, allowing safe recovery and retrieval of the canonical tag.
- **Tests & evidence summary:**
  - Full suite clean verify command: `mvn -f backend/pom.xml clean verify` completed with exit status `0` (`BUILD SUCCESS`).
  - Total tests run: 57, Failures: 0, Errors: 0, Skipped: 0.
  - Integration tests executed against PostgreSQL 18.6 Testcontainer (`postgres:18.6-alpine`) with Flyway V1 schema migration and Hibernate `ddl-auto=validate`.
  - Added concurrent race condition integration test (`VaultMetadataIntegrationTest.concurrentCaseInsensitiveTagCreationRecoversFromUniqueIndexConflict`) verifying two independent concurrent transactions recover from `uq_ci_tags_name` violation and return the identical tag with only 1 row persisted.
  - Flyway V1 manifest baseline verified: 41 named enums, 69 application tables, 103 foreign keys.
  - Spring Modulith modular architecture rules verified (`ApplicationArchitectureTests` passed).
  - Evidence file recorded: [`docs/implementation/backend-phase-1-test-evidence.md`](../backend-phase-1-test-evidence.md).
  - Graphify code-only graph refreshed via `.agents/refresh-graphify.ps1` (418 nodes, 1056 edges, 50 communities).
- **Residual risks:** None identified. Schema mapping, native enum types, composite keys, capability checks, concurrency handling, and soft delete state transitions fully validated against live PostgreSQL 18.6.

## Codex remediation

- [x] Make case-insensitive `createTag` race recovery cross-transaction safe. The current `saveAndFlush` / `DataIntegrityViolationException` catch in `VaultMetadataService` re-queries inside the same transaction that PostgreSQL/Hibernate has already marked rollback-only after the unique-index violation, so a concurrent loser cannot reliably return the canonical tag. Isolate the insert attempt from the canonical-row lookup with transaction semantics that permit the lookup to run after the failed insert, while preserving the existing `uq_ci_tags_name` index and public contract.
- [x] Add a PostgreSQL/Testcontainers regression that forces two independent transactions to create the same trimmed tag with different case concurrently, then proves both calls return the same persisted tag and only one case-insensitive row exists. Keep the existing sequential validation/reuse coverage.
- [x] Run the handoff's final command exactly once after remediation, update `backend-phase-1-test-evidence.md` with the new result/counts, refresh Graphify, and return the handoff to `IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`.
- [x] Synchronize the Phase 1 status/readme documentation with the implemented state. `docs/implementation/backend-phase-1.md` still says `AWAITING CODEX IMPLEMENTATION HANDOFF`, `docs/implementation/README.md` still says `OWNER IMPLEMENTATION REQUIRED`, and the root/backend READMEs still describe implementation skeletons. Keep Phase 1 active (not frozen) until the owner commits, but describe the reference/vault implementation and retained verification factually.
- [x] Fix the broken relative link to `TagCreator.java` in this handoff's implementation summary (`../../../backend/...` from this file, not `../../backend/...`). Return the handoff to `IMPLEMENTED_AWAITING_CODEX_REVIEW` after these documentation-only corrections. The retained Maven evidence remains applicable if production code and tests are unchanged.

## Final review

- **Codex status:** `READY_FOR_OWNER_COMMIT`
- **Final review record:** [`docs/reviews/2026-09-27-backend-phase-1-final-codex-review.md`](../../reviews/2026-09-27-backend-phase-1-final-codex-review.md)
- **Prior review records:** [`concurrency finding`](../../reviews/2026-09-27-backend-phase-1-reference-vault-codex-review.md) and [`documentation remediation`](../../reviews/2026-09-27-backend-phase-1-remediation-codex-review.md)
- **Review result:** No blocking findings remain. The tag-concurrency remediation and documentation synchronization are accepted.
- **Verification note:** Codex reviewed the retained 57-test green PostgreSQL evidence and Surefire reports but did not rerun tests, per final-review policy.
- **Next action:** Owner commits and pushes. Phase 1 remains active until that owner action, after which its documentation can be marked complete/frozen.
