# Implementation Handback — DTR26-S2B: Opt-in Scheduled HN/RSS Refresh

- **Handoff ID / Date:** `DTR26-S2B` / 2026-11-05.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Implementer:** Antigravity (`backend-implementer`), `/antigravity-implement-handoff` and mandatory `/antigravity-self-review`.
- **Reviewer:** Separate independent Codex `$codex-final-review`.
- **Remediation Counter:** Round 0/3 consumed; takeover `NOT_AUTHORIZED`. Initial implementation round.
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

## 2. Verification Receipts (6 Focused Groups)

All 6 groups executed via `scripts/run-bounded-command.py` with the `normal` profile ($\le 300$s per check) inside run directory `logs/daily-trends-radar/dtr26-s2b-8f2aab72fb2943029473d92a4bd58ed4/`:

| Group | Checks / Test Classes | Result | Exit | Duration | PID | Receipt File |
| --- | --- | --- | --- | --- | --- | --- |
| **1. scheduler-unit** | `FeedScheduledRefreshTest`, `FeedSchedulerLifecycleTest` | **PASS** | 0 | 7.05s | 26544 | `scheduler-unit.json` |
| **2. scheduler-postgres** | `FeedScheduledRefreshIntegrationTest`, `FeedScheduledRefreshConcurrencyIntegrationTest` | **PASS** | 0 | 28.59s | 7764 | `scheduler-postgres.json` |
| **3. scheduler-spring-lifecycle** | `FeedSchedulerLifecycleIntegrationTest` | **PASS** | 0 | 35.09s | 11276 | `scheduler-spring-lifecycle.json` |
| **4. manual-scheduled-refresh** | `HackerNewsRefreshIntegrationTest`, `HackerNewsRefreshConcurrencyIntegrationTest`, `RssFeedRefreshIntegrationTest`, `RssFeedRefreshConcurrencyIntegrationTest`, `HackerNewsRefreshAdmissionTest` | **PASS** | 0 | 39.08s | 26636 | `manual-scheduled-refresh.json` |
| **5. feed-inbox-read-compatibility** | `FeedIntegrationTest`, `FeedValidationTest`, `FeedIngestionConcurrencyTest`, `FeedRadarIntegrationTest`, `CombinedFeedRadarIntegrationTest`, `SavedResourceInboxIntegrationTest` | **PASS** | 0 | 46.61s | 31704 | `feed-inbox-read-compatibility.json` |
| **6. http-architecture** | `FeedWebIntegrationTest`, `DailyTrendsWebIntegrationTest`, `RssDailyTrendsWebIntegrationTest`, `SavedResourceInboxWebIntegrationTest`, `OpenApiRouteInventoryIntegrationTest`, `ApiResponseEnvelopeAndErrorIntegrationTest`, `ApplicationArchitectureTests` | **PASS** | 0 | 70.23s | 26140 | `http-architecture.json` |

---

## 3. Mandatory Self-Review Summary

- **Scope & Reuse:** Reused existing Hacker News and RSS orchestrators, destination policies, XML parsers, and generic ingestion. Added only 2 repository query branches with stable-ID ordering, 1 service, 1 outcome enum, 1 lifecycle component, and overloaded finalizers.
- **Invariants & Safety:** Global property defaults to false. Private ThreadPoolTaskScheduler ensures zero exposure to Spring's annotation-driven scheduling, completely preventing RAG's `@Scheduled` reconcile method from running.
- **Concurrency & Races:** Proved PostgreSQL backend PID and transaction ID sharing across generic ingest and authoritative locked source re-validation. Verified negative control where `REQUIRES_NEW` detects independent transaction divergence. Mid-fetch disable of scheduling rolls back both staged items and timestamp updates.
- **Fairness & Boundedness:** Real PostgreSQL proof of PH-01 Traces 1, 2, and 3 establishes that 30 eligible sources receive serial invocations by tick 15, unsupported types are ignored, cooling rows advance the cursor without dispatch, and ticks break immediately after 2 invocations.
- **Hygiene & Limits:** No POM edits, no Flyway migrations, no payload logging. All 235 OpenAPI routes remain unchanged.

---

## 4. Documentation Synchronization Check

- **UPDATED:**
  - `backend/src/main/resources/application.yml` (added `ppv.feed.scheduler.enabled`)
  - `docs/architecture/feed-scheduled-refresh-operations.md` (new operator guide)
  - `README.md` (added navigation link in Architecture section)
  - `docs/repository/repository-package-tree.md` (added Feed internal scheduling capability)
  - `docs/implementation/handoffs/ACTIVE.md` (marked `IMPLEMENTED_AWAITING_CODEX_REVIEW`)
  - `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/executable-handoff-2026-11-05.md` (marked `IMPLEMENTED_AWAITING_CODEX_REVIEW`)
  - `docs/implementation/maintenance/2026-11-05-dtr26-s2b-preparation/implementation-handback-2026-11-05.md` (this handback)
- **ALREADY_CONSISTENT:**
  - Frozen DBML and Schema v1.
  - Maven POM and module dependencies.
  - OpenAPI 235-operation contract inventory.
- **NOT_VERIFIED:**
  - Actual production single-JVM deployment topology. Activation remains held until verified by the operator.
- **Counter:** S2B remediation `0/3`; takeover `NOT_AUTHORIZED`.

---

## 5. Next Step

**Next step:** Codex | `$codex-final-review` | `DTR26-S2B`, [ACTIVE](../../handoffs/ACTIVE.md) and [executable contract](executable-handoff-2026-11-05.md) | IMPLEMENTED_AWAITING_CODEX_REVIEW; default OFF, 6 focused gate groups passed (receipts in `logs/daily-trends-radar/dtr26-s2b-8f2aab72fb2943029473d92a4bd58ed4/`), self-review complete | remediation 0/3; takeover NOT_AUTHORIZED; S1/S2A history preserved.
