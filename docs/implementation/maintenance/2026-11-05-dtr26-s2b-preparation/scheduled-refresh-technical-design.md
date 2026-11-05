# DTR26-S2B — Source-backed technical design (proposed, NOT implemented)

**Baseline:** `47d4d6d3db49c2fd37211147502e88453a536458` (2026-11-05). All rules below describe **proposed S2B behavior** unless identified as existing implementation.

**Preparation review:** [independent re-review 1](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-re-review-1.md) = `READY_FOR_HANDOFF`; PH-01/PH-02 CLOSED for design only. No S2B application/runtime result or implementation authority is claimed.

## 1. Current reuse map

| Already implemented | Exact reuse/limit |
| --- | --- |
| `FeedSourceRepository.findDueSources(Instant, Pageable)` | Enabled+scheduled sources, `nextFetchAt IS NULL OR <= cutoff`, null-first then time/id order; includes unsupported types and is exposed through existing API; **do not mutate its semantics**. |
| `FeedSourceService.createSource/updateSource` | Current source-enabled/schedule flags/interval/next-fetch transitions under current CRUD; positive interval including one minute accepted. |
| `FeedItemService.ingestFetch` | Atomic normalized upsert and source fetch time update after success, with source lock. It does **not** accept a scheduler flag. |
| `HackerNewsRefreshService` | `@Transactional(NEVER)` manual HN outbound fetch, existing bounded resources; default 30 selected top IDs; uses shared `HackerNewsRefreshAdmission`. |
| `RssFeedRefreshService` | `@Transactional(NEVER)` manual RSS document fetch, safe network/XML parser, same admission object. |
| HN/RSS finalizers | Short `@Transactional(REQUIRED, timeout=5)` + existing item/source lock order and authoritative enabled/type/RSS exact URL check. **No current scheduled-invocation check.** |
| Existing `/api/v1/feed/*` routes | Owner-authenticated source CRUD, HN manual POST, RSS manual POST, source due GET, HN-only Radar GET, combined Radar GET; no scheduler endpoint. |

## 2. Scheduler activation — PH-02 reviewed preparation design (runtime NOT_RUN)

**Selected proposed mechanism:** one **Feed-owned programmatically registered recurring callback**, not annotation-driven application-wide scheduling. At the published baseline, `RagRecoveryService.reconcile()` is an unconditional RAG `@Scheduled` method (30s default); adding `@EnableScheduling` anywhere in the *same application context* would also discover that RAG method. A Feed configuration package location or `@ConditionalOnProperty` **does not restrict** `@EnableScheduling` scanning to Feed.

- Introduce a conditional Feed-owned lifecycle component for `ppv.feed.scheduler.enabled=true`; property is **false by default**. **Do not add** `@EnableScheduling`, `@Scheduled`, `SchedulingConfigurer`, or an application-wide annotation task registrar to turn S2B on. Do not modify the RAG component to work around this finding.
- Feed owns a **private**, single-thread `ThreadPoolTaskScheduler` instance (not exposed as a general-purpose `TaskScheduler` bean), exactly **one** direct `scheduleWithFixedDelay(feedTick, scheduler.getClock().instant().plusSeconds(60), Duration.ofSeconds(60))` registration. Use the Spring `TaskScheduler` contract's `Instant + Duration` overload. Register once after application readiness (e.g., `ApplicationReadyEvent`), not during bean construction. No separate provider worker pool/queue.
- Own the returned `ScheduledFuture` and scheduler lifecycle. During context close: mark stopping **before** cancellation; cancel the future, refuse further dispatch, dispose the scheduler and handle the one potentially active tick within the existing bounded provider deadlines. Avoid a non-daemon orphan; prove deterministic start/stop/restart and failure behavior. No live hot-toggle; configuration changes require a restart.
- Global OFF means **no Feed timer instance, registration, callback or automatic provider I/O**. Global ON adds **only** the Feed callback and must not incidentally create `ScheduledAnnotationBeanPostProcessor`/activate RAG or unrelated `@Scheduled` methods. Another feature may separately have its own startup behavior; the S2B test must distinguish existing RAG bean initialization/recovery from newly *periodic* annotation-driven calls.
- Spring API references: [TaskScheduler fixed-delay registration](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html), [EnableScheduling annotation scope](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableScheduling.html). Check exact pinned Spring binary API when writing the later handoff/code; this document does not assert working implementation.

This design is scoped to **Owner-confirmed topology A: exactly one active backend JVM** (direct Owner decision 2026-11-05; see [Owner decisions](owner-scope-and-decisions.md)). Actual deployment topology is **NOT_VERIFIED** and must be checked before unattended polling is enabled. It supplies no distributed at-most-once guarantee; concurrent active JVMs (including rolling restart overlap) are out of scope and require a separately approved design, not a local lock presented as distributed coordination.

## 3. Candidate selection — PH-01 reviewed stable-ID design (runtime NOT_RUN)

**Do not reuse or change** the public `findDueSources` / `GET /api/v1/feed/sources/due`: it intentionally sorts `(next_fetch_at NULL first, next_fetch_at, id)` and includes additional enum types. That mutable ordering caused the independently reproduced 30-source starvation (PH-01).

**Selected S2B-only internal query:** filter *before limiting* to `enabled=true`, `scheduledRefreshEnabled=true`, `type IN (HACKER_NEWS,RSS)` and `(nextFetchAt IS NULL OR nextFetchAt <= :cutoff)`; **order solely by stable `source.id ASC`**. Use a nullable **in-memory `lastExaminedSourceId` cursor**, never `(nextFetchAt,id)`. Implement two explicit repository queries/branches to avoid nullable parameter typing surprises in JPQL/PostgreSQL:

```sql
-- JPQL-equivalent predicates (camelCase entity fields in actual JPQL):
-- From beginning, if cursor is null:
SELECT s FROM FeedSource s
WHERE s.enabled = true AND s.scheduledRefreshEnabled = true
  AND s.type IN :supportedTypes
  AND (s.nextFetchAt IS NULL OR s.nextFetchAt <= :cutoff)
ORDER BY s.id ASC

-- After cursor, if lastExaminedSourceId is not null:
SELECT s FROM FeedSource s
WHERE s.enabled = true AND s.scheduledRefreshEnabled = true
  AND s.type IN :supportedTypes
  AND (s.nextFetchAt IS NULL OR s.nextFetchAt <= :cutoff)
  AND s.id > :lastExaminedSourceId
ORDER BY s.id ASC
```

Each query uses `PageRequest.of(0, 20)` (never an unbounded Java-side fetch). The cursor is **the ID of the last row actually inspected**, not the last row in the returned page and not the last successfully refreshed source. Candidate `nextFetchAt` may change because of success; its **ID does not move**. If no due matching row exists *after* the cursor, reset it to `null` and **end the current tick**, retrying from the beginning on the next tick; do not exceed the tick's one-page/20-examined budget with an immediate wrap query.

**Exact tick algorithm (proposed):**

1. On each fixed-delay callback, snapshot `cutoff=now`, check stop/global enabled; query at most **20 supported due rows** after the cursor (or from start if null). Provider types excluded by the query do not consume the candidate limit.
2. Visit returned candidates by increasing ID. For **each actually examined** row, advance the cursor to its ID, including cooldown and busy cases. If currently cooling down, skip dispatch; otherwise invoke that provider's **scheduled** entrypoint. Count every orchestrator invocation (including `busy`/rejected) toward the **two-dispatch-per-tick** cap. Stop scanning **immediately after the second invocation**, leaving unexamined rows' IDs for subsequent ticks. At most 20 examined, two invocations and two outbound provider attempts; only one provider request path at a time.
3. Provider orchestrators acquire the existing shared admission **once**, then re-read due/source eligibility **after acquiring admission, before outbound I/O**; a now-not-due source returns without network and without false success timestamp/cooldown. Do not acquire the admission lock a second time in the scheduler. After any provider error/busy admission, apply bounded scheduler-local cooldown. Release the admission normally in the provider finally block.
4. On an empty query page, reset `lastExaminedSourceId=null` and end that tick. On restart reset cursor and cooldown. No durable snapshot, keyset by mutable due time, second per-tick page, global queue or new database table.

**PH-01 all-success repeat-tick trace (static design model, not DB acceptance):** 30 supported sources IDs 1–30, `id=1` initially `nextFetchAt=0`, IDs 2–30 `NULL`; source types alternate HN/RSS, interval one minute, all provider calls succeed; logical ticks at times 0, 60, ... seconds (assume instantaneous calls for this trace). Selected provider attempts are:

| Tick(s) | IDs actually attempted | Cursor after tick |
| --- | --- | --- |
| 1, 2, 3, 4, 5 | (1,2), (3,4), (5,6), (7,8), (9,10) | 2,4,6,8,10 |
| 6, 7, 8, 9, 10 | (11,12), (13,14), (15,16), (17,18), (19,20) | 12,14,16,18,20 |
| 11, 12, 13, 14, 15 | (21,22), (23,24), (25,26), (27,28), (29,30) | 22,24,26,28,30 |
| 16 | No ID greater than 30; reset cursor, **0 attempts** | null |
| 17 | (1,2) are due again; resume from start | 2 |

**30/30 unique sources attempted by tick 15**; no source is hidden by a recently successful source migrating to a later due-time key. A second 34-row static scenario has **30 supported** (HN/RSS), four interspersed unsupported IDs **4,12,19,27**, five permanent failures at IDs **1,2,3,5,6**, and two busy IDs **7,8**. With the same 5-minute cooldown and 2-invocation budget, initial attempt pairs are **(1,2), (3,5), (6,7), (8,9), ... , (33,34)**; 30/30 supported sources receive an invocation by tick 15, and the four unsupported IDs never do. Failure/busy still advances the ID cursor. These are **illustrative static traces only**, not real PostgreSQL, timer, or production receipts.

**Liveness boundary:** eventual first attempts hold for a finite, unchanged set of eligible sources remaining due until first service, continuing ticks and successful admission eventually possible. It is **not** a real-time fairness SLA under unbounded arrivals/source mutations, prolonged database errors, a permanently stopped scheduler, or a provider that can never be admitted. Reviewer must independently verify actual query/filter/attempt behavior on PostgreSQL and two counterexamples; a green static trace is not acceptance.

## 4. Invocation path and source authority

```
conditional tick (no DB transaction)
    -> scheduler-only due scan and global/schedule opt-in checks
    -> provider-specific scheduled invocation (HN or RSS)
         -> existing bounded single-flight admission (acquire ONCE inside provider)
         -> provider HTTP/parse outside ALL database transactions
         -> one short finalizer transaction
              -> generic ingestFetch (same physical transaction and existing lock order)
              -> authoritative locked source recheck
                 enabled && original provider type && scheduled_refresh_enabled
                 && RSS exact fetched feedUrl unchanged
                 // due eligibility was checked at dispatch; nextFetchAt has
                 // been rewritten by successful ingest, so do not compare it here
              -> commit OR rollback items+lastFetchedAt+nextFetchAt together
         -> admission release in finally
    -> scheduler-only cooldown on error (no changes to DB fetch times)
```

The existing manual `refreshSource(...)`/`refreshRssSource(...)` endpoints **must remain independent of scheduled flags**. Introduce the minimum explicit scheduled-invocation mode/flag passed down existing orchestrator→finalizer (or equivalent narrowly typed owner-local method), without a thread-local implicit mode or a second ingest pipeline. Tests must prove that generic `ingestFetch` joins the finalizer's **actual physical PostgreSQL transaction**; a test that observes only outside the ingest proxy is insufficient (see historical DTR26-S1 FR-05). Source row lock must remain held through commit; HTTP never holds a lock. Revalidate enabled/type at the final authority gate; RSS also exact `feedUrl`.

**Schedule changes during fetch:** for a scheduled invocation, if the latest authoritative row disables scheduling, rollback before commit. A concurrent interval edit **while scheduling remains enabled** does not cancel an already admitted refresh: generic ingestion computes the new `nextFetchAt` from the latest locked interval. Check due status immediately before dispatch, **not after ingest inside the finalizer**: ingest itself moves `nextFetchAt` into the future before final validation. A further due check there would reject every successful refresh. If the Owner instead requires cancellation after any interval edit, escalate that *different* contract before implementation. No guarantee of cancellation before an in-flight HTTP exchange completes, but no stale scheduled publication after the final check. Manual attempts retain existing behavior. Reviewer must independently verify that source schedule disable still rejects the commit and that an interval-only edit uses the fresh value; do not silently tighten accepted source update semantics.

**Avoid double admission:** the two provider orchestrators already acquire `HackerNewsRefreshAdmission` internally. The scheduler may check capacity for short-circuiting, but must **not acquire then invoke a method that acquires again**. If full, return one categorical busy result and set only a scheduler-side cooldown. Existing max 2 active across manual+HN+RSS is authoritative. A simple serial worker is enough and keeps runtime ownership explicit; a second background queue is not needed.

## 5. Retry/cooldown and timing

Use a scheduler-local bounded map `sourceId -> retryNotBefore` with **proposed fixed 5-minute cooldown** after an attempted provider failure or busy admission. This map is *not* a fetch success ledger. Keep at most **512** entries; evict expired first and use deterministic eviction if full. The 512-entry cap avoids unbounded in-memory retention but means sources evicted during extreme overload can be retried sooner; the global **two-attempts-per-tick** cap remains protective. Restart clears cooldown intentionally. Manual refresh is not throttled by this map. On successful refresh, remove source's cooldown entry. Crucially, `lastFetchedAt` and `nextFetchAt` remain unchanged on transport, XML, validation, timeout or transactional failure. The existing provider rollback semantics are retained.

A valid empty RSS document may count as a successful refresh; malformed/truncated XML does not. One-minute interval stays legal but is **best effort**, not a guarantee: fixed delay occurs after prior tick's work; two sequential HN calls may each require up to their accepted ~70s overall budget, so actual interval can exceed the configured minute. Application downtime also delays jobs. No replay backlog/storm on startup: initial delay and two-attempt cap ensure bounded first tick. Do not create arbitrary exponential retry engines or persistent failure statuses.

## 6. Lifecycle and operator evidence

The global switch is configured at **startup**; turning it off in deployment configuration requires restart/shutdown, not a live reconfiguration API. Graceful shutdown must stop new ticks, stop or finish in-flight work within existing time bounds, restore worker/admission state, and not create lingering threads. Verify with fake transport barriers/cancellation and bounded test receipts; no real RSS/HN connections in S2B acceptance. Log only source ID, provider type, categorical outcome and coarse timings; no feed URL/query tokens, raw XML, response bodies, article titles, credentials or full stack traces from upstream payloads.

**Pre-handoff stop conditions:** any proposal for more than one simultaneously active backend JVM contrary to Owner-confirmed topology A; exact-time SLA/durable retry requirement; breaking source CRUD or REST changes; inability to register a Feed-only programmatic timer without starting RAG annotation scheduling; inability to prove source flag at commit; starvation under finite stable due set; unacceptable resource growth; unapproved change to S1/S2A transaction, provider egress/identity or RAG isolation. Do not use a new schema, queue or lease to solve an unapproved problem.
