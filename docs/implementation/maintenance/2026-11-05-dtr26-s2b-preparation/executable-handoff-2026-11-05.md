# Executable slice handoff — DTR26-S2B: opt-in scheduled HN/RSS refresh

- **Handoff ID / created:** `DTR26-S2B` / Codex, 2026-11-05.
- **Status:** `READY_FOR_OWNER_COMMIT / ACCEPTED` after [independent re-review 3](reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md). FR-01–04 CLOSED; reviewer 14 fresh PostgreSQL tests pass plus 236 source-applicable reused; separate implementer all-six receipts total 250. Remediation 3/3 consumed, independently accepted/STOPPED; no takeover or activation.
- **Authority:** current Owner `$codex-create-handoff` invocation after Owner publication of the [reviewed preparation](README.md), [Owner scope/topology A](owner-scope-and-decisions.md), and [independent READY_FOR_HANDOFF re-review 1](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-re-review-1.md).
- **Entry commit:** `e296d55db227988b55e86cfb81e83f45e507703a`, `docs(feed): prepare DTR26-S2B scheduled refresh`; local HEAD and remote main verified equal; clean entry tree. Only eight preparation/review Markdown files changed from source baseline `47d4d6d3db49c2fd37211147502e88453a536458`.
- **Preparation findings:** PH-01/PH-02 CLOSED **for design only**. Their PostgreSQL fairness and Spring timer-isolation proof is mandatory below. Preserve the [initial CHANGES_REQUESTED review](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-review.md).
- **Implementer:** Antigravity, existing [backend-implementer](../../../../.agents/agents/backend-implementer/agent.md), `/antigravity-implement-handoff`, then mandatory `/antigravity-self-review`.
- **Reviewer:** separate independent Codex `$codex-final-review`; no implementer self-acceptance.
- **Counter:** S2B remediation `3/3 consumed; independently ACCEPTED / STOPPED`; no further correction round, takeover NOT_REQUIRED / NOT_AUTHORIZED. Initial review consumed 0/3; remediation rounds 1/2/3 consumed 1/3, 2/3, 3/3 respectively. S1/S2A historical exhausted counters/accepted takeovers remain separate; no allowance transfers.
- **Activation boundary:** implementation and controlled fake-provider tests only. Leave global default OFF. Actual one-active-JVM deployment conformance remains NOT_VERIFIED before unattended activation; this handoff does not start production polling.

## Goal and governing references

Honor existing per-source scheduling state through one explicitly enabled Feed timer in the Owner-confirmed **exactly one active backend JVM** topology. Fairly inspect due HN/RSS sources, invoke their accepted refresh paths serially, preserve manual operations and cached reads, and prevent scheduled publication after source scheduling is disabled.

Read the [technical design](scheduled-refresh-technical-design.md), [operations/security](operations-and-security.md), [acceptance plan](acceptance-and-review-plan.md), [decision/risk ledger](decision-and-risk-ledger.md), and current [re-review resolutions](reviews/2026-11-05-dtr26-s2b-independent-pre-handoff-re-review-1.md). The reviewed defaults become executable limits here; they were design proposals, not prior runtime PASS or separately invented Owner confirmations.

Applicable sources: [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [ACTIVE](../../handoffs/ACTIVE.md), [root](../../../../AGENTS.md), [backend](../../../../backend/AGENTS.md), [Feed](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md), [test](../../../../backend/src/test/AGENTS.md) and [docs](../../../AGENTS.md) instructions; [Phase 10](../../phase-10/README.md), [Phase 13](../../phase-13/README.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module matrix](../../../architecture/module-dependency-matrix.md), [package tree](../../../repository/repository-package-tree.md), [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml), [S2A publication closeout](../2026-11-04-dtr26-s2-preparation/owner-publication-closeout-2026-11-05.md) and [S2A independent acceptance](../2026-11-04-dtr26-s2-preparation/reviews/2026-11-05-dtr26-s2a-independent-takeover-review-1.md). Historical provider/scheduler deferrals are superseded only for this approved extension; frozen contracts and historical evidence remain intact.

## Simplicity and reuse

| Decision | Smallest adequate mechanism / burden |
| --- | --- |
| REUSE | Existing source flags/intervals, HN/RSS orchestrators, secure adapters, shared admission, generic ingestion/finalizers and Inbox. No new persistence or provider pipeline. |
| KEEP | Conditional private Feed timer with one callback; explicit readiness/stop ownership. Small implementation, meaningful OFF/ON/isolation/shutdown proof. |
| SIMPLIFY / KEEP | Scheduler-only stable-ID query/cursor, one page of 20 and two serial provider-entry invocations. Medium PostgreSQL progress verification; no mutable due-time cursor/job queue. |
| KEEP narrow change | Explicit scheduled invocation through existing orchestrator/finalizer, eligibility after admission and scheduling flag under final source lock. Medium race/physical-transaction proof; manual signatures/behavior preserved. |
| SIMPLIFY | Fixed five-minute scheduler-local cooldown, maximum 512 entries, deterministic eviction/restart reset. No retry engine, success-ledger mutation or manual throttle. |
| REUSE | Existing 235 HTTP operations, error/envelope/DTO contracts and metadata-only public due query; focused compatibility. |
| DEFER / REJECT | Distributed coordination, durable retries, dynamic switch/cron/history UI, extra executors/agents/dependencies/providers. Single-JVM best-effort scope needs none. |

## User-visible acceptance — MUST

These IDs map to the preparation acceptance matrix; details and minimal evidence below are part of each case.

| ID | Scenario/input | Observable result | Minimal proof / reuse |
| --- | --- | --- | --- |
| S2B-01 | Missing/false global property with enabled scheduled due rows | No Feed timer instance/future/callback or automatic DNS/HTTP. Manual HN/RSS remain usable with global and source scheduling OFF. No unrelated scheduling activation. | Real Spring default/OFF context, production conditional wiring, fake provider call counts. |
| S2B-02 | Global true, readiness, eligible and excluded sources | Exactly one private programmatic fixed-delay Feed task, first run readiness+60s. No unsupported/disabled/unscheduled/future dispatch or newly periodic RAG/unrelated task. | Real Spring ON context, recording owned timer, actual registration/negative assertions. |
| S2B-03 | Source CRUD, interval 1m, public due GET, manual/read routes | Existing validation/defaults/transition behavior and public null-first due order/type inclusion unchanged; timing documented best effort. No new HTTP operation. | Feed/HTTP/OpenAPI compatibility; 235 exact manifest unchanged. |
| S2B-04 | >20 due, cooling rows, early second invocation, empty page | At most 20 examined/two provider-entry invocations; busy/no-longer-eligible calls count. Cursor is last examined, never page end; one query/page, wrap only next tick. | Unit counters/clock plus actual bounded PostgreSQL query/order/filter observations. |
| S2B-05 | Both PH-01 traces and 20 initially cooled candidates | All 30 supported receive an invocation by logical tick 15 in each specified trace; unsupported never called. Success-driven nextFetchAt changes cannot hide rows. Cooled rows advance cursor. | Real PostgreSQL query/tick/provider-entry traces, not static models or scanned-row counts. |
| S2B-06 | Manual/scheduled contention; HN+RSS using two slots; third attempt | Existing same-source single-flight/global max-two remains authoritative, acquired once inside provider; no third provider execution/double acquire/leaked admission. | Fake transport barriers through actual orchestrators and shared admission. |
| S2B-07 | Source schedule/type/enabled/RSS URL edits mid-fetch; interval-only edit | Network/XML outside transactions. Locked same-transaction finalizer rolls back when scheduling OFF/source authority changes. Interval-only edit remains eligible and uses fresh interval. | Real PostgreSQL staged rollback/retained lock/PID+XID and independent-commit negative control for both providers. |
| S2B-08 | 429/timeout/invalid XML/finalizer failure; cooldown expiry/manual call | Items/last/next unchanged on failure. Scheduler suppresses attempts for five minutes, manual bypasses cooldown, success removes entry. Valid empty RSS may commit success. | Fixed clock + real DB state, exact boundary/512-cap/eviction tests and manual regression. |
| S2B-09 | Restart with many overdue rows or repeated readiness events | Fresh cursor/cooldown, one registration after readiness, 60s initial delay and two-invocation cap; no catch-up queue/duplicate registration. | Destroy/recreate controlled contexts and direct tick traces. |
| S2B-10 | Stop before readiness, duplicate stop, shutdown during blocked provider | Mark stopping before future cancellation; no later dispatch. Cancel/dispose exactly owned timer, finite active work/admission cleanup, no private timer thread leak. | Lifecycle barriers, actual owned-thread termination and fresh bounded receipts. |
| S2B-11 | Changed source/config/tree and existing manual/read/save contracts | No schema/POM/module/root HTTP/security/provider-safety change; accepted HN/RSS/Radar/Inbox behavior unchanged. | Focused changed-provider PG/web/architecture checks; hash-qualified reuse of untouched transport/parser evidence. |
| S2B-12 | Handback, review, deployment/run instructions | Fresh/reused/implementer/reviewer evidence separated; explicit one-JVM/no-overlap activation check, actual topology never inferred from Owner choice. Scope/docs/status agree; separate Codex acceptance before Owner publication. | Dated self-review/handback, source/receipt hashes, Docs Sync and operational activation ledger. |

S2B-12 must separately record **implementation evidence** and **actual deployment conformance**. Code acceptance/publication does not certify or enable unattended operation. The preparation explicitly places actual topology verification before activation: leave it NOT_VERIFIED/activation held until real deployment evidence exists; never relabel it PASS from the Owner's already-recorded A decision.

## Activation, timer lifecycle and fixed limits

Add only the startup property below under the existing ppv tree; absent and false mean OFF. No runtime toggle/API, automatic environment edit, or external opt-in during this implementation.

```yaml
ppv:
  feed:
    scheduler:
      enabled: ${PPV_FEED_SCHEDULER_ENABLED:false}
```

| Limit | Executable rule |
| --- | --- |
| Topology | Exactly one active backend JVM sharing the Feed database; no rolling-restart overlap. Operational conformance checked before unattended activation. |
| Timer | One private Feed-owned ThreadPoolTaskScheduler, pool size 1, one retained ScheduledFuture; no general TaskScheduler/ScheduledExecutorService bean or provider worker queue. |
| Start/cadence | Initialize/register once after this application's readiness, scheduleWithFixedDelay(task, scheduler.getClock().instant()+60s, Duration.ofSeconds(60)). Initial delay 60s; subsequent delay after prior tick completes. |
| Due scan | One scheduler-only DB page, maximum 20 matching rows; stable source ID ASC; cursor last actually examined ID. No second/wrap query in that tick. |
| Invocations | Maximum two serial scheduled provider-entry calls per tick, including busy/error/no-longer-eligible results. HN uses its existing default 30 selected IDs; RSS existing selection cap. This counts refresh invocations, not individual bounded HN HTTP exchanges. |
| Shared admission | Existing per-source single-flight and global two active refreshes across manual/scheduled HN/RSS; no additional scheduler acquisition. |
| Cooldown | Five minutes from categorized attempted failure/busy, maximum 512 sourceId→retryNotBefore entries, expiry inclusive. Manual calls do not consult it. |
| Shutdown | Mark stopping, cancel retained future with interruption, dispose private timer and await owned termination for at most 75s (existing 70s HN budget plus 5s cleanup allowance). Tests use shorter controlled waits and assert production bound. Nontermination is failed cleanup evidence, never a clean-stop claim. |

Global OFF must not allocate/initialize the Feed timer. ON must not add EnableScheduling, Scheduled, SchedulingConfigurer, a global annotation registrar or ScheduledAnnotationBeanPostProcessor. Do not edit RAG to achieve isolation or introduce a Feed→RAG dependency. Ignore readiness from another context and make registration/stop idempotent; close-before-ready cannot later start a timer. Clean up an initialized timer if registration fails. Use one owned registration with cancellation/shutdown policies preventing delayed/periodic continuation.

The actual pinned **spring-context 7.0.9** JAR was inspected at creation with javap: getClock(), Instant/Duration fixed-delay overload, pool size, initialize/shutdown/destroy and termination-await APIs are available. [TaskScheduler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html), [ThreadPoolTaskScheduler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/concurrent/ThreadPoolTaskScheduler.html), and [EnableScheduling scope](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableScheduling.html) explain the mechanism; current public pages label 7.0.10, so installed 7.0.9 remains version authority. This is API feasibility, not scheduler runtime PASS.

Serialize callbacks and keep cursor/cooldown owned by the tick service. Linearize stop and authorization of a new provider-entry invocation using a short lifecycle guard; do not hold that guard or a DB transaction across HTTP. Already admitted work may finish or be interrupted within existing provider deadlines; stop forbids a subsequent invocation from the tick. Check interruption/stop before each candidate and dispatch. Do not reset interruption and continue to a second provider. Recurring recoverable query/provider failures must not silently cancel the whole future: consume them categorically, preserve valid cursor/state, and retain the next bounded tick. No payload/URL/cause-stack logging.

Fixed delay is best effort: two sequential HN refreshes may occupy about 140s before the next 60s delay. Source interval=1 minute remains legal; no cron/exact-time or hard fairness latency SLA. Restart/downtime loses local cursor/cooldown and does not replay a backlog.

## Exact stable-ID selection and cooldown algorithm

Add separate internal repository branches, with supportedTypes exactly HACKER_NEWS/RSS:

- From beginning: enabled=true AND scheduledRefreshEnabled=true AND supported type AND (nextFetchAt IS NULL OR nextFetchAt<=cutoff), ORDER BY id ASC.
- After cursor: same predicates plus id>lastExaminedSourceId, ORDER BY id ASC.
- Both use PageRequest.of(0,20); filter in DB **before** limiting. No nullable-parameter OR trick, mutable nextFetchAt keyset, OFFSET walk, unbounded list or mutation of existing findDueSources/public due behavior.

One tick:

1. If stopping/interrupted, return without query/dispatch. Capture one UTC cutoff=now, prune expired cooldown entries, execute at most one appropriate page query with no transaction spanning provider work.
2. On a genuinely empty page, reset cursor=null and **end**; a query error is not an empty page and must not fabricate a wrap/success.
3. Visit ascending IDs. Before each visit check stop; increment examined and set cursor to this actual row's ID. Cooling rows skip provider invocation but advance the cursor.
4. Before a non-cooling call check stop again, increment invocation count, and dispatch directly to that provider's explicit **scheduled** entrypoint. Count every entrypoint call, including admission busy/capacity and NOT_ELIGIBLE; no success-only counting. Do not acquire admission in the scheduler.
5. REFRESHED removes cooldown. NOT_ELIGIBLE is a no-op: no provider I/O, ingestion, timestamps, success claim or new cooldown. Categorized provider failure/busy sets retryNotBefore=current clock+5m, then processing may continue within the two-call cap.
6. Stop immediately after invocation two. Do not advance to any remaining prefetched row. Otherwise finish at 20 examined/page end. No second page even if every row cooled.

Cooldown expiry is retryNotBefore<=now. Remove expired entries first; if inserting would exceed 512, evict smallest retryNotBefore, tie smallest source ID, then insert. Store IDs/times only, no XML/URL/error payload. Eviction may permit an earlier retry under extreme overload; the two-invocations-per-tick cap still applies. Restart clears map/cursor. No database failure status/backoff timestamp is added.

Mandatory PostgreSQL traces (logical ordinal IDs may map to generated IDs while preserving this order):

| Fixture | Required actual provider-entry trace |
| --- | --- |
| 30 supported, alternating HN/RSS; logical ID1 initially non-null due=0, ID2–30 NULL; interval=1m; all successes advance actual DB nextFetchAt; ticks t=0,60,… | Tick 1 (1,2), tick 2 (3,4), … tick 15 (29,30): 30/30 invoked. Tick 16 empty/reset with zero calls; tick 17 (1,2). Re-query PostgreSQL each tick. |
| 34 rows; unsupported 4/12/19/27; failures 1/2/3/5/6; busy 7/8; other supported succeed | First pairs (1,2), (3,5), (6,7), (8,9); tick 15 (33,34). All 30 supported invoked; zero unsupported; actual cooldown and success due-time mutation retained. |
| First 20 due IDs already cooling until t=300 | Tick 1 examines 20, zero invocations, cursor20. At t=60 tick2 invokes21/22, cursor22, **not page-end30**. Later ticks cover the remaining stable finite set. |
| Cutoff/flags/types | NULL and exactly-cutoff due included; one instant future excluded; disabled/unscheduled/unsupported filtered before limit. Public due endpoint still includes its accepted other types and null-first/time/id order. |

Retain per-tick examined/invocation IDs/cursor/query row bound and persisted success/failure times; a static model or counting scanned rows does not satisfy S2B-04/05. No new metrics service/API is needed. The liveness claim assumes finite stable eligibility, continuing ticks and eventual admission; no guarantee under unlimited arrivals, arbitrary mutation, prolonged DB outage or permanent stop.

## Scheduled invocation and commit authority

Use narrow scheduled methods on **existing internal HN/RSS orchestrators**, preserving manual public/named-interface and HTTP signatures. A small owner-local explicit mode/boolean carried through orchestrator→finalizer is adequate; no ThreadLocal or duplicate refresh pipeline.

1. The scheduled entrypoint retains NEVER transaction propagation. Acquire existing shared admission **once** inside the provider, then read fresh source eligibility before DNS/HTTP: existence, enabled, original provider type, scheduling enabled, positive interval and nextFetchAt null or <=fresh now. A source no longer eligible returns NOT_ELIGIBLE and releases admission. Due-query results are not final authority. An RSS invocation captures the exact fresh feedUrl it will fetch.
2. Reuse actual accepted HN/RSS fetch/parse/normalization/deadlines/provenance and post-fetch timestamp capture. HN fixed official targets, RSS configured public HTTPS/XXE/DNS policy, ARTICLE_URL identity and provider quotas stay unchanged. No article/enclosure download, implicit retry, new URL validation in source CRUD or extra provider.
3. Through the existing Spring-proxied REQUIRED finalizer (5s), call generic ingestFetch in the **same physical transaction**, preserving item-then-source lock order. Existing source row FOR UPDATE + entityManager.refresh must assert enabled/original provider type; additionally scheduledRefreshEnabled for scheduled mode; RSS exact fetched URL equality remains.
4. Schedule OFF/type/enabled/URL mismatch after fetch raises existing SOURCE_CHANGED_DURING_REFRESH and rolls back staged inserts, updates, lastFetchedAt and nextFetchAt. Do not create a new HTTP error/catalog; scheduler consumes existing categorical failures. Manual finalization never checks scheduling opt-in or scheduler cooldown/global property.
5. **Do not test due again after ingest:** it has already moved nextFetchAt. An interval-only edit while scheduling stays enabled permits completion; existing generic ingest uses the fresh locked interval for nextFetchAt=fetchedAt+interval. This is intentional even if that edit moved due time during HTTP. Valid empty RSS may successfully advance times.
6. Source lock remains held through commit; lock/transaction timeout maps to existing REFRESH_FINALIZATION_UNAVAILABLE. Release admission on every outcome, including recheck no-op, interruption, policy/XML failure, source edit and finalizer failure. Scheduler may never mark failed/no-op calls as fetched.

Reuse actual [HN physical-transaction proof](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshIntegrationTest.java), [RSS proof](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssFeedRefreshIntegrationTest.java) and both providers' concurrency fixtures. Prove PID+XID at **actual ingestion after Spring advice**, source timestamp update and finalizer beforeCommit; assert same identity/no early commit for scheduled HN **and** RSS. Adapt the test-only REQUIRES_NEW control so the **same identity assertion** rejects an independently committed ingest; observing only caller/annotation/active status is insufficient. No production test callbacks or weakened original assertions.

Use real PostgreSQL/fake transport barriers for: post-selection manual success making scheduled source not due; flag/type/enabled edits before I/O; schedule OFF during HTTP and after item staging; RSS URL edit; interval-only edit; staged existing-item update plus insert rollback; retained NOWAIT source lock; finalizer timeout; manual/scheduled same-source and cross-provider global capacity. Edits must proceed while HTTP blocks, and no provider/parser observation may have an active DB transaction.

## Precise change map

Production prefix below: backend/src/main/java/com/vhvkhangg/personalprivatevault/. No new module/dependency edge/named interface is required.

| Permitted target | Required bounded outcome |
| --- | --- |
| feed/internal/application/ | Add FeedScheduledRefreshService for tick/cursor/cooldown and a small internal outcome/mode if needed. Add scheduled entrypoints/shared private flow in existing HackerNewsRefreshService/RssFeedRefreshService and narrow scheduled authority in their finalizers. Preserve manual signatures, constructor compatibility as needed, accepted provider behavior and generic ingestion. |
| feed/internal/infrastructure/scheduling/ | New FeedSchedulerLifecycle (or cohesive equivalent), conditional startup/readiness/private scheduler/future/stop ownership; package-info.java. No public/global scheduler bean or provider worker queue. A package-local constructor/timer-creation seam for tests is sufficient; no generic factory framework. |
| feed/internal/infrastructure/persistence/FeedSourceRepository.java | Two scheduler-only supported-type stable-ID bounded query branches. Existing public findDueSources and its ordering remain unchanged; no entity/schema/migration/index. |
| backend/src/main/resources/application.yml | Only ppv.feed.scheduler.enabled env-backed default false, concise operational comment/link if useful. No Spring global scheduling, RAG/security/runtime/provider setting change. |
| backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/ and relevant internal subpackages | Five new selectors below; targeted existing HN/RSS/Feed tests needed for changed entrypoints/finalizers. Use PostgreSQL/Testcontainers and local fake transport/timer/clock. No real provider or production test bypass. |
| Current implementation-adjacent docs in next section | New operator guide, one navigation link, additive actual Feed package inventory, handback/status only. |

Read-only/reused targets: FeedItemService.ingestFetch, FeedSourceService CRUD, shared HackerNewsRefreshAdmission, infrastructure/hackernews and infrastructure/rss, controller/DTO/advice/facade contracts, root HTTP/security/bootstrap and all RAG code. If an in-scope defect demonstrably requires a change outside the map, stop with exact minimum remedy rather than quietly refactoring frozen behavior. New packages require package-info.java; inspect actual changed subtrees/visibility and preserve ADR-0016 root envelope/module-local adapters and public Vault/parent Knowledge boundaries.

## Verification, failures and handback evidence

New stable selectors:

- FeedScheduledRefreshTest — tick/cooldown/caps/errors/stop and exact counterexamples with fixed clocks.
- FeedSchedulerLifecycleTest — readiness/registration/stop idempotency, creation-failure cleanup and actual private timer termination using short controlled waits.
- FeedScheduledRefreshIntegrationTest — actual PostgreSQL queries/mutable due-time traces/cooldown/failure timestamps.
- FeedScheduledRefreshConcurrencyIntegrationTest — both scheduled provider paths, shared admission/source edits/transaction identity and meaningful negative controls.
- FeedSchedulerLifecycleIntegrationTest — real Spring default/OFF/ON/readiness/restart/shutdown, fake providers and recording private timer, explicit no-unrelated-periodic-task proof.

For Spring tests assert one exact Instant+Duration registration in ON and zero instance/registration/callback in default/OFF; run the captured callback with safe fake providers. Separately exercise the real private ThreadPoolTaskScheduler lifecycle under a short test budget to establish cancellation/owned-thread termination; a fake destroy-called flag alone cannot prove no leak. Assert production 60s/one-thread settings without waiting minutes. Do not mask the PH-02 defect by excluding the existing RAG bean/annotation registration evidence: assert no newly introduced scheduling processor, no registered RAG/unrelated recurring task and no periodic invocation delta. Do not invoke RAG methods/workers to manufacture that evidence. Existing singleton startup is distinct from new periodic work.

Other must-fail cases: mutable due-time cursor or page-end advancement; 20 cooling rows; busy counted only on success; scheduler double admission; scheduled flag OFF nevertheless commits; illegal independent ingest commit hidden by caller-only sampling; failures advance success times; manual rejected by schedule/global/cooldown; retry map >512 or payload retention; stop allows next candidate; startup/restart floods or introduces global scheduling. Use marker-bearing fake failures to prove URLs/XML/titles/credentials/cause stacks absent from new logs.

Final verification sequence from repository root, PowerShell, **six focused groups**, not a full application suite:

```powershell
$s2bPython = (Get-Command python -ErrorAction Stop).Source
$s2bRunDir = Join-Path 'logs/daily-trends-radar' ('dtr26-s2b-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $s2bRunDir -ErrorAction Stop | Out-Null
$s2bChecks = @(
    @{ Name = 'scheduler-unit'; Tests = 'FeedScheduledRefreshTest,FeedSchedulerLifecycleTest' }
    @{ Name = 'scheduler-postgres'; Tests = 'FeedScheduledRefreshIntegrationTest,FeedScheduledRefreshConcurrencyIntegrationTest' }
    @{ Name = 'scheduler-spring-lifecycle'; Tests = 'FeedSchedulerLifecycleIntegrationTest' }
    @{ Name = 'manual-scheduled-refresh'; Tests = 'HackerNewsRefreshIntegrationTest,HackerNewsRefreshConcurrencyIntegrationTest,RssFeedRefreshIntegrationTest,RssFeedRefreshConcurrencyIntegrationTest,HackerNewsRefreshAdmissionTest' }
    @{ Name = 'feed-inbox-read-compatibility'; Tests = 'FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest,FeedRadarIntegrationTest,CombinedFeedRadarIntegrationTest,SavedResourceInboxIntegrationTest' }
    @{ Name = 'http-architecture'; Tests = 'FeedWebIntegrationTest,DailyTrendsWebIntegrationTest,RssDailyTrendsWebIntegrationTest,SavedResourceInboxWebIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApiResponseEnvelopeAndErrorIntegrationTest,ApplicationArchitectureTests' }
)
foreach ($s2bCheck in $s2bChecks) {
    $s2bReceipt = Join-Path $s2bRunDir ($s2bCheck.Name + '.json')
    $s2bTestArgument = '-Dtest=' + $s2bCheck.Tests
    & $s2bPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt $s2bReceipt -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' $s2bTestArgument test
    if ($LASTEXITCODE -ne 0) { throw ('DTR26-S2B verification failed: ' + $s2bCheck.Name) }
}
```

Split a group into its exact classes if necessary for the cap, preserving the evidence set/unique receipts. No silently omitted/missing-test success, count quota or repeated identical failed run. Iterate focused checks, retain final source-applicable evidence, repeat only for new fixes/drift. Verify exact **235** generated operations, IDs/parameters/envelopes/errors/security/schemas unchanged, not count alone.

Untouched HN/RSS transport/parser/privacy/provenance evidence may be reused from accepted S1/S2A reviews by source/test/receipt hashes and applicability. S2A's 178 fresh independent plus 361 reused and separate 254 implementer tests are historical counts, not new S2B cases. New scheduled eligibility/authority/lifecycle/query behavior always needs fresh proof. Record fresh implementer, fresh independent reviewer and reused evidence separately.

Use [bounded-command-execution](../../../../.agents/skills/bounded-command-execution/SKILL.md)/scripts/run-bounded-command.py, normal ≤300s per focused command, new ignored receipts/logs, bounded diagnostics/owned cleanup. B1/B2 are independently accepted/Owner-published; separate full-suite ceiling 2,100s does not authorize extended/full-suite execution here. No full suite/clean verify, real provider smoke, QA/model/RAG/archived-S19-2 override, dependency/runtime/image download, hook/trust/startup repair/bypass or unsafe network call. Missing local prerequisites require factual stop; native DNS and supported root/trust/client enforcement limits stay explicit. Preserve all historic logs/receipts without deletion/overwrite/truncation/relocation.

## Documentation impact and owner

| Affected path | Authorized editor / observable sync evidence |
| --- | --- |
| This contract + docs/implementation/handoffs/ACTIVE.md | Codex creates authority. Antigravity records results/counter/dated handback+self-review, marks IMPLEMENTED_AWAITING_CODEX_REVIEW. Separate Codex records review/remediation/final status; no Owner-publication claim. |
| docs/architecture/feed-scheduled-refresh-operations.md — new | Antigravity with implementation: actual default-OFF property, two opt-ins, private timer/cadence/query/cooldown/stop behavior, success timestamps, exact one-JVM/no-overlap procedure, activation held until deployment check, manual/read compatibility and limits. Compare code/config/tests at self/independent review. |
| README.md | Antigravity may add one navigation link to the operator guide in its Architecture section. No dated banner/workflow/current-status cleanup. |
| docs/repository/repository-package-tree.md | Antigravity: additive actual Feed internal scheduling package/capability inventory only; no frozen module/version/policy/diagram rewrite. Check recursive tree and descriptors. |
| This directory's implementation-handback-2026-11-05.md, uniquely dated if later | Antigravity: S2B-01–12 mapping/receipts, self-review, attempt traces/DB identity/cleanup proof, source/receipt hashes, docs/activation ledger/limitations/counter. Correction evidence remains additive/dated. |
| Preparation README/status/Owner policy/scoped AGENTS/previous API notes; publication closeout | ChatGPT handles routine governance sync when separately requested. Preparation/historical S2A records stay dated; ACTIVE/this contract govern execution. After independent acceptance Owner publishes; ChatGPT records actual SHA/closeout/reset. No Drive/frontend/RAG scope follows. |

Operator guide must instruct: leave false until the Owner/operator verifies exactly one active backend JVM targeting the Feed database; stop and complete the old process before starting another during rollout; prevent overlapping active replicas. Current dev Compose files contain infrastructure services, not proof of backend process count. If topology unknown/multiple, keep OFF, record NOT_VERIFIED or failed conformance and stop activation; do not invent a lease/migration. Document per-source flags/positive interval, startup-only opt-in, 60s initial delay/best-effort cadence, failure timestamps/cooldown reset, source-OFF commit protection and property-OFF restart/shutdown procedure. No live deployment, process termination or actual enable action is authorized here.

**Docs Sync Check — creation:** UPDATED — this contract/ACTIVE only. ALREADY_CONSISTENT — published READY_FOR_HANDOFF at e296d55, unchanged source since 47d4d6d, no live handoff, accepted S2A/manual API/235 manifest/frozen ownership. DEFERRED_OUT_OF_SCOPE — stale root/backend/docs/Feed/README/Owner-policy/handoff-README/agent notices and old prospective S2B note; PH-L01/other LOW/INFO and AUTH-CLOCK remain in their registers. ChatGPT/future approved cleanup owns unrelated repairs. OWNER_DECISION_RECORDED — topology A, no repeated confirmation. NOT_VERIFIED — S2B runtime cases, actual topology/overlap/activation and future delivery publication; none inferred green.

**Creation validation (2026-11-05):** All 43 relative file links across ACTIVE/this contract resolve; strict UTF-8, whitespace and final-newline checks pass, including the new untracked contract. S2B-01–12 each appear once in the acceptance table. Six focused groups/23 selectors are checked against the existing test tree plus five explicitly prospective classes; the PowerShell gate parses without execution. Default-OFF YAML matches the contract. Only ACTIVE and this contract differ from entry; production/test/config/tooling/frozen artifacts are unchanged. Pinned Spring API metadata was inspected; no application test, timer instance, provider fetch, model or worker was executed. These checks establish document consistency, not runtime acceptance or deployment conformance.

## Engineering guidance, non-goals and completion

Relevant skills: java-spring-coding-standards, pragmatic-solid-design, reuse-and-consistency, modular-monolith-architecture, jpa-postgresql-persistence, backend-testing, feed-import-workflow-modeling, rest-api-http-contracts for compatibility, bounded-command-execution; mandatory antigravity-self-review. Existing backend-implementer suffices, no new/delegated agent. Shared codex-review-coverage is proportional: intent/data/fairness S2B-03–05/08, auth/egress/isolation/resources S2B-01/02/06/09/10, transactions/persistence S2B-07/08, API/ownership/tree/tooling/docs S2B-03/11/12. Every material deferred-runtime risk has exact evidence above; static preparation traces are not runtime tests.

**MUST NOT:** enable global annotation scheduling/RAG, modify RAG code or activate QA/models/workers; introduce provider/runtime/dependency/schema/index/DBML/migration/module/API/root HTTP/auth changes; distributed lease/queue/provider registry/extra worker pool/cron or history UI/live hot-toggle; scrape article/enclosure/content or discover providers; frontend/Drive/OAuth/Ideaverse; unrelated refactors/LOW cleanup or historical reviews/handbacks/contracts/receipt rewrites. Root personal-private-vault.zip stays untouched/unopened/uncertified. No universal DNS/process containment, deployment-security certification, exact-time SLA or cross-JVM/exactly-once claim.

**Blockers:** no material S2B Owner choice is open at creation. Stop with exact evidence for source drift invalidating preparation, unavailable prerequisites, inability to isolate/private-own the timer or prove fairness/same-transaction authority, required frozen/schema/dependency/API change, or a demand for multiple active JVMs/strict durable semantics. Do not weaken accepted guards or silently expand scope. Actual deployment verification gates activation, not this planning task.

Done requires mandatory implementation cases/negatives proved, focused final gate passing, consistent scoped docs, explicit remaining operational activation state and Antigravity self-review. Handback includes commands/exits/counts/skips/durations/unique receipts; case mapping; actual PG progress/caps/physical transaction/lock/rollback/negative-control evidence; Spring registration/no-RAG/termination observations; hash-qualified evidence reuse; changed tree, limits/findings/Docs Sync/counter. No unsupported warning-free/IDE-clean/runtime/activation claim.

Separate Codex slice review follows. Mandatory unverified implementation acceptance or open CRITICAL/HIGH/MEDIUM blocks READY_FOR_OWNER_COMMIT; LOW/INFO follow backlog policy. Initial 0/3; at most three Antigravity correction+independent re-review rounds, stop on acceptance. After three independently blocked rounds stop for explicit Owner takeover decision and subsequent independent review; no automatic takeover. Future comprehensive audit still adjudicates every actionable severity. No extra phase-final/milestone/repository-wide audit is invented for S2B.

## Implementation result

Initial submission and [initial independent review](reviews/2026-11-05-dtr26-s2b-independent-final-review.md) each passed **236 tests** with four MEDIUM corrections requested. Remediation 1 closed FR-01–03; remediation 2 supplied accepted lifecycle/privacy/history portions but retained a late-authority false positive. Remediation 3 repairs that case, requiring a committed scheduling edit before source locking, actual SOURCE_CHANGED_DURING_REFRESH, both item/timestamp rollbacks and a rejected independent guard-bypass control. [Independent re-review 3](reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md) accepts all FR-01–04 with **14 fresh** PostgreSQL tests plus **236 source-applicable reused** tests. Separate current implementer six-group receipts total **250**, zero failures/errors/skips, in `logs/daily-trends-radar/dtr26-s2b-rem3-final/`. Default OFF and actual deployment activation hold remain intact; implementation publication is pending.

## Codex remediation

[Independent re-review 3](reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md): **FR-01–04 MEDIUM CLOSED**. The causal pre-source-lock fixture completes without transaction expiry, observes SOURCE_CHANGED_DURING_REFRESH and correct rollback; the guard-bypassed control produces REFRESHED and fails the submitted assertion. Previous quota/cooldown/lifecycle/privacy/fairness/transaction/history evidence remains accepted. No implementation correction is outstanding.

The [round-3 handback](implementation-handback-remediation-3-2026-11-05.md) is additive; original/round-1/round-2 handbacks independently match their recorded hashes. All earlier reviews/receipts remain preserved. **Docs Sync Check — re-review 3:** acceptance review/current disposition/counter/guide header updated; accepted behavior, accurate uncommitted working-tree description, history/API/tree/default OFF consistent; actual deployment/activation and future Owner publication NOT_VERIFIED; older notices/backlog deferred. S2B **3/3 consumed; independently ACCEPTED / STOPPED; no further correction round; takeover NOT_REQUIRED / NOT_AUTHORIZED**.

## Final review

**READY FOR OWNER COMMIT / ACCEPTED** — [independent re-review 3](reviews/2026-11-05-dtr26-s2b-independent-re-review-3.md), 2026-11-05. FR-01–04 CLOSED; accepted remediation STOPPED at 3/3 without takeover. Owner alone publishes; subsequent verified publication closeout is pending. Actual deployment conformance/unattended activation remains separately held.

**Next step:** Owner | commit/push accepted delivery, then ChatGPT records verified publication closeout | DTR26-S2B, re-review 3 and this contract | READY_FOR_OWNER_COMMIT; retain default OFF until actual deployment conformance | 3/3 consumed; ACCEPTED / STOPPED; no takeover.
