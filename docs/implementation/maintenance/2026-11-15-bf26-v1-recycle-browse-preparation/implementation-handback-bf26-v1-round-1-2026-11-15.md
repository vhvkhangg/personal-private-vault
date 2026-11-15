# Implementation Handback — BF26-V1-RECYCLE-BROWSE (Round 1 Remediation)

**Date:** 2026-11-15  
**Handoff ID:** `BF26-V1-RECYCLE-BROWSE`  
**Implementer:** Antigravity (`backend-implementer`)  
**Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`  
**Owner Authority:** [owner-authorization-bf26-v1-2026-11-15.md](owner-authorization-bf26-v1-2026-11-15.md) at commit `757643e9adcafa0c54b0adb3cca93cfa8efd0731`  
**Executable Contract:** [executable-handoff-bf26-v1-2026-11-15.md](executable-handoff-bf26-v1-2026-11-15.md)  
**Independent Review Findings Addressed:** `FR-V1-01` through `FR-V1-05` from [2026-11-15-bf26-v1-independent-final-review.md](reviews/2026-11-15-bf26-v1-independent-final-review.md)  
**Remediation budget:** Round 1 of 3 consumed (`1/3 consumed`); takeover `NOT_AUTHORIZED / NOT_USED`.

---

## 1. Branch, HEAD, and Diff Inventory

- **Branch / HEAD:** `main` at `58cbd58d1f3fe6be002ba97eb6599292a758f8f5` (clean entry checkout).
- **Exact Scoped Boundaries (9 production + 4 test files, 0 edits outside boundary):**
  - **Modified Production Files (5):**
    - `P01`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java`
    - `P02`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java`
    - `P03`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java`
    - `P06`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/controller/VaultController.java`
    - `P09`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/mapper/VaultWebMapper.java`
  - **New Production Files (4):**
    - `P04`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultTrashCursor.java`
    - `P05`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/view/VaultTrashPageView.java`
    - `P07`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/dto/VaultTrashItemResponse.java`
    - `P08`: `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/dto/VaultTrashPageResponse.java`
  - **Modified Test Files (4):**
    - `T01`: `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultEntryIntegrationTest.java`
    - `T02`: `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultWebIntegrationTest.java`
    - `T03`: `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java`
    - `T04`: `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java`

---

## 2. Tool & Environment Details

- **OS:** Windows 11 Pro 64-bit (`10.0`, amd64)
- **JDK:** Oracle Java 25 GA (`25+37-LTS-3491`, mixed mode, sharing)
- **Maven:** Apache Maven 3.10.0 (`c43a36b8d67be7e0805a411bc0898af1a51f5472`)
- **Database:** Testcontainers PostgreSQL image `ppv-rag-postgres:18.6-pgvector0.8.6` (PostgreSQL 18.6 via Flyway migrations)
- **Execution Watchdog:** `scripts/run-bounded-command.py` with 300.0s hard timeout profile

---

## 3. Exact Source and Test SHA-256 Hashes

| File | Exact Path | SHA-256 Hash |
|---|---|---|
| `P01` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java` | `7f59cf804f75ba255bdec3afc707556844e2e53ebee593bf33a2cb7c3c8d0132` |
| `P02` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java` | `640262f35fdc18a6120f4713e9fc274bb1ab6ffe8cd11d44ae88e4acf57084a7` |
| `P03` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | `d2cae08c950b3ffc1fb0924b2d63a2a1d3838e7bebd6f0deaefe72e9cd0a16c6` |
| `P04` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultTrashCursor.java` | `551f0c581e64e2f9b64781e6296a9d7cbf3c13c7934a27a8ec8edd46f1c780f8` |
| `P05` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/view/VaultTrashPageView.java` | `7992766050bef2e082e7681ef6ff9c51b7f99c7b0a34d294e6fca731a504e1a6` |
| `P06` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/controller/VaultController.java` | `7a0797636f38522cb517c8881f194c0aaf4b1786204e8ef6a167336aeaa31617` |
| `P07` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/dto/VaultTrashItemResponse.java` | `c513939d75ad945913d7e0ba9b81a74ae149bc56cc7c863b84bad449cfca3171` |
| `P08` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/dto/VaultTrashPageResponse.java` | `b20b75b3fb270d509587ed3d1d8bd375ee2c372019b9d51a1f4dc5dd38003f6b` |
| `P09` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/mapper/VaultWebMapper.java` | `b7f2ca8c3ab11ff41dce2e685647f01f8a80580a5c7da19e48ef97c96049c9f9` |
| `T01` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultEntryIntegrationTest.java` | `a3f855de6f67c0de2bb13f21fa6926ceb9bd2e9641bb1882124c0baa3e08a32f` |
| `T02` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultWebIntegrationTest.java` | `c23edffff9e6ff3a31436d7318fadc5bdecf237b84ad771bdc3ece409420ce9f` |
| `T03` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java` | `5df70e38f92a2320feeb578d36c88dcbc0f44ef7aec04ea4d97168375d1d8981` |
| `T04` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | `c2f01e2c3ae43fe710404e0b394d27a934a08c686b082add85d8b43eb58dfd72` |

---

## 4. Resolution of Independent Review Findings (FR-V1-01 to FR-V1-05)

### FR-V1-01: Error Classification and Transaction Completion
- **Issue:** `VaultController` caught any `IllegalArgumentException` as 400 `INVALID_ARGUMENT`. An operational `IllegalArgumentException` or completion failure after query execution was incorrectly mapped to 400 instead of 500.
- **Resolution:**
  - Added public static `InvalidTrashInputException extends IllegalArgumentException` in `VaultEntryOperations.java`.
  - Updated `VaultEntryService.java` (limit validation) and `VaultTrashCursor.java` (cursor decoding) to throw `InvalidTrashInputException` strictly for bad user inputs.
  - In `VaultController.java`, explicitly caught `InvalidTrashInputException` for 400 `INVALID_ARGUMENT`. Other exceptions fall through to `catch (Exception ex)` which returns safe 500 `INTERNAL_ERROR` with `Cache-Control: no-store`.
  - In `VaultWebIntegrationTest#operationalAndCompletionFailuresHttp`, proved:
    1. Operational `IllegalArgumentException` thrown after real read produces 500 `INTERNAL_ERROR` with `Cache-Control: no-store`.
    2. Operational `IllegalStateException` produces 500 `INTERNAL_ERROR` with `Cache-Control: no-store`.
    3. Operational `DataIntegrityViolationException` produces 500 `INTERNAL_ERROR` with `Cache-Control: no-store`.
    4. Transaction completion failure (`TransactionSystemException` injected via proxy after read) produces 500 `INTERNAL_ERROR` with `Cache-Control: no-store`.

### FR-V1-02: PostgreSQL Query & Execution Plan Bound Proof
- **Issue:** Handwritten EXPLAIN SQL did not capture the actual query and bounds; fixture lacked distributed active gaps.
- **Resolution:**
  - In `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof`:
    1. Seeded 40 active entries with 6 distributed sparse deleted entries across active IDs (`[5, 12, 19, 26, 33, 39]`).
    2. Proved initial database fetch bounds: repository `findTrashPage(null, PageRequest.of(0, 3))` returns exactly 3 items (`limit+1`) matching the highest 3 deleted IDs.
    3. Proved continuation fetch bounds: passing `cursorId` returns exactly the next 3 items (`limit+1`), all strictly `< cursorId`.
    4. Captured and printed `EXPLAIN (ANALYZE, COSTS OFF)` on initial and continuation queries showing `Limit -> Index Scan Backward using vault_entries_pkey on vault_entries` with shared buffer hits = 2 and execution time ~0.03ms.
    5. Proved service `listTrash(2, null)` materializes exactly 2 items with non-null `nextCursor`.

### FR-V1-03: Boundary & Negative Test Fidelity
- **Issue:** Missing witnesses for all 17 enum types, populated boundaries, codec negatives, no-business-query/no-write checks, and expired session headers.
- **Resolution:**
  - **All 17 types:** In `VaultEntryIntegrationTest#stableTraversalAcrossMultiplePagesWithActiveGaps`, seeded and trashed all 17 `VaultEntryType` enum values with interleaved active gaps; traversed across multiple pages with strict ID-descending order, proving no active items appear and complete set is returned.
  - **Populated boundaries:** In `VaultEntryIntegrationTest#limitBoundsValidation` and `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`, tested default (25), minimum (1), and maximum (100) on a populated 30-item fixture; tested exactly-limit (5) with lookahead exhausted (`nextCursor: null`), and limit+1 (6) with continuation. Asserted meaningful values and presence of nullable keys.
  - **Codec negatives:** In `VaultEntryIntegrationTest#cursorValidationRejections` and `VaultWebIntegrationTest#cursorValidationHttp`, added malformed UTF-8, noncanonical Base64url (`djF8MTQy==`), positive long boundary `Long.MAX_VALUE`, and 19-digit overflow boundary `"9223372036854775808"`.
  - **No business query / no writes:** In `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp` and `#cursorValidationHttp`, verified `@MockitoSpyBean` `vaultEntryRepository.findTrashPage` is never invoked on invalid input. In `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire` and `#limitParsingAndRangeValidationHttp`, verified domain row counts before and after read operations are strictly unchanged.
  - **Expired token & headers:** In `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness`, verified an otherwise valid expired token with `profile=PRIVATE` claims yields 401 with `Cache-Control: no-store` before handler. Verified 403 (PUBLIC origins), 401 (Anonymous), and 401 (Invalid token) all carry `Cache-Control: containsString("no-store")`.

### FR-V1-04: OpenAPI Required Semantics
- **Issue:** `VaultTrashPageResponse` required only `items`; `VaultTrashItemResponse` required no fields; test did not check required-field semantics.
- **Resolution:**
  - In `VaultTrashItemResponse.java`, annotated `id`, `entryType`, and `deletedAt` with `@Schema(requiredMode = Schema.RequiredMode.REQUIRED)`.
  - In `VaultTrashPageResponse.java`, annotated `nextCursor` with `@Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true)`.
  - In `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds`:
    1. Asserted `VaultTrashPageResponse` requires `["items", "nextCursor"]` and `nextCursor` permits null.
    2. Asserted `VaultTrashItemResponse` has exactly 3 properties and requires `["id", "entryType", "deletedAt"]`.
    3. Added negative schema validation proving missing `nextCursor`, `id`, `entryType`, or `deletedAt` fails schema validation.
    4. Validated real populated and empty HTTP specimens against the generated schema.

### FR-V1-05: Evidence Provenance & Accurate Reporting
- **Issue:** Handback mapped requirements to inaccurate method names, shifted requirement IDs, and omitted receipt details.
- **Resolution:**
  - Restored exact canonical requirement table V1-01 to V1-19.
  - Mapped every requirement to actual final method names in source and surefire XML.
  - Captured actual observed wire, OpenAPI, plan, and error specimens (with `fieldErrors: []` and exact `Access is denied`).
  - Disclosed full receipt ledger including failed compilation/assertion attempts alongside passing runs.

---

## 5. Requirement-to-Evidence Mapping (Canonical V1-01 to V1-19)

| Canonical Req ID | Canonical Requirement Summary | Exact Test Class & Method Name | Watchdog Receipt & Surefire XML |
|---|---|---|---|
| **V1-01** | Exactly one additional GET `/api/v1/vault/trash`; existing entry/trash/restore handlers unchanged; no new PUBLIC/mutation/search route | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`<br>`ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` | `V1-HTTP-r3.json`<br>`V1-SEC-r2.json`<br>`TEST-...VaultWebIntegrationTest.xml` |
| **V1-02** | Persisted PRIVATE valid request 200; both persisted PUBLIC origins 403 ACCESS_DENIED before handler/read; anonymous/invalid/expired 401; eight original PUBLIC GET pairs unchanged | `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` | `V1-SEC-r2.json`<br>`TEST-...ProfileRouteAuthorizationIntegrationTest.xml` |
| **V1-03** | Actual HTTP data always has items/explicit nullable nextCursor, error/meta null; each item exactly three correctly typed properties | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`<br>`OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds` | `V1-HTTP-r3.json`<br>`V1-API.json`<br>`TEST-...VaultWebIntegrationTest.xml` |
| **V1-04** | Empty trash and all-active Vault each return 200, items [], nextCursor null | `VaultEntryIntegrationTest#emptyTrashAndAllActiveVaultProduceEmptyPage`<br>`VaultWebIntegrationTest#emptyTrashHttpReturns200WithEmptyList` | `V1-DB-r2.json`<br>`V1-HTTP-r3.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-05** | Omitted limit 25; 1/100 valid; output ≤limit and DB qualifying fetch ≤limit+1; no count/offset/unbounded paging | `VaultEntryIntegrationTest#limitBoundsValidation`<br>`VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`<br>`VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof` | `V1-DB-r2.json`<br>`V1-HTTP-r3.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-06** | 0/-1/101/non-integer/decimal/huge overflow limit yields safe canonical 400, no business repository read | `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`<br>`VaultEntryIntegrationTest#limitBoundsValidation` | `V1-HTTP-r3.json`<br>`V1-DB-r2.json`<br>`TEST-...VaultWebIntegrationTest.xml` |
| **V1-07** | Absent cursor starts, canonical roundtrip works; malformed/blank/padded/ambiguous/invalid UTF-8/version/components/ID/overlong values yield safe 400 | `VaultEntryIntegrationTest#cursorValidationRejections`<br>`VaultWebIntegrationTest#cursorValidationHttp` | `V1-DB-r2.json`<br>`V1-HTTP-r3.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-08** | Stable membership across ≥3 pages, all supported Vault enum types and active gaps: strictly descending IDs, no duplicate/skip, active excluded | `VaultEntryIntegrationTest#stableTraversalAcrossMultiplePagesWithActiveGaps` | `V1-DB-r2.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-09** | Last-emitted cursor, non-null iff lookahead exists; one/exactly-limit/limit+1/empty/exhausted boundaries correct | `VaultEntryIntegrationTest#stableTraversalAcrossMultiplePagesWithActiveGaps`<br>`VaultEntryIntegrationTest#limitBoundsValidation`<br>`VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire` | `V1-DB-r2.json`<br>`V1-HTTP-r3.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-10** | A genuinely committed restore between pages removes later membership while old cursor bound stays strict, no earlier-ID repetition | `VaultEntryIntegrationTest#committedRestoreBetweenPagesWeakConsistency` | `V1-DB-r2.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-11** | Committed re-trash retains ID/lifecycle rules; changed membership does not make read write/loop/lock; weak traversal semantics documented | `VaultEntryIntegrationTest#committedReTrashWeakConsistency` | `V1-DB-r2.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-12** | Actual PostgreSQL seek/filter/order/limit+1 with bound parameters, no count/offset/Java whole-table filter; representative sparse-trash plan captured | `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof` | `V1-DB-r2.json`<br>`TEST-...VaultEntryIntegrationTest.xml` |
| **V1-13** | Valid/invalid read makes no domain writes; real query and transaction/completion faults → safe 500 INTERNAL_ERROR with data: null, no-store | `VaultWebIntegrationTest#operationalAndCompletionFailuresHttp`<br>`VaultEntryIntegrationTest#committedReTrashWeakConsistency` | `V1-HTTP-r3.json`<br>`V1-DB-r2.json`<br>`TEST-...VaultWebIntegrationTest.xml` |
| **V1-14** | Existing trash/restore idempotence/timestamps, mutation locks and source/reservation guards retained | `VaultEntryIntegrationTest` (5 existing methods)<br>`VaultMutationConcurrencyTest` (13 methods) | `V1-DB-r2.json`<br>`V1-REG.json`<br>`TEST-...VaultMutationConcurrencyTest.xml` |
| **V1-15** | Existing Search trash exclusion and metadata rules retained; no title, Person detail, source content, NSFW flag, tags/favorite/rating/notes in list | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`<br>`VaultMetadataIntegrationTest` (11 methods)<br>`GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` | `V1-HTTP-r3.json`<br>`V1-REG.json`<br>`TEST-...GlobalSearchIntegrationTest.xml` |
| **V1-16** | Actual generated docs: one new GET, unique listVaultTrash, correct refs/page/item/parameters/default/bounds/security and canonical 200/400/401/403/500 | `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds` | `V1-API.json`<br>`TEST-...OpenApiRouteInventoryIntegrationTest.xml` |
| **V1-17** | All successful PRIVATE responses and applicable generated errors carry Cache-Control: no-store; conditional requests do not return 304 or bodyless success | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`<br>`VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`<br>`VaultWebIntegrationTest#conditionalHeadersDoNotReturn304`<br>`VaultWebIntegrationTest#operationalAndCompletionFailuresHttp`<br>`ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` | `V1-HTTP-r3.json`<br>`V1-SEC-r2.json`<br>`TEST-...VaultWebIntegrationTest.xml` |
| **V1-18** | Modulith boundaries, package ownership, ADR-0016, and dependency graph intact. Zero edits outside 9+4 boundary. | `ApplicationArchitectureTests` (38 methods) | `V1-ARCH.json`<br>`TEST-...ApplicationArchitectureTests.xml` |
| **V1-19** | Exact SHA-256 hashes, test evidence, surefire reports, and watchdog receipts recorded; full receipt disclosure including failed attempts | Recorded in this handback, self-review, and `logs/bf26-v1/impl-20261115-r1/` | Section 3 and Section 6 of this handback |

---

## 6. Full Verification Receipt Ledger

All runs executed under `scripts/run-bounded-command.py` with 300s timeout profile:

| Group | Target Selector | Attempt / Receipt Path | Duration | Exit Code | Result / Details |
|---|---|---|---|---|---|
| **V1-DB** | `VaultEntryIntegrationTest` | `V1-DB.json` | 35.08s | 1 | FAILED (compilation error: missing `java.util.Set` import in `VaultEntryIntegrationTest.java`) |
| **V1-DB** | `VaultEntryIntegrationTest` | `V1-DB-r2.json` | 57.09s | 0 | **PASS** (13/13 tests pass; PostgreSQL EXPLAIN plan printed) |
| **V1-HTTP** | `VaultWebIntegrationTest` | `V1-HTTP.json` | 37.09s | 1 | FAILED (assertion error: `countBefore` captured before test inserts in `limitParsingAndRangeValidationHttp`) |
| **V1-HTTP** | `VaultWebIntegrationTest` | `V1-HTTP-r2.json` | 8.55s | 1 | FAILED (compilation error: duplicate closing brace `}` at line 359 in `VaultWebIntegrationTest.java`) |
| **V1-HTTP** | `VaultWebIntegrationTest` | `V1-HTTP-r3.json` | 58.59s | 0 | **PASS** (7/7 tests pass; wire format, negative inputs, operational 500, no domain writes) |
| **V1-SEC** | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | `V1-SEC.json` | 65.11s | 1 | FAILED (assertion error: exact string match failed because Spring Security emits `"no-cache, no-store, max-age=0, must-revalidate"`) |
| **V1-SEC** | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | `V1-SEC-r2.json` | 86.61s | 0 | **PASS** (28/28 tests pass: 16 in `ProfileRouteAuthorizationIntegrationTest` + 12 in `SecurityFilterChainIntegrationTest`) |
| **V1-API** | `OpenApiRouteInventoryIntegrationTest` | `V1-API.json` | 35.58s | 0 | **PASS** (1/1 test passes; validates all 240 routes, required properties, negative schema, HTTP specimens) |
| **V1-ARCH** | `ApplicationArchitectureTests` | `V1-ARCH.json` | 27.56s | 0 | **PASS** (38/38 tests pass; Modulith and ArchUnit rules pass without violation) |
| **V1-REG** | `VaultMutationConcurrencyTest,VaultMetadataIntegrationTest,GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` | `V1-REG.json` | 33.08s | 0 | **PASS** (25/25 tests pass: 13 concurrency + 11 metadata + 1 global search) |

---

## 7. Case Accounting

- **Test Breakdown across suites:**
  - `VaultEntryIntegrationTest` (13 methods):
    - 5 existing: `createSetsActiveIdentityAndTimestamps`, `createWithNullTypeThrowsIllegalArgumentException`, `findBehavior`, `trashAndRestoreLifecycleWithDeterministicClock`, `missingTargetAndNullIdThrowExpectedExceptions`.
    - 8 added: `emptyTrashAndAllActiveVaultProduceEmptyPage`, `limitBoundsValidation`, `cursorValidationRejections`, `stableTraversalAcrossMultiplePagesWithActiveGaps`, `postgresPlanAndSqlBoundProof`, `committedRestoreBetweenPagesWeakConsistency`, `committedReTrashWeakConsistency`, `viewImmutability`.
  - `VaultWebIntegrationTest` (7 methods):
    - 1 existing: `vaultLifecycleOverHttp`.
    - 6 added: `trashHttpSuccessAndEnvelopeWire`, `emptyTrashHttpReturns200WithEmptyList`, `limitParsingAndRangeValidationHttp`, `cursorValidationHttp`, `conditionalHeadersDoNotReturn304`, `operationalAndCompletionFailuresHttp`.
  - `ProfileRouteAuthorizationIntegrationTest` (16 methods):
    - 15 existing: `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `exactAuthMethodsAndInfrastructureDoNotGrantPublicBusinessAccess`, `publicProfileDeniedOnUnknownRoutes`, `privateProfileReceives404OnUnknownRoutes`, `anonymousRequestYields401`, `publicAuthEndpointsAccessible`, `errorAndAsyncDispatcherRequiresPrivate`, `actualAsyncRedispatchRevalidatesPropagatedContextWithoutBearer`, `localOnDriveAndGoogleRoutesDeniedToPublicToken`, `dpAOpenApiSchemasAndErrorsMatchActualContract`, `sameUriAsyncAndErrorDispatcherDeniesPublicSession`, `methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths`, `directAndNestedFilmCreditDeniedToPublicWithPopulatedParent`, `sessionLifecycleAndMutationOnNewlyAccessibleDetailRoutes`, `sensitiveExportDeniedToPublicPermittedToPrivate`.
    - 1 added: `vaultTrashRouteAuthorizationWitness`.
  - `SecurityFilterChainIntegrationTest` (12 existing methods).
  - `OpenApiRouteInventoryIntegrationTest` (1 method):
    - `openApiContractContainsAllRoutesAndUniqueOperationIds` (validating all 240 endpoints).
  - `ApplicationArchitectureTests` (38 existing rules).
  - `VaultMutationConcurrencyTest` (13 existing methods).
  - `VaultMetadataIntegrationTest` (11 existing methods).
  - `GlobalSearchIntegrationTest` (1 targeted method: `verifiesRecycleBinExclusion`).
- **Total test executions across all 6 verification groups:** **112 passing tests, 0 failures, 0 errors, 0 skipped**.

---

## 8. Observed Actual Wire, Plan, and Error Specimens

### A. Success Specimen (`200 OK`)
Captured from `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`:
```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store

{
  "data": {
    "items": [
      {
        "id": 142,
        "entryType": "FICTION",
        "deletedAt": "2026-11-15T07:13:20.123456Z"
      }
    ],
    "nextCursor": null
  },
  "error": null,
  "meta": null
}
```

### B. Empty Trash Specimen (`200 OK`)
Captured from `VaultWebIntegrationTest#emptyTrashHttpReturns200WithEmptyList`:
```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store

{
  "data": {
    "items": [],
    "nextCursor": null
  },
  "error": null,
  "meta": null
}
```

### C. Public Profile Forbidden Specimen (`403 Forbidden`)
Captured from `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness`:
```http
HTTP/1.1 403 Forbidden
Content-Type: application/json
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

{
  "data": null,
  "error": {
    "code": "ACCESS_DENIED",
    "message": "Access is denied",
    "fieldErrors": []
  },
  "meta": null
}
```

### D. Invalid Limit Specimen (`400 Bad Request`)
Captured from `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`:
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
Cache-Control: no-store

{
  "data": null,
  "error": {
    "code": "INVALID_ARGUMENT",
    "message": "Limit must be between 1 and 100",
    "fieldErrors": []
  },
  "meta": null
}
```

### E. Operational Error Translation Specimen (`500 Internal Server Error`)
Captured from `VaultWebIntegrationTest#operationalAndCompletionFailuresHttp`:
```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Cache-Control: no-store

{
  "data": null,
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "An unexpected error occurred",
    "fieldErrors": []
  },
  "meta": null
}
```

### F. Observed PostgreSQL Query Execution Plan
Captured from `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof` (`logs/bf26-v1/impl-20261115-r1/V1-DB-r2.stdout.log`):

**Initial Query Plan:**
```text
ACTUAL_SPARSE_TRASH_PLAN_INITIAL:
Limit (actual time=0.019..0.023 rows=3.00 loops=1)
  Buffers: shared hit=2
  ->  Index Scan Backward using vault_entries_pkey on vault_entries (actual time=0.017..0.021 rows=3.00 loops=1)
        Filter: (deleted_at IS NOT NULL)
        Rows Removed by Filter: 13
        Index Searches: 1
        Buffers: shared hit=2
Planning Time: 0.080 ms
Execution Time: 0.033 ms
```

**Continuation Query Plan:**
```text
ACTUAL_SPARSE_TRASH_PLAN_CONTINUATION:
Limit (actual time=0.013..0.016 rows=3.00 loops=1)
  Buffers: shared hit=2
  ->  Index Scan Backward using vault_entries_pkey on vault_entries (actual time=0.013..0.015 rows=3.00 loops=1)
        Index Cond: (id < '52'::bigint)
        Filter: (deleted_at IS NOT NULL)
        Rows Removed by Filter: 21
        Index Searches: 1
        Buffers: shared hit=2
Planning Time: 0.066 ms
Execution Time: 0.026 ms
```

### G. Generated OpenAPI Schema Specimen
Verified in `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds`:
```json
{
  "VaultTrashPageResponse": {
    "type": "object",
    "required": ["items", "nextCursor"],
    "properties": {
      "items": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/VaultTrashItemResponse"
        }
      },
      "nextCursor": {
        "type": "string",
        "nullable": true,
        "description": "Opaque continuation cursor for next page, or null if exhausted"
      }
    }
  },
  "VaultTrashItemResponse": {
    "type": "object",
    "required": ["id", "entryType", "deletedAt"],
    "properties": {
      "id": {
        "type": "integer",
        "format": "int64"
      },
      "entryType": {
        "type": "string",
        "enum": ["ACCOUNT", "ALBUM", "BOOKMARK", "CONTACT", "CREDENTIAL", "DOCUMENT", "EVENT", "EXTERNAL_ACCOUNT", "FICTION", "FILM", "IMAGE", "NOTE", "ORGANIZATION", "PERSON", "SAVED_RESOURCE", "TASK", "TRANSACTION"]
      },
      "deletedAt": {
        "type": "string",
        "format": "date-time"
      }
    }
  }
}
```

---

## 9. Preserved Limits & NOT_VERIFIED Scope

- **NOT_VERIFIED:**
  - Production deployment and live cutover verification (out of testcontainer scope).
  - Real Owner production dataset sizes (fixtures verified up to representative synthetic sets).
  - Unrelated features: Hard delete, restore-all, purge jobs, and UI rendering (not authorized in V1).
- **CRITICAL / HIGH / MEDIUM Findings:** Zero open. Findings `FR-V1-01` through `FR-V1-05` have been addressed and verified with passing executable evidence.
- **LOW / INFO Backlog:** DPROB-L01/L02, DPROA-L01, DPB2W1-L01, DPB1-L02, DPA-L01 remain OPEN in milestone backlog.

---

## 10. Docs Sync Check

- **UPDATED:**
  - `docs/implementation/maintenance/2026-11-15-bf26-v1-recycle-browse-preparation/self-review-bf26-v1-round-1-2026-11-15.md`
  - `docs/implementation/maintenance/2026-11-15-bf26-v1-recycle-browse-preparation/implementation-handback-bf26-v1-round-1-2026-11-15.md`
  - `docs/implementation/handoffs/ACTIVE.md`
  - `docs/implementation/maintenance/2026-11-15-bf26-v1-recycle-browse-preparation/executable-handoff-bf26-v1-2026-11-15.md`
- **ALREADY_CONSISTENT:** All preparation documents, ADRs, schema, and root security configs.
- **DEFERRED_OUT_OF_SCOPE:** Routine root navigation / package tree / README updates (reserved for ChatGPT governance sync after Owner publication).
- **NOT_VERIFIED:** Production deployment suitability, live cutover, real Owner datasets.

---

Next step: Codex | $codex-final-review | BF26-V1-RECYCLE-BROWSE + docs/implementation/maintenance/2026-11-15-bf26-v1-recycle-browse-preparation/executable-handoff-bf26-v1-2026-11-15.md and docs/implementation/handoffs/ACTIVE.md | IMPLEMENTED_AWAITING_CODEX_REVIEW; remediation round 1 complete; all 6 verification groups passed; remediation 1/3 consumed; takeover NOT_AUTHORIZED / NOT_USED.
