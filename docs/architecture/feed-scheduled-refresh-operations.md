# Feed Scheduled Refresh Operations Guide

- **Status:** READY_FOR_OWNER_COMMIT / ACCEPTED (2026-11-05) for `DTR26-S2B` per [independent re-review 3](../implementation/maintenance/2026-11-05-dtr26-s2b-preparation/reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md). FR-01–04 CLOSED; remediation 3/3 consumed, independently accepted/STOPPED, no takeover. Delivery remains uncommitted. Keep default OFF; actual one-JVM/no-overlap deployment conformance and unattended activation remain separately held.
- **Module:** `feed`
- **Specification:** Opt-in, single-JVM background refresh of Hacker News and RSS/Atom feed sources honoring per-source scheduling state with stable-ID traversal, bounded concurrency, 5-minute cooldown, and rollback protection.

---

## 1. Architectural Overview and Guarantees

Feed scheduled refresh provides an automated polling mechanism for due Hacker News and RSS sources without introducing distributed clustering, database leases, message queues, or new Flyway schema migrations.

```text
[ApplicationReadyEvent]
       │
       ▼ (readiness + 60s)
[FeedSchedulerLifecycle] ──owns──> [ThreadPoolTaskScheduler] (pool=1, thread="feed-scheduler-")
       │
       ▼ (60s fixed delay)
[FeedScheduledRefreshService.executeTick()]
       │
       ├─► 1. Prune expired cooldowns (retryNotBefore <= now)
       ├─► 2. Query due sources: findScheduledDueSourcesFromStart / AfterCursor
       │       - Filter: enabled=true, scheduled_refresh_enabled=true, type IN (HN, RSS), next_fetch_at <= now OR NULL
       │       - Order: ORDER BY s.id ASC (stable, starvation-free)
       │       - Limit: PageRequest.of(0, 20)
       ├─► 3. If empty page: reset cursor=null, end tick
       └─► 4. Visit candidates ascending:
               - Advance cursor to current row ID
               - If in cooldown: skip dispatch, advance cursor
               - Increment invocation count (max 2 per tick)
               - Invoke provider scheduled entrypoint:
                   HackerNewsRefreshService.refreshScheduled(id) /
                   RssFeedRefreshService.refreshScheduled(id)
               - If invocations >= 2: break immediately (cursor = last examined row)
```

### Invariants

1. **Topology A (Single-JVM):** Designed strictly for topologies where exactly one backend JVM connects to the database. Distributed locking or multi-instance coordination is out of scope.
2. **Default OFF:** Global property `ppv.feed.scheduler.enabled` defaults to `false`. Global opt-in requires explicit startup configuration.
3. **Dual Opt-In:** A source is eligible for scheduled refresh if and only if:
   - Global property `ppv.feed.scheduler.enabled=true`
   - Per-source `scheduled_refresh_enabled=true`
   - Source `enabled=true`
   - Positive interval (`refresh_interval_minutes > 0`)
   - `next_fetch_at IS NULL` or `next_fetch_at <= now`
4. **RAG & Module Isolation:** The timer is a private `ThreadPoolTaskScheduler` owned directly by `FeedSchedulerLifecycle`. No `@EnableScheduling`, new `@Scheduled` annotation processor, or global `TaskScheduler` bean is introduced. Feed scheduling introduces zero delta to application-level scheduled task registries or periodic invocations; existing framework-level registrations (such as Modulith Moments or `RagRecoveryService.reconcile`) remain at their pre-existing baseline without any Feed-induced execution or configuration alteration.
5. **No Database Starvation:** Candidates are queried with stable `s.id ASC` ordering and an ID cursor (`s.id > :cursor`). Success-driven `next_fetch_at` modifications cannot hide unexamined sources.
6. **Bounded Resource Consumption:** At most 20 candidate rows examined per tick; at most 2 serial provider invocations per tick; 60s delay between ticks.
7. **Commit Authority Protection:** Scheduled finalizers verify `scheduled_refresh_enabled=true` under pessimistic write lock (`findByIdForUpdate`). Disabling scheduling mid-fetch rolls back staged items and fetch timestamps atomically.
8. **Fault Isolation:** Bounded scheduler-local 5-minute cooldown (maximum 512 entries with deterministic eviction) isolates failing or busy sources without updating database timestamps or throttling manual refreshes.

---

## 2. Configuration Reference

```yaml
ppv:
  feed:
    scheduler:
      enabled: ${PPV_FEED_SCHEDULER_ENABLED:false}
```

| Property | Default | Environment Variable | Purpose |
| --- | --- | --- | --- |
| `ppv.feed.scheduler.enabled` | `false` | `PPV_FEED_SCHEDULER_ENABLED` | Global startup toggle for the Feed background refresh timer. Must be explicitly set to `true` to activate scheduled polling. |

---

## 3. Lifecycle and Execution Timing

### 3.1 Startup Cadence
- Timer initializes only when `ppv.feed.scheduler.enabled=true`.
- Listens for `ApplicationReadyEvent` from matching `ApplicationContext`.
- Registration is idempotent; duplicate readiness events or foreign contexts are ignored.
- **Initial delay:** 60 seconds after `ApplicationReadyEvent`.
- **Cadence:** `scheduleWithFixedDelay(refreshService::executeTick, now + 60s, Duration.ofSeconds(60))`. The next tick begins 60 seconds *after* the prior tick completes all work.

### 3.2 Graceful Shutdown
- Implements `DisposableBean.destroy()`, delegating to `stop()`.
- Marks `stopping = true` on `FeedScheduledRefreshService` and cancels active `ScheduledFuture` with interruption (`cancel(true)`).
- Performs a single bounded await termination wait on `taskScheduler` for up to 75 seconds (`DEFAULT_SHUTDOWN_AWAIT_SECONDS`), accommodating existing provider HTTP deadlines (70s for HN, 25s for RSS).
- Distinguishes clean termination from timeout/failed cleanup via `isTerminated()`: returns `true` when the worker pool terminates cleanly within timeout, or logs a warning (`Feed scheduler did not terminate within ... seconds timeout`) and returns `false` if termination times out.

---

## 4. Query & Traversal Algorithm

### 4.1 SQL Queries
- Query 1 (from start / reset):
  ```sql
  SELECT s FROM FeedSource s
  WHERE s.enabled = true
    AND s.scheduledRefreshEnabled = true
    AND s.type IN :supportedTypes
    AND (s.nextFetchAt IS NULL OR s.nextFetchAt <= :cutoff)
  ORDER BY s.id ASC
  ```
- Query 2 (after cursor):
  ```sql
  SELECT s FROM FeedSource s
  WHERE s.enabled = true
    AND s.scheduledRefreshEnabled = true
    AND s.type IN :supportedTypes
    AND (s.nextFetchAt IS NULL OR s.nextFetchAt <= :cutoff)
    AND s.id > :afterId
  ORDER BY s.id ASC
  ```

### 4.2 Tick Processing Logic
1. If stopping or thread interrupted: exit immediately.
2. Capture UTC `now`. Prune expired cooldowns (`retryNotBefore <= now`).
3. Query one page of at most 20 rows (`PageRequest.of(0, 20)`).
4. If page is empty: reset cursor to `null`, exit tick.
5. Iterate rows by ascending ID:
   - Advance cursor to current row ID.
   - If cooling down (`retryNotBefore > now`): continue to next row without invoking provider.
   - Increment tick invocation count.
   - Invoke `refreshScheduled(sourceId)`.
   - On `REFRESHED`: remove source from cooldown map.
   - On `BUSY` or `FAILED`: set cooldown from outcome completion instant to `outcomeInstant + 5 minutes`.
   - On `NOT_ELIGIBLE`: no cooldown change, counts towards 2-invocation cap.
   - If invocations $\ge$ 2: break loop immediately. Cursor remains at the last actually examined row ID.

---

## 5. Provider Execution & Commit Authority

```text
refreshScheduled(sourceId) [@Transactional(NEVER)]
  │
  ├─► 1. HackerNewsRefreshAdmission.acquire(sourceId)
  │       - Global max 2 concurrent refreshes
  │       - Single-flight per source
  │       - If busy: throw FeedRefreshException -> outcome = BUSY
  │
  ├─► 2. Read fresh FeedSource row from DB (short read):
  │       - Assert: exists, enabled, matches provider type,
  │                 scheduledRefreshEnabled=true, interval > 0,
  │                 nextFetchAt <= now OR NULL
  │       - If invalid: release admission, return NOT_ELIGIBLE
  │
  ├─► 3. Perform network I/O + parsing outside DB transactions:
  │       - HN: HackerNewsClient.fetchTopStories / fetchItem
  │       - RSS: RssFeedClient.fetchFeed -> RssAtomParser.parseFeed
  │
  ├─► 4. Finalizer transaction [@Transactional(REQUIRED, timeout=5)]:
  │       - Generic ingest: FeedItemOperations.ingestFetch(sourceId, fetchedAt, items)
  │       - Pessimistic write lock: findByIdForUpdate(sourceId)
  │       - Refresh: entityManager.refresh(lockedSource)
  │       - Authority recheck:
  │           assert lockedSource.isEnabled()
  │           assert lockedSource.getType() == expectedType
  │           assert lockedSource.isScheduledRefreshEnabled()  <-- Rollback if false!
  │           assert lockedSource.getFeedUrl().equals(feedUrl) [RSS only]
  │       - Ingest updates nextFetchAt = fetchedAt + lockedSource.getRefreshIntervalMinutes()
  │
  └─► 5. finally: release admission
```

### Concurrent Source Edit Semantics
- **Scheduling toggled OFF during fetch:** Finalizer detects `!lockedSource.isScheduledRefreshEnabled()`, throws `SOURCE_CHANGED_DURING_REFRESH`, and rolls back both staged `feed_items` and `feed_sources` timestamp updates.
- **Enabled toggled OFF during fetch:** Finalizer rolls back completely.
- **Source type or RSS feedUrl changed during fetch:** Finalizer rolls back completely.
- **Interval-only edit during fetch:** Finalizer succeeds; generic ingestion uses the fresh locked interval to compute `next_fetch_at = fetchedAt + freshInterval`.

---

## 6. Cooldown and Overload Protection

- **Cooldown duration:** Fixed 5 minutes (`Duration.ofMinutes(5)`).
- **Triggers:** Categorical `BUSY` (admission already running / capacity reached) or `FAILED` (upstream 429, timeout, network error, XML parse error, finalizer timeout, or source changed during refresh).
- **Storage:** Local in-memory map `ConcurrentHashMap<Long, Instant>`. Never written to database.
- **Capacity:** Maximum 512 entries.
- **Eviction policy:**
  1. Expired entries (`retryNotBefore <= now`) are pruned first.
  2. If still at 512 capacity, evicts entry with smallest `retryNotBefore` (tie-breaker: smallest source ID).
- **Manual Isolation:** Manual refresh endpoints (`/refresh`, `/refresh-rss`) do NOT check the scheduler cooldown map and remain independently usable.

---

## 7. Topology A Operational & Deployment Procedures

> [!IMPORTANT]
> **Pre-activation Requirement:** The operator MUST verify that exactly one active backend JVM is connected to the database before enabling `ppv.feed.scheduler.enabled=true`.

### Rollout / Upgrade Procedure
1. **Prevent Overlap:** When deploying a new application container or jar, DO NOT use rolling zero-downtime restarts that temporarily run two instances concurrently against the same PostgreSQL database.
2. **Stop Old Process First:**
   - Stop and terminate the existing backend instance.
   - Wait for the process to exit completely (ensuring all locks and database connections are closed).
3. **Start New Process:**
   - Launch the updated backend instance.
   - On startup, the scheduler will observe an initial 60-second delay before the first tick, preventing startup query storms.
4. **If Multi-Instance Topology Is Required:** Keep `ppv.feed.scheduler.enabled=false`. Multi-instance scheduling requires distributed locking or cluster leader election, which is intentionally deferred.

---

## 8. Rollback and Disaster Recovery

### Disabling the Scheduler
1. Set `PPV_FEED_SCHEDULER_ENABLED=false` in application environment or configuration.
2. Restart the backend service.
3. Verify startup logs indicate no Feed scheduler registration.
4. All manual feed operations, Universal Inbox, and Radar endpoints remain fully operational.

### Database Recovery
- Because scheduled refreshes use the exact same transactional finalizer and generic ingestion as manual refreshes, no orphan rows, partial feed items, or corrupted timestamps can be persisted.
- If a source was mid-refresh when the application halted, database transactions are rolled back by PostgreSQL upon connection termination, and in-memory cooldown/cursor state resets cleanly on restart.
