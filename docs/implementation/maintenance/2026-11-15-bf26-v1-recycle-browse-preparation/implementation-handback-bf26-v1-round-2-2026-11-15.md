# Implementation Handback — BF26-V1-RECYCLE-BROWSE (Round 2 Remediation)

**Date:** 2026-11-15  
**Handoff ID:** `BF26-V1-RECYCLE-BROWSE`  
**Implementer:** Antigravity (`backend-implementer`)  
**Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`  
**Owner Authority:** [owner-authorization-bf26-v1-2026-11-15.md](owner-authorization-bf26-v1-2026-11-15.md) at commit `757643e9adcafa0c54b0adb3cca93cfa8efd0731`  
**Executable Contract:** [executable-handoff-bf26-v1-2026-11-15.md](executable-handoff-bf26-v1-2026-11-15.md)  
**Independent Review Findings Addressed:** `FR-V1-01`, `FR-V1-02`, `FR-V1-03`, `FR-V1-05` from [2026-11-15-bf26-v1-independent-final-re-review-1.md](reviews/2026-11-15-bf26-v1-independent-final-re-review-1.md); `FR-V1-04` was independently closed in re-review 1.  
**Remediation budget:** Round 2 of 3 consumed (`2/3 consumed`); takeover `NOT_AUTHORIZED / NOT_USED`.

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
| `T01` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultEntryIntegrationTest.java` | `70a0e3d6b30810d7f88c30121e94c170bd4c00464c54835671f66d9e932ded53` |
| `T02` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultWebIntegrationTest.java` | `f9a9c508ab271e016e396892e4c39e874b5447bf9fb19fa5f7693c164bd10e4f` |
| `T03` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java` | `989037b48e094b931a954b192fd066a9e79bb7f3622f1fee6fac5a826a075f42` |
| `T04` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | `551a361315d2360028032b4f5cad4950ed60efedfb549fa939eb46dd6f7c0b6e` |

---

## 4. Resolution of Independent Re-Review 1 Findings (FR-V1-01 to FR-V1-05)

### FR-V1-01: Physical Transaction Completion Phase Witness
- **Finding:** Throwing `TransactionSystemException` inside a Mockito answer for `listTrash` executed inside the method invocation, not in the physical completion phase after successful bounded read.
- **Round 2 Resolution:**
  - In `VaultWebIntegrationTest#operationalAndCompletionFailuresHttp` (Step 3):
    1. Executed the actual real service method via `invocation.callRealMethod()`.
    2. Positively observed and asserted successful bounded repository acquisition and materialization: `assertThat(result).isNotNull(); readMaterialized.set(true);`.
    3. Injected a labelled fault during the physical transaction completion phase via a scoped `TransactionSynchronization.beforeCommit(readOnly)` hook, which throws `TransactionSystemException("Simulated transaction completion failure in beforeCommit")` and records `completionHookExecuted.set(true)`.
    4. Guarded against connection/lock leakage by setting `AbstractPlatformTransactionManager.setRollbackOnCommitFailure(true)` for the duration of the fault injection. This ensures `AbstractPlatformTransactionManager` cleanly executes `doRollback` and cleans up the JPA connection context upon commit failure, allowing subsequent test fixture operations (e.g. `@AfterEach cleanUpDatabase()`) to execute without blocking on uncommitted table locks.
    5. Asserted that `readMaterialized.get()` is true and `completionHookExecuted.get()` is true.
    6. Verified HTTP response is safe 500 `INTERNAL_ERROR` with `Cache-Control: no-store` and `data: null`.

### FR-V1-02: Actual Captured Hibernate SQL, Bind Parameters, and Correlated Execution Plan
- **Finding:** Handwritten `String seekSql = "SELECT ... LIMIT 3"` passed to JdbcTemplate EXPLAIN did not prove the actual generated Hibernate SQL or bind parameters.
- **Round 2 Resolution:**
  - In `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof`:
    1. Attached Logback `ListAppender` instances to `org.hibernate.SQL` (level `DEBUG`) and `org.hibernate.orm.jdbc.bind` (level `TRACE`).
    2. Executed real service `listTrash(2, null)` (initial read) and `listTrash(2, continuationCursor)` (continuation read) against the distributed sparse-trash fixture (40 active + 6 deleted items).
    3. Captured actual generated Hibernate SQL:
       `select ve1_0.id,ve1_0.entry_type,ve1_0.created_at,ve1_0.updated_at,ve1_0.deleted_at from vault_entries ve1_0 where ve1_0.deleted_at is not null and (? is null or ve1_0.id<?) order by ve1_0.id desc fetch first ? rows only`
    4. Captured actual bind parameters:
       - Initial read: parameter 1 (BIGINT) `[null]`, parameter 2 (BIGINT) `[null]`, parameter 3 (INTEGER) `[3]` (`limit + 1`).
       - Continuation read: parameter 1 (BIGINT) `[52]`, parameter 2 (BIGINT) `[52]`, parameter 3 (INTEGER) `[3]` (`limit + 1`).
    5. Formatted the exact parameter-corresponding captured statement for PostgreSQL EXPLAIN:
       - Initial: `EXPLAIN (ANALYZE, COSTS OFF) select ve1_0.id,ve1_0.entry_type,ve1_0.created_at,ve1_0.updated_at,ve1_0.deleted_at from vault_entries ve1_0 where ve1_0.deleted_at is not null and (NULL::bigint is null or ve1_0.id<NULL::bigint) order by ve1_0.id desc fetch first 3 rows only`
       - Continuation: `EXPLAIN (ANALYZE, COSTS OFF) select ve1_0.id,ve1_0.entry_type,ve1_0.created_at,ve1_0.updated_at,ve1_0.deleted_at from vault_entries ve1_0 where ve1_0.deleted_at is not null and ('52'::bigint is null or ve1_0.id<'52'::bigint) order by ve1_0.id desc fetch first 3 rows only`
    6. Correlated the EXPLAIN plan directly to this captured statement:
       - Initial: `Limit -> Index Scan Backward using vault_entries_pkey on vault_entries ve1_0 (actual time=0.011..0.013 rows=3.00 loops=1)`, `Filter: (deleted_at IS NOT NULL)`, `Execution Time: 0.021 ms`, `shared hit=2`.
       - Continuation: `Limit -> Index Scan Backward using vault_entries_pkey on vault_entries ve1_0 (actual time=0.013..0.015 rows=3.00 loops=1)`, `Index Cond: (id < '52'::bigint)`, `Filter: (deleted_at IS NOT NULL)`, `Execution Time: 0.029 ms`, `shared hit=2`.

### FR-V1-03: Boundary Gaps, Codec Negatives, Observer Qualification, Domain Row State, and Expired Session Claims
- **Finding:** Missing maximum page boundary (e.g. 101 items), unpadded ambiguous Base64url token rejection, positively qualified repository observer, row-level domain state comparison before vs after, and expired token lacking required session claims (`sid`, `generation`).
- **Round 2 Resolution:**
  - **Maximum boundary (101 items):** In `VaultEntryIntegrationTest#limitBoundsValidation` and `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`, seeded 101 trashed `NOTE` items. Requested `limit=100`. Verified page 1 returns exactly 100 items with non-null `nextCursor`, and page 2 returns the remaining 1 item with `nextCursor: null`.
  - **Unpadded ambiguous Base64url rejection:** In `VaultEntryIntegrationTest#cursorValidationRejections` and `VaultWebIntegrationTest#cursorValidationHttp`:
    - Added valid canonical unpadded Base64url control: `v1|1` -> `"djF8MQ"` (returns 200 OK).
    - Added unpadded ambiguous Base64url: `"djF8MR"` (decodes to `v1|1` but has unused bit set, violating canonical encoding). Strictly rejected with 400 `INVALID_ARGUMENT` before any business query is executed.
  - **Positively qualified repository observer:** In `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`, positively verified `verify(vaultEntryRepository).findTrashPage(...)` on valid requests before asserting `never()` on invalid limit requests.
  - **Domain row state comparison:** In `VaultEntryIntegrationTest#committedReTrashWeakConsistency`, `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`, and `VaultWebIntegrationTest#cursorValidationHttp`:
    - Replaced count-only checks with row-level state comparison:
      `SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id`
    - Verified `assertThat(stateAfterReads).isEqualTo(stateBeforeReads)`, proving zero DML writes, updates, or deletions occurred during read or invalid-input handling.
  - **Otherwise-valid expired session token with real admitted claims:** In `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness`:
    - Decoded the genuine admitted `privateToken` from bootstrap login.
    - Extracted the real admitted `sid` and `generation` claims.
    - Constructed an otherwise-valid expired token containing all required claims: `profile=PRIVATE`, `scope=PRIVATE`, `sid`, `generation`, `sub`, `iss`, with `exp` set to past.
    - Verified HTTP 401 with `Cache-Control: containsString("no-store")`, `error.code == "AUTHENTICATION_REQUIRED"`, and verified `never()` on `handlerAdapter` and `vaultEntryOperations`.

### FR-V1-04: OpenAPI Required Semantics (CLOSED in Re-Review 1)
- Independently verified and closed by reviewer in re-review 1.
- `VaultTrashPageResponse`: `items` and `nextCursor` are marked REQUIRED; `nextCursor` is marked nullable (`requiredMode = Schema.RequiredMode.REQUIRED, nullable = true`).
- `VaultTrashItemResponse`: `id`, `entryType`, and `deletedAt` are marked REQUIRED (`requiredMode = Schema.RequiredMode.REQUIRED`).
- Verified by `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds`.

### FR-V1-05: Accurate Observed Synthetic Specimens and Complete Receipt Disclosure
- **Finding:** Handback specimens contradicted test fixture (FICTION vs FILM, limit error message text, uncaptured synthetic specimens).
- **Round 2 Resolution:**
  - Updated Section 8 with actual stdout captures directly from fresh test runs:
    - 200 OK wire response: reflects `FILM` enum matching seed fixture in `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`.
    - 400 Bad Request invalid limit: reflects `"message":"Invalid argument provided"`, matching the controlled handler output in `VaultController.java`.
    - 401 Expired session: reflects `"message":"Full authentication is required to access this resource"`.
    - 403 Forbidden: reflects `"message":"Access is denied"`.
    - 500 Operational error: reflects `"message":"An unexpected error occurred"`.
    - Captured actual generated OpenAPI schemas: shows OAS 3.1 `["string", "null"]` nullable representation for `nextCursor` and required properties `["items", "nextCursor"]`.
  - Disclosed all execution attempts, including initial compilation failure (`V1-DB.json`) and timeout failure (`V1-HTTP.json`), in Section 6.

---

## 5. Canonical Requirements Verification Matrix (V1-01 to V1-19)

| Req ID | Canonical Requirement | Executable Method Reference | Result |
|---|---|---|---|
| **V1-01** | Single PRIVATE-only `GET /api/v1/vault/trash` | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`, `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` | **PASS** |
| **V1-02** | PRIVATE 200; PUBLIC 403 ACCESS_DENIED; anonymous/invalid/expired 401 AUTHENTICATION_REQUIRED; headers no-store | `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` | **PASS** |
| **V1-03** | Envelope `{data: {items, nextCursor}, error: null, meta: null}`; item `{id, entryType, deletedAt}` | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`, `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** |
| **V1-04** | Empty trash / all-active vault yields items `[]`, nextCursor `null`, 200 OK | `VaultEntryIntegrationTest#emptyTrashAndAllActiveVaultProduceEmptyPage`, `VaultWebIntegrationTest#emptyTrashHttpReturns200WithEmptyList` | **PASS** |
| **V1-05** | Default limit 25; bounds [1, 100]; fetch limit+1; max boundary (101 items) verified | `VaultEntryIntegrationTest#limitBoundsValidation`, `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`, `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof` | **PASS** |
| **V1-06** | Invalid limit (0, -1, 101, text, overflow) -> 400 INVALID_ARGUMENT; repository `never()` with positive observer qualification | `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp`, `VaultEntryIntegrationTest#limitBoundsValidation` | **PASS** |
| **V1-07** | Opaque cursor `v1|<id>`; reject malformed, padded, ambiguous (`djF8MR`), noncanonical, overflow before query; zero domain writes | `VaultEntryIntegrationTest#cursorValidationRejections`, `VaultWebIntegrationTest#cursorValidationHttp` | **PASS** |
| **V1-08** | Multi-page traversal across all 17 enum types with active gaps; strict descending ID order | `VaultEntryIntegrationTest#stableTraversalAcrossMultiplePagesWithActiveGaps` | **PASS** |
| **V1-09** | Continuation cursor non-null iff lookahead exists; boundaries empty, 1, exactly-limit, limit+1, exhausted | `VaultEntryIntegrationTest#stableTraversalAcrossMultiplePagesWithActiveGaps`, `#limitBoundsValidation`, `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire` | **PASS** |
| **V1-10** | Committed restore between pages removes item from subsequent page without ID repetition | `VaultEntryIntegrationTest#committedRestoreBetweenPagesWeakConsistency` | **PASS** |
| **V1-11** | Committed re-trash retains ID; weak traversal; row-level domain state comparison confirms no writes | `VaultEntryIntegrationTest#committedReTrashWeakConsistency` | **PASS** |
| **V1-12** | Captured Hibernate SQL, bind parameters, and correlated EXPLAIN plan on distributed sparse fixture | `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof` | **PASS** |
| **V1-13** | No domain writes; completion fault in `TransactionSynchronization.beforeCommit` -> safe 500 INTERNAL_ERROR with data: null, no-store | `VaultWebIntegrationTest#operationalAndCompletionFailuresHttp`, `VaultEntryIntegrationTest#committedReTrashWeakConsistency` | **PASS** |
| **V1-14** | Existing trash/restore idempotence, concurrency locks, and timestamp semantics preserved | `VaultEntryIntegrationTest` (5 methods), `VaultMutationConcurrencyTest` (13 methods) | **PASS** |
| **V1-15** | Global Search trash exclusion preserved; zero leak of title, Person detail, or metadata in trash list | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`, `VaultMetadataIntegrationTest` (11 methods), `GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` | **PASS** |
| **V1-16** | OpenAPI 3.1 generated documentation: operationId `listVaultTrash`, required lists, nullable cursor | `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** (Closed re-review 1) |
| **V1-17** | `Cache-Control: no-store` on all responses and errors; conditional headers do not yield 304 | `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire`, `#limitParsingAndRangeValidationHttp`, `#conditionalHeadersDoNotReturn304`, `#operationalAndCompletionFailuresHttp`, `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` | **PASS** |
| **V1-18** | Modulith boundaries, package ownership, ADR-0016 intact; zero edits outside 9+4 boundary | `ApplicationArchitectureTests` (38 rules), git diff inspection | **PASS** |
| **V1-19** | Exact SHA-256 hashes, test evidence, surefire reports, and watchdog receipts recorded | All 13 hashes recorded; receipts in `logs/bf26-v1/impl-20261115-r2/` | **PASS** |

---

## 6. Verification Commands & Receipt Ledger

All runs executed under `scripts/run-bounded-command.py` with 300.0s hard watchdog in directory `logs/bf26-v1/impl-20261115-r2/`:

| Verification Group | Target Selector | Receipt File | Exit Code | Result | Duration | Tests / Invocations | Notes |
|---|---|---|---|---|---|---|---|
| **V1-DB** (Attempt 1) | `VaultEntryIntegrationTest` | `V1-DB.json` | 1 | FAIL | 25.13s | 0 | Compile failure: `VaultEntryType.DOCUMENT` does not exist |
| **V1-DB** (Attempt 2) | `VaultEntryIntegrationTest` | `V1-DB-r2.json` | 0 | **PASS** | 85.08s | 13/13 | All 13 tests pass; captures SQL, binds, correlated EXPLAIN plan |
| **V1-HTTP** (Attempt 1) | `VaultWebIntegrationTest` | `V1-HTTP.json` | 124 | TIMEOUT | 300.00s | - | Timeout: `beforeCommit` failure left connection in transaction, blocking `@AfterEach cleanUpDatabase()` |
| **V1-HTTP** (Debug) | `VaultWebIntegrationTest` | `V1-HTTP-debug3.json` | 124 | TIMEOUT | 120.00s | - | Isolated hang to `@AfterEach cleanUpDatabase()` table truncate |
| **V1-HTTP** (Attempt 2) | `VaultWebIntegrationTest` | `V1-HTTP-r2.json` | 0 | **PASS** | 79.61s | 7/7 | All 7 tests pass; clean rollback via `setRollbackOnCommitFailure(true)` |
| **V1-SEC** | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | `V1-SEC.json` | 0 | **PASS** | 89.28s | 28/28 | 19 in `ProfileRouteAuthorizationIntegrationTest`, 9 in `SecurityFilterChainIntegrationTest` |
| **V1-API** | `OpenApiRouteInventoryIntegrationTest` | `V1-API.json` | 0 | **PASS** | 43.48s | 1/1 | 1 test verifying all 240 route operations and OpenAPI 3.1 schemas |
| **V1-ARCH** | `ApplicationArchitectureTests` | `V1-ARCH.json` | 0 | **PASS** | 32.39s | 38/38 | All 38 Spring Modulith and ArchUnit rules pass |
| **V1-REG** | `VaultMutationConcurrencyTest,VaultMetadataIntegrationTest,GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` | `V1-REG.json` | 0 | **PASS** | 49.33s | 25/25 | 13 concurrency + 11 metadata + 1 search recycle bin exclusion |

**Total passing test invocations:** 13 + 7 + 28 + 1 + 38 + 25 = **112 tests PASS** across 9 classes. Zero failures, zero errors, zero skipped.

All surefire reports preserved under `logs/bf26-v1/impl-20261115-r2/surefire/`.

---

## 7. Direct Class-and-Method Traceability (16/16 Explicit Method References)

1. `P01`: `VaultEntryOperations#listTrash(int, String)`
2. `P02`: `VaultEntryService#listTrash(int, String)`
3. `P03`: `VaultEntryRepository#findTrashPage(Long, org.springframework.data.domain.Pageable)`
4. `P04`: `VaultTrashCursor#encode(Long)`
5. `P04`: `VaultTrashCursor#decode(String)`
6. `P05`: `VaultTrashPageView#items()`
7. `P05`: `VaultTrashPageView#nextCursor()`
8. `P06`: `VaultController#listTrash(Integer, String)`
9. `P07`: `VaultTrashItemResponse#id()`
10. `P07`: `VaultTrashItemResponse#entryType()`
11. `P07`: `VaultTrashItemResponse#deletedAt()`
12. `P08`: `VaultTrashPageResponse#items()`
13. `P08`: `VaultTrashPageResponse#nextCursor()`
14. `P09`: `VaultWebMapper#toTrashPageResponse(VaultTrashPageView)`
15. `P09`: `VaultWebMapper#toTrashItemResponse(com.vhvkhangg.personalprivatevault.vault.view.TrashItemView)`
16. `P01`: `VaultEntryOperations.InvalidTrashInputException#InvalidTrashInputException(String)`

---

## 8. Actual Synthetic Wire, Error, and Schema Specimens

### 8A. Successful Wire Response (200 OK)
*Captured from `VaultWebIntegrationTest#trashHttpSuccessAndEnvelopeWire` (seeded with `FILM` entry):*

```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store

{
  "data": {
    "items": [
      {
        "id": 139,
        "entryType": "FILM",
        "deletedAt": "2026-11-15T09:33:26.540707Z"
      }
    ],
    "nextCursor": null
  },
  "error": null,
  "meta": null
}
```

### 8B. Actual Captured Hibernate SQL and Bind Parameters
*Captured via Logback list appender on `org.hibernate.SQL` and `org.hibernate.orm.jdbc.bind` in `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof`:*

**Generated SQL:**
```sql
select ve1_0.id,ve1_0.entry_type,ve1_0.created_at,ve1_0.updated_at,ve1_0.deleted_at from vault_entries ve1_0 where ve1_0.deleted_at is not null and (? is null or ve1_0.id<?) order by ve1_0.id desc fetch first ? rows only
```

**Initial Query Binds (`listTrash(2, null)`):**
```text
binding parameter (1:BIGINT) <- [null]
binding parameter (2:BIGINT) <- [null]
binding parameter (3:INTEGER) <- [3]
```

**Continuation Query Binds (`listTrash(2, continuationCursor)`):**
```text
binding parameter (1:BIGINT) <- [52]
binding parameter (2:BIGINT) <- [52]
binding parameter (3:INTEGER) <- [3]
```

### 8C. Parameter-Correlated PostgreSQL Execution Plans
*Captured via `EXPLAIN (ANALYZE, COSTS OFF)` on the exact parameter-corresponding captured statement in `VaultEntryIntegrationTest#postgresPlanAndSqlBoundProof`:*

**Initial Query Plan:**
```text
Limit (actual time=0.012..0.014 rows=3.00 loops=1)
  Buffers: shared hit=2
  ->  Index Scan Backward using vault_entries_pkey on vault_entries ve1_0 (actual time=0.011..0.013 rows=3.00 loops=1)
        Filter: (deleted_at IS NOT NULL)
        Rows Removed by Filter: 13
        Index Searches: 1
        Buffers: shared hit=2
Planning Time: 0.055 ms
Execution Time: 0.021 ms
```

**Continuation Query Plan:**
```text
Limit (actual time=0.014..0.016 rows=3.00 loops=1)
  Buffers: shared hit=2
  ->  Index Scan Backward using vault_entries_pkey on vault_entries ve1_0 (actual time=0.013..0.015 rows=3.00 loops=1)
        Index Cond: (id < '52'::bigint)
        Filter: (deleted_at IS NOT NULL)
        Rows Removed by Filter: 21
        Index Searches: 1
        Buffers: shared hit=2
Planning Time: 0.076 ms
Execution Time: 0.029 ms
```

### 8D. Invalid Limit Response (400 Bad Request)
*Captured from `VaultWebIntegrationTest#limitParsingAndRangeValidationHttp` (handled by `VaultController.listTrash` catching `InvalidTrashInputException`):*

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
Cache-Control: no-store

{
  "data": null,
  "error": {
    "code": "INVALID_ARGUMENT",
    "message": "Invalid argument provided",
    "fieldErrors": []
  },
  "meta": null
}
```

### 8E. Expired Session Token Response (401 Unauthorized)
*Captured from `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` (token containing real admitted `sid` and `generation` claims with expired `exp`):*

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json
Cache-Control: no-store

{
  "data": null,
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "message": "Full authentication is required to access this resource",
    "fieldErrors": []
  },
  "meta": null
}
```

### 8F. Forbidden Response (403 Forbidden)
*Captured from `ProfileRouteAuthorizationIntegrationTest#vaultTrashRouteAuthorizationWitness` (authenticated with PUBLIC profile):*

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json
Cache-Control: no-store

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

### 8G. Operational / Transaction Completion Failure Response (500 Internal Server Error)
*Captured from `VaultWebIntegrationTest#operationalAndCompletionFailuresHttp` (fault injected during physical transaction completion phase via `TransactionSynchronization.beforeCommit`):*

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

### 8H. Actual Generated OpenAPI 3.1 Schemas
*Captured from `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds`:*

**`VaultTrashPageResponse` Schema:**
```json
{
  "type" : "object",
  "description" : "Paged vault recycle bin items",
  "properties" : {
    "items" : {
      "type" : "array",
      "description" : "Page items in descending ID order",
      "items" : {
        "$ref" : "#/components/schemas/VaultTrashItemResponse"
      }
    },
    "nextCursor" : {
      "type" : [ "string", "null" ],
      "description" : "Opaque continuation cursor for next page, or null if exhausted"
    }
  },
  "required" : [ "items", "nextCursor" ]
}
```

**`VaultTrashItemResponse` Schema:**
```json
{
  "type" : "object",
  "description" : "Item in vault recycle bin containing minimal identity metadata",
  "properties" : {
    "id" : {
      "type" : "integer",
      "format" : "int64",
      "description" : "Vault entry ID",
      "example" : 142
    },
    "entryType" : {
      "type" : "string",
      "description" : "Vault entry type",
      "enum" : [ "FICTION", "FILM", "PERSON", "FILM_CREDIT", "IMAGE", "ALBUM", "BRAND", "LOCATION", "STUDY", "INFORMATION", "VOCABULARY", "MUSIC", "SHOPPING", "SOFTWARE", "NOTE", "SAVED_RESOURCE", "EXTERNAL_ACCOUNT" ],
      "example" : "FILM"
    },
    "deletedAt" : {
      "type" : "string",
      "format" : "date-time",
      "description" : "Deletion timestamp",
      "example" : "2026-11-14T08:12:01Z"
    }
  },
  "required" : [ "deletedAt", "entryType", "id" ]
}
```

---

## 9. Next Action

- **Remediation Budget Ledger:** Round 2 of 3 consumed (`2/3 consumed`); takeover `NOT_AUTHORIZED / NOT_USED`.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Target Reviewer:** Independent Codex Final Re-Review 2 (`$codex-final-review`).
