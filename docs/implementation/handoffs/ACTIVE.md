# Active Implementation Handoff

- Handoff ID: `phase-10-feed-importdata-foundation`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Approved preparation: `0e530f2` (owner-committed; HEAD matches local origin/main)

## Goal

Implement the approved Phase 10 Feed + ImportData workflows over unchanged Schema v1: normalized ingestion,
Vault-backed saved resources and explicit Knowledge conversions, staged parsing/validation and transactional imports.
The owner authorizes this numbered phase through the current request and accepted preparation. Phases 0–9 stay
frozen; the Phase 7–9 milestone remains MILESTONE_READY.

## Sources of truth

- `docs/implementation/phase-10/README.md` — complete scope, fields/defaults, decision/state matrix, ordering and
  testing requirements. Implement its entire approved contract, not only the highlights below.
- `docs/implementation/phase-10/preparation-review.md` and
  `docs/implementation/phase-10/reviews/2026-10-01-phase-10-pre-handoff-codex-rereview.md` — READY FOR HANDOFF.
- Root and Feed/ImportData AGENTS.md; `.agents/rules/backend-phase-10-feed-importdata.md`.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — unchanged physical schema/enums/constraints.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`,
  `docs/repository/repository-package-tree.md` and frozen ADRs — ownership/package baselines.
- Public `vault` contracts and `knowledge/api/KnowledgeOperations.java` under
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/` — sole cross-module integration boundary.

## Implementation targets

Paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`:

- `feed/`: semantic public source/item/resource/conversion/view/enums named interfaces and owner-local
  internal/application, internal/domain, internal/infrastructure/persistence. Own exactly feed_sources,
  feed_items, saved_resources, saved_resource_conversions.
- `importdata/`: public job/view/enums named interfaces and owner-local internal/application, internal/parsing,
  internal/domain, internal/infrastructure/persistence. Own exactly import_jobs and import_job_items.
- Narrow module descriptors to actual public Vault + parent Knowledge named interfaces. Add meaningful package-info
  files, remove implemented placeholders, and synchronize package-tree implementation detail without redesign.
- Corresponding backend Feed/ImportData tests; architecture/privacy support changes only as necessary for this slice.
  Preserve existing consumer fixtures and frozen regression behavior.
- `backend/pom.xml` only for a justified minimal mature CSV/safe-YAML parser dependency if current facilities are
  insufficient; record rationale/safe configuration. No unrelated dependency/toolchain change.
- Phase 10 evidence/docs and this handoff result. Do not modify other modules' production code/public API signatures.

## Required behavior / invariants

- Both modules use only public Vault + parent Knowledge; no nested Knowledge imports, repositories or helper reuse.
  Public DTOs expose no persistence types; Knowledge remains authoritative for target validation/writes.
- JSONB input/entity/view graphs are deeply isolated, preserving nested maps/lists, JSON null and ordinary numeric
  fidelity. Reject unsupported/cyclic/invalid-key graphs through stable payload-free owner validation. No generic base.
- FeedSource scheduling/default/check/due-read rules follow README. Normalized ingestFetch requires enabled source
  and fetchedAt; batch items + fetch timestamps commit atomically and failure does not advance timestamps.
- FeedItem URL hash is lowercase SHA-256 of trimmed stored URL UTF-8. Implement source-scoped dual-key insert/update,
  split-row ambiguity rejection and identical/conflicting in-batch candidate policy. PostgreSQL arbitrates races;
  expected losers receive safe domain conflicts without private URL/external-ID/hash/vendor detail in logs.
- SavedResource is Vault-backed SAVED_RESOURCE. Atomic create and unique URL-hash loser rollback leave no orphan
  Vault entries. Support save-from-feed provenance snapshots and manual/social metadata/link saves.
  Explicit Study/Information/Note conversions use parent Knowledge create commands and commit target + provenance
  link together; multiple distinct targets allowed, no Vocabulary conversion.
- Import matrix is CSV/JSON for all four targets, Markdown for Note only. Parse CREATED text stores exact UTF-8
  SHA-256 file hash, canonical parent-command payloads and provisional VALID/INVALID rows, never final targets.
  Validate PARSED payloads structurally; Note hash is the only existing database dedupe lookup. No invented natural
  uniqueness/query for other targets. Preserve safe CSV/JSON/YAML and exact raw Markdown/title/file/hash rules.
- All parse/validate/execute/cancel mutations share one owner-local job-row guard. Acquire before decisions,
  refresh/read authoritative database state even when already managed, recheck status, hold through caller transaction.
  Prefer targeted PESSIMISTIC_WRITE + refresh; no schema change/global clear/independently committing transition.
- Incompatible/repeated callers get stable invalid-transition rejection; terminal states cannot regress.
  Execute only VALIDATED jobs with every item's explicit compatible decision. UPDATE checks duplicate Vault type
  publicly; item statuses/IDs and imported_items = IMPORTED + UPDATED follow README. All targets + execution state
  commit together; any target failure rolls back to committed VALIDATED. Cancel-first prevents target writes;
  successful execute-first prevents cancellation overwrite.
- All collection reads are positive-bounded and deterministically ordered per README. Store timestamps UTC;
  never log raw payloads, frontmatter, source metadata, URLs, hashes, secrets or raw persistence diagnostics.

## Non-goals

No live provider HTTP/scheduler/scraping, REST/OpenAPI/frontend, object-storage I/O, automatic conversions,
SavedResource-to-Vocabulary conversion, new Knowledge queries, Search/RAG/Phase 11+ work, deletion features,
DBML/Flyway/schema redesign, generic parser/JSON/persistence/event frameworks, custom agents/hooks or unrelated refactors.

## Test/evidence contract

- Antigravity owns tests. Implement the full canonical README testing lists with PostgreSQL Testcontainers/Flyway V1,
  never H2: Feed scheduling/due ordering, ingestion dual-key races/batch rollback, JSON source/view isolation,
  SavedResource duplicate loser Vault rollback, conversions/provenance failure rollback through parent APIs.
- Import coverage: matrix, CSV quoting/newlines, safe JSON/YAML, exact Markdown/file hash, payload isolation,
  structural vs target business validation, Note-only dedupe, decisions/counts, lifecycle/cancel/terminal guards,
  parent target writes and whole-job rollback. Verify exact public dependencies and privacy-safe errors/logs.
- Deterministic same-job races: execute-vs-execute using a target without natural uniqueness; execute-vs-cancel in
  both winner orders; parse-vs-cancel and validate-vs-cancel. Observe real PostgreSQL lock waiting and explicit commit,
  not timing-only sleeps. Assert fresh-state behavior including preloaded contexts, target/Vault counts,
  job/item statuses, classification/import counts and no-write/rollback outcomes.
- Preserve baseline 594 tests and architecture/privacy/rollback/contention regressions. Run focused tests first,
  then `mvn -f backend/pom.xml -ntp clean verify` on Java 25 and `git diff --check`.
- Evidence: `docs/implementation/phase-10/test-evidence.md`: exact commands/exits, Java/Maven/PostgreSQL/Testcontainers
  versions, actual testcase totals/failures/errors/skips, focused names/results, observed contention/query behavior,
  Flyway/Hibernate/Modulith results, parser rationale and warnings/limitations. No unverified IDE/warning-free claims.
  Refresh Graphify through existing tooling; generated artifacts stay untracked.
- Acceptance: complete approved scope and mandatory regressions, final checks green, truthful evidence,
  unchanged frozen baselines/public boundaries, no deferred scope or stray artifacts.

## Constraints / risks

- Skills: feed-import-workflow-modeling, java-spring-coding-standards, pragmatic-solid-design,
  reuse-and-consistency, design-pattern-selection, modular-monolith-architecture, jpa-postgresql-persistence,
  backend-testing. Reuse existing implementer/auditor routing and safety hook.
- Risks: dual unique keys, Vault identity rollback, mutable JSON leaves, parser safety/precision, preloaded state
  under locks, job terminal races, whole-job atomicity and payload-free conflicts. Stop/report frozen-scope conflicts.
- Never commit/push/tag/create PRs. Fill result and set IMPLEMENTED_AWAITING_CODEX_REVIEW on completion.
  Owner commit/push follows Codex final acceptance; ChatGPT then closes Phase 10/prepares Phase 11.
  Phase 10 itself does not trigger a milestone review.

## Latest Codex final acceptance

**READY FOR OWNER COMMIT** on 2026-10-01. F1–F7 are closed as recorded in
[`../phase-10/reviews/2026-10-01-phase-10-final-codex-acceptance.md`](../phase-10/reviews/2026-10-01-phase-10-final-codex-acceptance.md).
Independent `mvn -f backend/pom.xml -ntp clean verify` passed 685 tests with zero failures/errors/skips
(01:51, finished 19:35:01 +07:00). Knowledge and shared application configuration remain unchanged;
ImportData payload precision is owner-local and target normalization stays Knowledge-owned.

Commit message: `feat(backend): implement feed and importdata foundations`.
Owner commits/pushes, then gives the latest package to ChatGPT for Phase 10 closeout/Phase 11 preparation.
Phase 10 is not a milestone gate. Prior review/remediation sections below are historical.

## Implementer-reported F7 scope remediation

2026-10-01: **CHANGES_REQUESTED** in [`../phase-10/reviews/2026-10-01-phase-10-final-codex-scope-review.md`](../phase-10/reviews/2026-10-01-phase-10-final-codex-scope-review.md) resolved strictly within Phase 10:
- Reverted unauthorized changes to frozen Phase 8 Knowledge (`InformationItemService.java`).
- Reverted unauthorized application-wide serialization properties from `application.yml` and removed `VaultJsonFormatMapper.java`.
- Implemented owner-local `ImportPayloadJsonType` (`@Type(ImportPayloadJsonType.class)`) on `ImportJobItem.parsedPayload` in `importdata`, configuring its local `ObjectMapper` with `DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS`. This preserves high-precision numbers (`BigDecimal`) when loading `import_job_items.parsed_payload` without modifying global Hibernate/Jackson configuration or any other entity/module.
- `CsvImportParser` preserves unquoted content markdown verbatim in `import_job_items.parsed_payload`; when targets are executed via `KnowledgeOperations`, Knowledge applies its canonical Phase 8 domain rules (`NoteService` preserves whitespace verbatim into `notes.content_markdown`; `InformationItemService` applies its frozen `trimOrNull` normalization without modifying the frozen Phase 8 codebase).

All 685 tests pass cleanly in `mvn -f backend/pom.xml -ntp clean verify`. Returned to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Implementer-reported re-review remediation

2026-10-01: **CHANGES_REQUESTED** from Codex re-review resolved by Antigravity:
- F2 (High - Markdown precision/types): Fixed by enabling `USE_BIG_DECIMAL_FOR_FLOATS`, using owner-local `ImportPayloadJsonType`, recursively validating frontmatter keys, rejecting complex structures for scalar fields, and canonicalizing note items.
- F3 (Medium - Unquoted CSV whitespace): Fixed by removing `setIgnoreSurroundingSpaces(true)`, trimming non-content fields explicitly, preserving unquoted whitespace in `parsed_payload`, and adhering to canonical Phase 8 target normalization.
- F5 (Medium - Deterministic Feed database-loser contention & truthful evidence): Fixed by driving competing transactions past MVCC pre-checks into PostgreSQL locks on `saved_resources` and `feed_items` observed via `awaitCompetingLock`, proving domain conflict, 1 surviving row, Vault rollback, and secret redaction. Added external_id and url_hash collision races. Corrected evidence names, guard, builder call (`.get()`), scheduling formula, and separated baseline/new-domain/new-architecture counts.
- F6 (Medium - CSV row/header consistency): Fixed by validating `record.size() == headerNames.size()` with item-level `INVALID` (`"Inconsistent column count in CSV record"`) and rejecting duplicate/ambiguous headers with `InvalidImportJobException`.

All 685 tests pass cleanly in `mvn -f backend/pom.xml -ntp clean verify`. Ready for Codex final re-review.

## Initial Codex final review — historical remediation

Outcome: **CHANGES_REQUESTED** on 2026-10-01. Formal findings and independent verification:
[`../phase-10/reviews/2026-10-01-phase-10-final-codex-review.md`](../phase-10/reviews/2026-10-01-phase-10-final-codex-review.md).

Remediations for F1 (privacy), F2 (numeric/structural precision), F3 (whitespace), F4 (saveAndFlush uniqueness translation), F5 (contention tests & evidence) and F6 (CSV row/header validation) are fully implemented and verified.

## Implementation result

Implemented by Antigravity on 2026-10-01.

### 1. Scope & Implementation Summary

- **Module Descriptors & Encapsulation:**
  - `feed/package-info.java`: Declares `@ApplicationModule` with allowed dependencies `vault::entry`, `vault::enums`, `vault::view`, and `knowledge::api`.
  - `feed` exposes named interfaces: `source`, `item`, `resource`, `conversion`, `enums`, `view`.
  - `importdata/package-info.java`: Declares `@ApplicationModule` with allowed dependencies `vault::entry`, `vault::enums`, `vault::view`, and `knowledge::api`. Zero nested Knowledge module access.
  - `importdata` exposes named interfaces: `job`, `enums`, `view`.
  - Added dedicated `package-info.java` descriptors for all subpackages across `feed` and `importdata`. Synchronized package-tree implementation detail in `docs/repository/repository-package-tree.md`.
- **Feed Module (`feed`):**
  - **Enums & Views:** `FeedSourceType` (`GITHUB_TRENDING`, `HACKER_NEWS`, `REDDIT`, `RSS`, `WEBSITE`), `SavedResourceKind` (`ARTICLE`, `REPOSITORY`, `SOCIAL_POST`, `WEB_PAGE`, `OTHER`), `FeedSourceView`, `FeedItemView`, `SavedResourceView`, `SavedResourceConversionView`, `FeedJsonSnapshot`, `InvalidFeedJsonException`. Entity IDs are `Long` / `BIGINT`. Deep snapshot isolation ensures caller-side and view-side immutability for all JSONB maps/lists.
  - **FeedSource Operations:** `FeedSourceOperations`, `FeedSourceService`, `FeedSourceRepository`, `FeedSource` entity. Implements scheduling constraints, enabled defaults, initial `next_fetch_at` null until a fetch occurs, next fetch calculation (`next_fetch_at = (scheduled && refresh_interval_minutes != null && refresh_interval_minutes > 0 && last_fetched_at != null) ? last_fetched_at.plus(Duration.ofMinutes(refresh_interval_minutes)) : null`), side-effect free due reads, and deterministic ordering (`next_fetch_at ASC NULLS FIRST, id ASC`).
  - **FeedItem Operations & Ingestion:** `FeedItemOperations`, `FeedItemService`, `FeedItemRepository`, `FeedItem` entity. Deterministic dual-key upsert (`url_hash`, `external_id`), lowercase SHA-256 URL hashing, cross-row conflict detection, duplicate batch candidate rejection, bounded deterministic querying (`published_at DESC NULLS LAST, id DESC`), flush boundary check, and atomic batch rollback without advancing source timestamps on error.
  - **SavedResource Operations:** `SavedResourceOperations`, `SavedResourceService`, `SavedResourceRepository`, `SavedResource` entity. Atomic Vault `SAVED_RESOURCE` entry creation. Stable conflict detection on duplicate URL hash with `saveAndFlush` inside transaction translation boundary, catching unique violations and rolling back leaving 0 orphan Vault entries. Provenance snapshot retention from feed item ingestion.
  - **SavedResource Conversion:** `SavedResourceConversionOperations`, `SavedResourceConversionService`, `SavedResourceConversionRepository`, `SavedResourceConversion` entity. Converts saved resources to `StudyItem`, `InformationItem`, or `Note` using parent `KnowledgeOperations`. Multiple conversions to distinct targets allowed; atomic target creation + conversion link persistence with whole-transaction rollback on target or provenance failure.
- **ImportData Module (`importdata`):**
  - **Enums & Views:** `ImportTargetType` (`STUDY`, `INFORMATION`, `VOCABULARY`, `NOTE`), `ImportFormat` (`CSV`, `JSON`, `MARKDOWN`), `ImportJobStatus` (`CREATED`, `PARSED`, `VALIDATED`, `IMPORTED`, `CANCELLED`, `FAILED`), `ImportItemStatus` (`VALID`, `DUPLICATE`, `INVALID`, `IMPORTED`, `UPDATED`, `SKIPPED`), `ImportItemDecision` (`IMPORT`, `SKIP`, `UPDATE`), `ImportJobView`, `ImportJobItemView`, `ImportJsonSnapshot`, `InvalidImportJsonException`.
  - **Parsers:**
    - `CsvImportParser`: Uses `org.apache.commons:commons-csv:1.13.0` (`CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setAllowDuplicateHeaderNames(false).get()`). Omitted `.setIgnoreSurroundingSpaces(true)` and `.setTrim(true)`. Unquoted content fields (`contentMarkdown`, `content_markdown`, `description`, `example`, `review`, `meaning`) preserve exact Markdown whitespace (leading indentation, trailing hard breaks, final newlines) verbatim. Headers and non-content fields are explicitly trimmed. Enforces record width consistency (`record.size() == headerNames.size()`), marking inconsistent records as `INVALID` with `"Inconsistent column count in CSV record"`, preserving per-item indices and zero target writes. Validates header names for non-empty, non-blank, and case-insensitively unique names, throwing `InvalidImportJobException("Duplicate or ambiguous header in CSV import")`.
    - `JsonImportParser`: Jackson parser configured with `DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS` enforcing top-level array and object element constraints. Catches syntax errors safely, throwing privacy-safe `InvalidImportJobException("Invalid JSON syntax in import file")` without user payload leakage.
    - `MarkdownImportParser`: Jackson `YAMLFactory` configured with `DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS = true`. Extracts frontmatter with exact raw markdown preservation and filename stem fallback. Recursively validates frontmatter keys via `TargetPayloadCanonicalizer.isValidFrontmatter` (rejecting null, non-string, or blank keys at any nesting level while preserving legitimate unknown frontmatter). Rejects complex structures (lists/maps/arrays) for scalar string frontmatter fields (`title`, `summary`, `sourceName`, `sourceUrl`). Delegates item construction to `TargetPayloadCanonicalizer.canonicalize`.
    - `ImportPayloadJsonType`: Owner-local Hibernate UserType (`@Type(ImportPayloadJsonType.class)`) on `ImportJobItem.parsedPayload` using an `ObjectMapper` configured with `USE_BIG_DECIMAL_FOR_FLOATS = true`, preserving `BigDecimal` precision when loading `import_job_items.parsed_payload` without modifying global Hibernate/Jackson configuration or any other entity/module.
    - `Target Execution & Phase 8 Parity`: When targets are executed via `KnowledgeOperations`, Knowledge applies its canonical Phase 8 domain rules (`NoteService` preserves whitespace verbatim into `notes.content_markdown`; `InformationItemService` applies its frozen `trimOrNull` normalization without modifying the frozen Phase 8 codebase).
  - **ImportJob Operations & Lifecycle:**
    - `ImportJobOperations`, `ImportJobService`, `ImportJobRepository`, `ImportJobItemRepository`, `ImportJob` entity, `ImportJobItem` entity.
    - Lifecycle: `CREATED -> PARSED -> VALIDATED -> IMPORTED`.
    - Serialization guard: `acquireGuardAndRefresh` executes `importJobRepository.findByIdForUpdate(jobId)` followed by `entityManager.refresh(job, LockModeType.PESSIMISTIC_WRITE)` to authoritatively re-read the latest row status from PostgreSQL even when an entity is already managed in 1st-level cache.
    - Transitions: Parse requires `CREATED`; Validate requires `PARSED`; Execute requires `VALIDATED`; Cancel allows `CREATED`, `PARSED`, or `VALIDATED`. Terminal states (`IMPORTED`, `CANCELLED`, `FAILED`) cannot regress or mutate.
    - Execution is whole-job transactional: executes target creation or update via parent `KnowledgeOperations`. Rollback on any target failure cleanly reverts all writes and leaves job in `VALIDATED`.
    - Reads: `findJobItems` bounded and ordered by `item_index ASC`; `findRecentJobs` bounded and ordered by `created_at DESC, id DESC`.
- **Privacy-Safe Constraint Logging:**
  - Conflict exceptions and application logs strictly omit raw URLs, file hashes, secrets, frontmatter, caller-controlled unknown field names, and raw parser error strings.

### 2. Verification & Testing

- **Architecture Tests:** `ApplicationArchitectureTests` (16 tests PASS: 12 baseline + 4 Phase 10) verifying Modulith modular structure, named interface compliance, and dependency restrictions for `feed` and `importdata`.
- **Unit Validation Tests:**
  - `FeedValidationTest` (10 tests PASS): command validation, defensive copies, JSON snapshot roundtripping, cyclic/null-key rejection.
  - `ImportDataValidationTest` (23 tests PASS): CSV quoting/newlines, unknown keys privacy assertions, quoted and unquoted Markdown whitespace preservation, inconsistent column count rejection, duplicate/ambiguous header rejection, JSON array validation, fractional ID rejection, high-precision decimals preservation, non-scalar string rejection, malformed frontmatter rejection, Markdown YAML parsing with decimal precision, recursive blank key rejection, JSON snapshot isolation.
- **PostgreSQL Integration Tests:**
  - `FeedIntegrationTest` (29 tests PASS): schema validation, source scheduling & due ordering, dual-key ingestion upsert/conflicts, batch rollback, concurrent dual-key ingestion races (general, external_id collision, url_hash collision) with observed PostgreSQL lock contention, saved resource duplicate rollback, concurrent manual save race with observed contention, concurrent feed save race with observed contention, trigger-based post-target provenance failure rollback, multi-target conversions, JSON isolation, privacy-safe error logs.
  - `ImportDataIntegrationTest` (20 tests PASS): target/format matrix, full Markdown note lifecycle with duplicate hash detection, CSV study import with mixed decisions, JSON information import, end-to-end execution of `VOCABULARY` target, verbatim quoted and unquoted Note and Information markdown whitespace preservation, high-precision YAML decimal frontmatter persistence, inconsistent column count handling, catastrophic duplicate header failure handling, whole-job rollback on failure, cancellation from pre-import states, terminal state immutability, bounded deterministic queries.
- **Deterministic Lock Contention Tests:**
  - `ImportDataConcurrencyTest` (5 tests PASS):
    - All 5 race tests wrap Thread 2 in `tx2.execute` preloading the entity into its persistence context (`entityManager.find(ImportJob.class, job.id())`) before lock wait, verifying that `entityManager.refresh(job, LockModeType.PESSIMISTIC_WRITE)` forces fresh PostgreSQL state into the cached entity.
    - `executeVsExecuteContention`: Real `pg_locks` ungranted wait. Exactly one execution succeeds; loser re-reads `IMPORTED` and aborts with zero target writes.
    - `executeVsCancelCancelWins`: Cancel acquires lock first; waiting execute unblocks, re-reads `CANCELLED`, and aborts.
    - `executeVsCancelExecuteWins`: Execute acquires lock first; waiting cancel unblocks, re-reads `IMPORTED`, and rejects overwrite.
    - `parseVsCancelFromCreated`: Competing parse and cancel; cancel wins, waiting parse re-reads `CANCELLED` and rejects.
    - `validateVsCancelFromParsed`: Competing validate and cancel; cancel wins, waiting validate re-reads `CANCELLED` and rejects.
- **Test Count Breakdown:**
  - Approved pre-Phase-10 baseline: 594 tests
  - Phase 10 new domain tests: 87 tests (`Feed`: 39, `ImportData`: 48)
  - Phase 10 new architecture tests: 4 tests
  - Total tests run: 685 tests passing with 0 failures, 0 errors, 0 skips.
- **Verification Commands:**
  - `mvn -f backend/pom.xml -ntp clean verify` -> `BUILD SUCCESS` (01:48 min, finished 2026-10-01T18:34:54+07:00).
  - `git diff --check` -> Clean (exit code 0).
  - Graphify refreshed via `scripts/refresh-graphify.ps1`.
- **Evidence Document:** Full evidence recorded in `docs/implementation/phase-10/test-evidence.md`.

## Codex re-review remediation

Implementer remediation for F2, F3, F5, and F6:

1. **F2 (High - Markdown Precision & Types):** Configured YAML mapper with `USE_BIG_DECIMAL_FOR_FLOATS = true`, added owner-local `ImportPayloadJsonType` Hibernate UserType on `ImportJobItem.parsedPayload`, recursively rejected null/blank frontmatter keys, rejected complex structures for scalar fields, and delegated note item creation to `TargetPayloadCanonicalizer.canonicalize`.
2. **F3 (Medium - Unquoted CSV Whitespace):** Removed `.setIgnoreSurroundingSpaces(true)` in `CsvImportParser`, explicitly trimmed non-content fields while preserving content fields (`contentMarkdown`, `description`, etc.) verbatim in `import_job_items.parsed_payload`. Adhered to canonical Phase 8 target normalization upon execution. Added unquoted parse and persisted-entity integration tests.
3. **F5 (Medium - Deterministic Contention & Truthful Evidence):** Coordinated concurrent transactions in `FeedIntegrationTest` via `TransactionTemplate(REQUIRES_NEW)` holding Thread 1 uncommitted, driving Thread 2 into ungranted PostgreSQL unique key locks on `saved_resources` and `feed_items` observed via `awaitCompetingLock`. Thread 1 commits; Thread 2 catches `DataIntegrityViolationException`, translates to domain conflict exception, rolls back losing Vault entry, with zero leaked URLs/secrets. Added external_id and url_hash collision races. Corrected evidence with exact test names (`updatesFeedSourceAndAdjustsNextFetchAt`, `ingestFetchBasic`, etc.), `findByIdForUpdate` guard, `.get()` builder call, truthful contention description, scheduling formula (leaving `next_fetch_at` null until fetch), and separated baseline (594) / new-domain (87) / new-architecture (4) counts.
4. **F6 (Medium - CSV Row & Header Consistency):** Enforced `record.size() == headerNames.size()`, flagging inconsistent column counts as `INVALID` with `"Inconsistent column count in CSV record"`, preserving per-item indices and zero target writes. Validated header names for non-empty, non-blank, and no case-insensitive duplicates, throwing `InvalidImportJobException("Duplicate or ambiguous header in CSV import")`.
