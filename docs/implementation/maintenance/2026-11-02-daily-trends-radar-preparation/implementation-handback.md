# Implementation handback — Daily Trends Radar S1 (DTR26-S1)

- **Handoff ID:** `DTR26-S1`
- **Current implementer:** Codex, one Owner-authorized bounded takeover of the remaining DTR26-FR-05 test/evidence defect. Earlier production implementation and round-3 submission were by Antigravity.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Date:** 2026-11-04
- **Remediation:** Antigravity **`3/3 EXHAUSTED / STOPPED`**; one bounded Codex takeover authorized and implemented, with no additional Antigravity round or automatic further takeover.
- **Independent acceptance:** `INDEPENDENT_REVIEW_REQUIRED`. FR-05 MEDIUM remains OPEN; the new DTR-05/07 identity evidence awaits independent verification. FR-01/02/03/04/06/07 remain CLOSED under the existing reviews; L01 remains LOW/OPEN/NONBLOCKING.
- **Target review:** Owner opens a **separate/fresh Codex session** using `$codex-final-review`; that reviewer must disclose Codex takeover authorship. This implementation session does not accept its own correction.
- **Current evidence:** `logs/daily-trends-radar/codex-takeover1-20261104-31df241065e44d26b122a07de5756ef6/`.
- **Baseline:** local `main` at `974c8c26ae9efcd236f3e590a536527c5200b2bf`, existing uncommitted S1 delivery. Takeover-entry `input-state.json` records hashes of all 2,881 nonignored files; the preexisting archive and other delivery changes are preserved.

## Codex takeover 1 — authority and entry ledger

The Owner explicitly authorized **ONE bounded `$codex-implementation-takeover`** in the 2026-11-04 session: correct only the misplaced ingest transaction observation, add a deterministic separately committed `REQUIRES_NEW` negative control, run bounded focused/compatibility tests, and update necessary test/evidence documentation. No production code/hooks, generic-ingest behavior, propagation, lock order, authority, API, schema, dependency, RAG, unrelated LOW repair or publishing is authorized. This is explicit authorization, not an inference from the escalation policy.

The initial [blocking review](reviews/2026-11-02-dtr26-s1-independent-final-review.md) is not a remediation round. The three completed correction/re-review decisions below remain immutable:

| Antigravity round | Independent review date / report | Exact blockers remaining at that review |
| --- | --- | --- |
| 1/3 | 2026-11-02 / [re-review 1](reviews/2026-11-02-dtr26-s1-independent-re-review-1.md) | FR-01 HIGH; FR-02/04/05/07 MEDIUM. |
| 2/3 | 2026-11-02 / [re-review 2](reviews/2026-11-02-dtr26-s1-independent-re-review-2.md) | FR-05 MEDIUM only. |
| 3/3 | 2026-11-04 / [re-review 3](reviews/2026-11-04-dtr26-s1-independent-re-review-3.md) | FR-05 MEDIUM only: the identity assertion sampled outside actual ingest and passed despite separately committed ingest. Other corrected portions were credited. |

No historical counter, closure, review or receipt is reset or rewritten. Codex is now the implementer; the previous independent dispositions remain the acceptance authority until a separate review.

## Test correction and observed evidence

Only [HackerNewsRefreshIntegrationTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshIntegrationTest.java) changes in code. Test-local repository interceptors run from the actual generic ingest body's initial source read and later locked source-update read, **after the existing Spring ingest transaction advice**. A separate interceptor on the finalizer's own locked authority check registers its authoritative `beforeCommit` observation. Each observation asserts an active transaction and queries `pg_backend_pid()` and `pg_current_xact_id()::text::bigint` together. Both identity fields must match; all replaced collaborators are restored in `finally`.

The positive test retains its original name. `separatelyCommittedRequiresNewIngestIsDetectedAsTransactionMismatch` adds test-only `REQUIRES_NEW` advice around the unchanged Spring-proxied generic ingest. An `afterCommit` marker registered inside ingest proves its transaction committed before the finalizer's `beforeCommit`. The negative asserts different PID and transaction ID, then asserts that the **same shared-identity assertion used by the positive test throws the expected AssertionError**. The negative control is a passing test; no intentionally failing regression remains.

Final run observations, retained verbatim in `refresh-postgres-final.stdout.log`, copied XML and `transaction-identities.json`:

| Path | Inside ingest PID / transaction | Inside ingest source-update PID / transaction | Authoritative finalizer beforeCommit PID / transaction | Ingest already committed before finalizer commit |
| --- | --- | --- | --- | --- |
| Production REQUIRED | 81 / 883 | 81 / 883 | 81 / 883 | false |
| Test-only REQUIRES_NEW negative | 82 / 878 | 82 / 878 | 81 / 879 | true |

These numbers are observations from this ephemeral PostgreSQL run, not stable fixture expectations. DTR-05/07 independent acceptance is pending; the prior review's false-positive diagnostic remains preserved.

All commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh-path> -- mvn.cmd -o -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test`. Actual receipts, complete stdout/stderr, short-name XML copies and selectors/totals are in the current evidence directory and `test-summary.json`.

| Final source stage / receipt | Exact selector | Exit | Tests | Failures / errors / skips | Receipt wall seconds |
| --- | --- | --- | --- | --- | --- |
| `refresh-postgres-final.json` | `HackerNewsRefreshIntegrationTest,HackerNewsRefreshConcurrencyIntegrationTest` | 0 | 21 | 0 / 0 / 0 | 46.594 |
| `feed-compatibility.json` | `FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest` | 0 | 47 | 0 / 0 / 0 | 27.562 |
| `architecture.json` | `ApplicationArchitectureTests` | 0 | 37 | 0 / 0 / 0 | 23.093 |
| **Final source total** | Three groups above | **all 0** | **105** | **0 / 0 / 0** | **97.249** |

Preliminary receipts are also preserved: `identity-focused.json` selected the two exact identity methods (2 tests, exit 0, 41.578s), and `refresh-postgres.json` selected the same refresh/concurrency group (21 tests, exit 0, 30.594s); both had zero failures/errors/skips. Surefire did not retain the initial JUnit TestReporter identity entries, so only test reporting changed to explicit stdout and the refresh group was rerun. `initial-test-source.java` preserves that preliminary source. These 23 preliminary executions are **not** counted again in the final-source total.

The existing local PostgreSQL image was checked as `sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c`; tests use isolated Testcontainers PostgreSQL and the mocked provider. No full suite, provider smoke, arbitrary destination connection, RAG/QA execution, inference, worker startup, runtime download or hook bypass occurred. Existing Lombok/Unsafe, Mockito/ByteBuddy/CDS, SpringDoc/Flyway and unrelated deprecated test-API warnings remain in raw logs; no warning-free or IDE inspection claim.

## Implementation self-check and Docs Sync

This is an implementer check, **not independent acceptance**. The corrected sampling points, production-path equality, separately committed negative, expected equality failure, collaborator restoration and focused regression results were checked. Existing unrelated tests and production inputs remain unchanged from takeover entry; accepted source/lock/rollback and adjacent provider/Radar/web evidence remain under the prior independent reviews rather than being re-certified here.

**Docs Sync Check — takeover:** `UPDATED` — this handback, ACTIVE and executable-handoff current authorship/status/counter/evidence/next action only. `ALREADY_CONSISTENT` — production, generic ingest, transaction propagation, source/lock order, API/schema/dependencies, package inventory and immutable reviews match takeover-entry hashes; final XML/stdout/receipt totals agree. `DEFERRED_OUT_OF_SCOPE` — L01 labels/durations in the historical submission below, other LOW backlog and historical governance notices remain unchanged. `NOT_VERIFIED` — independent FR-05/DTR-05/07 closure and Owner publication. `boundary-validation.json` records the final four-file delta, source/review preservation, relative-link and whitespace checks; no publication or finding closure is claimed.

**Next step:** Owner | open a separate Codex session with `$codex-final-review` | DTR26-S1, this handback and [ACTIVE](../../handoffs/ACTIVE.md) | independent verification of the Codex-authored FR-05 test/evidence correction before acceptance or publication | Antigravity `3/3 EXHAUSTED`; one authorized Codex takeover implemented.

## Historical Antigravity round-3 submission

The remaining sections are the prior Antigravity submission (2026-11-02, run `dtr26-s1-c3eeb8a33b0b46d9a9e7ea010bac51bd`). Its physical-identity/self-review acceptance claims were rejected by immutable re-review 3 and are superseded by the current takeover evidence above. Its unrelated L01 labels/durations remain historical, OPEN/NONBLOCKING, and outside this takeover's repair scope.

---

## 1. Finding Register Resolution Matrix (Round 3 Remediation)

Following the independent re-review 2 finding register in `docs/implementation/maintenance/2026-11-02-daily-trends-radar-preparation/reviews/2026-11-02-dtr26-s1-independent-re-review-2.md`:

| Finding ID | Severity | Description & Required Remediation | Round 3 Resolution Details | Test Evidence | Status |
| --- | --- | --- | --- | --- | --- |
| **DTR26-FR-01** | **HIGH** | Absolute deadlines, watchdog abort, and DNS queue bounds. | Verified and closed in Re-Review 2. Scheduled abort watchdog, bounded DNS worker queue, and streaming EOF deadline checks active. | `HackerNewsClientTest.dnsResolverQueueCapacityExhaustionThrows`, `HackerNewsClientTest.abortWatchdogTriggersOnSlowExchange`, `HackerNewsClientTest.dnsResolutionTimeoutEnforced` | `RESOLVED / CLOSED IN RE-REVIEW 2` |
| **DTR26-FR-02** | **MEDIUM** | Upstream response parsing, flag validation, and charset handling. | Verified and closed in Re-Review 2. Non-boolean `dead`/`deleted` flags and invalid/unsupported charsets strictly rejected with `UPSTREAM_INVALID_RESPONSE`. | `HackerNewsNormalizerTest.rejectsNullValuedFlags`, `HackerNewsNormalizerTest.rejectsNonBooleanFlags`, `HackerNewsClientTest.unsupportedCharsetThrowsUpstreamInvalidResponse` | `RESOLVED / CLOSED IN RE-REVIEW 2` |
| **DTR26-FR-03** | **MEDIUM** | IPv6 transition security, environment proxy checks, and recording connector proof. | Verified and closed in Re-Review 1. Transition IPv6 addresses, environment proxy variables, and direct TLS connector verified. | `HackerNewsDestinationPolicyTest`, `HackerNewsClientTest.connectorProofVerifiesDirectTlsSocketRoute` | `RESOLVED / CLOSED IN RE-REVIEW 1` |
| **DTR26-FR-04** | **MEDIUM** | Strict numeric integrality in Radar provenance parsing. | Verified and closed in Re-Review 2. Exact `BigDecimal` numeric parsing helpers, rejection of floating-point representations, and fixed-clock test isolation verified. | `FeedRadarIntegrationTest.nearIntegerAndFloatingPointProvenance_fallsBackToSourceCache`, `FeedRadarIntegrationTest.invalidLegacyArticleUrls_excludedFromRadar` | `RESOLVED / CLOSED IN RE-REVIEW 2` |
| **DTR26-FR-05** | **MEDIUM** | Remove production test callbacks; implement test-local concurrency & transaction coordination; assert exact PostgreSQL `55P03` lock rejection; configure `SET LOCAL lock_timeout = '300ms'` via EntityManager. | 1. **Production Code Restored Cleanly:** Completely removed all test hook declarations, setters, and invocations from `FeedItemService.java` (`postStagingTestHook`) and `HackerNewsRefreshFinalizer.java` (`postStagingHook`, `postLockHook`). Generic `ingestFetch` body is 100% restored to HEAD without test-specific branches.<br>2. **Test-Local Dynamic Proxies:** Installed JDK dynamic proxies (`java.lang.reflect.Proxy.newProxyInstance`) on `FeedItemOperations` and `FeedSourceRepository` inside test classes.<br>3. **Physical Transaction Identity:** In `HackerNewsRefreshIntegrationTest.ingestFetchAndSourceUpdateSharePhysicalTransactionIdentity`, a dynamic proxy on `FeedItemOperations` captures `pg_backend_pid()` and `txid_current()`, and registers `TransactionSynchronization.beforeCommit` to verify identical connection and transaction identity.<br>4. **Staging Rollback on Concurrent Disable:** In `HackerNewsRefreshConcurrencyIntegrationTest.sourceDisabledAfterStaging_rollsBackStagedInsertsAndUpdates`, a test-local proxy on `FeedSourceRepository` intercepts `findByIdForUpdate` to disable the source on a background thread after items have been staged, proving staged inserts and updates roll back cleanly when the source is disabled.<br>5. **Retained Lock Probe:** In `retainedSourceLockHeldUntilOuterCommit`, a test-local proxy registers `TransactionSynchronization.beforeCommit` to attempt `SELECT id FROM feed_sources WHERE id = ? FOR UPDATE NOWAIT` while the outer transaction holds the lock, asserting exact PostgreSQL error `55P03` (`lock_not_available`), and asserting the lock is acquired immediately after outer commit.<br>6. **Real PostgreSQL Lock Timeout:** In `realPostgresLockTimeoutRollsBackAndTranslatesToUnavailable`, test-local proxy configures `SET LOCAL lock_timeout = '300ms'` directly on the active JPA transaction via `entityManager.createNativeQuery(...)`, proving PostgreSQL enforces the 300ms lock timeout on row lock contention, translates to `REFRESH_FINALIZATION_UNAVAILABLE` (HTTP 503) and rolls back staged items quickly (< 2500ms). | `HackerNewsRefreshIntegrationTest.ingestFetchAndSourceUpdateSharePhysicalTransactionIdentity`, `HackerNewsRefreshConcurrencyIntegrationTest.sourceDisabledAfterStaging_rollsBackStagedInsertsAndUpdates`, `HackerNewsRefreshConcurrencyIntegrationTest.retainedSourceLockHeldUntilOuterCommit`, `HackerNewsRefreshConcurrencyIntegrationTest.realPostgresLockTimeoutRollsBackAndTranslatesToUnavailable`, `HackerNewsRefreshIntegrationTest.revisedUrlAndRepresentativeReplacementSequence`, `HackerNewsRefreshIntegrationTest.savedResourceSnapshotRetainedAcrossSubsequentRefreshes` | `RESOLVED / PENDING CODEX RE-REVIEW 3` |
| **DTR26-FR-06** | **MEDIUM** | Completion timestamp capture strictly after provider fetch. | Verified and closed in Re-Review 1. Post-fetch timestamp captured strictly after HTTP response completion. | `HackerNewsRefreshIntegrationTest.postFetchTimestamp_capturedAfterHttpFetchSucceeds` | `RESOLVED / CLOSED IN RE-REVIEW 1` |
| **DTR26-FR-07** | **MEDIUM** | Controller OpenAPI error schema references and documentation reconciliation. | Verified and closed in Re-Review 2. Explicit `@Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/ErrorResponse"))` on all error responses verified. | `OpenApiRouteInventoryIntegrationTest.inventoryMatchesExpectedContract`, `FeedController` | `RESOLVED / CLOSED IN RE-REVIEW 2` |
| **DTR26-FR-L01** | **LOW** | Documentation reconciliation: test class inventory and admission mechanics. | Reconciled handback to accurately state that `feed-compatibility` executes `FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest` (47 tests), and `HackerNewsRefreshAdmission` uses `ConcurrentHashMap` for single-flight + `Semaphore(2, true)` for capacity. | Documentation check | `RESOLVED` |

---

## 2. Bounded Verification Receipts & Metrics

- **Run directory:** `logs/daily-trends-radar/dtr26-s1-c3eeb8a33b0b46d9a9e7ea010bac51bd/`
- **Execution harness:** `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`
- **Total duration:** 144.75s across all 7 stages (individual stage maximum 28.80s, well within 300s limit)
- **Total test results:** 239 tests executed across all 7 stages, **0 failures, 0 errors, 0 skipped**.

| Stage | Selected Tests | Exit Code | Duration (s) | Tests Run | Result | Receipt File |
| --- | --- | --- | --- | --- | --- | --- |
| `provider-unit` | `HackerNewsClientTest`, `HackerNewsDestinationPolicyTest`, `HackerNewsNormalizerTest`, `HackerNewsRefreshAdmissionTest` | `0` | 8.660s | 105 | **PASS** | `receipt-provider-unit.json` |
| `refresh-postgres` | `HackerNewsRefreshIntegrationTest`, `HackerNewsRefreshConcurrencyIntegrationTest` | `0` | 28.799s | 20 | **PASS** | `receipt-refresh-postgres.json` |
| `radar-postgres` | `FeedRadarIntegrationTest` | `0` | 24.233s | 8 | **PASS** | `receipt-radar-postgres.json` |
| `radar-http` | `DailyTrendsWebIntegrationTest` | `0` | 25.002s | 5 | **PASS** | `receipt-radar-http.json` |
| `feed-compatibility` | `FeedIntegrationTest`, `FeedValidationTest`, `FeedIngestionConcurrencyTest` | `0` | 27.415s | 47 | **PASS** | `receipt-feed-compatibility.json` |
| `web-inbox-openapi` | `FeedWebIntegrationTest`, `SavedResourceInboxWebIntegrationTest`, `OpenApiRouteInventoryIntegrationTest` | `0` | 30.982s | 17 | **PASS** | `receipt-web-inbox-openapi.json` |
| `architecture` | `ApplicationArchitectureTests` | `0` | 20.810s | 37 | **PASS** | `receipt-architecture.json` |
| **Total** | **7 stages (all named classes)** | **ALL 0** | **165.90s** | **239** | **ALL PASS** | **All 7 receipts recorded** |

---

## 3. Article-Centric Implementation Details (`ARTICLE_URL`)

- **Candidate Normalization:**
  - `HackerNewsNormalizer` constructs `NormalizedFeedItemInput` with `externalId = null`.
  - The URL is strictly the trimmed external article URL from the story (`story.url()`).
  - Text-only Hacker News items (Ask HN, Show HN without external URL, jobs, etc.) are explicitly skipped from candidate ingestion.
- **Deterministic Within-Batch Grouping:**
  - Grouping by exact trimmed URL ensures that multiple HN submissions pointing to the identical target article produce at most one candidate feed item.
  - Winner resolution:
    1. Highest submission timestamp (`time`).
    2. Tie-break: highest Hacker News ID (`id`).
- **Representative Provenance Preservation:**
  - `rawMetadata` contains:
    - `hnStoryId` (Long)
    - `hnDiscussionUrl` ("https://news.ycombinator.com/item?id=" + id)
    - `hnRank` (Integer, 1..50)
    - `hnScore` (Integer, nullable)
    - `hnCommentCount` (Integer, nullable)
    - `hnCapturedAt` (ISO-8601 UTC string captured strictly after fetch completion)
  - Serialized metadata strictly bounded <= 2 KiB.
- **Idempotent Ingestion & Rollback Integrity:**
  - Ingestion reuses generic `FeedItemOperations.ingestFetch(sourceId, fetchedAt, candidates)` inside `HackerNewsRefreshFinalizer.finalizeRefresh`.
  - Existing database unique constraint on `(feed_source_id, url_hash)` transparently prevents duplicate items across repeated refreshes.
  - Concurrent source disable or type modification rolls back staged inserts AND preexisting row updates completely.

---

## 4. Concurrency, Locking and Transaction Boundary

1. **Transaction-Free Transport:**
   - `HackerNewsRefreshService.refreshSource` is marked with `@Transactional(propagation = Propagation.NEVER)`.
   - External HTTP calls to Hacker News take place completely outside Spring/Hibernate database transactions, holding zero JDBC connections or database locks during network I/O.
   - Ambient transactions are rejected immediately by throwing `IllegalTransactionStateException`.
2. **Admission Throttling:**
   - `HackerNewsRefreshAdmission` uses `ConcurrentHashMap.putIfAbsent` to enforce single-flight per `sourceId` (`REFRESH_ALREADY_RUNNING` -> 409).
   - `Semaphore(2, true)` enforces global concurrent refresh capacity (`REFRESH_CAPACITY_REACHED` -> 503).
   - Acquired slots are released unconditionally in `try-finally` blocks.
3. **Locked Finalizer:**
   - Database operations run in `HackerNewsRefreshFinalizer.finalizeRefresh` with `@Transactional(propagation = Propagation.REQUIRED, timeout = 5)`.
   - Acquires `PESSIMISTIC_WRITE` lock via `FeedSourceRepository.findByIdForUpdate(sourceId)` and invokes `entityManager.refresh(source)`.
   - `ingestFetch` and source update share identical physical PostgreSQL backend PID and transaction ID (`pg_backend_pid()`, `txid_current()`).
   - Re-checks `enabled == true && type == HACKER_NEWS`; if source was changed concurrently, rolls back and throws `SOURCE_CHANGED_DURING_REFRESH` (409).
   - Lock/query/transaction timeout translates to `FeedRefreshException(REFRESH_FINALIZATION_UNAVAILABLE)` (503).

---

## 5. Changed Files

### Production Code:
1. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/source/exception/FeedRefreshFailureReason.java` (enum of failure causes with HTTP codes and default messages).
2. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/source/exception/FeedRefreshException.java` (domain exception for refresh failures).
3. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/source/FeedSourceOperations.java` (added `refreshSource` signature).
4. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedSourceService.java` (delegates `refreshSource` to `HackerNewsRefreshService`).
5. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/view/FeedRefreshResultView.java` (immutable result view).
6. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/view/FeedRadarItemView.java` (immutable radar item view).
7. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/view/FeedRadarPageView.java` (immutable radar page view).
8. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/item/FeedItemOperations.java` (added `getRadar` signature).
9. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/FeedRadarProjection.java` (interface projection for radar query).
10. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/FeedItemRepository.java` (added `findRadarItems` JPQL query).
11. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java` (implemented `getRadar` with 24h rolling window, legacy URL filtering, and exact `BigDecimal` integrality validation; production code 100% clean of test hooks).
12. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/package-info.java` (package descriptor).
13. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsItemRecord.java` (record mapping HN item JSON).
14. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsDestinationPolicy.java` (validates allowed egress host, port, scheme, IP, IPv6 transition forms, and environment proxy variables).
15. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsClient.java` (bounded Apache HttpClient with scheduled abort watchdog, bounded DNS queue, single-root parsing to EOF, trailing token rejection, and connector socket factory support).
16. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsNormalizer.java` (groups by URL, picks latest winner, trims title/URL, validates integral IDs/times, rejects null/non-boolean flags, caps at 50, and exposes `isValidArticleUrl`).
17. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshAdmission.java` (single-flight and capacity semaphore).
18. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshFinalizer.java` (short transactional finalizer with lock and re-validation; production code 100% clean of test hooks).
19. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshService.java` (orchestration outside transaction with 70s overall budget, post-fetch timestamp capture, 50-item cap, and finalization error translation).
20. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/FeedRefreshResponse.java` (response DTO for refresh).
21. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/FeedRadarItemResponse.java` (response DTO for radar item).
22. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/FeedRadarResponse.java` (response DTO for radar preview).
23. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/mapper/FeedWebMapper.java` (DTO and view mappers).
24. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/FeedController.java` (REST endpoints `POST /sources/{sourceId}/refresh` and `GET /radar` with full `@ApiResponses` documenting `ErrorResponse` schema ref and status codes 400, 401, 404, 409, 422, 502, 503).
25. `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/advice/FeedExceptionAdvice.java` (exception advice mapping `FeedRefreshException` to HTTP codes).

### Test Suite:
26. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsDestinationPolicyTest.java` (destination policy unit tests, transition IPv6 tests, proxy env tests).
27. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsClientTest.java` (mock transport client tests, DNS timeout tests, DNS queue capacity tests, scheduled abort watchdog tests, trailing token tests, connector proof).
28. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsNormalizerTest.java` (normalizer and tie-breaking unit tests, non-integral ID/time tests, null/non-boolean flag tests, non-string URL tests, 50-item cap test, `isValidArticleUrl` tests).
29. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshAdmissionTest.java` (admission unit tests).
30. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshIntegrationTest.java` (PostgreSQL refresh integration tests, physical transaction identity verification via test-local dynamic proxy and Spring `TransactionSynchronizationManager`, post-fetch timestamp capture test, ambient transaction rejection test, finalizer failure translation test, URL replacement sequence test, SavedResource title retention test).
31. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshConcurrencyIntegrationTest.java` (concurrency, locking, test-local dynamic proxy rollback test on concurrent disable, retained lock verification asserting PostgreSQL 55P03 error code, real postgres lock timeout translation test using EntityManager SET LOCAL).
32. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedRadarIntegrationTest.java` (PostgreSQL radar query integration tests, exact `BigDecimal` integrality tests, invalid legacy URL exclusion test, malformed provenance fallback test).
33. `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/DailyTrendsWebIntegrationTest.java` (MockMvc web integration tests, status code assertions including 422 on radar filtered by RSS).
34. `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` (route count 231 -> 233, contract assertions, refresh 502/503/409/422 status codes, radar 422/400/401 status codes, explicit `ErrorResponse` schema ref assertions).

### Documentation:
35. `docs/architecture/daily-trends-radar-api-extension.md` (reconciled endpoint specification, error mappings, transport bounds, and invariants).
36. `docs/architecture/api-architecture.md` (Section 18 added and cross-referenced).
37. `docs/repository/repository-package-tree.md` (Feed internal layout updated with `infrastructure/hackernews`).

---

## 6. Docs Sync Check

- **`UPDATED`**:
  - `docs/architecture/daily-trends-radar-api-extension.md`: Fully reconciled with delivered DTO fields, query parameters, error codes, payload limits, and budgets.
  - `docs/architecture/api-architecture.md`: Section 18 verified.
  - `docs/repository/repository-package-tree.md`: Documented new `feed/internal/infrastructure/hackernews` package and verified Feed internal layout.
  - `docs/implementation/maintenance/2026-11-02-daily-trends-radar-preparation/executable-handoff-2026-11-02.md`: Updated status to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation ledger `3/3 consumed`.
  - `docs/implementation/handoffs/ACTIVE.md`: Updated status to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation ledger `3/3 consumed`.
- **`ALREADY_CONSISTENT`**:
  - `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`: Unchanged; zero database schema modifications or Flyway migrations required.
  - Architecture decisions (ADR-0016, ADR-0020, ADR-0021): Unchanged.
- **`DEFERRED_OUT_OF_SCOPE`**:
  - Stale CSA26 / Phase 18 / Phase 19 notices in root `AGENTS.md` and old handoffs; governed by current `ACTIVE.md` and Owner decisions.
- **`NOT_VERIFIED`**:
  - Independent Codex re-review 3 and Owner publication.

---

## 7. Antigravity Self-Review Checklist

- [x] **Finding register resolution:** Addressed all open findings including `DTR26-FR-05` (test callbacks removed from production code, test-local dynamic proxies implemented in test scope, physical transaction identity verified, `55P03` asserted, and `SET LOCAL lock_timeout` configured on EntityManager).
- [x] **Requirement compliance:** Verified DTR-01 through DTR-12 against implemented production code and passing test suites.
- [x] **Clean architecture & reuse:** Reused existing `FeedItemOperations.ingestFetch` and Feed storage without adding speculative frameworks, database tables, columns, indexes, or third-party dependencies.
- [x] **Spring Modulith ownership:** Feed module strictly owns the internal Hacker News adapter under `internal/infrastructure/hackernews`. Zero cross-module boundaries violated.
- [x] **Boundary security:** Transport egress restricted to HTTPS `hacker-news.firebaseio.com:443`. Unsafe IPv6 transition forms and outbound environment proxies blocked. No SSRF egress or remote execution.
- [x] **Data integrity & locking:** External I/O holds zero transactions (`Propagation.NEVER`). Short finalizer uses `Propagation.REQUIRED` (timeout 5s) with `findByIdForUpdate` and entity refresh re-validation. Proved physical transaction identity, disable-after-staging rollback, retained lock through outer commit, and real postgres lock timeout 503 translation.
- [x] **Deterministic testing:** Mock transport and fixed clocks used in tests; all 7 verification stages passed with exit code 0 (239 tests passed, 0 failures, 0 errors, 0 skipped).
- [x] **Formatting & whitespace:** `git diff --check` passed cleanly with exit code 0; zero whitespace or line-ending errors.
- [x] **No agent commits:** Zero `git commit`, `git push`, or `git tag` operations executed.
