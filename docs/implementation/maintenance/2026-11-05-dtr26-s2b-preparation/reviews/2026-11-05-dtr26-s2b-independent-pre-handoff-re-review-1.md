# Independent pre-handoff re-review 1 — DTR26-S2B

- **Date / reviewer / baseline:** 2026-11-05 / Codex / main `47d4d6d3db49c2fd37211147502e88453a536458`, unchanged from the [initial preparation review](2026-11-05-dtr26-s2b-independent-pre-handoff-review.md).
- **Mode / authority:** Owner-approved maintenance-feature preparation only, per [Owner scope](../owner-scope-and-decisions.md). Six revised Markdown files reviewed; initial report preserved.
- **Disposition:** **READY_FOR_HANDOFF** for one bounded S2B implementation slice. **DTR26-S2B-PH-01 and PH-02 CLOSED for preparation design.** No blocking preparation finding or unresolved material Owner choice remains.
- **Execution:** [ACTIVE](../../../handoffs/ACTIVE.md) stays **NO_ACTIVE_HANDOFF**; implementation NOT_STARTED, S2B **0/3**, takeover NOT_AUTHORIZED. This review creates no implementation handoff and certifies no scheduler runtime result.

## Finding closures

| Stable ID | Preparation resolution / remaining runtime gate |
| --- | --- |
| DTR26-S2B-PH-01 MEDIUM | **CLOSED:** scheduler-only stable source-ID ordering, exact start/after-ID query branches, last-*examined* advancement and stop after two provider-entry invocations replace the moving nextFetchAt cursor. Independent static traces agree. Actual PostgreSQL query/dispatch proof remains mandatory S2B-04/05. |
| DTR26-S2B-PH-02 MEDIUM | **CLOSED:** one private Feed-owned programmatic timer, no application-wide annotation scheduling/registrar and no RAG source edit. Exact pinned API is available. Actual OFF/ON/stop and no-unrelated-periodic-task proof remains mandatory S2B-01/02/09/10/11. |

**PH-01:** [Technical design `3](../scheduled-refresh-technical-design.md) now filters enabled/scheduled/due HN/RSS in the database **before** limiting, orders only by immutable source ID and uses separate cursor-null/after-ID JPQL-equivalent branches. Each page is capped at 20; scanning stops after the second provider-entry invocation, including busy rejection. The cursor points to the last actually examined ID, never the last prefetched row. An empty page resets the cursor and ends the tick.

Because successful ingestion does not change source ID, it cannot move that source indefinitely ahead of this cursor. Under the stated finite/stable eligibility assumptions, the cursor advances or wraps; nextFetchAt mutations cannot prevent wrap. Cooling candidates advance the cursor without consuming an invocation. This preserves the accepted public due query's null-first/time/id ordering and extra provider metadata types.

Independent static interpretation of the revised rules verifies:

| Scenario | Observed design result |
| --- | --- |
| Initial 30-source all-success counterexample, one-minute intervals, ID1 non-null due=0 and IDs2–30 null | **30/30 invoked by logical tick 15**; tick 16 empty/reset; tick 17 invokes 1/2 again. |
| 34 rows, unsupported IDs4/12/19/27, five failures IDs1/2/3/5/6 and busy IDs7/8 | **30/30 supported IDs invoked by tick 15**, zero unsupported invocations. First pairs 1/2, 3/5, 6/7, 8/9; tick 15 invokes 33/34. Sixty logical ticks retain the caps. |
| First 20 IDs cooling until logical t=300 | First tick examines 20 with zero invocations and cursor 20; next tick invokes 21/22 and stops at cursor **22**, not page-end 30. All 30 eventually receive an invocation in the checked trace. |
| All traces | Maximum **20 examined / two invocations** per tick. |

These are deterministic **design models**, not real database, timer, provider or S2B application tests. The revised [acceptance plan](../acceptance-and-review-plan.md) explicitly requires actual PostgreSQL attempt traces, mutable due-time counterexamples, cooldown, unsupported filtering and attempted-versus-examined counters.

**PH-02:** [Technical design `2](../scheduled-refresh-technical-design.md) now selects a privately owned, single-thread ThreadPoolTaskScheduler with one direct fixed-delay registration after application readiness. It prohibits adding EnableScheduling, Scheduled, SchedulingConfigurer or a global annotation registrar, and keeps the existing RAG component unchanged. Stop is marked before cancelling the retained future; no subsequent dispatch, unowned worker queue or extra recurring callback is permitted.

Programmatic registration and fixed-delay behavior are supported by Spring's [TaskScheduler API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html); ThreadPoolTaskScheduler runs callbacks on its own scheduler thread. [Spring class documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/concurrent/ThreadPoolTaskScheduler.html). Independent **class-metadata inspection of the actual pinned spring-context 7.0.9 JAR**, not merely current web documentation, confirms getClock(), scheduleWithFixedDelay(Runnable, Instant, Duration), pool-size and initialize/shutdown/destroy/lifecycle methods. No dependency change/download or timer instance was needed.

The later executable handoff must keep ownership concrete: explicitly initialize/dispose the private instance, retain/cancel exactly one future, and test readiness registration/stop idempotency. The revised OFF/ON tests distinguish existing RAG singleton initialization from **newly periodic** annotation scheduling; they require no new unrelated task registration/invocation. These are future verification obligations, not claims that a running Spring context was tested here.

## Owner decision and feature-by-feature necessity

The [Owner decision register](../owner-scope-and-decisions.md#owner-topology-decision-register--a-explicitly-confirmed-2026-11-05) now explicitly records **A: exactly one active backend JVM**, confirmed on 2026-11-05. The prior unanswered topology question is historical and is not reissued. Actual deployment conformance/rolling-restart overlap remains **NOT_VERIFIED**, with an explicit stop condition before unattended polling is enabled. No cross-process mutual exclusion or durable exactly-once claim is made.

| Owner-visible function / mechanism | Disposition | Simplest adequate implementation / effort and approval boundary |
| --- | --- | --- |
| Global/per-source scheduling opt-in | KEEP / REUSE | Existing enabled/scheduled flags plus proposed startup default-OFF property. Small config + medium actual OFF/ON wiring proof. |
| HN/RSS refresh and source success timestamps | REUSE | Existing provider orchestration, secure transport/parser/normalization, generic ingest and current schedule interval. No second ingestion pipeline. |
| Fair bounded due service | SIMPLIFY / KEEP corrected design | Scheduler-only ID cursor, one <=20-row page and <=2 serial entry invocations. Medium query/tick implementation and PG attempt traces. |
| Manual/scheduled exclusion and final authority | REUSE / KEEP narrow change | Acquire current shared admission once; scheduled eligibility reread after acquisition, before I/O; explicit scheduled mode through existing finalizer; lock/physical transaction retained. Medium concurrency/rollback proof. |
| Failure control | KEEP simple proposed defaults | Five-minute scheduler-local cooldown, <=512 entries, deterministic eviction/restart reset; no false DB success or manual throttle. Small bounded-map/clock proof. |
| Timer lifecycle | KEEP / SIMPLIFY | One private programmatic timer, no annotation activation or provider worker queue. Medium readiness/stop/no-unrelated-task proof. |
| Current source CRUD, due GET, manual POSTs, both Radar GETs and Inbox | REUSE | Same wire contracts and 235-operation inventory; no scheduler endpoint/DTO/envelope redesign. Focused affected compatibility checks. |
| Generic jobs/providers, extra executors, durable queue, cron UI, dynamic toggle/history API | REMOVE / DEFER | No current Owner need. Avoid framework, schema, extra worker and validation burden. |
| Multiple active JVMs, strict SLA or durable retry | DEFER / new Owner approval if requested | Contrary to confirmed topology A/current best-effort scope. No distributed lease/migration is introduced. |
| New providers/content, Drive/frontend, RAG/OCR/GPU/model work, benchmarks | DEFER / N/A | Outside S2B preparation authority; no new agent, dependency or evaluation project. |

The numerical limits remain reviewed **proposed implementation defaults**, not separately invented Owner confirmations: 60s initial/fixed delay, 20 examined, two serial invocations, five-minute cooldown and 512 entries. They fit the best-effort/single-JVM goal and preserve positive one-minute intervals. Owner publication of reviewed preparation and a later explicit handoff invocation are still required.

## Applicable harness and future verification

The complete initial source/governance audit remains applicable: root/backend/docs/Feed/test AGENTS, Owner policy/Docs Sync contract, relevant Java/Spring/Feed/persistence/reuse/SOLID/testing/architecture/documentation/bounded-command skills, backend-implementer, existing .agents/.codex rules/hooks, bootstrap/YAML/POM, module matrix, ADR-0016, schema, Feed services/finalizers/admission and S2A public APIs. Only the six preparation files changed. No new skill, custom agent, hook, rule, schema or dependency is justified. Existing backend-implementer + mandatory self-review and bounded runner suffice; Python/RAG implementation agents are N/A.

Nineteen selected canonical source/harness files were checked against unchanged HEAD. Raw working-tree comparisons encountered existing CRLF/LF differences in RagRecoveryService, module-dependency-matrix and repository-package-tree; normalized contents match and no such file was rewritten. Actual recursive Feed inventory remains **114 production Java files / 26 packages / 24 test Java files**, descriptors present and no .gitkeep. Root ApiResponse and module-local adapters/exception/command/view grouping remain correct under ADR-0016. No business module should depend on RAG to isolate its timer.

**One separately committable S2B slice** remains appropriate: Feed-owned lifecycle/tick, internal due query, narrow scheduled invocation/finalizer guard, local cooldown and relevant config/operations/tests. Exact executable targets/selectors belong in the later Codex handoff, not a new handoff authored by this review.

All **S2B-01–12 remain NOT_RUN**. The bounded future gate is retained:

1. Deterministic scheduler logic/cooldown/selection tests, including both corrected traces, 20 cooled candidates, last-examined cursor and busy invocation accounting.
2. Real PostgreSQL due-query/tick progress and authoritative source edits: schedule OFF, type/RSS URL changes, fresh interval, actual ingest/source/beforeCommit PID/XID, retained lock and meaningful independent-commit negative control.
3. One controlled real Spring OFF/ON/readiness/shutdown test with fake provider/timer recording and explicit RAG/unrelated-periodic-task negatives; direct tick tests cover detailed clock behavior.
4. Focused changed manual HN/RSS concurrency/Feed/Inbox/web/OpenAPI/architecture compatibility. Reuse unchanged transport/parser/privacy evidence by hashes and scope, not arbitrary test counts.

Use the existing runner, normal **300s per focused command**, unique ignored receipts, offline/no-download prerequisites and owned cleanup. No full suite, real provider smoke, benchmark or extra workflow/milestone project is needed. Antigravity self-review, one independent slice final review, Owner publication and closeout follow the eventual active contract. Three implementation remediation/re-review rounds start at **0/3**; any future takeover requires explicit authorization and separate review.

## Evidence, coverage and Docs Sync Check

New ignored evidence: `logs/daily-trends-radar/codex-s2b-pre-handoff-rereview1-20261105-d573d3801238450fb291284fdda064b0/`. Entry hashes/state, six-file delta, static-inspection, source-harness-applicability-normalized, stable-id-design-model source/60s receipt/stdout and pinned-spring-api 60s receipt/stdout are retained. Both diagnostics exit 0; neither is counted as S2B runtime tests. Initial Graphify navigation/source audit and historical review remain unchanged/source-applicable; no graph rebuild.

| Shared dimensions | Current preparation disposition |
| --- | --- |
| 1 intent; 2 traceability | PASS — all twelve prospective cases map to the bounded feature; PH-01/02 corrected and topology A recorded. |
| 3 architecture; 4 ownership; 5 actual tree | PASS — Feed-local additive proposal, frozen schema/module edges and actual package inventory unchanged. |
| 6 shared contracts; 11 API | PASS design — root envelope/local adapters; public due ordering/manual/Radar/Inbox preserved; no extra operation. Runtime compatibility NOT_RUN. |
| 7 SOLID; 8 principles; 9 patterns | PASS scoped — stable-ID rotation and one owned timer replace unnecessary complexity, accepted provider/finalizer/admission reuse. |
| 10 domain; 19 data quality | PASS design/model — progress semantics, success-only timestamps, exact URL/identity and latest-interval rules preserved. Future PG proof mandatory. ML/retrieval N/A. |
| 12 validation; 20 formats | PASS requirements — existing interval/provider/XML/URI bounds; no format expansion. Runtime NOT_RUN. |
| 13 auth/privacy; 14 egress | PASS requirements — opt-in, secure existing adapters, safe logging, explicit no-unrelated-periodic work. Runtime NOT_VERIFIED at preparation. |
| 15 transactions; 17 persistence | PASS feasibility/requirements — explicit scheduled authority and same physical transaction/lock; database implementation/tests NOT_RUN. No migration. |
| 16 lifecycle; 18 resources | PASS design — one owned timer, finite page/map/invocations, corrected cursor; future lifecycle/PG measurements required. |
| 21 security tests; 22 test quality | PASS preparation — required concrete negatives cover both prior defects, original transaction risks and manual compatibility; no static-model substitution for runtime evidence. |
| 23 benchmarks; 24 evaluation science | N/A — no requested benchmark/ML capability. |
| 25 dependencies; 26 tooling/static diagnostics | PASS pinned API/no-change feasibility; build/IDE/compiler inspection NOT_RUN/NOT_VERIFIED, no warning-free claim. |
| 27 observability; 28 docs/governance | PASS requirements/status — evidence labels and current readiness synchronized; immutable initial failures retained. |
| 29 hygiene/delivery | PASS static — input hashes, normalized source reuse, scoped links and final boundary checks; no staging/publishing. |

- **UPDATED:** this new re-review and preparation status/next-action annotations only; substantive submitted algorithm/acceptance/Owner decision unchanged.
- **ALREADY_CONSISTENT:** NO_ACTIVE_HANDOFF, published S2A/closeout baseline, source/module/schema/API/tree, single-JVM restriction and all prospective test labels.
- **DEFERRED_OUT_OF_SCOPE:** unchanged stale dated AGENTS/agent/rule notices, old S2A prospective note and PH-L01/other LOW backlog; no unrelated cleanup or finding closure.
- **OWNER_DECISION_RECORDED:** topology A; no repeat confirmation requested. Actual deployment topology is an operational precondition before activation.
- **NOT_VERIFIED:** all actual S2B scheduler/application/PG behavior, operational deployment conformance and future publication. These remain explicit implementation/operational gates; this is preparation readiness only.

No production/test/config/tooling/handoff edit, Maven/DB/application test, actual scheduler/RAG/worker/model activation, live Feed/provider request, dependency/runtime download, hook/trust change or Git staging/publishing occurred. Final UTF-8/link/whitespace/input-history/index checks cover the review/status outputs. The prior CHANGES_REQUESTED report stays immutable and is superseded only for current preparation disposition.

**One suggested commit message:** `docs(feed): prepare DTR26-S2B scheduled refresh`

**Next step:** Owner | stage/commit/push the reviewed preparation, then separately invoke Codex `$codex-create-handoff` | DTR26-S2B six preparation files plus initial review and this re-review | READY_FOR_HANDOFF; published preparation and explicit invocation before implementation; ACTIVE remains NO_ACTIVE_HANDOFF | S2B implementation NOT_STARTED, 0/3; takeover NOT_AUTHORIZED.
