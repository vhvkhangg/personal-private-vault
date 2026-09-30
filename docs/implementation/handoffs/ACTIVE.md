# Active Implementation Handoff

- Handoff ID: `backend-phase-8-knowledge`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: Backend Phase 8 Knowledge foundation

## Goal

Implement the closed parent `knowledge` facade and its nested `study`, `information`, `vocabulary`, and `note`
Spring Modulith modules against unchanged Schema v1. Deliver Vault-backed identity, domain operations and bounded
reads, an explicit atomic Vocabulary review transition, and privacy-safe uniqueness behavior without pulling future
feed/import/search workflows into this phase.

## Sources of truth

- `docs/implementation/phase-8/README.md` and `preparation-review.md` — approved behavior and test contract.
- `docs/implementation/phase-8/reviews/2026-09-30-phase-8-pre-handoff-codex-rereview.md` — closed preparation findings.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — exact columns, enums, constraints, and FKs.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen ownership, topology, and dependency direction.
- `.agents/rules/backend-phase-8-knowledge.md` and
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/AGENTS.md` — scoped implementation guidance.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/`, implement exactly `study_items`,
  `information_items`, `vocabulary_items`, `vocabulary_reviews`, and `notes`, owned respectively by the four nested
  modules. Keep JPA entities/repositories/internal services inside their owning module. Use capability-oriented
  public operations with immutable commands/views and meaningful `package-info.java` named interfaces. Remove each
  replaced Knowledge `internal/.gitkeep`.
- Add a small parent `knowledge` facade that delegates to nested public operations. Externally consumable method
  signatures — including parameter, result, generic, and exception types — use parent-owned exposed Knowledge API
  types only. Narrow mapping is allowed; nested persistence/validation stays nested-owned. Keep the parent closed.
- Support create/update/find-by-ID for Study, Information, Vocabulary, and Note; a single transactional Vocabulary
  review-transition operation; bounded due-item and per-item review-history reads; and optional single Note lookup
  by imported hash. Do not add global unbounded lists/search or a second implementation path in the facade.
- Add focused tests under `backend/src/test/java/com/vhvkhangg/personalprivatevault/knowledge/` and final evidence at
  `docs/implementation/phase-8/test-evidence.md`.

## Required behavior / invariants

- Each Study/Information/Vocabulary/Note row shares the ID and transaction of its corresponding Vault Entry type;
  any domain failure leaves no orphan Vault Entry. Vault alone owns favorite/rating/tag/recycle behavior.
- Study permits zero authors but never both Person and Creator Group, and enforces nonnegative price with currency
  required when price exists (currency without price remains allowed),
  progress `0..100`, and frozen type-specific Website/YouTube fields. Validate People/Reference/Account only via
  public contracts. A YouTube Study account must be Account type `YOUTUBE_CHANNEL` on a platform whose trimmed name
  equals `YouTube` case-insensitively. Keep channel metadata Account-owned. Duplicate non-null channel assignments,
  including concurrent attempts, yield a stable Study conflict with PostgreSQL uniqueness as final arbiter.
- Information preserves the frozen simple content model; do not invent URL/category uniqueness or feed semantics.
- Vocabulary validates Reference language and frozen numeric SRS checks, while allowing duplicate word/language
  pairs. A review transition row-locks one item, records its actual committed previous interval/ease, appends one
  review row and updates current state atomically from an explicit requested result. Concurrent transitions
  serialize; no automatic scheduling formula is selected.
- Due Vocabulary reads use the inclusive-cutoff predicate and scheduled-first/ID ordering in the Phase 8 README:
  non-mastered scheduled rows at/before cutoff plus null-time `NEW`; exclude other null-time states and all
  `MASTERED`. Require a positive bounded limit and no read-side mutation.
- Note preserves Markdown/Obsidian content and arbitrary JSONB object frontmatter. Duplicate non-null imported
  hashes, including concurrent attempts, yield a stable Note conflict and roll back the losing Vault creation.
  Neither hash values nor raw PostgreSQL `Detail: Key` text may appear in logs/diagnostics.
- Public reads remain ID-/domain-/parent-scoped and explicitly bounded where collections can grow. No JPA entity,
  repository, nested internal, or nested DTO leaks through the external parent API.

## Non-goals

No DBML/Flyway/frozen foundation change; Phase 9 Collection, Phase 10 Feed/ImportData, global search, metadata
fetching/scraping/OAuth, automatic SRS algorithm, REST/controllers/OpenAPI, frontend, RAG, deletion, or speculative
adapters/events/generic CRUD bases. Do not implement a future caller's workflow merely to test the facade boundary.

## Test/evidence contract

- PostgreSQL Testcontainers/JUnit 5 coverage for all five tables and four Vault identity/rollback paths; Study
  author/type/currency/progress validation and sequential/concurrent YouTube-account uniqueness; Information
  persistence; Vocabulary duplicate allowance, numeric validation, due predicate/order/bounds, atomic review
  history/rollback and real row-lock contention; Note Markdown/frontmatter round-trip and sequential/concurrent
  imported-hash uniqueness with Vault rollback. Race tests must observe PostgreSQL contention/serialization, not
  rely on sleeps alone. Do not use H2.
- For both new unique-conflict paths, capture logs across competing worker threads, reach the PostgreSQL
  constraint, and assert stable domain conflicts with no raw vendor `Detail: Key` output. Use a distinctive private
  marker for Note's string hash and linked Account metadata in the Study case; assert neither marker appears.
  Preserve the existing privacy-safe constraint-logging policy.
- Verify exact nested and top-level Modulith dependencies, named interfaces, no internal-package access, and a
  representative external top-level test consumer that imports only parent Knowledge API types and passes
  architecture verification. Derive the supported nested dependency declaration from the current Spring Modulith
  model and actual imports; do not open modules to bypass verification.
- Verify unchanged Flyway V1/Hibernate schema validation. Final commands: `mvn -f backend/pom.xml -ntp clean verify`
  on Java 25, then `git diff --check`. Record commands/exits, test totals, PostgreSQL/architecture and privacy
  results, environment, and known diagnostics in `docs/implementation/phase-8/test-evidence.md`.

## Constraints / risks

- Stop and report any conflict with frozen Schema v1 or module boundaries; do not silently change the baseline.
  Keep transaction/constraint translation in owning nested application services; do not log raw throwable/root
  messages containing private values. Use the database as the final uniqueness arbiter and deliberate row locking
  for review transitions.
- Relevant skills: `knowledge-domain-modeling`, `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence`, and `backend-testing`.
- Agents do not commit, push, tag, or create/merge PRs.

## Implementation result

Backend Phase 8 (`knowledge`) foundation implementation is complete:

1. **Nested Application Modules & Entities**:
   - `knowledge.study`:
     - Entity: `StudyItem` (persisted to `study_items`, implements `Persistable<Long>` sharing `vault_entries` ID with `STUDY` type).
     - Validation: Person XOR Group author invariant, price >= 0 with currency code validation via `ReferenceCatalog`, progress `0..100`, Website site domain validation and normalization, YouTube channel account validation via `ExternalAccountOperations` (ownership `TRACKED`, type `YOUTUBE_CHANNEL`, platform trimmed case-insensitive `YouTube`).
     - Concurrency: Sequential and multi-threaded race handling on `study_items.youtube_channel_account_id` with `DataIntegrityViolationException` translated to `StudyConflictException`, atomical Vault entry rollback, and privacy-safe logging (no private markers, no PostgreSQL `Detail: Key`).
     - Exposed interfaces: `@NamedInterface("study")`, `@NamedInterface("enums")`, `@NamedInterface("view")`.
   - `knowledge.information`:
     - Entity: `InformationItem` (persisted to `information_items`, implements `Persistable<Long>` sharing `vault_entries` ID with `INFORMATION` type).
     - Validation: Required title, content text, citation, and markdown preservation without artificial uniqueness constraints.
     - Exposed interfaces: `@NamedInterface("information")`, `@NamedInterface("enums")`, `@NamedInterface("view")`.
   - `knowledge.vocabulary`:
     - Entities: `VocabularyItem` (persisted to `vocabulary_items`, implements `Persistable<Long>` with `VOCABULARY` type) and `VocabularyReview` (persisted to `vocabulary_reviews`).
     - Validation: Duplicate word/language allowed; language code validated via `ReferenceCatalog.language(code)`; numeric SRS invariants (ease factor >= 1.30, nonnegative repetitions/intervals/lapse counts).
     - Operations: Explicit atomic review transition with pessimistic row lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on `findByIdForUpdate` to safely serialize concurrent review transitions, record historical `VocabularyReview`, and update item state atomically.
     - Queries: Due vocabulary query (`findDueItems`) with non-mastered rows at/before cutoff plus null-time `NEW` ordered by non-null due dates first then `next_review_at` ASC and `id` ASC; bounded review history lookup ordered by `reviewed_at` DESC.
     - Exposed interfaces: `@NamedInterface("vocabulary")`, `@NamedInterface("enums")`, `@NamedInterface("view")`.
   - `knowledge.note`:
     - Entity: `Note` (persisted to `notes`, implements `Persistable<Long>` with `NOTE` type).
     - Features: Obsidian/Markdown syntax preservation intact (wiki-links, callouts, latex, code blocks); arbitrary JSONB `frontmatter` (`Map<String, Object>`) round-trip mapped with `@JdbcTypeCode(SqlTypes.JSON)`.
     - Concurrency: Sequential and multi-threaded race handling on `notes.imported_file_hash` with `DataIntegrityViolationException` translated to `NoteConflictException`, atomical Vault entry rollback, and privacy-safe logging (no private hash, no PostgreSQL `Detail: Key`).
     - Exposed interfaces: `@NamedInterface("note")`, `@NamedInterface("view")`.

2. **Parent Facade (`knowledge`)**:
   - Closed facade exposing parent-owned API types only in package `com.vhvkhangg.personalprivatevault.knowledge.api.*` (`@NamedInterface("api")`).
   - Delegates all operations to nested module interfaces with 1-to-1 DTO and exception mapping; zero persistence or domain validation duplication.
   - Completely encapsulates nested modules and internal packages from external callers.

3. **Representative External Consumer**:
   - `FeedKnowledgeConsumerTestFixture` under `src/test/java/com/vhvkhangg/personalprivatevault/feed/` consuming `KnowledgeOperations` via `knowledge.api.*` only, verifying Spring Modulith boundary rules.

4. **All `.gitkeep` Placeholders Removed**:
   - Removed `internal/.gitkeep` from parent `knowledge` and all four nested modules (`study`, `information`, `vocabulary`, `note`).

5. **Verification & Tests**:
   - Unit tests: `KnowledgeValidationTest` (26 tests) covering domain validations, author XOR invariants, website/account rules, SRS numeric invariants, and facade exception translations.
   - Architecture tests: `KnowledgeArchitectureTests` (3 tests) and `ApplicationArchitectureTests` (12 tests) verifying Spring Modulith module boundaries, no cyclicity, exact named interfaces, and strict encapsulation (zero `.internal.` leaks).
   - Integration tests: `KnowledgeIntegrationTest` (18 tests) running on PostgreSQL 18.6 Testcontainers covering schema validation, 4-way Vault rollback, study author and YouTube channel account race contention with observable PostgreSQL lock wait, information persistence, vocabulary duplicate allowance, numeric ease factor boundaries, concurrent review pessimistic lock serialization with serial state chaining, full due vocabulary query matrix, note markdown/JSONB frontmatter preservation, imported hash race contention with observable PostgreSQL lock wait, and full facade delegation.
   - Full monolith verification: `mvn -f backend/pom.xml -ntp clean verify` passed with 547/547 tests green in 01:02 min.
   - Clean diff: `git diff --check` passed with 0 warnings/errors.
   - Final evidence logged at `docs/implementation/phase-8/test-evidence.md`.

## Codex remediation

All three blocking issues from Codex final review (`docs/implementation/phase-8/reviews/2026-09-30-phase-8-final-codex-review.md`) have been remediated and verified:

1. **Website domain hostname validation**:
   - `StudyItemService.validateStudyItem`: Enforces strict hostname validation (`HOSTNAME_PATTERN`) rejecting URI schemes (`http://`, `https://`), ports (`:8080`), paths (`/docs`), fragments, queries, and invalid characters on both create and update operations, while preserving locale-independent trim and lowercase normalization.
   - Unit coverage: `KnowledgeValidationTest.enforcesWebsiteHostnameValidation` tests accepted hostnames (`example.com`, `sub.domain.co.uk`, `my-site.org`) and invalid schemes, paths, ports, labels, and boundary cases.
   - Integration coverage: `KnowledgeIntegrationTest.rejectsInvalidWebsiteHostnames` confirms rejection on both create and update with `InvalidStudyItemException`.

2. **Vocabulary ease factor scaling & range validation**:
   - `VocabularyService`: Added `MAX_EASE_FACTOR` (`999.99`) and `validateAndScaleEaseFactor` applied to both item create/update (`validateItem`) and SRS review transitions (`review`).
   - Validates ease factor *after* two-decimal scaling (`RoundingMode.HALF_UP`) and against the `numeric(5,2)` range (`0.01` to `999.99`). Rejects values that round to zero (e.g. `0.001`, `0.000`) or overflow (e.g. `1000.00`, `999.995`) with stable domain `InvalidVocabularyItemException` before hitting database constraints.
   - Unit coverage: `KnowledgeValidationTest.validatesEaseFactorBoundaries` verifies create, update, and review transitions reject `0.001`, `1000.00`, and `999.995` while accepting `0.005` (scaled to `0.01`) and `999.99`.
   - Integration coverage: `KnowledgeIntegrationTest.rejectsEaseFactorBoundariesAgainstPostgres` verifies rejection against PostgreSQL without database constraint violation errors.

3. **Observable PostgreSQL contention, serial state chaining, full due matrix, and privacy logging**:
   - **Observable Unique Contention & Privacy Logging (Study & Note)**:
     - `concurrentYoutubeAccountRaceIsSafe` and `concurrentImportedFileHashRaceIsSafe` coordinate transactions using `CountDownLatch` and `awaitCompetingLock` observing PostgreSQL `pg_locks` (`NOT l.granted`) and `pg_stat_activity` to guarantee real PostgreSQL unique-index lock contention.
     - Confirms losing transaction receives domain conflict exception and rolls back its Vault entry leaving exact 1 Vault entry.
     - Confirms captured logs across both worker thread streams contain neither private test markers (`FIRESHIP_CHANNEL_MARKER_999`, `SHA256_PRIVATE_HASH_MARKER_99999999`) nor raw vendor `Detail: Key` lines.
   - **Observable Row-Lock Contention & Serial Review Chaining**:
     - `concurrentReviewContentionSerializes` holds row lock (`SELECT FOR UPDATE`) on Thread 1, coordinates Thread 2's `reviewTransition` (`findByIdForUpdate`), observes PostgreSQL row lock contention via `awaitCompetingLock("vocabulary_items")`, commits Thread 1, and asserts Review 2 successfully executes and its `previous_*` fields match Review 1's `new_*` fields exactly (`previousIntervalDays` = 3, `previousEaseFactor` = 2.35).
   - **Complete Vocabulary Due Query Matrix**:
     - `dueVocabularyQueryMatrixAndOrdering` tests:
       - Inclusive cutoff boundary (matching exactly at cutoff).
       - Null-time `NEW` items included.
       - Null-time `LEARNING` and `REVIEW` items excluded.
       - `MASTERED` items excluded regardless of `next_review_at`.
       - Non-null scheduled items ordered before null scheduled items.
       - Secondary tie-break order (`next_review_at` ASC then `id` ASC).
       - Limit parameter enforcement.

4. **Monolith Verification**:
   - Full suite: `mvn -f backend/pom.xml -ntp clean verify` passed with 547/547 tests green in 01:02 min.
   - Diff hygiene: `git diff --check` passed cleanly with 0 errors.
   - Graphify refreshed via `scripts/refresh-graphify.ps1`.

## Final review

Codex final re-review on 2026-09-30 returned `READY FOR OWNER COMMIT`; all three findings are closed in
`docs/implementation/phase-8/reviews/2026-09-30-phase-8-final-codex-rereview.md`.

Suggested commit message: `feat(knowledge): establish phase 8 knowledge foundation`
