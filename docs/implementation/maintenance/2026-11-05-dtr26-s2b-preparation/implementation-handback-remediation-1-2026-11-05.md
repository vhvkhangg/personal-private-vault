# Implementation Handback — DTR26-S2B: Opt-in Scheduled HN/RSS Refresh

- **Handoff ID / Date:** `DTR26-S2B` / 2026-11-05.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Implementer:** Antigravity (`backend-implementer`), `/antigravity-implement-handoff` and mandatory `/antigravity-self-review`.
- **Reviewer:** Separate independent Codex `$codex-final-review`.
- **Remediation Counter:** Round 1/3 consumed; next correction/re-review 2/3; takeover `NOT_AUTHORIZED`.
- **Operational Status:** Default global property `ppv.feed.scheduler.enabled: false`. Actual production deployment conformance to Topology A (exactly one active backend JVM) remains `NOT_VERIFIED`; unattended activation is held pending deployment verification.

---

## 1. Acceptance Criteria Mapping (S2B-01 through S2B-12)

| Case ID | Scenario & Observable Proof | Test Selector(s) | Status |
| --- | --- | --- | --- |
| **S2B-01** | Global property absent/false: No timer instance, callback, or thread allocated. RAG scheduling un-triggered. Manual HN/RSS refresh usable. | `FeedSchedulerLifecycleIntegrationTest.SchedulerDisabledTest` | **PASS** |
| **S2B-02** | Global property true: Exactly one private ThreadPoolTaskScheduler (pool=1, thread `feed-scheduler-`), initial delay 60s, fixed delay 60s. No global scheduler bean or RAG scheduling. | `FeedSchedulerLifecycleIntegrationTest.SchedulerEnabledTest` | **PASS** |
| **S2B-03** | Source CRUD, interval=1m, public due GET, manual routes unchanged; exact 235 OpenAPI operations preserved. | `OpenApiRouteInventoryIntegrationTest`, `FeedValidationTest`, `FeedIntegrationTest` | **PASS** |
| **S2B-04** | At most 20 candidate rows examined; at most 2 serial provider invocations per tick; cursor tracks last actually examined row (never page end). Genuinely empty page wraps to start on next tick. | `FeedScheduledRefreshTest`, `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-05** | Real PostgreSQL verification of PH-01 Traces 1, 2, and 3: 30 alternating HN/RSS sources invoked by tick 15; unsupported sources 4/12/19/27 never dispatched; 20 cooling rows advance cursor and next tick breaks at second invocation without page-end advancement. | `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-06** | Shared admission: max 2 active refreshes across manual/scheduled HN/RSS; 3rd attempt returns `BUSY`; single-flight per source blocks concurrent refresh. | `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** |
| **S2B-07** | Source schedule/type/enabled/URL edits mid-fetch roll back staged items and timestamps (`SOURCE_CHANGED_DURING_REFRESH`); interval-only edit proceeds with fresh interval; shared physical transaction PID+XID proven for HN and RSS with negative control. | `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** |
| **S2B-08** | Provider failure/busy sets 5-minute cooldown; success removes cooldown; max 512 entries with deterministic eviction (smallest time, tie smallest ID); DB fetch times unchanged on failure. | `FeedScheduledRefreshTest`, `FeedScheduledRefreshIntegrationTest` | **PASS** |
| **S2B-09** | Application restart clears cursor and cooldown; single idempotent registration after readiness; 60s initial delay prevents startup query storm. | `FeedSchedulerLifecycleTest`, `FeedScheduledRefreshTest` | **PASS** |
| **S2B-10** | Stop before readiness prevents timer start; duplicate stop is idempotent; clean shutdown cancels future with interruption and terminates worker thread within timeout. | `FeedSchedulerLifecycleTest`, `FeedSchedulerLifecycleIntegrationTest` | **PASS** |
| **S2B-11** | Zero POM edits, zero database Flyway migrations, zero changes to frozen modules/interfaces; full backward compatibility with manual Radar and Universal Inbox. | All 6 focused test groups | **PASS** |
| **S2B-12** | Operator documentation, README link, package tree update, honest handback receipts, and explicit operational activation ledger separating code verification from deployment topology conformance. | `feed-scheduled-refresh-operations.md`, `README.md`, package tree, this document | **PASS** |

---

## 2. Initial Implementation Receipts (Historical Reference)

Initial implementation executed under `logs/daily-trends-radar/dtr26-s2b-8f2aab72fb2943029473d92a4bd58ed4/` (236 tests passed, exit 0, prior to independent review findings FR-01–FR-04).

---

## 3. Mandatory Self-Review Summary (Initial Implementation)

- Reused existing HN/RSS orchestrators, admission, and generic ingestion.
- Added internal scheduled refresh package, lifecycle component, and repository query branches.
- Default-OFF property ensures zero un-opted background activity.

---

## 4. Remediation Round 1 (2026-11-05) — Resolution of Findings FR-01 through FR-04

All four findings from independent final review `reviews/2026-11-05-dtr26-s2b-independent-final-review.md` are addressed and verified:

### FR-01 (MEDIUM) — Scheduled HN Selects 30 Items & Shared Provider Flow
- **`HackerNewsRefreshService.java`**: Scheduled HN refresh now uses default `SCHEDULED_DEFAULT_MAX_ITEMS = 30`, while manual callers retain their accepted `max=50` limit. Extracted private `executeRefreshFlow(Long sourceId, int boundedMaxItems, boolean scheduledMode)` to share common provider fetch/normalization/finalization steps without duplicating policies.
- **`RssFeedRefreshService.java`**: Similarly extracted private `executeRefreshFlow(Long sourceId, String feedUrl, boolean scheduledMode)` shared between manual and scheduled entry points.
- **`HackerNewsRefreshIntegrationTest.java`**: Added `scheduledHnRefresh_with60Stories_selectsExactly30_whileManualSelects50` with a 60-story fixture proving scheduled refresh selects and ingests exactly 30 items, whereas manual refresh requests 50 items.

### FR-02 (MEDIUM) — Cooldown Calculation from Completion Instant
- **`FeedScheduledRefreshService.java`**: `executeTick` retains `cutoff = Instant.now(clock)` for the due candidate query. When an outcome is `BUSY` or `FAILED`, it captures the outcome completion time `outcomeInstant = Instant.now(clock)` and sets cooldown `retryNotBefore = outcomeInstant.plus(COOLDOWN_DURATION)`.
- **`FeedScheduledRefreshTest.java`**: Added unit tests verifying advancing clock sets cooldown strictly from the outcome completion instant for both failure and busy outcomes.

### FR-03 (MEDIUM) — Lifecycle Initialization Boundary & Single Bounded Termination
- **`FeedSchedulerLifecycle.java`**:
  - The default constructor leaves the timer uninitialized.
  - Timer initialization occurs exclusively inside `synchronized onApplicationEvent(ApplicationReadyEvent)`.
  - Start and stop methods are fully serialized (`synchronized`).
  - `stop()` calls `taskScheduler.destroy()` exactly once (avoiding duplicate shutdown waits) and inspects `isTerminated()` to distinguish clean termination from timeout.
- **`FeedSchedulerLifecycleTest.java`**: Added unit tests covering:
  - Timer uninitialized before readiness event.
  - Stop consumes single await budget.
  - Cooperative clean termination vs timeout warning.
  - Interruption flag preservation.

### FR-04 (MEDIUM) — Finite Mandatory Proof & Truthful Documentation
- **Trace 1 (`FeedScheduledRefreshIntegrationTest.java`)**: Advances the clock 60s per tick in the 15-tick loop and records actual entrypoint invocations from spied refresh services.
- **Trace 2 (`FeedScheduledRefreshIntegrationTest.java`)**: Models per-source outcomes (5 FAILED, 2 BUSY, 23 REFRESHED), verifies all 30 supported IDs invoked in exact supported order, first pairs verified, 0 unsupported calls dispatched, and confirms that at tick 4 (t=180s) all 5 failing and 2 busy IDs are present in `cooldownMap`, while by tick 15 (t=840s) all entries have naturally expired and been pruned.
- **Negative Controls (`FeedScheduledRefreshConcurrencyIntegrationTest.java`)**: Runs `assertSharedPhysicalTransaction` against negative controls and asserts rejection.
- **Scheduled Authority & Rollback**:
  - Tested post-selection manual completion no-op.
  - Tested manual vs scheduled same-source barrier.
  - Tested late schedule-OFF after item staging: rolls back both the updated title on existing items and new item insertions, leaving `last_fetched_at` null.
  - Tested retained source lock blocks concurrent NOWAIT update until commit.
- **Spring Lifecycle & Privacy Observations (`FeedSchedulerLifecycleIntegrationTest.java`)**:
  - Captured callback execution in real Spring context.
  - Verified Logback marker-bearing log entries without logging private payloads.
- **Truthful Documentation (`feed-scheduled-refresh-operations.md`)**:
  - Line 48 updated: Removed absolute claim that RAG reconciliation is "never scheduled"; accurately states that the Feed private timer introduces zero delta to the existing scheduled task baseline.
  - Line 123 updated: Correctly specifies cooldown as `outcomeInstant + 5 minutes`.

---

## 5. Remediation Verification Receipts (All 6 Groups)

All 6 groups executed via `scripts/run-bounded-command.py` with the `normal` profile ($\le 300$s per check) inside fresh run directory `logs/daily-trends-radar/dtr26-s2b-rem1-final/`:

| Group | Checks / Test Classes | Result | Exit | Duration | Tests Run | Receipt File |
| --- | --- | --- | --- | --- | --- | --- |
| **1. scheduler-unit** | `FeedScheduledRefreshTest`, `FeedSchedulerLifecycleTest` | **PASS** | 0 | 8.55s | 25 | `scheduler-unit.json` |
| **2. scheduler-postgres** | `FeedScheduledRefreshIntegrationTest`, `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** | 0 | 51.11s | 14 | `scheduler-postgres.json` |
| **3. scheduler-spring-lifecycle** | `FeedSchedulerLifecycleIntegrationTest` | **PASS** | 0 | 32.08s | 5 | `scheduler-spring-lifecycle.json` |
| **4. manual-scheduled-refresh** | `HackerNewsRefreshIntegrationTest`, `HackerNewsRefreshConcurrencyIntegrationTest`, `RssFeedRefreshIntegrationTest`, `RssFeedRefreshConcurrencyIntegrationTest`, `HackerNewsRefreshAdmissionTest` | **PASS** | 0 | 60.39s | 49 | `manual-scheduled-refresh.json` |
| **5. feed-inbox-read-compatibility** | `FeedIntegrationTest`, `FeedValidationTest`, `FeedIngestionConcurrencyTest`, `FeedRadarIntegrationTest`, `CombinedFeedRadarIntegrationTest`, `SavedResourceInboxIntegrationTest` | **PASS** | 0 | 43.09s | 78 | `feed-inbox-read-compatibility.json` |
| **6. http-architecture** | `FeedWebIntegrationTest`, `DailyTrendsWebIntegrationTest`, `RssDailyTrendsWebIntegrationTest`, `SavedResourceInboxWebIntegrationTest`, `OpenApiRouteInventoryIntegrationTest`, `ApiResponseEnvelopeAndErrorIntegrationTest`, `ApplicationArchitectureTests` | **PASS** | 0 | 67.67s | 79 | `http-architecture.json` |
| **Total** | **All 6 Focused Groups** | **PASS** | **0** | **262.89s** | **250** | **All 0 Failures / Errors / Skips** |

---

## 6. Documentation Synchronization Check

- **UPDATED:**
  - `backend/src/main/resources/application.yml` (added `ppv.feed.scheduler.enabled`)
  - `docs/architecture/feed-scheduled-refresh-operations.md` (truthful zero-delta RAG baseline statement and outcome completion cooldown timing)
  - `README.md` (navigation link in Architecture section)
  - `docs/repository/repository-package-tree.md` (Feed internal scheduling capability)
  - `docs/implementation/handoffs/ACTIVE.md` (status `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation 1/3)
  - `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/executable-handoff-2026-11-05.md` (status `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation 1/3)
  - `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-2026-11-05.md` (this handback)
- **ALREADY_CONSISTENT:**
  - Frozen DBML and Schema v1 (0 migrations, 0 schema changes).
  - Maven POM and module dependencies (0 POM edits).
  - OpenAPI 235-operation contract inventory.
- **NOT_VERIFIED:**
  - Actual production single-JVM deployment topology. Activation remains held until verified by the operator.
- **Counter:** S2B remediation `1/3 consumed; next correction/re-review 2/3`; takeover `NOT_AUTHORIZED`.

---

## 7. Next Step

**Next step:** Codex | `$codex-final-review` | `DTR26-S2B`, [ACTIVE](../../handoffs/ACTIVE.md) and [executable contract](executable-handoff-2026-11-05.md) | IMPLEMENTED_AWAITING_CODEX_REVIEW; default OFF, 6 focused gate groups passed (250 tests, receipts in `logs/daily-trends-radar/dtr26-s2b-rem1-final/`), self-review complete | remediation 1/3; takeover NOT_AUTHORIZED; S1/S2A history preserved.
