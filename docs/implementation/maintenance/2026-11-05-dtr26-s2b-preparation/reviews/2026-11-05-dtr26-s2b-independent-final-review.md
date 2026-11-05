# Independent final review — DTR26-S2B

- **Date / reviewer / mode:** 2026-11-05 / Codex / initial independent implementation review; no submitted production or repository test change authored by this reviewer.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered IMPLEMENTED_AWAITING_CODEX_REVIEW; [executable contract](../executable-handoff-2026-11-05.md), [submitted handback](../implementation-handback-2026-11-05.md), published preparation at main `e296d55db227988b55e86cfb81e83f45e507703a`.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. Four MEDIUM implementation/evidence findings OPEN below. Preparation PH-01/PH-02 remain CLOSED **for design**; runtime proof is evaluated independently here.
- **Counter:** S2B **0/3 correction rounds consumed; next correction/re-review 1/3**. Initial review consumes no correction round. Takeover NOT_AUTHORIZED; S1/S2A history unchanged.
- **Activation:** keep default OFF. Confirmed topology A is not actual deployment conformance; that operational check remains NOT_VERIFIED before unattended activation and is not, by itself, a code-review blocker.

## Findings

| Stable ID | Severity / result |
| --- | --- |
| DTR26-S2B-FR-01 | MEDIUM OPEN — scheduled HN selects 50 stories instead of the contracted 30; duplicated provider flows already diverge. |
| DTR26-S2B-FR-02 | MEDIUM OPEN — cooldown starts at tick cutoff rather than failure/busy completion, shortening the required five-minute delay. |
| DTR26-S2B-FR-03 | MEDIUM OPEN — shutdown can await twice the configured bound; readiness/cleanup ownership does not meet the lifecycle contract. |
| DTR26-S2B-FR-04 | MEDIUM OPEN — mandatory fairness/lifecycle/race/privacy proof and current evidence/operations claims are incomplete or inaccurate. |

### FR-01 — wrong scheduled HN selection and duplicated refresh flow

[HackerNewsRefreshService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshService.java), **lines 161–165**, passes HackerNewsNormalizer.MAX_SELECTED_ITEMS into the new scheduled path. That constant is the accepted **maximum 50**, not the scheduled **default 30** required by the executable limits. The independent current-class probe supplies 60 valid fake top IDs and observes:

- scheduled outcome REFRESHED;
- **50** item-client calls;
- **50** normalized items passed to the fake finalizer;
- contracted scheduled default **30**.

No live request or database publication is claimed by this diagnostic. It proves the new path's actual selection behavior before finalization.

Both HN and RSS scheduled methods copy their existing fetch/parse/normalization/deadline/finalization bodies instead of reusing a shared private provider flow. This contradicts the handoff's no-duplicate-refresh-pipeline rule and has already produced the HN default drift.

**Minimum correction:** use **30 for scheduled HN**, preserving the manual caller's accepted max=50 behavior. Share the relevant provider steps within each existing orchestrator through a narrow private flow/helper or equivalent single ownership; preserve the different manual exception versus scheduled categorical-outcome contracts, post-admission eligibility, limits and finalizer mode. No cross-provider framework/interface or unrelated refactor. Add a >=51-ID fixture proving exactly 30 scheduled HN item calls/inputs and retain manual-limit regressions.

### FR-02 — cooldown is measured from the wrong instant

[FeedScheduledRefreshService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedScheduledRefreshService.java) captures now at **line 72** and uses that same tick-start instant at **line 129**, after the provider returns, to set retryNotBefore=now+5m.

The contract's algorithm requires **current clock + five minutes at the categorized failure/busy outcome**. A logical 70-second failed fake provider attempt produces:

| Observation | Result |
| --- | --- |
| Failure completed | 12:01:10Z |
| Recorded retryNotBefore | 12:05:00Z |
| Remaining delay after failure | **230 seconds**, expected **300** |

The probe advances a controlled clock; it does not wait 70 seconds. A later attempt in the same serial tick may receive an even shorter cooldown.

**Minimum correction:** retain one fixed cutoff for the due query, but capture the current clock at each failure/busy outcome for that source's cooldown. Preserve inclusive expiry, 512 cap/eviction, success removal and NOT_ELIGIBLE no-op behavior. Verify advancing-clock failure and busy outcomes with exact expiry boundaries, without long sleeps.

### FR-03 — lifecycle initialization and termination bounds

[FeedSchedulerLifecycle](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/scheduling/FeedSchedulerLifecycle.java):

1. The default constructor creates and **initializes** the private timer before application readiness; the contract requires initialization/registration once after readiness. Conditional OFF absence is correct, but the ON readiness boundary is not fully honored.
2. **Lines 127–128** call both shutdown() and destroy(). The latter repeats shutdown/termination waiting in the pinned Spring implementation. A nonterminating owned callback can therefore consume **two** await budgets.
3. Stop returns normally even when the future is cancelled but the owned thread has not terminated; cancelled-future/“stopped” flags alone do not establish successful cleanup.

The independent probe uses the actual lifecycle with a real private single-thread timer, a controlled immediate-start seam, a **one-second** await and a fake callback that ignores interruption until explicitly released. It observes **2,002ms** in stop, two timeout warnings, future.isCancelled=true, and **executor.isTerminated=false when stop returns**. After the probe releases its callback, the owned worker terminates. No thread is left behind by the reviewer.

This is a scaled negative control for the failure path: with production's 75-second setting the two waits can reach about **150 seconds**. A real 150-second test was neither needed nor run.

**Minimum correction:** initialize/register at the actual readiness boundary; keep start/stop ownership serialized as required by the handoff; perform one globally bounded termination wait, and distinguish failed termination from a clean stop. Preserve no later dispatch and existing provider deadlines. Use short real-owned-executor cooperative/failed-termination controls, captured callback invocation, readiness/stop races and actual termination assertions. No forcible native-thread termination, global task bean or new executor framework is required.

### FR-04 — finite mandatory proof and truthful current documentation

The green groups do not establish every claimed S2B-01–12 observation. Repairs must remain limited to the already-executable acceptance docket:

| Area | Actual observation / minimum missing proof |
| --- | --- |
| PostgreSQL all-success progress trace | [FeedScheduledRefreshIntegrationTest](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedScheduledRefreshIntegrationTest.java), lines **218–236**, never advances currentTickTime inside the 15-tick loop. The required t=0,60,… success-driven due reentry is not exercised. invokedIds is populated from **expected fixture IDs**, not observed entrypoint calls. Persisted success/cursor assertions are credited, but record actual calls and advance the clock per tick. |
| Five-failure/two-busy trace | In **lines 315–345**, the HN fake fails whenever any designated failing HN source exists, so it fails healthy HN calls too. busySupportedIds is computed but never used to create busy admission/outcomes; the cooldown map is obtained but not populated for those IDs. There is no 30-source observed invocation trace/assertion. Correct per-source outcomes, actually exercise two busy sources, assert all 30 actual entrypoint IDs/outcomes/first pairs and zero unsupported calls. |
| Physical transaction controls | Both scheduled providers' **positive** actual-ingest/source/beforeCommit PID+XID assertions and divergent REQUIRES_NEW observations are credited. The negative controls at [concurrency-test lines 324–350](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedScheduledRefreshConcurrencyIntegrationTest.java) only check divergence; run the **same shared-identity assertion** against each negative control and assert its rejection, as the contract requires. |
| New scheduled authority/compatibility | Credit schedule-OFF-during-fetch rollback for HN/RSS, HN fresh-interval success and cross-provider two-slot/third-BUSY proof. Still supply the scheduled-specific post-selection/manual-completion no-op and manual-versus-scheduled same-source barrier; late schedule-OFF after item staging with existing-item update plus insert rollback; retained-lock/finalization-timeout evidence or precise applicable shared-path reuse. Unchanged enabled/type/RSS URL checks may reuse demonstrated existing evidence with explicit applicability, rather than duplicating every old test. |
| Spring/lifecycle and privacy observations | Current unit tests primarily verify mocks, getters, constants and future cancellation. The real ON/OFF contexts do not run a captured Feed callback with recording fake providers or prove a blocked active tick stops without a subsequent dispatch/owned-thread leak. Supply the contract's controlled callback/real executor/restart/cleanup observations and marker-bearing failures observed in the **new logs**, not only assertions that logger calls or destroy flags exist. No live feed/RAG method invocation is needed. |
| RAG baseline claims | [Operator guide line 48](../../../../../docs/architecture/feed-scheduled-refresh-operations.md) says existing RAG reconciliation is “never scheduled”; the [handback](../implementation-handback-2026-11-05.md) says the private timer completely prevents it running. Yet the current Spring test explicitly observes an annotation processor and a registry containing RagRecoveryService.reconcile alongside the two Modulith Moments tasks. No global TaskScheduler bean is **not** proof of no execution. Document/test **no newly introduced registration/invocation delta**, preserve the existing baseline, and remove the absolute inactivity claim. Do not edit, disable or reopen RAG. |
| Counts and case claims | Receipts actually contain manual/shared **48**, Feed/Inbox/read **78**, HTTP/architecture **79**, not the submitted 44/47/56. The six groups total **236**, not the 178 implied by those submitted numbers. Reconcile receipts and map demonstrated/NOT_VERIFIED observations honestly in an additive dated correction handback; preserve originals. Actual deployment conformance remains separately NOT_VERIFIED. |

The Spring-context observation also corrects the earlier preparation inference: absence of EnableScheduling in repository source did **not** establish an inactive framework baseline. There is no S2B addition to global annotation scheduling in the production diff, and this review does **not** claim that S2B newly activates RAG or that QA/model inference occurred. PH-02's Feed-only programmatic design remains appropriate; the source/registry facts must replace the overstatement.

This finding is missing/incorrect evidence and documentation, not a claim that all the listed production races fail. It requires no new product feature, topology decision, RAG change, schema, dependency, audit or test-count target.

## Independent verification

Fresh ignored directory: `logs/daily-trends-radar/codex-s2b-final-review1-20261105-266b33280225412da654e75770c9d025/`.

| Fresh independent group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| Scheduler unit | 19 | 0 | 7.047 | 0 / 0 / 0 |
| Scheduler PostgreSQL | 9 | 0 | 29.578 | 0 / 0 / 0 |
| Scheduler Spring lifecycle | 3 | 0 | 32.609 | 0 / 0 / 0 |
| Manual/shared refresh | 48 | 0 | 39.109 | 0 / 0 / 0 |
| Feed/Inbox/read compatibility | 78 | 0 | 41.110 | 0 / 0 / 0 |
| HTTP/architecture | 79 | 0 | 64.735 | 0 / 0 / 0 |
| **Total** | **236** | **all 0** | **214.188** | **0 / 0 / 0** |

All six exact handoff selections ran once via scripts/run-bounded-command.py, normal **300s**, offline Maven and the native-access flag, using the already-installed PostgreSQL image sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c. Receipt/stdout and copied per-class Surefire XML agree. No application test was rerun for review/status-only edits.

The separate implementer receipts also reconcile to **236 tests / 226.655s**, all exit 0; these are not additional independent tests. Untouched accepted S1/S2A transport/parser/security/provenance proof remains source-applicable and is not added to the fresh count.

`S2bReviewProbeV2.java` runs the current compiled classes with fake clients/repository/clock/finalizer and one controlled owned timer under a separate **60s** receipt. Its exit 0 records diagnostic observations, **not acceptance PASS**. The first probe failed compilation because its mocked context was typed ApplicationContext instead of the event's ConfigurableApplicationContext; that source/failed receipt is retained, with the corrected V2 written separately. No submitted source/test was repaired.

## Scope, design and shared review coverage

The source/config/doc change map stays within the handoff: five existing Feed Java files, four new production Java files including the scheduling package descriptor, default-OFF YAML, five new tests, permitted navigation/package/operator docs and current handoff/handback. The actual recursive Feed inventory is **118 production Java files / 27 packages / 29 test Java files**, descriptors present, no .gitkeep. No schema/POM/migration/module dependency/root HTTP/auth/RAG change. Generic ingestion, source CRUD, shared admission and existing transport/parser files remain unchanged.

Root ApiResponse, module-local web adapters/advice and internal outcome placement remain consistent with ADR-0016; no common/provider framework is needed. Existing HN/RSS finalizer wrappers retain REQUIRED timeout=5 and the source lock in the ingest transaction. The new scheduled flag is an additional authority condition; generic DB ownership is preserved. Excessive duplicated provider-flow policy is the concrete maintainability problem in FR-01, not a request for more interfaces/patterns.

| Shared dimensions | Disposition / evidence |
| --- | --- |
| 1 intent; 2 traceability | FINDING — FR-01–04; executable limits and several PASS claims do not match behavior/proof. |
| 3 architecture; 4 ownership; 5 tree | PASS scoped — actual 118/27/29 inventory, descriptors and unchanged frozen ownership/schema/module graph. |
| 6 shared contracts; 11 API | PASS demonstrated — root envelope/Feed-local adapters, fresh unchanged 235-operation manifest/manual/read/Inbox compatibility. |
| 7 SOLID; 8 principles; 9 patterns | FINDING FR-01 duplication; otherwise cohesive tick/private timer/outcome and reuse of existing admission/finalizers; no new framework. |
| 10 domain; 19 provenance/data | FINDING FR-01/02; stable-ID query and success-only timestamps credited. Existing URL/provenance/parser behavior reused; ML N/A. |
| 12 validation; 20 formats | PASS demonstrated/reused — due filters and existing positive interval/provider/XML/URI boundaries; no format expansion. RAG/OCR/Office N/A. |
| 13 auth/privacy; 14 egress | PASS unchanged auth/secure adapters / NOT_VERIFIED new marker-observation subset FR-04. No live provider request. |
| 15 transactions; 17 persistence | PASS positive joined-transaction/schedule-OFF/interval evidence; NOT_VERIFIED scheduled-specific remainder/negative-control assertion under FR-04. No H2/schema rewrite. |
| 16 lifecycle; 18 resources | FINDING FR-01/02/03 — selected quota, cooldown timing and doubled cleanup wait; page/map/invocation bounds otherwise demonstrated. |
| 21 security tests; 22 test quality | FINDING FR-04 — green groups contain unobserved/misconfigured acceptance scenarios; preserve credited positives and fix the finite docket. |
| 23 benchmarks; 24 evaluation science | N/A — no throughput/ML project. Short clock/termination probes test existing contracts. |
| 25 dependencies; 26 tooling/static diagnostics | PASS scoped bounded/offline gates; no compiler [WARNING] lines in the fresh summaries. Existing JVM/Mockito notices retained; IDE/static inspection NOT_VERIFIED, no warning-free claim. |
| 27 observability; 28 docs/governance | FINDING FR-04 — false RAG/cleanup/proof/count claims; current independent disposition synchronized. |
| 29 hygiene/delivery | PASS — permitted file map, source/test/initial-history hashes and index boundary; no agent staging/publishing/archive access. |

## Docs Sync Check and bounded remediation

- **UPDATED:** this new review, ACTIVE/executable current disposition/counter/remediation and operator-guide status header only.
- **ALREADY_CONSISTENT:** reviewed/published preparation at e296d55, topology A restriction, default-OFF configuration, actual package inventory, frozen schema/module/API and accepted S1/S2A history.
- **FINDING / Antigravity:** fix FR-01–03 and supply FR-04's bounded proof/accurate guide plus an additive dated correction handback; do not overwrite this report or prior receipts.
- **DEFERRED_OUT_OF_SCOPE:** stale dated AGENTS/agent/policy notices, PH-L01/other LOW backlog, root ZIP and any baseline RAG reconciliation audit. No prior finding is closed or phase reopened here.
- **OWNER_DECISION_RECORDED:** one active JVM; no repeat A/B question. Deployment process/rollout conformance is still an explicit pre-activation gate, not presumed PASS.
- **NOT_VERIFIED:** listed mandatory implementation proof, actual deployment conformance and future publication.

Evidence includes entry hashes/state, tree-and-scope-inspection, supplied/independent summaries, six sets of XML/receipts and diagnostic sources/arguments/results. Submitted input hashes matched before status writes; final boundary verifies source/tests/handback/prior reviews remain unchanged by reviewer. UTF-8/relative links/whitespace/index checks apply to new review/current status outputs.

No production/test fix, real Feed/private-endpoint request, full suite, dependency/runtime/image download, RAG QA/model/inference-worker activation, native hook/trust change, Git staging or publishing was performed. Spring context tests use the existing baseline; no RAG method was directly invoked by the reviewer. The controlled probe releases and terminates its owned worker. Default OFF/activation hold remains.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then mandatory `/antigravity-self-review` and separate Codex `$codex-final-review` | DTR26-S2B FR-01–04 in this review and executable contract | correct bounded defects/proof before acceptance; keep default OFF and historical evidence | 0/3 consumed; next correction/re-review 1/3; takeover NOT_AUTHORIZED.
