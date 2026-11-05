# Implementation Handback — DTR26-S2B: Opt-in Scheduled HN/RSS Refresh (Remediation Round 2)

- **Handoff ID / Date:** `DTR26-S2B` / 2026-11-05.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Implementer:** Antigravity (`backend-implementer`), `/antigravity-implement-handoff` and mandatory `/antigravity-self-review`.
- **Reviewer:** Separate independent Codex `$codex-final-review`.
- **Remediation Counter:** Round 2/3 consumed; next correction/re-review 3/3; takeover `NOT_AUTHORIZED`.
- **Operational Status:** Default global property `ppv.feed.scheduler.enabled: false`. Actual production deployment conformance to Topology A (exactly one active backend JVM) remains `NOT_VERIFIED`; unattended activation is held pending deployment verification.

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
| **S2B-07** | Source schedule/type/enabled/URL edits mid-fetch roll back staged items and timestamps (`SOURCE_CHANGED_DURING_REFRESH`); interval-only edit proceeds with fresh interval; shared physical transaction PID+XID proven for HN and RSS with negative control. Late schedule-OFF barrier after real item staging rolls back both existing item update and new item insert. | `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** |
| **S2B-08** | Provider failure/busy sets 5-minute cooldown from completion instant; success removes cooldown; max 512 entries with deterministic eviction (smallest time, tie smallest ID); DB fetch times unchanged on failure. | `FeedScheduledRefreshTest`, `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-09** | Application restart clears cursor and cooldown; single idempotent registration after readiness; 60s initial delay prevents startup query storm. Controlled lifecycle recreation asserts clean state reset. | `FeedSchedulerLifecycleTest`, `FeedScheduledRefreshTest`, `FeedSchedulerLifecycleIntegrationTest` | **PASS** |
| **S2B-10** | Stop before readiness prevents timer start; duplicate stop is idempotent; clean shutdown cancels future with interruption and terminates worker thread within timeout. Stop during active provider blocks second dispatch and releases single-flight admission. | `FeedSchedulerLifecycleTest`, `FeedSchedulerLifecycleIntegrationTest` | **PASS** |
| **S2B-11** | Zero POM edits, zero database Flyway migrations, zero changes to frozen modules/interfaces; full backward compatibility with manual Radar and Universal Inbox. | All 6 focused test groups | **PASS** |
| **S2B-12** | Operator documentation, README link, package tree update, honest handback receipts, and explicit operational activation ledger separating code verification from deployment topology conformance. | `feed-scheduled-refresh-operations.md`, `README.md`, package tree, this document | **PASS** |

---

## 2. Remediation Round 2 (2026-11-05) — Resolution of Narrowed Finding FR-04

All four areas of narrowed finding `FR-04` from independent re-review 1 (`reviews/2026-11-05-dtr26-s2b-independent-re-review-1.md`) are addressed and verified:

### Area 1: Controlled Spring Callback / Restart / Active-Provider Stop
- **`FeedSchedulerLifecycleIntegrationTest.java`**:
  - Implemented `controlledSpringCallback_withSafeFakeProviders_activeStop_andRecreation` under `@SpringBootTest` with `ppv.feed.scheduler.enabled=true`.
  - Configured safe fake providers with due and excluded (`scheduled_refresh_enabled = false`) rows.
  - Used an owned, recording `ThreadPoolTaskScheduler` (`feed-spring-rec-`) with an immediate-start test seam.
  - Blocked inside the active provider for Due Source 1 using a latch.
  - Observed active acquisition of single-flight admission (`isSourceActive(due1.id()) == true`, `activeCount == 1`).
  - Executed `controlledLifecycle.stop()` while provider 1 was actively blocked.
  - Asserted clean termination within the bounded timeout (`isTerminated() == true`, `isStopped() == true`, worker thread interrupted).
  - Released provider latch in `finally` and confirmed single-flight admission was cleanly released (`isSourceActive(due1.id()) == false`, `activeCount == 0`).
  - Asserted `callCount == 1`, proving that second candidate (`due2`) was NOT dispatched because `stopping.get()` and interruption broke the loop. Excluded source was never queried or dispatched.
  - Recreated lifecycle and asserted fresh cursor (`lastExaminedSourceId == null`), fresh cooldown (`cooldownMap.isEmpty()`), single delayed registration (`scheduledFuture != null`, `isCancelled() == false`), followed by clean stop.

### Area 2: Marker-Bearing Privacy Observation
- **`FeedSchedulerLifecycleIntegrationTest.java`**:
  - Implemented `failureLoggingPrivacy_doesNotLeakSensitiveMarkers`.
  - Attached a Logback `ListAppender<ILoggingEvent>` to `FeedScheduledRefreshService`.
  - Injected a synthetic query failure containing URL, credential, XML, and title markers: `"https://user:SUPER_SECRET_PASSWORD@example.com/feed?token=API_KEY_9999 <sensitiveXml>SecretTitle</sensitiveXml>"`.
  - Executed `testService.executeTick()`.
  - Required that the expected safe warning event was logged (`appender.list.anyMatch(event -> event.getLevel() == Level.WARN && event.getFormattedMessage().contains("Scheduled due query failed"))`), ensuring a non-vacuous observation.
  - Exhaustively inspected all logged events, messages, formatted messages, argument arrays, and throwable proxies to verify zero occurrence of sensitive markers (`SUPER_SECRET_PASSWORD`, `API_KEY_9999`, `sensitiveXml`, `SecretTitle`).

### Area 3: Late Scheduling Authority After Real Staging
- **`FeedScheduledRefreshConcurrencyIntegrationTest.java`**:
  - In `scheduledRefresh_lateScheduleOffAfterStaging_rollsBackExistingItemUpdateAndNewInsert`, spied `feedItemOperations` using `@MockitoSpyBean`.
  - Hooked a concurrency barrier into `feedItemOperations.ingestFetch(eq(source.id()), any(), any())` strictly **after** `inv.callRealMethod()` (meaning real item `UPDATE` and `INSERT` rows have already been staged in the transactional persistence context) and **before** `findByIdForUpdate` re-locks the source.
  - Concurrently disabled scheduling on the source via an independent transaction and committed.
  - Released the barrier; the scheduled refresh finalizer detected `SOURCE_CHANGED_DURING_REFRESH` and threw `FeedRefreshException`.
  - Asserted rollback of both the existing item update (original title retained) and the new item insert (new URL row absent), and verified that both `last_fetch_at` and `next_fetch_at` remained null.

### Area 4: Evidence History & Accurate Current Claims
- **Preserved Round 1 Handback**: The rewritten round-1 handback is preserved as a dated artifact at `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-remediation-1-2026-11-05.md` (SHA-256 `014e9e4f37b08801bf3f991a3a5be574f96462a886080c22e96761fa12d1b39a`).
- **Restored Original Handback**: The original initial handback text has been restored to `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-2026-11-05.md` with verified SHA-256 `6aa20ce6e50b754f8c9f25e3c74d9cabf807bba0d9ea4ea7d8b49a43cffbaf0c`.
- **Operator Documentation Alignment**: Updated `docs/architecture/feed-scheduled-refresh-operations.md` §3.2 to distinguish clean termination from timeout/failed cleanup via `isTerminated()`.
- **Scheduled Task Baseline Alignment**: Corrected display names and comments in `FeedSchedulerLifecycleIntegrationTest.java` to accurately state that `ppv.feed.scheduler.enabled=true` introduces zero delta to the existing scheduled task baseline (preserving Moments and RagRecoveryService tasks without introducing any new TaskScheduler bean).

---

## 3. Focused Verification Receipts (Remediation Round 2)

All six focused test groups were executed under `scripts/run-bounded-command.py` with `--profile normal --timeout-seconds 300`. Receipts are recorded in `logs/daily-trends-radar/dtr26-s2b-rem2-final/`:

| Group | Test Classes | Receipt File | Exit Code | Result | Duration |
| --- | --- | --- | --- | --- | --- |
| **Group 1: scheduler-unit** | `FeedScheduledRefreshTest`, `FeedSchedulerLifecycleTest` | `scheduler-unit.json` | 0 | `PASS` | 8.5s |
| **Group 2: scheduler-postgres** | `FeedScheduledRefreshIntegrationTest`, `FeedScheduledRefreshConcurrencyIntegrationTest` | `scheduler-postgres.json` | 0 | `PASS` | 38.6s |
| **Group 3: scheduler-spring-lifecycle** | `FeedSchedulerLifecycleIntegrationTest` | `scheduler-spring-lifecycle.json` | 0 | `PASS` | 55.1s |
| **Group 4: manual-scheduled-refresh** | `HackerNewsRefreshIntegrationTest`, `HackerNewsRefreshConcurrencyIntegrationTest`, `RssFeedRefreshIntegrationTest`, `RssFeedRefreshConcurrencyIntegrationTest`, `HackerNewsRefreshAdmissionTest` | `manual-scheduled-refresh.json` | 0 | `PASS` | 44.8s |
| **Group 5: feed-inbox-read-compatibility** | `FeedIntegrationTest`, `FeedValidationTest`, `FeedIngestionConcurrencyTest`, `FeedRadarIntegrationTest`, `CombinedFeedRadarIntegrationTest`, `SavedResourceInboxIntegrationTest` | `feed-inbox-read-compatibility.json` | 0 | `PASS` | 45.2s |
| **Group 6: http-architecture** | `FeedWebIntegrationTest`, `DailyTrendsWebIntegrationTest`, `RssDailyTrendsWebIntegrationTest`, `SavedResourceInboxWebIntegrationTest`, `OpenApiRouteInventoryIntegrationTest`, `ApiResponseEnvelopeAndErrorIntegrationTest`, `ApplicationArchitectureTests` | `http-architecture.json` | 0 | `PASS` | 71.4s |

**Summary:** 6/6 test groups passed, 0 failures, 0 errors, 0 skips, all within the 300s bounded execution window.

---

## 4. Mandatory Self-Review Checklist (`antigravity-self-review`)

- [x] **Owner Scope Compliance:** Narrowed FR-04 areas 1–4 implemented exactly as specified; zero unrequested features.
- [x] **Reuse & Design:** Reused existing Feed orchestrators, admission, and finalizer pathways. Public 4-arg lifecycle constructor introduced for controlled test injection without modifying production behavior.
- [x] **Privacy & Invariants:** Confirmed synthetic failure logging does not leak URLs, credentials, XML, or titles. Default property remains `enabled: false`.
- [x] **Concurrency & Transactions:** Verified late schedule-OFF after item staging cleanly rolls back item rows and timestamps. Verified single-flight admission release on active stop.
- [x] **Cleanliness:** Zero compiler warnings/errors, zero unneeded debug statements, zero POM changes, zero Flyway migrations.
- [x] **Artifact Integrity:** Original handback restored; remediation round 1 preserved; remediation round 2 written cleanly as a dated artifact.
- [x] **Git Safety:** Zero commits, zero pushes, zero tags, zero PR creation.

---

## 5. Deployment Activation Ledger

| Dimension | Verification Status | Operational Notes |
| --- | --- | --- |
| **Code Implementation** | `VERIFIED` | S2B-01 through S2B-12 proven via focused unit, PostgreSQL, and Spring lifecycle suites. |
| **Local Default Property** | `VERIFIED` | `ppv.feed.scheduler.enabled: false` committed in `application.yml`. |
| **Production Topology A Conformance** | `NOT_VERIFIED` | Exactly one active backend JVM confirmed by Owner design decision, but actual deployment runtime environment/orchestration is not certified by this backend test suite. |
| **Unattended Polling Activation** | `HELD` | Background polling must remain disabled until production deployment verification is independently conducted. |
