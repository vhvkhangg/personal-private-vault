# Implementation Handback — DTR26-S2B: Opt-in Scheduled HN/RSS Refresh (Remediation Round 3)

- **Handoff ID / Date:** `DTR26-S2B` / 2026-11-05.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Implementer:** Antigravity (`backend-implementer`), `/antigravity-implement-handoff` and mandatory `/antigravity-self-review`.
- **Reviewer:** Separate independent Codex `$codex-final-review`.
- **Remediation Counter:** Round 3/3 consumed; next correction/re-review exhausted; takeover pending if blocked.
- **Operational Status:** Default global property `ppv.feed.scheduler.enabled: false` present in uncommitted working tree `backend/src/main/resources/application.yml` (default OFF). Actual production deployment conformance to Topology A (exactly one active backend JVM) remains `NOT_VERIFIED`; unattended activation is held pending deployment verification.

---

## 1. Acceptance Criteria Mapping (S2B-01 through S2B-12)

| Case ID | Scenario & Observable Proof | Test Selector(s) | Status |
| --- | --- | --- | --- |
| **S2B-01** | Global property absent/false: No timer instance, callback, or thread allocated. RAG scheduling un-triggered. Manual HN/RSS refresh usable. | `FeedSchedulerLifecycleIntegrationTest.SchedulerDisabledTest` | **PASS** |
| **S2B-02** | Global property true: Exactly one private ThreadPoolTaskScheduler (pool=1, thread `feed-scheduler-`), initial delay 60s, fixed delay 60s. Zero Feed delta to scheduled task baseline (Moments and RagRecoveryService reconcile preserved). | `FeedSchedulerLifecycleIntegrationTest.SchedulerEnabledTest` | **PASS** |
| **S2B-03** | Source CRUD, interval=1m, public due GET, manual routes unchanged; exact 235 OpenAPI operations preserved. | `OpenApiRouteInventoryIntegrationTest`, `FeedValidationTest`, `FeedIntegrationTest` | **PASS** |
| **S2B-04** | At most 20 candidate rows examined; at most 2 serial provider invocations per tick; cursor tracks last actually examined row (never page end). Genuinely empty page wraps to start on next tick. | `FeedScheduledRefreshTest`, `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-05** | Real PostgreSQL verification of PH-01 Traces 1, 2, and 3: 30 alternating HN/RSS sources invoked by tick 15; unsupported sources 4/12/19/27 never dispatched; 20 cooling rows advance cursor and next tick breaks at second invocation without page-end advancement. | `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-06** | Shared admission: max 2 active refreshes across manual/scheduled HN/RSS; 3rd attempt returns `BUSY`; single-flight per source blocks concurrent refresh. | `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** |
| **S2B-07** | Source schedule/type/enabled/URL edits mid-fetch roll back staged items and timestamps (`SOURCE_CHANGED_DURING_REFRESH`); interval-only edit proceeds with fresh interval; shared physical transaction PID+XID proven for HN and RSS with negative control. Causal pre-source-lock staging barrier proves rollback of flushed item update and new item insert, with `SOURCE_CHANGED_DURING_REFRESH` rejection and null `lastFetchedAt`/`nextFetchAt`. | `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** (subject to re-review 3) |
| **S2B-08** | Provider failure/busy sets 5-minute cooldown from completion instant; success removes cooldown; max 512 entries with deterministic eviction (smallest time, tie smallest ID); DB fetch times unchanged on failure. | `FeedScheduledRefreshTest`, `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-09** | Application restart clears cursor and cooldown; single idempotent registration after readiness; 60s initial delay prevents startup query storm. Controlled lifecycle recreation asserts clean state reset. | `FeedSchedulerLifecycleTest`, `FeedScheduledRefreshTest`, `FeedSchedulerLifecycleIntegrationTest` | **PASS** |
| **S2B-10** | Stop before readiness prevents timer start; duplicate stop is idempotent; clean shutdown cancels future with interruption and terminates worker thread within timeout. Stop during active provider blocks second dispatch and releases single-flight admission. | `FeedSchedulerLifecycleTest`, `FeedSchedulerLifecycleIntegrationTest` | **PASS** |
| **S2B-11** | Zero POM edits, zero database Flyway migrations, zero changes to frozen modules/interfaces; full backward compatibility with manual Radar and Universal Inbox. | All 6 focused test groups | **PASS** |
| **S2B-12** | Operator documentation, README link, package tree update, honest handback receipts, and explicit operational activation ledger separating code verification from deployment topology conformance. | `feed-scheduled-refresh-operations.md`, `README.md`, package tree, this document | **PASS** |

---

## 2. Remediation Round 3 (2026-11-05) — Resolution of Narrowed Finding FR-04

Independent re-review 2 identified that the Round 2 late-authority concurrency test placed its barrier after `feedItemOperations.ingestFetch` returned. Because `FeedItemService.ingestFetch` acquired `feedSourceRepository.findByIdForUpdate` and held the row lock until commit, the concurrent `UPDATE` blocked and timed out after 5s (`TransactionTimedOutException`), passing the test even if the scheduling authority guard was bypassed.

In Remediation Round 3, this causality and verification issue was resolved:

### Causal Pre-Source-Lock Staging Barrier & Observable Rejection
- **Location:** `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedScheduledRefreshConcurrencyIntegrationTest.java` (`scheduledRefresh_lateScheduleOffAfterStaging_rollsBackExistingItemUpdateAndNewInsert`).
- **Mechanism:**
  1. Unwrapped `feedItemOperations` target bean (`ingestTarget`).
  2. Proxied `ingestTarget`'s `feedSourceRepository` via `ProxyFactory(originalIngestRepository)` to intercept `findByIdForUpdate`.
  3. When `findByIdForUpdate` is invoked inside `FeedItemService.ingestFetch`:
     - Both existing item update (991) and new item insert (992) have already been passed to `feedItemRepository.save(...)`.
     - The hook explicitly calls `itemRepo.flush()`, pushing the SQL `UPDATE` and `INSERT` to PostgreSQL in the physical transaction, strictly **before** any `feed_sources` row lock is acquired.
     - Signals `stagingDoneLatch.countDown()`.
     - Awaits `proceedAfterDisableLatch.await(3, TimeUnit.SECONDS)`, asserting `isTrue()` so that any latch timeout immediately fails the fixture.
  4. The test's main thread awaits `stagingDoneLatch`, then runs `jdbcTemplate.update("UPDATE feed_sources SET scheduled_refresh_enabled = false WHERE id = ?", source.id())`.
     - Because the source row lock is **not** held, this concurrent `UPDATE` commits immediately in < 5ms (asserted `updated == 1`).
  5. The main thread signals `proceedAfterDisableLatch.countDown()`.
  6. The provider transaction resumes, calls `findByIdForUpdate`, and loads the committed row where `scheduled_refresh_enabled = false`.
  7. In `HackerNewsRefreshFinalizer.finalizeRefresh`, the scheduling authority predicate rejects the refresh and throws `FeedRefreshException(FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH)`.
  8. Captured via Spring's `org.springframework.aop.framework.Advised` interface on `hnFinalizer` (`finalizerException.get()`).
  9. Clean Assertions:
     - `outcome == ScheduledRefreshOutcome.FAILED`.
     - `finalizerException.get()` is instance of `FeedRefreshException` with reason `FeedRefreshFailureReason.SOURCE_CHANGED_DURING_REFRESH`.
     - Existing item title in PostgreSQL rolls back to `"Original Item Title"`.
     - New item 2 is not inserted (`allItems.hasSize(1)`).
     - Both `lastFetchedAt` and `nextFetchAt` on the source remain `null`.
  10. `finally` block cleans up: unlatches `proceedAfterDisableLatch`, restores `originalIngestRepository`, and removes `finalizerAdvice`.
- **Timing & Authority Verification:** The entire test completes in ~0.5s of execution time (well within the 5s transaction timeout), proving genuine authority checking rather than transaction expiry.

### Working Tree Local Default Property Description
- The local default property `ppv.feed.scheduler.enabled: false` is accurately documented as present in the uncommitted working tree file `backend/src/main/resources/application.yml` (default OFF). It is not described as "committed", since HEAD remains the preparation commit `e296d55db227988b55e86cfb81e83f45e507703a`.

### Preserved Artifact History
- Original Handback: SHA-256 `6aa20ce6e50b754f8c9f25e3c74d9cabf807bba0d9ea4ea7d8b49a43cffbaf0c` (`docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-2026-11-05.md`).
- Round 1 Handback: SHA-256 `014e9e4f37b08801bf3f991a3a5be574f96462a886080c22e96761fa12d1b39a` (`docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-remediation-1-2026-11-05.md`).
- Round 2 Handback: SHA-256 `254ff0f048ebb32e860a0df1d76daaf33eba7591286ac074203ac21ee3ff4fcd` (`docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-remediation-2-2026-11-05.md`).

---

## 3. Focused Verification Receipts (Remediation Round 3)

All six focused test groups were executed under `scripts/run-bounded-command.py` with `--profile normal --timeout-seconds 300`. Receipts are recorded in `logs/daily-trends-radar/dtr26-s2b-rem3-final/`:

| Group | Test Classes | Receipt File | Exit Code | Result | Duration | Tests Run | Failures / Errors / Skips |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **Group 1: scheduler-unit** | `FeedScheduledRefreshTest`, `FeedSchedulerLifecycleTest` | `group1-scheduler-unit.json` | 0 | `PASS` | 8.578s | 25 | 0 / 0 / 0 |
| **Group 2: scheduler-postgres** | `FeedScheduledRefreshIntegrationTest`, `FeedScheduledRefreshConcurrencyIntegrationTest` | `group2-scheduler-postgres.json` | 0 | `PASS` | 31.562s | 14 | 0 / 0 / 0 |
| **Group 3: scheduler-spring-lifecycle** | `FeedSchedulerLifecycleIntegrationTest` | `group3-scheduler-spring-lifecycle.json` | 0 | `PASS` | 37.609s | 5 | 0 / 0 / 0 |
| **Group 4: manual-scheduled-refresh** | `HackerNewsRefreshIntegrationTest`, `HackerNewsRefreshConcurrencyIntegrationTest`, `RssFeedRefreshIntegrationTest`, `RssFeedRefreshConcurrencyIntegrationTest`, `HackerNewsRefreshAdmissionTest` | `group4-manual-scheduled-refresh.json` | 0 | `PASS` | 41.125s | 49 | 0 / 0 / 0 |
| **Group 5: feed-inbox-read-compatibility** | `FeedIntegrationTest`, `FeedValidationTest`, `FeedIngestionConcurrencyTest`, `FeedRadarIntegrationTest`, `CombinedFeedRadarIntegrationTest`, `SavedResourceInboxIntegrationTest` | `group5-feed-inbox-read-compatibility.json` | 0 | `PASS` | 41.625s | 78 | 0 / 0 / 0 |
| **Group 6: http-architecture** | `FeedWebIntegrationTest`, `DailyTrendsWebIntegrationTest`, `RssDailyTrendsWebIntegrationTest`, `SavedResourceInboxWebIntegrationTest`, `OpenApiRouteInventoryIntegrationTest`, `ApiResponseEnvelopeAndErrorIntegrationTest`, `ApplicationArchitectureTests` | `group6-http-architecture.json` | 0 | `PASS` | 66.703s | 79 | 0 / 0 / 0 |

**Totals:** 6/6 test groups passed, 250 tests run, 0 failures, 0 errors, 0 skips, total execution duration 227.202s (all groups individually $\le 300$s).

---

## 4. Mandatory Self-Review Checklist (`antigravity-self-review`)

- [x] **Owner Scope Compliance:** Narrowed FR-04 late-authority barrier and observable rejection implemented cleanly; zero unrequested features.
- [x] **Reuse & Design:** Reused existing Feed orchestrators, AOP advice, and latch synchronization. No new production interfaces or patterns introduced.
- [x] **Privacy & Invariants:** Retained verified failure-logging privacy without leaking credentials, URLs, XML, or titles. Default property remains `enabled: false`.
- [x] **Concurrency & Transactions:** Verified causal pre-source-lock staging barrier, fast independent commit, explicit `SOURCE_CHANGED_DURING_REFRESH` rejection, and rollback of item update, insert, and timestamps.
- [x] **Cleanliness:** Zero compiler warnings, zero surefire errors, zero POM edits, zero Flyway migrations.
- [x] **Artifact Integrity:** Preserved original handback, round-1 handback, and round-2 handback intact with matching SHA-256 hashes.
- [x] **State Consistency:** Status `IMPLEMENTED_AWAITING_CODEX_REVIEW`, counter `3/3 consumed; next correction/re-review exhausted; takeover pending if blocked`. FR-04 marked addressed awaiting re-review 3 (not unilaterally closed).
- [x] **Git Safety:** Zero commits, zero pushes, zero tags, zero PR creation.

---

## 5. Deployment Activation Ledger

| Dimension | Verification Status | Operational Notes |
| --- | --- | --- |
| **Code Implementation** | `VERIFIED` | S2B-01 through S2B-12 proven via focused unit, PostgreSQL, and Spring lifecycle suites. |
| **Local Default Property** | `VERIFIED` | `ppv.feed.scheduler.enabled: false` present in uncommitted working tree `backend/src/main/resources/application.yml` (default OFF). |
| **Production Topology A Conformance** | `NOT_VERIFIED` | Exactly one active backend JVM confirmed by Owner design decision, but actual deployment runtime environment/orchestration is not certified by this backend test suite. |
| **Unattended Polling Activation** | `HELD` | Background polling must remain disabled until production deployment verification is independently conducted. |

---

## 6. Docs Sync Check

- `docs/architecture/feed-scheduled-refresh-operations.md`: ALREADY_CONSISTENT (§3.2 graceful shutdown wording updated in Round 2).
- `docs/repository/repository-package-tree.md`: ALREADY_CONSISTENT (scheduling package entries documented).
- `README.md`: ALREADY_CONSISTENT (operator guide link added).
- `docs/implementation/handoffs/ACTIVE.md`: UPDATED (`IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation 3/3).
- `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/executable-handoff-2026-11-05.md`: UPDATED (`IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation 3/3).
