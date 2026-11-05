# DTR26-S2B — Operator controls, failure semantics and security

**Status:** proposed operating contract; no live scheduler, network request or runtime verification has occurred in this preparation.

## No-network default

`ppv.feed.scheduler.enabled` is a **proposed new property**, absent in the inspected `application.yml` at source baseline. Default must be `false`. The per-source existing `scheduled_refresh_enabled=true` flag **alone** never activates a timer. Globally off must create **no Feed timer instance/registration and invoke no automatic HN/RSS fetch**, even if many source rows already have schedule flags and `nextFetchAt=null`. Feed global ON must use one **private programmatic TaskScheduler** registration: **never `@EnableScheduling` or a Feed `@Scheduled` bean** because an existing RAG component also has an annotated periodic method. Disabling global config is a restart-time operation unless a future separately approved runtime toggle is implemented.

Example **future configuration**, not current working syntax guaranteed by implementation:

```yaml
ppv:
  feed:
    scheduler:
      enabled: ${PPV_FEED_SCHEDULER_ENABLED:false}
```

When globally enabled, only sources meeting all of: `enabled=true`, `scheduled_refresh_enabled=true`, positive interval, due timestamp (including `null` for never fetched), and provider `HACKER_NEWS` or `RSS` are candidates. HN uses its fixed official API paths; RSS uses only configured `feedUrl` through the accepted public-HTTPS RSS policy. The scheduler never fetches an article link, enclosure, comment thread or credential-bearing URL. If `feedUrl` is missing/invalid, the existing RSS policy fails safely without network.

## Normal operations

- Existing source create/update APIs are the Owner-facing way to enable/disable an individual schedule; no scheduler management REST surface is proposed. The update path's existing positive interval rules remain intact.
- Existing `GET /api/v1/feed/sources/due` remains a **metadata query**, not a guarantee that a job will execute on a precise second; it continues to include other type values as previously accepted. The scheduler's internal selection is narrower.
- Each successful, fully committed generic ingest advances `lastFetchedAt` and `nextFetchAt` using the latest authoritative schedule interval under source lock. Last-fetched is evidence of successful normalized ingest, not merely an HTTP request beginning.
- The cadence is not a per-source cron schedule: **one Feed-owned single-thread programmatic fixed-delay callback** begins ~60 seconds after application readiness and polls for due sources. No exact-time SLA, priority, global popularity ranking or job-history dashboard is included.
- Fair scheduler-only candidate enumeration is **by immutable source ID**, never `(nextFetchAt,id)`. At most 20 eligible rows actually inspected and two scheduled provider-entry invocations per tick (a busy rejection consumes an invocation). The ID cursor advances to the last *examined* row, not the end of a prefetched page. An empty page after the cursor resets it and defers wrap until the next tick. Unsupported provider types are excluded at database query time, before limiting. Public due ordering stays unchanged.

## Failures and concurrency

| Event | Required behavior |
| --- | --- |
| Global scheduler property false | No Feed scheduler object/registered future, no automatic provider I/O; manual source refresh still usable. |
| Global scheduler property true | Exactly one Feed-owned programmatic recurring task; activation must not schedule RAG `reconcile()` or any other unrelated `@Scheduled` callback. |
| Source schedule disabled/changed after due selection | Provider acquires its existing shared admission **once** and rechecks due before outbound I/O; later, a scheduled-only authoritative source guard after network work rolls back on schedule OFF. Interval-only edit computes next due from the new locked interval. |
| Source disabled/type switched/RSS feedUrl edited during HTTP | Existing provider finalizer's source authority preserved; rollback items, last and next timestamps. |
| Concurrent manual + scheduled same source | Shared single-flight admission allows one; other rejects categorically, no double acquire, no duplicate network call. |
| Two different sources already refreshing | Max-two shared admission prevents third; no unbounded scheduler queue. |
| Provider 429/timeout/network/invalid XML | Preserve items and last/next fetch timestamps on failed refresh, in-memory 5-minute retry cooldown for that source, no tight retry storm. |
| Valid empty RSS response | Successful zero-item ingest may update last/next only when complete XML/normalization/finalizer succeeds. |
| Restart with many due rows | New process resets stable-ID cursor and cooldown; initial delay, at most 20 candidates actually examined and two provider-entry invocations per tick; no catch-up queue or import flood. |
| Shutdown while HTTP in progress | Mark stopping, cancel retained ScheduledFuture, prohibit further dispatch, dispose the private timer; end/finish the one in-flight tick within existing bounded provider deadlines, avoid leaked timer thread/admission or DB lock. |
| More than one concurrently active application JVM (including rollout overlap) | **NOT SUPPORTED by Owner-confirmed S2B topology A (2026-11-05)**. Prevent concurrent active replicas before enabling automatic Feed refresh; if topology changes, STOP for new Owner approval and separate design. Local admission/cooldown is not a distributed lease. |

Do not store upstream errors/raw XML/article text in the cooldown map. Only `sourceId -> time` plus bounded bookkeeping. Logs should avoid user-supplied URLs, private headers, credentials, cookies and provider body fragments. Config false must not be a misleading 'scheduling succeeded' status. No changes to JWT/authentication, routing, feed URL SSRF/XXE security, CORS, migrations or RAG.

## Manual smoke versus unattended network

No live-provider smoke is needed for preparation or S2B acceptance unless separately authorized. Acceptance should run a full Spring context with a **fake provider** and `enabled=true` to verify **one direct Feed registration** and dispatch after initial delay, with an assertion that no new `ScheduledAnnotationBeanPostProcessor`/RAG periodic registration is introduced. In default/off context assert **zero Feed registrations and DNS/HTTP**. Keep unrelated RAG singleton startup work distinct from annotation-driven periodic invocation. A disabled source, unsupported type or global-off case must not call the provider, even if the date says due. The test suite must not leave scheduler threads running across other tests; use a controlled/fake clock or direct tick invocation for detailed timing, with one bounded integration test of real Spring scheduling wiring.

## Owner-confirmed topology A — operational enforcement remains unverified

On **2026-11-05**, the Owner confirmed **A: only one active backend JVM** for S2B ([Owner decision](owner-scope-and-decisions.md)). Deployment evidence is **NOT_VERIFIED**: production/run documentation must prohibit or prevent two simultaneously active application JVMs sharing the Feed database, including rolling restart overlap. If multiple replicas, strict exactly-once across restarts or persistent failure states become required, this bounded design is insufficient; STOP and seek **new** Owner scope approval instead of silently adding a distributed lock, table or task system. The independent reviewer must check that the topology decision and operational stop condition are clearly recorded, not re-ask the already-answered A/B question.
