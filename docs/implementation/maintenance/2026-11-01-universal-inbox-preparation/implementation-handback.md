# Implementation handback — Universal Inbox browsing (UI26-INBOX-BROWSE)

- **Handoff ID:** `UI26-INBOX-BROWSE`
- **Implemented by:** Antigravity (`backend-implementer`)
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Date:** 2026-11-01
- **Remediation rounds consumed:** `2/3` (correction round 2 completed; takeover not authorized)
- **Target review skill:** Codex `$codex-final-review`

---

## 1. Finding Register & Remediation History

| Finding ID | Severity | Disposition / Remedy Implemented | Verification & Evidence | Status |
| --- | --- | --- | --- | --- |
| **UI26-FR-01** | MEDIUM | **OpenAPI terminal cursor schema & parameter bound:** Added `@Schema(nullable = true)` to `nextCursor` in `SavedResourceInboxPageResponse.java` and `@Parameter(schema = @Schema(maxLength = 512))` on `cursor` parameter in `SavedResourceController.java`. | `OpenApiRouteInventoryIntegrationTest`, independent re-review 1 | `CLOSED (Codex Re-Review 1)` |
| **UI26-FR-02** | MEDIUM | **Cursor timestamp from database projection tuple:** Stored database-read timestamps from `CandidateSummary.getSavedAt()` in `candidateSavedAtMap` in `SavedResourceService.java`, deriving lookahead cursor timestamp strictly from database projection tuple. | `SavedResourceInboxIntegrationTest`, independent re-review 1 | `CLOSED (Codex Re-Review 1)` |
| **UI26-FR-03** | MEDIUM | **Mandatory test evidence & documentation accuracy:** Added missing qualification negative, five-kind DB tests, changed limit / removed anchor, read-only transaction wrapping, HTTP keyset flow, nested metadata, and log privacy proofs. | `SavedResourceInboxIntegrationTest`, `SavedResourceInboxWebIntegrationTest`, independent re-review 1 | `CLOSED (Codex Re-Review 1)` |
| **UI26-FR-L01** | LOW | **Trailing blank lines at EOF:** Removed trailing blank lines in touched files; `git diff --check` passes cleanly with exit code 0. | `git diff --check`, independent re-review 1 | `CLOSED (Codex Re-Review 1)` |
| **UI26-FR-04** | MEDIUM | **OpenAPI integer limit schema & numeric default:** In `SavedResourceController.java`, removed the explicit `@Schema` override on `limit` parameter, allowing Springdoc to naturally infer `type: integer`, `format: int32`, numeric `default: 50`, `exclusiveMinimum: 0`, and `maximum: 100` from `int limit`, `@Positive`, and `@Max(100)`. In `OpenApiRouteInventoryIntegrationTest.java`, added strict non-coercing assertions verifying `type == "integer"`, `format == "int32"`, numeric integer node `default.isInt() == true` with value 50, and `maximum == 100`. | `OpenApiRouteInventoryIntegrationTest`, `SavedResourceInboxWebIntegrationTest` | `OPEN / PENDING CODEX REVIEW` |
| **UI26-FR-L02** | LOW | **Corrected test method aliases:** Updated all test method names in this handback to match the actual source tests (`trashedDuplicateUrlConflictsWithoutResurrection`, `duplicateUrlHashRollback`, `legacyReadsStillReturnTrashedSavedResources`, `vaultFavoriteAndTagOnSavedResource`, `committedTrashBeforeQualificationIsExcluded`). | Source inspection of `FeedIntegrationTest.java` and `SavedResourceInboxIntegrationTest.java` | `RESOLVED` |
| **UI26-FR-L03** | LOW | **Regression fixture hardening:**<br>1. In `SavedResourceInboxWebIntegrationTest.java`, added `.param("kind", "ARTICLE")` to matching-kind negative cursor requests (year > 9999, year < 0001, overflow ID, zero ID, unsupported version) so filter mismatch does not pre-empt timestamp and ID validations. In `errorMessagesDoNotLeakCredentialsOrSql`, added matching kind and asserted that neither `secretToken` nor `invalidCursor` appears in the response body or captured logs.<br>2. In `SavedResourceInboxIntegrationTest.java`, updated `generatedCursorMustUseDatabaseTupleEvenWithManagedCreationEntity` to use the deterministic controlled sub-microsecond fixture (`.123456100Z` and `.123456789Z`) with `ReflectionTestUtils.setField` and `entityManager.flush()`, confirming that the decoded cursor timestamp matches the persisted `.123457Z` microsecond database tuple. | `SavedResourceInboxWebIntegrationTest`, `SavedResourceInboxIntegrationTest` | `RESOLVED` |

---

## 2. Acceptance Matrix (UI-01 through UI-10)

| Requirement ID | Observable Behavior | Test Coverage | Test Receipt / Evidence | Status |
| --- | --- | --- | --- | --- |
| **UI-01** | Manual and Feed-item saves create one `SavedResource` sharing its Vault entry ID; trimmed URL SHA-256 hash remains unique; duplicate saves fail with conflict; concurrent saves race safely; trashed duplicate still conflicts without resurrecting or merging. | `FeedIntegrationTest.SavedResourceOperationsTests.trashedDuplicateUrlConflictsWithoutResurrection`, `duplicateUrlHashRollback`, `concurrentSavedResourceCreationRacesSafely` | `feed-compatibility.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-02** | Legacy reads (`findRecentSavedResources`, `findSavedResourceById`, `findSavedResourceByUrlHash`) and Knowledge conversions (`convertToStudyItem`, `convertToInformation`, `convertToNote`) retain shapes, HTTP codes, and semantics; legacy reads continue to return trashed resources. | `FeedIntegrationTest.SavedResourceOperationsTests.legacyReadsStillReturnTrashedSavedResources`, `FeedIntegrationTest.SavedResourceConversionTests` | `feed-compatibility.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-03** | `GET /api/v1/saved-resources/inbox` returns bounded page (default 50, max 100), optional kind filter, strict `(saved_at DESC, id DESC)` ordering, and opaque Base64url continuation cursor; empty list returns `items: []` and `nextCursor: null`. Non-empty terminal page returns items with `nextCursor: null`. Cursor timestamp derives strictly from database-read candidate tuple. | `SavedResourceInboxIntegrationTest` (18 real DB tests including `generatedCursorMustUseDatabaseTupleEvenWithManagedCreationEntity`), `SavedResourceInboxWebIntegrationTest` (15 tests including `httpKeysetPaginationAndTerminalNullCursor`) | `inbox-database.json`, `inbox-http-openapi.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-04** | Inbox excludes trashed resources via `VaultSearchOperations.filterQualifyingActiveEntries`. Multi-batch leading trash (>100 items), interleaved trash, and trash committed before batch qualification never cause false exhaustion or hide eligible items; restored entries re-appear at original timestamp; missing qualification result is excluded; all five kinds filter at database level; anchor removal and changed limit continue safely. | `SavedResourceInboxIntegrationTest` (`inboxExcludesTrashedResources`, `leadingTrashOverHundredRowsHandledWithoutExhaustion`, `interleavedTrashAcrossMultipleBatchesQualifiesCorrectly`, `committedTrashBeforeQualificationIsExcluded`, `restoredResourceReappearsInInbox`, `missingQualificationResultExcluded`, `allFiveKindsFilterCorrectlyAtDatabase`, `changedPageSizeAndRemovedAnchorRow`) | `inbox-database.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-05** | Vault actions (favorite, tag attach/detach) operate cleanly on saved resource IDs without duplicate state or affecting Inbox sort order. | `FeedIntegrationTest.SavedResourceOperationsTests.vaultFavoriteAndTagOnSavedResource` | `feed-compatibility.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-06** | Endpoint requires Bearer token (401 on missing/invalid token); URL, title, and metadata round-trip as untrusted JSON data without execution or SSRF fetching; non-HTTP URLs accepted without network egress; error messages and logs never leak tokens or SQL. | `SavedResourceInboxWebIntegrationTest` (`unauthenticatedInboxReturns401`, `invalidTokenReturns401`, `untrustedStringRoundTripsSafely`, `untrustedStringWithNestedMetadataRoundTripsSafely`, `nonHttpSchemeManualUrlAcceptedSafely`, `errorMessagesDoNotLeakCredentialsOrSql`, `invalidCursorErrors`) | `inbox-http-openapi.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-07** | Feed module owns inbox read and persistence context; cross-module access uses public `VaultSearchOperations.filterQualifyingActiveEntries`; explicit DTOs and `ApiResponse` envelope; no schema changes, migrations, or module allowlist changes. | `ApplicationArchitectureTests`, `SavedResourceController`, `FeedWebMapper` | `architecture-archive-boundary.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-08** | Archival guards preserved; no archived QA endpoints, model inference, worker launch, or runtime downloads. | `RagQaArchiveGuardTest` (3 MVC exclusion tests) | `architecture-archive-boundary.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-09** | Route inventory updated from 230 to 231; OpenAPI documents `findSavedResourceInbox`, parameters (limit as integer with numeric default 50, cursor with maxLength 512), 200 response with `SavedResourceInboxPageResponse`, nullable terminal `nextCursor`, and error envelopes; architecture docs updated. | `OpenApiRouteInventoryIntegrationTest`, `docs/architecture/universal-inbox-api-extension.md`, `docs/architecture/api-architecture.md` | `inbox-http-openapi.json` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |
| **UI-10** | Implementer self-review completed; handback maps UI-01–10 to receipts; canonical status files prepared for independent review; no agent commit or push. | Antigravity self-review checklist in this document, `ACTIVE.md`, `executable-handoff-2026-11-01.md` | `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/` | `IMPLEMENTED_AWAITING_CODEX_REVIEW` |

---

## 3. Bounded Verification Receipts & Metrics

Execution run directory: `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/`
Tooling: `scripts/run-bounded-command.py` with `--profile normal --timeout-seconds 300`

| Stage | Selected Tests | Exit Code | Duration (s) | Tests Run | Failures | Errors | Skipped | Receipt File |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `inbox-database` | `SavedResourceInboxCursorTest`, `SavedResourceInboxIntegrationTest` | `0` | 70.7s | 40 | 0 | 0 | 0 | `inbox-database.json` |
| `feed-compatibility` | `FeedIntegrationTest`, `FeedValidationTest` | `0` | 34.7s | 42 | 0 | 0 | 0 | `feed-compatibility.json` |
| `inbox-http-openapi` | `SavedResourceInboxWebIntegrationTest`, `FeedWebIntegrationTest`, `OpenApiRouteInventoryIntegrationTest` | `0` | 39.5s | 17 | 0 | 0 | 0 | `inbox-http-openapi.json` |
| `architecture-archive-boundary` | `ApplicationArchitectureTests`, `RagQaArchiveGuardTest` | `0` | 31.0s | 40 | 0 | 0 | 0 | `architecture-archive-boundary.json` |
| **Total** | **4 stages** | **ALL 0** | **175.9s** | **139** | **0** | **0** | **0** | **All 4 PASS** |

---

## 4. Cursor Format & Codec Implementation Details

- **Class:** `com.vhvkhangg.personalprivatevault.feed.internal.application.SavedResourceInboxCursor` (package-private).
- **Format:** `v1|<KIND|ALL>|<saved_at_iso_utc>|<id>`
  - Uses pipe `|` delimiter.
  - Example: `v1|ARTICLE|2026-11-01T12:00:00Z|42` or `v1|ALL|2026-11-01T12:00:00.123456Z|101`.
  - Encoded with `Base64.getUrlEncoder().withoutPadding()`.
- **Validation bounds:**
  - Length cap: strictly `<= 512` characters before Base64 decoding.
  - Header: version must be literal `v1`.
  - Filter consistency: cursor kind must equal requested `kind` (or `ALL` when no kind specified).
  - Timestamp range: must parse via ISO-8601 UTC and satisfy SQL range `0001-01-01T00:00:00Z <= instant <= 9999-12-31T23:59:59.999999999Z`.
  - ID range: must be positive signed 64-bit integer (`id > 0`).
  - Codec errors: all decode/parse failures throw standard `IllegalArgumentException` with safe static messages, mapped to `400 INVALID_ARGUMENT` by existing global web exception advice.

---

## 5. Query Batching & Persistence Context Protection

- **Candidate Query:** Uses Spring Data JPA interface-based projection `CandidateSummary(Long getId(), Instant getSavedAt())` on `SavedResourceRepository`.
- **Keyset Tuple Seek:**
  - First page: `ORDER BY sr.savedAt DESC, sr.id DESC LIMIT :limit`
  - Continuation: `WHERE (sr.savedAt < :savedAt OR (sr.savedAt = :savedAt AND sr.id < :id)) ORDER BY sr.savedAt DESC, sr.id DESC LIMIT :limit`
- **Database Projection Timestamp Retention:**
  - `SavedResourceService` records each candidate's `savedAt` projection value in `candidateSavedAtMap`.
  - The continuation cursor token derives its timestamp from `candidateSavedAtMap.get(lastReturnedId)`, guaranteeing the cursor timestamp reflects the database projection tuple even in the presence of caller-managed entities.
- **Batch Qualification:**
  - Qualified via `VaultSearchOperations.filterQualifyingActiveEntries(candidateIds, Set.of())`.
  - Candidates scanned in batches of `<= 100` IDs.
  - Entities are loaded by ID only for the final returned page subset (`limit`), preventing persistence context bloat during extensive trash scans.

---

## 6. Changed Files

### Production Code (Additive / Reuse):
1. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/resource/SavedResourceOperations.java` (added `findInboxSavedResources` read signature).
2. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/view/SavedResourceInboxPageView.java` (new immutable record).
3. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/SavedResourceInboxCursor.java` (new cursor codec with pipe delimiter and SQL range bounds).
4. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/SavedResourceRepository.java` (added `CandidateSummary` projection and keyset methods).
5. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/SavedResourceService.java` (implemented `findInboxSavedResources` with database projection timestamp map).
6. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/SavedResourceController.java` (added `GET /api/v1/saved-resources/inbox` endpoint with natural integer limit schema and OpenAPI cursor bound).
7. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/SavedResourceInboxPageResponse.java` (OpenAPI page response DTO with `nullable = true` on `nextCursor`).
8. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/mapper/FeedWebMapper.java` (added `toResponse(SavedResourceInboxPageView)`).

### Test Suite:
9. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/application/SavedResourceInboxCursorTest.java` (22 unit tests).
10. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxIntegrationTest.java` (18 PostgreSQL integration tests with deterministic sub-microsecond fixture).
11. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxWebIntegrationTest.java` (15 MockMvc integration tests with hardened matching-kind and token leakage checks).
12. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedIntegrationTest.java` (added trashed duplicate, legacy trashed read, vault action compatibility tests).
13. `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` (updated route manifest 230 -> 231 and added strict integer limit schema / numeric default / cursor bound assertions).

### Documentation:
14. `docs/architecture/universal-inbox-api-extension.md` (design, endpoint specification, non-empty terminal page semantics, and live-list qualification limits).
15. `docs/architecture/api-architecture.md` (linked Section 17 Universal Inbox extension).

---

## 7. Docs Sync Check

- **`UPDATED`**:
  - `docs/architecture/universal-inbox-api-extension.md`: Detailed documentation of `GET /api/v1/saved-resources/inbox`, non-empty terminal page semantics, and live-list qualification limits.
  - `docs/architecture/api-architecture.md`: Section 17 added and cross-referenced.
  - `docs/implementation/handoffs/ACTIVE.md`: Status set to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation `2/3`.
  - `docs/implementation/maintenance/2026-11-01-universal-inbox-preparation/executable-handoff-2026-11-01.md`: Implementation result and status set to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation `2/3`.
- **`ALREADY_CONSISTENT`**:
  - `docs/repository/repository-package-tree.md`: All classes fit cleanly into existing Feed packages (`feed/resource`, `feed/view`, `feed/internal/application`, `feed/internal/infrastructure/persistence`, `feed/internal/web`).
  - `docs/architecture/module-dependency-matrix.md`: Allowed dependency `feed -> vault::search` already exists in `package-info.java`.
  - Schema v1 and DBML: No database migrations or schema alterations introduced.
- **`DEFERRED_OUT_OF_SCOPE`**:
  - Root `AGENTS.md:1` and `docs/workflow/current-owner-delivery-policy.md:3` dated governance headers (owned by ChatGPT/Owner post-closeout).
- **`NOT_VERIFIED`**:
  - Execution performance under massive production datasets (>100k items) without dedicated indexes (noted in handoff, out of scope for this reuse-only slice).
  - Owner git commit/publish.

---

## 8. Mandatory Implementer Self-Review Verification

- [x] **Owner Requirement Compliance:** All 10 requirements (UI-01 through UI-10) implemented and verified with focused real tests.
- [x] **Remediation Completeness (Round 2):** Resolved blocking finding `UI26-FR-04` (integer schema / numeric default 50 for limit), updated test method aliases (`UI26-FR-L02`), and hardened regression fixtures (`UI26-FR-L03`).
- [x] **No Unrequested Scope / YAGNI:** No read/archive mutations, unread flags, title/notes editing, full count queries, or new database tables.
- [x] **Clean Architecture & SOLID:** Modulith boundaries respected; `VaultSearchOperations` public interface reused; no entity leakage into wire contracts; immutable views and records used throughout.
- [x] **Negative & Edge Cases:** Tested 0/1/100/101 items, timestamp ties, fractional seconds, leading trash >100 rows, interleaved trash, managed-entity caller transaction with deterministic sub-microsecond fixture, malformed/oversized/out-of-range cursors with matching kind filters, and filter mismatch.
- [x] **Security & Privacy:** Bearer authentication enforced; untrusted string and nested metadata payloads safely serialized; no secret tokens, encoded cursors, or SQL echoed in errors or logs (`errorMessagesDoNotLeakCredentialsOrSql`).
- [x] **Receipt Integrity:** All 4 bounded test receipts captured in `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/` without timeouts or retries.
- [x] **No Commit / Staging:** Git working tree remains unstaged; no commit, push, or tag executed.

---

**Next step:** Codex | `$codex-final-review` | `UI26-INBOX-BROWSE`, executable handoff and implementation handback linked above | verify remediation of UI26-FR-04, UI26-FR-L02, and UI26-FR-L03 before Owner commit | 2/3 consumed; next correction/re-review 3/3 if requested; takeover not authorized.
