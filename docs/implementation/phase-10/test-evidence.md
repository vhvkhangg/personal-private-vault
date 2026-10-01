# Backend Phase 10 — Test Verification Evidence

- Date: 2026-10-01
- Handoff ID: `phase-10-feed-importdata-foundation`
- Implementer: Antigravity
- Status: READY FOR OWNER COMMIT after Codex acceptance; all findings F1–F7 closed.

## Independent Codex final verification

`mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 685 tests, zero failures/errors/skips;
01:51, finished 2026-10-01T19:35:01+07:00. `git diff --check` was also clean.
Compiler notices include deprecated Commons CSV duplicate-header configuration in `CsvImportParser`,
Lombok/Unsafe terminal deprecation and the existing deprecated test-container API. No IDE inspection or
warning-free claim. See `reviews/2026-10-01-phase-10-final-codex-acceptance.md` for disposition and scope limits.

## Final Verification Command

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:48 min
- **Finished at:** 2026-10-01T18:34:54+07:00

```powershell
git diff --check
```

- **Exit status:** `0` (clean, no whitespace warnings or errors)

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Test Counts and Summary

- **Total tests run:** 685
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Test Count Composition

- **Approved pre-Phase-10 baseline:** 594 tests
- **Phase 10 new domain tests:** 87 tests (`Feed`: 39, `ImportData`: 48)
- **Phase 10 new architecture tests:** 4 tests in `ApplicationArchitectureTests`
- **Total tests:** 594 + 87 + 4 = 685 tests

### Breakdown by Test Class

| Test Class | Test Count | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest` | 36 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest` | 20 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest` | 18 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest` | 2 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest` | 9 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonValidationTest` | 30 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest` | 37 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest` | 13 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmValidationTest` | 44 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmLinkIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmCreditIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest` | 22 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest` | 26 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountValidationTest` | 21 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeValidationTest` | 27 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest` | 18 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionIntegrationTest` | 17 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.feed.FeedValidationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.feed.FeedIntegrationTest` | 29 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.importdata.ImportDataValidationTest` | 23 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.importdata.ImportDataIntegrationTest` | 20 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.importdata.ImportDataConcurrencyTest` | 5 | 0 | 0 | PASS |
| **Total** | **685** | **0** | **0** | **PASS** |

## Focused Phase 10 Tests & Invariant Verification

The 91 tests across Feed, ImportData, and Application Architecture for Phase 10 are detailed below with their exact method names, nested suites, and verified behaviors:

| Test Class / Nested Suite | Test Method | Result | Verified Behavior |
| :--- | :--- | :---: | :--- |
| `ApplicationArchitectureTests` | `verifiesFeedModuleNamedInterfaces` | PASS | Verifies `feed` module exposes only allowed `@NamedInterface` packages (`source`, `item`, `resource`, `conversion`, `enums`, `view`), concealing all `internal.*` implementation. |
| `ApplicationArchitectureTests` | `verifiesImportDataModuleNamedInterfaces` | PASS | Verifies `importdata` module exposes only allowed `@NamedInterface` packages (`job`, `enums`, `view`), concealing all `internal.*` implementation. |
| `ApplicationArchitectureTests` | `verifiesFeedModuleDependencies` | PASS | Enforces that `feed` only depends on allowed public named interfaces of `vault` (`entry`, `enums`, `view`) and `knowledge` (`api`), with no illegal cross-module dependencies. |
| `ApplicationArchitectureTests` | `verifiesImportDataModuleDependencies` | PASS | Enforces that `importdata` only depends on allowed public named interfaces of `vault` (`entry`, `enums`, `view`) and `knowledge` (`api`), with zero nested Knowledge access. |
| `FeedValidationTest$FeedSourceValidationTests` | `rejectsBlankName` | PASS | Rejects null or blank feed source name with `InvalidFeedSourceException`. |
| `FeedValidationTest$FeedSourceValidationTests` | `rejectsOverlongName` | PASS | Rejects feed source name exceeding 255 characters with `InvalidFeedSourceException`. |
| `FeedValidationTest$FeedSourceValidationTests` | `rejectsNullType` | PASS | Rejects null `FeedSourceType` with `InvalidFeedSourceException`. |
| `FeedValidationTest$FeedItemValidationTests` | `constructsNormalizedInputDefensively` | PASS | Constructs `NormalizedFeedItemInput` with defensive copy of metadata map against caller mutation. |
| `FeedValidationTest$SavedResourceValidationTests` | `constructsManualResourceCommandDefensively` | PASS | Constructs `CreateManualSavedResourceCommand` with defensive copy of metadata map. |
| `FeedValidationTest$FeedJsonSnapshotValidationTests` | `handlesNullAndEmpty` | PASS | Returns null for null input and empty unmodifiable map for empty map. |
| `FeedValidationTest$FeedJsonSnapshotValidationTests` | `roundTripsComplexStructure` | PASS | Preserves nested maps, lists, strings, booleans, integers, and floating-point values without data distortion. |
| `FeedValidationTest$FeedJsonSnapshotValidationTests` | `rejectsNullKeys` | PASS | Rejects map containing null key with `InvalidFeedJsonException`. |
| `FeedValidationTest$FeedJsonSnapshotValidationTests` | `rejectsUnsupportedValues` | PASS | Rejects map containing arbitrary non-JSON object values with `InvalidFeedJsonException`. |
| `FeedValidationTest$FeedJsonSnapshotValidationTests` | `rejectsCyclicStructures` | PASS | Detects and rejects cyclic map structures with `InvalidFeedJsonException`. |
| `FeedIntegrationTest$SchemaValidationTests` | `verifiesFeedTablesExist` | PASS | Verifies `feed_sources`, `feed_items`, `saved_resources`, and `saved_resource_conversions` exist in PostgreSQL `public` schema. |
| `FeedIntegrationTest$FeedSourceOperationsTests` | `createsFeedSourceWithDefaults` | PASS | Creates feed source with default enabled=true and retrieves by ID. |
| `FeedIntegrationTest$FeedSourceOperationsTests` | `createsFeedSourceWithScheduledRefresh` | PASS | Creates feed source with scheduled refresh interval; verifies initial `next_fetch_at` remains null until first fetch. |
| `FeedIntegrationTest$FeedSourceOperationsTests` | `updatesFeedSourceAndAdjustsNextFetchAt` | PASS | Applies scalar update to feed source and recalculates `next_fetch_at` from `last_fetched_at` when refresh interval changes. |
| `FeedIntegrationTest$FeedSourceOperationsTests` | `validatesSchedulingConstraints` | PASS | Enforces that enabling scheduled refresh requires positive interval (> 0). |
| `FeedIntegrationTest$FeedSourceOperationsTests` | `dueSourcesOrdering` | PASS | Queries due sources; orders null `next_fetch_at` first, then ascending `next_fetch_at`, then `id ASC`. |
| `FeedIntegrationTest$FeedSourceOperationsTests` | `dueReadIsSideEffectFree` | PASS | Confirms that querying due sources does not mutate timestamps or trigger writes. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `ingestFetchBasic` | PASS | Inserts items on ingestion, computes lowercase SHA-256 `url_hash` of trimmed URL UTF-8 bytes, and advances source `last_fetched_at` and `next_fetch_at`. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `dualKeyUpsert` | PASS | Performs deterministic dual-key upsert: updates existing item when `url_hash` or `external_id` matches. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `dualKeyCrossRowConflict` | PASS | Rejects batch with `FeedItemConflictException` when candidate's `external_id` matches row A but `url_hash` matches row B. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `conflictingCandidatesInSameBatch` | PASS | Rejects batch containing multiple distinct items with the same `url_hash` or `external_id`. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `batchFailureRollback` | PASS | Batch failure rolls back all candidate inserts/updates without advancing source fetch timestamps. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `concurrentDualKeyIngestionConvergesSafely` | PASS | Real PostgreSQL lock contention test: Thread 1 holds uncommitted transaction, Thread 2 drives past MVCC pre-check into PostgreSQL unique index contention on `feed_items`, observed via `awaitCompetingLock`; Thread 2 catches unique violation at `flush`, translates to `FeedItemConflictException`, leaving exactly 1 surviving item and zero leaked URLs/secrets. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `concurrentDualKeyIngestionCollidingOnExternalIdRacesSafely` | PASS | Real PostgreSQL lock contention test: Thread 1 holds uncommitted transaction, Thread 2 collides on `external_id`, blocking on `uq_feed_items_source_external_id` (observed via `awaitCompetingLock`); loser receives `FeedItemConflictException`, leaving exactly 1 item. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `concurrentDualKeyIngestionCollidingOnUrlHashRacesSafely` | PASS | Real PostgreSQL lock contention test: Thread 1 holds uncommitted transaction, Thread 2 collides on `url_hash`, blocking on `uq_feed_items_source_url_hash` (observed via `awaitCompetingLock`); loser receives `FeedItemConflictException`, leaving exactly 1 item. |
| `FeedIntegrationTest$FeedItemIngestionTests` | `readsRecentFeedItemsOrdered` | PASS | Returns items bounded by positive limit, ordered by `published_at DESC NULLS LAST, id DESC`. |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `createsManualSavedResource` | PASS | Creates manual saved resource backed by Vault `SAVED_RESOURCE` entry. |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `createsSavedResourceFromFeedItem` | PASS | Creates saved resource from feed item retaining provenance snapshot (`sourceId`, `feedItemId`, `externalId`, `url`, `title`). |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `duplicateUrlHashRollback` | PASS | Rejects duplicate URL hash with `SavedResourceConflictException` and rolls back transaction leaving 0 orphan Vault entries. |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `concurrentSavedResourceCreationRacesSafely` | PASS | Real PostgreSQL lock contention test: Thread 1 holds uncommitted transaction, Thread 2 drives past MVCC pre-check into PostgreSQL unique index contention on `uq_saved_resources_url_hash`, observed via `awaitCompetingLock`; Thread 2 catches unique violation at `saveAndFlush`, translates to `SavedResourceConflictException`, rolls back its Vault entry leaving exactly 1 surviving saved resource and 1 Vault entry, with zero URL/token leakage in output or logs. |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `concurrentFeedSavedResourceCreationRacesSafely` | PASS | Real PostgreSQL lock contention test: Thread 1 holds uncommitted transaction on feed item save, Thread 2 drives past MVCC pre-check into PostgreSQL unique index contention on `uq_saved_resources_url_hash`, observed via `awaitCompetingLock`; loser translates to `SavedResourceConflictException`, rolls back its Vault entry leaving exactly 1 surviving resource and 1 Vault entry. |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `findsSavedResourceByIdAndHash` | PASS | Finds saved resource by Vault ID and by computed URL hash. |
| `FeedIntegrationTest$SavedResourceOperationsTests` | `findsRecentSavedResources` | PASS | Returns saved resources bounded by positive limit, ordered by `saved_at DESC, id DESC`. |
| `FeedIntegrationTest$SavedResourceConversionTests` | `convertsToStudyItem` | PASS | Converts saved resource to Study item through parent `KnowledgeOperations` and records provenance conversion row. |
| `FeedIntegrationTest$SavedResourceConversionTests` | `convertsToInformationItemAndNote` | PASS | Converts saved resource to Information item and Note, allowing multiple distinct target conversions from same resource. |
| `FeedIntegrationTest$SavedResourceConversionTests` | `conversionFailureRollback` | PASS | Valid target Study item is created; a PostgreSQL trigger fails the provenance insert (`saved_resource_conversions`), proving that entire transaction rolls back both the Study item, the Vault entry, and the conversion row. |
| `FeedIntegrationTest$JsonSnapshotIsolationTests` | `inputMapIsolation` | PASS | Mutating caller input map after entity creation does not mutate stored or retrieved JSON snapshot. |
| `FeedIntegrationTest$JsonSnapshotIsolationTests` | `viewMapImmutability` | PASS | Retrieved view map is unmodifiable and cannot be mutated by caller. |
| `FeedIntegrationTest$PrivacySafeErrorTests` | `conflictExceptionsDoNotLeakRawUrls` | PASS | Verifies that conflict exception messages and application logs contain no raw URLs, hashes, or sensitive tokens. |
| `ImportDataValidationTest$CsvParserTests` | `parsesQuotedFieldsAndNewlines` | PASS | Parses quoted CSV fields, embedded commas, and multi-line fields into canonical payloads. |
| `ImportDataValidationTest$CsvParserTests` | `flagsMissingRequiredFields` | PASS | Flags CSV rows missing required fields as `INVALID` with privacy-safe diagnostic messages. |
| `ImportDataValidationTest$CsvParserTests` | `flagsUnknownColumnsPrivacySafely` | PASS | Flags CSV rows containing unrecognized columns as `INVALID` with stable payload-free error `"Unknown field encountered in item payload"`, asserting synthetic private key names never leak into errors. |
| `ImportDataValidationTest$CsvParserTests` | `preservesMarkdownWhitespace` | PASS | Asserts CSV parser retains leading indentation, trailing spaces, and final newlines verbatim for quoted Markdown content fields. |
| `ImportDataValidationTest$CsvParserTests` | `preservesUnquotedContentWhitespace` | PASS | Asserts CSV parser retains leading indentation, trailing spaces, and newlines verbatim for unquoted content fields while trimming non-content fields. |
| `ImportDataValidationTest$CsvParserTests` | `rejectsInconsistentColumnCountRows` | PASS | Validates record width: rows with extra or missing cells are flagged as `INVALID` with `"Inconsistent column count in CSV record"`, preserving per-item index and zero target writes. |
| `ImportDataValidationTest$CsvParserTests` | `rejectsDuplicateOrAmbiguousHeaders` | PASS | Rejects CSV input containing duplicate or case-insensitively ambiguous header names with `InvalidImportJobException("Duplicate or ambiguous header in CSV import")`. |
| `ImportDataValidationTest$JsonParserTests` | `rejectsNonArrayRoot` | PASS | Rejects non-array root JSON with `InvalidImportJobException("JSON root must be an array")`. |
| `ImportDataValidationTest$JsonParserTests` | `flagsNonObjectArrayElements` | PASS | Flags non-object array elements as `INVALID` import items with safe error message. |
| `ImportDataValidationTest$JsonParserTests` | `flagsUnknownJsonPropertiesPrivacySafely` | PASS | Unknown fields in JSON payloads are marked `INVALID` with stable `"Unknown field encountered in item payload"`, omitting caller key names. |
| `ImportDataValidationTest$JsonParserTests` | `rejectsFractionalIds` | PASS | Enforces exact integral conversions: rejects fractional IDs (e.g. `3.9`) and integer overflow rather than truncating/narrowing them silently. |
| `ImportDataValidationTest$JsonParserTests` | `preservesHighPrecisionDecimals` | PASS | Uses Jackson `USE_BIG_DECIMAL_FOR_FLOATS` to preserve `BigDecimal` precision without lossy double conversion. |
| `ImportDataValidationTest$JsonParserTests` | `rejectsComplexStructuresForScalarFields` | PASS | Rejects arrays/objects supplied for scalar string fields (`title`, `sourceName`, `sourceUrl`, `summary`) with `IllegalArgumentException`. |
| `ImportDataValidationTest$JsonParserTests` | `rejectsMalformedJsonSyntaxPrivacySafely` | PASS | Rejects malformed JSON syntax with privacy-safe `InvalidImportJobException("Invalid JSON syntax in import file")` without payload leakage. |
| `ImportDataValidationTest$MarkdownParserTests` | `extractsFrontmatterAndPreservesRaw` | PASS | Extracts YAML frontmatter while preserving entire raw markdown unchanged in canonical payload. |
| `ImportDataValidationTest$MarkdownParserTests` | `fallsBackToFilenameStem` | PASS | Falls back to filename stem when markdown frontmatter title is omitted. |
| `ImportDataValidationTest$MarkdownParserTests` | `canonicalizerRejectsMalformedFrontmatter` | PASS | Rejects malformed frontmatter structures (empty string keys, non-string keys) as `INVALID` with `"Malformed frontmatter in note item"`. |
| `ImportDataValidationTest$MarkdownParserTests` | `rejectsNonMapFrontmatter` | PASS | Rejects non-map frontmatter (e.g. YAML lists/scalars) as `INVALID` with `"Malformed frontmatter in note item"`. |
| `ImportDataValidationTest$MarkdownParserTests` | `preservesHighPrecisionYamlDecimals` | PASS | Parses YAML frontmatter with `USE_BIG_DECIMAL_FOR_FLOATS`, preserving exact numeric decimal precision in frontmatter map. |
| `ImportDataValidationTest$MarkdownParserTests` | `rejectsComplexStructuresForScalarFrontmatterFields` | PASS | Rejects complex structures (lists/maps/arrays) for scalar frontmatter fields (`summary`, `sourceName`, `sourceUrl`, `title`) as `INVALID`. |
| `ImportDataValidationTest$MarkdownParserTests` | `rejectsBlankAndNestedBlankFrontmatterKeys` | PASS | Recursively inspects frontmatter keys, rejecting blank, null, or empty string keys at top-level or in nested maps as `INVALID`. |
| `ImportDataValidationTest$ImportJsonSnapshotTests` | `deeplyIsolatesInput` | PASS | Deeply isolates maps and lists against caller mutations. |
| `ImportDataValidationTest$ImportJsonSnapshotTests` | `rejectsNullKeysAndCycles` | PASS | Rejects null keys and cyclic map structures with `InvalidImportJsonException`. |
| `ImportDataIntegrationTest$SchemaValidationTests` | `verifiesImportDataTablesExist` | PASS | Confirms `import_jobs` and `import_job_items` exist in PostgreSQL `public` schema. |
| `ImportDataIntegrationTest$TargetAndFormatMatrixTests` | `allowsSupportedMatrix` | PASS | Allows all supported combinations: CSV and JSON for all 4 targets, Markdown for Note only. |
| `ImportDataIntegrationTest$TargetAndFormatMatrixTests` | `rejectsUnsupportedMatrix` | PASS | Rejects unsupported combinations (e.g. Markdown for Study) before job creation with `InvalidImportJobException`. |
| `ImportDataIntegrationTest$MarkdownNoteLifecycleTests` | `fullMarkdownNoteLifecycle` | PASS | Executes full Note lifecycle: `CREATED -> PARSED -> VALIDATED -> IMPORTED` with frontmatter, hash, and parent Knowledge write. |
| `ImportDataIntegrationTest$MarkdownNoteLifecycleTests` | `duplicateNoteDetectionAndUpdate` | PASS | Detects duplicate Note by file hash during validation, and applies `UPDATE` decision to update existing note. |
| `ImportDataIntegrationTest$CsvStudyImportTests` | `parsesQuotedCsvAndExecutes` | PASS | Parses quoted CSV with multiple records, executing mixed `IMPORT` and `SKIP` decisions. |
| `ImportDataIntegrationTest$JsonInformationImportTests` | `parsesJsonAndExecutes` | PASS | Parses JSON array of Information items and executes import via parent Knowledge facade. |
| `ImportDataIntegrationTest$TransactionalRollbackTests` | `wholeJobRollbackOnTargetFailure` | PASS | Simulates target failure during execution; rolls back whole job and target writes atomically, leaving job in `VALIDATED` status. |
| `ImportDataIntegrationTest$CancellationAndTerminalStateTests` | `cancelsFromPreImportStates` | PASS | Cancels import job from `CREATED`, `PARSED`, and `VALIDATED` states. |
| `ImportDataIntegrationTest$CancellationAndTerminalStateTests` | `terminalStatesCannotMutate` | PASS | Prevents mutations on terminal states (`IMPORTED`, `CANCELLED`, `FAILED`) with `InvalidImportTransitionException`. |
| `ImportDataIntegrationTest$BoundedReadsTests` | `findJobItemsOrderedByIndex` | PASS | Returns job items ordered strictly by `item_index ASC`. |
| `ImportDataIntegrationTest$BoundedReadsTests` | `findRecentJobsOrdered` | PASS | Returns jobs bounded by positive limit, ordered by `created_at DESC, id DESC`. |
| `ImportDataIntegrationTest$VocabularyImportTests` | `parsesCsvAndExecutesVocabularyImport` | PASS | End-to-end execution of `VOCABULARY` target: parses CSV, validates, executes IMPORT decision, verifies persisted `vocabulary_items` and `vault_entries` rows in PostgreSQL. |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `preservesNoteMarkdownWhitespaceVerbatimEndToEnd` | PASS | Verifies Note Markdown imported from CSV preserves leading indentation, trailing spaces, and final newlines verbatim end-to-end in `notes.content_markdown`. |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `preservesInformationMarkdownWhitespaceVerbatimEndToEnd` | PASS | Verifies Information Markdown imported from CSV preserves internal indentation and trailing spaces verbatim end-to-end in `information_items.content_markdown`. |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `preservesUnquotedNoteMarkdownWhitespaceVerbatimEndToEnd` | PASS | Verifies unquoted Note Markdown imported from CSV preserves leading indentation and trailing spaces verbatim end-to-end in `notes.content_markdown`. |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `preservesUnquotedInformationMarkdownWhitespaceVerbatimEndToEnd` | PASS | Verifies unquoted Information Markdown imported from CSV preserves leading indentation and trailing spaces verbatim in `import_job_items.parsed_payload`, and upon execution applies Knowledge's canonical Phase 8 Information domain normalization (`trimOrNull`). |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `preservesHighPrecisionYamlDecimalFrontmatterEndToEnd` | PASS | Verifies high-precision decimal numbers in YAML frontmatter (e.g. `123456789.987654321`) are persisted accurately into PostgreSQL JSONB without floating-point distortion. |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `csvWithInconsistentColumnCountProducesInvalidItemAndNoTargetWrites` | PASS | Verifies CSV row with extra column is parsed as `INVALID` with `"Inconsistent column count in CSV record"`, preserving item index and producing zero target writes. |
| `ImportDataIntegrationTest$CsvWhitespacePreservationTests` | `csvWithDuplicateHeadersFailsParseCatastrophically` | PASS | Verifies CSV with duplicate headers fails parse catastrophically with `InvalidImportJobException("Duplicate or ambiguous header in CSV import")`, leaving job in `CREATED` and 0 items imported. |
| `ImportDataConcurrencyTest` | `executeVsExecuteContention` | PASS | Two concurrent execution attempts on same validated job: Thread 1 acquires pessimistic lock and executes; Thread 2 preloads entity into its persistence context, waits on PostgreSQL row lock, unblocks on commit, and re-reads fresh `IMPORTED` state via `refresh(LockModeType.PESSIMISTIC_WRITE)`, rejecting cleanly with zero duplicate target writes. |
| `ImportDataConcurrencyTest` | `executeVsCancelCancelWins` | PASS | Cancel and execute race: cancel acquires lock first; Thread 2 preloads entity, unblocks, observes fresh `CANCELLED` status, and rejects with zero target writes. |
| `ImportDataConcurrencyTest` | `executeVsCancelExecuteWins` | PASS | Cancel and execute race: execute acquires lock first; target is created, job becomes `IMPORTED`; Thread 2 preloads entity, unblocks, observes terminal `IMPORTED` status, and rejects without status overwrite. |
| `ImportDataConcurrencyTest` | `parseVsCancelFromCreated` | PASS | Competing parse and cancel on `CREATED` job: cancel acquires lock first and marks `CANCELLED`; Thread 2 preloads entity, unblocks, observes `CANCELLED`, and rejects with `InvalidImportTransitionException`. |
| `ImportDataConcurrencyTest` | `validateVsCancelFromParsed` | PASS | Competing validate and cancel on `PARSED` job: cancel acquires lock first and marks `CANCELLED`; Thread 2 preloads entity, unblocks, observes fresh `CANCELLED` status, and rejects cleanly. |

## Concurrency & PostgreSQL Lock Contention Analysis

Phase 10 requires deterministic race-safety for Feed item ingestion, SavedResource creation, and same-job lifecycle state mutations (`parse`, `validate`, `execute`, `cancel`).

### Mechanism

1. **Owner-Local Serialization Guard for Import Jobs:**
   - Every mutation in `ImportJobService` enters via `acquireGuardAndRefresh(jobId)`:
     ```java
     ImportJob job = importJobRepository.findByIdForUpdate(jobId)
             .orElseThrow(() -> new ImportJobNotFoundException(jobId));
     entityManager.refresh(job, LockModeType.PESSIMISTIC_WRITE);
     ```
   - `findByIdForUpdate` loads the job with a pessimistic write lock (`SELECT ... FOR UPDATE`).
   - `entityManager.refresh(job, LockModeType.PESSIMISTIC_WRITE)` forces an authoritative database re-read with `SELECT ... FOR UPDATE`, blocking competing transactions until the winner commits, and refreshing 1st-level cache state with committed row values.

2. **Terminal Invariants for Import Jobs:**
   - Status checks occur under the serialization guard:
     - `parse`: requires `CREATED`
     - `validate`: requires `PARSED`
     - `execute`: requires `VALIDATED`
     - `cancel`: requires `CREATED`, `PARSED`, or `VALIDATED`
   - Incompatible states or terminal states (`IMPORTED`, `CANCELLED`, `FAILED`) reject immediately with `InvalidImportTransitionException`.

3. **Preloaded Persistence Context Verification:**
   - In all five tests in `ImportDataConcurrencyTest`, Thread 2 runs inside its own transaction (`tx2.execute`) and calls `entityManager.find(ImportJob.class, job.id())` *before* acquiring the pessimistic lock.
   - This explicitly exercises Hibernate's 1st-level cache: Thread 2 already has a managed instance of `ImportJob` in the initial state. When Thread 1 commits and Thread 2's lock wait completes, `entityManager.refresh(job, LockModeType.PESSIMISTIC_WRITE)` forces the freshly committed status (`IMPORTED` or `CANCELLED`) into the cached instance, preventing any stale cache reads.

4. **Deterministic Feed Unique Constraint Contention Verification:**
   - In `FeedIntegrationTest`, five concurrency tests verify real database contention:
     - `concurrentSavedResourceCreationRacesSafely` (manual save)
     - `concurrentFeedSavedResourceCreationRacesSafely` (feed item save)
     - `concurrentDualKeyIngestionConvergesSafely` (external ID and URL hash race)
     - `concurrentDualKeyIngestionCollidingOnExternalIdRacesSafely` (explicit `external_id` race)
     - `concurrentDualKeyIngestionCollidingOnUrlHashRacesSafely` (explicit `url_hash` race)
   - **Coordination Pattern:**
     - Thread 1 runs in an uncommitted transaction via `TransactionTemplate` with `PROPAGATION_REQUIRES_NEW`, performs the insert (`saveAndFlush` or `flush`), and pauses before commit.
     - Thread 2 attempts the competing creation/ingestion with the same URL hash or external ID. In `READ_COMMITTED` isolation, Thread 2 passes the application MVCC pre-checks because Thread 1 is uncommitted.
     - Thread 2 executes `saveAndFlush` (or `feedItemRepository.flush()`) and blocks inside PostgreSQL on the unique constraint index (`uq_saved_resources_url_hash`, `uq_feed_items_source_url_hash`, or `uq_feed_items_source_external_id`).
     - Real lock contention is proven by polling `pg_locks` and `pg_stat_activity`:
       ```sql
       SELECT count(*) FROM pg_locks l
       JOIN pg_stat_activity a ON l.pid = a.pid
       WHERE NOT l.granted
         AND a.pid != pg_backend_pid()
         AND a.query ILIKE ?
       ```
     - Once contention is observed, Thread 1 is released to commit.
     - Thread 2 unblocks, catches `DataIntegrityViolationException`, and translates it cleanly into `SavedResourceConflictException` or `FeedItemConflictException`.
     - Losing Vault entries are rolled back; exactly 1 surviving row exists in PostgreSQL; output capture confirms zero leaked URLs, hashes, or vendor secrets in error messages or logs.

## Parser Configuration & Rationale

1. **CSV Parsing:**
   - **Library:** `org.apache.commons:commons-csv:1.13.0` added to `backend/pom.xml`.
   - **Configuration:** `CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setAllowDuplicateHeaderNames(false).get()`.
   - **Whitespace Fidelity:** Omitted `.setIgnoreSurroundingSpaces(true)` and `.setTrim(true)`. Unquoted content fields (`contentMarkdown`, `content_markdown`, `description`, `example`, `review`, `meaning`) preserve exact Markdown whitespace (leading indentation, trailing hard breaks, final newlines) verbatim. Headers and non-content fields are explicitly trimmed by policy in `CsvImportParser`.
   - **Row Width & Header Integrity:** Enforces `record.size() == headerNames.size()`. Records with extra or missing cells are flagged as `INVALID` with item diagnostic `"Inconsistent column count in CSV record"`, preserving per-item indices and zero target writes. Header names are verified for non-empty, non-blank, and unique case-insensitive strings; duplicate or ambiguous headers throw `InvalidImportJobException("Duplicate or ambiguous header in CSV import")`.

2. **JSON Parsing:**
   - **Library:** Jackson (`com.fasterxml.jackson.core:jackson-databind`), managed by Spring Boot starter.
   - **Configuration:** `DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS` enabled on `ObjectMapper` to parse floating-point numbers into `BigDecimal` without lossy `double` conversion. Root array and element object constraints enforced. Syntax errors caught and wrapped in privacy-safe `InvalidImportJobException("Invalid JSON syntax in import file")` without echoing input fragments.
    - **Hibernate JSON Type Precision (Owner-Local):** Implemented owner-local Hibernate UserType `ImportPayloadJsonType` (`@Type(ImportPayloadJsonType.class)`) on `ImportJobItem.parsedPayload`, configuring its local `ObjectMapper` with `DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS`. This preserves high-precision numbers (`BigDecimal`) when loading `import_job_items.parsed_payload` without modifying global Hibernate/Jackson configuration or any other entity/module.

3. **Markdown / YAML Parsing:**
   - **Library:** Jackson Dataformat YAML (`com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.3`) via `new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)`.
   - **Frontmatter Structural Validation:** Frontmatter is parsed into `Map<String, Object>`. Frontmatter keys are recursively validated via `TargetPayloadCanonicalizer.isValidFrontmatter` (rejecting null, non-string, or blank keys at top-level and in nested structures). Scalar string fields (`title`, `summary`, `sourceName`, `sourceUrl`) reject complex structures (`Map`, `Collection`, arrays). Raw markdown body is preserved verbatim.

4. **Target Canonicalization & Frozen Phase 8 Parity:**
   - `TargetPayloadCanonicalizer.canonicalize` performs owner-local structural normalization:
     - Exact range-checked integral conversions (`toExactLong`, `toExactInteger`) rejecting fractional numbers and overflow.
     - Preserves `BigDecimal` numeric precision.
     - Unknown fields emit stable constant diagnostic `"Unknown field encountered in item payload"`, never echoing caller-supplied keys.
   - `CsvImportParser` preserves unquoted content markdown verbatim in `import_job_items.parsed_payload`. When targets are executed via `KnowledgeOperations`, Knowledge applies its canonical Phase 8 domain rules (`NoteService` preserves whitespace verbatim into `notes.content_markdown`; `InformationItemService` applies its frozen `trimOrNull` normalization without modifying the frozen Phase 8 codebase).

## Privacy-Safe Constraint Logging & Error Handling

- All exceptions (`FeedItemConflictException`, `SavedResourceConflictException`, `InvalidFeedSourceException`, `InvalidImportJobException`, `InvalidImportTransitionException`) emit structured, safe error messages.
- Error messages identify entities by `Long` ID (bigint) or safe business keys (`sourceId`, `jobId`, `itemIndex`), explicitly omitting raw URLs, file hashes, external IDs, frontmatter content, caller-controlled unknown field names, or vendor secrets.
- Unknown fields encountered in item payloads produce stable constant diagnostic `"Unknown field encountered in item payload"`, never echoing caller-supplied keys.
- Catastrophic JSON/CSV syntax errors produce `"Failed to parse import content due to syntax or format error"` and `"Invalid JSON syntax in import file"`, never exposing parser input excerpts.
- Verified using `OutputCaptureExtension` in `FeedIntegrationTest$PrivacySafeErrorTests` and `FeedIntegrationTest$SavedResourceOperationsTests` to ensure zero log leakage under contention.

## Compiler & Runtime Diagnostic Notices

- **Lombok / sun.misc.Unsafe:** JDK 25 outputs a terminal deprecation warning for `sun.misc.Unsafe` methods invoked by Lombok / ByteBuddy during class generation.
- **Dynamic Java Agent Loading:** ByteBuddy / Mockito triggers a JVM warning about dynamic agent loading (`-XX:+EnableDynamicAgentLoading`).
- **AbstractPostgresIntegrationTest:** Testcontainers invokes deprecated container API in the test harness (pre-existing from Phase 0).
