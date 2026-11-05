# Independent pre-handoff review — DTR26-S2B

- **Date / reviewer / source:** 2026-11-05 / Codex / local main `47d4d6d3db49c2fd37211147502e88453a536458`; local origin/main agrees. Remote publication is recorded by the S2A closeout; no fresh remote-server verification is claimed here.
- **Mode / authority:** Owner-approved maintenance **preparation only**, per [Owner scope](../owner-scope-and-decisions.md). Six new Markdown files reviewed; no numbered-phase/milestone prerequisite is invented.
- **Final status:** **CHANGES_REQUESTED**. Two MEDIUM preparation findings remain OPEN. Process-topology confirmation was requested separately; it is not inferred from single-user identity.
- **Execution boundary:** [ACTIVE](../../../handoffs/ACTIVE.md) remains **NO_ACTIVE_HANDOFF**. No implementation handoff created. S2B implementation NOT_STARTED, **0/3** remediation, takeover NOT_AUTHORIZED; S1/S2A counters remain frozen.

## Required user-visible capabilities and simplest mechanisms

| Requirement | Authority / shortest reusable mechanism | Implementation / verification effort |
| --- | --- | --- |
| Automatically refresh opted-in due HN/RSS sources | Preparation approved; proposed global startup default OFF plus existing source flags/positive intervals. Reuse current provider flows and source metadata. | Medium: one Feed-owned timer/tick and internal due query. Future actual enabled/off context wiring and provider-call counts. |
| Keep serving every runnable source under bounded work | Required by README and S2B-05. Fair internal rotation, <=20 examined and <=2 serial attempts; public due query unchanged. | Medium: query/cursor semantics and DB trace. Current moving-key proposal fails PH-01 below. |
| Respect source edits and manual contention | Reuse one shared admission; scheduled-only authoritative flag check in existing finalizers, same physical transaction/lock through commit. | Medium: narrow invocation mode, fake-HTTP barriers, real PostgreSQL PID/XID/rollback and independent-commit negative control. |
| Avoid repeated failed scheduled attempts | Proposed 5-minute, <=512-entry in-memory cooldown; manual refresh bypass; failures preserve DB success timestamps. | Small implementation, focused clock/eviction/restart assertions. Not durable retry or success history. |
| Preserve manual APIs, Radar and Inbox | Reuse exact accepted routes/DTOs/errors/identity and save path; no new HTTP operations or migration. | Small adapter impact; reuse unchanged evidence and rerun affected manual/web/architecture cases. |
| Stop background work cleanly | One owner, fixed delay after completion, bounded provider deadlines, no extra async queue; no unrelated scheduling activation. | Medium lifecycle proof. PH-02 must choose an isolated registration mechanism first. |

Defaults **60s initial/fixed delay, 20 examined, two serial attempts, five-minute cooldown/512 entries** remain reviewer-assessable proposals, not separately confirmed Owner facts. Best-effort timing and restart-reset state are explicit. No exact-minute SLA is required.

## Blocking preparation findings

### DTR26-S2B-PH-01 — MEDIUM / OPEN: moving-key scan can permanently starve due sources

[Technical design](../scheduled-refresh-technical-design.md), **lines 27–31**, advances a nullable nextFetchAt/id cursor across up to 20 examined rows while dispatching only two. It wraps only after an empty page and claims the skipped rows are not permanently lost. Successful refreshes themselves move nextFetchAt, so a finite source population can keep producing rows beyond the cursor forever.

The published [generic ingestion](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java), **lines 144–159**, records nextFetchAt=fetchedAt+the locked current interval. The following static model uses exactly that transition and the proposed null-first/time/id keyset; all 30 sources are enabled/scheduled HN/RSS with legal one-minute intervals. Source 1 initially has nextFetchAt=0; sources 2–30 have null. No external edits, arrivals, failures or busy sources are needed.

| Tick / cutoff seconds | Page examined | Provider attempts | Saved cursor |
| --- | --- | --- | --- |
| 1 / 0 | null-due IDs 2–21 | 2, 3 | null, ID 21 |
| 2 / 60 | null IDs 22–30, then non-null IDs 1, 2, 3 | 22, 23 | next=60, ID 3 |
| 3 / 120 | 22, 23, now next=120 | 22, 23 | next=120, ID 23 |
| 4 / 180 and onward | 22, 23 move ahead again | 22, 23 repeatedly | moves forward; never empty/wraps |

At each subsequent one-minute tick the same successful pair is due again beyond the saved cursor. This recurrence continues without any unbounded external mutation. A 30-tick diagnostic confirms **26 sources never receive an attempt**; 2/3 run once and 22/23 run 29 times each. Merely examining a source does not refresh it. Cooldown cannot fix this all-success example.

Evidence: `selection-counterexample.py` and its normal-profile **60s** receipt/stdout under `logs/daily-trends-radar/codex-s2b-pre-handoff-20261105-e2e3ab0bf4e74ca795b7d0feb07a5947/`. Exit 0 means the predicted design failure was reproduced; this is **not** S2B runtime/DB acceptance or production implementation.

**Smallest correction:** choose a bounded rotation whose progress is not moved by its own successful refreshes and whose attempt budget cannot permanently skip unattempted candidates. A scheduler-only **stable source-ID cursor**, stopping at the last actually examined candidate when two attempts are consumed, is one simpler option; an explicitly bounded sweep epoch is another. Do not change existing public due ordering or introduce a queue/table. Write the selected internal query/comparator/wrap rules and an exact repeat-tick trace in the preparation.

**Future proof:** real PostgreSQL selection/tick trace showing actual provider attempts, not just scanned IDs, for (a) this mixed null/non-null one-minute all-success case and (b) the already-required >=30 sources with five failures, two busy and unsupported types. Keep the same finite caps and best-effort scope. This corrects an existing requirement, not a new fairness feature.

### DTR26-S2B-PH-02 — MEDIUM / OPEN: scheduler isolation mechanism is unresolved against an existing RAG timer

[Technical design](../scheduled-refresh-technical-design.md), **line 19**, and the [decision ledger](../decision-and-risk-ledger.md), R1/R8, require Feed-only conditional scheduling but leave the activation mechanism/annotation wiring unresolved. The real source has an unconditional component [RagRecoveryService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java), **lines 14–15 and 181–182**, with a 30-second `@Scheduled` reconcile method. Repository-wide annotation inspection finds no current `@EnableScheduling`.

Putting `@EnableScheduling` on a conditionally enabled Feed configuration **does not confine annotation discovery to Feed** in that same application context. It would also register the existing RAG method. Spring documents container-wide annotation detection in the [EnableScheduling API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableScheduling.html). The reconciliation method's security guards do not make its scheduling Feed-local. No RAG execution, QA inference or actual activation was performed or inferred by this review.

**Smallest correction:** explicitly select a Feed-owned programmatic task registration/lifecycle that does not enable scanning unrelated annotated beans—for example one conditional, single-thread TaskScheduler registering the Feed callback directly, without application-context `@EnableScheduling`. Spring already supplies the [TaskScheduler abstraction](https://docs.spring.io/spring-framework/reference/integration/scheduling.html); no new dependency/framework or RAG edit is needed. Keep the no-extra-worker-queue rule; a timer's one owned scheduling thread is distinct from an unnecessary provider worker pool.

**Future proof:** with Feed global ON, only the Feed callback is registered/invoked and RAG/unrelated scheduled callbacks are not newly registered/invoked; with OFF, no Feed timer/callback/provider work. Assert the real registration boundary and close/dispose ownership using fakes. Add this concrete negative to S2B-01/02/10/11 and document the selected activation mechanism before handoff.

## Feature necessity and Owner decisions

| Feature / mechanism | Disposition | Reason / burden / approval boundary |
| --- | --- | --- |
| Feed ownership, source fields, source CRUD and metadata-only due GET | REUSE | Frozen schema/public contract already suffice; no migration/new module/API. |
| HN/RSS clients, normalization and generic ingest | REUSE | Accepted provider/privacy/identity behavior; no second fetch or ingest implementation. |
| Global default-OFF timer and two-layer source opt-in | KEEP / SIMPLIFY | One conditional Feed-owned callback; resolve PH-02 rather than application-wide scheduling activation. Medium lifecycle/wiring work. |
| Mutable nextFetchAt keyset and scan-20/attempt-two claim | SIMPLIFY / REJECT current algorithm | PH-01 concrete starvation. Stable internal rotation is sufficient; no distributed machinery. Medium query/trace proof. |
| Scheduled-mode finalizer guard | KEEP | Narrow explicit owner-local invocation mode through existing orchestrator/finalizer, no thread-local or duplicate pipeline. Medium PostgreSQL race proof. |
| Failure cooldown and bounded bookkeeping | KEEP / SIMPLIFY | Fixed five minutes/512 entries are proportionate proposed defaults, not a retry framework. Small clock/memory tests. |
| Second async pool, generic provider registry, durable jobs, cron UI, extra REST/history API | REMOVE / DEFER | Not needed for the requested single-process feature; avoids new ownership and evidence burden. |
| Multi-JVM lease/exactly-once guarantee | DEFER / OWNER DECISION if required | Existing admission is process-local. Actual deployment process count is NOT_VERIFIED; a single-account app alone does not prove one JVM. |
| New providers, content/enclosures, Drive, frontend, RAG/QA/OCR/GPU or benchmarks | DEFER / N/A | Outside S2B authorization; no new agents, hooks, dependencies, model or evaluation gates. |

**Finite Owner clarification:** the Owner was asked whether S2B is deliberately limited to **one active backend JVM** or needs multiple active JVMs. That question remains pending when this report is written. Record the answer without inventing deployment observations. Multi-instance support would require separate scope/coordination decisions; do not silently add them. The two technical preparation corrections above can proceed independently of the reply.

These corrections stay within the approved preparation scope and change only unimplemented proposals. They add no capability. Implementation and unattended polling still need later approved handoff authority.

## Existing harness, architecture and finite verification plan

Reviewed all six new files: README, owner-scope-and-decisions, scheduled-refresh-technical-design, operations-and-security, acceptance-and-review-plan and decision-and-risk-ledger. Source/config inspection covers FeedSourceRepository/Service, FeedItemService, both provider orchestrators/finalizers, shared admission, source/package descriptors, application bootstrap/YAML, schema's feed_sources constraints, current module dependency matrix, ADR-0016 and actual Feed tree. Relevant test structure/manual/concurrency/OpenAPI evidence is inherited from published S2A; no S2B result is manufactured from its counts.

The actual Feed tree is **114 production Java files / 26 packages / 24 test Java files**; descriptors present, no .gitkeep. Feed dependencies remain Vault/parent Knowledge; do not import RAG internals to prevent PH-02. Root ApiResponse/module-local DTO/advice remains correct under ADR-0016. The proposed feature needs no new root/shared module or public provider framework.

Applicable governance/harness checked: supplied root AGENTS, backend/docs/Feed/test scoped AGENTS; current Owner policy/Docs Sync contract; backend-implementer; Feed modeling, Java/Spring, persistence, reuse/SOLID, architecture, testing, documentation and bounded-command skills; .agents handoff/Feed rules, hooks.json; .codex config/native-hook boundary. These remain unchanged across published S2A/preparation baselines. Existing backend-implementer + mandatory self-review is sufficient; Python/RAG agents and new hooks/rules/skills are unnecessary. Graphify's stale local graph was queried read-only via installed NetworkX for navigation and then verified in current source; no rebuild or extraction service.

Backend README/development Compose describe local dependencies, not certified production replica count. Preserve the single-primary-JVM operating assumption visibly; no actual deployment certification. No dependency upgrade/lockfile or hardware benchmark is justified by this bounded timer.

**One proposed vertical slice after corrected preparation:** Feed-only activation/tick and internal due query; reuse provider invocation/admission; add scheduled authority to existing finalizers; local cooldown/lifecycle; prospective scheduler tests and necessary config/operations documentation. No code or executable handoff is created here.

Future focused verification, **all NOT_RUN now**, uses existing normal watchdog **300s per command** and fresh receipts:

1. Deterministic selection/cooldown unit tests: caps, both starvation counterexamples, cursor reset, restart/eviction and manual bypass. Exact class selectors belong in the later executable handoff.
2. Real PostgreSQL scheduler query/dispatch and finalizer barriers: due/flags, latest interval, type/RSS URL edits, physical ingest/source/beforeCommit PID/XID and meaningful independent-commit negative control. Never H2.
3. One bounded real Spring registration/off/on/shutdown test with fake provider and explicit no-unrelated-task assertions; direct tick tests handle detailed timing without repeated real 60s waits.
4. Changed HN/RSS manual refresh/concurrency plus Feed/Inbox/web/OpenAPI/architecture compatibility selectors. Reuse unchanged provider/parser/security evidence with hashes; do not rerun a whole suite or add 29 test projects.

Check due eligibility freshly after shared admission is acquired and before scheduled I/O, so a manual completion between the outer scan and admission cannot cause an avoidable stale-due dispatch. Preserve the documented interval-only mid-fetch behavior, rollback on schedule OFF and no DB lock over HTTP. This is a handoff clarification within existing S2B-06/07, not a new pipeline.

## Shared coverage and Docs Sync Check

| Dimensions | Preparation disposition |
| --- | --- |
| 1 intent; 2 traceability | FINDING — S2B-05 progress claim fails PH-01; all twelve cases remain prospective. |
| 3 architecture; 4 ownership; 5 tree | PASS scoped design/tree — existing ownership/schema support reuse; no new dependency edge or package drift. |
| 6 shared contracts; 11 API | PASS design — root envelope/Feed adapters, no new routes; preserve public due contract and exact 235 operations. Runtime compatibility NOT_RUN. |
| 7 SOLID; 8 principles; 9 patterns | PASS reuse direction / FINDING current selection — avoid provider/job frameworks; simplify PH-01/02 mechanisms. |
| 10 domain; 19 data quality | FINDING PH-01; otherwise source-success timestamps/identity and latest-interval semantics are deliberately preserved. ML/retrieval N/A. |
| 12 validation; 20 formats | PASS requirements — retain positive interval/provider/XML/URI guards; no format expansion. Future runtime cases NOT_RUN. |
| 13 auth/privacy; 14 egress | PASS requirements / NOT_VERIFIED runtime — explicit opt-in, existing secure adapters, no payload logging; no provider calls made. |
| 15 transactions; 17 persistence | PASS feasibility / NOT_VERIFIED future code — existing retained source lock and joined ingest support a narrow scheduled guard, explicit PG race/identity obligations. No migration. |
| 16 lifecycle; 18 resources | FINDING PH-01/02 — finite queue/map/caps proposed, but fair service and isolated timer registration must be corrected and later measured. |
| 21 security testing; 22 test quality | FINDING PH-02 missing concrete negative; otherwise meaningful fake transport/PG barriers, no count quota or live smoke. |
| 23 benchmarks; 24 evaluation science | N/A — no benchmark/ML capability requested. Static recurrence diagnostic is design analysis, not runtime acceptance. |
| 25 dependencies; 26 tooling/static diagnostics | PASS no-change scope; existing runner/hooks sufficient. Build/IDE/compiler inspection NOT_RUN/NOT_VERIFIED, no warning-free claim. |
| 27 observability; 28 docs/governance | PASS planned safe logs/evidence labels / FINDING unresolved design claims; current preparation status updated only. |
| 29 hygiene/delivery | PASS static — all 24 existing relative links resolve, six UTF-8 input hashes captured; no production/source/handoff/publication changes. |

- **UPDATED:** this dated review and canonical S2B README disposition/next action only.
- **ALREADY_CONSISTENT:** published S2A at 17f8a359, closeout baseline 47d4d6d, NO_ACTIVE_HANDOFF, bounded maintenance authority, unchanged schema/module/API/tree and historical counters.
- **DEFERRED_OUT_OF_SCOPE:** old dated root/backend/docs AGENTS, backend-implementer B1/B2 notices and Phase-10-only provider deferrals; old S2A prospective note, PH-L01/other LOW backlog and unrelated roadmap/status cleanup. Current S2B scope/Owner policy govern; no historical record is rewritten.
- **BLOCKED_OWNER_DECISION:** process-topology clarification pending; separate scope required if multiple active JVMs are requested.
- **NOT_VERIFIED:** S2B runtime fairness, actual registration/lifecycle/transactions and all S2B-01–12 execution; actual deployment topology and future publication.

No Maven/application/DB test, scheduler/RAG/model/worker activation, live Feed/provider request, dependency/runtime download, hook/trust change or Git staging/publishing occurred. Only the static design model ran under a 60s receipt. The initial metadata print encountered a default-codepage read error after entry hashes were saved; explicit UTF-8 re-read succeeded without altering inputs. Review outputs and final boundary evidence are retained in the unique ignored directory above.

**Next step:** ChatGPT | bounded corrections for PH-01/PH-02 and record the Owner's process-topology answer, then independent Codex `$codex-pre-handoff-review` | DTR26-S2B six-file preparation and this review | resolve preparation blockers before READY_FOR_HANDOFF; ACTIVE stays NO_ACTIVE_HANDOFF | S2B implementation NOT_STARTED, 0/3; takeover NOT_AUTHORIZED.
